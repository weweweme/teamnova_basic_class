package game;

import structure.Structure;
import structure.Barricade;
import structure.Spike;
import structure.Landmine;
import structure.AmmoBox;

import gun.Bullet;
import unit.colonist.Colonist;
import unit.colonist.NameProvider;
import unit.enemy.Enemy;
import unit.enemy.EnemyType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/// <summary>
/// 게임 월드 전체를 관리하는 클래스
/// 100x20 크기의 사이드뷰 맵으로, 왼쪽은 안전지대, 오른쪽은 전장
/// 정착민/적/구조물/자원 등 모든 게임 오브젝트를 보유
/// </summary>
public class GameWorld {

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
    /// 총알 시스템 (이동/충돌 처리)
    /// </summary>
    private final BulletSystem bulletSystem = new BulletSystem();

    /// <summary>
    /// 화면 효과 (흔들림, 웨이브 경고 등)
    /// </summary>
    private final ScreenEffects screenEffects = new ScreenEffects();

    /// <summary>
    /// 합성 효과음 재생기
    /// </summary>
    private final SfxPlayer sfxPlayer;

    /// <summary>
    /// 정착민 이름 제공기 (중복 없는 랜덤 이름)
    /// </summary>
    private final NameProvider nameProvider = new NameProvider();

    /// <summary>
    /// 명중 이펙트의 지속 시간 (밀리초)
    /// </summary>
    private static final int EFFECT_DURATION = 200;

    /// <summary>
    /// 이펙트 복사본 재사용 버퍼 (렌더링용)
    /// </summary>
    private final ArrayList<HitEffect> effectsCopy = new ArrayList<>();

    /// <summary>
    /// 로그 문자열 재사용 버퍼 (렌더링용)
    /// </summary>
    private final ArrayList<String> logsCopy = new ArrayList<>();

    /// <summary>
    /// 제거 대상 적 재사용 버퍼
    /// </summary>
    private final ArrayList<Enemy> deadEnemies = new ArrayList<>();

    /// <summary>
    /// 화면에 표시 중인 이펙트 목록
    /// </summary>
    private final ArrayList<HitEffect> effects = new ArrayList<>();

    /// <summary>
    /// 게임 로그 큐 (오래된 순서, 앞쪽이 가장 오래된 것)
    /// </summary>
    private final ArrayDeque<LogEntry> logs = new ArrayDeque<>();

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
    /// 무적 모드 여부 (치트, 정착민 피해 무효화)
    /// </summary>
    private boolean invincible;

    /// <summary>
    /// 게임 월드 생성
    /// </summary>
    public GameWorld(SfxPlayer sfxPlayer) {
        this.sfxPlayer = sfxPlayer;
        this.enemiesKilled = 0;
    }

    /// <summary>
    /// 화면 효과 반환
    /// </summary>
    public ScreenEffects getScreenEffects() {
        return screenEffects;
    }

    /// <summary>
    /// 무적 모드 여부 반환
    /// </summary>
    public boolean isInvincible() {
        return invincible;
    }

    /// <summary>
    /// 무적 모드 설정
    /// </summary>
    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
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

                // 적의 블록 범위가 지뢰의 행+열과 겹치는지 확인
                int enemyCol = enemy.getPosition().getCol();
                int enemyRow = enemy.getPosition().getRow();
                String[] block = enemy.getSpec().getBlock();
                int blockWidth = block[0].length();
                int blockHeight = block.length;
                boolean colOverlap = enemyCol <= mine.getColumn() && enemyCol + blockWidth > mine.getColumn();
                boolean rowOverlap = enemyRow <= mine.getRow() && enemyRow + blockHeight > mine.getRow();
                boolean overlaps = colOverlap && rowOverlap;

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
    /// 정착민 추가
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
    /// 적 추가
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

                // 사망 애니메이션 지속 시간 (밀리초)
                final int DEATH_ANIM_DURATION = 800;
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
        bulletSystem.addBullet(bullet);
    }

    /// <summary>
    /// 현재 총알 목록 복사본 반환 (렌더링용)
    /// </summary>
    public synchronized ArrayList<Bullet> getBullets() {
        return bulletSystem.getBullets();
    }

    /// <summary>
    /// 모든 총알을 전진시키고, 적과 충돌 검사
    /// Main 루프에서 매 렌더 틱마다 호출
    /// </summary>
    public synchronized void advanceBullets() {
        bulletSystem.advanceBullets(enemies, effects, this);
    }

    /// <summary>
    /// 로그 메시지 추가
    /// 화면 용량을 넘으면 가장 오래된 것부터 제거
    /// 여러 스레드에서 동시에 호출될 수 있어 동기화 처리
    /// </summary>
    public synchronized void addLog(String message) {
        logs.add(new LogEntry(message, System.currentTimeMillis()));

        // 화면에 동시에 표시할 수 있는 최대 로그 수
        final int LOG_CAPACITY = 8;
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

        // 로그가 화면에 남아있는 시간 (밀리초)
        final int LOG_EXPIRE_MS = 5000;
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
    /// 이펙트 추가 (승격 특수효과 등 외부에서 직접 이펙트 생성 시 사용)
    /// </summary>
    public synchronized void addEffect(HitEffect effect) {
        effects.add(effect);
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
    /// 효과음 재생기 반환
    /// </summary>
    public SfxPlayer getSfxPlayer() {
        return sfxPlayer;
    }

    /// <summary>
    /// 이름 제공기 반환
    /// </summary>
    public NameProvider getNameProvider() {
        return nameProvider;
    }
}
