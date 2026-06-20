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
import com.example.week8.ui.GameCardAdapter;
import com.example.week8.ui.LibraryAdapter;
import com.example.week8.ui.TimelineAdapter;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 홈 화면 (허브)
/// Diary / Library / Timeline 세 화면의 미리보기를 한 화면에 모아 보여주고,
/// "더 보기" 버튼으로 각 전체 화면으로 진입
///
/// ──── 중첩 스크롤 구조 (이 화면의 학습 포인트) ────
/// 바깥: ScrollView (세로 스크롤) — 섹션 3개를 위아래로 쌓음
/// 안쪽: 각 섹션의 미리보기 RecyclerView
///   - Diary/Library 미리보기 → 가로 스크롤 (LinearLayoutManager HORIZONTAL)
///   - Timeline 미리보기 → 세로 일부 (스크롤 없이 몇 개만 표시)
/// → "세로 화면 안에 가로 목록" = 중첩 스크롤
///
/// ──── 어댑터 재사용 ────
/// 새 어댑터를 만들지 않고 기존 것을 그대로 사용:
///   Diary/Library 미리보기 → LibraryAdapter (표지 셀) + 가로 LayoutManager
///   Timeline 미리보기 → TimelineAdapter + 세로 LayoutManager
/// → "같은 어댑터라도 LayoutManager만 바꾸면 가로/세로/그리드가 된다"를 다시 확인
/// </summary>
public class HomeActivity extends AppCompatActivity {

    /// <summary>
    /// 미리보기에 보여줄 최대 항목 수
    /// 전체를 다 보여주면 미리보기 의미가 없으므로 앞에서 몇 개만 잘라 표시
    /// </summary>
    private static final int PREVIEW_MAX = 6;

    /// <summary>
    /// 다이어리 미리보기는 정보형 카드(세로)라 화면을 많이 차지하므로 적게 표시
    /// </summary>
    private static final int DIARY_PREVIEW_MAX = 3;

    /// <summary>
    /// 타임라인 미리보기는 세로라 화면을 많이 차지하므로 더 적게 표시
    /// </summary>
    private static final int TIMELINE_PREVIEW_MAX = 3;

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityHomeBinding binding;

    // ========== Lifecycle ==========

    /// <summary>
    /// 홈 화면 생성
    /// 세 섹션의 미리보기 RecyclerView + 더보기 버튼을 세팅
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

        setupDiaryPreview(gameRepository);
        setupLibraryPreview(gameRepository);
        setupTimelinePreview(logRepository, gameRepository);
        setupMoreButtons();
    }

    // ========== 섹션별 미리보기 세팅 ==========

    /// <summary>
    /// 다이어리 미리보기 (세로 일부, 정보형 카드)
    /// DiaryActivity와 같은 GameCardAdapter(제목·별점·한줄평)를 써서 "기록" 느낌을 강조
    /// → 표지 위주인 라이브러리 미리보기와 시각적으로 구분됨
    ///
    /// longClickListener=null: 미리보기라 BottomSheet 메뉴 불필요
    /// setItemTouchHelper 호출 안 함: 드래그 정렬 불필요 → ViewHolder가 핸들을 자동으로 숨김
    /// setNestedScrollingEnabled(false): 바깥 ScrollView가 스크롤을 담당 (몇 개뿐이라 가능)
    /// </summary>
    private void setupDiaryPreview(GameRepository gameRepository) {
        List<Game> preview = takeFirst(gameRepository.getAllGames(), DIARY_PREVIEW_MAX);
        GameCardAdapter adapter = new GameCardAdapter(preview, this::onGameClick, null);
        binding.recyclerDiaryPreview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDiaryPreview.setNestedScrollingEnabled(false);
        binding.recyclerDiaryPreview.setAdapter(adapter);
    }

    /// <summary>
    /// 라이브러리 미리보기 (가로 스크롤, 표지 셀)
    /// 다이어리와 같은 LibraryAdapter지만 별도 인스턴스로 세팅
    /// </summary>
    private void setupLibraryPreview(GameRepository gameRepository) {
        List<Game> preview = takeFirst(gameRepository.getAllGames(), PREVIEW_MAX);
        LibraryAdapter adapter = new LibraryAdapter(preview, this::onGameClick);
        binding.recyclerLibraryPreview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerLibraryPreview.setAdapter(adapter);
    }

    /// <summary>
    /// 타임라인 미리보기 (세로 일부, 멀티 뷰타입)
    /// 로그 앞쪽 몇 개만 잘라서 TimelineAdapter로 표시
    ///
    /// setNestedScrollingEnabled(false):
    ///   이 RecyclerView는 바깥 ScrollView 안에 있음. 둘 다 세로 스크롤이라
    ///   스크롤 충돌이 생길 수 있어, 안쪽 RecyclerView의 자체 스크롤을 꺼서
    ///   바깥 ScrollView가 전체 스크롤을 담당하게 함 (미리보기라 몇 개뿐이라 가능)
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
    /// "더 보기" 버튼 3개에 각 전체 화면 진입 리스너 등록
    /// </summary>
    private void setupMoreButtons() {
        binding.buttonMoreDiary.setOnClickListener(v ->
                startActivity(new Intent(this, DiaryActivity.class)));
        binding.buttonMoreLibrary.setOnClickListener(v ->
                startActivity(new Intent(this, LibraryActivity.class)));
        binding.buttonMoreTimeline.setOnClickListener(v ->
                startActivity(new Intent(this, TimelineActivity.class)));
    }

    // ========== 어댑터 콜백 ==========

    /// <summary>
    /// 미리보기 카드 클릭 → GameDetailActivity로 이동
    /// DiaryActivity/LibraryActivity의 onGameClick과 동일한 동작
    /// </summary>
    private void onGameClick(Game game) {
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }

    // ========== 내부 헬퍼 ==========

    /// <summary>
    /// 리스트 앞쪽 최대 count개를 새 리스트로 복사해 반환
    /// subList를 그대로 넘기면 원본 변경 시 문제가 생길 수 있어 새 ArrayList로 복사
    /// 원본이 count보다 적으면 있는 만큼만 반환
    /// </summary>
    private <T> List<T> takeFirst(List<T> source, int count) {
        int end = Math.min(count, source.size());
        return new ArrayList<>(source.subList(0, end));
    }
}
