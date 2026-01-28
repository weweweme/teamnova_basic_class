import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean isLate = false;
        boolean isMorningWorkFailed = false;
        String workStatus = "0"; // 1:외주, 2:사이드, 3:학습
        String mentalStatus;
        boolean isEmergency = false;
        boolean isOutActivityDone = false;
        boolean isAloneStatus = false;

        System.out.println("오늘은 무슨 요일인가요?");
        System.out.println("1.월 2.화 3.수 4.목 5.금 6.토 7.일");
        System.out.print("선택 (1~7) : ");

        String input = scanner.nextLine();
        int dayNum = 0;

        // 1. 정상 입력 체크 (1~7 사이의 숫자 한 글자)
        if (input.matches("[1-7]")) {
            dayNum = input.charAt(0) - '0';
        } else {
            System.out.println("\n---> [Action] 정신이 혼미해 숫자를 잘못 입력했습니다.");
            System.out.println("---> 뇌가 판단을 거부하여 무작위 요일로 하루를 시작합니다.");

            Random random = new Random();
            dayNum = random.nextInt(7) + 1;

            String[] dayNames = {"", "월", "화", "수", "목", "금", "토", "일"};
            System.out.println("---> [Result] 눈을 떠보니... " + dayNames[dayNum] + "요일인 것 같습니다.");
        }

        if (dayNum >= 6) {
            System.out.println("\n>> 주말입니다! 눈을 떴을 때 당신의 선택은?");
            System.out.println("1. 바로 일어나서 작업(Unity/개발)하러 가기");
            System.out.println("2. 8~9시까지 침대에서 스마트폰 보기");
            System.out.println("3. 점심때까지 다시 잠들기");
            System.out.print("선택 : ");

            String weekendChoice = scanner.nextLine();
            if (weekendChoice.equals("1")) {
                System.out.println("\n---> [Success] 생산적인 주말을 위해 즉시 맥북을 켭니다.");
            } else if (weekendChoice.equals("2")) {
                System.out.println("\n---> [Action] 충분히 휴식하며 컨디션을 회복합니다.");
            } else {
                System.out.println("\n---> [Failure] 너무 피곤했나 봅니다. 일어나니 오후 1시입니다.");
                isLate = true;
                isMorningWorkFailed = true;
            }

        } else {
            System.out.println("\n>> 평일 아침 7시 알람이 울립니다!");
            System.out.println("1. 벌떡 일어난다 (7시 정각 기상)");
            System.out.println("2. 5분만 더... (8시에 깸, 지각 위기)");
            System.out.println("3. 알람을 끄고 다시 잔다 (기상 실패)");
            System.out.print("선택 : ");

            String wakeUpChoice = scanner.nextLine();

            if (wakeUpChoice.equals("1")) {
                System.out.println("\n---> [Success] 상쾌하게 일어났습니다. 하지만 아직 침대 안입니다.");
                System.out.println("1. 바로 화장실로 직행한다");
                System.out.println("2. 앉아서 폰을 켠다 (유튜브/커뮤니티)");
                System.out.print("상세 선택 : ");

                String earlyAction = scanner.nextLine();
                if (earlyAction.equals("1")) {
                    System.out.println("---> [Success] 시간을 벌었습니다! 아침 식사 시간이 넉넉해집니다.");
                } else {
                    System.out.println("\n[!] 알고리즘의 늪에 빠졌습니다. 무엇을 먼저 확인하시겠습니까?");
                    System.out.println("1. 유튜브 쇼츠/릴스 (무한 스크롤의 시작)");
                    System.out.println("2. IT 커뮤니티 및 기술 뉴스 (업계 동향 체크)");
                    System.out.println("3. 어제 작업한 내용에 달린 피드백/메시지 확인");
                    System.out.print("스마트폰 활동 선택 : ");

                    String phoneActivity = scanner.nextLine();
                    if (phoneActivity.equals("1")) {
                        System.out.println("\n---> [Action] 짧고 자극적인 영상들을 정신없이 넘겨봅니다.");
                        System.out.println("---> [Failure] 5분만 본다는 게 30분이 훌쩍 지났습니다. 머리가 멍해집니다.");
                        System.out.println("1. 지금이라도 던져두고 씻으러 뛰어간다.");
                        System.out.println("2. 이미 늦은 거, 딱 한 시리즈만 더 보고 일어난다.");
                        System.out.print("대응 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            isLate = true;
                            System.out.println("---> [Result] 아침 식사를 포기하고 초인적인 속도로 외출 준비를 시작합니다.");
                        } else {
                            isLate = true;
                            System.out.println("---> [Result] 이제는 지각을 넘어 오전 일정 자체가 위태로워집니다.");
                        }
                    } else if (phoneActivity.equals("2")) {
                        System.out.println("\n---> [Action] 새로 나온 자바 프레임워크 소식과 유니티 업데이트 로그를 읽습니다.");
                        System.out.println("---> [Failure] 정보 과잉: 흥미로운 기술 논쟁에 몰입하다 보니 화장실 가는 것도 잊었습니다.");
                        System.out.println("1. 읽던 내용을 스크랩하고 즉시 일어난다.");
                        System.out.println("2. 연관된 다른 기술 문서까지 파고든다.");
                        System.out.print("대응 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            isLate = true;
                            System.out.println("---> [Result] 지식은 얻었지만 몸은 아직 침대입니다. 서둘러 움직입니다.");
                        } else {
                            isLate = true;
                            System.out.println("---> [Result] 아침부터 뇌 에너지를 너무 많이 썼습니다. 지친 상태로 하루를 시작합니다.");
                        }
                    } else {
                        System.out.println("\n---> [Action] 메신저와 알림을 확인하며 어제 작업 결과에 대한 반응을 살핍니다.");
                        System.out.println("---> [Failure] 감정 소모: 해결하기 까다로운 질문이나 이슈 리포트를 보고 정신이 번쩍 듭니다.");
                        System.out.println("1. 누운 채로 해결 방안을 폰으로 검색해본다.");
                        System.out.println("2. 일단 씻으면서 머릿속으로 로직을 정리한다.");
                        System.out.print("대응 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            isLate = true;
                            System.out.println("---> [Result] 침대 위에서 디버깅을 시작하는 최악의 시간 분배를 범했습니다.");
                        } else {
                            isLate = true;
                            System.out.println("---> [Result] 샤워하면서 코드를 구상합니다. 시간은 늦었지만 두뇌는 이미 가동 중입니다.");
                        }
                    }
                }

            } else if (wakeUpChoice.equals("2")) {
                System.out.println("\n---> [Action] 평소보다 늦은 기상입니다. 외출 준비 속도를 결정해야 합니다.");
                System.out.println("1. 초고속 준비 (세수만 하고 즉시 환복)");
                System.out.println("2. 평소 속도 유지 (찝찝함 없이 정돈하고 준비)");
                System.out.print("상세 선택 : ");

                String preparationChoice = scanner.nextLine();
                if (preparationChoice.equals("1")) {
                    System.out.println("\n---> [Success] 불필요한 동선을 줄여 시간을 확보했습니다.");
                    System.out.println("---> [Result] 시간에 여유가 생겼습니다.");
                } else if (preparationChoice.equals("2")) {
                    System.out.println("\n---> [Action] 평소 루틴대로 꼼꼼히 챙겨서 집을 나옵니다.");
                    System.out.println("---> [Result] 몸은 상쾌하지만, 업무 시작 시각이 다소 촉박해졌습니다.");
                } else {
                    System.out.println("\n---> [Failure] 준비 도중 딴짓을 하느라 예상보다 훨씬 늦게 집에서 나옵니다.");
                    isLate = true;
                }

            } else {
                isLate = true;
                System.out.println("\n---> [Failure] 눈을 뜨니 8시 40분! 9시 업무 시작이 코앞입니다.");
                System.out.println("1. 세수 생략, 전력 질주로 정각 도착 노리기");
                System.out.println("2. 늦은 건 인정, 세수만 하고 걸어서 느긋하게 도착하기");
                System.out.print("상세 선택 : ");

                String disasterAction = scanner.nextLine();
                if (disasterAction.equals("1")) {
                    System.out.println("\n---> [Action] 모자만 쓰고 달려나갑니다.");
                } else if (disasterAction.equals("2")) {
                    System.out.println("\n---> [Action] 최소한의 세수는 하고 집에서 나옵니다.");
                } else {
                    System.out.println("---> [Failure] 멘탈이 나가서 오전 루틴을 완전히 포기했습니다.");
                }
            }
        }

        // 2. 아침 식사 분기 (지각이 아닐 때만 식사 고민 가능)
        if (!isLate) {
            System.out.println("\n------------------------------------------------");
            System.out.println("아침 시간을 어떻게 사용할까?");
            System.out.println("1. 직접 요리 (가장 든든함, 20분 소요)");
            System.out.println("2. 오매김밥 픽업 (효율적, 10분 소요)");
            System.out.println("3. 식사 생략 후 즉시 작업실 이동");
            System.out.print("선택 : ");
            String foodChoice = scanner.nextLine();

            if (foodChoice.equals("1")) {
                System.out.println("\n---> [Action] 냉장고 문 앞에 섰습니다. 어느 쪽 칸을 먼저 확인해볼까요?");
                System.out.println("1. 위쪽 (냉장실 메인 칸)");
                System.out.println("2. 아래쪽 (신선 보관실)");
                System.out.print("선택 : ");
                String shelfChoice = scanner.nextLine();

                if (shelfChoice.equals("1")) {
                    System.out.println("---> [Success] 구석에 숨겨져 있던 계란 두 알과 유통기한이 아슬아슬한 식빵을 찾아냈습니다.");
                    System.out.println("[?] 발견한 재료로 어떤 아침을 만드시겠습니까?");
                    System.out.println("1. 정석적인 프렌치 토스트 (우유와 설탕까지 곁들인 고퀄리티)");
                    System.out.println("2. 속전속결 계란 프라이와 구운 식빵 (빠른 조리와 최소한의 설거지)");
                    System.out.println("3. 에그 샌드위치 (작업실로 걸어가면서 먹기 좋게 포장)");
                    System.out.print("조리 방식 선택 : ");

                    String cookChoice = scanner.nextLine();
                    if (cookChoice.equals("1")) {
                        System.out.println("\n---> [Action] 프라이팬에 버터를 두르고 우유에 적신 빵을 정성껏 굽습니다.");
                        System.out.println("---> [Result] 호텔 조식 부럽지 않은 아침을 즐겼습니다. 다만, 설거지 거리가 늘어나 외출 준비가 촉박해집니다.");
                    } else if (cookChoice.equals("2")) {
                        System.out.println("\n---> [Action] 토스터기에 빵을 던져넣고, 그 사이 반숙 프라이를 빠르게 부쳐냅니다.");
                        System.out.println("---> [Result] 영양과 시간을 모두 챙긴 합리적인 선택이었습니다. 기분 좋게 식기를 헹구고 나섭니다.");
                    } else if (cookChoice.equals("3")) {
                        System.out.println("\n---> [Action] 계란을 빠르게 스크램블하여 식빵 사이에 끼워 넣고 랩으로 감쌉니다.");
                        System.out.println("---> [Result] 집에서는 커피만 한 잔 마시고, 샌드위치는 사무실에 가서 먹기로 합니다.");
                    } else {
                        System.out.println("\n---> [Action] 요리하기 귀찮아져서 식빵만 입에 물고 나갑니다.");
                        System.out.println("---> [Result] 계란은 내일의 나에게 양보합니다. 허기는 가시지 않았지만 시간은 충분합니다.");
                    }

                } else {
                    if (shelfChoice.equals("2")) {
                        System.out.println("---> [Failure] 텅 비어있습니다. 장을 안 봐둔 과거의 나를 원망합니다.");
                    } else {
                        System.out.println("---> [Action] 고민만 하다가 요리할 시간을 놓쳐버렸습니다.");
                    }

                    System.out.println("\n[!] 이대로 굶을 순 없습니다. 작업실 가는 길에 대안을 찾습니다.");
                    System.out.println("1. 편의점에서 삼각김밥과 셀렉스 프로핏을 산다");
                    System.out.println("2. 빵집에서 취향에 맞는 빵을 고른다");
                    System.out.println("3. 오매김밥에서 빠르게 한 줄 포장한다");
                    System.out.print("대안 선택 : ");

                    String planBChoice = scanner.nextLine();

                    if (planBChoice.equals("1")) {
                        System.out.println("\n---> [Action] 편의점에 입장하여 음료 냉장고와 신선 식품 코너를 살핍니다.");
                        System.out.println("[?] 편의점에서 무엇을 핵심적으로 챙기시겠습니까?");
                        System.out.println("1. 단백질 위주 (셀렉스/더단백 프로틴 음료 + 닭가슴살 바)");
                        System.out.println("2. 가성비 탄수화물 (2+1 행사 중인 삼각김밥 세트)");
                        System.out.println("3. 빠른 당 충전 (초코바와 에너지 드링크)");
                        System.out.print("편의점 조합 선택 : ");

                        String convenienceChoice = scanner.nextLine();
                        if (convenienceChoice.equals("1")) {
                            System.out.println("\n---> [Action] 설탕이 적고 단백질 함량이 높은 제품들로 골라 담습니다.");
                            System.out.println("---> [Result] 테니스와 업무를 위한 근육 성장에 도움을 주는 최적의 영양 조합을 완성했습니다.");
                        } else if (convenienceChoice.equals("2")) {
                            System.out.println("\n---> [Action] 행사 중인 삼각김밥의 성분표를 대충 훑으며 가장 든든해 보이는 것을 집습니다.");
                            System.out.println("---> [Result] 지출은 아끼고 포만감은 챙겼습니다. 남은 돈은 나중에 테니스장 대여료에 보탭니다.");
                        } else if (convenienceChoice.equals("3")) {
                            System.out.println("\n---> [Action] 뇌를 빠르게 가동하기 위한 고당도 간식과 고카페인 음료를 선택합니다.");
                            System.out.println("---> [Result] 일시적인 각성 효과로 오전 업무의 폭발적인 속도를 예약했습니다.");
                        } else {
                            System.out.println("\n---> [Action] 물 한 병만 사고 빠르게 편의점을 빠져나옵니다.");
                            System.out.println("---> [Result] 공복의 위기감은 여전하지만 지각 시간은 1분 줄였습니다.");
                        }

                    } else if (planBChoice.equals("2")) {
                        System.out.println("\n---> [Action] 진열된 빵들을 보며 행복한 고민에 빠집니다.");
                        System.out.println("1. 소보로빵 (클래식함)");
                        System.out.println("2. 샌드위치 (든든한 한 끼)");
                        System.out.println("3. 메론빵 (달달한 당 충전)");
                        System.out.println("4. 모닝빵 (가볍게 시작)");
                        System.out.println("5. 에그타르트 (플렉스)");
                        System.out.print("빵 선택 : ");

                        String breadType = scanner.nextLine();
                        if (breadType.equals("1")) {
                            System.out.println("---> [Result] 고소한 소보로빵으로 아침을 해결합니다.");
                        } else if (breadType.equals("2")) {
                            System.out.println("---> [Result] 신선한 채소가 든 샌드위치로 건강을 챙깁니다.");
                        } else if (breadType.equals("3")) {
                            System.out.println("---> [Result] 달콤한 메론빵 덕분에 기분이 좋아집니다.");
                        } else if (breadType.equals("4")) {
                            System.out.println("---> [Result] 부담 없는 모닝빵으로 속을 달랩니다.");
                        } else if (breadType.equals("5")) {
                            System.out.println("---> [Result] 작은 사치인 에그타르트로 당을 보충합니다.");
                        } else {
                            System.out.println("---> [Failure] 빵을 고르지 못하고 구경만 하다가 나옵니다.");
                        }

                    } else if (planBChoice.equals("3")) {
                        System.out.println("---> [Action] 오매김밥 이모님께 빠르게 포장을 요청합니다.");
                        System.out.println("---> [Result] 베테랑 이모님의 화려한 손기술로 1분 만에 김밥 픽업 완료!");
                    } else {
                        System.out.println("---> [Failure] 결정을 못 내리고 걷다 보니 이미 작업실 건물 앞입니다. 결국 굶습니다.");
                    }
                }

            } else if (foodChoice.equals("2")) {
                System.out.println("\n---> [Action] 작업실 가는 길에 오매김밥에 들러 키오스크 앞에 섭니다.");
                System.out.println("1. 최애 메뉴 '땡초김밥' (매우 매움)");
                System.out.println("2. 고소한 '참치김밥' (든든함)");
                System.out.println("3. 달달한 '제육김밥' (자극적)");
                System.out.println("4. 담백한 '치즈김밥' (부드러움)");
                System.out.print("선택 : ");

                String gimbapChoice = scanner.nextLine();

                if (gimbapChoice.equals("1")) {
                    System.out.println("\n---> [Success] 입안이 얼얼할 정도로 매운 땡초김밥을 먹습니다.");

                    System.out.println("\n[!] 아뿔싸, 오늘따라 땡초가 유독 맵습니다. 속이 점점 타들어 가는데...?");
                    System.out.println("1. 프리랜서의 정신력으로 참고 작업실로 직행한다");
                    System.out.println("2. 약국에 들러 강력한 소화제(제산제)를 산다");
                    System.out.println("3. 편의점에 들러 시원한 우유를 산다");
                    System.out.print("위기 선택 : ");

                    String stomachAction = scanner.nextLine();

                    if (stomachAction.equals("1")) {
                        System.out.println("---> [Failure] 작업실에 도착했지만 배가 계속 아픕니다. 집중력이 수직 하락합니다.");
                    } else if (stomachAction.equals("2")) {
                        System.out.println("---> [Action] 근처 약국으로 달려가 액상 제산제를 들이킵니다.");
                        System.out.println("---> [Result] 약 기운 덕분에 속이 빠르게 진정됩니다. 완벽한 컨디션으로 복귀합니다.");
                    } else if (stomachAction.equals("3")) {
                        System.out.println("---> [Action] 편의점에서 흰 우유를 사서 급하게 마십니다.");
                        System.out.println("1. 원샷한다");
                        System.out.println("2. 천천히 마신다");
                        System.out.print("우유 마시는 법 : ");

                        String milkMethod = scanner.nextLine();
                        if (milkMethod.equals("1")) {
                            System.out.println("---> [Failure] 너무 차가운 우유를 급하게 마셨더니 이번엔 배탈이 나려고 합니다.");
                            System.out.println("1. 화장실이 있는 작업실까지 전력 질주한다");
                            System.out.println("2. 길가에 보이는 공용 화장실로 일단 들어간다");
                            System.out.print("긴급 선택 : ");

                            String toiletRun = scanner.nextLine();
                            isEmergency = true;
                            if (toiletRun.equals("1")) {
                                System.out.println("---> [Action] 작업실로 달립니다!");
                                System.out.println("---> [Result] 간신히 세이프! 화장실로 직행합니다.");
                            } else {
                                System.out.println("---> [Action] 공용 화장실의 위생 상태가 걱정되지만 선택의 여지가 없습니다.");
                                System.out.println("---> [Result] 위기를 넘겼지만 시간이 훌쩍 지나 9시가 넘었습니다.");
                            }
                        } else {
                            System.out.println("---> [Success] 우유가 매운맛을 중화시켜 줍니다. 속이 한결 편해졌습니다.");
                        }
                    } else {
                        System.out.println("---> [Failure] 멍하니 배를 부여잡고 길 한복판에서 시간을 허비했습니다.");
                    }

                } else if (gimbapChoice.equals("2")) {
                    System.out.println("---> [Result] 마요네즈가 듬뿍 들어간 참치김밥으로 든든하게 배를 채웠습니다.");
                    System.out.println("1. 단백질 보충을 위해 고기를 추가한다");
                    System.out.println("2. 목이 막히니 바로 커피숍으로 향한다");
                    System.out.print("추가 선택 : ");

                    String tunaAdd = scanner.nextLine();
                    if (tunaAdd.equals("1")) {
                        System.out.println("---> [Success] 영양 밸런스 완벽! 오전 내내 배고플 일은 없겠네요.");
                    } else {
                        System.out.println("---> [Action] 목이 퍽퍽하지만 참으며 커피를 향해 전진합니다.");
                    }

                } else if (gimbapChoice.equals("3")) {
                    System.out.println("---> [Result] 매콤달콤한 제육김밥 덕분에 에너지가 충전됩니다.");
                    System.out.println("1. 양념이 묻었을지 모르니 지금 바로 정돈한다");
                    System.out.println("2. 상관없다, 시간 아까우니 바로 걷는다");
                    System.out.print("위생 선택 : ");

                    String spicyAdd = scanner.nextLine();
                    if (spicyAdd.equals("1")) {
                        System.out.println("---> [Success] 입가에 묻은 양념을 닦아냈습니다. 깔끔한 상태로 이동합니다.");
                    } else {
                        System.out.println("\n---> [Action] 작업실을 향해 빠르게 걷는 중입니다.");
                        System.out.println("---> [Failure] 우연히 길가 쇼윈도 유리에 비친 내 얼굴을 보니 입가에 양념이 묻어있습니다.");

                        System.out.println("\n[!] '아... 이러고 김밥집부터 여기까지 걸어온 건가?' 주위 시선이 신경 쓰입니다.");
                        System.out.println("1. 즉시 멈춰서 물티슈로 박박 닦는다");
                        System.out.println("2. 어차피 작업실 다 왔다. 들어가서 닦자며 외면하고 걷는다");
                        System.out.println("3. 치열하게 사는 개발자의 훈장이라 생각하며 당당하게 걷는다");
                        System.out.print("수습 선택 : ");

                        String walkEmbarrassment = scanner.nextLine();
                        if (walkEmbarrassment.equals("1")) {
                            System.out.println("---> [Action] 길가에서 급히 얼굴을 정돈합니다.");
                            System.out.println("---> [Result] 찝찝함은 사라졌지만 이동 템포가 잠시 끊겼습니다.");
                        } else if (walkEmbarrassment.equals("2")) {
                            System.out.println("---> [Action] 고개를 숙이고 작업실 건물을 향해 전속력으로 걷습니다.");
                            System.out.println("---> [Result] 지각 위기는 면하겠지만, 마주치는 사람마다 얼굴을 피하게 됩니다.");
                        } else {
                            System.out.println("---> [Action] 핸드폰 셀프 카메라 속의 자신과 눈을 맞추며 '오늘 좀 치열해 보이는데?'라고 생각합니다.");
                            System.out.println("---> [Result] 근거 없는 자신감이 생깁니다. 오늘 작업 효율이 좋을 것 같은 예감이 듭니다.");
                        }
                    }

                } else if (gimbapChoice.equals("4")) {
                    System.out.println("---> [Result] 부드러운 치즈김밥으로 속 편안하게 아침을 시작합니다.");
                    System.out.println("1. 느끼함을 잡기 위해 탄산수를 추가로 산다");
                    System.out.println("2. 이 평온함을 유지하며 여유롭게 작업실로 향한다");
                    System.out.print("음료 선택 : ");

                    String cheeseAdd = scanner.nextLine();
                    if (cheeseAdd.equals("1")) {
                        System.out.println("---> [Success] 탄산수의 청량함이 입안을 깔끔하게 정리해 줍니다.");
                    } else {
                        System.out.println("---> [Result] 속이 아주 편안합니다. 오늘 개발 구현이 잘 풀릴 것 같습니다.");
                    }
                }

            } else {
                System.out.println("\n---> [Action] 배고픔을 참고 즉시 작업실로 향합니다.");
            }

            if (!isEmergency) {
                System.out.println("\n---> [Action] 작업실 입구 근처, 매머드 커피 앞에 도착했습니다.");
                System.out.println("1. 늘 먹던 디카페인 아메리카노");
                System.out.println("2. 카페인 든 아메리카노 (잠 깨는 용도)");
                System.out.print("선택 : ");

                String coffeeChoice = scanner.nextLine();
                if (coffeeChoice.equals("2")) {
                    System.out.println("---> [Result] 카페인이 혈관을 타고 흐릅니다. 잠이 확 깹니다.");
                } else {
                    System.out.println("---> [Result] 심장 떨림 없이 평온하게 작업을 시작할 준비를 마쳤습니다.");
                }

            } else {
                System.out.println("---> [Result] 심장 떨림 없이 평온하게 작업을 시작할 준비를 마쳤습니다.");
            }

        } else {
            System.out.println("\n[!] 작업실까지 도보로 이동 중입니다.");
            System.out.println("1. 최단 거리 골목길 (빠르지만 노면이 거칠고 복잡함)");
            System.out.println("2. 큰길 코스 (조금 돌아가지만 걷기 편하고 쾌적함)");
            System.out.print("이동 경로 선택 : ");

            String walkPath = scanner.nextLine();

            if (walkPath.equals("1")) {
                System.out.println("\n---> [Action] 좁은 골목길을 가로질러 전속력으로 걷습니다.");
                System.out.println("---> [Failure] 변수: 앞서가는 사람이 내뿜는 담배 연기가 직격으로 날아와 기분이 안좋아집니다.");
                System.out.println("1. 숨을 참고 초스피드로 추월하여 멀어진다.");
                System.out.println("2. 거리를 두기 위해 아예 느릿하게 걷거나 잠시 멈춘다.");
                System.out.print("대응 선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("\n---> [Action] 폐활량을 총동원해 숨을 참고 전력 질주로 앞질러 갑니다.");
                    System.out.println("---> [Result] 비흡연자의 고통을 느끼며 예정보다 빠르게 작업실에 입성했습니다.");
                } else {
                    System.out.println("\n---> [Action] 연기를 피하기 위해 발걸음을 늦추고 심호흡을 합니다.");
                    System.out.println("---> [Result] 도착은 조금 늦어졌으나 불필요한 감정 소모를 최소화했습니다.");
                }
            } else {
                System.out.println("\n---> [Action] 넓은 인도를 따라 일정한 보폭으로 걷습니다.");
                System.out.println("---> [Failure] 변수: 보도블록 교체 공사로 길이 막혀 있고, 건너편 신호등은 막 빨간불로 바뀝니다.");
                System.out.println("1. 무단횡단의 유혹을 뿌리치고 멀리 돌아가는 육교를 이용한다.");
                System.out.println("2. 신호를 기다리는 동안 스마트폰으로 오늘 작업 문서를 리마인드한다.");
                System.out.println("3. 공사 현장을 피해 차도 옆 좁은 길로 아슬아슬하게 통과한다.");
                System.out.print("대응 선택 : ");

                String mainRoadChoice = scanner.nextLine();
                if (mainRoadChoice.equals("1")) {
                    System.out.println("\n---> [Action] 계단을 오르내리며 육교를 건너 크게 우회합니다.");
                    System.out.println("---> [Result] 강제 유산소 운동이 되었지만, 안전하고 확실하게 공사 구간을 통과했습니다.");
                } else if (mainRoadChoice.equals("2")) {
                    System.out.println("\n---> [Action] 신호가 바뀌길 기다리며 마일스톤을 체크합니다.");
                    System.out.println("---> [Result] 시간은 지체됐지만, 작업실 도착 후 바로 시작할 수 있게 머릿속 정리가 끝났습니다.");
                } else {
                    System.out.println("\n---> [Action] 공사 가벽 사이 좁은 틈새로 빠르게 빠져나갑니다.");
                    System.out.println("---> [Result] 먼지를 조금 마셨지만, 지연 없이 최단 거리로 도착했습니다.");
                }
            }

            System.out.println("\n[!] 드디어 작업실 건물이 시야에 들어옵니다.");
            System.out.println("아 그런데 뭔가 아쉬운데,, 그래도 커피랑 먹을 거 사갈까?");
            System.out.println("1. 모바일 오더로 빛의 속도로 픽업한다.");
            System.out.println("2. 지금 그럴 때가 아니다. 곧장 입구로 달려간다.");
            System.out.print("최종 진입 선택 : ");

            if (scanner.nextLine().equals("1")) {
                System.out.println("\n[?] 어떤 메뉴 조합으로 주문하시겠습니까?");
                System.out.println("1. 늘 먹던 아이스 아메리카노 + 간단한 쿠키");
                System.out.println("2. 당 떨어지니까 시그니처 라떼 + 샌드위치");
                System.out.print("메뉴 선택 : ");

                String orderChoice = scanner.nextLine();
                System.out.println("\n---> [Action] 앱 결제 완료. 매장으로 뛰어 들어가 픽업대로 향합니다.");

                if (orderChoice.equals("1")) {
                    System.out.println("---> [Success] 이미 나와 있는 아메리카노를 낚아채듯 들고 나옵니다.");
                    System.out.println("---> [Result] 시간 손실 제로. 카페인 수혈 준비 완료.");
                } else {
                    System.out.println("---> [Failure] 변수: 샌드위치 데우는 시간 때문에 예상보다 2분 더 대기합니다.");
                    System.out.println("1. 기다린 김에 여유롭게 받아서 나간다.");
                    System.out.println("2. 그냥 환불하고 커피만 들고 간다.");
                    System.out.print("대기 상황 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Result] 시간은 좀 더 썼지만, 든든한 상태가 되었습니다.");
                    } else {
                        System.out.println("---> [Result] 돈은 아깝지만 시간을 택했습니다. 커피들고 갑니다.");
                    }
                }
            } else {
                System.out.println("\n---> [Action] 유혹을 뿌리치고 건물 로비로 진입합니다.");
                System.out.println("---> [Result] 가장 빠른 시간 내에 책상 앞에 앉는 것에 집중합니다.");
            }

            System.out.println("\n[!] 드디어 작업실 입구에 도착했습니다.");
            System.out.println("\n[!] 작업실 엘리베이터 앞에 도착했습니다. 이제 업무 공간으로 입장합니다.");
        }


        // 3. 오전 업무 상황 분기
        if (!isMorningWorkFailed) {
            System.out.println("\n================================================");
            System.out.println("작업실에 앉아 맥북을 열었습니다. 오늘 오전의 우선순위는?");
            System.out.println("1. 프리랜서 외주 프로젝트 (마감 임박)");
            System.out.println("2. 개인 사이드 프로젝트 (Unity 6 / Steam)");
            System.out.println("3. 팀노바 학습 및 기술 검증");
            System.out.print("선택 : ");
            workStatus = scanner.nextLine();

            if (workStatus.equals("1")) {
                System.out.println("\n[!] 프리랜서 업무: 현재 프로젝트의 진행 단계는?");
                System.out.println("1. 신규 프로젝트 분석 및 설계 (여유로움)");
                System.out.println("2. 핵심 기능 구현 및 개발 (집중 필요)");
                System.out.println("3. 최종 마감 및 버그 수정 (긴박함)");
                System.out.print("상세 선택 : ");
                String freelanceStatus = scanner.nextLine();

                if (freelanceStatus.equals("1")) {
                    System.out.println("\n---> [Action] 클라이언트가 보낸 기획서를 검토하며 요구사항을 분석합니다.");
                    System.out.println("1. 기술적 실현 가능성 검토 (PoC)");
                    System.out.println("2. 시스템 구조 설계 (Architecture)");
                    System.out.print("상세 작업 선택 : ");
                    String initialWork = scanner.nextLine();

                    if (initialWork.equals("1")) {
                        System.out.println("\n---> [Action] 핵심 라이브러리가 현재 환경에서 충돌 없이 돌아가는지 테스트합니다.");
                        System.out.println("1. 공식 샘플 코드 실행 및 의존성 분석");
                        System.out.println("2. 샌드박스 구축을 통한 부하 및 호환성 테스트");
                        System.out.print("검증 방식 선택 : ");

                        String pocMethod = scanner.nextLine();
                        if (pocMethod.equals("1")) {
                            System.out.println("\n---> [Action] 예제 코드를 컴파일하며 외부 라이브러리 의존 관계를 살핍니다.");
                            System.out.println("1. 내부 소스를 분석하여 커스텀 가능 범위 확인");
                            System.out.println("2. 블랙박스 상태로 라이브러리 인터페이스만 활용");
                            System.out.print("분석 수준 선택 : ");

                            String analyzeDepth = scanner.nextLine();
                            if (analyzeDepth.equals("1")) {
                                System.out.println("---> [Action] 라이브러리 내부 결합도를 체크하며 확장 지점을 찾습니다.");
                                System.out.println("---> [Result] 내부 로직이 너무 견고하게 닫혀있어 커스텀 시 오버헤드가 큼을 확인했습니다.");
                            } else {
                                System.out.println("---> [Action] 제공된 API 위주로 간단한 연동 테스트만 진행합니다.");
                                System.out.println("---> [Result] 당장은 작동하지만, 나중에 엣지 케이스에서 문제가 될 리스크를 안게 되었습니다.");
                            }
                        } else {
                            System.out.println("\n---> [Action] 실제 서비스와 유사한 데이터 셋을 넣어 부하 테스트를 진행합니다.");
                            System.out.println("1. 동시 접속 및 대량 데이터 처리량(Throughput) 측정");
                            System.out.println("2. 장시간 구동 시 메모리 누수(Memory Leak) 여부 모니터링");
                            System.out.print("측정 지표 선택 : ");

                            String metricChoice = scanner.nextLine();
                            if (metricChoice.equals("1")) {
                                System.out.println("---> [Action] 가상 유저를 생성하여 API 응답 시간을 기록합니다.");
                                System.out.println("---> [Result] 특정 구간에서 병목 현상이 발생하지만, 최적화로 해결 가능한 수준임을 파악했습니다.");
                            } else {
                                System.out.println("---> [Action] 프로파일러를 연결하고 힙 메모리의 변화 그래프를 관찰합니다.");
                                System.out.println("---> [Result] 참조 해제가 안 되는 객체를 발견하여 도입 전 수정 포인트를 확보했습니다.");
                            }
                        }
                    }

                } else if (freelanceStatus.equals("2")) {
                    System.out.println("\n---> [Action] 결제 모듈 및 사용자 API 연동 작업을 시작합니다.");
                    System.out.println("1. 테스트 주도 개발(TDD) 기반의 꼼꼼한 구현");
                    System.out.println("2. 기능 동작 중심의 빠른 프로토타이핑");
                    System.out.print("개발 전략 선택 : ");

                    String devStyle = scanner.nextLine();
                    if (devStyle.equals("1")) {
                        System.out.println("\n---> [Action] 실패하는 테스트 케이스를 작성하고 이를 통과하는 코드를 짭니다.");
                        System.out.println("1. 모든 예외 상황(Exception)에 대한 핸들링 로직 추가");
                        System.out.println("2. 핵심 비즈니스 로직의 단위 테스트 완성도에 집중");
                        System.out.print("TDD 우선순위 : ");

                        String tddFocus = scanner.nextLine();
                        if (tddFocus.equals("1")) {
                            System.out.println("---> [Action] 네트워크 오류, 데이터 누락 등 발생 가능한 변수를 코드로 방어합니다.");
                            System.out.println("---> [Result] 속도는 느리지만 어떤 상황에서도 죽지 않는 견고한 모듈이 완성되었습니다.");
                        } else {
                            System.out.println("---> [Action] 주요 함수들이 기획대로 값을 반환하는지 반복 확인합니다.");
                            System.out.println("---> [Result] 코드의 신뢰성이 확보되어 이후 리팩토링 시 안전 장치를 마련했습니다.");
                        }
                    } else {
                        System.out.println("\n---> [Action] 일단 화면에 결과가 나오는 것을 목표로 빠르게 코드를 작성합니다.");
                        System.out.println("1. 하드코딩된 값들을 변수화하고 최소한의 공통화 진행");
                        System.out.println("2. 리팩토링은 나중에, 오직 전체 프로세스 연결에 집중");
                        System.out.print("속도전 우선순위 : ");

                        String speedFocus = scanner.nextLine();
                        if (speedFocus.equals("1")) {
                            System.out.println("---> [Action] 중복되는 코드를 찾아내어 유틸리티 클래스로 분리합니다.");
                            System.out.println("---> [Result] 마감 시각을 지키면서도 최소한의 가독성은 챙긴 코드가 나왔습니다.");
                        } else {
                            System.out.println("---> [Action] 의식의 흐름대로 로직을 배치하며 기능을 완성합니다.");
                            System.out.println("---> [Result] 연결은 성공했지만, 나중에 본인이 봐도 이해하기 힘든 스파게티가 되었습니다.");
                        }
                    }

                } else if (freelanceStatus.equals("3")) {
                    System.out.println("\n---> [Action] 납품 전 최종 빌드를 뽑아 품질 검수(QA)를 시작합니다.");
                    System.out.println("1. 통합 이슈 리스트 기반 문제 해결");
                    System.out.println("2. 직접 플레이 및 엣지 케이스 발굴");
                    System.out.print("검수 방식 선택 : ");

                    String crunchAction = scanner.nextLine();
                    if (crunchAction.equals("1")) {
                        System.out.println("\n---> [Action] 문서에 정리된 버그 리스트를 하나씩 지워나갑니다.");
                        System.out.println("1. 사소한 UI 및 텍스트 오타까지 전부 수정");
                        System.out.println("2. 핵심 기능 작동 여부만 빠르게 재검증");
                        System.out.print("완성도 선택 : ");

                        String perfectionChoice = scanner.nextLine();
                        if (perfectionChoice.equals("1")) {
                            System.out.println("---> [Action] 새벽까지 모든 티켓을 'Closed' 상태로 만듭니다.");
                            System.out.println("---> [Result] 결벽에 가까운 퀄리티를 확보했으나, 체력이 방전되었습니다.");
                        } else {
                            System.out.println("---> [Action] 중요도가 낮은 티켓은 'Backlog'로 넘기고 일과를 마칩니다.");
                            System.out.println("---> [Result] 실질적인 기능에는 문제가 없는 상태로 마감 준비를 마쳤습니다.");
                        }
                    } else {
                        System.out.println("\n---> [Action] 프로젝트 리스트 중 검수가 필요한 게임을 선택합니다.");
                        System.out.println("1. 고피쉬 (Go Fish) - UI 상호작용 검수");
                        System.out.println("2. 포츈 (Fortune) - 입력 시스템 검수");
                        System.out.println("3. 스도쿠 (Sudoku) - 컴포넌트 활성화 검수");
                        System.out.println("4. 스푼 (Spoon) - 오브젝트 생명주기 검수");
                        System.out.println("5. 서브웨이 (Subway) - 런타임 예외 검수");
                        System.out.print("게임 선택 : ");

                        String gameChoice = scanner.nextLine();

                        if (gameChoice.equals("1")) {
                            System.out.println("\n---> [Action] 고피쉬: 특정 페이즈에서 클릭 방지 로직을 테스트합니다.");
                            System.out.println("1. 연타를 통해 클릭 차단 레이어 뚫기 시도");
                            System.out.println("2. 드래그 앤 드롭 중 강제 취소로 상태 꼬기");
                            System.out.print("테스트 선택 : ");

                            String goFishTest = scanner.nextLine();
                            if (goFishTest.equals("1")) {
                                System.out.println("---> [Action] 클릭이 막혀야 하는 타이밍에 무작위 입력을 넣습니다.");
                                System.out.println("---> [Result] 레이캐스트 타겟 설정 오류로 인한 오작동을 발견하여 수정했습니다.");
                            } else {
                                System.out.println("---> [Action] 카드를 든 채로 화면 밖으로 마우스를 이탈시킵니다.");
                                System.out.println("---> [Result] 드롭 핸들러가 호출되지 않는 엣지 케이스를 방어 로직에 추가했습니다.");
                            }

                        } else if (gameChoice.equals("2")) {
                            System.out.println("\n---> [Action] 포츈: 시드(Seed) 입력 제어 로직을 검수합니다.");
                            System.out.println("1. 입력 불가 타이밍에 강제로 Focus 주기");
                            System.out.println("2. 비정상적인 문자열 및 공백 입력 테스트");
                            System.out.print("테스트 선택 : ");

                            String fortuneTest = scanner.nextLine();
                            if (fortuneTest.equals("1")) {
                                System.out.println("---> [Action] 게임 진행 중 inputField가 활성화되는지 반복 확인합니다.");
                                System.out.println("---> [Result] 코루틴 처리 지연으로 입력을 받는 찰나의 순간을 찾아내어 차단했습니다.");
                            } else {
                                System.out.println("---> [Action] 특수 문자와 오버플로우급 숫자를 입력합니다.");
                                System.out.println("---> [Result] 정규식 검증 로직을 강화하여 데이터 오염을 방지했습니다.");
                            }

                        } else if (gameChoice.equals("3")) {
                            System.out.println("\n---> [Action] 스도쿠: UI 컴포넌트 비활성화 이슈를 추적합니다.");
                            System.out.println("1. 힌트 사용 후 버튼 활성화 상태 유지 확인");
                            System.out.println("2. 정답 체크 중 UI 조작 가능 여부 테스트");
                            System.out.print("테스트 선택 : ");

                            String sudokuTest = scanner.nextLine();
                            if (sudokuTest.equals("1")) {
                                System.out.println("---> [Action] 힌트를 남발하며 UI가 먹통이 되는지 살핍니다.");
                                System.out.println("---> [Result] CanvasGroup의 Interactable 값이 비정상적으로 고정되는 버그를 해결했습니다.");
                            } else {
                                System.out.println("---> [Action] 검사 로직이 도는 동안 숫자를 바꿔봅니다.");
                                System.out.println("---> [Result] 연산 중 입력을 막는 락(Lock) 기능을 보완했습니다.");
                            }

                        } else if (gameChoice.equals("4")) {
                            System.out.println("\n---> [Action] 스푼: 음식 오브젝트의 실시간 소실 문제를 검토합니다.");
                            System.out.println("1. 서빙 직후 파괴 시점 정밀 검사");
                            System.out.println("2. 풀링 시스템 반환 로직 확인");
                            System.out.print("테스트 선택 : ");

                            String spoonTest = scanner.nextLine();
                            if (spoonTest.equals("1")) {
                                System.out.println("---> [Action] 오브젝트가 활성화된 상태에서 부모 객체를 비활성화해봅니다.");
                                System.out.println("---> [Result] OnDisable 호출 시점이 꼬여 음식이 사라지는 현상을 수정했습니다.");
                            } else {
                                System.out.println("---> [Action] 풀에 반환된 객체가 재사용될 때 초기화 상태를 봅니다.");
                                System.out.println("---> [Result] 이전 상태가 남아있는 데이터 잔상 문제를 발견하여 초기화 코드를 추가했습니다.");
                            }

                        } else if (gameChoice.equals("5")) {
                            System.out.println("\n---> [Action] 서브웨이: 런타임 중 발생하는 NullReferenceException을 추적합니다.");
                            System.out.println("1. 씬 전환 및 데이터 로드 시점의 참조 확인");
                            System.out.println("2. 이벤트 구독 해제(Unsubscribe) 누락 여부 확인");
                            System.out.print("테스트 선택 : ");

                            String subwayTest = scanner.nextLine();
                            if (subwayTest.equals("1")) {
                                System.out.println("---> [Action] 데이터가 로드되기 전 Singleton 객체에 접근을 시도합니다.");
                                System.out.println("---> [Result] 초기화 순서 문제로 인한 Null 에러를 의존성 주입 단계에서 해결했습니다.");
                            } else {
                                System.out.println("---> [Action] 오브젝트 파괴 후 이벤트가 호출되는지 살핍니다.");
                                System.out.println("---> [Result] 파괴된 객체의 액션 구독이 남은 것을 발견하여 OnDestroy 로직을 보강했습니다.");
                            }

                        } else {
                            System.out.println("---> [Failure] 테스트 대상을 정하지 못해 유니티 에디터만 만지작거리다 시간이 갔습니다.");
                        }
                    }
                }

            } else if (workStatus.equals("2")) {
                System.out.println("\n[?] 사이드 프로젝트: 오늘 집중할 개발 영역은?");
                System.out.println("1. 핵심 엔진 모듈 최적화 (UniTask/R3/asmdef)");
                System.out.println("2. 게임 플레이 시스템 구현 (레일/이동/스킬)");
                System.out.println("3. 메타 시스템 및 데이터 관리 (보물창고/장비)");
                System.out.println("4. 유저 피드백 기반 조작감 개선 (Game Feel)");
                System.out.print("영역 선택 : ");
                String sideProjectArea = scanner.nextLine();

                if (sideProjectArea.equals("1")) {
                    // 기술적 최적화 심화 로직
                    System.out.println("\n[!] 기술 모듈 상세 검토:");
                    System.out.println("1. UniTask - 비동기 퍼포먼스 및 할당 최적화");
                    System.out.println("2. R3 - 리액티브 데이터 스트림 설계");
                    System.out.println("3. asmdef - 컴파일 타임 및 의존성 관리");
                    System.out.print("상세 선택 : ");
                    String techDetail = scanner.nextLine();

                    if (techDetail.equals("1")) {
                        System.out.println("\n---> [Action] 비동기 로직의 가비지(GC) 발생 및 실행 효율을 정밀 진단합니다.");
                        System.out.println("1. 기존 IEnumerator 코루틴의 UniTask 전환 및 구조 개선");
                        System.out.println("2. PlayerLoop 제어를 통한 연산 부하 분산");
                        System.out.print("최적화 경로 선택 : ");

                        String unitaskAction = scanner.nextLine();
                        if (unitaskAction.equals("1")) {
                            System.out.println("\n---> [Action] 프로파일러를 통해 'yield return' 시 발생하는 Allocation을 확인합니다.");
                            System.out.println("1. CancellationToken을 연결하여 예외 안전성 확보");
                            System.out.println("2. 비동기 메서드를 'async void'로 선언하여 빠르게 교체");
                            System.out.print("구현 방식 선택 : ");

                            String safetyChoice = scanner.nextLine();
                            if (safetyChoice.equals("1")) {
                                System.out.println("---> [Action] 객체 파괴 시 비동기 작업이 즉시 중단되도록 토큰을 주입합니다.");
                                System.out.println("---> [Result] 메모리 누수 리스크를 차단하고 안정적인 비동기 환경을 구축했습니다.");
                            } else {
                                // [Failure Case] async void 사용
                                System.out.println("---> [Action] 편리함을 위해 예외 추적이 어려운 'async void'를 남발합니다.");
                                System.out.println("---> [Failure] 비동기 작업 중 에러가 발생했으나 호출 스택이 끊겨 원인 파악이 불가능합니다.");

                                System.out.println("\n[!] 경고: 런타임 예외가 발생했지만 로그가 찍히지 않아 미궁에 빠졌습니다.");
                                System.out.println("1. Git Checkout: 방금 수정한 코드를 모두 버리고 안정적인 시점으로 롤백한다");
                                System.out.println("2. 이슈 발행: '비동기 호출 구조 전면 재검토' 티켓을 생성하고 일단 주석 처리한다");
                                System.out.print("실패 대응 선택 : ");

                                String failureFollowUp = scanner.nextLine();
                                if (failureFollowUp.equals("1")) {
                                    System.out.println("---> [Action] 작업 내역을 삭제합니다. 오늘 아침의 노력이 수포로 돌아갔습니다.");
                                    System.out.println("---> [Result] 시스템의 안정성은 지켰으나, 오전 진척도가 초기화되었습니다.");
                                } else {
                                    System.out.println("---> [Action] 문제가 된 async void 구문을 찾아 UniTaskVoid로 교체하는 이슈를 등록합니다.");
                                    System.out.println("---> [Result] 기술 부채를 하나 더 남긴 채, 찝찝한 기분으로 다음 작업에 들어갑니다.");
                                }
                            }
                        } else {
                            System.out.println("\n---> [Action] 메인 루프의 실행 병목 지점을 시각화하여 분석합니다.");
                            System.out.println("1. FixedUpdate 기반의 물리 연산 동기화 최적화");
                            System.out.println("2. 유니티 API를 백그라운드 스레드에서 직접 호출 (위험)");
                            System.out.print("루프 최적화 선택 : ");

                            String loopStrategy = scanner.nextLine();
                            if (loopStrategy.equals("1")) {
                                System.out.println("---> [Action] 물리 연산 직전에 비동기 로직이 실행되도록 배치합니다.");
                                System.out.println("---> [Result] 입력값 반영과 물리 결과 사이의 프레임 지연을 최소화했습니다.");
                            } else {
                                System.out.println("---> [Action] 속도를 높이기 위해 별도 스레드에서 Transform 정보 수정을 시도합니다.");
                                System.out.println("---> [Failure] 'UnityException: Main Thread Only' 에러가 콘솔창을 도배합니다.");

                                System.out.println("\n[!] 위기: 유니티 메인 스레드 정책 위반으로 런타임 크래시가 발생했습니다.");
                                System.out.println("1. 스레드 교정: 'UniTask.SwitchToMainThread()'를 적절한 위치에 주입하여 수습한다");
                                System.out.println("2. 작업 보류: 꼬인 스레드 로직을 당장 풀기 어려우니 산책을 하며 머리를 식힌다");
                                System.out.print("실패 대응 선택 : ");

                                String crashFollowUp = scanner.nextLine();
                                if (crashFollowUp.equals("1")) {
                                    System.out.println("---> [Action] 컨텍스트 전환 위치를 한 줄씩 디버깅하며 다시 잡습니다.");
                                    System.out.println("---> [Result] 삽질 끝에 에러를 잡았습니다.");
                                } else {
                                    System.out.println("---> [Action] 모니터를 끄고 작업실 밖 찬바람을 쐬러 나갑니다.");
                                    System.out.println("---> [Result] 기분 전환은 되었으나, 해결하지 못한 버그가 머릿속을 맴돕니다.");
                                    System.out.println("---> [Result] 다시 이슈 발행을 하고, 시도합니다...");
                                }
                            }
                        }

                    } else if (techDetail.equals("2")) {
                        System.out.println("\n---> [Action] R3를 활용하여 복합적인 상태 전파 로직을 단순화합니다.");
                        System.out.println("1. 'ReactiveProperty'를 활용한 UI 데이터 바인딩");
                        System.out.println("2. 다중 스트림을 묶어 복합적인 상태 변화 감지");
                        System.out.print("설계 방식 선택 : ");

                        String r3Action = scanner.nextLine();
                        if (r3Action.equals("1")) {
                            System.out.println("\n---> [Action] 값이 바뀔 때만 UI가 갱신되도록 'DistinctUntilChanged'를 적용합니다.");
                            System.out.println("1. 수명 주기에 맞춘 폐기(Dispose) 처리 포함");
                            System.out.println("2. 일단 구독 로직만 빠르게 구현");
                            System.out.print("상세 선택 : ");

                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Result] 메모리 누수 없이 깔끔한 리액티브 UI 시스템을 구축했습니다.");
                            } else {
                                System.out.println("---> [Failure] 해제 로직 누락으로 인해 씬 전환 시 중복 실행 및 메모리 누수 발생!");
                                System.out.println("\n[!] 누수 탐지! 어떻게 대안을 마련하시겠습니까?");
                                System.out.println("1. 모든 클래스에 'CompositeDisposable'을 도입하여 수동 해제 로직 강제화");
                                System.out.println("2. 유니티의 'OnDestroy'를 활용한 자동 해제 확장 메서드(AddTo) 적용");
                                System.out.print("대안 선택 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Action] 코드 양은 늘어나지만 명시적으로 구독을 관리하도록 구조를 바꿉니다.");
                                    System.out.println("---> [Result] 관리 포인트가 확실해져 예측 가능한 시스템이 되었습니다.");
                                } else {
                                    System.out.println("---> [Action] R3에서 제공하는 라이프사이클 관리 기능을 적극 활용합니다.");
                                    System.out.println("---> [Result] 실수할 여지를 원천 차단하는 효율적인 코드로 거듭났습니다.");
                                }
                            }
                        } else {
                            System.out.println("\n---> [Action] 여러 조건이 충족될 때만 스킬이 발동되게 짭니다.");
                            System.out.println("1. 필터링 조건을 정교하게 설정하여 최적화");
                            System.out.println("2. 모든 변화에 즉각 반응하도록 무거운 연산 연결");
                            System.out.print("최적화 선택 : ");

                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Result] 복잡한 조건문 없이 깔끔한 스킬 발동 로직을 정리했습니다.");
                            } else {
                                System.out.println("---> [Failure] 과도한 이벤트 발생으로 CPU 점유율 급증 및 프레임 드랍 발생!");
                                System.out.println("\n[!] 성능 저하! 어떻게 대안을 마련하시겠습니까?");
                                System.out.println("1. 'ThrottleFrame'을 적용해 프레임당 실행 횟수를 강제로 제한");
                                System.out.println("2. 스트림 대신 필요한 시점에만 값을 가져오는 Pull 방식으로 회귀");
                                System.out.print("대안 선택 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Action] 이벤트의 홍수 속에서 필요한 신호만 골라내도록 여과 장치를 설치합니다.");
                                    System.out.println("---> [Result] 부드러운 프레임을 유지하면서 리액티브의 장점도 지켜냈습니다.");
                                } else {
                                    System.out.println("---> [Action] 무분별한 리액티브 도입 대신 중요한 구간만 선별적으로 적용합니다.");
                                    System.out.println("---> [Result] 오버 엔지니어링을 경계하며 성능과 가독성 사이의 타협점을 찾았습니다.");
                                }
                            }
                        }

                    } else {
                        System.out.println("---> [Failure] 어떤 기술을 먼저 손댈지 고민하다가 에디터 폰트 설정만 바꿨습니다.");
                    }

                } else if (sideProjectArea.equals("2")) {
                    System.out.println("\n[!] 플레이 시스템 상세 설계 및 구현:");
                    System.out.println("1. 레일기반 캐릭터 이동 시스템 - 경로 및 속도 제어");
                    System.out.println("2. 캐릭터 제어 - 충돌 및 물리 판정 고도화");
                    System.out.println("3. 스킬 엔진 - 추상화 및 데이터 연동");
                    System.out.print("상세 영역 선택 : ");
                    String playDetail = scanner.nextLine();

                    if (playDetail.equals("1")) {
                        System.out.println("\n---> [Action] 캐릭터의 이동 경로가 될 레일 시스템 설계를 시작합니다.");
                        System.out.println("1. 노드(Transform) 배치 및 경로 생성 로직");
                        System.out.println("2. 레일 위 주행 물리 및 곡률 가감속 제어");
                        System.out.print("상세 공정 선택 : ");

                        String railChoice = scanner.nextLine();
                        if (railChoice.equals("1")) {
                            System.out.println("\n---> [Action] 맵에 Transform 오브젝트들을 배치하여 레일 노드를 구성합니다.");
                            System.out.println("1. 빈 오브젝트를 하나씩 생성하여 수동으로 노드 연결");
                            System.out.println("2. 부모 오브젝트 아래의 자식(Children)들을 자동으로 탐색하여 경로화");
                            System.out.print("노드 구성 방식 : ");

                            String nodeMethod = scanner.nextLine();
                            if (nodeMethod.equals("1")) {
                                System.out.println("\n---> [Action] 각 노드의 인스펙터에서 'Next Node'를 수동으로 할당합니다.");
                                System.out.println("1. 노드 간의 거리를 일정하게 유지하며 정밀 배치");
                                System.out.println("2. 일단 배치 후 베지어 핸들(Handle)로 곡선 형태 다듬기");
                                System.out.print("배치 전략 선택 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Result] 노드 밀도가 균일하여 등속도 운동 구현이 매우 쉬워졌습니다.");
                                } else {
                                    System.out.println("---> [Result] 급격한 곡선 구간에서 노드 부족으로 경로가 튀는 현상을 발견했습니다.");
                                }
                            } else {
                                System.out.println("\n---> [Action] 'GetComponentsInChildren'을 활용하여 자식 객체들을 경로 리스트로 변환합니다.");
                                System.out.println("1. 리스트 순서(Sibling Index)를 기준으로 자동 연결");
                                System.out.println("2. 노드 간 거리에 따라 자동 계산");
                                System.out.print("자동화 옵션 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Result] 노드 순서만 바꾸면 경로가 즉시 갱신되어 작업 속도가 빨라졌습니다.");
                                } else {
                                    System.out.println("---> [Result] 곡률 자동 계산 로직에 버그가 있어 경로가 꼬였습니다. 디버깅이 필요합니다.");
                                }
                            }

                        } else {
                            System.out.println("\n---> [Action] 레일의 굽은 정도(곡률)에 따른 주행 물리 로직을 정교화합니다.");
                            System.out.println("---> [Failure] 코너 구간에서 튕겨 나가려는 힘을 이기지 못하고 캐릭터가 이탈합니다!");
                            System.out.println("1. 속도 제한: 코너에서 속도가 안 붙게 강제로 최대치 깎기");
                            System.out.println("2. 경로 추적: 다음 노드를 바라보는 방향으로 몸을 틀어 자석처럼 경로에 붙이기");
                            System.out.println("3. 물리 재설계: 실제 회전 원리를 적용해 속도와 무게에 따른 원심력 계산");
                            System.out.print("물리 수정 방식 : ");

                            String railFix = scanner.nextLine();
                            if (railFix.equals("1")) {
                                System.out.println("---> [Action] 코너에 진입할 때 속도 수치를 강제로 낮춰 이탈을 막습니다.");
                                System.out.println("---> [Result] 안정적이지만 박진감이 사라졌습니다. 단순한 컨베이어 벨트 같은 느낌입니다.");
                            } else if (railFix.equals("2")) {
                                System.out.println("---> [Action] 물리 법칙은 일단 제쳐두고, 캐릭터가 항상 레일 위만 밟도록 강제로 위치를 고정합니다.");
                                System.out.println("---> [Result] 어떤 속도에서도 절대 이탈하지 않지만, 움직임이 기계처럼 딱딱해 보입니다.");
                            } else {
                                System.out.println("---> [Action] 레일이 꺾이는 각도에 맞춰 가속도를 미세하게 조절하는 공식을 적용합니다.");
                                System.out.println("---> [Result] 실제 롤러코스터처럼 코너에서 몸이 쏠리는 듯한 역동적인 움직임을 구현했습니다.");
                            }
                        }

                    } else if (playDetail.equals("2")) {
                        System.out.println("\n---> [Action] 2D 탑뷰 환경에 최적화된 이동 및 충돌 로직을 리팩토링합니다.");
                        System.out.println("1. 충돌 보정 및 미끄러짐 (Corner Slip & Wall Sliding)");
                        System.out.println("2. 대시(Dash) 및 회피 동작의 관성 제어");
                        System.out.print("물리 과제 선택 : ");

                        String moveChoice = scanner.nextLine();
                        if (moveChoice.equals("1")) {
                            System.out.println("\n---> [Action] 캐릭터가 벽 모서리에 걸렸을 때의 보정 로직을 설계합니다.");
                            System.out.println("1. 레이캐스트를 여러 갈래로 쏴서 빈 공간 찾기");
                            System.out.println("2. 충돌 박스를 둥근 모양으로 교체하여 미끄러뜨리기");
                            System.out.print("보정 방식 선택 : ");

                            String cornerMethod = scanner.nextLine();
                            if (cornerMethod.equals("1")) {
                                System.out.println("\n---> [Action] 캐릭터의 이동 방향으로 3개의 가느다란 감지선을 쏩니다.");
                                System.out.println("1. 중앙은 막혔지만 측면선 하나가 비어있다면 해당 방향으로 위치 이동");
                                System.out.println("2. 보정되는 거리를 아주 작게 설정하여 정밀도 높이기");
                                System.out.print("세부 튜닝 선택 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Result] 벽에 닿자마자 캐릭터가 알아서 빈 공간을 찾아 슥 미끄러져 들어갑니다.");
                                    System.out.println("---> [Result] 유저는 자신이 벽에 걸렸었다는 사실조차 모른 채 쾌적함을 느낍니다.");
                                } else {
                                    System.out.println("---> [Result] 보정 폭이 너무 작아 웬만한 모서리에는 여전히 캐릭터가 걸립니다.");

                                    System.out.println("\n[!] '이 맛이 아닌데...' 수치 재조정이 필요합니다.");
                                    System.out.println("1. 보정 허용 범위(Offset)를 픽셀 단위로 조금씩 늘려본다");
                                    System.out.println("2. 보정 시 밀어주는 속도(Correction Speed)를 높여본다");
                                    System.out.print("수정 방향 선택 : ");

                                    String tuneChoice = scanner.nextLine();
                                    if (tuneChoice.equals("1")) {
                                        System.out.println("\n---> [Action] 보정 범위를 기존 2픽셀에서 4픽셀로 확장합니다.");
                                        System.out.println("1. 오차 범위가 커졌으니 다른 벽을 뚫고 나가는지 체크");
                                        System.out.println("2. 일단 이대로 빌드해서 테스트");
                                        System.out.print("안전 검사 선택 : ");

                                        if (scanner.nextLine().equals("1")) {
                                            System.out.println("---> [Action] 맵의 좁은 틈새들을 돌아다니며 '벽뚫기' 버그가 없는지 확인합니다.");
                                            System.out.println("---> [Result] 버그 없이 완벽하게 미끄러지는 황금 수치를 찾아냈습니다.");
                                        } else {
                                            System.out.println("---> [Failure] 보정 범위가 너무 넓어져서 얇은 벽을 그냥 통과해버리는 대형 사고가 터졌습니다.");
                                            System.out.println("---> [Action] 식은땀을 흘리며 다시 수치를 낮추고 로직을 점검합니다.");
                                        }

                                    } else {
                                        System.out.println("\n---> [Action] 위치를 즉시 옮기지 않고 부드럽게 밀리도록 보간(Lerp) 수치를 높입니다.");
                                        System.out.println("---> [Result] 순간이동 하는 느낌 없이 자연스럽게 벽을 타고 흐르는 연출이 완성되었습니다.");
                                    }
                                }
                            } else {
                                System.out.println("\n---> [Action] 물리 엔진의 특성을 이용하기 위해 콜라이더를 둥글게 깎습니다.");
                                System.out.println("---> [Result] 코딩은 편해졌지만, 캐릭터가 벽 모서리에서 빙글빙글 도는 부작용이 생겼습니다.");

                                System.out.println("\n[!] 부작용 발생! 어떻게 수습하시겠습니까?");
                                System.out.println("1. 리지드바디(Rigidbody)의 회전 고정(Freeze Rotation) 옵션을 켠다");
                                System.out.println("2. 결국 다시 레이캐스트 방식의 코드로 돌아간다");
                                System.out.print("수습 선택 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Result] 회전은 막았지만, 각진 벽면에선 여전히 덜컹거림이 남았습니다.");
                                } else {
                                    System.out.println("---> [Action] 노가다를 감수하고 정밀한 픽셀 체크 로직을 다시 작성합니다.");
                                }
                            }

                        } else {
                            System.out.println("\n---> [Action] 이스트워드 특유의 '쫀득한' 대시 조작감을 구현합니다.");
                            System.out.println("1. 대시 시작 시 순간적인 가속도와 끝날 때의 잔상 효과");
                            System.out.println("2. 대시 중 무적 판정 및 장애물 통과 로직");
                            System.out.print("조작감 개선 선택 : ");

                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Action] 대시가 끝나는 시점에 미세한 감속 곡선을 주어 무게감을 더합니다.");
                                System.out.println("---> [Result] 캐릭터가 가벼워 보이지 않고 묵직하게 멈춰 서는 손맛을 잡았습니다.");
                            } else {
                                System.out.println("---> [Action] 대시 시작 프레임에 충돌 판정을 일시적으로 레이어 분리 처리합니다.");
                                System.out.println("---> [Result] 좁은 틈이나 적의 공격을 뚫고 지나가는 전략적인 플레이가 가능해졌습니다.");
                            }
                        }

                    } else if (playDetail.equals("3")) {
                        System.out.println("\n---> [Action] 2D 액션에 특화된 스킬 시스템을 설계합니다.");
                        System.out.println("1. 무기 교체 및 공격 콤보 시스템 (Weapon Swap)");
                        System.out.println("2. 투척물 및 충전(Charge)형 스킬 로직");
                        System.out.print("아키텍처 선택 : ");

                        String skillChoice = scanner.nextLine();
                        if (skillChoice.equals("1")) {
                            System.out.println("\n---> [Action] 프라이팬, 샷건 등 무기별로 다른 공격 로직을 캡슐화합니다.");
                            System.out.println("1. 근접 공격의 히트박스 타이밍과 애니메이션 이벤트 연동");
                            System.out.println("2. 무기 교체 시 기존 공격 캔슬 및 후딜레이 초기화 제어");
                            System.out.print("상세 설계 방향 : ");

                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Action] 애니메이션의 특정 프레임에서만 충돌체가 활성화되도록 이벤트를 짭니다.");
                                System.out.println("---> [Result] 눈으로 보는 동작과 실제 타격 판정이 일치하여 신뢰도가 높아졌습니다.");
                            } else {
                                System.out.println("---> [Action] 무기를 바꿀 때 상태 머신(FSM)을 강제로 초기화하여 조작 씹힘을 방지합니다.");
                                System.out.println("---> [Result] 빠르고 경쾌한 무기 스왑 액션이 가능해졌습니다.");
                            }
                        } else {
                            System.out.println("\n---> [Action] 버튼을 꾹 눌러서 발동하는 충전 시스템을 구현합니다.");
                            System.out.println("1. 충전 단계에 따른 이펙트 크기 및 데미지 배율 적용");
                            System.out.println("2. ScriptableObject를 활용해 무기별 최대 충전 시간 데이터화");
                            System.out.print("데이터화 방식 : ");

                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Action] 충전 게이지가 찰 때마다 캐릭터 주위에 파티클이 모이도록 구현합니다.");
                                System.out.println("---> [Result] 시각적인 피드백이 확실해져 유저가 충전 완료 시점을 직관적으로 알게 됩니다.");
                            } else {
                                System.out.println("---> [Action] 무기별 밸런스 수치를 SO 파일로 빼서 실시간으로 튜닝합니다.");
                                System.out.println("---> [Result] 매번 코드를 수정할 필요 없이 '황금 밸런스'를 찾는 속도가 빨라졌습니다.");
                            }
                        }
                    }

                } else if (sideProjectArea.equals("3")) {
                    System.out.println("\n[!] 시스템 상세 설계:");
                    System.out.println("1. 보물 창고(인벤토리) - 데이터 연동 및 획득 연출");
                    System.out.println("2. 장비 및 스탯 - 능력치 계산기 및 실시간 갱신");
                    System.out.print("상세 선택 : ");
                    String metaDetail = scanner.nextLine();

                    if (metaDetail.equals("1")) {
                        System.out.println("\n---> [Action] 아이템 DB와 인벤토리 UI를 연결하고 획득 연출을 짭니다.");
                        System.out.println("1. 아이템 획득 시 캐릭터 머리 위로 아이템을 띄우는 연출 (Eastward 스타일)");
                        System.out.println("2. 인벤토리 가득 참 상태일 때의 예외 처리 로직");
                        System.out.print("상세 공정 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 획득한 아이템의 스프라이트를 생성하고 통통 튀는 애니메이션을 넣습니다.");
                            System.out.println("1. 연출 중 일시적으로 플레이어 조작 제한");
                            System.out.println("2. 조작 제한 없이 하단 알림창으로만 표시");
                            System.out.print("연출 방식 : ");

                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Result] 중요한 보물을 얻었다는 성취감이 확실히 전달됩니다.");
                            } else {
                                System.out.println("---> [Result] 게임의 흐름은 끊기지 않지만, 획득한 느낌이 다소 심심합니다.");
                            }
                        } else {
                            System.out.println("---> [Action] 가방이 꽉 찼을 때 아이템을 바닥에 다시 드롭하거나 팝업을 띄웁니다.");
                            System.out.println("---> [Result] 데이터 유실 없이 깔끔하게 아이템 획득 로직을 방어했습니다.");
                        }

                    } else {
                        System.out.println("\n---> [Action] 장비 등급과 강화 수치를 반영하는 스탯 시스템을 구축합니다.");
                        System.out.println("1. 복합 스탯(공격력 + 속성 데미지) 계산 식 정립");
                        System.out.println("2. 장비 교체 시 UI 수치 텍스트 애니메이션(카운팅) 구현");
                        System.out.print("상세 작업 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 기본 데미지에 곱연산과 합연산이 섞인 최종 데미지 공식을 검증합니다.");
                            System.out.println("---> [Result] 특정 조건에서 데미지가 비정상적으로 뻥튀기되는 버그를 잡았습니다.");
                        } else {
                            System.out.println("---> [Action] 공격력이 오를 때 숫자가 드르륵 올라가는 시각 효과를 넣습니다.");
                            System.out.println("---> [Result] 장비를 바꿀 때마다 강해진다는 느낌을 시각적으로 강조했습니다.");
                        }
                    }

                } else {
                    System.out.println("---> [Failure] 목표를 정하지 못해 유니티 에디터 창만 만지작거리다 오전이 끝났습니다.");
                }

            } else if (workStatus.equals("3")) {
                System.out.println("\n[?] 팀노바 학습: 현재 진행 중인 과제 단계를 선택하세요.");
                System.out.println("1. [1주차] 사용성에 맞는 컴퓨터 견적 및 논리 세우기");
                System.out.println("2. [2주차] 사이트 분석 및 변수/자료형 매칭 검증");
                System.out.println("3. 기술 내부 동작 원리 및 문서화 (기본 소양)");
                System.out.print("상세 선택 : ");
                String studyDetail = scanner.nextLine();

                if (studyDetail.equals("1")) {
                    System.out.println("\n---> [Action] '나의 사용성'을 정의하고 그에 맞는 부품 사양을 분석합니다.");
                    System.out.println("1. 현재 개발 환경(Unity 6, 멀티태스킹)에 최적화된 CPU/RAM 기준 설정");
                    System.out.println("2. 예산 대비 성능 분석 및 부품 간 병목 현상 검증");
                    System.out.print("상세 공정 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Action] 왜 Ryzen 5 5600이 필요한지, 내장 그래픽이 들어가는 CPU를 굳이 사용하지 않는지 논리를 정리합니다.");
                        System.out.println("1. 벤치마크 점수와 CPU스펙 비교 비교");
                        System.out.println("2. AI 복사 붙여넣기로 과제 진행");
                        System.out.print("검증 방식 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 사용성에 기반한 명확한 기준이 확립되었습니다. 누가 물어도 논리적으로 답변 가능합니다.");
                        } else {
                            System.out.println("---> [Result] '왜 나에게 필요한가'에 대한 객관적 기준이 부족하다는 피드백을 예상합니다.");
                            System.out.println("---> [Result] 처음부터 다시 과제에 대해 고민해봅니다");
                        }
                    } else {
                        System.out.println("---> [Action] 컴퓨존 등 판매 데이터를 분석하여 가성비와 안정성을 동시에 검토합니다.");
                        System.out.println("---> [Result] 시장가에 근거한 합리적인 선택임을 입증할 자료를 확보했습니다.");
                    }

                } else if (studyDetail.equals("2")) {
                    System.out.println("\n---> [Action] 특정 사이트를 선정하여 내부 데이터 구조를 변수로 치환해 봅니다.");
                    System.out.println("1. 사이트 UI 요소들을 어떤 자료형(int, String, boolean 등)으로 담을지 분석");
                    System.out.println("2. 해당 자료형을 선택한 이유에 대한 논리적 근거 작성");
                    System.out.print("분석 단계 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Action] 이미지란 무엇인가? 텍스트란 무엇인가? 근본부터 질문합니다.");
                        System.out.println("1. 메모리 효율성과 데이터 표현 범위를 고려한 선택");
                        System.out.println("2. 서비스 확장성(예: 숫자가 커질 경우 long 고려)까지 염두에 둔 설계");
                        System.out.print("논리 전개 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 단순 암기가 아닌, 데이터의 본질에 근거한 변수 활용 능력을 증명했습니다.");
                        } else {
                            System.out.println("---> [Result] 명확하게 무엇을 모르고있는지, 그리고 왜 선택했는지 근거가 부족함을 깨닫습니다.");
                            System.out.println("---> [Result] 처음부터 다시 과제에 대해 고민해봅니다");
                        }
                    } else {
                        System.out.println("---> [Action] 선정된 사이트의 핵심 기능(로그인, 장바구니 등)을 변수 꾸러미로 시각화합니다.");
                        System.out.println("---> [Result] 추상적인 웹사이트를 개발자의 시선(데이터 구조)으로 바라보기 시작했습니다.");
                    }

                } else if (studyDetail.equals("3")) {
                    System.out.println("\n---> [Action] 공부한 내용을 남에게 설명하듯 문서로 구조화합니다.");
                    System.out.println("1. 특정 기술의 메모리 적재 과정 흐름도 작성");
                    System.out.println("2. 기술 스택 비교 분석표 작성 (A vs B)");
                    System.out.print("문서화 전략 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Result] 머릿속에 파편화되어 있던 지식들이 하나의 논리적인 선으로 연결되었습니다.");
                    } else {
                        System.out.println("---> [Result] 표면적인 정보 나열은 끝냈으나, 핵심 원리에 대한 설명이 부족해 재조사가 필요합니다.");
                    }
                } else {
                    System.out.println("---> [Failure] 과제의 본질(사용성, 논리적 근거)을 놓치고 단순 구글링에 의존했습니다.");
                }
            }
        }

        System.out.println("\n========== [오전 일과 종료] ==========");

        // 4. 오후 업무 상황 분기


        System.out.println("\n========== [오후 일과 시작] ==========");
        System.out.println("[!] 오후 2시. 집중력이 최고조에 달하거나, 혹은 급격히 무너지는 시점입니다.");

        System.out.println("1. 업무 연속성 유지");
        System.out.println("2. 새로운 작업으로 전환");
        System.out.println("3. 외부 활동 및 리프레시 (지인 만남 / 테니스)");
        System.out.print("선택 : ");
        String pmChoice = scanner.nextLine();

        if (pmChoice.equals("1")) {
            if (workStatus.equals("1")) { // 프리랜서 외주 업무
                System.out.println("\n[!] 프리랜서 업무: 현재 프로젝트의 진행 단계는?");
                System.out.println("1. 신규 프로젝트 분석 및 설계 (여유로움)");
                System.out.println("2. 핵심 기능 구현 및 개발 (집중 필요)");
                System.out.println("3. 최종 마감 및 버그 수정 (긴박함)");
                System.out.print("상세 선택 : ");
                String freelanceStatus = scanner.nextLine();

                if (freelanceStatus.equals("1")) {
                    System.out.println("\n---> [Action] 클라이언트가 보낸 기획서를 검토하며 요구사항을 분석합니다.");
                    System.out.println("1. 기술적 실현 가능성 검토 (PoC)");
                    System.out.println("2. 시스템 구조 설계 (Architecture)");
                    System.out.print("상세 작업 선택 : ");
                    String initialWork = scanner.nextLine();

                    if (initialWork.equals("1")) {
                        System.out.println("\n---> [Action] 핵심 라이브러리가 현재 환경에서 충돌 없이 돌아가는지 테스트합니다.");
                        System.out.println("1. 공식 샘플 코드 실행 및 의존성 분석");
                        System.out.println("2. 샌드박스 구축을 통한 부하 및 호환성 테스트");
                        System.out.print("검증 방식 선택 : ");
                        String pocMethod = scanner.nextLine();

                        if (pocMethod.equals("1")) {
                            System.out.println("\n---> [Action] 예제 코드를 컴파일하며 외부 라이브러리 의존 관계를 살핍니다.");
                            System.out.println("---> [Failure] 프로젝트의 .NET API 호환성 수준이 라이브러리 요구 사양보다 낮아 빌드 에러가 발생합니다.");
                            System.out.println("1. Player Settings에서 .NET Framework 수준 상향");
                            System.out.println("2. 하위 호환성이 보장된 구버전 라이브러리 탐색");
                            System.out.print("대응 선택 : ");
                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Action] 프로젝트 전역 설정을 변경하고 종속성을 재구성합니다.");
                                System.out.println("---> [Result] 빌드 충돌을 해결하고 내부 소스 분석 가능한 상태를 확보했습니다.");
                            } else {
                                System.out.println("---> [Action] 호환성이 확인된 릴리즈 버전을 찾아 재설치합니다.");
                                System.out.println("---> [Result] 최신 기능은 포기했으나 런타임 안정성을 우선 확보했습니다.");
                            }
                        } else {
                            System.out.println("\n---> [Action] 실제 서비스와 유사한 데이터 셋을 넣어 부하 테스트를 진행합니다.");
                            System.out.println("---> [Failure] 특정 데이터 포맷 처리 중 힙 메모리 점유율이 비정상적으로 치솟습니다.");
                            System.out.println("1. 프로파일러를 통한 메모리 누수 지점 추적");
                            System.out.println("2. 데이터 로딩 방식을 스트리밍 방식으로 변경");
                            System.out.print("대응 선택 : ");
                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Action] 참조 해제가 안 되는 싱글톤 오브젝트를 발견하여 정리합니다.");
                                System.out.println("---> [Result] 병목 현상을 해결하고 최적화 포인트를 확보했습니다.");
                            } else {
                                System.out.println("---> [Action] 한 번에 모든 데이터를 로드하지 않고 분할 로딩 로직을 짭니다.");
                                System.out.println("---> [Result] 메모리 부하를 50% 이상 절감하는 데 성공했습니다.");
                            }
                        }
                    }
                } else if (freelanceStatus.equals("2")) {
                    System.out.println("\n---> [Action] 결제 모듈 및 사용자 API 연동 작업을 시작합니다.");
                    System.out.println("1. 테스트 주도 개발(TDD) 기반의 꼼꼼한 구현");
                    System.out.println("2. 기능 동작 중심의 빠른 프로토타이핑");
                    System.out.print("개발 전략 선택 : ");
                    String devStyle = scanner.nextLine();

                    if (devStyle.equals("1")) {
                        System.out.println("\n---> [Action] 실패하는 테스트 케이스를 먼저 작성합니다.");
                        System.out.println("---> [Failure] 비동기 콜백 호출 시 타이밍 이슈로 테스트가 간헐적으로 실패합니다.");
                        System.out.println("1. UniTask를 활용하여 결과 수신 시까지 명확한 Wait 로직 추가");
                        System.out.println("2. 타임아웃 예외 처리를 통해 무한 대기 방지");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] await 구문을 정교하게 배치하여 실행 순서를 강제합니다.");
                            System.out.println("---> [Result] 에러가 나는 상황이 한정적으로 제한된 결제 모듈을 완성했습니다.");
                        } else {
                            System.out.println("---> [Action] CancellationToken을 연결하여 일정 시간 후 작업을 강제 종료합니다.");
                            System.out.println("---> [Result] 시스템이 멈추지 않는 방어적인 로직을 구축했습니다.");
                        }
                    } else {
                        System.out.println("\n---> [Action] 빠른 연동을 위해 화면 위주로 코드를 배치합니다.");
                        System.out.println("---> [Failure] API 응답값이 null일 경우에 대한 방어 로직 누락으로 크래시가 발생합니다.");
                        System.out.println("1. 전역적인 Null Check 유틸리티 도입");
                        System.out.println("2. 옵셔널 패턴을 활용한 데이터 접근");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 모든 데이터 진입점에 유효성 검사기를 배치합니다.");
                            System.out.println("---> [Result] 런타임 에러를 줄인 프로토타입을 완성했습니다.");
                        } else {
                            System.out.println("---> [Action] 데이터가 없을 경우 기본값을 반환하도록 구조를 바꿉니다.");
                            System.out.println("---> [Result] 예외 상황에서도 UI가 깨지지 않는 안정성을 확보했습니다.");
                        }
                    }
                } else if (freelanceStatus.equals("3")) {
                    System.out.println("\n---> [Action] 납품 전 최종 빌드를 뽑아 품질 검수(QA)를 시작합니다.");
                    System.out.println("1. 고피쉬 (Go Fish) / 2. 포츈 (Fortune) / 3. 스도쿠 / 4. 스푼 / 5. 서브웨이");
                    System.out.print("게임 선택 : ");
                    String gameChoice = scanner.nextLine();

                    if (gameChoice.equals("1")) {
                        System.out.println("\n---> [Action] 고피쉬: 특정 페이즈에서 클릭 방지 로직을 테스트합니다.");
                        System.out.println("---> [Failure] 연타 시 클릭 차단 레이어가 뚫리는 하드웨어 매크로 현상을 발견했습니다.");
                        System.out.println("1. 입력 즉시 플래그 변수로 락을 거는 로직 추가");
                        System.out.println("2. 버튼 이벤트 사이에 최소 쿨타임 강제");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 상태 관리를 통해 중복 진입을 원천 차단합니다.");
                            System.out.println("---> [Result] 비정상적인 입력에도 로직이 꼬이지 않게 수습했습니다.");
                        } else {
                            System.out.println("---> [Action] 시간 기반 필터링을 도입하여 유효 입력만 선별합니다.");
                            System.out.println("---> [Result] 오작동을 해결하고 사용자 경험을 개선했습니다.");
                        }
                    } else if (gameChoice.equals("2")) {
                        System.out.println("\n---> [Action] 포츈: 시드(Seed) 입력 제어 로직을 검수합니다.");
                        System.out.println("---> [Failure] 특수 문자를 섞어 입력할 경우 정규식 검사 단계에서 에러가 터집니다.");
                        System.out.println("1. 허용 문자열 범위를 재정의하는 정규식 수정");
                        System.out.println("2. 입력 필드 자체에서 특수 문자 입력을 원천 차단");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 다양한 예외 문자를 포함하도록 검증 식을 보강합니다.");
                            System.out.println("---> [Result] 데이터 오염 없는 안정적인 입력을 구현했습니다.");
                        } else {
                            System.out.println("---> [Action] InputField의 Content Type 설정을 정수 전용으로 바꿉니다.");
                            System.out.println("---> [Result] 휴먼 에러 가능성을 원천 제거했습니다.");
                        }
                    } else if (gameChoice.equals("3")) {
                        System.out.println("\n---> [Action] 스도쿠: UI 컴포넌트 비활성화 이슈를 추적합니다.");
                        System.out.println("---> [Failure] 힌트 사용 직후 특정 버튼의 활성화 상태가 돌아오지 않습니다.");
                        System.out.println("1. CanvasGroup의 블로킹 상태를 수동으로 해제");
                        System.out.println("2. 버튼의 Interactable 속성을 명시적으로 갱신");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 전역 UI 매니저를 통해 레이어 상태를 리셋합니다.");
                            System.out.println("---> [Result] 먹통이 되던 UI 조작 기능을 정상화했습니다.");
                        } else {
                            System.out.println("---> [Action] 버튼 컴포넌트에 접근하여 강제로 활성화 신호를 보냅니다.");
                            System.out.println("---> [Result] 버그를 수정하고 부드러운 전환을 확인했습니다.");
                        }
                    } else if (gameChoice.equals("4")) {
                        System.out.println("\n---> [Action] 스푼: 음식 오브젝트의 실시간 소실 문제를 검토합니다.");
                        System.out.println("---> [Failure] 오브젝트 풀링 반환 시점이 꼬여 음식이 허공에서 사라집니다.");
                        System.out.println("1. 수명 주기를 관리하는 스크립트의 실행 순서 조정");
                        System.out.println("2. 반환 로직에 0.1초의 지연 시간을 주어 물리 연산 완료 대기");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] Execution Order를 변경하여 초기화 로직을 우선시합니다.");
                            System.out.println("---> [Result] 사라짐 현상을 잡고 데이터 잔상 문제를 해결했습니다.");
                        } else {
                            System.out.println("---> [Action] 물리 충돌이 끝난 후 풀에 반환되도록 로직을 수정합니다.");
                            System.out.println("---> [Result] 안정적인 오브젝트 관리가 가능해졌습니다.");
                        }
                    } else if (gameChoice.equals("5")) {
                        System.out.println("\n---> [Action] 서브웨이: 씬 전환 시 NullReferenceException을 추적합니다.");
                        System.out.println("---> [Failure] 싱글톤 객체가 파괴되기 직전 접근을 시도하여 에러가 발생합니다.");
                        System.out.println("1. 널 조건부 연산자(?.)를 사용하여 안전하게 접근");
                        System.out.println("2. OnDestroy 시점에 이벤트 구독을 확실히 해제");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 객체 존재 여부를 확인하는 방어 코드를 전면 배치합니다.");
                            System.out.println("---> [Result] 씬 전환 시 발생하던 크래시를 완벽히 차단했습니다.");
                        } else {
                            System.out.println("---> [Action] 이벤트 버스 시스템의 잔여 핸들러를 모두 정리합니다.");
                            System.out.println("---> [Result] 메모리 누수 방지 및 예외 상황 수습을 완료했습니다.");
                        }
                    }
                }

            } else if (workStatus.equals("2")) { // 사이드 프로젝트 (Unity 6)
                System.out.println("\n[!] 사이드 프로젝트: 전투 시스템 폴리싱 및 버그 픽스");
                System.out.println("1. 회피 선딜레이 삭제 및 반응성 개선");
                System.out.println("2. 캐릭터 간격 겹침(Clumping) 버그 수정");
                System.out.println("3. 전투 UI 게이지 애니메이션 추가");
                System.out.print("작업 선택 : ");
                String unityTask = scanner.nextLine();

                if (unityTask.equals("1")) {
                    System.out.println("\n[?] 어떤 방식으로 선딜레이를 제거하시겠습니까?");
                    System.out.println("1. Animation Event를 활용한 프레임 단위 판정 조절");
                    System.out.println("2. State Machine Behaviour를 이용한 상태 진입 즉시 허용");
                    System.out.print("선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 애니메이션 클립의 특정 프레임에 트리거 이벤트를 심습니다.");
                        System.out.println("---> [Result] 조작 반응성이 눈에 띄게 개선되어 액션의 긴박함이 살아났습니다.");
                    } else {
                        System.out.println("---> [Action] 상태 머신의 OnStateEnter 시점에 입력 잠금을 해제합니다.");
                        System.out.println("---> [Failure] 휴먼 에러: 이전 애니메이션의 잔상(Blending) 중에도 회피가 발동하여 모션이 꼬입니다.");
                        System.out.println("1. Has Exit Time 옵션을 조정하여 전환 시점 최적화");
                        System.out.println("2. 애니메이션 레이어 가중치를 수동으로 계산하여 보정");
                        System.out.print("수정 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 전환 시간을 0.1초로 단축하여 즉각적인 모션 변화를 유도합니다.");
                            System.out.println("---> [Result] 끊김 없는 부드러운 회피 액션을 구현했습니다.");
                        } else {
                            System.out.println("---> [Action] 레이어별 가중치 수치를 직접 조정하여 부자연스러운 겹침을 제거합니다.");
                            System.out.println("---> [Result] 비주얼과 조작감 사이의 균형을 찾아냈습니다.");
                        }
                    }

                } else if (unityTask.equals("2")) {
                    System.out.println("\n[?] 캐릭터 간 충돌 및 길찾기 시스템을 어떻게 구축하시겠습니까?");
                    System.out.println("1. Physics.OverlapSphere 기반의 밀어내기 로직");
                    System.out.println("2. 직접 구현한 알고리즘 기반의 최적 경로 탐색");
                    System.out.print("선택 : ");

                    String moveChoice = scanner.nextLine();

                    if (moveChoice.equals("1")) {
                        System.out.println("---> [Action] 주변 캐릭터의 위치를 계산하여 반대 방향으로 힘을 가합니다.");
                        System.out.println("---> [Failure] 물리 엔진 충돌: 계산된 힘이 중첩되어 캐릭터가 갑자기 하늘로 튕겨 나갑니다.");
                        System.out.println("1. 리지드바디의 y축 위치를 고정(Freeze Position Y) 처리");
                        System.out.println("2. 힘을 가하는 대신 위치값(Transform)을 보간하여 이동");
                        System.out.print("수정 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 제약 조건을 추가하여 비정상적인 수직 사출 현상을 막습니다.");
                            System.out.println("---> [Result] 적들이 서로를 밀어내며 적절한 간격을 유지합니다.");
                        } else {
                            System.out.println("---> [Action] 물리 연산을 제외하고 Lerp를 이용해 부드럽게 위치를 조정합니다.");
                            System.out.println("---> [Result] 물리적 튕김 없이 안정적인 군집 이동을 구현했습니다.");
                        }
                    } else if (moveChoice.equals("2")) {
                        System.out.println("\n[?] 탐색 효율과 정밀도 중 무엇을 우선하시겠습니까?");
                        System.out.println("1. A* (에이스타) - 휴리스틱을 이용한 최단 경로 탐색");
                        System.out.println("2. BFS (너비 우선 탐색) - 모든 타일 완전 탐색");
                        System.out.println("3. 다익스트라 - 비용 기반의 가중치 탐색");
                        System.out.print("알고리즘 선택 : ");

                        String pathAlgo = scanner.nextLine();
                        if (pathAlgo.equals("1")) {
                            System.out.println("---> [Action] G비용과 H비용을 합산하여 다음 노드를 결정하는 로직을 짭니다.");
                            System.out.println("---> [Result] 성능과 정확도의 균형을 잡은 효율적인 길찾기를 완성했습니다.");
                        } else if (pathAlgo.equals("2")) {
                            System.out.println("---> [Action] Queue를 활용해 가까운 노드부터 순차적으로 탐색합니다.");
                            System.out.println("---> [Failure] 휴먼 에러: 맵이 넓어짐에 따라 탐색 노드 수가 폭증하여 프레임 드랍이 발생합니다.");
                            System.out.println("---> [Action] 탐색 범위를 제한하거나 UniTask를 이용해 연산을 분산 처리합니다.");
                            System.out.println("---> [Result] 부하를 줄이면서도 확실하게 목표를 찾는 로직을 확보했습니다.");
                        } else if (pathAlgo.equals("3")) {
                            System.out.println("---> [Action] 각 타일의 이동 비용(늪지대, 산악 등)을 반영한 경로를 탐색합니다.");
                            System.out.println("---> [Result] 지형의 특성을 고려한 전략적인 유닛 이동 시스템을 구축했습니다.");
                        } else {
                            System.out.println("---> [Failure] 유효하지 않은 알고리즘 선택입니다. 설계를 처음부터 다시 검토합니다.");
                        }
                    } else {
                        System.out.println("\n---> [Action] 구현 방식을 결정하지 못하고 유니티 에디터만 멍하니 바라봅니다.");
                        System.out.println("---> [Failure] 결정 장애: 이도 저도 아닌 상태로 에너지만 소모한 채 작업 시간이 종료됩니다.");
                    }

                } else if (unityTask.equals("3")) {
                    System.out.println("\n[?] HP 게이지 애니메이션의 연출 방식을 정합니다.");
                    System.out.println("1. DOTween - 체력 감소 시 흰색 잔상이 따라오는 효과");
                    System.out.println("2. Image Fill Amount - 피격 시 게이지가 떨리는 연출");
                    System.out.println("3. Sprite Color Swap - 위험 상태일 때 붉은색 점멸 효과");
                    System.out.print("선택 : ");

                    String uiTask = scanner.nextLine();
                    if (uiTask.equals("1")) {
                        System.out.println("---> [Action] 현재 체력 바 뒤에 별도의 Image를 배치하고 DoFillAmount로 딜레이를 줍니다.");
                        System.out.println("---> [Result] 타격 피드백이 명확해져 전투의 시각적 완성도가 높아졌습니다.");
                    } else if (uiTask.equals("2")) {
                        System.out.println("---> [Action] RectTransform의 위치값을 루프 돌려 Shake 효과를 구현합니다.");
                        System.out.println("---> [Failure] 휴먼 에러: 피격 방향 계산 오류로 게이지가 캔버스 밖으로 튀어나갑니다.");
                        System.out.println("1. 앵커(Anchor) 포인트를 중앙으로 고정하고 수치 재조정");
                        System.out.println("2. 진동 세기(Strength) 값을 낮추고 보간(Smoothing) 적용");
                        System.out.print("수정 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 피벗과 앵커를 정렬하여 상대 좌표 기준으로 진동하게 만듭니다.");
                            System.out.println("---> [Result] UI 레이아웃이 깨지지 않는 안정적인 피격 효과를 완성했습니다.");
                        } else {
                            System.out.println("---> [Action] 과도한 움직임을 제어하기 위해 수치를 픽셀 단위로 미세 튜닝합니다.");
                            System.out.println("---> [Result] 자극적이지 않으면서 확실한 타격감을 전달하게 되었습니다.");
                        }
                    } else {
                        System.out.println("---> [Action] Image 컴포넌트의 컬러 값을 반복적으로 교체합니다.");
                        System.out.println("---> [Result] 위기 상황임을 유저에게 직관적으로 알리는 연출이 적용되었습니다.");
                    }

                } else {
                    System.out.println("\n---> [Action] 구현 아이디어가 떠오르지 않아 핀터레스트에서 게임 레퍼런스를 수집합니다.");
                    System.out.println("---> [Result] 새로운 연출 영감을 얻어 내일의 구현 목록에 추가합니다.");
                }

            } else if (workStatus.equals("3")) { // 팀노바 3주차 과제
                System.out.println("\n[!] 팀노바: 제어문 심화 알고리즘 (3주차 과제)");
                System.out.println("1. [별 찍기] 중첩 반복문 기초");
                System.out.println("2. [마름모] 대칭 구조 논리 설계");
                System.out.println("3. [원형] 좌표 거리 공식 응용");
                System.out.print("과제 선택 : ");
                String studyTask = scanner.nextLine();

                if (studyTask.equals("1")) {
                    System.out.println("\n[?] 어떤 루프 구조로 별 찍기를 시작하시겠습니까?");
                    System.out.println("1. 직관적인 이중 for문 구성");
                    System.out.println("2. 단일 for문 내부에 조건문 중첩");
                    System.out.print("전략 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] i와 j의 관계식을 단순하게 설계하여 코드를 작성합니다.");
                        System.out.println("---> [Result] 군더더기 없는 로직으로 단번에 별 찍기에 성공했습니다.");
                    } else {
                        System.out.println("---> [Action] 단일 루프로 처리하려다 인덱스 계산이 복잡해집니다.");
                        System.out.println("---> [Failure] 휴먼 에러: 증감식 부호(++)를 잘못 써서 콘솔에 무한히 별이 찍힙니다.");
                        System.out.println("---> [Action] 강제 종료 후 j 변수 참조 위치를 수정합니다.");
                        System.out.println("---> [Result] 우회했지만 결국 의도한 모양을 출력했습니다.");
                    }

                } else if (studyTask.equals("2")) {
                    System.out.println("\n[?] 마름모의 대칭성을 어떻게 해결하시겠습니까?");
                    System.out.println("1. 상단/하단 루프를 완전히 독립적으로 분리");
                    System.out.println("2. 변수 하나로 공백과 별의 개수를 가감 제어");
                    System.out.print("전략 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 상단 삼각형과 하단 삼각형의 범위를 명확히 나누어 작성합니다.");
                        System.out.println("---> [Result] 논리가 꼬일 틈 없이 깔끔한 대칭 마름모가 출력되었습니다.");
                    } else {
                        System.out.println("---> [Action] 변수 하나에 여러 역할을 부여하다가 변곡점에서 값이 튑니다.");
                        System.out.println("---> [Failure] 아이디어 오류: 별 개수가 짝수로 늘어나 마름모가 비대칭으로 나옵니다.");
                        System.out.println("1. (2 * i + 1) 공식을 적용해 홀수 체계로 보정");
                        System.out.println("2. i값 자체를 2씩 증감시켜 루프 구조 변경");
                        System.out.print("수정 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 산술 공식을 보완하여 정중앙을 기준으로 별을 다시 배치합니다.");
                            System.out.println("---> [Result] 균형 잡힌 마름모를 완성했습니다.");
                        } else {
                            System.out.println("---> [Action] 제어 변수 자체를 홀수로 강제하여 논리적 비약을 제거합니다.");
                            System.out.println("---> [Result] 탄탄한 근거를 가진 코드로 거듭났습니다.");
                        }
                    }

                } else if (studyTask.equals("3")) {
                    System.out.println("\n[?] 원형의 좌표 시스템을 어떻게 정의하시겠습니까?");
                    System.out.println("1. (0, 0) 원점 기준의 사분면 확장 방식");
                    System.out.println("2. (r, r) 오프셋을 적용한 전체 스캔 방식");
                    System.out.print("전략 선택 : ");

                    if (scanner.nextLine().equals("2")) {
                        System.out.println("---> [Action] 좌표 평면 전체를 훑으며 피타고라스 정리로 별을 찍습니다.");
                        System.out.println("---> [Result] 콘솔 중앙에 일그러짐 없는 정원을 성공적으로 구현했습니다.");
                    } else {
                        System.out.println("---> [Action] 사분면 하나만 계산하여 복사하려다 좌표 변환에서 막힙니다.");
                        System.out.println("---> [Failure] 휴먼 에러: 원점이 구석에 박혀 원의 일부만 출력됩니다.");
                        System.out.println("1. 반복문 시작점을 -r로 넓혀 사분면 전체 확보");
                        System.out.println("2. 출력 시점에 좌표 보정값(Offset)을 더해 위치 이동");
                        System.out.print("수정 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Action] 음수 영역까지 루프 범위를 확장하여 잘린 부분을 채웁니다.");
                            System.out.println("---> [Result] 온전한 형태의 원형 알고리즘을 확보했습니다.");
                        } else {
                            System.out.println("---> [Action] 출력 로직에 수동 오프셋을 주어 좌표계를 이동시킵니다.");
                            System.out.println("---> [Result] 시각적으로 완벽한 위치에 원을 배치했습니다.");
                        }
                    }

                } else {
                    System.out.println("\n---> [Action] 피로 누적으로 코딩 효율이 저하되어 강의 영상을 복습합니다.");
                    System.out.println("---> [Result] 제어문의 동작 원리를 다시 다지며 다시 과제를 준비합니다.");
                }

            } else {
                System.out.println("\n---> [Action] 집중력이 풀려 코딩을 중단하고 팀노바 동영상 강의를 복습합니다.");
                System.out.println("---> [Result] 놓쳤던 개념을 다시 정리하며 내일의 과제를 기약합니다.");
            }

        } else if (pmChoice.equals("2")) {
            System.out.println("[?] 현재 흐름을 끊고 어떤 새로운 영역으로 두뇌를 환기하시겠습니까?");

            if (workStatus.equals("1")) {
                System.out.println("1. 사이드 프로젝트 (Unity 6 개발 갈증 해소)");
                System.out.println("2. 팀노바 과제 (기초 논리 및 알고리즘 강화)");
            } else if (workStatus.equals("2")) {
                System.out.println("1. 외주 프로젝트 (마감 및 클라이언트 대응)");
                System.out.println("2. 팀노바 과제 (알고리즘 및 데이터 구조)");
            } else {
                System.out.println("1. 외주 프로젝트 (실전 투입 및 수익 활동)");
                System.out.println("2. 사이드 프로젝트 (개인적 성취 및 기능 구현)");
            }

            System.out.print("전환 대상 선택 : ");
            String switchChoice = scanner.nextLine();

            if (workStatus.equals("1")) {
                workStatus = switchChoice.equals("1") ? "2" : "3";
            } else if (workStatus.equals("2")) {
                workStatus = switchChoice.equals("1") ? "1" : "3";
            } else {
                workStatus = switchChoice.equals("1") ? "1" : "2";
            }

            System.out.println("\n---> [Action] 기존 도구들을 닫고 새로운 프로젝트 환경을 로드합니다.");
            System.out.println("---> [Failure] 휴먼 에러: 이전 작업의 잔상이 남아 새로운 로직에 집중하지 못하고 멍하니 모니터만 봅니다.");
            System.out.println("1. 15분간 짧은 취침이나 스트레칭으로 뇌를 리셋한다.");
            System.out.println("2. 일단 코드를 한 줄씩 타이핑하며 억지로 몰입을 유도한다.");
            System.out.print("대응 선택 : ");

            if (scanner.nextLine().equals("1")) {
                System.out.println("---> [Action] 환기 시간을 가진 후 맑아진 정신으로 다시 앉습니다.");
                System.out.println("---> [Result] 전환 오버헤드를 최소화하고 높은 몰입도로 새 작업을 시작했습니다.");
            } else {
                System.out.println("---> [Action] 무거운 머리로 키보드를 두드리기 시작합니다.");
                System.out.println("---> [Result] 속도는 느리지만 천천히 문맥을 파악하며 적응에 성공했습니다.");
            }

            if (workStatus.equals("1")) {
                System.out.println("\n[!] 프리랜서 업무: 현재 프로젝트의 진행 단계는?");
                System.out.println("1. 신규 프로젝트 분석 및 설계 / 2. 핵심 기능 구현 / 3. 최종 마감 및 QA");
                System.out.print("상세 선택 : ");
                String freelanceStatus = scanner.nextLine();

                if (freelanceStatus.equals("1")) {
                    System.out.println("\n---> [Action] 클라이언트가 보낸 기획서를 검토하며 요구사항을 분석합니다.");
                    System.out.println("1. 기술적 실현 가능성 검토 (PoC) / 2. 시스템 구조 설계");
                    System.out.print("선택 : ");
                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 핵심 라이브러리 의존성 분석을 진행합니다.");
                        System.out.println("---> [Failure] .NET API 버전 불일치로 빌드 에러가 발생합니다.");
                        System.out.println("1. 프로젝트 버전 상향 / 2. 구버전 라이브러리 사용");
                        System.out.print("대응 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 설정을 변경하여 내부 소스 분석 가능한 상태를 확보했습니다.");
                        } else {
                            System.out.println("---> [Result] 안정성이 확인된 구버전으로 교체하여 환경을 구축했습니다.");
                        }
                    }
                } else if (freelanceStatus.equals("2")) {
                    System.out.println("\n---> [Action] 결제 모듈 및 사용자 API 연동 작업을 시작합니다.");
                    System.out.println("1. TDD 기반 구현 / 2. 빠른 프로토타이핑");
                    System.out.print("선택 : ");
                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 실패하는 테스트 케이스를 먼저 작성합니다.");
                        System.out.println("---> [Failure] 비동기 타이밍 이슈로 테스트가 간헐적으로 실패합니다.");
                        System.out.println("1. UniTask Wait 로직 추가 / 2. 타임아웃 예외 처리");
                        System.out.print("대응 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 레이스 컨디션을 해결하고 견고한 모듈을 완성했습니다.");
                        } else {
                            System.out.println("---> [Result] 무한 대기를 방지하는 방어적 로직을 구축했습니다.");
                        }
                    }
                } else if (freelanceStatus.equals("3")) {
                    System.out.println("\n---> [Action] 납품 전 최종 빌드를 뽑아 품질 검수를 시작합니다.");
                    System.out.println("1. 고피쉬 / 2. 포츈 / 3. 스도쿠 / 4. 스뿐 / 5. 서브웨이");
                    System.out.print("게임 선택 : ");
                    String gameChoice = scanner.nextLine();
                    if (gameChoice.equals("1")) {
                        System.out.println("---> [Action] 고피쉬: 클릭 방지 로직을 테스트합니다.");
                        System.out.println("---> [Failure] 하드웨어 매크로 연타 시 레이어가 뚫리는 현상을 발견했습니다.");
                        System.out.println("1. 플래그 기반 락 구현 / 2. 쿨타임(Throttle) 적용");
                        System.out.print("대응 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 상태 관리를 통해 중복 진입을 원천 차단했습니다.");
                        } else {
                            System.out.println("---> [Result] 시간 기반 필터링으로 오작동을 해결했습니다.");
                        }
                    }
                }

            } else if (workStatus.equals("2")) {
                System.out.println("\n[!] 사이드 프로젝트: 전투 시스템 폴리싱 및 버그 픽스");
                System.out.println("1. 회피 선딜레이 삭제 / 2. 길찾기 시스템 고도화 / 3. UI 게이지 애니메이션");
                System.out.print("작업 선택 : ");
                String unityTask = scanner.nextLine();

                if (unityTask.equals("1")) {
                    System.out.println("\n[?] 선딜레이 제거 방식 선택: 1. Animation Event / 2. State Machine");
                    System.out.print("선택 : ");
                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 애니메이션 클립 내부에 트리거 이벤트를 심습니다.");
                        System.out.println("---> [Result] 조작 반응성을 확보하여 손맛을 개선했습니다.");
                    } else {
                        System.out.println("---> [Action] 상태 진입 시 즉시 입력 락을 해제합니다.");
                        System.out.println("---> [Failure] 블렌딩 구간에서 모션이 겹쳐 보이는 현상이 발생합니다.");
                        System.out.println("1. 전환 시간 단축 / 2. 레이어 가중치 수동 제어");
                        System.out.print("대응 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 즉각적인 모션 변화로 반응성을 확보했습니다.");
                        } else {
                            System.out.println("---> [Result] 가중치 조절로 비주얼 버그와 성능을 모두 챙겼습니다.");
                        }
                    }
                } else if (unityTask.equals("2")) {
                    System.out.println("\n[?] 길찾기 구현 전략: 1. OverlapSphere 밀어내기 / 2. 알고리즘 직접 구현");
                    System.out.print("선택 : ");
                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 주변 캐릭터의 위치를 계산해 힘을 가합니다.");
                        System.out.println("---> [Failure] 힘이 중첩되어 캐릭터가 하늘로 사출됩니다.");
                        System.out.println("1. y축 고정 설정 / 2. Transform 보간 사용");
                        System.out.print("대응 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 제약 조건을 추가해 안정적인 간격 유지를 완성했습니다.");
                        } else {
                            System.out.println("---> [Result] 물리 연산 없이 부드러운 군집 이동을 구현했습니다.");
                        }
                    } else {
                        System.out.println("\n[?] 알고리즘 선택: 1. A* / 2. BFS / 3. 다익스트라");
                        System.out.print("선택 : ");
                        String algoChoice = scanner.nextLine();
                        if (algoChoice.equals("1")) {
                            System.out.println("---> [Result] 가장 효율적인 최단 경로 탐색 로직을 확보했습니다.");
                        } else if (algoChoice.equals("2")) {
                            System.out.println("---> [Action] Queue를 활용해 완전 탐색을 시작합니다.");
                            System.out.println("---> [Failure] 맵이 넓어 연산량이 폭증하여 프레임 드랍이 발생합니다.");
                            System.out.println("1. 탐색 범위 제한 / 2. 코루틴 분산 처리");
                            System.out.print("대응 : ");
                            if (scanner.nextLine().equals("1")) {
                                System.out.println("---> [Result] 시야 내 노드만 최적화하여 부하를 해결했습니다.");
                            } else {
                                System.out.println("---> [Result] 비동기 연산으로 끊김 없는 탐색을 구현했습니다.");
                            }
                        }
                    }
                }

            } else {
                System.out.println("---> [Failure] 뇌가 과부하 되어 터졌습니다. 휴식이 필요합니다.");
            }

        } else if (pmChoice.equals("3")) {
            isOutActivityDone = true;
            System.out.println("\n========== [오후 외부 활동 시작] ==========");
            System.out.println("[!] 모니터를 벗어나 세상 밖으로 나갑니다. 오늘의 목적지는?");
            System.out.println("1. 지인과의 만남 (카페/식사/대화)");
            System.out.println("2. 테니스 동호회 (운동/땀/승부)");
            System.out.print("활동 선택 : ");
            String outActivity = scanner.nextLine();

            if (outActivity.equals("1")) {
                System.out.println("\n[?] 오후의 나른함을 깰 어떤 성격의 만남입니까?");
                System.out.println("1. 근처에서 일하는 동료와 짧은 티타임 (업계 동향 공유)");
                System.out.println("2. 점심 겸 가벼운 브런치 미팅 (네트워킹)");
                System.out.print("만남 선택 : ");

                String meetingType = scanner.nextLine();
                if (meetingType.equals("1")) {
                    System.out.println("\n---> [Action] 카페에 앉아 최근 유니티 6의 런타임 성능 이슈에 대해 대화합니다.");
                    System.out.println("---> [Failure] 휴먼 에러: 대화가 예상보다 길어져 오후에 계획했던 외주 피드백 마감 시간이 촉박해집니다.");
                    System.out.println("1. 대화를 서둘러 마무리하고 작업실로 복귀한다.");
                    System.out.println("2. 이왕 나온 김에 확실히 영감을 얻기 위해 대화를 마저 이어간다.");
                    System.out.print("대응 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 핵심적인 정보만 교환하고 빠르게 복귀하여 다시 자리에 앉습니다.");
                        System.out.println("---> [Result] 환기된 기분으로 마감 시간 전 무사히 작업을 끝냈습니다.");
                    } else {
                        System.out.println("---> [Action] 깊이 있는 기술적 조언을 듣느라 결국 작업실에 늦게 도착합니다.");
                        System.out.println("---> [Result] 유익한 정보를 얻었으나, 오늘 저녁 작업 시간이 늘어나는 페널티를 받았습니다.");
                    }
                } else {
                    System.out.println("\n---> [Action] 가벼운 식사와 함께 근황을 나누며 업무 압박감을 잠시 내려놓습니다.");
                    System.out.println("---> [Failure] 식후 식곤증이 몰려오며 머리가 멍해지는 '브레인 포그'가 발생합니다.");
                    System.out.println("1. 진한 에스프레소를 마셔 강제로 뇌를 깨운다.");
                    System.out.println("2. 근처 공원을 짧게 산책하며 혈액순환을 돕는다.");
                    System.out.print("대응 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 카페인의 힘으로 집중력을 억지로 끌어올립니다.");
                        System.out.println("---> [Result] 즉각적인 각성 효과로 오후 작업을 재개할 수 있게 되었습니다.");
                    } else {
                        System.out.println("---> [Action] 햇볕을 쬐며 걷다 보니 꼬였던 알고리즘 문제의 아이디어가 떠오릅니다.");
                        System.out.println("---> [Result] 컨디션 회복과 동시에 기술적 돌파구를 찾았습니다.");
                    }
                }

            } else if (outActivity.equals("2")) {
                System.out.println("\n[!] 테니스 코트에 도착했습니다. 예약된 코트의 상태는?");
                System.out.println("1. 실외 코트");
                System.out.println("2. 실내 코트");
                System.out.print("코트 선택 : ");
                String courtType = scanner.nextLine();

                if (courtType.equals("1")) {
                    System.out.println("\n[?] 현재 실외 기온과 날씨 상태는 어떠합니까?");
                    System.out.println("1. 폭염 (한여름의 열기) / 2. 한파 (겨울의 냉기) / 3. 강풍 (돌풍) / 4. 적당함");
                    System.out.print("날씨 선택 : ");
                    String weatherType = scanner.nextLine();

                    if (weatherType.equals("1")) {
                        System.out.println("\n---> [Action] 강한 햇살 아래서 수분 섭취를 늘리며 신체 온도를 조절합니다.");
                        System.out.println("---> [Failure] 환경 변수: 과도한 땀으로 그립이 미끄러지고 체력이 빠르게 소진됩니다.");
                        System.out.println("1. 그립 테이프를 교체하고 휴식 시간을 늘려 컨디션을 방어한다.");
                        System.out.println("2. 젖은 그립을 닦아가며 정신력으로 현재의 높은 텐션을 유지한다.");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 구속은 낮췄으나 안정적인 스윙이 가능한 컨디션을 확보했습니다.");
                        } else {
                            System.out.println("---> [Result] 체력 부담은 크지만 공격적인 스트로크가 가능한 상태를 유지합니다.");
                        }
                    } else if (weatherType.equals("2")) {
                        System.out.println("\n---> [Action] 근육 경직을 막기 위해 평소보다 긴 시간 웜업에 집중합니다.");
                        System.out.println("---> [Failure] 환경 변수: 추위로 인해 손끝의 감각이 둔해지고 팔꿈치에 무리가 옵니다.");
                        System.out.println("1. 핫팩으로 손을 녹이며 짧고 간결한 스윙 위주로 몸을 사린다.");
                        System.out.println("2. 체온을 올리기 위해 더 활발히 움직이며 전신 스윙을 강행한다.");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 손목 부상 위험을 줄이고 정교한 면 제어가 가능한 상태가 되었습니다.");
                        } else {
                            System.out.println("---> [Result] 관절의 뻣뻣함은 남았으나 강한 구속을 낼 수 있는 열을 확보했습니다.");
                        }
                    } else if (weatherType.equals("3")) {
                        System.out.println("\n---> [Action] 풍향을 체크하며 공의 변화무쌍한 궤적에 대비합니다.");
                        System.out.println("---> [Failure] 환경 변수: 갑작스러운 돌풍에 토스와 스윙 타이밍이 계속 어긋납니다.");
                        System.out.println("1. 베이스라인 뒤로 물러나 안전한 탑스핀 위주로 궤적을 수정한다.");
                        System.out.println("2. 낮은 탄도의 슬라이스를 활용해 바람의 저항을 최소화한다.");
                        System.out.print("대응 선택 : ");
                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 실수를 최소화할 수 있는 방어적인 타구 컨디션을 갖췄습니다.");
                        } else {
                            System.out.println("---> [Result] 바람을 역이용할 수 있는 변칙적인 스트로크 감각을 깨웠습니다.");
                        }
                    } else {
                        System.out.println("\n[!] 돌발 상황 발생: 야외 코트 이용이 불가능해졌습니다.");
                        System.out.println("1. 갑작스러운 소나기로 인한 경기 중단");
                        System.out.println("2. 예약 시스템 오류로 인한 코트 중복 배정");
                        System.out.print("상황 선택 : ");
                        String unexpectedEvent = scanner.nextLine();

                        if (unexpectedEvent.equals("1")) {
                            System.out.println("\n---> [Action] 라켓 가방을 차에 싣고 동료들이 추천한 근처 맛집으로 이동합니다.");
                            System.out.println("[?] 식사 자리에서 나눌 평범한 대화 주제는?");
                            System.out.println("1. 최근의 일상과 개인적인 근황 (가벼운 수다)");
                            System.out.println("2. 주변의 맛집이나 취미, 운동 이야기 (테니스 외적인 관심사)");
                            System.out.print("주제 선택 : ");

                            if (scanner.nextLine().equals("1")) {
                                System.out.println("\n---> [Action] 요즘 사는 이야기와 소소한 고민들을 나누며 웃고 떠듭니다.");
                                System.out.println("---> [Failure] 분위기에 취해 대화가 너무 길어지면서 오늘 계획했던 저녁 일정이 통째로 밀리기 시작합니다.");
                                System.out.println("1. 즐거운 분위기를 끊지 않고 끝까지 시간을 함께한다.");
                                System.out.println("2. 시계를 확인하고 조심스럽게 먼저 일어날 준비를 한다.");
                                System.out.print("대응 선택 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Action] 오늘 할 일을 머릿속에서 지우고 사람들과의 시간에 집중합니다.");
                                    System.out.println("---> [Result] 사람들과 깊이 소통하며 정서적인 충전은 했으나, 저녁 공부 시간은 사라졌습니다.");
                                } else {
                                    System.out.println("---> [Action] 아쉬움이 남지만 다음 만남을 기약하며 자리를 정리합니다.");
                                    System.out.println("---> [Result] 대인관계의 즐거움과 개인적인 일정 사이의 선을 잘 지켰습니다.");
                                }
                            } else {
                                System.out.println("\n---> [Action] 새로 생긴 핫플레이스나 요즘 즐겨 보는 영상들로 대화의 꽃을 피웁니다.");
                                System.out.println("---> [Failure] 휴먼 에러: 너무 편하게 대화하다가 실수로 누군가의 기분을 상하게 할 뻔한 말실수를 합니다.");
                                System.out.println("1. 즉시 사과하고 가벼운 농담으로 분위기를 환기한다.");
                                System.out.println("2. 자연스럽게 화제를 돌려 상황을 모면한다.");
                                System.out.print("대응 선택 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Action] 솔직하게 사과하며 대화를 성숙하게 이어나갑니다.");
                                    System.out.println("---> [Result] 오히려 더 돈독해진 신뢰 관계를 쌓으며 즐겁게 식사를 마쳤습니다.");
                                } else {
                                    System.out.println("---> [Action] 화제를 전환하며 분위기를 살피느라 에너지를 많이 소모합니다.");
                                    System.out.println("---> [Result] 위기는 넘겼지만 정신적으로 조금 지친 상태로 귀가하게 되었습니다.");
                                }
                            }

                        } else {
                            System.out.println("\n---> [Action] 빗소리를 들으며 차분히 운전하여 작업실로 향합니다.");
                            System.out.println("[?] 복귀 중 어떤 방식으로 두뇌를 환기하시겠습니까?");
                            System.out.println("1. 요즘 유행하는 대중음악이나 라디오 청취 (가벼운 휴식)");
                            System.out.println("2. 평소 보고 싶었던 유튜브 영상의 오디오 청취 (흥미 위주)");
                            System.out.print("환기 방식 선택 : ");

                            if (scanner.nextLine().equals("1")) {
                                System.out.println("\n---> [Action] 좋아하는 노래들을 따라 부르며 운전 자체를 즐깁니다.");
                                System.out.println("---> [Result] 운동을 못 한 아쉬움을 털어내고 상쾌한 기분으로 작업실에 도착했습니다.");
                            } else {
                                System.out.println("\n---> [Action] 평소 구독하던 여행이나 맛집 관련 채널의 소리를 듣습니다.");
                                System.out.println("---> [Failure] 휴먼 에러: 영상 내용에 너무 집중하다가 길을 잘못 들어 도착 시간이 늦어집니다.");
                                System.out.println("1. 당황하지 않고 경로를 재탐색하여 안전하게 운전한다.");
                                System.out.println("2. 늦어진 김에 근처 편의점에 들러 야식거리를 사서 들어간다.");
                                System.out.print("대응 선택 : ");

                                if (scanner.nextLine().equals("1")) {
                                    System.out.println("---> [Action] 마음을 가다듬고 정규 경로로 복귀하여 무사히 도착합니다.");
                                    System.out.println("---> [Result] 약간 늦었지만 스스로의 평정심을 유지하는 법을 다시 배웠습니다.");
                                } else {
                                    System.out.println("---> [Action] 맛있는 간식을 사서 작업실로 들어가 기분 전환을 합니다.");
                                    System.out.println("---> [Result] 도착은 늦었지만 든든한 야식과 함께 기분 좋게 저녁 업무를 준비합니다.");
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("\n---> [Action] 실내의 일정한 온도와 조명 아래서 랠리에만 집중합니다.");
                    System.out.println("---> [Result] 외부 변수 없이 오직 실력에만 몰입할 수 있는 컨디션을 확보했습니다.");
                }


                System.out.println("\n[!] 오늘 매칭된 상대의 실력은?");
                System.out.println("1. 나보다 강한 상대 (도전자 입장)");
                System.out.println("2. 비등한 상대 (치열한 혈투)");
                System.out.println("3. 나보다 약한 상대 (자세 교정 및 리드)");
                System.out.print("상대 선택 : ");
                String opponentLevel = scanner.nextLine();

                if (opponentLevel.equals("1")) {
                    System.out.println("\n---> [Action] 상대의 강한 구속에 밀리지 않기 위해 라켓을 짧고 빠르게 휘두릅니다.");
                    System.out.println("---> [Failure] 압박감: 상대의 공 무게에 눌려 스트로크가 계속 네트에 걸리거나 짧아집니다.");
                    System.out.println("1. 수비적으로 전환하여 상대의 실수를 유도하는 끈질긴 플레이");
                    System.out.println("2. 더 과감한 공격으로 먼저 승부수를 던지는 하이리스크 전략");
                    System.out.print("대응 선택 : ");
                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 깊숙한 로브와 슬라이스로 상대의 템포를 뺏습니다.");
                        System.out.println("---> [Result] 역전에 성공하며 한 단계 성장한 실력을 증명했습니다.");
                    } else {
                        System.out.println("---> [Action] 베이스라인 끝에서 전력으로 맞받아칩니다.");
                        System.out.println("---> [Result] 승패를 떠나 강력한 손맛을 느끼며 스트레스를 해소했습니다.");
                    }
                } else if (opponentLevel.equals("2")) {
                    System.out.println("\n---> [Action] 파이널 세트까지 가는 긴 랠리 속에 체력전을 펼칩니다.");
                    System.out.println("---> [Failure] 체력 저하: 반복되는 좌우 움직임에 허벅지 근육이 딱딱하게 굳어옵니다.");
                    System.out.println("1. 무리하지 않고 휴식 시간을 늘리며 컨디션 조절");
                    System.out.println("2. 마지막 남은 에너지를 짜내어 전력 질주");
                    System.out.print("대응 선택 : ");
                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Action] 호흡을 가다듬으며 안전하게 경기를 마무리합니다.");
                        System.out.println("---> [Result] 부상 없이 기분 좋게 운동을 마친 정도로 만족합니다.");
                    } else {
                        System.out.println("---> [Action] 쥐가 나기 직전까지 코트를 뛰며 모든 공을 받아냅니다.");
                        System.out.println("---> [Result] 승리했지만 너무 피곤하고 가서 곯아 떨어질 것 같습니다.");
                    }
                } else {
                    System.out.println("\n---> [Action] 여유 있는 템포로 평소 연습하던 새로운 타법을 실전에 적용해봅니다.");
                    System.out.println("---> [Result] 폼을 점검하며 자신감을 충전하는 유익한 시간을 보냈습니다.");
                }

            } else {
                // outActivity 선택지(1. 지인, 2. 테니스) 외의 예외 상황 혹은 취소 시
                System.out.println("\n[!] 돌발 상황: 계획했던 외부 활동이 갑작스러운 사정으로 무산되었습니다.");
                System.out.println("1. 붕 뜬 시간, 근처 카페나 서점에서 혼자만의 시간을 갖는다.");
                System.out.println("2. 김빠진 김에 일찍 귀가해서 집밥을 먹으며 쉰다.");
                System.out.print("대안 선택 : ");

                String alternative = scanner.nextLine();
                isAloneStatus = true;

                if (alternative.equals("1")) {
                    System.out.println("\n---> [Action] 근처에서 분위기가 가장 차분한 곳을 찾아 발걸음을 옮깁니다.");
                    System.out.println("[?] 어디에서 시간을 보내시겠습니까?");
                    System.out.println("1. 조용한 카페 (기술 아티클 독서 및 로직 구상)");
                    System.out.println("2. 대형 서점 (신간 구경 및 자기계발 서적 탐독)");
                    System.out.print("장소 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Action] 창가 자리에 앉아 맥북을 폅니다. 코딩은 안 해도 기술 문서를 훑어봅니다.");
                        System.out.println("1. 유니티 6의 새로운 렌더링 파이프라인 최적화 기사를 정독한다.");
                        System.out.println("2. 평소 관심 있던 디자인 패턴 관련 블로그를 살펴본다.");
                        System.out.print("학습 주제 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 복잡했던 드로우 콜 최적화에 대한 실마리를 찾았습니다. 두뇌가 맑아집니다.");
                        } else {
                            System.out.println("---> [Result] 모듈화 설계에 대한 새로운 영감을 얻었습니다. 빨리 코드로 옮기고 싶어집니다.");
                        }
                    } else {
                        System.out.println("\n---> [Action] 서점 특유의 종이 냄새를 맡으며 베스트셀러 코너를 서성입니다.");
                        System.out.println("1. 기술 서적 코너에서 최신 C# 서적을 뒤적거린다.");
                        System.out.println("2. 인문/에세이 코너에서 마음을 다스리는 책을 읽는다.");
                        System.out.print("도서 카테고리 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("---> [Result] 이미 아는 내용도 있지만, 기초를 다시 다지는 유익한 시간이 되었습니다.");
                        } else {
                            System.out.println("---> [Result] 개발 외적인 시각을 넓히며 정신적인 여유를 되찾았습니다.");
                        }
                    }

                    System.out.println("\n---> [Action] 가방을 고쳐 메고 귀가를 위해 정류장과 역사를 살핍니다.");
                    System.out.println("[?] 어떤 교통수단을 이용하여 귀가하시겠습니까?");
                    System.out.println("1. 지하철 (정확한 시간, 하지만 인파 속의 답답함)");
                    System.out.println("2. 버스 (창밖 풍경, 하지만 교통 체증의 변수)");
                    System.out.print("교통수단 선택 : ");

                    String transport = scanner.nextLine();

                    if (transport.equals("1")) {
                        System.out.println("\n---> [Action] 지하철 개찰구를 지나 승강장으로 내려갑니다.");
                        System.out.println("[?] 열차 안에서의 상황은 어떠합니까?");
                        System.out.println("1. 운 좋게 앉아서 가기 (완벽한 휴식 가능)");
                        System.out.println("2. 서서 가기 (사람들 사이에 끼어 이동)");
                        System.out.print("열차 상황 선택 : ");

                        if (scanner.nextLine().equals("1")) {
                            System.out.println("\n---> [Action] 자리에 앉아 눈을 감고 뇌를 비웁니다.");
                            System.out.println("---> [Result] 집까지 에너지를 보존하며 편안하게 이동합니다.");
                        } else {
                            System.out.println("\n---> [Action] 스마트폰으로 짧은 영상을 보거나 사람 구경을 하며 버팁니다.");
                            System.out.println("---> [Result] 다리는 좀 아프지만, 북적이는 활력 속에 정신은 깨어있습니다.");
                        }
                    } else {
                        System.out.println("\n---> [Action] 버스 정류장에서 노선을 확인하고 승차합니다.");
                        System.out.println("[?] 창가 자리에 앉은 당신, 무엇을 하시겠습니까?");
                        System.out.println("1. 창밖 풍경을 보며 멍 때리기 (흘러가는 불빛 구경)");
                        System.out.println("2. 이어폰을 끼고 유튜브 시청하기 (도파민 충전)");
                        System.out.println("3. 플레이리스트를 정독하며 좋아하는 음악 듣기");
                        System.out.print("버스 모드 선택 : ");

                        String busMode = scanner.nextLine();
                        if (busMode.equals("1")) {
                            System.out.println("\n---> [Action] 유리창에 머리를 기대고 창밖 도시의 밤 풍경을 감상합니다.");
                            System.out.println("---> [Result] 복잡했던 알고리즘 생각들이 정리되며 기분 좋은 피로감이 찾아옵니다.");
                        } else if (busMode.equals("2")) {
                            System.out.println("\n[?] 유튜브에서 알고리즘이 추천해준 영상 중 무엇을 클릭하시겠습니까?");
                            System.out.println("1. 롤(LoL) 하이라이트 혹은 강의 영상 (뇌지컬 자극)");
                            System.out.println("2. 대리 만족을 위한 화끈한 먹방 (대리 포만감)");
                            System.out.println("3. 귀여운 고양이나 강아지 동물 영상 (힐링)");
                            System.out.print("유튜브 영상 선택 : ");

                            String youtubeChoice = scanner.nextLine();
                            if (youtubeChoice.equals("1")) {
                                System.out.println("\n---> [Action] 좋아하는 스트리머의 플레이와 입담에 몰입합니다.");
                                System.out.println("---> [Result] 플레이를 보니 묘하게 승부욕이 자극되며 두뇌가 예열됩니다.");
                            } else if (youtubeChoice.equals("2")) {
                                System.out.println("\n---> [Action] 산더미처럼 쌓인 음식들을 해치우는 모습을 멍하니 봅니다.");
                                System.out.println("---> [Result] 갑자기 배가 너무 고파지기 시작합니다.");
                            } else {
                                System.out.println("\n---> [Action] 솜방망이 같은 발바닥과 꼬리 짓에 아빠 미소를 짓습니다.");
                                System.out.println("---> [Result] 오늘 하루의 긴장이 사르르 녹아내리며 무해한 에너지를 충전했습니다.");
                            }
                        } else {
                            System.out.println("\n---> [Action] 노이즈 캔슬링을 켜고 나만의 음악 세계에 빠져듭니다.");
                            System.out.println("---> [Result] 일찍 귀가하는 아쉬움을 열정적인 비트로 달래며 텐션을 올립니다.");
                        }
                    }

                    System.out.println("\n[!] 어느덧 집 근처에 도착했습니다.");
                }
            }
        }



        System.out.println("\n========== [저녁 식사 고민] ==========");

        if (isOutActivityDone && !isAloneStatus) {
            System.out.println("[!] 일정이 끝나고 지인들이 밥 먹고 가자며 붙잡네요.");
            System.out.println("1. 지인들과 함께 식당으로 향한다");
            System.out.println("2. 정중히 거절하고 집으로 돌아가 혼자 먹는다");
            System.out.print("행보 선택 : ");

            if (scanner.nextLine().equals("1")) {
                System.out.println("\n[?] 어떤 종류의 식당으로 지인들을 안내하시겠습니까?");
                System.out.println("1. 고기/전골류 (직접 굽고 끓이며 길어지는 대화)");
                System.out.println("2. 단품 식사류 (국밥, 덮밥 등 빠르게 먹고 일어날 수 있는 곳)");
                System.out.print("식당 타입 선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("\n---> [Action] 불판 앞에서 대화가 무르익습니다. 맛있는 냄새가 옷에 뱁니다.");
                    System.out.println("1. 분위기에 맞춰 가볍게 반주를 곁들인다");
                    System.out.println("2. 끝까지 물만 마시며 정신력을 유지한다");
                    System.out.print("음주 여부 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Failure] 알코올이 들어가니 의지력이 무너집니다. 오늘은 공부가 힘들 것 같습니다.");
                        mentalStatus = "Exhausted";
                    } else {
                        System.out.println("\n---> [Success] 맨정신을 유지하며 대화의 흐름만 탑니다. 다만 샤워 시간은 피할 수 없습니다.");
                        mentalStatus = "Refreshed_But_Late";
                    }
                } else {
                    System.out.println("\n---> [Action] 식사에만 집중하며 오늘 있었던 테니스/미팅 이야기를 나눕니다.");
                    System.out.println("1. 식후 결제 내기를 제안한다 (가위바위보)");
                    System.out.println("2. 깔끔하게 더치페이하고 바로 일어난다");
                    System.out.print("식후 정리 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Result] 내기에서 이겨 공짜 밥을 먹었습니다. 기분이 최고조에 달합니다.");
                        mentalStatus = "Normal";
                    } else {
                        System.out.println("\n---> [Result] 계산 과정을 빠르게 끝내고 예정된 시간에 맞춰 복귀합니다.");
                        mentalStatus = "Normal";
                    }
                }
            } else {
                System.out.println("\n---> [Action] \"내일 마감이 있어서요!\"라며 쿨하게 작별 인사를 고합니다.");
                System.out.println("1. 편의점에서 좋아하는 도시락을 골라 들어간다");
                System.out.println("2. 다이어트 겸 단백질 쉐이크로 간단히 때운다");
                System.out.print("혼밥 메뉴 선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("\n---> [Result] 편안한 환경에서 좋아하는 유튜브를 보며 식사합니다. 정서적 충전 완료.");
                    mentalStatus = "Normal";
                } else {
                    System.out.println("\n---> [Result] 배는 좀 고프지만 몸이 가볍습니다. 즉시 몰입할 준비가 되었습니다.");
                    mentalStatus = "Normal";
                }
            }

        } else {
            System.out.println("\n[!] 어느덧 해가 지고 저녁 시간이 되었습니다. 식사는 어떻게 할까요?");
            System.out.println("1. 근처 친구를 소환해 밖에서 먹는다");
            System.out.println("2. 바로 집으로 들어간다");
            System.out.print("행보 선택 : ");

            if (scanner.nextLine().equals("1")) {
                System.out.println("\n[?] 친구와 만나 무엇을 하시겠습니까?");
                System.out.println("1. 작업실 근처 단골집에서 술 없이 밥만 먹기");
                System.out.println("2. 기분 전환을 위해 조금 떨어진 핫플레이스로 이동");
                System.out.print("외식 방식 선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("\n---> [Action] 친구와 서로의 근황을 털어놓으며 스트레스를 풉니다.");
                    System.out.println("1. 친구가 고민 상담을 시작해 대화가 길어진다");
                    System.out.println("2. 적당히 끊고 각자 할 일을 하러 헤어진다");
                    System.out.print("대화 전개 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Failure] 친구를 위로해주다 보니 진이 다 빠졌습니다. 감정 소모가 큽니다.");
                        mentalStatus = "Exhausted";
                    } else {
                        System.out.println("\n---> [Result] 짧고 굵은 대화로 리프레시를 마치고 작업실로 복귀합니다.");
                        mentalStatus = "Normal";
                    }
                } else {
                    System.out.println("\n---> [Action] 익숙하지 않은 동네를 구경하며 시각적인 자극을 얻습니다.");
                    System.out.println("1. 웨이팅이 긴 유명 맛집에 도전한다");
                    System.out.println("2. 줄이 없는 한적한 식당에 들어간다");
                    System.out.print("식당 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Failure] 30분 넘게 서 있었더니 다리가 아프고 배고픔에 예민해졌습니다.");
                        mentalStatus = "Exhausted";
                    } else {
                        System.out.println("\n---> [Result] 숨겨진 맛집을 발견했습니다. 기분 좋게 식사하고 돌아갑니다.");
                        mentalStatus = "Normal";
                    }
                }
            } else {
                System.out.println("\n---> [Action] 현관문을 여니 맛있는 냄새가 반겨줍니다. '오, 오늘 밥 해주셨네!'");
                System.out.println("1. 차려진 밥을 감사히 먹고 바로 공부한다");
                System.out.println("2. 밥 먹고 양심상 설거지와 뒷정리를 자처한다");
                System.out.print("가사 기여 선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("\n---> [Action] 신속하게 식사를 마치고 책상 앞에 앉습니다.");
                    System.out.println("1. 배가 너무 불러 식곤증이 몰려온다");
                    System.out.println("2. 적당히 조절해서 먹어 컨디션이 좋다");
                    System.out.print("포만감 조절 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Failure] 눕고 싶은 유혹이 강렬합니다. 의지가 박약해집니다.");
                        mentalStatus = "Exhausted";
                    } else {
                        System.out.println("\n---> [Result] 최상의 컨디션입니다");
                        mentalStatus = "Normal";
                    }
                } else {
                    System.out.println("\n---> [Action] 앞치마를 두르고 산더미 같은 설거지를 해치웁니다.");
                    System.out.println("1. 설거지하며 머릿속으로 로직을 정리한다");
                    System.out.println("2. 아무 생각 없이 노동 자체에 집중한다");
                    System.out.print("작업 태도 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Result] 손은 움직이지만 뇌는 이미 코딩 중입니다. 효율적인 예열 완료.");
                        mentalStatus = "Normal";
                    } else {
                        System.out.println("\n---> [Result] 뇌가 휴식을 취하며 초기화되었습니다. 깨끗한 정신입니다.");
                        mentalStatus = "Normal";
                    }
                }
            }
        }


        System.out.println("\n========== [저녁 일과 시작] ==========");
        System.out.println("[?] 오늘 남은 에너지를 어떻게 연소시키겠습니까?");
        System.out.println("1. 사무실(작업실) 복귀 : 생산성 모드 (프로젝트 작업 및 기술 학습)");
        System.out.println("2. 집에서 보내기 : 셧다운 모드 (완전한 휴식 및 개인 정비)");
        System.out.print("장소 선택 : ");

        String locationChoice = scanner.nextLine();

        if (locationChoice.equals("1")) {
            System.out.println("\n[!] 늦은 시간, 다시 작업실로 향합니다.");
            System.out.println("[?] 작업실까지 어떤 템포로 이동하시겠습니까?");
            System.out.println("1. 속전속결 (택시나 전력 질주로 최대한 빠르게 복귀)");
            System.out.println("2. 완급조절 (밤바람을 맞으며 느긋하게 사색하며 이동)");
            System.out.print("이동 스타일 선택 : ");

            if (scanner.nextLine().equals("1")) {
                System.out.println("\n---> [Action] 한시라도 빨리 코드를 치기 위해 가장 빠른 수단을 택합니다.");
                System.out.println("---> [Result] 흐름이 끊기기 전에 책상 앞에 앉았습니다. 하지만 작업실까지 오느라 지쳤습니다.");
                mentalStatus = "Exhausted";
            } else {
                System.out.println("\n---> [Action] 서두르지 않고 밤거리를 거닐며 머릿속 로직을 가다듬습니다.");
                System.out.println("---> [Result] 복잡했던 인터페이스 구조가 단순하게 정리되었습니다. 맑은 정신으로 입성합니다.");
                mentalStatus = "Normal";
            }

            System.out.println("\n[!] 고요한 작업실, 맥북의 팬 소리만 들리는 완벽한 몰입 환경입니다.");

            if (mentalStatus.equals("Exhausted")) {
                System.out.println("---> [Warning] 몸은 지쳤지만 사무실의 공기가 정신을 긴장시킵니다.");
                System.out.println("1. 정신력으로 버티며 사이드 프로젝트 마일스톤 기능을 구현한다.");
                System.out.println("2. 코드 대신 기술 아티클을 읽으며 이론 공부를 한다.");
                System.out.print("선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("---> [Failure] 집중력 한계로 뇌 정지가 옵니다. 코드 한 줄 치기가 힘겹습니다.");
                    System.out.println("---> [Result] 결국 효율이 나지 않아 자괴감만 안고 퇴근을 결정합니다.");
                } else {
                    System.out.println("---> [Success] 무리한 구현 대신 DDD나 렌더링 이론을 정리하며 내실을 다집니다.");
                    System.out.println("---> [Result] 지적 자산을 쌓았다는 만족감과 함께 하루를 정리합니다.");
                }
            } else {
                System.out.println("[?] 어떤 성과를 만들어내시겠습니까?");
                System.out.println("1. 사이드 프로젝트 핵심 기능 구현 (Unity 6 / C#)");
                System.out.println("2. 고난도 기술 학습 (저수준 렌더링 파이프라인 / 수학적 원리)");
                System.out.print("작업 선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("\n---> [Action] 듀얼 모니터를 꽉 채운 코드와 에디터에 몰입합니다.");
                    System.out.println("---> [Failure] 변수: 특정 조건에서 루프가 꼬이는 '엣지 케이스' 발견!");
                    System.out.println("1. 밤을 새우더라도 직접 끝까지 수정한다.");
                    System.out.println("2. AI에게 가이드를 받고 논리 구조를 함께 디버깅한다.");
                    System.out.print("수정 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("---> [Result] 새벽 2시, 모든 테스트를 통과하고 깃허브에 커밋을 날립니다.");
                    } else {
                        System.out.println("---> [Result] AI의 조언으로 구조적 결함을 빠르게 해결하고 가벼운 마음으로 퇴근합니다.");
                    }
                } else {
                    System.out.println("\n---> [Action] 종이와 펜을 꺼내 GPU 파이프라인의 수학적 모델을 그립니다.");
                    System.out.println("---> [Result] 화면에 점 하나가 찍히는 원리를 완벽히 이해하며 '득도'한 기분을 느킵니다.");
                }
            }

            System.out.println("\n[!] 텅 빈 건물 로비를 나섭니다. 집으로 어떻게 돌아가시겠습니까?");
            System.out.println("1. 속전속결 귀가 (졸린 몸을 이끌고 최단 거리로 빠르게 이동)");
            System.out.println("2. 완급조절 귀가 (좋아하는 음악을 들으며 새벽 공기를 즐기기)");
            System.out.print("귀가 스타일 선택 : ");

            String returnStyle = scanner.nextLine();

            if (returnStyle.equals("1")) {
                System.out.println("\n---> [Action] 오직 이불속이라는 목적지만 생각하며 집으로 질주합니다.");
                System.out.println("---> [Result] 꾸벅꾸벅 졸음과 싸우며 무사히 집 근처에 도착했습니다.");

                System.out.println("\n[!] 현관문을 열자마자 가방을 던져두고 욕실로 향합니다.");
                System.out.println("---> [Action] 뜨거운 물로 오늘 하루의 긴장과 피로를 빠르게 씻어냅니다.");
                System.out.println("---> [Result] 젖은 머리를 대충 말리고 침대에 몸을 던집니다. 눕자마자 의식이 흐려집니다.");
            } else {
                System.out.println("\n---> [Action] 노이즈 캔슬링을 켜고 나만의 플레이리스트를 재생합니다.");
                System.out.println("---> [Result] 가로등 불빛 아래 고요한 새벽 거리를 걸으며 오늘 작업한 로직을 흐뭇하게 되새깁니다.");

                System.out.println("\n[!] 차분해진 마음으로 집에 들어와 욕실 불을 켭니다.");
                System.out.println("---> [Action] 좋아하는 향의 바디워시를 쓰며 느긋하게 샤워를 즐깁니다.");
                System.out.println("---> [Result] 개운해진 몸으로 깨끗한 침구 속에 들어갑니다. 완벽한 하루의 마무리입니다.");
            }

        } else {
            System.out.println("\n[!] 현관문을 열고 들어오는 순간, 모든 긴장이 풀립니다.");
            System.out.println("[?] 지친 몸을 먼저 정돈하시겠습니까?");
            System.out.println("1. 귀가 즉시 샤워 (개운하게 휴식 준비)");
            System.out.println("2. 일단 쇼파에 눕기 (귀찮음의 승리)");
            System.out.print("선택 : ");

            if (scanner.nextLine().equals("1")) {
                System.out.println("\n---> [Action] 따뜻한 물로 오늘 하루의 외부 먼지와 긴장을 씻어냅니다.");
                System.out.println("---> [Result] 뽀송뽀송해진 상태로 진정한 자유 시간을 맞이합니다.");
            } else {
                System.out.println("\n---> [Action] 외출복도 갈아입지 못한 채 거실 쇼파에 쓰러지듯 눕습니다.");
                System.out.println("---> [Result] 10분만 쉰다는 게 30분이 훌쩍 지나버렸습니다. 무거운 몸을 이끌고 겨우 씻고 나옵니다.");
            }

            if (mentalStatus.equals("Refreshed_But_Late")) {
                System.out.println("\n---> [Status] 기분은 좋지만 이미 시간은 자정입니다.");
                System.out.println("1. 딱 한 판만! 롤(LoL) 랭크 게임 접속");
                System.out.println("2. 침대에 누워 유튜브 알고리즘에 몸을 맡기기");
                System.out.print("선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("\n[?] 현재 랭크 게임의 대기 열이 잡혔습니다. 당신의 포지션은?");
                    System.out.println("1. 주 라인 (자신 있는 캐리 시도)");
                    System.out.println("2. 서포터/포지션 자동 선택 (팀원에게 운명을 맡김)");
                    System.out.print("포지션 확인 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Action] 라인전에서 압도하며 초반 분위기를 가져옵니다.");
                        System.out.println("---> [Failure] 변수: 갑작스러운 팀원의 탈주나 던지기로 역전패를 당합니다.");
                        System.out.println("---> [Result] 새벽 4시, 끓어오르는 분노를 삭이며 억지로 컴퓨터를 끕니다.");
                    } else {
                        System.out.println("\n---> [Action] 팀원들의 뒤를 받치며 안정적인 운영을 시도합니다.");
                        System.out.println("---> [Result] 비록 지지는 않았지만, 내가 캐리하지 못했다는 묘한 아쉬움 속에 게임을 마칩니다.");
                    }
                } else {
                    System.out.println("\n[?] 알고리즘이 추천해준 밤샘용 영상 리스트입니다.");
                    System.out.println("1. 시간 순삭! 롤드컵 역대급 하이라이트");
                    System.out.println("2. 몽글몽글한 강아지/고양이 힐링 쇼츠");
                    System.out.print("영상 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Action] 페이커의 환상적인 무빙을 보며 시간을 보냅니다.");
                        System.out.println("---> [Result] 도파민 과다로 눈이 초롱초롱해졌지만, 몸은 녹초가 되어 잠듭니다.");
                    } else {
                        System.out.println("\n---> [Action] 무해한 생명체들의 움직임을 보며 정서적 안정을 얻습니다.");
                        System.out.println("---> [Result] 오늘 하루 쌓였던 날 선 감정들이 모두 치유되는 기분입니다.");
                    }
                }
            } else {
                System.out.println("\n[?] 나를 위한 보상의 시간, 무엇을 할까요?");
                System.out.println("1. 스팀(Steam) 라이브러리에 묵혀둔 인디 게임 실행");
                System.out.println("2. 넷플릭스/디즈니 플러스 최신작 정주행");
                System.out.print("선택 : ");

                if (scanner.nextLine().equals("1")) {
                    System.out.println("\n[?] 어떤 장르의 인디 게임을 즐기시겠습니까?");
                    System.out.println("1. 로그라이크/소울라이크 (도전과 자극)");
                    System.out.println("2. 힐링/경영 시뮬레이션 (평화와 안식)");
                    System.out.print("장르 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Action] 죽고 다시 시작하기를 반복하며 보스의 패턴을 익힙니다.");
                        System.out.println("---> [Result] 성취감은 얻었으나 손가락과 손목에 피로가 누적됩니다.");
                    } else {
                        System.out.println("\n---> [Action] 나만의 농장을 가꾸거나 손님들을 맞이하며 소소한 재미를 느낍니다.");
                        System.out.println("---> [Result] 현실의 복잡한 로직을 잊고 평온한 가상 세계에서 힐링했습니다.");
                    }
                } else {
                    System.out.println("\n[?] 영화를 보며 곁들일 야식은?");
                    System.out.println("1. 편의점 꿀조합 (라면+삼각김밥)");
                    System.out.println("2. 배달 앱 인기 메뉴 (치킨/피자)");
                    System.out.print("메뉴 선택 : ");

                    if (scanner.nextLine().equals("1")) {
                        System.out.println("\n---> [Action] 자극적인 맛에 혀가 즐겁습니다. 가성비 최고의 휴식입니다.");
                        System.out.println("---> [Result] 영화가 끝나갈 때쯤 밀려오는 나트륨의 포만감 속에 깊은 잠에 빠집니다.");
                    } else {
                        System.out.println("\n---> [Action] 거금을 들여 주문한 치킨을 뜯으며 화려한 액션 영화를 감상합니다.");
                        System.out.println("---> [Result] 자본의 맛과 시각적 즐거움이 합쳐져 완벽한 보상을 누렸습니다.");
                    }
                }
            }

        }

        System.out.println("\n[!] 조명을 끄고 이불 속으로 파고듭니다. 굿나잇이요.");

        scanner.close();
    }
}







//import sun.misc.Unsafe;
//import java.lang.reflect.Field;
//public class Main {
//
//    static Unsafe unsafe;
//
//    static {
//        try {
//            Field f = Unsafe.class.getDeclaredField("theUnsafe");
//            f.setAccessible(true);
//            unsafe = (Unsafe) f.get(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        // 1. 비교 대상 (글자수 동일하게 6글자)
//        String eng = "Gemini";       // 6글자 -> Latin-1
//        String kor = "개발자입니다";   // 6글자 -> UTF-16
//
//        System.out.println("==================================================================");
//        System.out.println("            [Java 9+ Compact Strings Memory Dump View]            ");
//        System.out.println("==================================================================\n");
//
//        analyzeString("English (Latin-1)", eng);
//        System.out.println("\n------------------------------------------------------------------\n");
//        analyzeString("Korean  (UTF-16) ", kor);
//    }
//
//    private static void analyzeString(String label, String target) throws Exception {
//        // Coder 및 배열 가져오기
//        Field coderField = String.class.getDeclaredField("coder");
//        long coderOffset = unsafe.objectFieldOffset(coderField);
//        byte coderValue = unsafe.getByte(target, coderOffset);
//
//        Field valueField = String.class.getDeclaredField("value");
//        valueField.setAccessible(true);
//        byte[] internalArray = (byte[]) valueField.get(target);
//
//        // 출력 헤더
//        System.out.printf("👉 [%s] 값: \"%s\"\n", label, target);
//        System.out.printf("   - Coder: %d (%s)\n", coderValue, (coderValue == 0 ? "1 Byte/char" : "2 Bytes/char"));
//        System.out.printf("   - Size : %d bytes\n", internalArray.length);
//        System.out.println("   - Dump :");
//
//        // ★ 핵심: 메모리 헥사 덤프 출력 (4바이트 단위 끊기) ★
//        printHexDump(internalArray);
//    }
//
//    private static void printHexDump(byte[] array) {
//        System.out.println("   +-------------------------------------------------+");
//        System.out.print("   | ");
//
//        for (int i = 0; i < array.length; i++) {
//            // 1. 바이트 값을 Hex로 출력
//            System.out.printf("%02X ", array[i]);
//
//            // 2. 4바이트마다 파이프(|) 구분자 추가 (마지막 제외)
//            if ((i + 1) % 4 == 0 && (i + 1) < array.length) {
//                System.out.print("| ");
//            }
//
//            // 3. 16바이트마다 줄바꿈 (메모리 뷰어 형식)
//            if ((i + 1) % 16 == 0 && (i + 1) < array.length) {
//                System.out.println("|");
//                System.out.print("   | ");
//            }
//        }
//
//        // 남은 여백 채우기 (정렬을 위해)
//        int remainder = array.length % 16;
//        if (remainder != 0) {
//            // (16 - 나머지) 만큼 공백 채움, 파이프 위치 고려
//            // 구현 복잡도를 줄이기 위해 단순히 줄바꿈만 처리
//        }
//
//        System.out.println(); // 끝
//        System.out.println("   +-------------------------------------------------+");
//    }
//}

//public class Main {
//    public static void main(String[] args) {
//        char c1 = 'H';
//        char c2 = 'i';
//
//        System.out.println("=== [char 데이터 검증] ===");
//
//        // 포맷: '문자' -> 10진수: 값 | 16진수: 0x값
//        System.out.printf("Char '%c' -> 10진수: %3d  |  16진수: 0x%X\n", c1, (int)c1, (int)c1);
//        System.out.printf("Char '%c' -> 10진수: %3d  |  16진수: 0x%X\n", c2, (int)c2, (int)c2);
//    }
//}

//public class Main {
//
//    static Unsafe unsafe;
//
//    static {
//        try {
//            Field f = Unsafe.class.getDeclaredField("theUnsafe");
//            f.setAccessible(true);
//            unsafe = (Unsafe) f.get(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        // -------------------------------------------------------
//        // 1. 변수 준비
//        // -------------------------------------------------------
//        int a = 10;
//        int b = 20;
//        String s1 = new String("Hi");
//        String s2 = new String("Hi");
//
//        // -------------------------------------------------------
//        // 2. [Stack View]
//        // -------------------------------------------------------
//        System.out.println("========== [1. Stack Memory View] ==========");
//        System.out.println("| 변수명 |  타입  |      Stack 저장값 (Hex)       |  비고");
//        System.out.println("|-------|--------|------------------------------|------");
//
//        System.out.printf("|   a   |   int  | [ 0x%08X ]             | 값 (10)\n", a);
//        System.out.printf("|   b   |   int  | [ 0x%08X ]             | 값 (20)\n", b);
//        System.out.println("|-------|--------|------------------------------|------");
//
//        long s1Addr = getAddress(s1);
//        long s2Addr = getAddress(s2);
//
//        System.out.printf("|   s1  | String | [ 0x%X ]           | 참조 (Heap 주소)\n", s1Addr);
//        System.out.printf("|   s2  | String | [ 0x%X ]           | 참조 (Heap 주소)\n", s2Addr);
//        System.out.println("========================================================\n");
//
//
//        // -------------------------------------------------------
//        // 3. [Heap View]
//        // -------------------------------------------------------
//        System.out.println("========== [2. Heap Memory Trace] ==========");
//
//        // (1) 덤프 출력
//        System.out.printf("👉 1단계: String 객체 주소 [ 0x%X ] 메모리 조회\n", s1Addr);
//        System.out.println("   [String Object Dump]");
//        printMemory(s1, 16);
//        System.out.println("   => 분석: 알 수 없는 숫자들로 가득합니다. 해석이 필요합니다.\n");
//
//
//        // (2) [NEW 위치] 구조 분석 (여기서 먼저 배치를 확인!)
//        System.out.println("   [JVM 필드 확인]");
//        System.out.println("   이 객체가 어떻게 구성되어 있는지 메모리 확인.");
//
//        System.out.println("   -----------------------------------------------------");
//        System.out.println("   [ 0  ~ 11 byte ] : Object Header");
//        printOffset("hash");
//        printOffset("coder");
//        System.out.println("   [ ..padding..  ] : Alignment (빈 공간)");
//
//        // value 필드 위치 저장
//        Field valueField = String.class.getDeclaredField("value");
//        long valueOffset = unsafe.objectFieldOffset(valueField);
//        System.out.printf("   [ %2d번째 byte  ] : value 필드 \n", valueOffset);
//        System.out.println("   -----------------------------------------------------\n");
//
//
//        // (3) 연결 고리 확인 (위 분석 결과를 토대로 확인)
//        System.out.println("   [참조 연결 확인: 위 분석에 따라 20번째 칸을 확인합니다]");
//
//        // 그 위치의 값을 읽음
//        int compressedPtr = unsafe.getInt(s1, valueOffset);
//
//        System.out.printf("   A. 타겟 위치 : 객체 시작 + %d byte 지점 (Offset)\n", valueOffset);
//        System.out.printf("   B. 발견된 값 : [ 0x%X ] \n", compressedPtr);
//        System.out.println("   C. 의미      : 실제 byte[] 배열을 가리키는 압축된 주소값\n");
//
//
//        // (4) 내부 배열로 이동
//        valueField.setAccessible(true);
//        byte[] internalArray = (byte[]) valueField.get(s1);
//        long arrayAddr = getAddress(internalArray);
//
//        System.out.printf("👉 2단계: 실제 byte[] 배열 주소 [ 0x%X ] 조회\n", arrayAddr);
//        System.out.println("   [byte[] Array Dump]");
//        printMemory(internalArray, 24);
//
//        // 데이터 검증
//        System.out.println("   => 결과: 0x48('H'), 0x69('i') 데이터 확인 완료");
//        System.out.println("========================================================");
//    }
//
//    // -------------------------------------------------------
//    // [Helper Methods]
//    // -------------------------------------------------------
//
//    private static void printMemory(Object obj, int bytesToRead) {
//        System.out.print("   Values: ");
//        for (int i = 0; i < bytesToRead; i++) {
//            byte b = unsafe.getByte(obj, (long) i);
//            System.out.printf("%02X ", b);
//            if ((i + 1) % 4 == 0) System.out.print("| ");
//        }
//        System.out.println();
//    }
//
//    private static long getAddress(Object o) {
//        Object[] array = new Object[] { o };
//        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
//        return (unsafe.addressSize() == 4)
//                ? unsafe.getInt(array, baseOffset)
//                : unsafe.getLong(array, baseOffset);
//    }
//
//    private static void printOffset(String fieldName) {
//        try {
//            Field field = String.class.getDeclaredField(fieldName);
//            long offset = unsafe.objectFieldOffset(field);
//            System.out.printf("   [ %2d번째 byte  ] : %s 필드\n", offset, fieldName);
//        } catch (NoSuchFieldException e) {
//            System.out.println("   [    (N/A)     ] : " + fieldName);
//        }
//    }
//}