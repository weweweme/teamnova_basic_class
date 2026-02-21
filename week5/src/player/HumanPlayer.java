package player;

import board.*;
import core.*;

/// <summary>
/// 사람 플레이어 (기본 모드)
/// 화살표 키로 커서를 이동하고 Enter로 기물을 선택/이동
/// 실제 입력 처리는 HumanInput에 위임
/// </summary>
public class HumanPlayer extends Player {

    // ========== 필드 ==========

    /// <summary>
    /// 키보드 입력 처리
    /// </summary>
    private final HumanInput input = new HumanInput();

    // ========== 생성자 ==========

    public HumanPlayer(int color, String name) {
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
}
