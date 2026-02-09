import java.util.Scanner;

public class Main {

    // ========== 상수 ==========

    static final int INIT_MONEY = 50000000;           // 초기 자본 5000만원
    static final int GOAL_MONEY = 300000000;          // 목표 금액 3억원
    static final int MAX_SLOT = 30;                   // 매대 최대 슬롯
    static final int DEFAULT_PRICE_MULTIPLIER = 3;   // 기본 가격 배율

    // 손님 유형별 선호 상품 개수
    static final int FAMILY_ITEM_COUNT = 6;           // 가족: 고기, 음료, 식재료
    static final int COUPLE_ITEM_COUNT = 6;           // 커플: 술, 안주
    static final int FRIENDS_ITEM_COUNT = 8;          // 친구들: 다양하게
    static final int SOLO_ITEM_COUNT = 6;             // 혼자: 라면, 맥주, 아이스크림

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
            // 아침: 도매상, 영업, 재고 확인 모두 가능
            System.out.println("[1] 도매상 가기 (상품 입고)");
            System.out.println("[2] 영업 시작");
            System.out.println("[3] 현재 재고 확인");
        } else {
            // 오후: 영업, 재고 확인만 가능
            System.out.println("[1] (도매상 마감)");
            System.out.println("[2] 영업 시작");
            System.out.println("[3] 현재 재고 확인");
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
        System.out.println("           [ 현재 재고 ]");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", usedSlot, MAX_SLOT);
        System.out.println();

        // 재고가 있는 상품만 출력
        int totalStock = 0;

        // 음료
        if (cola.stock > 0 || cider.stock > 0 || water.stock > 0 || pocari.stock > 0 || ipro.stock > 0) {
            System.out.println("--- 음료 ---");
            if (cola.stock > 0) {
                System.out.printf("  %s: %d개%n", cola.name, cola.stock);
                totalStock = totalStock + cola.stock;
            }
            if (cider.stock > 0) {
                System.out.printf("  %s: %d개%n", cider.name, cider.stock);
                totalStock = totalStock + cider.stock;
            }
            if (water.stock > 0) {
                System.out.printf("  %s: %d개%n", water.name, water.stock);
                totalStock = totalStock + water.stock;
            }
            if (pocari.stock > 0) {
                System.out.printf("  %s: %d개%n", pocari.name, pocari.stock);
                totalStock = totalStock + pocari.stock;
            }
            if (ipro.stock > 0) {
                System.out.printf("  %s: %d개%n", ipro.name, ipro.stock);
                totalStock = totalStock + ipro.stock;
            }
        }

        // 맥주
        if (cass.stock > 0 || terra.stock > 0 || hite.stock > 0) {
            System.out.println("--- 맥주 ---");
            if (cass.stock > 0) {
                System.out.printf("  %s: %d개%n", cass.name, cass.stock);
                totalStock = totalStock + cass.stock;
            }
            if (terra.stock > 0) {
                System.out.printf("  %s: %d개%n", terra.name, terra.stock);
                totalStock = totalStock + terra.stock;
            }
            if (hite.stock > 0) {
                System.out.printf("  %s: %d개%n", hite.name, hite.stock);
                totalStock = totalStock + hite.stock;
            }
        }

        // 소주
        if (chamisul.stock > 0 || cheumcherum.stock > 0 || jinro.stock > 0) {
            System.out.println("--- 소주 ---");
            if (chamisul.stock > 0) {
                System.out.printf("  %s: %d개%n", chamisul.name, chamisul.stock);
                totalStock = totalStock + chamisul.stock;
            }
            if (cheumcherum.stock > 0) {
                System.out.printf("  %s: %d개%n", cheumcherum.name, cheumcherum.stock);
                totalStock = totalStock + cheumcherum.stock;
            }
            if (jinro.stock > 0) {
                System.out.printf("  %s: %d개%n", jinro.name, jinro.stock);
                totalStock = totalStock + jinro.stock;
            }
        }

        // 간식/안주
        if (driedSquid.stock > 0 || peanut.stock > 0 || chip.stock > 0) {
            System.out.println("--- 간식/안주 ---");
            if (driedSquid.stock > 0) {
                System.out.printf("  %s: %d개%n", driedSquid.name, driedSquid.stock);
                totalStock = totalStock + driedSquid.stock;
            }
            if (peanut.stock > 0) {
                System.out.printf("  %s: %d개%n", peanut.name, peanut.stock);
                totalStock = totalStock + peanut.stock;
            }
            if (chip.stock > 0) {
                System.out.printf("  %s: %d개%n", chip.name, chip.stock);
                totalStock = totalStock + chip.stock;
            }
        }

        // 고기
        if (samgyupsal.stock > 0 || moksal.stock > 0 || sausage.stock > 0) {
            System.out.println("--- 고기 ---");
            if (samgyupsal.stock > 0) {
                System.out.printf("  %s: %d개%n", samgyupsal.name, samgyupsal.stock);
                totalStock = totalStock + samgyupsal.stock;
            }
            if (moksal.stock > 0) {
                System.out.printf("  %s: %d개%n", moksal.name, moksal.stock);
                totalStock = totalStock + moksal.stock;
            }
            if (sausage.stock > 0) {
                System.out.printf("  %s: %d개%n", sausage.name, sausage.stock);
                totalStock = totalStock + sausage.stock;
            }
        }

        // 해수욕 용품
        if (tube.stock > 0 || sunscreen.stock > 0 || beachBall.stock > 0) {
            System.out.println("--- 해수욕 용품 ---");
            if (tube.stock > 0) {
                System.out.printf("  %s: %d개%n", tube.name, tube.stock);
                totalStock = totalStock + tube.stock;
            }
            if (sunscreen.stock > 0) {
                System.out.printf("  %s: %d개%n", sunscreen.name, sunscreen.stock);
                totalStock = totalStock + sunscreen.stock;
            }
            if (beachBall.stock > 0) {
                System.out.printf("  %s: %d개%n", beachBall.name, beachBall.stock);
                totalStock = totalStock + beachBall.stock;
            }
        }

        // 식재료
        if (ssamjang.stock > 0 || lettuce.stock > 0 || kimchi.stock > 0) {
            System.out.println("--- 식재료 ---");
            if (ssamjang.stock > 0) {
                System.out.printf("  %s: %d개%n", ssamjang.name, ssamjang.stock);
                totalStock = totalStock + ssamjang.stock;
            }
            if (lettuce.stock > 0) {
                System.out.printf("  %s: %d개%n", lettuce.name, lettuce.stock);
                totalStock = totalStock + lettuce.stock;
            }
            if (kimchi.stock > 0) {
                System.out.printf("  %s: %d개%n", kimchi.name, kimchi.stock);
                totalStock = totalStock + kimchi.stock;
            }
        }

        // 라면
        if (shinRamen.stock > 0 || jinRamen.stock > 0 || neoguri.stock > 0) {
            System.out.println("--- 라면 ---");
            if (shinRamen.stock > 0) {
                System.out.printf("  %s: %d개%n", shinRamen.name, shinRamen.stock);
                totalStock = totalStock + shinRamen.stock;
            }
            if (jinRamen.stock > 0) {
                System.out.printf("  %s: %d개%n", jinRamen.name, jinRamen.stock);
                totalStock = totalStock + jinRamen.stock;
            }
            if (neoguri.stock > 0) {
                System.out.printf("  %s: %d개%n", neoguri.name, neoguri.stock);
                totalStock = totalStock + neoguri.stock;
            }
        }

        // 아이스크림
        if (melona.stock > 0 || screwBar.stock > 0 || fishBread.stock > 0) {
            System.out.println("--- 아이스크림 ---");
            if (melona.stock > 0) {
                System.out.printf("  %s: %d개%n", melona.name, melona.stock);
                totalStock = totalStock + melona.stock;
            }
            if (screwBar.stock > 0) {
                System.out.printf("  %s: %d개%n", screwBar.name, screwBar.stock);
                totalStock = totalStock + screwBar.stock;
            }
            if (fishBread.stock > 0) {
                System.out.printf("  %s: %d개%n", fishBread.name, fishBread.stock);
                totalStock = totalStock + fishBread.stock;
            }
        }

        // 기타
        if (firework.stock > 0) {
            System.out.println("--- 기타 ---");
            System.out.printf("  %s: %d개%n", firework.name, firework.stock);
            totalStock = totalStock + firework.stock;
        }

        // 재고 없음 메시지
        if (totalStock == 0) {
            System.out.println("재고가 없습니다. 도매상에서 상품을 입고하세요!");
        }

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("총 재고: %d개%n", totalStock);
        System.out.println("----------------------------------------");

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
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
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cola.name, cola.buyPrice, cola.sellPrice, cola.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cider.name, cider.buyPrice, cider.sellPrice, cider.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", water.name, water.buyPrice, water.sellPrice, water.stock);
                System.out.printf("4. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", pocari.name, pocari.buyPrice, pocari.sellPrice, pocari.stock);
                System.out.printf("5. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", ipro.name, ipro.buyPrice, ipro.sellPrice, ipro.stock);
            } else if (category == 2) {
                // 맥주
                System.out.println("========================================");
                System.out.println("            [ 맥주 ] (1박스=24개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cass.name, cass.buyPrice, cass.sellPrice, cass.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", terra.name, terra.buyPrice, terra.sellPrice, terra.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", hite.name, hite.buyPrice, hite.sellPrice, hite.stock);
            } else if (category == 3) {
                // 소주
                System.out.println("========================================");
                System.out.println("            [ 소주 ] (1박스=20병)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", chamisul.name, chamisul.buyPrice, chamisul.sellPrice, chamisul.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cheumcherum.name, cheumcherum.buyPrice, cheumcherum.sellPrice, cheumcherum.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", jinro.name, jinro.buyPrice, jinro.sellPrice, jinro.stock);
            } else if (category == 4) {
                // 간식/안주
                System.out.println("========================================");
                System.out.println("         [ 간식/안주 ] (1박스=20개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", driedSquid.name, driedSquid.buyPrice, driedSquid.sellPrice, driedSquid.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", peanut.name, peanut.buyPrice, peanut.sellPrice, peanut.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", chip.name, chip.buyPrice, chip.sellPrice, chip.stock);
            } else if (category == 5) {
                // 고기
                System.out.println("========================================");
                System.out.println("            [ 고기 ] (1판=10팩)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", samgyupsal.name, samgyupsal.buyPrice, samgyupsal.sellPrice, samgyupsal.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", moksal.name, moksal.buyPrice, moksal.sellPrice, moksal.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", sausage.name, sausage.buyPrice, sausage.sellPrice, sausage.stock);
            } else if (category == 6) {
                // 해수욕 용품
                System.out.println("========================================");
                System.out.println("        [ 해수욕용품 ] (1묶음=5개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", tube.name, tube.buyPrice, tube.sellPrice, tube.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", sunscreen.name, sunscreen.buyPrice, sunscreen.sellPrice, sunscreen.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", beachBall.name, beachBall.buyPrice, beachBall.sellPrice, beachBall.stock);
            } else if (category == 7) {
                // 식재료
                System.out.println("========================================");
                System.out.println("          [ 식재료 ] (1박스=10개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", ssamjang.name, ssamjang.buyPrice, ssamjang.sellPrice, ssamjang.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", lettuce.name, lettuce.buyPrice, lettuce.sellPrice, lettuce.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", kimchi.name, kimchi.buyPrice, kimchi.sellPrice, kimchi.stock);
            } else if (category == 8) {
                // 라면
                System.out.println("========================================");
                System.out.println("            [ 라면 ] (1박스=40개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", shinRamen.name, shinRamen.buyPrice, shinRamen.sellPrice, shinRamen.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", jinRamen.name, jinRamen.buyPrice, jinRamen.sellPrice, jinRamen.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", neoguri.name, neoguri.buyPrice, neoguri.sellPrice, neoguri.stock);
            } else if (category == 9) {
                // 아이스크림
                System.out.println("========================================");
                System.out.println("        [ 아이스크림 ] (1박스=24개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", melona.name, melona.buyPrice, melona.sellPrice, melona.stock);
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", screwBar.name, screwBar.buyPrice, screwBar.sellPrice, screwBar.stock);
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", fishBread.name, fishBread.buyPrice, fishBread.sellPrice, fishBread.stock);
            } else if (category == 10) {
                // 기타
                System.out.println("========================================");
                System.out.println("            [ 기타 ] (1박스=10개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", firework.name, firework.buyPrice, firework.sellPrice, firework.stock);
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

        // 새 상품이면 (재고가 0이면) 슬롯 체크
        boolean isNewProduct = (product.stock == 0);
        if (isNewProduct && usedSlot >= MAX_SLOT) {
            System.out.println("[!!] 매대 공간이 부족합니다. (사용: " + usedSlot + "/" + MAX_SLOT + "칸)");
            return;
        }

        int totalCost = product.buyPrice * quantity;

        // 자본 체크
        if (totalCost > money) {
            System.out.println("[!!] 자본이 부족합니다. (필요: " + String.format("%,d", totalCost) + "원)");
            return;
        }

        // 구매 처리
        money = money - totalCost;
        product.addStock(quantity);

        // 새 상품이면 슬롯 1칸 사용
        if (isNewProduct) {
            usedSlot = usedSlot + 1;
        }

        System.out.println("[OK] " + product.name + " " + quantity + "개 구매 완료! (-" + String.format("%,d", totalCost) + "원)");
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
            System.out.printf("1. %s (재고: %d)%n", cola.name, cola.stock);
            System.out.printf("2. %s (재고: %d)%n", cider.name, cider.stock);
            System.out.printf("3. %s (재고: %d)%n", water.name, water.stock);
            System.out.printf("4. %s (재고: %d)%n", pocari.name, pocari.stock);
            System.out.printf("5. %s (재고: %d)%n", ipro.name, ipro.stock);
        } else if (category == 2) {
            System.out.println("[ 맥주 ]");
            System.out.printf("1. %s (재고: %d)%n", cass.name, cass.stock);
            System.out.printf("2. %s (재고: %d)%n", terra.name, terra.stock);
            System.out.printf("3. %s (재고: %d)%n", hite.name, hite.stock);
        } else if (category == 3) {
            System.out.println("[ 소주 ]");
            System.out.printf("1. %s (재고: %d)%n", chamisul.name, chamisul.stock);
            System.out.printf("2. %s (재고: %d)%n", cheumcherum.name, cheumcherum.stock);
            System.out.printf("3. %s (재고: %d)%n", jinro.name, jinro.stock);
        } else if (category == 4) {
            System.out.println("[ 간식/안주 ]");
            System.out.printf("1. %s (재고: %d)%n", driedSquid.name, driedSquid.stock);
            System.out.printf("2. %s (재고: %d)%n", peanut.name, peanut.stock);
            System.out.printf("3. %s (재고: %d)%n", chip.name, chip.stock);
        } else if (category == 5) {
            System.out.println("[ 고기 ]");
            System.out.printf("1. %s (재고: %d)%n", samgyupsal.name, samgyupsal.stock);
            System.out.printf("2. %s (재고: %d)%n", moksal.name, moksal.stock);
            System.out.printf("3. %s (재고: %d)%n", sausage.name, sausage.stock);
        } else if (category == 6) {
            System.out.println("[ 해수욕용품 ]");
            System.out.printf("1. %s (재고: %d)%n", tube.name, tube.stock);
            System.out.printf("2. %s (재고: %d)%n", sunscreen.name, sunscreen.stock);
            System.out.printf("3. %s (재고: %d)%n", beachBall.name, beachBall.stock);
        } else if (category == 7) {
            System.out.println("[ 식재료 ]");
            System.out.printf("1. %s (재고: %d)%n", ssamjang.name, ssamjang.stock);
            System.out.printf("2. %s (재고: %d)%n", lettuce.name, lettuce.stock);
            System.out.printf("3. %s (재고: %d)%n", kimchi.name, kimchi.stock);
        } else if (category == 8) {
            System.out.println("[ 라면 ]");
            System.out.printf("1. %s (재고: %d)%n", shinRamen.name, shinRamen.stock);
            System.out.printf("2. %s (재고: %d)%n", jinRamen.name, jinRamen.stock);
            System.out.printf("3. %s (재고: %d)%n", neoguri.name, neoguri.stock);
        } else if (category == 9) {
            System.out.println("[ 아이스크림 ]");
            System.out.printf("1. %s (재고: %d)%n", melona.name, melona.stock);
            System.out.printf("2. %s (재고: %d)%n", screwBar.name, screwBar.stock);
            System.out.printf("3. %s (재고: %d)%n", fishBread.name, fishBread.stock);
        } else if (category == 10) {
            System.out.println("[ 기타 ]");
            System.out.printf("1. %s (재고: %d)%n", firework.name, firework.stock);
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
        // 재고가 임계값보다 많으면 주문 안 함
        if (product.stock > threshold) {
            return 0;
        }

        int boxSize = product.boxSize;
        int cost = product.buyPrice * boxSize;

        // 자본 체크
        if (cost > money) {
            System.out.printf(" - %s: 자본 부족 (필요: %,d원)%n", product.name, cost);
            return 0;
        }

        // 새 상품이면 (재고가 0이면) 슬롯 체크
        boolean isNewProduct = (product.stock == 0);
        if (isNewProduct && usedSlot >= MAX_SLOT) {
            System.out.printf(" - %s: 매대 공간 부족 (사용: %d/%d칸)%n", product.name, usedSlot, MAX_SLOT);
            return 0;
        }

        // 주문 처리
        money = money - cost;
        product.addStock(boxSize);

        // 새 상품이면 슬롯 1칸 사용
        if (isNewProduct) {
            usedSlot = usedSlot + 1;
        }

        System.out.printf(" - %s 1박스(%d개) 구매 (-%,d원)%n", product.name, boxSize, cost);

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
        int successCount = 0;    // 판매 성공 횟수
        int failCount = 0;       // 판매 실패 횟수

        // 손님 응대 루프
        for (int i = 1; i <= todayCustomers; i++) {

            // 랜덤 손님 유형 (0: 가족, 1: 커플, 2: 친구들, 3: 혼자)
            int customerType = (int)(Math.random() * 4);
            String customerName;
            String customerMessage;

            if (customerType == 0) {
                customerName = "가족 손님";
                customerMessage = "바베큐 하려고 왔어요~";
            } else if (customerType == 1) {
                customerName = "커플 손님";
                customerMessage = "술 한잔 하려고요~";
            } else if (customerType == 2) {
                customerName = "친구들";
                customerMessage = "놀러왔어요!";
            } else {
                customerName = "혼자 온 손님";
                customerMessage = "간단히 좀 살게요.";
            }

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.printf("[ 손님 %d/%d - %s ]%n", i, todayCustomers, customerName);
            System.out.printf("\"%s\"%n", customerMessage);
            System.out.println();

            // 손님 유형별 원하는 상품 결정
            String wantItem;
            int wantAmount = 1 + (int)(Math.random() * 3);  // 1~3개
            int itemStock;
            int itemSellPrice;
            int itemBuyPrice;

            if (customerType == 0) {
                // 가족: 고기, 음료, 식재료 선호
                int itemChoice = (int)(Math.random() * FAMILY_ITEM_COUNT);
                if (itemChoice == 0) {
                    wantItem = samgyupsal.name;
                    itemStock = samgyupsal.stock;
                    itemSellPrice = samgyupsal.sellPrice;
                    itemBuyPrice = samgyupsal.buyPrice;
                } else if (itemChoice == 1) {
                    wantItem = moksal.name;
                    itemStock = moksal.stock;
                    itemSellPrice = moksal.sellPrice;
                    itemBuyPrice = moksal.buyPrice;
                } else if (itemChoice == 2) {
                    wantItem = cola.name;
                    itemStock = cola.stock;
                    itemSellPrice = cola.sellPrice;
                    itemBuyPrice = cola.buyPrice;
                } else if (itemChoice == 3) {
                    wantItem = cider.name;
                    itemStock = cider.stock;
                    itemSellPrice = cider.sellPrice;
                    itemBuyPrice = cider.buyPrice;
                } else if (itemChoice == 4) {
                    wantItem = ssamjang.name;
                    itemStock = ssamjang.stock;
                    itemSellPrice = ssamjang.sellPrice;
                    itemBuyPrice = ssamjang.buyPrice;
                } else {
                    wantItem = lettuce.name;
                    itemStock = lettuce.stock;
                    itemSellPrice = lettuce.sellPrice;
                    itemBuyPrice = lettuce.buyPrice;
                }

            } else if (customerType == 1) {
                // 커플: 술, 안주 선호
                int itemChoice = (int)(Math.random() * COUPLE_ITEM_COUNT);
                if (itemChoice == 0) {
                    wantItem = chamisul.name;
                    itemStock = chamisul.stock;
                    itemSellPrice = chamisul.sellPrice;
                    itemBuyPrice = chamisul.buyPrice;
                } else if (itemChoice == 1) {
                    wantItem = cheumcherum.name;
                    itemStock = cheumcherum.stock;
                    itemSellPrice = cheumcherum.sellPrice;
                    itemBuyPrice = cheumcherum.buyPrice;
                } else if (itemChoice == 2) {
                    wantItem = cass.name;
                    itemStock = cass.stock;
                    itemSellPrice = cass.sellPrice;
                    itemBuyPrice = cass.buyPrice;
                } else if (itemChoice == 3) {
                    wantItem = terra.name;
                    itemStock = terra.stock;
                    itemSellPrice = terra.sellPrice;
                    itemBuyPrice = terra.buyPrice;
                } else if (itemChoice == 4) {
                    wantItem = driedSquid.name;
                    itemStock = driedSquid.stock;
                    itemSellPrice = driedSquid.sellPrice;
                    itemBuyPrice = driedSquid.buyPrice;
                } else {
                    wantItem = peanut.name;
                    itemStock = peanut.stock;
                    itemSellPrice = peanut.sellPrice;
                    itemBuyPrice = peanut.buyPrice;
                }

            } else if (customerType == 2) {
                // 친구들: 다양하게
                int itemChoice = (int)(Math.random() * FRIENDS_ITEM_COUNT);
                if (itemChoice == 0) {
                    wantItem = chip.name;
                    itemStock = chip.stock;
                    itemSellPrice = chip.sellPrice;
                    itemBuyPrice = chip.buyPrice;
                } else if (itemChoice == 1) {
                    wantItem = firework.name;
                    itemStock = firework.stock;
                    itemSellPrice = firework.sellPrice;
                    itemBuyPrice = firework.buyPrice;
                } else if (itemChoice == 2) {
                    wantItem = tube.name;
                    itemStock = tube.stock;
                    itemSellPrice = tube.sellPrice;
                    itemBuyPrice = tube.buyPrice;
                } else if (itemChoice == 3) {
                    wantItem = beachBall.name;
                    itemStock = beachBall.stock;
                    itemSellPrice = beachBall.sellPrice;
                    itemBuyPrice = beachBall.buyPrice;
                } else if (itemChoice == 4) {
                    wantItem = sunscreen.name;
                    itemStock = sunscreen.stock;
                    itemSellPrice = sunscreen.sellPrice;
                    itemBuyPrice = sunscreen.buyPrice;
                } else if (itemChoice == 5) {
                    wantItem = cass.name;
                    itemStock = cass.stock;
                    itemSellPrice = cass.sellPrice;
                    itemBuyPrice = cass.buyPrice;
                } else if (itemChoice == 6) {
                    wantItem = melona.name;
                    itemStock = melona.stock;
                    itemSellPrice = melona.sellPrice;
                    itemBuyPrice = melona.buyPrice;
                } else {
                    wantItem = sausage.name;
                    itemStock = sausage.stock;
                    itemSellPrice = sausage.sellPrice;
                    itemBuyPrice = sausage.buyPrice;
                }

            } else {
                // 혼자: 라면, 맥주, 아이스크림 선호
                int itemChoice = (int)(Math.random() * SOLO_ITEM_COUNT);
                if (itemChoice == 0) {
                    wantItem = shinRamen.name;
                    itemStock = shinRamen.stock;
                    itemSellPrice = shinRamen.sellPrice;
                    itemBuyPrice = shinRamen.buyPrice;
                } else if (itemChoice == 1) {
                    wantItem = jinRamen.name;
                    itemStock = jinRamen.stock;
                    itemSellPrice = jinRamen.sellPrice;
                    itemBuyPrice = jinRamen.buyPrice;
                } else if (itemChoice == 2) {
                    wantItem = neoguri.name;
                    itemStock = neoguri.stock;
                    itemSellPrice = neoguri.sellPrice;
                    itemBuyPrice = neoguri.buyPrice;
                } else if (itemChoice == 3) {
                    wantItem = hite.name;
                    itemStock = hite.stock;
                    itemSellPrice = hite.sellPrice;
                    itemBuyPrice = hite.buyPrice;
                } else if (itemChoice == 4) {
                    wantItem = screwBar.name;
                    itemStock = screwBar.stock;
                    itemSellPrice = screwBar.sellPrice;
                    itemBuyPrice = screwBar.buyPrice;
                } else {
                    wantItem = fishBread.name;
                    itemStock = fishBread.stock;
                    itemSellPrice = fishBread.sellPrice;
                    itemBuyPrice = fishBread.buyPrice;
                }
            }

            // 원하는 상품 출력
            System.out.printf("원하는 상품: %s %d개%n", wantItem, wantAmount);

            // 재고 확인 및 판매 처리
            if (itemStock >= wantAmount) {
                // 판매 성공
                int saleAmount = itemSellPrice * wantAmount;
                int profitAmount = (itemSellPrice - itemBuyPrice) * wantAmount;

                // 재고 차감 (상품별로 처리) + 재고가 0이 되면 슬롯 반환
                if (wantItem.equals(cola.name)) {
                    cola.removeStock(wantAmount);
                    if (cola.stock == 0) usedSlot--;
                } else if (wantItem.equals(cider.name)) {
                    cider.removeStock(wantAmount);
                    if (cider.stock == 0) usedSlot--;
                } else if (wantItem.equals(water.name)) {
                    water.removeStock(wantAmount);
                    if (water.stock == 0) usedSlot--;
                } else if (wantItem.equals(pocari.name)) {
                    pocari.removeStock(wantAmount);
                    if (pocari.stock == 0) usedSlot--;
                } else if (wantItem.equals(ipro.name)) {
                    ipro.removeStock(wantAmount);
                    if (ipro.stock == 0) usedSlot--;
                } else if (wantItem.equals(cass.name)) {
                    cass.removeStock(wantAmount);
                    if (cass.stock == 0) usedSlot--;
                } else if (wantItem.equals(terra.name)) {
                    terra.removeStock(wantAmount);
                    if (terra.stock == 0) usedSlot--;
                } else if (wantItem.equals(hite.name)) {
                    hite.removeStock(wantAmount);
                    if (hite.stock == 0) usedSlot--;
                } else if (wantItem.equals(chamisul.name)) {
                    chamisul.removeStock(wantAmount);
                    if (chamisul.stock == 0) usedSlot--;
                } else if (wantItem.equals(cheumcherum.name)) {
                    cheumcherum.removeStock(wantAmount);
                    if (cheumcherum.stock == 0) usedSlot--;
                } else if (wantItem.equals(jinro.name)) {
                    jinro.removeStock(wantAmount);
                    if (jinro.stock == 0) usedSlot--;
                } else if (wantItem.equals(driedSquid.name)) {
                    driedSquid.removeStock(wantAmount);
                    if (driedSquid.stock == 0) usedSlot--;
                } else if (wantItem.equals(peanut.name)) {
                    peanut.removeStock(wantAmount);
                    if (peanut.stock == 0) usedSlot--;
                } else if (wantItem.equals(chip.name)) {
                    chip.removeStock(wantAmount);
                    if (chip.stock == 0) usedSlot--;
                } else if (wantItem.equals(samgyupsal.name)) {
                    samgyupsal.removeStock(wantAmount);
                    if (samgyupsal.stock == 0) usedSlot--;
                } else if (wantItem.equals(moksal.name)) {
                    moksal.removeStock(wantAmount);
                    if (moksal.stock == 0) usedSlot--;
                } else if (wantItem.equals(sausage.name)) {
                    sausage.removeStock(wantAmount);
                    if (sausage.stock == 0) usedSlot--;
                } else if (wantItem.equals(tube.name)) {
                    tube.removeStock(wantAmount);
                    if (tube.stock == 0) usedSlot--;
                } else if (wantItem.equals(sunscreen.name)) {
                    sunscreen.removeStock(wantAmount);
                    if (sunscreen.stock == 0) usedSlot--;
                } else if (wantItem.equals(beachBall.name)) {
                    beachBall.removeStock(wantAmount);
                    if (beachBall.stock == 0) usedSlot--;
                } else if (wantItem.equals(ssamjang.name)) {
                    ssamjang.removeStock(wantAmount);
                    if (ssamjang.stock == 0) usedSlot--;
                } else if (wantItem.equals(lettuce.name)) {
                    lettuce.removeStock(wantAmount);
                    if (lettuce.stock == 0) usedSlot--;
                } else if (wantItem.equals(kimchi.name)) {
                    kimchi.removeStock(wantAmount);
                    if (kimchi.stock == 0) usedSlot--;
                } else if (wantItem.equals(shinRamen.name)) {
                    shinRamen.removeStock(wantAmount);
                    if (shinRamen.stock == 0) usedSlot--;
                } else if (wantItem.equals(jinRamen.name)) {
                    jinRamen.removeStock(wantAmount);
                    if (jinRamen.stock == 0) usedSlot--;
                } else if (wantItem.equals(neoguri.name)) {
                    neoguri.removeStock(wantAmount);
                    if (neoguri.stock == 0) usedSlot--;
                } else if (wantItem.equals(melona.name)) {
                    melona.removeStock(wantAmount);
                    if (melona.stock == 0) usedSlot--;
                } else if (wantItem.equals(screwBar.name)) {
                    screwBar.removeStock(wantAmount);
                    if (screwBar.stock == 0) usedSlot--;
                } else if (wantItem.equals(fishBread.name)) {
                    fishBread.removeStock(wantAmount);
                    if (fishBread.stock == 0) usedSlot--;
                } else if (wantItem.equals(firework.name)) {
                    firework.removeStock(wantAmount);
                    if (firework.stock == 0) usedSlot--;
                }

                // 매출/이익 누적
                money = money + saleAmount;
                todaySales = todaySales + saleAmount;
                todayProfit = todayProfit + profitAmount;
                successCount++;

                System.out.printf("[OK] 판매 완료! (+%,d원)%n", saleAmount);

            } else if (itemStock > 0) {
                // 일부만 판매 가능
                int actualAmount = itemStock;
                int saleAmount = itemSellPrice * actualAmount;
                int profitAmount = (itemSellPrice - itemBuyPrice) * actualAmount;

                // 재고 차감
                if (wantItem.equals(cola.name)) {
                    cola.removeStock(actualAmount);
                } else if (wantItem.equals(cider.name)) {
                    cider.removeStock(actualAmount);
                } else if (wantItem.equals(water.name)) {
                    water.removeStock(actualAmount);
                } else if (wantItem.equals(pocari.name)) {
                    pocari.removeStock(actualAmount);
                } else if (wantItem.equals(ipro.name)) {
                    ipro.removeStock(actualAmount);
                } else if (wantItem.equals(cass.name)) {
                    cass.removeStock(actualAmount);
                } else if (wantItem.equals(terra.name)) {
                    terra.removeStock(actualAmount);
                } else if (wantItem.equals(hite.name)) {
                    hite.removeStock(actualAmount);
                } else if (wantItem.equals(chamisul.name)) {
                    chamisul.removeStock(actualAmount);
                } else if (wantItem.equals(cheumcherum.name)) {
                    cheumcherum.removeStock(actualAmount);
                } else if (wantItem.equals(jinro.name)) {
                    jinro.removeStock(actualAmount);
                } else if (wantItem.equals(driedSquid.name)) {
                    driedSquid.removeStock(actualAmount);
                } else if (wantItem.equals(peanut.name)) {
                    peanut.removeStock(actualAmount);
                } else if (wantItem.equals(chip.name)) {
                    chip.removeStock(actualAmount);
                } else if (wantItem.equals(samgyupsal.name)) {
                    samgyupsal.removeStock(actualAmount);
                } else if (wantItem.equals(moksal.name)) {
                    moksal.removeStock(actualAmount);
                } else if (wantItem.equals(sausage.name)) {
                    sausage.removeStock(actualAmount);
                } else if (wantItem.equals(tube.name)) {
                    tube.removeStock(actualAmount);
                } else if (wantItem.equals(sunscreen.name)) {
                    sunscreen.removeStock(actualAmount);
                } else if (wantItem.equals(beachBall.name)) {
                    beachBall.removeStock(actualAmount);
                } else if (wantItem.equals(ssamjang.name)) {
                    ssamjang.removeStock(actualAmount);
                } else if (wantItem.equals(lettuce.name)) {
                    lettuce.removeStock(actualAmount);
                } else if (wantItem.equals(kimchi.name)) {
                    kimchi.removeStock(actualAmount);
                } else if (wantItem.equals(shinRamen.name)) {
                    shinRamen.removeStock(actualAmount);
                } else if (wantItem.equals(jinRamen.name)) {
                    jinRamen.removeStock(actualAmount);
                } else if (wantItem.equals(neoguri.name)) {
                    neoguri.removeStock(actualAmount);
                } else if (wantItem.equals(melona.name)) {
                    melona.removeStock(actualAmount);
                } else if (wantItem.equals(screwBar.name)) {
                    screwBar.removeStock(actualAmount);
                } else if (wantItem.equals(fishBread.name)) {
                    fishBread.removeStock(actualAmount);
                } else if (wantItem.equals(firework.name)) {
                    firework.removeStock(actualAmount);
                }

                // 재고가 0이 되었으므로 슬롯 1칸 반환
                usedSlot = usedSlot - 1;

                money = money + saleAmount;
                todaySales = todaySales + saleAmount;
                todayProfit = todayProfit + profitAmount;
                successCount++;

                System.out.printf("[--] %d개만 판매... (+%,d원)%n", actualAmount, saleAmount);

            } else {
                // 재고 없음
                failCount++;
                System.out.println("[XX] 재고 없음... 손님이 그냥 갔습니다.");
            }
        }

        // 하루 정산
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
