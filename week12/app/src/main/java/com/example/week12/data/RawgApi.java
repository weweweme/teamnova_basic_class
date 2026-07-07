package com.example.week12.data;

import android.os.Handler;
import android.os.Looper;

import com.example.week12.model.RawgGame;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// RAWG 게임 검색 API 호출 담당 클래스
///
/// 하는 일 하나: 검색어를 받아 RAWG 서버에 물어보고, 결과(RawgGame 목록)를 콜백으로 돌려준다.
/// 06 슬라이드의 코드가 실제 클래스가 된 것 — 주소 만들기 → GET → JSON 파싱 → Handler로 결과 전달.
///
/// ──── 지금은 골격만 (B2) ────
/// 실제 네트워크 호출과 JSON 파싱은 다음 단계(B3)에서 채운다.
/// 여기서는 "어떤 스레드에서, 어떤 통로로 결과를 돌려줄지"의 구조만 잡는다.
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
    /// ──── 지금은 골격 (B2) ────
    /// 실제 GET 요청과 JSON 파싱은 B3에서 채운다.
    /// 지금은 스레드를 띄우고 "빈 결과"를 메인으로 돌려주는 구조만 있다.
    /// </summary>
    public void search(String query, RawgSearchCallback callback) {
        new Thread(() -> {                              // 오래 걸리는 일은 서브 스레드 (11주차)
            // TODO(B3): 여기서 주소 완성 → HttpURLConnection GET → JSON 파싱 → results 채우기
            //   완성할 주소 예: BASE_URL + "?search=" + query + "&key=" + API_KEY
            List<RawgGame> results = new ArrayList<>();  // B3 전까지는 빈 목록

            // 결과 반영(콜백)은 메인 스레드에서 (화면은 메인에서만 — 11주차)
            mainHandler.post(() -> callback.onSuccess(results));
        }).start();
    }
}
