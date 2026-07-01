package com.example.week10.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week10.App;
import com.example.week10.databinding.ActivityReviewFeedBinding;
import com.example.week10.detail.GameDetailActivity;
import com.example.week10.model.Game;
import com.example.week10.model.ReviewFeedItem;

import java.util.List;

/// <summary>
/// 커뮤니티 "최근 리뷰" 전체 피드 화면 (홈의 "더 보기")
///
/// ──── 무엇을 하나 ────
/// 이 기기의 모든 계정이 남긴 리뷰를 최신순으로 전부 보여준다.
/// 항목을 누르면 그 게임 상세로 이동.
/// </summary>
public class ReviewFeedActivity extends AppCompatActivity {

    /// <summary>
    /// activity_review_feed.xml의 View 묶음
    /// </summary>
    private ActivityReviewFeedBinding binding;

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성 — 최근 리뷰 전체를 목록에 채운다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityReviewFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        List<ReviewFeedItem> feed = ((App) getApplication()).getCommunityRepository().getRecentReviews();

        boolean isEmpty = feed.isEmpty();
        binding.textViewFeedEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerReviewFeed.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        binding.recyclerReviewFeed.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReviewFeed.setAdapter(new ReviewFeedAdapter(feed, this::openGame));
    }

    /// <summary>
    /// 피드 항목 클릭 → 그 게임 상세로 이동
    /// 피드는 게임 id만 알고 있으므로, 저장소에서 Game을 찾아 Parcelable로 넘긴다
    /// </summary>
    private void openGame(int gameId) {
        Game game = ((App) getApplication()).getGameRepository().findById(gameId);
        if (game == null) {
            return;
        }
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }

    /// <summary>
    /// ActionBar ← 버튼 → 화면 닫기 (홈으로 복귀)
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
