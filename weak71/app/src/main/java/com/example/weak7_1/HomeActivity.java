package com.example.weak7_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 홈 화면 (앱 실행 시 가장 먼저 보이는 화면)
 * 레이아웃: LinearLayout (vertical)
 * 역할: 5개 데모 화면으로 이동하는 허브(메뉴) 역할
 * 학습 포인트: Intent를 통한 Activity 전환, 4대 구성요소 간 소통 방식
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnCalculator = findViewById(R.id.btnCalculator);
        Button btnCard = findViewById(R.id.btnCard);

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

        // 로그인 화면 버튼의 클릭 이벤트 등록
        btnLogin.setOnClickListener(v -> {
            // Intent는 안드로이드 시스템에게 보내는 요청서 (어디서 → 어디로)
            // LoginActivity.class를 넘겨서 "이 액티비티를 만들어달라"고 요청
            Intent loginActivity = new Intent(this, LoginActivity.class);
            // 요청서를 시스템에 제출 → 시스템이 LoginActivity를 생성하고 화면에 띄움
            startActivity(loginActivity);
        });

        btnProfile.setOnClickListener(v -> {
            Intent profileActivity = new Intent(this, ProfileActivity.class);
            startActivity(profileActivity);
        });

        btnSettings.setOnClickListener(v -> {
            Intent settingActivity = new Intent(this, SettingActivity.class);
            startActivity(settingActivity);
        });

        btnCalculator.setOnClickListener(v -> {
            Intent calculatorActivity = new Intent(this, CalculatorActivity.class);
            startActivity(calculatorActivity);
        });

        btnCard.setOnClickListener(v -> {
            Intent cardActivity = new Intent(this, CardActivity.class);
            startActivity(cardActivity);
        });
    }
}
