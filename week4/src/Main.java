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
                    System.out.printf("현재 자본: %,d원%n", money);
                    System.out.printf("매대: %d / %d칸%n", usedSlot, MAX_SLOT);
                    System.out.println();

                    // 상품 목록 출력
                    System.out.println("--- 음료 ---");
                    System.out.printf("1. %s | 매입 %,d원 | 판매 %,d원%n", cola.name, cola.buyPrice, cola.sellPrice);
                    System.out.printf("2. %s | 매입 %,d원 | 판매 %,d원%n", cider.name, cider.buyPrice, cider.sellPrice);
                    System.out.printf("3. %s | 매입 %,d원 | 판매 %,d원%n", water.name, water.buyPrice, water.sellPrice);
                    System.out.printf("4. %s | 매입 %,d원 | 판매 %,d원%n", pocari.name, pocari.buyPrice, pocari.sellPrice);
                    System.out.printf("5. %s | 매입 %,d원 | 판매 %,d원%n", ipro.name, ipro.buyPrice, ipro.sellPrice);
                    System.out.println();

                    System.out.println("--- 맥주 ---");
                    System.out.printf("6. %s | 매입 %,d원 | 판매 %,d원%n", cass.name, cass.buyPrice, cass.sellPrice);
                    System.out.printf("7. %s | 매입 %,d원 | 판매 %,d원%n", terra.name, terra.buyPrice, terra.sellPrice);
                    System.out.printf("8. %s | 매입 %,d원 | 판매 %,d원%n", hite.name, hite.buyPrice, hite.sellPrice);
                    System.out.println();

                    System.out.println("--- 소주 ---");
                    System.out.printf("9. %s | 매입 %,d원 | 판매 %,d원%n", chamisul.name, chamisul.buyPrice, chamisul.sellPrice);
                    System.out.printf("10. %s | 매입 %,d원 | 판매 %,d원%n", cheumcherum.name, cheumcherum.buyPrice, cheumcherum.sellPrice);
                    System.out.printf("11. %s | 매입 %,d원 | 판매 %,d원%n", jinro.name, jinro.buyPrice, jinro.sellPrice);
                    System.out.println();

                    System.out.println("--- 간식/안주 ---");
                    System.out.printf("12. %s | 매입 %,d원 | 판매 %,d원%n", driedSquid.name, driedSquid.buyPrice, driedSquid.sellPrice);
                    System.out.printf("13. %s | 매입 %,d원 | 판매 %,d원%n", peanut.name, peanut.buyPrice, peanut.sellPrice);
                    System.out.printf("14. %s | 매입 %,d원 | 판매 %,d원%n", chip.name, chip.buyPrice, chip.sellPrice);
                    System.out.println();

                    System.out.println("--- 고기 ---");
                    System.out.printf("15. %s | 매입 %,d원 | 판매 %,d원%n", samgyupsal.name, samgyupsal.buyPrice, samgyupsal.sellPrice);
                    System.out.printf("16. %s | 매입 %,d원 | 판매 %,d원%n", moksal.name, moksal.buyPrice, moksal.sellPrice);
                    System.out.printf("17. %s | 매입 %,d원 | 판매 %,d원%n", sausage.name, sausage.buyPrice, sausage.sellPrice);
                    System.out.println();

                    System.out.println("--- 해수욕 용품 ---");
                    System.out.printf("18. %s | 매입 %,d원 | 판매 %,d원%n", tube.name, tube.buyPrice, tube.sellPrice);
                    System.out.printf("19. %s | 매입 %,d원 | 판매 %,d원%n", sunscreen.name, sunscreen.buyPrice, sunscreen.sellPrice);
                    System.out.printf("20. %s | 매입 %,d원 | 판매 %,d원%n", beachBall.name, beachBall.buyPrice, beachBall.sellPrice);
                    System.out.println();

                    System.out.println("--- 식재료 ---");
                    System.out.printf("21. %s | 매입 %,d원 | 판매 %,d원%n", ssamjang.name, ssamjang.buyPrice, ssamjang.sellPrice);
                    System.out.printf("22. %s | 매입 %,d원 | 판매 %,d원%n", lettuce.name, lettuce.buyPrice, lettuce.sellPrice);
                    System.out.printf("23. %s | 매입 %,d원 | 판매 %,d원%n", kimchi.name, kimchi.buyPrice, kimchi.sellPrice);
                    System.out.println();

                    System.out.println("--- 라면 ---");
                    System.out.printf("24. %s | 매입 %,d원 | 판매 %,d원%n", shinRamen.name, shinRamen.buyPrice, shinRamen.sellPrice);
                    System.out.printf("25. %s | 매입 %,d원 | 판매 %,d원%n", jinRamen.name, jinRamen.buyPrice, jinRamen.sellPrice);
                    System.out.printf("26. %s | 매입 %,d원 | 판매 %,d원%n", neoguri.name, neoguri.buyPrice, neoguri.sellPrice);
                    System.out.println();

                    System.out.println("--- 아이스크림 ---");
                    System.out.printf("27. %s | 매입 %,d원 | 판매 %,d원%n", melona.name, melona.buyPrice, melona.sellPrice);
                    System.out.printf("28. %s | 매입 %,d원 | 판매 %,d원%n", screwBar.name, screwBar.buyPrice, screwBar.sellPrice);
                    System.out.printf("29. %s | 매입 %,d원 | 판매 %,d원%n", fishBread.name, fishBread.buyPrice, fishBread.sellPrice);
                    System.out.println();

                    System.out.println("--- 기타 ---");
                    System.out.printf("30. %s | 매입 %,d원 | 판매 %,d원%n", firework.name, firework.buyPrice, firework.sellPrice);
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
