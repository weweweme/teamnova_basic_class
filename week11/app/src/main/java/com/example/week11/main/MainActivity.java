package com.example.week11.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.account.AccountManager;
import com.example.week11.account.LoginActivity;
import com.example.week11.community.CommunityFragment;
import com.example.week11.databinding.ActivityMainBinding;
import com.example.week11.home.HomeFragment;

/// <summary>
/// 하단 탭 컨테이너 (앱의 새 홈 베이스)
/// 로그인 이후 이 화면으로 들어와, 아래 탭으로 '내 일지'와 '커뮤니티'를 오간다
///
/// ──── 구조 ────
/// activity_main.xml = 위쪽 프래그먼트 칸(mainFragmentContainer) + 아래 탭 바(bottomNav)
/// 탭을 누르면 칸의 프래그먼트만 갈아 끼움 → "화면 전환"이 아니라 "패널 교체"
///
/// 계정 관리 메뉴(로그아웃/계정삭제/초기화)와 "뒤로 두 번 눌러 종료"는
/// 탭과 무관하게 앱 전체에 걸리는 동작이라, 탭 안(Fragment)이 아니라 이 컨테이너가 담당한다.
///
/// Unity 비유: 하나의 Scene(Canvas)에서 탭에 따라 UI 패널만 Show/Hide 하는 것
/// </summary>
public class MainActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding — activity_main.xml의 뷰(bottomNav 등) 참조
    /// </summary>
    private ActivityMainBinding binding;

    /// <summary>
    /// 전역 계정 관리자 (로그아웃 / 계정 삭제 메뉴 처리에 사용)
    /// </summary>
    private AccountManager accountManager;

    /// <summary>
    /// '내 일지' 탭 프래그먼트 (한 번 만들어 재사용 → 탭 오갈 때마다 새로 만들지 않음)
    /// </summary>
    private final Fragment homeFragment = new HomeFragment();

    /// <summary>
    /// '커뮤니티' 탭 프래그먼트 (한 번 만들어 재사용)
    /// </summary>
    private final Fragment communityFragment = new CommunityFragment();

    /// <summary>
    /// "뒤로 두 번 눌러 종료"의 대기 시간(ms) — 이 시간 안에 다시 누르면 종료
    /// </summary>
    private static final long BACK_EXIT_WINDOW_MS = 2000L;

    /// <summary>
    /// 뒤로가기를 한 번 눌러 "종료 대기" 상태인지 여부
    /// </summary>
    private boolean backReadyToExit = false;

    /// <summary>
    /// "뒤로 두 번 눌러 종료" 타이머용 Handler (일정 시간 뒤 대기 상태 해제)
    /// </summary>
    private final Handler backHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 대기 시간이 지나면 "종료 대기" 상태를 해제하는 작업 (2초 안에 다시 안 누르면 처음으로)
    /// </summary>
    private final Runnable resetBackRunnable = () -> backReadyToExit = false;

    /// <summary>
    /// 화면 생성 — 탭 바를 연결하고, 최초에는 '내 일지' 탭을 보여준다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        accountManager = ((App) getApplication()).getAccountManager();

        // 화면이 처음 만들어질 때만 기본 탭(내 일지)을 세팅
        // (savedInstanceState != null = 회전 등으로 재생성 → 프래그먼트는 시스템이 복원하므로 다시 안 넣음)
        if (savedInstanceState == null) {
            showFragment(homeFragment);
            binding.bottomNav.setSelectedItemId(R.id.tab_diary);
        }

        // 탭 선택 리스너: 어떤 탭이 눌렸는지 id로 구분해 해당 프래그먼트로 교체
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.tab_diary) {
                showFragment(homeFragment);
                return true;    // 이 항목을 '선택됨'으로 처리
            }
            if (id == R.id.tab_community) {
                showFragment(communityFragment);
                return true;
            }
            return false;
        });

        setupBackToExit();
    }

    /// <summary>
    /// 프래그먼트 칸의 내용을 주어진 프래그먼트로 교체
    /// </summary>
    private void showFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, fragment)
                .commit();
    }

    // ========== "뒤로 두 번 눌러 종료" ==========

    /// <summary>
    /// "뒤로 두 번 눌러 종료" 처리 등록
    /// - 첫 번째 뒤로가기: 안내 토스트 + 2초 타이머 예약 (아직 종료 안 함)
    /// - 2초 안에 두 번째 뒤로가기: 앱 종료
    /// (탭 컨테이너가 앱의 첫 화면이라, 실수로 뒤로가기 한 번에 꺼지는 걸 막는 흔한 폴리시)
    /// </summary>
    private void setupBackToExit() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 두 번째(대기 중) → 종료
                if (backReadyToExit) {
                    finish();
                    return;
                }

                // 첫 번째 → 대기 상태로 만들고 안내, 2초 뒤 자동 해제 예약
                backReadyToExit = true;
                Toast.makeText(MainActivity.this, R.string.home_back_to_exit,
                        Toast.LENGTH_SHORT).show();
                backHandler.postDelayed(resetBackRunnable, BACK_EXIT_WINDOW_MS);
            }
        });
    }

    /// <summary>
    /// 화면이 사라질 때 예약해 둔 Handler 작업을 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        backHandler.removeCallbacksAndMessages(null);
    }

    // ========== ActionBar 메뉴 (⋮ 로그아웃 / 계정 삭제 / 초기화) ==========

    /// <summary>
    /// ActionBar에 menu_home.xml을 띄운다 (로그아웃/계정삭제/초기화)
    /// </summary>
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /// <summary>
    /// 메뉴 항목 선택 처리 — 단일 값(항목 id)으로 여러 분기
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        if (id == R.id.action_delete_account) {
            confirmDeleteAccount();
            return true;
        }
        if (id == R.id.action_reset_all) {
            confirmResetAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /// <summary>
    /// 전체 초기화 확인 다이얼로그 (테스트/시연용)
    /// 확인 시 모든 계정·prefs를 비우고 로그인 화면으로 → "새 설치" 상태
    /// </summary>
    private void confirmResetAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_confirm_title)
                .setMessage(R.string.reset_confirm_message)
                .setPositiveButton(R.string.reset_confirm_ok, (dialog, which) -> {
                    // App이 초기화 + 테스트 계정 재심기까지 처리 (테스트 계정은 안 사라짐)
                    ((App) getApplication()).resetAllData();
                    goToLogin();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /// <summary>
    /// 로그아웃 확인 다이얼로그 → 확인 시 세션을 비우고 로그인 화면으로
    /// </summary>
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.logout_confirm_ok, (dialog, which) -> {
                    accountManager.logout();
                    goToLogin();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /// <summary>
    /// 계정 삭제 확인 다이얼로그 → 확인 시 현재 계정을 통째로 지우고 로그인 화면으로
    /// 되돌릴 수 없는 동작이라 별명을 메시지에 넣어 "어떤 계정을 지우는지" 분명히 보여준다
    /// </summary>
    private void confirmDeleteAccount() {
        // 현재 로그인 계정이 없으면(비정상) 아무것도 하지 않음
        if (!accountManager.hasCurrentAccount()) {
            return;
        }
        String currentId = accountManager.getCurrentAccountId();
        String nickname = accountManager.getNickname(currentId);

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account_confirm_title)
                .setMessage(getString(R.string.delete_account_confirm_message, nickname))
                .setPositiveButton(R.string.delete_account_confirm_ok, (dialog, which) -> {
                    accountManager.deleteAccount(currentId);
                    goToLogin();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /// <summary>
    /// 로그인 화면으로 이동 (로그아웃/계정 삭제 후 공통)
    /// FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK:
    ///   기존 화면을 모두 제거 → 뒤로가기로 로그인된 화면에 못 돌아옴
    /// </summary>
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
