package com.example.week12.data;

/// <summary>
/// RAWG 스토어 링크(/games/{id}/stores) 조회 결과를 돌려받는 통로(콜백)
///
/// 성공/실패를 따로 두지 않고, 링크가 없거나 실패하면 "빈 문자열"로 돌려준다 (부가 기능이라 단순하게).
/// RawgApi가 메인 스레드에서 불러주므로 콜백 안에서 바로 화면을 만져도 안전(11주차 Handler).
/// </summary>
public interface RawgStoreCallback {

    /// <summary>
    /// 스토어 링크 결과 — 있으면 그 URL, 없거나 실패하면 빈 문자열
    /// </summary>
    void onResult(String storeUrl);
}
