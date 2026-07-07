package com.example.week12.data;

/// <summary>
/// AI(Gemini)가 한글/줄임말 검색어를 정식 영어 게임 제목으로 바꿔주는 결과 통로(콜백)
///
/// AI 호출도 서버 통신이라 오래 걸리므로, 결과를 곧바로 return하지 못하고
/// "다 되면 알려줄게" 방식으로 이 통로로 넘긴다 (RAWG/번역 콜백과 같은 원리).
/// GameNameResolver가 메인 스레드에서 불러주므로, 이어서 바로 검색을 걸어도 안전하다.
/// </summary>
public interface GameNameCallback {

    /// <summary>
    /// 보정 성공 — 정식 영어 게임 제목을 전달 (예: "파판" → "Final Fantasy")
    /// </summary>
    void onResolved(String englishTitle);

    /// <summary>
    /// 보정 실패 — 사유 전달 (호출자는 번역/원문으로 폴백)
    /// </summary>
    void onError(String message);
}
