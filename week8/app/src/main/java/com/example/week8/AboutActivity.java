package com.example.week8;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    /// 개발자의 GitHub 프로필 주소
    /// ACTION_VIEW Intent로 브라우저를 열 때 사용
    /// </summary>
    private static final String DEVELOPER_GITHUB_URL = "https://github.com/weweweme";

    /// <summary>
    /// 개발자 피드백을 받을 이메일 주소
    /// ACTION_SENDTO Intent의 mailto: URI에 사용
    /// </summary>
    private static final String DEVELOPER_EMAIL = "argentdarae@naver.com";

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityAboutBinding binding;

    // ========== Lifecycle ==========

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 개발자 GitHub 버튼 → 브라우저로 이동
        binding.buttonDeveloperGithub.setOnClickListener(v -> openDeveloperGithub());

        // 개발자에게 피드백 버튼 → 메일 앱으로 이동
        binding.buttonSendFeedback.setOnClickListener(v -> sendFeedbackEmail());
    }

    // ========== 암시적 Intent: 브라우저 열기 ==========

    /// <summary>
    /// 개발자의 GitHub 프로필을 브라우저로 열기
    ///
    /// ──── ACTION_VIEW 복습 ────
    /// 공식 문서: https://developer.android.com/guide/components/intents-common#Browser
    ///
    /// GameDetailActivity의 "스토어 열기"와 동일한 패턴:
    ///   Intent(ACTION_VIEW, Uri.parse("https://...")) → startActivity
    /// → Android가 scheme="https"를 보고 브라우저 앱을 찾아 실행
    ///
    /// 실제 Intent 내부 모습:
    ///   action = "android.intent.action.VIEW"
    ///   data   = "https://github.com/weweweme"
    ///
    /// try-catch 사용 이유:
    /// Android 11+ 패키지 가시성 제한으로 resolveActivity()가 null 나올 수 있음
    /// → ActivityNotFoundException으로 안전 처리
    /// </summary>
    private void openDeveloperGithub() {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DEVELOPER_GITHUB_URL));
        try {
            startActivity(viewIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.about_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    // ========== 암시적 Intent: 메일 앱 열기 ==========

    /// <summary>
    /// 메일 앱을 열어 개발자에게 피드백 메일 작성 화면 띄우기
    ///
    /// ──── ACTION_SENDTO 학습 ────
    /// 공식 문서: https://developer.android.com/guide/components/intents-common#Email
    ///
    /// ACTION_SENDTO: "이 주소로 뭔가 보낼 수 있는 앱 열어줘" 요청
    /// mailto:email@example.com → Android가 scheme="mailto"를 보고 메일 앱을 찾아 실행
    ///
    /// ──── ACTION_SEND vs ACTION_SENDTO ────
    /// ACTION_SEND: 데이터를 "보낼 수 있는 아무 앱" 후보로 열림 (카톡, 메시지, 메일 전부)
    /// ACTION_SENDTO + mailto: 메일 앱만 대상으로 지정 → 받는 사람까지 지정 가능
    ///
    /// 실제 Intent 내부 모습:
    ///   action = "android.intent.action.SENDTO"
    ///   data   = "mailto:argentdarae@naver.com"
    ///   extras = subject="(Game Diary) 피드백", text="앱을 사용하시며..."
    /// → 메일 앱이 받는 사람/제목/본문을 미리 채운 작성 화면을 띄움
    ///
    /// Uri.fromParts("mailto", email, null):
    ///   Uri.parse("mailto:" + email)과 거의 동일하지만,
    ///   이메일에 특수문자가 있어도 안전하게 처리됨 (공식 권장 방식)
    /// </summary>
    private void sendFeedbackEmail() {
        // mailto URI 생성: "mailto:argentdarae@naver.com"
        Uri mailtoUri = Uri.fromParts("mailto", DEVELOPER_EMAIL, null);

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, mailtoUri);
        // 제목과 본문 미리 채우기 (사용자는 본문만 작성하면 됨)
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_feedback_subject));
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.about_feedback_body));

        // 메일 앱이 있는지 먼저 확인
        // → 없으면 Chooser가 빈 화면으로 떠도 ActivityNotFoundException이 안 나므로 미리 체크
        // → resolveActivity가 동작하려면 AndroidManifest의 <queries>에 SENDTO + mailto를 선언해둬야 함
        //   (Android 11+ 패키지 가시성 규칙)
        if (sendIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.about_no_mail, Toast.LENGTH_SHORT).show();
            return;
        }

        // Chooser로 감싸면 기본 메일 앱으로 바로 가지 않고 "메일 앱 선택" 다이얼로그 표시
        Intent chooser = Intent.createChooser(sendIntent,
                getString(R.string.about_feedback_chooser_title));
        startActivity(chooser);
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
