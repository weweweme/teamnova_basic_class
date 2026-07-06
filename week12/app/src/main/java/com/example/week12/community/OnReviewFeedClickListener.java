package com.example.week12.community;

/// <summary>
/// 리뷰 피드 항목 클릭 콜백
/// 항목을 누르면 그 리뷰의 게임 id를 넘겨줌 → 화면이 해당 게임 상세로 이동
/// </summary>
public interface OnReviewFeedClickListener {

    /// <summary>
    /// 피드 항목을 눌렀을 때 호출 (리뷰 대상 게임 id 전달)
    /// </summary>
    void onReviewFeedClick(int gameId);
}
