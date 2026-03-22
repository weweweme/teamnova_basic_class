package com.example.weak7_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 홈 화면 (앱 실행 시 가장 먼저 보이는 화면)
 * 레이아웃: ScrollView > LinearLayout (vertical)
 * 역할: 13개 데모 화면으로 이동하는 허브(메뉴) 역할
 *
 * [섹션 1] 레이아웃 학습 (1~5번): 각 레이아웃 종류 체험
 * [섹션 2] Manifest 학습 (6~13번): AndroidManifest.xml의 다양한 요소 체험
 *
 * 학습 포인트: Intent를 통한 Activity 전환, 4대 구성요소 간 소통 방식
 */
public class HomeActivity extends AppCompatActivity {

    /*
     * [onCreate - Activity 생명주기의 시작점]
     *
     * Activity가 생성될 때 시스템이 가장 먼저 호출하는 메서드.
     * Unity 비유: MonoBehaviour의 Start()와 유사 (초기화 담당).
     *
     * [super.onCreate(savedInstanceState)]
     * 부모 클래스(AppCompatActivity)의 onCreate를 먼저 호출해야 한다.
     * savedInstanceState: 화면 회전 등으로 Activity가 재생성될 때 이전 상태를 복원하기 위한 데이터.
     * null이면 최초 생성, 값이 있으면 복원 중이라는 뜻.
     *
     * [setContentView(R.layout.activity_home)]
     * 이 Activity가 사용할 레이아웃 XML을 지정한다.
     * "activity_home.xml을 화면에 그려라"라는 뜻.
     * Unity 비유: 씬을 로드하면 해당 씬의 오브젝트들이 화면에 배치되는 것과 유사.
     *
     * [EdgeToEdge.enable(this)]
     * 상태바(시계, 배터리)와 네비게이션바(하단 뒤로가기) 영역까지 콘텐츠를 확장한다.
     * 이를 사용하면 앱이 전체 화면을 활용할 수 있지만,
     * 상태바 영역과 콘텐츠가 겹칠 수 있으므로 WindowInsets 처리가 필요하다.
     * - 없으면: 상태바/네비게이션바 영역은 시스템이 차지하고, 앱 콘텐츠는 그 아래에 배치됨.
     * - 있으면: 앱 콘텐츠가 화면 전체를 사용하여, 상태바 뒤까지 그려짐. 더 몰입감 있는 UI.
     *
     * [ViewCompat.setOnApplyWindowInsetsListener]
     * EdgeToEdge로 확장한 후, 시스템 UI(상태바, 네비게이션바) 영역만큼
     * 자동으로 패딩을 추가하여 콘텐츠가 겹치지 않게 해준다.
     * Unity 비유: Safe Area를 감지하여 UI 영역을 조정하는 것과 동일한 개념.
     * - 없으면: EdgeToEdge 사용 시 콘텐츠가 상태바와 겹쳐서 시계 뒤에 텍스트가 보이는 등 문제 발생.
     * - 있으면: 시스템 UI 영역만큼 자동 패딩 → 콘텐츠가 안전한 영역에만 배치됨.
     * 즉, EdgeToEdge와 WindowInsets는 항상 세트로 사용해야 한다.
     *
     * [R.id와 @+id - 리소스 ID 시스템]
     *
     * XML에서 뷰에 ID를 부여할 때: android:id="@+id/main"
     * - @ → 리소스를 참조한다는 뜻
     * - + → 이 ID가 없으면 새로 생성하라는 뜻
     * - id/main → ID 카테고리에서 "main"이라는 이름
     *
     * 즉, @+id/main = "main이라는 ID를 새로 만들어서 이 뷰에 부여해라"
     *
     * ID는 런타임이 아니라 빌드 타임에 생성된다.
     * 앱을 빌드할 때 R.java 파일이 자동 생성되며, 모든 @+id가 정수(int) 상수로 변환된다.
     * 런타임에서는 이미 만들어진 정수 ID로 뷰를 빠르게 찾는 것.
     *
     * 참조만 할 때 (이미 선언된 ID를 가리킬 때):
     *   app:layout_constraintTop_toBottomOf="@id/imageView"  (+ 없음)
     *   → "이미 있는 imageView라는 ID를 참조해라"
     *
     * + 를 안 붙이면:
     *   - 선언 시: @id/xxx → 해당 ID가 아직 없으면 빌드 에러 발생
     *   - 참조 시: @id/xxx → 정상 (이미 다른 곳에서 @+id로 선언했으니까)
     *
     * Unity 비유:
     *   - @+id = 오브젝트를 만들고 이름을 붙이는 것 (선언)
     *   - @id  = 이미 있는 오브젝트를 이름으로 찾는 것 (참조)
     *
     * 공식 문서:
     * - Activity 생명주기: https://developer.android.com/guide/components/activities/activity-lifecycle
     * - Edge-to-Edge: https://developer.android.com/develop/ui/views/layout/edge-to-edge
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ═══════════ 섹션 1: 레이아웃 학습 (기존) ═══════════

        /*
         * [Android 4대 구성요소와 Intent]
         *
         * Android에는 4대 구성요소가 있다: Activity, Service, BroadcastReceiver, ContentProvider
         * 이들은 서로 직접 참조하지 않고, Intent(요청서)를 통해 시스템에 위임하여 소통한다.
         *
         * 왜 직접 참조가 아니라 Intent를 쓰는가?
         *
         * 1. 생명주기 관리
         *    - Android 시스템이 메모리 부족 시 구성요소를 언제든 파괴/재생성할 수 있다.
         *    - 직접 참조하면 파괴된 객체를 참조하게 되어 위험하다.
         *
         * 2. 앱 간 통신
         *    - Intent는 같은 앱 안에서뿐 아니라 다른 앱의 구성요소도 호출할 수 있다.
         *    - 예: 내 앱에서 카메라 앱 열기, 카카오톡으로 공유하기
         *    - 다른 앱의 클래스를 직접 참조하는 건 불가능하므로, Intent가 필수적이다.
         *
         * 3. 보안
         *    - 시스템이 Intent를 중간에서 가로채어 권한을 체크할 수 있다.
         *    - 허가되지 않은 컴포넌트 접근을 시스템 레벨에서 차단한다.
         *
         * 흐름: 현재 Activity → Intent(요청서) → Android 시스템 → 대상 Activity 생성
         *
         * 공식 문서:
         * - Intent 개요: https://developer.android.com/guide/components/intents-filters
         * - 앱 구성요소: https://developer.android.com/guide/components/fundamentals
         */

        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingActivity.class)));

        findViewById(R.id.btnCalculator).setOnClickListener(v ->
                startActivity(new Intent(this, CalculatorActivity.class)));

        findViewById(R.id.btnCard).setOnClickListener(v ->
                startActivity(new Intent(this, CardActivity.class)));

        // ═══════════ 섹션 2: Manifest 학습 (신규) ═══════════

        /*
         * [Manifest 학습 섹션]
         *
         * 아래 8개 Activity는 각각 AndroidManifest.xml의 서로 다른 요소를 체험하기 위한 데모.
         * 실제 비즈니스 로직이 아니라, Manifest 선언이 앱 동작에 어떤 영향을 주는지 보여준다.
         *
         * Unity 비유: 각 씬이 서로 다른 Project Settings 옵션을 테스트하는 데모 씬인 것.
         */

        // 6. 카메라 - <uses-permission>, <uses-feature> 체험
        findViewById(R.id.btnCamera).setOnClickListener(v ->
                startActivity(new Intent(this, CameraActivity.class)));

        // 7. 화면 속성 - screenOrientation, configChanges, windowSoftInputMode 체험
        findViewById(R.id.btnPortrait).setOnClickListener(v ->
                startActivity(new Intent(this, PortraitActivity.class)));

        // 10. 전체화면 - Activity 단위 테마 오버라이드 체험
        findViewById(R.id.btnFullScreen).setOnClickListener(v ->
                startActivity(new Intent(this, FullScreenActivity.class)));

        // 11. 실행 모드 - launchMode (singleTop vs standard) 체험
        findViewById(R.id.btnLaunchMode).setOnClickListener(v ->
                startActivity(new Intent(this, LaunchModeActivity.class)));

        // 12. 서비스/리시버 - <service>, <receiver> 체험
        findViewById(R.id.btnServiceReceiver).setOnClickListener(v ->
                startActivity(new Intent(this, ServiceReceiverActivity.class)));

    }
}
