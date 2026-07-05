package com.example.week11.trash;

import com.example.week11.model.Game;

/// <summary>
/// 휴지통 항목의 복원 / 영구삭제 콜백
/// 어댑터(뷰)가 버튼 클릭을 Activity로 위임하는 통로
/// </summary>
public interface OnTrashActionListener {

    /// <summary>
    /// "복원" 버튼 → 이 게임을 보관함으로 되돌림
    /// </summary>
    void onRestore(Game game);

    /// <summary>
    /// "영구삭제" 버튼 → 이 게임을 완전히 제거
    /// </summary>
    void onDeletePermanently(Game game);
}
