package entity.colonist;

import java.util.HashMap;

/// <summary>
/// 정착민 유형(ColonistType)에 대응하는 속성 데이터(ColonistSpec)를 제공하는 팩토리
/// 내부에 유형별 속성을 미리 등록해두고, getSpec()으로 조회
/// </summary>
public class ColonistFactory {

    /// <summary>
    /// 유형별 속성 매핑 테이블
    /// </summary>
    private final HashMap<ColonistType, ColonistSpec> specs = new HashMap<>();

    /// <summary>
    /// 팩토리 생성 시 모든 유형의 속성을 등록
    /// </summary>
    public ColonistFactory() {
        // 사격수 — 패시브: 속사 (발사 간격 20% 감소)
        specs.put(ColonistType.GUNNER, new ColonistSpec("사격수", 100, 0.8, 0.0, 0));

        // 저격수 — 패시브: 치명타 (30% 확률로 2배 데미지)
        specs.put(ColonistType.SNIPER, new ColonistSpec("저격수", 100, 1.0, 0.3, 0));

        // 돌격수 — 패시브: 넉백 (명중 시 적 1칸 밀어냄)
        specs.put(ColonistType.ASSAULT, new ColonistSpec("돌격수", 100, 1.0, 0.0, 1));
    }

    /// <summary>
    /// 지정한 유형의 속성 데이터 반환
    /// </summary>
    public ColonistSpec getSpec(ColonistType type) {
        return specs.get(type);
    }
}
