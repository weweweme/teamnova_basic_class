package piece;

/// <summary>
/// 기물 설정 팩토리
/// PieceType에 따라 기물의 이름, 기호, 가치, 이동 방향을 설정
/// 기물별 데이터를 한 곳에서 관리하여 Piece 클래스와 역할 분리
/// </summary>
public class PieceFactory {

    // ========== 기물별 이동 방향 상수 ==========

    // 킹: 8방향 (상하좌우 + 대각선) 각 1칸
    private static final int[][] KING_DIRECTIONS = {
        {-1, -1}, {-1, 0}, {-1, 1},   // 좌상, 상, 우상
        {0, -1},           {0, 1},     // 좌,       우
        {1, -1},  {1, 0},  {1, 1}     // 좌하, 하, 우하
    };

    // 퀸: 직선 4방향 + 대각선 4방향 = 8방향
    private static final int[][] QUEEN_DIRECTIONS = {
        {-1, 0},  {1, 0},  {0, -1}, {0, 1},    // 상, 하, 좌, 우
        {-1, -1}, {-1, 1}, {1, -1}, {1, 1}     // 좌상, 우상, 좌하, 우하
    };

    // 룩: 직선 4방향 (상하좌우)
    private static final int[][] ROOK_DIRECTIONS = {
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}   // 상, 하, 좌, 우
    };

    // 비숍: 대각선 4방향
    private static final int[][] BISHOP_DIRECTIONS = {
        {-1, -1}, {-1, 1}, {1, -1}, {1, 1}   // 좌상, 우상, 좌하, 우하
    };

    // 나이트: L자 이동 (한 방향 2칸 + 수직 방향 1칸 = 8가지 조합)
    private static final int[][] KNIGHT_DIRECTIONS = {
        {-2, -1}, {-2, 1},   // 위로 2칸 + 좌/우 1칸
        {-1, -2}, {-1, 2},   // 위로 1칸 + 좌/우 2칸
        {1, -2},  {1, 2},    // 아래로 1칸 + 좌/우 2칸
        {2, -1},  {2, 1}     // 아래로 2칸 + 좌/우 1칸
    };

    // ========== 설정 메서드 ==========

    /// <summary>
    /// 기물 종류에 따라 이름, 기호, 가치, 이동 방향을 설정
    /// 생성자에서 호출되며, 프로모션 시에도 호출하여 기물 종류를 변경
    /// </summary>
    public static void configure(Piece piece, PieceType type) {
        piece.type = type;

        switch (type) {
            case KING:
                piece.name = "킹";
                piece.description = "모든 방향으로 1칸 이동";
                piece.symbol = (piece.color == Piece.RED) ? "K" : "k";
                piece.value = 0;
                piece.directions = KING_DIRECTIONS;
                piece.repeatMove = false;
                break;

            case QUEEN:
                piece.name = "퀸";
                piece.description = "가로, 세로, 대각선으로 무제한 이동";
                piece.symbol = (piece.color == Piece.RED) ? "Q" : "q";
                piece.value = 9;
                piece.directions = QUEEN_DIRECTIONS;
                piece.repeatMove = true;
                break;

            case ROOK:
                piece.name = "룩";
                piece.description = "가로, 세로로 무제한 이동";
                piece.symbol = (piece.color == Piece.RED) ? "R" : "r";
                piece.value = 5;
                piece.directions = ROOK_DIRECTIONS;
                piece.repeatMove = true;
                break;

            case BISHOP:
                piece.name = "비숍";
                piece.description = "대각선으로 무제한 이동";
                piece.symbol = (piece.color == Piece.RED) ? "B" : "b";
                piece.value = 3;
                piece.directions = BISHOP_DIRECTIONS;
                piece.repeatMove = true;
                break;

            case KNIGHT:
                piece.name = "나이트";
                piece.description = "L자 형태로 이동 (다른 기물을 뛰어넘음)";
                piece.symbol = (piece.color == Piece.RED) ? "N" : "n";
                piece.value = 3;
                piece.directions = KNIGHT_DIRECTIONS;
                piece.repeatMove = false;
                break;

            case PAWN:
                piece.name = "폰";
                piece.description = "전진 1칸 (첫 수 2칸), 대각선으로 잡기";
                piece.symbol = (piece.color == Piece.RED) ? "P" : "p";
                piece.value = 1;
                // 폰은 일반 directions 대신 이동 전용/잡기 전용 방향 사용
                piece.directions = null;
                piece.repeatMove = false;
                // 빨간팀은 위로(-1), 파란팀은 아래로(+1) 전진
                int dir = (piece.color == Piece.RED) ? Piece.RED_DIRECTION : Piece.BLUE_DIRECTION;
                // 전진 방향 (빈 칸으로만 이동, 첫 이동 시 2칸 가능)
                piece.moveOnlyDirections = new int[][] {{dir, 0}};
                // 대각선 잡기 방향 (적군이 있을 때만 이동)
                piece.captureOnlyDirections = new int[][] {{dir, -1}, {dir, 1}};
                break;

            default:
                break;
        }
    }
}
