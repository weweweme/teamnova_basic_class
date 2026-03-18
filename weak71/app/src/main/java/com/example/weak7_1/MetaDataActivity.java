package com.example.weak7_1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.google.android.material.appbar.MaterialToolbar;

/**
 * ============================================================
 * MetaDataActivity - Service / BroadcastReceiver 데모
 * ============================================================
 *
 * 이 Activity에서 학습하는 Android 컴포넌트 2가지:
 *
 * 1) Service (포그라운드 서비스)
 *    - 화면(UI) 없이 백그라운드에서 작업을 수행하는 컴포넌트.
 *    - startForegroundService()로 시작하고, stopService()로 중지한다.
 *    - Unity 비유: DontDestroyOnLoad()로 유지되는 오브젝트에서
 *                  StartCoroutine()으로 백그라운드 작업을 실행하는 것.
 *
 * 2) BroadcastReceiver (방송 수신자)
 *    - 브로드캐스트(방송)를 수신하는 컴포넌트.
 *    - registerReceiver()로 등록, unregisterReceiver()로 해제.
 *    - sendBroadcast()로 직접 브로드캐스트를 보낼 수도 있다.
 *    - Unity 비유: UnityEvent.AddListener()로 이벤트를 구독하고,
 *                  RemoveListener()로 구독을 해제하는 것과 같다.
 */
public class MetaDataActivity extends AppCompatActivity {

    private static final String TAG = "MetaDataActivity";

    /**
     * 커스텀 브로드캐스트 액션.
     * sendBroadcast()로 이 액션을 가진 Intent를 보내면,
     * 같은 액션으로 IntentFilter를 등록한 리시버가 수신한다.
     *
     * Unity 비유: EventManager.TriggerEvent("CUSTOM_TICK") 같은 커스텀 이벤트 이름.
     */
    static final String ACTION_CUSTOM_TICK = "com.example.weak7_1.ACTION_CUSTOM_TICK";

    /** 커스텀 브로드캐스트 발송 간격 (밀리초) */
    private static final long BROADCAST_INTERVAL_MS = 10_000;

    // ── Service 관련 UI ──
    private Button btnStartService;
    private Button btnStopService;
    private TextView tvServiceStatus;
    private TextView tvServiceCounter;

    // ── BroadcastReceiver 관련 UI ──
    private Button btnRegisterReceiver;
    private Button btnUnregisterReceiver;
    private TextView tvReceiverStatus;
    private TextView tvTickCount;
    private TextView tvLastTick;

    private TimeTickReceiver timeTickReceiver;
    private boolean isReceiverRegistered = false;
    private int tickCount = 0;

    /** 서비스 카운터 폴링용 Handler */
    private Handler uiHandler;
    private Runnable counterPollRunnable;

    /** 커스텀 브로드캐스트 발송용 Handler + Runnable */
    private Handler broadcastHandler;
    private Runnable broadcastRunnable;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meta_data);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ── 툴바 설정 ──
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        initNotificationPermissionLauncher();
        setupServiceButtons();
        setupReceiverButtons();
        startCounterPolling();
    }

    private void initViews() {
        btnStartService = findViewById(R.id.btnStartService);
        btnStopService = findViewById(R.id.btnStopService);
        tvServiceStatus = findViewById(R.id.tvServiceStatus);
        tvServiceCounter = findViewById(R.id.tvServiceCounter);

        btnRegisterReceiver = findViewById(R.id.btnRegisterReceiver);
        btnUnregisterReceiver = findViewById(R.id.btnUnregisterReceiver);
        tvReceiverStatus = findViewById(R.id.tvReceiverStatus);
        tvTickCount = findViewById(R.id.tvTickCount);
        tvLastTick = findViewById(R.id.tvLastTick);
    }

    private void initNotificationPermissionLauncher() {
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startMyForegroundService();
                    } else {
                        Toast.makeText(this,
                                "알림 권한이 필요합니다. 설정에서 허용해주세요.",
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // ============================================================
    // Service 시작/중지
    // ============================================================

    private void setupServiceButtons() {
        btnStartService.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS);
                    return;
                }
            }
            startMyForegroundService();
        });

        btnStopService.setOnClickListener(v -> stopMyForegroundService());
    }

    private void startMyForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        tvServiceStatus.setText("서비스 상태: 실행 중");
        Toast.makeText(this, "포그라운드 서비스가 시작되었습니다", Toast.LENGTH_SHORT).show();
    }

    private void stopMyForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        stopService(serviceIntent);

        tvServiceStatus.setText("서비스 상태: 중지됨");
        tvServiceCounter.setText("백그라운드 작업 횟수: 0회");
        Toast.makeText(this, "포그라운드 서비스가 중지되었습니다", Toast.LENGTH_SHORT).show();
    }

    // ============================================================
    // 서비스 상태 실시간 폴링
    // ============================================================

    /**
     * MyForegroundService의 static 변수를 1초마다 읽어와 화면에 표시한다.
     * 서비스가 5초마다 카운터를 증가시키는데, 그 값이 여기에 실시간 반영된다.
     */
    private void startCounterPolling() {
        uiHandler = new Handler(Looper.getMainLooper());
        counterPollRunnable = new Runnable() {
            @Override
            public void run() {
                tvServiceCounter.setText("백그라운드 작업 횟수: "
                        + MyForegroundService.counter + "회");

                if (MyForegroundService.isRunning) {
                    tvServiceStatus.setText("서비스 상태: 실행 중");
                } else {
                    tvServiceStatus.setText("서비스 상태: 중지됨");
                }

                uiHandler.postDelayed(this, 1000);
            }
        };
        uiHandler.postDelayed(counterPollRunnable, 1000);
    }

    // ============================================================
    // BroadcastReceiver 등록/해제
    // ============================================================

    private void setupReceiverButtons() {
        btnRegisterReceiver.setOnClickListener(v -> {
            if (isReceiverRegistered) {
                Toast.makeText(this, "이미 등록되어 있습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1) 리시버 생성 + 콜백 설정
            timeTickReceiver = new TimeTickReceiver();
            timeTickReceiver.setOnTickListener(currentTime -> {
                tickCount++;
                tvTickCount.setText("수신 횟수: " + tickCount);
                tvLastTick.setText("마지막 수신: " + currentTime);
            });

            // 2) 커스텀 액션으로 IntentFilter 등록
            IntentFilter filter = new IntentFilter(ACTION_CUSTOM_TICK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                registerReceiver(timeTickReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(timeTickReceiver, filter);
            }

            isReceiverRegistered = true;
            tvReceiverStatus.setText("리시버 상태: 등록됨 (10초마다 수신)");

            // 3) 10초마다 커스텀 브로드캐스트를 발송하는 타이머 시작
            startBroadcastTimer();

            Toast.makeText(this,
                    "리시버 등록 완료. 10초마다 브로드캐스트를 수신합니다.",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "커스텀 리시버 등록 + 브로드캐스트 타이머 시작");
        });

        btnUnregisterReceiver.setOnClickListener(v -> unregisterTimeTickReceiver());
    }

    /**
     * 10초마다 커스텀 브로드캐스트를 발송하는 타이머를 시작한다.
     *
     * sendBroadcast(intent)로 Intent를 방송하면,
     * 같은 액션(ACTION_CUSTOM_TICK)으로 등록된 모든 리시버가 수신한다.
     *
     * Unity 비유: InvokeRepeating("SendEvent", 0f, 10f)으로
     *            주기적으로 이벤트를 발생시키는 것.
     */
    private void startBroadcastTimer() {
        broadcastHandler = new Handler(Looper.getMainLooper());
        broadcastRunnable = new Runnable() {
            @Override
            public void run() {
                // 커스텀 브로드캐스트 발송
                Intent tickIntent = new Intent(ACTION_CUSTOM_TICK);
                tickIntent.setPackage(getPackageName());
                sendBroadcast(tickIntent);
                Log.d(TAG, "커스텀 브로드캐스트 발송: " + ACTION_CUSTOM_TICK);

                // 10초 후 다시 실행
                broadcastHandler.postDelayed(this, BROADCAST_INTERVAL_MS);
            }
        };
        // 최초 10초 후 시작
        broadcastHandler.postDelayed(broadcastRunnable, BROADCAST_INTERVAL_MS);
    }

    /** 브로드캐스트 타이머를 중지한다. */
    private void stopBroadcastTimer() {
        if (broadcastHandler != null && broadcastRunnable != null) {
            broadcastHandler.removeCallbacks(broadcastRunnable);
        }
    }

    private void unregisterTimeTickReceiver() {
        if (isReceiverRegistered && timeTickReceiver != null) {
            stopBroadcastTimer();
            unregisterReceiver(timeTickReceiver);
            isReceiverRegistered = false;
            tvReceiverStatus.setText("리시버 상태: 미등록");
            Toast.makeText(this, "리시버가 해제되었습니다",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "등록된 리시버가 없습니다",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ============================================================
    // Activity 라이프사이클
    // ============================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 서비스 폴링 중지
        if (uiHandler != null && counterPollRunnable != null) {
            uiHandler.removeCallbacks(counterPollRunnable);
        }

        // 브로드캐스트 타이머 중지
        stopBroadcastTimer();

        // 리시버 해제
        if (isReceiverRegistered && timeTickReceiver != null) {
            unregisterReceiver(timeTickReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
