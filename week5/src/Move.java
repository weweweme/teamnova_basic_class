/// <summary>
/// 이동 정보 클래스
/// 출발 칸과 도착 칸의 좌표를 담는 단순 데이터 클래스
/// </summary>
public class Move {

    // ========== 필드 ==========

    // 출발 행 (0~7)
    public int fromRow;

    // 출발 열 (0~7)
    public int fromCol;

    // 도착 행 (0~7)
    public int toRow;

    // 도착 열 (0~7)
    public int toCol;

    // ========== 생성자 ==========

    /// <summary>
    /// 이동 정보 생성
    /// 출발 위치와 도착 위치를 지정
    /// </summary>
    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }

    // ========== 메서드 ==========

    /// <summary>
    /// 이동 정보를 체스 표기법으로 변환
    /// 예: "e2 → e4"
    /// </summary>
    public String toNotation() {
        return Util.toNotation(fromRow, fromCol) + " → " + Util.toNotation(toRow, toCol);
    }
}
