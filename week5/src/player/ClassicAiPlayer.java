package player;

import board.SimpleBoard;

/// <summary>
/// AI 플레이어 (공식 모드)
/// 기본 AI(AiPlayer)에 프로모션 선택 기능을 추가
/// </summary>
public class ClassicAiPlayer extends AiPlayer implements Promotable {

    // ========== 생성자 ==========

    public ClassicAiPlayer(int color, String name, int difficulty) {
        super(color, name, difficulty);
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// AI는 항상 퀸으로 승격 (가장 강한 기물)
    /// </summary>
    @Override
    public int choosePromotion(SimpleBoard board) {
        return 1;
    }
}
