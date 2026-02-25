package system;

/// <summary>
/// 게임 화면을 콘솔에 출력하는 클래스
/// 화면 버퍼에 타일과 오브젝트를 그린 뒤 한번에 출력
/// 오브젝트는 4x2 블록(가로 4문자 x 세로 2줄)으로 렌더링
/// </summary>
public class Renderer {

    /// <summary>
    /// 출력할 게임 맵
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 화면 버퍼 [행][열], 매 프레임마다 새로 채움
    /// </summary>
    private final char[][] buffer;

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
    /// 지정한 맵으로 렌더러 생성
    /// </summary>
    public Renderer(GameMap gameMap) {
        this.gameMap = gameMap;
        this.buffer = new char[GameMap.HEIGHT][GameMap.WIDTH];
    }

    /// <summary>
    /// 맵 전체를 화면에 출력
    /// 버퍼를 초기화하고, 오브젝트를 그린 뒤 한번에 출력
    /// </summary>
    public void render() {
        clearBuffer();
        drawResources();
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
    /// 버퍼의 내용을 화면에 한번에 출력
    /// </summary>
    private void flush() {
        Util.clearScreen();

        StringBuilder screen = new StringBuilder(GameMap.WIDTH * GameMap.HEIGHT + GameMap.HEIGHT);

        for (int row = 0; row < GameMap.HEIGHT; row++) {
            for (int col = 0; col < GameMap.WIDTH; col++) {
                screen.append(buffer[row][col]);
            }
            screen.append('\n');
        }

        System.out.print(screen);
        System.out.flush();
    }
}
