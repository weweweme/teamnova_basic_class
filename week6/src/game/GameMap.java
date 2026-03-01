package game;

import structure.Structure;
import structure.Barricade;
import structure.Spike;
import structure.Landmine;
import structure.AmmoBox;

import gun.Bullet;
import entity.colonist.Colonist;
import entity.enemy.Enemy;
import entity.enemy.EnemyType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/// <summary>
/// 게임 맵 전체를 관리하는 클래스
/// 80x20 크기의 사이드뷰 맵으로, 왼쪽은 안전지대, 오른쪽은 전장
/// 정착민/적/로그 등 모든 게임 오브젝트를 보유
/// </summary>
public class GameMap {

    /// <summary>
    /// 맵 가로 크기 (문자 단위)
    /// </summary>
    public static final int WIDTH = 100;

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
    /// 설치된 가시덫 목록
    /// </summary>
    private final ArrayList<Spike> spikes = new ArrayList<>();

    /// <summary>
    /// 설치된 지뢰 목록
    /// </summary>
    private final ArrayList<Landmine> landmines = new ArrayList<>();

    /// <summary>
    /// 설치된 탄약 상자 목록
    /// </summary>
    private final ArrayList<AmmoBox> ammoBoxes = new ArrayList<>();

    /// <summary>
    /// 현재 맵에 있는 적 목록
    /// </summary>
    private final ArrayList<Enemy> enemies = new ArrayList<>();

    /// <summary>
    /// 날아가는 총알 목록 (스레드가 아닌 데이터 객체)
    /// </summary>
    private final ArrayList<Bullet> bullets = new ArrayList<>();

    /// <summary>
    /// 명중 이펙트의 지속 시간 (밀리초)
    /// </summary>
    private final int EFFECT_DURATION = 200;

    /// <summary>
    /// 총알 복사본 재사용 버퍼 (렌더링용)
    /// </summary>
    private final ArrayList<Bullet> bulletsCopy = new ArrayList<>();

    /// <summary>
    /// 이펙트 복사본 재사용 버퍼 (렌더링용)
    /// </summary>
    private final ArrayList<HitEffect> effectsCopy = new ArrayList<>();

    /// <summary>
    /// 로그 문자열 재사용 버퍼 (렌더링용)
    /// </summary>
    private final ArrayList<String> logsCopy = new ArrayList<>();

    /// <summary>
    /// 제거 대상 총알 재사용 버퍼
    /// </summary>
    private final ArrayList<Bullet> bulletsToRemove = new ArrayList<>();

    /// <summary>
    /// 제거 대상 적 재사용 버퍼
    /// </summary>
    private final ArrayList<Enemy> deadEnemies = new ArrayList<>();

    /// <summary>
    /// 화면에 표시 중인 이펙트 목록
    /// </summary>
    private final ArrayList<HitEffect> effects = new ArrayList<>();

    /// <summary>
    /// 화면에 동시에 표시할 수 있는 최대 로그 수
    /// </summary>
    private final int LOG_CAPACITY = 8;

    /// <summary>
    /// 로그가 화면에 남아있는 시간 (밀리초)
    /// </summary>
    private final int LOG_EXPIRE_MS = 5000;

    /// <summary>
    /// 게임 로그 큐 (오래된 순서, 앞쪽이 가장 오래된 것)
    /// </summary>
    private final ArrayDeque<LogEntry> logs = new ArrayDeque<>();

    /// <summary>
    /// 사망 애니메이션 지속 시간 (밀리초)
    /// </summary>
    private final int DEATH_ANIM_DURATION = 800;

    /// <summary>
    /// 다음 정착민에게 부여할 알파벳 라벨 (A부터 순서대로)
    /// </summary>
    private char nextLabel = 'A';

    /// <summary>
    /// 처치한 적 수
    /// </summary>
    private int enemiesKilled;

    /// <summary>
    /// 종류별 처치 수 (통계 출력용)
    /// </summary>
    private final HashMap<EnemyType, Integer> killsByType = new HashMap<>();

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
    /// 가시덫 설치
    /// </summary>
    public void addSpike(Spike spike) {
        spikes.add(spike);
    }

    /// <summary>
    /// 설치된 가시덫 목록 반환
    /// </summary>
    public ArrayList<Spike> getSpikes() {
        return spikes;
    }

    /// <summary>
    /// 파괴된 가시덫 제거
    /// </summary>
    public void removeDestroyedSpikes() {
        spikes.removeIf(Structure::isDestroyed);
    }

    /// <summary>
    /// 지뢰 설치
    /// </summary>
    public void addLandmine(Landmine landmine) {
        landmines.add(landmine);
    }

    /// <summary>
    /// 설치된 지뢰 목록 반환
    /// </summary>
    public ArrayList<Landmine> getLandmines() {
        return landmines;
    }

    /// <summary>
    /// 폭발한 지뢰 제거
    /// </summary>
    public void removeExplodedLandmines() {
        landmines.removeIf(Structure::isDestroyed);
    }

    /// <summary>
    /// 적이 지뢰 위를 지나는지 확인하여 폭발 처리
    /// 매 렌더 틱마다 호출
    /// </summary>
    public synchronized void checkLandmines() {
        for (Landmine mine : landmines) {
            if (mine.isDestroyed()) {
                continue;
            }

            for (Enemy enemy : enemies) {
                if (!enemy.isLiving()) {
                    continue;
                }

                // 적의 블록 범위가 지뢰 열과 겹치는지 확인
                int enemyCol = enemy.getPosition().getCol();
                int blockWidth = enemy.getSpec().getBlock()[0].length();
                boolean overlaps = enemyCol <= mine.getColumn() && enemyCol + blockWidth > mine.getColumn();

                if (overlaps) {
                    addLog("[폭발] 지뢰 기폭!");
                    mine.explode(enemies, this);
                    break;
                }
            }
        }

        removeExplodedLandmines();
    }

    /// <summary>
    /// 탄약 상자 설치
    /// </summary>
    public void addAmmoBox(AmmoBox ammoBox) {
        ammoBoxes.add(ammoBox);
    }

    /// <summary>
    /// 설치된 탄약 상자 목록 반환
    /// </summary>
    public ArrayList<AmmoBox> getAmmoBoxes() {
        return ammoBoxes;
    }

    /// <summary>
    /// 파괴된 탄약 상자 제거
    /// </summary>
    public void removeDestroyedAmmoBoxes() {
        ammoBoxes.removeIf(Structure::isDestroyed);
    }

    /// <summary>
    /// 탄약 상자가 살아있으면 발사 간격 배율 반환 (0.7 = 빨라짐, 1.0 = 보통)
    /// </summary>
    public double getFireRateMultiplier() {
        for (AmmoBox box : ammoBoxes) {
            if (!box.isDestroyed()) {
                return box.getFireRateMultiplier();
            }
        }
        return 1.0;
    }

    /// <summary>
    /// 다음 정착민 라벨을 발급하고 알파벳을 한 칸 전진
    /// </summary>
    public char issueNextLabel() {
        char label = nextLabel;
        nextLabel++;
        return label;
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
        deadEnemies.clear();
        long now = System.currentTimeMillis();

        for (Enemy enemy : enemies) {
            if (!enemy.isLiving()) {
                // 사망 애니메이션이 끝난 적만 제거
                boolean animDone = now - enemy.getDeathTime() > DEATH_ANIM_DURATION;
                if (animDone) {
                    deadEnemies.add(enemy);
                }
            }
        }
        for (Enemy enemy : deadEnemies) {
            enemy.stopRunning();
            enemies.remove(enemy);
            enemiesKilled++;
            // 종류별 처치 수 기록
            EnemyType type = enemy.getType();
            killsByType.put(type, killsByType.getOrDefault(type, 0) + 1);
            // 처치 보상: 적 종류마다 다름 (일반 1~3, 강한 4~6, 보스 10~15)
            supply.add(enemy.getSpec().getReward());
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
    /// 종류별 처치 수 반환 (통계 출력용)
    /// </summary>
    public HashMap<EnemyType, Integer> getKillsByType() {
        return killsByType;
    }

    /// <summary>
    /// 총알 추가 (정착민 스레드에서 호출)
    /// </summary>
    public synchronized void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    /// <summary>
    /// 현재 총알 목록 복사본 반환 (렌더링용)
    /// </summary>
    public synchronized ArrayList<Bullet> getBullets() {
        bulletsCopy.clear();
        bulletsCopy.addAll(bullets);
        return bulletsCopy;
    }

    /// <summary>
    /// 모든 총알을 전진시키고, 적과 충돌 검사
    /// 적에 명중하면 피해를 주고 총알 제거, 화면 밖이면 제거
    /// Main 루프에서 매 렌더 틱마다 호출
    /// </summary>
    public synchronized void advanceBullets() {
        bulletsToRemove.clear();

        for (Bullet bullet : bullets) {
            bullet.advance();

            // 화면 밖으로 나간 총알 제거
            if (bullet.isOffScreen()) {
                bulletsToRemove.add(bullet);
                continue;
            }

            // 적과 충돌 검사
            for (Enemy enemy : enemies) {
                if (!enemy.isLiving()) {
                    continue;
                }

                // 적 블록 범위 계산
                String[] block = enemy.getSpec().getBlock();
                int enemyRow = enemy.getPosition().getRow();
                int enemyCol = enemy.getPosition().getCol();
                int blockHeight = block.length;
                int blockWidth = block[0].length();

                // 총알이 적 블록 범위 안에 있으면 명중
                boolean hitRow = bullet.getRow() >= enemyRow && bullet.getRow() < enemyRow + blockHeight;
                boolean hitCol = bullet.getCol() >= enemyCol && bullet.getCol() < enemyCol + blockWidth;

                if (hitRow && hitCol) {
                    enemy.takeDamage(bullet.getDamage());

                    // 넉백 적용 (명중 시 적을 오른쪽으로 밀어냄)
                    if (bullet.getKnockback() > 0 && enemy.isLiving()) {
                        int newCol = enemy.getPosition().getCol() + bullet.getKnockback();
                        if (newCol < WIDTH) {
                            enemy.getPosition().setCol(newCol);
                        }
                    }

                    // 무기별 명중 이펙트 (총알의 문자/색상 사용)
                    char hitChar = bullet.getBulletChar() == '*' ? '!' : bullet.getBulletChar();
                    int hitColor = bullet.getBulletColor() != 0 ? bullet.getBulletColor() : 33;
                    effects.add(new HitEffect(bullet.getRow(), bullet.getCol(), System.currentTimeMillis(), hitChar, hitColor));

                    // 적 처치 시 로그
                    if (!enemy.isLiving()) {
                        String enemyName = enemy.getSpec().getDisplayName();
                        addLog("[" + bullet.getShooterLabel() + "] " + enemyName + " 처치!");
                    }

                    // 관통 총알은 제거하지 않고 계속 전진
                    if (!bullet.isPiercing()) {
                        bulletsToRemove.add(bullet);
                    }
                    break;
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
    }

    /// <summary>
    /// 로그 메시지 추가
    /// 화면 용량을 넘으면 가장 오래된 것부터 제거
    /// 여러 스레드에서 동시에 호출될 수 있어 동기화 처리
    /// </summary>
    public synchronized void addLog(String message) {
        logs.add(new LogEntry(message, System.currentTimeMillis()));
        if (logs.size() > LOG_CAPACITY) {
            logs.poll();
        }
    }

    /// <summary>
    /// 만료되지 않은 로그 메시지 목록 반환 (렌더링용)
    /// 5초가 지난 로그는 자동으로 큐에서 제거
    /// </summary>
    public synchronized ArrayList<String> getRecentLogs() {
        long now = System.currentTimeMillis();

        // 큐 앞쪽부터 만료된 로그 제거
        while (!logs.isEmpty() && now - logs.peek().getCreatedTime() > LOG_EXPIRE_MS) {
            logs.poll();
        }

        logsCopy.clear();
        for (LogEntry entry : logs) {
            logsCopy.add(entry.getMessage());
        }
        return logsCopy;
    }

    /// <summary>
    /// 만료된 이펙트를 제거하고 남은 이펙트 목록 복사본 반환
    /// </summary>
    public synchronized ArrayList<HitEffect> getEffects() {
        long now = System.currentTimeMillis();
        effects.removeIf(effect -> now - effect.getCreatedTime() > EFFECT_DURATION);
        effectsCopy.clear();
        effectsCopy.addAll(effects);
        return effectsCopy;
    }

    /// <summary>
    /// 총알 명중 시 화면에 잠시 표시되는 이펙트
    /// </summary>
    public static class HitEffect {

        /// <summary>
        /// 이펙트 표시 행
        /// </summary>
        private final int row;

        /// <summary>
        /// 이펙트 표시 열
        /// </summary>
        private final int col;

        /// <summary>
        /// 이펙트 생성 시각 (밀리초)
        /// </summary>
        private final long createdTime;

        /// <summary>
        /// 이펙트 표시 문자 (무기마다 다름)
        /// </summary>
        private final char effectChar;

        /// <summary>
        /// 이펙트 ANSI 색상 코드 (무기마다 다름)
        /// </summary>
        private final int effectColor;

        public HitEffect(int row, int col, long createdTime, char effectChar, int effectColor) {
            this.row = row;
            this.col = col;
            this.createdTime = createdTime;
            this.effectChar = effectChar;
            this.effectColor = effectColor;
        }

        /// <summary>
        /// 이펙트 행 반환
        /// </summary>
        public int getRow() {
            return row;
        }

        /// <summary>
        /// 이펙트 열 반환
        /// </summary>
        public int getCol() {
            return col;
        }

        /// <summary>
        /// 이펙트 생성 시각 반환
        /// </summary>
        public long getCreatedTime() {
            return createdTime;
        }

        /// <summary>
        /// 이펙트 표시 문자 반환
        /// </summary>
        public char getEffectChar() {
            return effectChar;
        }

        /// <summary>
        /// 이펙트 색상 코드 반환
        /// </summary>
        public int getEffectColor() {
            return effectColor;
        }
    }

    /// <summary>
    /// 로그 한 줄의 메시지와 생성 시각을 담는 클래스
    /// 생성 후 일정 시간이 지나면 큐에서 제거됨
    /// </summary>
    private class LogEntry {

        /// <summary>
        /// 로그 메시지 내용
        /// </summary>
        private final String message;

        /// <summary>
        /// 로그가 생성된 시각 (밀리초)
        /// </summary>
        private final long createdTime;

        public LogEntry(String message, long createdTime) {
            this.message = message;
            this.createdTime = createdTime;
        }

        /// <summary>
        /// 메시지 반환
        /// </summary>
        public String getMessage() {
            return message;
        }

        /// <summary>
        /// 생성 시각 반환
        /// </summary>
        public long getCreatedTime() {
            return createdTime;
        }
    }
}
