package com.example.week10.account;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.week10.model.Account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    /// 계정별 파일 이름 앞에 붙이는 접두사
    /// 예: id가 "alice"면 파일 이름은 "user_alice"가 됨 (계정마다 별도 PlayerPrefs 슬롯)
    /// </summary>
    private static final String FILE_USER_PREFIX = "user_";

    /// <summary>
    /// (user_<id> 파일) key: 화면에 보여줄 별명
    /// </summary>
    private static final String KEY_NICKNAME = "nickname";

    /// <summary>
    /// (user_<id> 파일) key: 로그인용 PIN
    /// 주의: 학습용 가상 계정이라 암호화 없이 평문 저장 — 실제 앱에서는 절대 이렇게 하면 안 됨
    /// </summary>
    private static final String KEY_PIN = "pin";

    /// <summary>
    /// 실제 저장소 핸들 — "app_global" 파일을 가리킨다
    /// 한 번 열어두고 계속 재사용 (매번 다시 열 필요 없음)
    /// </summary>
    private final SharedPreferences globalPrefs;

    /// <summary>
    /// 계정별 파일(user_<id>)을 그때그때 열기 위해 보관하는 컨텍스트
    /// 계정마다 파일 이름이 달라(user_alice, user_bob...) globalPrefs처럼 미리 하나만 열어둘 수 없음
    /// → 필요할 때 이 컨텍스트로 해당 계정 파일을 연다
    /// getApplicationContext: Activity는 화면이 닫히면 사라지므로, 앱 전체 수명과 같은 컨텍스트를 보관
    /// </summary>
    private final Context appContext;

    /// <summary>
    /// 관리자 생성 — app_global 파일을 연다
    /// MODE_PRIVATE: 이 앱만 읽고 쓸 수 있는 비공개 파일 (다른 앱 접근 불가)
    /// </summary>
    /// <param name="context">파일을 열기 위한 안드로이드 컨텍스트 (App에서 넘겨줌)</param>
    public AccountManager(Context context) {
        this.appContext = context.getApplicationContext();
        // getSharedPreferences(이름, 모드): 그 이름의 PlayerPrefs를 연다(없으면 새로 만듦)
        this.globalPrefs = appContext.getSharedPreferences(FILE_GLOBAL, Context.MODE_PRIVATE);
    }

    /// <summary>
    /// 해당 계정의 전용 파일(user_<id>)을 연다
    /// 자격증명(별명/PIN)을 읽고 쓰는 다른 메서드들이 공통으로 사용
    /// </summary>
    private SharedPreferences openUserPrefs(String id) {
        return appContext.getSharedPreferences(FILE_USER_PREFIX + id, Context.MODE_PRIVATE);
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

    // ========== 자격증명 (user_<id> 파일의 별명/PIN) ==========

    /// <summary>
    /// 해당 계정의 별명을 반환 (로그인 화면 목록 표시용)
    /// 저장된 별명이 없으면 id를 그대로 돌려줌 (이름표가 비는 일이 없도록)
    /// </summary>
    public String getNickname(String id) {
        return openUserPrefs(id).getString(KEY_NICKNAME, id);
    }

    /// <summary>
    /// 별명을 새 값으로 바꾼다 (프로필 편집에서 사용)
    /// 별명은 AccountManager가 소유한 key(KEY_NICKNAME)라 여기서 수정한다
    /// (id/PIN은 그대로 두고 표시용 별명만 변경)
    /// </summary>
    public void updateNickname(String id, String nickname) {
        openUserPrefs(id).edit()
                .putString(KEY_NICKNAME, nickname)
                .apply();
    }

    /// <summary>
    /// 가입된 모든 계정을 Account 객체 목록으로 반환 (id 순서로 정렬)
    ///
    /// getAccountIds()는 순서가 들쭉날쭉한 Set이라, 화면에 매번 같은 순서로
    /// 보여주려면 List로 옮겨 정렬해야 한다. (Spinner 항목 순서가 흔들리면 사용자가 헷갈림)
    /// </summary>
    public List<Account> getAccounts() {
        List<String> ids = new ArrayList<>(getAccountIds());
        // id 기준 오름차순 정렬 → 화면에 항상 같은 순서로 표시
        Collections.sort(ids);

        List<Account> accounts = new ArrayList<>();
        for (String id : ids) {
            accounts.add(new Account(id, getNickname(id)));
        }
        return accounts;
    }

    /// <summary>
    /// 입력한 PIN이 해당 계정의 저장된 PIN과 같은지 확인
    /// 저장된 PIN이 아예 없으면(=비정상 상태) 무조건 실패 처리
    /// </summary>
    public boolean verifyPin(String id, String inputPin) {
        String savedPin = openUserPrefs(id).getString(KEY_PIN, null);
        boolean hasPin = savedPin != null;
        boolean matches = hasPin && savedPin.equals(inputPin);
        return matches;
    }

    /// <summary>
    /// 로그인 시도: PIN이 맞으면 현재 로그인 계정으로 지정하고 true, 틀리면 아무것도 하지 않고 false
    /// (검증 + current_account 세팅을 한 번에 — 화면 쪽 코드가 단순해짐)
    /// </summary>
    public boolean login(String id, String pin) {
        if (!verifyPin(id, pin)) {
            return false;
        }
        setCurrentAccount(id);
        return true;
    }

    // ========== 회원가입 ==========

    /// <summary>
    /// 새 계정을 만든다
    ///   ① 전역 목록(account_ids)에 id 추가
    ///   ② 그 계정 전용 파일(user_<id>)에 별명/PIN 기록 → 이 시점에 새 파일이 생김
    /// 이미 있는 아이디면 아무것도 하지 않고 false를 돌려준다 (덮어쓰기 방지).
    ///
    /// 빈칸·형식·PIN 일치 같은 입력 검증은 화면(SignupActivity)에서 미리 끝낸다고 보고,
    /// 여기서는 "중복 여부"만 마지막 안전장치로 확인한다.
    /// </summary>
    public boolean register(String id, String nickname, String pin) {
        if (isRegistered(id)) {
            return false;
        }

        // ① 전역 계정 목록에 추가
        Set<String> ids = getAccountIds();
        ids.add(id);
        globalPrefs.edit()
                .putStringSet(KEY_ACCOUNT_IDS, ids)
                .apply();

        // ② 계정 전용 파일에 별명/PIN 기록 (없던 파일이면 이때 새로 만들어짐)
        openUserPrefs(id).edit()
                .putString(KEY_NICKNAME, nickname)
                .putString(KEY_PIN, pin)
                .apply();

        return true;
    }

    // ========== 로그아웃 / 계정 삭제 ==========

    /// <summary>
    /// 로그아웃: 현재 로그인 계정을 비우고, "로그인 유지"도 끈다
    ///
    /// 로그인 유지까지 끄는 이유: 끄지 않으면 다음 실행 때 자동 로그인이 켜져 있어
    /// 방금 로그아웃한 계정으로 다시 들어가 버린다 (로그아웃한 의도와 어긋남).
    /// 계정 데이터(user_<id> 파일)는 그대로 둔다 — 다시 로그인하면 기록이 남아 있음.
    /// </summary>
    public void logout() {
        setAutoLogin(false);
        clearCurrentAccount();
    }

    /// <summary>
    /// 계정 삭제: 그 계정의 전용 파일을 통째로 지우고 전역 목록에서도 뺀다 (되돌릴 수 없음)
    ///   ① user_<id> 파일 삭제 → 별명/PIN/프로필 등 그 계정의 모든 저장값이 사라짐
    ///   ② account_ids 목록에서 제거 → 로그인 화면 드롭다운에서 사라짐
    ///   ③ 지운 계정이 지금 로그인 중이던 계정이면 세션도 비움(로그아웃)
    /// </summary>
    public void deleteAccount(String id) {
        // ① 계정 전용 파일 통째 삭제
        // deleteSharedPreferences: 그 이름의 SharedPreferences 파일 자체를 지움 (API 24+)
        // minSdk 33이라 항상 사용 가능. 그 파일을 들고 있는 참조가 없어야 안전한데,
        // openUserPrefs는 매번 새로 열어 쓰고 보관하지 않으므로 문제 없음.
        appContext.deleteSharedPreferences(FILE_USER_PREFIX + id);

        // ② 전역 계정 목록에서 제거
        Set<String> ids = getAccountIds();
        ids.remove(id);
        globalPrefs.edit()
                .putStringSet(KEY_ACCOUNT_IDS, ids)
                .apply();

        // ③ 지운 계정이 현재 로그인 계정이면 세션 비우기
        // getCurrentAccountId()가 null이어도 id.equals(null)은 false라 안전
        boolean wasCurrent = id.equals(getCurrentAccountId());
        if (wasCurrent) {
            logout();
        }
    }
}
