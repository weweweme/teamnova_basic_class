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

    // ========== Intent 계약: GameDetail ↔ ReviewWrite 데이터 교환 ==========
    // 이 Activity가 "호출될 때 받는 값(입력)"과 "결과로 돌려주는 값(출력)"의 계약 상수
    // Activity는 자기 자신의 계약을 자기가 소유 — 호출자는 ReviewWriteActivity.EXTRA_XXX로 참조

    // ─── 입력 (GameDetail → ReviewWrite) ─────────────────────────────────

    /// <summary>
    /// 어떤 게임의 리뷰를 작성/수정 중인지 식별하는 게임 ID
    /// 사용 시점: GameDetail에서 "리뷰 작성" 버튼을 눌러 이 화면을 열 때
    ///   GameDetail 쪽:  intent.putExtra(EXTRA_GAME_ID, game.getId());
    ///   ReviewWrite 쪽: getIntent().getIntExtra(EXTRA_GAME_ID, -1);
    /// (현재는 사용 안 하지만, 10주차 DB 저장 시 "어느 게임의 리뷰인지" 판별에 사용 예정)
    /// </summary>
    public static final String EXTRA_GAME_ID = "extra_game_id";

    /// <summary>
    /// 이 화면 상단에 표시할 게임 제목 (예: "엘든 링")
    /// 사용 시점: GameDetail에서 ReviewWrite를 열 때 → 상단 textViewGameTitle에 바로 표시
    /// 사용자가 "어떤 게임의 리뷰를 작성 중인지" 한눈에 알 수 있게 하기 위함
    /// </summary>
    public static final String EXTRA_GAME_TITLE = "extra_game_title";

    /// <summary>
    /// 기존에 이미 입력되어 있던 별점 (0.0~5.0)
    /// 사용 시점: 이미 별점 3.5를 준 게임의 리뷰를 "수정"하러 들어올 때
    ///   → SeekBar가 처음부터 3.5 위치로 세팅돼서 "수정 중"임이 직관적으로 보임
    /// 새로 작성 모드면 0.0이 넘어옴 → SeekBar가 0에서 시작
    /// </summary>
    public static final String EXTRA_CURRENT_RATING = "extra_current_rating";

    /// <summary>
    /// 기존에 이미 입력되어 있던 한줄평
    /// 사용 시점: 수정 모드 진입 시 EditText에 미리 텍스트를 채워넣어
    ///   사용자가 기존 문구를 지우고 새로 쓸 필요 없이 바로 편집 가능하게 함
    /// 새로 작성 모드면 빈 문자열이 넘어옴 → EditText도 빈 상태
    /// </summary>
    public static final String EXTRA_CURRENT_REVIEW = "extra_current_review";

    // ─── 출력 (ReviewWrite → GameDetail 결과 Intent) ──────────────────────

    /// <summary>
    /// 사용자가 SeekBar로 조절한 최종 별점 (0.0~5.0)
    /// 사용 시점: "저장" 버튼 클릭 시 결과 Intent에 담김
    ///   ReviewWrite 쪽: resultIntent.putExtra(EXTRA_RATING, currentRating);
    ///   GameDetail 쪽(reviewLauncher 람다 안):
    ///     result.getData().getFloatExtra(ReviewWriteActivity.EXTRA_RATING, 0f);
    /// → GameDetail이 이 값을 받아 Game 객체에 반영하고 화면을 다시 그림
    /// </summary>
    public static final String EXTRA_RATING = "extra_rating";

    /// <summary>
    /// 사용자가 EditText에 입력한 최종 한줄평 텍스트
    /// 사용 시점: "저장" 버튼 클릭 시 결과 Intent에 담겨 GameDetail로 전달
    /// → GameDetail이 카드의 "리뷰 없음" 자리를 이 한줄평으로 교체
    /// </summary>
    public static final String EXTRA_REVIEW = "extra_review";

    // ========== 회전 대응 Bundle 키 ==========
    // 공식 문서: https://developer.android.com/topic/libraries/architecture/saving-states
    //
    // ──── onSaveInstanceState / onRestoreInstanceState 란? ────
    // 시스템이 Activity를 "임시로 파괴 후 재생성"해야 할 때 자동 호출되는 콜백 쌍
    //   onSaveInstanceState(outState) → 파괴 직전 호출 → Bundle에 상태 저장
    //   onRestoreInstanceState(bundle) → 재생성 후 호출 → Bundle에서 상태 꺼내 복원
    //
    // ──── 호출되는 대표 상황 (임시 파괴) ────
    //   1. 화면 회전 (세로 ↔ 가로 전환) ← 가장 흔한 케이스
    //   2. 시스템 언어 변경 (한국어 → 영어 등)
    //   3. 다크모드 토글 (라이트 ↔ 다크)
    //   4. 폰트 크기/디스플레이 크기 변경, 멀티윈도우 전환
    //   5. 메모리 부족 → 백그라운드 앱의 프로세스가 시스템에 의해 죽었다가 복귀
    //
    // ──── 호출 안 되는 상황 (영구 종료) ────
    //   - 사용자가 뒤로가기 버튼으로 나감
    //   - 코드에서 finish() 호출
    //   - 앱 스위처에서 스와이프로 종료
    //   → "더 이상 필요 없으니 끝"이라는 의도 → 상태 저장 안 함
    //
    // ──── Lifecycle 흐름 ────
    //   [정상 종료]  onPause → onStop → onDestroy
    //   [재생성]    onPause → onStop → onSaveInstanceState → onDestroy
    //                → (새 인스턴스) onCreate(Bundle) → onStart
    //                → onRestoreInstanceState(Bundle) → onResume
    //
    // ──── Android가 자동으로 저장해주는 항목 ────
    // id가 붙은 View의 기본 상태는 Android가 자동 저장/복원 함
    //   - EditText의 입력 텍스트, CheckBox의 체크 상태, ScrollView의 스크롤 위치 등
    // 그래서 editTextReview는 사실 수동 저장 없이도 텍스트가 복원됨
    // 하지만 SeekBar 값으로부터 계산한 currentRating 같은 "내 로직 변수"는 자동 저장 X
    //   → 수동으로 Bundle에 넣어야 함 (학습 목적으로 한줄평도 같이 수동 저장)
    //
    // ──── 테스트 방법 ────
    // 에뮬레이터에서 Ctrl+← / Ctrl+→ (Mac: Cmd+←/→)로 회전
    //   → 별점 3.5 + 한줄평 입력 상태에서 회전 → 값 유지되는지 확인
    //
    // Unity 비유: 씬이 리로드될 때 OnDisable에서 값을 빼놓고 OnEnable에서 다시 넣는 패턴
    // SharedPreferences(영구 저장)와의 차이: Bundle은 메모리 기반 휘발성, 앱 종료 시 사라짐

    /// <summary>
    /// Bundle 저장 키: 회전 시점의 currentRating(별점) 값
    /// onSaveInstanceState에서 outState.putFloat으로 저장,
    /// onRestoreInstanceState에서 savedInstanceState.getFloat으로 복원
    /// </summary>
    private static final String KEY_SAVED_RATING = "key_saved_rating";

    /// <summary>
    /// Bundle 저장 키: 회전 시점의 EditText 내용(한줄평)
    /// EditText는 id가 있어 자동 복원되지만, 학습 목적으로 명시적 저장/복원도 함께 구현
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
        //
        // EditText의 텍스트는 사실 여기서 저장 안 해도 자동으로 복원됨
        //   - XML에서 android:id가 붙어 있으면 super.onSaveInstanceState(outState) 가
        //     View 계층 상태를 Bundle에 자동으로 넣어줌 (EditText 텍스트도 여기 포함)
        //   - 복원 시 super.onRestoreInstanceState(savedInstanceState) 가
        //     EditText에 자동으로 텍스트를 다시 세팅해줌
        //
        // 주의 (테스트하다가 헷갈리는 포인트):
        //   이 줄만 지워보고 회전하면 한줄평이 사라지는데, 이건 "자동 저장이 안 돼서"가 아니라
        //   아래 onRestoreInstanceState의 setText("") 호출이 자동 복원 값을 덮어쓰기 때문임
        //   → 자동 저장을 진짜로 체감하려면 복원 쪽 setText 호출도 함께 지워야 함
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
        //
        // 이 setText 호출은 "자동 복원된 값을 한 번 더 덮어쓰는" 구조
        //   super.onRestoreInstanceState(savedInstanceState)가 이미 EditText에
        //   자동으로 텍스트를 채워놨는데, 바로 아래 setText 호출이 그 위를 덮어씀
        //   - Bundle에 KEY_SAVED_REVIEW 가 있으면: 같은 값이라 겉보기 변화 없음
        //   - Bundle에 KEY_SAVED_REVIEW 가 없으면: 기본값 ""로 덮어 써서 자동 복원 효과가 사라짐
        //
        // 그래서 저장(onSaveInstanceState)과 복원(여기)은 항상 쌍으로 관리해야 하고,
        // 둘 다 주석 처리해야 "자동 저장/복원"이 드러남
        String savedReview = savedInstanceState.getString(KEY_SAVED_REVIEW, "");
        binding.editTextReview.setText(savedReview);
    }

    /// <summary>
    /// ActionBar의 ← 버튼 클릭 처리
    ///
    /// finish() 동작:
    ///   - 이 Activity를 "종료해달라"고 Android에 요청
    ///   - 호출 직후 onPause → onStop → onDestroy 순서로 콜백 자동 실행됨
    ///   - 백스택에서 이 Activity가 제거되고 호출자(GameDetail)로 돌아감
    ///   - setResult를 호출하지 않았으므로 호출자는 RESULT_CANCELED를 받음
    ///     → GameDetail의 reviewLauncher 람다는 "취소"로 판단해 아무 작업 안 함
    ///
    /// finish()가 호출되는 경로:
    ///   ① 사용자가 ActionBar ← 버튼 클릭 (이 메서드)
    ///   ② 사용자가 뒤로가기 버튼 클릭 (Android가 내부적으로 finish() 호출)
    ///   ③ saveAndFinish() 안에서 setResult 후 명시적으로 호출 (저장 시나리오)
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
    /// 4. 화면이 닫히는 순간 GameDetail의 reviewLauncher 람다가 자동 호출되어 결과 수신
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

        // ──── setResult란? ────
        // 공식 레퍼런스: https://developer.android.com/reference/android/app/Activity#setResult(int,%20android.content.Intent)
        //
        // "이 Activity가 종료될 때 호출자(부모)에게 돌려줄 결과를 미리 예약하는 메서드"
        //   시그니처 2가지:
        //     setResult(int resultCode)                 → 코드만 돌려줌 (성공/취소 구분만)
        //     setResult(int resultCode, Intent data)    → 코드 + 데이터 Intent 돌려줌
        //
        //   resultCode 종류:
        //     RESULT_OK       (= -1)  "정상 완료"
        //     RESULT_CANCELED (= 0)   "취소됨" (setResult 안 호출하면 이 값이 기본)
        //     RESULT_FIRST_USER 이상  "커스텀 결과 코드" (원하면 정의 가능)
        //
        // ──── "예약"이라는 의미 ────
        // setResult 호출만으로는 결과가 전달되지 않음
        //   → Activity 내부에 "종료될 때 돌려줄 결과"로 저장만 해둠
        //   → 아래 finish()가 호출되어 Activity가 실제로 닫힐 때
        //     Android가 저장된 결과를 꺼내서 호출자에게 전달
        //   → 그래서 "setResult + finish()" 쌍은 항상 같이 나옴
        //
        // ──── 취소 경로와 비교 ────
        // 사용자가 ← 버튼이나 뒤로가기로 나갈 때는 setResult를 안 부르고 finish()만 호출됨
        //   → 호출자는 resultCode = RESULT_CANCELED, data = null 을 받음
        //   → reviewLauncher 람다 쪽에서 isOk 체크에 걸려서 아무 동작 안 함
        //
        // Unity 비유:
        //   WinForm의 Modal 다이얼로그에서 DialogResult 설정 후 Close 호출하는 것과 동일
        setResult(RESULT_OK, resultIntent);

        // ──── finish() 동작 상세 ────
        // 이 Activity를 "종료해달라"고 Android에 요청하는 메서드 (Lifecycle 콜백이 아님)
        //
        // 호출 직후 벌어지는 일:
        //   1. Android가 이 Activity의 onPause → onStop → onDestroy 를 순서대로 호출
        //   2. 백스택에서 ReviewWriteActivity 제거
        //   3. 이전 화면(GameDetail)이 다시 화면에 나타나고 onRestart → onStart → onResume
        //   4. 위에서 setResult로 예약해둔 결과가 GameDetail로 전달됨
        //   5. GameDetail의 reviewLauncher 람다(onCreate에서 등록)가 자동 실행됨
        //      → result.getData()로 결과 Intent 꺼내 별점/한줄평 반영
        //
        // 주의: finish() 호출 뒤에도 이 메서드의 다음 줄이 있다면 실행됨
        //   finish()는 "즉시 죽임"이 아니라 "종료 예약"이기 때문
        //   (여기선 이 줄이 마지막이라 상관없음)
        finish();
    }
}
