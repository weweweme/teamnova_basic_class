import java.util.Scanner;

public class Main {
    // scanner를 클래스 레벨로 선언: 여러 메서드에서 공유해야 하므로
    // static인 이유: main과 다른 static 메서드에서 접근해야 함
    static Scanner scanner;

    public static void main(String[] args) {
        // Scanner: 콘솔 입력을 받기 위한 표준 입력 스트림 래퍼
        // System.in을 직접 쓰면 바이트 단위라 불편, Scanner는 토큰 단위로 파싱해줌
        scanner = new Scanner(System.in);

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
                    drawDiamond();
                    break;
                case "2":
                    drawCircle();
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

    // drawDiamond: 마름모 그리기 과제 메서드
    // static인 이유: main에서 직접 호출해야 하므로
    // 반환값 없음: 콘솔 출력만 수행
    private static void drawDiamond() {
        System.out.println("\n=== 마름모 그리기 ===");
        System.out.println("0: 메인 메뉴로 돌아가기");
        System.out.println("숫자 입력: 해당 크기의 마름모 출력\n");

        // 서브메뉴 루프: 0 입력 전까지 반복
        while (true) {
            System.out.print("마름모 크기 입력 (0=돌아가기): ");
            String input = scanner.nextLine();

            // 0 입력시 메인 메뉴로 복귀
            if (input.equals("0")) {
                System.out.println();
                return;
            }

            // 숫자 검증: 모든 문자가 숫자인지 확인
            // try-catch 대신 사전 검증 방식 사용
            // 이유: 예외는 예측 불가능한 외부 요인에만 사용하는 것이 바람직
            boolean isNumber = true;
            for (int i = 0; i < input.length(); i++) {
                // Character.isDigit(): 해당 문자가 0~9인지 확인
                if (!Character.isDigit(input.charAt(i))) {
                    isNumber = false;
                    break;
                }
            }

            // 빈 문자열이거나 숫자가 아닌 경우
            if (!isNumber || input.isEmpty()) {
                System.out.println("올바른 숫자를 입력해주세요.\n");
                continue;
            }

            // 이제 안전하게 파싱 가능
            int n = Integer.parseInt(input);

            // 유효성 검사: 1 이상이어야 마름모 출력 가능
            if (n < 1) {
                System.out.println("1 이상의 숫자를 입력해주세요.\n");
                continue;
            }

            // 마름모 출력
            // n = 반높이 (n=3이면 총 5줄)
            //
            // n=3일 때 구조 분석:
            // i=1: 공백 2개(n-1), 별 1개(2*1-1)  →  "  *"
            // i=2: 공백 1개(n-2), 별 3개(2*2-1)  →  " ***"
            // i=3: 공백 0개(n-3), 별 5개(2*3-1)  →  "*****"
            //
            // 공백이 n-i인 이유:
            // - 마름모는 가운데 정렬이 필요함
            // - 가장 넓은 줄(i=n)은 공백 0개, 가장 좁은 줄(i=1)은 공백 n-1개
            // - 줄 번호 i가 증가할수록 공백은 감소해야 함 → n-i
            System.out.println();

            // 상단부 (1줄 ~ n줄): 별이 1개에서 2n-1개로 증가
            for (int i = 1; i <= n; i++) {
                // 공백 출력: n-i개 (가운데 정렬용)
                // j는 반복 횟수를 세는 카운터 변수
                for (int j = 0; j < n - i; j++) {
                    System.out.print(" ");
                }
                // 별 출력: 2*i-1개 (1, 3, 5, ... 홀수 증가)
                // 2*i-1인 이유: i=1→1개, i=2→3개, i=3→5개 (홀수 수열)
                for (int j = 0; j < 2 * i - 1; j++) {
                    System.out.print("*");
                }
                // 줄바꿈: 한 줄 출력 완료 후 다음 줄로
                System.out.println();
            }

            // 하단부 (n-1줄 ~ 1줄): 별이 2n-3개에서 1개로 감소
            // i를 n-1부터 시작: 가운데 줄은 상단부에서 이미 출력했으므로 제외
            for (int i = n - 1; i >= 1; i--) {
                for (int j = 0; j < n - i; j++) {
                    System.out.print(" ");
                }
                for (int j = 0; j < 2 * i - 1; j++) {
                    System.out.print("*");
                }
                System.out.println();
            }

            System.out.println();
        }
    }

    // drawCircle: 원 그리기 과제 메서드
    // static인 이유: main에서 직접 호출해야 하므로
    private static void drawCircle() {
        // =====================================================
        // 설계 1단계: 출발점
        // 사용자 입력 숫자 = 원의 반지름 r
        // =====================================================
        System.out.println("\n=== 원 그리기 ===");
        System.out.println("0: 메인 메뉴로 돌아가기");
        System.out.println("숫자 입력: 해당 반지름의 원 출력\n");

        while (true) {
            System.out.print("원 반지름 입력 (0=돌아가기): ");
            String input = scanner.nextLine();

            if (input.equals("0")) {
                System.out.println();
                return;
            }

            // 숫자 검증: 모든 문자가 숫자인지 확인
            // try-catch 대신 사전 검증 방식 사용
            // 이유: 예외는 예측 불가능한 외부 요인에만 사용하는 것이 바람직
            boolean isNumber = true;
            for (int i = 0; i < input.length(); i++) {
                // Character.isDigit(): 해당 문자가 0~9인지 확인
                if (!Character.isDigit(input.charAt(i))) {
                    isNumber = false;
                    break;
                }
            }

            // 빈 문자열이거나 숫자가 아닌 경우
            if (!isNumber || input.isEmpty()) {
                System.out.println("올바른 숫자를 입력해주세요.\n");
                continue;
            }

            // 이제 안전하게 파싱 가능
            int r = Integer.parseInt(input);

            if (r < 1) {
                System.out.println("1 이상의 숫자를 입력해주세요.\n");
                continue;
            }

            // =====================================================
            // 설계 2단계: 문제 정의
            // 콘솔에 r 크기의 원을 그리려면?
            // → 어떤 위치에 '*'를 찍어야 할지 판단해야 함
            // =====================================================

            // =====================================================
            // 설계 3단계: 아이디어
            // 콘솔 = 2차원 좌표 격자
            // 각 (x, y) 위치에서 "이 점이 원 위에 있나?" 판단
            // → 있으면 '*', 없으면 ' '
            // =====================================================

            // =====================================================
            // 설계 4단계: 판단 기준
            // "원 위에 있다" = "중심에서의 거리가 반지름과 같다"
            // 거리 공식: √[(x-cx)² + (y-cy)²]
            // 이 거리 ≈ r 이면 '*' 출력
            // =====================================================

            // =====================================================
            // 설계 5단계: 구현
            // - 중심: (r, r) - 출력 영역의 가운데
            // - 좌표 순회: 2중 for문으로 (x, y) 전체 탐색
            // - 판정: 거리 ≈ r 이면 '*', 아니면 공백
            // =====================================================
            System.out.println();

            // 중심 좌표: (cx, cy)
            // r로 설정하는 이유: 0~2r 범위의 가운데에 원을 배치하기 위해
            int cx = r;
            int cy = r;

            // 제곱 비교를 위한 범위 계산 (Math.sqrt 대신 사용)
            // 원래: r - 0.5 <= distance <= r + 0.5
            // 양변 제곱: (r-0.5)² <= distance² <= (r+0.5)²
            //
            // (r - 0.5)² = r² - r + 0.25 ≈ r² - r
            // (r + 0.5)² = r² + r + 0.25 ≈ r² + r
            //
            // int 연산으로 근사하여 Math 없이 처리
            int rSquared = r * r;
            int rSquaredMin = rSquared - r;  // (r - 0.5)²의 근사값
            int rSquaredMax = rSquared + r;  // (r + 0.5)²의 근사값

            // y좌표 순회: 0부터 2r까지 (원의 지름만큼)
            for (int y = 0; y <= 2 * r; y++) {
                // x좌표 순회: 0부터 2r까지
                for (int x = 0; x <= 2 * r; x++) {
                    // 중심에서의 x, y 방향 거리
                    int dx = x - cx;
                    int dy = y - cy;

                    // 거리의 제곱 계산 (Math.pow 대신 직접 곱셈)
                    // dx * dx: (x - cx)²
                    // dy * dy: (y - cy)²
                    int distanceSquared = dx * dx + dy * dy;

                    // 거리² 가 r² 범위 내에 있으면 원 위의 점
                    // Math.sqrt 불필요: 제곱 상태로 비교
                    if (distanceSquared >= rSquaredMin && distanceSquared <= rSquaredMax) {
                        System.out.print("* ");
                    } else {
                        System.out.print("  ");
                    }
                }
                System.out.println();
            }

            System.out.println();
        }
    }
}
