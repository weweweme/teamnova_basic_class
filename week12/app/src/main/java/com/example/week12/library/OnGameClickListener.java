package com.example.week12.library;

import com.example.week12.model.Game;

/// <summary>
/// 게임 카드/항목 클릭 콜백
/// 어댑터(LibraryAdapter)와 Activity 간 클릭 이벤트 전달용
///
/// 어댑터는 자기가 무슨 일을 해야 할지 모름 (GameDetail로 가야 할지, 다른 동작인지)
/// → 어댑터가 직접 Intent를 만들지 않고 Activity에 "클릭됐다"고 알려주는 통로 역할
///
/// @FunctionalInterface: 추상 메서드 1개뿐 → 람다/메서드 참조로 넘길 수 있음
/// (Activity에서 this::onGameClick 으로 전달)
/// </summary>
@FunctionalInterface
public interface OnGameClickListener {

    /// <summary>
    /// 카드가 클릭됐을 때 호출
    /// 어떤 Game이 클릭됐는지 인자로 전달
    /// </summary>
    void onGameClick(Game game);
}
