package com.example.week11.community;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    /// <summary>
    /// 피드 집계 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// 계정마다 파일을 읽는 무거운 집계는 서브 스레드에서, 목록 표시만 메인에 넘긴다
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

        binding.recyclerFollowingFeed.setLayoutManager(new LinearLayoutManager(this));
        // 당겨서 새로고침 → 피드 다시 로드 (SwipeRefreshLayout이 자체 스피너를 보여줌)
        binding.swipeFollowingFeed.setOnRefreshListener(() -> loadFeed(true));

        loadFeed(false);   // 최초 로딩 (가운데 스피너)
    }

    /// <summary>
    /// 팔로우한 사람들의 리뷰를 백그라운드로 모아 목록에 채운다
    /// isRefresh=false(최초): 가운데 스피너 / true(당겨서 새로고침): SwipeRefreshLayout 자체 스피너
    /// </summary>
    private void loadFeed(boolean isRefresh) {
        App app = (App) getApplication();
        String currentId = app.getAccountManager().getCurrentAccountId();

        if (!isRefresh) {
            // 최초 로딩만 가운데 스피너 표시 (새로고침은 위쪽 자체 스피너로 충분)
            binding.progressFollowingFeed.setVisibility(View.VISIBLE);
            binding.recyclerFollowingFeed.setVisibility(View.GONE);
            binding.textViewFollowingFeedEmpty.setVisibility(View.GONE);
        }

        // 팔로우한 사람들의 리뷰 모으기는 계정마다 파일을 읽는 디스크 작업 → 서브 스레드에서
        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 스피너가 보이게 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // ⏳ 무거운 일: 팔로우한 계정들의 리뷰를 모아 최신순 정렬 (디스크 읽기)
            List<ReviewFeedItem> feed = app.getCommunityRepository().getFollowingFeed(currentId);

            // 결과 반영만 메인 줄로
            mainHandler.post(() -> {
                binding.progressFollowingFeed.setVisibility(View.GONE);
                binding.swipeFollowingFeed.setRefreshing(false);   // 새로고침 스피너 끄기

                boolean isEmpty = feed.isEmpty();
                binding.textViewFollowingFeedEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.recyclerFollowingFeed.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                binding.recyclerFollowingFeed.setAdapter(new ReviewFeedAdapter(feed, this::openGame));
            });
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 메인 큐에 남아있는 피드 반영 작업을 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
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
