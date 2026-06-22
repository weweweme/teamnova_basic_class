package com.example.week8.timeline;

import androidx.annotation.NonNull;

import com.example.week8.R;
import com.example.week8.databinding.ItemLogCompletedBinding;
import com.example.week8.model.ActivityLog;

/// <summary>
/// 타임라인 - COMPLETED(게임 완료) 로그 뷰홀더
/// item_log_completed.xml을 바인딩 (강조 카드: 트로피 + 배경색)
/// </summary>
public class CompletedLogViewHolder extends LogViewHolder {

    private final ItemLogCompletedBinding binding;

    public CompletedLogViewHolder(@NonNull ItemLogCompletedBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 로그 데이터를 셀에 바인딩
    /// </summary>
    @Override
    public void bindLog(ActivityLog log, String gameTitle) {
        binding.textViewContent.setText(
                binding.getRoot().getContext().getString(R.string.timeline_completed, gameTitle));
        binding.textViewTime.setText(TimeAgoFormatter.format(log.getTimestamp()));
    }
}
