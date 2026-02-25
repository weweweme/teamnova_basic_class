import system.GameMap;
import system.Renderer;
import system.Util;

/// <summary>
/// 게임 진입점
/// 맵 생성, 렌더링, 입력 루프 담당
/// </summary>
public class Main {

    public static void main(String[] args) {
        GameMap gameMap = new GameMap();
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
