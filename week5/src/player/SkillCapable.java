package player;

import board.SimpleBoard;
import piece.Piece;
import skill.Skill;
import item.Item;

/// <summary>
/// 스킬/아이템 기능 인터페이스
/// SkillGame에서 스킬/아이템 선택 기능을 제공
/// </summary>
public interface SkillCapable {

    /// <summary>
    /// 이번 턴에 할 행동을 선택
    /// 0: 이동, 1: 스킬, 2: 아이템
    /// </summary>
    int chooseAction(SimpleBoard board, Skill[] skills, Item[] items);

    /// <summary>
    /// 사용할 스킬을 선택 (스킬 번호 반환, Util.NONE이면 취소)
    /// </summary>
    int chooseSkill(SimpleBoard board, Skill[] skills);

    /// <summary>
    /// 스킬 대상 칸을 선택 (보드에서 대상 좌표 선택)
    /// targets: 선택 가능한 좌표 목록
    /// 반환: {행, 열} 또는 null(취소)
    /// </summary>
    int[] chooseSkillTarget(SimpleBoard board, int[][] targets, int targetCount);

    /// <summary>
    /// 사용할 아이템 종류를 선택 (인덱스 반환, Util.NONE이면 취소)
    /// </summary>
    int chooseItemType(SimpleBoard board, Item[] items);

    /// <summary>
    /// 아이템 설치 칸을 선택 (빈 칸 좌표)
    /// 반환: {행, 열} 또는 null(취소)
    /// </summary>
    int[] chooseItemTarget(SimpleBoard board);

    /// <summary>
    /// 부활할 기물을 선택 (잡힌 기물 목록에서 선택)
    /// captured: 부활 가능한 기물 목록
    /// 반환: 선택한 인덱스 또는 Util.NONE(취소)
    /// </summary>
    int chooseReviveTarget(SimpleBoard board, Piece[] captured);
}
