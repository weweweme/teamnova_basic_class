package com.example.week12.data;

/// <summary>
/// 번역 결과를 돌려받는 통로(콜백)
///
/// RAWG 콜백들과 같은 원리 — 번역도 서버 통신이라 오래 걸리므로, 결과를 곧바로 return하지 못하고
/// "다 되면 알려줄게" 방식으로 이 통로로 넘긴다.
/// Translator가 메인 스레드에서 불러주므로, 콜백 안에서 바로 화면(TextView)을 만져도 안전하다.
///
/// Unity 비유: 코루틴으로 웹 요청을 보내고, 끝나면 콜백(Action&lt;string&gt;)으로 결과를 받는 것과 같음.
/// </summary>
public interface TranslateCallback {

    /// <summary>
    /// 번역 성공 — 한국어로 바뀐 글을 전달
    /// </summary>
    void onSuccess(String translatedText);

    /// <summary>
    /// 번역 실패 — 사람이 읽을 실패 사유 전달 (조용히 무시할 수도 있음)
    /// </summary>
    void onError(String message);
}
