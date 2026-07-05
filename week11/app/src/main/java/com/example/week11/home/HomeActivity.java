package com.example.week11.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.account.AccountManager;
import com.example.week11.account.LoginActivity;
import com.example.week11.account.ProfileEditActivity;
import com.example.week11.account.UserPrefs;
import com.example.week11.community.FollowListActivity;
import com.example.week11.community.FollowingFeedActivity;
import com.example.week11.community.RankingActivity;
import com.example.week11.detail.GameDetailActivity;
import com.example.week11.library.LibraryActivity;
import com.example.week11.stats.StatsActivity;
import com.example.week11.timeline.TimelineActivity;
import com.example.week11.data.ActivityLogRepository;
import com.example.week11.data.CommunityRepository;
import com.example.week11.data.GameRepository;
import com.example.week11.databinding.ActivityHomeBinding;
import com.example.week11.model.ActivityLog;
import com.example.week11.model.Game;
import com.example.week11.model.GameStatus;
import com.example.week11.library.LibraryAdapter;
import com.example.week11.timeline.TimelineAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// <summary>
/// 홈 화면 (허브)
/// 보관함 / 최근 활동 미리보기를 한 화면에 모아 보여주고,
/// "더 보기" 버튼으로 각 전체 화면으로 진입
///
/// ──── 중첩 스크롤 구조 (이 화면의 학습 포인트) ────
/// 바깥: ScrollView (세로 스크롤) — 섹션들을 위아래로 쌓음
/// 안쪽: 각 섹션의 미리보기 RecyclerView
///   - 보관함 미리보기 → 가로 스크롤 (LinearLayoutManager HORIZONTAL)
///   - 최근 활동 미리보기 → 세로 일부 (스크롤 없이 몇 개만 표시)
/// → "세로 화면 안에 가로 목록" = 중첩 스크롤
///
/// ──── 어댑터 재사용 ────
/// 새 어댑터를 만들지 않고 기존 것을 그대로 사용:
///   보관함 미리보기 → LibraryAdapter (표지 셀) + 가로 LayoutManager
///   최근 활동 미리보기 → TimelineAdapter + 세로 LayoutManager
/// </summary>
public class HomeActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityHomeBinding binding;

    /// <summary>
    /// 전역 계정 관리자 (로그아웃 / 계정 삭제 메뉴 처리에 사용)
    /// </summary>
    private AccountManager accountManager;

    /// <summary>
    /// "뒤로 두 번 눌러 종료"의 대기 시간(ms) — 이 시간 안에 다시 누르면 종료
    /// </summary>
    private static final long BACK_EXIT_WINDOW_MS = 2000L;

    /// <summary>
    /// 뒤로가기를 한 번 눌러 "종료 대기" 상태인지 여부
    /// </summary>
    private boolean backReadyToExit = false;

    /// <summary>
    /// "뒤로 두 번 눌러 종료" 타이머용 Handler (일정 시간 뒤 대기 상태 해제)
    /// </summary>
    private final Handler backHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 대기 시간이 지나면 "종료 대기" 상태를 해제하는 작업
    /// (2초 안에 다시 안 누르면 처음 상태로 되돌림)
    /// </summary>
    private final Runnable resetBackRunnable = () -> backReadyToExit = false;

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
    /// 보관함 미리보기를 매 프레임 조금씩 오른쪽으로 흘려보내는 반복 작업 (마퀴 방식)
    /// 어댑터가 무한 순환(setLooping)이라 끝이 없어, 끝 다음에 처음이 자연스럽게 이어진다.
    /// "위치로 점프"가 아니라 몇 px씩 연속 이동이라 부드럽게 흐른다.
    /// </summary>
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            binding.recyclerLibraryPreview.scrollBy(AUTO_SCROLL_SPEED_PX, 0);
            // 다음 프레임을 다시 예약 (반복)
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

    // ========== Lifecycle ==========

    /// <summary>
    /// 홈 화면 생성
    /// 섹션별 미리보기 RecyclerView + 더보기 버튼을 세팅
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // App 공용 저장소
        App app = (App) getApplication();
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
                startActivity(new Intent(this, ProfileEditActivity.class)));

        // 팔로잉/팔로워 숫자 탭 → 각각의 목록 화면
        binding.textViewFollowingHome.setOnClickListener(v ->
                openFollowList(FollowListActivity.MODE_FOLLOWING));
        binding.textViewFollowerHome.setOnClickListener(v ->
                openFollowList(FollowListActivity.MODE_FOLLOWERS));

        // 통계 카드 탭 또는 "더 보기" → 상세 통계 화면
        View.OnClickListener openStats = v ->
                startActivity(new Intent(this, StatsActivity.class));
        binding.cardStats.setOnClickListener(openStats);
        binding.buttonMoreStats.setOnClickListener(openStats);

        setupBackToExit();
        // 미리보기 터치 감지 등록 (만지면 자동 스크롤 멈춤 → 손 떼면 잠시 뒤 재개)
        setupPreviewTouchPause();
    }

    /// <summary>
    /// "뒤로 두 번 눌러 종료" 처리 등록
    /// - 첫 번째 뒤로가기: 안내 토스트 + 2초 타이머 예약 (아직 종료 안 함)
    /// - 2초 안에 두 번째 뒤로가기: 앱 종료
    /// - 2초가 지나면 resetBackRunnable이 대기 상태를 풀어 "처음"으로 되돌림
    /// (홈은 앱의 첫 화면이라, 실수로 뒤로가기 한 번에 앱이 꺼지는 걸 막는 흔한 폴리시)
    /// </summary>
    private void setupBackToExit() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 두 번째(대기 중) → 종료
                if (backReadyToExit) {
                    finish();
                    return;
                }

                // 첫 번째 → 대기 상태로 만들고 안내, 2초 뒤 자동 해제 예약
                backReadyToExit = true;
                Toast.makeText(HomeActivity.this, R.string.home_back_to_exit,
                        Toast.LENGTH_SHORT).show();
                backHandler.postDelayed(resetBackRunnable, BACK_EXIT_WINDOW_MS);
            }
        });
    }

    /// <summary>
    /// 화면이 사라질 때, 예약해 둔 Handler 작업들을 전부 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        backHandler.removeCallbacksAndMessages(null);
        autoScrollHandler.removeCallbacksAndMessages(null);
    }

    /// <summary>
    /// 화면이 가려질 때(다른 화면으로 이동 등) 자동 넘김을 멈춘다
    /// 안 보이는 화면에서 스크롤을 계속 돌릴 이유가 없음 (배터리·불필요 동작 방지)
    /// </summary>
    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScroll();
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
    ///   → 자동 스크롤과 유저 조작을 깔끔하게 구분할 수 있음
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

    /// <summary>
    /// 다른 화면에서 상태 변경/게임 추가/삭제 후 돌아오면 통계 숫자를 다시 계산
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();
        App app = (App) getApplication();
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

    // ========== ActionBar 메뉴 (⋮ 로그아웃 / 계정 삭제) ==========

    /// <summary>
    /// ActionBar에 menu_home.xml을 띄운다
    /// inflate: 메뉴 리소스(XML)를 실제 메뉴 항목들로 만들어 ActionBar에 얹는 것
    /// </summary>
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /// <summary>
    /// 메뉴 항목 선택 처리
    /// 단일 값(선택된 항목 id)으로 여러 분기 → switch 사용
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_following_feed) {
            startActivity(new Intent(this, FollowingFeedActivity.class));
            return true;
        }
        if (id == R.id.action_ranking) {
            startActivity(new Intent(this, RankingActivity.class));
            return true;
        }
        if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        if (id == R.id.action_delete_account) {
            confirmDeleteAccount();
            return true;
        }
        if (id == R.id.action_reset_all) {
            confirmResetAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /// <summary>
    /// 전체 초기화 확인 다이얼로그 (테스트/시연용)
    /// 확인 시 모든 계정·prefs를 비우고 로그인 화면으로 → "새 설치" 상태
    /// </summary>
    private void confirmResetAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_confirm_title)
                .setMessage(R.string.reset_confirm_message)
                .setPositiveButton(R.string.reset_confirm_ok, (dialog, which) -> {
                    // App이 초기화 + 테스트 계정 재심기까지 처리 (테스트 계정은 안 사라짐)
                    ((App) getApplication()).resetAllData();
                    goToLogin();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /// <summary>
    /// 로그아웃 확인 다이얼로그 → 확인 시 세션을 비우고 로그인 화면으로
    /// </summary>
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.logout_confirm_ok, (dialog, which) -> {
                    accountManager.logout();
                    goToLogin();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /// <summary>
    /// 계정 삭제 확인 다이얼로그 → 확인 시 현재 계정을 통째로 지우고 로그인 화면으로
    /// 되돌릴 수 없는 동작이라 별명을 메시지에 넣어 "어떤 계정을 지우는지" 분명히 보여준다
    /// </summary>
    private void confirmDeleteAccount() {
        // 현재 로그인 계정이 없으면(비정상) 아무것도 하지 않음
        if (!accountManager.hasCurrentAccount()) {
            return;
        }
        String currentId = accountManager.getCurrentAccountId();
        String nickname = accountManager.getNickname(currentId);

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account_confirm_title)
                .setMessage(getString(R.string.delete_account_confirm_message, nickname))
                .setPositiveButton(R.string.delete_account_confirm_ok, (dialog, which) -> {
                    accountManager.deleteAccount(currentId);
                    goToLogin();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /// <summary>
    /// 로그인 화면으로 이동 (로그아웃/계정 삭제 후 공통)
    /// FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK:
    ///   홈을 비롯한 기존 화면을 모두 제거 → 뒤로가기로 로그인된 화면에 못 돌아옴
    /// </summary>
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ========== 프로필 헤더 ==========

    /// <summary>
    /// 프로필 헤더(아바타 + 별명 + 한 줄 소개)를 현재 계정 정보로 채운다
    ///   - 별명 → AccountManager
    ///   - 아바타 색 / 한 줄 소개 → UserPrefs
    /// 한 줄 소개가 비어 있으면 "추가해보세요" 안내 문구를 대신 보여준다
    /// </summary>
    private void setupProfileHeader() {
        String id = accountManager.getCurrentAccountId();
        // 로그인 상태가 아니면(비정상) 헤더를 건드리지 않음
        if (id == null) {
            return;
        }

        String nickname = accountManager.getNickname(id);
        UserPrefs userPrefs = ((App) getApplication()).getUserPrefs();
        int avatarColor = userPrefs.getAvatarColor();
        String bio = userPrefs.getBio();

        // 별명 + 아바타(첫 글자 + 색)
        binding.textViewNicknameHome.setText(nickname);
        binding.textViewAvatarHome.setText(initialOf(nickname));
        binding.textViewAvatarHome.setBackgroundTintList(ColorStateList.valueOf(avatarColor));

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

        // 팔로잉 · 팔로워 수 (커뮤니티 저장소가 계정들을 훑어 집계)
        CommunityRepository community = ((App) getApplication()).getCommunityRepository();
        int following = community.getFollowingCount(id);
        int follower = community.getFollowerCount(id);
        binding.textViewFollowingHome.setText(getString(R.string.profile_following_count, following));
        binding.textViewFollowerHome.setText(getString(R.string.profile_follower_count, follower));
    }

    /// <summary>
    /// 오늘 첫 방문이면 출석을 집계하고 연출(토스트)을 띄운다
    /// 같은 날 다시 들어오면(또는 테마 변경으로 화면 재생성되면) recordVisit이 false를 돌려줘
    /// 토스트가 다시 뜨지 않는다 → 하루 한 번만 축하
    /// </summary>
    private void recordTodayVisit() {
        UserPrefs userPrefs = ((App) getApplication()).getUserPrefs();
        if (userPrefs == null) {
            return;
        }

        // 오늘 날짜는 여기서 구해 UserPrefs에 넘긴다 (UserPrefs는 시계에 직접 의존하지 않음)
        boolean newVisitToday = userPrefs.recordVisit(LocalDate.now());
        if (newVisitToday) {
            String message = getString(R.string.attendance_toast, userPrefs.getStreak());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /// <summary>
    /// 팔로잉/팔로워 목록 화면을 연다 (mode로 무엇을 볼지 전달)
    /// </summary>
    private void openFollowList(String mode) {
        Intent intent = new Intent(this, FollowListActivity.class);
        intent.putExtra(FollowListActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    /// <summary>
    /// 별명의 첫 글자(대문자)를 반환, 비어 있으면 물음표
    /// 아바타 원 안에 넣을 한 글자를 만든다 (한글은 대문자 변환이 없어 그대로 나옴)
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
    /// Repository가 상태별 집계를 계산해주고, 여기서는 화면에 표시만
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

        UserPrefs userPrefs = ((App) getApplication()).getUserPrefs();
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
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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
        // (그리드는 LayoutManager가 폭을 나누지만, 가로 스크롤은 고정해야 여러 개가 보임)
        final int PREVIEW_ITEM_WIDTH_DP = 120;

        List<Game> preview = takeRandom(gameRepository.getAllGames(), PREVIEW_MAX);
        LibraryAdapter adapter = new LibraryAdapter(preview, this::onGameClick, null,
                ((App) getApplication()).getUserPrefs());

        // 가로 스크롤 미리보기 → 셀 폭을 고정해야 여러 개가 보임
        // (안 하면 셀이 match_parent라 화면 폭을 꽉 채워 1개만 보임)
        adapter.setItemWidthDp(PREVIEW_ITEM_WIDTH_DP);

        // 미리보기 내용이 화면 폭보다 넓을 때만(= 다 안 들어올 때만) 순환/자동 스크롤을 켠다
        // (1~2개라 화면에 다 보이면 굳이 돌릴 필요가 없어 어색함)
        float density = getResources().getDisplayMetrics().density;
        int itemWidthPx = (int) (PREVIEW_ITEM_WIDTH_DP * density);   // 셀 한 칸 폭(px)
        int screenWidthPx = getResources().getDisplayMetrics().widthPixels;
        previewScrollable = preview.size() * itemWidthPx > screenWidthPx;

        // 순환은 스크롤 가능할 때만 (다 보이면 그냥 실제 개수만 표시)
        adapter.setLooping(previewScrollable);
        binding.recyclerLibraryPreview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerLibraryPreview.setAdapter(adapter);

        // 순환할 때만 가운데에서 시작 → 좌우 어느 쪽으로 넘겨도 내용이 계속 이어짐
        if (previewScrollable) {
            binding.recyclerLibraryPreview.scrollToPosition(adapter.getItemCount() / 2);
        }
    }

    /// <summary>
    /// 최근 활동 미리보기 (세로 일부, 멀티 뷰타입)
    /// 로그 앞쪽 몇 개만 잘라서 TimelineAdapter로 표시
    ///
    /// setNestedScrollingEnabled(false):
    ///   바깥 ScrollView와 세로 스크롤이 충돌하지 않게 안쪽 자체 스크롤을 끔
    ///   (미리보기라 몇 개뿐이라 가능)
    /// </summary>
    private void setupTimelinePreview(ActivityLogRepository logRepository,
                                      GameRepository gameRepository) {
        final int TIMELINE_PREVIEW_MAX = 3;
        // 더미 + 내 실제 활동 로그를 합친 최신순 목록에서 앞쪽 몇 개만
        UserPrefs userPrefs = ((App) getApplication()).getUserPrefs();
        List<ActivityLog> merged = logRepository.getMergedLogs(userPrefs.getActivityLogs());
        List<ActivityLog> preview = takeFirst(merged, TIMELINE_PREVIEW_MAX);
        TimelineAdapter adapter = new TimelineAdapter(preview, gameRepository);
        binding.recyclerTimelinePreview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTimelinePreview.setNestedScrollingEnabled(false);
        binding.recyclerTimelinePreview.setAdapter(adapter);
    }

    /// <summary>
    /// "더 보기" 버튼에 각 전체 화면 진입 리스너 등록
    /// </summary>
    private void setupMoreButtons() {
        binding.buttonMoreLibrary.setOnClickListener(v ->
                startActivity(new Intent(this, LibraryActivity.class)));
        binding.buttonMoreTimeline.setOnClickListener(v ->
                startActivity(new Intent(this, TimelineActivity.class)));
    }

    // ========== 어댑터 콜백 ==========

    /// <summary>
    /// 미리보기 카드 클릭 → GameDetailActivity로 이동
    /// </summary>
    private void onGameClick(Game game) {
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }

    // ========== 내부 헬퍼 ==========

    /// <summary>
    /// 리스트 앞쪽 최대 count개를 새 리스트로 복사해 반환
    /// 원본이 count보다 적으면 있는 만큼만 반환
    /// </summary>
    private <T> List<T> takeFirst(List<T> source, int count) {
        int end = Math.min(count, source.size());
        return new ArrayList<>(source.subList(0, end));
    }

    /// <summary>
    /// 원본을 건드리지 않고 무작위로 최대 count개를 뽑아 반환
    /// 복사본을 섞은 뒤 앞에서 count개를 자름 ("오늘의 게임" 느낌의 랜덤 미리보기)
    /// 원본이 count보다 적으면 있는 만큼만 반환
    /// </summary>
    private <T> List<T> takeRandom(List<T> source, int count) {
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        int end = Math.min(count, copy.size());
        return new ArrayList<>(copy.subList(0, end));
    }
}
