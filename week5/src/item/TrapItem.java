package item;

import board.SimpleBoard;
import piece.Piece;
import piece.SkillPiece;

/// <summary>
/// 함정 아이템
/// 빈 칸에 설치. 상대 기물이 밟으면 1턴 동결 (다음 턴에 이동 불가)
/// 설치 횟수: 1회
/// </summary>
public class TrapItem extends Item {

    // ========== 생성자 ==========

    /// <summary>
    /// 인벤토리용 생성자 (설치 가능 횟수 관리)
    /// </summary>
    public TrapItem() {
        super("함정", "빈 칸에 설치. 상대가 밟으면 1턴 동결", 1);
    }

    /// <summary>
    /// 보드 배치용 생성자 (설치 위치 지정)
    /// </summary>
    public TrapItem(int ownerColor, int row, int col) {
        super("함정", "빈 칸에 설치. 상대가 밟으면 1턴 동결", ownerColor, row, col);
    }

    // ========== 아이템 효과 ==========

    /// <summary>
    /// 발동 효과: 밟은 기물을 동결 상태로 만듦 (다음 턴에 이동 불가)
    /// </summary>
    @Override
    public void trigger(SimpleBoard board, Piece steppedPiece) {
        ((SkillPiece) steppedPiece).frozen = true;
    }

    /// <summary>
    /// 보드 표시 기호: ^
    /// </summary>
    @Override
    public String getSymbol() {
        return "^";
    }
}
