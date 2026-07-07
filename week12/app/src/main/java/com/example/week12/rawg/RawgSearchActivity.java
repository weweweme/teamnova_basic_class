package com.example.week12.rawg;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.data.RawgApi;
import com.example.week12.data.RawgSearchCallback;
import com.example.week12.databinding.ActivityRawgSearchBinding;
import com.example.week12.model.RawgGame;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// RAWG 게임 검색 화면
/// 검색어를 입력하면 RAWG에 물어보고, 결과를 리스트로 보여준다.
///
/// ──── 화면 상태 3가지 (같은 자리에 겹쳐 두고 하나만 보여줌) ────
/// 로딩:    검색 중 → 스피너만
/// 결과:    성공 + 결과 있음 → 리스트만
/// 빈 상태: 검색 전 안내 / 결과 없음 / 실패 → 안내 문구만
///
/// 검색 자체는 RawgApi가 서브 스레드에서 처리하고, 결과 콜백은 메인 스레드로 온다(11주차)
/// → 콜백 안에서 바로 화면(리스트/스피너)을 만져도 안전
///
/// 현재(P2)는 결과를 "보여주기"까지만. 항목을 탭하면 보관함에 추가하는 건 P4에서 연결.
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
    /// 결과 리스트 어댑터 (처음엔 빈 목록 → 검색 성공 시 updateItems로 채움)
    /// </summary>
    private RawgResultAdapter adapter;

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
        binding.recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewResults.setAdapter(adapter);

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
    /// 입력한 검색어로 RAWG 검색을 실행
    /// 검색어가 비어있으면 안내만 하고 실행하지 않는다
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

        // 로딩 상태로 전환 (스피너만 보이게)
        showLoading();

        // 서브 스레드에서 검색 → 결과는 메인 스레드로 콜백 (11주차)
        rawgApi.search(query, new RawgSearchCallback() {
            @Override
            public void onSuccess(List<RawgGame> results) {
                if (results.isEmpty()) {
                    showEmpty("검색 결과가 없어요");
                    return;
                }
                // 결과 표시 (리스트만 보이게)
                adapter.updateItems(results);
                showResults();
            }

            @Override
            public void onError(String message) {
                // 실패 — 03 주의점 (인터넷/서버 문제). 안내 문구 + 토스트
                showEmpty("불러오지 못했어요");
                Toast.makeText(RawgSearchActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /// <summary>
    /// 결과 항목 클릭 처리
    /// 현재(P2)는 어떤 게임을 골랐는지 토스트로만 확인. 보관함 추가는 P4에서 연결.
    /// </summary>
    private void onResultClick(RawgGame game) {
        Toast.makeText(this, "선택: " + game.getName(), Toast.LENGTH_SHORT).show();
    }

    // ========== 화면 상태 전환 (셋 중 하나만 표시) ==========

    /// <summary>
    /// 로딩 상태 — 스피너만 표시
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
