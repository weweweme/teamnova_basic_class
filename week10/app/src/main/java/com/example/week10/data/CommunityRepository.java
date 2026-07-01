package com.example.week10.data;

import android.content.Context;

import com.example.week10.account.AccountManager;
import com.example.week10.account.UserPrefs;
import com.example.week10.model.Account;
import com.example.week10.model.AccountProfile;
import com.example.week10.model.Game;
import com.example.week10.model.GameReview;
import com.example.week10.model.ReviewFeedItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    /// 한 계정의 공개 프로필 한 장 (유저 프로필 화면용)
    /// </summary>
    public AccountProfile getProfile(String accountId) {
        UserPrefs prefs = new UserPrefs(appContext, accountId);
        return new AccountProfile(
                accountId,
                accountManager.getNickname(accountId),
                prefs.getAvatarColor(),
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

        List<ReviewFeedItem> list = new ArrayList<>();
        for (int gameId : prefs.getReviewedGameIds()) {
            Game game = gameRepository.findById(gameId);
            if (game == null) {
                continue;
            }
            list.add(new ReviewFeedItem(
                    nickname,
                    avatarColor,
                    gameId,
                    game.getTitle(),
                    prefs.getRating(gameId),
                    prefs.getReview(gameId),
                    prefs.getReviewedAt(gameId)));
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
                        gameId,
                        game.getTitle(),
                        prefs.getRating(gameId),
                        prefs.getReview(gameId),
                        prefs.getReviewedAt(gameId)));
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
