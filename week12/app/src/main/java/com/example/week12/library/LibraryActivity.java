package com.example.week12.library;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week12.App;
import com.example.week12.R;
import com.example.week12.about.AboutActivity;
import com.example.week12.account.UserPrefs;
import com.example.week12.addgame.AddGameActivity;
import com.example.week12.detail.GameDetailActivity;
import com.example.week12.trash.TrashActivity;

import java.util.Comparator;

import com.example.week12.data.GameRepository;
import com.example.week12.databinding.ActivityLibraryBinding;
import com.example.week12.databinding.BottomSheetAddGameBinding;
import com.example.week12.databinding.BottomSheetGameActionsBinding;
import com.example.week12.rawg.RawgSearchActivity;
import com.example.week12.model.ActivityLogType;
import com.example.week12.model.Game;
import com.example.week12.model.GameStatus;
import com.example.week12.model.Genre;
import com.example.week12.model.Platform;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    /// 현재 로그인 계정의 개인 설정 저장소 (마지막 필터 탭 / 정렬 / 즐겨찾기)
    /// </summary>
    private UserPrefs userPrefs;

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
    /// 검색 디바운스가 지난 뒤 대기하는 시간(ms) — 이 시간만큼 입력이 멈추면 한 번만 필터
    /// </summary>
    private static final long SEARCH_DEBOUNCE_MS = 300L;

    /// <summary>
    /// 검색 디바운스용 Handler (메인 줄에 "잠시 뒤 필터" 작업을 예약)
    /// 타이핑마다 즉시 필터링하면 글자마다 목록을 다시 그려 렉이 생김 →
    /// 입력이 멈춘 뒤 SEARCH_DEBOUNCE_MS 후에 딱 한 번만 필터하도록 미룬다
    /// </summary>
    private final Handler searchHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 디바운스가 실제로 실행할 작업 (필터링)
    /// removeCallbacks로 취소하려면 항상 "같은 인스턴스"여야 하므로 필드로 보관
    /// </summary>
    private final Runnable searchFilterRunnable = this::applyCurrentFilter;


    /// <summary>
    /// 검색 입력창 (ActionBar의 돋보기). 최근 검색어 칩을 눌렀을 때 이 창에 검색어를 넣어야 해서 보관
    /// </summary>
    private SearchView searchView;

    /// <summary>
    /// 검색 메뉴 항목 (펼침/접힘 상태를 확인해 최근 검색어 표시 여부를 정함)
    /// </summary>
    private MenuItem searchItem;

    /// <summary>
    /// 현재 정렬 기준 (FAB 다이얼로그에서 선택). 기본은 최근 추가순
    /// </summary>
    private GameSortOrder currentSort = GameSortOrder.RECENT;

    /// <summary>
    /// 보기 모드: true=그리드(2열), false=리스트(1열)
    /// 같은 어댑터/셀에 LayoutManager만 바꿔 배치를 전환 (Steam 라이브러리식)
    /// </summary>
    private boolean isGridMode = true;

    /// <summary>
    /// 현재 선택된 장르들 (장르 칩 다중 선택). 비어있으면 전체 장르
    /// 한 게임은 장르가 하나뿐이라, 여러 장르 선택은 OR(이 중 하나면 통과)로 동작
    /// </summary>
    private final Set<Genre> selectedGenres = new HashSet<>();

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

        // App 공용 GameRepository + 현재 계정 저장소
        App app = (App) getApplication();
        gameRepository = app.getGameRepository();
        userPrefs = app.getUserPrefs();

        // 마지막으로 고른 정렬 기준을 먼저 복원 (이후 필터 적용 시 이 값이 쓰임)
        restoreLastSort();

        // RecyclerView 설정 — 보기 모드(그리드/리스트)에 맞는 LayoutManager 적용
        // 클릭 → 상세, 길게 누르기 → BottomSheet
        adapter = new LibraryAdapter(gameRepository.getAllGames(), this::onGameClick, this::onGameLongClick,
                ((App) getApplication()).getUserPrefs());
        binding.recyclerViewLibrary.setAdapter(adapter);
        applyViewMode();

        setupFilterTabs();
        setupGenreChips();

        // 마지막으로 보던 필터 탭 복원 (탭을 고르면 리스너가 필터까지 적용)
        restoreLastFilterTab();

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
                    String coverUri = data.getStringExtra(AddGameActivity.EXTRA_COVER_URI);

                    // enum은 name() 문자열로 건너왔으므로 valueOf로 복원
                    Genre genre = Genre.valueOf(genreName);
                    Platform platform = Platform.valueOf(platformName);

                    // 저장소에 추가 후 현재 탭 기준으로 다시 필터 (새 게임은 찜 목록 기본)
                    Game added = gameRepository.addGame(title, genre, platform, storeUrl, coverUri);
                    // 활동 로그에 "추가함" 기록 → 최근 활동 피드에 표시
                    userPrefs.addActivityLog(ActivityLogType.ADDED, added.getId(), "");
                    applyCurrentFilter();
                    updateTabCounts();
                }
        );
    }

    /// <summary>
    /// 다른 화면에서 상태/데이터 변경 후 돌아왔을 때 현재 탭 기준으로 다시 필터
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();
        applyCurrentFilter();
        updateTabCounts();
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 예약돼 있던 검색 디바운스 작업을 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchHandler.removeCallbacksAndMessages(null);
    }

    // ========== 필터 탭 ==========

    /// <summary>
    /// 필터 탭 구성: "전체" + GameStatus 4종 + 선택 리스너
    /// </summary>
    private void setupFilterTabs() {
        TabLayout tabLayout = binding.tabLayoutFilter;

        // 탭만 먼저 추가하고, 텍스트(개수 포함)는 updateTabCounts에서 채움
        // 순서: 전체(0) → 상태 4종(1~4) → 즐겨찾기 ♥(맨 끝)
        tabLayout.addTab(tabLayout.newTab());
        for (int i = 0; i < GameStatus.values().length; i++) {
            tabLayout.addTab(tabLayout.newTab());
        }
        tabLayout.addTab(tabLayout.newTab());  // 즐겨찾기(♥) 탭
        updateTabCounts();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onFilterTabChosen(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 사용 안 함
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onFilterTabChosen(tab.getPosition());
            }
        });
    }

    /// <summary>
    /// 필터 탭이 선택됐을 때: 그 위치를 마지막 상태로 저장하고 필터를 적용
    /// (선택/재선택 두 경우가 같은 동작이라 한 곳으로 묶음)
    /// </summary>
    private void onFilterTabChosen(int position) {
        if (userPrefs != null) {
            userPrefs.setLastFilterTab(position);
        }
        applyFilter(position);
    }

    /// <summary>
    /// 마지막으로 보던 필터 탭을 복원해 선택
    /// 선택하면 위 리스너가 동작해 필터까지 적용된다.
    /// 저장값이 현재 탭 개수를 벗어나면(예: 탭 구성이 바뀐 경우) 전체 탭으로 안전하게 처리
    /// </summary>
    private void restoreLastFilterTab() {
        if (userPrefs == null) {
            return;
        }
        TabLayout tabLayout = binding.tabLayoutFilter;
        int savedTab = userPrefs.getLastFilterTab();
        boolean validRange = savedTab >= 0 && savedTab < tabLayout.getTabCount();
        int target = validRange ? savedTab : TAB_POSITION_ALL;

        TabLayout.Tab tab = tabLayout.getTabAt(target);
        if (tab != null) {
            tab.select();
        }
    }

    /// <summary>
    /// 현재 선택된 탭 기준으로 다시 필터 (없으면 전체)
    /// </summary>
    private void applyCurrentFilter() {
        int selected = binding.tabLayoutFilter.getSelectedTabPosition();
        applyFilter(selected < 0 ? TAB_POSITION_ALL : selected);
    }

    /// <summary>
    /// 각 탭 라벨에 게임 개수를 붙여 표시 ("전체 20", "완료 7" 등)
    /// 데이터가 바뀔 때(추가/삭제/상태변경) 호출해서 개수를 최신으로 유지
    /// (검색/정렬은 개수를 바꾸지 않으므로 호출 불필요)
    /// </summary>
    private void updateTabCounts() {
        TabLayout tabLayout = binding.tabLayoutFilter;

        // 0번 탭: 전체
        TabLayout.Tab allTab = tabLayout.getTabAt(TAB_POSITION_ALL);
        if (allTab != null) {
            allTab.setText(getString(R.string.library_filter_all)
                    + " " + gameRepository.getTotalCount());
        }

        // 1번부터: 각 상태별 개수
        GameStatus[] statuses = GameStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i + 1);
            if (tab != null) {
                tab.setText(statuses[i].getDisplayName()
                        + " " + gameRepository.countByStatus(statuses[i]));
            }
        }

        // 맨 끝 탭: 즐겨찾기(개수 포함) — 계정별 fav_ 저장소를 훑어서 셈
        TabLayout.Tab favTab = tabLayout.getTabAt(favoriteTabPosition());
        if (favTab != null) {
            favTab.setText("즐겨찾기 " + countFavorites());
        }
    }

    /// <summary>
    /// 즐겨찾기(♥) 탭의 위치 = 전체(0) 다음에 상태 4종이 오고, 그 바로 뒤
    /// (상태 개수가 바뀌어도 자동으로 맞도록 계산으로 구함)
    /// </summary>
    private int favoriteTabPosition() {
        return GameStatus.values().length + 1;
    }

    /// <summary>
    /// 현재 계정이 즐겨찾기한 게임 개수 (탭 라벨 "♥ N"에 표시)
    /// </summary>
    private int countFavorites() {
        if (userPrefs == null) {
            return 0;
        }
        int count = 0;
        for (Game game : gameRepository.getAllGames()) {
            if (userPrefs.isFavorite(game.getId())) {
                count++;
            }
        }
        return count;
    }

    // ========== 장르 칩 필터 ==========

    /// <summary>
    /// 장르 칩 구성: 게임이 있는 장르들을 다중 선택 칩으로 추가
    /// "전체" 칩은 없음 — 아무것도 선택 안 하면 전체로 간주
    /// 여러 장르를 동시에 켜면 OR 필터 (선택한 장르 중 하나면 통과)
    ///
    /// 각 칩에 setId(generateViewId)로 고유 id 부여:
    ///   코드로 만든 칩은 기본 id가 없는데(NO_ID), id가 없으면 체크 상태 토글이
    ///   제대로 동작하지 않아 클릭해도 선택이 안 되는 문제가 있음
    /// </summary>
    private void setupGenreChips() {
        ChipGroup chipGroup = binding.chipGroupGenre;

        for (Genre genre : Genre.values()) {
            if (gameRepository.countByGenre(genre) == 0) {
                continue;
            }
            // new Chip(this) 대신 XML(Filter 스타일)을 inflate → Bridge 테마에서도 체크 정상 동작
            Chip chip = (Chip) getLayoutInflater()
                    .inflate(R.layout.item_genre_chip, chipGroup, false);
            chip.setId(View.generateViewId());
            chip.setText(genre.getDisplayName());
            chip.setTag(genre);

            // 칩 체크 변경 → 선택 장르 집합 갱신 + 재필터
            chip.setOnCheckedChangeListener((button, isChecked) -> {
                if (isChecked) {
                    selectedGenres.add(genre);
                } else {
                    selectedGenres.remove(genre);
                }
                applyCurrentFilter();
            });

            chipGroup.addView(chip);
        }
    }

    /// <summary>
    /// 탭(상태) + 장르(칩) + 검색어(제목) 조건으로 게임을 걸러 어댑터에 반영
    /// 1) 상태 → 1.5) 장르 → 2) 검색 → 3) 정렬
    /// 결과가 비면 빈 상태 안내 (검색 중이면 "검색 결과 없음" 문구로 구분)
    /// </summary>
    private void applyFilter(int tabPosition) {
        // 1) 상태(또는 즐겨찾기) 필터
        List<Game> statusFiltered = new ArrayList<>();
        if (tabPosition == TAB_POSITION_ALL) {
            statusFiltered.addAll(gameRepository.getAllGames());
        } else if (tabPosition == favoriteTabPosition()) {
            // 맨 끝 ♥ 탭: 즐겨찾기한 게임만
            for (Game game : gameRepository.getAllGames()) {
                if (userPrefs.isFavorite(game.getId())) {
                    statusFiltered.add(game);
                }
            }
        } else {
            GameStatus targetStatus = GameStatus.values()[tabPosition - 1];
            for (Game game : gameRepository.getAllGames()) {
                if (game.getStatus() == targetStatus) {
                    statusFiltered.add(game);
                }
            }
        }

        // 1.5) 장르 필터 (선택된 장르가 있을 때만, OR 조건)
        if (!selectedGenres.isEmpty()) {
            statusFiltered.removeIf(game -> !selectedGenres.contains(game.getGenre()));
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

    // ========== 보기 모드 (그리드 / 리스트) ==========

    /// <summary>
    /// 현재 보기 모드에 맞는 LayoutManager 적용
    /// 그리드 = GridLayoutManager(2열), 리스트 = LinearLayoutManager(1열)
    /// 같은 어댑터/셀을 그대로 두고 LayoutManager만 교체 → 배치가 완전히 달라짐
    /// </summary>
    private void applyViewMode() {
        if (isGridMode) {
            // 격자에 한 줄당 표시할 열 개수
            final int GRID_SPAN_COUNT = 2;
            binding.recyclerViewLibrary.setLayoutManager(new GridLayoutManager(this, GRID_SPAN_COUNT));
        } else {
            binding.recyclerViewLibrary.setLayoutManager(new LinearLayoutManager(this));
        }
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
    /// 마지막으로 고른 정렬 기준을 복원해 currentSort에 반영
    /// 저장은 이름(문자열)으로 돼 있으므로 GameSortOrder로 되돌린다.
    /// 저장값이 없거나 알 수 없는 이름이면 기본(RECENT)으로 안전하게 처리
    /// </summary>
    private void restoreLastSort() {
        if (userPrefs == null) {
            return;
        }
        String savedName = userPrefs.getLastSort(GameSortOrder.RECENT.name());
        currentSort = parseSortOrder(savedName);
    }

    /// <summary>
    /// 정렬 기준 이름(문자열)을 GameSortOrder로 변환 (알 수 없으면 RECENT)
    /// 잘못된 값이 와도 앱이 죽지 않도록 하나씩 비교해 찾는다
    /// </summary>
    private GameSortOrder parseSortOrder(String name) {
        for (GameSortOrder order : GameSortOrder.values()) {
            if (order.name().equals(name)) {
                return order;
            }
        }
        return GameSortOrder.RECENT;
    }

    /// <summary>
    /// 정렬 옵션 다이얼로그 표시 (FAB 클릭 시)
    /// 4종(기본순/이름순/별점 높은순/별점 낮은순)을 단일 선택으로 띄우고,
    /// 선택 시 currentSort 변경 → 마지막 상태로 저장 → 현재 필터를 다시 적용
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
                    if (userPrefs != null) {
                        userPrefs.setLastSort(currentSort.name());
                    }
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
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.library_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 검색을 확정(엔터/검색 버튼)한 순간 → 최근 검색어로 저장
                userPrefs.pushSearchQuery(query);
                // 결과가 이미 아래에 떠 있으니 최근 검색어 목록은 접는다
                hideRecentSearches();
                // true 반환: SearchView 기본 동작(제안 목록 등) 없이 우리가 처리 끝냈음을 알림
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;

                // 디바운스: 직전에 예약한 필터를 취소하고, 300ms 뒤에 한 번만 실행
                // → 사용자가 빠르게 타이핑하는 동안에는 필터가 계속 미뤄지다가,
                //   입력이 멈춘 순간 딱 한 번만 목록을 다시 그림 (글자마다 렉 제거)
                searchHandler.removeCallbacks(searchFilterRunnable);
                searchHandler.postDelayed(searchFilterRunnable, SEARCH_DEBOUNCE_MS);

                // 최근 검색어 표시 여부는 힌트라 즉시 갱신 (가벼움)
                updateRecentSearchVisibility();
                return true;
            }
        });

        // 돋보기를 눌러 검색창을 펼친 순간 (입력은 아직 비어 있음) → 최근 검색어 표시
        searchView.setOnSearchClickListener(v -> showRecentSearches());

        // 검색창을 접으면(← 또는 X) 최근 검색어 목록도 함께 숨김
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                // 펼치는 순간엔 입력이 비어 있으니 최근 검색어를 바로 보여준다
                // (collapseActionView 방식이라 setOnSearchClickListener가 안 불릴 수 있어 여기서 처리)
                showRecentSearches();
                return true;  // 펼침 허용
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                hideRecentSearches();
                return true;  // 접힘 허용
            }
        });

        // "지우기" → 검색 기록 전체 삭제 후 목록 숨김
        binding.textViewClearRecentSearch.setOnClickListener(v -> {
            userPrefs.clearSearchHistory();
            hideRecentSearches();
        });
        return true;
    }

    // ========== 최근 검색어 ==========

    /// <summary>
    /// 검색창이 펼쳐져 있고 입력이 비어 있을 때만 최근 검색어를 보여준다
    /// (입력 도중엔 결과가 중요하므로 최근 검색어는 감춤)
    /// </summary>
    private void updateRecentSearchVisibility() {
        boolean searchOpen = searchItem != null && searchItem.isActionViewExpanded();
        boolean queryEmpty = currentQuery.isEmpty();
        if (searchOpen && queryEmpty) {
            showRecentSearches();
        } else {
            hideRecentSearches();
        }
    }

    /// <summary>
    /// 최근 검색어 칩들을 만들어 표시한다
    /// 저장된 기록이 없으면 영역을 숨김(GONE)
    /// 칩을 누르면 그 검색어로 SearchView를 채워 바로 검색되게 한다
    /// </summary>
    private void showRecentSearches() {
        ChipGroup chipGroup = binding.chipGroupRecentSearch;
        chipGroup.removeAllViews();

        List<String> history = userPrefs.getSearchHistory();
        if (history.isEmpty()) {
            binding.layoutRecentSearch.setVisibility(View.GONE);
            return;
        }

        for (String query : history) {
            // 장르 칩과 달리 체크가 아닌 "누르면 검색" 액션 칩
            Chip chip = (Chip) getLayoutInflater()
                    .inflate(R.layout.item_recent_search_chip, chipGroup, false);
            chip.setText(query);
            // 칩을 누르면 그 검색어를 검색창에 넣고 곧바로 제출(true) → 결과 필터 + 기록 최신화
            chip.setOnClickListener(v -> searchView.setQuery(query, true));
            chipGroup.addView(chip);
        }

        binding.layoutRecentSearch.setVisibility(View.VISIBLE);
    }

    /// <summary>
    /// 최근 검색어 영역을 숨긴다
    /// </summary>
    private void hideRecentSearches() {
        binding.layoutRecentSearch.setVisibility(View.GONE);
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

        if (itemId == R.id.action_view_toggle) {
            // 그리드 ↔ 리스트 전환 + 아이콘을 "반대 모드"로 갱신
            isGridMode = !isGridMode;
            applyViewMode();
            item.setIcon(isGridMode
                    ? R.drawable.ic_view_list   // 그리드 모드 → "리스트로 전환" 아이콘
                    : R.drawable.ic_view_grid); // 리스트 모드 → "그리드로 전환" 아이콘
            return true;
        }

        if (itemId == R.id.action_add_game) {
            // + 아이콘 → 추가 방법 선택 (검색으로 추가 / 직접 입력)
            showAddGameChooser();
            return true;
        }

        if (itemId == R.id.action_about) {
            // 앱 정보 → AboutActivity
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        if (itemId == R.id.action_trash) {
            // 휴지통 → TrashActivity
            startActivity(new Intent(this, TrashActivity.class));
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
    /// "+ 게임 추가" → 추가 방법 선택 BottomSheet (검색으로 추가 / 직접 입력)
    /// - 검색으로 추가: RAWG 검색 화면에서 API로 제목·표지·장르 자동 채움
    /// - 직접 입력: 기존 수동 입력 폼 (둘을 병존시켜, API 실패 시 대비책도 됨)
    /// </summary>
    private void showAddGameChooser() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetAddGameBinding sheetBinding =
                BottomSheetAddGameBinding.inflate(getLayoutInflater());

        // 검색으로 추가 → RAWG 검색 화면 (결과 탭 시 스스로 보관함에 추가하고 돌아옴)
        sheetBinding.actionSearchAdd.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, RawgSearchActivity.class));
        });

        // 둘러보기 → RAWG 탐색 화면 (인기/신작/장르별로 구경하다 골라서 추가)
        sheetBinding.actionExplore.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, com.example.week12.rawg.ExploreActivity.class));
        });

        // 직접 입력 → 기존 수동 추가 폼 (결과 Intent는 addGameLauncher가 받아 처리)
        sheetBinding.actionManualAdd.setOnClickListener(v -> {
            dialog.dismiss();
            addGameLauncher.launch(new Intent(this, AddGameActivity.class));
        });

        dialog.setContentView(sheetBinding.getRoot());
        dialog.show();
    }

    /// <summary>
    /// 격자 셀 길게 누르기 → BottomSheet(삭제/공유/상세) 표시
    /// </summary>
    private void onGameLongClick(Game game) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetGameActionsBinding sheetBinding =
                BottomSheetGameActionsBinding.inflate(getLayoutInflater());

        sheetBinding.textViewSheetTitle.setText(game.getTitle());

        // 삭제: 즉시 제거하되 "실행취소" 스낵바로 몇 초간 되돌릴 기회를 준다
        sheetBinding.actionDelete.setOnClickListener(v -> {
            dialog.dismiss();
            deleteWithUndo(game);
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
    /// 삭제 → 즉시 목록에서 제거하되, 짧게 "실행취소" 기회를 준다.
    /// - 실행취소를 누르면 원래 위치로 복원
    /// - 실행취소 없이 스낵바가 닫히면 휴지통으로 이동 (영구삭제 아님 → 휴지통에서 복구 가능)
    ///
    /// 커밋 판정은 스낵바의 onDismissed(왜 닫혔나)로 한다:
    ///   실행취소를 누르면 event가 DISMISS_EVENT_ACTION → 그 경우만 휴지통행에서 제외
    /// removedIndex·game은 이 스낵바 하나에만 필요하므로 지역 변수로 캡처 (별도 필드 불필요)
    /// </summary>
    private void deleteWithUndo(Game game) {
        // 즉시 제거 → 목록/탭 개수 바로 반영. 제거된 위치는 실행취소 복원용으로 보관
        int removedIndex = gameRepository.removeGame(game.getId());
        applyCurrentFilter();
        updateTabCounts();

        Snackbar.make(binding.getRoot(),
                        getString(R.string.delete_undo_message, game.getTitle()),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.delete_undo_action, v -> {
                    // 실행취소 → 원래 위치로 복원 (휴지통으로 안 감)
                    gameRepository.insertGame(removedIndex, game);
                    applyCurrentFilter();
                    updateTabCounts();
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar sb, int event) {
                        // 실행취소가 아닌 이유로 닫힘(시간초과·스와이프·다른 스낵바로 교체 등) → 휴지통으로
                        // 버린 시각을 함께 넘겨 30일 자동 삭제 판정에 사용
                        if (event != DISMISS_EVENT_ACTION) {
                            gameRepository.moveToTrash(game, System.currentTimeMillis());
                        }
                    }
                })
                .show();
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
