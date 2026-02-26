import system.Building;
import system.BuildingState;
import system.BuildingType;
import system.Colonist;
import system.Cursor;
import system.DayNightCycle;
import system.GameMap;
import system.GatheringState;
import system.MapGenerator;
import system.MovingState;
import system.Position;
import system.Renderer;
import system.Resource;
import system.RestingState;
import system.Supply;
import system.Util;

/// <summary>
/// 게임 진입점
/// 타이틀 화면 → 게임 초기화 → 입력 루프 → 통계 출력
/// </summary>
public class Main {

    public static void main(String[] args) {
        Util.enableRawMode();

        // ===== 타이틀 화면 =====
        StringBuilder title = new StringBuilder();
        title.append("\033[H\033[2J");
        title.append("\n\n\n\n\n\n");
        title.append("             ╔═════════════════════════════════╗\n");
        title.append("             ║                                 ║\n");
        title.append("             ║        표  류  자  들           ║\n");
        title.append("             ║       - The Castaways -         ║\n");
        title.append("             ║                                 ║\n");
        title.append("             ╚═════════════════════════════════╝\n");
        title.append("\n");
        title.append("         낯선 행성에 불시착한 생존자들의 식민지 서바이벌\n");
        title.append("\n\n");
        title.append("              [조작법]\n");
        title.append("               ↑↓ : 정착민 선택\n");
        title.append("               1  : 이동       2  : 채집\n");
        title.append("               3  : 휴식       4  : 건설\n");
        title.append("               q  : 종료\n");
        title.append("\n\n");
        title.append("              1번을 눌러 시작하세요...\n");
        System.out.print(title);
        System.out.flush();

        // 1번 키 입력 대기 (다른 키는 무시)
        // 실행 시 버퍼에 남은 Enter 등이 있어도 '1'이 아니면 무시됨
        final int KEY_START = '1';
        int startKey;
        do {
            startKey = Util.readKey();
        } while (startKey != KEY_START);

        // ===== 게임 초기화 =====
        GameMap gameMap = new GameMap();
        MapGenerator mapGenerator = new MapGenerator(gameMap);
        Cursor cursor = new Cursor();
        Renderer renderer = new Renderer(gameMap, cursor);

        // 맵에 자원 랜덤 배치
        mapGenerator.generate();

        // 테스트용 초기 물자
        Supply initialSupply = gameMap.getSupply();
        for (int i = 0; i < 30; i++) {
            initialSupply.add(system.ResourceType.TREE);
            initialSupply.add(system.ResourceType.ROCK);
        }
        for (int i = 0; i < 15; i++) {
            initialSupply.add(system.ResourceType.FOOD);
            initialSupply.add(system.ResourceType.IRON);
        }

        // 정착민 3명 배치 및 맵에 등록
        Colonist chulsoo = new Colonist("김철수", new Position(15, 55), gameMap);
        Colonist younghee = new Colonist("이영희", new Position(15, 60), gameMap);
        Colonist minsoo = new Colonist("박민수", new Position(15, 65), gameMap);
        gameMap.addColonist(chulsoo);
        gameMap.addColonist(younghee);
        gameMap.addColonist(minsoo);

        // 낮/밤 주기 생성 및 렌더러에 연결
        DayNightCycle dayNightCycle = new DayNightCycle(gameMap, mapGenerator);
        renderer.setDayNightCycle(dayNightCycle);

        Util.clearScreen();

        // 낮/밤 주기 스레드 시작
        dayNightCycle.start();

        // 정착민 스레드 시작
        chulsoo.start();
        younghee.start();
        minsoo.start();

        // 렌더링 간격 (밀리초)
        final int RENDER_INTERVAL = 100;
        // 입력 체크 간격 (밀리초, 짧을수록 입력이 즉각적)
        final int INPUT_CHECK_INTERVAL = 16;
        // 명령 키
        final int KEY_MOVE = '1';
        final int KEY_GATHER = '2';
        final int KEY_REST = '3';
        final int KEY_BUILD = '4';

        // 건물 선택 키
        final int KEY_BUILD_WALL = 'a';
        final int KEY_BUILD_STORAGE = 'b';
        final int KEY_BUILD_BEDROOM = 'c';
        final int KEY_BUILD_TOWER = 'd';

        /// <summary>
        /// 커서 모드에 진입하게 만든 명령 키 (이동인지 채집인지 구분)
        /// </summary>
        int pendingCommand = 0;

        /// <summary>
        /// 건설할 건물 종류 (건물 선택 후 커서 모드에서 사용)
        /// </summary>
        BuildingType pendingBuildingType = null;

        /// <summary>
        /// 건물 선택 모드 여부 (true면 a/b/c로 건물 종류 선택 대기)
        /// </summary>
        boolean buildSelectMode = false;

        long lastRenderTime = 0;

        // ===== 메인 루프 =====
        boolean running = true;
        while (running) {
            // 입력 체크 (매 루프마다, 약 16ms 간격)
            // 주의: System.in.available()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                if (System.in.available() > 0) {
                    int key = Util.readKey();

                    // 게임오버 시 q키만 허용
                    if (renderer.isGameOver()) {
                        if (key == Util.KEY_QUIT) {
                            running = false;
                        }
                    } else if (buildSelectMode) {
                        // 건물 선택 모드 — a/b/c로 건물 종류 선택
                        BuildingType selectedType = null;
                        switch (key) {
                            case KEY_BUILD_WALL:
                                selectedType = BuildingType.WALL;
                                break;
                            case KEY_BUILD_STORAGE:
                                selectedType = BuildingType.STORAGE;
                                break;
                            case KEY_BUILD_BEDROOM:
                                selectedType = BuildingType.BEDROOM;
                                break;
                            case KEY_BUILD_TOWER:
                                selectedType = BuildingType.TOWER;
                                break;
                            case Util.KEY_QUIT:
                                buildSelectMode = false;
                                renderer.setBuildSelectMode(false);
                                break;
                        }

                        if (selectedType != null) {
                            // 자원이 충분한지 확인
                            Supply supply = gameMap.getSupply();
                            if (supply.canAfford(selectedType)) {
                                pendingBuildingType = selectedType;
                                pendingCommand = KEY_BUILD;
                                buildSelectMode = false;
                                renderer.setBuildSelectMode(false);
                                renderer.setCursorModeLabel("건설 위치 지정");
                                renderer.setCursorMode(true);
                            }
                            // 자원 부족하면 무시 (선택 모드 유지)
                        }
                    } else if (renderer.isCursorMode()) {
                        // 커서 모드
                        switch (key) {
                            case Util.KEY_UP:
                            case Util.KEY_DOWN:
                            case Util.KEY_LEFT:
                            case Util.KEY_RIGHT:
                                cursor.move(key);
                                break;
                            case Util.KEY_ENTER:
                                // 목표 위치 확정 → 명령 종류에 따라 처리
                                Colonist selected = gameMap.getColonists().get(renderer.getSelectedIndex());
                                int targetRow = cursor.getPosition().getRow();
                                int targetCol = cursor.getPosition().getCol();

                                if (pendingCommand == KEY_MOVE) {
                                    // 이동 명령
                                    selected.changeState(new MovingState(new Position(targetRow, targetCol)));
                                    renderer.setCursorMode(false);
                                } else if (pendingCommand == KEY_GATHER) {
                                    // 채집 명령 — 커서 위치에 자원이 있어야 함
                                    Resource resource = gameMap.findResourceAt(targetRow, targetCol);
                                    if (resource != null) {
                                        selected.changeState(new GatheringState(resource));
                                        renderer.setCursorMode(false);
                                    }
                                    // 자원이 없으면 무시 (커서 모드 유지)
                                } else if (pendingCommand == KEY_BUILD) {
                                    // 건설 명령 — 자원 차감 후 건설 상태로 전환
                                    Supply supply = gameMap.getSupply();
                                    if (supply.spend(pendingBuildingType)) {
                                        Position buildPos = new Position(targetRow, targetCol);
                                        selected.changeState(new BuildingState(pendingBuildingType, buildPos));
                                        renderer.setCursorMode(false);
                                    }
                                }
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
                                pendingCommand = KEY_MOVE;
                                renderer.setCursorModeLabel("이동 위치 지정");
                                renderer.setCursorMode(true);
                                break;
                            case KEY_GATHER:
                                // 채집 명령 → 커서 모드로 전환 (자원 선택)
                                pendingCommand = KEY_GATHER;
                                renderer.setCursorModeLabel("채집 대상 지정");
                                renderer.setCursorMode(true);
                                break;
                            case KEY_REST:
                                // 휴식 명령 → 즉시 휴식 상태로 전환
                                Colonist restTarget = gameMap.getColonists().get(renderer.getSelectedIndex());
                                restTarget.changeState(new RestingState());
                                break;
                            case KEY_BUILD:
                                // 건설 명령 → 건물 선택 모드로 전환
                                buildSelectMode = true;
                                renderer.setBuildSelectMode(true);
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

        // ===== 종료 =====
        dayNightCycle.stopRunning();
        gameMap.clearEnemies();
        for (Colonist colonist : gameMap.getColonists()) {
            colonist.stopRunning();
        }

        Util.disableRawMode();
        Util.clearScreen();

        // 게임 통계 출력
        int survivors = 0;
        for (Colonist colonist : gameMap.getColonists()) {
            if (colonist.isLiving()) {
                survivors++;
            }
        }

        System.out.println("=== 게임 통계 ===");
        System.out.println("생존 일수: " + dayNightCycle.getDay() + "일");
        System.out.println("생존자: " + survivors + "/" + gameMap.getColonists().size() + "명");
        System.out.println("처치한 적: " + gameMap.getEnemiesKilled() + "마리");
        System.out.println("건설한 건물: " + gameMap.getBuildings().size() + "채");
        System.out.println("채집 횟수: " + gameMap.getResourcesGathered() + "회");
    }
}
