package com.example.week12.data;

/// <summary>
/// 한글 검색어를 "검색에 바로 쓸 영어"로 바꿔주는 공용 클래스 (RAWG 검색·보관함 검색이 함께 씀)
///
/// ──── 보정 순서 (폴백 체인) ────
///   1순위 AI(Gemini)  — "배그"→"PUBG" 처럼 줄임말·별칭까지 정식 영어 제목으로
///   2순위 번역          — AI 실패(한도 초과 등) 시 "엘든링"→"Elden Ring"
///   최후  원문 그대로   — 둘 다 실패해도 뭔가는 검색되게
/// 영어만 있으면 보정 없이 그대로 돌려준다(통신 낭비 방지).
///
/// ──── 왜 공용으로 뺐나 ────
/// RAWG 검색과 보관함 검색이 같은 "한글→영어" 처리를 필요로 해서, 한 곳에 모아 재사용한다(DRY).
/// 결과는 ResolvedQueryCallback으로 돌려주며, 내부 통신은 각 도우미가 메인 스레드로 콜백한다.
/// </summary>
public class KoreanQueryResolver {

    /// <summary>
    /// 1순위 — AI(Gemini)로 정식 영어 게임명 보정
    /// </summary>
    private final GameNameResolver aiResolver = new GameNameResolver();

    /// <summary>
    /// 2순위 — 한→영 번역 (AI 실패 시 폴백)
    /// </summary>
    private final Translator translator = new Translator();

    /// <summary>
    /// 검색어를 영어로 바꿔 콜백으로 돌려준다.
    /// 영어면 즉시 그대로, 한글이면 AI→번역→원문 순으로 시도한 결과를 돌려준다.
    /// </summary>
    public void toEnglish(String query, ResolvedQueryCallback callback) {
        // 영어(한글 없음)면 보정 없이 그대로 (즉시 콜백)
        if (!hasKorean(query)) {
            callback.onResolved(query);
            return;
        }

        // 1순위: AI
        aiResolver.resolve(query, new GameNameCallback() {
            @Override
            public void onResolved(String englishTitle) {
                callback.onResolved(pick(englishTitle, query));
            }

            @Override
            public void onError(String message) {
                fallbackTranslate(query, callback);   // 2순위로
            }
        });
    }

    /// <summary>
    /// AI가 실패했을 때 번역으로 한 번 더, 그래도 안 되면 원문 그대로
    /// </summary>
    private void fallbackTranslate(String query, ResolvedQueryCallback callback) {
        translator.translateToEnglish(query, new TranslateCallback() {
            @Override
            public void onSuccess(String english) {
                callback.onResolved(pick(english, query));
            }

            @Override
            public void onError(String message) {
                callback.onResolved(query);   // 최후: 원문
            }
        });
    }

    /// <summary>
    /// 글자에 한글이 하나라도 있는지 (한글 음절 '가'~'힣' 또는 자모 범위)
    /// 호출자가 "보정이 필요한 입력인지"(로딩 표시 등)를 미리 판단할 때도 쓰라고 static으로 연다
    /// </summary>
    public static boolean hasKorean(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isSyllable = c >= 0xAC00 && c <= 0xD7A3;   // 완성형 한글 (가~힣)
            boolean isJamo = c >= 0x1100 && c <= 0x11FF;       // 첫가끝 자모
            boolean isCompatJamo = c >= 0x3130 && c <= 0x318F; // 호환 자모 (ㄱ, ㅏ 등)
            if (isSyllable || isJamo || isCompatJamo) {
                return true;
            }
        }
        return false;
    }

    /// <summary>
    /// 보정 결과가 비어 있으면 원문을 쓰고, 있으면 앞뒤 공백을 정리해 쓴다
    /// </summary>
    private static String pick(String converted, String original) {
        boolean usable = converted != null && !converted.trim().isEmpty();
        return usable ? converted.trim() : original;
    }
}
