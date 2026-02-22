package item;

import board.SimpleBoard;
import core.Util;
import piece.Piece;

/// <summary>
/// 아이템 추상 클래스
/// 빈 칸에 설치하여 상대 기물이 밟으면 효과 발동
/// 각 하위 클래스(BombItem, TrapItem)가 자기만의 효과를 구현
/// 설치된 아이템은 양쪽 모두에게 보임
/// </summary>
public abstract class Item {

    // ========== 필드 ==========

    // 아이템 이름 ("폭탄", "함정")
    public String name;

    // 아이템 설명 (메뉴에 표시)
    public String description;

    // 설치한 플레이어의 색상
    public int ownerColor;

    // 설치된 위치 (행)
    public int row;

    // 설치된 위치 (열)
    public int col;

    // 최대 설치 횟수 (이 아이템 종류를 몇 개까지 설치할 수 있는지)
    protected int maxUses;

    // 남은 설치 횟수
    public int remainingUses;

    // ========== 생성자 ==========

    /// <summary>
    /// 아이템 생성 (인벤토리용)
    /// 이름, 설명, 최대 사용 횟수를 설정
    /// 위치는 설치 시 지정
    /// </summary>
    public Item(String name, String description, int maxUses) {
        this.name = name;
        this.description = description;
        this.maxUses = maxUses;
        this.remainingUses = maxUses;
        this.row = Util.NONE;
        this.col = Util.NONE;
        this.ownerColor = Util.NONE;
    }

    /// <summary>
    /// 아이템 생성 (보드 배치용)
    /// 설치자 색상과 위치를 지정
    /// </summary>
    public Item(String name, String description, int ownerColor, int row, int col) {
        this.name = name;
        this.description = description;
        this.maxUses = 0;
        this.remainingUses = 0;
        this.ownerColor = ownerColor;
        this.row = row;
        this.col = col;
    }

    // ========== 공통 메서드 ==========

    /// <summary>
    /// 아이템을 설치할 수 있는지 확인 (남은 횟수가 있는지)
    /// </summary>
    public boolean hasUses() {
        return remainingUses > 0;
    }

    /// <summary>
    /// 설치 횟수 1회 차감
    /// </summary>
    public void useCharge() {
        remainingUses--;
    }

    // ========== 추상 메서드 ==========

    /// <summary>
    /// 상대 기물이 이 아이템이 설치된 칸에 도착했을 때 발동되는 효과
    /// 각 하위 클래스가 자기만의 효과로 구현 (메서드 오버라이딩)
    /// BombItem: 기물 제거, TrapItem: 기물 동결
    /// </summary>
    public abstract void trigger(SimpleBoard board, Piece steppedPiece);

    /// <summary>
    /// 보드에 표시할 기호 반환
    /// 각 하위 클래스가 자기만의 기호로 구현 (메서드 오버라이딩)
    /// </summary>
    public abstract String getSymbol();
}
