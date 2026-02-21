package core;

/// <summary>
/// 체스 게임 전용 상수 및 유틸리티
/// 체스판 크기, 좌표, 프로모션, 스킬 모드 등 체스 규칙에 관련된 상수 모음
/// 범용 기능(입력, 화면, 딜레이)은 Util 클래스에 분리되어 있음
/// </summary>
public class Chess {

    // ========== 체스판 크기 ==========

    /// <summary>
    /// 체스판 한 변의 칸 수 (8x8 정사각형 격자)
    /// </summary>
    public static final int BOARD_SIZE = 8;

    /// <summary>
    /// 한 팀이 가질 수 있는 최대 기물 수 (킹1 + 퀸1 + 룩2 + 비숍2 + 나이트2 + 폰8 = 16)
    /// </summary>
    public static final int MAX_PIECES_PER_SIDE = 16;

    /// <summary>
    /// 하나의 좌표를 이루는 값의 수 (행, 열 = 2개)
    /// </summary>
    public static final int COORD_SIZE = 2;

    // ========== 체스판 행 위치 ==========

    // 체스에서 8번 줄이 맨 위(파란팀), 1번 줄이 맨 아래(빨간팀)

    /// <summary>
    /// 8번 줄 (파란팀 주요 기물)
    /// </summary>
    public static final int ROW_8 = 0;

    /// <summary>
    /// 7번 줄 (파란팀 폰)
    /// </summary>
    public static final int ROW_7 = 1;

    /// <summary>
    /// 6번 줄
    /// </summary>
    public static final int ROW_6 = 2;

    /// <summary>
    /// 5번 줄
    /// </summary>
    public static final int ROW_5 = 3;

    /// <summary>
    /// 4번 줄
    /// </summary>
    public static final int ROW_4 = 4;

    /// <summary>
    /// 3번 줄
    /// </summary>
    public static final int ROW_3 = 5;

    /// <summary>
    /// 2번 줄 (빨간팀 폰)
    /// </summary>
    public static final int ROW_2 = 6;

    /// <summary>
    /// 1번 줄 (빨간팀 주요 기물)
    /// </summary>
    public static final int ROW_1 = 7;

    // ========== 체스판 열 위치 ==========

    /// <summary>
    /// a열
    /// </summary>
    public static final int COL_A = 0;

    /// <summary>
    /// b열
    /// </summary>
    public static final int COL_B = 1;

    /// <summary>
    /// c열
    /// </summary>
    public static final int COL_C = 2;

    /// <summary>
    /// d열
    /// </summary>
    public static final int COL_D = 3;

    /// <summary>
    /// e열
    /// </summary>
    public static final int COL_E = 4;

    /// <summary>
    /// f열
    /// </summary>
    public static final int COL_F = 5;

    /// <summary>
    /// g열
    /// </summary>
    public static final int COL_G = 6;

    /// <summary>
    /// h열
    /// </summary>
    public static final int COL_H = 7;

    // ========== 프로모션 선택 상수 ==========

    /// <summary>
    /// 퀸으로 승격
    /// </summary>
    public static final int PROMOTE_QUEEN = 1;

    /// <summary>
    /// 룩으로 승격
    /// </summary>
    public static final int PROMOTE_ROOK = 2;

    /// <summary>
    /// 비숍으로 승격
    /// </summary>
    public static final int PROMOTE_BISHOP = 3;

    /// <summary>
    /// 나이트로 승격
    /// </summary>
    public static final int PROMOTE_KNIGHT = 4;

    // ========== 행동 선택 상수 (스킬 모드) ==========

    /// <summary>
    /// 기물 이동
    /// </summary>
    public static final int ACTION_MOVE = 0;

    /// <summary>
    /// 스킬 사용
    /// </summary>
    public static final int ACTION_SKILL = 1;

    /// <summary>
    /// 아이템 설치
    /// </summary>
    public static final int ACTION_ITEM = 2;

    // ========== 스킬 배열 인덱스 ==========

    /// <summary>
    /// 파괴 스킬
    /// </summary>
    public static final int SKILL_DESTROY = 0;

    /// <summary>
    /// 방패 스킬
    /// </summary>
    public static final int SKILL_SHIELD = 1;

    /// <summary>
    /// 부활 스킬
    /// </summary>
    public static final int SKILL_REVIVE = 2;

    // ========== 좌표 변환 ==========

    /// <summary>
    /// 내부 좌표(행, 열)를 체스 표기법으로 변환
    /// 예: (6, 4) → "e2"
    /// 행 0이 8번 줄(위), 행 7이 1번 줄(아래)
    /// </summary>
    public static String toNotation(int row, int col) {
        // 열 번호(0~7)를 알파벳(a~h)으로 변환
        char file = (char) ('a' + col);
        // 행 번호(0~7)를 체스 줄 번호(8~1)로 변환
        int rank = 8 - row;
        return "" + file + rank;
    }
}
