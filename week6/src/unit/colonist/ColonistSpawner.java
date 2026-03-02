package unit.colonist;

import game.GameWorld;
import game.Position;
import gun.Pistol;

/// <summary>
/// 정착민 생성을 담당하는 클래스
/// Spec 조회, 이름/라벨 발급, 무기 장착, 월드 등록, 스레드 시작까지 일괄 처리
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
    /// 유형별 Spec 조회 (승격 등에서 사용)
    /// </summary>
    public ColonistSpec getSpec(ColonistType type) {
        return factory.getSpec(type);
    }
}
