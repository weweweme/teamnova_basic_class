package com.example.week12.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;

import com.example.week12.App;
import com.example.week12.R;
import com.example.week12.databinding.ActivityLoginBinding;
import com.example.week12.data.TestAccountSeeder;
import com.example.week12.main.MainActivity;
import com.example.week12.intro.OnboardingActivity;
import com.example.week12.model.Account;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 로그인 화면 (가상 계정 시스템 입구)
///
/// ──── 무엇을 하나 ────
/// 가입된 계정을 드롭다운(Spinner)으로 고르고, PIN을 입력해 로그인한다.
/// 서버 없이 SharedPreferences(=PlayerPrefs)에 저장된 PIN과 비교하는 "흉내내기 로그인".
///
/// ──── 흐름 ────
/// Splash → LoginActivity → (PIN 맞으면) MainActivity(하단 탭)
///
/// ──── 누가 데이터를 다루나 ────
/// 모든 검증/세션 처리는 AccountManager(App이 보유하는 단일 인스턴스)에 맡긴다.
/// 이 화면은 "입력을 받아 AccountManager에 물어보고, 결과에 따라 화면을 바꾸는" 역할만.
///
/// Unity 비유: 로그인 Panel(UI)이 AccountManager(매니저 싱글톤)에게
/// "이 PIN 맞아?" 물어보고, 맞으면 다음 Scene을 LoadSceneMode.Single로 여는 것과 같음.
/// </summary>
public class LoginActivity extends AppCompatActivity {

    /// <summary>
    /// 카카오 계정을 우리 계정 시스템에 넣을 때 id 앞에 붙이는 접두사 (예: kakao_1234567890)
    /// "이 계정이 카카오 계정인지" 판단할 때도 이 접두사를 쓴다 (MainActivity 로그아웃 등)
    /// (public static: 다른 화면이 LoginActivity.KAKAO_ACCOUNT_PREFIX로 참조)
    /// </summary>
    public static final String KAKAO_ACCOUNT_PREFIX = "kakao_";

    /// <summary>
    /// activity_login.xml의 View들을 모아둔 ViewBinding 묶음
    /// (binding.spinnerAccount, binding.editTextPin, binding.buttonLogin ...)
    /// </summary>
    private ActivityLoginBinding binding;

    /// <summary>
    /// 전역 계정 관리자 (App이 보유하는 단 하나의 인스턴스)
    /// 로그인 검증·세션 세팅을 여기에 맡긴다
    /// </summary>
    private AccountManager accountManager;

    /// <summary>
    /// 현재 Spinner에 표시 중인 계정 목록
    /// Spinner는 "몇 번째 항목을 골랐는지"(위치 번호)만 알려주므로,
    /// 그 번호로 실제 계정(id 포함)을 찾으려면 같은 순서의 목록을 들고 있어야 한다.
    /// </summary>
    private final List<Account> accounts = new ArrayList<>();

    // ========== Lifecycle ==========

    /// <summary>
    /// 로그인 화면 생성
    /// ViewBinding 연결 → 계정 관리자 확보 → (임시) 테스트 계정 심기 → 목록 채우기 → 버튼 리스너 등록
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // App에서 공용 계정 관리자 꺼내오기 (모든 화면이 같은 인스턴스를 공유)
        accountManager = ((App) getApplication()).getAccountManager();

        // 가입된 계정을 Spinner에 채우고, 계정 유무에 따라 화면 모양을 맞춘다
        loadAccountsIntoSpinner();

        // "로그인 유지" 체크박스를 저장된 설정값으로 맞춰둔다 (지난번 선택을 기억)
        binding.checkBoxKeepLogin.setChecked(accountManager.isAutoLogin());

        // 버튼 리스너 등록 (Unity의 Button.onClick.AddListener와 같은 개념)
        binding.buttonLogin.setOnClickListener(v -> onLoginClicked());
        binding.buttonSignup.setOnClickListener(v -> onSignupClicked());
        binding.buttonKakaoLogin.setOnClickListener(v -> onKakaoLoginClicked());
    }

    // ========== 카카오 로그인 ==========

    /// <summary>
    /// "카카오로 로그인" 버튼 클릭 → 카카오계정으로 로그인 (웹 로그인)
    ///
    /// 카카오톡 앱이 없는 기기(에뮬레이터 등)에서도 되도록 loginWithKakaoAccount(웹)로 통일한다.
    /// (실서비스에선 카톡 앱 설치 시 loginWithKakaoTalk을 먼저 쓰는 패턴도 있음)
    /// 성공하면 토큰을 받고, 이어서 내 프로필(닉네임)을 조회한다.
    /// </summary>
    private void onKakaoLoginClicked() {
        UserApiClient.getInstance().loginWithKakaoAccount(this, (token, error) -> {
            if (error != null) {
                showToast("카카오 로그인 실패");
            } else if (token != null) {
                fetchKakaoUserAndProceed();
            }
            // 이 콜백은 코틀린 함수 타입(반환형 Unit)이라, 자바에선 Unit.INSTANCE를 돌려줘야 한다
            // (카카오 SDK가 코틀린으로 만들어져서 생기는 한 줄 — 신경 쓸 건 위 if/else뿐)
            return Unit.INSTANCE;
        });
    }

    /// <summary>
    /// 카카오 로그인 성공 후, 내 정보(카카오 유저 ID + 닉네임)를 조회해 우리 계정에 연결
    /// </summary>
    private void fetchKakaoUserAndProceed() {
        UserApiClient.getInstance().me((user, error) -> {
            boolean failed = error != null || user == null || user.getId() == null;
            if (failed) {
                showToast("카카오 정보 조회 실패");
                return Unit.INSTANCE;
            }
            long kakaoId = user.getId();
            String nickname = extractNickname(user);
            String imageUrl = extractImageUrl(user);
            linkKakaoAndProceed(kakaoId, nickname, imageUrl);
            return Unit.INSTANCE;
        });
    }

    /// <summary>
    /// 카카오 유저 객체에서 닉네임을 안전하게 꺼낸다 (동의 안 했거나 없으면 기본값)
    /// 카카오 프로필은 동의 여부에 따라 중간 객체가 null일 수 있어 단계마다 확인한다
    /// </summary>
    private String extractNickname(User user) {
        String fallback = "카카오사용자";
        boolean hasProfile = user.getKakaoAccount() != null
                && user.getKakaoAccount().getProfile() != null;
        if (!hasProfile) {
            return fallback;
        }
        String nickname = user.getKakaoAccount().getProfile().getNickname();
        boolean hasNickname = nickname != null && !nickname.isEmpty();
        return hasNickname ? nickname : fallback;
    }

    /// <summary>
    /// 카카오 유저에서 프로필 사진 주소를 안전하게 꺼낸다 (없으면 빈 문자열 → 색깔 원 아바타 유지)
    /// </summary>
    private String extractImageUrl(User user) {
        boolean hasProfile = user.getKakaoAccount() != null
                && user.getKakaoAccount().getProfile() != null;
        if (!hasProfile) {
            return "";
        }
        String url = user.getKakaoAccount().getProfile().getProfileImageUrl();
        return url != null ? url : "";
    }

    /// <summary>
    /// 카카오 유저를 우리 가상 계정 시스템에 연결하고, 기존 로그인 흐름(튜토리얼/홈)으로 진입
    ///
    /// 계정 id = "kakao_" + 카카오유저ID (영문/숫자/밑줄이라 형식 안전).
    /// 없으면 PIN 없이 등록(카카오 버튼으로만 로그인 — PIN 폼으로는 못 들어옴),
    /// 있으면 닉네임만 최신으로 갱신. 이렇게 가상 계정과 카카오 계정이 병존한다.
    /// </summary>
    private void linkKakaoAndProceed(long kakaoId, String nickname, String imageUrl) {
        String accountId = KAKAO_ACCOUNT_PREFIX + kakaoId;

        if (!accountManager.isRegistered(accountId)) {
            accountManager.register(accountId, nickname, "");
        } else {
            accountManager.updateNickname(accountId, nickname);
        }

        accountManager.setCurrentAccount(accountId);
        // 로그인 유지 체크 상태를 그대로 반영 (카카오도 동일하게 기억)
        accountManager.setAutoLogin(binding.checkBoxKeepLogin.isChecked());

        // 카카오 프로필 사진 주소를 이 계정의 UserPrefs에 저장 (현재 계정이 정해진 뒤라야 올바른 파일에 씀)
        // → 홈/프로필 아바타가 색깔 원 대신 이 사진을 보여줌 (있을 때만)
        ((App) getApplication()).getUserPrefs().setAvatarImageUrl(imageUrl);

        showToast(getString(R.string.login_success, nickname));
        proceedAfterAuth();
    }

    // ========== 계정 목록 표시 ==========

    /// <summary>
    /// 가입된 계정들을 Spinner에 채운다
    ///
    /// 계정이 하나도 없으면 입력 UI(드롭다운/PIN/로그인 버튼)를 숨기고 안내 문구를 보여준다.
    /// (없는 계정으로 로그인 시도를 할 수 없으니 입력칸 자체를 가린다)
    /// </summary>
    private void loadAccountsIntoSpinner() {
        accounts.clear();
        accounts.addAll(accountManager.getAccounts());

        // Spinner에는 "계정 아이디"를 보여준다 — 로그인할 때 고르는 식별자이기 때문
        // (별명은 로그인 후 화면 표시용이라 여기엔 안 씀)
        // 계정이 없으면 목록이 비어 Spinner도 비지만, 로그인 폼은 항상 그대로 보여준다
        // 테스트 계정은 뒤에 "(test)"를 붙여 구분 (표시만 바꿀 뿐, 로그인은 실제 id로 함)
        List<String> accountIds = new ArrayList<>();
        for (Account account : accounts) {
            String display = account.getId();
            if (TestAccountSeeder.isTestAccount(account.getId())) {
                display = display + " (test)";
            }
            accountIds.add(display);
        }

        // ArrayAdapter: 문자열 목록 ↔ Spinner 항목을 이어주는 다리
        // android.R.layout.simple_spinner_item: 안드로이드 기본 제공 한 줄짜리 항목 레이아웃
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                accountIds);
        // 드롭다운을 펼쳤을 때 각 줄의 모양 (역시 기본 제공 레이아웃)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAccount.setAdapter(adapter);
    }

    // ========== 버튼 동작 ==========

    /// <summary>
    /// 로그인 버튼 클릭 처리
    /// 선택된 계정 + 입력 PIN을 AccountManager에 넘겨 검증하고, 결과에 따라 화면을 처리
    /// </summary>
    private void onLoginClicked() {
        // 선택할 계정이 하나도 없으면(첫 실행 등) 회원가입을 안내하고 끝
        // (목록이 비어 있을 때 아래 accounts.get(...)을 호출하면 오류가 나므로 먼저 막음)
        if (accounts.isEmpty()) {
            showToast(getString(R.string.login_need_signup));
            return;
        }

        // 입력 PIN 읽기 (앞뒤 공백 제거)
        String pin = binding.editTextPin.getText().toString().trim();

        // PIN이 비어 있으면 검증할 것도 없이 안내만
        boolean pinEmpty = pin.isEmpty();
        if (pinEmpty) {
            showToast(getString(R.string.login_pin_empty));
            return;
        }

        // Spinner가 고른 위치 번호로 실제 계정을 찾는다
        int selectedPosition = binding.spinnerAccount.getSelectedItemPosition();
        Account selected = accounts.get(selectedPosition);

        // 검증 + 세션 세팅을 한 번에 (성공 시 내부에서 current_account가 지정됨)
        boolean success = accountManager.login(selected.getId(), pin);

        if (success) {
            // 체크 상태를 저장 → 켜져 있으면 다음 앱 실행 때 Splash가 로그인을 건너뛰고 바로 홈으로 보냄
            accountManager.setAutoLogin(binding.checkBoxKeepLogin.isChecked());
            showToast(getString(R.string.login_success, selected.getNickname()));
            proceedAfterAuth();
        } else {
            showToast(getString(R.string.login_failed));
        }
    }

    /// <summary>
    /// 회원가입 버튼 클릭 처리 → 회원가입 화면으로 이동
    /// SignupActivity는 같은 account 패키지라 import 없이 바로 참조 가능
    /// (가입에 성공하면 SignupActivity가 자동 로그인 후 Home으로 보내므로
    ///  이 로그인 화면으로 되돌아오지 않는다)
    /// </summary>
    private void onSignupClicked() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    // ========== 화면 이동 ==========

    /// <summary>
    /// 로그인 성공 후 다음 화면 결정
    ///   이 계정이 튜토리얼을 본 적 없으면 → Onboarding(튜토리얼)
    ///   이미 봤으면 → Home
    /// (튜토리얼은 계정마다 따로 본다 → 같은 폰이라도 새 계정으로 로그인하면 다시 봄)
    ///
    /// FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK:
    ///   로그인 화면을 백스택에서 제거 → 다음 화면에서 뒤로가기로 로그인에 안 돌아옴
    /// </summary>
    private void proceedAfterAuth() {
        App app = (App) getApplication();

        // 로그인 직후라 현재 계정이 정해져 있어 getUserPrefs()는 항상 그 계정 것을 돌려줌
        UserPrefs userPrefs = app.getUserPrefs();
        boolean seenTutorial = userPrefs.hasSeenTutorial();

        Class<?> target = seenTutorial ? MainActivity.class : OnboardingActivity.class;
        Intent intent = new Intent(this, target);

        // setFlags: 이 화면을 "어떻게" 띄울지 옵션을 건다
        //   FLAG_ACTIVITY_CLEAR_TASK : 지금까지 쌓여 있던 화면들(Splash·Login 등)을 전부 비움
        //   FLAG_ACTIVITY_NEW_TASK   : 비운 자리에 이 화면을 새 출발점으로 띄움
        //                              (CLEAR_TASK는 NEW_TASK와 함께 써야 동작함)
        //   → 결과: 이전 화면이 모두 사라져, 다음 화면에서 뒤로가기를 눌러도 로그인으로 안 돌아옴
        //   (화면 쌓임을 "책 위에 책 얹기"라 하면, 이건 쌓인 책을 다 치우고 새 책 한 권만 올리는 것)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ========== 헬퍼 ==========

    /// <summary>
    /// 짧은 안내 메시지(Toast)를 띄운다
    /// 같은 패턴이 여러 번 반복되므로 한 곳으로 묶음
    /// </summary>
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
