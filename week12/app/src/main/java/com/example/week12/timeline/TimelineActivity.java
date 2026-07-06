package com.example.week12.timeline;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.App;
import com.example.week12.account.UserPrefs;
import com.example.week12.databinding.ActivityTimelineBinding;
import com.example.week12.model.ActivityLog;

import java.util.List;

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

    /// <summary>
    /// 활동 로그 집계 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// 더미 + 내 실제 로그를 합쳐 최신순 정렬하는 건 계정 파일을 읽는 디스크 작업 → 서브 스레드에서
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
    /// 로그 모으기(계정 파일 읽기 + 병합 정렬)는 서브 스레드에서, 화면 반영만 메인 줄로
    /// </summary>
    private void loadTimeline() {
        App app = (App) getApplication();
        UserPrefs userPrefs = app.getUserPrefs();

        // 로딩 스피너 표시 (기존 목록은 그대로 뒤에 둔 채 위에 스피너만 얹음)
        binding.progressTimeline.setVisibility(View.VISIBLE);

        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 스피너가 보이게 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // 무거운 일: 더미 + 내 실제 로그를 합쳐 최신순 정렬 (계정 파일 읽기)
            // 서버에서 데이터를 가져오는 것과 같음
            List<ActivityLog> logs =
                    app.getActivityLogRepository().getMergedLogs(userPrefs.getActivityLogs());

            // 결과 반영만 메인 줄로 (어댑터 생성·setAdapter는 메인에서)
            mainHandler.post(() -> {
                binding.progressTimeline.setVisibility(View.GONE);
                adapter = new TimelineAdapter(logs, app.getGameRepository());
                binding.recyclerViewTimeline.setAdapter(adapter);
            });
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 메인 큐에 남아있는 반영 작업을 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
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
