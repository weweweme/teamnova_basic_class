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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ActivityDiaryBinding;
import com.example.week8.databinding.BottomSheetGameActionsBinding;
import com.example.week8.model.Game;
import com.example.week8.model.Genre;
import com.example.week8.model.Platform;
import com.example.week8.ui.GameCardAdapter;
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

        // 게임 추가 화면에서 결과를 받을 런처 등록 (Lifecycle 연동 위해 onCreate에서)
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

        // 삭제 (다음 커밋에서 구현 — 지금은 Toast로 동작 확인용)
        sheetBinding.actionDelete.setOnClickListener(v -> {
            Toast.makeText(this, "삭제: " + game.getTitle(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // 공유 (다음 커밋에서 구현)
        sheetBinding.actionShare.setOnClickListener(v -> {
            Toast.makeText(this, "공유: " + game.getTitle(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
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
