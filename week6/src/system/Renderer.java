package system;

/// <summary>
/// 게임 화면을 콘솔에 출력하는 클래스
/// 맵의 모든 타일을 문자로 조립하여 한번에 출력
/// </summary>
public class Renderer {

    /// <summary>
    /// 출력할 게임 맵
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 지정한 맵으로 렌더러 생성
    /// </summary>
    public Renderer(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /// <summary>
    /// 맵 전체를 화면에 출력
    /// 화면을 지우고, 모든 타일을 문자열로 조립한 뒤 한번에 출력
    /// </summary>
    public void render() {
        Util.clearScreen();

        // 전체 화면을 한번에 조립 (120칸 x 40줄 + 줄바꿈)
        StringBuilder screen = new StringBuilder(GameMap.WIDTH * GameMap.HEIGHT + GameMap.HEIGHT);

        for (int row = 0; row < GameMap.HEIGHT; row++) {
            for (int col = 0; col < GameMap.WIDTH; col++) {
                Tile tile = gameMap.getTile(row, col);

                // 이동 가능한 타일은 빈칸, 불가능한 타일은 #으로 표시
                if (tile.isWalkable()) {
                    screen.append(' ');
                } else {
                    screen.append('#');
                }
            }
            screen.append('\n');
        }

        System.out.print(screen);
        System.out.flush();
    }
}
