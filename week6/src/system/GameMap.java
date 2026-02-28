package system;

import java.util.ArrayList;

/// <summary>
/// 게임 맵 전체를 관리하는 클래스
/// 80x20 크기의 사이드뷰 맵으로, 왼쪽은 안전지대, 오른쪽은 전장
/// 정착민/적/로그 등 모든 게임 오브젝트를 보유
/// </summary>
public class GameMap {

    /// <summary>
    /// 맵 가로 크기 (문자 단위)
    /// </summary>
    public static final int WIDTH = 80;

    /// <summary>
    /// 맵 세로 크기 (문자 단위)
    /// </summary>
    public static final int HEIGHT = 20;

    /// <summary>
    /// 정착민 목록
    /// </summary>
    private final ArrayList<Colonist> colonists = new ArrayList<>();

    /// <summary>
    /// 식민지 공유 보급품
    /// </summary>
    private final Supply supply = new Supply();

    /// <summary>
    /// 안전지대와 전장을 나누는 바리케이드
    /// </summary>
    private final Barricade barricade = new Barricade();

    /// <summary>
    /// 현재 맵에 있는 적 목록
    /// </summary>
    private final ArrayList<Enemy> enemies = new ArrayList<>();

    /// <summary>
    /// 최대 로그 보관 수
    /// </summary>
    private static final int LOG_CAPACITY = 8;

    /// <summary>
    /// 게임 로그 메시지 목록 (오래된 순서)
    /// </summary>
    private final ArrayList<String> logs = new ArrayList<>();

    /// <summary>
    /// 처치한 적 수
    /// </summary>
    private int enemiesKilled;

    /// <summary>
    /// 맵 생성
    /// </summary>
    public GameMap() {
        this.enemiesKilled = 0;
    }

    /// <summary>
    /// 지정한 좌표가 이동 가능한지 확인 (맵 범위 안이고 지면 아래가 아님)
    /// </summary>
    public boolean isWalkable(int row, int col) {
        boolean validRow = row >= 0 && row < HEIGHT - 2;
        boolean validCol = col >= 0 && col < WIDTH;
        return validRow && validCol;
    }

    /// <summary>
    /// 식민지 공유 보급품 반환
    /// </summary>
    public Supply getSupply() {
        return supply;
    }

    /// <summary>
    /// 바리케이드 반환
    /// </summary>
    public Barricade getBarricade() {
        return barricade;
    }

    /// <summary>
    /// 맵에 정착민 추가
    /// </summary>
    public void addColonist(Colonist colonist) {
        colonists.add(colonist);
    }

    /// <summary>
    /// 정착민 목록 반환
    /// </summary>
    public ArrayList<Colonist> getColonists() {
        return colonists;
    }

    /// <summary>
    /// 맵에 적 추가
    /// </summary>
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    /// <summary>
    /// 적 목록 반환
    /// </summary>
    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    /// <summary>
    /// 죽은 적을 목록에서 제거하고 스레드 종료
    /// </summary>
    public void removeDeadEnemies() {
        ArrayList<Enemy> dead = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (!enemy.isLiving()) {
                dead.add(enemy);
            }
        }
        for (Enemy enemy : dead) {
            enemy.stopRunning();
            enemies.remove(enemy);
            enemiesKilled++;
        }
    }

    /// <summary>
    /// 모든 적을 제거하고 스레드 종료 (밤 종료 시)
    /// </summary>
    public void clearEnemies() {
        for (Enemy enemy : enemies) {
            enemy.stopRunning();
        }
        enemies.clear();
    }

    /// <summary>
    /// 처치한 적 수 반환
    /// </summary>
    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    /// <summary>
    /// 로그 메시지 추가 (최대 보관 수 초과 시 오래된 것부터 삭제)
    /// 여러 스레드에서 동시에 호출될 수 있어 동기화 처리
    /// </summary>
    public synchronized void addLog(String message) {
        logs.add(message);
        if (logs.size() > LOG_CAPACITY) {
            logs.remove(0);
        }
    }

    /// <summary>
    /// 현재 로그 목록 복사본 반환 (렌더링용)
    /// </summary>
    public synchronized ArrayList<String> getRecentLogs() {
        return new ArrayList<>(logs);
    }
}
