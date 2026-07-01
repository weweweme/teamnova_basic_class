package com.example.week10.model;

/// <summary>
/// 피드(팔로잉 피드 등)의 리뷰 항목 한 개
///
/// ──── 무엇인가 ────
/// "누가(작성자) 어떤 게임에 별점 몇 점 + 한줄평을 언제 남겼나"를 한 줄로 묶은 그릇.
/// 서버에서 내려온 활동 피드처럼 보이지만, 실제로는 이 기기의 계정 파일들을 훑어 모은 것.
/// </summary>
public class ReviewFeedItem {

    /// <summary>작성자 별명</summary>
    private final String nickname;

    /// <summary>작성자 아바타 색 (ARGB)</summary>
    private final int avatarColor;

    /// <summary>리뷰 대상 게임 id (피드 항목을 눌러 그 게임으로 이동할 때 사용)</summary>
    private final int gameId;

    /// <summary>리뷰 대상 게임 제목</summary>
    private final String gameTitle;

    /// <summary>별점 (0.0~5.0)</summary>
    private final float rating;

    /// <summary>한줄평</summary>
    private final String review;

    /// <summary>작성 시각 (밀리초) — 최신순 정렬용</summary>
    private final long timestamp;

    /// <summary>
    /// 피드 항목 생성
    /// </summary>
    public ReviewFeedItem(String nickname, int avatarColor, int gameId, String gameTitle,
                          float rating, String review, long timestamp) {
        this.nickname = nickname;
        this.avatarColor = avatarColor;
        this.gameId = gameId;
        this.gameTitle = gameTitle;
        this.rating = rating;
        this.review = review;
        this.timestamp = timestamp;
    }

    /// <summary>작성자 별명 반환</summary>
    public String getNickname() {
        return nickname;
    }

    /// <summary>작성자 아바타 색 반환</summary>
    public int getAvatarColor() {
        return avatarColor;
    }

    /// <summary>게임 id 반환</summary>
    public int getGameId() {
        return gameId;
    }

    /// <summary>게임 제목 반환</summary>
    public String getGameTitle() {
        return gameTitle;
    }

    /// <summary>별점 반환</summary>
    public float getRating() {
        return rating;
    }

    /// <summary>한줄평 반환</summary>
    public String getReview() {
        return review;
    }

    /// <summary>작성 시각(밀리초) 반환</summary>
    public long getTimestamp() {
        return timestamp;
    }
}
