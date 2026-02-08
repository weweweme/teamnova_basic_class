import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // ========== 상수 ==========

        final int INIT_MONEY = 50000000;           // 초기 자본 5000만원
        final int GOAL_MONEY = 300000000;          // 목표 금액 3억원
        final int MAX_SLOT = 30;                   // 매대 최대 슬롯
        final int DEFAULT_PRICE_MULTIPLIER = 3;   // 기본 가격 배율

        // ========== 게임 변수 ==========

        int money;
        int goalMoney;
        int priceMultiplier;
        int usedSlot = 0;
        int day = 1;

        Scanner scanner = new Scanner(System.in);

        // ========== 게임 시작 화면 ==========

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

        // ========== 상품 객체 생성 (배율 적용) ==========
        // sellPrice에만 배율 적용 (매입가는 현실 가격, 판매가는 배율 적용)

        // 음료
        Cola cola = new Cola("코카콜라", 800, 1500 * priceMultiplier, 7);
        Cider cider = new Cider("칠성사이다", 800, 1500 * priceMultiplier, 6);
        Water water = new Water("삼다수", 400, 1000 * priceMultiplier, 5);
        Pocari pocari = new Pocari("포카리스웨트", 1200, 2000 * priceMultiplier, 6);
        Ipro ipro = new Ipro("이프로", 1000, 1800 * priceMultiplier, 5);

        // 맥주
        Cass cass = new Cass("카스", 1500, 3000 * priceMultiplier, 8);
        Terra terra = new Terra("테라", 1600, 3200 * priceMultiplier, 8);
        Hite hite = new Hite("하이트", 1400, 2800 * priceMultiplier, 7);

        // 소주
        Chamisul chamisul = new Chamisul("참이슬", 1200, 2500 * priceMultiplier, 9);
        Cheumcherum cheumcherum = new Cheumcherum("처음처럼", 1200, 2500 * priceMultiplier, 8);
        Jinro jinro = new Jinro("진로", 1300, 2600 * priceMultiplier, 7);

        // 간식/안주
        DriedSquid driedSquid = new DriedSquid("마른오징어", 3000, 6000 * priceMultiplier, 6);
        Peanut peanut = new Peanut("땅콩", 2000, 4000 * priceMultiplier, 5);
        Chip chip = new Chip("감자칩", 1500, 3000 * priceMultiplier, 6);

        // 고기
        Samgyupsal samgyupsal = new Samgyupsal("삼겹살", 8000, 15000 * priceMultiplier, 10);
        Moksal moksal = new Moksal("목살", 9000, 16000 * priceMultiplier, 9);
        Sausage sausage = new Sausage("소세지", 3000, 6000 * priceMultiplier, 7);

        // 해수욕 용품
        Tube tube = new Tube("튜브", 5000, 15000 * priceMultiplier, 7);
        Sunscreen sunscreen = new Sunscreen("선크림", 8000, 20000 * priceMultiplier, 8);
        BeachBall beachBall = new BeachBall("비치볼", 3000, 8000 * priceMultiplier, 5);

        // 식재료
        Ssamjang ssamjang = new Ssamjang("쌈장", 2000, 4000 * priceMultiplier, 6);
        Lettuce lettuce = new Lettuce("상추", 2000, 4000 * priceMultiplier, 7);
        Kimchi kimchi = new Kimchi("김치", 3000, 6000 * priceMultiplier, 5);

        // 라면
        ShinRamen shinRamen = new ShinRamen("신라면", 800, 1500 * priceMultiplier, 8);
        JinRamen jinRamen = new JinRamen("진라면", 700, 1400 * priceMultiplier, 7);
        Neoguri neoguri = new Neoguri("너구리", 800, 1500 * priceMultiplier, 6);

        // 아이스크림
        Melona melona = new Melona("메로나", 500, 1200 * priceMultiplier, 7);
        ScrewBar screwBar = new ScrewBar("스크류바", 600, 1300 * priceMultiplier, 6);
        FishBread fishBread = new FishBread("붕어싸만코", 800, 1500 * priceMultiplier, 6);

        // 기타
        Firework firework = new Firework("폭죽", 5000, 15000 * priceMultiplier, 9);

        // ========== 게임 루프 ==========

        boolean playing = true;

        while (playing) {

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
                break;
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
                break;
            }

            // 하루 시작 메뉴
            System.out.println();
            System.out.println("========================================");
            System.out.println("          [  " + day + "일차 - 아침  ]");
            System.out.println("========================================");
            System.out.println("현재 자본: " + String.format("%,d", money) + "원");
            System.out.println("매대 현황: " + usedSlot + " / " + MAX_SLOT + "칸");
            System.out.println();
            System.out.println("[1] 도매상 가기 (상품 입고)");
            System.out.println("[2] 영업 시작");
            System.out.println("[3] 현재 재고 확인");
            System.out.println("[0] 게임 종료");
            System.out.print(">> ");

            int choice = scanner.nextInt();

            if (choice == 1) {
                // 도매상
                boolean shopping = true;

                while (shopping) {
                    System.out.println();
                    System.out.println("========================================");
                    System.out.println("            [ 도매상 ]");
                    System.out.println("========================================");
                    System.out.println("현재 자본: " + String.format("%,d", money) + "원");
                    System.out.println("매대: " + usedSlot + " / " + MAX_SLOT + "칸");
                    System.out.println();

                    // 상품 목록 출력
                    System.out.println("--- 음료 ---");
                    System.out.println("1. " + cola.name + " | 매입 " + String.format("%,d", cola.buyPrice) + "원 | 판매 " + String.format("%,d", cola.sellPrice) + "원");
                    System.out.println("2. " + cider.name + " | 매입 " + String.format("%,d", cider.buyPrice) + "원 | 판매 " + String.format("%,d", cider.sellPrice) + "원");
                    System.out.println("3. " + water.name + " | 매입 " + String.format("%,d", water.buyPrice) + "원 | 판매 " + String.format("%,d", water.sellPrice) + "원");
                    System.out.println("4. " + pocari.name + " | 매입 " + String.format("%,d", pocari.buyPrice) + "원 | 판매 " + String.format("%,d", pocari.sellPrice) + "원");
                    System.out.println("5. " + ipro.name + " | 매입 " + String.format("%,d", ipro.buyPrice) + "원 | 판매 " + String.format("%,d", ipro.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 맥주 ---");
                    System.out.println("6. " + cass.name + " | 매입 " + String.format("%,d", cass.buyPrice) + "원 | 판매 " + String.format("%,d", cass.sellPrice) + "원");
                    System.out.println("7. " + terra.name + " | 매입 " + String.format("%,d", terra.buyPrice) + "원 | 판매 " + String.format("%,d", terra.sellPrice) + "원");
                    System.out.println("8. " + hite.name + " | 매입 " + String.format("%,d", hite.buyPrice) + "원 | 판매 " + String.format("%,d", hite.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 소주 ---");
                    System.out.println("9. " + chamisul.name + " | 매입 " + String.format("%,d", chamisul.buyPrice) + "원 | 판매 " + String.format("%,d", chamisul.sellPrice) + "원");
                    System.out.println("10. " + cheumcherum.name + " | 매입 " + String.format("%,d", cheumcherum.buyPrice) + "원 | 판매 " + String.format("%,d", cheumcherum.sellPrice) + "원");
                    System.out.println("11. " + jinro.name + " | 매입 " + String.format("%,d", jinro.buyPrice) + "원 | 판매 " + String.format("%,d", jinro.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 간식/안주 ---");
                    System.out.println("12. " + driedSquid.name + " | 매입 " + String.format("%,d", driedSquid.buyPrice) + "원 | 판매 " + String.format("%,d", driedSquid.sellPrice) + "원");
                    System.out.println("13. " + peanut.name + " | 매입 " + String.format("%,d", peanut.buyPrice) + "원 | 판매 " + String.format("%,d", peanut.sellPrice) + "원");
                    System.out.println("14. " + chip.name + " | 매입 " + String.format("%,d", chip.buyPrice) + "원 | 판매 " + String.format("%,d", chip.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 고기 ---");
                    System.out.println("15. " + samgyupsal.name + " | 매입 " + String.format("%,d", samgyupsal.buyPrice) + "원 | 판매 " + String.format("%,d", samgyupsal.sellPrice) + "원");
                    System.out.println("16. " + moksal.name + " | 매입 " + String.format("%,d", moksal.buyPrice) + "원 | 판매 " + String.format("%,d", moksal.sellPrice) + "원");
                    System.out.println("17. " + sausage.name + " | 매입 " + String.format("%,d", sausage.buyPrice) + "원 | 판매 " + String.format("%,d", sausage.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 해수욕 용품 ---");
                    System.out.println("18. " + tube.name + " | 매입 " + String.format("%,d", tube.buyPrice) + "원 | 판매 " + String.format("%,d", tube.sellPrice) + "원");
                    System.out.println("19. " + sunscreen.name + " | 매입 " + String.format("%,d", sunscreen.buyPrice) + "원 | 판매 " + String.format("%,d", sunscreen.sellPrice) + "원");
                    System.out.println("20. " + beachBall.name + " | 매입 " + String.format("%,d", beachBall.buyPrice) + "원 | 판매 " + String.format("%,d", beachBall.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 식재료 ---");
                    System.out.println("21. " + ssamjang.name + " | 매입 " + String.format("%,d", ssamjang.buyPrice) + "원 | 판매 " + String.format("%,d", ssamjang.sellPrice) + "원");
                    System.out.println("22. " + lettuce.name + " | 매입 " + String.format("%,d", lettuce.buyPrice) + "원 | 판매 " + String.format("%,d", lettuce.sellPrice) + "원");
                    System.out.println("23. " + kimchi.name + " | 매입 " + String.format("%,d", kimchi.buyPrice) + "원 | 판매 " + String.format("%,d", kimchi.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 라면 ---");
                    System.out.println("24. " + shinRamen.name + " | 매입 " + String.format("%,d", shinRamen.buyPrice) + "원 | 판매 " + String.format("%,d", shinRamen.sellPrice) + "원");
                    System.out.println("25. " + jinRamen.name + " | 매입 " + String.format("%,d", jinRamen.buyPrice) + "원 | 판매 " + String.format("%,d", jinRamen.sellPrice) + "원");
                    System.out.println("26. " + neoguri.name + " | 매입 " + String.format("%,d", neoguri.buyPrice) + "원 | 판매 " + String.format("%,d", neoguri.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 아이스크림 ---");
                    System.out.println("27. " + melona.name + " | 매입 " + String.format("%,d", melona.buyPrice) + "원 | 판매 " + String.format("%,d", melona.sellPrice) + "원");
                    System.out.println("28. " + screwBar.name + " | 매입 " + String.format("%,d", screwBar.buyPrice) + "원 | 판매 " + String.format("%,d", screwBar.sellPrice) + "원");
                    System.out.println("29. " + fishBread.name + " | 매입 " + String.format("%,d", fishBread.buyPrice) + "원 | 판매 " + String.format("%,d", fishBread.sellPrice) + "원");
                    System.out.println();

                    System.out.println("--- 기타 ---");
                    System.out.println("30. " + firework.name + " | 매입 " + String.format("%,d", firework.buyPrice) + "원 | 판매 " + String.format("%,d", firework.sellPrice) + "원");
                    System.out.println();

                    System.out.println("구매할 상품 번호 (0: 돌아가기)");
                    System.out.print(">> ");
                    int productChoice = scanner.nextInt();

                    if (productChoice == 0) {
                        shopping = false;

                    } else if (productChoice >= 1 && productChoice <= 30) {
                        // 수량 입력
                        System.out.print("수량 입력 >> ");
                        int quantity = scanner.nextInt();

                        // 슬롯 체크
                        int remainingSlot = MAX_SLOT - usedSlot;
                        if (quantity > remainingSlot) {
                            System.out.println("[!!] 매대 공간이 부족합니다. (남은 칸: " + remainingSlot + ")");

                        } else {
                            // 상품별 구매 처리
                            String productName = "";
                            int totalCost = 0;

                            if (productChoice == 1) {
                                productName = cola.name;
                                totalCost = cola.buyPrice * quantity;
                            } else if (productChoice == 2) {
                                productName = cider.name;
                                totalCost = cider.buyPrice * quantity;
                            } else if (productChoice == 3) {
                                productName = water.name;
                                totalCost = water.buyPrice * quantity;
                            } else if (productChoice == 4) {
                                productName = pocari.name;
                                totalCost = pocari.buyPrice * quantity;
                            } else if (productChoice == 5) {
                                productName = ipro.name;
                                totalCost = ipro.buyPrice * quantity;
                            } else if (productChoice == 6) {
                                productName = cass.name;
                                totalCost = cass.buyPrice * quantity;
                            } else if (productChoice == 7) {
                                productName = terra.name;
                                totalCost = terra.buyPrice * quantity;
                            } else if (productChoice == 8) {
                                productName = hite.name;
                                totalCost = hite.buyPrice * quantity;
                            } else if (productChoice == 9) {
                                productName = chamisul.name;
                                totalCost = chamisul.buyPrice * quantity;
                            } else if (productChoice == 10) {
                                productName = cheumcherum.name;
                                totalCost = cheumcherum.buyPrice * quantity;
                            } else if (productChoice == 11) {
                                productName = jinro.name;
                                totalCost = jinro.buyPrice * quantity;
                            } else if (productChoice == 12) {
                                productName = driedSquid.name;
                                totalCost = driedSquid.buyPrice * quantity;
                            } else if (productChoice == 13) {
                                productName = peanut.name;
                                totalCost = peanut.buyPrice * quantity;
                            } else if (productChoice == 14) {
                                productName = chip.name;
                                totalCost = chip.buyPrice * quantity;
                            } else if (productChoice == 15) {
                                productName = samgyupsal.name;
                                totalCost = samgyupsal.buyPrice * quantity;
                            } else if (productChoice == 16) {
                                productName = moksal.name;
                                totalCost = moksal.buyPrice * quantity;
                            } else if (productChoice == 17) {
                                productName = sausage.name;
                                totalCost = sausage.buyPrice * quantity;
                            } else if (productChoice == 18) {
                                productName = tube.name;
                                totalCost = tube.buyPrice * quantity;
                            } else if (productChoice == 19) {
                                productName = sunscreen.name;
                                totalCost = sunscreen.buyPrice * quantity;
                            } else if (productChoice == 20) {
                                productName = beachBall.name;
                                totalCost = beachBall.buyPrice * quantity;
                            } else if (productChoice == 21) {
                                productName = ssamjang.name;
                                totalCost = ssamjang.buyPrice * quantity;
                            } else if (productChoice == 22) {
                                productName = lettuce.name;
                                totalCost = lettuce.buyPrice * quantity;
                            } else if (productChoice == 23) {
                                productName = kimchi.name;
                                totalCost = kimchi.buyPrice * quantity;
                            } else if (productChoice == 24) {
                                productName = shinRamen.name;
                                totalCost = shinRamen.buyPrice * quantity;
                            } else if (productChoice == 25) {
                                productName = jinRamen.name;
                                totalCost = jinRamen.buyPrice * quantity;
                            } else if (productChoice == 26) {
                                productName = neoguri.name;
                                totalCost = neoguri.buyPrice * quantity;
                            } else if (productChoice == 27) {
                                productName = melona.name;
                                totalCost = melona.buyPrice * quantity;
                            } else if (productChoice == 28) {
                                productName = screwBar.name;
                                totalCost = screwBar.buyPrice * quantity;
                            } else if (productChoice == 29) {
                                productName = fishBread.name;
                                totalCost = fishBread.buyPrice * quantity;
                            } else if (productChoice == 30) {
                                productName = firework.name;
                                totalCost = firework.buyPrice * quantity;
                            }

                            // 자본 체크
                            if (totalCost > money) {
                                System.out.println("[!!] 자본이 부족합니다. (필요: " + String.format("%,d", totalCost) + "원)");

                            } else {
                                // 구매 처리
                                money = money - totalCost;
                                usedSlot = usedSlot + quantity;

                                // 재고 추가
                                if (productChoice == 1) {
                                    cola.addStock(quantity);
                                } else if (productChoice == 2) {
                                    cider.addStock(quantity);
                                } else if (productChoice == 3) {
                                    water.addStock(quantity);
                                } else if (productChoice == 4) {
                                    pocari.addStock(quantity);
                                } else if (productChoice == 5) {
                                    ipro.addStock(quantity);
                                } else if (productChoice == 6) {
                                    cass.addStock(quantity);
                                } else if (productChoice == 7) {
                                    terra.addStock(quantity);
                                } else if (productChoice == 8) {
                                    hite.addStock(quantity);
                                } else if (productChoice == 9) {
                                    chamisul.addStock(quantity);
                                } else if (productChoice == 10) {
                                    cheumcherum.addStock(quantity);
                                } else if (productChoice == 11) {
                                    jinro.addStock(quantity);
                                } else if (productChoice == 12) {
                                    driedSquid.addStock(quantity);
                                } else if (productChoice == 13) {
                                    peanut.addStock(quantity);
                                } else if (productChoice == 14) {
                                    chip.addStock(quantity);
                                } else if (productChoice == 15) {
                                    samgyupsal.addStock(quantity);
                                } else if (productChoice == 16) {
                                    moksal.addStock(quantity);
                                } else if (productChoice == 17) {
                                    sausage.addStock(quantity);
                                } else if (productChoice == 18) {
                                    tube.addStock(quantity);
                                } else if (productChoice == 19) {
                                    sunscreen.addStock(quantity);
                                } else if (productChoice == 20) {
                                    beachBall.addStock(quantity);
                                } else if (productChoice == 21) {
                                    ssamjang.addStock(quantity);
                                } else if (productChoice == 22) {
                                    lettuce.addStock(quantity);
                                } else if (productChoice == 23) {
                                    kimchi.addStock(quantity);
                                } else if (productChoice == 24) {
                                    shinRamen.addStock(quantity);
                                } else if (productChoice == 25) {
                                    jinRamen.addStock(quantity);
                                } else if (productChoice == 26) {
                                    neoguri.addStock(quantity);
                                } else if (productChoice == 27) {
                                    melona.addStock(quantity);
                                } else if (productChoice == 28) {
                                    screwBar.addStock(quantity);
                                } else if (productChoice == 29) {
                                    fishBread.addStock(quantity);
                                } else if (productChoice == 30) {
                                    firework.addStock(quantity);
                                }

                                System.out.println("[OK] " + productName + " " + quantity + "개 구매 완료! (-" + String.format("%,d", totalCost) + "원)");
                            }
                        }

                    } else {
                        System.out.println("잘못된 입력입니다.");
                    }
                }

            } else if (choice == 2) {
                // 영업 시작
                System.out.println();
                System.out.println("========================================");
                System.out.println("           [ 영업 시작 ]");
                System.out.println("========================================");
                // TODO: 영업 구현

                day++;

            } else if (choice == 3) {
                // 재고 확인
                System.out.println();
                System.out.println("========================================");
                System.out.println("           [ 현재 재고 ]");
                System.out.println("========================================");
                // TODO: 재고 확인 구현

            } else if (choice == 0) {
                playing = false;

            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }

        System.out.println("게임을 종료합니다.");
        scanner.close();
    }
}
