package com.example.week8.library;

import com.example.week8.model.Game;

/// <summary>
/// 게임 카드 길게 누르기 콜백
/// 어댑터(LibraryAdapter)와 Activity 간 long click 이벤트 전달
///
/// 일반적인 용도: 컨텍스트 메뉴(BottomSheetDialog 등) 표시
/// 카드 짧게 누르기(OnGameClickListener)와 길게 누르기를 명확히 분리하기 위해 별도 인터페이스
///
/// @FunctionalInterface: 추상 메서드 1개뿐 → 람다/메서드 참조로 넘길 수 있음
/// (Activity에서 this::onGameLongClick 으로 전달)
/// </summary>
@FunctionalInterface
public interface OnGameLongClickListener {

    /// <summary>
    /// 카드를 길게 눌렀을 때 호출
    /// 어떤 Game이 길게 눌렸는지 인자로 전달
    /// </summary>
    void onGameLongClick(Game game);
}
