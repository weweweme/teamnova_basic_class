package player;

import board.*;
import core.*;

import java.util.ArrayList;

/// <summary>
/// AI 플레이어용 수 선택 전략
/// 합법적인 수 중 랜덤으로 선택
/// </summary>
public class AiInput {

    // ========== 수 선택 ==========

    /// <summary>
    /// AI가 수를 선택
    /// 합법적인 수 중 랜덤으로 선택
    /// </summary>
    public Move chooseMove(SimpleBoard board, int color, String name) {
        // AI 턴에도 현재 보드를 보여줌
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println(name + "이(가) 생각 중...");

        // 생각하는 시간 연출
        Util.delay(1000);

        // 모든 합법적인 수 가져오기
        ArrayList<Move> allMoves = board.getAllValidMoves(color);

        // 수가 없으면 null (체크메이트 또는 스테일메이트 상태)
        if (allMoves.isEmpty()) {
            return null;
        }

        // 랜덤 선택
        return allMoves.get(Util.rand(allMoves.size()));
    }
}
