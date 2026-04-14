package com.example.week8;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.week8.databinding.ActivityAboutBinding;

/// <summary>
/// 앱 정보 화면
/// 앱 이름/버전/설명과 사용 API 소개를 보여주고,
/// 외부 사이트 방문(ACTION_VIEW) + 개발자에게 피드백 메일(ACTION_SENDTO) 기능 제공
/// Unity로 비유하면 "Credits/About" 씬
///
/// ──── Intent 학습 ────
/// 송신 (다음 커밋):
///   ACTION_VIEW + https URI → 브라우저로 RAWG 사이트 열기 (GameDetail에서 한 번 연습)
///   ACTION_SENDTO + mailto URI → 메일 앱으로 피드백 메일 작성
///
/// ──── ACTION_SEND vs ACTION_SENDTO 차이 ────
/// ACTION_SEND: 데이터를 "보낼 수 있는 아무 앱"에게 던짐 (카톡/메시지/메일 등 전부 후보)
/// ACTION_SENDTO + mailto: 메일 앱만 대상으로 지정 (받는 사람/제목/본문까지 미리 채움)
/// </summary>
public class AboutActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityAboutBinding binding;

    // ========== Lifecycle ==========

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // edge-to-edge 비활성화
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        // ViewBinding 연결
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 버튼 리스너 (다음 커밋에서 ACTION_VIEW / ACTION_SENDTO 구현)
        binding.buttonVisitRawg.setOnClickListener(v -> {
            // TODO: ACTION_VIEW
        });
        binding.buttonSendFeedback.setOnClickListener(v -> {
            // TODO: ACTION_SENDTO
        });
    }

    /// <summary>
    /// ActionBar의 ← 버튼 클릭 처리
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
