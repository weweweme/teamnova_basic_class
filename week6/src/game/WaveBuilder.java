package game;

import unit.enemy.EnemyType;

import java.util.ArrayList;

/// <summary>
/// 밤 웨이브를 구성하는 클래스
/// 일차와 난이도에 따라 출현할 적 목록을 생성
/// </summary>
public class WaveBuilder {

    /// <summary>
    /// 보스급 적 후보 목록
    /// </summary>
    private static final EnemyType[] BOSSES = {EnemyType.DRAGON, EnemyType.GOLEM};

    /// <summary>
    /// 강한 적 후보 목록
    /// </summary>
    private static final EnemyType[] STRONGS = {EnemyType.BEAR, EnemyType.BANDIT, EnemyType.SCORPION, EnemyType.ORC};

    /// <summary>
    /// 일반 적 후보 목록
    /// </summary>
    private static final EnemyType[] NORMALS = {EnemyType.WOLF, EnemyType.SPIDER, EnemyType.SKELETON,
                                                EnemyType.ZOMBIE, EnemyType.RAT, EnemyType.SLIME};

    /// <summary>
    /// 난이도 (적 수 배율 적용용)
    /// </summary>
    private final Difficulty difficulty;

    /// <summary>
    /// 웨이브 적 목록 (매 웨이브마다 clear 후 재사용)
    /// </summary>
    private final ArrayList<EnemyType> wave = new ArrayList<>();

    /// <summary>
    /// 지정한 난이도로 웨이브 빌더 생성
    /// </summary>
    public WaveBuilder(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /// <summary>
    /// 일차에 따라 출현할 적 목록을 구성
    /// 큰 적부터 작은 적 순서로 추가 (배치 시 중앙 정렬용)
    /// 1~5일차는 고정 웨이브, 6일차부터 무작위 구성, 5일마다 보스
    /// </summary>
    public ArrayList<EnemyType> buildWave(int day, boolean stormActive) {
        wave.clear();

        if (day <= 5) {
            buildFixedWave(day);
        } else {
            buildRandomWave(day, stormActive);
        }

        return wave;
    }

    /// <summary>
    /// 1~5일차 고정 웨이브 구성
    /// </summary>
    private void buildFixedWave(int day) {
        switch (day) {
            case 1:
                addToWave(EnemyType.WOLF, 3);
                break;
            case 2:
                addToWave(EnemyType.WOLF, 4);
                addToWave(EnemyType.SPIDER, 2);
                break;
            case 3:
                addToWave(EnemyType.BEAR, 1);
                addToWave(EnemyType.WOLF, 3);
                addToWave(EnemyType.SKELETON, 3);
                break;
            case 4:
                addToWave(EnemyType.BEAR, 1);
                addToWave(EnemyType.SCORPION, 2);
                addToWave(EnemyType.ZOMBIE, 2);
                addToWave(EnemyType.WOLF, 2);
                break;
            case 5:
                addToWave(EnemyType.DRAGON, 1);
                addToWave(EnemyType.ORC, 1);
                addToWave(EnemyType.BANDIT, 2);
                addToWave(EnemyType.SPIDER, 2);
                addToWave(EnemyType.RAT, 2);
                break;
        }
    }

    /// <summary>
    /// 6일차 이후 무작위 웨이브 구성
    /// 보스 → 강한 → 일반 순서로 추가 (큰 것부터)
    /// 폭풍 경고 시 일반 몬스터 50% 추가
    /// </summary>
    private void buildRandomWave(int day, boolean stormActive) {
        // 5일마다 보스 등장
        boolean isBossDay = day % 5 == 0;
        if (isBossDay) {
            int index = (int) (Math.random() * BOSSES.length);
            wave.add(BOSSES[index]);
        }

        // 강한 몬스터 (일차 / 2 마리, 난이도 배율 적용)
        int strongCount = difficulty.applyEnemyCount(day / 2);
        for (int i = 0; i < strongCount; i++) {
            int index = (int) (Math.random() * STRONGS.length);
            wave.add(STRONGS[index]);
        }

        // 일반 몬스터 (3 + 일차 마리, 난이도 배율 적용)
        // 폭풍 경고 시 일반 몬스터 50% 추가
        int baseNormalCount = 3 + day;
        if (stormActive) {
            baseNormalCount = baseNormalCount * 3 / 2;
        }
        int normalCount = difficulty.applyEnemyCount(baseNormalCount);
        for (int i = 0; i < normalCount; i++) {
            int index = (int) (Math.random() * NORMALS.length);
            wave.add(NORMALS[index]);
        }
    }

    /// <summary>
    /// 웨이브 목록에 지정한 적을 여러 마리 추가
    /// </summary>
    private void addToWave(EnemyType type, int count) {
        for (int i = 0; i < count; i++) {
            wave.add(type);
        }
    }
}
