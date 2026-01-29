import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Scanner: 콘솔 입력을 받기 위한 표준 입력 스트림 래퍼
        // System.in을 직접 쓰면 바이트 단위라 불편, Scanner는 토큰 단위로 파싱해줌
        Scanner scanner = new Scanner(System.in);

        // isRunning: 프로그램 전체 실행 상태를 제어하는 플래그
        // while(true) + break 대신 명시적 플래그를 쓰면 의도가 더 명확함
        boolean isRunning = true;

        while (isRunning) {
            // 메뉴 출력: 사용자가 선택지를 알 수 있도록 안내
            System.out.println("=== 3주차 과제 ===");
            System.out.println("1. 마름모 그리기");
            System.out.println("2. 원 그리기");
            System.out.println("3. 별 그리기");
            System.out.println("4. 보너스 (유사 스도쿠)");
            System.out.println("그 외: 종료");
            System.out.print("선택: ");

            // input: 사용자 입력값 저장
            // String 타입인 이유: 숫자가 아닌 입력도 받아서 처리해야 하므로
            String input = scanner.nextLine();

            // switch문: 다중 분기에 적합, if-else 체인보다 가독성 좋음
            switch (input) {
                case "1":
                    // TODO: 마름모 과제
                    break;
                case "2":
                    // TODO: 원 그리기
                    break;
                case "3":
                    // TODO: 별 그리기
                    break;
                case "4":
                    // TODO: 보너스 과제
                    break;
                default:
                    // 1~4 외의 입력은 종료 의사로 간주
                    isRunning = false;
                    System.out.println("프로그램을 종료합니다.");
                    break;
            }
        }

        // 자원 해제: Scanner는 내부적으로 System.in을 잡고 있으므로 닫아줌
        scanner.close();
    }
}
