package com.example.week12.rawg;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.data.RawgApi;
import com.example.week12.data.RawgSearchCallback;
import com.example.week12.databinding.ActivityExploreBinding;
import com.example.week12.model.RawgGame;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 탐색 화면 — RAWG에서 게임을 "둘러보는" 화면
///
/// 검색(RawgSearchActivity)이 "이름으로 찾기"라면, 탐색은 "구경하기":
///   인기 / 신작 / 장르별 목록을 탭으로 전환하며 본다.
/// 결과 리스트와 "탭 → 보관함 추가"는 검색 화면과 완전히 동일한 부품을 재사용한다
///   (RawgResultAdapter, RawgGameAdder). 여기서 새로 다루는 건 "모드 탭"뿐이다.
///
/// 상태 3가지(로딩/결과/빈 상태)는 검색 화면과 같은 방식으로 겹쳐 두고 하나만 보여준다.
/// </summary>
public class ExploreActivity extends AppCompatActivity {

    /// <summary>
    /// 탭에 표시할 모드 이름들 (0=인기, 1=신작, 그 뒤는 장르)
    /// </summary>
    private static final String[] MODE_LABELS = {
            "인기", "신작", "액션", "RPG", "전략", "시뮬레이션", "어드벤처"
    };

    /// <summary>
    /// 각 모드의 RAWG 장르 코드(slug). 인기·신작은 장르가 아니라 null.
    /// (MODE_LABELS와 같은 순서 — 같은 index끼리 짝)
    /// </summary>
    private static final String[] MODE_GENRE_SLUGS = {
            null, null, "action", "role-playing-games-rpg", "strategy", "simulation", "adventure"
    };

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityExploreBinding binding;

    /// <summary>
    /// RAWG 조회 담당
    /// </summary>
    private final RawgApi rawgApi = new RawgApi();

    /// <summary>
    /// 결과 리스트 어댑터 (검색 화면과 같은 것 재사용)
    /// </summary>
    private RawgResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityExploreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar ← 뒤로가기 버튼
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 결과 리스트: 세로 + 빈 어댑터. 탭 → 보관함 추가 후 목록 새로고침(방금 추가한 게임에 '보관함' 배지가 바로 뜨게)
        adapter = new RawgResultAdapter(new ArrayList<>(),
                game -> RawgGameAdder.promptAddToLibrary(this, game,
                        () -> adapter.notifyDataSetChanged()));
        binding.recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewResults.setAdapter(adapter);

        setupTabs();
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

    /// <summary>
    /// 모드 탭을 만들고, 탭을 고르면 해당 목록을 불러오게 연결. 첫 탭(인기)을 바로 로드.
    /// </summary>
    private void setupTabs() {
        for (String label : MODE_LABELS) {
            binding.tabLayoutMode.addTab(binding.tabLayoutMode.newTab().setText(label));
        }

        binding.tabLayoutMode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadMode(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 같은 탭을 다시 누르면 새로고침
                loadMode(tab.getPosition());
            }
        });

        // 첫 진입: 인기 탭(0)을 바로 불러온다
        loadMode(0);
    }

    /// <summary>
    /// 선택된 탭 위치에 맞는 목록을 RAWG에서 불러온다 (0=인기, 1=신작, 그 외=장르별)
    /// </summary>
    private void loadMode(int position) {
        showLoading();

        RawgSearchCallback callback = new RawgSearchCallback() {
            @Override
            public void onSuccess(List<RawgGame> results) {
                if (results.isEmpty()) {
                    showEmpty("결과가 없어요");
                    return;
                }
                adapter.updateItems(results);
                showResults();
            }

            @Override
            public void onError(String message) {
                showEmpty("불러오지 못했어요");
                Toast.makeText(ExploreActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };

        if (position == 0) {
            rawgApi.discoverPopular(callback);
        } else if (position == 1) {
            rawgApi.discoverNew(callback);
        } else {
            rawgApi.discoverByGenre(MODE_GENRE_SLUGS[position], callback);
        }
    }

    // ========== 화면 상태 전환 (셋 중 하나만 표시) ==========

    /// <summary>
    /// 로딩 상태 — 스피너만
    /// </summary>
    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewResults.setVisibility(View.GONE);
        binding.textViewEmpty.setVisibility(View.GONE);
    }

    /// <summary>
    /// 결과 상태 — 리스트만
    /// </summary>
    private void showResults() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewResults.setVisibility(View.VISIBLE);
        binding.textViewEmpty.setVisibility(View.GONE);
    }

    /// <summary>
    /// 빈 상태 — 안내 문구만 (결과 없음 / 실패 공용)
    /// </summary>
    private void showEmpty(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewResults.setVisibility(View.GONE);
        binding.textViewEmpty.setVisibility(View.VISIBLE);
        binding.textViewEmpty.setText(message);
    }
}
