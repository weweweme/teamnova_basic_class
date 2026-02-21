package player;

import board.*;
import core.*;

/// <summary>
/// AI 플레이어 (기본 모드)
/// 난이도에 따라 다른 전략으로 수를 선택
/// 실제 전략 로직은 AiInput에 위임
/// </summary>
public class AiPlayer extends Player {

    // ========== 난이도 상수 ==========

    // 쉬움 (랜덤 선택)
    public static final int EASY = 0;

    // 보통 (우선순위 기반 전략)
    public static final int NORMAL = 1;

    // ========== 필드 ==========

    /// <summary>
    /// AI 전략 처리
    /// </summary>
    private final AiInput aiInput;

    // ========== 생성자 ==========

    public AiPlayer(int color, String name, int difficulty) {
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
}
