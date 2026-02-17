package player;

import board.*;
import core.*;
import piece.*;
import skill.Skill;
import item.Item;

/// <summary>
/// AI 플레이어
/// 난이도에 따라 다른 전략으로 수를 선택
/// 쉬움: 랜덤 선택
/// 보통: 체크메이트 → 기물 잡기(가치순) → 체크 → 랜덤
/// </summary>
public class AiPlayer extends Player {

    // ========== 난이도 상수 ==========

    // 쉬움 (랜덤 선택)
    public static final int EASY = 0;

    // 보통 (우선순위 기반 전략)
    public static final int NORMAL = 1;

    // ========== 필드 ==========

    // AI 난이도
    private int difficulty;

    // ========== 생성자 ==========

    public AiPlayer(int color, String name, int difficulty) {
        super(color, name);
        this.difficulty = difficulty;
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// AI가 수를 선택
    /// 난이도에 따라 쉬움(랜덤) 또는 보통(우선순위 전략) 사용
    /// </summary>
    @Override
    public Move chooseMove(SimpleBoard board) {
        // AI 턴에도 현재 보드를 보여줌
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println(name + "이(가) 생각 중...");

        // 생각하는 시간 연출
        Util.delay(1000);

        // 모든 합법적인 수 가져오기
        java.util.ArrayList<Move> allMoves = board.getAllValidMoves(color);

        // 수가 없으면 null (체크메이트 또는 스테일메이트 상태)
        if (allMoves.isEmpty()) {
            return null;
        }

        // 난이도에 따라 전략 선택
        switch (difficulty) {
            case EASY:
                return chooseEasy(allMoves);
            default:
                return chooseNormal(board, allMoves);
        }
    }

    // ========== 난이도별 전략 ==========

    /// <summary>
    /// 쉬움: 합법적인 수 중 랜덤으로 선택
    /// </summary>
    private Move chooseEasy(java.util.ArrayList<Move> allMoves) {
        return allMoves.get(Util.rand(allMoves.size()));
    }

    /// <summary>
    /// 보통: 우선순위 기반 전략
    /// 체크메이트 > 기물 잡기(가치 높은 순) > 체크 > 랜덤
    /// </summary>
    private Move chooseNormal(SimpleBoard board, java.util.ArrayList<Move> allMoves) {
        // 상대 색상
        int opponentColor = (color == Piece.RED) ? Piece.BLUE : Piece.RED;

        // 1순위: 체크메이트 가능한 수
        for (Move move : allMoves) {
            if (wouldCheckmate(board, move, opponentColor)) {
                return move;
            }
        }

        // 2순위: 기물 잡기 (가치가 가장 높은 기물 우선)
        Move bestCapture = null;
        int bestValue = 0;

        for (Move move : allMoves) {
            if (board.grid[move.toRow][move.toCol].isEmpty()) {
                continue;
            }
            Piece target = board.grid[move.toRow][move.toCol].getPiece();
            if (target.color != color && target.value > bestValue) {
                bestValue = target.value;
                bestCapture = move;
            }
        }

        if (bestCapture != null) {
            return bestCapture;
        }

        // 3순위: 체크를 거는 수
        for (Move move : allMoves) {
            if (wouldCheck(board, move, opponentColor)) {
                return move;
            }
        }

        // 4순위: 랜덤 선택
        return allMoves.get(Util.rand(allMoves.size()));
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// AI는 항상 퀸으로 승격 (가장 강한 기물)
    /// </summary>
    @Override
    public int choosePromotion(SimpleBoard board) {
        return 1;
    }

    // ========== 스킬/아이템 AI 전략 ==========

    /// <summary>
    /// AI 행동 선택 전략
    /// 1순위: 파괴 스킬로 상대 퀸 제거 가능하면 스킬 사용
    /// 2순위: 가치 5 이상인 잡힌 기물이 있고 부활 가능하면 스킬 사용
    /// 3순위: 20% 확률로 방패 스킬 사용
    /// 4순위: 30% 확률로 아이템 설치
    /// 5순위: 일반 이동
    /// </summary>
    @Override
    public int chooseAction(SimpleBoard board, Skill[] skills, Item[] items) {
        int opponentColor = (color == Piece.RED) ? Piece.BLUE : Piece.RED;

        // 파괴 스킬 확인 (인덱스 0): 상대 퀸이 있으면 파괴 우선
        if (skills[0].hasUses() && skills[0].canUse(board.grid, color)) {
            for (int r = 0; r < SimpleBoard.SIZE; r++) {
                for (int c = 0; c < SimpleBoard.SIZE; c++) {
                    if (board.grid[r][c].isEmpty()) {
                        continue;
                    }
                    Piece piece = board.grid[r][c].getPiece();
                    if (piece instanceof Queen && piece.color == opponentColor) {
                        return 1;  // 스킬
                    }
                }
            }
        }

        // 부활 스킬 확인 (인덱스 2): 가치 높은 잡힌 기물이 있으면 부활
        if (skills[2].hasUses() && skills[2].canUse(board.grid, color)) {
            SkillBoard skillBoard = (SkillBoard) board;
            Piece[] captured = skillBoard.getCapturedPieces(color);
            for (Piece p : captured) {
                if (p.value >= 5) {
                    return 1;  // 스킬
                }
            }
        }

        // 방패 스킬 확인 (인덱스 1): 20% 확률로 사용
        boolean shieldAvailable = skills[1].hasUses() && skills[1].canUse(board.grid, color);  // 방패 스킬을 사용할 수 있는지
        boolean randomTrigger = Util.rand(5) == 0;  // 20% 확률로 발동하는지
        if (shieldAvailable && randomTrigger) {
            return 1;  // 스킬
        }

        // 아이템 설치: 30% 확률로 사용
        boolean hasItem = false;
        for (Item item : items) {
            if (item.hasUses()) {
                hasItem = true;
                break;
            }
        }
        if (hasItem && Util.rand(10) < 3) {
            return 2;  // 아이템
        }

        return 0;  // 이동
    }

    /// <summary>
    /// AI 스킬 선택
    /// 우선순위: 파괴(퀸 대상) > 부활(가치 높은 기물) > 방패(가치 높은 기물)
    /// </summary>
    @Override
    public int chooseSkill(SimpleBoard board, Skill[] skills) {
        // 파괴 스킬 (인덱스 0)
        if (skills[0].hasUses() && skills[0].canUse(board.grid, color)) {
            return 0;
        }

        // 부활 스킬 (인덱스 2)
        if (skills[2].hasUses() && skills[2].canUse(board.grid, color)) {
            return 2;
        }

        // 방패 스킬 (인덱스 1)
        if (skills[1].hasUses() && skills[1].canUse(board.grid, color)) {
            return 1;
        }

        return -1;
    }

    /// <summary>
    /// AI 스킬 대상 선택 (가장 가치 높은 기물이 있는 칸 우선)
    /// 기물이 없는 대상(빈 칸)이면 랜덤 선택
    /// </summary>
    @Override
    public int[] chooseSkillTarget(SimpleBoard board, int[][] targets, int targetCount) {
        if (targetCount == 0) {
            return null;
        }

        // 가치가 가장 높은 기물이 있는 칸 선택
        int bestIndex = 0;
        int bestValue = 0;

        for (int i = 0; i < targetCount; i++) {
            if (board.grid[targets[i][0]][targets[i][1]].isEmpty()) {
                continue;
            }
            Piece piece = board.grid[targets[i][0]][targets[i][1]].getPiece();
            if (piece.value > bestValue) {
                bestValue = piece.value;
                bestIndex = i;
            }
        }

        // 기물이 없는 대상(빈 칸, 부활 위치 등)이면 랜덤 선택
        if (bestValue == 0) {
            bestIndex = Util.rand(targetCount);
        }

        return targets[bestIndex];
    }

    /// <summary>
    /// AI 아이템 종류 선택 (사용 가능한 것 중 랜덤)
    /// </summary>
    @Override
    public int chooseItemType(SimpleBoard board, Item[] items) {
        java.util.ArrayList<Integer> available = new java.util.ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            if (items[i].hasUses()) {
                available.add(i);
            }
        }
        if (available.isEmpty()) {
            return -1;
        }
        return available.get(Util.rand(available.size()));
    }

    /// <summary>
    /// AI 아이템 설치 위치 선택 (중앙 근처의 빈 칸에 랜덤 설치)
    /// 중앙에 빈 칸이 없으면 전체에서 탐색
    /// </summary>
    @Override
    public int[] chooseItemTarget(SimpleBoard board) {
        SkillBoard skillBoard = (SkillBoard) board;
        // 보드 중앙 4x4 영역(2~5행, 2~5열)에서 빈 칸 탐색
        java.util.ArrayList<int[]> candidates = new java.util.ArrayList<>();
        for (int r = 2; r <= 5; r++) {
            for (int c = 2; c <= 5; c++) {
                if (board.grid[r][c].isEmpty() && skillBoard.getItem(r, c) == null) {
                    candidates.add(new int[]{r, c});
                }
            }
        }

        // 중앙에 빈 칸이 없으면 전체에서 탐색
        if (candidates.isEmpty()) {
            for (int r = 0; r < SimpleBoard.SIZE; r++) {
                for (int c = 0; c < SimpleBoard.SIZE; c++) {
                    if (board.grid[r][c].isEmpty() && skillBoard.getItem(r, c) == null) {
                        candidates.add(new int[]{r, c});
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(Util.rand(candidates.size()));
    }

    /// <summary>
    /// AI 부활 대상 선택 (가치가 가장 높은 기물 우선)
    /// </summary>
    @Override
    public int chooseReviveTarget(SimpleBoard board, Piece[] captured) {
        if (captured.length == 0) {
            return -1;
        }

        int bestIndex = 0;
        int bestValue = 0;

        for (int i = 0; i < captured.length; i++) {
            if (captured[i].value > bestValue) {
                bestValue = captured[i].value;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    // ========== 시뮬레이션 ==========

    /// <summary>
    /// 이 수를 두면 상대가 체크메이트가 되는지 시뮬레이션
    /// 임시로 이동했다가 되돌리는 방식
    /// </summary>
    private boolean wouldCheckmate(SimpleBoard board, Move move, int opponentColor) {
        // 원래 상태 저장
        Piece movingPiece = board.grid[move.fromRow][move.fromCol].getPiece();
        Piece capturedPiece = board.grid[move.toRow][move.toCol].getPiece();
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;
        boolean origHasMoved = movingPiece.hasMoved;

        // 임시로 이동 실행
        board.grid[move.toRow][move.toCol].setPiece(movingPiece);
        board.grid[move.fromRow][move.fromCol].removePiece();
        movingPiece.row = move.toRow;
        movingPiece.col = move.toCol;
        movingPiece.hasMoved = true;

        // 체크메이트 확인
        boolean checkmate = board.isCheckmate(opponentColor);

        // 원래 상태로 복원
        board.grid[move.fromRow][move.fromCol].setPiece(movingPiece);
        board.grid[move.toRow][move.toCol].setPiece(capturedPiece);
        movingPiece.row = origRow;
        movingPiece.col = origCol;
        movingPiece.hasMoved = origHasMoved;

        return checkmate;
    }

    /// <summary>
    /// 이 수를 두면 상대가 체크 상태가 되는지 시뮬레이션
    /// 임시로 이동했다가 되돌리는 방식
    /// </summary>
    private boolean wouldCheck(SimpleBoard board, Move move, int opponentColor) {
        // 원래 상태 저장
        Piece movingPiece = board.grid[move.fromRow][move.fromCol].getPiece();
        Piece capturedPiece = board.grid[move.toRow][move.toCol].getPiece();
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;

        // 임시로 이동 실행
        board.grid[move.toRow][move.toCol].setPiece(movingPiece);
        board.grid[move.fromRow][move.fromCol].removePiece();
        movingPiece.row = move.toRow;
        movingPiece.col = move.toCol;

        // 체크 확인
        boolean check = board.isInCheck(opponentColor);

        // 원래 상태로 복원
        board.grid[move.fromRow][move.fromCol].setPiece(movingPiece);
        board.grid[move.toRow][move.toCol].setPiece(capturedPiece);
        movingPiece.row = origRow;
        movingPiece.col = origCol;

        return check;
    }
}
