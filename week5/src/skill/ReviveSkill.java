package skill;

import java.util.ArrayList;
import core.Board;
import piece.Piece;

/// <summary>
/// 부활 스킬
/// 잡힌 아군 기물을 자기 팀 뒷줄의 빈 칸에 부활 (킹 제외)
/// 사용 횟수: 1회
/// </summary>
public class ReviveSkill extends Skill {

    // ========== 생성자 ==========

    public ReviveSkill() {
        super("부활", "잡힌 아군 기물을 뒷줄에 부활 (킹 제외)", 1);
    }

    // ========== 스킬 로직 ==========

    /// <summary>
    /// 사용 가능 여부: 잡힌 아군 기물이 있고 뒷줄에 빈 칸이 있어야 사용 가능
    /// </summary>
    @Override
    public boolean canUse(Board board, int color) {
        if (!hasUses()) {
            return false;
        }

        // 잡힌 아군 기물이 있는지
        Piece[] captured = board.getCapturedPieces(color);
        if (captured.length == 0) {
            return false;
        }

        // 뒷줄에 빈 칸이 있는지
        int[][] targets = getTargets(board, color);
        return targets.length > 0;
    }

    /// <summary>
    /// 대상: 자기 팀 뒷줄(빨간팀: 7행, 파란팀: 0행)의 빈 칸 좌표
    /// </summary>
    @Override
    public int[][] getTargets(Board board, int color) {
        // 빨간팀 뒷줄: 7행(1번 줄), 파란팀 뒷줄: 0행(8번 줄)
        int backRow = (color == Piece.RED) ? 7 : 0;
        ArrayList<int[]> targets = new ArrayList<>();

        for (int c = 0; c < Board.SIZE; c++) {
            if (board.getPiece(backRow, c) == null) {
                targets.add(new int[]{backRow, c});
            }
        }
        return targets.toArray(new int[0][]);
    }

    /// <summary>
    /// 효과: 부활 위치 지정 (실제 부활은 SkillGame에서 board.revivePiece 호출)
    /// 부활할 기물 선택은 Player.chooseReviveTarget()으로 별도 처리
    /// </summary>
    @Override
    public void execute(Board board, int targetRow, int targetCol, int color) {
        // 이 메서드는 직접 호출되지 않음
        // SkillGame에서 부활 기물 선택 → 위치 선택 → board.revivePiece() 순서로 처리
        useCharge();
    }
}
