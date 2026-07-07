package com.example.week12.data;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 영어 → 한국어 번역 담당 클래스
///
/// 하는 일 하나: 영어 글을 받아 번역 서버에 물어보고, 한국어 결과를 콜백으로 돌려준다.
/// RAWG(RawgApi)와 완전히 같은 흐름 — 주소 만들기 → GET → 응답 파싱 → Handler로 결과 전달.
///
/// ──── 어떤 번역 서버를 쓰나 ────
/// 구글 번역의 "비공식" 주소(translate_a/single)를 쓴다. 열쇠(API 키)가 필요 없어 바로 쓸 수 있다.
/// 대신 "공식"이 아니라서, 아주 많이 연타하면 잠깐 막힐 수 있다 (발표용 짧은 사용엔 문제없음).
///   → 화면에서 "번역 보기"를 눌렀을 때만 부르고, 한 번 번역한 건 재사용(캐시)하면 연타를 피할 수 있다.
///
/// ──── 왜 글을 잘라서 보내나 ────
/// 이 주소는 번역할 글을 주소(URL) 뒤에 붙여 보내는데(GET), 주소는 너무 길면 잘린다.
/// 그래서 긴 설명은 한 번에 다 보내지 못하고, 일정 길이로 잘라(여러 조각) 각각 번역한 뒤 이어붙인다.
///
/// Unity 비유: 서버에 요청 보내는 UnityWebRequest 래퍼. 결과는 콜백으로 넘겨준다.
/// </summary>
public class Translator {

    /// <summary>
    /// 구글 번역 비공식 주소 (여기에 ?sl=원문언어&tl=목표언어&q=글 을 붙여 완성한다)
    /// 이 클래스만 쓰는 값이라 private로 닫아둠
    /// </summary>
    private static final String BASE_URL = "https://translate.googleapis.com/translate_a/single";

    /// <summary>
    /// 원문 언어 코드 (en = 영어). RAWG가 주는 게임 이름·설명이 영어라 고정
    /// </summary>
    private static final String SOURCE_LANG = "en";

    /// <summary>
    /// 목표 언어 코드 (ko = 한국어)
    /// </summary>
    private static final String TARGET_LANG = "ko";

    /// <summary>
    /// 한 번에 보낼 최대 글자 수 (주소가 너무 길어지지 않게 이 길이로 잘라 보냄)
    /// 이보다 긴 글은 여러 조각으로 나눠 각각 번역한 뒤 이어붙인다
    /// </summary>
    private static final int MAX_CHUNK_CHARS = 1000;

    /// <summary>
    /// 서브 스레드에서 만든 결과를 메인 스레드로 넘기는 Handler
    /// 콜백을 이걸 통해 부르면, 호출자는 콜백 안에서 바로 화면을 만져도 안전하다
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 영어 글을 한국어로 번역한다 (오래 걸리므로 서브 스레드에서 실행)
    ///
    /// englishText: 번역할 영어 글 (게임 이름 + 설명 등)
    /// callback: 결과를 돌려받을 통로 (성공/실패) — 메인 스레드에서 불린다
    ///
    /// 흐름: 긴 글이면 조각으로 나눔 → 각 조각을 GET으로 번역 → 이어붙임 → Handler로 결과 전달
    /// </summary>
    public void translateToKorean(String englishText, TranslateCallback callback) {
        // 빈 글은 번역할 게 없으니 통신 없이 그대로 돌려준다 (서버 낭비 방지)
        boolean nothingToTranslate = englishText == null || englishText.trim().isEmpty();
        if (nothingToTranslate) {
            String passthrough = englishText == null ? "" : englishText;
            mainHandler.post(() -> callback.onSuccess(passthrough));
            return;
        }

        new Thread(() -> {                              // 오래 걸리는 일은 서브 스레드
            try {
                // 긴 글은 조각으로 나눠 각각 번역한 뒤 순서대로 이어붙인다
                List<String> chunks = splitIntoChunks(englishText, MAX_CHUNK_CHARS);
                StringBuilder result = new StringBuilder();
                for (String chunk : chunks) {
                    result.append(translateChunk(chunk));
                }
                String translated = result.toString();
                mainHandler.post(() -> callback.onSuccess(translated));

            } catch (IOException | JSONException e) {
                // 인터넷 없음/서버 오류(IOException) 또는 응답 형태가 예상과 다름(JSONException)
                postError(callback, "번역 실패: " + e.getMessage());
            }
        }).start();
    }

    /// <summary>
    /// 조각 하나(짧은 영어 글)를 번역 서버에 보내 한국어로 돌려받는다
    /// (서브 스레드에서 불림 — 통신·파싱만 담당)
    /// </summary>
    private String translateChunk(String text) throws IOException, JSONException {
        HttpURLConnection conn = null;                  // finally에서 정리하려고 try 밖에 선언
        try {
            // 글에 공백·기호가 있어 주소에 안전한 형태로 변환(인코딩)한다
            // (Charset 버전 encode는 checked 예외를 안 던짐 — minSdk 33이라 사용 가능)
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            // client=gtx: 비공식 주소가 요구하는 고정 값, dt=t: "번역문을 달라"는 뜻
            String query = "?client=gtx"
                    + "&sl=" + SOURCE_LANG
                    + "&tl=" + TARGET_LANG
                    + "&dt=t&q=" + encoded;
            URL url = new URL(BASE_URL + query);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");               // GET = 읽기(번역 결과를 읽어옴)

            int responseCode = conn.getResponseCode();
            boolean isOk = responseCode == HttpURLConnection.HTTP_OK;
            if (!isOk) {
                // 서버가 거절하면 예외로 올려 상위(translateToKorean)에서 실패 처리
                throw new IOException("서버 오류 (코드 " + responseCode + ")");
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            return parseTranslation(builder.toString());

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /// <summary>
    /// 번역 서버 응답에서 한국어 번역문만 꺼내 이어붙인다
    ///
    /// 응답 생김새(중첩 배열): [ [ ["번역문","원문",...], ["번역문2","원문2",...] ], ... ]
    ///   - 루트[0] = "문장 조각들의 목록"
    ///   - 각 조각의 [0] = 번역된 한국어 (조각의 [1]은 원문 영어라 안 씀)
    /// → 조각들의 [0]을 순서대로 이어붙이면 전체 번역문이 된다
    /// </summary>
    private String parseTranslation(String body) throws JSONException {
        JSONArray root = new JSONArray(body);
        JSONArray sentences = root.getJSONArray(0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentences.length(); i++) {
            JSONArray sentence = sentences.getJSONArray(i);
            // 조각의 번역문이 비어(null) 있을 수 있어 방어 후 이어붙임
            if (!sentence.isNull(0)) {
                sb.append(sentence.getString(0));
            }
        }
        return sb.toString();
    }

    /// <summary>
    /// 긴 글을 최대 길이(maxChars) 이하의 조각들로 나눈다
    /// 단어 중간에서 뚝 자르지 않도록, 한계 직전의 공백/줄바꿈/마침표에서 끊는다
    /// </summary>
    private List<String> splitIntoChunks(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        int length = text.length();

        while (start < length) {
            // 이번 조각의 끝 후보 (남은 글이 짧으면 끝까지)
            int end = Math.min(start + maxChars, length);

            // 아직 글이 남았는데 한계에 걸렸다면, 단어를 안 쪼개게 뒤로 살짝 당겨 끊는다
            boolean needBreak = end < length;
            if (needBreak) {
                int breakAt = lastBreakIndex(text, start, end);
                if (breakAt > start) {
                    end = breakAt;
                }
            }

            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }

    /// <summary>
    /// start~end 구간에서 "끊기 좋은 자리"(공백/줄바꿈/마침표) 중 가장 뒤를 찾아
    /// 그 다음 위치를 반환 (없으면 end 그대로 → 어쩔 수 없이 그 자리에서 끊음)
    /// </summary>
    private int lastBreakIndex(String text, int start, int end) {
        for (int i = end - 1; i > start; i--) {
            char c = text.charAt(i);
            boolean isBreak = c == ' ' || c == '\n' || c == '.';
            if (isBreak) {
                return i + 1;   // 구분자까지 이번 조각에 포함하고, 다음 조각은 그 뒤부터
            }
        }
        return end;
    }

    /// <summary>
    /// 번역 실패 사유를 메인 스레드에서 콜백(onError)으로 전달
    /// </summary>
    private void postError(TranslateCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
