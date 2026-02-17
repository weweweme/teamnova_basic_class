package piece;

import board.SimpleBoard;
import cell.Cell;

/// <summary>
/// 체스 기물 클래스
/// PieceFactory가 PieceType에 따라 이름, 기호, 가치, 이동 방향을 설정
/// 모든 기물이 동일한 데이터 기반 이동 시스템을 사용
/// </summary>
public class Piece {

    // ========== 색상 상수 ==========

    // 빨간팀 (하단, 선공)
    public static final int RED = 0;

    // 파란팀 (상단, 후공)
    public static final int BLUE = 1;

    // ========== 방향 상수 ==========

    // 빨간팀 전진 방향 (위쪽, 행 감소)
    public static final int RED_DIRECTION = -1;

    // 파란팀 전진 방향 (아래쪽, 행 증가)
    public static final int BLUE_DIRECTION = 1;

    // ========== 필드 ==========

    // 기물 종류 (PieceType 열거형으로 식별)
    public PieceType type;

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

    // 기물의 가치 (AI가 기물 잡기 우선순위를 정할 때 사용)
    // 퀸9 > 룩5 > 비숍3 = 나이트3 > 폰1, 킹은 0 (잡을 수 없음)
    public int value;

    // 방패 상태 (true이면 상대가 이 기물을 잡을 수 없음, 스킬 모드에서 사용)
    public boolean shielded;

    // 동결 상태 (true이면 이번 턴에 이동할 수 없음, 스킬 모드에서 사용)
    public boolean frozen;

    // ========== 이동 방향 (PieceFactory에서 설정) ==========

    /// <summary>
    /// 이 기물이 이동할 수 있는 방향 목록
    /// 각 원소는 {행 변화량, 열 변화량} 형태
    /// 예: {-1, 0}은 위쪽 한 칸, {1, 1}은 우하 대각선 한 칸
    /// </summary>
    int[][] directions;

    /// <summary>
    /// 이동 방향을 반복하는지 여부
    /// true: 한 방향으로 가로막힐 때까지 계속 전진 (퀸, 룩, 비숍)
    /// false: 한 방향으로 한 칸만 이동 (킹, 나이트)
    /// </summary>
    boolean repeatMove;

    /// <summary>
    /// 빈 칸으로만 이동 가능한 방향 (잡기 불가)
    /// 폰의 전진 이동에 사용
    /// 첫 이동이면 같은 방향으로 한 칸 더 이동 가능 (폰의 2칸 전진)
    /// </summary>
    int[][] moveOnlyDirections;

    /// <summary>
    /// 적군이 있을 때만 이동 가능한 방향 (빈 칸 이동 불가)
    /// 폰의 대각선 잡기에 사용
    /// </summary>
    int[][] captureOnlyDirections;

    // ========== 이동 버퍼 ==========

    // 한 기물이 가질 수 있는 최대 이동 가능 칸 수
    // 퀸이 빈 보드 중앙에서 최대 27칸 이동 가능, 여유분 포함
    public static final int MAX_MOVES = 28;

    // 이동 가능한 칸 버퍼 (매번 새로 만들지 않고 재사용)
    public final int[][] moveBuffer = new int[MAX_MOVES][SimpleBoard.COORD_SIZE];

    // 현재 유효한 이동 가능 칸 수 (moveBuffer에서 이 수만큼만 유효)
    public int moveCount;

    // ========== 생성자 ==========

    /// <summary>
    /// 기물 생성자
    /// 종류, 색상, 시작 위치를 받아 기물의 모든 속성을 자동 설정
    /// </summary>
    public Piece(PieceType type, int color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.hasMoved = false;
        this.shielded = false;
        this.frozen = false;
        PieceFactory.configure(this, type);
    }

    // ========== 이동 규칙 ==========

    /// <summary>
    /// 이 기물이 현재 보드에서 이동할 수 있는 모든 칸을 버퍼에 저장
    /// 결과는 moveBuffer에 저장되고, 반환값은 유효한 칸 수
    /// </summary>
    public int getValidMoves(Cell[][] board) {
        moveCount = 0;
        calculateMoves(board);
        return moveCount;
    }

    /// <summary>
    /// 이동 가능한 칸을 계산하여 버퍼에 저장
    /// 세 종류의 방향 설정에 따라 자동으로 이동 규칙을 적용:
    /// 1. directions: 이동과 잡기 모두 가능한 방향 (킹, 퀸, 룩, 비숍, 나이트)
    /// 2. moveOnlyDirections: 빈 칸으로만 이동 가능한 방향 (폰 전진)
    /// 3. captureOnlyDirections: 적군이 있을 때만 이동 가능한 방향 (폰 대각선)
    /// </summary>
    private void calculateMoves(Cell[][] board) {
        // 일반 방향 (이동 + 잡기 모두 가능)
        if (directions != null) {
            for (int[] dir : directions) {
                if (repeatMove) {
                    // 한 방향으로 가로막힐 때까지 계속 전진
                    slideMoves(board, dir[0], dir[1]);
                } else {
                    // 한 방향으로 한 칸만 이동
                    int r = row + dir[0];
                    int c = col + dir[1];

                    // 보드 범위 확인
                    boolean rowOutOfBounds = r < 0 || r >= 8;   // 행이 보드 범위를 넘는지
                    boolean colOutOfBounds = c < 0 || c >= 8;   // 열이 보드 범위를 넘는지
                    if (rowOutOfBounds || colOutOfBounds) {
                        continue;
                    }

                    // 빈 칸이거나 적군이면 이동 가능
                    if (board[r][c].isEmpty() || isEnemy(board[r][c].getPiece())) {
                        addMove(r, c);
                    }
                }
            }
        }

        // 이동 전용 방향 (빈 칸으로만 이동, 잡기 불가)
        if (moveOnlyDirections != null) {
            for (int[] dir : moveOnlyDirections) {
                int r = row + dir[0];
                int c = col + dir[1];

                // 보드 범위 확인
                boolean rowOutOfBounds = r < 0 || r >= 8;   // 행이 보드 범위를 넘는지
                boolean colOutOfBounds = c < 0 || c >= 8;   // 열이 보드 범위를 넘는지
                if (rowOutOfBounds || colOutOfBounds) {
                    continue;
                }

                // 빈 칸이 아니면 이동 불가 (잡기 불가)
                if (!board[r][c].isEmpty()) {
                    continue;
                }
                addMove(r, c);

                // 첫 이동이면 같은 방향으로 한 칸 더 이동 가능 (폰의 2칸 전진)
                if (!hasMoved) {
                    int r2 = row + dir[0] * 2;
                    int c2 = col + dir[1] * 2;

                    boolean r2OutOfBounds = r2 < 0 || r2 >= 8;   // 2칸 앞이 보드 범위를 넘는지
                    boolean c2OutOfBounds = c2 < 0 || c2 >= 8;   // 2칸 옆이 보드 범위를 넘는지
                    if (r2OutOfBounds || c2OutOfBounds) {
                        continue;
                    }

                    // 2칸 앞도 비어있어야 이동 가능 (1칸 앞은 이미 빈 칸 확인됨)
                    if (board[r2][c2].isEmpty()) {
                        addMove(r2, c2);
                    }
                }
            }
        }

        // 잡기 전용 방향 (적군이 있을 때만 이동)
        if (captureOnlyDirections != null) {
            for (int[] dir : captureOnlyDirections) {
                int r = row + dir[0];
                int c = col + dir[1];

                // 보드 범위 확인
                boolean rowOutOfBounds = r < 0 || r >= 8;   // 행이 보드 범위를 넘는지
                boolean colOutOfBounds = c < 0 || c >= 8;   // 열이 보드 범위를 넘는지
                if (rowOutOfBounds || colOutOfBounds) {
                    continue;
                }

                // 적군이 있을 때만 잡기 가능
                if (board[r][c].hasPiece() && isEnemy(board[r][c].getPiece())) {
                    addMove(r, c);
                }
            }
        }
    }

    /// <summary>
    /// 이동 가능한 칸을 버퍼에 추가
    /// calculateMoves에서 좌표를 저장할 때 사용
    /// </summary>
    private void addMove(int row, int col) {
        moveBuffer[moveCount][0] = row;
        moveBuffer[moveCount][1] = col;
        moveCount++;
    }

    // ========== 공통 메서드 ==========

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
    /// 결과를 버퍼에 추가
    /// </summary>
    private void slideMoves(Cell[][] board, int dRow, int dCol) {
        int r = row + dRow;
        int c = col + dCol;

        // 보드 범위 안에서 계속 전진
        while (r >= 0 && r < 8 && c >= 0 && c < 8) {
            if (board[r][c].isEmpty()) {
                // 빈 칸 → 이동 가능, 계속 전진
                addMove(r, c);
            } else {
                Piece target = board[r][c].getPiece();
                if (isEnemy(target)) {
                    // 적군 → 잡을 수 있음
                    addMove(r, c);
                }
                // 적군이든 아군이든 여기서 멈춤
                break;
            }

            r += dRow;
            c += dCol;
        }
    }
}
