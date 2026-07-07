package com.example.week12.data;

import android.content.Context;

import com.example.week12.account.AccountManager;
import com.example.week12.account.UserPrefs;
import com.example.week12.model.Account;
import com.example.week12.model.AccountProfile;
import com.example.week12.model.Game;
import com.example.week12.model.GameReview;
import com.example.week12.model.ReviewFeedItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/// <summary>
/// 커뮤니티(이 기기 안의 여러 계정) 데이터를 모으는 저장소
///
/// ──── 무엇을 하나 ────
/// "서버 없는 미니 커뮤니티"의 재료를 만든다.
/// 한 앱은 이 기기에 저장된 모든 계정 파일(user_<id>)을 읽을 수 있으므로,
/// 각 계정의 공개 정보(별명·아바타·소개·출석)를 모아 목록으로 돌려준다.
///
/// ──── 역할 분리 ────
/// - 계정 목록/별명 → AccountManager
/// - 각 계정의 아바타 색/소개/출석 → 그 계정의 UserPrefs (여기서 계정마다 새로 열어 읽음)
/// 이렇게 "여러 계정을 가로질러 읽는" 일을 한곳(이 클래스)에 모아둔다.
/// (앞으로 랭킹·팔로우 같은 커뮤니티 기능도 여기에 쌓으면 된다)
/// </summary>
public class CommunityRepository {

    /// <summary>
    /// 계정 파일(user_<id>)을 그때그때 열기 위한 앱 컨텍스트
    /// </summary>
    private final Context appContext;

    /// <summary>
    /// 계정 목록/별명을 얻기 위한 전역 계정 관리자
    /// </summary>
    private final AccountManager accountManager;

    /// <summary>
    /// 게임 저장소 — 리뷰의 게임 id로 제목을 찾을 때 사용 (피드에 "게임 제목" 표시)
    /// </summary>
    private final GameRepository gameRepository;

    /// <summary>
    /// 무작위 판정용 난수 생성기 (새로고침 시 확률적으로 새 리뷰를 만들 때 사용)
    /// </summary>
    private final Random random = new Random();

    /// <summary>
    /// 자동 생성 리뷰 문구 후보 (%s = 게임 제목) — 어떤 게임에 붙여도 자연스럽게 읽히는 한 줄들
    /// </summary>
    private static final String[] AUTO_REVIEW_TEMPLATES = {
            "%s 요즘 다시 잡았어요",
            "%s 역시 믿고 하는 게임",
            "%s 생각보다 제 취향이네요",
            "%s 이제야 해봤는데 좋아요",
            "%s 계속 손이 가네요",
            "%s 추천받아 해봤는데 만족"
    };

    /// <summary>
    /// 자동 생성 리뷰의 별점 후보 (0.5 단위, 후하게 3.5~5.0)
    /// </summary>
    private static final float[] AUTO_REVIEW_RATINGS = {3.5f, 4.0f, 4.5f, 5.0f};

    /// <summary>
    /// 저장소 생성 (App이 하나 만들어 공유)
    /// </summary>
    public CommunityRepository(Context context, AccountManager accountManager,
                              GameRepository gameRepository) {
        this.appContext = context.getApplicationContext();
        this.accountManager = accountManager;
        this.gameRepository = gameRepository;
    }

    /// <summary>
    /// 이 기기의 모든 계정을 "공개 프로필" 목록으로 반환 (아이디 순 — getAccounts가 이미 정렬)
    ///
    /// 각 계정마다 그 계정 파일을 열어(UserPrefs) 아바타/소개/출석을 읽어 한 장으로 묶는다.
    /// </summary>
    public List<AccountProfile> getProfiles() {
        List<AccountProfile> profiles = new ArrayList<>();

        for (Account account : accountManager.getAccounts()) {
            String id = account.getId();

            // 그 계정의 개인 설정 파일을 읽기용으로 연다 (현재 로그인 계정이 아니어도 됨)
            UserPrefs prefs = new UserPrefs(appContext, id);

            profiles.add(new AccountProfile(
                    id,
                    account.getNickname(),
                    prefs.getAvatarColor(),
                    prefs.getAvatarImageUrl(),
                    prefs.getBio(),
                    prefs.getStreak(),
                    prefs.getVisitCount(),
                    prefs.getReviewCount()));
        }

        return profiles;
    }

    /// <summary>
    /// 유저 랭킹 — 리뷰를 많이 쓴 순으로 정렬한 프로필 목록 ("가장 활발한 리뷰어")
    /// 리뷰 수가 같으면 연속 출석(streak)이 높은 순으로 다시 정렬
    /// </summary>
    public List<AccountProfile> getRanking() {
        List<AccountProfile> profiles = getProfiles();

        // 리뷰 수 내림차순 → (동점이면) 연속 출석 내림차순
        Comparator<AccountProfile> byReviews =
                Comparator.comparingInt(AccountProfile::getReviewCount).reversed();
        Comparator<AccountProfile> byStreak =
                Comparator.comparingInt(AccountProfile::getStreak).reversed();
        Collections.sort(profiles, byReviews.thenComparing(byStreak));

        return profiles;
    }

    /// <summary>
    /// 특정 게임에 대한 "다른 사람들의 리뷰"를 모아 반환 (게임 상세의 소셜 섹션용)
    ///
    /// 이 기기의 각 계정 파일을 열어, 그 계정이 이 게임에 정식 리뷰를 남겼으면 한 개씩 모은다.
    /// excludeAccountId(보통 지금 로그인한 나)는 목록에서 뺀다 — "다른 사람들"이니까.
    /// </summary>
    /// <param name="gameId">대상 게임 id</param>
    /// <param name="excludeAccountId">제외할 계정(내 것). null이면 아무도 제외 안 함</param>
    public List<GameReview> getReviewsForGame(int gameId, String excludeAccountId) {
        List<GameReview> reviews = new ArrayList<>();

        // 좋아요 "내가 눌렀는지"를 확인할 뷰어(=지금 로그인한 나) 저장소 (한 번만 연다)
        UserPrefs viewerPrefs = (excludeAccountId != null)
                ? new UserPrefs(appContext, excludeAccountId) : null;

        for (Account account : accountManager.getAccounts()) {
            String id = account.getId();
            if (id.equals(excludeAccountId)) {
                continue;
            }

            UserPrefs prefs = new UserPrefs(appContext, id);
            // 그 계정이 이 게임에 정식 리뷰를 남긴 경우만 포함
            if (!prefs.hasReview(gameId)) {
                continue;
            }

            int likeCount = getLikeCount(gameId, id);
            boolean likedByMe = viewerPrefs != null && viewerPrefs.hasLiked(gameId, id);

            reviews.add(new GameReview(
                    account.getNickname(),
                    prefs.getAvatarColor(),
                    prefs.getAvatarImageUrl(),
                    gameId,
                    id,
                    prefs.getRating(gameId),
                    prefs.getReview(gameId),
                    likeCount,
                    likedByMe));
        }

        return reviews;
    }

    /// <summary>
    /// 특정 리뷰(게임 gameId, 작성자 reviewerId)에 눌린 좋아요 개수
    /// 이 기기의 모든 계정을 훑어 그 리뷰에 좋아요를 누른 계정 수를 센다
    /// </summary>
    public int getLikeCount(int gameId, String reviewerId) {
        int count = 0;
        for (Account account : accountManager.getAccounts()) {
            UserPrefs prefs = new UserPrefs(appContext, account.getId());
            if (prefs.hasLiked(gameId, reviewerId)) {
                count++;
            }
        }
        return count;
    }

    /// <summary>
    /// 이 계정을 팔로우한 사람 수(팔로워 수)
    /// 모든 계정을 훑어 accountId를 팔로우(follow_accountId)한 계정을 센다
    /// </summary>
    public int getFollowerCount(String accountId) {
        int count = 0;
        for (Account account : accountManager.getAccounts()) {
            if (account.getId().equals(accountId)) {
                continue;  // 자기 자신은 제외
            }
            UserPrefs prefs = new UserPrefs(appContext, account.getId());
            if (prefs.isFollowing(accountId)) {
                count++;
            }
        }
        return count;
    }

    /// <summary>
    /// 이 계정이 팔로우한 사람 수(팔로잉 수)
    ///
    /// 파일의 follow_ key 개수를 직접 세지 않고, 팔로잉 "목록"의 크기로 센다.
    /// getFollowing이 "지금 존재하는 계정"만 골라 담으므로,
    /// 삭제된 계정을 가리키는 죽은 follow_ key는 자연히 빠져 숫자와 목록이 항상 일치한다.
    /// (예: 팔로우하던 상대가 계정 삭제돼도 내 팔로잉 수가 부풀지 않음)
    /// </summary>
    public int getFollowingCount(String accountId) {
        return getFollowing(accountId).size();
    }

    /// <summary>
    /// 이 계정이 "팔로우한 사람들"의 프로필 목록 (팔로잉 목록)
    /// </summary>
    public List<AccountProfile> getFollowing(String accountId) {
        UserPrefs myPrefs = new UserPrefs(appContext, accountId);
        List<AccountProfile> result = new ArrayList<>();
        for (AccountProfile profile : getProfiles()) {
            if (profile.getId().equals(accountId)) {
                continue;
            }
            if (myPrefs.isFollowing(profile.getId())) {
                result.add(profile);
            }
        }
        return result;
    }

    /// <summary>
    /// 새로 가입한 계정이 기존 계정들 중 무작위로 minCount명 이상을 팔로우하게 한다.
    /// 팔로잉 피드가 처음부터 비어 있지 않도록 하는 시연/테스트 편의 기능.
    /// 후보(나를 뺀 모든 계정 = 주로 테스트 계정)를 섞어 앞에서 몇 명을 골라 팔로우한다.
    /// </summary>
    public void autoFollowRandom(String newAccountId, int minCount) {
        // 후보 = 나를 뺀 모든 계정
        List<AccountProfile> candidates = new ArrayList<>();
        for (AccountProfile profile : getProfiles()) {
            boolean isMyself = profile.getId().equals(newAccountId);
            if (!isMyself) {
                candidates.add(profile);
            }
        }

        // 팔로우할 상대가 아무도 없으면 아무것도 하지 않음
        if (candidates.isEmpty()) {
            return;
        }

        // 무작위로 섞는다 → 앞에서 잘라 쓰면 "무작위 선택"이 됨
        Collections.shuffle(candidates);

        // 팔로우할 인원수: 최소 minCount명, 단 후보가 그보다 적으면 있는 만큼만
        // 후보가 넉넉하면 minCount ~ 후보수 사이의 무작위 인원 (매번 조금씩 다르게)
        int available = candidates.size();
        int floor = Math.min(minCount, available);      // 최소 인원 (후보가 적으면 그만큼 낮춤)
        int extra = available - floor;                  // 더 팔로우할 여지
        int followCount = floor + new Random().nextInt(extra + 1);   // floor ~ available 무작위

        // 새 계정의 개인 파일에 follow_<상대id>를 기록 (팔로잉 관계 저장)
        UserPrefs myPrefs = new UserPrefs(appContext, newAccountId);
        for (int i = 0; i < followCount; i++) {
            myPrefs.setFollowing(candidates.get(i).getId(), true);
        }
    }

    /// <summary>
    /// 확률적으로 "내가 팔로우한 테스트 계정 한 명"이 새 리뷰를 쓰게 한다 (새로고침 연출용).
    /// 팔로잉 피드에 방금 올라온 듯한 새 항목이 생겨 "살아있는 커뮤니티" 느낌을 준다.
    /// chancePercent(0~100)% 확률로 실행하며, 실제로 리뷰를 썼으면 true를 돌려준다.
    /// </summary>
    public boolean maybePostRandomReview(String viewerId, int chancePercent) {
        // 확률 판정 — 통과 못 하면 아무 일도 하지 않음
        boolean rolled = random.nextInt(100) < chancePercent;
        if (!rolled) {
            return false;
        }

        // 후보 = 내가 팔로우한 계정 중 '테스트 계정'만 (피드에 뜨려면 내가 팔로우한 사람이어야 함)
        List<AccountProfile> candidates = new ArrayList<>();
        for (AccountProfile profile : getFollowing(viewerId)) {
            if (TestAccountSeeder.isTestAccount(profile.getId())) {
                candidates.add(profile);
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }

        // 무작위로 한 명 고른다
        AccountProfile poster = candidates.get(random.nextInt(candidates.size()));
        UserPrefs posterPrefs = new UserPrefs(appContext, poster.getId());

        // 그 계정이 아직 리뷰하지 않은 게임들만 후보로 (새 리뷰 = 새 피드 항목)
        List<Integer> alreadyReviewed = posterPrefs.getReviewedGameIds();
        List<Game> notYetReviewed = new ArrayList<>();
        for (Game game : gameRepository.getAllGames()) {
            if (!alreadyReviewed.contains(game.getId())) {
                notYetReviewed.add(game);
            }
        }
        // 이미 모든 게임을 리뷰했다면 더 쓸 게 없음
        if (notYetReviewed.isEmpty()) {
            return false;
        }

        // 무작위 게임 + 무작위 문구 + 무작위 별점으로 리뷰 작성 (작성 시각 = 지금 → 피드 맨 위)
        Game target = notYetReviewed.get(random.nextInt(notYetReviewed.size()));
        String template = AUTO_REVIEW_TEMPLATES[random.nextInt(AUTO_REVIEW_TEMPLATES.length)];
        String text = String.format(template, target.getTitle());
        float rating = AUTO_REVIEW_RATINGS[random.nextInt(AUTO_REVIEW_RATINGS.length)];
        posterPrefs.saveReview(target.getId(), rating, text);
        return true;
    }

    /// <summary>
    /// 한 계정의 공개 프로필 한 장 (유저 프로필 화면용)
    /// </summary>
    public AccountProfile getProfile(String accountId) {
        UserPrefs prefs = new UserPrefs(appContext, accountId);
        return new AccountProfile(
                accountId,
                accountManager.getNickname(accountId),
                prefs.getAvatarColor(),
                prefs.getAvatarImageUrl(),
                prefs.getBio(),
                prefs.getStreak(),
                prefs.getVisitCount(),
                prefs.getReviewCount());
    }

    /// <summary>
    /// 한 계정이 남긴 리뷰들을 최신순으로 반환 (유저 프로필의 "작성한 리뷰" 목록용)
    /// </summary>
    public List<ReviewFeedItem> getUserReviews(String accountId) {
        UserPrefs prefs = new UserPrefs(appContext, accountId);
        String nickname = accountManager.getNickname(accountId);
        int avatarColor = prefs.getAvatarColor();

        // 좋아요 "내가 눌렀는지"는 지금 로그인한 나(뷰어) 기준으로 판단
        String viewerId = accountManager.getCurrentAccountId();
        UserPrefs viewerPrefs = viewerId != null ? new UserPrefs(appContext, viewerId) : null;

        List<ReviewFeedItem> list = new ArrayList<>();
        for (int gameId : prefs.getReviewedGameIds()) {
            Game game = gameRepository.findById(gameId);
            if (game == null) {
                continue;
            }
            boolean likedByMe = viewerPrefs != null && viewerPrefs.hasLiked(gameId, accountId);
            list.add(new ReviewFeedItem(
                    nickname,
                    avatarColor,
                    prefs.getAvatarImageUrl(),
                    gameId,
                    game.getTitle(),
                    prefs.getRating(gameId),
                    prefs.getReview(gameId),
                    prefs.getReviewedAt(gameId),
                    accountId,                          // 작성자 id
                    getLikeCount(gameId, accountId),    // 이 리뷰의 총 좋아요 수
                    likedByMe));                        // 내가 눌렀는지
        }
        Collections.sort(list, Comparator.comparingLong(ReviewFeedItem::getTimestamp).reversed());
        return list;
    }

    /// <summary>
    /// 팔로잉 피드 — 이 계정이 팔로우한 사람들이 남긴 리뷰만 작성 시각 최신순으로 모아 반환
    ///
    /// 내가 팔로우한 계정만 골라, 그들이 리뷰한 게임마다 (작성자·게임제목·별점·한줄평·시각) 항목을 만든다.
    /// 삭제된 게임(제목을 못 찾는 경우)은 건너뛴다.
    /// </summary>
    public List<ReviewFeedItem> getFollowingFeed(String accountId) {
        UserPrefs myPrefs = new UserPrefs(appContext, accountId);
        List<ReviewFeedItem> feed = new ArrayList<>();

        for (Account account : accountManager.getAccounts()) {
            String id = account.getId();
            // 내가 팔로우한 계정만 (나 자신·팔로우 안 한 계정은 제외)
            if (id.equals(accountId) || !myPrefs.isFollowing(id)) {
                continue;
            }

            UserPrefs prefs = new UserPrefs(appContext, id);
            for (int gameId : prefs.getReviewedGameIds()) {
                Game game = gameRepository.findById(gameId);
                if (game == null) {
                    continue;
                }
                feed.add(new ReviewFeedItem(
                        account.getNickname(),
                        prefs.getAvatarColor(),
                        prefs.getAvatarImageUrl(),
                        gameId,
                        game.getTitle(),
                        prefs.getRating(gameId),
                        prefs.getReview(gameId),
                        prefs.getReviewedAt(gameId),
                        id,                                 // 작성자 id
                        getLikeCount(gameId, id),           // 이 리뷰의 총 좋아요 수
                        myPrefs.hasLiked(gameId, id)));     // 내가 눌렀는지
            }
        }

        // 작성 시각 내림차순 (최신 리뷰가 위로)
        Collections.sort(feed, Comparator.comparingLong(ReviewFeedItem::getTimestamp).reversed());
        return feed;
    }

    /// <summary>
    /// 이 계정을 "팔로우한 사람들"의 프로필 목록 (팔로워 목록)
    /// </summary>
    public List<AccountProfile> getFollowers(String accountId) {
        List<AccountProfile> result = new ArrayList<>();
        for (AccountProfile profile : getProfiles()) {
            if (profile.getId().equals(accountId)) {
                continue;
            }
            UserPrefs theirPrefs = new UserPrefs(appContext, profile.getId());
            if (theirPrefs.isFollowing(accountId)) {
                result.add(profile);
            }
        }
        return result;
    }

}
