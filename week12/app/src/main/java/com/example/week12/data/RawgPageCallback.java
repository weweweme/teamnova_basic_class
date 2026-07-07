package com.example.week12.data;

import com.example.week12.model.RawgGame;

import java.util.List;

/// <summary>
/// RAWG "한 페이지" 조회 결과를 돌려받는 통로(콜백) — 무한 스크롤(페이지네이션)용
///
/// ──── 검색 콜백과 뭐가 다른가 ────
/// RawgSearchCallback: 결과 목록만 준다 (한 번에 끝나는 조회용)
/// RawgPageCallback: 결과 목록 + "다음 페이지가 더 있는지(hasNext)"를 같이 준다
///   → 화면은 hasNext를 보고 "바닥까지 스크롤하면 다음 페이지를 더 불러올지" 판단한다
///
/// RAWG 응답의 "next"(다음 페이지 주소) 유무로 hasNext를 정한다.
/// RawgApi가 메인 스레드에서 불러주므로 콜백 안에서 바로 화면을 만져도 안전(11주차 Handler).
/// </summary>
public interface RawgPageCallback {

    /// <summary>
    /// 페이지 조회 성공 — 이 페이지의 결과 목록 + 다음 페이지가 더 있는지 여부
    /// </summary>
    void onSuccess(List<RawgGame> results, boolean hasNext);

    /// <summary>
    /// 조회 실패 — 사람이 읽을 실패 사유
    /// </summary>
    void onError(String message);
}
