package com.example.week12.rawg;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.data.RawgApi;
import com.example.week12.data.RawgPageCallback;
import com.example.week12.databinding.ActivityExploreBinding;
import com.example.week12.model.RawgGame;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 탐색 화면 — RAWG에서 게임을 "둘러보는" 화면 (인기/신작/장르별 탭)
///
/// 검색(RawgSearchActivity)이 "이름으로 찾기"라면, 탐색은 "구경하기".
/// 결과 리스트·무한 스크롤·"탭 → 보관함 추가"는 검색 화면과 같은 부품을 재사용한다
///   (RawgResultAdapter, InfiniteScrollListener, RawgGameAdder). 여기서 새로 다루는 건 "모드 탭"뿐이다.
///
/// 바닥까지 스크롤하면 현재 탭의 다음 페이지를 이어 붙인다(무한 스크롤).
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

    /// <summary>
    /// 스크롤 위치 계산용 (무한 스크롤 리스너가 사용)
    /// </summary>
    private LinearLayoutManager layoutManager;

    /// <summary>
    /// 지금 보고 있는 탭 위치 (다음 페이지도 같은 탭 기준으로 이어 불러오기 위해 기억)
    /// </summary>
    private int currentPosition = 0;

    /// <summary>
    /// 다음에 불러올 페이지 번호 (1부터)
    /// </summary>
    private int nextPage = 1;

    /// <summary>
    /// 지금 페이지를 불러오는 중인지 (중복 요청 방지)
    /// </summary>
    private boolean isLoading = false;

    /// <summary>
    /// 다음 페이지가 더 있는지 (RAWG next 유무)
    /// </summary>
    private boolean hasNext = false;

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
        layoutManager = new LinearLayoutManager(this);
        binding.recyclerViewResults.setLayoutManager(layoutManager);
        binding.recyclerViewResults.setAdapter(adapter);

        // 바닥 근처까지 스크롤하면 다음 페이지 요청 (무한 스크롤)
        binding.recyclerViewResults.addOnScrollListener(
                new InfiniteScrollListener(layoutManager, this::loadMore));

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
    /// 모드 탭을 만들고, 탭을 고르면 해당 목록을 첫 페이지부터 불러오게 연결. 첫 탭(인기)을 바로 로드.
    /// </summary>
    private void setupTabs() {
        for (String label : MODE_LABELS) {
            binding.tabLayoutMode.addTab(binding.tabLayoutMode.newTab().setText(label));
        }

        binding.tabLayoutMode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectMode(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 같은 탭을 다시 누르면 새로고침
                selectMode(tab.getPosition());
            }
        });

        // 첫 진입: 인기 탭(0)을 바로 불러온다
        selectMode(0);
    }

    /// <summary>
    /// 탭을 골랐을 때 — 페이지 상태를 초기화하고 첫 페이지를 불러온다
    /// </summary>
    private void selectMode(int position) {
        currentPosition = position;
        nextPage = 1;
        hasNext = false;

        showLoading();
        loadPage(true);
    }

    /// <summary>
    /// 바닥 근처에서 호출됨 — 현재 탭의 다음 페이지를 이어서 불러온다
    /// 이미 불러오는 중이거나 다음 페이지가 없으면 아무것도 안 함
    /// </summary>
    private void loadMore() {
        if (isLoading || !hasNext) {
            return;
        }
        loadPage(false);
    }

    /// <summary>
    /// 현재 탭(currentPosition)의 nextPage 페이지를 불러온다
    /// firstPage=true면 목록을 "교체", false면 기존 목록 뒤에 "이어붙임"
    /// </summary>
    private void loadPage(boolean firstPage) {
        isLoading = true;

        RawgPageCallback callback = new RawgPageCallback() {
            @Override
            public void onSuccess(List<RawgGame> results, boolean more) {
                isLoading = false;
                hasNext = more;
                nextPage++;

                if (firstPage) {
                    if (results.isEmpty()) {
                        showEmpty("결과가 없어요");
                        return;
                    }
                    adapter.updateItems(results);
                    showResults();
                } else {
                    adapter.appendItems(results);
                }
            }

            @Override
            public void onError(String message) {
                isLoading = false;
                if (firstPage) {
                    showEmpty("불러오지 못했어요");
                }
                Toast.makeText(ExploreActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };

        // 탭 위치에 맞는 조회 (0=인기, 1=신작, 그 외=장르별)
        if (currentPosition == 0) {
            rawgApi.discoverPopular(nextPage, callback);
        } else if (currentPosition == 1) {
            rawgApi.discoverNew(nextPage, callback);
        } else {
            rawgApi.discoverByGenre(MODE_GENRE_SLUGS[currentPosition], nextPage, callback);
        }
    }

    // ========== 화면 상태 전환 (셋 중 하나만 표시) ==========

    /// <summary>
    /// 로딩 상태 — 스피너만 (첫 페이지 로딩 중)
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
