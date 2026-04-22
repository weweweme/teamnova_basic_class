package com.example.week8;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.databinding.ActivityGameDetailBinding;
import com.example.week8.model.Game;

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
        //   람다 안에서 바깥 필드(game, binding 등)에 접근 가능 (클로저)
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

                    // [3단계] 클래스 필드 game에 반영
                    game.setRating(rating);
                    game.setReview(review);

                    // [4단계] 화면 재바인딩 → 변경된 별점/한줄평이 TextView에 표시됨
                    bindGameData();
                }
        );

        // 게임 정보를 화면에 표시
        bindGameData();

        // 버튼 리스너 등록
        setupButtons();
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
        // 리뷰 작성 버튼 → ReviewWriteActivity를 forResult로 실행
        binding.buttonReview.setOnClickListener(v -> openReviewWrite());

        // 공유 버튼 → 게임 정보를 다른 앱으로 공유
        binding.buttonShare.setOnClickListener(v -> shareGame());

        // 스토어 열기 버튼 → 브라우저에서 Steam 페이지 열기
        binding.buttonStore.setOnClickListener(v -> openStoreUrl());

        // 스크린샷 버튼 → ScreenshotActivity로 이동
        binding.buttonScreenshot.setOnClickListener(v -> openScreenshot());
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
        intent.putExtra(ReviewWriteActivity.EXTRA_CURRENT_RATING, game.getRating());
        intent.putExtra(ReviewWriteActivity.EXTRA_CURRENT_REVIEW, game.getReview());

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

        // 리뷰가 있으면 별점 + 한줄평도 포함
        boolean hasReview = game.getReview() != null && !game.getReview().isEmpty();
        if (hasReview) {
            shareText += "\n★ " + game.getRating() + " - " + game.getReview();
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
    /// 현재 게임의 제목을 Intent extras로 전달 (어떤 게임의 스크린샷인지 표시용)
    /// 카메라 호출은 ScreenshotActivity 내부에서 처리 (다음 커밋에서 구현)
    /// </summary>
    private void openScreenshot() {
        Intent intent = new Intent(this, ScreenshotActivity.class);
        intent.putExtra(ScreenshotActivity.EXTRA_GAME_TITLE, game.getTitle());
        startActivity(intent);
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

        // 별점 + 한줄평
        boolean hasReview = game.getReview() != null && !game.getReview().isEmpty();
        String ratingReview;
        if (hasReview) {
            ratingReview = "★ " + game.getRating() + "  " + game.getReview();
        } else {
            ratingReview = "리뷰 없음";
        }
        binding.textViewRatingReview.setText(ratingReview);

        // 표지 이미지
        // 주의: getIdentifier()는 이름(문자열)으로 이미지를 찾아서 느림 (비권장 표시됨)
        // R.drawable.cover_zelda 처럼 직접 지정하면 빠르지만,
        // 게임마다 이미지 이름이 다르므로 문자열 검색이 불가피
        int coverResId = getResources().getIdentifier(
                game.getCoverAssetName(), "drawable", getPackageName());
        if (coverResId != 0) {
            binding.imageViewCover.setImageResource(coverResId);
        } else {
            binding.imageViewCover.setImageResource(R.mipmap.ic_launcher);
        }
    }
}
