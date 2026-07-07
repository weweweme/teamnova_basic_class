package com.example.week12.model;

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

    /// <summary>작성자 아바타 사진 주소 (없으면 빈 문자열 → 색깔 원 사용)</summary>
    private final String avatarImageUrl;

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

    /// <summary>작성자 계정 id — 좋아요를 "누구의 리뷰"에 눌렀는지 저장할 때 필요</summary>
    private final String reviewerId;

    /// <summary>이 리뷰의 좋아요 수 (토글하면 바뀌므로 가변)</summary>
    private int likeCount;

    /// <summary>지금 보는 내가 이 리뷰에 좋아요를 눌렀는지 (토글하면 바뀜)</summary>
    private boolean likedByMe;

    /// <summary>
    /// 방금 실시간으로 목록에 끼워넣은 항목인지 (등장 애니메이션을 한 번 재생할지 판단)
    /// 화면 표시용 임시 값이라 저장/전달 대상이 아님
    /// </summary>
    private boolean justAdded = false;

    /// <summary>
    /// 피드 항목 생성
    /// </summary>
    public ReviewFeedItem(String nickname, int avatarColor, String avatarImageUrl,
                          int gameId, String gameTitle,
                          float rating, String review, long timestamp,
                          String reviewerId, int likeCount, boolean likedByMe) {
        this.nickname = nickname;
        this.avatarColor = avatarColor;
        this.avatarImageUrl = avatarImageUrl;
        this.gameId = gameId;
        this.gameTitle = gameTitle;
        this.rating = rating;
        this.review = review;
        this.timestamp = timestamp;
        this.reviewerId = reviewerId;
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
    }

    /// <summary>작성자 계정 id 반환</summary>
    public String getReviewerId() {
        return reviewerId;
    }

    /// <summary>좋아요 수 반환</summary>
    public int getLikeCount() {
        return likeCount;
    }

    /// <summary>내가 좋아요를 눌렀는지 반환</summary>
    public boolean isLikedByMe() {
        return likedByMe;
    }

    /// <summary>
    /// 좋아요를 토글한다 — 내 누름 상태를 뒤집고, 좋아요 수를 ±1 조정
    /// (화면에 즉시 반영하기 위해 항목 자체의 값을 갱신)
    /// </summary>
    public void toggleLikedByMe() {
        likedByMe = !likedByMe;
        likeCount += likedByMe ? 1 : -1;
    }

    /// <summary>방금 실시간 삽입된 항목인지 반환</summary>
    public boolean isJustAdded() {
        return justAdded;
    }

    /// <summary>방금 삽입됨 표시 설정 (등장 애니메이션을 한 번 재생한 뒤 false로 끈다)</summary>
    public void setJustAdded(boolean justAdded) {
        this.justAdded = justAdded;
    }

    /// <summary>작성자 별명 반환</summary>
    public String getNickname() {
        return nickname;
    }

    /// <summary>작성자 아바타 색 반환</summary>
    public int getAvatarColor() {
        return avatarColor;
    }

    /// <summary>작성자 아바타 사진 주소 반환 (없으면 빈 문자열)</summary>
    public String getAvatarImageUrl() {
        return avatarImageUrl;
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
