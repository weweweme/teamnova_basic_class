package com.example.week12.rawg;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.data.KoreanQueryResolver;
import com.example.week12.data.RawgApi;
import com.example.week12.data.RawgPageCallback;
import com.example.week12.databinding.ActivityRawgSearchBinding;
import com.example.week12.model.RawgGame;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// RAWG 게임 검색 화면
/// 검색어를 입력하면 RAWG에 물어보고, 결과를 리스트로 보여준다. 바닥까지 스크롤하면 다음 페이지를 이어 붙인다(무한 스크롤).
///
/// ──── 화면 상태 3가지 (같은 자리에 겹쳐 두고 하나만 보여줌) ────
/// 로딩:    첫 검색 중 → 스피너만
/// 결과:    성공 + 결과 있음 → 리스트만 (다음 페이지는 리스트 아래에 조용히 이어붙임)
/// 빈 상태: 검색 전 안내 / 결과 없음 / 실패 → 안내 문구만
///
/// 통신은 RawgApi가 서브 스레드에서 처리하고 결과 콜백은 메인 스레드로 온다(11주차)
/// → 콜백 안에서 바로 화면을 만져도 안전. 항목 탭 → 상태 선택 후 보관함 추가(RawgGameAdder).
/// </summary>
public class RawgSearchActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityRawgSearchBinding binding;

    /// <summary>
    /// RAWG 검색 호출 담당 (서브 스레드 + 파싱은 이 안에서 처리)
    /// </summary>
    private final RawgApi rawgApi = new RawgApi();

    /// <summary>
    /// 한글 검색어를 영어로 바꿔주는 공용 보정기 (AI→번역→원문 폴백을 안에서 다 처리)
    /// </summary>
    private final KoreanQueryResolver queryResolver = new KoreanQueryResolver();

    /// <summary>
    /// 결과 리스트 어댑터 (첫 페이지는 updateItems로 교체, 다음 페이지는 appendItems로 이어붙임)
    /// </summary>
    private RawgResultAdapter adapter;

    /// <summary>
    /// 스크롤 위치 계산용 (무한 스크롤 리스너가 사용)
    /// </summary>
    private LinearLayoutManager layoutManager;

    /// <summary>
    /// 지금 보고 있는 검색어 (다음 페이지도 같은 검색어로 이어서 불러오기 위해 기억)
    /// </summary>
    private String currentQuery = "";

    /// <summary>
    /// 다음에 불러올 페이지 번호 (1부터 시작, 페이지를 성공적으로 받을 때마다 +1)
    /// </summary>
    private int nextPage = 1;

    /// <summary>
    /// 지금 페이지를 불러오는 중인지 (중복 요청 방지 — 스크롤 중 여러 번 안 부르게)
    /// </summary>
    private boolean isLoading = false;

    /// <summary>
    /// 다음 페이지가 더 있는지 (RAWG 응답의 next 유무). false면 더 안 불러온다
    /// </summary>
    private boolean hasNext = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRawgSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 결과 RecyclerView: 세로 리스트 + 빈 어댑터로 시작
        adapter = new RawgResultAdapter(new ArrayList<>(), this::onResultClick);
        layoutManager = new LinearLayoutManager(this);
        binding.recyclerViewResults.setLayoutManager(layoutManager);
        binding.recyclerViewResults.setAdapter(adapter);

        // 바닥 근처까지 스크롤하면 다음 페이지 요청 (무한 스크롤)
        binding.recyclerViewResults.addOnScrollListener(
                new InfiniteScrollListener(layoutManager, this::loadMore));

        // 검색 버튼 클릭 → 검색 실행
        binding.buttonSearch.setOnClickListener(v -> doSearch());

        // 키보드의 "검색" 엔터로도 검색 실행 (imeOptions=actionSearch와 짝)
        binding.editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });

        // 검색 전 안내 문구 (빈 상태)
        showEmpty("게임 이름을 검색해보세요");
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

    // ========== 검색 ==========

    /// <summary>
    /// 입력한 검색어로 새 검색을 시작한다
    ///
    /// 검색어에 한글이 있으면(RAWG는 영어 전용) 공용 보정기로 영어로 바꾼 뒤 검색한다
    /// (AI→번역→원문 폴백은 KoreanQueryResolver가 안에서 처리). 영어면 바로 검색.
    /// </summary>
    private void doSearch() {
        String query = "";
        if (binding.editTextSearch.getText() != null) {
            query = binding.editTextSearch.getText().toString().trim();
        }

        if (query.isEmpty()) {
            Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // 한글이면 보정에 시간이 걸리니 미리 로딩 표시 (영어면 아래 콜백이 즉시 불림)
        if (KoreanQueryResolver.hasKorean(query)) {
            showLoading();
        }
        // 영어로 바뀐(또는 원래 영어인) 검색어로 실제 검색 시작
        queryResolver.toEnglish(query, this::startSearch);
    }

    /// <summary>
    /// 실제 검색 시작 (첫 페이지부터) — 페이지 상태 초기화 후 첫 페이지 로드
    /// </summary>
    private void startSearch(String query) {
        currentQuery = query;
        nextPage = 1;
        hasNext = false;

        showLoading();
        loadPage(true);   // 첫 페이지
    }

    /// <summary>
    /// 바닥 근처에서 호출됨 — 다음 페이지를 이어서 불러온다
    /// 이미 불러오는 중이거나 다음 페이지가 없으면 아무것도 안 함 (중복/헛요청 방지)
    /// </summary>
    private void loadMore() {
        if (isLoading || !hasNext || currentQuery.isEmpty()) {
            return;
        }
        loadPage(false);   // 다음 페이지 (이어붙이기)
    }

    /// <summary>
    /// 현재 검색어의 nextPage 페이지를 불러온다
    /// firstPage=true면 결과로 목록을 "교체"하고, false면 기존 목록 뒤에 "이어붙인다"
    /// </summary>
    private void loadPage(boolean firstPage) {
        isLoading = true;

        rawgApi.search(currentQuery, nextPage, new RawgPageCallback() {
            @Override
            public void onSuccess(List<RawgGame> results, boolean more) {
                isLoading = false;
                hasNext = more;
                nextPage++;

                if (firstPage) {
                    if (results.isEmpty()) {
                        showEmpty("검색 결과가 없어요");
                        return;
                    }
                    adapter.updateItems(results);
                    showResults();
                } else {
                    adapter.appendItems(results);   // 다음 페이지를 리스트 아래에 이어붙임
                }
            }

            @Override
            public void onError(String message) {
                isLoading = false;
                // 첫 페이지 실패면 빈 상태로, 다음 페이지 실패면 리스트는 유지하고 안내만
                if (firstPage) {
                    showEmpty("불러오지 못했어요");
                }
                Toast.makeText(RawgSearchActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /// <summary>
    /// 결과 항목 클릭 → 공용 헬퍼로 "상태 선택 → 보관함 추가"
    /// 추가가 끝나면 finish로 보관함에 복귀 (onResume에서 목록 새로고침 → 추가된 게임이 보임)
    /// </summary>
    private void onResultClick(RawgGame game) {
        RawgGameAdder.promptAddToLibrary(this, game, this::finish);
    }

    // ========== 화면 상태 전환 (셋 중 하나만 표시) ==========

    /// <summary>
    /// 로딩 상태 — 스피너만 표시 (첫 페이지 검색 중)
    /// </summary>
    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewResults.setVisibility(View.GONE);
        binding.textViewEmpty.setVisibility(View.GONE);
    }

    /// <summary>
    /// 결과 상태 — 리스트만 표시
    /// </summary>
    private void showResults() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewResults.setVisibility(View.VISIBLE);
        binding.textViewEmpty.setVisibility(View.GONE);
    }

    /// <summary>
    /// 빈 상태 — 안내 문구만 표시 (검색 전 안내 / 결과 없음 / 실패 공용)
    /// </summary>
    private void showEmpty(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewResults.setVisibility(View.GONE);
        binding.textViewEmpty.setVisibility(View.VISIBLE);
        binding.textViewEmpty.setText(message);
    }
}
