package com.example.weak7_1;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * ============================================================
 * MetaDataActivity - meta-data / Service / BroadcastReceiver 데모
 * ============================================================
 *
 * 이 Activity에서 학습하는 Android 4대 컴포넌트 중 3가지:
 *
 * 1) meta-data 읽기
 *    - AndroidManifest.xml에 <meta-data> 태그로 선언한 key-value 쌍을
 *      PackageManager를 통해 런타임에 읽어온다.
 *    - Application 레벨: 앱 전체에서 공유하는 설정 (API 키, 버전 메모 등)
 *    - Activity 레벨: 특정 Activity에만 적용되는 설정 (설명, 난이도 등)
 *    - Unity 비유: ScriptableObject에 설정을 저장해두고
 *                  Resources.Load<MyConfig>()로 읽어오는 패턴과 같다.
 *                  Application 레벨 = 프로젝트 전역 설정 (Project Settings)
 *                  Activity 레벨 = 특정 씬/오브젝트에만 붙는 설정 ([SerializeField])
 *
 * 2) Service (포그라운드 서비스)
 *    - 화면(UI) 없이 백그라운드에서 작업을 수행하는 컴포넌트.
 *    - startForegroundService()로 시작하고, stopService()로 중지한다.
 *    - Unity 비유: DontDestroyOnLoad()로 유지되는 오브젝트에서
 *                  StartCoroutine()으로 백그라운드 작업을 실행하는 것.
 *                  포그라운드 서비스 = 진행 표시줄이 있는 코루틴.
 *
 * 3) BroadcastReceiver (방송 수신자)
 *    - 시스템이나 다른 앱에서 보내는 브로드캐스트를 수신하는 컴포넌트.
 *    - registerReceiver()로 등록, unregisterReceiver()로 해제.
 *    - Unity 비유: UnityEvent.AddListener()로 이벤트를 구독하고,
 *                  RemoveListener()로 구독을 해제하는 것과 같다.
 *                  시스템 이벤트 = Unity의 OnApplicationPause, OnApplicationFocus 같은 콜백.
 */
public class MetaDataActivity extends AppCompatActivity {

    /**
     * TAG = 로그에 사용하는 태그 이름.
     * Logcat에서 이 값으로 필터링하면 이 Activity의 로그만 볼 수 있다.
     * Unity 비유: Debug.Log()의 메시지 앞에 "[MetaData]" 접두사를 붙이는 것.
     */
    private static final String TAG = "MetaDataActivity";

    // ── UI 요소 참조 변수들 ──
    // Unity 비유: [SerializeField] private Text tvAppApiKey; 처럼
    //            Inspector에서 드래그&드롭으로 연결하는 것 대신,
    //            Android에서는 findViewById()로 XML의 뷰와 Java 코드를 연결한다.

    // 섹션 1: Application 레벨 meta-data 표시용
    private TextView tvAppApiKey;
    private TextView tvAppVersionNote;

    // 섹션 2: Activity 레벨 meta-data 표시용
    private TextView tvActivityDescription;
    private TextView tvDifficultyLevel;

    // 섹션 3: Service 관련
    private Button btnStartService;
    private Button btnStopService;
    private TextView tvServiceStatus;

    // 섹션 4: BroadcastReceiver 관련
    private Button btnRegisterReceiver;
    private Button btnUnregisterReceiver;
    private TextView tvReceiverStatus;
    private TextView tvLastTick;

    /**
     * TimeTickReceiver 인스턴스.
     * registerReceiver()와 unregisterReceiver()에 같은 인스턴스를 사용해야 한다.
     * Unity 비유: AddListener()에 전달한 콜백 참조를 변수에 저장해두고,
     *            나중에 RemoveListener()할 때 같은 참조를 전달해야 하는 것과 같다.
     */
    private TimeTickReceiver timeTickReceiver;

    /**
     * 리시버 등록 여부를 추적하는 플래그.
     * unregisterReceiver()를 이미 해제된 리시버에 호출하면 IllegalArgumentException이 발생하므로,
     * 이 플래그로 중복 해제를 방지한다.
     * Unity 비유: isSubscribed 같은 bool 변수로 이벤트 중복 구독/해제를 관리하는 것.
     */
    private boolean isReceiverRegistered = false;

    /**
     * ActivityResultLauncher - Android 13+(API 33) 이상에서 알림 권한을 요청하기 위한 런처.
     *
     * 왜 필요한가?
     * - Android 13부터 POST_NOTIFICATIONS 권한이 런타임 권한으로 바뀌었다.
     * - 포그라운드 서비스는 반드시 알림을 표시해야 하므로, 이 권한이 필요하다.
     * - 권한이 없으면 알림이 표시되지 않아 서비스가 제대로 동작하지 않는다.
     *
     * Unity 비유: iOS에서 푸시 알림을 보내기 전에
     *            UnityEngine.iOS.NotificationServices.RegisterForNotifications()로
     *            사용자에게 권한을 요청하는 것과 같다.
     *
     * 동작 흐름:
     * 1) 사용자가 "서비스 시작" 버튼을 누름
     * 2) Android 13+ && 권한 미부여 → 이 런처로 권한 요청 팝업 표시
     * 3) 사용자가 허용/거부 선택
     * 4) 콜백에서 결과에 따라 서비스 시작 또는 안내 메시지 표시
     */
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * EdgeToEdge.enable(this)
         * → 상태바, 네비게이션바 영역까지 앱 컨텐츠를 확장한다.
         *   기본적으로 시스템 바 뒤에도 앱이 그려지게 된다.
         *   Unity 비유: Canvas의 Render Mode를 "Screen Space - Overlay"로 설정하면
         *              화면 전체를 덮는 것과 유사하다.
         */
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meta_data);

        /*
         * WindowInsets 처리
         * → EdgeToEdge를 사용하면 시스템 바 영역과 앱 컨텐츠가 겹칠 수 있다.
         *   setOnApplyWindowInsetsListener()로 시스템 바 높이만큼 패딩을 추가하여
         *   컨텐츠가 시스템 바에 가려지지 않게 한다.
         * Unity 비유: Screen.safeArea를 읽어서 UI를 안전 영역 안에 배치하는 것.
         */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ── UI 요소 바인딩 (findViewById) ──
        // XML에서 정의한 뷰를 Java 코드에서 사용할 수 있도록 연결한다.
        // Unity 비유: GameObject.Find("이름").GetComponent<Text>()와 같다.
        //            다만 Android에서는 ID(R.id.xxx)로 찾는다.
        initViews();

        // ── 알림 권한 요청 런처 초기화 ──
        // 반드시 onCreate()에서 초기화해야 한다 (Activity가 STARTED 상태 이전에).
        // Unity 비유: Awake()에서 초기화하는 것처럼, 라이프사이클 초기에 등록해야 한다.
        initNotificationPermissionLauncher();

        // ── meta-data 읽기 ──
        loadApplicationMetaData();
        loadActivityMetaData();

        // ── 버튼 클릭 리스너 설정 ──
        setupServiceButtons();
        setupReceiverButtons();
    }

    /**
     * UI 요소들을 findViewById()로 바인딩하는 메서드.
     *
     * Unity 비유:
     *   void Start() {
     *       tvAppApiKey = transform.Find("tvAppApiKey").GetComponent<Text>();
     *       // ... 나머지도 동일
     *   }
     *
     * Android에서 R.id.xxx는 res/layout XML에서 android:id="@+id/xxx"로 정의한 ID다.
     * "+id"는 "새 ID를 생성하면서 참조"한다는 의미.
     */
    private void initViews() {
        // 섹션 1: Application meta-data
        tvAppApiKey = findViewById(R.id.tvAppApiKey);
        tvAppVersionNote = findViewById(R.id.tvAppVersionNote);

        // 섹션 2: Activity meta-data
        tvActivityDescription = findViewById(R.id.tvActivityDescription);
        tvDifficultyLevel = findViewById(R.id.tvDifficultyLevel);

        // 섹션 3: Service
        btnStartService = findViewById(R.id.btnStartService);
        btnStopService = findViewById(R.id.btnStopService);
        tvServiceStatus = findViewById(R.id.tvServiceStatus);

        // 섹션 4: BroadcastReceiver
        btnRegisterReceiver = findViewById(R.id.btnRegisterReceiver);
        btnUnregisterReceiver = findViewById(R.id.btnUnregisterReceiver);
        tvReceiverStatus = findViewById(R.id.tvReceiverStatus);
        tvLastTick = findViewById(R.id.tvLastTick);
    }

    /**
     * 알림 권한 요청 런처를 초기화한다.
     *
     * registerForActivityResult() 패턴:
     * - 기존의 onActivityResult() / onRequestPermissionsResult() 콜백을 대체하는 현대적 방식.
     * - 결과를 처리할 콜백을 미리 등록해두고, launch()로 권한 요청을 시작한다.
     * - Unity 비유: 코루틴으로 비동기 작업을 실행하고 yield return으로 결과를 받는 패턴.
     *              또는 async/await 패턴에서 Task<bool>의 결과를 받는 것과 유사.
     */
    private void initNotificationPermissionLauncher() {
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 권한이 허용됨 → 서비스 시작
                        Log.d(TAG, "알림 권한 허용됨 → 포그라운드 서비스 시작");
                        startMyForegroundService();
                    } else {
                        // 권한이 거부됨 → 안내 메시지
                        Log.w(TAG, "알림 권한 거부됨 → 서비스 시작 불가");
                        Toast.makeText(this,
                                "알림 권한이 필요합니다. 설정에서 허용해주세요.",
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // ============================================================
    // 섹션 1: Application 레벨 meta-data 읽기
    // ============================================================

    /**
     * AndroidManifest.xml의 <application> 태그 안에 선언된 meta-data를 읽는다.
     *
     * 사용하는 API:
     *   PackageManager.getApplicationInfo(패키지명, GET_META_DATA)
     *   → ApplicationInfo 객체를 반환
     *   → applicationInfo.metaData (Bundle 타입)에서 key로 값을 꺼낸다
     *
     * Unity 비유:
     *   // ScriptableObject에서 설정값 읽기
     *   var config = Resources.Load<AppConfig>("GlobalConfig");
     *   string apiKey = config.apiKey;
     *   string versionNote = config.versionNote;
     *
     * meta-data에 올 수 있는 값 타입:
     *   - String:  getString("key")
     *   - int:     getInt("key")
     *   - float:   getFloat("key")
     *   - boolean: getBoolean("key")
     *   - resource 참조: getInt("key") → R.xxx.yyy ID 값
     *
     * 주의사항:
     *   meta-data의 value가 숫자로만 구성된 문자열이면 (예: "12345")
     *   Android가 자동으로 int로 변환한다.
     *   문자열로 읽으려면 getString()을 사용하되, null 체크를 반드시 해야 한다.
     */
    private void loadApplicationMetaData() {
        try {
            // getPackageManager() → Android 시스템의 패키지 관리자를 가져온다.
            // getApplicationInfo() → 이 앱의 ApplicationInfo를 가져온다.
            // GET_META_DATA 플래그 → meta-data 정보도 함께 포함해서 가져오라는 의미.
            //                       이 플래그 없이 호출하면 metaData 필드가 null이 된다!
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
                    getPackageName(),   // 현재 앱의 패키지명 (com.example.weak7_1)
                    PackageManager.GET_META_DATA  // meta-data 포함 플래그
            );

            // metaData는 Bundle 타입 (key-value 저장소)
            // Unity 비유: Bundle = Dictionary<string, object>와 유사
            Bundle metaData = appInfo.metaData;

            if (metaData != null) {
                // Application 레벨 meta-data에서 값 읽기
                // key는 AndroidManifest.xml에서 android:name으로 지정한 값과 일치해야 한다
                String apiKey = metaData.getString("com.example.weak7_1.API_KEY");
                String versionNote = metaData.getString("com.example.weak7_1.VERSION_NOTE");

                tvAppApiKey.setText("API Key: " + apiKey);
                tvAppVersionNote.setText("Version Note: " + versionNote);

                Log.d(TAG, "[Application meta-data] API_KEY=" + apiKey
                        + ", VERSION_NOTE=" + versionNote);
            } else {
                tvAppApiKey.setText("API Key: (meta-data 없음)");
                tvAppVersionNote.setText("Version Note: (meta-data 없음)");
                Log.w(TAG, "Application meta-data가 null입니다. "
                        + "AndroidManifest.xml에 <meta-data>를 선언했는지 확인하세요.");
            }

        } catch (PackageManager.NameNotFoundException e) {
            // 패키지를 찾을 수 없는 경우 (이론적으로 자기 자신의 패키지는 항상 찾을 수 있음)
            Log.e(TAG, "Application meta-data 읽기 실패: " + e.getMessage());
            tvAppApiKey.setText("API Key: (읽기 실패)");
            tvAppVersionNote.setText("Version Note: (읽기 실패)");
        }
    }

    // ============================================================
    // 섹션 2: Activity 레벨 meta-data 읽기
    // ============================================================

    /**
     * AndroidManifest.xml의 특정 <activity> 태그 안에 선언된 meta-data를 읽는다.
     *
     * 사용하는 API:
     *   PackageManager.getActivityInfo(ComponentName, GET_META_DATA)
     *   → ActivityInfo 객체를 반환
     *   → activityInfo.metaData (Bundle 타입)에서 key로 값을 꺼낸다
     *
     * ComponentName이란?
     *   - 앱의 특정 컴포넌트(Activity, Service 등)를 식별하는 클래스.
     *   - (패키지명, 클래스명)의 쌍으로 구성된다.
     *   - Unity 비유: 씬 이름 + 오브젝트 이름으로 특정 오브젝트를 식별하는 것.
     *
     * Application 레벨과의 차이:
     *   - Application 레벨: 앱 전체에서 공유 → getApplicationInfo()
     *   - Activity 레벨: 특정 Activity에만 적용 → getActivityInfo()
     *   - Unity 비유: static 전역 변수 vs 인스턴스 변수의 차이
     */
    private void loadActivityMetaData() {
        try {
            // ComponentName = (패키지명, Activity 클래스) 쌍
            // this = 현재 Activity 인스턴스
            // getClass() = MetaDataActivity.class 반환
            ComponentName componentName = new ComponentName(this, getClass());

            // getActivityInfo()로 이 Activity의 정보를 가져온다
            // GET_META_DATA 플래그를 반드시 포함해야 metaData가 null이 아니다
            ActivityInfo activityInfo = getPackageManager().getActivityInfo(
                    componentName,
                    PackageManager.GET_META_DATA
            );

            Bundle metaData = activityInfo.metaData;

            if (metaData != null) {
                // Activity 레벨 meta-data에서 값 읽기
                String description = metaData.getString(
                        "com.example.weak7_1.ACTIVITY_DESCRIPTION");
                int difficultyLevel = metaData.getInt(
                        "com.example.weak7_1.DIFFICULTY_LEVEL", 0);
                // getInt()의 두 번째 파라미터(0)는 키가 없을 때 반환할 기본값.
                // Unity 비유: PlayerPrefs.GetInt("key", defaultValue)와 같다.

                tvActivityDescription.setText("Activity 설명: " + description);
                tvDifficultyLevel.setText("난이도: " + difficultyLevel + " / 5");

                Log.d(TAG, "[Activity meta-data] DESCRIPTION=" + description
                        + ", DIFFICULTY=" + difficultyLevel);
            } else {
                tvActivityDescription.setText("Activity 설명: (meta-data 없음)");
                tvDifficultyLevel.setText("난이도: (meta-data 없음)");
                Log.w(TAG, "Activity meta-data가 null입니다. "
                        + "AndroidManifest.xml의 <activity> 태그 안에 "
                        + "<meta-data>를 선언했는지 확인하세요.");
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Activity meta-data 읽기 실패: " + e.getMessage());
            tvActivityDescription.setText("Activity 설명: (읽기 실패)");
            tvDifficultyLevel.setText("난이도: (읽기 실패)");
        }
    }

    // ============================================================
    // 섹션 3: Service 시작/중지
    // ============================================================

    /**
     * Service 시작/중지 버튼의 클릭 리스너를 설정한다.
     *
     * 포그라운드 서비스 시작 흐름:
     * 1) Android 13+ → POST_NOTIFICATIONS 권한 확인 → 없으면 요청
     * 2) 권한 있음 (또는 Android 12 이하) → startMyForegroundService() 호출
     * 3) MyForegroundService.onStartCommand() 실행
     * 4) 5초 이내에 startForeground() 호출 필수 (안 하면 ANR 발생)
     *
     * Unity 비유:
     *   "서비스 시작" = StartCoroutine(BackgroundWork())
     *   "서비스 중지" = StopCoroutine(BackgroundWork())
     *   단, Android 서비스는 Activity와 독립적으로 실행된다는 점이 다르다.
     *   Activity가 종료되어도 서비스는 계속 실행될 수 있다.
     */
    private void setupServiceButtons() {
        // ── 서비스 시작 버튼 ──
        btnStartService.setOnClickListener(v -> {
            Log.d(TAG, "서비스 시작 버튼 클릭");

            /*
             * Android 13 (API 33, TIRAMISU) 이상에서는
             * POST_NOTIFICATIONS가 런타임 권한으로 변경되었다.
             * 포그라운드 서비스의 알림을 표시하려면 이 권한이 필요하다.
             *
             * Build.VERSION.SDK_INT: 현재 기기의 Android API 레벨
             * Build.VERSION_CODES.TIRAMISU: Android 13 = API 33
             *
             * Unity 비유:
             *   #if UNITY_ANDROID && UNITY_2023_1_OR_NEWER
             *   if (Permission.HasUserAuthorizedPermission("notifications") == false) {
             *       Permission.RequestUserPermission("notifications");
             *   }
             *   #endif
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 현재 권한 상태 확인
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 없으면 요청 팝업을 띄운다
                    Log.d(TAG, "POST_NOTIFICATIONS 권한 요청 시작");
                    notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS);
                    return;  // 권한 결과 콜백에서 서비스 시작
                }
            }

            // 권한이 있거나 Android 12 이하 → 바로 서비스 시작
            startMyForegroundService();
        });

        // ── 서비스 중지 버튼 ──
        btnStopService.setOnClickListener(v -> {
            Log.d(TAG, "서비스 중지 버튼 클릭");
            stopMyForegroundService();
        });
    }

    /**
     * 포그라운드 서비스를 시작하는 메서드.
     *
     * startForegroundService() vs startService():
     * - Android 8+(API 26) 이상에서 포그라운드 서비스를 시작하려면
     *   반드시 startForegroundService()를 사용해야 한다.
     * - startService()를 사용하면 백그라운드 서비스 제한에 의해
     *   IllegalStateException이 발생할 수 있다.
     *
     * ContextCompat.startForegroundService()를 사용하면
     * API 레벨에 따라 자동으로 적절한 메서드를 호출해준다:
     * - API 26+ → startForegroundService()
     * - API 25 이하 → startService()
     *
     * Unity 비유: Platform Dependent Compilation처럼
     *            런타임 환경에 따라 적절한 API를 선택하는 것.
     */
    private void startMyForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        tvServiceStatus.setText("서비스 상태: 실행 중");
        Toast.makeText(this, "포그라운드 서비스가 시작되었습니다", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "MyForegroundService 시작 요청 완료");
    }

    /**
     * 포그라운드 서비스를 중지하는 메서드.
     *
     * stopService()를 호출하면 서비스의 onDestroy()가 호출된다.
     * Unity 비유: Destroy(gameObject)를 호출하면 OnDestroy()가 호출되는 것과 같다.
     */
    private void stopMyForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        stopService(serviceIntent);

        tvServiceStatus.setText("서비스 상태: 중지됨");
        Toast.makeText(this, "포그라운드 서비스가 중지되었습니다", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "MyForegroundService 중지 요청 완료");
    }

    // ============================================================
    // 섹션 4: BroadcastReceiver 등록/해제
    // ============================================================

    /**
     * BroadcastReceiver 등록/해제 버튼의 클릭 리스너를 설정한다.
     *
     * ACTION_TIME_TICK이란?
     * - 시스템이 매 분(60초)마다 보내는 브로드캐스트.
     * - 시계 위젯 등이 이 이벤트를 수신하여 시간을 갱신한다.
     * - Manifest에서는 등록할 수 없고, 코드에서만 registerReceiver()로 등록 가능.
     * - Unity 비유: InvokeRepeating("UpdateClock", 0f, 60f)과 유사하지만,
     *              시스템이 보내주는 이벤트를 구독하는 방식이다.
     *
     * 등록 방식 비교:
     * 1) Manifest 등록 (<receiver> 태그)
     *    - 앱이 꺼져있어도 이벤트를 수신할 수 있다.
     *    - 단, Android 8+에서는 대부분의 implicit broadcast가 Manifest 등록 불가.
     *    - Unity 비유: 씬에 배치된 오브젝트의 이벤트 = 씬이 로드되면 자동으로 활성화.
     *
     * 2) 코드 등록 (registerReceiver)
     *    - Activity/Service가 살아있을 때만 수신.
     *    - 등록한 컴포넌트가 종료되기 전에 반드시 unregisterReceiver() 해야 한다.
     *    - Unity 비유: OnEnable()에서 AddListener(), OnDisable()에서 RemoveListener().
     *
     * ACTION_TIME_TICK은 코드 등록만 가능한 대표적인 예시다.
     */
    private void setupReceiverButtons() {
        // ── 리시버 등록 버튼 ──
        btnRegisterReceiver.setOnClickListener(v -> {
            if (isReceiverRegistered) {
                Toast.makeText(this, "이미 등록되어 있습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "TIME_TICK 리시버 등록 시작");

            // TimeTickReceiver 인스턴스 생성
            // 콜백을 설정하여 tick 이벤트를 Activity에 전달한다
            timeTickReceiver = new TimeTickReceiver();
            timeTickReceiver.setOnTickListener(currentTime -> {
                // 리시버에서 전달받은 시간을 TextView에 표시
                // Unity 비유: 이벤트 콜백에서 UI를 업데이트하는 것과 같다
                tvLastTick.setText("마지막 수신: " + currentTime);
            });

            // IntentFilter = "어떤 종류의 브로드캐스트를 수신할지" 정의
            // Unity 비유: EventTrigger에서 "PointerClick"을 선택하는 것처럼
            //            수신할 이벤트 타입을 지정한다.
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);

            /*
             * registerReceiver() 호출
             *
             * Android 14+ (API 34, UPSIDE_DOWN_CAKE) 이상에서는
             * 리시버 등록 시 RECEIVER_EXPORTED 또는 RECEIVER_NOT_EXPORTED 플래그를
             * 반드시 지정해야 한다.
             *
             * RECEIVER_NOT_EXPORTED:
             *   → 이 리시버는 같은 앱 내부에서 보낸 브로드캐스트만 수신한다.
             *   → 다른 앱에서 보낸 브로드캐스트는 무시한다.
             *   → 보안상 더 안전하다.
             *   → 단, 시스템 브로드캐스트(ACTION_TIME_TICK 등)는 예외적으로 수신 가능.
             *
             * RECEIVER_EXPORTED:
             *   → 다른 앱에서 보낸 브로드캐스트도 수신할 수 있다.
             *
             * Unity 비유: SendMessage() vs BroadcastMessage()의 범위 차이.
             *            NOT_EXPORTED = SendMessage (자기 오브젝트만)
             *            EXPORTED = BroadcastMessage (전체 범위)
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ : RECEIVER_NOT_EXPORTED 플래그 필수
                registerReceiver(timeTickReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                // Android 13 이하 : 플래그 없이 등록
                registerReceiver(timeTickReceiver, filter);
            }

            isReceiverRegistered = true;
            tvReceiverStatus.setText("리시버 상태: 등록됨 (매 분마다 수신)");
            Toast.makeText(this,
                    "TIME_TICK 리시버가 등록되었습니다.\n매 분마다 이벤트를 수신합니다.",
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "TIME_TICK 리시버 등록 완료");
        });

        // ── 리시버 해제 버튼 ──
        btnUnregisterReceiver.setOnClickListener(v -> {
            unregisterTimeTickReceiver();
        });
    }

    /**
     * TimeTickReceiver를 안전하게 해제하는 메서드.
     *
     * 왜 안전하게 해야 하는가?
     * - 이미 해제된 리시버를 다시 해제하면 IllegalArgumentException 발생.
     * - onDestroy()에서도 호출하므로, 버튼으로 먼저 해제한 경우를 대비해야 한다.
     * - isReceiverRegistered 플래그로 중복 해제를 방지한다.
     *
     * Unity 비유:
     *   if (isSubscribed) {
     *       myEvent.RemoveListener(OnEventReceived);
     *       isSubscribed = false;
     *   }
     */
    private void unregisterTimeTickReceiver() {
        if (isReceiverRegistered && timeTickReceiver != null) {
            unregisterReceiver(timeTickReceiver);
            isReceiverRegistered = false;
            tvReceiverStatus.setText("리시버 상태: 미등록");
            tvLastTick.setText("마지막 수신: -");
            Toast.makeText(this, "TIME_TICK 리시버가 해제되었습니다",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "TIME_TICK 리시버 해제 완료");
        } else {
            Toast.makeText(this, "등록된 리시버가 없습니다",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ============================================================
    // Activity 라이프사이클
    // ============================================================

    /**
     * Activity가 종료될 때 호출되는 콜백.
     *
     * 여기서 반드시 리시버를 해제해야 한다.
     * 해제하지 않으면 메모리 누수(memory leak)가 발생하고,
     * 시스템이 "Are you missing a call to unregisterReceiver()?" 경고를 띄운다.
     *
     * Unity 비유: OnDestroy()에서 이벤트 구독을 해제하는 것과 동일.
     *   void OnDestroy() {
     *       if (isSubscribed) {
     *           myEvent.RemoveListener(OnEventReceived);
     *       }
     *   }
     *
     * 서비스는 Activity와 독립적으로 실행되므로,
     * Activity가 종료되어도 서비스는 계속 실행된다.
     * 서비스까지 함께 중지하고 싶다면 여기서 stopService()를 호출하면 된다.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 등록된 리시버가 있으면 해제
        if (isReceiverRegistered && timeTickReceiver != null) {
            unregisterReceiver(timeTickReceiver);
            isReceiverRegistered = false;
            Log.d(TAG, "onDestroy()에서 TIME_TICK 리시버 해제");
        }
    }
}
