package system;

/// <summary>
/// 맵에 자원을 배치하는 클래스
/// 초기 맵 생성 시 배치, 시간 경과 시 자원 재생성 담당
/// 광산(ORE)은 맵 가장자리에 고정 배치, 나머지는 랜덤 배치
/// </summary>
public class MapGenerator {

    /// <summary>
    /// 초기 열매 덤불 배치 수
    /// </summary>
    private static final int INITIAL_FOOD_COUNT = 4;

    /// <summary>
    /// 초기 자재 더미 배치 수
    /// </summary>
    private static final int INITIAL_MATERIAL_COUNT = 6;

    /// <summary>
    /// 초기 광산 배치 수 (맵 4모서리에 고정)
    /// </summary>
    private static final int INITIAL_ORE_COUNT = 4;

    /// <summary>
    /// 매 낮마다 종류별 최대 재생성 수
    /// </summary>
    private static final int RESPAWN_PER_DAY = 2;

    /// <summary>
    /// 자원 블록의 가로 크기
    /// </summary>
    private static final int BLOCK_WIDTH = 10;

    /// <summary>
    /// 자원 블록의 세로 크기
    /// </summary>
    private static final int BLOCK_HEIGHT = 6;

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
    /// 초기 맵에 자원을 배치
    /// 광산을 먼저 고정 배치한 뒤, 식량과 자재를 랜덤 배치
    /// </summary>
    public void generate() {
        placeOreMines();
        placeResources(ResourceType.FOOD, INITIAL_FOOD_COUNT);
        placeResources(ResourceType.MATERIAL, INITIAL_MATERIAL_COUNT);
    }

    /// <summary>
    /// 맵 가장자리 고정 위치에 광산(ORE) 배치
    /// 광산은 영구 자원으로, 한번 배치되면 사라지지 않음
    /// </summary>
    private void placeOreMines() {
        // 4개 모서리 근처에 배치 (블록 크기를 고려하여 여백 확보)
        int[][] positions = {
            {2, 5},
            {2, GameMap.WIDTH - BLOCK_WIDTH - 5},
            {GameMap.HEIGHT - BLOCK_HEIGHT - 2, 5},
            {GameMap.HEIGHT - BLOCK_HEIGHT - 2, GameMap.WIDTH - BLOCK_WIDTH - 5}
        };

        for (int[] pos : positions) {
            gameMap.addResource(new Resource(new Position(pos[0], pos[1]), ResourceType.ORE));
        }
    }

    /// <summary>
    /// 지정한 종류의 자원을 지정한 수만큼 랜덤 위치에 배치
    /// 다른 자원의 블록과 겹치지 않는 위치에만 배치
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
    /// 식량과 자재만 재생성 (광산은 영구 자원이므로 재생성 불필요)
    /// </summary>
    public void respawnResources() {
        respawnIfNeeded(ResourceType.FOOD, INITIAL_FOOD_COUNT);
        respawnIfNeeded(ResourceType.MATERIAL, INITIAL_MATERIAL_COUNT);
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
    /// 지정한 위치의 블록 영역이 다른 자원과 겹치지 않는지 확인
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
