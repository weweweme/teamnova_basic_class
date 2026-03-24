package com.example.weak7_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * [인스타그램 스타일 프로필 화면]
 *
 * 레이아웃: ConstraintLayout
 * 컨셉: 인스타그램 마이페이지를 ConstraintLayout으로 재현
 *
 * 학습 포인트:
 * 1. 제약(Constraint) 기본 - 모든 뷰에 가로/세로 최소 2개 제약 필요
 *    constraintTop_toBottomOf, constraintStart_toEndOf 등으로 뷰 간 상대 배치
 *    Unity 비유: RectTransform 앵커를 다른 오브젝트에 연결
 *
 * 2. Chain (체인) - 게시물/팔로워/팔로잉 숫자 3개를 가로로 균등 분배
 *    뷰들이 서로의 Start/End를 물고 있으면 체인이 형성됨
 *
 * 3. 0dp (match_constraint) - 양쪽 제약 사이를 꽉 채움
 *
 * 4. 다른 레이아웃 섞어 쓰기 - LinearLayout(버튼), HorizontalScrollView(스토리),
 *    GridLayout(게시물)을 ConstraintLayout 안에 자식으로 넣는 실무 패턴
 *
 * ConstraintLayout이 적합한 이유:
 * 인스타 프로필은 가로/세로가 복잡하게 엮여있다 (사진은 왼쪽, 숫자는 오른쪽 등).
 * LinearLayout만으로는 이런 2차원 배치를 구현하기 어렵다.
 */
public class InstaProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insta_profile);
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