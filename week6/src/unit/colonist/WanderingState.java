package unit.colonist;

import game.Direction;
import game.Util;
import structure.Barricade;
import game.GameWorld;

/// <summary>
/// 배회 상태 — 안전지대(바리케이드 왼쪽)에서 랜덤으로 돌아다님
/// </summary>
public class WanderingState extends ColonistState {

    /// <summary>
    /// 8방향 이동 방향 목록
    /// </summary>
    private final Direction[] DIRECTIONS = Direction.values();

    /// <summary>
    /// 회복 틱 카운터
    /// </summary>
    private int healTick;

    @Override
    public void enter(Colonist colonist) {
        healTick = 0;
    }

    @Override
    public void update(Colonist colonist) {
        // 자동 회복 (체력이 최대가 아닐 때만)
        healTick++;

        // 자동 회복 간격 (틱 수, 6틱 = 3초마다 1 회복)
        final int HEAL_INTERVAL = 6;
        if (healTick >= HEAL_INTERVAL) {
            healTick = 0;
            if (colonist.getHp() < colonist.getMaxHp()) {

                // 자동 회복량
                final int HEAL_AMOUNT = 1;
                colonist.heal(HEAL_AMOUNT);
            }
        }

        // 랜덤 방향 선택
        int randomIndex = Util.rand(DIRECTIONS.length);
        Direction direction = DIRECTIONS[randomIndex];

        // 이동할 좌표 계산
        int newRow = colonist.getPosition().getRow() + direction.getDeltaRow();
        int newCol = colonist.getPosition().getCol() + direction.getDeltaCol();

        // 안전지대 범위 안에서만 이동
        boolean validRow = newRow >= 0 && newRow < GameWorld.HEIGHT;

        // 안전지대 오른쪽 끝 (블록에서 가장 넓은 행이 바리케이드와 겹치지 않도록)
        String[] block = colonist.getBlock();
        int maxWidth = 0;
        for (String row : block) {
            if (row.length() > maxWidth) {
                maxWidth = row.length();
            }
        }
        final int MAX_COL = Barricade.COLUMN - maxWidth;
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
