package com.example.weak7_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnLinear = findViewById(R.id.btnLinearLayout);
        Button btnConstraint = findViewById(R.id.btnConstraintLayout);
        Button btnScroll = findViewById(R.id.btnScrollView);
        Button btnGrid = findViewById(R.id.btnGridLayout);
        Button btnFrame = findViewById(R.id.btnFrameLayout);

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

        // btnLinear 버튼의 클릭 이벤트 등록
        btnLinear.setOnClickListener(v -> {
            // Intent는 안드로이드 시스템에게 보내는 요청서 (어디서 → 어디로)
            // LoginActivity.class를 넘겨서 "이 액티비티를 만들어달라"고 요청
            Intent intent = new Intent(this, LoginActivity.class);
            // 요청서를 시스템에 제출 → 시스템이 LoginActivity를 생성하고 화면에 띄움
            startActivity(intent);
        });

        // 아직 만들지 않은 액티비티는 Toast로 안내
        btnConstraint.setOnClickListener(v ->
                Toast.makeText(this, "ConstraintLayout 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
        btnScroll.setOnClickListener(v ->
                Toast.makeText(this, "ScrollView 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
        btnGrid.setOnClickListener(v ->
                Toast.makeText(this, "GridLayout 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
        btnFrame.setOnClickListener(v ->
                Toast.makeText(this, "FrameLayout 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
    }
}
