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
/// - 상단 '🏆 랭킹' 버튼 → 유저 랭킹 화면(RankingActivity)으로 진입
///
/// ──── Activity → Fragment ────
/// 예전엔 FollowingFeedActivity였으나, 하단 탭 도입으로 이 탭의 "패널"이 됨.
/// 계정마다 파일을 읽는 무거운 집계는 서브 스레드에서, 목록 표시만 mainHandler로 메인에 넘긴다.
/// </summary>
public class CommunityFragment extends Fragment {

    /// <summary>
    /// ViewBinding — fragment_community.xml의 뷰 참조 (뷰 사라질 때 null로 비움)
    /// </summary>
    private FragmentCommunityBinding binding;

    /// <summary>
    /// 피드 집계 결과를 메인 스레드에 반영할 때 쓰는 Handler
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
    /// 뷰가 만들어진 직후 — 목록 세팅 + 랭킹 진입 + 최초 피드 로딩
    /// </summary>
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerCommunityFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        // 당겨서 새로고침 → 피드 다시 로드 (SwipeRefreshLayout이 자체 스피너를 보여줌)
        binding.swipeCommunityFeed.setOnRefreshListener(() -> loadFeed(true));

        // 🏆 랭킹 버튼 → 유저 랭킹 화면 (감초: 별도 Activity로 가볍게 진입)
        binding.buttonRanking.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RankingActivity.class)));

        loadFeed(false);   // 최초 로딩 (가운데 스피너)
    }

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
        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 스피너가 보이게 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // 당겨서 새로고침이면 50% 확률로 팔로우 중인 테스트 계정이 새 리뷰를 올림
            // → 새로고침할 때마다 가끔 새 항목이 맨 위에 뜨는 "살아있는 피드" 느낌
            if (isRefresh) {
                final int NEW_REVIEW_CHANCE_PERCENT = 50;
                app.getCommunityRepository().maybePostRandomReview(currentId, NEW_REVIEW_CHANCE_PERCENT);
            }

            // ⏳ 무거운 일: 팔로우한 계정들의 리뷰를 모아 최신순 정렬 (디스크 읽기)
            List<ReviewFeedItem> feed = app.getCommunityRepository().getFollowingFeed(currentId);

            // 결과 반영만 메인 줄로
            mainHandler.post(() -> {
                // 반영 직전에 뷰가 이미 사라졌으면(탭 전환 등) 아무것도 하지 않음 (안전장치)
                if (binding == null) {
                    return;
                }
                binding.progressCommunityFeed.setVisibility(View.GONE);
                binding.swipeCommunityFeed.setRefreshing(false);   // 새로고침 스피너 끄기

                boolean isEmpty = feed.isEmpty();
                binding.textViewCommunityFeedEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.recyclerCommunityFeed.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                binding.recyclerCommunityFeed.setAdapter(new ReviewFeedAdapter(feed, this::openGame));
            });
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 뷰가 사라질 때, 아직 메인 큐에 남아있는 피드 반영 작업을 취소 + binding 해제 (누수 방지)
    /// </summary>
    @Override
    public void onDestroyView() {
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
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
