package com.example.weak7_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 로그인 화면
 * 레이아웃: LinearLayout (vertical)
 * 크기 조절: 고정 dp (기본 방식)
 * 학습 포인트: LinearLayout은 자식 뷰를 순서대로 한 방향(세로/가로)으로 쌓는 레이아웃.
 *            Unity의 Vertical/Horizontal Layout Group과 동일한 개념.
 *            가장 단순하고 직관적이지만, 복잡한 배치에는 한계가 있다.
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}