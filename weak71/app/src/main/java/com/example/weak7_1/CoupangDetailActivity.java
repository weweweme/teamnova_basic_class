package com.example.weak7_1;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * [쿠팡 스타일 상품 상세 화면]
 *
 * 레이아웃: ScrollView + LinearLayout (vertical)
 * 컨셉: 쿠팡 상품 상세 페이지를 ScrollView로 재현
 *
 * 학습 포인트:
 * 1. ScrollView → 화면에 안 담기는 긴 콘텐츠를 스크롤 가능하게
 *    자식 1개만 가능 → LinearLayout으로 감싸기
 *    Unity 비유: Scroll Rect + Content(VerticalLayoutGroup)
 *
 * 2. 툴바는 ScrollView 바깥 → 스크롤해도 고정
 *    ScrollView에 height="0dp" + weight="1" → 나머지 영역 전부 차지
 *
 * 3. 두꺼운 구분선(8dp) → 쿠팡 스타일 섹션 구분
 *
 * 4. 취소선(strikethrough) → XML로 안 되고 Java에서 paintFlags로 적용
 *
 * ScrollView가 적합한 이유:
 * 상품 상세는 이미지 + 가격 + 배송 + 설명이 위→아래로 길게 이어지는 구조.
 * 스크롤 없이는 화면에 다 표시할 수 없다.
 */
public class CoupangDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_coupang_detail);
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

        // ── 원래 가격에 취소선 적용 ──
        // XML만으로는 취소선을 넣을 수 없어서 Java에서 paintFlags를 설정한다.
        // Paint.STRIKE_THRU_TEXT_FLAG → 텍스트 위에 가로줄을 그어서 "47,500원" 처럼 표시.
        TextView tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
