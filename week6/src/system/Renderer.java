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
    /// 커서 심볼 (가로 3문자, 세로 1줄)
    /// </summary>
    private static final String CURSOR_SYMBOL = "[ ]";

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
    /// 맵 전체를 화면에 출력
    /// 버퍼를 초기화하고, 오브젝트를 그린 뒤 패널과 함께 출력
    /// </summary>
    public void render() {
        clearBuffer();
        drawResources();
        drawColonists();

        if (cursorMode) {
            drawCursor();
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

        // 정착민 목록
        lines.add(" [정착민]");
        for (int i = 0; i < colonists.size(); i++) {
            Colonist colonist = colonists.get(i);

            // 선택된 정착민에 > 표시
            String marker = (i == selectedIndex) ? " > " : "   ";
            lines.add(marker + "[" + colonist.getLabel() + "] " + colonist.getColonistName());
        }

        lines.add("");

        // 선택된 정착민 상세 정보
        if (!colonists.isEmpty()) {
            Colonist selected = colonists.get(selectedIndex);
            lines.add(" ──────────────");
            lines.add(" " + selected.getName());
            lines.add(" 체력: " + buildBar(selected.getHp(), selected.getMaxHp()));
            lines.add(" 피로: " + buildBar(selected.getFatigue(), selected.getMaxFatigue()));
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
