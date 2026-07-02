package com.example.week10.timeline;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week10.App;
import com.example.week10.account.UserPrefs;
import com.example.week10.databinding.ActivityTimelineBinding;

/// <summary>
/// 타임라인 화면 (활동 피드 — 멀티 뷰타입)
/// ActivityLogRepository의 활동 로그를 종류별로 다른 모양의 셀로 표시
///
/// DiaryActivity/LibraryActivity와 같은 RecyclerView지만,
/// 항목마다 레이아웃이 다른 점이 핵심:
///   DiaryActivity   = 모든 셀 동일 (LinearLayoutManager)
///   LibraryActivity = 모든 셀 동일 (GridLayoutManager)
///   TimelineActivity = 셀마다 다름 (멀티 뷰타입, getItemViewType)
///
/// 활동 로그 + 게임 제목을 함께 보여주기 위해 두 저장소를 모두 사용:
///   ActivityLogRepository → 로그 목록
///   GameRepository        → gameId로 게임 제목 조회 (어댑터가 사용)
/// </summary>
public class TimelineActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityTimelineBinding binding;

    /// <summary>
    /// 타임라인 셀을 그리는 멀티 뷰타입 어댑터
    /// </summary>
    private TimelineAdapter adapter;

    // ========== Lifecycle ==========

    /// <summary>
    /// 타임라인 화면 생성
    /// ViewBinding 연결 + 두 저장소 접근 + LinearLayoutManager/어댑터 세팅
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // RecyclerView 설정
        // 셀 자체는 세로 한 줄씩이므로 LinearLayoutManager (배치는 단순, 차별점은 셀 모양)
        binding.recyclerViewTimeline.setLayoutManager(new LinearLayoutManager(this));
        loadTimeline();
    }

    /// <summary>
    /// 다른 화면에서 활동(추가/완료/리뷰)을 하고 돌아오면 최근 활동을 다시 불러온다
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();
        loadTimeline();
    }

    /// <summary>
    /// 더미 + 내 실제 활동 로그를 합친 최신순 목록으로 어댑터를 새로 세팅
    /// 어댑터에 로그 목록 + 게임 제목 조회용 GameRepository를 함께 주입
    /// </summary>
    private void loadTimeline() {
        App app = (App) getApplication();
        UserPrefs userPrefs = app.getUserPrefs();
        adapter = new TimelineAdapter(
                app.getActivityLogRepository().getMergedLogs(userPrefs.getActivityLogs()),
                app.getGameRepository());
        binding.recyclerViewTimeline.setAdapter(adapter);
    }

    /// <summary>
    /// ActionBar ← 버튼 처리
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
