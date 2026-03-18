package com.example.weak7_1;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 딥링크(Deep Link) 데모 화면
 *
 * [딥링크란?]
 * 특정 URL(커스텀 스킴)을 통해 앱의 원하는 화면을 직접 여는 기능.
 * 예: weak71://open/demo?id=123 → 이 앱의 DeepLinkActivity가 열림.
 *
 * Unity 비유: Unity 모바일 빌드에서 커스텀 URL 스킴을 등록하면
 * myGame://invite?code=abc 같은 링크로 앱을 열 수 있다.
 * (Unity에서는 Player Settings > Other Settings > Supported URL schemes에서 설정하고,
 *  Application.absoluteURL 또는 Application.deepLinkActivated 이벤트로 수신)
 *
 * [URL 구조]
 * scheme://host/path?query
 * weak71://open/demo?id=123&name=test
 *   ↓       ↓    ↓      ↓
 * scheme  host  path   query(key=value 쌍)
 *
 * [Manifest에서의 선언 (intent-filter)]
 * AndroidManifest.xml에 다음과 같이 선언해야 딥링크가 동작한다:
 *
 *   <intent-filter>
 *       <action android:name="android.intent.action.VIEW" />
 *       <category android:name="android.intent.category.DEFAULT" />
 *       <category android:name="android.intent.category.BROWSABLE" />
 *       <data android:scheme="weak71"
 *             android:host="open"
 *             android:pathPrefix="/demo" />
 *   </intent-filter>
 *
 * - ACTION_VIEW: "이 데이터를 보여달라"는 요청 (브라우저에서 링크 클릭 시 전달됨)
 * - DEFAULT: 암시적 Intent를 받으려면 필수
 * - BROWSABLE: 브라우저에서 이 Activity를 실행할 수 있게 허용.
 *              이 카테고리가 없으면 브라우저의 링크 클릭으로는 앱이 열리지 않는다.
 *              Unity 비유: 웹에서 "Open in App" 버튼을 눌렀을 때 앱이 열리려면 이게 필요.
 *
 * [App Links (autoVerify)]
 * android:autoVerify="true"를 intent-filter에 추가하면 "App Links"가 된다.
 * App Links는 서버 측에 /.well-known/assetlinks.json 파일을 배치하여
 * "이 도메인은 이 앱이 소유한 것"을 인증하는 방식이다.
 * 인증이 완료되면 사용자에게 "어떤 앱으로 열까?" 선택지를 보여주지 않고
 * 바로 해당 앱이 열린다.
 *
 * Unity 비유: Unity에서 Universal Links(iOS) / App Links(Android)를 설정할 때
 * apple-app-site-association 또는 assetlinks.json을 서버에 올려야 하는 것과 동일.
 * 커스텀 스킴(weak71://)은 서버 인증이 필요 없지만, https:// 도메인 기반은 필요.
 *
 * 이 데모에서는 커스텀 스킴(weak71://)을 사용하므로 autoVerify는 적용하지 않았다.
 *
 * 공식 문서:
 * - 딥링크 개요: https://developer.android.com/training/app-links/deep-linking
 * - App Links: https://developer.android.com/training/app-links/verify-android-applinks
 */
public class DeepLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deep_link);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
         * [URI 정보를 표시할 TextView 참조 가져오기]
         *
         * findViewById()로 XML에서 선언한 뷰를 Java 코드에서 찾는다.
         * Unity 비유: GameObject.Find("tvFullUri") 또는
         *            transform.Find("tvFullUri").GetComponent<Text>()와 유사.
         */
        TextView tvFullUri = findViewById(R.id.tvFullUri);
        TextView tvScheme = findViewById(R.id.tvScheme);
        TextView tvHost = findViewById(R.id.tvHost);
        TextView tvPath = findViewById(R.id.tvPath);
        TextView tvQuery = findViewById(R.id.tvQuery);

        /*
         * [Intent에서 URI 데이터 가져오기]
         *
         * getIntent(): 이 Activity를 시작시킨 Intent 객체를 가져온다.
         * getData(): Intent에 담긴 URI 데이터를 가져온다.
         *
         * 딥링크로 열린 경우: URI 데이터가 있음 (not null)
         * 홈 화면 버튼으로 열린 경우: URI 데이터가 없음 (null)
         *
         * Unity 비유: 딥링크로 앱이 열리면 Application.absoluteURL에 URL이 담기고,
         *            일반 실행이면 빈 문자열인 것과 유사.
         *
         * [Uri 클래스의 파싱 메서드들]
         * - getScheme(): "weak71" (프로토콜 부분)
         * - getHost(): "open" (도메인 부분)
         * - getPath(): "/demo" (경로 부분)
         * - getQuery(): "id=123&name=test" (쿼리 문자열 전체)
         * - getQueryParameter("id"): "123" (특정 키의 값만 추출)
         *
         * Unity 비유: C#의 System.Uri 클래스와 거의 동일한 구조.
         *   new Uri("weak71://open/demo?id=123")
         *   uri.Scheme → "weak71"
         *   uri.Host → "open"
         *   uri.AbsolutePath → "/demo"
         *   uri.Query → "?id=123"
         */
        Uri uri = getIntent().getData();

        if (uri != null) {
            // 딥링크로 열림 → URI의 각 구성요소를 파싱하여 표시
            tvFullUri.setText("전체 URI: " + uri.toString());
            tvScheme.setText("scheme: " + uri.getScheme());
            tvHost.setText("host: " + uri.getHost());
            tvPath.setText("path: " + uri.getPath());

            /*
             * [쿼리 파라미터 처리]
             *
             * getQuery()는 쿼리 문자열 전체를 반환한다 (예: "id=123&name=test").
             * 개별 값이 필요하면 getQueryParameter("key")를 사용한다.
             *
             * 쿼리가 없을 수도 있으므로 null 체크가 필요하다.
             * 예: weak71://open/demo (쿼리 없음) → getQuery() == null
             *     weak71://open/demo?id=123 (쿼리 있음) → getQuery() == "id=123"
             */
            String query = uri.getQuery();
            if (query != null) {
                tvQuery.setText("query: " + query);
            } else {
                tvQuery.setText("query: (없음)");
            }
        } else {
            /*
             * [일반 실행 (딥링크 없이 열린 경우)]
             *
             * 홈 화면의 버튼을 눌러서 이 Activity를 열면
             * Intent에 URI 데이터가 없으므로 getData()가 null을 반환한다.
             * 이 경우 사용자에게 딥링크 데이터가 없다는 것을 알려준다.
             */
            tvFullUri.setText("홈에서 직접 열림 (딥링크 데이터 없음)");
            tvScheme.setText("scheme: -");
            tvHost.setText("host: -");
            tvPath.setText("path: -");
            tvQuery.setText("query: -");
        }
    }
}
