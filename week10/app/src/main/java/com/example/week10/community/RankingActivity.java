package com.example.week10.community;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week10.App;
import com.example.week10.databinding.ActivityRankingBinding;
import com.example.week10.model.AccountProfile;

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
        List<AccountProfile> ranking = app.getCommunityRepository().getRanking();
        // 내 계정에 "(나)" 표시를 붙이기 위해 현재 로그인 아이디를 넘긴다
        String currentId = app.getAccountManager().getCurrentAccountId();

        boolean isEmpty = ranking.isEmpty();
        binding.textViewRankingEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerRanking.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        binding.recyclerRanking.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRanking.setAdapter(new RankingAdapter(ranking, currentId, app.getUserPrefs()));
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
