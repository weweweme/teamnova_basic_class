package com.example.week10.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week10.App;
import com.example.week10.detail.GameDetailActivity;
import com.example.week10.library.LibraryActivity;
import com.example.week10.stats.StatsActivity;
import com.example.week10.timeline.TimelineActivity;
import com.example.week10.data.ActivityLogRepository;
import com.example.week10.data.GameRepository;
import com.example.week10.databinding.ActivityHomeBinding;
import com.example.week10.model.ActivityLog;
import com.example.week10.model.Game;
import com.example.week10.model.GameStatus;
import com.example.week10.library.LibraryAdapter;
import com.example.week10.timeline.TimelineAdapter;

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

        setupStatsSummary(gameRepository);
        setupLibraryPreview(gameRepository);
        setupTimelinePreview(logRepository, gameRepository);
        setupMoreButtons();

        // 통계 카드 탭 또는 "더 보기" → 상세 통계 화면
        View.OnClickListener openStats = v ->
                startActivity(new Intent(this, StatsActivity.class));
        binding.cardStats.setOnClickListener(openStats);
        binding.buttonMoreStats.setOnClickListener(openStats);
    }

    /// <summary>
    /// 다른 화면에서 상태 변경/게임 추가/삭제 후 돌아오면 통계 숫자를 다시 계산
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();
        GameRepository gameRepository = ((App) getApplication()).getGameRepository();
        setupStatsSummary(gameRepository);
        // 화면에 돌아올 때마다 미리보기를 다시 뽑음 → 매번 다른 게임이 보임
        setupLibraryPreview(gameRepository);
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
    /// 보관함 미리보기 (가로 스크롤, 표지 셀)
    /// longClickListener=null: 미리보기라 BottomSheet 메뉴 불필요
    /// </summary>
    private void setupLibraryPreview(GameRepository gameRepository) {
        // 미리보기에 보여줄 최대 게임 수
        final int PREVIEW_MAX = 6;
        // 가로 미리보기 셀 한 칸의 고정 폭(dp)
        // (그리드는 LayoutManager가 폭을 나누지만, 가로 스크롤은 고정해야 여러 개가 보임)
        final int PREVIEW_ITEM_WIDTH_DP = 120;

        List<Game> preview = takeRandom(gameRepository.getAllGames(), PREVIEW_MAX);
        LibraryAdapter adapter = new LibraryAdapter(preview, this::onGameClick, null);

        // 가로 스크롤 미리보기 → 셀 폭을 고정해야 여러 개가 보임
        // (안 하면 셀이 match_parent라 화면 폭을 꽉 채워 1개만 보임)
        adapter.setItemWidthDp(PREVIEW_ITEM_WIDTH_DP);
        binding.recyclerLibraryPreview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerLibraryPreview.setAdapter(adapter);
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
        List<ActivityLog> preview = takeFirst(logRepository.getAllLogs(), TIMELINE_PREVIEW_MAX);
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
