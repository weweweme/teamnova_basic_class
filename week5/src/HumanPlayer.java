import java.util.Scanner;

/// <summary>
/// 사람 플레이어
/// WASD로 커서를 이동하고 Enter로 기물을 선택/이동
/// 매 입력마다 화면을 지우고 보드를 다시 그림
/// </summary>
public class HumanPlayer extends Player {

    // ========== 생성자 ==========

    public HumanPlayer(int color, String name, Scanner scanner) {
        super(color, name, scanner);
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// WASD 커서 조작으로 수를 선택
    /// 1단계: 보드에서 자기 기물을 선택 (탐색 모드)
    /// 2단계: 선택한 기물의 이동 가능한 칸 중 도착지 선택 (이동 모드)
    /// q 입력 시 null 반환 (게임 종료)
    /// </summary>
    @Override
    public Move chooseMove(Board board) {
        // 커서 시작 위치 (자기 팀 킹 위치)
        int[] kingPos = board.findKing(color);
        int cursorRow = kingPos[0];
        int cursorCol = kingPos[1];

        // 1단계: 기물 선택 (탐색 모드)
        while (true) {
            Util.clearScreen();
            board.print(cursorRow, cursorCol);
            System.out.println();
            System.out.println(name + "의 차례 (" + getColorName() + ")");
            System.out.println("WASD: 이동 | Enter: 선택 | q: 종료");
            System.out.print(">> ");

            String input = scanner.nextLine().trim().toLowerCase();

            // 커서 이동
            int[] moved = moveCursor(cursorRow, cursorCol, input);
            if (moved != null) {
                cursorRow = moved[0];
                cursorCol = moved[1];
                continue;
            }

            // 게임 종료
            if (input.equals("q")) {
                return null;
            }

            // Enter (빈 입력) → 기물 선택 시도
            if (input.isEmpty()) {
                Piece piece = board.getPiece(cursorRow, cursorCol);

                // 자기 기물이 있는 칸인지 확인
                if (piece == null || piece.color != color) {
                    continue;
                }

                // 이동 가능한 수가 있는지 확인
                int[][] validMoves = board.getFilteredMoves(cursorRow, cursorCol);
                if (validMoves.length == 0) {
                    continue;
                }

                // 2단계: 도착지 선택 (이동 모드)
                int selectedRow = cursorRow;
                int selectedCol = cursorCol;
                Move result = chooseDest(board, selectedRow, selectedCol, validMoves);

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
    /// 선택한 기물의 이동 가능한 칸 중 도착지를 WASD로 선택
    /// q 입력 시 null 반환 (선택 취소, 1단계로 돌아감)
    /// </summary>
    private Move chooseDest(Board board, int selectedRow, int selectedCol, int[][] validMoves) {
        // 커서를 첫 번째 이동 가능한 칸으로 이동
        int cursorRow = validMoves[0][0];
        int cursorCol = validMoves[0][1];

        while (true) {
            Util.clearScreen();
            board.print(cursorRow, cursorCol, selectedRow, selectedCol, validMoves);
            System.out.println();
            Piece piece = board.getPiece(selectedRow, selectedCol);
            System.out.println(piece.name + " 선택됨 (" + Util.toNotation(selectedRow, selectedCol) + ")");
            System.out.println("WASD: 이동 | Enter: 확정 | q: 취소");
            System.out.print(">> ");

            String input = scanner.nextLine().trim().toLowerCase();

            // 커서 이동
            int[] moved = moveCursor(cursorRow, cursorCol, input);
            if (moved != null) {
                cursorRow = moved[0];
                cursorCol = moved[1];
                continue;
            }

            // 선택 취소
            if (input.equals("q")) {
                return null;
            }

            // Enter (빈 입력) → 도착지 확정 시도
            if (input.isEmpty()) {
                // 이동 가능한 칸인지 확인
                if (board.isInArray(cursorRow, cursorCol, validMoves)) {
                    return new Move(selectedRow, selectedCol, cursorRow, cursorCol);
                }
                // 이동 불가능한 칸이면 무시
            }
        }
    }

    // ========== 커서 이동 ==========

    /// <summary>
    /// WASD 입력에 따라 커서를 한 칸 이동
    /// 보드 범위(0~7)를 벗어나지 않도록 제한
    /// 이동했으면 새 좌표 반환, WASD가 아니면 null 반환
    /// </summary>
    private int[] moveCursor(int row, int col, String input) {
        int newRow = row;
        int newCol = col;

        switch (input) {
            case "w":
                newRow = row - 1;  // 위로 (행 감소)
                break;
            case "s":
                newRow = row + 1;  // 아래로 (행 증가)
                break;
            case "a":
                newCol = col - 1;  // 왼쪽 (열 감소)
                break;
            case "d":
                newCol = col + 1;  // 오른쪽 (열 증가)
                break;
            default:
                return null;  // WASD가 아닌 입력
        }

        // 보드 범위 확인
        if (newRow < 0 || newRow >= Board.SIZE || newCol < 0 || newCol >= Board.SIZE) {
            return null;  // 범위 밖이면 이동하지 않음
        }

        return new int[]{newRow, newCol};
    }

    // ========== 유틸 ==========

    /// <summary>
    /// 색상 이름 반환 ("빨간팀" 또는 "파란팀")
    /// </summary>
    private String getColorName() {
        if (color == Piece.RED) {
            return Util.RED + "빨간팀" + Util.RESET;
        }
        return Util.BLUE + "파란팀" + Util.RESET;
    }
}
