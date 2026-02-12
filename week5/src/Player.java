import java.util.Scanner;

/// <summary>
/// 플레이어 추상 클래스
/// 모든 플레이어(사람, AI)의 공통 필드와 메서드를 정의
/// 각 하위 클래스가 자기만의 수 선택 방식을 구현
/// </summary>
public abstract class Player {

    // ========== 필드 ==========

    // 플레이어 색상 (Piece.RED 또는 Piece.BLUE)
    public int color;

    // 플레이어 이름 ("플레이어 1", "AI" 등)
    public String name;

    // 입력용 스캐너
    protected Scanner scanner;

    // ========== 생성자 ==========

    /// <summary>
    /// 플레이어 생성
    /// 색상, 이름, 입력 스캐너를 설정
    /// </summary>
    public Player(int color, String name, Scanner scanner) {
        this.color = color;
        this.name = name;
        this.scanner = scanner;
    }

    // ========== 추상 메서드 ==========

    /// <summary>
    /// 이번 턴에 둘 수를 선택하여 반환
    /// 각 하위 클래스가 자기만의 방식으로 구현 (메서드 오버라이딩)
    /// HumanPlayer: WASD로 커서 이동 + Enter로 선택
    /// AiPlayer: 알고리즘으로 결정
    /// null 반환 시 게임 종료 요청
    /// </summary>
    public abstract Move chooseMove(Board board);
}
