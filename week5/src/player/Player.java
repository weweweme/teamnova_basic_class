package player;

import board.*;
import core.*;
import piece.Piece;
import skill.Skill;
import item.Item;

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

    // ========== 생성자 ==========

    /// <summary>
    /// 플레이어 생성
    /// 색상과 이름을 설정
    /// </summary>
    public Player(int color, String name) {
        this.color = color;
        this.name = name;
    }

    // ========== 추상 메서드 ==========

    /// <summary>
    /// 이번 턴에 둘 수를 선택하여 반환
    /// 각 하위 클래스가 자기만의 방식으로 구현 (메서드 오버라이딩)
    /// HumanPlayer: 화살표 키로 커서 이동 + Enter로 선택
    /// AiPlayer: 알고리즘으로 결정
    /// null 반환 시 게임 종료 요청
    /// </summary>
    public abstract Move chooseMove(Board board);

    /// <summary>
    /// 폰 프로모션 시 승격할 기물 선택
    /// 1: 퀸, 2: 룩, 3: 비숍, 4: 나이트
    /// </summary>
    public abstract int choosePromotion(Board board);

    // ========== 스킬/아이템 메서드 (기본 구현) ==========
    // 스킬 모드에서만 사용. 기본값은 "아무것도 안 함"
    // HumanPlayer와 AiPlayer가 스킬 모드용으로 오버라이딩

    /// <summary>
    /// 이번 턴에 할 행동을 선택 (스킬 모드 전용)
    /// 0: 이동, 1: 스킬, 2: 아이템
    /// 기본값: 항상 이동 (일반 모드에서는 이 메서드를 호출하지 않음)
    /// </summary>
    public int chooseAction(Board board, Skill[] skills, Item[] items) {
        return 0;
    }

    /// <summary>
    /// 사용할 스킬을 선택 (스킬 번호 반환, -1이면 취소)
    /// 기본값: 취소
    /// </summary>
    public int chooseSkill(Board board, Skill[] skills) {
        return -1;
    }

    /// <summary>
    /// 스킬 대상 칸을 선택 (보드에서 대상 좌표 선택)
    /// targets: 선택 가능한 좌표 목록
    /// 반환: {행, 열} 또는 null(취소)
    /// </summary>
    public int[] chooseSkillTarget(Board board, int[][] targets, int targetCount) {
        return null;
    }

    /// <summary>
    /// 사용할 아이템 종류를 선택 (인덱스 반환, -1이면 취소)
    /// </summary>
    public int chooseItemType(Board board, Item[] items) {
        return -1;
    }

    /// <summary>
    /// 아이템 설치 칸을 선택 (빈 칸 좌표)
    /// 반환: {행, 열} 또는 null(취소)
    /// </summary>
    public int[] chooseItemTarget(Board board) {
        return null;
    }

    /// <summary>
    /// 부활할 기물을 선택 (잡힌 기물 목록에서 선택)
    /// captured: 부활 가능한 기물 목록
    /// 반환: 선택한 인덱스 또는 -1(취소)
    /// </summary>
    public int chooseReviveTarget(Board board, Piece[] captured) {
        return -1;
    }
}
