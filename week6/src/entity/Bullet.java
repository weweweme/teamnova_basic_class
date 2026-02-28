package entity;

import world.GameMap;

/// <summary>
/// 정착민이 발사한 총알
/// 스레드가 아닌 데이터 객체 — Main 루프에서 위치를 전진시킴
/// 발사 시점의 적 위치를 향해 직선으로 날아감
/// </summary>
public class Bullet {

    /// <summary>
    /// 틱당 가로 이동 거리 (열 단위)
    /// </summary>
    private static final int SPEED = 3;

    /// <summary>
    /// 발사 위치 (행)
    /// </summary>
    private final int startRow;

    /// <summary>
    /// 발사 위치 (열)
    /// </summary>
    private final int startCol;

    /// <summary>
    /// 조준 대상 위치 (행)
    /// </summary>
    private final int targetRow;

    /// <summary>
    /// 조준 대상 위치 (열)
    /// </summary>
    private final int targetCol;

    /// <summary>
    /// 이 총알의 피해량
    /// </summary>
    private final int damage;

    /// <summary>
    /// 총알의 현재 열
    /// </summary>
    private int col;

    /// <summary>
    /// 지정한 발사 위치에서 조준 위치를 향해 날아가는 총알 생성
    /// </summary>
    public Bullet(int startRow, int startCol, int targetRow, int targetCol, int damage) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.damage = damage;
        this.col = startCol;
    }

    /// <summary>
    /// 총알을 오른쪽으로 전진
    /// </summary>
    public void advance() {
        col += SPEED;
    }

    /// <summary>
    /// 현재 행 반환 (발사→조준 직선 위의 위치를 계산)
    /// </summary>
    public int getRow() {
        int totalColDist = targetCol - startCol;

        // 가로 거리가 0이면 직선 발사 (수평)
        if (totalColDist <= 0) {
            return startRow;
        }

        int colProgress = col - startCol;
        int totalRowDist = targetRow - startRow;
        return startRow + (int) ((long) totalRowDist * colProgress / totalColDist);
    }

    /// <summary>
    /// 현재 열 반환
    /// </summary>
    public int getCol() {
        return col;
    }

    /// <summary>
    /// 피해량 반환
    /// </summary>
    public int getDamage() {
        return damage;
    }

    /// <summary>
    /// 화면 밖으로 나갔는지 확인 (가로 또는 세로)
    /// </summary>
    public boolean isOffScreen() {
        int currentRow = getRow();
        boolean outOfWidth = col >= GameMap.WIDTH;
        boolean outOfHeight = currentRow < 0 || currentRow >= GameMap.HEIGHT;
        return outOfWidth || outOfHeight;
    }
}
