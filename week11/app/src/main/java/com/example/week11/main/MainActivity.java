package com.example.week11.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.week11.R;
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
/// Unity 비유: 하나의 Scene(Canvas)에서 탭에 따라 UI 패널만 Show/Hide 하는 것.
/// 매번 Scene을 새로 로드(Activity 새로 띄우기)하지 않아 빠르고 상태 유지가 쉬움
/// </summary>
public class MainActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding — activity_main.xml의 뷰(bottomNav 등) 참조
    /// </summary>
    private ActivityMainBinding binding;

    /// <summary>
    /// '내 일지' 탭 프래그먼트 (한 번 만들어 재사용 → 탭 오갈 때마다 새로 만들지 않음)
    /// </summary>
    private final Fragment homeFragment = new HomeFragment();

    /// <summary>
    /// '커뮤니티' 탭 프래그먼트 (한 번 만들어 재사용)
    /// </summary>
    private final Fragment communityFragment = new CommunityFragment();

    /// <summary>
    /// 화면 생성 — 탭 바를 연결하고, 최초에는 '내 일지' 탭을 보여준다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 화면이 처음 만들어질 때만 기본 탭(내 일지)을 세팅
        // (savedInstanceState != null = 회전 등으로 재생성 → 프래그먼트는 시스템이 복원하므로 다시 안 넣음)
        if (savedInstanceState == null) {
            showFragment(homeFragment);
            binding.bottomNav.setSelectedItemId(R.id.tab_diary);
        }

        // 탭 선택 리스너: 어떤 탭이 눌렸는지 id로 구분해 해당 프래그먼트로 교체
        // 단일 값(항목 id)으로 여러 분기 → switch 대신 if로도 무방하나 여기선 명확히 id 비교
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
    }

    /// <summary>
    /// 프래그먼트 칸의 내용을 주어진 프래그먼트로 교체
    /// replace: 칸에 있던 프래그먼트를 치우고 새 것으로 바꿔 끼움
    /// </summary>
    private void showFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, fragment)
                .commit();
    }
}
