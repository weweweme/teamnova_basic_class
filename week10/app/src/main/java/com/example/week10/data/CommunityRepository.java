package com.example.week10.data;

import android.content.Context;

import com.example.week10.account.AccountManager;
import com.example.week10.account.UserPrefs;
import com.example.week10.model.Account;
import com.example.week10.model.AccountProfile;
import com.example.week10.model.GameReview;

import java.util.ArrayList;
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
    /// 저장소 생성 (App이 하나 만들어 공유)
    /// </summary>
    public CommunityRepository(Context context, AccountManager accountManager) {
        this.appContext = context.getApplicationContext();
        this.accountManager = accountManager;
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
                    prefs.getVisitCount()));
        }

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

            reviews.add(new GameReview(
                    account.getNickname(),
                    prefs.getAvatarColor(),
                    prefs.getRating(gameId),
                    prefs.getReview(gameId)));
        }

        return reviews;
    }
}
