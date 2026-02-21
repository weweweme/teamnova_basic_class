package player;

import board.*;
import core.*;
import core.Chess;
import piece.Piece;

/// <summary>
/// 사람 플레이어 (기본 모드)
/// 화살표 키로 커서를 이동하고 Enter로 기물을 선택/이동
/// 매 입력마다 화면을 지우고 보드를 다시 그림
/// </summary>
public class HumanPlayer extends Player {

    // ========== 필드 ==========

    /// <summary>
    /// 선택한 이동 정보를 담는 객체 (매번 새로 만들지 않고 재사용)
    /// </summary>
    private final Move selectedMove = new Move(0, 0, 0, 0);

    // ========== 생성자 ==========

    public HumanPlayer(int color, String name) {
        super(color, name);
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// 화살표 키 조작으로 수를 선택
    /// 1단계: 보드에서 자기 기물을 선택 (탐색 모드)
    /// 2단계: 선택한 기물의 이동 가능한 칸 중 도착지 선택 (이동 모드)
    /// q 입력 시 null 반환 (게임 종료)
    /// </summary>
    @Override
    public Move chooseMove(SimpleBoard board) {
        // 커서 시작 위치 (자기 팀 킹 위치)
        Piece king = board.getKing(color);
        int cursorRow = king.row;
        int cursorCol = king.col;

        // 1단계: 기물 선택 (탐색 모드)
        while (true) {
            Util.clearScreen();
            board.print(cursorRow, cursorCol);
            System.out.println();
            System.out.println(name + "의 차례 (" + getColorName() + ")");
            // 체크 상태 경고
            if (board.isInCheck(color)) {
                System.out.println(">> 체크! 킹을 보호하세요!");
            }
            System.out.println("방향키: 이동 | Enter: 선택 | q: 종료");

            int key = Util.readKey();

            // 커서 이동
            int[] moved = moveCursor(cursorRow, cursorCol, key);
            if (moved != null) {
                cursorRow = moved[0];
                cursorCol = moved[1];
                continue;
            }

            // 게임 종료
            if (key == Util.KEY_QUIT) {
                return null;
            }

            // Enter → 기물 선택 시도
            if (key == Util.KEY_ENTER) {
                // 자기 기물이 있는 칸인지 확인
                if (board.grid[cursorRow][cursorCol].isEmpty()) {
                    continue;
                }
                Piece piece = board.grid[cursorRow][cursorCol].getPiece();
                if (piece.color != color) {
                    continue;
                }

                // 이동 가능한 수가 있는지 확인
                int validMoveCount = board.getFilteredMoves(cursorRow, cursorCol);
                if (validMoveCount == 0) {
                    continue;
                }
                int[][] validMoves = board.getFilteredBuffer();

                // 2단계: 도착지 선택 (이동 모드)
                Move result = chooseDest(board, cursorRow, cursorCol, validMoves, validMoveCount);

                if (result != null) {
                    // 이동 확정
                    return result;
                }
                // null이면 선택 취소 → 1단계로 돌아감
            }
        }
    }

    // ========== 도착지 선택 ==========

    /// <summary>
    /// 선택한 기물의 이동 가능한 칸 중 도착지를 화살표 키로 선택
    /// q 입력 시 null 반환 (선택 취소, 1단계로 돌아감)
    /// </summary>
    protected Move chooseDest(SimpleBoard board, int selectedRow, int selectedCol, int[][] validMoves, int validMoveCount) {
        // 커서를 첫 번째 이동 가능한 칸으로 이동
        int cursorRow = validMoves[0][0];
        int cursorCol = validMoves[0][1];

        while (true) {
            Util.clearScreen();
            board.print(cursorRow, cursorCol, selectedRow, selectedCol, validMoves, validMoveCount);
            System.out.println();
            Piece piece = board.grid[selectedRow][selectedCol].getPiece();
            System.out.println(piece.name + " 선택됨 (" + Chess.toNotation(selectedRow, selectedCol) + ")");
            System.out.println("방향키: 이동 | Enter: 확정 | q: 취소");

            int key = Util.readKey();

            // 커서 이동
            int[] moved = moveCursor(cursorRow, cursorCol, key);
            if (moved != null) {
                cursorRow = moved[0];
                cursorCol = moved[1];
                continue;
            }

            // 선택 취소
            if (key == Util.KEY_QUIT) {
                return null;
            }

            // Enter → 도착지 확정 시도
            if (key == Util.KEY_ENTER) {
                // 이동 가능한 칸인지 확인
                if (board.isInArray(cursorRow, cursorCol, validMoves, validMoveCount)) {
                    selectedMove.set(selectedRow, selectedCol, cursorRow, cursorCol);
                    return selectedMove;
                }
                // 이동 불가능한 칸이면 무시
            }
        }
    }

    // ========== 커서 이동 ==========

    /// <summary>
    /// 화살표 키 입력에 따라 커서를 한 칸 이동
    /// 보드 범위(0~7)를 벗어나지 않도록 제한
    /// 이동했으면 새 좌표 반환, 화살표 키가 아니면 null 반환
    /// </summary>
    protected int[] moveCursor(int row, int col, int key) {
        int newRow = row;
        int newCol = col;

        switch (key) {
            case Util.KEY_UP:
                newRow = row - 1;  // 위로 (행 감소)
                break;
            case Util.KEY_DOWN:
                newRow = row + 1;  // 아래로 (행 증가)
                break;
            case Util.KEY_LEFT:
                newCol = col - 1;  // 왼쪽 (열 감소)
                break;
            case Util.KEY_RIGHT:
                newCol = col + 1;  // 오른쪽 (열 증가)
                break;
            default:
                return null;  // 화살표 키가 아닌 입력
        }

        // 보드 범위 확인
        boolean rowOutOfBounds = newRow < 0 || newRow >= Chess.BOARD_SIZE;   // 행이 보드 범위를 넘는지
        boolean colOutOfBounds = newCol < 0 || newCol >= Chess.BOARD_SIZE;   // 열이 보드 범위를 넘는지
        if (rowOutOfBounds || colOutOfBounds) {
            return null;  // 범위 밖이면 이동하지 않음
        }

        return new int[]{newRow, newCol};
    }

    // ========== 유틸 ==========

    /// <summary>
    /// 색상 이름 반환 ("빨간팀" 또는 "파란팀")
    /// </summary>
    protected String getColorName() {
        if (color == Piece.RED) {
            return Util.RED + "빨간팀" + Util.RESET;
        }
        return Util.BLUE + "파란팀" + Util.RESET;
    }
}
