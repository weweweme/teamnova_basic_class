import java.util.Scanner;

public class Main {
    // 프로그램이 시작되는 main 메서드는 static
    // static 메서드에서는 특정 클래스 인스턴스의 메서드를 호출하거나 static 메서드만 호출할 수 있는데
    // 현재 과제 특성상 큰 복잡성은 필요 없다고 생각해서 다른 클래스를 추가 안하고 실제 과제 구현 내용이 담긴 메서드도 static으로 구현하였다
    // 여기서 사용하려면 멤버가 static이어야 해서 Scanner 객체도 static으로 선언
    // 여러개의 인스턴스를 사용하는 건 메모리 낭비이기 때문
    static Scanner scanner;

    public static void main(String[] args) {
        // Scanner는 Java에서 사용자의 입력을 받기 위한 클래스
        // 스트림이란? 출발지에서 도착점 까지의 데이터 흐름. 단방향이다
        // 자바에서 가장 기본이 되는 입력 스트림은 InputStream
        // System.in은 InputStream 타입의 필드
        scanner = new Scanner(System.in);

        // isRunning: 프로그램 전체 실행 상태를 제어하는 플래그
        // while(true) + break 대신 명시적 플래그를 쓰면 의도가 더 명확함
        boolean isRunning = true;

        // 메뉴 선택 => 기능 실행 => 메뉴 복귀를 반복
        // 사용자가 종료를 선택할 때까지 while로 루프
        while (isRunning) {
            System.out.println("=== 3주차 과제 ===");
            System.out.println("1. 마름모 그리기");
            System.out.println("2. 원 그리기");
            System.out.println("3. 별 그리기");
            System.out.println("4. 유사 스도쿠");
            System.out.println("999. 비운의 실패작 육망성");
            System.out.println("그 외: 종료");
            System.out.print("선택: ");

            // 숫자가 아닌 입력도 받아서 처리해야 하므로 String으로 받음
            String input = scanner.nextLine();

            // switch문은 하나의 변수를 여러 고정값과 비교할 때 적합하다
            // if-else는 특정 조건이나 여러 조건을 &&, ||로 조합할 때 적합하다
            switch (input) {
                case "1":
                    /*
                     * 마름모 그리기
                     *
                     * 정의: 네 변의 길이가 모두 같은 등변 사각형
                     *
                     * 1. 기준: 이걸 콘솔에 그리려면?
                     * => n = 마름모의 반높이 (n=3이면 총 2n-1 = 5줄)
                     *
                     * 2. 아이디어: 상단부(넓어짐)와 하단부(좁아짐)로 나눠서 출력
                     *
                     * 3. 규칙
                     * 각 줄에서 공백 = n-i개, 별 = 2*i-1개
                     * 줄 번호가 커질수록 공백은 줄고, 별은 늘어남
                     * n=3일 때: i=1 => "  *", i=2 => " ***", i=3 => "*****"
                     *
                     * 4. 구현
                     * [입력] 사용자로부터 n 입력
                     * [처리]
                     * 상단부: i를 1부터 n까지 증가시키며 각 줄 출력
                     * 하단부: i를 n-1부터 1까지 감소시키며 각 줄 출력
                     * 각 줄: 공백 n-i개 출력 => 별 2*i-1개 출력 => 줄바꿈
                     * [출력] 완성된 마름모
                     */

                    System.out.println("\n=== 마름모 그리기 ===");
                    System.out.println("0: 메인 메뉴로 돌아가기");
                    System.out.println("숫자 입력: 해당 크기의 마름모 출력\n");

                    while (true) {
                        System.out.print("마름모 크기 입력 (0=돌아가기): ");
                        String inputStr = scanner.nextLine();

                        // 0 입력시 메뉴로 복귀
                        if (inputStr.equals("0")) {
                            System.out.println();
                            break;
                        }

                        // 숫자 검증: 빈 문자열이거나 숫자가 아닌 문자가 있으면 무효
                        boolean isValid = !inputStr.isEmpty();
                        for (int i = 0; i < inputStr.length() && isValid; i++) {
                            if (!Character.isDigit(inputStr.charAt(i))) {
                                isValid = false;
                            }
                        }
                        if (!isValid) {
                            System.out.println("올바른 숫자를 입력해주세요.\n");
                            continue;
                        }

                        int n = Integer.parseInt(inputStr);
                        System.out.println();

                        // 상단부 (i: 1 => n)
                        for (int i = 1; i <= n; i++) {
                            for (int j = 0; j < n - i; j++) {      // 공백 n-i개
                                System.out.print(" ");
                            }
                            for (int j = 0; j < 2 * i - 1; j++) {  // 별 2*i-1개
                                System.out.print("*");
                            }
                            System.out.println();
                        }

                        // 하단부 (i: n-1 => 1), 가운데 줄은 상단부에서 출력했으므로 n-1부터
                        for (int i = n - 1; i >= 1; i--) {
                            for (int j = 0; j < n - i; j++) {      // 공백 n-i개
                                System.out.print(" ");
                            }
                            for (int j = 0; j < 2 * i - 1; j++) {  // 별 2*i-1개
                                System.out.print("*");
                            }
                            System.out.println();
                        }

                        System.out.println();
                    }
                    break;

                case "2":
                    /*
                     * 원 그리기
                     *
                     * 정의: 주어진 점에 이르는 거리가 일정한 점들로 이루어진 평면 도형
                     * => 중심(주어진 점)에서 r(일정한 거리)만큼 떨어진 점들을 찍으면 원이 된다
                     *
                     * 1. 기준: 이걸 콘솔에 그리려면?
                     * => r = 원의 반지름, 중심은 (r, r)로 설정
                     * 예: r=3 입력 => 중심 (3,3), 캔버스 크기 0~6 (2r)
                     *
                     * 2. 규칙
                     * 중심에서 각 점까지의 거리 공식 (피타고라스 정리)
                     * https://m.blog.naver.com/jamogenius/221134715541
                     * 거리² = (x-rx)² + (y-ry)² 공식을 완성시키는 x, y 좌표에 점을 찍어 원을 완성하기
                     *
                     * 3. 구현
                     * [입력] 사용자로부터 r 입력
                     * [처리]
                     * 좌표 전체를 순회하며 각 점에서 중심(r,r)까지의 거리² 계산
                     * 거리²가 r - 0.5 ≤ 거리 ≤ r + 0.5 범위 내인지 판정
                     * - 정확히 r²인 점만 찍으면 정수 좌표 특성상 원이 끊어짐
                     *   하지만 원은 2.5 같은 위치를 지나갈 수 있음
                     *
                     *   0   1   2   ○   3   4   <= 원이 2.5 위치를 지남
                     *           ⬆      ⬆ ️
                     *          2와 3 중 뭘 찍어야 하나?
                     *          둘 다 원에서 0.5 떨어져 있음
                     *
                     *   허용치 < 0.5 => 둘 다 탈락 => 구멍
                     *   허용치 >= 0.5 => 최소 하나 포함 => 연결됨
                     *
                     * - 허용 거리 ±0.5를 양변에 제곱하면 r²-r ≤ 거리 ≤ r²+r (이보다 작으면 끊어지고, 크면 두꺼워짐)
                     * [출력] 범위 내면 '*', 아니면 ' ' 출력
                     */

                    System.out.println("\n=== 원 그리기 ===");
                    System.out.println("0: 메인 메뉴로 돌아가기");
                    System.out.println("숫자 입력: 해당 반지름의 원 출력\n");

                    while (true) {
                        System.out.print("원 반지름 입력 (0=돌아가기): ");
                        String inputStr = scanner.nextLine();

                        // 0 입력시 메뉴로 복귀
                        if (inputStr.equals("0")) {
                            System.out.println();
                            break;
                        }

                        // 숫자 검증: 빈 문자열이거나 숫자가 아닌 문자가 있으면 무효
                        boolean isValid = !inputStr.isEmpty();
                        for (int i = 0; i < inputStr.length() && isValid; i++) {
                            if (!Character.isDigit(inputStr.charAt(i))) {
                                isValid = false;
                            }
                        }
                        if (!isValid) {
                            System.out.println("올바른 숫자를 입력해주세요.\n");
                            continue;
                        }

                        int r = Integer.parseInt(inputStr);

                        // r=1: 현재 코드로는 제대로 안 그려지는 엣지케이스
                        if (r == 1) {
                            System.out.println();
                            System.out.println(" * ");
                            System.out.println("* *");
                            System.out.println(" * ");
                            System.out.println();
                            continue;
                        }

                        System.out.println();

                        // 허용 범위 계산
                        // 예: r=5 => rSquared=25, 범위는 20~30
                        int rSquared = r * r;
                        int rSquaredMin = rSquared - r;  // r²-r (±0.5 허용의 하한)
                        int rSquaredMax = rSquared + r;  // r²+r (±0.5 허용의 상한)

                        // 캔버스 전체를 순회하며 각 점이 원 위에 있는지 판정
                        // 캔버스 크기: 0~2r (중심이 (r,r)이므로 양쪽으로 r씩 필요)
                        for (int y = 0; y <= 2 * r; y++) {
                            for (int x = 0; x <= 2 * r; x++) {
                                // 현재 점 (x,y)에서 중심 (r,r)까지의 거리² 계산
                                // 피타고라스: 거리² = dx² + dy²
                                int dx = x - r;  // x방향 거리
                                int dy = y - r;  // y방향 거리
                                int distanceSquared = dx * dx + dy * dy;

                                // 거리²가 허용 범위 내에 있으면 원 위의 점
                                if (distanceSquared >= rSquaredMin && distanceSquared <= rSquaredMax) {
                                    System.out.print("*");
                                } else {
                                    System.out.print(" ");
                                }
                            }
                            System.out.println();
                        }

                        System.out.println();
                    }
                    break;

                case "3":
                    // TODO: 오각별 구현 예정
                    System.out.println("\n=== 별 그리기 ===");
                    System.out.println("구현 예정입니다.\n");
                    break;

                case "4":
                    /*
                     * 유사 스도쿠
                     *
                     * 1. 기준: n = 격자 크기 (n×n에 0~n-1 배치)
                     *
                     * 2. 아이디어: 각 행의 시작 숫자를 1씩 늘리면 자연스럽게 중복이 사라짐
                     *
                     * 3. 규칙
                     * value = (row + col) % n
                     * row + col: 행 번호만큼 시작점이 밀림
                     * % n: 끝에 도달하면 다시 0으로 돌아감
                     * n=3일 때: 0행 => "0 1 2", 1행 => "1 2 0", 2행 => "2 0 1"
                     *
                     * 4. 구현
                     * [입력] 사용자로부터 n 입력
                     * [처리]
                     * 2중 for문으로 row, col 순회 (0~n-1)
                     * 각 칸의 값 = (row + col) % n 계산
                     * [출력] 각 칸의 값 출력
                     */

                    System.out.println("\n=== 유사 스도쿠 ===");
                    System.out.println("0: 메인 메뉴로 돌아가기");
                    System.out.println("숫자 입력: 해당 크기의 스도쿠 출력\n");

                    while (true) {
                        System.out.print("스도쿠 크기 입력 (0=돌아가기): ");
                        String inputStr = scanner.nextLine();

                        // 0 입력시 메뉴로 복귀
                        if (inputStr.equals("0")) {
                            System.out.println();
                            break;
                        }

                        // 숫자 검증: 빈 문자열이거나 숫자가 아닌 문자가 있으면 무효
                        boolean isValid = !inputStr.isEmpty();
                        for (int i = 0; i < inputStr.length() && isValid; i++) {
                            if (!Character.isDigit(inputStr.charAt(i))) {
                                isValid = false;
                            }
                        }
                        if (!isValid) {
                            System.out.println("올바른 숫자를 입력해주세요.\n");
                            continue;
                        }

                        int n = Integer.parseInt(inputStr);
                        System.out.println();

                        for (int row = 0; row < n; row++) {
                            for (int col = 0; col < n; col++) {
                                // row만큼 시작점이 밀리고, n을 넘으면 0으로 돌아감
                                int value = (row + col) % n;
                                System.out.print(value + " ");
                            }
                            System.out.println();
                        }

                        System.out.println();
                    }
                    break;

                case "999":
                    // 육망성
                    drawHexagram();
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

    /// <summary>
    /// 점 (px, py)가 선분 (x1,y1)-(x2,y2) 위에 있는지 판정
    /// </summary>
    private static boolean isNearLine(int px, int py, int x1, int y1, int x2, int y2) {
        // 직선의 방정식 계수 (두 점 (x1,y1), (x2,y2)를 지나는 직선)
        // 두 점을 지나는 직선: a = y2-y1, b = -(x2-x1), c = 나머지
        int a = y2 - y1;
        int b = -(x2 - x1);
        int c = (x2 - x1) * y1 - (y2 - y1) * x1;

        // 두 점이 같으면 직선이 아님
        int aabbSum = a * a + b * b;
        if (aabbSum == 0) return false;

        // 4 * (ax + by + c)² <= (a² + b²) 비교
        // true: 점이 직선에 충분히 가까움
        // false: 점이 직선에서 멀어서 제외
        int d = a * px + b * py + c;
        return 4 * d * d <= aabbSum;
    }

    /*
     * 육망성 (Hexagram) 그리기
     *
     * 참고: 기하학적으로 별 다각형(star polygon)이 아닌 compound figure
     * 두 개의 정삼각형을 반대 방향으로 겹쳐 만든 6각 별 모양 도형
     *
     * 1. 기준: n = 별의 반높이 (전체 캔버스 2n × 2n)
     *
     * 2. 아이디어: 위로 향하는 삼각형(△)과 아래로 향하는 삼각형(▽)을 겹쳐서 그림
     *
     * 3. 규칙
     * △의 3개 변 + ▽의 3개 변 = 총 6개 변
     * 각 변은 직선이고, 직선도 정수가 아닌 위치를 지나감
     * => 원 그리기와 같은 문제: 딱 직선 위에 있는 정수 점이 별로 없음 => 끊어짐
     * 직선에서 ±0.5 거리 이내인 점도 포함해서 해결
     * 6개 변 중 하나라도 가까우면 * 출력하는 방식
     *
     * 4. 구현
     * [입력] 사용자로부터 n 입력
     * [처리]
     * 두 삼각형의 꼭짓점 좌표 계산
     * 2중 for문으로 (x,y) 좌표 전체 순회 (0~2n)
     * 각 점에서 6개 변까지의 거리 계산
     * [출력] 6개의 변 중 하나라도 범위라고 판단되면 '*', 아니면 ' ' 출력
     */
    private static void drawHexagram() {
        System.out.println("\n=== 육망성 그리기 ===");
        System.out.println("0: 메인 메뉴로 돌아가기");
        System.out.println("숫자 입력: 해당 크기의 육망성 출력\n");

        while (true) {
            System.out.print("육망성 크기 입력 (0=돌아가기): ");
            String inputStr = scanner.nextLine();

            // 0 입력시 메뉴로 복귀
            if (inputStr.equals("0")) {
                System.out.println();
                return;
            }

            // 숫자 검증: 빈 문자열이거나 숫자가 아닌 문자가 있으면 무효
            boolean isValid = !inputStr.isEmpty();
            for (int i = 0; i < inputStr.length() && isValid; i++) {
                if (!Character.isDigit(inputStr.charAt(i))) {
                    isValid = false;
                }
            }
            if (!isValid) {
                System.out.println("올바른 숫자를 입력해주세요.\n");
                continue;
            }

            int n = Integer.parseInt(inputStr);

            // n=1: 별 하나만 출력
            if (n == 1) {
                System.out.println("\n*\n");
                continue;
            }

            System.out.println();

            /*
             * 캔버스: 0~2n, 중심: (n, n)
             *
             * 꼭대기는 맨 위(y=0), 밑변은 중심보다 n/2 아래
             * 꼭대기는 맨 아래(y=2n), 윗변은 중심보다 n/2 위
             *
             * n/2는 두 삼각형이 겹치는 정도 (위쪽 삼각형 밑변과 아래쪽 삼각형 윗변 사이 거리)
             * 값이 작아질수록(n/3, n/4) 두 가로선이 가까워져서 가운데 빈 공간이 좁아짐 => 별 모양이 이상해짐
             */

            // 꼭짓점: 상단(n,0), 좌하단(0, n+n/2), 우하단(2n, n+n/2)
            int t1TopY = 0;
            int t1LeftX = 0;
            int t1LeftY = n + n / 2;
            int t1RightX = 2 * n;
            int t1RightY = n + n / 2;

            // 꼭짓점: 하단(n,2n), 좌상단(0, n-n/2), 우상단(2n, n-n/2)
            int t2BottomY = 2 * n;
            int t2LeftX = 0;
            int t2LeftY = n - n / 2;
            int t2RightX = 2 * n;
            int t2RightY = n - n / 2;

            // 캔버스 전체를 순회하며 각 점이 별 위에 있는지 판정
            // 원 그리기와 같은 원리: 직선에서 0.5 이내인 점을 찍음 (함수 내부에서 처리)
            for (int y = 0; y <= 2 * n; y++) {
                for (int x = 0; x <= 2 * n; x++) {
                    // 현재 점 (x,y)가 6개 변 중 하나라도 가까우면 별 위의 점
                    // △: 꼭대기-좌하단, 꼭대기-우하단, 좌하단-우하단 (3개)
                    // ▽: 꼭대기-좌상단, 꼭대기-우상단, 좌상단-우상단 (3개)
                    boolean onStar =
                        isNearLine(x, y, n, t1TopY, t1LeftX, t1LeftY) ||
                        isNearLine(x, y, n, t1TopY, t1RightX, t1RightY) ||
                        isNearLine(x, y, t1LeftX, t1LeftY, t1RightX, t1RightY) ||
                        isNearLine(x, y, n, t2BottomY, t2LeftX, t2LeftY) ||
                        isNearLine(x, y, n, t2BottomY, t2RightX, t2RightY) ||
                        isNearLine(x, y, t2LeftX, t2LeftY, t2RightX, t2RightY);

                    System.out.print(onStar ? "* " : "  ");
                }
                System.out.println();
            }

            System.out.println();
        }
    }

}
