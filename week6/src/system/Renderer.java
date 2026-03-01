package system;

import entity.Bullet;
import entity.colonist.Colonist;
import entity.colonist.ColonistType;
import entity.enemy.Enemy;
import world.Barricade;
import world.DayNightCycle;
import world.GameMap;
import world.GameMap.HitEffect;
import world.Spike;

import java.util.ArrayList;

/// <summary>
/// 게임 화면을 콘솔에 출력하는 클래스
/// 맵 버퍼(좌측) + 우측 패널 + 하단 로그를 합쳐서 한번에 출력
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
    /// 하단 로그 표시 줄 수
    /// </summary>
    private static final int LOG_LINES = 8;

    /// <summary>
    /// 화면 버퍼 [행][열], 매 프레임마다 새로 채움
    /// </summary>
    private final char[][] buffer;

    /// <summary>
    /// 색상 버퍼 [행][열], ANSI 색상 코드 (0이면 기본색, 31=빨강, 90=짙은 회색)
    /// </summary>
    private final int[][] colorBuffer;

    /// <summary>
    /// 출력할 게임 맵
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 낮/밤 주기 (시간 표시용)
    /// </summary>
    private DayNightCycle dayNightCycle;

    /// <summary>
    /// 현재 선택된 정착민 번호
    /// </summary>
    private int selectedIndex;


    /// <summary>
    /// 지정한 맵으로 렌더러 생성
    /// </summary>
    public Renderer(GameMap gameMap) {
        this.gameMap = gameMap;
        this.buffer = new char[GameMap.HEIGHT][GameMap.WIDTH];
        this.colorBuffer = new int[GameMap.HEIGHT][GameMap.WIDTH];
        this.selectedIndex = 0;
    }

    /// <summary>
    /// 낮/밤 주기 설정
    /// </summary>
    public void setDayNightCycle(DayNightCycle dayNightCycle) {
        this.dayNightCycle = dayNightCycle;
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
    /// 모든 정착민이 사망했는지 확인
    /// </summary>
    public boolean isGameOver() {
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
    public boolean isVictory() {
        return dayNightCycle != null && dayNightCycle.isVictory();
    }

    /// <summary>
    /// 맵 전체를 화면에 출력
    /// 버퍼를 초기화하고, 오브젝트를 그린 뒤 패널과 함께 출력
    /// </summary>
    public void render() {
        clearBuffer();
        drawBarricade();
        drawSpikes();
        drawColonists();
        drawEnemies();
        drawBullets();
        drawEffects();
        flush();
    }

    /// <summary>
    /// 버퍼를 공백으로 초기화
    /// </summary>
    private void clearBuffer() {
        for (int row = 0; row < GameMap.HEIGHT; row++) {
            for (int col = 0; col < GameMap.WIDTH; col++) {
                buffer[row][col] = ' ';
                colorBuffer[row][col] = 0;
            }
        }
    }

    /// <summary>
    /// 바리케이드를 세로로 그림 (## 문자, 맵 전체 높이)
    /// 파괴되었으면 빨간색 .. 으로 표시
    /// </summary>
    private void drawBarricade() {
        Barricade barricade = gameMap.getBarricade();
        boolean destroyed = barricade.isDestroyed();
        char wallChar = destroyed ? '.' : '#';

        // 파괴: 빨간색, 피격: 빨간색, 수리: 초록색, 평상시: 기본색
        int color = 0;
        if (destroyed || barricade.isRecentlyHit()) {
            color = 31;
        } else if (barricade.isRecentlyRepaired()) {
            color = 32;
        }

        for (int row = 0; row < GameMap.HEIGHT; row++) {
            buffer[row][Barricade.COLUMN] = wallChar;
            buffer[row][Barricade.COLUMN + 1] = wallChar;
            colorBuffer[row][Barricade.COLUMN] = color;
            colorBuffer[row][Barricade.COLUMN + 1] = color;
        }
    }

    /// <summary>
    /// 설치된 가시덫을 세로로 그림 (^^ 문자, 맵 전체 높이)
    /// 파괴된 가시덫은 표시하지 않음
    /// </summary>
    private void drawSpikes() {
        for (Spike spike : gameMap.getSpikes()) {
            if (spike.isDestroyed()) {
                continue;
            }
            int col = spike.getColumn();

            for (int row = 0; row < GameMap.HEIGHT; row++) {
                if (col >= 0 && col < GameMap.WIDTH) {
                    buffer[row][col] = '^';
                    colorBuffer[row][col] = 33;
                }
            }
        }
    }

    /// <summary>
    /// 모든 정착민을 버퍼에 그림
    /// 살아있으면 기본색, 죽었으면 회색 → 소멸 애니메이션
    /// </summary>
    private void drawColonists() {
        long now = System.currentTimeMillis();

        for (Colonist colonist : gameMap.getColonists()) {
            int row = colonist.getPosition().getRow();
            int col = colonist.getPosition().getCol();
            char label = colonist.getLabel();
            String[] block = {"(" + label + ")", " |  "};

            if (colonist.isLiving()) {
                drawBlock(row, col, block);
            } else {
                // 사망 애니메이션
                long elapsed = now - colonist.getDeathTime();

                if (elapsed < 400) {
                    // Phase 1: 전체 짙은 회색으로 정지
                    int[] rowColors = {90, 90};
                    drawColoredBlock(row, col, block, rowColors);
                } else if (elapsed < 800) {
                    // Phase 2: 아래부터 한 줄씩 소멸
                    double progress = (double) (elapsed - 400) / 400;
                    int removedRows = (int) Math.ceil(progress * block.length);
                    int visibleRows = block.length - removedRows;

                    if (visibleRows > 0) {
                        String[] partialBlock = new String[visibleRows];
                        int[] rowColors = new int[visibleRows];
                        for (int i = 0; i < visibleRows; i++) {
                            partialBlock[i] = block[i];
                            rowColors[i] = 90;
                        }
                        drawColoredBlock(row, col, partialBlock, rowColors);
                    }
                }
                // Phase 3 (800ms+): 아무것도 안 그림
            }
        }
    }

    /// <summary>
    /// 맵의 모든 적을 버퍼에 그림
    /// 살아있는 적: HP 비율에 따라 위에서부터 빨간색
    /// 죽은 적: 짙은 회색 → 아래부터 소멸 애니메이션
    /// </summary>
    private void drawEnemies() {
        long now = System.currentTimeMillis();

        for (Enemy enemy : gameMap.getEnemies()) {
            int row = enemy.getPosition().getRow();
            int col = enemy.getPosition().getCol();
            String[] block = enemy.getType().getBlock();
            int blockHeight = block.length;

            if (enemy.isLiving()) {
                // 살아있는 적: HP 비율에 따라 위에서부터 빨간색
                double hpRatio = (double) enemy.getHp() / enemy.getMaxHp();
                int redRows = (int) Math.ceil((1.0 - hpRatio) * blockHeight);

                int[] rowColors = new int[blockHeight];
                for (int i = 0; i < redRows; i++) {
                    rowColors[i] = 31;
                }
                drawColoredBlock(row, col, block, rowColors);
            } else {
                // 죽은 적: 사망 애니메이션
                long elapsed = now - enemy.getDeathTime();

                if (elapsed < 400) {
                    // Phase 1: 전체 짙은 회색으로 정지
                    int[] rowColors = new int[blockHeight];
                    for (int i = 0; i < blockHeight; i++) {
                        rowColors[i] = 90;
                    }
                    drawColoredBlock(row, col, block, rowColors);
                } else if (elapsed < 800) {
                    // Phase 2: 아래부터 한 줄씩 소멸
                    double progress = (double) (elapsed - 400) / 400;
                    int removedRows = (int) Math.ceil(progress * blockHeight);
                    int visibleRows = blockHeight - removedRows;

                    if (visibleRows > 0) {
                        String[] partialBlock = new String[visibleRows];
                        int[] rowColors = new int[visibleRows];
                        for (int i = 0; i < visibleRows; i++) {
                            partialBlock[i] = block[i];
                            rowColors[i] = 90;
                        }
                        drawColoredBlock(row, col, partialBlock, rowColors);
                    }
                }
                // Phase 3 (800ms+): 아무것도 안 그림 (제거 대기)
            }
        }
    }

    /// <summary>
    /// 날아가는 총알을 버퍼에 * 로 그림
    /// </summary>
    private void drawBullets() {
        for (Bullet bullet : gameMap.getBullets()) {
            int row = bullet.getRow();
            int col = bullet.getCol();

            if (row >= 0 && row < GameMap.HEIGHT && col >= 0 && col < GameMap.WIDTH) {
                buffer[row][col] = '*';
            }
        }
    }

    /// <summary>
    /// 명중 이펙트를 버퍼에 노란색 ! 로 그림
    /// </summary>
    private void drawEffects() {
        for (HitEffect effect : gameMap.getEffects()) {
            int row = effect.getRow();
            int col = effect.getCol();

            if (row >= 0 && row < GameMap.HEIGHT && col >= 0 && col < GameMap.WIDTH) {
                buffer[row][col] = '!';
                colorBuffer[row][col] = 33;
            }
        }
    }

    /// <summary>
    /// 지정한 위치에 블록을 버퍼에 그림
    /// 맵 범위를 벗어나는 부분은 무시
    /// </summary>
    private void drawBlock(int startRow, int startCol, String[] block) {
        for (int blockRow = 0; blockRow < block.length; blockRow++) {
            int bufferRow = startRow + blockRow;

            if (bufferRow < 0 || bufferRow >= GameMap.HEIGHT) {
                continue;
            }

            String line = block[blockRow];

            for (int blockCol = 0; blockCol < line.length(); blockCol++) {
                int bufferCol = startCol + blockCol;

                if (bufferCol < 0 || bufferCol >= GameMap.WIDTH) {
                    continue;
                }

                buffer[bufferRow][bufferCol] = line.charAt(blockCol);
            }
        }
    }

    /// <summary>
    /// 지정한 위치에 블록을 버퍼에 그리면서 행별 색상도 기록
    /// rowColors[i]가 0이 아니면 해당 행에 ANSI 색상 적용
    /// </summary>
    private void drawColoredBlock(int startRow, int startCol, String[] block, int[] rowColors) {
        for (int blockRow = 0; blockRow < block.length; blockRow++) {
            int bufferRow = startRow + blockRow;

            if (bufferRow < 0 || bufferRow >= GameMap.HEIGHT) {
                continue;
            }

            String line = block[blockRow];
            int color = rowColors[blockRow];

            for (int blockCol = 0; blockCol < line.length(); blockCol++) {
                int bufferCol = startCol + blockCol;

                if (bufferCol < 0 || bufferCol >= GameMap.WIDTH) {
                    continue;
                }

                buffer[bufferRow][bufferCol] = line.charAt(blockCol);
                colorBuffer[bufferRow][bufferCol] = color;
            }
        }
    }

    /// <summary>
    /// 우측 패널 내용을 줄 단위로 생성
    /// 시간, 보급품, 정착민 목록, 선택 정착민 상세 정보 표시
    /// </summary>
    private ArrayList<String> buildPanel() {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<Colonist> colonists = gameMap.getColonists();

        // 시간 표시
        if (dayNightCycle != null) {
            String phase = dayNightCycle.isNight() ? "밤" : "낮";
            String diffName = dayNightCycle.getDifficultyName();
            lines.add(" [" + diffName + "] " + dayNightCycle.getDay() + "일차 " + phase);

            if (!dayNightCycle.isNight()) {
                lines.add("  전환까지 " + dayNightCycle.getRemainingSeconds() + "초");
            }

            if (dayNightCycle.isNight()) {
                int alive = gameMap.getEnemies().size();
                int pending = dayNightCycle.getPendingCount();
                int total = dayNightCycle.getTotalWaveSize();
                int defeated = total - alive - pending;
                lines.add("  처치 " + defeated + "/" + total);
            }

            lines.add("");
        }

        // 보급품 + 바리케이드
        Barricade barricade = gameMap.getBarricade();
        lines.add(" [보급] " + gameMap.getSupply().getAmount());
        lines.add(" [바리] " + buildBar(barricade.getHp(), barricade.getMaxHp()));
        lines.add(" [처치] " + gameMap.getEnemiesKilled() + "마리");
        lines.add("");

        // 정착민 목록
        lines.add(" [정착민]");
        for (int i = 0; i < colonists.size(); i++) {
            Colonist colonist = colonists.get(i);
            String marker = (i == selectedIndex) ? " > " : "   ";

            if (colonist.isLiving()) {
                String typeName = colonist.getType().getDisplayName();
                String stateName = colonist.getCurrentState().getDisplayName();
                lines.add(marker + "[" + colonist.getLabel() + "] " + typeName + " " + stateName);
            } else {
                lines.add(marker + "[" + colonist.getLabel() + "] 사망");
            }
        }

        lines.add("");

        // 선택된 정착민 상세 정보
        if (!colonists.isEmpty()) {
            Colonist selected = colonists.get(selectedIndex);
            lines.add(" ──────────────");
            lines.add(" " + selected.getColonistName());
            lines.add(" 유형: " + selected.getType().getDisplayName());

            if (selected.isLiving()) {
                lines.add(" 상태: " + selected.getCurrentState().getDisplayName());
                lines.add(" 체력: " + buildBar(selected.getHp(), selected.getMaxHp()));
                lines.add(" 무기: Lv" + selected.getWeaponLevel());
            } else {
                lines.add(" 상태: 사망");
            }
        }

        lines.add("");

        // 승리 / 게임오버 / 명령 안내
        if (isVictory()) {
            lines.add(" ──────────────");
            lines.add(" [승리!]");
            lines.add(" " + dayNightCycle.getDay() + "일을");
            lines.add(" 버텨냈습니다!");
            lines.add("");
            lines.add(" [통계]");
            lines.add(" 처치: " + gameMap.getEnemiesKilled() + "마리");
            lines.add("");
            lines.add(" q: 종료");
        } else if (isGameOver()) {
            lines.add(" ──────────────");
            lines.add(" [게임 오버]");
            lines.add(" 모든 정착민이");
            lines.add(" 사망했습니다.");
            lines.add("");
            lines.add(" [통계]");
            if (dayNightCycle != null) {
                lines.add(" 생존: " + dayNightCycle.getDay() + "일");
            }
            lines.add(" 처치: " + gameMap.getEnemiesKilled() + "마리");
            lines.add("");
            lines.add(" q: 종료");
        } else {
            lines.add(" ──────────────");

            boolean isNight = dayNightCycle != null && dayNightCycle.isNight();
            if (isNight) {
                // 밤: 전투 상태 표시
                int alive = gameMap.getEnemies().size();
                int pending = dayNightCycle.getPendingCount();
                int total = dayNightCycle.getTotalWaveSize();
                int defeated = total - alive - pending;
                lines.add(" [전투 중]");
                lines.add(" 진행: " + defeated + "/" + total);
                lines.add(" 남은 적: " + (alive + pending));
                lines.add(" q: 종료");
            } else {
                // 낮: 관리 명령
                lines.add(" [명령] (낮)");
                lines.add(" 1: 수리 (보급10)");
                lines.add(" 2: 강화 (보급15)");
                lines.add(" 3: 치료 (보급10)");
                lines.add(" 4: 가시덫 (보급20)");
                lines.add(" n: 밤 건너뛰기");
                lines.add(" q: 종료");
            }
        }

        return lines;
    }

    /// <summary>
    /// 수치를 막대 형태로 표시 (예: "######.... 60")
    /// 전체 10칸 중 비율만큼 채움
    /// </summary>
    private String buildBar(int current, int max) {
        int barLength = 10;
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
    /// 한글 등 전각 문자를 고려한 터미널 표시 폭 계산
    /// </summary>
    private int displayWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isKorean = c >= 0xAC00 && c <= 0xD7A3;
            boolean isCjk = c >= 0x3000 && c <= 0x9FFF;
            if (isKorean || isCjk) {
                width += 2;
            } else {
                width += 1;
            }
        }
        return width;
    }

    /// <summary>
    /// 문자열을 지정한 표시 폭에 맞춰 공백으로 패딩
    /// </summary>
    private String padToWidth(String text, int targetWidth) {
        int currentWidth = displayWidth(text);
        StringBuilder padded = new StringBuilder(text);
        for (int i = currentWidth; i < targetWidth; i++) {
            padded.append(' ');
        }
        return padded.toString();
    }

    /// <summary>
    /// 버퍼와 우측 패널을 합쳐서 화면에 한번에 출력
    /// 맵 영역 + 구분선 + 로그 영역 모두 우측 패널과 나란히 표시
    /// </summary>
    private void flush() {
        ArrayList<String> panelLines = buildPanel();
        ArrayList<String> logs = gameMap.getRecentLogs();

        // 전체 높이: 맵(20) + 구분선(1) + 로그(8) = 29줄
        int totalHeight = GameMap.HEIGHT + 1 + LOG_LINES;
        int totalWidth = GameMap.WIDTH + PANEL_SEPARATOR.length() + PANEL_WIDTH;
        StringBuilder screen = new StringBuilder(totalWidth * totalHeight + totalHeight + 10);

        // 커서를 맨 위로 이동
        screen.append("\033[H");

        for (int row = 0; row < totalHeight; row++) {
            // 좌측 내용 (맵 / 구분선 / 로그)
            if (row < GameMap.HEIGHT) {
                // 맵 버퍼 (색상 적용)
                for (int col = 0; col < GameMap.WIDTH; col++) {
                    int color = colorBuffer[row][col];
                    if (color != 0) {
                        screen.append("\033[");
                        screen.append(color);
                        screen.append('m');
                        screen.append(buffer[row][col]);
                        screen.append("\033[0m");
                    } else {
                        screen.append(buffer[row][col]);
                    }
                }
            } else if (row == GameMap.HEIGHT) {
                // 로그 구분선
                for (int i = 0; i < GameMap.WIDTH; i++) {
                    screen.append('-');
                }
            } else {
                // 로그 줄
                int logIndex = row - GameMap.HEIGHT - 1;
                String logContent = "";
                if (logIndex < logs.size()) {
                    logContent = " " + logs.get(logIndex);
                }
                screen.append(padToWidth(logContent, GameMap.WIDTH));
            }

            // 우측 패널
            screen.append(PANEL_SEPARATOR);
            if (row < panelLines.size()) {
                screen.append(padPanel(panelLines.get(row)));
            } else {
                screen.append(padPanel(""));
            }

            screen.append('\n');
        }

        // 화면 아래 잔여 내용 지움
        screen.append("\033[J");

        System.out.print(screen);
        System.out.flush();
    }
}
