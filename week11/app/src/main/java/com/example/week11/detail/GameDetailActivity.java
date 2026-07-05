package com.example.week11.detail;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.account.UserPrefs;
import com.example.week11.data.CommunityRepository;
import com.example.week11.databinding.ActivityGameDetailBinding;
import com.example.week11.model.Game;
import com.example.week11.model.GameReview;
import com.example.week11.model.ActivityLogType;
import com.example.week11.model.GameStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/// <summary>
/// 게임 상세 화면
/// MainActivity에서 Parcelable로 전달받은 Game 데이터를 표시
/// 리뷰 작성 / 공유 / 스토어 열기 / 스크린샷 버튼 제공
/// Unity로 비유하면 아이템 상세 Panel에 데이터를 바인딩하는 것
///
/// ──── Intent 학습 ────
/// 수신: MainActivity에서 명시적 Intent + Parcelable extras로 Game 전달받음
/// 결과 반환: ReviewWriteActivity를 startActivityForResult로 호출하고,
///           onActivityResult에서 별점/한줄평 결과를 받아 화면 갱신
/// 송신 (이후 단계): ACTION_SEND chooser, ACTION_VIEW 브라우저
/// </summary>
public class GameDetailActivity extends AppCompatActivity {

    /// <summary>
    /// Intent extras에서 Game 객체를 꺼낼 때 사용하는 키
    /// 보내는 쪽(MainActivity)과 받는 쪽(여기)이 같은 키를 써야 함
    /// </summary>
    public static final String EXTRA_GAME = "extra_game";

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityGameDetailBinding binding;

    /// <summary>
    /// 표지 이미지 디코딩 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// getMainLooper() = 메인(UI) 스레드 줄에 작업을 넣겠다는 뜻
    /// 무거운 JPG 디코딩은 서브 스레드에서 하고, setImageBitmap(화면 갱신)만 이 Handler로 메인에 넘긴다
    /// Unity로 비유하면: 워커 스레드에서 만든 결과를 메인 스레드 디스패처로 넘겨 UI에 반영
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// Intent로 전달받은 게임 데이터
    /// </summary>
    private Game game;

    /// <summary>
    /// ReviewWriteActivity를 실행하고 결과(별점/한줄평)를 받는 런처
    ///
    /// ──── Launcher(런처)란? ────
    /// 공식 레퍼런스: https://developer.android.com/reference/androidx/activity/result/ActivityResultLauncher
    ///
    /// Launcher = "발사기/실행기"라는 뜻
    /// registerForActivityResult(...)를 호출하면 Launcher 객체가 반환됨
    /// 이 Launcher가 들고 있는 정보:
    ///   - 어떤 Contract(계약)를 사용하는지 (입력/출력 타입)
    ///   - 결과가 도착하면 실행할 콜백 람다
    ///   - Lifecycle 연동 정보 (내부 Registry 참조)
    ///
    /// 주요 메서드:
    ///   launch(input)         → Contract에 맞는 입력을 넣어 실제로 Activity 실행
    ///   launch(input, options) → ActivityOptions(화면 전환 애니메이션 등)와 함께 실행
    ///   unregister()          → 수동 해제 (보통 필요 없음. Lifecycle이 자동 처리)
    ///
    /// 제네릭 타입 ActivityResultLauncher<Intent>:
    ///   꺾쇠 안의 Intent는 Contract의 "입력 타입"
    ///   StartActivityForResult Contract는 Intent를 입력으로 받으므로 <Intent>
    ///   만약 RequestPermission Contract라면 <String>이 됨
    ///
    /// 등록과 실행의 분리:
    ///   등록(register): onCreate에서 1회 — Lifecycle 연동 위해 반드시 초기에
    ///   실행(launch):   사용자 조작 시점 — 버튼 클릭 등에서 원하는 만큼 반복 호출
    ///
    /// ──── 현대 방식 (ActivityResultLauncher) ────
    /// 공식 문서: https://developer.android.com/training/basics/intents/result
    ///
    /// 예전에는 startActivityForResult + onActivityResult + requestCode 조합을 썼지만,
    /// Android 공식이 "더 이상 권장하지 않음(deprecated)"이라고 선언함
    /// 이유:
    ///   1. requestCode를 전역 int 상수로 관리해야 해서 실수 유발
    ///   2. onActivityResult 하나에 여러 결과가 몰려서 if-else 분기 지저분
    ///   3. Activity 생명주기와 무관하게 결과가 오면 크래시 가능
    ///
    /// 현대 방식의 장점:
    ///   1. 요청마다 별도 Launcher → requestCode 불필요
    ///   2. 결과 처리 콜백이 Launcher 생성 시 바로 묶여 있음 → 코드 가까이 배치
    ///   3. 내부적으로 Lifecycle 연동 → 안전
    ///
    /// Screenshot의 galleryLauncher와 동일한 패턴
    /// </summary>
    private ActivityResultLauncher<Intent> reviewLauncher;

    /// <summary>
    /// ScreenshotActivity를 실행하고 결과(스크린샷이 추가된 Game)를 받는 런처
    /// ScreenshotActivity가 갤러리에서 이미지를 고를 때마다 game.addScreenshot한 결과를
    /// setResult로 돌려보내므로, 이 람다에서 Repository 원본에 반영해야 함
    /// </summary>
    private ActivityResultLauncher<Intent> screenshotLauncher;

    /// <summary>
    /// 현재 로그인 계정의 개인 설정 저장소
    /// 별점/한줄평은 계정마다 다르므로, "내 평가"는 전역 Game이 아니라 여기서 읽고 쓴다
    /// (같은 게임이라도 alice의 리뷰와 bob의 리뷰가 다름)
    /// </summary>
    private UserPrefs userPrefs;

    // ========== Lifecycle ==========

    /// <summary>
    /// 상세 화면 생성
    /// Intent extras에서 Game 객체를 꺼내고 화면에 바인딩 + 버튼 리스너 등록
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityGameDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // MainActivity에서 intent.putExtra("extra_game", game)으로 보낸 Game 객체를 꺼냄
        // 흐름: MainActivity가 Game을 Parcel에 포장(writeToParcel)해서 Intent에 실음
        //       → 이 Activity가 Intent에서 Parcel을 꺼내고 CREATOR로 Game 복원
        //       → 결과적으로 MainActivity의 Game과 동일한 데이터를 가진 새 Game 객체가 됨
        game = getIntent().getParcelableExtra(EXTRA_GAME, Game.class);

        // 게임 데이터가 없으면 화면을 닫음 (비정상 진입 방지)
        if (game == null) {
            finish();
            return;
        }

        // 현재 로그인 계정의 저장소 (내 별점/한줄평을 여기서 읽고 씀)
        userPrefs = ((App) getApplication()).getUserPrefs();

        // 이 게임을 "방금 봤음"으로 기록 → 홈 "최근 본 게임" 섹션에 최신순으로 반영
        userPrefs.pushRecentGame(game.getId());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ──────── 리뷰 작성 결과를 받을 런처 등록 ────────
        //
        // 왜 onCreate 안에서 등록해야 하는가 (Lifecycle 연동):
        //   registerForActivityResult는 Activity 내부의 ActivityResultRegistry에 콜백을 등록함
        //   이 Registry는 Activity의 Lifecycle을 감지해서
        //     - STARTED 상태일 때만 결과 전달
        //     - 회전으로 Activity가 재생성되면 같은 키로 자동 재등록
        //     - destroy 시 자동 해제 (메모리 누수 방지)
        //
        //   공식 규칙: Activity가 STARTED 상태가 되기 전에 등록해야 함
        //     → STARTED 이후 등록 시 IllegalStateException 발생
        //     → 실무적으론 onCreate가 표준 위치 (또는 필드 선언 시 초기화도 가능)
        //   공식 문서:
        //     - Activity Result API 가이드:
        //       https://developer.android.com/training/basics/intents/result
        //     - registerForActivityResult 레퍼런스:
        //       https://developer.android.com/reference/androidx/activity/result/ActivityResultCaller
        //     - Lifecycle 상태 설명:
        //       https://developer.android.com/reference/androidx/lifecycle/Lifecycle.State
        //
        //   버튼 클릭 시점에 등록하면 회전 후 결과가 도착했을 때 콜백이 없어 결과가 누락될 수 있음
        //
        // 첫 번째 인자: Contract (계약서 — 입력/출력 타입 정의)
        //   new ActivityResultContracts.StartActivityForResult()
        //     = "입력은 Intent, 출력은 ActivityResult(resultCode + data Intent)"라는 범용 계약
        //   다른 Contract 예시:
        //     RequestPermission          → 입력 String(권한 이름),  출력 Boolean
        //     TakePicture                → 입력 Uri(저장 경로),     출력 Boolean
        //     PickVisualMedia            → 입력 요청,              출력 Uri
        //     GetContent                 → 입력 MIME String,      출력 Uri
        //   우리는 범용 Intent 실행 + Intent 결과 받기 패턴이라 StartActivityForResult 사용
        //
        // 두 번째 인자: 결과가 도착했을 때 실행될 람다 콜백
        //   result 파라미터의 타입은 Contract가 정한 출력 타입(ActivityResult)
        //   람다 안에서 바깥 필드(game, binding 등)에 접근 가능
        reviewLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // [1단계] 성공/취소 구분
                    // ReviewWriteActivity가 setResult(RESULT_OK, data) + finish() 하면
                    //   → resultCode = RESULT_OK, data = 결과 Intent
                    // 사용자가 뒤로가기로 취소하면
                    //   → resultCode = RESULT_CANCELED, data = null
                    boolean isOk = result.getResultCode() == RESULT_OK;
                    boolean hasData = result.getData() != null;
                    if (!isOk || !hasData) {
                        // 취소 또는 데이터 없음 → 아무것도 하지 않고 조기 종료
                        return;
                    }

                    // [2단계] 결과 Intent에서 Extras 꺼내기
                    // ReviewWriteActivity가 putExtra로 넣은 값을 같은 키로 꺼냄
                    // getFloatExtra의 두 번째 인자 0f: 키가 없을 때 반환할 기본값 (방어)
                    Intent data = result.getData();
                    float rating = data.getFloatExtra(ReviewWriteActivity.EXTRA_RATING, 0f);
                    String review = data.getStringExtra(ReviewWriteActivity.EXTRA_REVIEW);

                    // [3단계] 내 계정 파일에 정식 리뷰 저장 (계정마다 다른 "내 평가")
                    userPrefs.saveReview(game.getId(), rating, review);

                    // [3.5단계] 활동 로그에 "리뷰함" 기록 → 최근 활동 피드에 표시
                    userPrefs.addActivityLog(ActivityLogType.REVIEWED, game.getId(), review);

                    // [4단계] 전역 Game/Repository에도 반영 (보관함·홈·통계가 아직 전역 값을 읽으므로 유지)
                    // game은 Parcelable 사본이라 여기 setRating/setReview는 사본만 갱신 → 원본도 updateGame으로 갱신
                    game.setRating(rating);
                    game.setReview(review);
                    ((App) getApplication()).getGameRepository().updateGame(game);

                    // [5단계] 화면 재바인딩 → 바뀐 내 평가가 표시됨
                    bindGameData();
                }
        );

        // ──────── 스크린샷 결과를 받을 런처 등록 ────────
        // ScreenshotActivity가 갤러리에서 이미지를 고를 때마다 game을 setResult로 돌려줌
        // 여기서는 그 변경된 game을 받아 클래스 필드와 Repository 원본에 모두 반영
        screenshotLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    boolean isOk = result.getResultCode() == RESULT_OK;
                    boolean hasData = result.getData() != null;
                    if (!isOk || !hasData) {
                        return;
                    }

                    // 변경된 Game을 결과 Intent에서 꺼냄
                    Game updated = result.getData().getParcelableExtra(
                            ScreenshotActivity.EXTRA_GAME, Game.class);
                    if (updated == null) {
                        return;
                    }

                    // 클래스 필드 game도 최신 사본으로 교체
                    // (이후 다른 작업에서 game.getScreenshots()로 최신 목록을 보게 됨)
                    game = updated;

                    // Repository 원본에도 반영 → 다른 화면으로 돌아갔을 때 일관성 유지
                    ((App) getApplication()).getGameRepository().updateGame(game);

                    // 추가된 스크린샷이 상세 화면에 바로 보이도록 재바인딩
                    bindGameData();
                }
        );

        // 게임 정보를 화면에 표시
        bindGameData();

        // 버튼 리스너 등록
        setupButtons();
    }

    /// <summary>
    /// 화면이 다시 앞으로 올 때마다 "다른 사람들의 평가"를 새로 읽어 갱신
    /// (다른 계정이 이 게임에 리뷰를 남겼거나, 다른 화면 다녀온 뒤 최신 상태 반영)
    /// onResume은 첫 진입(onCreate 직후)에도 호출되므로 첫 표시도 여기서 처리됨
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();
        setupOthersReviews();
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 메인 큐에 남아있는 표지 반영 작업을 취소
    /// 디코딩이 끝나기 전에 뒤로가기로 나가면, 남은 작업이 사라진 화면을 붙잡아 누수가 생김
    /// removeCallbacksAndMessages(null) = 이 Handler에 걸린 예약을 전부 제거
    /// Unity로 비유하면 OnDestroy에서 CancelInvoke를 부르는 것과 같은 안전장치
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    /// <summary>
    /// 제목 옆 즐겨찾기 하트를 현재 상태로 갱신 (즐겨찾기면 채운 ♥·빨강, 아니면 빈 ♡·회색)
    /// </summary>
    private void updateFavoriteHeart() {
        boolean favorite = userPrefs.isFavorite(game.getId());
        binding.textViewFavoriteToggle.setText(favorite ? "♥" : "♡");
        binding.textViewFavoriteToggle.setTextColor(favorite ? 0xFFE53935 : 0xFF999999);
    }

    /// <summary>
    /// "다른 사람들의 평가" 섹션 채우기
    /// 이 게임에 대해 다른 계정들이 남긴 리뷰를 모아 평균 별점 + 목록으로 표시
    /// (다른 계정 리뷰가 없으면 안내 문구만)
    /// </summary>
    private void setupOthersReviews() {
        App app = (App) getApplication();
        CommunityRepository community = app.getCommunityRepository();
        String currentId = app.getAccountManager().getCurrentAccountId();
        int gameId = game.getId();

        // 나를 뺀 다른 계정들의 이 게임 리뷰
        List<GameReview> others = community.getReviewsForGame(gameId, currentId);

        // ── 전체 평균(나 포함) 계산 ──
        // 다른 사람들 별점 합 + (내가 평가했으면) 내 별점
        boolean hasMine = userPrefs.hasReview(gameId);
        int totalCount = others.size() + (hasMine ? 1 : 0);
        float totalSum = hasMine ? userPrefs.getRating(gameId) : 0f;
        for (GameReview review : others) {
            totalSum += review.getRating();
        }

        // 한 명이라도 평가했으면 전체 평균 줄 표시
        if (totalCount > 0) {
            String averageText = String.format(Locale.getDefault(), "%.1f", totalSum / totalCount);
            binding.textViewGameAverage.setText(
                    getString(R.string.detail_total_average, averageText, totalCount));
            binding.textViewGameAverage.setVisibility(View.VISIBLE);
        } else {
            binding.textViewGameAverage.setVisibility(View.GONE);
        }

        // ── 다른 사람들 리뷰: 미리보기 몇 개만, 나머지는 "더 보기" 게시판으로 ──
        final int PREVIEW_MAX = 3;
        boolean othersEmpty = others.isEmpty();
        binding.textViewOthersEmpty.setVisibility(othersEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerOthersReviews.setVisibility(othersEmpty ? View.GONE : View.VISIBLE);

        if (!othersEmpty) {
            // 미리보기는 앞에서 최대 PREVIEW_MAX개만 (전체는 게시판에서)
            List<GameReview> preview = others.size() > PREVIEW_MAX
                    ? new ArrayList<>(others.subList(0, PREVIEW_MAX))
                    : others;
            // 바깥 ScrollView와 충돌하지 않도록 자체 스크롤 끔
            binding.recyclerOthersReviews.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerOthersReviews.setNestedScrollingEnabled(false);
            binding.recyclerOthersReviews.setAdapter(new GameReviewAdapter(preview, userPrefs));
        }

        // 미리보기보다 리뷰가 많을 때만 "더 보기" → 게시판(전체 목록) 화면
        boolean hasMore = others.size() > PREVIEW_MAX;
        binding.buttonMoreReviews.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        if (hasMore) {
            binding.buttonMoreReviews.setOnClickListener(v -> openReviewBoard());
        }
    }

    /// <summary>
    /// "더 보기" → 이 게임의 리뷰 게시판(다른 사람들 전체 목록) 화면으로 이동
    /// </summary>
    private void openReviewBoard() {
        Intent intent = new Intent(this, ReviewBoardActivity.class);
        intent.putExtra(ReviewBoardActivity.EXTRA_GAME_ID, game.getId());
        intent.putExtra(ReviewBoardActivity.EXTRA_GAME_TITLE, game.getTitle());
        startActivity(intent);
    }

    /// <summary>
    /// ActionBar의 ← 버튼 클릭 처리
    /// home(android.R.id.home)이 ← 버튼의 ID
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ========== 버튼 설정 ==========

    /// <summary>
    /// 각 버튼에 클릭 리스너 등록
    /// </summary>
    private void setupButtons() {
        // 제목 옆 즐겨찾기 하트 → 토글 + 하트 갱신
        updateFavoriteHeart();
        binding.textViewFavoriteToggle.setOnClickListener(v -> {
            int gameId = game.getId();
            userPrefs.setFavorite(gameId, !userPrefs.isFavorite(gameId));
            updateFavoriteHeart();
        });

        // 상태 변경 버튼 → 상태 선택 다이얼로그 표시
        binding.buttonChangeStatus.setOnClickListener(v -> showStatusDialog());

        // 리뷰 작성 버튼 → ReviewWriteActivity를 forResult로 실행
        binding.buttonReview.setOnClickListener(v -> openReviewWrite());

        // 공유 버튼 → 게임 정보를 다른 앱으로 공유
        binding.buttonShare.setOnClickListener(v -> shareGame());

        // 스토어 열기 버튼 → 브라우저에서 Steam 페이지 열기
        binding.buttonStore.setOnClickListener(v -> openStoreUrl());

        // 스크린샷 버튼 → ScreenshotActivity로 이동
        binding.buttonScreenshot.setOnClickListener(v -> openScreenshot());
    }

    // ========== 상태 변경 ==========

    /// <summary>
    /// 진행 상태 선택 다이얼로그 표시
    /// 4개 상태(플레이중/완료/중단/백로그)를 단일 선택 목록으로 띄우고,
    /// 선택 시 Game과 Repository 원본에 반영 + 화면 갱신
    ///
    /// ──── 인덱스 ↔ enum 매핑 ────
    /// 다이얼로그 목록 순서 = GameStatus.values() 순서 (둘 다 같은 enum 배열에서 만듦)
    /// 현재 상태의 ordinal()을 초기 선택 위치로 전달 → 지금 상태에 체크 표시됨
    /// </summary>
    private void showStatusDialog() {
        GameStatus[] statuses = GameStatus.values();

        // 다이얼로그에 보여줄 상태 이름 배열 (enum 순서 그대로)
        String[] statusNames = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            statusNames[i] = statuses[i].getDisplayName();
        }

        // 현재 상태가 처음에 선택돼 보이도록 초기 인덱스 지정
        int currentIndex = game.getStatus().ordinal();

        new AlertDialog.Builder(this)
                .setTitle(R.string.detail_change_status)
                .setSingleChoiceItems(statusNames, currentIndex, (dialog, which) -> {
                    // 선택한 위치의 상태로 변경
                    GameStatus selected = statuses[which];
                    // "완료"로 새로 바뀌는 경우만 로그 (이미 완료였는데 다시 고르면 기록 안 함)
                    boolean newlyCompleted = selected == GameStatus.COMPLETED
                            && game.getStatus() != GameStatus.COMPLETED;
                    game.setStatus(selected);

                    // Repository 원본에도 반영 → 라이브러리 필터/리스트에 일관 반영
                    ((App) getApplication()).getGameRepository().updateGame(game);

                    // 완료로 바뀌었으면 활동 로그에 "완료함" 기록
                    if (newlyCompleted) {
                        userPrefs.addActivityLog(ActivityLogType.COMPLETED, game.getId(), "");
                    }

                    // 화면의 상태 텍스트 갱신
                    bindGameData();

                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /// <summary>
    /// ReviewWriteActivity를 실행하고 결과(별점/한줄평)를 받아옴
    /// 현재 게임의 ID, 제목, 기존 별점/한줄평을 Intent에 담아서 보냄
    ///
    /// ──── 결과 처리 위치 ────
    /// 결과가 돌아오면 onCreate에서 등록해둔 reviewLauncher의 람다가 실행됨
    /// → 별점/한줄평을 Game에 반영하고 bindGameData()로 화면 갱신
    /// </summary>
    private void openReviewWrite() {
        Intent intent = new Intent(this, ReviewWriteActivity.class);

        // 게임 정보를 putExtra로 전달 (ReviewWrite에서 제목 표시 + 기존 리뷰 채우기 용)
        intent.putExtra(ReviewWriteActivity.EXTRA_GAME_ID, game.getId());
        intent.putExtra(ReviewWriteActivity.EXTRA_GAME_TITLE, game.getTitle());
        // 프리필은 "내 계정"의 기존 리뷰 (전역 Game이 아니라 내 UserPrefs에서)
        intent.putExtra(ReviewWriteActivity.EXTRA_CURRENT_RATING, userPrefs.getRating(game.getId()));
        intent.putExtra(ReviewWriteActivity.EXTRA_CURRENT_REVIEW, userPrefs.getReview(game.getId()));

        // ──────── launch(intent) 내부 동작 ────────
        // 이 한 줄이 트리거하는 6단계:
        //   1. Contract.createIntent(intent) — Contract가 input을 실행용 Intent로 변환
        //      StartActivityForResult는 입력 Intent를 그대로 통과시킴
        //      (RequestPermission이라면 String → 권한요청 Intent로 변환 등)
        //   2. Registry가 내부 requestCode 자동 할당 — 옛 requestCode 상수를 대체
        //   3. 시스템에 startActivityForResult(실행Intent, 내부코드) 호출
        //   4. Android가 ReviewWriteActivity 실행 → 사용자 조작 → setResult + finish
        //   5. 결과가 돌아오면 ComponentActivity가 가로채서 Registry로 라우팅
        //   6. Contract.parseResult로 결과 변환 → onCreate에서 등록한 람다 호출
        //
        // Lifecycle에 따른 콜백 타이밍:
        //   - 결과 도착 시 STARTED 상태면 → 즉시 람다 실행
        //   - STOPPED 상태면 → Registry가 결과를 저장해뒀다가 STARTED 될 때 람다 실행
        //   - 회전으로 재생성 중이면 → 새 인스턴스가 같은 키로 재등록되고 그때 람다 실행
        //
        // 결과적으로 우리 코드는:
        //   - launch 호출 후 바로 다음 줄이 실행됨 (비동기)
        //   - 결과는 onCreate에서 등록한 reviewLauncher 람다로 자동 도착 (requestCode 불필요)
        //   - 같은 Launcher를 여러 번 launch해도 OK (매번 새로 등록할 필요 없음)
        reviewLauncher.launch(intent);
    }

    // ========== 암시적 Intent: 공유 ==========

    /// <summary>
    /// 게임 정보를 다른 앱(카톡, 메시지 등)으로 공유
    ///
    /// ──── 암시적 Intent + Chooser 학습 ────
    /// 명시적 Intent: 목적지 Activity를 직접 지정 (new Intent(this, ReviewWriteActivity.class))
    /// 암시적 Intent: "이런 작업을 할 수 있는 앱 아무나" 요청 (ACTION_SEND)
    ///
    /// Unity로 비유하면:
    /// 명시적 = 특정 오브젝트의 메서드를 직접 호출 (GetComponent<T>().DoSomething())
    /// 암시적 = SendMessage("DoSomething")로 "이걸 할 수 있는 누구든" 호출
    ///
    /// Chooser를 쓰는 이유:
    /// 그냥 startActivity(intent)하면 기본 앱이 설정된 경우 바로 그 앱으로 감
    /// createChooser()를 쓰면 항상 앱 선택 다이얼로그가 뜸
    /// </summary>
    private void shareGame() {
        // 공유할 텍스트 조합
        String shareText = game.getTitle();

        // 내 리뷰가 있으면 별점 + 한줄평도 포함 (내 계정 것)
        int gameId = game.getId();
        if (userPrefs.hasReview(gameId)) {
            shareText += "\n★ " + userPrefs.getRating(gameId) + " - " + userPrefs.getReview(gameId);
        }

        // ACTION_SEND: "이 데이터를 보낼 수 있는 앱 목록 보여줘"
        // setType("text/plain"): 보내는 데이터가 텍스트임을 알려줌 (MIME 타입)
        // EXTRA_TEXT: 공유할 텍스트 본문
        // EXTRA_SUBJECT: 제목 (이메일 앱 등에서 사용)
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, game.getTitle());
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // createChooser: 항상 앱 선택 다이얼로그를 표시
        // 두 번째 파라미터는 다이얼로그 상단에 표시되는 제목
        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.detail_share));
        startActivity(chooser);
    }

    // ========== 스크린샷 화면 이동 ==========

    /// <summary>
    /// ScreenshotActivity로 이동
    /// 현재 Game 객체를 통째로 전달하여 ScreenshotActivity가 스크린샷을 추가할 수 있게 함
    /// 결과는 screenshotLauncher 람다로 돌아와 Repository에 반영됨
    /// </summary>
    private void openScreenshot() {
        Intent intent = new Intent(this, ScreenshotActivity.class);
        intent.putExtra(ScreenshotActivity.EXTRA_GAME, game);
        screenshotLauncher.launch(intent);
    }

    // ========== 암시적 Intent: 스토어 열기 ==========

    /// <summary>
    /// 브라우저에서 게임의 Steam 스토어 페이지를 열기
    ///
    /// ──── ACTION_VIEW 학습 ────
    /// ACTION_SEND는 "이 데이터를 보낼 수 있는 앱"을 찾지만,
    /// ACTION_VIEW는 "이 URI를 열 수 있는 앱"을 찾음
    /// URI의 종류에 따라 Android가 적절한 앱을 자동으로 선택:
    ///   https://... → 웹 브라우저(Chrome 등)가 열림
    ///   tel:010... → 전화 앱이 열림
    ///   geo:37.5,127.0 → 지도 앱이 열림
    ///
    /// Unity로 비유하면 Application.OpenURL()과 동일
    /// Chooser를 쓰지 않는 이유: URL을 여는 앱은 보통 브라우저 하나이므로
    /// 앱 선택 다이얼로그 없이 바로 열어도 자연스러움
    /// </summary>
    private void openStoreUrl() {
        // 스토어 URL이 없으면 사용자에게 알림 (커스텀 게임 등 URL이 없는 경우)
        String url = game.getStoreUrl();
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, R.string.detail_no_store_url, Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Uri란? ──
        // 공식 문서: https://developer.android.com/reference/android/net/Uri
        // Intent 가이드: https://developer.android.com/guide/components/intents-common
        // 주소를 표현하는 Android 클래스 (Unity의 AssetReference처럼 경로를 구조화해서 들고 있음)
        // String 그대로면 그냥 글자 덩어리지만, Uri 객체로 만들면 주소의 각 부분을 메서드로 꺼낼 수 있음
        //
        // 예) Uri.parse("https://store.steampowered.com/app/1245620/ELDEN_RING/")
        //   uri.getScheme() → "https"                     (Scheme: 통신 규약. 어떤 방식으로 연결할지)
        //   uri.getHost()   → "store.steampowered.com"    (Host: 서버 주소. 인터넷에서 어떤 컴퓨터에 접속할지)
        //   uri.getPath()   → "/app/1245620/ELDEN_RING/"  (Path: 경로. 그 서버 안에서 어떤 페이지를 볼지)
        //
        // Android가 Uri를 요구하는 이유:
        // scheme(https, tel, geo 등)을 보고 어떤 앱을 열지 판단하기 때문
        // → "https" → 브라우저, "tel" → 전화 앱, "geo" → 지도 앱
        //
        // ── Intent 생성자가 String이 아닌 Uri를 받는 이유 ──
        // new Intent(ACTION_VIEW, url) → 컴파일 에러 (String은 못 받음)
        // new Intent(ACTION_VIEW, Uri.parse(url)) → Uri 타입이라 OK
        // → Android가 이걸 보고 "https 주소를 VIEW 할 수 있는 앱 = 브라우저"를 찾아서 실행
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(viewIntent);
    }

    // ========== 데이터 바인딩 ==========

    /// <summary>
    /// Game 객체의 정보를 각 View에 표시
    /// Unity로 비유하면 UI 텍스트에 데이터를 세팅하는 것
    /// </summary>
    private void bindGameData() {
        // 제목
        binding.textViewTitle.setText(game.getTitle());

        // 장르 · 플랫폼
        String genrePlatform = game.getGenre().getDisplayName()
                + " · " + game.getPlatform().getDisplayName();
        binding.textViewGenrePlatform.setText(genrePlatform);

        // 진행 상태 배지 (이름 + 상태색 배경)
        // 배지 모양(둥근 배경)은 XML, 색은 상태별로 다르므로 코드에서 tint 적용
        GameStatus status = game.getStatus();
        binding.textViewStatus.setText(status.getDisplayName());
        binding.textViewStatus.setBackgroundTintList(
                ColorStateList.valueOf(status.getColorArgb()));

        // 별점/한줄평은 "내 계정" 것을 표시 (전역 Game이 아니라 내 UserPrefs에서)
        int gameId = game.getId();
        float myRating = userPrefs.getRating(gameId);
        boolean hasMyReview = userPrefs.hasReview(gameId);

        // 별점: RatingBar로 별을 채우고 숫자도 함께 표시
        binding.ratingBar.setRating(myRating);

        // 한줄평 (내 리뷰 유무에 따라 숫자/문구 분기)
        if (hasMyReview) {
            binding.textViewRatingValue.setText(String.valueOf(myRating));
            binding.textViewReview.setText(userPrefs.getReview(gameId));
        } else {
            // 내 리뷰가 없으면 숫자는 비우고 "리뷰 없음" 안내
            binding.textViewRatingValue.setText("");
            binding.textViewReview.setText("리뷰 없음");
        }

        // 리뷰 버튼 텍스트: 내 리뷰가 이미 있으면 "수정", 없으면 "작성"
        // 같은 화면에서 같은 버튼이지만 상태에 따라 의미가 달라지므로 라벨도 맞춰줌
        if (hasMyReview) {
            binding.buttonReview.setText(R.string.detail_edit_review);
        } else {
            binding.buttonReview.setText(R.string.detail_write_review);
        }

        // 표지 이미지 (백그라운드 디코딩 → Handler로 메인 반영)
        // 주의: getIdentifier()는 이름(문자열)으로 이미지를 찾아서 느림 (비권장 표시됨)
        // 게임마다 이미지 이름이 달라 문자열 검색이 불가피
        int coverResId = getResources().getIdentifier(
                game.getCoverAssetName(), "drawable", getPackageName());
        loadCoverAsync(coverResId);

        // 스크린샷 썸네일 표시
        bindScreenshots();
    }

    /// <summary>
    /// 표지 이미지를 백그라운드에서 디코딩한 뒤, Handler로 메인 스레드에서 화면에 반영
    /// 왜 이렇게 하나:
    ///   setImageResource(resId)는 메인 스레드에서 JPG를 그 자리에서 디코딩한다 →
    ///   표지가 크면 그동안 화면이 잠깐 멈출 수 있음
    ///   그래서 무거운 디코딩(BitmapFactory.decodeResource)은 서브 스레드로 빼고,
    ///   완성된 Bitmap을 화면에 얹는 일(setImageBitmap)만 Handler로 메인에 넘긴다
    /// coverResId가 0이면 해당 이미지가 없다는 뜻 → 기본 아이콘 표시
    /// </summary>
    private void loadCoverAsync(int coverResId) {
        // 이미지가 없으면 디코딩할 것도 없으니 기본 아이콘만 바로 표시
        if (coverResId == 0) {
            binding.imageViewCover.setImageResource(R.mipmap.ic_launcher);
            return;
        }

        // [로딩 상태] 디코딩이 끝나기 전에는 회색 박스를 보여줌
        // → 완성되면 실제 표지(컬러 이미지)로 바뀌므로, 로딩 전/후가 확연히 다르게 보임
        binding.imageViewCover.setImageDrawable(new ColorDrawable(0xFFBDBDBD));

        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 디코딩을 일부러 1.5초 느리게 해서 로딩 과정을 눈으로 확인 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                // 단일 작업이라 발생하지 않음 (컴파일러 요구사항)
            }

            // ⏳ 무거운 일: JPG를 실제 Bitmap으로 디코딩 (여기서 시간이 걸림)
            Bitmap cover = BitmapFactory.decodeResource(getResources(), coverResId);

            // 결과(화면 반영)만 메인 줄로 넘김 — setImageBitmap은 메인에서만 안전
            mainHandler.post(() -> binding.imageViewCover.setImageBitmap(cover));
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 추가된 스크린샷을 가로 썸네일로 표시
    /// 스크린샷이 없으면 라벨·스크롤뷰를 숨기고, 있으면 각 Uri로 ImageView를 동적 생성
    /// (개수가 적고 재활용이 불필요해 RecyclerView 대신 HorizontalScrollView 사용)
    /// </summary>
    private void bindScreenshots() {
        java.util.List<String> screenshots = game.getScreenshots();
        boolean hasScreenshots = screenshots != null && !screenshots.isEmpty();

        // 스크린샷 없으면 섹션 전체 숨김
        binding.textViewScreenshotsLabel.setVisibility(hasScreenshots ? View.VISIBLE : View.GONE);
        binding.scrollScreenshots.setVisibility(hasScreenshots ? View.VISIBLE : View.GONE);
        if (!hasScreenshots) {
            return;
        }

        // 매번 다시 그리므로 기존 썸네일 제거 후 재생성
        binding.layoutScreenshots.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int widthPx = (int) (120 * density);
        int heightPx = (int) (70 * density);
        int marginPx = (int) (8 * density);

        for (String uriString : screenshots) {
            ImageView thumbnail = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(widthPx, heightPx);
            lp.setMarginEnd(marginPx);
            thumbnail.setLayoutParams(lp);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setImageURI(Uri.parse(uriString));
            binding.layoutScreenshots.addView(thumbnail);
        }
    }
}
