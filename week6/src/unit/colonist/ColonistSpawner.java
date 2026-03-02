package unit.colonist;

import game.GameWorld;
import game.Position;
import gun.Pistol;

/// <summary>
/// 정착민 생성을 담당하는 클래스
/// Spec 조회, 이름/라벨 발급, 무기 장착, 월드 등록, 스레드 시작까지 일괄 처리
/// 승격 시 서브클래스 교체도 담당
/// </summary>
public class ColonistSpawner {

    /// <summary>
    /// 정착민 데이터 조회용 팩토리
    /// </summary>
    private final ColonistFactory factory;

    /// <summary>
    /// 팩토리를 지정하여 스포너 생성
    /// </summary>
    public ColonistSpawner(ColonistFactory factory) {
        this.factory = factory;
    }

    /// <summary>
    /// BASIC 정착민을 생성하여 월드에 등록하고 스레드 시작
    /// 이름은 NameProvider에서 랜덤, 라벨은 GameWorld에서 순서대로 발급
    /// </summary>
    public Colonist spawn(GameWorld gameWorld, Position position) {
        ColonistSpec spec = factory.getSpec(ColonistType.BASIC);
        String name = gameWorld.getNameProvider().pickName();
        char label = gameWorld.issueNextLabel();

        Colonist colonist = new Colonist(ColonistType.BASIC, spec, name, label, position, gameWorld);
        colonist.setGun(new Pistol());
        gameWorld.addColonist(colonist);
        colonist.start();

        return colonist;
    }

    /// <summary>
    /// BASIC 정착민을 전문 유형으로 승격
    /// 새 서브클래스(Gunner/Sniper/Assault)를 생성하고
    /// 이전 체력과 무기를 이어받은 뒤 특수효과 발동
    /// </summary>
    public Colonist promote(Colonist old, ColonistType newType, GameWorld gameWorld) {
        ColonistSpec newSpec = factory.getSpec(newType);
        String name = old.getColonistName();
        char label = old.getLabel();
        Position position = old.getPosition();

        // 역할에 맞는 서브클래스 생성
        Colonist promoted;
        switch (newType) {
            case GUNNER:
                promoted = new Gunner(newSpec, name, label, position, gameWorld);
                break;
            case SNIPER:
                promoted = new Sniper(newSpec, name, label, position, gameWorld);
                break;
            case ASSAULT:
                promoted = new Assault(newSpec, name, label, position, gameWorld);
                break;
            default:
                return old;
        }

        // 이전 정착민의 체력과 무기 이어받기
        promoted.transferStateFrom(old);

        // 이전 정착민 스레드 정지
        old.stopRunning();

        // 목록에서 교체 (같은 인덱스에 새 인스턴스 배치)
        int index = gameWorld.getColonists().indexOf(old);
        gameWorld.getColonists().set(index, promoted);

        // 새 스레드 시작
        promoted.start();

        return promoted;
    }

    /// <summary>
    /// 유형별 Spec 조회 (패널 표시 등에서 사용)
    /// </summary>
    public ColonistSpec getSpec(ColonistType type) {
        return factory.getSpec(type);
    }
}
