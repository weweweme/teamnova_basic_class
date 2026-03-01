package game;

import java.util.Random;

/// <summary>
/// 유틸리티 클래스
/// 게임 로직과 무관한 범용 헬퍼 메서드 모음
/// </summary>
public class Util {

    /// <summary>
    /// 인스턴스 생성 방지 (유틸리티 클래스)
    /// </summary>
    private Util() {}

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

    /// <summary>
    /// 터미널 비프음 출력 (BEL 문자)
    /// </summary>
    public static void beep() {
        System.out.print('\007');
        System.out.flush();
    }
}
