package com.example.week8.timeline;

/// <summary>
/// 타임스탬프(밀리초)를 "n시간 전" / "n일 전" 같은 사람이 읽기 쉬운 문구로 변환하는 헬퍼
///
/// 타임라인의 모든 뷰타입(ViewHolder 4종)이 공통으로 쓰므로 별도 유틸로 분리
/// static 메서드 1개뿐 — 상태 없는 순수 변환 함수라 static 유틸로 둠
/// </summary>
public class TimeAgoFormatter {

    private static final long MINUTE_MS = 60L * 1000L;
    private static final long HOUR_MS = 60L * MINUTE_MS;
    private static final long DAY_MS = 24L * HOUR_MS;

    /// <summary>
    /// 인스턴스화 막기 (static 유틸 클래스)
    /// </summary>
    private TimeAgoFormatter() {
    }

    /// <summary>
    /// 과거 시각(밀리초)을 현재 기준 "방금 전 / n분 전 / n시간 전 / n일 전"으로 변환
    /// </summary>
    public static String format(long pastMillis) {
        long diff = System.currentTimeMillis() - pastMillis;

        if (diff < HOUR_MS) {
            long minutes = diff / MINUTE_MS;
            if (minutes <= 0) {
                return "방금 전";
            }
            return minutes + "분 전";
        }

        if (diff < DAY_MS) {
            long hours = diff / HOUR_MS;
            return hours + "시간 전";
        }

        long days = diff / DAY_MS;
        return days + "일 전";
    }
}
