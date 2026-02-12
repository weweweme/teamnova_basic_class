import java.util.ArrayList;

/// <summary>
/// 기물 추상 클래스
/// 모든 체스 기물의 공통 필드와 메서드를 정의
/// 각 하위 클래스(King, Queen 등)가 자기만의 이동 규칙을 구현
/// </summary>
public abstract class Piece {

    // ========== 색상 상수 ==========

    // 빨간팀 (하단, 선공)
    public static final int RED = 0;

    // 파란팀 (상단, 후공)
    public static final int BLUE = 1;

    // ========== 필드 ==========

    // 기물의 색상 (RED 또는 BLUE)
    public int color;

    // 현재 행 위치 (0이 위쪽 8번 줄, 7이 아래쪽 1번 줄)
    public int row;

    // 현재 열 위치 (0이 왼쪽 a열, 7이 오른쪽 h열)
    public int col;

    // 기물 이름 ("킹", "퀸" 등)
    public String name;

    // 기물 이니셜 ("K", "q" 등 - 대문자는 빨간팀, 소문자는 파란팀)
    public String symbol;

    // 이 기물이 한 번이라도 움직였는지 (캐슬링, 폰 첫 이동 판정에 사용)
    public boolean hasMoved;

    // 이동 가능한 칸 목록 (매번 새로 만들지 않고 재사용)
    protected ArrayList<int[]> moves = new ArrayList<>();

    // ========== 생성자 ==========

    /// <summary>
    /// 기물 생성자
    /// 색상과 시작 위치를 설정
    /// name과 symbol은 각 하위 클래스에서 설정
    /// </summary>
    public Piece(int color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.hasMoved = false;
    }

    // ========== 이동 규칙 ==========

    /// <summary>
    /// 이 기물이 현재 보드에서 이동할 수 있는 모든 칸의 좌표를 반환
    /// 초기화와 변환은 여기서 처리하고, 실제 이동 규칙은 하위 클래스가 구현
    /// </summary>
    public int[][] getValidMoves(Piece[][] board) {
        moves.clear();
        calculateMoves(board);
        return moves.toArray(new int[0][]);
    }

    /// <summary>
    /// 실제 이동 가능한 칸을 계산하는 메서드
    /// 각 하위 클래스가 자기만의 이동 규칙으로 구현 (메서드 오버라이딩)
    /// 결과는 moves 목록에 추가
    /// </summary>
    protected abstract void calculateMoves(Piece[][] board);

    // ========== 공통 메서드 ==========

    /// <summary>
    /// 같은 편인지 확인
    /// </summary>
    public boolean isAlly(Piece other) {
        return other != null && this.color == other.color;
    }

    /// <summary>
    /// 적군인지 확인 (상대가 존재하고 색상이 다른 경우)
    /// </summary>
    public boolean isEnemy(Piece other) {
        return other != null && this.color != other.color;
    }

    // ========== 이동 헬퍼 ==========

    /// <summary>
    /// 한 방향으로 직선 이동 가능한 칸들을 구함 (퀸, 룩, 비숍 공통)
    /// 빈 칸이면 계속 전진, 적군이면 잡고 멈춤, 아군이면 멈춤
    /// 결과를 전달받은 목록에 추가
    /// </summary>
    protected void slideMoves(Piece[][] board, int dRow, int dCol, ArrayList<int[]> moves) {
        int r = row + dRow;
        int c = col + dCol;

        // 보드 범위 안에서 계속 전진
        while (r >= 0 && r < 8 && c >= 0 && c < 8) {
            Piece target = board[r][c];

            if (target == null) {
                // 빈 칸 → 이동 가능, 계속 전진
                moves.add(new int[]{r, c});
            } else if (isEnemy(target)) {
                // 적군 → 잡을 수 있음, 여기서 멈춤
                moves.add(new int[]{r, c});
                break;
            } else {
                // 아군 → 이동 불가, 여기서 멈춤
                break;
            }

            r += dRow;
            c += dCol;
        }
    }
}
