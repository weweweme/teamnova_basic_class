package core;

import piece.Piece;

/// <summary>
/// 체스판의 한 칸을 나타내는 기본 클래스
/// 기물만 관리하는 일반 모드용
/// 스킬 모드에서는 SkillCell이 이 클래스를 상속하여 아이템 등을 추가
/// </summary>
public class Cell {

    // ========== 필드 ==========

    // 이 칸에 있는 기물 (비어있으면 null)
    private Piece piece;

    // ========== 생성자 ==========

    /// <summary>
    /// 빈 칸 생성
    /// </summary>
    public Cell() {
        this.piece = null;
    }

    // ========== 기물 관련 ==========

    /// <summary>
    /// 이 칸의 기물 반환 (없으면 null)
    /// </summary>
    public Piece getPiece() {
        return piece;
    }

    /// <summary>
    /// 이 칸에 기물 배치 (제거하려면 null 전달)
    /// </summary>
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /// <summary>
    /// 이 칸에 기물이 있는지 확인
    /// </summary>
    public boolean hasPiece() {
        return piece != null;
    }

    /// <summary>
    /// 이 칸이 비어있는지 확인 (기물 없음)
    /// </summary>
    public boolean isEmpty() {
        return piece == null;
    }
}
