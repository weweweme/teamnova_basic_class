package com.example.weak7_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 카드 겹침 화면
 * 레이아웃: FrameLayout
 * 크기 조절: 고정 dp
 * 학습 포인트: FrameLayout은 자식 뷰들을 같은 위치에 겹쳐서 배치한다.
 *            XML에서 나중에 선언한 뷰가 위에(앞에) 그려진다.
 *            Unity에서 Layout Group 없이 같은 위치에 여러 UI를 쌓는 것과 동일.
 *            Hierarchy에서 아래에 있는 오브젝트가 위에 렌더링되는 것과 같은 원리.
 *            카드 겹침, 이미지 위 텍스트 오버레이, 로딩 화면 등에 사용.
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
    }
}