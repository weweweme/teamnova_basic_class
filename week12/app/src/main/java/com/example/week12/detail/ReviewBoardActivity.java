package com.example.week12.detail;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.App;
import com.example.week12.databinding.ActivityReviewBoardBinding;
import com.example.week12.model.GameReview;

import java.util.List;

/// <summary>
/// 리뷰 게시판 화면 (게임 상세의 "더 보기")
///
/// ──── 무엇을 하나 ────
/// 특정 게임에 대한 "다른 사람들의 평가"를 전체 목록(게시판)으로 보여준다.
/// 게임 상세에는 미리보기 몇 개만 보이고, "더 보기"로 여기 들어오면 전부 볼 수 있다.
/// (내 평가는 제외 — 게임 상세 위쪽 "내 평가"에 따로 있음)
/// </summary>
public class ReviewBoardActivity extends AppCompatActivity {

    /// <summary>입력: 어느 게임의 리뷰인지 (게임 id)</summary>
    public static final String EXTRA_GAME_ID = "extra_game_id";

    /// <summary>입력: 화면 상단에 표시할 게임 제목</summary>
    public static final String EXTRA_GAME_TITLE = "extra_game_title";

    /// <summary>
    /// activity_review_board.xml의 View 묶음
    /// </summary>
    private ActivityReviewBoardBinding binding;

    /// <summary>
    /// 리뷰 집계 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// 계정마다 파일을 읽고 좋아요까지 세는 무거운 집계는 서브 스레드에서, 목록 표시만 메인에 넘긴다
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성 — 이 게임의 다른 사람들 리뷰 전체를 목록에 채운다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityReviewBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int gameId = getIntent().getIntExtra(EXTRA_GAME_ID, -1);
        String gameTitle = getIntent().getStringExtra(EXTRA_GAME_TITLE);
        binding.textViewBoardGameTitle.setText(gameTitle);

        App app = (App) getApplication();
        String currentId = app.getAccountManager().getCurrentAccountId();

        // 로딩 스피너 표시, 목록/빈 안내 숨김 (아직 집계 전)
        binding.progressBoard.setVisibility(View.VISIBLE);
        binding.recyclerBoard.setVisibility(View.GONE);
        binding.textViewBoardEmpty.setVisibility(View.GONE);

        // 이 게임의 다른 사람들 리뷰 모으기는 계정마다 파일을 읽고 좋아요까지 세는 무거운 작업 → 서브 스레드
        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 스피너가 보이게 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // 무거운 일: 나를 뺀 다른 계정들의 이 게임 리뷰 전체 모으기 (디스크 읽기)
            // 서버에서 데이터를 가져오는 것과 같음
            List<GameReview> reviews = app.getCommunityRepository().getReviewsForGame(gameId, currentId);

            // 결과 반영만 메인 줄로
            mainHandler.post(() -> {
                binding.progressBoard.setVisibility(View.GONE);

                boolean isEmpty = reviews.isEmpty();
                binding.textViewBoardEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.recyclerBoard.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                binding.recyclerBoard.setLayoutManager(new LinearLayoutManager(this));
                binding.recyclerBoard.setAdapter(new GameReviewAdapter(reviews, app.getUserPrefs()));
            });
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 메인 큐에 남아있는 리뷰 반영 작업을 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    /// <summary>
    /// ActionBar ← 버튼 → 화면 닫기 (게임 상세로 복귀)
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
