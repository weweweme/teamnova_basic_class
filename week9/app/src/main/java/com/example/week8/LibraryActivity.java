package com.example.week8;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.week8.ui.GameSortOrder;

import java.util.Comparator;

import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ActivityLibraryBinding;
import com.example.week8.databinding.BottomSheetGameActionsBinding;
import com.example.week8.model.Game;
import com.example.week8.model.GameStatus;
import com.example.week8.model.Genre;
import com.example.week8.model.Platform;
import com.example.week8.ui.LibraryAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 보관함 화면 (게임 표지 격자 + 상태별 필터 탭)
/// 앱의 메인 컬렉션 화면 — 내 게임 전체를 표지 격자로 보고, 상태 탭으로 필터링
///
/// ──── 이 화면이 가진 기능 ────
/// - 상태별 필터 탭 (전체/플레이중/완료/중단/찜 목록)
/// - 게임 추가(+ 메뉴) / 앱 정보(⋮ 메뉴)
/// - 셀 클릭 → 상세, 셀 길게 누르기 → BottomSheet(삭제/공유)
/// - 다른 앱의 "공유"로 들어온 텍스트 수신 (Intent Filter)
///
/// ──── Lifecycle 학습 ────
/// onResume: 다른 화면에서 상태/별점 변경 후 돌아오면 현재 탭 기준으로 다시 필터
/// onNewIntent: 앱이 살아있을 때 다른 앱의 공유로 다시 호출될 때 처리
/// </summary>
public class LibraryActivity extends AppCompatActivity {

    /// <summary>
    /// 격자에 한 줄당 표시할 열 개수 (2 ↔ 3 바꿔보며 관찰)
    /// </summary>
    private static final int GRID_SPAN_COUNT = 2;

    /// <summary>
    /// "전체" 탭의 위치 (0번). 1번부터 GameStatus.values()와 1:1 대응
    /// </summary>
    private static final int TAB_POSITION_ALL = 0;

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityLibraryBinding binding;

    /// <summary>
    /// 게임 데이터 저장소 (App 공용 인스턴스)
    /// </summary>
    private GameRepository gameRepository;

    /// <summary>
    /// 격자 셀을 그리는 어댑터
    /// </summary>
    private LibraryAdapter adapter;

    /// <summary>
    /// 게임 추가 화면을 실행하고 결과를 받는 런처
    /// </summary>
    private ActivityResultLauncher<Intent> addGameLauncher;

    /// <summary>
    /// 현재 검색어 (SearchView 입력). 비어있으면 검색 안 함
    /// 상태 탭 필터 결과 안에서 제목으로 한 번 더 거르는 데 사용
    /// </summary>
    private String currentQuery = "";

    /// <summary>
    /// 현재 정렬 기준 (FAB 다이얼로그에서 선택). 기본은 최근 추가순
    /// </summary>
    private GameSortOrder currentSort = GameSortOrder.RECENT;

    // ========== Lifecycle ==========

    /// <summary>
    /// 보관함 화면 생성
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // App 공용 GameRepository
        gameRepository = ((App) getApplication()).getGameRepository();

        // RecyclerView 설정 (GridLayoutManager 격자)
        binding.recyclerViewLibrary.setLayoutManager(
                new GridLayoutManager(this, GRID_SPAN_COUNT));
        // 클릭 → 상세, 길게 누르기 → BottomSheet
        adapter = new LibraryAdapter(
                gameRepository.getAllGames(), this::onGameClick, this::onGameLongClick);
        binding.recyclerViewLibrary.setAdapter(adapter);

        setupFilterTabs();

        // 정렬 FAB → 정렬 옵션 다이얼로그
        binding.fabSort.setOnClickListener(v -> showSortDialog());

        // 게임 추가 결과 런처 등록
        addGameLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    boolean isOk = result.getResultCode() == RESULT_OK;
                    boolean hasData = result.getData() != null;
                    if (!isOk || !hasData) {
                        return;
                    }

                    Intent data = result.getData();
                    String title = data.getStringExtra(AddGameActivity.EXTRA_TITLE);
                    String genreName = data.getStringExtra(AddGameActivity.EXTRA_GENRE);
                    String platformName = data.getStringExtra(AddGameActivity.EXTRA_PLATFORM);
                    String storeUrl = data.getStringExtra(AddGameActivity.EXTRA_STORE_URL);

                    // enum은 name() 문자열로 건너왔으므로 valueOf로 복원
                    Genre genre = Genre.valueOf(genreName);
                    Platform platform = Platform.valueOf(platformName);

                    // 저장소에 추가 후 현재 탭 기준으로 다시 필터 (새 게임은 찜 목록 기본)
                    gameRepository.addGame(title, genre, platform, storeUrl);
                    applyCurrentFilter();
                }
        );

        // 다른 앱에서 공유로 실행됐다면 받은 텍스트 처리
        handleIncomingShareIntent(getIntent());
    }

    /// <summary>
    /// 앱이 살아있는 상태에서 다른 앱이 공유로 다시 부르면 호출됨
    /// </summary>
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingShareIntent(intent);
    }

    /// <summary>
    /// 다른 화면에서 상태/데이터 변경 후 돌아왔을 때 현재 탭 기준으로 다시 필터
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();
        applyCurrentFilter();
    }

    // ========== 공유 수신 ==========

    /// <summary>
    /// 다른 앱에서 "ACTION_SEND + text/plain"으로 실행했을 때 받은 텍스트 처리
    /// 지금은 학습용으로 Toast만 표시
    /// </summary>
    private void handleIncomingShareIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        boolean isSendAction = Intent.ACTION_SEND.equals(intent.getAction());
        boolean isTextType = "text/plain".equals(intent.getType());
        if (!isSendAction || !isTextType) {
            return;
        }

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null || sharedText.isEmpty()) {
            return;
        }

        String message = getString(R.string.main_shared_text, sharedText);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // ========== 필터 탭 ==========

    /// <summary>
    /// 필터 탭 구성: "전체" + GameStatus 4종 + 선택 리스너
    /// </summary>
    private void setupFilterTabs() {
        TabLayout tabLayout = binding.tabLayoutFilter;

        tabLayout.addTab(tabLayout.newTab().setText(R.string.library_filter_all));
        for (GameStatus status : GameStatus.values()) {
            tabLayout.addTab(tabLayout.newTab().setText(status.getDisplayName()));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                applyFilter(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 사용 안 함
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                applyFilter(tab.getPosition());
            }
        });
    }

    /// <summary>
    /// 현재 선택된 탭 기준으로 다시 필터 (없으면 전체)
    /// </summary>
    private void applyCurrentFilter() {
        int selected = binding.tabLayoutFilter.getSelectedTabPosition();
        applyFilter(selected < 0 ? TAB_POSITION_ALL : selected);
    }

    /// <summary>
    /// 탭(상태) + 검색어(제목) 두 조건으로 게임을 걸러 어댑터에 반영
    /// 1) 상태 탭으로 1차 필터 → 2) 검색어가 있으면 제목으로 2차 필터
    /// 결과가 비면 빈 상태 안내 (검색 중이면 "검색 결과 없음" 문구로 구분)
    /// </summary>
    private void applyFilter(int tabPosition) {
        // 1) 상태 필터
        List<Game> statusFiltered = new ArrayList<>();
        if (tabPosition == TAB_POSITION_ALL) {
            statusFiltered.addAll(gameRepository.getAllGames());
        } else {
            GameStatus targetStatus = GameStatus.values()[tabPosition - 1];
            for (Game game : gameRepository.getAllGames()) {
                if (game.getStatus() == targetStatus) {
                    statusFiltered.add(game);
                }
            }
        }

        // 2) 제목 검색 필터 (검색어가 있을 때만)
        boolean hasQuery = currentQuery != null && !currentQuery.trim().isEmpty();
        List<Game> filtered;
        if (hasQuery) {
            String query = currentQuery.trim().toLowerCase();
            filtered = new ArrayList<>();
            for (Game game : statusFiltered) {
                // 대소문자 무시하고 제목에 검색어가 포함되면 통과
                if (game.getTitle().toLowerCase().contains(query)) {
                    filtered.add(game);
                }
            }
        } else {
            filtered = statusFiltered;
        }

        // 3) 정렬 적용 (기본순은 원본 순서 유지)
        applySorting(filtered);

        adapter.updateItems(filtered);

        // 빈 상태: 검색 중이면 "검색 결과 없음", 아니면 "이 상태의 게임 없음"
        boolean isEmpty = filtered.isEmpty();
        binding.textViewEmpty.setText(hasQuery
                ? R.string.library_search_empty
                : R.string.library_empty);
        binding.textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewLibrary.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // ========== 정렬 ==========

    /// <summary>
    /// 현재 정렬 기준(currentSort)에 맞게 리스트를 제자리 정렬
    /// DEFAULT(기본순)는 원본(추가) 순서를 유지하므로 정렬하지 않음
    /// </summary>
    private void applySorting(List<Game> list) {
        switch (currentSort) {
            case NAME:
                // 제목 가나다/알파벳 순
                list.sort(Comparator.comparing(Game::getTitle));
                break;
            case RATING_HIGH:
                // 별점 높은 순 (내림차순)
                list.sort(Comparator.comparingDouble(Game::getRating).reversed());
                break;
            case RATING_LOW:
                // 별점 낮은 순 (오름차순)
                list.sort(Comparator.comparingDouble(Game::getRating));
                break;
            case RECENT:
            default:
                // 최근 추가순: id가 클수록 나중에 추가됐으므로 id 내림차순
                // (새로 추가한 게임이 맨 위 — Letterboxd/Goodreads의 기본 정렬과 동일)
                list.sort(Comparator.comparingInt(Game::getId).reversed());
                break;
        }
    }

    /// <summary>
    /// 정렬 옵션 다이얼로그 표시 (FAB 클릭 시)
    /// 4종(기본순/이름순/별점 높은순/별점 낮은순)을 단일 선택으로 띄우고,
    /// 선택 시 currentSort 변경 후 현재 필터를 다시 적용
    /// </summary>
    private void showSortDialog() {
        GameSortOrder[] orders = GameSortOrder.values();

        String[] orderNames = new String[orders.length];
        for (int i = 0; i < orders.length; i++) {
            orderNames[i] = orders[i].getDisplayName();
        }

        int currentIndex = currentSort.ordinal();

        new AlertDialog.Builder(this)
                .setTitle(R.string.library_sort)
                .setSingleChoiceItems(orderNames, currentIndex, (dialog, which) -> {
                    currentSort = orders[which];
                    applyCurrentFilter();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ========== ActionBar 메뉴 (+ 게임 추가 / ⋮ 앱 정보) ==========

    /// <summary>
    /// 메뉴 inflate (게임 추가 + / 앱 정보 ⋮)
    /// </summary>
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // 검색(SearchView) 연결: 입력이 바뀔 때마다 currentQuery 갱신 + 다시 필터
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.library_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 입력 중 onQueryTextChange로 이미 처리되므로 제출은 별도 동작 없음
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                applyCurrentFilter();
                return true;
            }
        });
        return true;
    }

    /// <summary>
    /// 메뉴 항목 클릭 처리
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        if (itemId == R.id.action_add_game) {
            // + 아이콘 → AddGameActivity를 런처로 실행 (결과로 새 게임 정보 받음)
            addGameLauncher.launch(new Intent(this, AddGameActivity.class));
            return true;
        }

        if (itemId == R.id.action_about) {
            // 앱 정보 → AboutActivity
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ========== 어댑터 콜백 ==========

    /// <summary>
    /// 격자 셀 클릭 → GameDetailActivity로 이동
    /// </summary>
    private void onGameClick(Game game) {
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }

    /// <summary>
    /// 격자 셀 길게 누르기 → BottomSheet(삭제/공유/상세) 표시
    /// </summary>
    private void onGameLongClick(Game game) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetGameActionsBinding sheetBinding =
                BottomSheetGameActionsBinding.inflate(getLayoutInflater());

        sheetBinding.textViewSheetTitle.setText(game.getTitle());

        // 삭제: Repository에서 제거 후 현재 탭 다시 필터
        // (그리드는 필터된 목록이라 position 직접 계산 대신 재필터가 안전)
        sheetBinding.actionDelete.setOnClickListener(v -> {
            gameRepository.removeGame(game.getId());
            applyCurrentFilter();
            dialog.dismiss();
        });

        // 공유: ACTION_SEND chooser
        sheetBinding.actionShare.setOnClickListener(v -> {
            dialog.dismiss();
            shareGame(game);
        });

        // 상세 보기: 클릭과 동일
        sheetBinding.actionDetail.setOnClickListener(v -> {
            dialog.dismiss();
            onGameClick(game);
        });

        dialog.setContentView(sheetBinding.getRoot());
        dialog.show();
    }

    /// <summary>
    /// 게임 정보를 다른 앱으로 공유 (ACTION_SEND chooser)
    /// </summary>
    private void shareGame(Game game) {
        String shareText = game.getTitle();
        boolean hasReview = game.getReview() != null && !game.getReview().isEmpty();
        if (hasReview) {
            shareText += "\n★ " + game.getRating() + " - " + game.getReview();
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, game.getTitle());
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.detail_share));
        startActivity(chooser);
    }
}
