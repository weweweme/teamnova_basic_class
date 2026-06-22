package com.example.week10.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week10.App;
import com.example.week10.R;
import com.example.week10.databinding.ActivitySignupBinding;
import com.example.week10.home.HomeActivity;

/// <summary>
/// 회원가입 화면 (가상 계정 시스템)
///
/// ──── 무엇을 하나 ────
/// 계정 아이디 / 별명 / PIN / PIN 확인을 입력받아 새 계정을 만든다.
/// 가입에 성공하면 그 계정으로 바로 로그인된 상태로 HomeActivity에 들어간다.
///
/// ──── 흐름 ────
/// Login → [회원가입] → SignupActivity → (가입 성공) 자동 로그인 → Home
///
/// ──── 누가 데이터를 다루나 ────
/// 실제 저장(account_ids 추가 + user_<id> 파일 생성)은 AccountManager에 맡긴다.
/// 이 화면은 입력값을 검사하고, 통과하면 AccountManager.register()를 호출하는 역할만.
///
/// Unity 비유: 캐릭터 생성 Panel이 입력을 검사한 뒤 SaveManager에
/// "이 이름으로 새 세이브 슬롯 만들어줘"라고 요청하는 것과 같음.
/// </summary>
public class SignupActivity extends AppCompatActivity {

    /// <summary>
    /// 계정 아이디에 허용하는 글자 규칙: 영문 대소문자 / 숫자 / 밑줄(_)만, 1글자 이상
    ///
    /// 아이디는 user_<id> 라는 파일 이름의 일부가 되기 때문에,
    /// 한글·공백·특수문자가 들어가면 파일 이름으로 문제가 될 수 있어 안전한 글자만 허용한다.
    /// (별명은 화면 표시용이라 한글도 자유롭게 가능 — 규칙 적용 안 함)
    /// </summary>
    private static final String ID_PATTERN = "[a-zA-Z0-9_]+";

    /// <summary>
    /// PIN 자릿수 (4자리 고정)
    /// 레이아웃의 maxLength와 같은 값 — 한쪽만 바뀌어 어긋나는 일이 없게 상수로 둠
    /// </summary>
    private static final int PIN_LENGTH = 4;

    /// <summary>
    /// activity_signup.xml의 View들을 모아둔 ViewBinding 묶음
    /// </summary>
    private ActivitySignupBinding binding;

    /// <summary>
    /// 전역 계정 관리자 (App이 보유하는 단 하나의 인스턴스)
    /// 실제 가입 처리를 여기에 맡긴다
    /// </summary>
    private AccountManager accountManager;

    // ========== Lifecycle ==========

    /// <summary>
    /// 회원가입 화면 생성
    /// ViewBinding 연결 → 계정 관리자 확보 → 가입 버튼 리스너 등록
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        accountManager = ((App) getApplication()).getAccountManager();

        binding.buttonSignup.setOnClickListener(v -> onSignupClicked());
    }

    // ========== 가입 처리 ==========

    /// <summary>
    /// 가입하기 버튼 클릭 처리
    /// 입력값을 차례로 검사하고, 모두 통과하면 계정을 만든 뒤 자동 로그인 → Home
    /// </summary>
    private void onSignupClicked() {
        // 입력값 읽기 (앞뒤 공백 제거)
        String id = binding.editTextId.getText().toString().trim();
        String nickname = binding.editTextNickname.getText().toString().trim();
        String pin = binding.editTextPin.getText().toString().trim();
        String pinConfirm = binding.editTextPinConfirm.getText().toString().trim();

        // ──── 입력 검증 (하나라도 걸리면 안내하고 멈춤) ────

        // 아이디: 빈칸 / 형식
        boolean idEmpty = id.isEmpty();
        if (idEmpty) {
            showToast(getString(R.string.signup_id_empty));
            return;
        }
        boolean idValid = id.matches(ID_PATTERN);
        if (!idValid) {
            showToast(getString(R.string.signup_id_invalid));
            return;
        }

        // 별명: 빈칸
        boolean nicknameEmpty = nickname.isEmpty();
        if (nicknameEmpty) {
            showToast(getString(R.string.signup_nickname_empty));
            return;
        }

        // PIN: 정확히 4자리
        boolean pinValid = pin.length() == PIN_LENGTH;
        if (!pinValid) {
            showToast(getString(R.string.signup_pin_invalid));
            return;
        }

        // PIN 확인: 위 PIN과 같은지
        boolean pinMatches = pin.equals(pinConfirm);
        if (!pinMatches) {
            showToast(getString(R.string.signup_pin_mismatch));
            return;
        }

        // ──── 실제 가입 (중복 아이디면 false) ────
        boolean created = accountManager.register(id, nickname, pin);
        if (!created) {
            showToast(getString(R.string.signup_id_taken));
            return;
        }

        // 방금 만든 계정으로 바로 로그인 상태로 만든 뒤 Home 진입
        accountManager.setCurrentAccount(id);
        showToast(getString(R.string.signup_success, nickname));
        goToHome();
    }

    // ========== 화면 이동 ==========

    /// <summary>
    /// 가입 성공 → HomeActivity로 이동
    /// FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK:
    ///   로그인/회원가입 화면을 백스택에서 모두 제거 → 홈에서 뒤로가기로 돌아오지 않음
    /// TODO: Phase 6에서 가입 직후 "계정별 튜토리얼(Onboarding)"을 거쳐 Home으로 가도록 변경
    /// </summary>
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ========== 헬퍼 ==========

    /// <summary>
    /// 짧은 안내 메시지(Toast)를 띄운다
    /// </summary>
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
