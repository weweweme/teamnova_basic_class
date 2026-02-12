import java.util.Random;
import java.io.IOException;

/// <summary>
/// 유틸리티 클래스
/// 게임 로직과 무관한 범용 헬퍼 메서드 모음
/// </summary>
public class Util {

    // ========== 상수 ==========

    // 잘못된 입력을 나타내는 값
    public static final int INVALID_INPUT = -1;

    // ========== 키 상수 ==========

    // 화살표 위
    public static final int KEY_UP = 1000;

    // 화살표 아래
    public static final int KEY_DOWN = 1001;

    // 화살표 왼쪽
    public static final int KEY_LEFT = 1002;

    // 화살표 오른쪽
    public static final int KEY_RIGHT = 1003;

    // Enter 키
    public static final int KEY_ENTER = 1004;

    // q 키 (종료/취소)
    public static final int KEY_QUIT = 1005;

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

    // ========== 터미널 모드 ==========

    /// <summary>
    /// 터미널을 raw 모드로 전환 (키 입력을 즉시 읽기 위해)
    /// Enter 없이 한 글자씩 읽을 수 있게 됨
    /// 프로그램이 비정상 종료되어도 터미널이 복원되도록 안전장치 등록
    /// </summary>
    public static void enableRawMode() {
        // 주의: Process.exec()와 waitFor()는 checked exception이라 try-catch 필수 (컴파일러 요구)
        try {
            // -icanon: 줄 단위 대기 해제 (키 하나씩 즉시 읽기)
            // -echo: 입력한 키가 화면에 표시되지 않도록 함
            Runtime.getRuntime().exec(new String[]{
                "sh", "-c", "stty -icanon -echo < /dev/tty"
            }).waitFor();
        } catch (Exception e) {
            // 단일 스레드 앱에서는 발생하지 않음 (컴파일러 요구사항)
        }

        // 프로그램이 비정상 종료(Ctrl+C 등)되어도 터미널 복원
        Runtime.getRuntime().addShutdownHook(new Thread(() -> disableRawMode()));
    }

    /// <summary>
    /// 터미널을 원래 모드로 복원
    /// </summary>
    public static void disableRawMode() {
        // 주의: Process.exec()와 waitFor()는 checked exception이라 try-catch 필수 (컴파일러 요구)
        try {
            Runtime.getRuntime().exec(new String[]{
                "sh", "-c", "stty sane < /dev/tty"
            }).waitFor();
        } catch (Exception e) {
            // 컴파일러 요구사항
        }
    }

    // ========== 입력 관련 ==========

    /// <summary>
    /// 키 하나를 읽어서 반환
    /// 화살표 키는 KEY_UP/DOWN/LEFT/RIGHT 상수로 반환
    /// Enter는 KEY_ENTER, q는 KEY_QUIT 반환
    /// 그 외 키는 문자 코드 그대로 반환
    /// </summary>
    public static int readKey() {
        // 주의: System.in.read()와 Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
        try {
            int ch = System.in.read();

            // ESC (화살표 키의 시작 바이트)
            if (ch == 27) {
                // 화살표 키는 ESC + [ + 방향 문자가 연속으로 전송됨
                // 잠깐 대기하여 후속 바이트가 도착하도록 함
                Thread.sleep(10);

                if (System.in.available() > 0) {
                    int next = System.in.read();
                    if (next == '[' && System.in.available() > 0) {
                        int arrow = System.in.read();
                        switch (arrow) {
                            case 'A':
                                return KEY_UP;
                            case 'B':
                                return KEY_DOWN;
                            case 'C':
                                return KEY_RIGHT;
                            case 'D':
                                return KEY_LEFT;
                        }
                    }
                }
                // ESC 단독 또는 인식할 수 없는 키 조합
                return INVALID_INPUT;
            }

            // Enter (CR 또는 LF)
            if (ch == 10 || ch == 13) {
                return KEY_ENTER;
            }

            // q 또는 Q → 종료/취소
            if (ch == 'q' || ch == 'Q') {
                return KEY_QUIT;
            }

            // 그 외 → 문자 코드 그대로 반환
            return ch;
        } catch (Exception e) {
            // IOException, InterruptedException 등 (컴파일러 요구사항)
            return INVALID_INPUT;
        }
    }

    /// <summary>
    /// 숫자 키 하나를 읽어서 정수로 반환
    /// 숫자가 아니면 INVALID_INPUT(-1) 반환
    /// </summary>
    public static int readInt() {
        int key = readKey();

        // 숫자 키(0~9)인 경우만 정수로 변환
        if (key >= '0' && key <= '9') {
            return key - '0';
        }
        return INVALID_INPUT;
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
    /// ANSI 이스케이프 코드로 화면을 지우고 커서를 왼쪽 위로 이동
    /// </summary>
    public static void clearScreen() {
        // \033[H: 커서를 왼쪽 위로 이동
        // \033[2J: 화면 전체 지우기
        // \033[3J: 스크롤 버퍼까지 지우기
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
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
