package system;

/// <summary>
/// 맵에 자원을 배치하는 클래스
/// 초기 맵 생성 시 랜덤 배치, 시간 경과 시 자원 재생성 담당
/// </summary>
public class MapGenerator {

    /// <summary>
    /// 초기 열매 덤불 배치 수
    /// </summary>
    private static final int INITIAL_FOOD_COUNT = 12;

    /// <summary>
    /// 초기 나무 배치 수
    /// </summary>
    private static final int INITIAL_TREE_COUNT = 15;

    /// <summary>
    /// 초기 바위 배치 수
    /// </summary>
    private static final int INITIAL_ROCK_COUNT = 10;

    /// <summary>
    /// 초기 철광석 배치 수
    /// </summary>
    private static final int INITIAL_IRON_COUNT = 5;

    /// <summary>
    /// 매 낮마다 종류별 최대 재생성 수
    /// </summary>
    private static final int RESPAWN_PER_DAY = 2;

    /// <summary>
    /// 4x2 블록의 가로 크기
    /// </summary>
    private static final int BLOCK_WIDTH = 4;

    /// <summary>
    /// 4x2 블록의 세로 크기
    /// </summary>
    private static final int BLOCK_HEIGHT = 2;

    /// <summary>
    /// 자원을 배치할 맵
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 지정한 맵으로 생성기 생성
    /// </summary>
    public MapGenerator(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /// <summary>
    /// 초기 맵에 자원을 랜덤 배치
    /// </summary>
    public void generate() {
        placeResources(ResourceType.FOOD, INITIAL_FOOD_COUNT);
        placeResources(ResourceType.TREE, INITIAL_TREE_COUNT);
        placeResources(ResourceType.ROCK, INITIAL_ROCK_COUNT);
        placeResources(ResourceType.IRON, INITIAL_IRON_COUNT);
    }

    /// <summary>
    /// 지정한 종류의 자원을 지정한 수만큼 랜덤 위치에 배치
    /// 다른 자원의 4x2 블록과 겹치지 않는 위치에만 배치
    /// </summary>
    private void placeResources(ResourceType type, int count) {
        // 배치 시도 최대 횟수 (무한 루프 방지)
        int maxAttempts = count * 20;
        int placed = 0;
        int attempts = 0;

        while (placed < count && attempts < maxAttempts) {
            // 블록이 맵 안에 들어오도록 범위 제한
            int row = Util.rand(GameMap.HEIGHT - BLOCK_HEIGHT);
            int col = Util.rand(GameMap.WIDTH - BLOCK_WIDTH);

            if (isAreaEmpty(row, col)) {
                gameMap.addResource(new Resource(new Position(row, col), type));
                placed++;
            }

            attempts++;
        }
    }

    /// <summary>
    /// 빈 랜덤 위치에 자원 하나를 재생성
    /// 채집으로 사라진 자원을 보충할 때 사용
    /// 부품(IRON과 별도)은 재생 안 되므로 호출 시 종류를 지정
    /// </summary>
    public void respawnResource(ResourceType type) {
        // 배치 시도 최대 횟수
        int maxAttempts = 100;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int row = Util.rand(GameMap.HEIGHT - BLOCK_HEIGHT);
            int col = Util.rand(GameMap.WIDTH - BLOCK_WIDTH);

            if (isAreaEmpty(row, col)) {
                gameMap.addResource(new Resource(new Position(row, col), type));
                return;
            }
        }
    }

    /// <summary>
    /// 낮이 시작될 때 부족한 자원을 보충
    /// 각 종류별로 초기 배치 수 대비 부족한 만큼 최대 2개씩 재생성
    /// </summary>
    public void respawnResources() {
        respawnIfNeeded(ResourceType.FOOD, INITIAL_FOOD_COUNT);
        respawnIfNeeded(ResourceType.TREE, INITIAL_TREE_COUNT);
        respawnIfNeeded(ResourceType.ROCK, INITIAL_ROCK_COUNT);
        respawnIfNeeded(ResourceType.IRON, INITIAL_IRON_COUNT);
    }

    /// <summary>
    /// 한 종류의 자원이 초기 배치 수보다 적으면 최대 2개까지 재생성
    /// </summary>
    private void respawnIfNeeded(ResourceType type, int initialCount) {
        int currentCount = countResources(type);
        int deficit = initialCount - currentCount;

        // 부족분과 매일 최대 재생량 중 작은 값만큼 보충
        int toRespawn = Math.min(deficit, RESPAWN_PER_DAY);

        for (int i = 0; i < toRespawn; i++) {
            respawnResource(type);
        }
    }

    /// <summary>
    /// 맵에 있는 특정 종류의 자원 수를 셈
    /// </summary>
    private int countResources(ResourceType type) {
        int count = 0;
        for (Resource resource : gameMap.getResources()) {
            if (resource.getType() == type) {
                count++;
            }
        }
        return count;
    }

    /// <summary>
    /// 지정한 위치의 4x2 영역이 다른 자원과 겹치지 않는지 확인
    /// </summary>
    private boolean isAreaEmpty(int row, int col) {
        for (Resource existing : gameMap.getResources()) {
            int existRow = existing.getPosition().getRow();
            int existCol = existing.getPosition().getCol();

            // 두 블록의 행/열이 겹치는지 확인
            boolean rowOverlap = row < existRow + BLOCK_HEIGHT && row + BLOCK_HEIGHT > existRow;
            boolean colOverlap = col < existCol + BLOCK_WIDTH && col + BLOCK_WIDTH > existCol;

            if (rowOverlap && colOverlap) {
                return false;
            }
        }
        return true;
    }
}
