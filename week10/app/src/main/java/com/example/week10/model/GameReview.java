package com.example.week10.model;

/// <summary>
/// 한 게임에 대한 "누군가의 리뷰" 한 개 (게임 상세의 "다른 사람들의 평가" 표시용)
///
/// ──── 무엇인가 ────
/// 어떤 계정이 특정 게임에 남긴 별점 + 한줄평을, 화면에 보여줄 정보(작성자 별명·아바타 색)와
/// 함께 묶은 그릇. 서버에서 내려온 남의 리뷰처럼 보이지만, 실제로는 이 기기의 다른 계정
/// 파일(user_<id>)에서 읽어온 것.
/// </summary>
public class GameReview {

    /// <summary>작성자 별명</summary>
    private final String nickname;

    /// <summary>작성자 아바타 색 (ARGB)</summary>
    private final int avatarColor;

    /// <summary>별점 (0.0~5.0)</summary>
    private final float rating;

    /// <summary>한줄평</summary>
    private final String review;

    /// <summary>
    /// 리뷰 하나 생성
    /// </summary>
    public GameReview(String nickname, int avatarColor, float rating, String review) {
        this.nickname = nickname;
        this.avatarColor = avatarColor;
        this.rating = rating;
        this.review = review;
    }

    /// <summary>작성자 별명 반환</summary>
    public String getNickname() {
        return nickname;
    }

    /// <summary>작성자 아바타 색 반환</summary>
    public int getAvatarColor() {
        return avatarColor;
    }

    /// <summary>별점 반환</summary>
    public float getRating() {
        return rating;
    }

    /// <summary>한줄평 반환</summary>
    public String getReview() {
        return review;
    }
}
