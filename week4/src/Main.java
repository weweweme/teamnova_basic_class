import java.util.Scanner;

public class Main {

    // ========== 상수 ==========

    static final int INIT_MONEY = 50000000;           // 초기 자본 5000만원
    static final int GOAL_MONEY = 300000000;          // 목표 금액 3억원
    static final int MAX_SLOT = 30;                   // 매대 최대 슬롯
    static final int DEFAULT_PRICE_MULTIPLIER = 3;   // 기본 가격 배율

    // ========== 게임 변수 ==========

    static int money;
    static int goalMoney;
    static int priceMultiplier;
    static int usedSlot = 0;
    static int day = 1;
    static boolean isMorning = true;  // true: 아침, false: 오후

    static Scanner scanner = new Scanner(System.in);

    // ========== 카테고리별 자동주문 정책 ==========
    // 각 카테고리의 자동주문 활성화 여부와 임계값

    // 활성화 여부 (기본 true - 모든 카테고리 자동주문 활성화)
    static boolean autoOrderDrink = true;       // 음료
    static boolean autoOrderBeer = true;        // 맥주
    static boolean autoOrderSoju = true;        // 소주
    static boolean autoOrderSnack = true;       // 간식/안주
    static boolean autoOrderMeat = true;        // 고기
    static boolean autoOrderBeach = true;       // 해수욕 용품
    static boolean autoOrderGrocery = true;     // 식재료
    static boolean autoOrderRamen = true;       // 라면
    static boolean autoOrderIcecream = true;    // 아이스크림
    static boolean autoOrderEtc = true;         // 기타

    // 임계값 (재고가 이 값 이하면 자동주문, 기본 10개)
    static int thresholdDrink = 10;
    static int thresholdBeer = 10;
    static int thresholdSoju = 10;
    static int thresholdSnack = 10;
    static int thresholdMeat = 10;
    static int thresholdBeach = 10;
    static int thresholdGrocery = 10;
    static int thresholdRamen = 10;
    static int thresholdIcecream = 10;
    static int thresholdEtc = 10;

    // ========== 상품 객체 ==========
    // initProducts()에서 초기화

    // 음료
    static Product cola;
    static Product cider;
    static Product water;
    static Product pocari;
    static Product ipro;

    // 맥주
    static Product cass;
    static Product terra;
    static Product hite;

    // 소주
    static Product chamisul;
    static Product cheumcherum;
    static Product jinro;

    // 간식/안주
    static Product driedSquid;
    static Product peanut;
    static Product chip;

    // 고기
    static Product samgyupsal;
    static Product moksal;
    static Product sausage;

    // 해수욕 용품
    static Product tube;
    static Product sunscreen;
    static Product beachBall;

    // 식재료
    static Product ssamjang;
    static Product lettuce;
    static Product kimchi;

    // 라면
    static Product shinRamen;
    static Product jinRamen;
    static Product neoguri;

    // 아이스크림
    static Product melona;
    static Product screwBar;
    static Product fishBread;

    // 기타
    static Product firework;

    public static void main(String[] args) {

        // 게임 시작 화면 출력
        printStartScreen();

        int startChoice = scanner.nextInt();

        if (startChoice == 1) {
            // 기본 모드
            money = INIT_MONEY;
            goalMoney = GOAL_MONEY;
            priceMultiplier = DEFAULT_PRICE_MULTIPLIER;

        } else if (startChoice == 2) {
            // 커스텀 모드
            System.out.println();
            System.out.println("========================================");
            System.out.println("         [ 커스텀 게임 설정 ]");
            System.out.println("========================================");

            System.out.print("시작 자본 입력 (만원 단위): ");
            int inputMoney = scanner.nextInt();
            money = inputMoney * 10000;

            System.out.print("목표 금액 입력 (만원 단위): ");
            int inputGoal = scanner.nextInt();
            goalMoney = inputGoal * 10000;

            System.out.print("가격 배율 입력 (1~100, 판매가에만 적용): ");
            priceMultiplier = scanner.nextInt();

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.println("시작 자본: " + String.format("%,d", money) + "원");
            System.out.println("목표 금액: " + String.format("%,d", goalMoney) + "원");
            System.out.println("가격 배율: " + priceMultiplier + "배");
            System.out.println("----------------------------------------");

        } else {
            // 종료
            System.out.println("게임을 종료합니다.");
            scanner.close();
            return;
        }

        // 상품 초기화 (배율 적용)
        initProducts();

        // ========== 게임 루프 ==========

        boolean playing = true;

        while (playing) {

            // 승리/패배 조건 체크 (게임 종료 시 true 반환)
            if (checkWinOrLose()) {
                break;
            }

            // 하루 시작 메뉴 출력
            printDailyMenu();

            int choice = scanner.nextInt();

            if (choice == 1) {
                // 도매상
                if (isMorning) {
                    goWholesaler();
                    isMorning = false;  // 도매상 갔다오면 오후로 전환
                } else {
                    System.out.println("[!!] 도매상은 오전에만 이용 가능합니다.");
                }

            } else if (choice == 2) {
                // 영업 시작
                startBusiness();
                day++;              // 다음 날로
                isMorning = true;   // 아침으로 리셋

            } else if (choice == 3) {
                // 재고 확인
                showInventory();

            } else if (choice == 4) {
                // 매대 관리
                manageDisplay();

            } else if (choice == 0) {
                playing = false;

            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }

        System.out.println("게임을 종료합니다.");
        scanner.close();
    }

    // ========== 콘솔 청소 ==========
    // 빈 줄을 출력하여 이전 내용을 위로 밀어냄
    // IDE 콘솔에서도 작동하는 방식

    static void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    // ========== 딜레이 (밀리초) ==========
    // 게임 연출을 위한 대기 시간

    static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // 무시
        }
    }

    // ========== 랜덤 숫자 (0 ~ max-1) ==========
    // 간편한 랜덤 생성용

    static int rand(int max) {
        return (int)(Math.random() * max);
    }

    // ========== 게임 시작 화면 출력 ==========

    static void printStartScreen() {
        System.out.println("========================================");
        System.out.println("     _____                      ");
        System.out.println("    / ____|                     ");
        System.out.println("   | (___  _   _ _ __   ___ _ __ ");
        System.out.println("    \\___ \\| | | | '_ \\ / _ \\ '__|");
        System.out.println("    ____) | |_| | |_) |  __/ |   ");
        System.out.println("   |_____/ \\__,_| .__/ \\___|_|   ");
        System.out.println("                | |              ");
        System.out.println("                |_|   @ Gangneung");
        System.out.println("========================================");
        System.out.println("목표: 돈을 모아서 펜션 주인이 되자!");
        System.out.println();
        System.out.println("[1] 게임 시작 (기본: 5000만원 -> 3억원, 배율 3배)");
        System.out.println("[2] 커스텀 게임 (자본/목표/배율 직접 설정)");
        System.out.println("[그 외] 종료");
        System.out.print(">> ");
    }

    // ========== 하루 시작 메뉴 출력 ==========

    static void printDailyMenu() {
        clearScreen();
        System.out.println("========================================");
        if (isMorning) {
            System.out.println("          [  " + day + "일차 - 아침  ]");
        } else {
            System.out.println("          [  " + day + "일차 - 오후  ]");
        }
        System.out.println("========================================");
        System.out.println("현재 자본: " + String.format("%,d", money) + "원");
        System.out.println("매대 현황: " + usedSlot + " / " + MAX_SLOT + "칸");
        System.out.println();

        if (isMorning) {
            // 아침: 도매상, 영업, 재고 확인, 매대 관리 모두 가능
            System.out.println("[1] 도매상 가기 (상품 입고)");
            System.out.println("[2] 영업 시작");
            System.out.println("[3] 현재 재고 확인");
            System.out.println("[4] 매대 관리");
        } else {
            // 오후: 영업, 재고 확인, 매대 관리만 가능
            System.out.println("[1] (도매상 마감)");
            System.out.println("[2] 영업 시작");
            System.out.println("[3] 현재 재고 확인");
            System.out.println("[4] 매대 관리");
        }
        System.out.println("[0] 게임 종료");
        System.out.print(">> ");
    }

    // ========== 승리/패배 조건 체크 ==========
    // 게임 종료 시 true 반환

    static boolean checkWinOrLose() {
        // 승리 조건 체크
        if (money >= goalMoney) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("         *** 축하합니다! ***");
            System.out.println("========================================");
            System.out.println("목표 금액 " + String.format("%,d", goalMoney) + "원을 달성했습니다!");
            System.out.println("이제 펜션 주인이 되셨습니다!");
            System.out.println("총 " + day + "일 만에 달성!");
            System.out.println("========================================");
            return true;
        }

        // 패배 조건 체크
        if (money <= 0) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("          *** 파산 ***");
            System.out.println("========================================");
            System.out.println("자본금이 바닥났습니다...");
            System.out.println(day + "일차에 파산했습니다.");
            System.out.println("========================================");
            return true;
        }

        return false;
    }

    // ========== 재고 확인 ==========

    static void showInventory() {
        clearScreen();
        System.out.println("========================================");
        System.out.printf("       [ 매대 현황 ] %d / %d칸%n", usedSlot, MAX_SLOT);
        System.out.println("========================================");
        System.out.println();

        // 재고가 있는 상품만 막대그래프로 출력
        boolean hasStock = false;

        if (cola.displayStock > 0) {
            printStockBar(cola.name, cola.displayStock);
            hasStock = true;
        }
        if (cider.displayStock > 0) {
            printStockBar(cider.name, cider.displayStock);
            hasStock = true;
        }
        if (water.displayStock > 0) {
            printStockBar(water.name, water.displayStock);
            hasStock = true;
        }
        if (pocari.displayStock > 0) {
            printStockBar(pocari.name, pocari.displayStock);
            hasStock = true;
        }
        if (ipro.displayStock > 0) {
            printStockBar(ipro.name, ipro.displayStock);
            hasStock = true;
        }
        if (cass.displayStock > 0) {
            printStockBar(cass.name, cass.displayStock);
            hasStock = true;
        }
        if (terra.displayStock > 0) {
            printStockBar(terra.name, terra.displayStock);
            hasStock = true;
        }
        if (hite.displayStock > 0) {
            printStockBar(hite.name, hite.displayStock);
            hasStock = true;
        }
        if (chamisul.displayStock > 0) {
            printStockBar(chamisul.name, chamisul.displayStock);
            hasStock = true;
        }
        if (cheumcherum.displayStock > 0) {
            printStockBar(cheumcherum.name, cheumcherum.displayStock);
            hasStock = true;
        }
        if (jinro.displayStock > 0) {
            printStockBar(jinro.name, jinro.displayStock);
            hasStock = true;
        }
        if (driedSquid.displayStock > 0) {
            printStockBar(driedSquid.name, driedSquid.displayStock);
            hasStock = true;
        }
        if (peanut.displayStock > 0) {
            printStockBar(peanut.name, peanut.displayStock);
            hasStock = true;
        }
        if (chip.displayStock > 0) {
            printStockBar(chip.name, chip.displayStock);
            hasStock = true;
        }
        if (samgyupsal.displayStock > 0) {
            printStockBar(samgyupsal.name, samgyupsal.displayStock);
            hasStock = true;
        }
        if (moksal.displayStock > 0) {
            printStockBar(moksal.name, moksal.displayStock);
            hasStock = true;
        }
        if (sausage.displayStock > 0) {
            printStockBar(sausage.name, sausage.displayStock);
            hasStock = true;
        }
        if (tube.displayStock > 0) {
            printStockBar(tube.name, tube.displayStock);
            hasStock = true;
        }
        if (sunscreen.displayStock > 0) {
            printStockBar(sunscreen.name, sunscreen.displayStock);
            hasStock = true;
        }
        if (beachBall.displayStock > 0) {
            printStockBar(beachBall.name, beachBall.displayStock);
            hasStock = true;
        }
        if (ssamjang.displayStock > 0) {
            printStockBar(ssamjang.name, ssamjang.displayStock);
            hasStock = true;
        }
        if (lettuce.displayStock > 0) {
            printStockBar(lettuce.name, lettuce.displayStock);
            hasStock = true;
        }
        if (kimchi.displayStock > 0) {
            printStockBar(kimchi.name, kimchi.displayStock);
            hasStock = true;
        }
        if (shinRamen.displayStock > 0) {
            printStockBar(shinRamen.name, shinRamen.displayStock);
            hasStock = true;
        }
        if (jinRamen.displayStock > 0) {
            printStockBar(jinRamen.name, jinRamen.displayStock);
            hasStock = true;
        }
        if (neoguri.displayStock > 0) {
            printStockBar(neoguri.name, neoguri.displayStock);
            hasStock = true;
        }
        if (melona.displayStock > 0) {
            printStockBar(melona.name, melona.displayStock);
            hasStock = true;
        }
        if (screwBar.displayStock > 0) {
            printStockBar(screwBar.name, screwBar.displayStock);
            hasStock = true;
        }
        if (fishBread.displayStock > 0) {
            printStockBar(fishBread.name, fishBread.displayStock);
            hasStock = true;
        }
        if (firework.displayStock > 0) {
            printStockBar(firework.name, firework.displayStock);
            hasStock = true;
        }

        // 매대가 비어있으면
        if (!hasStock) {
            System.out.println("  매대가 비어있습니다.");
            System.out.println("  도매상에서 상품을 입고하세요!");
        }

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    // ========== 매대 관리 ==========

    static void manageDisplay() {
        boolean managing = true;

        while (managing) {
            clearScreen();
            System.out.println("========================================");
            System.out.println("            [ 매대 관리 ]");
            System.out.println("========================================");
            System.out.printf("매대: %d / %d칸%n", usedSlot, MAX_SLOT);
            System.out.println();
            System.out.println("[1] 상품 진열 (창고 → 매대)");
            System.out.println("[2] 상품 회수 (매대 → 창고)");
            System.out.println("[3] 창고 재고 확인");
            System.out.println("[4] 자동 배정 (카테고리 균형)");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int choice = scanner.nextInt();

            if (choice == 1) {
                displayProduct();
            } else if (choice == 2) {
                returnProduct();
            } else if (choice == 3) {
                showWarehouse();
            } else if (choice == 4) {
                autoArrangeDisplay();
            } else if (choice == 0) {
                managing = false;
            }
        }
    }

    // ========== 창고 재고 확인 ==========

    static void showWarehouse() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("           [ 창고 재고 ]");
        System.out.println("========================================");
        System.out.println();

        boolean hasStock = false;

        // 창고에 재고가 있는 상품만 출력
        if (cola.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", cola.name, cola.warehouseStock);
            hasStock = true;
        }
        if (cider.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", cider.name, cider.warehouseStock);
            hasStock = true;
        }
        if (water.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", water.name, water.warehouseStock);
            hasStock = true;
        }
        if (pocari.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", pocari.name, pocari.warehouseStock);
            hasStock = true;
        }
        if (ipro.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", ipro.name, ipro.warehouseStock);
            hasStock = true;
        }
        if (cass.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", cass.name, cass.warehouseStock);
            hasStock = true;
        }
        if (terra.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", terra.name, terra.warehouseStock);
            hasStock = true;
        }
        if (hite.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", hite.name, hite.warehouseStock);
            hasStock = true;
        }
        if (chamisul.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", chamisul.name, chamisul.warehouseStock);
            hasStock = true;
        }
        if (cheumcherum.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", cheumcherum.name, cheumcherum.warehouseStock);
            hasStock = true;
        }
        if (jinro.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", jinro.name, jinro.warehouseStock);
            hasStock = true;
        }
        if (driedSquid.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", driedSquid.name, driedSquid.warehouseStock);
            hasStock = true;
        }
        if (peanut.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", peanut.name, peanut.warehouseStock);
            hasStock = true;
        }
        if (chip.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", chip.name, chip.warehouseStock);
            hasStock = true;
        }
        if (samgyupsal.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", samgyupsal.name, samgyupsal.warehouseStock);
            hasStock = true;
        }
        if (moksal.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", moksal.name, moksal.warehouseStock);
            hasStock = true;
        }
        if (sausage.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", sausage.name, sausage.warehouseStock);
            hasStock = true;
        }
        if (tube.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", tube.name, tube.warehouseStock);
            hasStock = true;
        }
        if (sunscreen.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", sunscreen.name, sunscreen.warehouseStock);
            hasStock = true;
        }
        if (beachBall.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", beachBall.name, beachBall.warehouseStock);
            hasStock = true;
        }
        if (ssamjang.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", ssamjang.name, ssamjang.warehouseStock);
            hasStock = true;
        }
        if (lettuce.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", lettuce.name, lettuce.warehouseStock);
            hasStock = true;
        }
        if (kimchi.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", kimchi.name, kimchi.warehouseStock);
            hasStock = true;
        }
        if (shinRamen.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", shinRamen.name, shinRamen.warehouseStock);
            hasStock = true;
        }
        if (jinRamen.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", jinRamen.name, jinRamen.warehouseStock);
            hasStock = true;
        }
        if (neoguri.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", neoguri.name, neoguri.warehouseStock);
            hasStock = true;
        }
        if (melona.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", melona.name, melona.warehouseStock);
            hasStock = true;
        }
        if (screwBar.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", screwBar.name, screwBar.warehouseStock);
            hasStock = true;
        }
        if (fishBread.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", fishBread.name, fishBread.warehouseStock);
            hasStock = true;
        }
        if (firework.warehouseStock > 0) {
            System.out.printf("%-16s %d개%n", firework.name, firework.warehouseStock);
            hasStock = true;
        }

        if (!hasStock) {
            System.out.println("  창고가 비어있습니다.");
        }

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    // ========== 상품 진열 (창고 → 매대) ==========

    static void displayProduct() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 진열 ] 창고 → 매대");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", usedSlot, MAX_SLOT);
        System.out.println();

        // 창고에 재고가 있는 상품 목록 출력
        System.out.println("--- 창고 재고 ---");
        int num = 1;
        if (cola.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, cola.name, cola.warehouseStock);
        if (cider.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, cider.name, cider.warehouseStock);
        if (water.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, water.name, water.warehouseStock);
        if (pocari.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, pocari.name, pocari.warehouseStock);
        if (ipro.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, ipro.name, ipro.warehouseStock);
        if (cass.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, cass.name, cass.warehouseStock);
        if (terra.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, terra.name, terra.warehouseStock);
        if (hite.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, hite.name, hite.warehouseStock);
        if (chamisul.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, chamisul.name, chamisul.warehouseStock);
        if (cheumcherum.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, cheumcherum.name, cheumcherum.warehouseStock);
        if (jinro.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, jinro.name, jinro.warehouseStock);
        if (driedSquid.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, driedSquid.name, driedSquid.warehouseStock);
        if (peanut.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, peanut.name, peanut.warehouseStock);
        if (chip.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, chip.name, chip.warehouseStock);
        if (samgyupsal.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, samgyupsal.name, samgyupsal.warehouseStock);
        if (moksal.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, moksal.name, moksal.warehouseStock);
        if (sausage.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, sausage.name, sausage.warehouseStock);
        if (tube.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, tube.name, tube.warehouseStock);
        if (sunscreen.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, sunscreen.name, sunscreen.warehouseStock);
        if (beachBall.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, beachBall.name, beachBall.warehouseStock);
        if (ssamjang.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, ssamjang.name, ssamjang.warehouseStock);
        if (lettuce.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, lettuce.name, lettuce.warehouseStock);
        if (kimchi.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, kimchi.name, kimchi.warehouseStock);
        if (shinRamen.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, shinRamen.name, shinRamen.warehouseStock);
        if (jinRamen.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, jinRamen.name, jinRamen.warehouseStock);
        if (neoguri.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, neoguri.name, neoguri.warehouseStock);
        if (melona.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, melona.name, melona.warehouseStock);
        if (screwBar.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, screwBar.name, screwBar.warehouseStock);
        if (fishBread.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, fishBread.name, fishBread.warehouseStock);
        if (firework.warehouseStock > 0) System.out.printf("%d. %s (%d개)%n", num++, firework.name, firework.warehouseStock);

        if (num == 1) {
            System.out.println("  창고가 비어있습니다.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        System.out.println();
        System.out.print("진열할 상품명 입력 (취소: 0): ");
        String productName = scanner.next();

        if (productName.equals("0")) {
            return;
        }

        // 상품 찾기
        Product product = findProductByName(productName);

        if (product == null) {
            System.out.println("[!!] 상품을 찾을 수 없습니다.");
            return;
        }

        if (product.warehouseStock == 0) {
            System.out.println("[!!] 창고에 재고가 없습니다.");
            return;
        }

        // 새 상품이면 슬롯 체크
        boolean isNewOnDisplay = (product.displayStock == 0);
        if (isNewOnDisplay && usedSlot >= MAX_SLOT) {
            System.out.println("[!!] 매대 공간이 부족합니다. (" + usedSlot + "/" + MAX_SLOT + "칸)");
            return;
        }

        System.out.printf("수량 입력 (창고: %d개, 전체: a): ", product.warehouseStock);
        String amountStr = scanner.next();

        int amount;
        if (amountStr.equals("a") || amountStr.equals("A")) {
            amount = product.warehouseStock;
        } else {
            amount = Integer.parseInt(amountStr);
        }

        if (amount <= 0) {
            return;
        }

        if (amount > product.warehouseStock) {
            amount = product.warehouseStock;
        }

        // 진열 처리
        product.displayFromWarehouse(amount);

        // 새 상품이면 슬롯 1칸 사용
        if (isNewOnDisplay) {
            usedSlot = usedSlot + 1;
        }

        System.out.printf("[OK] %s %d개 매대에 진열!%n", product.name, amount);
    }

    // ========== 상품 회수 (매대 → 창고) ==========

    static void returnProduct() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 회수 ] 매대 → 창고");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", usedSlot, MAX_SLOT);
        System.out.println();

        // 매대에 진열된 상품 목록 출력
        System.out.println("--- 매대 재고 ---");
        int num = 1;
        if (cola.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, cola.name, cola.displayStock);
        if (cider.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, cider.name, cider.displayStock);
        if (water.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, water.name, water.displayStock);
        if (pocari.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, pocari.name, pocari.displayStock);
        if (ipro.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, ipro.name, ipro.displayStock);
        if (cass.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, cass.name, cass.displayStock);
        if (terra.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, terra.name, terra.displayStock);
        if (hite.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, hite.name, hite.displayStock);
        if (chamisul.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, chamisul.name, chamisul.displayStock);
        if (cheumcherum.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, cheumcherum.name, cheumcherum.displayStock);
        if (jinro.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, jinro.name, jinro.displayStock);
        if (driedSquid.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, driedSquid.name, driedSquid.displayStock);
        if (peanut.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, peanut.name, peanut.displayStock);
        if (chip.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, chip.name, chip.displayStock);
        if (samgyupsal.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, samgyupsal.name, samgyupsal.displayStock);
        if (moksal.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, moksal.name, moksal.displayStock);
        if (sausage.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, sausage.name, sausage.displayStock);
        if (tube.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, tube.name, tube.displayStock);
        if (sunscreen.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, sunscreen.name, sunscreen.displayStock);
        if (beachBall.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, beachBall.name, beachBall.displayStock);
        if (ssamjang.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, ssamjang.name, ssamjang.displayStock);
        if (lettuce.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, lettuce.name, lettuce.displayStock);
        if (kimchi.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, kimchi.name, kimchi.displayStock);
        if (shinRamen.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, shinRamen.name, shinRamen.displayStock);
        if (jinRamen.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, jinRamen.name, jinRamen.displayStock);
        if (neoguri.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, neoguri.name, neoguri.displayStock);
        if (melona.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, melona.name, melona.displayStock);
        if (screwBar.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, screwBar.name, screwBar.displayStock);
        if (fishBread.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, fishBread.name, fishBread.displayStock);
        if (firework.displayStock > 0) System.out.printf("%d. %s (%d개)%n", num++, firework.name, firework.displayStock);

        if (num == 1) {
            System.out.println("  매대가 비어있습니다.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        System.out.println();
        System.out.print("회수할 상품명 입력 (취소: 0): ");
        String productName = scanner.next();

        if (productName.equals("0")) {
            return;
        }

        // 상품 찾기
        Product product = findProductByName(productName);

        if (product == null) {
            System.out.println("[!!] 상품을 찾을 수 없습니다.");
            return;
        }

        if (product.displayStock == 0) {
            System.out.println("[!!] 매대에 재고가 없습니다.");
            return;
        }

        System.out.printf("수량 입력 (매대: %d개, 전체: a): ", product.displayStock);
        String amountStr = scanner.next();

        int amount;
        if (amountStr.equals("a") || amountStr.equals("A")) {
            amount = product.displayStock;
        } else {
            amount = Integer.parseInt(amountStr);
        }

        if (amount <= 0) {
            return;
        }

        if (amount > product.displayStock) {
            amount = product.displayStock;
        }

        // 회수 처리
        product.returnToWarehouse(amount);

        // 매대가 비워지면 슬롯 반환
        if (product.displayStock == 0) {
            usedSlot = usedSlot - 1;
        }

        System.out.printf("[OK] %s %d개 창고로 회수!%n", product.name, amount);
    }

    // ========== 상품명으로 상품 찾기 ==========

    static Product findProductByName(String name) {
        if (name.equals(cola.name) || name.equals("콜라")) return cola;
        if (name.equals(cider.name) || name.equals("사이다")) return cider;
        if (name.equals(water.name) || name.equals("물") || name.equals("삼다수")) return water;
        if (name.equals(pocari.name) || name.equals("포카리")) return pocari;
        if (name.equals(ipro.name) || name.equals("이프로")) return ipro;
        if (name.equals(cass.name) || name.equals("카스")) return cass;
        if (name.equals(terra.name) || name.equals("테라")) return terra;
        if (name.equals(hite.name) || name.equals("하이트")) return hite;
        if (name.equals(chamisul.name) || name.equals("참이슬")) return chamisul;
        if (name.equals(cheumcherum.name) || name.equals("처음처럼")) return cheumcherum;
        if (name.equals(jinro.name) || name.equals("진로")) return jinro;
        if (name.equals(driedSquid.name) || name.equals("오징어")) return driedSquid;
        if (name.equals(peanut.name) || name.equals("땅콩")) return peanut;
        if (name.equals(chip.name) || name.equals("과자") || name.equals("칩")) return chip;
        if (name.equals(samgyupsal.name) || name.equals("삼겹살")) return samgyupsal;
        if (name.equals(moksal.name) || name.equals("목살")) return moksal;
        if (name.equals(sausage.name) || name.equals("소세지")) return sausage;
        if (name.equals(tube.name) || name.equals("튜브")) return tube;
        if (name.equals(sunscreen.name) || name.equals("선크림")) return sunscreen;
        if (name.equals(beachBall.name) || name.equals("비치볼")) return beachBall;
        if (name.equals(ssamjang.name) || name.equals("쌈장")) return ssamjang;
        if (name.equals(lettuce.name) || name.equals("상추")) return lettuce;
        if (name.equals(kimchi.name) || name.equals("김치")) return kimchi;
        if (name.equals(shinRamen.name) || name.equals("신라면")) return shinRamen;
        if (name.equals(jinRamen.name) || name.equals("진라면")) return jinRamen;
        if (name.equals(neoguri.name) || name.equals("너구리")) return neoguri;
        if (name.equals(melona.name) || name.equals("메로나")) return melona;
        if (name.equals(screwBar.name) || name.equals("스크류바")) return screwBar;
        if (name.equals(fishBread.name) || name.equals("붕어싸만코") || name.equals("붕어")) return fishBread;
        if (name.equals(firework.name) || name.equals("폭죽")) return firework;
        return null;
    }

    // ========== 자동 배정 (카테고리 균형) ==========

    static void autoArrangeDisplay() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("      [ 자동 배정 - 카테고리 균형 ]");
        System.out.println("========================================");
        System.out.printf("현재 매대: %d / %d칸%n", usedSlot, MAX_SLOT);
        System.out.println();

        // 남은 슬롯 계산
        int remainingSlots = MAX_SLOT - usedSlot;
        if (remainingSlots <= 0) {
            System.out.println("[!!] 매대가 꽉 찼습니다.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        // 카테고리별 상품 배열 (창고에 재고가 있는 것만)
        // 10개 카테고리, 각 카테고리당 최대 5개 상품
        Product[][] categories = new Product[10][5];
        int[] categoryCounts = new int[10];
        String[] categoryNames = {"음료", "맥주", "소주", "간식", "고기", "해수욕", "식재료", "라면", "아이스크림", "기타"};

        // 카테고리 0: 음료
        if (cola.warehouseStock > 0 && cola.displayStock == 0) categories[0][categoryCounts[0]++] = cola;
        if (cider.warehouseStock > 0 && cider.displayStock == 0) categories[0][categoryCounts[0]++] = cider;
        if (water.warehouseStock > 0 && water.displayStock == 0) categories[0][categoryCounts[0]++] = water;
        if (pocari.warehouseStock > 0 && pocari.displayStock == 0) categories[0][categoryCounts[0]++] = pocari;
        if (ipro.warehouseStock > 0 && ipro.displayStock == 0) categories[0][categoryCounts[0]++] = ipro;

        // 카테고리 1: 맥주
        if (cass.warehouseStock > 0 && cass.displayStock == 0) categories[1][categoryCounts[1]++] = cass;
        if (terra.warehouseStock > 0 && terra.displayStock == 0) categories[1][categoryCounts[1]++] = terra;
        if (hite.warehouseStock > 0 && hite.displayStock == 0) categories[1][categoryCounts[1]++] = hite;

        // 카테고리 2: 소주
        if (chamisul.warehouseStock > 0 && chamisul.displayStock == 0) categories[2][categoryCounts[2]++] = chamisul;
        if (cheumcherum.warehouseStock > 0 && cheumcherum.displayStock == 0) categories[2][categoryCounts[2]++] = cheumcherum;
        if (jinro.warehouseStock > 0 && jinro.displayStock == 0) categories[2][categoryCounts[2]++] = jinro;

        // 카테고리 3: 간식/안주
        if (driedSquid.warehouseStock > 0 && driedSquid.displayStock == 0) categories[3][categoryCounts[3]++] = driedSquid;
        if (peanut.warehouseStock > 0 && peanut.displayStock == 0) categories[3][categoryCounts[3]++] = peanut;
        if (chip.warehouseStock > 0 && chip.displayStock == 0) categories[3][categoryCounts[3]++] = chip;

        // 카테고리 4: 고기
        if (samgyupsal.warehouseStock > 0 && samgyupsal.displayStock == 0) categories[4][categoryCounts[4]++] = samgyupsal;
        if (moksal.warehouseStock > 0 && moksal.displayStock == 0) categories[4][categoryCounts[4]++] = moksal;
        if (sausage.warehouseStock > 0 && sausage.displayStock == 0) categories[4][categoryCounts[4]++] = sausage;

        // 카테고리 5: 해수욕 용품
        if (tube.warehouseStock > 0 && tube.displayStock == 0) categories[5][categoryCounts[5]++] = tube;
        if (sunscreen.warehouseStock > 0 && sunscreen.displayStock == 0) categories[5][categoryCounts[5]++] = sunscreen;
        if (beachBall.warehouseStock > 0 && beachBall.displayStock == 0) categories[5][categoryCounts[5]++] = beachBall;

        // 카테고리 6: 식재료
        if (ssamjang.warehouseStock > 0 && ssamjang.displayStock == 0) categories[6][categoryCounts[6]++] = ssamjang;
        if (lettuce.warehouseStock > 0 && lettuce.displayStock == 0) categories[6][categoryCounts[6]++] = lettuce;
        if (kimchi.warehouseStock > 0 && kimchi.displayStock == 0) categories[6][categoryCounts[6]++] = kimchi;

        // 카테고리 7: 라면
        if (shinRamen.warehouseStock > 0 && shinRamen.displayStock == 0) categories[7][categoryCounts[7]++] = shinRamen;
        if (jinRamen.warehouseStock > 0 && jinRamen.displayStock == 0) categories[7][categoryCounts[7]++] = jinRamen;
        if (neoguri.warehouseStock > 0 && neoguri.displayStock == 0) categories[7][categoryCounts[7]++] = neoguri;

        // 카테고리 8: 아이스크림
        if (melona.warehouseStock > 0 && melona.displayStock == 0) categories[8][categoryCounts[8]++] = melona;
        if (screwBar.warehouseStock > 0 && screwBar.displayStock == 0) categories[8][categoryCounts[8]++] = screwBar;
        if (fishBread.warehouseStock > 0 && fishBread.displayStock == 0) categories[8][categoryCounts[8]++] = fishBread;

        // 카테고리 9: 기타
        if (firework.warehouseStock > 0 && firework.displayStock == 0) categories[9][categoryCounts[9]++] = firework;

        // 진열 가능한 상품 수 계산
        int totalAvailable = 0;
        for (int i = 0; i < 10; i++) {
            totalAvailable = totalAvailable + categoryCounts[i];
        }

        if (totalAvailable == 0) {
            System.out.println("[!!] 창고에 진열할 상품이 없습니다.");
            System.out.println("     (이미 진열 중이거나 창고가 비어있음)");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        // 카테고리별로 순환하며 진열
        // 각 카테고리에서 한 상품씩 번갈아 진열하여 균형 맞춤
        int displayedCount = 0;
        int[] categoryIndex = new int[10];  // 각 카테고리에서 현재 진열 중인 인덱스

        System.out.println("[ 진열 결과 ]");

        // 라운드 로빈 방식으로 진열
        boolean hasMore = true;
        while (hasMore && usedSlot < MAX_SLOT) {
            hasMore = false;

            for (int cat = 0; cat < 10; cat++) {
                // 이 카테고리에 진열할 상품이 남아있는지
                if (categoryIndex[cat] < categoryCounts[cat]) {
                    // 슬롯이 남아있는지
                    if (usedSlot >= MAX_SLOT) {
                        break;
                    }

                    Product product = categories[cat][categoryIndex[cat]];
                    int amount = product.warehouseStock;

                    // 진열 처리
                    product.displayFromWarehouse(amount);
                    usedSlot = usedSlot + 1;
                    displayedCount++;

                    System.out.printf(" - [%s] %s %d개 진열%n", categoryNames[cat], product.name, amount);

                    categoryIndex[cat]++;
                    hasMore = true;
                }
            }
        }

        System.out.println();
        System.out.printf("총 %d개 상품 진열 완료!%n", displayedCount);
        System.out.printf("매대: %d / %d칸%n", usedSlot, MAX_SLOT);
        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    // ========== 재고 막대그래프 출력 ==========
    // 상품명과 재고 수량을 막대그래프로 표시

    /// <summary>
    /// 문자열의 화면 출력 폭 계산
    /// 한글은 2칸, 영문/숫자는 1칸
    /// </summary>
    static int getDisplayWidth(String str) {
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

    static void printStockBar(String name, int stock) {
        // 상품명 출력 (한글 8글자 기준 = 화면 폭 16칸)
        int maxWidth = 16;
        int nameWidth = getDisplayWidth(name);
        int padding = maxWidth - nameWidth;

        System.out.print(name);
        for (int i = 0; i < padding; i++) {
            System.out.print(" ");
        }

        // 막대 출력 (재고 2개당 █ 1개, 최대 20개)
        int barLength = stock / 2;
        if (barLength > 20) {
            barLength = 20;
        }
        if (barLength == 0 && stock > 0) {
            barLength = 1;  // 최소 1개는 표시
        }

        for (int i = 0; i < barLength; i++) {
            System.out.print("█");
        }

        // 수량 표시
        System.out.printf(" %d%n", stock);
    }

    // ========== 도매상 (메인 메뉴) ==========

    static void goWholesaler() {
        boolean shopping = true;

        while (shopping) {
            clearScreen();
            System.out.println("========================================");
            System.out.println("            [ 도매상 ]");
            System.out.println("========================================");
            System.out.printf("현재 자본: %,d원%n", money);
            System.out.printf("매대: %d / %d칸%n", usedSlot, MAX_SLOT);
            System.out.println();
            System.out.println("[1] 카테고리별 구매");
            System.out.println("[2] 정책 설정");
            System.out.println("[3] 자동주문 실행");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int choice = scanner.nextInt();

            if (choice == 1) {
                buyByCategory();
            } else if (choice == 2) {
                setPolicies();
            } else if (choice == 3) {
                executeAutoOrder();
            } else if (choice == 0) {
                shopping = false;
            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // ========== 카테고리별 구매 ==========

    static void buyByCategory() {
        boolean browsing = true;

        while (browsing) {
            clearScreen();
            System.out.println("========================================");
            System.out.println("        [ 카테고리 선택 ]");
            System.out.println("========================================");
            System.out.printf("현재 자본: %,d원 | 매대: %d / %d칸%n", money, usedSlot, MAX_SLOT);
            System.out.println();
            System.out.println("[1] 음료        [2] 맥주        [3] 소주");
            System.out.println("[4] 간식/안주   [5] 고기        [6] 해수욕용품");
            System.out.println("[7] 식재료      [8] 라면        [9] 아이스크림");
            System.out.println("[10] 기타");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int categoryChoice = scanner.nextInt();

            if (categoryChoice == 0) {
                browsing = false;
            } else if (categoryChoice >= 1 && categoryChoice <= 10) {
                buyCategoryProducts(categoryChoice);
            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // ========== 카테고리 내 상품 구매 ==========

    static void buyCategoryProducts(int category) {
        boolean buying = true;

        while (buying) {
            clearScreen();

            // 카테고리별 상품 목록 출력
            if (category == 1) {
                // 음료
                System.out.println("========================================");
                System.out.println("            [ 음료 ] (1박스=24개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cola.name, cola.buyPrice, cola.sellPrice, cola.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cider.name, cider.buyPrice, cider.sellPrice, cider.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", water.name, water.buyPrice, water.sellPrice, water.displayStock);
                System.out.printf("4. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", pocari.name, pocari.buyPrice, pocari.sellPrice, pocari.displayStock);
                System.out.printf("5. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", ipro.name, ipro.buyPrice, ipro.sellPrice, ipro.displayStock);
            } else if (category == 2) {
                // 맥주
                System.out.println("========================================");
                System.out.println("            [ 맥주 ] (1박스=24개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cass.name, cass.buyPrice, cass.sellPrice, cass.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", terra.name, terra.buyPrice, terra.sellPrice, terra.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", hite.name, hite.buyPrice, hite.sellPrice, hite.displayStock);
            } else if (category == 3) {
                // 소주
                System.out.println("========================================");
                System.out.println("            [ 소주 ] (1박스=20병)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", chamisul.name, chamisul.buyPrice, chamisul.sellPrice, chamisul.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cheumcherum.name, cheumcherum.buyPrice, cheumcherum.sellPrice, cheumcherum.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", jinro.name, jinro.buyPrice, jinro.sellPrice, jinro.displayStock);
            } else if (category == 4) {
                // 간식/안주
                System.out.println("========================================");
                System.out.println("         [ 간식/안주 ] (1박스=20개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", driedSquid.name, driedSquid.buyPrice, driedSquid.sellPrice, driedSquid.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", peanut.name, peanut.buyPrice, peanut.sellPrice, peanut.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", chip.name, chip.buyPrice, chip.sellPrice, chip.displayStock);
            } else if (category == 5) {
                // 고기
                System.out.println("========================================");
                System.out.println("            [ 고기 ] (1판=10팩)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", samgyupsal.name, samgyupsal.buyPrice, samgyupsal.sellPrice, samgyupsal.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", moksal.name, moksal.buyPrice, moksal.sellPrice, moksal.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", sausage.name, sausage.buyPrice, sausage.sellPrice, sausage.displayStock);
            } else if (category == 6) {
                // 해수욕 용품
                System.out.println("========================================");
                System.out.println("        [ 해수욕용품 ] (1묶음=5개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", tube.name, tube.buyPrice, tube.sellPrice, tube.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", sunscreen.name, sunscreen.buyPrice, sunscreen.sellPrice, sunscreen.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", beachBall.name, beachBall.buyPrice, beachBall.sellPrice, beachBall.displayStock);
            } else if (category == 7) {
                // 식재료
                System.out.println("========================================");
                System.out.println("          [ 식재료 ] (1박스=10개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", ssamjang.name, ssamjang.buyPrice, ssamjang.sellPrice, ssamjang.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", lettuce.name, lettuce.buyPrice, lettuce.sellPrice, lettuce.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", kimchi.name, kimchi.buyPrice, kimchi.sellPrice, kimchi.displayStock);
            } else if (category == 8) {
                // 라면
                System.out.println("========================================");
                System.out.println("            [ 라면 ] (1박스=40개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", shinRamen.name, shinRamen.buyPrice, shinRamen.sellPrice, shinRamen.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", jinRamen.name, jinRamen.buyPrice, jinRamen.sellPrice, jinRamen.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", neoguri.name, neoguri.buyPrice, neoguri.sellPrice, neoguri.displayStock);
            } else if (category == 9) {
                // 아이스크림
                System.out.println("========================================");
                System.out.println("        [ 아이스크림 ] (1박스=24개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", melona.name, melona.buyPrice, melona.sellPrice, melona.displayStock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", screwBar.name, screwBar.buyPrice, screwBar.sellPrice, screwBar.displayStock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", fishBread.name, fishBread.buyPrice, fishBread.sellPrice, fishBread.displayStock);
            } else if (category == 10) {
                // 기타
                System.out.println("========================================");
                System.out.println("            [ 기타 ] (1박스=10개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", firework.name, firework.buyPrice, firework.sellPrice, firework.displayStock);
            }

            System.out.println();
            System.out.println("구매할 상품 번호 (0: 돌아가기)");
            System.out.print(">> ");

            int productChoice = scanner.nextInt();

            if (productChoice == 0) {
                buying = false;
            } else {
                // 수량 입력
                System.out.print("수량 입력 >> ");
                int quantity = scanner.nextInt();

                // 상품 구매 처리
                purchaseProduct(category, productChoice, quantity);
            }
        }
    }

    // ========== 상품 구매 처리 ==========

    static void purchaseProduct(int category, int productNum, int quantity) {
        // 카테고리와 상품 번호로 상품 찾기
        Product product = getProductByCategoryAndNum(category, productNum);

        if (product == null) {
            System.out.println("[!!] 잘못된 상품 번호입니다.");
            return;
        }

        int totalCost = product.buyPrice * quantity;

        // 자본 체크
        if (totalCost > money) {
            System.out.println("[!!] 자본이 부족합니다. (필요: " + String.format("%,d", totalCost) + "원)");
            return;
        }

        // 구매 처리 (창고로 입고)
        money = money - totalCost;
        product.addToWarehouse(quantity);

        System.out.println("[OK] " + product.name + " " + quantity + "개 창고로 입고! (-" + String.format("%,d", totalCost) + "원)");
    }

    // ========== 카테고리와 번호로 상품 찾기 ==========

    static Product getProductByCategoryAndNum(int category, int num) {
        if (category == 1) {
            // 음료
            if (num == 1) return cola;
            if (num == 2) return cider;
            if (num == 3) return water;
            if (num == 4) return pocari;
            if (num == 5) return ipro;
        } else if (category == 2) {
            // 맥주
            if (num == 1) return cass;
            if (num == 2) return terra;
            if (num == 3) return hite;
        } else if (category == 3) {
            // 소주
            if (num == 1) return chamisul;
            if (num == 2) return cheumcherum;
            if (num == 3) return jinro;
        } else if (category == 4) {
            // 간식/안주
            if (num == 1) return driedSquid;
            if (num == 2) return peanut;
            if (num == 3) return chip;
        } else if (category == 5) {
            // 고기
            if (num == 1) return samgyupsal;
            if (num == 2) return moksal;
            if (num == 3) return sausage;
        } else if (category == 6) {
            // 해수욕 용품
            if (num == 1) return tube;
            if (num == 2) return sunscreen;
            if (num == 3) return beachBall;
        } else if (category == 7) {
            // 식재료
            if (num == 1) return ssamjang;
            if (num == 2) return lettuce;
            if (num == 3) return kimchi;
        } else if (category == 8) {
            // 라면
            if (num == 1) return shinRamen;
            if (num == 2) return jinRamen;
            if (num == 3) return neoguri;
        } else if (category == 9) {
            // 아이스크림
            if (num == 1) return melona;
            if (num == 2) return screwBar;
            if (num == 3) return fishBread;
        } else if (category == 10) {
            // 기타
            if (num == 1) return firework;
        }
        return null;
    }

    // ========== 정책 설정 ==========

    static void setPolicies() {
        boolean setting = true;

        while (setting) {
            clearScreen();
            System.out.println("========================================");
            System.out.println("          [ 정책 설정 ]");
            System.out.println("========================================");
            System.out.println();
            System.out.println("[1] 카테고리 단위 설정");
            System.out.println("[2] 개별 상품 설정");
            System.out.println("[3] 현재 정책 확인");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int choice = scanner.nextInt();

            if (choice == 1) {
                setCategoryPolicy();
            } else if (choice == 2) {
                setIndividualPolicy();
            } else if (choice == 3) {
                showCurrentPolicies();
            } else if (choice == 0) {
                setting = false;
            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // ========== 카테고리 단위 정책 설정 ==========

    static void setCategoryPolicy() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("      [ 카테고리 단위 설정 ]");
        System.out.println("========================================");
        System.out.println();
        System.out.println("[1] 음료        [2] 맥주        [3] 소주");
        System.out.println("[4] 간식/안주   [5] 고기        [6] 해수욕용품");
        System.out.println("[7] 식재료      [8] 라면        [9] 아이스크림");
        System.out.println("[10] 기타");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int categoryChoice = scanner.nextInt();

        if (categoryChoice == 0) {
            return;
        }

        if (categoryChoice < 1 || categoryChoice > 10) {
            System.out.println("잘못된 입력입니다.");
            return;
        }

        // 카테고리명 가져오기
        String categoryName = getCategoryName(categoryChoice);

        System.out.println();
        System.out.println("[ " + categoryName + " 카테고리 설정 ]");
        System.out.println("[1] 자동주문 등록 (임계값 입력)");
        System.out.println("[2] 자동주문 해제");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int actionChoice = scanner.nextInt();

        if (actionChoice == 0) {
            return;
        } else if (actionChoice == 1) {
            System.out.print("임계값 입력 (재고 몇 개 이하면 주문?) >> ");
            int threshold = scanner.nextInt();

            // 카테고리별 정책 설정
            if (categoryChoice == 1) {
                autoOrderDrink = true;
                thresholdDrink = threshold;
            } else if (categoryChoice == 2) {
                autoOrderBeer = true;
                thresholdBeer = threshold;
            } else if (categoryChoice == 3) {
                autoOrderSoju = true;
                thresholdSoju = threshold;
            } else if (categoryChoice == 4) {
                autoOrderSnack = true;
                thresholdSnack = threshold;
            } else if (categoryChoice == 5) {
                autoOrderMeat = true;
                thresholdMeat = threshold;
            } else if (categoryChoice == 6) {
                autoOrderBeach = true;
                thresholdBeach = threshold;
            } else if (categoryChoice == 7) {
                autoOrderGrocery = true;
                thresholdGrocery = threshold;
            } else if (categoryChoice == 8) {
                autoOrderRamen = true;
                thresholdRamen = threshold;
            } else if (categoryChoice == 9) {
                autoOrderIcecream = true;
                thresholdIcecream = threshold;
            } else if (categoryChoice == 10) {
                autoOrderEtc = true;
                thresholdEtc = threshold;
            }

            System.out.println("[OK] " + categoryName + " 카테고리 등록 완료 (임계값: " + threshold + "개)");

        } else if (actionChoice == 2) {
            // 자동주문 해제
            if (categoryChoice == 1) {
                autoOrderDrink = false;
            } else if (categoryChoice == 2) {
                autoOrderBeer = false;
            } else if (categoryChoice == 3) {
                autoOrderSoju = false;
            } else if (categoryChoice == 4) {
                autoOrderSnack = false;
            } else if (categoryChoice == 5) {
                autoOrderMeat = false;
            } else if (categoryChoice == 6) {
                autoOrderBeach = false;
            } else if (categoryChoice == 7) {
                autoOrderGrocery = false;
            } else if (categoryChoice == 8) {
                autoOrderRamen = false;
            } else if (categoryChoice == 9) {
                autoOrderIcecream = false;
            } else if (categoryChoice == 10) {
                autoOrderEtc = false;
            }

            System.out.println("[OK] " + categoryName + " 카테고리 자동주문 해제됨");
        }
    }

    // ========== 카테고리명 가져오기 ==========

    static String getCategoryName(int category) {
        if (category == 1) return "음료";
        if (category == 2) return "맥주";
        if (category == 3) return "소주";
        if (category == 4) return "간식/안주";
        if (category == 5) return "고기";
        if (category == 6) return "해수욕용품";
        if (category == 7) return "식재료";
        if (category == 8) return "라면";
        if (category == 9) return "아이스크림";
        if (category == 10) return "기타";
        return "";
    }

    // ========== 개별 상품 정책 설정 ==========

    static void setIndividualPolicy() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("       [ 개별 상품 설정 ]");
        System.out.println("========================================");
        System.out.println("먼저 카테고리를 선택하세요.");
        System.out.println();
        System.out.println("[1] 음료        [2] 맥주        [3] 소주");
        System.out.println("[4] 간식/안주   [5] 고기        [6] 해수욕용품");
        System.out.println("[7] 식재료      [8] 라면        [9] 아이스크림");
        System.out.println("[10] 기타");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int categoryChoice = scanner.nextInt();

        if (categoryChoice == 0) {
            return;
        }

        if (categoryChoice < 1 || categoryChoice > 10) {
            System.out.println("잘못된 입력입니다.");
            return;
        }

        // 해당 카테고리 상품 목록 출력
        System.out.println();
        printCategoryProductsForPolicy(categoryChoice);

        System.out.print("상품 번호 선택 >> ");
        int productNum = scanner.nextInt();

        Product product = getProductByCategoryAndNum(categoryChoice, productNum);

        if (product == null) {
            System.out.println("[!!] 잘못된 상품 번호입니다.");
            return;
        }

        System.out.println();
        System.out.println("[ " + product.name + " 설정 ]");
        System.out.println("[1] 자동주문 등록 (임계값 입력)");
        System.out.println("[2] 자동주문 해제");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int actionChoice = scanner.nextInt();

        if (actionChoice == 0) {
            return;
        } else if (actionChoice == 1) {
            System.out.print("임계값 입력 (재고 몇 개 이하면 주문?) >> ");
            int threshold = scanner.nextInt();

            product.autoOrderEnabled = true;
            product.autoOrderThreshold = threshold;

            System.out.println("[OK] " + product.name + " 등록 완료 (임계값: " + threshold + "개)");

        } else if (actionChoice == 2) {
            product.autoOrderEnabled = false;
            System.out.println("[OK] " + product.name + " 자동주문 해제됨");
        }
    }

    // ========== 정책 설정용 카테고리 상품 출력 ==========

    static void printCategoryProductsForPolicy(int category) {
        if (category == 1) {
            System.out.println("[ 음료 ]");
            System.out.printf("1. %s (재고: %d)%n", cola.name, cola.displayStock);
            System.out.printf("2. %s (재고: %d)%n", cider.name, cider.displayStock);
            System.out.printf("3. %s (재고: %d)%n", water.name, water.displayStock);
            System.out.printf("4. %s (재고: %d)%n", pocari.name, pocari.displayStock);
            System.out.printf("5. %s (재고: %d)%n", ipro.name, ipro.displayStock);
        } else if (category == 2) {
            System.out.println("[ 맥주 ]");
            System.out.printf("1. %s (재고: %d)%n", cass.name, cass.displayStock);
            System.out.printf("2. %s (재고: %d)%n", terra.name, terra.displayStock);
            System.out.printf("3. %s (재고: %d)%n", hite.name, hite.displayStock);
        } else if (category == 3) {
            System.out.println("[ 소주 ]");
            System.out.printf("1. %s (재고: %d)%n", chamisul.name, chamisul.displayStock);
            System.out.printf("2. %s (재고: %d)%n", cheumcherum.name, cheumcherum.displayStock);
            System.out.printf("3. %s (재고: %d)%n", jinro.name, jinro.displayStock);
        } else if (category == 4) {
            System.out.println("[ 간식/안주 ]");
            System.out.printf("1. %s (재고: %d)%n", driedSquid.name, driedSquid.displayStock);
            System.out.printf("2. %s (재고: %d)%n", peanut.name, peanut.displayStock);
            System.out.printf("3. %s (재고: %d)%n", chip.name, chip.displayStock);
        } else if (category == 5) {
            System.out.println("[ 고기 ]");
            System.out.printf("1. %s (재고: %d)%n", samgyupsal.name, samgyupsal.displayStock);
            System.out.printf("2. %s (재고: %d)%n", moksal.name, moksal.displayStock);
            System.out.printf("3. %s (재고: %d)%n", sausage.name, sausage.displayStock);
        } else if (category == 6) {
            System.out.println("[ 해수욕용품 ]");
            System.out.printf("1. %s (재고: %d)%n", tube.name, tube.displayStock);
            System.out.printf("2. %s (재고: %d)%n", sunscreen.name, sunscreen.displayStock);
            System.out.printf("3. %s (재고: %d)%n", beachBall.name, beachBall.displayStock);
        } else if (category == 7) {
            System.out.println("[ 식재료 ]");
            System.out.printf("1. %s (재고: %d)%n", ssamjang.name, ssamjang.displayStock);
            System.out.printf("2. %s (재고: %d)%n", lettuce.name, lettuce.displayStock);
            System.out.printf("3. %s (재고: %d)%n", kimchi.name, kimchi.displayStock);
        } else if (category == 8) {
            System.out.println("[ 라면 ]");
            System.out.printf("1. %s (재고: %d)%n", shinRamen.name, shinRamen.displayStock);
            System.out.printf("2. %s (재고: %d)%n", jinRamen.name, jinRamen.displayStock);
            System.out.printf("3. %s (재고: %d)%n", neoguri.name, neoguri.displayStock);
        } else if (category == 9) {
            System.out.println("[ 아이스크림 ]");
            System.out.printf("1. %s (재고: %d)%n", melona.name, melona.displayStock);
            System.out.printf("2. %s (재고: %d)%n", screwBar.name, screwBar.displayStock);
            System.out.printf("3. %s (재고: %d)%n", fishBread.name, fishBread.displayStock);
        } else if (category == 10) {
            System.out.println("[ 기타 ]");
            System.out.printf("1. %s (재고: %d)%n", firework.name, firework.displayStock);
        }
    }

    // ========== 현재 정책 확인 ==========

    static void showCurrentPolicies() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 현재 정책 확인 ]");
        System.out.println("========================================");
        System.out.println();

        // 카테고리 정책 출력
        System.out.println("[ 카테고리 정책 ]");
        boolean hasCategoryPolicy = false;

        if (autoOrderDrink) {
            System.out.println(" - 음료: 임계값 " + thresholdDrink + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderBeer) {
            System.out.println(" - 맥주: 임계값 " + thresholdBeer + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderSoju) {
            System.out.println(" - 소주: 임계값 " + thresholdSoju + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderSnack) {
            System.out.println(" - 간식/안주: 임계값 " + thresholdSnack + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderMeat) {
            System.out.println(" - 고기: 임계값 " + thresholdMeat + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderBeach) {
            System.out.println(" - 해수욕용품: 임계값 " + thresholdBeach + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderGrocery) {
            System.out.println(" - 식재료: 임계값 " + thresholdGrocery + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderRamen) {
            System.out.println(" - 라면: 임계값 " + thresholdRamen + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderIcecream) {
            System.out.println(" - 아이스크림: 임계값 " + thresholdIcecream + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderEtc) {
            System.out.println(" - 기타: 임계값 " + thresholdEtc + "개");
            hasCategoryPolicy = true;
        }

        if (!hasCategoryPolicy) {
            System.out.println(" (없음)");
        }

        System.out.println();

        // 개별 상품 정책 출력
        System.out.println("[ 개별 상품 정책 ]");
        boolean hasIndividualPolicy = false;

        // 모든 상품 체크
        if (cola.autoOrderEnabled) {
            System.out.println(" - " + cola.name + ": 임계값 " + cola.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (cider.autoOrderEnabled) {
            System.out.println(" - " + cider.name + ": 임계값 " + cider.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (water.autoOrderEnabled) {
            System.out.println(" - " + water.name + ": 임계값 " + water.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (pocari.autoOrderEnabled) {
            System.out.println(" - " + pocari.name + ": 임계값 " + pocari.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (ipro.autoOrderEnabled) {
            System.out.println(" - " + ipro.name + ": 임계값 " + ipro.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (cass.autoOrderEnabled) {
            System.out.println(" - " + cass.name + ": 임계값 " + cass.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (terra.autoOrderEnabled) {
            System.out.println(" - " + terra.name + ": 임계값 " + terra.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (hite.autoOrderEnabled) {
            System.out.println(" - " + hite.name + ": 임계값 " + hite.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (chamisul.autoOrderEnabled) {
            System.out.println(" - " + chamisul.name + ": 임계값 " + chamisul.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (cheumcherum.autoOrderEnabled) {
            System.out.println(" - " + cheumcherum.name + ": 임계값 " + cheumcherum.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (jinro.autoOrderEnabled) {
            System.out.println(" - " + jinro.name + ": 임계값 " + jinro.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (driedSquid.autoOrderEnabled) {
            System.out.println(" - " + driedSquid.name + ": 임계값 " + driedSquid.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (peanut.autoOrderEnabled) {
            System.out.println(" - " + peanut.name + ": 임계값 " + peanut.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (chip.autoOrderEnabled) {
            System.out.println(" - " + chip.name + ": 임계값 " + chip.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (samgyupsal.autoOrderEnabled) {
            System.out.println(" - " + samgyupsal.name + ": 임계값 " + samgyupsal.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (moksal.autoOrderEnabled) {
            System.out.println(" - " + moksal.name + ": 임계값 " + moksal.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (sausage.autoOrderEnabled) {
            System.out.println(" - " + sausage.name + ": 임계값 " + sausage.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (tube.autoOrderEnabled) {
            System.out.println(" - " + tube.name + ": 임계값 " + tube.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (sunscreen.autoOrderEnabled) {
            System.out.println(" - " + sunscreen.name + ": 임계값 " + sunscreen.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (beachBall.autoOrderEnabled) {
            System.out.println(" - " + beachBall.name + ": 임계값 " + beachBall.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (ssamjang.autoOrderEnabled) {
            System.out.println(" - " + ssamjang.name + ": 임계값 " + ssamjang.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (lettuce.autoOrderEnabled) {
            System.out.println(" - " + lettuce.name + ": 임계값 " + lettuce.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (kimchi.autoOrderEnabled) {
            System.out.println(" - " + kimchi.name + ": 임계값 " + kimchi.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (shinRamen.autoOrderEnabled) {
            System.out.println(" - " + shinRamen.name + ": 임계값 " + shinRamen.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (jinRamen.autoOrderEnabled) {
            System.out.println(" - " + jinRamen.name + ": 임계값 " + jinRamen.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (neoguri.autoOrderEnabled) {
            System.out.println(" - " + neoguri.name + ": 임계값 " + neoguri.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (melona.autoOrderEnabled) {
            System.out.println(" - " + melona.name + ": 임계값 " + melona.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (screwBar.autoOrderEnabled) {
            System.out.println(" - " + screwBar.name + ": 임계값 " + screwBar.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (fishBread.autoOrderEnabled) {
            System.out.println(" - " + fishBread.name + ": 임계값 " + fishBread.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (firework.autoOrderEnabled) {
            System.out.println(" - " + firework.name + ": 임계값 " + firework.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }

        if (!hasIndividualPolicy) {
            System.out.println(" (없음)");
        }

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    // ========== 자동주문 실행 ==========

    static void executeAutoOrder() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 자동주문 실행 ]");
        System.out.println("========================================");
        System.out.println();

        int totalCost = 0;
        int orderCount = 0;

        // 카테고리 정책 기반 주문 (음료)
        if (autoOrderDrink) {
            totalCost = totalCost + autoOrderProduct(cola, thresholdDrink);
            totalCost = totalCost + autoOrderProduct(cider, thresholdDrink);
            totalCost = totalCost + autoOrderProduct(water, thresholdDrink);
            totalCost = totalCost + autoOrderProduct(pocari, thresholdDrink);
            totalCost = totalCost + autoOrderProduct(ipro, thresholdDrink);
        }

        // 맥주
        if (autoOrderBeer) {
            totalCost = totalCost + autoOrderProduct(cass, thresholdBeer);
            totalCost = totalCost + autoOrderProduct(terra, thresholdBeer);
            totalCost = totalCost + autoOrderProduct(hite, thresholdBeer);
        }

        // 소주
        if (autoOrderSoju) {
            totalCost = totalCost + autoOrderProduct(chamisul, thresholdSoju);
            totalCost = totalCost + autoOrderProduct(cheumcherum, thresholdSoju);
            totalCost = totalCost + autoOrderProduct(jinro, thresholdSoju);
        }

        // 간식/안주
        if (autoOrderSnack) {
            totalCost = totalCost + autoOrderProduct(driedSquid, thresholdSnack);
            totalCost = totalCost + autoOrderProduct(peanut, thresholdSnack);
            totalCost = totalCost + autoOrderProduct(chip, thresholdSnack);
        }

        // 고기
        if (autoOrderMeat) {
            totalCost = totalCost + autoOrderProduct(samgyupsal, thresholdMeat);
            totalCost = totalCost + autoOrderProduct(moksal, thresholdMeat);
            totalCost = totalCost + autoOrderProduct(sausage, thresholdMeat);
        }

        // 해수욕용품
        if (autoOrderBeach) {
            totalCost = totalCost + autoOrderProduct(tube, thresholdBeach);
            totalCost = totalCost + autoOrderProduct(sunscreen, thresholdBeach);
            totalCost = totalCost + autoOrderProduct(beachBall, thresholdBeach);
        }

        // 식재료
        if (autoOrderGrocery) {
            totalCost = totalCost + autoOrderProduct(ssamjang, thresholdGrocery);
            totalCost = totalCost + autoOrderProduct(lettuce, thresholdGrocery);
            totalCost = totalCost + autoOrderProduct(kimchi, thresholdGrocery);
        }

        // 라면
        if (autoOrderRamen) {
            totalCost = totalCost + autoOrderProduct(shinRamen, thresholdRamen);
            totalCost = totalCost + autoOrderProduct(jinRamen, thresholdRamen);
            totalCost = totalCost + autoOrderProduct(neoguri, thresholdRamen);
        }

        // 아이스크림
        if (autoOrderIcecream) {
            totalCost = totalCost + autoOrderProduct(melona, thresholdIcecream);
            totalCost = totalCost + autoOrderProduct(screwBar, thresholdIcecream);
            totalCost = totalCost + autoOrderProduct(fishBread, thresholdIcecream);
        }

        // 기타
        if (autoOrderEtc) {
            totalCost = totalCost + autoOrderProduct(firework, thresholdEtc);
        }

        // 개별 상품 정책 기반 주문 (카테고리에 없는 것만)
        if (!autoOrderDrink) {
            if (cola.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(cola, cola.autoOrderThreshold);
            if (cider.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(cider, cider.autoOrderThreshold);
            if (water.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(water, water.autoOrderThreshold);
            if (pocari.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(pocari, pocari.autoOrderThreshold);
            if (ipro.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(ipro, ipro.autoOrderThreshold);
        }
        if (!autoOrderBeer) {
            if (cass.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(cass, cass.autoOrderThreshold);
            if (terra.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(terra, terra.autoOrderThreshold);
            if (hite.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(hite, hite.autoOrderThreshold);
        }
        if (!autoOrderSoju) {
            if (chamisul.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(chamisul, chamisul.autoOrderThreshold);
            if (cheumcherum.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(cheumcherum, cheumcherum.autoOrderThreshold);
            if (jinro.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(jinro, jinro.autoOrderThreshold);
        }
        if (!autoOrderSnack) {
            if (driedSquid.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(driedSquid, driedSquid.autoOrderThreshold);
            if (peanut.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(peanut, peanut.autoOrderThreshold);
            if (chip.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(chip, chip.autoOrderThreshold);
        }
        if (!autoOrderMeat) {
            if (samgyupsal.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(samgyupsal, samgyupsal.autoOrderThreshold);
            if (moksal.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(moksal, moksal.autoOrderThreshold);
            if (sausage.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(sausage, sausage.autoOrderThreshold);
        }
        if (!autoOrderBeach) {
            if (tube.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(tube, tube.autoOrderThreshold);
            if (sunscreen.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(sunscreen, sunscreen.autoOrderThreshold);
            if (beachBall.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(beachBall, beachBall.autoOrderThreshold);
        }
        if (!autoOrderGrocery) {
            if (ssamjang.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(ssamjang, ssamjang.autoOrderThreshold);
            if (lettuce.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(lettuce, lettuce.autoOrderThreshold);
            if (kimchi.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(kimchi, kimchi.autoOrderThreshold);
        }
        if (!autoOrderRamen) {
            if (shinRamen.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(shinRamen, shinRamen.autoOrderThreshold);
            if (jinRamen.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(jinRamen, jinRamen.autoOrderThreshold);
            if (neoguri.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(neoguri, neoguri.autoOrderThreshold);
        }
        if (!autoOrderIcecream) {
            if (melona.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(melona, melona.autoOrderThreshold);
            if (screwBar.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(screwBar, screwBar.autoOrderThreshold);
            if (fishBread.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(fishBread, fishBread.autoOrderThreshold);
        }
        if (!autoOrderEtc) {
            if (firework.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(firework, firework.autoOrderThreshold);
        }

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("총 주문 금액: -%,d원%n", totalCost);
        System.out.printf("남은 자본: %,d원%n", money);
        System.out.println("----------------------------------------");

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    // ========== 개별 상품 자동주문 처리 ==========
    // 재고가 임계값 이하면 1박스 주문, 주문 금액 반환

    static int autoOrderProduct(Product product, int threshold) {
        // 총 재고(창고+매대)가 임계값보다 많으면 주문 안 함
        int totalStock = product.warehouseStock + product.displayStock;
        if (totalStock > threshold) {
            return 0;
        }

        int boxSize = product.boxSize;
        int cost = product.buyPrice * boxSize;

        // 자본 체크
        if (cost > money) {
            System.out.printf(" - %s: 자본 부족 (필요: %,d원)%n", product.name, cost);
            return 0;
        }

        // 주문 처리 (창고로 입고)
        money = money - cost;
        product.addToWarehouse(boxSize);

        System.out.printf(" - %s 1박스(%d개) 창고 입고 (-%,d원)%n", product.name, boxSize, cost);

        return cost;
    }

    // ========== 영업 시작 (손님 응대) ==========

    static void startBusiness() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("           [ 영업 시작 ]");
        System.out.println("========================================");

        // 하루 영업 변수
        int todayCustomers = 10 + (int)(Math.random() * 11);  // 10~20명
        int todaySales = 0;      // 오늘 매출
        int todayProfit = 0;     // 오늘 순이익
        int successCount = 0;    // 판매 성공 건수
        int failCount = 0;       // 판매 실패 건수

        // 손님 응대 루프
        for (int i = 1; i <= todayCustomers; i++) {

            // 랜덤 손님 유형 (0: 가족, 1: 커플, 2: 친구들, 3: 혼자)
            int customerType = (int)(Math.random() * 4);
            String customerName;
            String customerMessage;

            // 손님별 구매할 상품 목록
            Product[] shoppingList;
            int[] shoppingAmounts;

            if (customerType == 0) {
                // 가족: 바베큐 세트 (5~8종류)
                customerName = "가족 손님";
                customerMessage = "바베큐 하려고 왔어요~ 많이 살게요!";
                shoppingList = new Product[]{samgyupsal, moksal, sausage, lettuce, ssamjang, kimchi, cola, cider, water};
                shoppingAmounts = new int[]{2 + rand(3), 1 + rand(2), rand(3), 2 + rand(2), 1, 1, 3 + rand(3), 2 + rand(2), 2 + rand(3)};
            } else if (customerType == 1) {
                // 커플: 술 + 안주 (4~6종류)
                customerName = "커플 손님";
                customerMessage = "오늘 달 보면서 한잔 하려고요~";
                shoppingList = new Product[]{chamisul, cheumcherum, cass, terra, driedSquid, peanut, chip};
                shoppingAmounts = new int[]{2 + rand(2), 1 + rand(2), 2 + rand(2), 1 + rand(2), 1 + rand(2), 1, 1 + rand(2)};
            } else if (customerType == 2) {
                // 친구들: 파티 세트 (5~9종류)
                customerName = "친구들";
                customerMessage = "우리 오늘 펜션에서 파티해요!!";
                shoppingList = new Product[]{cass, terra, hite, chamisul, chip, peanut, firework, melona, screwBar, cola};
                shoppingAmounts = new int[]{4 + rand(4), 3 + rand(3), 2 + rand(3), 2 + rand(2), 2 + rand(2), 1 + rand(2), 3 + rand(3), 2 + rand(3), 2 + rand(2), 2 + rand(3)};
            } else {
                // 혼자: 간단히 (3~5종류)
                customerName = "혼자 온 손님";
                customerMessage = "라면이랑 맥주 좀 주세요.";
                shoppingList = new Product[]{shinRamen, jinRamen, neoguri, cass, cola, melona};
                shoppingAmounts = new int[]{2 + rand(2), 1 + rand(2), 1 + rand(2), 2 + rand(2), 1 + rand(2), 1 + rand(2)};
            }

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.printf("[ 손님 %d/%d - %s ]%n", i, todayCustomers, customerName);
            delay(500);
            System.out.printf("\"%s\"%n", customerMessage);
            delay(300);
            System.out.println();

            // 손님의 쇼핑 리스트 처리
            int customerSales = 0;
            int customerProfit = 0;

            System.out.println("쇼핑 리스트:");
            for (int j = 0; j < shoppingList.length; j++) {
                Product product = shoppingList[j];
                int wantAmount = shoppingAmounts[j];

                // 수량 0이면 스킵
                if (wantAmount <= 0) {
                    continue;
                }

                delay(300);
                System.out.printf(" - %s %d개: ", product.name, wantAmount);
                delay(200);

                if (product.displayStock >= wantAmount) {
                    // 전부 판매 가능
                    int saleAmount = product.sellPrice * wantAmount;
                    int profitAmount = (product.sellPrice - product.buyPrice) * wantAmount;

                    product.sell(wantAmount);
                    if (product.displayStock == 0) {
                        usedSlot--;
                    }

                    money = money + saleAmount;
                    customerSales = customerSales + saleAmount;
                    customerProfit = customerProfit + profitAmount;
                    todaySales = todaySales + saleAmount;
                    todayProfit = todayProfit + profitAmount;
                    successCount++;

                    System.out.printf("OK (+%,d원)%n", saleAmount);

                } else if (product.displayStock > 0) {
                    // 일부만 판매
                    int actualAmount = product.displayStock;
                    int saleAmount = product.sellPrice * actualAmount;
                    int profitAmount = (product.sellPrice - product.buyPrice) * actualAmount;

                    product.sell(actualAmount);
                    usedSlot--;  // 재고 0이 됨

                    money = money + saleAmount;
                    customerSales = customerSales + saleAmount;
                    customerProfit = customerProfit + profitAmount;
                    todaySales = todaySales + saleAmount;
                    todayProfit = todayProfit + profitAmount;
                    successCount++;
                    failCount++;  // 일부 실패로 카운트

                    System.out.printf("%d개만... (+%,d원)%n", actualAmount, saleAmount);

                } else {
                    // 재고 없음
                    failCount++;
                    System.out.println("재고 없음!");
                }
            }

            // 손님 총액 표시
            delay(400);
            if (customerSales > 0) {
                System.out.printf(">> 손님 결제: %,d원%n", customerSales);
            } else {
                System.out.println(">> 아무것도 못 사고 갔습니다...");
            }
        }

        // 하루 정산
        delay(800);  // 정산 준비 연출
        System.out.println();
        System.out.println("========================================");
        System.out.printf("          [ %d일차 정산 ]%n", day);
        System.out.println("========================================");
        System.out.printf("오늘 방문 손님: %d명%n", todayCustomers);
        System.out.printf("판매 성공: %d건%n", successCount);
        System.out.printf("판매 실패: %d건%n", failCount);
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("  오늘 매출:    %,d원%n", todaySales);
        System.out.printf("  순이익:      +%,d원%n", todayProfit);
        System.out.println("----------------------------------------");
        System.out.printf("  현재 총 자본: %,d원%n", money);
        System.out.printf("  목표까지:     %,d원%n", goalMoney - money);
        System.out.println("========================================");
    }

    // ========== 상품 초기화 메서드 ==========
    // 배율을 적용하여 상품 객체 생성

    static void initProducts() {
        // 음료 (1박스 = 24개)
        cola = new Product("코카콜라", 800, 1500 * priceMultiplier, 7, 24);
        cider = new Product("칠성사이다", 800, 1500 * priceMultiplier, 6, 24);
        water = new Product("삼다수", 400, 1000 * priceMultiplier, 5, 24);
        pocari = new Product("포카리스웨트", 1200, 2000 * priceMultiplier, 6, 24);
        ipro = new Product("이프로", 1000, 1800 * priceMultiplier, 5, 24);

        // 맥주 (1박스 = 24개)
        cass = new Product("카스", 1500, 3000 * priceMultiplier, 8, 24);
        terra = new Product("테라", 1600, 3200 * priceMultiplier, 8, 24);
        hite = new Product("하이트", 1400, 2800 * priceMultiplier, 7, 24);

        // 소주 (1박스 = 20병)
        chamisul = new Product("참이슬", 1200, 2500 * priceMultiplier, 9, 20);
        cheumcherum = new Product("처음처럼", 1200, 2500 * priceMultiplier, 8, 20);
        jinro = new Product("진로", 1300, 2600 * priceMultiplier, 7, 20);

        // 간식/안주 (1박스 = 20개)
        driedSquid = new Product("마른오징어", 3000, 6000 * priceMultiplier, 6, 20);
        peanut = new Product("땅콩", 2000, 4000 * priceMultiplier, 5, 20);
        chip = new Product("감자칩", 1500, 3000 * priceMultiplier, 6, 20);

        // 고기 (1판 = 10팩)
        samgyupsal = new Product("삼겹살", 8000, 15000 * priceMultiplier, 10, 10);
        moksal = new Product("목살", 9000, 16000 * priceMultiplier, 9, 10);
        sausage = new Product("소세지", 3000, 6000 * priceMultiplier, 7, 10);

        // 해수욕 용품 (1묶음 = 5개)
        tube = new Product("튜브", 5000, 15000 * priceMultiplier, 7, 5);
        sunscreen = new Product("선크림", 8000, 20000 * priceMultiplier, 8, 5);
        beachBall = new Product("비치볼", 3000, 8000 * priceMultiplier, 5, 5);

        // 식재료 (1박스 = 10개)
        ssamjang = new Product("쌈장", 2000, 4000 * priceMultiplier, 6, 10);
        lettuce = new Product("상추", 2000, 4000 * priceMultiplier, 7, 10);
        kimchi = new Product("김치", 3000, 6000 * priceMultiplier, 5, 10);

        // 라면 (1박스 = 40개)
        shinRamen = new Product("신라면", 800, 1500 * priceMultiplier, 8, 40);
        jinRamen = new Product("진라면", 700, 1400 * priceMultiplier, 7, 40);
        neoguri = new Product("너구리", 800, 1500 * priceMultiplier, 6, 40);

        // 아이스크림 (1박스 = 24개)
        melona = new Product("메로나", 500, 1200 * priceMultiplier, 7, 24);
        screwBar = new Product("스크류바", 600, 1300 * priceMultiplier, 6, 24);
        fishBread = new Product("붕어싸만코", 800, 1500 * priceMultiplier, 6, 24);

        // 기타 (1박스 = 10개)
        firework = new Product("폭죽", 5000, 15000 * priceMultiplier, 9, 10);
    }
}
