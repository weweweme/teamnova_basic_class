package com.example.week12.data;

import android.os.Handler;
import android.os.Looper;

import com.example.week12.model.RawgGame;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/// <summary>
/// RAWG 게임 검색 API 호출 담당 클래스
///
/// 하는 일 하나: 검색어를 받아 RAWG 서버에 물어보고, 결과(RawgGame 목록)를 콜백으로 돌려준다.
/// 06 슬라이드의 코드가 실제 클래스가 된 것 — 주소 만들기 → GET → JSON 파싱 → Handler로 결과 전달.
///
/// Unity 비유: 서버에 요청 보내는 UnityWebRequest 래퍼. 결과는 콜백으로 넘겨준다.
/// </summary>
public class RawgApi {

    /// <summary>
    /// RAWG API 열쇠(신분증). RAWG 계정에서 발급받은 값.
    /// 이 클래스만 쓰는 값이라 private로 닫아둠 (다른 데서 참조할 일 없음).
    ///
    /// 주의: 이 키는 비밀번호 같은 값이라, 공개 저장소에 올릴 거면 새로 발급받아 숨기는 게 안전.
    ///       (학습용이라 코드에 그대로 둠)
    /// </summary>
    private static final String API_KEY = "3ab64f02833c4853851d0ccd13025cf6";

    /// <summary>
    /// 게임 검색 기본 주소 (여기에 ?search=검색어&key=열쇠 를 붙여 완성한다)
    /// RESTful: /games 라는 "대상"을 GET으로 읽어오는 주소
    /// </summary>
    private static final String BASE_URL = "https://api.rawg.io/api/games";

    /// <summary>
    /// 서브 스레드에서 만든 결과를 메인 스레드로 넘기는 Handler (11주차)
    /// 콜백을 이걸 통해 부르면, 호출자는 콜백 안에서 바로 화면을 만져도 안전하다
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 검색어로 RAWG에 게임을 검색한다 (오래 걸리므로 서브 스레드에서 실행)
    ///
    /// query: 사용자가 입력한 검색어 (예: "zelda")
    /// callback: 결과를 돌려받을 통로 (성공/실패) — 메인 스레드에서 불린다
    ///
    /// 흐름(06 슬라이드 그대로): 주소 완성 → GET → 응답 글자 읽기 → JSON 파싱 → Handler로 결과 전달
    /// </summary>
    public void search(String query, RawgSearchCallback callback) {
        new Thread(() -> {                              // 오래 걸리는 일은 서브 스레드 (11주차)
            // 연결 객체를 finally에서 정리하려고 try 밖에 미리 선언 (실패해도 끊어주기 위함)
            HttpURLConnection conn = null;
            try {
                // ① 주소 완성
                //    검색어에 공백·한글이 있을 수 있어 URL에 안전한 형태로 변환(인코딩)한다
                //    예: "hollow knight" → "hollow%20knight" (공백을 %20으로)
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = BASE_URL + "?search=" + encodedQuery + "&key=" + API_KEY;
                URL url = new URL(urlString);

                // ② GET = "읽어서 줘" (RESTful 동사, CRUD의 Read)
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // ②-1 서버 응답 코드 확인 (200 OK가 정상 — 401은 키 문제, 404는 없는 주소 등)
                int responseCode = conn.getResponseCode();
                boolean isOk = responseCode == HttpURLConnection.HTTP_OK;
                if (!isOk) {
                    // 03 주의점 — 서버가 정상 응답이 아니면 실패로 처리
                    postError(callback, "서버 오류 (코드 " + responseCode + ")");
                    return;   // 이 스레드 종료 (아래 파싱으로 안 감)
                }

                // ③ 응답(JSON 글자)을 한 줄씩 읽어 이어붙임
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                // ④ JSON 글자 → RawgGame 목록으로 꺼내기 (파싱은 아래 헬퍼로 분리)
                List<RawgGame> results = parseResults(builder.toString());

                // ⑤ 성공 — 결과를 메인 스레드에서 콜백으로 전달
                mainHandler.post(() -> callback.onSuccess(results));

            } catch (IOException | JSONException e) {
                // 03 주의점 — 인터넷 없음/서버 오류(IOException) 또는 형태가 예상과 다름(JSONException)
                postError(callback, "불러오기 실패: " + e.getMessage());
            } finally {
                // 성공이든 실패든 연결은 정리 (열어둔 연결을 끊음)
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    /// <summary>
    /// RAWG 응답 JSON(글자)에서 게임 목록을 꺼내 RawgGame 목록으로 만든다
    ///
    /// RAWG 응답 구조:
    ///   { "count": 1234, "results": [ {게임}, {게임}, ... ] }
    /// → "results" 배열을 돌며 각 게임에서 필요한 이름표만 꺼낸다
    ///
    /// 형태가 예상과 다르면 JSONException을 던짐 → 호출부(search)의 catch가 실패로 처리
    /// </summary>
    private List<RawgGame> parseResults(String json) throws JSONException {
        List<RawgGame> list = new ArrayList<>();

        JSONObject root = new JSONObject(json);
        JSONArray results = root.getJSONArray("results");   // 게임들이 담긴 배열

        for (int i = 0; i < results.length(); i++) {
            JSONObject gameObj = results.getJSONObject(i);

            // 이름표를 보고 값 꺼내기 (07 슬라이드에서 말한 "이름표 붙은 상자")
            int rawgId = gameObj.optInt("id", 0);
            String name = gameObj.optString("name", "");

            // 표지: 없거나 null일 수 있음 → 없으면 null로 (P3에서 null이면 기본 아이콘)
            String coverImageUrl = null;
            if (!gameObj.isNull("background_image")) {
                coverImageUrl = gameObj.optString("background_image", null);
            }

            // 출시일: 미정이면 null일 수 있음 → 없으면 빈 문자열
            String released = "";
            if (!gameObj.isNull("released")) {
                released = gameObj.optString("released", "");
            }

            // 평점: 0.0~5.0 (double로 오므로 float로 변환)
            float rating = (float) gameObj.optDouble("rating", 0);

            // 장르·플랫폼: 배열의 "첫 번째" 것만 대표로 꺼낸다 (게임 하나에 여러 개일 수 있음)
            String genreSlug = firstGenreSlug(gameObj);
            String platformSlug = firstPlatformSlug(gameObj);

            list.add(new RawgGame(rawgId, name, coverImageUrl, released, rating,
                    genreSlug, platformSlug));
        }

        return list;
    }

    /// <summary>
    /// 게임 JSON에서 첫 장르의 코드값(slug)을 꺼낸다. 없으면 빈 문자열
    /// 구조: "genres": [ { "name":"Action", "slug":"action" }, ... ]
    /// (opt 계열만 써서 값이 없어도 예외 없이 빈 문자열로 넘어감)
    /// </summary>
    private String firstGenreSlug(JSONObject gameObj) {
        JSONArray genres = gameObj.optJSONArray("genres");
        if (genres == null || genres.length() == 0) {
            return "";
        }
        JSONObject first = genres.optJSONObject(0);
        return first != null ? first.optString("slug", "") : "";
    }

    /// <summary>
    /// 게임 JSON에서 첫 플랫폼의 코드값(slug)을 꺼낸다. 없으면 빈 문자열
    /// 구조: "platforms": [ { "platform": { "name":"PC", "slug":"pc" } }, ... ]
    /// (플랫폼 정보가 platform이라는 한 겹 안에 더 들어있는 형태라 두 번 파고든다)
    /// </summary>
    private String firstPlatformSlug(JSONObject gameObj) {
        JSONArray platforms = gameObj.optJSONArray("platforms");
        if (platforms == null || platforms.length() == 0) {
            return "";
        }
        JSONObject firstWrap = platforms.optJSONObject(0);
        if (firstWrap == null) {
            return "";
        }
        JSONObject platformObj = firstWrap.optJSONObject("platform");
        return platformObj != null ? platformObj.optString("slug", "") : "";
    }

    /// <summary>
    /// 실패 사유를 메인 스레드에서 콜백(onError)으로 전달 (여러 곳에서 재사용)
    /// </summary>
    private void postError(RawgSearchCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
