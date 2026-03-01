package game;

/// <summary>
/// 로그 한 줄의 메시지와 생성 시각을 담는 클래스
/// 생성 후 일정 시간이 지나면 큐에서 제거됨
/// </summary>
public class LogEntry {

    /// <summary>
    /// 로그 메시지 내용
    /// </summary>
    private final String message;

    /// <summary>
    /// 로그가 생성된 시각 (밀리초)
    /// </summary>
    private final long createdTime;

    public LogEntry(String message, long createdTime) {
        this.message = message;
        this.createdTime = createdTime;
    }

    /// <summary>
    /// 메시지 반환
    /// </summary>
    public String getMessage() {
        return message;
    }

    /// <summary>
    /// 생성 시각 반환
    /// </summary>
    public long getCreatedTime() {
        return createdTime;
    }
}
