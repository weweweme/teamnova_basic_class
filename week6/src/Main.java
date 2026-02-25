import system.GameMap;
import system.Position;
import system.Renderer;
import system.Resource;
import system.ResourceType;
import system.Util;

/// <summary>
/// 게임 진입점
/// 맵 생성, 자원 배치, 렌더링, 입력 루프 담당
/// </summary>
public class Main {

    public static void main(String[] args) {
        GameMap gameMap = new GameMap();

        // 테스트용 자원 배치
        gameMap.addResource(new Resource(new Position(5, 10), ResourceType.TREE));
        gameMap.addResource(new Resource(new Position(5, 30), ResourceType.TREE));
        gameMap.addResource(new Resource(new Position(10, 20), ResourceType.ROCK));
        gameMap.addResource(new Resource(new Position(10, 50), ResourceType.ROCK));
        gameMap.addResource(new Resource(new Position(15, 40), ResourceType.IRON));
        gameMap.addResource(new Resource(new Position(15, 70), ResourceType.IRON));
        gameMap.addResource(new Resource(new Position(20, 60), ResourceType.TREE));
        gameMap.addResource(new Resource(new Position(25, 80), ResourceType.ROCK));

        Renderer renderer = new Renderer(gameMap);

        Util.enableRawMode();
        renderer.render();

        // q 키를 누르면 종료
        while (true) {
            int key = Util.readKey();
            if (key == Util.KEY_QUIT) {
                break;
            }
        }

        Util.disableRawMode();
    }
}
