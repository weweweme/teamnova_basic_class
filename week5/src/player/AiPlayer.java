package player;

import board.*;
import core.*;

/// <summary>
/// AI 플레이어 (기본 모드)
/// 랜덤으로 수를 선택
/// 실제 전략 로직은 AiInput에 위임
/// </summary>
public class AiPlayer extends Player {

    // ========== 필드 ==========

    /// <summary>
    /// AI 전략 처리
    /// </summary>
    private final AiInput aiInput;

    // ========== 생성자 ==========

    public AiPlayer(int color, String name) {
        super(color, name);
        this.aiInput = new AiInput();
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
}
