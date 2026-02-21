package player;

import board.SimpleBoard;

/// <summary>
/// 공식 모드 플레이어 추상 클래스
/// 기본 Player에 프로모션 선택 기능을 추가
/// </summary>
public abstract class ClassicPlayer extends Player {

    // ========== 생성자 ==========

    public ClassicPlayer(int color, String name) {
        super(color, name);
    }

    // ========== 추상 메서드 ==========

    /// <summary>
    /// 폰 프로모션 시 승격할 기물 선택
    /// 1: 퀸, 2: 룩, 3: 비숍, 4: 나이트
    /// </summary>
    public abstract int choosePromotion(SimpleBoard board);
}
