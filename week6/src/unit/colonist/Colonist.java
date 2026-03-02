package unit.colonist;

import gun.Gun;
import gun.Pistol;

import unit.GameUnit;
import game.Position;
import game.Util;
import game.GameWorld;

/// <summary>
/// 정착민 한 명을 나타내는 클래스
/// 각 정착민은 자기 스레드에서 자율적으로 행동
/// 상태 패턴으로 행동을 관리 (WanderingState, ShootingState 등)
/// </summary>
public class Colonist extends GameUnit {

    /// <summary>
    /// 정착민 유형 (타입 식별용)
    /// </summary>
    private final ColonistType type;

    /// <summary>
    /// 정착민 속성 데이터 (체력/패시브 등)
    /// </summary>
    private final ColonistSpec spec;

    /// <summary>
    /// 정착민 이름 (한글, 패널에서만 표시)
    /// </summary>
    private final String name;

    /// <summary>
    /// 맵에 표시할 알파벳 라벨 (A, B, C 순서)
    /// </summary>
    private final char label;

    /// <summary>
    /// 화면에 그릴 블록 (템플릿의 '@'를 라벨로 치환한 결과)
    /// </summary>
    private final String[] block;

    /// <summary>
    /// 장착한 무기 (발사 패턴과 데미지 결정)
    /// </summary>
    private Gun gun;

    /// <summary>
    /// 현재 행동 상태
    /// </summary>
    private ColonistState currentState;

    /// <summary>
    /// 사망 시각 (0이면 살아있음, 양수면 사망 시점의 밀리초)
    /// </summary>
    private long deathTime;

    /// <summary>
    /// 발사 횟수 카운터 (SPECIAL_INTERVAL 도달 시 특수효과 발동)
    /// </summary>
    private int shotCount;

    /// <summary>
    /// 지정한 유형, 이름, 위치, 맵으로 정착민 생성
    /// 체력은 유형의 최대 체력, 배회 상태로 시작
    /// </summary>
    public Colonist(ColonistType type, ColonistSpec spec, String name, char label, Position position, GameWorld gameWorld) {
        super(position, gameWorld, spec.getMaxHp());
        this.type = type;
        this.spec = spec;
        this.name = name;
        this.label = label;

        // 블록 템플릿의 플레이스홀더('@')를 이 정착민의 라벨로 치환
        String[] template = spec.getBlockTemplate();
        this.block = new String[template.length];
        for (int i = 0; i < template.length; i++) {
            this.block[i] = template[i].replace('@', label);
        }

        this.gun = new Pistol();
        this.currentState = new WanderingState();
    }

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 현재 상태의 update를 호출
    /// </summary>
    @Override
    public void run() {
        currentState.enter(this);

        while (isRunning() && isLiving()) {
            currentState.update(this);

            // 행동 틱 간격 (밀리초)
            final int TICK_DELAY = 500;
            Util.delay(TICK_DELAY);
        }

        if (!isLiving()) {
            Util.beep();
            getGameWorld().addLog("[" + label + ": " + name + "] 사망");
        }
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    @Override
    public int getMaxHp() {
        return spec.getMaxHp();
    }

    /// <summary>
    /// 피해를 받아 체력 감소, 사망 시 시각 기록
    /// 무적 모드일 때는 피해 무효
    /// </summary>
    @Override
    public void takeDamage(int damage) {
        if (getGameWorld().isInvincible()) {
            return;
        }
        super.takeDamage(damage);

        if (getHp() == 0 && deathTime == 0) {
            deathTime = System.currentTimeMillis();
        }
    }

    /// <summary>
    /// 사망 시각 반환 (0이면 살아있음)
    /// </summary>
    public long getDeathTime() {
        return deathTime;
    }

    /// <summary>
    /// 정착민 속성 데이터 반환
    /// </summary>
    public ColonistSpec getSpec() {
        return spec;
    }

    /// <summary>
    /// 정착민 유형 반환
    /// </summary>
    public ColonistType getType() {
        return type;
    }

    /// <summary>
    /// 이전 정착민의 체력과 무기를 이어받기 (승격 시 호출)
    /// 최대 체력 증가분만큼 추가 회복
    /// </summary>
    public void transferStateFrom(Colonist old) {
        this.gun = old.gun;

        // 이전 체력 + 최대 체력 증가분
        int hpGain = Math.max(this.getMaxHp() - old.getMaxHp(), 0);
        int targetHp = Math.min(old.getHp() + hpGain, this.getMaxHp());
        setHp(targetHp);
    }

    /// <summary>
    /// 발사 직후 호출되는 콜백
    /// 발사 횟수를 세고, SPECIAL_INTERVAL에 도달하면 specialAttack() 발동
    /// </summary>
    public void onShoot(unit.enemy.Enemy target) {
        shotCount++;
        final int SPECIAL_INTERVAL = 5;
        if (shotCount < SPECIAL_INTERVAL) {
            return;
        }
        shotCount = 0;
        specialAttack(target);
    }

    /// <summary>
    /// 주기적으로 발동하는 특수공격
    /// 서브클래스에서 역할별 고유 효과를 구현
    /// </summary>
    protected void specialAttack(unit.enemy.Enemy target) {
        // 기본 정착민은 특수공격 없음
    }

    /// <summary>
    /// 상태 전환
    /// 현재 상태의 exit → 새 상태의 enter 순서로 호출
    /// </summary>
    public void changeState(ColonistState newState) {
        currentState.exit(this);
        currentState = newState;
        currentState.enter(this);
        getGameWorld().addLog("[" + label + ": " + name + "] " + newState.getDisplayName());
    }

    /// <summary>
    /// 정착민 이름 반환
    /// </summary>
    public String getColonistName() {
        return name;
    }

    /// <summary>
    /// 맵 표시용 알파벳 라벨 반환
    /// </summary>
    public char getLabel() {
        return label;
    }

    /// <summary>
    /// 화면에 그릴 블록 반환
    /// </summary>
    public String[] getBlock() {
        return block;
    }

    /// <summary>
    /// 장착한 무기 반환
    /// </summary>
    public Gun getGun() {
        return gun;
    }

    /// <summary>
    /// 무기 교체
    /// </summary>
    public void setGun(Gun gun) {
        this.gun = gun;
    }

    /// <summary>
    /// 현재 상태 반환
    /// </summary>
    public ColonistState getCurrentState() {
        return currentState;
    }
}
