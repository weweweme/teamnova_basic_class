package core;

import java.util.Random;

/// <summary>
/// 유틸리티 클래스
/// 게임 로직과 무관한 범용 헬퍼 메서드 모음
/// </summary>
public class Util {

    // ========== 상수 ==========

    // 미설정을 나타내는 값 (커서 없음, 선택 없음, 위치 미지정 등)
    public static final int NONE = -1;

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

        // 터미널 커서 숨기기 (게임에서 커서가 불필요하므로)
        System.out.print("\033[?25l");
        System.out.flush();

        // 프로그램이 비정상 종료(Ctrl+C 등)되어도 터미널 복원
        Runtime.getRuntime().addShutdownHook(new Thread(Util::disableRawMode));
    }

    /// <summary>
    /// 터미널을 원래 모드로 복원
    /// </summary>
    public static void disableRawMode() {
        // 터미널 커서 복원
        System.out.print("\033[?25h");
        System.out.flush();

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
        // ESC 키의 아스키 코드 (화살표 키가 이 코드로 시작됨)
        final int ESC_KEY = 27;
        // Enter 키의 아스키 코드 (운영체제마다 다른 코드를 보냄)
        final int ENTER_UNIX = 10;
        final int ENTER_WINDOWS = 13;
        // 화살표 키 후속 바이트 대기 시간 (밀리초)
        final int ARROW_KEY_DELAY = 10;

        // 주의: System.in.read()와 Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
        try {
            int ch = System.in.read();

            // ESC (화살표 키의 시작 바이트)
            if (ch == ESC_KEY) {
                // 화살표 키는 ESC + [ + 방향 문자가 연속으로 전송됨
                // 잠깐 대기하여 후속 바이트가 도착하도록 함
                Thread.sleep(ARROW_KEY_DELAY);

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

            // Enter 키 (운영체제에 따라 다른 코드가 올 수 있음)
            if (ch == ENTER_UNIX || ch == ENTER_WINDOWS) {
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
