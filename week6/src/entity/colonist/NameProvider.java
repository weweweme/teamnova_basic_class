package entity.colonist;

import java.util.ArrayList;
import java.util.Random;

/// <summary>
/// 정착민 이름을 중복 없이 제공하는 클래스
/// 30개 이름 풀에서 랜덤 선택, 사용된 이름은 재사용하지 않음
/// </summary>
public class NameProvider {

    /// <summary>
    /// 이름 풀 (30개 한국 이름)
    /// </summary>
    private static final String[] NAME_POOL = {
        "김철수", "이영희", "박민수", "최지훈", "정수빈",
        "강도윤", "조서연", "윤하준", "장예은", "임지호",
        "한소율", "오유진", "서준혁", "신다은", "권태양",
        "황민재", "송하린", "류시우", "전예린", "배준서",
        "안지민", "홍서준", "유채원", "문도현", "양하은",
        "손민호", "노수아", "구지안", "민서윤", "곽은호"
    };

    /// <summary>
    /// 이미 사용된 이름 목록
    /// </summary>
    private final ArrayList<String> usedNames = new ArrayList<>();

    /// <summary>
    /// 이름 풀 소진 시 대체 이름 번호
    /// </summary>
    private int fallbackCount;

    /// <summary>
    /// 난수 생성기
    /// </summary>
    private final Random random = new Random();

    /// <summary>
    /// 미사용 이름 중 하나를 랜덤 선택하여 반환
    /// 풀이 모두 소진되면 "표류자1", "표류자2" 형태로 대체
    /// </summary>
    public String pickName() {
        // 미사용 이름 수집
        ArrayList<String> available = new ArrayList<>();
        for (String name : NAME_POOL) {
            if (!usedNames.contains(name)) {
                available.add(name);
            }
        }

        // 풀 소진 시 대체 이름
        if (available.isEmpty()) {
            fallbackCount++;
            String fallback = "표류자" + fallbackCount;
            usedNames.add(fallback);
            return fallback;
        }

        // 랜덤 선택
        String picked = available.get(random.nextInt(available.size()));
        usedNames.add(picked);
        return picked;
    }
}
