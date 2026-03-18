package com.example.weak7_1;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * 전체 화면 모드 액티비티 - 액티비티별 테마 오버라이드 데모
 *
 * 학습 포인트:
 * 1. 이 액티비티는 EdgeToEdge.enable(this)를 사용하지 않는다.
 *    대신 AndroidManifest.xml에서 android:theme="@style/Theme.Weak71.FullScreen"을 지정하여
 *    테마 수준에서 전체 화면을 구현한다.
 *
 *    Unity 비유:
 *    - EdgeToEdge = 코드에서 Screen.fullScreen = true 로 직접 제어하는 방식
 *    - 테마 기반 전체화면 = Player Settings > Resolution and Presentation > Fullscreen Mode에서
 *      빌드 시점에 설정하는 방식
 *    - 둘 다 같은 결과를 내지만, 접근 방식이 다르다 (런타임 코드 vs 설정 파일)
 *
 * 2. 액티비티별 테마 오버라이드:
 *    AndroidManifest.xml에서 특정 <activity>에 android:theme 속성을 지정하면,
 *    해당 액티비티만 앱 기본 테마와 다른 테마를 사용한다.
 *
 *    Unity 비유:
 *    - 앱 전체 테마(Theme.Weak71) = Project Settings > Quality의 기본 프로파일
 *    - 액티비티 테마(Theme.Weak71.FullScreen) = 특정 Scene에만 적용한 Post-Processing Profile
 *    - Scene에 Volume 컴포넌트를 추가해서 Override하는 것처럼,
 *      activity에 theme 속성을 추가해서 Override한다.
 *
 * 3. WindowInsetsControllerCompat:
 *    상태바(Status Bar)와 네비게이션바(Navigation Bar)를 프로그래밍으로 숨기거나 보여준다.
 *
 *    Unity 비유:
 *    - 상태바/네비게이션바 = Screen Space - Overlay 모드의 Canvas
 *    - 숨기기 = Canvas.enabled = false (게임 화면 위의 시스템 UI를 숨기는 것)
 *    - WindowInsetsControllerCompat = Canvas의 enabled 속성을 제어하는 매니저 클래스
 */
public class FullScreenActivity extends AppCompatActivity {

    /**
     * 시스템 바(상태바 + 네비게이션바)가 현재 보이는 상태인지 추적하는 플래그.
     * Unity 비유: bool isUIVisible 같은 상태 변수.
     * 토글 버튼을 누를 때마다 이 값이 반전된다.
     */
    private boolean isSystemBarsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * [주의] EdgeToEdge.enable(this)를 호출하지 않는다!
         *
         * 이 액티비티는 AndroidManifest.xml에서 지정한 테마(Theme.Weak71.FullScreen)에 의해
         * 전체 화면 스타일이 적용된다. EdgeToEdge는 코드 기반 방식이고,
         * 여기서는 테마 기반 방식을 사용하여 두 방식의 차이를 학습한다.
         *
         * Unity 비유:
         * - EdgeToEdge = Start()에서 Screen.fullScreen = true; 호출 (코드 방식)
         * - 테마 기반 = Player Settings에서 Fullscreen Mode 선택 (설정 방식)
         */
        setContentView(R.layout.activity_full_screen);

        /*
         * WindowInsets 리스너: 시스템 바 영역만큼 안전한 패딩을 적용한다.
         * 테마 기반 전체화면이라도, 시스템 바가 표시될 때 콘텐츠가 겹치지 않도록
         * 패딩 처리는 여전히 필요하다.
         *
         * Unity 비유: SafeArea를 계산해서 UI 패딩을 주는 것과 동일.
         * Screen.safeArea를 읽어서 RectTransform의 offset을 조정하는 로직.
         */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ── 툴바 (뒤로가기 버튼) 설정 ──
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 시스템 바 토글 버튼 설정
        setupToggleButton();

        // 현재 테마 정보를 읽어서 화면에 표시
        displayThemeInfo();
    }

    /**
     * 상태바/네비게이션바 토글 버튼을 설정한다.
     *
     * WindowInsetsControllerCompat을 사용하여 시스템 바를 숨기거나 보여준다.
     * 이것은 Android에서 공식 권장하는 시스템 바 제어 방법이다.
     *
     * Unity 비유:
     * - WindowInsetsControllerCompat = UIManager 같은 싱글톤 매니저
     * - show/hide = Canvas.enabled를 true/false로 토글
     * - BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE = 숨긴 후 화면 가장자리를 스와이프하면
     *   잠깐 보였다가 다시 사라지는 동작. 게임에서 숨긴 UI를 특정 제스처로 잠깐 보는 것과 유사.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupToggleButton() {
        Button btnToggle = findViewById(R.id.btnToggleSystemBars);

        btnToggle.setOnClickListener(v -> {
            /*
             * WindowCompat.getInsetsController()로 WindowInsetsControllerCompat 인스턴스를 얻는다.
             * 이 컨트롤러가 시스템 바의 표시/숨김을 관리한다.
             *
             * Unity 비유: UIManager.Instance를 가져와서 UI를 제어하는 패턴.
             */
            WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

            if (isSystemBarsVisible) {
                /*
                 * 시스템 바 숨기기
                 *
                 * hide(WindowInsetsCompat.Type.systemBars()):
                 *   상태바 + 네비게이션바를 모두 숨긴다.
                 *
                 * setSystemBarsBehavior(BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE):
                 *   숨긴 후 화면 가장자리를 스와이프하면 바가 일시적으로 나타났다가 다시 숨겨진다.
                 *   완전히 가리는 것이 아니라, 필요할 때 잠깐 볼 수 있는 모드.
                 *
                 * Unity 비유:
                 * - Canvas.enabled = false; // UI 전체 숨기기
                 * - 하지만 특정 입력(스와이프)에 반응하여 잠깐 보여주는 것은
                 *   Coroutine으로 잠깐 보여줬다가 다시 숨기는 패턴과 비슷하다.
                 */
                controller.hide(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
                btnToggle.setText("상태바/네비게이션바 보이기");
            } else {
                /*
                 * 시스템 바 보이기
                 * show(WindowInsetsCompat.Type.systemBars()):
                 *   숨겨진 상태바 + 네비게이션바를 다시 표시한다.
                 *
                 * Unity 비유: Canvas.enabled = true; // UI 다시 보이기
                 */
                controller.show(WindowInsetsCompat.Type.systemBars());
                btnToggle.setText("상태바/네비게이션바 숨기기");
            }

            // 상태 플래그 반전 (Unity의 isUIVisible = !isUIVisible 과 동일)
            isSystemBarsVisible = !isSystemBarsVisible;
        });
    }

    /**
     * 현재 테마에서 colorPrimary 등의 속성 값을 읽어서 화면에 표시한다.
     *
     * TypedArray를 사용하여 테마 속성을 런타임에 읽는다.
     * 이것은 현재 적용된 테마가 무엇이든, 그 테마의 실제 값을 동적으로 가져온다.
     *
     * Unity 비유:
     * - TypedArray = Material.GetColor("_Color") 처럼 Material의 속성을 런타임에 읽는 것
     * - obtainStyledAttributes() = material.GetFloat(), material.GetColor() 등
     * - 어떤 Material(테마)이 적용되어 있든, 그 Material의 실제 속성값을 코드로 읽을 수 있다.
     *
     * [TypedArray 사용 패턴]
     * 1. obtainStyledAttributes(읽고 싶은 속성 배열)로 TypedArray를 얻는다
     * 2. getColor(), getString() 등으로 값을 읽는다
     * 3. 반드시 recycle()을 호출하여 메모리를 반환한다
     *    (Unity의 ObjectPool에서 사용 후 Return하는 것과 같다)
     */
    private void displayThemeInfo() {
        TextView tvThemeInfo = findViewById(R.id.tvThemeInfo);

        /*
         * 읽고 싶은 테마 속성들을 배열로 정의한다.
         * android.R.attr.colorPrimary = Android 시스템이 정의한 표준 테마 속성
         * com.google.android.material.R.attr.colorOnPrimary = Material 라이브러리가 정의한 속성
         *
         * Unity 비유: Shader에서 읽고 싶은 Property 이름들을 미리 정의해두는 것.
         * Shader.PropertyToID("_MainColor"), Shader.PropertyToID("_Emission") 등.
         */
        int[] attrs = {
                android.R.attr.colorPrimary,                               // 기본 브랜드 색상
                com.google.android.material.R.attr.colorOnPrimary,         // Primary 위의 텍스트/아이콘 색상
                com.google.android.material.R.attr.colorSecondary,         // 보조 브랜드 색상
                com.google.android.material.R.attr.colorSurface            // 카드, 시트 등의 배경 색상
        };

        /*
         * obtainStyledAttributes()로 현재 테마에서 속성 값들을 읽어온다.
         * 반환된 TypedArray는 attrs 배열과 같은 순서로 값이 들어있다.
         *
         * Unity 비유: Material mat = renderer.material;
         * 이후 mat.GetColor("_Color") 등으로 값을 읽는 것과 같다.
         */
        TypedArray typedArray = getTheme().obtainStyledAttributes(attrs);

        try {
            // 각 속성의 색상 값을 읽는다 (기본값 0 = 투명)
            int colorPrimary = typedArray.getColor(0, 0);
            int colorOnPrimary = typedArray.getColor(1, 0);
            int colorSecondary = typedArray.getColor(2, 0);
            int colorSurface = typedArray.getColor(3, 0);

            /*
             * 색상 값을 16진수(Hex) 문자열로 변환하여 표시한다.
             * Integer.toHexString()은 ARGB 순서로 출력한다.
             * toUpperCase()로 대문자로 변환하여 가독성을 높인다.
             *
             * Unity 비유: ColorUtility.ToHtmlStringRGBA(color) 와 동일한 역할.
             */
            String info = "--- 현재 테마 속성값 ---\n\n"
                    + "colorPrimary:\n  #" + Integer.toHexString(colorPrimary).toUpperCase() + "\n\n"
                    + "colorOnPrimary:\n  #" + Integer.toHexString(colorOnPrimary).toUpperCase() + "\n\n"
                    + "colorSecondary:\n  #" + Integer.toHexString(colorSecondary).toUpperCase() + "\n\n"
                    + "colorSurface:\n  #" + Integer.toHexString(colorSurface).toUpperCase() + "\n\n"
                    + "--- 테마 이름 ---\n"
                    + "Theme.Weak71.FullScreen\n"
                    + "(앱 기본: Theme.Weak71)";

            tvThemeInfo.setText(info);

        } finally {
            /*
             * TypedArray는 반드시 recycle()을 호출해야 한다!
             * 내부적으로 풀(Pool)에서 가져온 객체이므로, 사용 후 반환해야 메모리 누수가 없다.
             *
             * Unity 비유:
             * - ObjectPool<T>에서 pool.Get()으로 가져온 후 pool.Release(obj)로 반환하는 것과 동일.
             * - 또는 ListPool<T>.Get() / ListPool<T>.Release()와 같은 패턴.
             * - recycle()을 빼먹으면 Unity에서 Pool에 Release 안 한 것과 같은 메모리 누수 발생.
             */
            typedArray.recycle();
        }
    }
}
