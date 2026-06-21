package com.example.week8;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ActivityLibraryBinding;
import com.example.week8.model.Game;
import com.example.week8.model.GameStatus;
import com.example.week8.ui.LibraryAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 라이브러리 화면 (게임 표지 격자 + 상태별 필터 탭)
/// 상단 탭(전체/플레이중/완료/중단/백로그)으로 게임을 골라 격자에 표시
///
/// DiaryActivity(세로 리스트, 드래그 정렬)와 역할이 갈림:
///   DiaryActivity   = 기록 중심 + 드래그로 직접 정렬
///   LibraryActivity = 소장 전체 + 상태별 필터로 골라보기 (Steam 라이브러리 느낌)
///
/// ──── Lifecycle 학습 ────
/// onResume: 다른 화면에서 상태/별점 변경 후 돌아오면 현재 탭 기준으로 다시 필터
/// </summary>
public class LibraryActivity extends AppCompatActivity {

    /// <summary>
    /// 격자에 한 줄당 표시할 열 개수
    /// 값을 3으로 바꾸면 한 줄에 3개씩 배치됨 (★ 테스트: 2 ↔ 3 바꿔보며 관찰)
    /// </summary>
    private static final int GRID_SPAN_COUNT = 2;

    /// <summary>
    /// "전체" 탭의 위치 (0번)
    /// 1번부터는 GameStatus.values() 순서와 1:1 대응 (탭 위치 - 1 = enum 인덱스)
    /// </summary>
    private static final int TAB_POSITION_ALL = 0;

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityLibraryBinding binding;

    /// <summary>
    /// 게임 데이터 저장소 (App 공용 인스턴스)
    /// </summary>
    private GameRepository gameRepository;

    /// <summary>
    /// 격자 셀을 그리는 어댑터
    /// </summary>
    private LibraryAdapter adapter;

    // ========== Lifecycle ==========

    /// <summary>
    /// 라이브러리 화면 생성
    /// ViewBinding 연결 + Repository 접근 + 격자/어댑터 + 필터 탭 세팅
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // App 공용 GameRepository 가져오기 (DiaryActivity와 같은 인스턴스 공유)
        gameRepository = ((App) getApplication()).getGameRepository();

        // RecyclerView 설정 (GridLayoutManager 격자)
        binding.recyclerViewLibrary.setLayoutManager(
                new GridLayoutManager(this, GRID_SPAN_COUNT));
        // 어댑터는 처음 전체 목록으로 생성 (탭 세팅 후 applyFilter로 다시 맞춤)
        adapter = new LibraryAdapter(gameRepository.getAllGames(), this::onGameClick);
        binding.recyclerViewLibrary.setAdapter(adapter);

        setupFilterTabs();
    }

    /// <summary>
    /// 다른 화면에서 상태/데이터 변경 후 돌아왔을 때, 현재 선택된 탭 기준으로 다시 필터
    /// (예: 어떤 게임을 "완료"로 바꾸고 돌아오면 완료 탭 결과가 갱신됨)
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();
        int selected = binding.tabLayoutFilter.getSelectedTabPosition();
        // 화면 최초 진입 시점엔 선택 탭이 없을 수 있어(-1) 전체로 처리
        applyFilter(selected < 0 ? TAB_POSITION_ALL : selected);
    }

    // ========== 필터 탭 ==========

    /// <summary>
    /// 필터 탭 구성: "전체" + GameStatus 4종을 순서대로 추가하고 선택 리스너 등록
    /// 탭 위치와 enum의 대응: 0=전체, 1~4 = GameStatus.values()[위치-1]
    /// </summary>
    private void setupFilterTabs() {
        TabLayout tabLayout = binding.tabLayoutFilter;

        // 0번 탭: 전체
        tabLayout.addTab(tabLayout.newTab().setText(R.string.library_filter_all));
        // 1~4번 탭: 각 상태 (enum 순서대로)
        for (GameStatus status : GameStatus.values()) {
            tabLayout.addTab(tabLayout.newTab().setText(status.getDisplayName()));
        }

        // 탭 선택 시 해당 위치로 필터
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                applyFilter(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 사용 안 함
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 같은 탭 다시 눌러도 동작 동일하게 한 번 더 필터 (무해)
                applyFilter(tab.getPosition());
            }
        });
    }

    /// <summary>
    /// 탭 위치에 따라 게임을 걸러 어댑터에 반영하고, 결과가 비면 빈 상태 안내 표시
    /// </summary>
    private void applyFilter(int tabPosition) {
        List<Game> filtered;

        if (tabPosition == TAB_POSITION_ALL) {
            // 전체: 원본 목록 그대로 (어댑터가 내부에서 복사하므로 그대로 넘겨도 안전)
            filtered = gameRepository.getAllGames();
        } else {
            // 상태별: 탭 위치 - 1 로 enum을 찾아 일치하는 게임만 모음
            GameStatus targetStatus = GameStatus.values()[tabPosition - 1];
            filtered = new ArrayList<>();
            for (Game game : gameRepository.getAllGames()) {
                if (game.getStatus() == targetStatus) {
                    filtered.add(game);
                }
            }
        }

        adapter.updateItems(filtered);

        // 빈 상태 토글: 결과가 없으면 안내 문구, 있으면 격자
        boolean isEmpty = filtered.isEmpty();
        binding.textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewLibrary.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /// <summary>
    /// ActionBar ← 버튼 처리
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ========== 어댑터 콜백 ==========

    /// <summary>
    /// 격자 셀 클릭 콜백 — 클릭된 Game을 GameDetailActivity로 전달
    /// </summary>
    private void onGameClick(Game game) {
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }
}
