package com.example.week12.model;

/// <summary>
/// RAWG 게임 상세 정보 (한 게임의 자세한 데이터)
///
/// ──── 검색 결과(RawgGame)와 뭐가 다른가 ────
/// RawgGame: 검색 목록에 뜨는 "요약"(제목·표지·평점 정도)
/// RawgGameDetail: 게임 하나를 콕 집어(/games/{id}) 받아온 "상세"(설명·개발사·메타크리틱 등)
///
/// RESTful로 보면: 검색은 "목록"(/games?search=), 상세는 "자원 하나"(/games/{id}) — 슬라이드 04의 그 구분.
/// 화면(상세)에서만 잠깐 쓰이고 저장하지 않으므로 가벼운 모델 (Parcelable 불필요).
///
/// ──── 값이 어디서 오나 (RAWG JSON 이름표) ────
/// description ← "description_raw" (사람이 읽는 설명, 태그 없는 순수 텍스트)
/// metacritic  ← "metacritic" (메타크리틱 점수 0~100, 없으면 0)
/// developer   ← "developers"[0]."name" (개발사, 없으면 빈 문자열)
/// website     ← "website" (공식 홈페이지 주소, 없으면 빈 문자열)
/// </summary>
public class RawgGameDetail {

    /// <summary>
    /// 게임 설명 (순수 텍스트). 없으면 빈 문자열
    /// </summary>
    private final String description;

    /// <summary>
    /// 메타크리틱 점수 (0~100). 점수가 없으면 0
    /// </summary>
    private final int metacritic;

    /// <summary>
    /// 개발사 이름 (여러 곳이면 첫 번째). 없으면 빈 문자열
    /// </summary>
    private final String developer;

    /// <summary>
    /// 공식 홈페이지 주소. 없으면 빈 문자열
    /// </summary>
    private final String website;

    /// <summary>
    /// 상세 정보 생성 (값은 RawgApi가 /games/{id} 응답에서 꺼내 채운다)
    /// </summary>
    public RawgGameDetail(String description, int metacritic, String developer, String website) {
        this.description = description;
        this.metacritic = metacritic;
        this.developer = developer;
        this.website = website;
    }

    /// <summary>
    /// 게임 설명 반환 (없으면 빈 문자열)
    /// </summary>
    public String getDescription() {
        return description;
    }

    /// <summary>
    /// 메타크리틱 점수 반환 (없으면 0)
    /// </summary>
    public int getMetacritic() {
        return metacritic;
    }

    /// <summary>
    /// 개발사 이름 반환 (없으면 빈 문자열)
    /// </summary>
    public String getDeveloper() {
        return developer;
    }

    /// <summary>
    /// 공식 홈페이지 주소 반환 (없으면 빈 문자열)
    /// </summary>
    public String getWebsite() {
        return website;
    }
}
