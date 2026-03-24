package com.example.weak7_1;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * [카카오톡 스타일 로그인 화면]
 *
 * 레이아웃: LinearLayout (vertical)
 * 컨셉: 카카오톡 로그인 화면을 LinearLayout만으로 재현
 *
 * 학습 포인트:
 * 1. Space + layout_weight → 콘텐츠를 화면 중앙~상단에 배치하는 기법
 *    위 Space(weight=1) : 아래 Space(weight=2) = 1:2 → 상단 1/3 지점에 콘텐츠 위치
 *
 * 2. TextInputLayout → Material Design 입력 필드 (floating label 애니메이션)
 *    EditText를 감싸면 힌트가 위로 올라가는 효과가 자동 적용됨
 *
 * 3. MaterialButton 커스텀 → backgroundTint, cornerRadius, textColor로 카카오 스타일 적용
 *
 * LinearLayout이 적합한 이유:
 * 로그인 화면은 "로고 → 입력 → 버튼 → 링크"가 위에서 아래로 순서대로 쌓이는 구조.
 * 이런 1차원 배치에는 LinearLayout이 가장 직관적이고 간단하다.
 * Unity 비유: VerticalLayoutGroup 하나로 로그인 UI를 구성하는 것.
 */
public class KakaoLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_kakao_login);
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

        // ── 하단 링크 클릭 (UI 데모: Toast로 피드백만 표시) ──
        findViewById(R.id.tvSignUp).setOnClickListener(v ->
                Toast.makeText(this, "UI 데모 - 카카오계정 만들기 화면으로 이동", Toast.LENGTH_SHORT).show());
        findViewById(R.id.tvFindAccount).setOnClickListener(v ->
                Toast.makeText(this, "UI 데모 - 계정 찾기 화면으로 이동", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}