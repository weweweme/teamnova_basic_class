package com.example.week10.timeline;

import androidx.annotation.NonNull;

import com.example.week10.R;
import com.example.week10.databinding.ItemLogReviewedBinding;
import com.example.week10.model.ActivityLog;

/// <summary>
/// 타임라인 - REVIEWED(리뷰 작성) 로그 뷰홀더
/// item_log_reviewed.xml을 바인딩 (인용구: 상단 내용 + 리뷰 본문)
/// payload에 리뷰 본문이 들어있어 textViewReviewBody에 표시
/// </summary>
public class ReviewedLogViewHolder extends LogViewHolder {

    private final ItemLogReviewedBinding binding;

    public ReviewedLogViewHolder(@NonNull ItemLogReviewedBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 로그 데이터를 셀에 바인딩
    /// payload(리뷰 본문)는 인용구 스타일 TextView에 표시
    /// </summary>
    @Override
    public void bindLog(ActivityLog log, String gameTitle) {
        binding.textViewContent.setText(
                binding.getRoot().getContext().getString(R.string.timeline_reviewed, gameTitle));
        binding.textViewReviewBody.setText(log.getPayload());
        binding.textViewTime.setText(TimeAgoFormatter.format(log.getTimestamp()));
    }
}
