package system;

/// <summary>
/// 낮/밤 주기를 관리하는 스레드
/// 낮(40초)과 밤(20초)을 반복하며, 현재 시간 상태를 제공
/// </summary>
public class DayNightCycle extends Thread {

    /// <summary>
    /// 낮 지속 시간 (밀리초)
    /// </summary>
    private static final int DAY_DURATION = 40000;

    /// <summary>
    /// 밤 지속 시간 (밀리초)
    /// </summary>
    private static final int NIGHT_DURATION = 20000;

    /// <summary>
    /// 틱 간격 (밀리초)
    /// </summary>
    private static final int TICK_DELAY = 500;

    /// <summary>
    /// 현재 일차 (1부터 시작)
    /// </summary>
    private int day;

    /// <summary>
    /// 현재 밤인지 여부 (false면 낮)
    /// </summary>
    private boolean night;

    /// <summary>
    /// 현재 페이즈(낮 또는 밤)에서 경과한 시간 (밀리초)
    /// </summary>
    private long elapsedInPhase;

    /// <summary>
    /// 스레드 실행 여부
    /// </summary>
    private volatile boolean running;

    /// <summary>
    /// 1일차 낮부터 시작
    /// </summary>
    public DayNightCycle() {
        this.day = 1;
        this.night = false;
        this.elapsedInPhase = 0;
        this.running = true;
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        while (running) {
            long now = System.currentTimeMillis();
            long delta = now - lastTime;
            lastTime = now;

            elapsedInPhase += delta;

            // 현재 페이즈 지속 시간 초과 시 전환
            int currentDuration = night ? NIGHT_DURATION : DAY_DURATION;
            if (elapsedInPhase >= currentDuration) {
                elapsedInPhase = 0;

                if (night) {
                    // 밤 → 낮 (새로운 일차)
                    night = false;
                    day++;
                } else {
                    // 낮 → 밤
                    night = true;
                }
            }

            Util.delay(TICK_DELAY);
        }
    }

    /// <summary>
    /// 스레드 안전하게 종료
    /// </summary>
    public void stopRunning() {
        running = false;
    }

    /// <summary>
    /// 현재 일차 반환
    /// </summary>
    public int getDay() {
        return day;
    }

    /// <summary>
    /// 현재 밤인지 확인
    /// </summary>
    public boolean isNight() {
        return night;
    }

    /// <summary>
    /// 현재 페이즈의 남은 시간(초) 반환
    /// </summary>
    public int getRemainingSeconds() {
        int currentDuration = night ? NIGHT_DURATION : DAY_DURATION;
        long remaining = currentDuration - elapsedInPhase;
        if (remaining < 0) {
            remaining = 0;
        }
        return (int) (remaining / 1000);
    }
}
