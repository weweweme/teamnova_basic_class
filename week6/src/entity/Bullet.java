package entity;

import world.GameMap;

/// <summary>
/// 정착민이 발사한 총알
/// 스레드가 아닌 데이터 객체 — Main 루프에서 위치를 전진시킴
/// 발사 시점의 적 위치를 향해 직선으로 날아감
/// 무기에 따라 속도, 외형, 관통 여부가 다름
/// </summary>
public class Bullet {

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
    /// 발사한 정착민의 라벨 (처치 로그용)
    /// </summary>
    private final char shooterLabel;

    /// <summary>
    /// 틱당 가로 이동 거리 (무기마다 다름)
    /// </summary>
    private final int speed;

    /// <summary>
    /// 화면에 표시할 문자 (무기마다 다름)
    /// </summary>
    private final char bulletChar;

    /// <summary>
    /// ANSI 색상 코드 (0이면 기본색)
    /// </summary>
    private final int bulletColor;

    /// <summary>
    /// 관통 여부 (true면 적을 뚫고 계속 날아감)
    /// </summary>
    private final boolean piercing;

    /// <summary>
    /// 총알의 현재 열
    /// </summary>
    private int col;

    /// <summary>
    /// 기본 총알 생성 (피스톨용: 속도 3, 문자 *, 기본색, 비관통)
    /// </summary>
    public Bullet(int startRow, int startCol, int targetRow, int targetCol, int damage, char shooterLabel) {
        this(startRow, startCol, targetRow, targetCol, damage, shooterLabel, 3, '*', 0, false);
    }

    /// <summary>
    /// 무기별 속성을 지정하여 총알 생성
    /// </summary>
    public Bullet(int startRow, int startCol, int targetRow, int targetCol, int damage,
                  char shooterLabel, int speed, char bulletChar, int bulletColor, boolean piercing) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.damage = damage;
        this.shooterLabel = shooterLabel;
        this.speed = speed;
        this.bulletChar = bulletChar;
        this.bulletColor = bulletColor;
        this.piercing = piercing;
        this.col = startCol;
    }

    /// <summary>
    /// 총알을 오른쪽으로 전진 (속도만큼 이동)
    /// </summary>
    public void advance() {
        col += speed;
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
    /// 발사한 정착민의 라벨 반환
    /// </summary>
    public char getShooterLabel() {
        return shooterLabel;
    }

    /// <summary>
    /// 화면에 표시할 문자 반환
    /// </summary>
    public char getBulletChar() {
        return bulletChar;
    }

    /// <summary>
    /// ANSI 색상 코드 반환 (0이면 기본색)
    /// </summary>
    public int getBulletColor() {
        return bulletColor;
    }

    /// <summary>
    /// 관통 여부 반환
    /// </summary>
    public boolean isPiercing() {
        return piercing;
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
