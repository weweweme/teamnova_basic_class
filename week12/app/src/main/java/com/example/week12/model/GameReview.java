package com.example.week12.model;

/// <summary>
/// 한 게임에 대한 "누군가의 리뷰" 한 개 (게임 상세의 "다른 사람들의 평가" 표시용)
///
/// ──── 무엇인가 ────
/// 어떤 계정이 특정 게임에 남긴 별점 + 한줄평을, 화면에 보여줄 정보(작성자 별명·아바타 색)와
/// 함께 묶은 그릇. 서버에서 내려온 남의 리뷰처럼 보이지만, 실제로는 이 기기의 다른 계정
/// 파일(user_<id>)에서 읽어온 것.
///
/// ──── 좋아요 ────
/// 좋아요 개수(likeCount)와 "내가 눌렀는지(likedByMe)"는 하트를 누를 때 화면에서 바뀌므로
/// (final이 아닌) 변경 가능한 상태로 둔다. reviewerId는 어떤 작성자의 리뷰인지 식별용.
/// </summary>
public class GameReview {

    /// <summary>작성자 별명</summary>
    private final String nickname;

    /// <summary>작성자 아바타 색 (ARGB)</summary>
    private final int avatarColor;

    /// <summary>리뷰 대상 게임 id (좋아요 key를 만들 때 사용)</summary>
    private final int gameId;

    /// <summary>작성자 계정 id (좋아요 key를 만들 때 사용)</summary>
    private final String reviewerId;

    /// <summary>별점 (0.0~5.0)</summary>
    private final float rating;

    /// <summary>한줄평</summary>
    private final String review;

    /// <summary>좋아요 개수 (하트 누르면 바뀜)</summary>
    private int likeCount;

    /// <summary>내가 좋아요를 눌렀는지 (하트 누르면 바뀜)</summary>
    private boolean likedByMe;

    /// <summary>
    /// 리뷰 하나 생성
    /// </summary>
    public GameReview(String nickname, int avatarColor, int gameId, String reviewerId,
                      float rating, String review, int likeCount, boolean likedByMe) {
        this.nickname = nickname;
        this.avatarColor = avatarColor;
        this.gameId = gameId;
        this.reviewerId = reviewerId;
        this.rating = rating;
        this.review = review;
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
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

    /// <summary>작성자 계정 id 반환</summary>
    public String getReviewerId() {
        return reviewerId;
    }

    /// <summary>별점 반환</summary>
    public float getRating() {
        return rating;
    }

    /// <summary>한줄평 반환</summary>
    public String getReview() {
        return review;
    }

    /// <summary>좋아요 개수 반환</summary>
    public int getLikeCount() {
        return likeCount;
    }

    /// <summary>내가 좋아요를 눌렀는지 반환</summary>
    public boolean isLikedByMe() {
        return likedByMe;
    }

    /// <summary>
    /// 좋아요를 토글한다 — 눌렀으면 끄고(개수 -1), 안 눌렀으면 켜고(개수 +1)
    /// (실제 저장은 화면에서 UserPrefs로 따로 처리하고, 이 객체는 표시 상태만 갱신)
    /// </summary>
    public void toggleLike() {
        if (likedByMe) {
            likedByMe = false;
            likeCount--;
        } else {
            likedByMe = true;
            likeCount++;
        }
    }
}
