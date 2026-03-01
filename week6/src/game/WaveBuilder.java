package game;

import entity.enemy.EnemyType;

import java.util.ArrayList;

/// <summary>
/// 밤 웨이브를 구성하는 클래스
/// 일차와 난이도에 따라 출현할 적 목록을 생성
/// </summary>
public class WaveBuilder {

    /// <summary>
    /// 난이도 설정 (적 수 배율 적용용)
    /// </summary>
    private final DifficultySettings settings;

    /// <summary>
    /// 지정한 난이도로 웨이브 빌더 생성
    /// </summary>
    public WaveBuilder(DifficultySettings settings) {
        this.settings = settings;
    }

    /// <summary>
    /// 일차에 따라 출현할 적 목록을 구성
    /// 큰 적부터 작은 적 순서로 추가 (배치 시 중앙 정렬용)
    /// 1~5일차는 고정 웨이브, 6일차부터 무작위 구성, 5일마다 보스
    /// </summary>
    public ArrayList<EnemyType> buildWave(int day, boolean stormActive) {
        if (day <= 5) {
            return buildFixedWave(day);
        }
        return buildRandomWave(day, stormActive);
    }

    /// <summary>
    /// 1~5일차 고정 웨이브 구성
    /// </summary>
    private ArrayList<EnemyType> buildFixedWave(int day) {
        ArrayList<EnemyType> wave = new ArrayList<>();

        switch (day) {
            case 1:
                addToWave(wave, EnemyType.WOLF, 3);
                break;
            case 2:
                addToWave(wave, EnemyType.WOLF, 4);
                addToWave(wave, EnemyType.SPIDER, 2);
                break;
            case 3:
                addToWave(wave, EnemyType.BEAR, 1);
                addToWave(wave, EnemyType.WOLF, 3);
                addToWave(wave, EnemyType.SKELETON, 3);
                break;
            case 4:
                addToWave(wave, EnemyType.BEAR, 1);
                addToWave(wave, EnemyType.SCORPION, 2);
                addToWave(wave, EnemyType.ZOMBIE, 2);
                addToWave(wave, EnemyType.WOLF, 2);
                break;
            case 5:
                addToWave(wave, EnemyType.DRAGON, 1);
                addToWave(wave, EnemyType.ORC, 1);
                addToWave(wave, EnemyType.BANDIT, 2);
                addToWave(wave, EnemyType.SPIDER, 2);
                addToWave(wave, EnemyType.RAT, 2);
                break;
        }

        return wave;
    }

    /// <summary>
    /// 6일차 이후 무작위 웨이브 구성
    /// 보스 → 강한 → 일반 순서로 추가 (큰 것부터)
    /// 폭풍 경고 시 일반 몬스터 50% 추가
    /// </summary>
    private ArrayList<EnemyType> buildRandomWave(int day, boolean stormActive) {
        ArrayList<EnemyType> wave = new ArrayList<>();

        // 5일마다 보스 등장
        boolean isBossDay = day % 5 == 0;
        if (isBossDay) {
            EnemyType[] bosses = {EnemyType.DRAGON, EnemyType.GOLEM};
            int index = (int) (Math.random() * bosses.length);
            wave.add(bosses[index]);
        }

        // 강한 몬스터 (일차 / 2 마리, 난이도 배율 적용)
        EnemyType[] strongs = {EnemyType.BEAR, EnemyType.BANDIT, EnemyType.SCORPION, EnemyType.ORC};
        int strongCount = settings.applyEnemyCount(day / 2);
        for (int i = 0; i < strongCount; i++) {
            int index = (int) (Math.random() * strongs.length);
            wave.add(strongs[index]);
        }

        // 일반 몬스터 (3 + 일차 마리, 난이도 배율 적용)
        // 폭풍 경고 시 일반 몬스터 50% 추가
        EnemyType[] normals = {EnemyType.WOLF, EnemyType.SPIDER, EnemyType.SKELETON,
                               EnemyType.ZOMBIE, EnemyType.RAT, EnemyType.SLIME};
        int baseNormalCount = 3 + day;
        if (stormActive) {
            baseNormalCount = baseNormalCount * 3 / 2;
        }
        int normalCount = settings.applyEnemyCount(baseNormalCount);
        for (int i = 0; i < normalCount; i++) {
            int index = (int) (Math.random() * normals.length);
            wave.add(normals[index]);
        }

        return wave;
    }

    /// <summary>
    /// 웨이브 목록에 지정한 적을 여러 마리 추가
    /// </summary>
    private void addToWave(ArrayList<EnemyType> wave, EnemyType type, int count) {
        for (int i = 0; i < count; i++) {
            wave.add(type);
        }
    }
}
