package com.example.weak7_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 계산기 화면
 * 레이아웃: ConstraintLayout + GridLayout
 * 크기 조절: 좌우 Guideline(5%~95%)으로 버튼 영역을 화면의 90%로 제한
 * 학습 포인트: GridLayout은 행(row)과 열(column) 격자에 뷰를 배치한다.
 *            Unity의 Grid Layout Group과 동일한 개념.
 *            columnCount로 열 수를 지정하면 자식 뷰가 자동으로 줄바꿈된다.
 *            columnWeight로 각 버튼이 균등한 너비를 가지도록 분배.
 *            세로 Guideline(vertical)으로 좌우 여백을 비율 기반으로 잡음.
 */
public class CalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calculator);
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