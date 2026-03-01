package entity.enemy;

import java.util.HashMap;

/// <summary>
/// 적 종류(EnemyType)에 대응하는 속성 데이터(EnemySpec)를 제공하는 팩토리
/// 내부에 종류별 속성을 미리 등록해두고, getSpec()으로 조회
/// </summary>
public class EnemyFactory {

    /// <summary>
    /// 종류별 속성 매핑 테이블
    /// </summary>
    private final HashMap<EnemyType, EnemySpec> specs = new HashMap<>();

    /// <summary>
    /// 팩토리 생성 시 모든 종류의 속성을 등록
    /// </summary>
    public EnemyFactory() {
        // ── 일반 (6x3) ──

        specs.put(EnemyType.WOLF, new EnemySpec(
            "늑대", 30, 3, 400, 2, EnemyTrait.STANDARD, new String[]{
                " /\\/\\ ",
                " (oo) ",
                "  \\/  "
            }));

        specs.put(EnemyType.SPIDER, new EnemySpec(
            "거미", 20, 2, 250, 2, EnemyTrait.CHARGER, new String[]{
                "\\(oo)/",
                " \\\\// ",
                "/(  )\\"
            }));

        specs.put(EnemyType.SKELETON, new EnemySpec(
            "해골", 25, 3, 350, 2, EnemyTrait.STANDARD, new String[]{
                " (^^) ",
                " /||\\ ",
                "  /\\  "
            }));

        specs.put(EnemyType.ZOMBIE, new EnemySpec(
            "좀비", 35, 4, 500, 3, EnemyTrait.STANDARD, new String[]{
                " (00) ",
                " /||\\ ",
                " _/\\_ "
            }));

        specs.put(EnemyType.RAT, new EnemySpec(
            "쥐", 15, 1, 200, 1, EnemyTrait.CHARGER, new String[]{
                "/\\_/| ",
                "(o.o) ",
                " \\_|  "
            }));

        specs.put(EnemyType.SLIME, new EnemySpec(
            "슬라임", 40, 2, 450, 3, EnemyTrait.REGENERATING, new String[]{
                " .--. ",
                "(o  o)",
                " '--' "
            }));

        // ── 강한 (8x4) ──

        specs.put(EnemyType.BEAR, new EnemySpec(
            "곰", 80, 8, 600, 5, EnemyTrait.ARMORED, new String[]{
                " /\\  /\\ ",
                "( O  O )",
                " |VVVV| ",
                " /____\\ "
            }));

        specs.put(EnemyType.BANDIT, new EnemySpec(
            "도적", 60, 6, 350, 4, EnemyTrait.STANDARD, new String[]{
                " _/~~\\_ ",
                "|(-_-)| ",
                "/|    |\\",
                " /\\  /\\ "
            }));

        specs.put(EnemyType.SCORPION, new EnemySpec(
            "전갈", 70, 7, 400, 5, EnemyTrait.ARMORED, new String[]{
                "___  /\\ ",
                "<(oo)/_>",
                " /|| \\/ ",
                " /||\\ \\ "
            }));

        specs.put(EnemyType.ORC, new EnemySpec(
            "오크", 90, 9, 500, 6, EnemyTrait.STANDARD, new String[]{
                " /~~~~\\ ",
                "|(o  o)|",
                "-|/VV\\|-",
                " /|  |\\ "
            }));

        // ── 보스 (12x5 / 10x5) ──

        specs.put(EnemyType.DRAGON, new EnemySpec(
            "드래곤", 200, 15, 300, 10, EnemyTrait.STANDARD, new String[]{
                "  /\\    /\\  ",
                " / O\\__/O \\ ",
                "<   VVVV   >",
                " \\ /\\  /\\ / ",
                "  V  \\/  V  "
            }));

        specs.put(EnemyType.GOLEM, new EnemySpec(
            "골렘", 300, 10, 700, 15, EnemyTrait.ARMORED, new String[]{
                " .------. ",
                "| O    O |",
                "|==|==|==|",
                "|  |  |  |",
                "|__|__|__|"
            }));
    }

    /// <summary>
    /// 지정한 종류의 속성 데이터 반환
    /// </summary>
    public EnemySpec getSpec(EnemyType type) {
        return specs.get(type);
    }
}
