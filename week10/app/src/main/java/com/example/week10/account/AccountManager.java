package com.example.week10.account;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/// <summary>
/// 전역 계정 관리자 — "app_global" 파일 담당
///
/// ──── 무엇을 하나 ────
/// 계정 시스템 전체에서 "계정과 무관한 앱 전체 정보"만 책임진다.
///   - 가입된 계정이 누구누구인지 (account_ids)
///   - 지금 로그인된 계정이 누구인지 (current_account)
///   - 로그인 유지 켜져있는지 (auto_login)
///
/// ──── SharedPreferences = PlayerPrefs ────
/// Unity의 PlayerPrefs와 같은 key-value 저장소인데, 이름을 붙여 여러 개 만들 수 있다.
/// 여기서는 "app_global"이라는 이름의 파일 하나를 연다.
/// (계정별 개인 정보는 별도 파일 "user_<id>"에 저장하며, Phase 6의 UserPrefs가 담당)
///
/// ──── 누가 보유하나 ────
/// App(Application)이 단 하나만 만들어 보유 → 모든 Activity가 공유한다.
///   사용처: ((App) getApplication()).getAccountManager()
///
/// 현재(Phase 1)는 app_global 값을 읽고 쓰는 기본 기능만 갖춘 뼈대.
/// 로그인 검증(Phase 2)·회원가입(Phase 3)·로그아웃/삭제(Phase 4)는 이 위에 쌓는다.
/// </summary>
public class AccountManager {

    /// <summary>
    /// 전역 정보를 저장할 SharedPreferences 파일 이름
    /// 이 이름으로 열면 항상 같은 파일에 접근한다 (PlayerPrefs의 "전역 슬롯")
    /// </summary>
    private static final String FILE_GLOBAL = "app_global";

    /// <summary>
    /// key: 가입된 계정 아이디 목록 (문자열 여러 개를 담는 StringSet)
    /// 예: {"alice", "bob"}
    /// </summary>
    private static final String KEY_ACCOUNT_IDS = "account_ids";

    /// <summary>
    /// key: 지금 로그인된 계정 아이디 (한 명)
    /// 값이 없으면(=key가 비어있으면) 아무도 로그인하지 않은 상태
    /// </summary>
    private static final String KEY_CURRENT_ACCOUNT = "current_account";

    /// <summary>
    /// key: "로그인 유지" 켜짐 여부 (true면 다음 실행 시 자동 로그인)
    /// </summary>
    private static final String KEY_AUTO_LOGIN = "auto_login";

    /// <summary>
    /// 실제 저장소 핸들 — "app_global" 파일을 가리킨다
    /// 한 번 열어두고 계속 재사용 (매번 다시 열 필요 없음)
    /// </summary>
    private final SharedPreferences globalPrefs;

    /// <summary>
    /// 관리자 생성 — app_global 파일을 연다
    /// MODE_PRIVATE: 이 앱만 읽고 쓸 수 있는 비공개 파일 (다른 앱 접근 불가)
    /// </summary>
    /// <param name="context">파일을 열기 위한 안드로이드 컨텍스트 (App에서 넘겨줌)</param>
    public AccountManager(Context context) {
        // getSharedPreferences(이름, 모드): 그 이름의 PlayerPrefs를 연다(없으면 새로 만듦)
        this.globalPrefs = context.getSharedPreferences(FILE_GLOBAL, Context.MODE_PRIVATE);
    }

    // ========== 계정 목록 (account_ids) ==========

    /// <summary>
    /// 가입된 계정 아이디 목록을 반환
    ///
    /// 주의: getStringSet이 돌려주는 Set은 "직접 수정하면 안 되는" 원본이다.
    /// (안드로이드 문서가 명시: 반환된 Set을 고치면 저장값이 깨질 수 있음)
    /// 그래서 새 HashSet으로 복사해 안전한 사본을 돌려준다.
    /// 두 번째 인자: 저장된 값이 없을 때 돌려줄 기본값 → 빈 집합
    /// </summary>
    public Set<String> getAccountIds() {
        Set<String> stored = globalPrefs.getStringSet(KEY_ACCOUNT_IDS, new HashSet<>());
        return new HashSet<>(stored);
    }

    /// <summary>
    /// 해당 아이디가 이미 가입되어 있는지 확인 (회원가입 중복 체크 등에 사용)
    /// </summary>
    public boolean isRegistered(String id) {
        return getAccountIds().contains(id);
    }

    // ========== 현재 로그인 계정 (current_account) ==========

    /// <summary>
    /// 지금 로그인된 계정이 있는지 확인 (Tester) — 값을 꺼내기 전에 존재부터 확인
    /// contains: 그 key가 파일에 들어있는지
    /// </summary>
    public boolean hasCurrentAccount() {
        return globalPrefs.contains(KEY_CURRENT_ACCOUNT);
    }

    /// <summary>
    /// 지금 로그인된 계정 아이디를 반환 (Doer)
    /// 로그인 안 된 상태면 null이 나올 수 있으니, 보통 hasCurrentAccount()로 먼저 확인하고 호출
    /// </summary>
    public String getCurrentAccountId() {
        return globalPrefs.getString(KEY_CURRENT_ACCOUNT, null);
    }

    /// <summary>
    /// 현재 로그인 계정을 지정 (로그인 성공 시 호출)
    ///
    /// edit() → 값 변경 → apply() 가 SharedPreferences 쓰기 3단계.
    /// apply(): 메모리에는 즉시 반영하고 디스크 저장은 백그라운드에서 처리(비동기) → 화면 안 끊김.
    /// (commit()도 있지만 디스크까지 동기로 기다려 UI가 멈출 수 있어 apply를 쓴다)
    /// </summary>
    public void setCurrentAccount(String id) {
        globalPrefs.edit()
                .putString(KEY_CURRENT_ACCOUNT, id)
                .apply();
    }

    /// <summary>
    /// 현재 로그인 계정을 비운다 (로그아웃 시 호출)
    /// remove: 그 key 자체를 삭제 → 이후 hasCurrentAccount()가 false가 됨
    /// </summary>
    public void clearCurrentAccount() {
        globalPrefs.edit()
                .remove(KEY_CURRENT_ACCOUNT)
                .apply();
    }

    // ========== 로그인 유지 (auto_login) ==========

    /// <summary>
    /// "로그인 유지"가 켜져 있는지 반환 (기본값: 꺼짐)
    /// </summary>
    public boolean isAutoLogin() {
        return globalPrefs.getBoolean(KEY_AUTO_LOGIN, false);
    }

    /// <summary>
    /// "로그인 유지" 켜짐/꺼짐을 저장 (로그인 화면의 체크박스와 연동 예정)
    /// </summary>
    public void setAutoLogin(boolean enabled) {
        globalPrefs.edit()
                .putBoolean(KEY_AUTO_LOGIN, enabled)
                .apply();
    }
}
