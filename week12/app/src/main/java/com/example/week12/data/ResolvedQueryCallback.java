package com.example.week12.data;

/// <summary>
/// 검색어를 영어로 보정한 결과를 돌려받는 통로(콜백)
///
/// 한글이면 AI/번역을 거쳐 영어로 바뀐 뒤(오래 걸림) 이 통로로 전달되고,
/// 영어면 곧바로 그대로 전달된다. 어느 경우든 메인 스레드에서 불린다.
/// (실패/폴백까지 KoreanQueryResolver가 처리하므로, 여기선 항상 "검색에 바로 쓸 영어"만 받는다)
/// </summary>
public interface ResolvedQueryCallback {

    /// <summary>
    /// 검색에 바로 쓸 수 있는 최종 검색어 (영어로 보정됐거나, 원래 영어였거나, 최후엔 원문)
    /// </summary>
    void onResolved(String englishQuery);
}
