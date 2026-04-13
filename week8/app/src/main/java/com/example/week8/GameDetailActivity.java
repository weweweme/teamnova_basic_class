package com.example.week8;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

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
    /// ReviewWriteActivity에 forResult 요청할 때 사용하는 요청 코드
    /// onActivityResult에서 "이 결과가 리뷰 작성에서 온 건지" 구분하는 데 사용
    /// Unity로 비유하면 콜백을 구분하는 ID 태그
    /// </summary>
    private static final int REQUEST_CODE_REVIEW = 1001;

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityGameDetailBinding binding;

    /// <summary>
    /// Intent로 전달받은 게임 데이터
    /// </summary>
    private Game game;

    // ========== Lifecycle ==========

    /// <summary>
    /// 상세 화면 생성
    /// Intent extras에서 Game 객체를 꺼내고 화면에 바인딩 + 버튼 리스너 등록
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // edge-to-edge 비활성화
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

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

    /// <summary>
    /// ReviewWriteActivity에서 돌아왔을 때 결과를 받는 콜백
    ///
    /// forResult 흐름 정리:
    /// 1. 이 화면에서 startActivityForResult(intent, REQUEST_CODE_REVIEW) 호출
    /// 2. ReviewWrite에서 setResult(RESULT_OK, data) + finish()
    /// 3. 이 메서드가 자동 호출됨
    /// 4. requestCode로 "어디서 온 결과인지" 구분
    /// 5. resultCode로 "성공/취소" 확인
    /// 6. data Intent에서 별점/한줄평 꺼내서 Game에 반영
    ///
    /// Unity로 비유하면 팝업 Panel의 onClose 콜백이 호출되는 것
    /// </summary>
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 리뷰 작성 결과인지 확인
        boolean isReviewResult = requestCode == REQUEST_CODE_REVIEW;
        // 사용자가 저장 버튼을 눌렀는지 확인 (뒤로가기면 RESULT_CANCELED)
        boolean isSuccess = resultCode == RESULT_OK;
        boolean hasData = data != null;

        if (isReviewResult && isSuccess && hasData) {
            // 결과에서 별점/한줄평 꺼내기
            float rating = data.getFloatExtra(ReviewWriteActivity.EXTRA_RATING, 0f);
            String review = data.getStringExtra(ReviewWriteActivity.EXTRA_REVIEW);

            // Game 객체에 반영
            game.setRating(rating);
            game.setReview(review);

            // 화면 갱신
            bindGameData();
        }
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

        // 스토어, 스크린샷 버튼은 이후 단계에서 구현
    }

    /// <summary>
    /// ReviewWriteActivity를 startActivityForResult로 실행
    /// 현재 게임의 ID, 제목, 기존 별점/한줄평을 Intent에 담아서 보냄
    /// </summary>
    private void openReviewWrite() {
        Intent intent = new Intent(this, ReviewWriteActivity.class);

        // 게임 정보를 putExtra로 전달 (ReviewWrite에서 제목 표시 + 기존 리뷰 채우기 용)
        intent.putExtra(ReviewWriteActivity.EXTRA_GAME_ID, game.getId());
        intent.putExtra(ReviewWriteActivity.EXTRA_GAME_TITLE, game.getTitle());
        intent.putExtra(ReviewWriteActivity.EXTRA_CURRENT_RATING, game.getRating());
        intent.putExtra(ReviewWriteActivity.EXTRA_CURRENT_REVIEW, game.getReview());

        // forResult로 실행: 결과를 onActivityResult에서 받음
        // REQUEST_CODE_REVIEW로 "이건 리뷰 작성 요청이다"라고 태그를 붙임
        startActivityForResult(intent, REQUEST_CODE_REVIEW);
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
