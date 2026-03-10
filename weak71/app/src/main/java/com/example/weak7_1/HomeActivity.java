package com.example.weak7_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLinear = findViewById(R.id.btnLinearLayout);
        Button btnConstraint = findViewById(R.id.btnConstraintLayout);
        Button btnScroll = findViewById(R.id.btnScrollView);
        Button btnGrid = findViewById(R.id.btnGridLayout);
        Button btnFrame = findViewById(R.id.btnFrameLayout);

        // 아직 만들지 않은 액티비티는 Toast로 안내
        btnLinear.setOnClickListener(v ->
                Toast.makeText(this, "LinearLayout 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
        btnConstraint.setOnClickListener(v ->
                Toast.makeText(this, "ConstraintLayout 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
        btnScroll.setOnClickListener(v ->
                Toast.makeText(this, "ScrollView 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
        btnGrid.setOnClickListener(v ->
                Toast.makeText(this, "GridLayout 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
        btnFrame.setOnClickListener(v ->
                Toast.makeText(this, "FrameLayout 데모 준비 중", Toast.LENGTH_SHORT).show()
        );
    }
}
