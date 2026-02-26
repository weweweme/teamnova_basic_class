import system.Colonist;
import system.Cursor;
import system.GameMap;
import system.MapGenerator;
import system.MovingState;
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
        Util.clearScreen();

        // 정착민 스레드 시작
        chulsoo.start();
        younghee.start();
        minsoo.start();

        // 렌더링 간격 (밀리초)
        final int RENDER_INTERVAL = 100;
        // 입력 체크 간격 (밀리초, 짧을수록 입력이 즉각적)
        final int INPUT_CHECK_INTERVAL = 16;
        // 이동 명령 키
        final int KEY_MOVE = '1';

        long lastRenderTime = 0;

        // 메인 루프
        boolean running = true;
        while (running) {
            // 입력 체크 (매 루프마다, 약 16ms 간격)
            // 주의: System.in.available()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                if (System.in.available() > 0) {
                    int key = Util.readKey();

                    if (renderer.isCursorMode()) {
                        // 커서 모드
                        switch (key) {
                            case Util.KEY_UP:
                            case Util.KEY_DOWN:
                            case Util.KEY_LEFT:
                            case Util.KEY_RIGHT:
                                cursor.move(key);
                                break;
                            case Util.KEY_ENTER:
                                // 목표 위치 확정 → 선택된 정착민에게 이동 명령
                                Colonist selected = gameMap.getColonists().get(renderer.getSelectedIndex());
                                int targetRow = cursor.getPosition().getRow();
                                int targetCol = cursor.getPosition().getCol();
                                selected.changeState(new MovingState(new Position(targetRow, targetCol)));
                                renderer.setCursorMode(false);
                                break;
                            case Util.KEY_QUIT:
                                // 커서 모드 취소 → 시뮬레이션 모드로 복귀
                                renderer.setCursorMode(false);
                                break;
                        }
                    } else {
                        // 시뮬레이션 모드
                        switch (key) {
                            case Util.KEY_QUIT:
                                running = false;
                                break;
                            case Util.KEY_UP:
                                renderer.selectPrevious();
                                break;
                            case Util.KEY_DOWN:
                                renderer.selectNext();
                                break;
                            case KEY_MOVE:
                                // 이동 명령 → 커서 모드로 전환
                                renderer.setCursorMode(true);
                                break;
                        }
                    }

                    // 입력 즉시 화면 갱신
                    renderer.render();
                    lastRenderTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
                // IOException (컴파일러 요구사항)
            }

            // 일정 간격마다 화면 갱신 (정착민 이동 반영)
            long now = System.currentTimeMillis();
            if (now - lastRenderTime >= RENDER_INTERVAL) {
                renderer.render();
                lastRenderTime = now;
            }

            Util.delay(INPUT_CHECK_INTERVAL);
        }

        // 정착민 스레드 종료
        for (Colonist colonist : gameMap.getColonists()) {
            colonist.stopRunning();
        }

        Util.disableRawMode();
    }
}
