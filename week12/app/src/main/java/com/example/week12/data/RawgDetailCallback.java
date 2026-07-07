package com.example.week12.data;

import com.example.week12.model.RawgGameDetail;

/// <summary>
/// RAWG 게임 상세(/games/{id}) 결과를 돌려받는 통로(콜백)
///
/// 검색 콜백(RawgSearchCallback)과 같은 원리 — 상세도 서버 통신이라 오래 걸리므로,
/// 결과를 곧바로 return하지 못하고 "준비되면 알려줄게" 방식으로 이 통로로 넘긴다.
/// RawgApi가 메인 스레드에서 불러주므로 콜백 안에서 바로 화면을 만져도 안전(11주차 Handler).
/// </summary>
public interface RawgDetailCallback {

    /// <summary>
    /// 상세 조회 성공 — 게임 상세 정보를 전달
    /// </summary>
    void onSuccess(RawgGameDetail detail);

    /// <summary>
    /// 상세 조회 실패 — 사람이 읽을 실패 사유 전달 (조용히 무시할 수도 있음)
    /// </summary>
    void onError(String message);
}
