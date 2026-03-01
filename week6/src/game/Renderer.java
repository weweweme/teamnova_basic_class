package game;

import gun.Bullet;
import entity.colonist.Colonist;
import entity.enemy.Enemy;
import structure.AmmoBox;
import structure.Barricade;
import game.GameMap.HitEffect;
import structure.Landmine;
import structure.Spike;

import java.util.ArrayList;
import java.util.Arrays;

/// <summary>
/// 게임 화면을 콘솔에 출력하는 클래스
/// 맵 버퍼(좌측) + 우측 패널 + 하단 로그를 합쳐서 한번에 출력
/// </summary>
public class Renderer {

    /// <summary>
    /// 우측 패널 가로 크기 (구분선 제외)
    /// </summary>
    private final int PANEL_WIDTH = 22;

    /// <summary>
    /// 사망 애니메이션 1단계 시간 (짙은 회색 정지)
    /// </summary>
    private final int DEATH_PHASE1_MS = 400;

    /// <summary>
    /// 사망 애니메이션 전체 시간 (1단계 정지 + 2단계 분해)
    /// </summary>
    private final int DEATH_ANIM_MS = 800;

    /// <summary>
    /// 빨간색 ANSI 색상 코드
    /// </summary>
    private final int COLOR_RED = 31;

    /// <summary>
    /// 초록색 ANSI 색상 코드
    /// </summary>
    private final int COLOR_GREEN = 32;

    /// <summary>
    /// 짙은 회색 ANSI 색상 코드
    /// </summary>
    private final int COLOR_DARK_GRAY = 90;

    /// <summary>
    /// 행별 색상 재사용 버퍼 (매 프레임 배열 재생성 방지)
    /// 맵 높이만큼 확보하면 모든 블록에 대응 가능
    /// </summary>
    private final int[] reusableColors = new int[GameMap.HEIGHT];

    /// <summary>
    /// 우측 패널 줄 재사용 버퍼
    /// </summary>
    private final ArrayList<String> panelLines = new ArrayList<>();

    /// <summary>
    /// 화면 출력 재사용 버퍼
    /// </summary>
    private final StringBuilder screenBuilder = new StringBuilder(8192);

    /// <summary>
    /// 체력바 조립 재사용 버퍼
    /// </summary>
    private final StringBuilder barBuilder = new StringBuilder();

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
    /// 무기 상점 모드 여부
    /// </summary>
    private boolean shopMode;

    /// <summary>
    /// 건설 모드 여부
    /// </summary>
    private boolean buildMode;

    /// <summary>
    /// 모집 모드 여부
    /// </summary>
    private boolean recruitMode;

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
    /// 무기 상점 모드 설정
    /// </summary>
    public void setShopMode(boolean shopMode) {
        this.shopMode = shopMode;
    }

    /// <summary>
    /// 건설 모드 설정
    /// </summary>
    public void setBuildMode(boolean buildMode) {
        this.buildMode = buildMode;
    }

    /// <summary>
    /// 모집 모드 설정
    /// </summary>
    public void setRecruitMode(boolean recruitMode) {
        this.recruitMode = recruitMode;
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

        if (isGameOver()) {
            drawGameOverScreen();
        } else {
            drawBarricade();
            drawSpikes();
            drawLandmines();
            drawAmmoBoxes();
            drawColonists();
            drawEnemies();
            drawBullets();
            drawEffects();
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
            color = COLOR_RED;
        } else if (barricade.isRecentlyRepaired()) {
            color = COLOR_GREEN;
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
        final int COLOR_YELLOW = 33;

        for (Spike spike : gameMap.getSpikes()) {
            if (spike.isDestroyed()) {
                continue;
            }
            int col = spike.getColumn();

            for (int row = 0; row < GameMap.HEIGHT; row++) {
                if (col >= 0 && col < GameMap.WIDTH) {
                    buffer[row][col] = '^';
                    colorBuffer[row][col] = COLOR_YELLOW;
                }
            }
        }
    }

    /// <summary>
    /// 설치된 지뢰를 세로로 그림 (@ 문자, 빨간색)
    /// </summary>
    private void drawLandmines() {
        for (Landmine mine : gameMap.getLandmines()) {
            if (mine.isDestroyed()) {
                continue;
            }
            int col = mine.getColumn();

            for (int row = 0; row < GameMap.HEIGHT; row++) {
                if (col >= 0 && col < GameMap.WIDTH) {
                    buffer[row][col] = '@';
                    colorBuffer[row][col] = COLOR_RED;
                }
            }
        }
    }

    /// <summary>
    /// 안전지대에 설치된 탄약 상자를 그림 (= 문자, 초록색)
    /// </summary>
    private void drawAmmoBoxes() {
        for (AmmoBox box : gameMap.getAmmoBoxes()) {
            if (box.isDestroyed()) {
                continue;
            }
            int col = box.getColumn();

            for (int row = 0; row < GameMap.HEIGHT; row++) {
                if (col >= 0 && col < GameMap.WIDTH) {
                    buffer[row][col] = '=';
                    colorBuffer[row][col] = COLOR_GREEN;
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
            int blockHeight = block.length;

            if (colonist.isLiving()) {
                drawBlock(row, col, block);
            } else {
                // 사망 애니메이션
                long elapsed = now - colonist.getDeathTime();

                if (elapsed < DEATH_PHASE1_MS) {
                    // Phase 1: 전체 짙은 회색으로 정지

                    Arrays.fill(reusableColors, 0, blockHeight, COLOR_DARK_GRAY);
                    drawColoredBlock(row, col, block, reusableColors, blockHeight);
                } else if (elapsed < DEATH_ANIM_MS) {
                    // Phase 2: 아래부터 한 줄씩 소멸
                    int phase2Duration = DEATH_ANIM_MS - DEATH_PHASE1_MS;
                    double progress = (double) (elapsed - DEATH_PHASE1_MS) / phase2Duration;
                    int removedRows = (int) Math.ceil(progress * blockHeight);
                    int visibleRows = blockHeight - removedRows;

                    if (visibleRows > 0) {

                        Arrays.fill(reusableColors, 0, visibleRows, COLOR_DARK_GRAY);
                        drawColoredBlock(row, col, block, reusableColors, visibleRows);
                    }
                }
                // Phase 3 (DEATH_ANIM_MS 이후): 아무것도 안 그림
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
            String[] block = enemy.getSpec().getBlock();
            int blockHeight = block.length;

            if (enemy.isLiving()) {
                // 살아있는 적: HP 비율에 따라 위에서부터 빨간색

                Arrays.fill(reusableColors, 0, blockHeight, 0);

                double hpRatio = (double) enemy.getHp() / enemy.getMaxHp();
                int redRows = (int) Math.ceil((1.0 - hpRatio) * blockHeight);
                for (int i = 0; i < redRows; i++) {
                    reusableColors[i] = COLOR_RED;
                }
                drawColoredBlock(row, col, block, reusableColors, blockHeight);
            } else {
                // 죽은 적: 사망 애니메이션
                long elapsed = now - enemy.getDeathTime();

                if (elapsed < DEATH_PHASE1_MS) {
                    // Phase 1: 전체 짙은 회색으로 정지

                    Arrays.fill(reusableColors, 0, blockHeight, COLOR_DARK_GRAY);
                    drawColoredBlock(row, col, block, reusableColors, blockHeight);
                } else if (elapsed < DEATH_ANIM_MS) {
                    // Phase 2: 아래부터 한 줄씩 소멸
                    int phase2Duration = DEATH_ANIM_MS - DEATH_PHASE1_MS;
                    double progress = (double) (elapsed - DEATH_PHASE1_MS) / phase2Duration;
                    int removedRows = (int) Math.ceil(progress * blockHeight);
                    int visibleRows = blockHeight - removedRows;

                    if (visibleRows > 0) {

                        Arrays.fill(reusableColors, 0, visibleRows, COLOR_DARK_GRAY);
                        drawColoredBlock(row, col, block, reusableColors, visibleRows);
                    }
                }
                // Phase 3 (DEATH_ANIM_MS 이후): 아무것도 안 그림 (제거 대기)
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
                buffer[row][col] = bullet.getBulletChar();
                if (bullet.getBulletColor() != 0) {
                    colorBuffer[row][col] = bullet.getBulletColor();
                }
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
                buffer[row][col] = effect.getEffectChar();
                colorBuffer[row][col] = effect.getEffectColor();
            }
        }
    }

    /// <summary>
    /// 게임오버 시 맵 영역에 ASCII 아트를 그림
    /// </summary>
    private void drawGameOverScreen() {
        String[] art = {
            "  ████   ███  ██   ██ ██████ ",
            " ██     ██ ██ ███ ███ ██     ",
            " ██ ██ ██████ ██ █ ██ ████   ",
            " ██  █ ██  ██ ██   ██ ██     ",
            "  ████ ██  ██ ██   ██ ██████ ",
            "",
            "  ████  ██  ██ ██████ █████  ",
            " ██  ██ ██  ██ ██     ██  ██ ",
            " ██  ██ ██  ██ ████   █████  ",
            " ██  ██  ████  ██     ██  ██ ",
            "  ████    ██   ██████ ██  ██ "
        };

        // 세로 중앙 정렬
        int startRow = (GameMap.HEIGHT - art.length) / 2;
        // 가로 중앙 정렬
        int artWidth = art[0].length();
        int startCol = (GameMap.WIDTH - artWidth) / 2;

        for (int i = 0; i < art.length; i++) {
            int row = startRow + i;
            if (row < 0 || row >= GameMap.HEIGHT) {
                continue;
            }

            String line = art[i];
            for (int j = 0; j < line.length(); j++) {
                int col = startCol + j;
                if (col < 0 || col >= GameMap.WIDTH) {
                    continue;
                }

                char ch = line.charAt(j);
                if (ch != ' ') {
                    buffer[row][col] = ch;
                    colorBuffer[row][col] = COLOR_RED;
                }
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
    /// 지정한 위치에 블록의 앞쪽 rows행을 버퍼에 그리면서 행별 색상도 기록
    /// rowColors[i]가 0이 아니면 해당 행에 ANSI 색상 적용
    /// </summary>
    private void drawColoredBlock(int startRow, int startCol, String[] block, int[] rowColors, int rows) {
        for (int blockRow = 0; blockRow < rows; blockRow++) {
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
    private void buildPanel() {
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
            } else if (shopMode) {
                // 무기 상점 모드
                panelLines.add(" [무기 상점]");
                panelLines.add(" 1: 피스톨 (무료)");
                panelLines.add(" 2: 샷건  (보급25)");
                panelLines.add(" 3: 라이플 (보급20)");
                panelLines.add(" 4: 미니건 (보급30)");
                panelLines.add(" q: 취소");
            } else if (recruitMode) {
                // 모집 모드
                panelLines.add(" [모집] (보급40)");
                panelLines.add(" 1: 사격수 (속사)");
                panelLines.add(" 2: 저격수 (치명타)");
                panelLines.add(" 3: 돌격수 (넉백)");
                panelLines.add(" q: 취소");
            } else if (buildMode) {
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
    /// 패널 한 줄을 고정 폭에 맞춰 대상 버퍼에 직접 추가
    /// 짧으면 공백으로 채우고, 길면 잘라냄
    /// </summary>
    private void appendPadPanel(StringBuilder dest, String text) {
        if (text.length() >= PANEL_WIDTH) {
            dest.append(text, 0, PANEL_WIDTH);
        } else {
            dest.append(text);
            int padding = PANEL_WIDTH - text.length();
            for (int i = 0; i < padding; i++) {
                dest.append(' ');
            }
        }
    }

    /// <summary>
    /// 한글 등 전각 문자를 고려한 터미널 표시 폭 계산
    /// </summary>
    private int displayWidth(String text) {
        final char KOREAN_START = 0xAC00;
        final char KOREAN_END = 0xD7A3;
        final char CJK_START = 0x3000;
        final char CJK_END = 0x9FFF;

        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isKorean = c >= KOREAN_START && c <= KOREAN_END;
            boolean isCjk = c >= CJK_START && c <= CJK_END;
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
    private String padToWidth(String text) {
        int currentWidth = displayWidth(text);
        return text + " ".repeat(Math.max(0, GameMap.WIDTH - currentWidth));
    }

    /// <summary>
    /// 버퍼와 우측 패널을 합쳐서 화면에 한번에 출력
    /// 맵 영역 + 구분선 + 로그 영역 모두 우측 패널과 나란히 표시
    /// </summary>
    private void flush() {
        buildPanel();
        ArrayList<String> logs = gameMap.getRecentLogs();

        // 전체 높이: 맵(20) + 구분선(1) + 로그(8) = 29줄
        int LOG_LINES = 8;
        int totalHeight = GameMap.HEIGHT + 1 + LOG_LINES;
        String PANEL_SEPARATOR = "|||";

        screenBuilder.setLength(0);

        // 커서를 맨 위로 이동
        screenBuilder.append("\033[H");

        for (int row = 0; row < totalHeight; row++) {
            // 좌측 내용 (맵 / 구분선 / 로그)
            if (row < GameMap.HEIGHT) {
                // 맵 버퍼 (색상 적용)
                for (int col = 0; col < GameMap.WIDTH; col++) {
                    int color = colorBuffer[row][col];
                    if (color != 0) {
                        screenBuilder.append("\033[");
                        screenBuilder.append(color);
                        screenBuilder.append('m');
                        screenBuilder.append(buffer[row][col]);
                        screenBuilder.append("\033[0m");
                    } else {
                        screenBuilder.append(buffer[row][col]);
                    }
                }
            } else if (row == GameMap.HEIGHT) {
                // 로그 구분선
                screenBuilder.append("-".repeat(GameMap.WIDTH));
            } else {
                // 로그 줄 (하단 정렬: 새 로그가 아래, 기존 로그가 위로)
                int logLine = row - GameMap.HEIGHT - 1;
                int offset = LOG_LINES - logs.size();
                int logIndex = logLine - offset;

                String logContent = "";
                boolean hasLog = logIndex >= 0 && logIndex < logs.size();
                if (hasLog) {
                    logContent = " " + logs.get(logIndex);
                }
                screenBuilder.append(padToWidth(logContent));
            }

            // 우측 패널
            screenBuilder.append(PANEL_SEPARATOR);
            if (row < panelLines.size()) {
                appendPadPanel(screenBuilder, panelLines.get(row));
            } else {
                appendPadPanel(screenBuilder, "");
            }

            screenBuilder.append('\n');
        }

        // 화면 아래 잔여 내용 지움
        screenBuilder.append("\033[J");

        System.out.print(screenBuilder);
        System.out.flush();
    }
}
