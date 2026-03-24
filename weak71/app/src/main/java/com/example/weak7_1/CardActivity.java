package com.example.weak7_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 5번 화면 - 뮤직 플레이어 (FrameLayout 학습)
 *
 * [레이아웃 구조]
 * LinearLayout (vertical)
 * ├── MaterialToolbar (뒤로가기)
 * └── FrameLayout (뷰 겹침 영역)
 *     ├── [1층] ImageView - 앨범 아트 (배경)
 *     ├── [2층] View - 하단 그라데이션 (가독성 확보)
 *     └── [3층] LinearLayout - 곡 정보 + 재생 컨트롤
 *
 * [FrameLayout 핵심 학습 포인트]
 * - 자식 뷰들이 같은 위치에 겹쳐 배치된다 (Unity: Layout Group 없이 UI 쌓기)
 * - XML 선언 순서 = Z-order (나중에 선언 = 위에 그려짐)
 * - layout_gravity: 부모 안에서 자식의 위치 결정 (바깥 정렬)
 * - gravity: 자기 안에서 내용물 정렬 (안쪽 정렬)
 *
 * [UI 데모 전용 - 실제 음악 재생 없음]
 */
public class CardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_card);
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
