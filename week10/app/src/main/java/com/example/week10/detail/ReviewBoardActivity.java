package com.example.week10.detail;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week10.App;
import com.example.week10.databinding.ActivityReviewBoardBinding;
import com.example.week10.model.GameReview;

import java.util.List;

/// <summary>
/// 리뷰 게시판 화면 (게임 상세의 "더 보기")
///
/// ──── 무엇을 하나 ────
/// 특정 게임에 대한 "다른 사람들의 평가"를 전체 목록(게시판)으로 보여준다.
/// 게임 상세에는 미리보기 몇 개만 보이고, "더 보기"로 여기 들어오면 전부 볼 수 있다.
/// (내 평가는 제외 — 게임 상세 위쪽 "내 평가"에 따로 있음)
/// </summary>
public class ReviewBoardActivity extends AppCompatActivity {

    /// <summary>입력: 어느 게임의 리뷰인지 (게임 id)</summary>
    public static final String EXTRA_GAME_ID = "extra_game_id";

    /// <summary>입력: 화면 상단에 표시할 게임 제목</summary>
    public static final String EXTRA_GAME_TITLE = "extra_game_title";

    /// <summary>
    /// activity_review_board.xml의 View 묶음
    /// </summary>
    private ActivityReviewBoardBinding binding;

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성 — 이 게임의 다른 사람들 리뷰 전체를 목록에 채운다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityReviewBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int gameId = getIntent().getIntExtra(EXTRA_GAME_ID, -1);
        String gameTitle = getIntent().getStringExtra(EXTRA_GAME_TITLE);
        binding.textViewBoardGameTitle.setText(gameTitle);

        App app = (App) getApplication();
        String currentId = app.getAccountManager().getCurrentAccountId();
        // 나를 뺀 다른 계정들의 이 게임 리뷰 전체
        List<GameReview> reviews = app.getCommunityRepository().getReviewsForGame(gameId, currentId);

        boolean isEmpty = reviews.isEmpty();
        binding.textViewBoardEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerBoard.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        binding.recyclerBoard.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerBoard.setAdapter(new GameReviewAdapter(reviews, app.getUserPrefs()));
    }

    /// <summary>
    /// ActionBar ← 버튼 → 화면 닫기 (게임 상세로 복귀)
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
