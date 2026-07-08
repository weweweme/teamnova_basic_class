package com.example.week12.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/// <summary>
/// 발표 시연용 로그를 보기 좋게 다듬는 헬퍼 (증명용 — API 요청/응답을 읽기 쉽게)
///
/// - prettyPreview: JSON을 들여쓰기해서 개행된 형태로, 너무 길면 앞부분만 잘라 보여준다
/// - mask: 토큰 같은 비밀값을 앞뒤 일부만 남기고 가린다 ("진짜 토큰이 왔다"는 증명 + 노출 방지)
/// </summary>
public final class LogFormat {

    /// <summary>
    /// 인스턴스를 만들 이유가 없어 생성자를 막아둠
    /// </summary>
    private LogFormat() {
    }

    /// <summary>
    /// JSON 문자열을 들여쓰기(개행)된 형태로 바꾸고, maxChars를 넘으면 앞부분만 잘라 돌려준다.
    /// JSON이 아니면(파싱 실패) 원문 그대로 자른다.
    /// </summary>
    public static String prettyPreview(String raw, int maxChars) {
        if (raw == null) {
            return "(없음)";
        }
        String pretty = tryPretty(raw.trim());
        String out = pretty != null ? pretty : raw;
        if (out.length() > maxChars) {
            out = out.substring(0, maxChars) + "\n… (이하 생략, 총 " + raw.length() + "자)";
        }
        return out;
    }

    /// <summary>
    /// JSON(객체/배열)이면 2칸 들여쓰기로 예쁘게, 아니면 null
    /// </summary>
    private static String tryPretty(String trimmed) {
        try {
            if (trimmed.startsWith("{")) {
                return new JSONObject(trimmed).toString(2);
            }
            if (trimmed.startsWith("[")) {
                return new JSONArray(trimmed).toString(2);
            }
        } catch (JSONException e) {
            // JSON이 아니면 그대로 (원문 출력)
        }
        return null;
    }

    /// <summary>
    /// 토큰 같은 비밀값을 가린다 — 앞 8자 + … + 뒤 4자 + (총 길이).
    /// "진짜 토큰이 발급됐다"는 건 보여주되 전체는 노출하지 않는다 (짧으면 길이만 표시).
    /// </summary>
    public static String mask(String secret) {
        if (secret == null || secret.isEmpty()) {
            return "(없음)";
        }
        int len = secret.length();
        if (len <= 14) {
            return "***(" + len + "자)";
        }
        return secret.substring(0, 8) + "…" + secret.substring(len - 4) + " (총 " + len + "자)";
    }
}
