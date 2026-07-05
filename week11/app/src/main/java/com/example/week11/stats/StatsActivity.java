package com.example.week11.stats;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.data.GameRepository;
import com.example.week11.databinding.ActivityStatsBinding;
import com.example.week11.model.GameStatus;
import com.example.week11.model.Genre;

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

    /// <summary>
    /// 통계 집계 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// 숫자 집계는 서브 스레드에서 하고, 막대 그래프 그리기만 이 Handler로 메인에 넘긴다
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

        loadStatsAsync();
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 메인 큐에 남아있는 통계 반영 작업을 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    /// <summary>
    /// 통계 숫자 집계는 백그라운드에서, 막대 그래프 그리기는 메인에서
    /// - 집계(getTotalCount/countByStatus/countByGenre 등)는 데이터만 읽으므로 서브 스레드에서 가능
    /// - 뷰 생성(inflate/setText)은 메인 스레드에서만 가능 → 집계 결과를 Handler로 넘겨 그린다
    /// </summary>
    private void loadStatsAsync() {
        // 로딩 스피너 표시, 내용은 숨김 (아직 집계 전)
        binding.progressStats.setVisibility(View.VISIBLE);
        binding.scrollStatsContent.setVisibility(View.GONE);

        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 통계가 무겁다고 가정하고 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // ⏳ 숫자 집계 (데이터 읽기) — 서브 스레드에서 미리 계산해 둔다
            // 상태별 게임 수 (전체 + 상태 4종)
            int total = gameRepository.getTotalCount();
            GameStatus[] statuses = GameStatus.values();
            int[] statusCounts = new int[statuses.length];
            for (int i = 0; i < statuses.length; i++) {
                statusCounts[i] = gameRepository.countByStatus(statuses[i]);
            }

            // 별점 분포
            int[] ratingDist = gameRepository.getRatingDistribution();
            int unrated = gameRepository.countUnrated();

            // 장르 분포 (개수 있는 장르만, 많은 순 정렬)
            List<Genre> genres = new ArrayList<>();
            for (Genre g : Genre.values()) {
                if (gameRepository.countByGenre(g) > 0) {
                    genres.add(g);
                }
            }
            genres.sort(Comparator.comparingInt(
                    (Genre g) -> gameRepository.countByGenre(g)).reversed());
            int[] genreCounts = new int[genres.size()];
            for (int i = 0; i < genres.size(); i++) {
                genreCounts[i] = gameRepository.countByGenre(genres.get(i));
            }

            // 집계 끝 → 화면 그리기는 메인 줄로 넘김 (뷰 생성은 메인에서만)
            mainHandler.post(() -> {
                renderStatusSummary(total, statuses, statusCounts);
                renderRatingDistribution(ratingDist, unrated);
                renderGenreDistribution(genres, genreCounts);

                // 다 그렸으면 스피너 숨기고 내용 표시
                binding.progressStats.setVisibility(View.GONE);
                binding.scrollStatsContent.setVisibility(View.VISIBLE);
            });
        }).start();                                     // 서브 스레드 실행
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
    /// 상태별 게임 수를 행으로 그림 ("전체 20", "완료 7" 등) — 미리 집계한 값 사용
    /// 전체 + GameStatus 4종을 순서대로
    /// </summary>
    private void renderStatusSummary(int total, GameStatus[] statuses, int[] statusCounts) {
        binding.layoutStatusSummary.removeAllViews();

        // 전체
        addStatusRow(getString(R.string.stats_status_total), total);
        // 상태별 (백그라운드에서 미리 센 값)
        for (int i = 0; i < statuses.length; i++) {
            addStatusRow(statuses[i].getDisplayName(), statusCounts[i]);
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
    /// 별점 분포 막대 그래프 (0.5단위 10단계, 5.0부터 위→아래) — 미리 집계한 값 사용
    /// </summary>
    private void renderRatingDistribution(int[] distribution, int unratedCount) {
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
    private void renderGenreDistribution(List<Genre> genres, int[] genreCounts) {
        // 막대 비율 기준이 될 최댓값 (가장 많은 장르 = 100%)
        int maxCount = 1;
        for (int count : genreCounts) {
            maxCount = Math.max(maxCount, count);
        }

        binding.layoutGenreDistribution.removeAllViews();

        for (int i = 0; i < genres.size(); i++) {
            Genre genre = genres.get(i);
            int count = genreCounts[i];

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
