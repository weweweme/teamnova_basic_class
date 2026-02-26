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

        // 정착민 3명 배치 및 맵에 등록
        Colonist chulsoo = new Colonist("김철수", new Position(20, 55), gameMap);
        Colonist younghee = new Colonist("이영희", new Position(20, 60), gameMap);
        Colonist minsoo = new Colonist("박민수", new Position(20, 65), gameMap);
        gameMap.addColonist(chulsoo);
        gameMap.addColonist(younghee);
        gameMap.addColonist(minsoo);

        Util.enableRawMode();

        // 정착민 스레드 시작
        chulsoo.start();
        younghee.start();
        minsoo.start();

        // 입력 루프
        while (true) {
            renderer.render();

            // 입력이 있을 때만 처리 (없으면 렌더링만 반복)
            // 주의: System.in.available()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                if (System.in.available() > 0) {
                    int key = Util.readKey();

                    if (key == Util.KEY_QUIT) {
                        break;
                    }

                    if (renderer.isCursorMode()) {
                        // 커서 모드: 화살표로 커서 이동
                        cursor.move(key);
                    } else {
                        // 시뮬레이션 모드: ↑↓로 정착민 선택
                        switch (key) {
                            case Util.KEY_UP:
                                renderer.selectPrevious();
                                break;
                            case Util.KEY_DOWN:
                                renderer.selectNext();
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                // IOException (컴파일러 요구사항)
            }

            // 렌더링 주기 (100ms)
            Util.delay(100);
        }

        // 정착민 스레드 종료
        for (Colonist colonist : gameMap.getColonists()) {
            colonist.stopRunning();
        }

        Util.disableRawMode();
    }
}
