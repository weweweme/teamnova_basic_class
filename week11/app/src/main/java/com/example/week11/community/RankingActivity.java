package com.example.week11.community;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week11.App;
import com.example.week11.account.UserPrefs;
import com.example.week11.data.CommunityRepository;
import com.example.week11.databinding.ActivityRankingBinding;
import com.example.week11.model.AccountProfile;

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
    /// 무거운 디스크 집계는 서브 스레드에서 하고, 목록 표시만 이 Handler로 메인에 넘긴다
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성 — 랭킹 목록을 받아 RecyclerView에 채운다
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

        App app = (App) getApplication();
        // 현재 로그인 아이디/개인 설정은 빠르게 얻을 수 있어 미리 확보 (백그라운드로 넘기려고)
        // 내 계정에 "(나)" 표시를 붙이기 위해 현재 로그인 아이디도 넘긴다
        String currentId = app.getAccountManager().getCurrentAccountId();
        UserPrefs userPrefs = app.getUserPrefs();
        CommunityRepository community = app.getCommunityRepository();

        // 로딩 스피너 표시, 목록/빈 안내는 숨김 (아직 데이터 없음)
        binding.progressRanking.setVisibility(View.VISIBLE);
        binding.recyclerRanking.setVisibility(View.GONE);
        binding.textViewRankingEmpty.setVisibility(View.GONE);

        // 랭킹 집계는 계정마다 파일을 열어 리뷰 수를 세는 디스크 작업 → 서브 스레드에서
        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 계정이 적어 금방 끝나므로, 스피너가 보이게 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // ⏳ 무거운 일: 계정마다 리뷰 수를 세서 내림차순 정렬 (디스크 읽기)
            List<AccountProfile> ranking = community.getRanking();

            // 결과 반영만 메인 줄로 (화면 갱신은 메인에서만)
            mainHandler.post(() -> {
                binding.progressRanking.setVisibility(View.GONE);

                boolean isEmpty = ranking.isEmpty();
                binding.textViewRankingEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.recyclerRanking.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                binding.recyclerRanking.setLayoutManager(new LinearLayoutManager(this));
                binding.recyclerRanking.setAdapter(new RankingAdapter(ranking, currentId, userPrefs));
            });
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 메인 큐에 남아있는 랭킹 반영 작업을 취소 (누수 방지)
    /// 집계가 끝나기 전에 뒤로가기로 나가면 남은 작업이 사라진 화면을 붙잡을 수 있음
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
