package com.example.week12.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.App;
import com.example.week12.R;
import com.example.week12.account.AccountManager;
import com.example.week12.account.ProfileEditActivity;
import com.example.week12.account.UserPrefs;
import com.example.week12.community.FollowListActivity;
import com.example.week12.data.ActivityLogRepository;
import com.example.week12.data.CommunityRepository;
import com.example.week12.data.GameRepository;
import com.example.week12.databinding.FragmentHomeBinding;
import com.example.week12.detail.GameDetailActivity;
import com.example.week12.library.LibraryActivity;
import com.example.week12.library.LibraryAdapter;
import com.example.week12.model.ActivityLog;
import com.example.week12.model.Game;
import com.example.week12.model.GameStatus;
import com.example.week12.stats.StatsActivity;
import com.example.week12.timeline.TimelineActivity;
import com.example.week12.timeline.TimelineAdapter;
import com.example.week12.util.CoverImageLoader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// <summary>
/// '내 일지' 탭 화면 (개인 기록 허브)
/// 보관함 / 최근 활동 미리보기를 한 화면에 모아 보여주고, "더 보기"로 각 전체 화면으로 진입
///
/// ──── 중첩 스크롤 구조 (이 화면의 학습 포인트) ────
/// 바깥: ScrollView (세로 스크롤) — 섹션들을 위아래로 쌓음
/// 안쪽: 각 섹션의 미리보기 RecyclerView (보관함=가로, 최근활동=세로 일부)
///
/// ──── Activity → Fragment ────
/// 예전엔 HomeActivity였으나, 하단 탭(MainActivity) 도입으로 이 탭의 "패널"이 됨.
/// 그래서 화면 생성은 onCreateView(뷰 만들기) + onViewCreated(뷰 세팅)로 나뉜다.
/// (Unity 비유: Activity=Scene 통째 로드, Fragment=Scene 안의 UI 패널 하나)
/// </summary>
public class HomeFragment extends Fragment {

    /// <summary>
    /// ViewBinding 객체 (뷰가 사라질 때 null로 비워 누수 방지)
    /// </summary>
    private FragmentHomeBinding binding;

    /// <summary>
    /// 전역 계정 관리자 (프로필 헤더 집계에 사용)
    /// </summary>
    private AccountManager accountManager;

    /// <summary>
    /// 자동 스크롤 프레임 간격(ms) — 약 60fps
    /// </summary>
    private static final long AUTO_SCROLL_FRAME_MS = 16;

    /// <summary>
    /// 한 프레임에 옆으로 이동할 픽셀 수 (작을수록 느리게 흐름)
    /// </summary>
    private static final int AUTO_SCROLL_SPEED_PX = 3;

    /// <summary>
    /// 유저가 미리보기에서 손을 뗀 뒤, 자동 스크롤을 다시 시작하기까지 기다리는 시간(ms)
    /// </summary>
    private static final long AUTO_SCROLL_RESUME_DELAY_MS = 3000;

    /// <summary>
    /// 미리보기 자동 넘김용 Handler
    /// 화면이 보일 때만(onResume) 돌리고, 안 보이면(onPause) 멈춘다 → 배터리·불필요한 스크롤 방지
    /// </summary>
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 보관함 미리보기를 매 프레임 조금씩 흘려보내는 반복 작업 (마퀴 방식)
    /// 어댑터가 무한 순환(setLooping)이라 끝 다음에 처음이 자연스럽게 이어진다
    /// </summary>
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            // 뷰가 이미 사라졌으면(binding null) 아무것도 하지 않음 (안전장치)
            if (binding == null) {
                return;
            }
            binding.recyclerLibraryPreview.scrollBy(AUTO_SCROLL_SPEED_PX, 0);
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_FRAME_MS);
        }
    };

    /// <summary>
    /// 유저가 손을 뗀 뒤 잠시 있다가 자동 스크롤을 다시 켜는 작업
    /// </summary>
    private final Runnable autoScrollResumeRunnable = this::startAutoScroll;

    /// <summary>
    /// 미리보기가 화면 폭보다 넓어서 스크롤/순환할 만한 상태인지 (setupLibraryPreview에서 계산)
    /// false면(내용이 다 보이면) 자동 스크롤을 아예 시작하지 않는다
    /// </summary>
    private boolean previewScrollable = false;

    /// <summary>
    /// 팔로잉/팔로워 집계(모든 계정 파일을 훑는 디스크 작업) 결과를 메인 스레드에 반영하는 Handler
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
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /// <summary>
    /// 뷰가 만들어진 직후 — 섹션별 미리보기 + 클릭 리스너를 세팅 (예전 onCreate 역할)
    /// </summary>
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App app = (App) requireActivity().getApplication();
        GameRepository gameRepository = app.getGameRepository();
        ActivityLogRepository logRepository = app.getActivityLogRepository();
        accountManager = app.getAccountManager();

        // 오늘 첫 방문이면 출석 집계 + 연출 (프로필 헤더보다 먼저 → 헤더가 갱신된 값 표시)
        recordTodayVisit();
        setupProfileHeader();
        setupStatsSummary(gameRepository);
        setupRecentViewed(gameRepository);
        setupLibraryPreview(gameRepository);
        setupTimelinePreview(logRepository, gameRepository);
        setupMoreButtons();

        // 프로필 헤더 탭 → 프로필 편집 화면
        binding.cardProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileEditActivity.class)));

        // 팔로잉/팔로워 숫자 탭 → 각각의 목록 화면
        binding.textViewFollowingHome.setOnClickListener(v ->
                openFollowList(FollowListActivity.MODE_FOLLOWING));
        binding.textViewFollowerHome.setOnClickListener(v ->
                openFollowList(FollowListActivity.MODE_FOLLOWERS));

        // 통계 카드 탭 또는 "더 보기" → 상세 통계 화면
        View.OnClickListener openStats = v ->
                startActivity(new Intent(requireContext(), StatsActivity.class));
        binding.cardStats.setOnClickListener(openStats);
        binding.buttonMoreStats.setOnClickListener(openStats);

        // 미리보기 터치 감지 등록 (만지면 자동 스크롤 멈춤 → 손 떼면 잠시 뒤 재개)
        setupPreviewTouchPause();
    }

    /// <summary>
    /// 이 탭이 다시 보일 때(탭 전환/다른 화면에서 복귀) 데이터를 다시 계산해 최신 상태로 그림
    /// </summary>
    @Override
    public void onResume() {
        super.onResume();
        App app = (App) requireActivity().getApplication();
        GameRepository gameRepository = app.getGameRepository();
        // 프로필 편집 후 돌아오면 바뀐 아바타/별명/소개를 다시 그림
        setupProfileHeader();
        setupStatsSummary(gameRepository);
        // 방금 상세를 보고 돌아왔으면 그 게임이 "최근 본 게임" 맨 앞에 반영됨
        setupRecentViewed(gameRepository);
        // 화면에 돌아올 때마다 미리보기를 다시 뽑음 → 매번 다른 게임이 보임
        setupLibraryPreview(gameRepository);
        // 방금 추가/완료/리뷰한 활동이 "최근 활동" 맨 위에 반영됨
        setupTimelinePreview(app.getActivityLogRepository(), gameRepository);

        // 보관함 미리보기 자동 넘김(마퀴) 시작
        startAutoScroll();
    }

    /// <summary>
    /// 이 탭이 가려질 때 자동 넘김을 멈춘다 (안 보이는데 스크롤 돌릴 이유 없음)
    /// </summary>
    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    /// <summary>
    /// 뷰가 사라질 때 예약된 Handler 작업 취소 + binding 참조 해제 (누수 방지)
    /// </summary>
    @Override
    public void onDestroyView() {
        autoScrollHandler.removeCallbacksAndMessages(null);
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
    }

    // ========== 미리보기 자동 넘김(마퀴) ==========

    /// <summary>
    /// 자동 스크롤 시작 (예약 중복을 막고 새로 예약)
    /// 단, 미리보기가 화면에 다 들어오면(스크롤할 게 없으면) 시작하지 않는다
    /// </summary>
    private void startAutoScroll() {
        if (!previewScrollable) {
            return;
        }
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_FRAME_MS);
    }

    /// <summary>
    /// 자동 스크롤 정지 (스크롤 반복 + 재개 예약 둘 다 취소)
    /// </summary>
    private void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        autoScrollHandler.removeCallbacks(autoScrollResumeRunnable);
    }

    /// <summary>
    /// 유저가 손을 뗀 뒤 일정 시간 후 자동 스크롤을 다시 켜도록 예약
    /// (기다리는 동안 다시 만지면 ACTION_DOWN에서 stopAutoScroll이 이 예약을 취소함)
    /// </summary>
    private void scheduleAutoScrollResume() {
        autoScrollHandler.removeCallbacks(autoScrollResumeRunnable);
        autoScrollHandler.postDelayed(autoScrollResumeRunnable, AUTO_SCROLL_RESUME_DELAY_MS);
    }

    /// <summary>
    /// 미리보기를 유저가 만지면 자동 스크롤을 멈추고, 손을 떼면 잠시 뒤 다시 켜지도록 터치 감지 등록
    /// onInterceptTouchEvent는 "실제 손가락 터치"에만 불리고, 우리가 코드로 하는 scrollBy에는 안 불림
    /// return false: 터치를 가로채지 않음 → 유저가 직접 좌우로 넘기는 동작은 그대로 동작
    /// </summary>
    private void setupPreviewTouchPause() {
        binding.recyclerLibraryPreview.addOnItemTouchListener(
                new RecyclerView.SimpleOnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv,
                                                         @NonNull MotionEvent e) {
                        switch (e.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                stopAutoScroll();            // 만지기 시작 → 멈춤
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                scheduleAutoScrollResume();  // 손 뗌 → 잠시 뒤 재개
                                break;
                        }
                        return false;
                    }
                });
    }

    // ========== 프로필 헤더 ==========

    /// <summary>
    /// 프로필 헤더(아바타 + 별명 + 한 줄 소개 + 출석 + 팔로잉/팔로워)를 현재 계정 정보로 채운다
    /// </summary>
    private void setupProfileHeader() {
        String id = accountManager.getCurrentAccountId();
        // 로그인 상태가 아니면(비정상) 헤더를 건드리지 않음
        if (id == null) {
            return;
        }

        String nickname = accountManager.getNickname(id);
        UserPrefs userPrefs = ((App) requireActivity().getApplication()).getUserPrefs();
        int avatarColor = userPrefs.getAvatarColor();
        String bio = userPrefs.getBio();

        // 별명 + 아바타(첫 글자 + 색)
        binding.textViewNicknameHome.setText(nickname);
        binding.textViewAvatarHome.setText(initialOf(nickname));
        binding.textViewAvatarHome.setBackgroundTintList(ColorStateList.valueOf(avatarColor));

        // 프로필 사진이 있으면(카카오 등) 색깔 원 위에 사진을 얹어 보여줌 (없으면 색깔 원 그대로)
        String avatarImageUrl = userPrefs.getAvatarImageUrl();
        boolean hasAvatarImage = avatarImageUrl != null && !avatarImageUrl.isEmpty();
        if (hasAvatarImage) {
            // 원형 배경(bg_avatar_circle) 외곽선으로 사진을 동그랗게 자른다
            binding.imageViewAvatarHome.setClipToOutline(true);
            CoverImageLoader loader = ((App) requireActivity().getApplication()).getCoverImageLoader();
            loader.loadUri(binding.imageViewAvatarHome, avatarImageUrl);
            binding.imageViewAvatarHome.setVisibility(View.VISIBLE);
        } else {
            binding.imageViewAvatarHome.setVisibility(View.GONE);
        }

        // 한 줄 소개 (없으면 안내 문구)
        boolean bioEmpty = bio.isEmpty();
        if (bioEmpty) {
            binding.textViewBioHome.setText(R.string.profile_bio_empty_hint);
        } else {
            binding.textViewBioHome.setText(bio);
        }

        // 출석/방문 (연속 일수 · 누적 횟수)
        int streak = userPrefs.getStreak();
        int visitCount = userPrefs.getVisitCount();
        binding.textViewAttendanceHome.setText(
                getString(R.string.attendance_summary, streak, visitCount));

        // 팔로잉 · 팔로워 수: 모든 계정 파일을 훑어 세는 디스크 작업 → 서브 스레드에서 집계
        // (내 프로필 1개만 읽는 위쪽 값들과 달리, 계정이 많아질수록 느려질 수 있어 메인 스레드에서 뺀다)
        CommunityRepository community =
                ((App) requireActivity().getApplication()).getCommunityRepository();
        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            int following = community.getFollowingCount(id);
            int follower = community.getFollowerCount(id);

            // 결과 반영만 메인 줄로 (집계가 끝났을 때 화면이 이미 사라졌으면 무시)
            mainHandler.post(() -> {
                if (binding == null) {
                    return;
                }
                binding.textViewFollowingHome.setText(
                        getString(R.string.profile_following_count, following));
                binding.textViewFollowerHome.setText(
                        getString(R.string.profile_follower_count, follower));
            });
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 오늘 첫 방문이면 출석을 집계하고 연출(토스트)을 띄운다
    /// recordVisit이 날짜로 가드되어, 같은 날 다시 들어오면 false → 토스트가 다시 뜨지 않음
    /// </summary>
    private void recordTodayVisit() {
        UserPrefs userPrefs = ((App) requireActivity().getApplication()).getUserPrefs();
        if (userPrefs == null) {
            return;
        }

        // 오늘 날짜는 여기서 구해 UserPrefs에 넘긴다 (UserPrefs는 시계에 직접 의존하지 않음)
        boolean newVisitToday = userPrefs.recordVisit(LocalDate.now());
        if (newVisitToday) {
            String message = getString(R.string.attendance_toast, userPrefs.getStreak());
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /// <summary>
    /// 팔로잉/팔로워 목록 화면을 연다 (mode로 무엇을 볼지 전달)
    /// </summary>
    private void openFollowList(String mode) {
        Intent intent = new Intent(requireContext(), FollowListActivity.class);
        intent.putExtra(FollowListActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    /// <summary>
    /// 별명의 첫 글자(대문자)를 반환, 비어 있으면 물음표
    /// </summary>
    private String initialOf(String nickname) {
        if (nickname.isEmpty()) {
            return getString(R.string.profile_avatar_placeholder);
        }
        char first = Character.toUpperCase(nickname.charAt(0));
        return String.valueOf(first);
    }

    // ========== 통계 요약 ==========

    /// <summary>
    /// 통계 카드 숫자 채우기 (전체/완료/플레이중/찜 목록 개수)
    /// </summary>
    private void setupStatsSummary(GameRepository gameRepository) {
        binding.textViewStatTotal.setText(
                String.valueOf(gameRepository.getTotalCount()));
        binding.textViewStatCompleted.setText(
                String.valueOf(gameRepository.countByStatus(GameStatus.COMPLETED)));
        binding.textViewStatPlaying.setText(
                String.valueOf(gameRepository.countByStatus(GameStatus.PLAYING)));
        binding.textViewStatBacklog.setText(
                String.valueOf(gameRepository.countByStatus(GameStatus.BACKLOG)));
    }

    // ========== 섹션별 미리보기 세팅 ==========

    /// <summary>
    /// 최근 본 게임 미리보기 (가로 스크롤, 표지 셀)
    /// 게임 상세를 연 순서(최신순)대로 UserPrefs에서 id 목록을 받아 실제 Game으로 바꿔 표시
    /// 아직 본 게임이 없으면 섹션을 통째로 숨김(GONE)
    /// </summary>
    private void setupRecentViewed(GameRepository gameRepository) {
        // 가로 미리보기 셀 한 칸의 고정 폭(dp) — 보관함 미리보기와 같은 값
        final int PREVIEW_ITEM_WIDTH_DP = 120;

        UserPrefs userPrefs = ((App) requireActivity().getApplication()).getUserPrefs();
        List<Integer> recentIds = userPrefs.getRecentGameIds();

        // id를 실제 Game으로 변환 (혹시 사라진 게임 id는 건너뜀)
        List<Game> recentGames = new ArrayList<>();
        for (int id : recentIds) {
            Game game = gameRepository.findById(id);
            if (game != null) {
                recentGames.add(game);
            }
        }

        // 본 게임이 하나도 없으면 섹션 자체를 숨김
        boolean empty = recentGames.isEmpty();
        binding.layoutRecentViewed.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }

        LibraryAdapter adapter = new LibraryAdapter(recentGames, this::onGameClick, null,
                userPrefs);
        adapter.setItemWidthDp(PREVIEW_ITEM_WIDTH_DP);
        binding.recyclerRecentViewed.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerRecentViewed.setAdapter(adapter);
    }

    /// <summary>
    /// 보관함 미리보기 (가로 스크롤, 표지 셀)
    /// longClickListener=null: 미리보기라 BottomSheet 메뉴 불필요
    /// </summary>
    private void setupLibraryPreview(GameRepository gameRepository) {
        // 미리보기에 보여줄 최대 게임 수 (많을수록 순환이 자연스러움)
        final int PREVIEW_MAX = 12;
        // 가로 미리보기 셀 한 칸의 고정 폭(dp)
        final int PREVIEW_ITEM_WIDTH_DP = 120;

        List<Game> preview = takeRandom(gameRepository.getAllGames(), PREVIEW_MAX);
        LibraryAdapter adapter = new LibraryAdapter(preview, this::onGameClick, null,
                ((App) requireActivity().getApplication()).getUserPrefs());

        // 가로 스크롤 미리보기 → 셀 폭을 고정해야 여러 개가 보임
        adapter.setItemWidthDp(PREVIEW_ITEM_WIDTH_DP);

        // 미리보기 내용이 화면 폭보다 넓을 때만(= 다 안 들어올 때만) 순환/자동 스크롤을 켠다
        float density = getResources().getDisplayMetrics().density;
        int itemWidthPx = (int) (PREVIEW_ITEM_WIDTH_DP * density);   // 셀 한 칸 폭(px)
        int screenWidthPx = getResources().getDisplayMetrics().widthPixels;
        previewScrollable = preview.size() * itemWidthPx > screenWidthPx;

        // 순환은 스크롤 가능할 때만 (다 보이면 그냥 실제 개수만 표시)
        adapter.setLooping(previewScrollable);
        binding.recyclerLibraryPreview.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerLibraryPreview.setAdapter(adapter);

        // 순환할 때만 가운데에서 시작 → 좌우 어느 쪽으로 넘겨도 내용이 계속 이어짐
        if (previewScrollable) {
            binding.recyclerLibraryPreview.scrollToPosition(adapter.getItemCount() / 2);
        }
    }

    /// <summary>
    /// 최근 활동 미리보기 (세로 일부, 멀티 뷰타입)
    /// 로그 앞쪽 몇 개만 잘라서 TimelineAdapter로 표시
    /// setNestedScrollingEnabled(false): 바깥 ScrollView와 세로 스크롤 충돌 방지
    /// </summary>
    private void setupTimelinePreview(ActivityLogRepository logRepository,
                                      GameRepository gameRepository) {
        final int TIMELINE_PREVIEW_MAX = 3;
        // 더미 + 내 실제 활동 로그를 합친 최신순 목록에서 앞쪽 몇 개만
        UserPrefs userPrefs = ((App) requireActivity().getApplication()).getUserPrefs();
        List<ActivityLog> merged = logRepository.getMergedLogs(userPrefs.getActivityLogs());
        List<ActivityLog> preview = takeFirst(merged, TIMELINE_PREVIEW_MAX);
        TimelineAdapter adapter = new TimelineAdapter(preview, gameRepository);
        binding.recyclerTimelinePreview.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTimelinePreview.setNestedScrollingEnabled(false);
        binding.recyclerTimelinePreview.setAdapter(adapter);
    }

    /// <summary>
    /// "더 보기" 버튼에 각 전체 화면 진입 리스너 등록
    /// </summary>
    private void setupMoreButtons() {
        binding.buttonMoreLibrary.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), LibraryActivity.class)));
        binding.buttonMoreTimeline.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), TimelineActivity.class)));
    }

    // ========== 어댑터 콜백 ==========

    /// <summary>
    /// 미리보기 카드 클릭 → GameDetailActivity로 이동
    /// </summary>
    private void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }

    // ========== 내부 헬퍼 ==========

    /// <summary>
    /// 리스트 앞쪽 최대 count개를 새 리스트로 복사해 반환
    /// </summary>
    private <T> List<T> takeFirst(List<T> source, int count) {
        int end = Math.min(count, source.size());
        return new ArrayList<>(source.subList(0, end));
    }

    /// <summary>
    /// 원본을 건드리지 않고 무작위로 최대 count개를 뽑아 반환 ("오늘의 게임" 느낌)
    /// </summary>
    private <T> List<T> takeRandom(List<T> source, int count) {
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        int end = Math.min(count, copy.size());
        return new ArrayList<>(copy.subList(0, end));
    }
}
