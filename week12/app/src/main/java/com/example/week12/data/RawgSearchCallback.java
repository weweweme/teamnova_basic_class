package com.example.week12.data;

import com.example.week12.model.RawgGame;

import java.util.List;

/// <summary>
/// RAWG 검색 결과를 돌려받는 통로(콜백)
///
/// ──── 왜 콜백이 필요한가 ────
/// 검색은 서버 통신이라 오래 걸린다 → 반드시 서브 스레드에서 돌린다 (11주차 / 03 주의점).
/// 그래서 search()는 결과를 곧바로 return할 수 없다. "일이 끝나면 알려줄게" 하고 먼저 돌아가고,
/// 나중에 결과가 준비되면 이 통로로 알려준다.
///
/// Unity 비유: 코루틴이나 async 작업이 끝났을 때 부를 콜백(Action) 델리게이트와 같다.
/// 호출자는 onSuccess/onError 두 경우를 미리 적어두고, 실제 실행은 나중에 일어난다.
///
/// ──── 두 갈래 (06 흐름도의 성공/실패) ────
/// onSuccess: 결과 목록을 받음 (검색 성공 — 0건일 수도 있음)
/// onError:   사람이 읽을 실패 사유를 받음 (인터넷 없음/서버 오류 등)
///
/// 주의: 이 콜백은 메인 스레드에서 불리도록 RawgApi가 Handler로 넘겨준다
///       → 콜백 안에서 바로 화면(setText 등)을 만져도 안전 (화면은 메인에서만)
/// </summary>
public interface RawgSearchCallback {

    /// <summary>
    /// 검색 성공 — 결과 목록을 전달 (검색어에 맞는 게 없으면 빈 목록)
    /// </summary>
    void onSuccess(List<RawgGame> results);

    /// <summary>
    /// 검색 실패 — 사람이 읽을 실패 사유를 전달 (화면에 안내 문구로 보여줄 용도)
    /// </summary>
    void onError(String message);
}
