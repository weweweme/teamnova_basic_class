/// <summary>
/// 사람 플레이어
/// 화살표 키로 커서를 이동하고 Enter로 기물을 선택/이동
/// 매 입력마다 화면을 지우고 보드를 다시 그림
/// </summary>
public class HumanPlayer extends Player {

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
    /// 선택한 기물의 이동 가능한 칸 중 도착지를 화살표 키로 선택
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
                if (board.isInArray(cursorRow, cursorCol, validMoves)) {
                    return new Move(selectedRow, selectedCol, cursorRow, cursorCol);
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
    private int[] moveCursor(int row, int col, int key) {
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
        if (newRow < 0 || newRow >= Board.SIZE || newCol < 0 || newCol >= Board.SIZE) {
            return null;  // 범위 밖이면 이동하지 않음
        }

        return new int[]{newRow, newCol};
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// 폰 프로모션 시 승격할 기물을 선택
    /// 숫자 키(1~4)로 선택
    /// </summary>
    @Override
    public int choosePromotion(Board board) {
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println("프로모션! 승격할 기물을 선택하세요:");
        System.out.println("[1] 퀸  [2] 룩  [3] 비숍  [4] 나이트");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key >= 1 && key <= 4) {
                return key;
            }
        }
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
