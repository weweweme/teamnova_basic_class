package com.example.week12.community;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.App;
import com.example.week12.account.UserPrefs;
import com.example.week12.databinding.ActivityRankingBinding;
import com.example.week12.model.AccountProfile;

import java.util.List;

/// <summary>
/// "유저 랭킹" 화면 (커뮤니티)
///
/// ──── 무엇을 하나 ────
/// 이 기기의 계정들을 "리뷰를 많이 쓴 순"으로 순위 매겨 보여준다 (가장 활발한 리뷰어).
/// 서버 있는 앱의 랭킹처럼 보이지만, 실제로는 각 계정 파일(user_<id>)의 리뷰 수를 세어 정렬한 것.
///
/// ──── 데이터 ────
/// CommunityRepository.getRanking()이 계정마다 리뷰 수를 세서 내림차순으로 정렬해준다.
/// </summary>
public class RankingActivity extends AppCompatActivity {

    /// <summary>
    /// activity_ranking.xml의 View 묶음
    /// </summary>
    private ActivityRankingBinding binding;

    /// <summary>
    /// 랭킹 집계 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// 모든 계정 파일(user_<id>)을 훑어 리뷰 수를 세는 건 디스크 작업 → 서브 스레드에서, 표시만 메인에서
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성 — 랭킹을 백그라운드로 집계해 RecyclerView에 채운다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRankingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.recyclerRanking.setLayoutManager(new LinearLayoutManager(this));

        App app = (App) getApplication();
        // 내 계정에 "(나)" 표시 + 팔로우 버튼 상태에 필요한 값들 (가벼운 조회라 메인에서 미리 확보)
        String currentId = app.getAccountManager().getCurrentAccountId();
        UserPrefs userPrefs = app.getUserPrefs();

        // 로딩 스피너 표시, 목록/빈 안내 숨김 (아직 집계 전)
        binding.progressRanking.setVisibility(View.VISIBLE);
        binding.recyclerRanking.setVisibility(View.GONE);
        binding.textViewRankingEmpty.setVisibility(View.GONE);

        // 모든 계정을 훑어 리뷰 수로 정렬하는 무거운 집계 → 서브 스레드에서
        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 스피너가 보이게 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // ⏳ 무거운 일: 계정마다 리뷰 수를 세어 내림차순 정렬 (디스크 읽기)
            List<AccountProfile> ranking = app.getCommunityRepository().getRanking();

            // 결과 반영만 메인 줄로
            mainHandler.post(() -> {
                binding.progressRanking.setVisibility(View.GONE);

                boolean isEmpty = ranking.isEmpty();
                binding.textViewRankingEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.recyclerRanking.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                binding.recyclerRanking.setAdapter(
                        new RankingAdapter(ranking, currentId, userPrefs));
            });
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 메인 큐에 남아있는 랭킹 반영 작업을 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    /// <summary>
    /// ActionBar ← 버튼 → 화면 닫기 (홈으로 복귀)
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
