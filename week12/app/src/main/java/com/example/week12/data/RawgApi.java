package com.example.week12.data;

import android.os.Handler;
import android.os.Looper;

import com.example.week12.model.RawgGame;
import com.example.week12.model.RawgGameDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
        // 검색어에 공백·한글이 있을 수 있어 URL에 안전한 형태로 변환(인코딩)한다
        // 예: "hollow knight" → "hollow%20knight" (공백을 %20으로)
        // (Charset 버전 encode는 checked 예외를 안 던짐 — minSdk 33이라 사용 가능)
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        // 실제 통신·파싱은 공용 fetchList가 담당 (인기/신작/장르별도 같은 걸 재사용)
        fetchList("search=" + encodedQuery, callback);
    }

    /// <summary>
    /// 인기 게임 목록 (많이 담긴 순 = ordering=-added), 20개
    /// 검색과 응답 구조가 같아 같은 파싱·콜백을 그대로 쓴다 (주소의 "주문"만 다름)
    /// </summary>
    public void discoverPopular(RawgSearchCallback callback) {
        fetchList("ordering=-added&page_size=20", callback);
    }

    /// <summary>
    /// 신작 게임 목록 (최근 6개월~오늘 출시, 최신순), 20개
    /// dates=시작,끝 으로 기간을 지정 — "오늘"은 실행 시점 날짜라 앱에서 계산한다
    /// </summary>
    public void discoverNew(RawgSearchCallback callback) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusMonths(6);
        String dates = from + "," + today;   // "yyyy-MM-dd,yyyy-MM-dd" 형식
        fetchList("dates=" + dates + "&ordering=-released&page_size=20", callback);
    }

    /// <summary>
    /// 장르별 게임 목록 (해당 장르 + 평점 높은 순), 20개
    /// genreSlug: RAWG 장르 코드 (예: "action", "role-playing-games-rpg")
    /// </summary>
    public void discoverByGenre(String genreSlug, RawgSearchCallback callback) {
        fetchList("genres=" + genreSlug + "&ordering=-rating&page_size=20", callback);
    }

    /// <summary>
    /// 게임 목록을 가져오는 공용 통신 메서드 (검색·인기·신작·장르별이 모두 이걸 재사용)
    ///
    /// queryParams: 주소의 "?" 뒤에 붙일 주문 (예: "search=zelda", "ordering=-added&page_size=20").
    ///   여기에 &key=... 를 붙여 완성한다.
    /// 흐름(06 슬라이드 그대로): 주소 완성 → GET → 응답 글자 읽기 → JSON 파싱 → Handler로 결과 전달.
    /// 응답 구조가 검색과 동일(결과 배열)하므로 parseResults를 그대로 쓴다.
    /// </summary>
    private void fetchList(String queryParams, RawgSearchCallback callback) {
        new Thread(() -> {                              // 오래 걸리는 일은 서브 스레드 (11주차)
            HttpURLConnection conn = null;              // finally에서 정리하려고 try 밖에 선언
            try {
                // 주소 완성: 기본 주소 + 주문 + 열쇠
                URL url = new URL(BASE_URL + "?" + queryParams + "&key=" + API_KEY);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");           // GET = 읽기 (Read)

                // 서버 응답 코드 확인 (200 OK가 정상 — 401 키 문제, 404 없는 주소 등)
                int responseCode = conn.getResponseCode();
                boolean isOk = responseCode == HttpURLConnection.HTTP_OK;
                if (!isOk) {
                    postError(callback, "서버 오류 (코드 " + responseCode + ")");
                    return;
                }

                // 응답(JSON 글자)을 한 줄씩 읽어 이어붙임
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                // JSON 글자 → RawgGame 목록 → 성공 콜백(메인 스레드)
                List<RawgGame> results = parseResults(builder.toString());
                mainHandler.post(() -> callback.onSuccess(results));

            } catch (IOException | JSONException e) {
                // 03 주의점 — 인터넷 없음/서버 오류(IOException) 또는 형태가 예상과 다름(JSONException)
                postError(callback, "불러오기 실패: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    /// <summary>
    /// rawgId로 게임 상세 하나를 가져온다 (/games/{id}) — 오래 걸리므로 서브 스레드
    ///
    /// 검색(search)이 "목록 훑기"라면, 이건 "자원 하나 콕 집기"(RESTful /games/{id} — 슬라이드 04).
    /// 성공하면 RawgGameDetail을, 실패하면 사유를 메인 스레드에서 콜백으로 전달.
    /// </summary>
    public void getGameDetail(int rawgId, RawgDetailCallback callback) {
        new Thread(() -> {                              // 오래 걸리는 일은 서브 스레드 (11주차)
            HttpURLConnection conn = null;
            try {
                // 주소: 검색과 달리 id를 "경로"에 붙인다 (/games/3498?key=...)
                URL url = new URL(BASE_URL + "/" + rawgId + "?key=" + API_KEY);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");           // GET = 읽기 (Read)

                int responseCode = conn.getResponseCode();
                boolean isOk = responseCode == HttpURLConnection.HTTP_OK;
                if (!isOk) {
                    postDetailError(callback, "서버 오류 (코드 " + responseCode + ")");
                    return;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                RawgGameDetail detail = parseDetail(builder.toString());
                mainHandler.post(() -> callback.onSuccess(detail));

            } catch (IOException | JSONException e) {
                postDetailError(callback, "불러오기 실패: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    /// <summary>
    /// /games/{id} 응답 JSON에서 상세 정보를 꺼낸다
    /// (검색과 달리 results 배열이 아니라, 게임 하나가 통째로 최상위에 옴)
    /// </summary>
    private RawgGameDetail parseDetail(String json) throws JSONException {
        JSONObject o = new JSONObject(json);

        // 설명: 태그 없는 순수 텍스트("description_raw")를 쓴다 (HTML "description"보다 화면에 바로 쓰기 좋음)
        String description = o.optString("description_raw", "");

        // 메타크리틱: 없으면(JSON null) 0
        int metacritic = o.optInt("metacritic", 0);

        // 개발사: developers 배열의 첫 번째 이름
        String developer = "";
        JSONArray developers = o.optJSONArray("developers");
        if (developers != null && developers.length() > 0) {
            JSONObject first = developers.optJSONObject(0);
            if (first != null) {
                developer = first.optString("name", "");
            }
        }

        // 공식 홈페이지
        String website = o.optString("website", "");

        return new RawgGameDetail(description, metacritic, developer, website);
    }

    /// <summary>
    /// 상세 조회 실패 사유를 메인 스레드에서 콜백(onError)으로 전달
    /// </summary>
    private void postDetailError(RawgDetailCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
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

            // 장르·플랫폼: 전체 slug 목록을 모은다 (매핑되는 첫 항목을 나중에 고르기 위해)
            List<String> genreSlugs = allGenreSlugs(gameObj);
            List<String> platformSlugs = allPlatformSlugs(gameObj);

            list.add(new RawgGame(rawgId, name, coverImageUrl, released, rating,
                    genreSlugs, platformSlugs));
        }

        return list;
    }

    /// <summary>
    /// 게임 JSON에서 장르 코드값(slug)을 모두 모아 목록으로 돌려준다 (없으면 빈 목록)
    /// 구조: "genres": [ { "name":"Action", "slug":"action" }, ... ]
    /// (opt 계열만 써서 값이 없어도 예외 없이 넘어감. 빈 slug는 건너뜀)
    /// </summary>
    private List<String> allGenreSlugs(JSONObject gameObj) {
        List<String> slugs = new ArrayList<>();
        JSONArray genres = gameObj.optJSONArray("genres");
        if (genres == null) {
            return slugs;
        }
        for (int i = 0; i < genres.length(); i++) {
            JSONObject g = genres.optJSONObject(i);
            if (g == null) {
                continue;
            }
            String slug = g.optString("slug", "");
            if (!slug.isEmpty()) {
                slugs.add(slug);
            }
        }
        return slugs;
    }

    /// <summary>
    /// 게임 JSON에서 플랫폼 코드값(slug)을 모두 모아 목록으로 돌려준다 (없으면 빈 목록)
    /// 구조: "platforms": [ { "platform": { "name":"PC", "slug":"pc" } }, ... ]
    /// (플랫폼 정보가 platform이라는 한 겹 안에 더 들어있는 형태라 두 번 파고든다)
    /// </summary>
    private List<String> allPlatformSlugs(JSONObject gameObj) {
        List<String> slugs = new ArrayList<>();
        JSONArray platforms = gameObj.optJSONArray("platforms");
        if (platforms == null) {
            return slugs;
        }
        for (int i = 0; i < platforms.length(); i++) {
            JSONObject wrap = platforms.optJSONObject(i);
            if (wrap == null) {
                continue;
            }
            JSONObject platformObj = wrap.optJSONObject("platform");
            if (platformObj == null) {
                continue;
            }
            String slug = platformObj.optString("slug", "");
            if (!slug.isEmpty()) {
                slugs.add(slug);
            }
        }
        return slugs;
    }

    /// <summary>
    /// 실패 사유를 메인 스레드에서 콜백(onError)으로 전달 (여러 곳에서 재사용)
    /// </summary>
    private void postError(RawgSearchCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
