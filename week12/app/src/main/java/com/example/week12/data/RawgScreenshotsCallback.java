package com.example.week12.data;

import java.util.List;

/// <summary>
/// RAWG 스크린샷(/games/{id}/screenshots) 조회 결과를 돌려받는 통로(콜백)
///
/// 스토어 링크와 마찬가지로 부가 기능이라 성공/실패를 나누지 않고,
/// 이미지 주소 목록을 그대로 준다 (없거나 실패하면 빈 목록).
/// RawgApi가 메인 스레드에서 불러주므로 콜백 안에서 바로 화면을 만져도 안전(11주차 Handler).
/// </summary>
public interface RawgScreenshotsCallback {

    /// <summary>
    /// 스크린샷 이미지 주소 목록 (https). 없거나 실패하면 빈 목록
    /// </summary>
    void onResult(List<String> imageUrls);
}
