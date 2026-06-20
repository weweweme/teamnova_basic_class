package com.example.week8;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ActivityLibraryBinding;
import com.example.week8.model.Game;
import com.example.week8.ui.LibraryAdapter;

/// <summary>
/// 라이브러리 화면 (게임 표지 격자 보기)
/// GameRepository의 게임 목록을 GridLayoutManager로 2열 격자에 표지 위주로 표시
///
/// DiaryActivity(세로 리스트)와 같은 데이터를 다른 형태로 보여주는 화면:
///   DiaryActivity   = LinearLayoutManager (한 줄에 하나, 상세 정보)
///   LibraryActivity = GridLayoutManager (한 줄에 여러 개, 표지 위주)
/// → 같은 RecyclerView라도 LayoutManager만 바꾸면 배치가 완전히 달라짐을 보여주는 예시
///
/// ──── Lifecycle 학습 ────
/// onResume: 다른 화면에서 별점/리뷰 변경 후 돌아오면 표지/제목 갱신 반영
/// </summary>
public class LibraryActivity extends AppCompatActivity {

    /// <summary>
    /// 격자에 한 줄당 표시할 열 개수
    /// 값을 3으로 바꾸면 한 줄에 3개씩 배치됨 (★ 테스트: 2 ↔ 3 바꿔보며 관찰)
    /// </summary>
    private static final int GRID_SPAN_COUNT = 2;

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
    /// ViewBinding 연결 + 공용 Repository 접근 + GridLayoutManager/어댑터 세팅
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

        // RecyclerView 설정
        // LayoutManager만 GridLayoutManager로 두면 어댑터/뷰홀더는 그대로 두고 격자 배치
        // GRID_SPAN_COUNT 값이 한 줄당 열 개수
        binding.recyclerViewLibrary.setLayoutManager(
                new GridLayoutManager(this, GRID_SPAN_COUNT));
        adapter = new LibraryAdapter(gameRepository.getAllGames(), this::onGameClick);
        binding.recyclerViewLibrary.setAdapter(adapter);
    }

    /// <summary>
    /// 다른 화면에서 데이터 변경 후 돌아왔을 때 격자 갱신
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
    /// DiaryActivity.onGameClick과 동일한 동작 (같은 OnGameClickListener 계약)
    /// </summary>
    private void onGameClick(Game game) {
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }
}
