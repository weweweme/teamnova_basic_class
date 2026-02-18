package skill;

import board.SimpleBoard;
import board.SkillBoard;
import core.Util;
import cell.Cell;
import piece.Piece;

/// <summary>
/// 부활 스킬
/// 잡힌 아군 기물을 자기 팀 뒷줄의 빈 칸에 부활 (킹 제외)
/// 사용 횟수: 1회
/// </summary>
public class ReviveSkill extends Skill {

    // ========== 필드 ==========

    // 잡힌 아군 기물 수 (SkillGame이 매 턴 갱신)
    // canUse에서 보드 객체 없이 판단하기 위해 외부에서 주입
    private int capturedCount = 0;

    // ========== 생성자 ==========

    public ReviveSkill() {
        super("부활", "잡힌 아군 기물을 뒷줄에 부활 (킹 제외)", 1);
    }

    // ========== 외부 정보 갱신 ==========

    /// <summary>
    /// 잡힌 아군 기물 수를 갱신
    /// 격자에 없는 정보이므로 SkillGame이 매 턴 시작 시 호출
    /// </summary>
    public void setCapturedCount(int count) {
        capturedCount = count;
    }

    // ========== 스킬 로직 ==========

    /// <summary>
    /// 사용 가능 여부: 잡힌 아군 기물이 있고 뒷줄에 빈 칸이 있어야 사용 가능
    /// 잡힌 기물 수는 capturedCount 필드로 확인 (격자에 없는 정보)
    /// </summary>
    @Override
    public boolean canUse(Cell[][] grid, int color) {
        if (!hasUses()) {
            return false;
        }

        // 잡힌 아군 기물이 없으면 사용 불가
        if (capturedCount == 0) {
            return false;
        }

        // 뒷줄에 빈 칸이 하나라도 있는지 확인
        int backRow = (color == Piece.RED) ? Util.BOARD_SIZE - 1 : 0;
        for (int c = 0; c < Util.BOARD_SIZE; c++) {
            if (grid[backRow][c].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /// <summary>
    /// 대상 찾기: 자기 팀 뒷줄(빨간팀: 7행, 파란팀: 0행)의 빈 칸 좌표
    /// 결과를 버퍼(targets, targetCount)에 저장하여 메모리 재사용
    /// </summary>
    @Override
    public void findTargets(Cell[][] grid, int color) {
        // 빨간팀 뒷줄: 7행(1번 줄), 파란팀 뒷줄: 0행(8번 줄)
        int backRow = (color == Piece.RED) ? Util.BOARD_SIZE - 1 : 0;
        targetCount = 0;

        for (int c = 0; c < Util.BOARD_SIZE; c++) {
            if (grid[backRow][c].isEmpty()) {
                targets[targetCount][0] = backRow;
                targets[targetCount][1] = c;
                targetCount++;
            }
        }
    }

    /// <summary>
    /// 효과: 부활 위치 지정 (실제 부활은 SkillGame에서 board.revivePiece 호출)
    /// 부활할 기물 선택은 Player.chooseReviveTarget()으로 별도 처리
    /// </summary>
    @Override
    public void execute(SimpleBoard board, int targetRow, int targetCol, int color) {
        // 이 메서드는 직접 호출되지 않음
        // SkillGame에서 부활 기물 선택 → 위치 선택 → board.revivePiece() 순서로 처리
        useCharge();
    }
}
