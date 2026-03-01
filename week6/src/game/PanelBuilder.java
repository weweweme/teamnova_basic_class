package game;

import entity.colonist.Colonist;
import structure.Barricade;

import java.util.ArrayList;

/// <summary>
/// 우측 정보 패널의 내용을 생성하는 클래스
/// 시간, 보급품, 정착민 목록, 명령 안내 등을 줄 단위로 구성
/// </summary>
public class PanelBuilder {

    /// <summary>
    /// 게임 맵 (보급품, 정착민, 적 정보 조회용)
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 입력 처리기 (메뉴 모드 상태 조회용)
    /// </summary>
    private final InputHandler inputHandler;

    /// <summary>
    /// 낮/밤 주기 (시간 표시용)
    /// </summary>
    private DayNightCycle dayNightCycle;

    /// <summary>
    /// 패널 줄 재사용 버퍼
    /// </summary>
    private final ArrayList<String> panelLines = new ArrayList<>();

    /// <summary>
    /// 체력바 조립 재사용 버퍼
    /// </summary>
    private final StringBuilder barBuilder = new StringBuilder();

    /// <summary>
    /// 패널 빌더 생성
    /// </summary>
    public PanelBuilder(GameMap gameMap, InputHandler inputHandler) {
        this.gameMap = gameMap;
        this.inputHandler = inputHandler;
    }

    /// <summary>
    /// 낮/밤 주기 설정
    /// </summary>
    public void setDayNightCycle(DayNightCycle dayNightCycle) {
        this.dayNightCycle = dayNightCycle;
    }

    /// <summary>
    /// 현재 패널 줄 목록 반환
    /// </summary>
    public ArrayList<String> getPanelLines() {
        return panelLines;
    }

    /// <summary>
    /// 패널 내용을 갱신
    /// 시간, 보급품, 정착민 목록, 선택 정착민 상세 정보, 명령 안내 표시
    /// </summary>
    public void build(int selectedIndex) {
        panelLines.clear();
        ArrayList<Colonist> colonists = gameMap.getColonists();

        // 시간 표시
        if (dayNightCycle != null) {
            String phase = dayNightCycle.isNight() ? "밤" : "낮";
            String diffName = dayNightCycle.getDifficultyName();
            panelLines.add(" [" + diffName + "] " + dayNightCycle.getDay() + "일차 " + phase);

            if (!dayNightCycle.isNight()) {
                panelLines.add("  전환까지 " + dayNightCycle.getRemainingSeconds() + "초");
            }

            if (dayNightCycle.isNight()) {
                int alive = gameMap.getEnemies().size();
                int pending = dayNightCycle.getPendingCount();
                int total = dayNightCycle.getTotalWaveSize();
                int defeated = total - alive - pending;
                panelLines.add("  처치 " + defeated + "/" + total);
            }

            panelLines.add("");
        }

        // 보급품 + 바리케이드
        Barricade barricade = gameMap.getBarricade();
        panelLines.add(" [보급] " + gameMap.getSupply().getAmount());
        panelLines.add(" [바리] Lv" + barricade.getLevel() + " " + buildBar(barricade.getHp(), barricade.getMaxHp()));
        panelLines.add(" [처치] " + gameMap.getEnemiesKilled() + "마리");
        panelLines.add("");

        // 정착민 목록
        panelLines.add(" [정착민]");
        for (int i = 0; i < colonists.size(); i++) {
            Colonist colonist = colonists.get(i);
            String marker = (i == selectedIndex) ? " > " : "   ";

            if (colonist.isLiving()) {
                String typeName = colonist.getSpec().getDisplayName();
                String stateName = colonist.getCurrentState().getDisplayName();
                panelLines.add(marker + "[" + colonist.getLabel() + "] " + typeName + " " + stateName);
            } else {
                panelLines.add(marker + "[" + colonist.getLabel() + "] 사망");
            }
        }

        panelLines.add("");

        // 선택된 정착민 상세 정보
        if (!colonists.isEmpty()) {
            Colonist selected = colonists.get(selectedIndex);
            panelLines.add(" ──────────────");
            panelLines.add(" " + selected.getColonistName());
            panelLines.add(" 유형: " + selected.getSpec().getDisplayName());

            if (selected.isLiving()) {
                panelLines.add(" 상태: " + selected.getCurrentState().getDisplayName());
                panelLines.add(" 체력: " + buildBar(selected.getHp(), selected.getMaxHp()));
                panelLines.add(" 무기: " + selected.getGun().getName());
            } else {
                panelLines.add(" 상태: 사망");
            }
        }

        panelLines.add("");

        // 승리 / 게임오버 / 명령 안내
        if (isVictory()) {
            panelLines.add(" ──────────────");
            panelLines.add(" [승리!]");
            panelLines.add(" " + dayNightCycle.getDay() + "일을");
            panelLines.add(" 버텨냈습니다!");
            panelLines.add("");
            panelLines.add(" [통계]");
            panelLines.add(" 처치: " + gameMap.getEnemiesKilled() + "마리");
            panelLines.add("");
            panelLines.add(" q: 종료");
        } else if (isGameOver()) {
            panelLines.add(" ──────────────");
            panelLines.add(" [게임 오버]");
            panelLines.add(" 모든 정착민이");
            panelLines.add(" 사망했습니다.");
            panelLines.add("");
            panelLines.add(" [통계]");
            if (dayNightCycle != null) {
                panelLines.add(" 생존: " + dayNightCycle.getDay() + "일");
            }
            panelLines.add(" 처치: " + gameMap.getEnemiesKilled() + "마리");
            panelLines.add("");
            panelLines.add(" q: 종료");
        } else {
            panelLines.add(" ──────────────");

            boolean isNight = dayNightCycle != null && dayNightCycle.isNight();
            if (isNight) {
                // 밤: 전투 상태 표시
                int alive = gameMap.getEnemies().size();
                int pending = dayNightCycle.getPendingCount();
                int total = dayNightCycle.getTotalWaveSize();
                int defeated = total - alive - pending;
                panelLines.add(" [전투 중]");
                panelLines.add(" 진행: " + defeated + "/" + total);
                panelLines.add(" 남은 적: " + (alive + pending));

                // 웨이브 구성 미리보기
                ArrayList<String> preview = dayNightCycle.getWavePreview();
                if (!preview.isEmpty()) {
                    panelLines.add("");
                    panelLines.add(" [웨이브]");
                    for (String entry : preview) {
                        panelLines.add("  " + entry);
                    }
                }

                panelLines.add(" q: 종료");
            } else if (inputHandler.isShopMode()) {
                // 무기 상점 모드
                panelLines.add(" [무기 상점]");
                panelLines.add(" 1: 피스톨 (무료)");
                panelLines.add(" 2: 샷건  (보급25)");
                panelLines.add(" 3: 라이플 (보급20)");
                panelLines.add(" 4: 미니건 (보급30)");
                panelLines.add(" q: 취소");
            } else if (inputHandler.isRecruitMode()) {
                // 모집 모드
                panelLines.add(" [모집] (보급40)");
                panelLines.add(" 1: 사격수 (속사)");
                panelLines.add(" 2: 저격수 (치명타)");
                panelLines.add(" 3: 돌격수 (넉백)");
                panelLines.add(" q: 취소");
            } else if (inputHandler.isBuildMode()) {
                // 건설 모드
                panelLines.add(" [건설]");
                panelLines.add(" 1: 가시덫 (보급20)");
                panelLines.add(" 2: 지뢰  (보급25)");
                panelLines.add(" 3: 탄약상자 (보급20)");
                if (barricade.canUpgrade()) {
                    panelLines.add(" 4: 바리강화 (보급" + barricade.getUpgradeCost() + ")");
                } else {
                    panelLines.add(" 4: 바리강화 (MAX)");
                }
                panelLines.add(" q: 취소");
            } else {
                // 낮: 관리 명령
                panelLines.add(" [명령] (낮)");
                panelLines.add(" 1: 수리 (보급10)");
                panelLines.add(" 2: 무기 구매");
                panelLines.add(" 3: 치료 (보급10)");
                panelLines.add(" 4: 건설");
                panelLines.add(" 5: 모집 (보급40)");
                panelLines.add(" n: 밤 건너뛰기");
                panelLines.add(" q: 종료");
            }
        }
    }

    /// <summary>
    /// 수치를 막대 형태로 표시 (예: "######.... 60")
    /// 전체 10칸 중 비율만큼 채움
    /// </summary>
    private String buildBar(int current, int max) {
        barBuilder.setLength(0);
        int barLength = 10;
        int filled = (int) ((double) current / max * barLength);

        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                barBuilder.append('#');
            } else {
                barBuilder.append('.');
            }
        }
        barBuilder.append(' ');
        barBuilder.append(current);

        return barBuilder.toString();
    }

    /// <summary>
    /// 모든 정착민이 사망했는지 확인
    /// </summary>
    private boolean isGameOver() {
        for (Colonist colonist : gameMap.getColonists()) {
            if (colonist.isLiving()) {
                return false;
            }
        }
        return true;
    }

    /// <summary>
    /// 승리 조건 달성 여부 확인
    /// </summary>
    private boolean isVictory() {
        return dayNightCycle != null && dayNightCycle.isVictory();
    }
}
