package com.example.week12.model;

/// <summary>
/// RAWG 검색 결과 한 건을 담는 모델
///
/// ──── 우리 Game과 뭐가 다른가 ────
/// Game: 우리 보관함에 실제로 저장되는 "내 게임" (별점/리뷰/상태/스크린샷까지 가진 무거운 모델)
/// RawgGame: RAWG 서버가 검색으로 돌려준 "후보 게임"의 요약 정보만 담는 가벼운 모델
///
/// 검색 화면에서 목록으로 보여주다가, 사용자가 하나를 고르면 그때 Game으로 변환해서 보관함에 추가한다.
/// (그래서 Activity를 건너다닐 일이 없어 Parcelable이 필요 없음 — Game과 달리 단순함)
///
/// Unity 비유: 상점 목록에 뜨는 "구매 후보 아이템 카드"의 요약 데이터. 실제 인벤토리에 넣으면
/// 별점·소지 여부 등이 붙은 진짜 아이템(Game)이 되는 것과 같다.
///
/// ──── 값이 어디서 오나 (RAWG JSON의 어느 이름표) ────
/// rawgId        ← "id"
/// name          ← "name"
/// coverImageUrl ← "background_image" (표지. 없을 수 있어 null 허용)
/// released      ← "released" (출시일 "2017-03-03" 형식, 없으면 빈 문자열)
/// rating        ← "rating" (RAWG 이용자 평균 평점 0.0~5.0)
/// genreSlug     ← "genres"[0]."slug" (첫 장르의 코드값, 예: "action". 없으면 빈 문자열)
/// platformSlug  ← "platforms"[0]."platform"."slug" (첫 플랫폼의 코드값, 예: "pc". 없으면 빈 문자열)
///
/// ──── slug(슬러그)가 뭔가 ────
/// RAWG가 장르·플랫폼을 컴퓨터가 다루기 좋게 매긴 짧은 코드 문자열 (예: 액션 → "action", PC → "pc").
/// 사람에게 보여주는 이름("Action")과 달리 항상 소문자·고정값이라, 우리 enum으로 바꿀 때 기준으로 쓰기 좋다.
/// 실제 변환은 RawgGameMapper가 담당 (여기서는 원본 값만 그대로 보관).
/// </summary>
public class RawgGame {

    /// <summary>
    /// RAWG가 매긴 게임 고유 번호 (우리 Game의 id와는 별개 — RAWG 세계의 번호)
    /// </summary>
    private final int rawgId;

    /// <summary>
    /// 게임 제목 (예: "The Legend of Zelda: Breath of the Wild")
    /// </summary>
    private final String name;

    /// <summary>
    /// 표지 이미지 주소 (https://... 원격 이미지). RAWG에 표지가 없는 게임이면 null일 수 있음
    /// </summary>
    private final String coverImageUrl;

    /// <summary>
    /// 출시일 문자열 (예: "2017-03-03"). 미정/없음이면 빈 문자열
    /// </summary>
    private final String released;

    /// <summary>
    /// RAWG 이용자 평균 평점 (0.0 ~ 5.0). 평가가 없으면 0
    /// </summary>
    private final float rating;

    /// <summary>
    /// 첫 장르의 코드값(slug) — 우리 Genre enum으로 바꿀 때 기준 (예: "action"). 없으면 빈 문자열
    /// </summary>
    private final String genreSlug;

    /// <summary>
    /// 첫 플랫폼의 코드값(slug) — 우리 Platform enum으로 바꿀 때 기준 (예: "pc"). 없으면 빈 문자열
    /// </summary>
    private final String platformSlug;

    /// <summary>
    /// 검색 결과 한 건 생성 (모든 값은 RawgApi가 JSON에서 꺼내 채운다)
    /// </summary>
    public RawgGame(int rawgId, String name, String coverImageUrl, String released, float rating,
                    String genreSlug, String platformSlug) {
        this.rawgId = rawgId;
        this.name = name;
        this.coverImageUrl = coverImageUrl;
        this.released = released;
        this.rating = rating;
        this.genreSlug = genreSlug;
        this.platformSlug = platformSlug;
    }

    /// <summary>
    /// RAWG 고유 번호 반환
    /// </summary>
    public int getRawgId() {
        return rawgId;
    }

    /// <summary>
    /// 게임 제목 반환
    /// </summary>
    public String getName() {
        return name;
    }

    /// <summary>
    /// 표지 이미지 주소 반환 (없으면 null)
    /// </summary>
    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    /// <summary>
    /// 출시일 문자열 반환 (없으면 빈 문자열)
    /// </summary>
    public String getReleased() {
        return released;
    }

    /// <summary>
    /// RAWG 평균 평점 반환 (0.0 ~ 5.0)
    /// </summary>
    public float getRating() {
        return rating;
    }

    /// <summary>
    /// 첫 장르 코드값(slug) 반환 (없으면 빈 문자열)
    /// </summary>
    public String getGenreSlug() {
        return genreSlug;
    }

    /// <summary>
    /// 첫 플랫폼 코드값(slug) 반환 (없으면 빈 문자열)
    /// </summary>
    public String getPlatformSlug() {
        return platformSlug;
    }
}
