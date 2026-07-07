package com.example.week12.data;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/// <summary>
/// AI(Gemini)로 게임 검색어를 "정식 영어 제목"으로 보정하는 클래스
///
/// 하는 일: 사용자가 친 한글/줄임말/오타(예: "파판", "배그", "위처")를 Gemini에 보내
/// 정식 영어 제목(예: "Final Fantasy", "PUBG", "The Witcher")만 받아 콜백으로 돌려준다.
///
/// ──── 왜 번역이 아니라 AI인가 ────
/// 번역기는 "소리/뜻"만 알아서 "파판"·"배그" 같은 별칭을 못 잡는다.
/// AI(LLM)는 게임 이름을 지식으로 알고 있어, 별칭·오타·한글 표기를 정식 제목으로 잘 바꾼다.
///
/// ──── 통신 방식 ────
/// RawgApi와 같은 흐름 — 서브 스레드에서 HTTP 요청 → 응답 파싱 → Handler로 메인 스레드 콜백.
/// 요청은 POST(보낼 내용이 있으므로), 인증은 헤더 x-goog-api-key로 API 키를 넣는다.
///
/// Unity 비유: 서버에 프롬프트를 보내고 답만 받아오는 UnityWebRequest 래퍼.
/// </summary>
public class GameNameResolver {

    /// <summary>
    /// Gemini API 키 — Google AI Studio에서 발급한 값 (RAWG 키처럼 앱 신분증)
    /// 이 클래스만 쓰는 값이라 private로 닫아둠.
    /// 주의: GCP 키라 GitHub가 기본 차단함 → 프라이빗 학습 repo라 "시크릿 허용"으로 그대로 올림.
    /// </summary>
    private static final String API_KEY = "AQ.Ab8RN6Ky85M00GsI6cLGKOecaSf2Yjtyr_47cSeiLMNMFiIa5A";

    /// <summary>
    /// Gemini 텍스트 생성 주소 (모델: gemini-2.5-flash — 빠르고 무료 티어로 충분)
    /// </summary>
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    /// <summary>
    /// AI에게 주는 지시문 — "정식 영어 제목만 답하라" (뒤에 사용자 검색어를 붙여 완성)
    /// 설명·따옴표 없이 제목만 받아야 그대로 RAWG 검색에 넣을 수 있다.
    /// </summary>
    private static final String PROMPT =
            "You are a video game database. Convert the user's search query "
            + "(which may be Korean, an abbreviation, or a misspelling) into the official "
            + "English game title. Reply with ONLY the title and nothing else — "
            + "no explanation, no quotes. Query: ";

    /// <summary>
    /// 서브 스레드 결과를 메인 스레드로 넘기는 Handler
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 검색어를 AI로 보정한다 (오래 걸리므로 서브 스레드에서 실행)
    ///
    /// query: 사용자가 친 검색어 (한글/줄임말/오타 가능)
    /// callback: 결과 통로 (성공: 영어 제목 / 실패: 사유) — 메인 스레드에서 불린다
    /// </summary>
    public void resolve(String query, GameNameCallback callback) {
        // 빈 검색어는 보정할 게 없음
        boolean empty = query == null || query.trim().isEmpty();
        if (empty) {
            mainHandler.post(() -> callback.onError("빈 검색어"));
            return;
        }

        new Thread(() -> {                              // 오래 걸리는 일은 서브 스레드
            HttpURLConnection conn = null;              // finally에서 정리하려고 try 밖에 선언
            try {
                conn = (HttpURLConnection) new URL(BASE_URL).openConnection();
                conn.setRequestMethod("POST");          // POST = 보낼 내용(프롬프트)이 있음
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-goog-api-key", API_KEY);   // 헤더로 키 전달
                conn.setDoOutput(true);                 // 요청 본문을 쓸 것이다

                // 요청 본문(JSON)을 만들고 UTF-8로 내보낸다 (한글이 안 깨지도록 인코딩 명시)
                String body = buildRequestBody(query);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                boolean isOk = responseCode == HttpURLConnection.HTTP_OK;
                if (!isOk) {
                    // 429(한도 초과)·400·403 등 → 실패로 올려 호출자가 폴백하게 함
                    postError(callback, "AI 오류 (코드 " + responseCode + ")");
                    return;
                }

                String responseText = readAll(conn.getInputStream());
                String title = parseTitle(responseText);
                mainHandler.post(() -> callback.onResolved(title));

            } catch (IOException | JSONException e) {
                // 인터넷 없음/서버 오류(IOException) 또는 응답 형태가 예상과 다름(JSONException)
                postError(callback, "AI 실패: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    /// <summary>
    /// Gemini 요청 본문(JSON)을 만든다: { "contents":[ { "parts":[ { "text": 프롬프트+검색어 } ] } ] }
    /// org.json으로 만들어 한글·특수문자가 안전하게 이스케이프되게 한다
    /// </summary>
    private String buildRequestBody(String query) throws JSONException {
        JSONObject part = new JSONObject().put("text", PROMPT + query);
        JSONArray parts = new JSONArray().put(part);
        JSONObject content = new JSONObject().put("parts", parts);
        JSONArray contents = new JSONArray().put(content);
        JSONObject root = new JSONObject().put("contents", contents);
        return root.toString();
    }

    /// <summary>
    /// 응답 JSON에서 AI가 답한 제목 글자를 꺼내 다듬는다
    /// 경로: candidates[0].content.parts[0].text
    /// 혹시 따옴표로 감싸거나 앞뒤 공백이 있으면 정리해 그대로 검색에 쓸 수 있게 한다
    /// </summary>
    private String parseTitle(String responseJson) throws JSONException {
        JSONObject root = new JSONObject(responseJson);
        JSONArray candidates = root.getJSONArray("candidates");
        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");
        String text = parts.getJSONObject(0).getString("text");

        // 앞뒤 공백/줄바꿈 제거 + 혹시 감싼 따옴표 제거
        String cleaned = text.trim();
        boolean quoted = cleaned.length() >= 2
                && cleaned.startsWith("\"") && cleaned.endsWith("\"");
        if (quoted) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    /// <summary>
    /// 응답 스트림을 통째로 읽어 문자열로 (UTF-8)
    /// </summary>
    private String readAll(java.io.InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    /// <summary>
    /// 실패 사유를 메인 스레드에서 콜백(onError)으로 전달
    /// </summary>
    private void postError(GameNameCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
