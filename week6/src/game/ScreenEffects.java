package game;

/// <summary>
/// 화면 효과 관리 (흔들림, 웨이브 경고 등)
/// 게임 로직에서 발동하고, Renderer에서 읽어 화면에 반영
/// </summary>
public class ScreenEffects {

    /// <summary>
    /// 화면 흔들림 시작 시각 (밀리초)
    /// </summary>
    private long shakeStartTime;

    /// <summary>
    /// 화면 흔들림 지속 시간 (밀리초, 0이면 비활성)
    /// </summary>
    private int shakeDuration;

    /// <summary>
    /// 화면 흔들림 강도 (좌우 이동 칸 수)
    /// </summary>
    private int shakeIntensity;

    /// <summary>
    /// 웨이브 경고 표시 지속 시간 (밀리초)
    /// </summary>
    private static final int WAVE_WARNING_DURATION = 2000;

    /// <summary>
    /// 웨이브 경고 시작 시각 (밀리초, 0이면 비활성)
    /// </summary>
    private long waveWarningStartTime;

    /// <summary>
    /// 화면 흔들림 발동
    /// </summary>
    public synchronized void triggerScreenShake(int durationMs, int intensity) {
        this.shakeStartTime = System.currentTimeMillis();
        this.shakeDuration = durationMs;
        this.shakeIntensity = intensity;
    }

    /// <summary>
    /// 현재 흔들림 오프셋 반환 (0이면 흔들림 없음)
    /// 50ms마다 좌우 방향 교대
    /// </summary>
    public synchronized int getScreenShakeOffset() {
        if (shakeDuration == 0) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        boolean expired = elapsed >= shakeDuration;
        if (expired) {
            shakeDuration = 0;
            return 0;
        }

        // 50ms마다 방향 교대 (+intensity, -intensity, ...)
        boolean even = (elapsed / 50) % 2 == 0;
        return even ? shakeIntensity : -shakeIntensity;
    }

    /// <summary>
    /// 수직 흔들림 오프셋 반환 (0이면 흔들림 없음)
    /// 수평 흔들림과 다른 주기(75ms)로 교대하여 불규칙한 진동 생성
    /// </summary>
    public synchronized int getVerticalShakeOffset() {
        if (shakeDuration == 0) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        boolean expired = elapsed >= shakeDuration;
        if (expired) {
            return 0;
        }

        // 75ms마다 방향 교대 (수평과 다른 주기로 불규칙 효과)
        int phase = (int) (elapsed / 75) % 3;
        switch (phase) {
            case 0: return 1;
            case 1: return -1;
            default: return 0;
        }
    }

    /// <summary>
    /// 웨이브 경고 발동 (밤 시작 시 호출)
    /// </summary>
    public synchronized void triggerWaveWarning() {
        this.waveWarningStartTime = System.currentTimeMillis();
    }

    /// <summary>
    /// 웨이브 경고 활성 여부 반환
    /// </summary>
    public synchronized boolean isWaveWarningActive() {
        if (waveWarningStartTime == 0) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - waveWarningStartTime;
        boolean expired = elapsed >= WAVE_WARNING_DURATION;
        if (expired) {
            waveWarningStartTime = 0;
            return false;
        }
        return true;
    }
}
