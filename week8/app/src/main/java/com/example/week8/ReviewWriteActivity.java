package com.example.week8;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.databinding.ActivityReviewWriteBinding;

/// <summary>
/// 리뷰 작성 화면
/// GameDetailActivity에서 forResult로 호출되어, 별점+한줄평을 입력받고 결과를 돌려줌
/// Unity로 비유하면 팝업 Panel에서 입력받고 콜백으로 결과를 돌려주는 패턴
///
/// ──── Intent 학습: 결과 반환 (forResult) ────
/// 흐름:
/// 1. GameDetail이 startActivityForResult(intent, code)로 이 화면을 실행
/// 2. 사용자가 별점+한줄평 입력 후 저장 버튼 클릭
/// 3. 이 화면이 setResult(RESULT_OK, data)로 결과를 설정하고 finish()
/// 4. GameDetail의 onActivityResult에서 결과를 받아 화면 갱신
///ㅁㄴ
/// ──── Lifecycle 학습 (이후 단계에서 추가) ────
/// onSaveInstanceState: 회전 시 입력 중인 텍스트/별점 보존
/// onPause: 임시 초안(draft) 영구 저장
/// </summary>
public class ReviewWriteActivity extends AppCompatActivity {

    /// <summary>
    /// 결과 Intent에서 별점을 꺼낼 때 사용하는 키
    /// GameDetailActivity와 동일한 키를 사용해야 함ㅁ
    /// </summary>
    public static final String EXTRA_RATING = "extra_rating";

    /// <summary>
    /// 결과 Intent에서 한줄평을 꺼낼 때 사용하는 키
    /// </summary>
    public static final String EXTRA_REVIEW = "extra_review";

    /// <summary>
    /// Intent에서 게임 ID를 받을 때 사용하는 키
    /// </summary>
    public static final String EXTRA_GAME_ID = "extra_game_id";

    /// <summary>
    /// Intent에서 게임 제목을 받을 때 사용하는 키
    /// </summary>
    public static final String EXTRA_GAME_TITLE = "extra_game_title";

    /// <summary>
    /// Intent에서 기존 별점을 받을 때 사용하는 키
    /// </summary>
    public static final String EXTRA_CURRENT_RATING = "extra_current_rating";

    /// <summary>
    /// Intent에서 기존 한줄평을 받을 때 사용하는 키
    /// </summary>
    public static final String EXTRA_CURRENT_REVIEW = "extra_current_review";

    // ========== 회전 대응 Bundle 키 ==========
    // onSaveInstanceState/onRestoreInstanceState에서 사용
    // Unity로 비유하면 씬 리로드 시 임시 보관하는 키-값 쌍의 키

    /// <summary>
    /// 회전 시 별점을 Bundle에 저장할 때 사용하는 키
    /// </summary>
    private static final String KEY_SAVED_RATING = "key_saved_rating";

    /// <summary>
    /// 회전 시 한줄평을 Bundle에 저장할 때 사용하는 키
    /// </summary>
    private static final String KEY_SAVED_REVIEW = "key_saved_review";

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityReviewWriteBinding binding;

    /// <summary>
    /// 현재 선택된 별점 (0.0 ~ 5.0, 0.5 단위)
    /// SeekBar(0~10)를 2로 나눠서 계산
    /// </summary>
    private float currentRating;

    // ========== Lifecycle ==========

    /// <summary>
    /// 리뷰 작성 화면 생성
    /// Intent에서 게임 정보를 받아 화면에 표시하고,
    /// SeekBar/버튼 리스너를 등록
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityReviewWriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Intent에서 게임 정보 받기
        String gameTitle = getIntent().getStringExtra(EXTRA_GAME_TITLE);
        float existingRating = getIntent().getFloatExtra(EXTRA_CURRENT_RATING, 0f);
        String existingReview = getIntent().getStringExtra(EXTRA_CURRENT_REVIEW);

        // 게임 제목 표시
        binding.textViewGameTitle.setText(gameTitle);

        // 기존 별점/한줄평이 있으면 채워넣기
        currentRating = existingRating;
        // SeekBar는 0~10 정수, 별점은 0.0~5.0이므로 ×2
        binding.seekBarRating.setProgress((int) (existingRating * 2));
        updateRatingLabel();

        if (existingReview != null) {
            binding.editTextReview.setText(existingReview);
        }

        // SeekBar 변경 리스너
        // Unity로 비유하면 Slider.onValueChanged.AddListener
        binding.seekBarRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // SeekBar 값(0~10)을 2로 나눠서 별점(0.0~5.0)으로 변환
                currentRating = progress / 2.0f;
                updateRatingLabel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 사용하지 않지만 인터페이스 구현을 위해 필요
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 사용하지 않지만 인터페이스 구현을 위해 필요
            }
        });

        // 저장 버튼 클릭 → 결과 반환 후 화면 닫기
        binding.buttonSave.setOnClickListener(v -> saveAndFinish());
    }

    // ========== 회전 대응 (onSaveInstanceState / onRestoreInstanceState) ==========
    // Unity로 비유하면 씬이 다시 로드될 때 OnDisable에서 값을 빼놓고 OnEnable에서 다시 넣는 패턴
    // SharedPreferences(영구 저장)와 달리 Bundle은 메모리에만 존재하는 휘발성 데이터
    // → 앱이 완전히 종료되면 사라짐, 화면 회전처럼 Activity가 재생성될 때만 유효

    /// <summary>
    /// Activity가 파괴되기 직전에 호출되어, 현재 입력 상태를 Bundle에 저장
    /// 화면 회전 시 Android가 Activity를 파괴→재생성하므로,
    /// 사용자가 입력 중인 별점과 한줄평을 잃지 않도록 임시 보관
    /// </summary>
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // 현재 별점 저장 (SeekBar → float 변환값)
        outState.putFloat(KEY_SAVED_RATING, currentRating);

        // 현재 한줄평 저장
        // EditText는 id가 있으면 Android가 자동 복원해주지만,
        // 학습 목적으로 명시적 저장/복원을 함께 구현
        String review = "";
        if (binding.editTextReview.getText() != null) {
            review = binding.editTextReview.getText().toString();
        }
        outState.putString(KEY_SAVED_REVIEW, review);
    }

    /// <summary>
    /// Activity 재생성 후 onCreate → onStart 사이에 호출
    /// onSaveInstanceState에서 저장한 Bundle을 받아 입력 상태를 복원
    /// onCreate에서 Intent 기본값을 세팅한 뒤, 여기서 회전 전 값으로 덮어씀
    /// </summary>
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // 별점 복원: Bundle → currentRating → SeekBar 위치 → 라벨 텍스트
        currentRating = savedInstanceState.getFloat(KEY_SAVED_RATING, 0f);
        binding.seekBarRating.setProgress((int) (currentRating * 2));
        updateRatingLabel();

        // 한줄평 복원
        String savedReview = savedInstanceState.getString(KEY_SAVED_REVIEW, "");
        binding.editTextReview.setText(savedReview);
    }

    /// <summary>
    /// ActionBar의 ← 버튼 클릭 처리
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ========== UI 갱신 ==========

    /// <summary>
    /// 별점 라벨 텍스트 갱신 (예: "별점: ★ 3.5")
    /// </summary>
    private void updateRatingLabel() {
        String label = getString(R.string.review_rating_label, String.valueOf(currentRating));
        binding.textViewRatingLabel.setText(label);
    }

    // ========== 결과 반환 ==========

    /// <summary>
    /// 저장 버튼 클릭 시 호출
    /// 별점과 한줄평을 Intent에 담아 결과로 설정하고 화면을 닫음
    ///
    /// 흐름:
    /// 1. 새 Intent를 만들어 별점/한줄평을 putExtra로 담음
    /// 2. setResult(RESULT_OK, intent)로 "성공 + 데이터"를 설정
    /// 3. finish()로 이 화면을 닫음
    /// 4. GameDetail의 onActivityResult가 호출되어 결과를 받음
    /// </summary>
    private void saveAndFinish() {
        String review = "";
        if (binding.editTextReview.getText() != null) {
            review = binding.editTextReview.getText().toString();
        }

        // 결과 Intent 생성 (별도의 목적지 없이, 데이터를 담는 봉투로만 사용)
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RATING, currentRating);
        resultIntent.putExtra(EXTRA_REVIEW, review);

        // RESULT_OK: "정상적으로 완료됨"을 의미 (뒤로가기로 나가면 RESULT_CANCELED)
        setResult(RESULT_OK, resultIntent);

        // 화면 닫기 → GameDetail로 돌아감
        finish();
    }
}
