import system.Colonist;
import system.Cursor;
import system.GameMap;
import system.MapGenerator;
import system.Position;
import system.Renderer;
import system.Util;

/// <summary>
/// 게임 진입점
/// 맵 생성, 렌더링, 입력 루프 담당
/// </summary>
public class Main {

    public static void main(String[] args) {
        GameMap gameMap = new GameMap();
        MapGenerator mapGenerator = new MapGenerator(gameMap);
        Cursor cursor = new Cursor();
        Renderer renderer = new Renderer(gameMap, cursor);

        // 맵에 자원 랜덤 배치
        mapGenerator.generate();

        // 테스트용 정착민 3명 배치
        gameMap.addColonist(new Colonist("김철수", new Position(20, 55)));
        gameMap.addColonist(new Colonist("이영희", new Position(20, 60)));
        gameMap.addColonist(new Colonist("박민수", new Position(20, 65)));

        Util.enableRawMode();
        renderer.render();

        // 입력 루프: 화살표로 커서 이동, q로 종료
        while (true) {
            int key = Util.readKey();

            if (key == Util.KEY_QUIT) {
                break;
            }

            cursor.move(key);
            renderer.render();
        }

        Util.disableRawMode();
    }
}
