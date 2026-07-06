package com.example.week12.community;

import com.example.week12.model.ReviewFeedItem;

/// <summary>
/// 리뷰 피드 항목의 좋아요(하트) 토글 콜백
/// 하트를 누르면 이미 토글된 항목을 넘겨줌 → 화면이 그 결과를 내 계정 파일에 저장
/// (ViewHolder는 화면만 갱신하고, 실제 저장은 화면/프래그먼트가 담당 → 역할 분리)
/// </summary>
public interface OnReviewLikeToggleListener {

    /// <summary>
    /// 하트를 눌러 좋아요 상태가 바뀌었을 때 호출 (토글이 끝난 항목 전달)
    /// </summary>
    void onReviewLikeToggle(ReviewFeedItem item);
}
