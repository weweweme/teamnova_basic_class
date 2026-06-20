package com.example.week8;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ActivityDiaryBinding;
import com.example.week8.databinding.BottomSheetGameActionsBinding;
import com.example.week8.model.Game;
import com.example.week8.model.Genre;
import com.example.week8.model.Platform;
import com.example.week8.ui.GameCardAdapter;
import com.example.week8.ui.GameCardItemTouchCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/// <summary>
/// 메인 화면 (내 게임 다이어리)
/// GameRepository에서 게임 목록을 읽어 ScrollView에 카드 형태로 표시
/// ActionBar의 + 아이콘으로 게임 추가, ⋮ 메뉴로 앱 정보 이동
/// Unity로 비유하면 Prefab을 Instantiate하여 ScrollView Content에 붙이는 것
///
/// ──── Lifecycle 학습 ────
/// onResume: GameDetail/ReviewWrite 다녀온 뒤 변경된 데이터 반영
/// onNewIntent: 앱이 살아있을 때 다른 앱의 공유로 다시 호출될 때 처리
/// </summary>
public class DiaryActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityDiaryBinding binding;

    /// <summary>
    /// 게임 데이터 저장소
    /// Unity로 비유하면 GameData 목록을 들고 있는 매니저
    /// </summary>
    private GameRepository gameRepository;

    /// <summary>
    /// 게임 카드 리스트를 화면에 그리는 어댑터
    /// gameRepository.getAllGames()를 그대로 참조하므로 Repository 변경 시 notify만 하면 반영됨
    /// </summary>
    private GameCardAdapter adapter;

    /// <summary>
    /// AddGameActivity를 실행하고 "새로 추가된 게임 정보"를 결과로 받는 런처
    /// GameDetail의 reviewLauncher, Screenshot의 galleryLauncher와 동일한 패턴
    /// </summary>
    private ActivityResultLauncher<Intent> addGameLauncher;

    // ========== Lifecycle ==========

    /// <summary>
    /// 메인 화면 생성
    /// ViewBinding 연결 + GameRepository 초기화 + 게임 카드 리스트 생성
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityDiaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // App에서 공용 GameRepository 가져오기
        // (모든 Activity가 같은 인스턴스를 공유하므로 다른 화면에서 수정한 결과가 그대로 반영됨)
        gameRepository = ((App) getApplication()).getGameRepository();

        // RecyclerView 설정
        // LayoutManager: 카드를 어떻게 배치할지 결정 (세로 선형 = LinearLayoutManager)
        // Adapter: Repository의 게임 목록을 카드에 채워주는 통역사
        // 카드 클릭 시 onGameClick(game)이 호출되도록 메서드 참조를 콜백으로 전달
        binding.recyclerViewGames.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameCardAdapter(
                gameRepository.getAllGames(),
                this::onGameClick,
                this::onGameLongClick);
        binding.recyclerViewGames.setAdapter(adapter);

        // 드래그 정렬 설정
        // 1. 콜백 생성 (위치 이동 시 adapter.onItemMove 호출되도록 메서드 참조 연결)
        // 2. ItemTouchHelper 생성 후 RecyclerView에 부착 → 내부 터치 이벤트 가로채기 시작
        // 3. 어댑터에 ItemTouchHelper 늦은 주입 → ViewHolder의 핸들이 startDrag 호출에 사용
        //    (어댑터 생성자에서 받으려 하면 ItemTouchHelper ↔ Adapter 순환 의존 → setter로 분리)
        //
        // ──── adapter::onItemMove 의 "::" 의미(메서드 참조) ────
        // "::" 는 Java의 메서드 참조(method reference) 문법.
        // "이 객체의 이 메서드를 나중에 호출할 함수로 넘긴다"는 뜻 (지금 호출하는 게 아님).
        //
        // ─ 왜 "인터페이스 자리"에 "메서드"를 넘길 수 있나? (함수형 인터페이스) ─
        // GameCardItemTouchCallback 생성자는 OnItemMoveListener 인터페이스를 받는데,
        // 이 인터페이스에는 추상 메서드가 onItemMove(from, to) 딱 하나뿐임.
        // → 이렇게 "추상 메서드가 1개뿐인 인터페이스"를 함수형 인터페이스라 부르고,
        //   Java는 이걸 "함수 1개"와 동일하게 취급함.
        // → 그래서 인터페이스 구현 객체를 만드는 대신, 시그니처가 맞는 함수(람다/메서드 참조)를
        //   바로 넘길 수 있음.
        //
        // 컴파일러가 내부적으로 해주는 일 (adapter::onItemMove를 넘기면):
        //   new GameCardItemTouchCallback(new OnItemMoveListener() {
        //       public void onItemMove(int from, int to) {
        //           adapter.onItemMove(from, to);   // ← 이 구현체를 자동 생성해줌
        //       }
        //   });
        //
        // 꽂히는 조건은 "시그니처 일치"뿐:
        //   인터페이스: void onItemMove(int, int)
        //   adapter  : void onItemMove(int, int)   → 파라미터·반환형 일치 → OK
        //   (메서드 이름은 달라도 됨. 시그니처만 맞으면 됨)
        // 비유: 콘센트(인터페이스)는 "구멍 모양(시그니처)"만 맞으면 어느 플러그든 꽂아줌.
        //       어느 회사 제품(어느 객체의 메서드)인지는 안 따짐.
        //
        // ─ 람다 vs 메서드 참조 (둘은 완전히 같은 의미) ─
        //   (from, to) -> adapter.onItemMove(from, to)   // 람다로 풀어쓴 형태
        //   adapter::onItemMove                          // 메서드 참조로 줄인 형태
        // → 이미 그 일을 하는 메서드가 있으면 람다로 다시 감싸지 않고 :: 로 바로 가리킴
        //
        // this::onGameClick (위 어댑터 생성 부분)도 같은 문법.
        // "this(=DiaryActivity)의 onGameClick 메서드를 클릭 콜백으로 넘긴다"는 뜻.
        GameCardItemTouchCallback dragCallback = new GameCardItemTouchCallback(adapter::onItemMove);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewGames);
        adapter.setItemTouchHelper(itemTouchHelper);

        // 게임 추가 화면에서 결과를 받을 런처 등록
        addGameLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 사용자가 "추가하기" 버튼으로 저장 → RESULT_OK + 입력값 Intent 도착
                    // 사용자가 ← 뒤로가기로 취소 → RESULT_CANCELED, data null
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

                    // enum은 Parcelable이 아니라 name() 문자열로 건너왔으므로 valueOf로 복원
                    Genre genre = Genre.valueOf(genreName);
                    Platform platform = Platform.valueOf(platformName);

                    // 저장소에 실제로 추가 (ID 자동 부여)
                    gameRepository.addGame(title, genre, platform, storeUrl);

                    // 어댑터에 "마지막 위치에 새 항목이 추가됐다"고 알림
                    // notifyDataSetChanged()는 전체 리스트를 다시 그려서 비효율적
                    // notifyItemInserted(position)은 그 위치만 추가 애니메이션과 함께 갱신
                    int newPosition = gameRepository.getAllGames().size() - 1;
                    adapter.notifyItemInserted(newPosition);
                }
        );

        // 다른 앱에서 "공유하기"로 이 앱을 실행했다면 받은 텍스트를 처리
        // (앱이 죽어있던 상태에서 공유로 실행되면 onCreate로 Intent가 들어옴)
        handleIncomingShareIntent(getIntent());
    }

    /// <summary>
    /// 앱이 이미 살아있는 상태에서 다른 앱이 "공유하기"로 우리 앱을 다시 부르면 호출됨
    /// (singleTop/singleTask launchMode에서 주로 사용되지만, 표준 launchMode에서도 안전망)
    ///
    /// onCreate는 "앱이 처음 생성될 때"만 Intent를 받고,
    /// onNewIntent는 "이미 떠있는 Activity가 Intent를 다시 받을 때" 호출됨
    /// → 두 곳 모두에서 처리해야 모든 케이스 커버
    /// </summary>
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        // 새 Intent로 setIntent()를 해줘야 getIntent()가 최신 값을 반환
        setIntent(intent);
        handleIncomingShareIntent(intent);
    }

    // ========== Intent 수신: 다른 앱의 "공유하기" 결과 처리 ==========

    /// <summary>
    /// 다른 앱에서 "ACTION_SEND + text/plain"으로 우리 앱을 실행했을 때 받은 텍스트 처리
    ///
    /// ──── Intent Filter 수신 학습 ────
    /// AndroidManifest에 intent-filter를 등록해두면 Android가 우리 앱을 "공유 대상" 후보로 띄움
    ///   액션(SEND) + 카테고리(DEFAULT) + 데이터(text/plain) 세 조건이 모두 맞아야 매칭됨
    ///
    /// 실제 들어오는 Intent 모습 (Chrome에서 글자 공유했을 때):
    ///   action = "android.intent.action.SEND"
    ///   type   = "text/plain"
    ///   extras = EXTRA_TEXT: "사용자가 선택해서 공유한 텍스트"
    ///
    /// </summary>
    private void handleIncomingShareIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        // 이 Intent가 "공유(SEND) + 텍스트(text/plain)"인지 확인
        boolean isSendAction = Intent.ACTION_SEND.equals(intent.getAction());
        boolean isTextType = "text/plain".equals(intent.getType());
        if (!isSendAction || !isTextType) {
            return;
        }

        // 공유받은 텍스트 꺼내기
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null || sharedText.isEmpty()) {
            return;
        }

        // Toast로 받은 텍스트 표시
        // 지금은 학습용으로 Toast만 띄우지만,
        // 실제 프로덕트에선 이 텍스트를 이용해서 게임을 추가하는 흐름이 자연스러울 것 같다
        String message = getString(R.string.main_shared_text, sharedText);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /// <summary>
    /// Activity가 포그라운드로 돌아올 때마다 호출
    /// 다른 화면(GameDetail, ReviewWrite)에서 별점/리뷰를 변경하고 돌아오면
    /// 카드 리스트를 다시 그려서 변경사항을 반영
    /// Unity로 비유하면 OnEnable()에서 UI를 갱신하는 것과 동일
    ///
    /// ★ 호출 시점:
    /// - 앱 최초 실행 시 (onCreate → onStart → onResume)
    /// - 다른 Activity에서 백키로 돌아올 때
    /// - 홈키로 나갔다가 다시 들어올 때
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();

        // 다른 화면(GameDetail, ReviewWrite, Screenshot)에서 별점/리뷰/스크린샷이 변경됐을 수 있음
        // 어떤 항목이 어떻게 변했는지 구체적으로 알지 못하므로 전체 리스트를 다시 바인딩
        // (notifyItemChanged로 정밀 갱신하려면 변경 위치를 추적해야 함 — 이후 단계에서 개선 여지)
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // ========== ActionBar 오버플로우 메뉴 (⋮) ==========

    /// <summary>
    /// 메뉴를 처음 화면에 표시할 준비 단계에서 호출되는 콜백
    ///
    /// 호출 타이밍:
    ///   - Activity가 ActionBar를 처음 그릴 때 1회 자동 호출 (onCreate 직후)
    ///   - invalidateOptionsMenu() 를 호출하면 다시 불림 (메뉴 동적 변경 시)
    ///   - 이후에는 보통 다시 호출되지 않음 → "한 번만 세팅하는" 메서드
    ///
    /// 하는 일:
    ///   menu_main.xml에 정의된 <item> 태그들을 실제 MenuItem 객체로 부풀림(inflate)
    ///   → ActionBar에 + 아이콘과 ⋮ 오버플로우 메뉴 표시됨
    ///
    /// 반환값:
    ///   true  → 메뉴를 표시하겠다
    ///   false → 메뉴를 표시하지 않겠다 (거의 사용 안 함)
    ///
    /// Unity 비유: MenuPrefab을 MenuContainer에 Instantiate
    /// </summary>
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /// <summary>
    /// 사용자가 메뉴 항목을 탭했을 때 호출되는 콜백
    ///
    /// 호출 타이밍 (사용자 탭이 트리거):
    ///   - ActionBar에 노출된 + 아이콘 탭 → item.getItemId() == R.id.action_add_game
    ///   - ⋮ 오버플로우 메뉴 안의 항목 탭 → item.getItemId() == R.id.action_about
    ///   - ActionBar ← (Up) 버튼 탭 → item.getItemId() == android.R.id.home
    ///     (우리는 setDisplayHomeAsUpEnabled(true)를 안 썼으므로 이 경로는 없음)
    ///
    /// 반환값:
    ///   true  → "내가 이 항목을 처리했으니 더 전파하지 마"
    ///   super 호출 → "모르는 항목이니 부모가 처리하도록 위임"
    ///
    /// ──── startActivity vs launcher.launch 구분 ────
    /// 결과를 돌려받을 필요가 있는가로 결정:
    ///   - action_add_game: AddGameActivity가 "입력받은 게임 정보"를 돌려줘야 함
    ///                      → launcher.launch (결과 수신용)
    ///   - action_about:    AboutActivity는 정보 표시만 할 뿐, 돌려줄 값 없음
    ///                      → startActivity (결과 불필요)
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_add_game) {
            // + 아이콘 → AddGameActivity를 런처로 실행
            // "사용자가 입력한 게임 정보"를 결과로 받아야 하므로 launcher 사용
            // 결과는 onCreate에서 등록한 addGameLauncher 람다로 도착
            Intent intent = new Intent(this, AddGameActivity.class);
            addGameLauncher.launch(intent);
            return true;
        }

        if (itemId == R.id.action_library) {
            // "라이브러리" 메뉴 → LibraryActivity로 이동 (그리드 뷰)
            // 같은 게임 데이터를 격자 형태로 보여줄 뿐 돌려줄 값이 없으므로 단순 startActivity
            // (임시 진입점 — Phase 6 HomeActivity 도입 후 그쪽으로 이동 예정)
            Intent intent = new Intent(this, LibraryActivity.class);
            startActivity(intent);
            return true;
        }

        if (itemId == R.id.action_timeline) {
            // "타임라인" 메뉴 → TimelineActivity로 이동 (활동 피드, 멀티 뷰타입)
            // (임시 진입점 — Phase 6 HomeActivity 도입 후 그쪽으로 이동 예정)
            Intent intent = new Intent(this, TimelineActivity.class);
            startActivity(intent);
            return true;
        }

        if (itemId == R.id.action_about) {
            // "앱 정보" 메뉴 → AboutActivity로 이동
            // About은 정보 표시만, 돌려줄 값이 없으므로 단순 startActivity
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ========== 어댑터 콜백 ==========

    /// <summary>
    /// GameCardAdapter의 카드 클릭 콜백
    /// onCreate에서 어댑터 생성 시 this::onGameClick으로 메서드 참조를 콜백으로 넘겼기 때문에
    /// 사용자가 카드를 탭할 때마다 이 메서드가 호출됨
    ///
    /// 동작: 클릭된 Game을 Parcelable로 Intent에 담아 GameDetailActivity로 이동
    /// </summary>
    private void onGameClick(Game game) {
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }

    /// <summary>
    /// 게임 정보를 다른 앱(카톡, 메시지 등)으로 공유
    /// ACTION_SEND + Chooser 패턴 (GameDetailActivity의 공유 버튼과 동일한 동작)
    /// </summary>
    private void shareGame(Game game) {
        // 공유할 텍스트 조합 (제목 + 리뷰 있으면 별점·한줄평 포함)
        String shareText = game.getTitle();
        boolean hasReview = game.getReview() != null && !game.getReview().isEmpty();
        if (hasReview) {
            shareText += "\n★ " + game.getRating() + " - " + game.getReview();
        }

        // ACTION_SEND: "이 데이터를 보낼 수 있는 앱 목록 보여줘"
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, game.getTitle());
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // createChooser: 항상 앱 선택 다이얼로그 표시
        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.detail_share));
        startActivity(chooser);
    }

    /// <summary>
    /// 카드 길게 누르기 콜백
    /// BottomSheetDialog로 컨텍스트 메뉴(삭제 / 공유 / 상세 보기) 표시
    /// 각 메뉴 항목의 실제 동작은 후속 커밋에서 구현 (지금은 stub)
    /// </summary>
    private void onGameLongClick(Game game) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetGameActionsBinding sheetBinding =
                BottomSheetGameActionsBinding.inflate(getLayoutInflater());

        // 상단에 어떤 게임에 대한 액션인지 표시
        sheetBinding.textViewSheetTitle.setText(game.getTitle());

        // 삭제: Repository에서 제거 + 어댑터에 위치 알림
        // notifyItemRemoved는 해당 위치만 제거 애니메이션과 함께 갱신 (전체 리바인딩 X)
        sheetBinding.actionDelete.setOnClickListener(v -> {
            int removedPosition = gameRepository.removeGame(game.getId());
            if (removedPosition >= 0) {
                adapter.notifyItemRemoved(removedPosition);
            }
            dialog.dismiss();
        });

        // 공유: 게임 정보를 다른 앱(카톡, 메시지 등)으로 공유
        // GameDetailActivity의 shareGame과 동일한 ACTION_SEND + Chooser 패턴
        sheetBinding.actionShare.setOnClickListener(v -> {
            dialog.dismiss();
            shareGame(game);
        });

        // 상세 보기 — 카드 짧게 누르기와 동일한 동작
        sheetBinding.actionDetail.setOnClickListener(v -> {
            dialog.dismiss();
            onGameClick(game);
        });

        dialog.setContentView(sheetBinding.getRoot());
        dialog.show();
    }
}
