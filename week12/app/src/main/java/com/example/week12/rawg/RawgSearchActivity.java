package com.example.week12.rawg;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.App;
import com.example.week12.account.UserPrefs;
import com.example.week12.data.GameRepository;
import com.example.week12.data.RawgApi;
import com.example.week12.data.RawgSearchCallback;
import com.example.week12.databinding.ActivityRawgSearchBinding;
import com.example.week12.model.ActivityLogType;
import com.example.week12.model.Game;
import com.example.week12.model.GameStatus;
import com.example.week12.model.Genre;
import com.example.week12.model.Platform;
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
    /// 결과 항목 클릭 처리 — 어떤 상태로 담을지 먼저 물어본다
    ///
    /// 검색으로 추가하는 게임은 이미 플레이했을 수도 있어 "찜 목록"으로 단정하지 않는다.
    /// 상태 목록(플레이중/완료/중단/찜 목록)을 보여주고, 고른 상태로 addGameWithStatus 진행.
    /// </summary>
    private void onResultClick(RawgGame game) {
        // 상태 목록의 표시 이름들을 뽑아 선택지로 (enum 순서 그대로 → 고른 index로 상태를 되찾음)
        GameStatus[] statuses = GameStatus.values();
        String[] labels = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            labels[i] = statuses[i].getDisplayName();
        }

        new AlertDialog.Builder(this)
                .setTitle("어떤 상태로 추가할까요?")
                .setItems(labels, (dialog, which) -> addGameWithStatus(game, statuses[which]))
                .setNegativeButton("취소", null)
                .show();
    }

    /// <summary>
    /// 고른 상태로 게임을 보관함에 추가하고 복귀
    ///
    /// 흐름: RawgGame → (장르/플랫폼 매핑) → repository.addGame(상태 지정) → 최근 활동 기록 → 토스트 → finish
    /// finish 후 보관함(LibraryActivity)의 onResume이 목록을 새로고침해 추가된 게임이 보인다.
    /// </summary>
    private void addGameWithStatus(RawgGame game, GameStatus status) {
        App app = (App) getApplication();
        GameRepository repository = app.getGameRepository();

        // RAWG 슬러그 목록 → 우리 enum (매핑되는 첫 항목, 없으면 기타)
        Genre genre = RawgGameMapper.toGenre(game.getGenreSlugs());
        Platform platform = RawgGameMapper.toPlatform(game.getPlatformSlugs());

        // 보관함에 추가: 제목·장르·플랫폼·표지·상태 + RAWG 정식 id를 채움 (수동 입력 대체)
        // storeUrl은 RAWG 검색 목록에 없어 빈 값. 표지는 https 원격 주소를 coverUri로 저장 →
        // 보관함 그리드가 loadUri로 표시(P3에서 원격 로딩 지원). 표지 없는 게임이면 null → 기본 아이콘
        // rawgId: 나중에 서버/소셜에서 "같은 게임"을 사용자 간에 맞추는 공통 키로 심어둔다
        Game added = repository.addGame(
                game.getName(), genre, platform, "", game.getCoverImageUrl(), status,
                game.getRawgId());

        // 최근 활동 피드에 "추가함" 기록 (수동 추가와 동일 — 로그인 상태에서만 userPrefs 존재)
        UserPrefs userPrefs = app.getUserPrefs();
        if (userPrefs != null) {
            userPrefs.addActivityLog(ActivityLogType.ADDED, added.getId(), "");
        }

        Toast.makeText(this, "보관함에 추가됨: " + added.getTitle(), Toast.LENGTH_SHORT).show();
        finish();   // 보관함으로 복귀 → onResume에서 목록 새로고침
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
