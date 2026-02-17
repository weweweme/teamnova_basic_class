package player;

import board.SimpleBoard;

/// <summary>
/// 프로모션 기능 인터페이스
/// ClassicGame 이상의 모드에서 폰 승격 시 기물 선택 기능을 제공
/// </summary>
public interface Promotable {

    /// <summary>
    /// 폰 프로모션 시 승격할 기물 선택
    /// 1: 퀸, 2: 룩, 3: 비숍, 4: 나이트
    /// </summary>
    int choosePromotion(SimpleBoard board);
}
