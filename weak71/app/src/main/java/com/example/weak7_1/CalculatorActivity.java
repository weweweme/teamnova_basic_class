package com.example.weak7_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 4번 화면 - iOS 계산기 (GridLayout 학습)
 *
 * [레이아웃 구조]
 * LinearLayout (vertical, 검정 배경)
 * ├── MaterialToolbar (뒤로가기)
 * ├── TextView (결과 표시, weight=1로 남은 공간 전부 차지)
 * └── GridLayout (4열 × 5행, 계산기 버튼)
 *
 * [GridLayout 핵심 학습 포인트]
 * - columnCount="4" → 한 줄에 4개씩 자동 배치 (Unity Grid Layout Group의 constraintCount)
 * - layout_columnWeight="1" + width="0dp" → 균등 분배 (LinearLayout weight와 동일 원리)
 * - layout_columnSpan="2" → 여러 칸 병합 (0 버튼이 2칸 차지)
 * - android.widget.Button → Material3 자동 변환 방지, 커스텀 배경색 적용
 *
 * [계산기 동작]
 * - 숫자(0~9, .) → 화면에 숫자 입력
 * - 연산자(+, -, ×, ÷) → 첫 번째 숫자 저장 + 연산자 기억
 * - = → 저장된 숫자와 현재 숫자로 계산 실행
 * - AC → 전부 초기화
 * - ± → 부호 반전
 * - % → 100으로 나누기
 */
public class CalculatorActivity extends AppCompatActivity {

    private TextView tvResult;

    // 현재 화면에 보이는 숫자 문자열
    private String currentDisplay = "0";
    // 연산자 누르기 전에 저장해둔 첫 번째 숫자
    private double firstOperand = 0;
    // 선택된 연산자 ("", "+", "-", "×", "÷")
    private String pendingOperator = "";
    // 연산자를 막 눌렀는지 (다음 숫자 입력 시 화면을 새로 시작)
    private boolean startNewInput = true;

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

        tvResult = findViewById(R.id.tvResult);

        setupNumberButtons();
        setupOperatorButtons();
        setupFunctionButtons();
    }

    // ── 숫자 버튼 (0~9, .) ──
    private void setupNumberButtons() {
        int[] numBtnIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        for (int id : numBtnIds) {
            findViewById(id).setOnClickListener(v -> {
                String digit = ((Button) v).getText().toString();
                if (startNewInput) {
                    currentDisplay = digit;
                    startNewInput = false;
                } else if (currentDisplay.equals("0")) {
                    currentDisplay = digit;
                } else {
                    currentDisplay += digit;
                }
                tvResult.setText(currentDisplay);
            });
        }

        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (startNewInput) {
                currentDisplay = "0.";
                startNewInput = false;
            } else if (!currentDisplay.contains(".")) {
                currentDisplay += ".";
            }
            tvResult.setText(currentDisplay);
        });
    }

    // ── 연산자 버튼 (+, -, ×, ÷, =) ──
    private void setupOperatorButtons() {
        // 사칙연산 버튼: 첫 번째 숫자를 저장하고, 연산자를 기억
        int[] opBtnIds = {R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide};
        for (int id : opBtnIds) {
            findViewById(id).setOnClickListener(v -> {
                // 이전 연산이 남아있으면 먼저 계산
                if (!startNewInput && !pendingOperator.isEmpty()) {
                    calculate();
                }
                firstOperand = Double.parseDouble(currentDisplay);
                pendingOperator = ((Button) v).getText().toString();
                startNewInput = true;
            });
        }

        // = 버튼: 계산 실행
        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            if (!pendingOperator.isEmpty()) {
                calculate();
                pendingOperator = "";
            }
        });
    }

    // ── 기능 버튼 (AC, ±, %) ──
    private void setupFunctionButtons() {
        // AC: 전부 초기화
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentDisplay = "0";
            firstOperand = 0;
            pendingOperator = "";
            startNewInput = true;
            tvResult.setText(currentDisplay);
        });

        // ±: 부호 반전
        findViewById(R.id.btnSign).setOnClickListener(v -> {
            double value = Double.parseDouble(currentDisplay);
            value = -value;
            currentDisplay = formatResult(value);
            tvResult.setText(currentDisplay);
        });

        // %: 100으로 나누기
        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            double value = Double.parseDouble(currentDisplay);
            value = value / 100.0;
            currentDisplay = formatResult(value);
            tvResult.setText(currentDisplay);
        });
    }

    // ── 계산 실행 ──
    private void calculate() {
        double secondOperand = Double.parseDouble(currentDisplay);
        double result;

        switch (pendingOperator) {
            case "+": result = firstOperand + secondOperand; break;
            case "-": result = firstOperand - secondOperand; break;
            case "×": result = firstOperand * secondOperand; break;
            case "÷":
                if (secondOperand == 0) {
                    currentDisplay = "오류";
                    tvResult.setText(currentDisplay);
                    startNewInput = true;
                    return;
                }
                result = firstOperand / secondOperand;
                break;
            default: return;
        }

        currentDisplay = formatResult(result);
        firstOperand = result;
        startNewInput = true;
        tvResult.setText(currentDisplay);
    }

    // ── 결과 포맷 (정수면 소수점 제거) ──
    private String formatResult(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
