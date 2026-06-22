package com.example.week10.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week10.App;
import com.example.week10.R;
import com.example.week10.databinding.ActivityLoginBinding;
import com.example.week10.home.HomeActivity;
import com.example.week10.model.Account;

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
/// Splash → LoginActivity → (PIN 맞으면) HomeActivity
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

        // 버튼 리스너 등록 (Unity의 Button.onClick.AddListener와 같은 개념)
        binding.buttonLogin.setOnClickListener(v -> onLoginClicked());
        binding.buttonSignup.setOnClickListener(v -> onSignupClicked());
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

        boolean hasAccounts = !accounts.isEmpty();

        // 계정 유무에 따라 입력 UI / 안내 문구를 켜고 끈다
        // View.VISIBLE: 보임, View.GONE: 숨김(자리도 차지 안 함)
        int inputVisibility = hasAccounts ? View.VISIBLE : View.GONE;
        binding.spinnerAccount.setVisibility(inputVisibility);
        binding.editTextPin.setVisibility(inputVisibility);
        binding.buttonLogin.setVisibility(inputVisibility);
        binding.textViewNoAccounts.setVisibility(hasAccounts ? View.GONE : View.VISIBLE);

        if (!hasAccounts) {
            return;
        }

        // Spinner에는 별명만 보여준다 (id는 내부 식별용이라 화면에 노출하지 않음)
        List<String> nicknames = new ArrayList<>();
        for (Account account : accounts) {
            nicknames.add(account.getNickname());
        }

        // ArrayAdapter: 문자열 목록 ↔ Spinner 항목을 이어주는 다리
        // android.R.layout.simple_spinner_item: 안드로이드 기본 제공 한 줄짜리 항목 레이아웃
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nicknames);
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
            showToast(getString(R.string.login_success, selected.getNickname()));
            goToHome();
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
    /// 로그인 성공 → HomeActivity로 이동
    /// FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK:
    ///   로그인 화면을 백스택에서 제거 → 홈에서 뒤로가기 눌러도 로그인으로 안 돌아옴
    ///   (Unity에서 로그인 Scene을 LoadSceneMode.Single로 날리는 것과 동일)
    /// </summary>
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
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
