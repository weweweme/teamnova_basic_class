import java.util.Scanner;
import java.util.Random;

/// <summary>
/// 유틸리티 클래스
/// 게임 로직과 무관한 범용 헬퍼 메서드 모음
/// </summary>
public class Util {

    // ========== 상수 ==========

    // 잘못된 입력을 나타내는 값
    public static final int INVALID_INPUT = -1;

    // ========== ANSI 색상 코드 ==========

    // 빨간팀 글자 색상
    public static final String RED = "\033[31m";

    // 파란팀 글자 색상
    public static final String BLUE = "\033[34m";

    // 색상 초기화 (원래 색으로 되돌리기)
    public static final String RESET = "\033[0m";

    // ========== 내부 필드 ==========

    // 랜덤 숫자 생성용
    private static final Random random = new Random();

    // ========== 입력 관련 ==========

    /// <summary>
    /// 정수 입력
    /// 숫자가 아닌 값을 입력하면 INVALID_INPUT(-1) 반환
    /// </summary>
    public static int readInt(Scanner scanner) {
        if (scanner.hasNextInt()) {
            return scanner.nextInt();
        }
        // 숫자가 아닌 입력은 소비하고 버림
        scanner.next();
        return INVALID_INPUT;
    }

    /// <summary>
    /// 체스 좌표 입력 (예: "e2")
    /// 유효한 형식이면 {행, 열} 배열 반환, 아니면 null 반환
    /// </summary>
    public static int[] readPosition(Scanner scanner) {
        String input = scanner.next().trim().toLowerCase();
        return fromNotation(input);
    }

    // ========== 좌표 변환 ==========

    /// <summary>
    /// 내부 좌표(행, 열)를 체스 표기법으로 변환
    /// 예: (6, 4) → "e2"
    /// 행 0이 8번 줄(위), 행 7이 1번 줄(아래)
    /// </summary>
    public static String toNotation(int row, int col) {
        // 열 번호(0~7)를 알파벳(a~h)으로 변환
        char file = (char) ('a' + col);
        // 행 번호(0~7)를 체스 줄 번호(8~1)로 변환
        int rank = 8 - row;
        return "" + file + rank;
    }

    /// <summary>
    /// 체스 표기법을 내부 좌표로 변환
    /// 예: "e2" → {6, 4}
    /// 잘못된 형식이면 null 반환
    /// </summary>
    public static int[] fromNotation(String notation) {
        // 정확히 2글자여야 함 (예: "e2")
        if (notation == null || notation.length() != 2) {
            return null;
        }

        char file = notation.charAt(0);  // 열 알파벳 (a~h)
        char rank = notation.charAt(1);  // 줄 숫자 (1~8)

        // 유효 범위 확인
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }

        // 알파벳을 열 번호로, 숫자를 행 번호로 변환
        int col = file - 'a';
        int row = 8 - (rank - '0');

        return new int[]{row, col};
    }

    // ========== 콘솔 관련 ==========

    /// <summary>
    /// 콘솔 화면 지우기
    /// Grep Console 플러그인이 이 텍스트를 감지하면 콘솔을 비움
    /// </summary>
    public static void clearScreen() {
        System.out.println("/clear");
    }

    /// <summary>
    /// 지정한 시간(밀리초)만큼 대기
    /// </summary>
    public static void delay(int ms) {
        // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // 단일 스레드 앱에서는 발생하지 않음 (컴파일러 요구사항)
        }
    }

    // ========== 기타 ==========

    /// <summary>
    /// 0부터 max-1 사이의 랜덤 정수 반환
    /// </summary>
    public static int rand(int max) {
        return random.nextInt(max);
    }
}
