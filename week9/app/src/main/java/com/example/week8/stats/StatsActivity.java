package com.example.week8.stats;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.App;
import com.example.week8.R;
import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ActivityStatsBinding;
import com.example.week8.model.GameStatus;
import com.example.week8.model.Genre;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/// <summary>
/// 통계 화면
/// 상태별 게임 수 + 별점 분포 그래프를 한 화면에 모아 보여줌
/// 홈의 통계 요약 카드를 탭하면 진입 (요약 → 상세)
///
/// 행(상태/별점 단계)은 데이터 기반이라 XML에 고정하지 않고 코드로 동적 생성
/// </summary>
public class StatsActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityStatsBinding binding;

    /// <summary>
    /// 게임 데이터 저장소 (App 공용 인스턴스)
    /// </summary>
    private GameRepository gameRepository;

    // ========== Lifecycle ==========

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        gameRepository = ((App) getApplication()).getGameRepository();

        setupStatusSummary();
        setupRatingDistribution();
        setupGenreDistribution();
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

    // ========== 상태별 게임 수 ==========

    /// <summary>
    /// 상태별 게임 수를 행으로 생성 ("전체 20", "완료 7" 등)
    /// 전체 + GameStatus 4종을 순서대로
    /// </summary>
    private void setupStatusSummary() {
        binding.layoutStatusSummary.removeAllViews();

        // 전체
        addStatusRow(getString(R.string.stats_status_total), gameRepository.getTotalCount());
        // 상태별
        for (GameStatus status : GameStatus.values()) {
            addStatusRow(status.getDisplayName(), gameRepository.countByStatus(status));
        }
    }

    /// <summary>
    /// "라벨 ........ 숫자" 형태의 한 행을 만들어 컨테이너에 추가
    /// </summary>
    private void addStatusRow(String label, int count) {
        View row = getLayoutInflater().inflate(
                R.layout.item_stat_row, binding.layoutStatusSummary, false);
        ((TextView) row.findViewById(R.id.textViewStatLabel)).setText(label);
        ((TextView) row.findViewById(R.id.textViewStatCount)).setText(String.valueOf(count));
        binding.layoutStatusSummary.addView(row);
    }

    // ========== 별점 분포 그래프 ==========

    /// <summary>
    /// 별점 분포 막대 그래프 (0.5단위 10단계, 5.0부터 위→아래)
    /// HomeActivity에 있던 로직을 이 화면으로 이동
    /// </summary>
    private void setupRatingDistribution() {
        int[] distribution = gameRepository.getRatingDistribution();
        int unratedCount = gameRepository.countUnrated();

        // 막대 비율 기준 최댓값 — 별점 단계와 미평가까지 포함해 가장 큰 값
        int maxCount = Math.max(1, unratedCount);
        for (int count : distribution) {
            maxCount = Math.max(maxCount, count);
        }

        binding.layoutRatingDistribution.removeAllViews();

        for (int i = distribution.length - 1; i >= 0; i--) {
            float star = (i + 1) * 0.5f;
            int count = distribution[i];

            View row = getLayoutInflater().inflate(
                    R.layout.item_rating_bar, binding.layoutRatingDistribution, false);

            TextView label = row.findViewById(R.id.textViewRatingLabel);
            ProgressBar bar = row.findViewById(R.id.progressBar);
            TextView countText = row.findViewById(R.id.textViewRatingCount);

            label.setText("★ " + star);
            bar.setMax(maxCount);
            bar.setProgress(count);
            countText.setText(String.valueOf(count));

            binding.layoutRatingDistribution.addView(row);
        }

        // 별점 막대 아래에 "미평가" 행 추가 (아직 별점 안 매긴 게임)
        View unratedRow = getLayoutInflater().inflate(
                R.layout.item_rating_bar, binding.layoutRatingDistribution, false);
        TextView unratedLabel = unratedRow.findViewById(R.id.textViewRatingLabel);
        ProgressBar unratedBar = unratedRow.findViewById(R.id.progressBar);
        TextView unratedText = unratedRow.findViewById(R.id.textViewRatingCount);
        unratedLabel.setText(R.string.stats_unrated);
        unratedBar.setMax(maxCount);
        unratedBar.setProgress(unratedCount);
        unratedText.setText(String.valueOf(unratedCount));
        binding.layoutRatingDistribution.addView(unratedRow);
    }

    // ========== 장르 분포 그래프 ==========

    /// <summary>
    /// 장르 분포 막대 그래프
    /// 게임이 1개 이상 있는 장르만, 개수 많은 순으로 표시
    /// (장르 11종을 다 표시하면 0개 빈 막대가 많아 보기 나쁨)
    /// </summary>
    private void setupGenreDistribution() {
        // 1) 개수 있는 장르만 모으기
        List<Genre> genres = new ArrayList<>();
        for (Genre genre : Genre.values()) {
            if (gameRepository.countByGenre(genre) > 0) {
                genres.add(genre);
            }
        }

        // 2) 개수 많은 순(내림차순) 정렬
        genres.sort(Comparator.comparingInt(
                (Genre g) -> gameRepository.countByGenre(g)).reversed());

        // 막대 비율 기준이 될 최댓값 (가장 많은 장르 = 100%)
        int maxCount = 1;
        for (Genre genre : genres) {
            maxCount = Math.max(maxCount, gameRepository.countByGenre(genre));
        }

        binding.layoutGenreDistribution.removeAllViews();

        for (Genre genre : genres) {
            int count = gameRepository.countByGenre(genre);

            View row = getLayoutInflater().inflate(
                    R.layout.item_genre_bar, binding.layoutGenreDistribution, false);

            TextView label = row.findViewById(R.id.textViewGenreLabel);
            ProgressBar bar = row.findViewById(R.id.progressBar);
            TextView countText = row.findViewById(R.id.textViewGenreCount);

            label.setText(genre.getDisplayName());
            bar.setMax(maxCount);
            bar.setProgress(count);
            countText.setText(String.valueOf(count));

            binding.layoutGenreDistribution.addView(row);
        }
    }
}
