package system;

import java.util.ArrayList;

/// <summary>
/// 게임 화면을 콘솔에 출력하는 클래스
/// 맵 버퍼에 오브젝트를 그린 뒤, 우측 패널과 함께 한번에 출력
/// </summary>
public class Renderer {

    /// <summary>
    /// 우측 패널 구분선
    /// </summary>
    private static final String PANEL_SEPARATOR = "|||";

    /// <summary>
    /// 우측 패널 가로 크기 (구분선 제외)
    /// </summary>
    private static final int PANEL_WIDTH = 22;

    /// <summary>
    /// 출력할 게임 맵
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 플레이어 커서 (커서 모드에서 사용)
    /// </summary>
    private final Cursor cursor;

    /// <summary>
    /// 화면 버퍼 [행][열], 매 프레임마다 새로 채움
    /// </summary>
    private final char[][] buffer;

    /// <summary>
    /// 현재 선택된 정착민 번호
    /// </summary>
    private int selectedIndex;

    /// <summary>
    /// 커서 모드 여부 (false면 시뮬레이션 모드)
    /// </summary>
    private boolean cursorMode;

    /// <summary>
    /// 커서 모드에서 실행 중인 명령 이름 (예: "이동 위치 지정", "채집 대상 지정")
    /// </summary>
    private String cursorModeLabel;

    /// <summary>
    /// 건물 선택 모드 여부
    /// </summary>
    private boolean buildSelectMode;

    /// <summary>
    /// 낮/밤 주기 (시간 표시용)
    /// </summary>
    private DayNightCycle dayNightCycle;

    /// <summary>
    /// 열매 덤불 4x2 블록 (1행: " %% ", 2행: " %% ")
    /// </summary>
    private static final String[] FOOD_BLOCK = {" %% ", " %% "};

    /// <summary>
    /// 나무 4x2 블록 (1행: " ^  ", 2행: "/|\ ")
    /// </summary>
    private static final String[] TREE_BLOCK = {" ^  ", "/|\\ "};

    /// <summary>
    /// 바위 4x2 블록 (1행: " __ ", 2행: "|__|")
    /// </summary>
    private static final String[] ROCK_BLOCK = {" __ ", "|__|"};

    /// <summary>
    /// 철광석 4x2 블록 (1행: "/==\", 2행: "\==/")
    /// </summary>
    private static final String[] IRON_BLOCK = {"/==\\", "\\==/"};

    /// <summary>
    /// 벽 4x2 블록 (1행: "####", 2행: "####")
    /// </summary>
    private static final String[] WALL_BLOCK = {"####", "####"};

    /// <summary>
    /// 저장소 4x2 블록 (1행: "[==]", 2행: "[==]")
    /// </summary>
    private static final String[] STORAGE_BLOCK = {"[==]", "[==]"};

    /// <summary>
    /// 침실 4x2 블록 (1행: "[~~]", 2행: "[~~]")
    /// </summary>
    private static final String[] BEDROOM_BLOCK = {"[~~]", "[~~]"};

    /// <summary>
    /// 방어탑 4x2 블록 (1행: "/||\", 2행: "|  |")
    /// </summary>
    private static final String[] TOWER_BLOCK = {"/||\\", "|  |"};

    /// <summary>
    /// 적 4x2 블록 (1행: " XX ", 2행: " XX ")
    /// </summary>
    private static final String[] ENEMY_BLOCK = {" XX ", " XX "};

    /// <summary>
    /// 커서 심볼 (가로 3문자, 세로 1줄)
    /// </summary>
    private static final String CURSOR_SYMBOL = "[ ]";

    /// <summary>
    /// 이동 목표 마커 심볼
    /// </summary>
    private static final String TARGET_SYMBOL = " x ";

    /// <summary>
    /// 지정한 맵과 커서로 렌더러 생성
    /// </summary>
    public Renderer(GameMap gameMap, Cursor cursor) {
        this.gameMap = gameMap;
        this.cursor = cursor;
        this.buffer = new char[GameMap.HEIGHT][GameMap.WIDTH];
        this.selectedIndex = 0;
        this.cursorMode = false;
    }

    /// <summary>
    /// 선택된 정착민 번호 반환
    /// </summary>
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /// <summary>
    /// 다음 정착민 선택 (↓ 키)
    /// </summary>
    public void selectNext() {
        int count = gameMap.getColonists().size();
        if (count > 0) {
            selectedIndex = (selectedIndex + 1) % count;
        }
    }

    /// <summary>
    /// 이전 정착민 선택 (↑ 키)
    /// </summary>
    public void selectPrevious() {
        int count = gameMap.getColonists().size();
        if (count > 0) {
            selectedIndex = (selectedIndex - 1 + count) % count;
        }
    }

    /// <summary>
    /// 커서 모드 여부 반환
    /// </summary>
    public boolean isCursorMode() {
        return cursorMode;
    }

    /// <summary>
    /// 커서 모드 설정
    /// </summary>
    public void setCursorMode(boolean cursorMode) {
        this.cursorMode = cursorMode;
    }

    /// <summary>
    /// 커서 모드 라벨 설정 (패널에 표시할 명령 이름)
    /// </summary>
    public void setCursorModeLabel(String label) {
        this.cursorModeLabel = label;
    }

    /// <summary>
    /// 건물 선택 모드 설정
    /// </summary>
    public void setBuildSelectMode(boolean buildSelectMode) {
        this.buildSelectMode = buildSelectMode;
    }

    /// <summary>
    /// 낮/밤 주기 설정
    /// </summary>
    public void setDayNightCycle(DayNightCycle dayNightCycle) {
        this.dayNightCycle = dayNightCycle;
    }

    /// <summary>
    /// 맵 전체를 화면에 출력
    /// 버퍼를 초기화하고, 오브젝트를 그린 뒤 패널과 함께 출력
    /// </summary>
    public void render() {
        clearBuffer();
        drawResources();
        drawBuildings();
        drawTargetMarkers();
        drawColonists();
        drawEnemies();

        if (cursorMode) {
            drawCursor();
        }

        // 밤이면 맵 테두리에 점 표시
        if (dayNightCycle != null && dayNightCycle.isNight()) {
            drawNightBorder();
        }

        flush();
    }

    /// <summary>
    /// 버퍼를 공백으로 초기화
    /// </summary>
    private void clearBuffer() {
        for (int row = 0; row < GameMap.HEIGHT; row++) {
            for (int col = 0; col < GameMap.WIDTH; col++) {
                buffer[row][col] = ' ';
            }
        }
    }

    /// <summary>
    /// 맵의 모든 자원을 버퍼에 4x2 블록으로 그림
    /// </summary>
    private void drawResources() {
        for (Resource resource : gameMap.getResources()) {
            int row = resource.getPosition().getRow();
            int col = resource.getPosition().getCol();

            String[] block = getBlock(resource.getType());
            drawBlock(row, col, block);
        }
    }

    /// <summary>
    /// 맵의 모든 건물을 버퍼에 4x2 블록으로 그림
    /// </summary>
    private void drawBuildings() {
        for (Building building : gameMap.getBuildings()) {
            int row = building.getPosition().getRow();
            int col = building.getPosition().getCol();

            String[] block = getBuildingBlock(building.getType());
            drawBlock(row, col, block);
        }
    }

    /// <summary>
    /// 모든 정착민을 버퍼에 4x2 블록으로 그림
    /// 이니셜로 구분: (A) / " | "
    /// </summary>
    private void drawColonists() {
        for (Colonist colonist : gameMap.getColonists()) {
            int row = colonist.getPosition().getRow();
            int col = colonist.getPosition().getCol();
            char label = colonist.getLabel();

            String[] block = {"(" + label + ")", " |  "};
            drawBlock(row, col, block);
        }
    }

    /// <summary>
    /// 맵의 모든 적을 버퍼에 4x2 블록으로 그림
    /// </summary>
    private void drawEnemies() {
        for (Enemy enemy : gameMap.getEnemies()) {
            if (!enemy.isLiving()) {
                continue;
            }
            int row = enemy.getPosition().getRow();
            int col = enemy.getPosition().getCol();
            drawBlock(row, col, ENEMY_BLOCK);
        }
    }

    /// <summary>
    /// 이동/채집 중인 정착민의 목표 위치에 마커를 그림
    /// </summary>
    private void drawTargetMarkers() {
        for (Colonist colonist : gameMap.getColonists()) {
            ColonistState state = colonist.getCurrentState();

            // 목표 위치 추출 (이동, 채집, 건설 상태에서)
            Position target = null;
            if (state instanceof MovingState) {
                target = ((MovingState) state).getTarget();
            } else if (state instanceof GatheringState) {
                target = ((GatheringState) state).getTarget().getPosition();
            } else if (state instanceof BuildingState) {
                target = ((BuildingState) state).getBuildPosition();
            }

            if (target == null) {
                continue;
            }

            int row = target.getRow();
            int col = target.getCol();

            // 목표 마커를 버퍼에 그림
            for (int i = 0; i < TARGET_SYMBOL.length(); i++) {
                int bufferCol = col + i;

                if (bufferCol >= 0 && bufferCol < GameMap.WIDTH) {
                    buffer[row][bufferCol] = TARGET_SYMBOL.charAt(i);
                }
            }
        }
    }

    /// <summary>
    /// 커서를 버퍼에 그림 (커서 모드일 때만 호출)
    /// </summary>
    private void drawCursor() {
        int row = cursor.getPosition().getRow();
        int col = cursor.getPosition().getCol();

        for (int i = 0; i < CURSOR_SYMBOL.length(); i++) {
            int bufferCol = col + i;

            if (bufferCol >= 0 && bufferCol < GameMap.WIDTH) {
                buffer[row][bufferCol] = CURSOR_SYMBOL.charAt(i);
            }
        }
    }

    /// <summary>
    /// 밤일 때 맵 테두리에 점을 그려 밤 분위기 표현
    /// 상단/하단 1줄, 좌측/우측 1열에 점 표시
    /// </summary>
    private void drawNightBorder() {
        // 상단 테두리
        for (int col = 0; col < GameMap.WIDTH; col++) {
            buffer[0][col] = '.';
        }
        // 하단 테두리
        for (int col = 0; col < GameMap.WIDTH; col++) {
            buffer[GameMap.HEIGHT - 1][col] = '.';
        }
        // 좌측 테두리
        for (int row = 0; row < GameMap.HEIGHT; row++) {
            buffer[row][0] = '.';
        }
        // 우측 테두리
        for (int row = 0; row < GameMap.HEIGHT; row++) {
            buffer[row][GameMap.WIDTH - 1] = '.';
        }
    }

    /// <summary>
    /// 자원 종류에 맞는 4x2 블록 반환
    /// </summary>
    private String[] getBlock(ResourceType type) {
        switch (type) {
            case FOOD:
                return FOOD_BLOCK;
            case TREE:
                return TREE_BLOCK;
            case ROCK:
                return ROCK_BLOCK;
            case IRON:
                return IRON_BLOCK;
            default:
                return FOOD_BLOCK;
        }
    }

    /// <summary>
    /// 건물 종류에 맞는 4x2 블록 반환
    /// </summary>
    private String[] getBuildingBlock(BuildingType type) {
        switch (type) {
            case WALL:
                return WALL_BLOCK;
            case STORAGE:
                return STORAGE_BLOCK;
            case BEDROOM:
                return BEDROOM_BLOCK;
            case TOWER:
                return TOWER_BLOCK;
            default:
                return WALL_BLOCK;
        }
    }

    /// <summary>
    /// 지정한 위치에 4x2 블록을 버퍼에 그림
    /// 맵 범위를 벗어나는 부분은 무시
    /// </summary>
    private void drawBlock(int startRow, int startCol, String[] block) {
        // 블록 높이(2줄)만큼 반복
        for (int blockRow = 0; blockRow < block.length; blockRow++) {
            int bufferRow = startRow + blockRow;

            // 맵 세로 범위를 벗어나면 무시
            if (bufferRow < 0 || bufferRow >= GameMap.HEIGHT) {
                continue;
            }

            String line = block[blockRow];

            // 블록 가로(4문자)만큼 반복
            for (int blockCol = 0; blockCol < line.length(); blockCol++) {
                int bufferCol = startCol + blockCol;

                // 맵 가로 범위를 벗어나면 무시
                if (bufferCol < 0 || bufferCol >= GameMap.WIDTH) {
                    continue;
                }

                buffer[bufferRow][bufferCol] = line.charAt(blockCol);
            }
        }
    }

    /// <summary>
    /// 우측 패널 내용을 줄 단위로 생성
    /// 정착민 목록과 선택된 정착민의 상세 정보를 표시
    /// </summary>
    private ArrayList<String> buildPanel() {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<Colonist> colonists = gameMap.getColonists();

        // 시간 표시
        if (dayNightCycle != null) {
            String phase = dayNightCycle.isNight() ? "밤" : "낮";
            int remaining = dayNightCycle.getRemainingSeconds();
            lines.add(" [시간] " + dayNightCycle.getDay() + "일차 " + phase);
            lines.add("  전환까지 " + remaining + "초");

            // 밤이면 적 수 표시
            if (dayNightCycle.isNight()) {
                int enemyCount = gameMap.getEnemies().size();
                lines.add("  적: " + enemyCount + "마리");
            }
            lines.add("");
        }

        // 자원 보유량
        Supply supply = gameMap.getSupply();
        lines.add(" [물자]");
        lines.add("  식량: " + supply.getFood());
        lines.add("  목재: " + supply.getWood());
        lines.add("  석재: " + supply.getStone());
        lines.add("  철: " + supply.getIron());
        lines.add("");

        // 정착민 목록
        lines.add(" [정착민]");
        for (int i = 0; i < colonists.size(); i++) {
            Colonist colonist = colonists.get(i);

            // 선택된 정착민에 > 표시, 상태도 함께 표시
            String marker = (i == selectedIndex) ? " > " : "   ";
            String stateName = colonist.getCurrentState().getDisplayName();
            lines.add(marker + "[" + colonist.getLabel() + "] " + stateName);
        }

        lines.add("");

        // 선택된 정착민 상세 정보
        if (!colonists.isEmpty()) {
            Colonist selected = colonists.get(selectedIndex);
            lines.add(" ──────────────");
            lines.add(" " + selected.getColonistName());
            lines.add(" 상태: " + selected.getCurrentState().getDisplayName());
            lines.add(" 체력: " + buildBar(selected.getHp(), selected.getMaxHp()));
            lines.add(" 피로: " + buildBar(selected.getFatigue(), selected.getMaxFatigue()));
        }

        lines.add("");

        // 모드별 명령 안내
        if (cursorMode) {
            lines.add(" ──────────────");
            // 커서 모드 라벨이 설정되어 있으면 사용, 없으면 기본값
            String label = (cursorModeLabel != null) ? cursorModeLabel : "위치 지정";
            lines.add(" [" + label + "]");
            lines.add(" 방향키: 커서 이동");
            lines.add(" Enter: 확정");
            lines.add(" q: 취소");
        } else if (buildSelectMode) {
            lines.add(" ──────────────");
            lines.add(" [건물 선택]");
            lines.add(" a: 벽");
            lines.add("    목재" + BuildingType.WALL.getWoodCost());
            lines.add(" b: 저장소");
            lines.add("    목재" + BuildingType.STORAGE.getWoodCost() + " 석재" + BuildingType.STORAGE.getStoneCost());
            lines.add(" c: 침실");
            lines.add("    목재" + BuildingType.BEDROOM.getWoodCost() + " 석재" + BuildingType.BEDROOM.getStoneCost());
            lines.add(" d: 방어탑");
            lines.add("    목재" + BuildingType.TOWER.getWoodCost() + " 석재" + BuildingType.TOWER.getStoneCost() + " 철" + BuildingType.TOWER.getIronCost());
            lines.add(" q: 취소");
        } else {
            lines.add(" ──────────────");
            lines.add(" [명령]");
            lines.add(" 1: 이동");
            lines.add(" 2: 채집");
            lines.add(" 3: 휴식");
            lines.add(" 4: 건설");
        }

        return lines;
    }

    /// <summary>
    /// 수치를 막대 형태로 표시 (예: "████░░ 80")
    /// 전체 10칸 중 비율만큼 채움
    /// </summary>
    private String buildBar(int current, int max) {
        // 막대 전체 길이
        int barLength = 10;

        // 현재 비율에 해당하는 채워진 칸 수
        int filled = (int) ((double) current / max * barLength);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append('#');
            } else {
                bar.append('.');
            }
        }
        bar.append(' ');
        bar.append(current);

        return bar.toString();
    }

    /// <summary>
    /// 패널 한 줄을 고정 폭에 맞춰 반환
    /// 짧으면 공백으로 채우고, 길면 잘라냄
    /// </summary>
    private String padPanel(String text) {
        if (text.length() >= PANEL_WIDTH) {
            return text.substring(0, PANEL_WIDTH);
        }

        StringBuilder padded = new StringBuilder(text);
        while (padded.length() < PANEL_WIDTH) {
            padded.append(' ');
        }
        return padded.toString();
    }

    /// <summary>
    /// 버퍼와 우측 패널을 합쳐서 화면에 한번에 출력
    /// </summary>
    private void flush() {
        ArrayList<String> panelLines = buildPanel();

        int totalWidth = GameMap.WIDTH + PANEL_SEPARATOR.length() + PANEL_WIDTH;
        StringBuilder screen = new StringBuilder(totalWidth * GameMap.HEIGHT + GameMap.HEIGHT + 10);

        // 커서를 맨 위로 이동 (문자열에 포함하여 한번에 출력)
        screen.append("\033[H");

        for (int row = 0; row < GameMap.HEIGHT; row++) {
            // 맵 버퍼
            for (int col = 0; col < GameMap.WIDTH; col++) {
                screen.append(buffer[row][col]);
            }

            // 구분선 + 패널
            screen.append(PANEL_SEPARATOR);
            if (row < panelLines.size()) {
                screen.append(padPanel(panelLines.get(row)));
            } else {
                screen.append(padPanel(""));
            }

            screen.append('\n');
        }

        // 화면 아래 잔여 내용 지움 (한번에 출력되므로 깜빡임 없음)
        screen.append("\033[J");

        System.out.print(screen);
        System.out.flush();
    }
}
