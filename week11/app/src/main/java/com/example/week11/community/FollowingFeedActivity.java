package com.example.week11.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week11.App;
import com.example.week11.databinding.ActivityFollowingFeedBinding;
import com.example.week11.detail.GameDetailActivity;
import com.example.week11.model.Game;
import com.example.week11.model.ReviewFeedItem;

import java.util.List;

/// <summary>
/// 팔로잉 피드 화면 (커뮤니티)
///
/// ──── 무엇을 하나 ────
/// 지금 로그인한 계정이 "팔로우한 사람들"이 남긴 리뷰만 최신순으로 보여준다.
/// 항목을 누르면 그 게임 상세로 이동.
/// </summary>
public class FollowingFeedActivity extends AppCompatActivity {

    /// <summary>
    /// activity_following_feed.xml의 View 묶음
    /// </summary>
    private ActivityFollowingFeedBinding binding;

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성 — 팔로우한 사람들의 리뷰를 최신순으로 채운다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFollowingFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        App app = (App) getApplication();
        String currentId = app.getAccountManager().getCurrentAccountId();
        List<ReviewFeedItem> feed = app.getCommunityRepository().getFollowingFeed(currentId);

        boolean isEmpty = feed.isEmpty();
        binding.textViewFollowingFeedEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerFollowingFeed.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        binding.recyclerFollowingFeed.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFollowingFeed.setAdapter(new ReviewFeedAdapter(feed, this::openGame));
    }

    /// <summary>
    /// 피드 항목 클릭 → 그 게임 상세로 이동
    /// 피드는 게임 id만 알므로 저장소에서 Game을 찾아 Parcelable로 넘긴다
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
