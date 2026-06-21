package com.example.week8;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week8.data.ActivityLogRepository;
import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ActivityHomeBinding;
import com.example.week8.model.ActivityLog;
import com.example.week8.model.Game;
import com.example.week8.ui.LibraryAdapter;
import com.example.week8.ui.TimelineAdapter;

import java.util.ArrayList;
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
    /// 보관함 미리보기에 보여줄 최대 항목 수 (가로 스크롤이라 넉넉히)
    /// </summary>
    private static final int PREVIEW_MAX = 6;

    /// <summary>
    /// 최근 활동 미리보기는 세로라 화면을 많이 차지하므로 더 적게 표시
    /// </summary>
    private static final int TIMELINE_PREVIEW_MAX = 3;

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

        setupLibraryPreview(gameRepository);
        setupTimelinePreview(logRepository, gameRepository);
        setupMoreButtons();
    }

    // ========== 섹션별 미리보기 세팅 ==========

    /// <summary>
    /// 보관함 미리보기 (가로 스크롤, 표지 셀)
    /// longClickListener=null: 미리보기라 BottomSheet 메뉴 불필요
    /// </summary>
    private void setupLibraryPreview(GameRepository gameRepository) {
        List<Game> preview = takeFirst(gameRepository.getAllGames(), PREVIEW_MAX);
        LibraryAdapter adapter = new LibraryAdapter(preview, this::onGameClick, null);
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
        List<ActivityLog> preview = takeFirst(
                logRepository.getAllLogs(), TIMELINE_PREVIEW_MAX);
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
}
