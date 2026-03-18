package com.example.weak7_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * launchMode 데모 화면
 * Manifest 학습 포인트: android:launchMode, FLAG_ACTIVITY_SINGLE_TOP
 *
 * ============================================================
 * [launchMode = Activity 인스턴스 관리 방식]
 * ============================================================
 *
 * Android는 Activity를 "Task"라는 스택(Stack) 구조로 관리한다.
 * 새 Activity를 열면 스택 맨 위에 쌓이고(push),
 * 뒤로가기를 누르면 맨 위에서 제거된다(pop).
 *
 * Unity 비유: SceneManager의 씬 스택과 비슷하다.
 *            LoadScene(Additive)로 씬을 쌓고, UnloadScene()으로 제거하는 것.
 *
 * launchMode는 "같은 Activity를 다시 열 때" 어떻게 처리할지 결정한다:
 *
 * ────────────────────────────────────────────────────
 * standard (기본값):
 *   매번 새 인스턴스를 생성하여 스택에 쌓는다.
 *   같은 Activity라도 여러 개가 스택에 동시 존재 가능.
 *
 *   Unity 비유: Instantiate()를 호출할 때마다 새 GameObject가 생기는 것.
 *              Destroy하지 않는 한 계속 쌓인다.
 *
 *   스택 예: [Home] → [Launch] → [Launch] → [Launch]
 *           뒤로가기 3번 눌러야 Home으로 돌아감.
 *
 * ────────────────────────────────────────────────────
 * singleTop:
 *   이미 스택 맨 위(top)에 있으면 새로 만들지 않고 재사용한다.
 *   이때 onCreate() 대신 onNewIntent()가 호출된다.
 *   맨 위가 아니면 standard와 동일하게 새 인스턴스 생성.
 *
 *   Unity 비유: Singleton 패턴에 조건이 붙는 것.
 *              if (Instance != null && Instance.IsOnTop()) return Instance;
 *              else Instantiate(new Instance);
 *
 *   스택 예: [Home] → [Launch] 에서 singleTop으로 다시 열면
 *           → [Home] → [Launch] (같은 인스턴스, onNewIntent 호출)
 *
 * ────────────────────────────────────────────────────
 * singleTask:
 *   Task 내에서 하나만 존재할 수 있다.
 *   이미 존재하면 그 위의 모든 Activity를 제거(clear top)하고 재사용.
 *
 *   Unity 비유: DontDestroyOnLoad 매니저 오브젝트.
 *              씬 전환 시에도 하나만 유지되고, 중복 생성 시 기존 것이 우선.
 *
 * ────────────────────────────────────────────────────
 * singleInstance:
 *   자신만의 별도 Task에서 단독으로 실행된다.
 *   다른 Activity와 같은 Task에 공존할 수 없다.
 *
 *   Unity 비유: 완전히 별도의 씬(프로세스)에서 실행되는 독립 시스템.
 *              다른 오브젝트와 같은 씬에 있을 수 없다.
 *
 * ============================================================
 * [이 데모의 핵심 관찰 포인트]
 * ============================================================
 *
 * 1. "자기 자신을 다시 열기" 버튼:
 *    → 일반 startActivity() 호출 = standard 방식
 *    → 매번 새 인스턴스가 생성됨 (hashCode 변경, onCreate 카운트 증가)
 *    → 뒤로가기를 누르면 이전 인스턴스로 돌아감
 *
 * 2. "FLAG_ACTIVITY_SINGLE_TOP으로 열기" 버튼:
 *    → singleTop 방식
 *    → 이미 맨 위에 있으므로 기존 인스턴스 재사용 (hashCode 동일, onNewIntent 카운트 증가)
 *    → 새 인스턴스가 스택에 쌓이지 않음
 */
public class LaunchModeActivity extends AppCompatActivity {

    /*
     * [static 변수 = 클래스 레벨 변수]
     *
     * static이므로 Activity 인스턴스가 새로 생성되어도 값이 유지된다.
     * 앱 프로세스가 살아있는 동안 모든 인스턴스가 이 값을 공유한다.
     *
     * Unity 비유: static 변수와 완전히 동일.
     *            MonoBehaviour의 static 필드는 씬이 바뀌어도 유지되는 것과 같다.
     *
     * 주의: 앱이 완전히 종료되면(프로세스 kill) 0으로 초기화된다.
     *       영구 저장이 필요하면 SharedPreferences를 사용해야 한다.
     */
    private static int createCount = 0;

    /*
     * [인스턴스 변수 = 각 인스턴스마다 개별 값]
     *
     * static이 아니므로 새 인스턴스가 생성되면 0부터 시작한다.
     * onNewIntent()가 호출될 때마다 증가.
     * singleTop으로 재사용될 때만 의미가 있다.
     */
    private int newIntentCount = 0;

    /*
     * 이벤트 로그를 저장하는 리스트.
     * onCreate, onNewIntent 호출 시마다 타임스탬프와 함께 기록.
     *
     * static으로 선언하여 여러 인스턴스 간에 로그가 공유되도록 한다.
     * 새 인스턴스가 생성되어도 이전 로그가 유지되어 흐름을 추적할 수 있다.
     *
     * Unity 비유: static List<string> logs처럼
     *            씬 전환과 무관하게 유지되는 전역 로그.
     */
    private static final ArrayList<String> eventLog = new ArrayList<>();

    /* UI 요소 참조 변수 */
    private TextView tvHashCode;
    private TextView tvTaskId;
    private TextView tvCreateCount;
    private TextView tvNewIntentCount;
    private TextView tvEventLog;

    /* 타임스탬프 포맷 */
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* EdgeToEdge + WindowInsets: Safe Area 설정 */
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launch_mode);
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

        // ===== UI 요소 바인딩 =====
        tvHashCode = findViewById(R.id.tvHashCode);
        tvTaskId = findViewById(R.id.tvTaskId);
        tvCreateCount = findViewById(R.id.tvCreateCount);
        tvNewIntentCount = findViewById(R.id.tvNewIntentCount);
        tvEventLog = findViewById(R.id.tvEventLog);
        Button btnRelaunchSelf = findViewById(R.id.btnRelaunchSelf);
        Button btnRelaunchWithFlag = findViewById(R.id.btnRelaunchWithFlag);

        // ===== onCreate 카운트 증가 및 로그 기록 =====
        /*
         * onCreate()가 호출됨 = 새 인스턴스가 생성됨.
         * static 변수 createCount를 증가시켜 총 생성 횟수를 추적한다.
         *
         * hashCode(): 이 인스턴스의 고유 해시값.
         * getTaskId(): 이 Activity가 속한 Task의 ID.
         * 이 두 값으로 "같은 인스턴스인지, 같은 Task인지"를 구분할 수 있다.
         */
        createCount++;
        addLog("onCreate | hash=" + hashCode() + " | taskId=" + getTaskId());

        // ===== 화면 정보 업데이트 =====
        updateDisplay();

        // ===== 버튼 클릭 리스너 =====

        /*
         * [자기 자신을 다시 열기 - standard 방식]
         *
         * new Intent(this, LaunchModeActivity.class)로 자기 자신을 다시 연다.
         * 플래그를 지정하지 않으므로, Manifest의 launchMode 설정을 따른다.
         * 이 Activity의 launchMode가 standard이면 매번 새 인스턴스가 스택에 쌓인다.
         *
         * 관찰 포인트:
         * - hashCode가 바뀌는가? → 바뀌면 새 인스턴스
         * - createCount가 증가하는가? → 증가하면 onCreate가 호출됨
         * - 뒤로가기를 누르면? → 이전 인스턴스로 돌아감 (스택에서 pop)
         */
        btnRelaunchSelf.setOnClickListener(v -> {
            Intent intent = new Intent(this, LaunchModeActivity.class);
            startActivity(intent);
        });

        /*
         * [FLAG_ACTIVITY_SINGLE_TOP으로 열기 - singleTop 방식]
         *
         * Intent에 FLAG_ACTIVITY_SINGLE_TOP 플래그를 추가한다.
         * 이 Activity가 이미 스택 맨 위에 있으면:
         *   → 새 인스턴스를 만들지 않음
         *   → 기존 인스턴스의 onNewIntent()가 호출됨
         *   → hashCode 동일, newIntentCount 증가
         *
         * Manifest에서 android:launchMode="singleTop"을 선언하는 것과
         * 코드에서 FLAG_ACTIVITY_SINGLE_TOP을 지정하는 것은 같은 효과이다.
         * 차이점: Manifest는 항상 적용, 플래그는 특정 호출에서만 적용.
         *
         * Unity 비유:
         *   if (FindObjectOfType<LaunchModeActivity>() != null)
         *       existingInstance.OnNewIntent(data);
         *   else
         *       Instantiate(prefab);
         */
        btnRelaunchWithFlag.setOnClickListener(v -> {
            Intent intent = new Intent(this, LaunchModeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    /**
     * [onNewIntent - singleTop으로 재사용될 때 호출]
     *
     * 이 메서드는 Activity가 이미 스택 맨 위에 있고,
     * FLAG_ACTIVITY_SINGLE_TOP 또는 launchMode="singleTop"으로 호출될 때 실행된다.
     *
     * onCreate() 대신 이 메서드가 호출되므로,
     * 새로운 Intent 데이터를 여기서 처리해야 한다.
     *
     * Unity 비유:
     *   Singleton 패턴에서 이미 인스턴스가 존재할 때,
     *   새 데이터로 갱신(Refresh)하는 메서드가 호출되는 것.
     *   Start()가 아닌 별도의 Initialize(newData) 메서드가 호출된다고 생각하면 된다.
     *
     * 생명주기 차이:
     *   새 인스턴스 → onCreate() → onStart() → onResume()
     *   재사용       → onNewIntent() → onResume()
     *   (onStart()도 생략될 수 있다)
     *
     * @param intent 새로 전달된 Intent 데이터
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        /*
         * setIntent(intent):
         * → 이 Activity가 가지고 있는 Intent를 새 것으로 교체한다.
         *   이후 getIntent()를 호출하면 새 Intent가 반환된다.
         *   교체하지 않으면 최초 생성 시의 오래된 Intent가 계속 유지된다.
         */
        setIntent(intent);

        newIntentCount++;
        addLog("onNewIntent | hash=" + hashCode() + " | taskId=" + getTaskId());

        // 화면 정보 갱신
        updateDisplay();
    }

    /**
     * 화면의 모든 정보를 현재 상태에 맞게 업데이트한다.
     */
    private void updateDisplay() {
        tvHashCode.setText("인스턴스 hashCode: " + hashCode());
        tvTaskId.setText("Task ID: " + getTaskId());
        tvCreateCount.setText("onCreate 호출 횟수: " + createCount);
        tvNewIntentCount.setText("onNewIntent 호출 횟수: " + newIntentCount);

        // 이벤트 로그 전체를 TextView에 표시
        StringBuilder logText = new StringBuilder("이벤트 로그:\n");
        for (String entry : eventLog) {
            logText.append(entry).append("\n");
        }
        tvEventLog.setText(logText.toString());
    }

    /**
     * 이벤트 로그에 타임스탬프와 함께 항목을 추가한다.
     *
     * @param message 로그 메시지 (예: "onCreate | hash=12345 | taskId=1")
     */
    private void addLog(String message) {
        String timestamp = timeFormat.format(new Date());
        eventLog.add("[" + timestamp + "] " + message);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // standard 모드로 여러 인스턴스가 스택에 쌓여 있을 수 있으므로,
        // FLAG_ACTIVITY_CLEAR_TOP으로 HomeActivity 위의 모든 Activity를 제거하고 홈으로 돌아간다.
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        return true;
    }
}
