package com.example.weak7_1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * ============================================================
 * MyForegroundService - 포그라운드 서비스 데모
 * ============================================================
 *
 * [Service란?]
 * - 화면(UI) 없이 백그라운드에서 작업을 수행하는 Android 4대 컴포넌트 중 하나.
 * - Activity와 독립적으로 실행된다. Activity가 종료되어도 서비스는 계속 실행 가능.
 * - Unity 비유:
 *     DontDestroyOnLoad()로 씬 전환에도 살아남는 매니저 오브젝트에서
 *     StartCoroutine()으로 백그라운드 작업을 실행하는 것과 같다.
 *     씬(Activity)을 닫아도 코루틴(서비스)은 계속 돌아간다.
 *
 * [포그라운드 서비스(Foreground Service)란?]
 * - 사용자에게 알림(Notification)을 보여주면서 실행되는 서비스.
 * - 시스템이 메모리 부족 시에도 잘 종료하지 않는다 (일반 서비스보다 우선순위 높음).
 * - 예: 음악 재생, GPS 추적, 파일 다운로드 등 사용자가 인지해야 하는 작업.
 * - Unity 비유:
 *     단순한 코루틴이 아니라, 진행 상황을 UI(ProgressBar)로 보여주는 코루틴.
 *     사용자가 "이 작업이 실행 중"이라는 걸 알 수 있다.
 *
 * [NotificationChannel (알림 채널)]
 * - Android 8+(API 26, Oreo)부터 도입된 알림 분류 시스템.
 * - 알림을 "채널" 단위로 그룹화하여 사용자가 채널별로 알림을 켜고 끌 수 있다.
 * - 예: "채팅 알림", "마케팅 알림", "서비스 알림" 등을 별도 채널로 분리.
 * - 채널 없이 알림을 보내면 Android 8+에서는 알림이 표시되지 않는다!
 * - Unity 비유:
 *     Unity의 알림 카테고리(NotificationChannel)와 거의 동일한 개념.
 *     iOS의 UNNotificationCategory, Android의 NotificationChannel이
 *     각각 알림 종류를 분류하는 데 사용된다.
 *
 * [서비스 생명주기]
 * startForegroundService() → onCreate() → onStartCommand() → (작업 수행) → onDestroy()
 *
 * Unity 비유로 매핑:
 *   onCreate()       = Awake() - 초기 설정
 *   onStartCommand() = Start() 또는 OnEnable() - 작업 시작 명령 수신
 *   onDestroy()      = OnDestroy() - 정리(cleanup) 작업
 */
public class MyForegroundService extends Service {

    private static final String TAG = "MyForegroundService";

    /**
     * NotificationChannel ID
     * 알림 채널의 고유 식별자. 알림을 보낼 때 이 ID로 채널을 지정한다.
     * Unity 비유: 이벤트 시스템에서 이벤트 이름(string key)과 같다.
     */
    private static final String CHANNEL_ID = "foreground_service_channel";

    /**
     * 알림에 사용할 고유 ID.
     * startForeground()와 NotificationManager.notify()에 같은 ID를 사용하면
     * 기존 알림을 업데이트할 수 있다 (새 알림이 추가되는 게 아니라 내용만 변경됨).
     */
    private static final int NOTIFICATION_ID = 1001;

    /**
     * Handler - 주기적 작업을 실행하기 위한 핸들러.
     *
     * Handler란?
     * - 메인(UI) 스레드의 메시지 큐에 작업을 예약하는 도구.
     * - postDelayed()로 "N밀리초 후에 이 코드를 실행해줘"라고 예약할 수 있다.
     * - Unity 비유: Invoke("MethodName", delay)와 거의 동일.
     *              또는 코루틴에서 yield return new WaitForSeconds(5f)와 유사.
     *
     * Looper.getMainLooper()를 지정하면 메인(UI) 스레드에서 실행된다.
     * → UI 업데이트(알림 갱신 등)가 안전하게 가능.
     */
    private Handler handler;

    /**
     * Runnable - Handler가 반복 실행할 작업 정의.
     * Unity 비유: Update()에서 매 프레임 실행되는 로직 대신,
     *            일정 간격(5초)마다 실행되는 InvokeRepeating의 콜백 메서드.
     */
    private Runnable counterRunnable;

    /**
     * 카운터 - 서비스가 시작된 후 5초마다 증가하는 값.
     * 알림에 표시하여 서비스가 실행 중임을 사용자에게 보여준다.
     *
     * static으로 선언하여 Activity에서 MyForegroundService.counter로 읽을 수 있다.
     * Unity 비유: static int counter처럼 외부에서 직접 참조 가능한 값.
     */
    static int counter = 0;

    /**
     * 서비스 실행 여부 플래그.
     * Activity에서 MyForegroundService.isRunning으로 확인한다.
     */
    static boolean isRunning = false;

    /**
     * NotificationManager - 알림을 시스템에 표시/업데이트/취소하는 관리자.
     * Unity 비유: NotificationCenter 같은 알림 관리 싱글턴.
     */
    private NotificationManager notificationManager;

    // ============================================================
    // 서비스 생명주기 콜백
    // ============================================================

    /**
     * 서비스가 처음 생성될 때 호출된다 (1회만).
     * startForegroundService()를 여러 번 호출해도 onCreate()는 최초 1회만 호출되고,
     * 이후에는 onStartCommand()만 반복 호출된다.
     *
     * Unity 비유: Awake() - 오브젝트가 생성될 때 최초 1회 호출.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() - 서비스 생성됨");

        // NotificationManager 가져오기
        // getSystemService()로 Android 시스템 서비스를 가져온다.
        // Unity 비유: FindObjectOfType<NotificationManager>()와 유사.
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Handler 초기화 (메인 스레드에서 실행)
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * startForegroundService() 또는 startService()가 호출될 때마다 실행된다.
     * 서비스에 작업 시작 명령이 전달되는 시점.
     *
     * ★ 중요: startForegroundService()로 시작했으면
     *         5초 이내에 startForeground()를 반드시 호출해야 한다!
     *         호출하지 않으면 ANR(Application Not Responding) 에러가 발생한다.
     *         Unity 비유: 코루틴에서 5초 이내에 yield return을 하지 않으면
     *                    프레임이 멈추는 것(freezing)과 유사한 심각한 에러.
     *
     * @param intent  서비스를 시작할 때 전달된 Intent (추가 데이터 포함 가능)
     * @param flags   시작 플래그 (시스템이 서비스를 재시작할 때 사용)
     * @param startId 시작 요청의 고유 ID (여러 번 시작 시 구분용)
     * @return START_STICKY: 시스템이 서비스를 종료한 후 리소스가 확보되면 자동 재시작
     *         START_NOT_STICKY: 재시작하지 않음
     *         START_REDELIVER_INTENT: 재시작 시 마지막 Intent를 다시 전달
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() - 서비스 시작 명령 수신");

        // ── Step 1: NotificationChannel 생성 ──
        // Android 8+(API 26) 이상에서는 알림 채널이 필수다.
        // 채널이 없으면 알림이 표시되지 않는다!
        createNotificationChannel();

        // ── Step 2: 알림(Notification) 빌드 ──
        Notification notification = buildNotification("서비스 실행 중... (카운터: " + counter + ")");

        // ── Step 3: 포그라운드 서비스로 전환 ──
        // startForeground()를 호출하면:
        // 1) 알림이 상태바에 표시된다
        // 2) 서비스가 포그라운드 상태로 전환된다 (시스템이 잘 종료하지 않음)
        // 3) 사용자가 "이 앱이 백그라운드에서 실행 중"이라는 걸 알 수 있다
        //
        // Unity 비유: 로딩 화면에 ProgressBar를 표시하면서 백그라운드 로드를 실행하는 것.
        //            사용자에게 "작업 중"이라고 알려주면서 작업을 계속하는 패턴.
        //
        // Android 14(API 34)부터 startForeground()에 서비스 타입을 명시해야 한다.
        // Manifest의 foregroundServiceType과 일치해야 한다.
        // 타입 없이 호출하면 MissingForegroundServiceTypeException 크래시 발생!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        Log.d(TAG, "startForeground() 호출 완료 - 포그라운드 상태로 전환됨");

        // 실행 중 플래그 설정
        isRunning = true;

        // ── Step 4: 주기적 카운터 업데이트 시작 ──
        startCounter();

        /*
         * START_STICKY 반환:
         * - 시스템이 메모리 부족으로 서비스를 종료한 후,
         *   메모리가 확보되면 자동으로 서비스를 재시작한다.
         * - 단, 재시작 시 intent는 null로 전달된다.
         * - 음악 재생 같이 계속 실행되어야 하는 서비스에 적합.
         * - Unity 비유: 게임이 백그라운드로 갔다가 다시 돌아올 때
         *              자동으로 코루틴을 재개하는 것.
         */
        return START_STICKY;
    }

    /**
     * bindService()로 바인딩할 때 호출된다.
     * 이 서비스는 바인딩을 사용하지 않으므로 null을 반환한다.
     *
     * Bound Service란?
     * - Activity와 서비스가 직접 통신(메서드 호출)할 수 있는 방식.
     * - Unity 비유: GetComponent<T>()로 다른 컴포넌트의 메서드를 직접 호출하는 것.
     * - 여기서는 사용하지 않으므로 null 반환 (Started Service 패턴만 사용).
     */
    @Override
    public IBinder onBind(Intent intent) {
        // 바인딩 미사용 → null 반환
        return null;
    }

    /**
     * 서비스가 종료될 때 호출된다.
     * stopService() 또는 stopSelf()가 호출되면 실행된다.
     *
     * 여기서 반드시 리소스를 정리(cleanup)해야 한다:
     * - Handler의 콜백 제거 (메모리 누수 방지)
     * - 타이머, 리스너, 연결 등 해제
     *
     * Unity 비유: OnDestroy()에서 StopAllCoroutines(), CancelInvoke() 호출.
     *   void OnDestroy() {
     *       StopAllCoroutines();    // 모든 코루틴 중지
     *       CancelInvoke();         // 모든 Invoke 취소
     *   }
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() - 서비스 종료됨. 카운터 최종값: " + counter);

        // Handler에 예약된 모든 콜백을 제거한다.
        // 제거하지 않으면 서비스가 종료된 후에도 콜백이 실행되어 크래시가 발생할 수 있다.
        // Unity 비유: CancelInvoke("IncrementCounter") 호출.
        if (handler != null) {
            handler.removeCallbacks(counterRunnable);
            Log.d(TAG, "Handler 콜백 제거 완료");
        }

        // 카운터 초기화 + 실행 플래그 해제
        counter = 0;
        isRunning = false;
    }

    // ============================================================
    // NotificationChannel 생성
    // ============================================================

    /**
     * NotificationChannel을 생성한다.
     *
     * [NotificationChannel이란?]
     * - Android 8+(API 26, Oreo)부터 도입된 알림 분류 시스템.
     * - 모든 알림은 반드시 하나의 채널에 속해야 한다.
     * - 사용자가 설정 앱에서 채널별로 알림을 켜고 끌 수 있다.
     * - 예: 카카오톡에서 "메시지 알림"과 "광고 알림"을 별도 채널로 분리.
     *
     * Unity 비유:
     *   Unity의 Mobile Notifications 패키지에서도
     *   AndroidNotificationChannel을 먼저 등록하고,
     *   그 채널 ID로 알림을 보내는 것과 정확히 같은 패턴이다.
     *
     * 채널은 한 번 생성하면 앱을 삭제하기 전까지 유지된다.
     * createNotificationChannel()을 여러 번 호출해도 이미 존재하면 무시된다.
     * → 그래서 매번 호출해도 안전하다.
     */
    private void createNotificationChannel() {
        // NotificationChannel은 API 26+ 전용이므로 버전 체크
        // minSdk가 27이므로 사실 항상 실행되지만, 명시적으로 체크하는 것이 좋은 습관.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,                               // 채널 고유 ID
                    "포그라운드 서비스 채널",                      // 사용자에게 표시되는 채널 이름
                    NotificationManager.IMPORTANCE_LOW         // 중요도: LOW = 소리 없음, 상태바에만 표시
            );

            // 채널 설명 - 사용자가 설정 앱에서 채널을 볼 때 표시되는 설명
            channel.setDescription("포그라운드 서비스 실행 중 알림을 표시하는 채널입니다.");

            /*
             * 중요도(Importance) 레벨:
             * IMPORTANCE_HIGH    → 화면 상단에 팝업(헤드업) + 소리 + 진동
             * IMPORTANCE_DEFAULT → 소리 + 진동 (기본값)
             * IMPORTANCE_LOW     → 소리 없음, 상태바에만 조용히 표시
             * IMPORTANCE_MIN     → 상태바에도 최소한으로 표시
             * IMPORTANCE_NONE    → 차단됨
             *
             * 포그라운드 서비스 알림은 보통 LOW를 사용한다.
             * → 사용자에게 "실행 중"을 알리되, 방해하지 않도록.
             */

            // 시스템에 채널 등록
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "NotificationChannel 생성 완료: " + CHANNEL_ID);
        }
    }

    // ============================================================
    // 알림(Notification) 빌드
    // ============================================================

    /**
     * 포그라운드 서비스에 표시할 알림을 빌드한다.
     *
     * NotificationCompat.Builder를 사용하면 하위 버전 호환성이 자동으로 처리된다.
     * Unity 비유: 직접 #if UNITY_ANDROID ... #endif 분기를 하는 대신
     *            호환성 래퍼를 사용하는 것.
     *
     * @param contentText 알림에 표시할 텍스트
     * @return 빌드된 Notification 객체
     */
    private Notification buildNotification(String contentText) {
        // PendingIntent = "나중에 실행할 Intent"를 감싸는 래퍼.
        // 알림을 클릭했을 때 MetaDataActivity를 열도록 설정한다.
        // FLAG_IMMUTABLE: Intent의 내용을 변경할 수 없게 잠금 (Android 12+ 필수).
        // Unity 비유: 콜백을 미리 등록해두고, 사용자가 알림을 탭하면 실행되는 것.
        Intent notificationIntent = new Intent(this, MetaDataActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("포그라운드 서비스 실행 중")    // 알림 제목
                .setContentText(contentText)                   // 알림 내용
                .setSmallIcon(android.R.drawable.ic_media_play) // 상태바 아이콘 (시스템 기본 아이콘 사용)
                .setContentIntent(pendingIntent)               // 알림 클릭 시 실행할 Intent
                .setOngoing(true)                              // 사용자가 스와이프로 지울 수 없음
                .setSilent(true)                               // 소리/진동 없음
                .build();

        /*
         * setOngoing(true)
         * → 알림을 "지속적"으로 표시한다. 사용자가 스와이프로 지울 수 없다.
         *   서비스를 중지해야만 알림이 사라진다.
         *   Unity 비유: 닫기 버튼 없는 모달 팝업.
         *
         * setSilent(true)
         * → 알림이 갱신될 때마다 소리/진동이 발생하지 않는다.
         *   5초마다 알림을 업데이트하므로, 매번 소리가 나면 사용자가 짜증남.
         */
    }

    // ============================================================
    // 주기적 카운터 업데이트
    // ============================================================

    /**
     * 5초마다 카운터를 증가시키고 알림을 업데이트하는 작업을 시작한다.
     *
     * Handler.postDelayed() 패턴:
     * - handler.postDelayed(runnable, delayMillis)
     * - delayMillis 후에 runnable을 실행한다.
     * - Runnable 안에서 다시 postDelayed()를 호출하면 반복 실행이 된다.
     * - Unity 비유:
     *     // InvokeRepeating과 유사
     *     InvokeRepeating("IncrementCounter", 5f, 5f);
     *
     *     // 또는 코루틴 패턴과 유사
     *     IEnumerator CounterCoroutine() {
     *         while (true) {
     *             counter++;
     *             UpdateUI();
     *             yield return new WaitForSeconds(5f);
     *         }
     *     }
     */
    private void startCounter() {
        // 기존에 예약된 콜백이 있으면 제거 (중복 실행 방지)
        if (counterRunnable != null) {
            handler.removeCallbacks(counterRunnable);
        }

        counterRunnable = new Runnable() {
            @Override
            public void run() {
                counter++;
                Log.d(TAG, "카운터 증가: " + counter);

                // 알림 내용을 업데이트한다.
                // 같은 NOTIFICATION_ID로 notify()를 호출하면
                // 새 알림이 추가되는 게 아니라 기존 알림의 내용만 변경된다.
                // Unity 비유: UI Text 컴포넌트의 text 속성을 업데이트하는 것.
                Notification updatedNotification = buildNotification(
                        "서비스 실행 중... (카운터: " + counter + ")");
                notificationManager.notify(NOTIFICATION_ID, updatedNotification);

                // 5초 후에 자기 자신을 다시 실행 → 무한 반복
                // Unity 비유: Invoke("IncrementCounter", 5f) 를 콜백 안에서 다시 호출
                handler.postDelayed(this, 5000);
            }
        };

        // 최초 5초 후에 시작
        handler.postDelayed(counterRunnable, 5000);
        Log.d(TAG, "카운터 시작 - 5초 간격으로 증가합니다");
    }
}
