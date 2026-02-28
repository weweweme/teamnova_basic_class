package entity;

import core.Direction;
import core.Util;
import world.Barricade;
import world.GameMap;

/// <summary>
/// 배회 상태 — 안전지대(바리케이드 왼쪽)에서 랜덤으로 돌아다님
/// </summary>
public class WanderingState extends ColonistState {

    /// <summary>
    /// 8방향 이동 방향 목록
    /// </summary>
    private static final Direction[] DIRECTIONS = Direction.values();

    /// <summary>
    /// 안전지대 오른쪽 끝 (정착민 블록이 바리케이드와 겹치지 않도록 여유 확보)
    /// </summary>
    private static final int MAX_COL = Barricade.COLUMN - 4;

    @Override
    public void enter(Colonist colonist) {
        // 배회 상태 진입 시 별도 초기화 없음
    }

    @Override
    public void update(Colonist colonist) {
        // 랜덤 방향 선택
        int randomIndex = Util.rand(DIRECTIONS.length);
        Direction direction = DIRECTIONS[randomIndex];

        // 이동할 좌표 계산
        int newRow = colonist.getPosition().getRow() + direction.getDeltaRow();
        int newCol = colonist.getPosition().getCol() + direction.getDeltaCol();

        // 안전지대 범위 안에서만 이동
        boolean validRow = newRow >= 0 && newRow < GameMap.HEIGHT;
        boolean validCol = newCol >= 0 && newCol <= MAX_COL;
        if (validRow && validCol) {
            colonist.getPosition().moveTo(newRow, newCol);
        }
    }

    @Override
    public void exit(Colonist colonist) {
        // 배회 상태 퇴장 시 별도 정리 없음
    }

    @Override
    public String getDisplayName() {
        return "배회";
    }
}
