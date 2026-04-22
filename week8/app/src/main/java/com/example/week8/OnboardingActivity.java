package com.example.week8;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.databinding.ActivityOnboardingBinding;

/// <summary>
/// 온보딩 화면 (앱 최초 실행 시 소개)
/// ViewFlipper로 3페이지를 순서대로 보여주고, 완료 시 MainActivity로 이동
/// Unity로 비유하면 튜토리얼 Panel 3장을 버튼으로 넘기는 것
///
/// ──── Lifecycle 학습 ────
/// onSaveInstanceState: 현재 페이지 인덱스를 Bundle에 저장 (회전 대응)
///   → 2페이지에서 화면 회전해도 2페이지 그대로 유지
///   → Unity에서 Scene 재로드 시 static 변수에 상태를 백업하는 것과 비슷하지만,
///     Android는 Bundle이라는 전용 임시 저장소를 제공
/// onRestoreInstanceState: Bundle에서 페이지 인덱스 복원
///
/// ──── Intent 학습 ────
/// 명시적 Intent: 완료 시 MainActivity로 이동
/// FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK: 백스택 정리
/// </summary>
public class OnboardingActivity extends AppCompatActivity {

    /// <summary>
    /// 온보딩 전체 페이지 개수 (3페이지 고정)
    /// "마지막 페이지인지" 판별, "N / 3" 표시에 사용
    /// </summary>
    private static final int TOTAL_PAGES = 3;

    /// <summary>
    /// onSaveInstanceState에서 Bundle에 "현재 페이지 번호"를 저장할 때 사용하는 키
    ///
    /// Bundle은 키-값 저장소 (HashMap처럼 동작)라서,
    /// 값을 넣을 때와 꺼낼 때 같은 문자열을 써야 함
    ///   outState.putInt("current_page", 2);                   // 저장
    ///   savedInstanceState.getInt("current_page", 0);          // 복원 (같은 키!)
    ///
    /// 키 오타로 인한 버그를 막기 위해 상수로 뽑아둠
    /// </summary>
    private static final String KEY_CURRENT_PAGE = "current_page";

    /// <summary>
    /// ViewBinding 객체
    /// activity_onboarding.xml 레이아웃 안의 id가 붙은 View들을 모아놓은 묶음
    ///
    /// ──── ViewBinding 이란? ────
    /// 공식 문서: https://developer.android.com/topic/libraries/view-binding
    ///
    /// XML 레이아웃 파일 이름이 activity_onboarding.xml 이면,
    /// Android 빌드 시스템이 자동으로 ActivityOnboardingBinding 클래스를 생성해줌
    ///   (파일명 첫 글자 대문자화 + 언더스코어 제거 + "Binding" 붙임)
    ///
    /// 이 binding 객체 하나에 XML의 모든 View가 필드로 들어있음
    ///   XML에 android:id="@+id/buttonNext" 있으면 → binding.buttonNext
    ///   XML에 android:id="@+id/viewFlipper" 있으면 → binding.viewFlipper
    ///   XML에 android:id="@+id/textViewPageIndicator" 있으면 → binding.textViewPageIndicator
    ///
    /// 구식 방법(findViewById):
    ///   Button btn = findViewById(R.id.buttonNext);
    ///   → 매번 View 하나씩 찾아와야 하고, 타입 캐스팅 오류/null 가능성 있음
    /// ViewBinding:
    ///   binding.buttonNext
    ///   → 타입 안전 + null 안전 + 자동완성 지원
    ///
    /// Unity 비유:
    /// Inspector에서 SerializeField로 수동 연결해놓은 UI 참조들을
    /// "자동으로 한 번에 모아준 객체"라고 생각하면 됨
    /// </summary>
    private ActivityOnboardingBinding binding;

    // ========== Lifecycle ==========

    /// <summary>
    /// 온보딩 화면 생성
    /// ViewBinding 연결 + 버튼 리스너 등록 + 페이지 표시 갱신
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ──────── ViewBinding 연결 (XML 레이아웃 → 실제 화면) ────────
        //
        // ① inflate(getLayoutInflater())
        //    "부풀리다"는 뜻. XML(설계도)을 읽어 실제 View 객체들로 만들어냄
        //    activity_onboarding.xml 안의 모든 View(ViewFlipper, Button, TextView...)가
        //    메모리에 Java 객체로 생성되고, 그걸 ActivityOnboardingBinding 객체에 담아 반환
        //    → 이때까지는 메모리에만 있고 화면엔 아직 안 보임
        //    Unity 비유: Instantiate(prefab) 로 씬에 GameObject를 생성
        //
        // ② setContentView(binding.getRoot())
        //    inflate로 만든 View 덩어리의 최상위(root)를 Activity의 메인 화면으로 등록
        //    이 시점에 비로소 사용자 눈에 보임
        //    Unity 비유: 생성한 GameObject를 Canvas/Scene에 SetParent로 붙이는 것
        //
        // 두 줄이 합쳐져야 "XML → 메모리 객체 → 실제 화면" 과정이 완성됨
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 페이지 표시 갱신 ("1 / 3")
        updatePageIndicator();

        // 다음/시작 버튼 클릭 리스너
        // Unity로 비유하면 Button.onClick.AddListener(() => OnNextClicked())
        binding.buttonNext.setOnClickListener(v -> onNextClicked());
    }

    /// <summary>
    /// 회전 등으로 Activity가 파괴되기 전에 현재 페이지 인덱스를 Bundle에 저장
    /// Unity로 비유하면 Scene 언로드 직전에 현재 상태를 임시 딕셔너리에 백업하는 것
    ///
    /// ★ 핵심 학습: 이 메서드는 "회전/메모리 부족으로 인한 임시 파괴"에만 호출됨
    ///   사용자가 뒤로가기로 나가면 호출되지 않음 (영구 종료이므로)
    /// </summary>
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // 현재 ViewFlipper가 보여주고 있는 페이지 인덱스 저장
        outState.putInt(KEY_CURRENT_PAGE, binding.viewFlipper.getDisplayedChild());
    }

    /// <summary>
    /// Activity가 재생성된 후 Bundle에서 페이지 인덱스를 복원
    /// Unity로 비유하면 Scene 로드 후 백업해둔 상태를 꺼내서 적용하는 것
    ///
    /// ★ 테스트 방법: 2페이지에서 에뮬레이터 회전 버튼 클릭 → 2페이지 그대로인지 확인
    /// </summary>
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // 저장된 페이지 인덱스로 ViewFlipper 이동
        int savedPage = savedInstanceState.getInt(KEY_CURRENT_PAGE, 0);
        binding.viewFlipper.setDisplayedChild(savedPage);

        // 페이지 표시 및 버튼 텍스트 갱신
        updatePageIndicator();
    }

    // ========== 버튼 동작 ==========

    /// <summary>
    /// 다음/시작 버튼 클릭 처리
    /// 마지막 페이지가 아니면 다음 페이지로, 마지막이면 온보딩 완료 처리
    /// </summary>
    private void onNextClicked() {
        int currentPage = binding.viewFlipper.getDisplayedChild();
        boolean isLastPage = currentPage >= TOTAL_PAGES - 1;

        if (isLastPage) {
            completeOnboarding();
        } else {
            // 다음 페이지로 이동
            binding.viewFlipper.showNext();
            updatePageIndicator();
        }
    }

    /// <summary>
    /// 페이지 표시 텍스트와 버튼 텍스트 갱신
    /// 현재 페이지에 따라 "1 / 3" 표시 + 마지막 페이지면 버튼을 "시작하기"로 변경
    /// </summary>
    private void updatePageIndicator() {
        int currentPage = binding.viewFlipper.getDisplayedChild();

        // 페이지 표시 갱신 ("1 / 3", "2 / 3", "3 / 3")
        // 사용자에게는 1부터 시작하는 번호로 표시
        String indicator = (currentPage + 1) + " / " + TOTAL_PAGES;
        binding.textViewPageIndicator.setText(indicator);

        // 마지막 페이지면 버튼 텍스트를 "시작하기"로 변경
        boolean isLastPage = currentPage >= TOTAL_PAGES - 1;
        if (isLastPage) {
            binding.buttonNext.setText(R.string.onboarding_start);
        } else {
            binding.buttonNext.setText(R.string.onboarding_next);
        }
    }

    // ========== 온보딩 완료 ==========

    /// <summary>
    /// 온보딩 완료 처리
    /// MainActivity로 이동
    /// TODO: 10주차에서 SharedPreferences 학습 후 완료 플래그 저장 추가
    /// </summary>
    private void completeOnboarding() {
        // MainActivity로 이동
        Intent intent = new Intent(this, MainActivity.class);

        // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
        // → Onboarding을 백스택에서 제거 (뒤로가기로 돌아올 수 없게)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }
}
