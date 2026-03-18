package com.example.weak7_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 프로필 화면
 * 레이아웃: ConstraintLayout
 * 크기 조절: 고정 크기(dp) + Guideline(비율 기반 위치) 조합 (실무 방식)
 * 학습 포인트: ConstraintLayout은 각 뷰에 상하좌우 제약(Constraint)을 걸어 위치를 결정.
 *            Unity의 RectTransform 앵커 설정과 유사.
 *            Guideline을 사용하면 화면의 퍼센트(%) 기준으로 위치를 잡을 수 있어
 *            다양한 화면 크기에서도 비율이 유지된다.
 */
public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
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