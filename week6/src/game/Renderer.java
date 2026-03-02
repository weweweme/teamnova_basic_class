package game;

import gun.Bullet;
import unit.colonist.Colonist;
import unit.enemy.Enemy;
import structure.AmmoBox;
import structure.Barricade;
import game.HitEffect;
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
    /// 사망 애니메이션 1단계 시간 (짙은 회색 정지)
    /// </summary>
    private static final int DEATH_PHASE1_MS = 400;

    /// <summary>
    /// 사망 애니메이션 전체 시간 (1단계 정지 + 2단계 분해)
    /// </summary>
    private static final int DEATH_ANIM_MS = 800;

    /// <summary>
    /// 빨간색 ANSI 색상 코드
    /// </summary>
    private static final int COLOR_RED = 31;

    /// <summary>
    /// 초록색 ANSI 색상 코드
    /// </summary>
    private static final int COLOR_GREEN = 32;

    /// <summary>
    /// 짙은 회색 ANSI 색상 코드
    /// </summary>
    private static final int COLOR_DARK_GRAY = 90;

    /// <summary>
    /// 행별 색상 재사용 버퍼 (매 프레임 배열 재생성 방지)
    /// 맵 높이만큼 확보하면 모든 블록에 대응 가능
    /// </summary>
    private final int[] reusableColors = new int[GameWorld.HEIGHT];

    /// <summary>
    /// 화면 출력 재사용 버퍼
    /// </summary>
    private final StringBuilder screenBuilder = new StringBuilder(8192);

    /// <summary>
    /// 우측 패널 빌더
    /// </summary>
    private PanelBuilder panelBuilder;

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
    private final GameWorld gameWorld;

    /// <summary>
    /// 낮/밤 주기 (시간 표시용)
    /// </summary>
    private DayNightCycle dayNightCycle;

    /// <summary>
    /// 현재 선택된 정착민 번호
    /// </summary>
    private int selectedIndex;

    /// <summary>
    /// 입력 처리기 (메뉴 모드 상태 조회용)
    /// </summary>
    private InputHandler inputHandler;

    /// <summary>
    /// 지정한 맵으로 렌더러 생성
    /// </summary>
    public Renderer(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.buffer = new char[GameWorld.HEIGHT][GameWorld.WIDTH];
        this.colorBuffer = new int[GameWorld.HEIGHT][GameWorld.WIDTH];
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
    /// 입력 처리기 설정 및 패널 빌더 생성
    /// </summary>
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.panelBuilder = new PanelBuilder(gameWorld, inputHandler);
        this.panelBuilder.setDayNightCycle(dayNightCycle);
    }

    /// <summary>
    /// 다음 정착민 선택 (↓ 키)
    /// </summary>
    public void selectNext() {
        int count = gameWorld.getColonists().size();
        if (count > 0) {
            selectedIndex = (selectedIndex + 1) % count;
        }
    }

    /// <summary>
    /// 이전 정착민 선택 (↑ 키)
    /// </summary>
    public void selectPrevious() {
        int count = gameWorld.getColonists().size();
        if (count > 0) {
            selectedIndex = (selectedIndex - 1 + count) % count;
        }
    }

    /// <summary>
    /// 모든 정착민이 사망했는지 확인
    /// </summary>
    public boolean isGameOver() {
        for (Colonist colonist : gameWorld.getColonists()) {
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
    /// 매 프레임 호출되는 렌더링 진입점
    /// 1) 버퍼 초기화 → 2) 오브젝트 그리기 (뒤→앞 순서) → 3) flush로 화면 출력
    /// 게임오버 시에는 오브젝트 대신 GAME OVER 아스키 아트만 그림
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

        // 배치 모드 커서는 모든 오브젝트 위에 표시
        if (inputHandler != null && inputHandler.isPlacementMode()) {
            drawPlacementCursor();
        }

        flush();
    }

    /// <summary>
    /// char 버퍼를 공백, 색상 버퍼를 0(기본색)으로 초기화
    /// 매 프레임 시작 시 이전 프레임 잔상을 지움
    /// </summary>
    private void clearBuffer() {
        for (int row = 0; row < GameWorld.HEIGHT; row++) {
            for (int col = 0; col < GameWorld.WIDTH; col++) {
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
        Barricade barricade = gameWorld.getBarricade();
        boolean destroyed = barricade.isDestroyed();
        char wallChar = destroyed ? '.' : '#';

        // 파괴: 빨간색, 피격: 빨간색, 수리: 초록색, 평상시: 기본색
        int color = 0;
        if (destroyed || barricade.isRecentlyHit()) {
            color = COLOR_RED;
        } else if (barricade.isRecentlyRepaired()) {
            color = COLOR_GREEN;
        }

        for (int row = 0; row < GameWorld.HEIGHT; row++) {
            buffer[row][Barricade.COLUMN] = wallChar;
            buffer[row][Barricade.COLUMN + 1] = wallChar;
            colorBuffer[row][Barricade.COLUMN] = color;
            colorBuffer[row][Barricade.COLUMN + 1] = color;
        }
    }

    /// <summary>
    /// 설치된 가시덫을 해당 위치에 한 칸으로 그림 (^ 문자, 노란색)
    /// 파괴된 가시덫은 표시하지 않음
    /// </summary>
    private void drawSpikes() {
        final int COLOR_YELLOW = 33;

        for (Spike spike : gameWorld.getSpikes()) {
            if (spike.isDestroyed()) {
                continue;
            }
            int row = spike.getRow();
            int col = spike.getColumn();

            boolean validRow = row >= 0 && row < GameWorld.HEIGHT;
            boolean validCol = col >= 0 && col < GameWorld.WIDTH;
            if (validRow && validCol) {
                buffer[row][col] = '^';
                colorBuffer[row][col] = COLOR_YELLOW;
            }
        }
    }

    /// <summary>
    /// 설치된 지뢰를 해당 위치에 한 칸으로 그림 (@ 문자, 빨간색)
    /// </summary>
    private void drawLandmines() {
        for (Landmine mine : gameWorld.getLandmines()) {
            if (mine.isDestroyed()) {
                continue;
            }
            int row = mine.getRow();
            int col = mine.getColumn();

            boolean validRow = row >= 0 && row < GameWorld.HEIGHT;
            boolean validCol = col >= 0 && col < GameWorld.WIDTH;
            if (validRow && validCol) {
                buffer[row][col] = '@';
                colorBuffer[row][col] = COLOR_RED;
            }
        }
    }

    /// <summary>
    /// 안전지대에 설치된 탄약 상자를 해당 위치에 한 칸으로 그림 (= 문자, 초록색)
    /// </summary>
    private void drawAmmoBoxes() {
        for (AmmoBox box : gameWorld.getAmmoBoxes()) {
            if (box.isDestroyed()) {
                continue;
            }
            int row = box.getRow();
            int col = box.getColumn();

            boolean validRow = row >= 0 && row < GameWorld.HEIGHT;
            boolean validCol = col >= 0 && col < GameWorld.WIDTH;
            if (validRow && validCol) {
                buffer[row][col] = '=';
                colorBuffer[row][col] = COLOR_GREEN;
            }
        }
    }

    /// <summary>
    /// 배치 모드 커서를 버퍼에 그림
    /// 500ms 주기로 구조물 문자(^/@/=)와 커서(+)를 번갈아 표시
    /// </summary>
    private void drawPlacementCursor() {
        int row = inputHandler.getCursorRow();
        int col = inputHandler.getCursorCol();
        int type = inputHandler.getPlacementType();

        boolean validRow = row >= 0 && row < GameWorld.HEIGHT;
        boolean validCol = col >= 0 && col < GameWorld.WIDTH;
        if (!validRow || !validCol) {
            return;
        }

        // 깜빡임: 500ms 주기로 구조물 문자 ↔ 커서 '+' 전환
        final int BLINK_INTERVAL = 500;
        boolean showStructure = (System.currentTimeMillis() / BLINK_INTERVAL) % 2 == 0;

        // 밝은 흰색 (커서 표시용)
        final int COLOR_BRIGHT_WHITE = 97;
        // 구조물별 색상
        final int COLOR_YELLOW = 33;

        final int TYPE_SPIKE = 1;
        final int TYPE_LANDMINE = 2;
        final int TYPE_AMMOBOX = 3;

        if (showStructure) {
            // 구조물 미리보기 표시
            switch (type) {
                case TYPE_SPIKE:
                    buffer[row][col] = '^';
                    colorBuffer[row][col] = COLOR_YELLOW;
                    break;
                case TYPE_LANDMINE:
                    buffer[row][col] = '@';
                    colorBuffer[row][col] = COLOR_RED;
                    break;
                case TYPE_AMMOBOX:
                    buffer[row][col] = '=';
                    colorBuffer[row][col] = COLOR_GREEN;
                    break;
            }
        } else {
            // 십자 커서 표시
            buffer[row][col] = '+';
            colorBuffer[row][col] = COLOR_BRIGHT_WHITE;
        }
    }

    /// <summary>
    /// 모든 정착민을 버퍼에 그림
    /// 살아있으면 기본색으로 블록 출력
    /// 사망 시 3단계 애니메이션:
    ///   Phase 1 (0~400ms): 전체 짙은 회색으로 정지
    ///   Phase 2 (400~800ms): 아래부터 한 줄씩 소멸 (진행률에 비례)
    ///   Phase 3 (800ms~): 완전 소멸, 아무것도 안 그림
    /// </summary>
    private void drawColonists() {
        long now = System.currentTimeMillis();

        for (Colonist colonist : gameWorld.getColonists()) {
            int row = colonist.getPosition().getRow();
            int col = colonist.getPosition().getCol();
            String[] block = colonist.getBlock();
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
    /// 살아있는 적: HP 손실 비율만큼 위쪽 행부터 빨간색으로 채움
    ///   예) HP 60% → 블록 상단 40%가 빨강, 하단 60%는 기본색
    /// 죽은 적: 정착민과 동일한 3단계 사망 애니메이션
    /// </summary>
    private void drawEnemies() {
        long now = System.currentTimeMillis();

        for (Enemy enemy : gameWorld.getEnemies()) {
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
    /// 날아가는 총알을 버퍼에 그림
    /// 총알의 현재 위치는 Bullet.getRow()가 발사→조준 직선 보간으로 계산
    /// 무기별로 다른 문자와 색상 사용 (Bullet의 bulletChar, bulletColor)
    /// </summary>
    private void drawBullets() {
        for (Bullet bullet : gameWorld.getBullets()) {
            int row = bullet.getRow();
            int col = bullet.getCol();

            if (row >= 0 && row < GameWorld.HEIGHT && col >= 0 && col < GameWorld.WIDTH) {
                buffer[row][col] = bullet.getBulletChar();
                if (bullet.getBulletColor() != 0) {
                    colorBuffer[row][col] = bullet.getBulletColor();
                }
            }
        }
    }

    /// <summary>
    /// 일시적 이펙트를 버퍼에 그림 (200ms 지속)
    /// 일반 명중: 노란색 !, 치명타: 밝은 빨강 * 3칸, 폭발: 다이아몬드 형태
    /// GameWorld.getEffects()에서 만료되지 않은 이펙트만 반환
    /// </summary>
    private void drawEffects() {
        for (HitEffect effect : gameWorld.getEffects()) {
            int row = effect.getRow();
            int col = effect.getCol();

            if (row >= 0 && row < GameWorld.HEIGHT && col >= 0 && col < GameWorld.WIDTH) {
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
        int startRow = (GameWorld.HEIGHT - art.length) / 2;
        // 가로 중앙 정렬
        int artWidth = art[0].length();
        int startCol = (GameWorld.WIDTH - artWidth) / 2;

        for (int i = 0; i < art.length; i++) {
            int row = startRow + i;
            if (row < 0 || row >= GameWorld.HEIGHT) {
                continue;
            }

            String line = art[i];
            for (int j = 0; j < line.length(); j++) {
                int col = startCol + j;
                if (col < 0 || col >= GameWorld.WIDTH) {
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
    /// 웨이브 경고 행의 표시 폭 (가로 정렬 고정용)
    /// </summary>
    private static final int WAVE_WARNING_ROW = GameWorld.HEIGHT / 2;

    /// <summary>
    /// 웨이브 경고 텍스트를 ANSI 색상과 함께 출력 (버퍼 대신 직접 렌더링)
    /// 한글이 2칸 차지하므로, 버퍼를 거치지 않고 문자열로 직접 출력하여 폭 정렬 유지
    /// </summary>
    private void appendWaveWarningRow(StringBuilder sb) {
        String warning = ">> 적이 밀려온다! <<";

        // 좌측 패딩 (가로 중앙 정렬, 기존 displayWidth 재사용)
        int warningWidth = displayWidth(warning);
        int leftPad = (GameWorld.WIDTH - warningWidth) / 2;
        int rightPad = GameWorld.WIDTH - warningWidth - leftPad;

        for (int i = 0; i < leftPad; i++) {
            sb.append(' ');
        }

        // 밝은 노랑 (ANSI 93)
        sb.append("\033[93m");
        sb.append(warning);
        sb.append("\033[0m");

        for (int i = 0; i < rightPad; i++) {
            sb.append(' ');
        }
    }

    /// <summary>
    /// 지정한 위치에 블록(여러 줄 문자 배열)을 버퍼에 복사
    /// 맵 경계(0~WIDTH, 0~HEIGHT)를 벗어나는 문자는 자동으로 잘림
    /// 정착민/적의 외형 블록(String[])을 그릴 때 사용
    /// </summary>
    private void drawBlock(int startRow, int startCol, String[] block) {
        for (int blockRow = 0; blockRow < block.length; blockRow++) {
            int bufferRow = startRow + blockRow;

            if (bufferRow < 0 || bufferRow >= GameWorld.HEIGHT) {
                continue;
            }

            String line = block[blockRow];

            for (int blockCol = 0; blockCol < line.length(); blockCol++) {
                int bufferCol = startCol + blockCol;

                if (bufferCol < 0 || bufferCol >= GameWorld.WIDTH) {
                    continue;
                }

                buffer[bufferRow][bufferCol] = line.charAt(blockCol);
            }
        }
    }

    /// <summary>
    /// drawBlock의 색상 버전 — 블록의 앞쪽 rows행만 그리면서 행마다 다른 색상 적용
    /// rowColors[i]가 0이면 기본색, 0이 아니면 해당 행 전체에 ANSI 색상 코드 적용
    /// 적 HP 표시(빨강 그라데이션)와 사망 애니메이션(회색 부분 렌더링)에 사용
    /// </summary>
    private void drawColoredBlock(int startRow, int startCol, String[] block, int[] rowColors, int rows) {
        for (int blockRow = 0; blockRow < rows; blockRow++) {
            int bufferRow = startRow + blockRow;

            if (bufferRow < 0 || bufferRow >= GameWorld.HEIGHT) {
                continue;
            }

            String line = block[blockRow];
            int color = rowColors[blockRow];

            for (int blockCol = 0; blockCol < line.length(); blockCol++) {
                int bufferCol = startCol + blockCol;

                if (bufferCol < 0 || bufferCol >= GameWorld.WIDTH) {
                    continue;
                }

                buffer[bufferRow][bufferCol] = line.charAt(blockCol);
                colorBuffer[bufferRow][bufferCol] = color;
            }
        }
    }

    /// <summary>
    /// 패널 한 줄을 고정 폭에 맞춰 대상 버퍼에 직접 추가
    /// 짧으면 공백으로 채우고, 길면 잘라냄
    /// </summary>
    private void appendPadPanel(StringBuilder dest, String text) {

        // 우측 패널 가로 크기 (구분선 제외)
        final int PANEL_WIDTH = 22;
        if (text.length() >= PANEL_WIDTH) {
            dest.append(text, 0, PANEL_WIDTH);
        } else {
            dest.append(text);
            int padding = PANEL_WIDTH - text.length();
            dest.append(" ".repeat(padding));
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
        return text + " ".repeat(Math.max(0, GameWorld.WIDTH - currentWidth));
    }

    /// <summary>
    /// 최종 화면 조립 및 출력 — 모든 draw 완료 후 호출
    /// 1) 커서를 터미널 맨 위로 이동 (ANSI \033[H)
    /// 2) 행 순회하며 좌측(맵/구분선/로그) + 우측(패널)을 한 줄씩 조립
    ///    - 맵 행: 화면 흔들림 오프셋 적용, 색상 버퍼로 ANSI 코드 삽입
    ///    - 웨이브 경고 행: 버퍼 대신 한글 문자열 직접 삽입 (폭 정렬 유지)
    ///    - 구분선 행: '-' 반복
    ///    - 로그 행: 하단 정렬 (최신 로그가 아래쪽)
    /// 3) StringBuilder 하나로 전체 화면을 조립한 뒤 System.out.print로 일괄 출력
    ///    (행별 println 대신 한번에 출력하여 깜빡임 방지)
    /// </summary>
    private void flush() {
        panelBuilder.build(selectedIndex);
        ArrayList<String> panelLines = panelBuilder.getPanelLines();
        ArrayList<String> logs = gameWorld.getRecentLogs();

        // 전체 높이: 맵(20) + 구분선(1) + 로그(8) = 29줄
        int LOG_LINES = 8;
        int totalHeight = GameWorld.HEIGHT + 1 + LOG_LINES;
        String PANEL_SEPARATOR = "|||";

        screenBuilder.setLength(0);

        // 커서를 맨 위로 이동
        screenBuilder.append("\033[H");

        // 흔들림 오프셋 (행 루프 바깥에서 한 번만 조회)
        int shakeOffset = gameWorld.getScreenEffects().getScreenShakeOffset();
        int verticalShake = gameWorld.getScreenEffects().getVerticalShakeOffset();

        // 웨이브 경고 활성 여부 (행 루프 바깥에서 한 번만 조회)
        boolean waveWarning = gameWorld.getScreenEffects().isWaveWarningActive();

        for (int row = 0; row < totalHeight; row++) {
            // 좌측 내용 (맵 / 구분선 / 로그)
            if (row < GameWorld.HEIGHT) {
                // 웨이브 경고 행: 한글 포함 문자열을 버퍼 대신 직접 출력
                boolean isWarningRow = waveWarning && row == WAVE_WARNING_ROW;
                if (isWarningRow) {
                    appendWaveWarningRow(screenBuilder);
                } else {
                    // 오른쪽 이동: 앞에 빈 칸 추가
                    if (shakeOffset > 0) {
                        for (int s = 0; s < shakeOffset; s++) {
                            screenBuilder.append(' ');
                        }
                    }

                    // 수직 흔들림: 버퍼에서 읽을 소스 행을 이동 (맵 범위 내로 제한)
                    int sourceRow = row + verticalShake;
                    if (sourceRow < 0) {
                        sourceRow = 0;
                    } else if (sourceRow >= GameWorld.HEIGHT) {
                        sourceRow = GameWorld.HEIGHT - 1;
                    }

                    // 맵 버퍼 (색상 적용, 흔들림 시 끝부분 잘림)
                    int renderWidth = GameWorld.WIDTH - Math.abs(shakeOffset);
                    int startCol = shakeOffset < 0 ? -shakeOffset : 0;
                    for (int col = startCol; col < startCol + renderWidth; col++) {
                        int color = colorBuffer[sourceRow][col];
                        if (color != 0) {
                            screenBuilder.append("\033[");
                            screenBuilder.append(color);
                            screenBuilder.append('m');
                            screenBuilder.append(buffer[sourceRow][col]);
                            screenBuilder.append("\033[0m");
                        } else {
                            screenBuilder.append(buffer[sourceRow][col]);
                        }
                    }

                    // 왼쪽 이동: 뒤에 빈 칸 추가 (패널 구분선 정렬 유지)
                    if (shakeOffset < 0) {
                        for (int s = 0; s < -shakeOffset; s++) {
                            screenBuilder.append(' ');
                        }
                    }
                }
            } else if (row == GameWorld.HEIGHT) {
                // 로그 구분선
                screenBuilder.append("-".repeat(GameWorld.WIDTH));
            } else {
                // 로그 줄 (상단 정렬: 최신 로그가 위, 오래된 로그가 아래로 밀림)
                int logLine = row - GameWorld.HEIGHT - 1;

                // 최신 로그부터 역순으로 표시 (채팅 스타일)
                int logIndex = logs.size() - 1 - logLine;

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
