package com.example.week11.community;

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

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.databinding.FragmentCommunityBinding;
import com.example.week11.detail.GameDetailActivity;
import com.example.week11.model.Game;
import com.example.week11.model.ReviewFeedItem;

import java.util.List;

/// <summary>
/// '커뮤니티' 탭 화면 (소셜 — 감초 역할)
///
/// ──── 무엇을 하나 ────
/// - 메인: 팔로잉 피드 (내가 팔로우한 사람들의 리뷰만 최신순) — 항목 탭 → 게임 상세
/// - 상단 '🏆 랭킹' 버튼 → 유저 랭킹 화면(RankingActivity)
/// - 실시간 피드: 이 탭을 보고 있는 동안 8초마다 새 글을 확인(Handler 폴링)해서,
///   새 리뷰가 생기면 화면을 확 바꾸지 않고 상단에 "새 리뷰 N개 ↑" pill만 띄운다
///   (트위터·인스타처럼 읽던 위치를 지켜줌 → pill을 눌러야 맨 위로 갱신)
///
/// ──── 핸들러 학습 포인트 ────
/// pollRunnable이 postDelayed로 "자기 자신을 다시 예약"해 8초 주기로 반복 실행된다.
/// 화면이 보일 때만(onResume) 돌리고 안 보이면(onPause) 멈춘다 → 배터리·누수 방지.
/// (Unity 비유: Update가 아니라 InvokeRepeating으로 8초마다 서버 확인을 도는 코루틴)
/// </summary>
public class CommunityFragment extends Fragment {

    /// <summary>
    /// ViewBinding — fragment_community.xml의 뷰 참조 (뷰 사라질 때 null로 비움)
    /// </summary>
    private FragmentCommunityBinding binding;

    /// <summary>
    /// 백그라운드 집계 결과를 메인 스레드에 반영할 때 쓰는 Handler
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 실시간 피드 폴링(8초 주기) 스케줄용 Handler (결과 반영용 mainHandler와 역할 분리)
    /// </summary>
    private final Handler pollHandler = new Handler(Looper.getMainLooper());

    /// <summary>새 글 확인 주기(ms) — 8초마다 폴링</summary>
    private static final long POLL_INTERVAL_MS = 8000L;

    /// <summary>폴링 때 팔로우 중인 테스트 계정이 새 리뷰를 쓸 확률(%)</summary>
    private static final int AUTO_POST_CHANCE_PERCENT = 40;

    /// <summary>
    /// 지금 화면에 보여주고 있는 피드 맨 위(가장 최신) 리뷰의 작성 시각
    /// 폴링으로 받은 피드에 이 시각보다 새로운 항목이 있으면 "새 글"로 센다
    /// </summary>
    private long newestShownTimestamp = 0L;

    /// <summary>
    /// 폴링으로 미리 받아둔 최신 피드 (pill을 누르면 이걸 화면에 반영)
    /// </summary>
    private List<ReviewFeedItem> pendingFeed = null;

    /// <summary>
    /// 8초마다 새 글을 확인하고, 자기 자신을 다시 예약하는 반복 작업 (Handler 주기 실행)
    /// </summary>
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            pollForNewReviews();
            // 다음 확인을 다시 예약 → 8초 주기로 반복 (자기 재예약)
            pollHandler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    // ========== 생명주기 ==========

    /// <summary>
    /// 뷰(레이아웃)를 부풀려 반환 — 아직 세팅 전
    /// </summary>
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /// <summary>
    /// 뷰가 만들어진 직후 — 목록 세팅 + 랭킹/새글 pill 리스너 + 최초 피드 로딩
    /// </summary>
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerCommunityFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        // 당겨서 새로고침 → 피드 다시 로드
        binding.swipeCommunityFeed.setOnRefreshListener(() -> loadFeed(true));

        // 🏆 랭킹 버튼 → 유저 랭킹 화면
        binding.buttonRanking.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RankingActivity.class)));

        // "새 리뷰 N개 ↑" pill → 미리 받아둔 최신 피드를 반영하고 맨 위로 스크롤
        binding.buttonNewReviews.setOnClickListener(v -> applyPendingFeed());

        loadFeed(false);   // 최초 로딩 (가운데 스피너)
    }

    /// <summary>
    /// 이 탭이 보이기 시작하면 실시간 폴링을 켠다 (8초 주기)
    /// </summary>
    @Override
    public void onResume() {
        super.onResume();
        startPolling();
    }

    /// <summary>
    /// 이 탭이 가려지면 폴링을 멈춘다 (안 보이는데 서버 확인을 돌릴 이유 없음)
    /// </summary>
    @Override
    public void onPause() {
        super.onPause();
        stopPolling();
    }

    /// <summary>
    /// 뷰가 사라질 때, 예약된 모든 Handler 작업 취소 + binding 해제 (누수 방지)
    /// </summary>
    @Override
    public void onDestroyView() {
        stopPolling();
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
    }

    // ========== 실시간 폴링 (Handler 주기 실행) ==========

    /// <summary>
    /// 폴링 시작 (중복 예약을 막고 새로 예약)
    /// </summary>
    private void startPolling() {
        pollHandler.removeCallbacks(pollRunnable);
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    /// <summary>
    /// 폴링 정지
    /// </summary>
    private void stopPolling() {
        pollHandler.removeCallbacks(pollRunnable);
    }

    /// <summary>
    /// 새 글 확인 1회: (확률적으로) 팔로우 중인 테스트 계정이 새 리뷰를 쓰게 하고,
    /// 최신 피드를 백그라운드로 다시 모아 "새 글이 있는지" 판단한다
    /// </summary>
    private void pollForNewReviews() {
        App app = (App) requireActivity().getApplication();
        String currentId = app.getAccountManager().getCurrentAccountId();

        new Thread(() -> {
            // 폴링 때도 확률적으로 새 리뷰를 만들어 "가만히 둬도 피드가 살아 움직이게"
            app.getCommunityRepository().maybePostRandomReview(currentId, AUTO_POST_CHANCE_PERCENT);
            List<ReviewFeedItem> feed = app.getCommunityRepository().getFollowingFeed(currentId);

            mainHandler.post(() -> onPolled(feed));
        }).start();
    }

    /// <summary>
    /// 폴링 결과 반영 — 새 글이 있으면 pill을 띄운다 (지금 화면이 비어 있으면 바로 채운다)
    /// </summary>
    private void onPolled(List<ReviewFeedItem> feed) {
        // 반영 직전에 뷰가 사라졌으면 아무것도 하지 않음
        if (binding == null) {
            return;
        }

        // 아직 아무것도 안 보이는 상태(빈 피드)라면 pill 없이 바로 채운다 (방해할 게 없음)
        boolean nothingShownYet = newestShownTimestamp == 0L;
        if (nothingShownYet) {
            renderFeed(feed);
            return;
        }

        // 지금 보여주는 것보다 새로운(시각이 더 큰) 항목 수를 센다
        int newCount = 0;
        for (ReviewFeedItem item : feed) {
            if (item.getTimestamp() > newestShownTimestamp) {
                newCount++;
            }
        }

        // 새 글이 있으면 화면을 바꾸지 않고 pill만 띄운다 (읽던 위치 보존)
        if (newCount > 0) {
            pendingFeed = feed;
            binding.buttonNewReviews.setText(getString(R.string.community_new_reviews, newCount));
            binding.buttonNewReviews.setVisibility(View.VISIBLE);
        }
    }

    /// <summary>
    /// pill 탭 → 미리 받아둔 최신 피드를 화면에 반영하고 맨 위로 스크롤
    /// </summary>
    private void applyPendingFeed() {
        if (pendingFeed == null) {
            return;
        }
        renderFeed(pendingFeed);
        binding.recyclerCommunityFeed.scrollToPosition(0);
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
            // 최초 로딩만 가운데 스피너 표시 (새로고침은 위쪽 자체 스피너로 충분)
            binding.progressCommunityFeed.setVisibility(View.VISIBLE);
            binding.recyclerCommunityFeed.setVisibility(View.GONE);
            binding.textViewCommunityFeedEmpty.setVisibility(View.GONE);
        }

        // 팔로우한 사람들의 리뷰 모으기는 계정마다 파일을 읽는 디스크 작업 → 서브 스레드에서
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

            // ⏳ 무거운 일: 팔로우한 계정들의 리뷰를 모아 최신순 정렬 (디스크 읽기)
            List<ReviewFeedItem> feed = app.getCommunityRepository().getFollowingFeed(currentId);

            mainHandler.post(() -> {
                if (binding == null) {
                    return;
                }
                binding.progressCommunityFeed.setVisibility(View.GONE);
                binding.swipeCommunityFeed.setRefreshing(false);   // 새로고침 스피너 끄기
                renderFeed(feed);
            });
        }).start();
    }

    /// <summary>
    /// 받은 피드를 화면에 그린다 (빈 상태 토글 + 어댑터 교체 + "지금 보여주는 최신 시각" 갱신 + pill 숨김)
    /// </summary>
    private void renderFeed(List<ReviewFeedItem> feed) {
        boolean isEmpty = feed.isEmpty();
        binding.textViewCommunityFeedEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerCommunityFeed.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.recyclerCommunityFeed.setAdapter(
                new ReviewFeedAdapter(feed, this::openGame, this::onLikeToggle));

        // 피드는 최신순 정렬이므로 맨 앞 항목이 "가장 최신" → 그 시각을 기준선으로 저장
        newestShownTimestamp = isEmpty ? 0L : feed.get(0).getTimestamp();

        // 방금 최신을 반영했으니 새 글 알림은 숨기고 대기 피드도 비운다
        binding.buttonNewReviews.setVisibility(View.GONE);
        pendingFeed = null;
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
