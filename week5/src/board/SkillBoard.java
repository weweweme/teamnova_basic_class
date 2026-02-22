package board;

import cell.Cell;
import cell.SkillCell;
import core.Chess;
import core.Util;
import java.util.ArrayList;
import piece.*;
import item.Item;

/// <summary>
/// 스킬 모드 보드
/// 공식 체스 규칙(ClassicBoard) + 아이템/스킬 시스템 추가
/// 방패, 동결, 기물 제거/부활, 아이템 설치/발동 기능 제공
/// 기물은 SkillPiece로 생성되어 방패/동결 상태를 자체적으로 보유
/// </summary>
public class SkillBoard extends ClassicBoard {

    // ========== 상수 ==========

    /// <summary>
    /// 아이템이 발동되지 않았음을 나타내는 반환값
    /// </summary>
    public static final String NOT_TRIGGERED = "";

    // ========== 생성자 ==========

    public SkillBoard() {
        super();
    }

    // ========== 팩토리 메서드 ==========

    /// <summary>
    /// 스킬 모드용 칸(SkillCell) 생성
    /// 아이템 등 스킬 모드 전용 속성을 지원하는 칸으로 격자를 채움
    /// </summary>
    @Override
    protected Cell createCell() {
        return new SkillCell();
    }

    /// <summary>
    /// 스킬 모드용 기물(SkillPiece) 생성
    /// 방패/동결 상태를 지원하는 기물로 생성
    /// </summary>
    @Override
    protected Piece createPiece(PieceType type, int color, int row, int col) {
        return new SkillPiece(type, color, row, col);
    }

    /// <summary>
    /// 격자의 칸을 SkillCell로 반환 (아이템 접근용)
    /// </summary>
    private SkillCell skillCell(int r, int c) {
        return (SkillCell) grid[r][c];
    }

    // ========== 훅 메서드 오버라이드 ==========

    /// <summary>
    /// 동결된 기물은 이동 불가
    /// </summary>
    @Override
    protected boolean isMovementBlocked(Piece piece) {
        return ((SkillPiece) piece).frozen;
    }

    /// <summary>
    /// 방패가 걸린 기물은 잡기 불가
    /// </summary>
    @Override
    protected boolean isCaptureBlocked(Piece target) {
        return ((SkillPiece) target).shielded;
    }

    // ========== 보드 출력 (스킬 모드) ==========

    /// <summary>
    /// 한 칸의 표시 문자열 결정 (스킬 모드 확장)
    /// 방패(!), 동결(~), 아이템(*/^)을 항상 표시
    /// </summary>
    @Override
    protected String renderCell(int r, int c, int cursorRow, int cursorCol, int selectedRow, int selectedCol, int[][] validMoves) {
        boolean hasPiece = grid[r][c].hasPiece();
        boolean isCursor = (r == cursorRow && c == cursorCol);
        boolean isSelected = (r == selectedRow && c == selectedCol);
        boolean isValidMove = isInArray(r, c, validMoves, validMoveCount);

        // 1순위: 커서 또는 선택된 기물 → 대괄호로 감싸기
        if (isCursor || isSelected) {
            if (hasPiece) {
                Piece piece = grid[r][c].getPiece();
                String colorCode = (piece.color == Piece.RED) ? Util.RED : Util.BLUE;
                return "[" + colorCode + piece.symbol + Util.RESET + "]";
            }
            return "[ ]";
        }

        // 2순위: 이동 가능한 칸 → · 표시
        if (isValidMove) {
            return " · ";
        }

        // 3순위: 기물이 있는 칸 (방패/동결 효과 표시)
        if (hasPiece) {
            SkillPiece piece = (SkillPiece) grid[r][c].getPiece();
            String colorCode = (piece.color == Piece.RED) ? Util.RED : Util.BLUE;
            // 방패 표시: 기호 앞에 ! 표시
            String prefix = piece.shielded ? "!" : " ";
            // 동결 표시: 기호 뒤에 ~ 표시
            String suffix = piece.frozen ? "~" : " ";
            return prefix + colorCode + piece.symbol + Util.RESET + suffix;
        }

        // 4순위: 아이템이 설치된 빈 칸 (양쪽 모두에게 보임)
        if (skillCell(r, c).hasItem()) {
            Item item = skillCell(r, c).getItem();
            String colorCode = (item.ownerColor == Piece.RED) ? Util.RED : Util.BLUE;
            return " " + colorCode + item.getSymbol() + Util.RESET + " ";
        }

        return "   ";
    }

    // ========== 아이템 관리 ==========

    /// <summary>
    /// 보드에 아이템을 설치
    /// </summary>
    public void placeItem(Item item) {
        skillCell(item.row, item.col).setItem(item);
    }

    /// <summary>
    /// 지정한 칸의 아이템 반환 (없으면 null)
    /// </summary>
    public Item getItem(int row, int col) {
        boolean rowOutOfBounds = row < 0 || row >= Chess.BOARD_SIZE;   // 행이 보드 범위를 넘는지
        boolean colOutOfBounds = col < 0 || col >= Chess.BOARD_SIZE;   // 열이 보드 범위를 넘는지
        if (rowOutOfBounds || colOutOfBounds) {
            return null;
        }
        return skillCell(row, col).getItem();
    }

    /// <summary>
    /// 이동 후 도착 칸에 상대 아이템이 있으면 발동
    /// 자기 아이템 위에는 발동하지 않음 (설치자는 자기 아이템을 밟아도 안전)
    /// 발동 후 아이템 제거
    /// 반환값: 발동된 아이템 이름 (발동되지 않았으면 빈 문자열)
    /// </summary>
    public String triggerItem(int row, int col) {
        SkillCell cell = skillCell(row, col);

        // 아이템이 없거나 기물이 없거나 자기 아이템이면 무시
        if (!cell.hasItem() || cell.isEmpty()) {
            return NOT_TRIGGERED;
        }
        Item item = cell.getItem();
        if (item.ownerColor == cell.getPiece().color) {
            return NOT_TRIGGERED;
        }

        // 아이템 효과 발동
        String itemName = item.name;
        item.trigger(this, cell.getPiece());

        // 발동된 아이템 제거
        cell.removeItem();

        return itemName;
    }

    // ========== 효과 관리 ==========

    /// <summary>
    /// 특정 색상의 모든 기물에서 방패 상태 해제
    /// 자기 턴 시작 시 호출 (지난 턴에 건 방패를 해제)
    /// </summary>
    public void clearShields(int color) {
        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                if (grid[r][c].hasPiece()) {
                    SkillPiece piece = (SkillPiece) grid[r][c].getPiece();
                    if (piece.color == color && piece.shielded) {
                        piece.shielded = false;
                    }
                }
            }
        }
    }

    /// <summary>
    /// 특정 색상의 모든 기물에서 동결 상태 해제
    /// 동결된 플레이어의 턴 시작 시 호출
    /// </summary>
    public void clearFreezes(int color) {
        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                if (grid[r][c].hasPiece()) {
                    SkillPiece piece = (SkillPiece) grid[r][c].getPiece();
                    if (piece.color == color && piece.frozen) {
                        piece.frozen = false;
                    }
                }
            }
        }
    }

    /// <summary>
    /// 특정 색상에 동결되지 않은 기물이 있는지 확인
    /// 모든 기물이 동결되면 턴을 넘겨야 함
    /// </summary>
    public boolean hasUnfrozenPieces(int color) {
        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                if (grid[r][c].hasPiece()) {
                    SkillPiece piece = (SkillPiece) grid[r][c].getPiece();
                    if (piece.color == color && !piece.frozen) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ========== 기물 제거/부활 ==========

    /// <summary>
    /// 지정한 칸의 기물을 제거하고 잡힌 기물 목록에 추가
    /// 파괴 스킬, 폭탄 아이템에서 사용
    /// </summary>
    public void removePiece(int row, int col) {
        if (grid[row][col].hasPiece()) {
            capturedPieces.add(grid[row][col].getPiece());
            grid[row][col].removePiece();
        }
    }

    /// <summary>
    /// 잡힌 기물 중 특정 색상의 기물 수 반환 (킹 제외)
    /// 배열을 생성하지 않고 수만 세어 반환
    /// </summary>
    public int getCapturedCount(int color) {
        int count = 0;
        for (Piece p : capturedPieces) {
            if (p.color == color && p.type != PieceType.KING) {
                count++;
            }
        }
        return count;
    }

    /// <summary>
    /// 잡힌 기물 중 특정 색상의 기물 목록 반환 (킹 제외)
    /// 부활 스킬에서 부활 대상 선택 시 사용
    /// </summary>
    public Piece[] getCapturedPieces(int color) {
        ArrayList<Piece> result = new ArrayList<>();
        for (Piece p : capturedPieces) {
            if (p.color == color && p.type != PieceType.KING) {
                result.add(p);
            }
        }
        return result.toArray(new Piece[0]);
    }

    /// <summary>
    /// 잡힌 기물을 지정한 위치에 부활
    /// 잡힌 기물 목록에서 제거하고 보드에 배치
    /// 방패/동결 상태를 초기화
    /// </summary>
    public void revivePiece(Piece piece, int row, int col) {
        capturedPieces.remove(piece);
        grid[row][col].setPiece(piece);
        piece.row = row;
        piece.col = col;
        piece.hasMoved = true;  // 부활한 기물은 이동한 것으로 처리
        SkillPiece skillPiece = (SkillPiece) piece;
        skillPiece.shielded = false;
        skillPiece.frozen = false;
    }
}
