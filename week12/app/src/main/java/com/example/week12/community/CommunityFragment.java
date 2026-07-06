package com.example.week12.community;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.App;
import com.example.week12.R;
import com.example.week12.databinding.FragmentCommunityBinding;
import com.example.week12.detail.GameDetailActivity;
import com.example.week12.model.Game;
import com.example.week12.model.ReviewFeedItem;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// '커뮤니티' 탭 화면 (소셜 — 감초 역할)
///
/// ──── 무엇을 하나 ────
/// - 메인: 팔로잉 피드 (내가 팔로우한 사람들의 리뷰만 최신순) — 항목 탭 → 게임 상세, 하트 → 좋아요
/// - 상단 '🏆 랭킹' 버튼 → 유저 랭킹 화면(RankingActivity)
/// - 실시간 피드: 이 탭을 보는 동안 8초마다 새 글을 확인(Handler 폴링)
///     · 맨 위를 보고 있으면 → 새 리뷰를 목록 맨 위에 애니메이션으로 바로 끼워넣음
///     · 아래를 읽는 중이면 → 방해하지 않고 "새 리뷰 N개 ↑" pill만 띄움 (탭하면 맨 위로)
///
/// ──── 핸들러 학습 포인트 ────
/// pollRunnable이 postDelayed로 "자기 자신을 다시 예약"해 8초 주기로 반복 실행된다.
/// 화면이 보일 때만(onResume) 돌리고 안 보이면(onPause) 멈춘다 → 배터리·누수 방지.
/// </summary>
public class CommunityFragment extends Fragment {

    /// <summary>ViewBinding — 뷰 사라질 때 null로 비움</summary>
    private FragmentCommunityBinding binding;

    /// <summary>지금 화면에 걸려 있는 피드 어댑터 (새 리뷰를 맨 위에 끼워넣을 때 사용)</summary>
    private ReviewFeedAdapter feedAdapter;

    /// <summary>백그라운드 집계 결과를 메인 스레드에 반영할 때 쓰는 Handler</summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// <summary>실시간 피드 폴링(8초 주기) 스케줄용 Handler</summary>
    private final Handler pollHandler = new Handler(Looper.getMainLooper());

    /// <summary>새 글 확인 주기(ms) — 8초마다 폴링</summary>
    private static final long POLL_INTERVAL_MS = 8000L;

    /// <summary>폴링 때 팔로우 중인 테스트 계정이 새 리뷰를 쓸 확률(%)</summary>
    private static final int AUTO_POST_CHANCE_PERCENT = 40;

    /// <summary>
    /// 지금 화면에 보여주고 있는 피드 맨 위(가장 최신) 리뷰의 작성 시각
    /// 폴링으로 받은 피드에 이 시각보다 새로운 항목이 있으면 "새 글"로 본다
    /// </summary>
    private long newestShownTimestamp = 0L;

    /// <summary>
    /// 아직 화면에 안 끼워넣은 새 리뷰들 (아래로 스크롤 중이라 pill로만 알린 것) — 최신순
    /// pill을 누르면 이걸 맨 위에 한꺼번에 끼워넣는다
    /// </summary>
    private final List<ReviewFeedItem> pendingFresh = new ArrayList<>();

    /// <summary>8초마다 새 글을 확인하고 자기 자신을 다시 예약하는 반복 작업</summary>
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            pollForNewReviews();
            pollHandler.postDelayed(this, POLL_INTERVAL_MS);   // 다음 확인 예약(자기 재예약)
        }
    };

    // ========== 생명주기 ==========

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerCommunityFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        // 기본 삽입 애니메이션(미세한 페이드)은 끈다 → 각 항목에서 직접 "슬라이드+페이드" 등장을 재생
        binding.recyclerCommunityFeed.setItemAnimator(null);
        // 당겨서 새로고침 → 피드 다시 로드
        binding.swipeCommunityFeed.setOnRefreshListener(() -> loadFeed(true));

        // 🏆 랭킹 버튼 → 유저 랭킹 화면
        binding.buttonRanking.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RankingActivity.class)));

        // "새 리뷰 N개 ↑" pill → 밀려있던 새 리뷰를 끼워넣고 맨 위로 스크롤
        binding.buttonNewReviews.setOnClickListener(v -> applyPendingFresh());

        loadFeed(false);   // 최초 로딩 (가운데 스피너)
    }

    /// <summary>이 탭이 보이기 시작하면 실시간 폴링을 켠다 (8초 주기)</summary>
    @Override
    public void onResume() {
        super.onResume();
        startPolling();
    }

    /// <summary>이 탭이 가려지면 폴링을 멈춘다</summary>
    @Override
    public void onPause() {
        super.onPause();
        stopPolling();
    }

    /// <summary>뷰가 사라질 때 예약된 모든 Handler 작업 취소 + binding 해제 (누수 방지)</summary>
    @Override
    public void onDestroyView() {
        stopPolling();
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
    }

    // ========== 실시간 폴링 (Handler 주기 실행) ==========

    private void startPolling() {
        pollHandler.removeCallbacks(pollRunnable);
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void stopPolling() {
        pollHandler.removeCallbacks(pollRunnable);
    }

    /// <summary>
    /// 새 글 확인 1회: (확률적으로) 팔로우 중인 테스트 계정이 새 리뷰를 쓰게 하고,
    /// 최신 피드를 백그라운드로 다시 모아 새 글이 있는지 판단한다
    /// </summary>
    private void pollForNewReviews() {
        App app = (App) requireActivity().getApplication();
        String currentId = app.getAccountManager().getCurrentAccountId();

        new Thread(() -> {
            app.getCommunityRepository().maybePostRandomReview(currentId, AUTO_POST_CHANCE_PERCENT);
            List<ReviewFeedItem> feed = app.getCommunityRepository().getFollowingFeed(currentId);
            mainHandler.post(() -> onPolled(feed));
        }).start();
    }

    /// <summary>
    /// 폴링 결과 반영 — 새 리뷰가 있으면 맨 위에 끼워넣거나(맨 위를 보는 중) pill로 알린다(아래 읽는 중)
    /// </summary>
    private void onPolled(List<ReviewFeedItem> feed) {
        if (binding == null) {
            return;
        }

        // 아직 아무것도 안 보이는 상태면 그냥 채운다 (방해할 게 없음)
        boolean nothingShownYet = newestShownTimestamp == 0L || feedAdapter == null;
        if (nothingShownYet) {
            renderFeed(feed);
            return;
        }

        // 지금 보여주는 것보다 새로운(시각이 더 큰) 항목만 추린다
        // 피드는 최신순 정렬이라 새 글은 앞쪽에 모여 있음 → 오래된 게 나오면 멈춤
        List<ReviewFeedItem> fresh = new ArrayList<>();
        for (ReviewFeedItem item : feed) {
            if (item.getTimestamp() > newestShownTimestamp) {
                fresh.add(item);
            } else {
                break;
            }
        }
        if (fresh.isEmpty()) {
            return;
        }

        // 기준 시각을 최신으로 갱신 (다음 폴링에서 같은 항목을 또 "새 글"로 세지 않도록)
        newestShownTimestamp = fresh.get(0).getTimestamp();

        if (isAtTop() && pendingFresh.isEmpty()) {
            // 맨 위를 보고 있으면 pill 없이 바로 끼워넣어 자동 갱신 (위에서 슬라이드+페이드로 등장)
            markJustAdded(fresh);
            feedAdapter.prependItems(fresh);
            binding.recyclerCommunityFeed.scrollToPosition(0);   // 새 항목이 top에 보이게
        } else {
            // 아래를 읽는 중이면 방해하지 않고 모아뒀다가 pill로 알린다 (읽던 위치 보존)
            // 새 배치가 더 최신이므로 대기목록 맨 앞에 붙인다 (전체 최신순 유지)
            pendingFresh.addAll(0, fresh);
            binding.buttonNewReviews.setText(
                    getString(R.string.community_new_reviews, pendingFresh.size()));
            binding.buttonNewReviews.setVisibility(View.VISIBLE);
        }
    }

    /// <summary>목록이 맨 위(첫 항목이 보임)에 있는지</summary>
    private boolean isAtTop() {
        LinearLayoutManager lm =
                (LinearLayoutManager) binding.recyclerCommunityFeed.getLayoutManager();
        return lm != null && lm.findFirstVisibleItemPosition() <= 0;
    }

    /// <summary>새로 끼워지는 항목마다 등장 애니메이션 표시 (ViewHolder가 슬라이드+페이드로 나타냄)</summary>
    private void markJustAdded(List<ReviewFeedItem> items) {
        for (ReviewFeedItem item : items) {
            item.setJustAdded(true);
        }
    }

    /// <summary>
    /// pill 탭 → 밀려있던 새 리뷰를 맨 위에 끼워넣고(등장 애니메이션) 맨 위로 스크롤, pill 숨김
    /// </summary>
    private void applyPendingFresh() {
        if (!pendingFresh.isEmpty()) {
            List<ReviewFeedItem> toAdd = new ArrayList<>(pendingFresh);
            markJustAdded(toAdd);   // 등장 애니메이션 표시 → ViewHolder가 슬라이드+페이드로 나타냄
            feedAdapter.prependItems(toAdd);
            pendingFresh.clear();
        }
        binding.recyclerCommunityFeed.smoothScrollToPosition(0);
        binding.buttonNewReviews.setVisibility(View.GONE);
    }

    // ========== 피드 로딩/표시 ==========

    /// <summary>
    /// 팔로우한 사람들의 리뷰를 백그라운드로 모아 목록에 채운다
    /// isRefresh=false(최초): 가운데 스피너 / true(당겨서 새로고침): SwipeRefreshLayout 자체 스피너
    /// </summary>
    private void loadFeed(boolean isRefresh) {
        App app = (App) requireActivity().getApplication();
        String currentId = app.getAccountManager().getCurrentAccountId();

        if (!isRefresh) {
            binding.progressCommunityFeed.setVisibility(View.VISIBLE);
            binding.recyclerCommunityFeed.setVisibility(View.GONE);
            binding.textViewCommunityFeedEmpty.setVisibility(View.GONE);
        }

        new Thread(() -> {
            // [관찰용] 스피너가 보이게 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // 당겨서 새로고침이면 50% 확률로 팔로우 중인 테스트 계정이 새 리뷰를 올림
            if (isRefresh) {
                final int NEW_REVIEW_CHANCE_PERCENT = 50;
                app.getCommunityRepository().maybePostRandomReview(currentId, NEW_REVIEW_CHANCE_PERCENT);
            }

            List<ReviewFeedItem> feed = app.getCommunityRepository().getFollowingFeed(currentId);

            mainHandler.post(() -> {
                if (binding == null) {
                    return;
                }
                binding.progressCommunityFeed.setVisibility(View.GONE);
                binding.swipeCommunityFeed.setRefreshing(false);
                renderFeed(feed);
            });
        }).start();
    }

    /// <summary>
    /// 받은 피드를 처음부터 그린다 (어댑터 새로 교체 + 기준 시각 갱신 + pill/대기목록 초기화)
    /// </summary>
    private void renderFeed(List<ReviewFeedItem> feed) {
        boolean isEmpty = feed.isEmpty();
        binding.textViewCommunityFeedEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerCommunityFeed.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        feedAdapter = new ReviewFeedAdapter(feed, this::openGame, this::onLikeToggle);
        binding.recyclerCommunityFeed.setAdapter(feedAdapter);

        // 피드는 최신순이라 맨 앞이 "가장 최신" → 그 시각을 기준선으로 저장
        newestShownTimestamp = isEmpty ? 0L : feed.get(0).getTimestamp();

        // 방금 최신을 반영했으니 새 글 알림/대기목록 초기화
        pendingFresh.clear();
        binding.buttonNewReviews.setVisibility(View.GONE);
    }

    /// <summary>
    /// 하트 토글 결과를 내(현재 로그인) 계정 파일에 저장
    /// (ViewHolder가 항목을 이미 토글했으므로 item.isLikedByMe()가 새 상태)
    /// </summary>
    private void onLikeToggle(ReviewFeedItem item) {
        App app = (App) requireActivity().getApplication();
        app.getUserPrefs().setLiked(item.getGameId(), item.getReviewerId(), item.isLikedByMe());
    }

    /// <summary>
    /// 피드 항목 클릭 → 그 게임 상세로 이동
    /// 피드는 게임 id만 알므로 저장소에서 Game을 찾아 Parcelable로 넘긴다
    /// </summary>
    private void openGame(int gameId) {
        Game game = ((App) requireActivity().getApplication()).getGameRepository().findById(gameId);
        if (game == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }
}
