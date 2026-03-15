package com.example.weak7_1;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingActivity extends AppCompatActivity {

    // 현재 표시 중인 Toast를 저장. 새 Toast를 띄우기 전에 이전 것을 cancel()로 즉시 취소한다.
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
         * [R.id - 리소스 ID 시스템]
         *
         * R.id.settingEditProfile 등은 XML에서 android:id="@+id/settingEditProfile"로 지정한 뷰의 ID.
         * Android 빌드 시 R.java 파일이 자동 생성되며, 모든 리소스에 정수(int) ID가 부여된다.
         * Unity 비유: Inspector에서 오브젝트에 Tag를 달고, GameObject.FindWithTag()로 찾는 것과 유사.
         *
         * [findViewById - 뷰 찾기]
         *
         * findViewById(R.id.xxx)는 현재 레이아웃(setContentView로 설정한 XML)에서
         * 해당 ID를 가진 뷰 객체를 찾아 반환한다.
         * Unity 비유: transform.Find("ChildName").GetComponent<T>()와 유사.
         *
         * [setOnClickListener - 클릭 이벤트 등록]
         *
         * 뷰가 클릭되었을 때 실행할 콜백(람다)을 등록한다.
         * v → 클릭된 뷰 자체가 파라미터로 들어온다.
         * Unity 비유: button.onClick.AddListener(() => { ... }) 와 동일한 개념.
         *
         * [Toast - 짧은 메시지 팝업]
         *
         * 화면 하단에 잠깐 나타났다 사라지는 알림 메시지.
         * Toast.makeText(context, 메시지, 길이)로 생성하고 .show()로 표시한다.
         * LENGTH_SHORT(약 2초), LENGTH_LONG(약 3.5초) 두 가지 길이가 있다.
         *
         * [(TextView) v - 캐스팅]
         *
         * setOnClickListener의 파라미터 v는 View 타입으로 들어온다.
         * getText()는 TextView에만 있는 메서드이므로, (TextView)로 캐스팅해야 사용 가능.
         * Unity 비유: GetComponent<Collider>()로 받은 걸 BoxCollider로 캐스팅하는 것과 유사.
         */

        // 클릭 리스너를 등록할 설정 항목들의 ID 배열
        int[] settingIds = {
                R.id.settingEditProfile,
                R.id.settingChangePassword,
                R.id.settingChangeEmail,
                R.id.settingPushNotification,
                R.id.settingEmailNotification,
                R.id.settingMarketing,
                R.id.settingLanguage,
                R.id.settingDarkMode,
                R.id.settingFontSize,
                R.id.settingClearCache,
                R.id.settingTerms,
                R.id.settingPrivacy,
                R.id.settingAppVersion
        };

        // 반복문으로 모든 설정 항목에 동일한 클릭 리스너를 등록
        for (int id : settingIds) {
            TextView item = findViewById(id);
            item.setOnClickListener(v -> {
                // 이전 Toast가 아직 표시 중이면 즉시 취소 (빠른 피드백을 위해)
                if (currentToast != null) currentToast.cancel();

                // 클릭된 뷰(v)를 TextView로 캐스팅하여 텍스트를 가져옴
                String text = ((TextView) v).getText().toString();
                currentToast = Toast.makeText(this, text + " 선택됨", Toast.LENGTH_SHORT);
                currentToast.show();
            });
        }

        // 로그아웃 버튼
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            if (currentToast != null) currentToast.cancel();

            currentToast = Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT);
            currentToast.show();
        });
    }
}
