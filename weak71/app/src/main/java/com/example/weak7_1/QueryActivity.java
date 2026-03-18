package com.example.weak7_1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

/**
 * 패키지 가시성 데모 화면
 * Manifest 학습 포인트: <queries> 요소, Android 11 패키지 가시성 변경
 *
 * ============================================================
 * [queries = 다른 앱을 감지하기 위한 사전 선언]
 * ============================================================
 *
 * Android 11(API 30)부터 앱의 "패키지 가시성"이 제한되었다.
 * 다른 앱이 설치되어 있는지 확인하려면, Manifest에 미리 선언해야 한다.
 *
 * 왜 이런 제한이 생겼나?
 * → 개인정보 보호. 설치된 앱 목록만으로도 사용자의 종교, 건강, 정치 성향 등을 유추할 수 있다.
 *   예: 특정 종교 앱, 특정 질병 관리 앱이 설치되어 있다면...
 *   그래서 "꼭 필요한 앱만 확인할 수 있게" 제한한 것이다.
 *
 * Unity 비유:
 *   FindObjectsOfType<T>()로 씬의 모든 오브젝트를 찾을 수 있는 것이 기존 방식이라면,
 *   Android 11+는 "사전에 등록한 타입만 Find할 수 있도록" 제한한 것이다.
 *   마치 Physics.Raycast에서 LayerMask를 지정하지 않으면
 *   아무것도 감지되지 않는 것과 비슷하다.
 *
 * ============================================================
 * [queries 선언 방법 (AndroidManifest.xml)]
 * ============================================================
 *
 * 1. 특정 패키지명으로 선언:
 *    <queries>
 *        <package android:name="com.android.chrome" />
 *    </queries>
 *    → Chrome이 설치되어 있는지 직접 확인 가능
 *
 * 2. Intent 패턴으로 선언:
 *    <queries>
 *        <intent>
 *            <action android:name="android.intent.action.VIEW" />
 *            <data android:scheme="https" />
 *        </intent>
 *    </queries>
 *    → https 링크를 열 수 있는 앱들을 queryIntentActivities()로 조회 가능
 *
 * 3. QUERY_ALL_PACKAGES 권한 (비권장):
 *    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
 *    → 모든 앱을 조회 가능하지만, Play Store 심사에서 거부될 확률이 높다.
 *      런처 앱, 보안 앱 등 특수한 경우에만 허용된다.
 */
public class QueryActivity extends AppCompatActivity {

    /* UI 요소 참조 변수 */
    private TextView tvChromeStatus;
    private TextView tvMapsStatus;
    private TextView tvWebApps;
    private TextView tvShareApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* EdgeToEdge + WindowInsets: Safe Area 설정 (매 Activity 동일 패턴) */
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_query);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===== UI 요소 바인딩 =====
        tvChromeStatus = findViewById(R.id.tvChromeStatus);
        tvMapsStatus = findViewById(R.id.tvMapsStatus);
        tvWebApps = findViewById(R.id.tvWebApps);
        tvShareApps = findViewById(R.id.tvShareApps);
        Button btnRefresh = findViewById(R.id.btnRefresh);

        // ===== 최초 실행 시 모든 정보 조회 =====
        refreshAllChecks();

        // ===== 새로고침 버튼 =====
        /*
         * 사용자가 앱을 설치/삭제한 후 다시 확인할 수 있도록 새로고침 기능을 제공한다.
         * 앱 설치 상태는 실시간으로 바뀔 수 있으므로 재조회가 필요하다.
         */
        btnRefresh.setOnClickListener(v -> refreshAllChecks());
    }

    /**
     * 모든 체크를 한 번에 실행하는 메서드.
     * onCreate()와 새로고침 버튼에서 공통으로 호출한다.
     *
     * Unity 비유: Refresh() 메서드를 만들어서
     *            Start()와 OnButtonClick() 양쪽에서 호출하는 패턴.
     */
    private void refreshAllChecks() {
        // 섹션 1: 특정 앱 설치 확인
        checkPackageInstalled("com.android.chrome", tvChromeStatus, "Chrome");
        checkPackageInstalled("com.google.android.apps.maps", tvMapsStatus, "Google Maps");

        // 섹션 2: Intent를 처리할 수 있는 앱 조회
        queryWebApps();
        queryShareApps();
    }

    /**
     * 특정 패키지가 설치되어 있는지 확인한다.
     *
     * getPackageInfo(packageName, 0):
     * → 해당 패키지명의 앱 정보를 조회한다.
     * → 설치되어 있으면 PackageInfo 객체가 반환된다.
     * → 설치되어 있지 않으면 NameNotFoundException이 발생한다.
     *
     * 중요: Android 11+에서는 Manifest의 <queries>에 해당 패키지를 선언해야
     *       설치되어 있어도 찾을 수 있다! 선언하지 않으면 항상 NameNotFoundException.
     *
     * Unity 비유:
     *   GameObject.Find("ObjectName")이 null을 반환하면 없는 것이고,
     *   객체를 반환하면 있는 것인 것처럼,
     *   getPackageInfo()도 성공/실패로 존재 여부를 판단한다.
     *   다만 try-catch 방식이라는 점이 다르다 (Java의 예외 처리 패턴).
     *
     * @param packageName 확인할 앱의 패키지명 (예: "com.android.chrome")
     * @param targetView  결과를 표시할 TextView
     * @param displayName UI에 표시할 앱 이름 (예: "Chrome")
     */
    private void checkPackageInstalled(String packageName, TextView targetView, String displayName) {
        try {
            /*
             * getPackageManager(): 이 앱이 실행 중인 기기의 패키지 매니저를 가져온다.
             * PackageManager는 기기에 설치된 모든 앱 정보를 관리하는 시스템 서비스.
             *
             * getPackageInfo(packageName, 0):
             * → 두 번째 파라미터 0 = 추가 플래그 없음 (기본 정보만 요청).
             *   GET_ACTIVITIES, GET_PERMISSIONS 등의 플래그로 상세 정보를 요청할 수 있다.
             */
            getPackageManager().getPackageInfo(packageName, 0);
            targetView.setText(displayName + ": ✅ 설치됨");
        } catch (PackageManager.NameNotFoundException e) {
            /*
             * NameNotFoundException:
             * → 패키지를 찾을 수 없음. 두 가지 원인:
             *   1. 실제로 설치되어 있지 않음
             *   2. Android 11+에서 <queries>에 선언하지 않아 보이지 않음
             *
             * 어떤 원인인지 구분할 방법이 없으므로,
             * <queries> 선언이 올바른지 Manifest를 먼저 확인해야 한다.
             */
            targetView.setText(displayName + ": ❌ 미설치 (또는 queries 미선언)");
        }
    }

    /**
     * https:// 링크를 열 수 있는 앱 목록을 조회한다.
     *
     * queryIntentActivities():
     * → 주어진 Intent를 처리할 수 있는 Activity 목록을 반환한다.
     * → Android 11+에서는 <queries>에 해당 Intent 패턴을 선언해야 결과가 나온다.
     *
     * Intent(ACTION_VIEW, Uri.parse("https://"))
     * → "이 URL을 열어줄 수 있는 앱이 있나요?"라는 질의.
     *   Chrome, Firefox, Samsung Internet 등 브라우저 앱이 해당된다.
     *
     * Unity 비유:
     *   Physics.OverlapSphere()로 주변의 콜라이더를 검색하는 것과 유사하다.
     *   "이 범위 안에 어떤 오브젝트가 있는가?" → "이 Intent를 처리할 수 있는 앱이 있는가?"
     */
    private void queryWebApps() {
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://"));

        /*
         * PackageManager.MATCH_DEFAULT_ONLY:
         * → CATEGORY_DEFAULT가 있는 Activity만 검색한다.
         *   즉, 암시적 Intent에 의해 기본으로 실행될 수 있는 Activity만 포함.
         *   사용자가 직접 선택하지 않아도 자동으로 열리는 앱들이다.
         */
        List<ResolveInfo> activities = getPackageManager()
                .queryIntentActivities(webIntent, PackageManager.MATCH_DEFAULT_ONLY);

        tvWebApps.setText("https:// 링크를 열 수 있는 앱: " + activities.size() + "개");
    }

    /**
     * 텍스트 공유를 받을 수 있는 앱 목록을 조회한다.
     *
     * Intent(ACTION_SEND) + type("text/plain")
     * → "텍스트를 공유받을 수 있는 앱이 있나요?"라는 질의.
     *   카카오톡, 메모 앱, 이메일 앱 등이 해당된다.
     *
     * Unity 비유:
     *   NativeShare 플러그인이 공유 대상 앱 목록을 보여주는 것과 같다.
     *   Android에서는 시스템이 자동으로 앱 선택 화면(Chooser)을 제공한다.
     */
    private void queryShareApps() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        List<ResolveInfo> activities = getPackageManager()
                .queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);

        tvShareApps.setText("텍스트 공유를 받을 수 있는 앱: " + activities.size() + "개");
    }
}
