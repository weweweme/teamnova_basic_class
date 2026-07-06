package com.example.week12.model;

/// <summary>
/// 활동 로그 데이터 모델
/// 타임라인에 표시할 활동 한 건 (게임을 추가/완료/리뷰/플레이한 기록)
///
/// ──── Parcelable을 구현하지 않는 이유 ────
/// Game은 Activity 간 Intent로 주고받아서 Parcelable이 필요했지만,
/// ActivityLog는 현재 TimelineActivity 안에서만 목록으로 표시하고
/// 다른 Activity로 넘기지 않으므로 Parcelable이 필요 없음
/// (필요해지면 그때 Game처럼 Parcelable 추가)
///
/// ──── payload 필드의 의미는 type에 따라 다름 ────
///   ADDED     → 사용 안 함 (빈 문자열)
///   COMPLETED → 사용 안 함 (빈 문자열)
///   REVIEWED  → 리뷰 본문 텍스트
///   PLAYED    → 플레이 시간 텍스트 (예: "3시간 20분")
/// 타입마다 의미가 달라 하나의 String으로 통합 (멀티 뷰타입에서 타입별로 해석)
/// </summary>
public class ActivityLog {

    // ========== 필드 ==========

    /// <summary>
    /// 활동 종류 (추가/완료/리뷰/플레이) — 어떤 뷰타입으로 그릴지 결정
    /// </summary>
    private final ActivityLogType type;

    /// <summary>
    /// 어떤 게임에 대한 로그인지 (Game의 id와 매칭)
    /// 표시할 때 GameRepository.findById로 게임 제목/표지를 가져옴
    /// </summary>
    private final int gameId;

    /// <summary>
    /// 활동이 일어난 시각 (System.currentTimeMillis() 기준 밀리초)
    /// 화면에는 "n시간 전" / 날짜 형태로 변환해 표시
    /// </summary>
    private final long timestamp;

    /// <summary>
    /// 타입별 부가 정보 (REVIEWED=리뷰 본문, PLAYED=플레이 시간, 그 외=빈 문자열)
    /// </summary>
    private final String payload;

    // ========== 생성자 ==========

    /// <summary>
    /// 활동 로그 생성 (모든 필드가 고정값이라 생성자에서 한 번에 받음)
    /// </summary>
    public ActivityLog(ActivityLogType type, int gameId, long timestamp, String payload) {
        this.type = type;
        this.gameId = gameId;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    // ========== Getter ==========

    /// <summary>
    /// 활동 종류 반환
    /// </summary>
    public ActivityLogType getType() {
        return this.type;
    }

    /// <summary>
    /// 대상 게임 ID 반환
    /// </summary>
    public int getGameId() {
        return this.gameId;
    }

    /// <summary>
    /// 활동 시각(밀리초) 반환
    /// </summary>
    public long getTimestamp() {
        return this.timestamp;
    }

    /// <summary>
    /// 타입별 부가 정보 반환
    /// </summary>
    public String getPayload() {
        return this.payload;
    }
}
