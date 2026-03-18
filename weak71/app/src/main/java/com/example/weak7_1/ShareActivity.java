package com.example.weak7_1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 공유 수신 화면 (ShareActivity)
 *
 * 다른 앱에서 "공유" 버튼을 통해 전달한 데이터를 수신하여 화면에 표시한다.
 *
 * [핵심 개념: Intent-Filter]
 * AndroidManifest.xml에 intent-filter를 등록하면,
 * 이 액티비티가 특정 타입의 요청(Intent)을 처리할 수 있다고 시스템에 알린다.
 *
 * Unity 비유:
 *   Unity에서 특정 이벤트 타입에 대한 콜백을 등록하는 것과 동일하다.
 *   예) IPointerClickHandler를 구현하면 클릭 이벤트를 받을 수 있듯이,
 *       intent-filter에 ACTION_SEND를 등록하면 "공유" 이벤트를 받을 수 있다.
 *   또는 EventManager.Subscribe("Share", OnShareReceived)처럼
 *   특정 이벤트 키에 대한 리스너를 등록하는 것으로 생각할 수 있다.
 *
 * [Manifest에 필요한 설정]
 * - action: android.intent.action.SEND (공유 액션)
 * - category: android.intent.category.DEFAULT (암시적 인텐트를 받으려면 필수)
 * - data mimeType: 수신할 데이터 타입 (text/*, image/* 등)
 * - exported="true": 다른 앱에서 이 액티비티에 접근 가능하도록 허용
 *                    Unity 비유: public 접근 제한자와 비슷.
 *                    true = public (외부 접근 가능)
 *                    false = private (같은 앱 내부에서만 접근)
 */
public class ShareActivity extends AppCompatActivity {

    // 수신 데이터의 타입과 내용을 표시할 텍스트뷰
    private TextView tvSharedType;
    private TextView tvSharedContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_share);

        // WindowInsets 처리: 시스템 바(상태바, 네비게이션 바) 영역만큼 패딩 적용
        // Unity 비유: SafeArea를 고려해서 UI 영역을 조정하는 것과 동일
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 뷰 바인딩 (findViewById)
        // Unity 비유: GameObject.Find("tvSharedType").GetComponent<Text>()와 비슷
        tvSharedType = findViewById(R.id.tvSharedType);
        tvSharedContent = findViewById(R.id.tvSharedContent);

        // 이 액티비티를 실행한 인텐트를 확인하여 공유 데이터 처리
        handleIncomingIntent(getIntent());
    }

    /**
     * 수신한 인텐트를 분석하여 공유 데이터를 화면에 표시하는 메서드.
     *
     * [Intent란?]
     * Android에서 컴포넌트 간 통신을 위한 메시지 객체.
     * Unity 비유: SendMessage()나 이벤트 시스템으로 전달하는 데이터 패킷과 유사.
     *
     * [ACTION_SEND란?]
     * Android 표준 공유 액션. 사용자가 앱에서 "공유" 버튼을 누르면
     * 시스템이 ACTION_SEND 인텐트를 만들어서 수신 가능한 앱 목록을 보여준다.
     * Unity 비유: 글로벌 이벤트 버스에서 "Share"라는 이벤트를 발행(publish)하면,
     *            구독(subscribe)한 모든 리스너에게 전달되는 것과 동일.
     *
     * [mimeType이란?]
     * 데이터의 형식을 나타내는 문자열. (예: text/plain, image/jpeg)
     * intent-filter에서 mimeType을 지정하면 해당 타입의 데이터만 수신한다.
     * Unity 비유: 이벤트 데이터의 타입 체크와 비슷.
     *            OnEvent(EventData data)에서 data가 StringData인지 ImageData인지
     *            확인하고 분기하는 것과 동일한 패턴.
     *
     * @param intent 이 액티비티를 실행한 인텐트
     */
    private void handleIncomingIntent(Intent intent) {
        // 인텐트의 액션과 타입 확인
        String action = intent.getAction();
        String type = intent.getType();

        // ACTION_SEND이고 타입이 존재하는 경우 = 다른 앱에서 공유한 데이터가 있음
        if (Intent.ACTION_SEND.equals(action) && type != null) {

            if (type.startsWith("text/")) {
                // [텍스트 공유 수신]
                // EXTRA_TEXT: 공유된 텍스트 데이터를 담고 있는 표준 키
                // Unity 비유: EventData에서 "text"라는 키로 string 값을 꺼내는 것
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

                tvSharedType.setText("데이터 타입: " + type);

                if (sharedText != null) {
                    tvSharedContent.setText("수신 데이터:\n\n" + sharedText);
                } else {
                    tvSharedContent.setText("수신 데이터: 텍스트가 비어있음");
                }

            } else if (type.startsWith("image/")) {
                // [이미지 공유 수신]
                // EXTRA_STREAM: 공유된 파일(이미지 등)의 URI를 담고 있는 표준 키
                // Uri는 파일의 위치를 가리키는 주소.
                // Unity 비유: AssetDatabase.GetAssetPath()로 에셋 경로를 얻는 것과 유사.
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

                tvSharedType.setText("데이터 타입: " + type);

                if (imageUri != null) {
                    tvSharedContent.setText("수신 데이터 (이미지 URI):\n\n" + imageUri.toString());
                } else {
                    tvSharedContent.setText("수신 데이터: 이미지 URI가 비어있음");
                }

            } else {
                // 기타 타입: text나 image가 아닌 다른 MIME 타입
                tvSharedType.setText("데이터 타입: " + type);
                tvSharedContent.setText("수신 데이터: 지원하지 않는 타입입니다");
            }

        } else {
            // [직접 실행된 경우]
            // ACTION_SEND가 아닌 경우 = 홈 화면이나 다른 경로에서 직접 실행됨
            // Unity 비유: 이벤트 없이 직접 Scene을 로드한 경우
            tvSharedType.setText("데이터 타입: 없음");
            tvSharedContent.setText("홈에서 직접 열림 (공유 데이터 없음)");
        }
    }
}
