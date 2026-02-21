package player;

import board.SimpleBoard;
import core.Chess;
import core.Move;

/// <summary>
/// AI 플레이어 (공식 모드)
/// AI 전략(AiInput)과 프로모션 선택 기능을 함께 제공
/// </summary>
public class ClassicAiPlayer extends ClassicPlayer {

    // ========== 필드 ==========

    /// <summary>
    /// AI 전략 처리
    /// </summary>
    private final AiInput aiInput;

    // ========== 생성자 ==========

    public ClassicAiPlayer(int color, String name, int difficulty) {
        super(color, name);
        this.aiInput = new AiInput(difficulty);
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// AI가 수를 선택
    /// AiInput에 위임
    /// </summary>
    @Override
    public Move chooseMove(SimpleBoard board) {
        return aiInput.chooseMove(board, color, name);
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// AI는 항상 퀸으로 승격 (가장 강한 기물)
    /// </summary>
    @Override
    public int choosePromotion(SimpleBoard board) {
        return Chess.PROMOTE_QUEEN;
    }
}
