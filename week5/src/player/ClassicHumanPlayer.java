package player;

import board.SimpleBoard;
import core.Util;

/// <summary>
/// 사람 플레이어 (공식 모드)
/// 기본 모드의 조작(HumanPlayer)에 프로모션 선택 기능을 추가
/// </summary>
public class ClassicHumanPlayer extends HumanPlayer implements Promotable {

    // ========== 생성자 ==========

    public ClassicHumanPlayer(int color, String name) {
        super(color, name);
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// 폰 프로모션 시 승격할 기물을 선택
    /// 숫자 키(1~4)로 선택
    /// </summary>
    @Override
    public int choosePromotion(SimpleBoard board) {
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println("프로모션! 승격할 기물을 선택하세요:");
        System.out.println("[" + Util.PROMOTE_QUEEN + "] 퀸  ["
                + Util.PROMOTE_ROOK + "] 룩  ["
                + Util.PROMOTE_BISHOP + "] 비숍  ["
                + Util.PROMOTE_KNIGHT + "] 나이트");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key >= Util.PROMOTE_QUEEN && key <= Util.PROMOTE_KNIGHT) {
                return key;
            }
        }
    }
}
