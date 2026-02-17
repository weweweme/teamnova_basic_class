package item;

import board.SimpleBoard;
import board.SkillBoard;
import piece.Piece;
import piece.King;

/// <summary>
/// 폭탄 아이템
/// 빈 칸에 설치. 상대 기물이 밟으면 해당 기물을 즉시 제거
/// 킹은 폭탄으로 제거할 수 없음 (체크메이트로만 잡을 수 있도록)
/// 설치 횟수: 1회
/// </summary>
public class BombItem extends Item {

    // ========== 생성자 ==========

    /// <summary>
    /// 인벤토리용 생성자 (설치 가능 횟수 관리)
    /// </summary>
    public BombItem() {
        super("폭탄", "빈 칸에 설치. 상대가 밟으면 기물 제거", 1);
    }

    /// <summary>
    /// 보드 배치용 생성자 (설치 위치 지정)
    /// </summary>
    public BombItem(int ownerColor, int row, int col) {
        super("폭탄", "빈 칸에 설치. 상대가 밟으면 기물 제거", ownerColor, row, col);
    }

    // ========== 아이템 효과 ==========

    /// <summary>
    /// 발동 효과: 밟은 기물을 보드에서 제거
    /// 킹이 밟으면 제거하지 않음 (아이템 소모만 됨)
    /// </summary>
    @Override
    public void trigger(SimpleBoard board, Piece steppedPiece) {
        // 킹은 폭탄으로 제거할 수 없음
        if (steppedPiece instanceof King) {
            return;
        }
        ((SkillBoard) board).removePiece(steppedPiece.row, steppedPiece.col);
    }

    /// <summary>
    /// 보드 표시 기호: * (설치자에게만 보임)
    /// </summary>
    @Override
    public String getSymbol() {
        return "*";
    }
}
