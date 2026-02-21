package player;

import board.SimpleBoard;
import core.Move;

/// <summary>
/// 사람 플레이어 (공식 모드)
/// 기본 조작(HumanInput)과 프로모션 선택 기능을 함께 제공
/// </summary>
public class ClassicHumanPlayer extends ClassicPlayer {

    // ========== 필드 ==========

    /// <summary>
    /// 키보드 입력 처리
    /// </summary>
    private final HumanInput input = new HumanInput();

    // ========== 생성자 ==========

    public ClassicHumanPlayer(int color, String name) {
        super(color, name);
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// 화살표 키 조작으로 수를 선택
    /// HumanInput에 위임
    /// </summary>
    @Override
    public Move chooseMove(SimpleBoard board) {
        return input.chooseMove(board, color, name);
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// 폰 프로모션 시 승격할 기물을 선택
    /// HumanInput에 위임
    /// </summary>
    @Override
    public int choosePromotion(SimpleBoard board) {
        return input.choosePromotion(board);
    }
}
