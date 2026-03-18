package com.example.weak7_1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ============================================================
 * TimeTickReceiver - BroadcastReceiver 데모
 * ============================================================
 *
 * [BroadcastReceiver란?]
 * - 시스템 또는 다른 앱이 보내는 브로드캐스트(방송)를 수신하는 컴포넌트.
 * - 특정 이벤트가 발생했을 때 알림을 받아 처리할 수 있다.
 * - Unity 비유:
 *     Unity의 이벤트 시스템(EventTrigger / UnityEvent / EventListener)과 같다.
 *     특정 이벤트(클릭, 충돌 등)가 발생하면 등록된 콜백(리스너)이 호출되는 패턴.
 *
 *     // Unity 이벤트 구독 패턴
 *     myButton.onClick.AddListener(OnButtonClicked);     // 구독 = registerReceiver()
 *     myButton.onClick.RemoveListener(OnButtonClicked);  // 해제 = unregisterReceiver()
 *
 *     // 또는 C# event 패턴
 *     GameManager.OnGameStart += HandleGameStart;        // 구독
 *     GameManager.OnGameStart -= HandleGameStart;        // 해제
 *
 * [ACTION_TIME_TICK이란?]
 * - 시스템이 매 분(약 60초)마다 보내는 브로드캐스트.
 * - 시계 앱, 위젯 등이 이 이벤트를 수신하여 표시 시간을 갱신한다.
 * - Unity 비유: InvokeRepeating("UpdateClock", 0f, 60f)과 유사하지만,
 *              이건 시스템이 보내주는 외부 이벤트를 "구독"하는 방식이다.
 *              자기가 직접 타이머를 돌리는 게 아니라, 시스템의 타이머를 구독한다.
 *
 * [브로드캐스트 등록 방식 2가지]
 *
 * 1) Manifest 등록 (정적 등록)
 *    - AndroidManifest.xml의 <receiver> 태그에 선언한다.
 *    - 앱이 실행되지 않은 상태에서도 이벤트를 수신할 수 있다.
 *    - 단, Android 8+(API 26)부터 대부분의 implicit broadcast는 Manifest 등록이 불가.
 *    - Unity 비유: 씬에 미리 배치된 오브젝트의 이벤트 핸들러.
 *                 씬이 로드되면 자동으로 활성화되어 이벤트를 받을 준비가 된다.
 *
 *    <예시 - AndroidManifest.xml>
 *    <receiver android:name=".MyReceiver" android:exported="false">
 *        <intent-filter>
 *            <action android:name="android.intent.action.BOOT_COMPLETED"/>
 *        </intent-filter>
 *    </receiver>
 *
 * 2) 코드 등록 (동적 등록) - 이 데모에서 사용하는 방식
 *    - Java/Kotlin 코드에서 registerReceiver()로 런타임에 등록한다.
 *    - Activity/Service가 살아있는 동안만 이벤트를 수신한다.
 *    - 반드시 unregisterReceiver()로 해제해야 한다 (메모리 누수 방지).
 *    - Unity 비유: OnEnable()에서 AddListener(), OnDisable()에서 RemoveListener().
 *
 *    <예시 - Java 코드>
 *    IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
 *    registerReceiver(timeTickReceiver, filter);     // 등록
 *    unregisterReceiver(timeTickReceiver);           // 해제
 *
 * ★ ACTION_TIME_TICK은 코드 등록만 가능하다!
 *   Manifest에 등록해도 시스템이 이 리시버에게 이벤트를 보내지 않는다.
 *   이유: 매 분마다 모든 앱에게 브로드캐스트를 보내면 배터리가 빨리 닳기 때문.
 *         실제로 화면에 시간을 표시하는 앱(Activity가 활성 상태)만 받을 수 있게 제한.
 */
public class TimeTickReceiver extends BroadcastReceiver {

    private static final String TAG = "TimeTickReceiver";

    /**
     * 콜백 인터페이스 - 리시버가 tick 이벤트를 수신했을 때 Activity에 알리기 위한 인터페이스.
     *
     * 왜 인터페이스를 사용하는가?
     * - BroadcastReceiver는 독립적인 컴포넌트이므로, Activity의 UI에 직접 접근할 수 없다.
     * - 인터페이스(콜백)를 통해 느슨한 결합(loose coupling)으로 데이터를 전달한다.
     * - Unity 비유:
     *     C#에서 event/delegate 패턴을 사용하여 컴포넌트 간 통신하는 것과 같다.
     *
     *     // Unity 델리게이트 패턴
     *     public delegate void OnTickDelegate(string time);
     *     public event OnTickDelegate OnTick;
     *
     *     // 또는 UnityAction 사용
     *     public UnityAction<string> onTickCallback;
     *
     *     // 이벤트 발생 시
     *     OnTick?.Invoke(currentTime);
     */
    public interface OnTickListener {
        /**
         * 매 분 TIME_TICK 이벤트가 수신될 때 호출된다.
         * @param currentTime 현재 시각 문자열 (HH:mm:ss 형식)
         */
        void onTick(String currentTime);
    }

    /**
     * 콜백 리스너 참조.
     * MetaDataActivity에서 setOnTickListener()로 설정한다.
     * Unity 비유: 이벤트 핸들러 참조를 저장하는 멤버 변수.
     *            private UnityAction<string> onTickCallback;
     */
    private OnTickListener onTickListener;

    /**
     * 콜백 리스너를 설정하는 메서드.
     * Activity에서 리시버를 생성한 후 이 메서드로 콜백을 등록한다.
     *
     * Unity 비유:
     *   var receiver = new TimeTickReceiver();
     *   receiver.OnTick += (time) => { tvLastTick.text = time; };
     *
     * @param listener tick 이벤트를 수신할 리스너
     */
    public void setOnTickListener(OnTickListener listener) {
        this.onTickListener = listener;
    }

    /**
     * 브로드캐스트를 수신했을 때 호출되는 콜백 메서드.
     *
     * 이것이 BroadcastReceiver의 핵심 메서드다.
     * 시스템이 ACTION_TIME_TICK 브로드캐스트를 보내면,
     * registerReceiver()로 등록된 이 리시버의 onReceive()가 호출된다.
     *
     * Unity 비유:
     *   // UnityEvent의 콜백 메서드
     *   void OnEventReceived() {
     *       Debug.Log("이벤트 수신!");
     *       // UI 업데이트
     *   }
     *
     * ★ 주의사항:
     * - onReceive()는 메인(UI) 스레드에서 실행된다.
     * - 10초 이내에 완료해야 한다. 초과하면 ANR(Application Not Responding) 발생.
     * - 무거운 작업(네트워크, DB 등)은 여기서 직접 하지 말고 Service로 위임해야 한다.
     * - Unity 비유: Update()에서 무거운 작업을 하면 프레임 드롭이 발생하는 것과 같다.
     *              무거운 작업은 코루틴이나 Task로 비동기 처리해야 하는 것과 동일.
     *
     * @param context 리시버가 실행되는 Context (Activity 또는 Application Context)
     * @param intent  수신된 브로드캐스트 Intent (action, extras 등 포함)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // intent.getAction()으로 수신된 브로드캐스트의 종류를 확인한다.
        // ACTION_TIME_TICK이 아닌 다른 브로드캐스트가 올 수도 있으므로 확인이 필요.
        // Unity 비유: OnTriggerEnter(Collider other)에서 other.tag를 확인하는 것.
        if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {

            // 현재 시각을 포맷팅한다
            // SimpleDateFormat: 날짜/시간을 원하는 형식의 문자열로 변환하는 도구
            // Unity 비유: DateTime.Now.ToString("HH:mm:ss")와 동일
            String currentTime = new SimpleDateFormat(
                    "HH:mm:ss", Locale.getDefault()
            ).format(new Date());

            // 로그 출력 - Logcat에서 확인 가능
            // Unity 비유: Debug.Log($"[TimeTickReceiver] 시스템 시간 수신: {currentTime}");
            Log.d(TAG, "ACTION_TIME_TICK 수신! 현재 시각: " + currentTime);

            // Toast = 화면 하단에 잠시 나타났다 사라지는 메시지
            // LENGTH_SHORT: 약 2초간 표시
            // LENGTH_LONG: 약 3.5초간 표시
            // Unity 비유: 화면에 잠시 떴다 사라지는 팝업 텍스트 (DOTween 페이드 애니메이션)
            Toast.makeText(context,
                    "TIME_TICK 수신: " + currentTime,
                    Toast.LENGTH_SHORT).show();

            // 콜백으로 Activity에 현재 시각을 전달한다
            // null 체크: 리스너가 설정되지 않았을 수도 있으므로
            // Unity 비유: OnTick?.Invoke(currentTime);  (null-conditional 호출)
            if (onTickListener != null) {
                onTickListener.onTick(currentTime);
            }
        }
    }
}
