package com.example.week12.rawg;

import com.example.week12.model.RawgGame;

/// <summary>
/// RAWG 검색 결과 항목 클릭 콜백
/// 어댑터(RawgResultAdapter)와 Activity 간 클릭 이벤트 전달용
///
/// 어댑터는 클릭됐을 때 무슨 일을 해야 할지 모름 (보관함에 추가할지, 상세로 갈지)
/// → 어댑터가 직접 처리하지 않고 Activity에 "이 게임이 클릭됐다"고 알려주는 통로 역할
/// (보관함의 OnGameClickListener와 같은 패턴 — 대상 모델만 RawgGame으로 다름)
///
/// @FunctionalInterface: 추상 메서드 1개뿐 → 람다/메서드 참조로 넘길 수 있음
/// </summary>
@FunctionalInterface
public interface OnRawgResultClickListener {

    /// <summary>
    /// 검색 결과 항목이 클릭됐을 때 호출 (어떤 RawgGame인지 전달)
    /// </summary>
    void onResultClick(RawgGame game);
}
