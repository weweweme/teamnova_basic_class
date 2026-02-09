import java.util.Scanner;

/// <summary>
/// 유틸리티 클래스
/// 게임 로직과 무관한 범용 헬퍼 메서드 모음
/// </summary>
public class Util {

    // ========== 상수 ==========

    public static final int INVALID_INPUT = -1;  // 잘못된 입력 (숫자가 아닌 경우)

    // ========== 콘솔 관련 ==========

    /// <summary>
    /// 콘솔 청소
    /// 빈 줄을 출력하여 이전 내용을 위로 밀어냄
    /// </summary>
    public static void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    /// <summary>
    /// 문자열의 화면 출력 폭 계산
    /// 한글은 2칸, 영문/숫자는 1칸
    /// </summary>
    public static int getDisplayWidth(String str) {
        int width = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 한글 범위: 가(0xAC00) ~ 힣(0xD7A3)
            if (c >= 0xAC00 && c <= 0xD7A3) {
                width += 2;
            } else {
                width += 1;
            }
        }
        return width;
    }

    // ========== 시간 관련 ==========

    /// <summary>
    /// 딜레이 (밀리초)
    /// 게임 연출을 위한 대기 시간
    /// 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
    /// </summary>
    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // 단일 스레드 앱에서는 발생하지 않음 (컴파일러 요구사항)
        }
    }

    /// <summary>
    /// 일정 시간(1.5초) 동안 입력 대기 (입력 감지되면 true 반환)
    ///
    /// [@SuppressWarnings("BusyWait") 사용 이유]
    /// - IDE가 "루프 안에서 Thread.sleep() 호출은 busy-wait 패턴이라 비효율적"이라고 경고함
    /// - busy-wait: 조건이 만족될 때까지 반복문으로 계속 확인하는 방식 (CPU 자원 낭비 가능)
    /// - 의도된 동작이므로 경고를 억제함
    /// </summary>
    @SuppressWarnings("BusyWait")
    public static boolean waitForInput() {
        try {
            // 시작 시간 기록
            long start = System.currentTimeMillis();

            // 1.5초 동안 반복
            while (System.currentTimeMillis() - start < 1500) {

                // 입력 버퍼에 데이터가 있으면
                if (System.in.available() > 0) {

                    // 버퍼 비우기 (입력된 문자들 제거, 반환값은 의도적으로 무시)
                    while (System.in.available() > 0) {
                        int ignored = System.in.read();
                    }
                    return true;  // 입력 감지됨
                }

                // 50ms 대기 (CPU 부하 줄이기)
                Thread.sleep(50);
            }

            return false;  // 1.5초 지남, 입력 없음

        } catch (Exception e) {
            // 단일 스레드 콘솔 앱에서는 발생하지 않음 (컴파일러 요구사항)
            return false;
        }
    }

    // ========== 입력 관련 ==========

    /// <summary>
    /// 정수 입력
    /// 숫자로 변환 가능하면 해당 숫자, 아니면 INVALID_INPUT(-1) 반환
    /// </summary>
    public static int readInt(Scanner scanner) {
        String input = scanner.next();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return INVALID_INPUT;
        }
    }

    // ========== 기타 ==========

    /// <summary>
    /// 랜덤 숫자 (0 ~ max-1)
    /// 간편한 랜덤 생성용
    /// </summary>
    public static int rand(int max) {
        return (int)(Math.random() * max);
    }
}
