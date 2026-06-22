package com.example.week8.timeline;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week8.databinding.ItemLogAddedBinding;
import com.example.week8.model.ActivityLog;

/// <summary>
/// 타임라인 - ADDED(게임 추가) 로그 뷰홀더
/// item_log_added.xml을 바인딩 (심플 한 줄: 내용 + 시간)
/// </summary>
public class AddedLogViewHolder extends RecyclerView.ViewHolder {

    private final ItemLogAddedBinding binding;

    public AddedLogViewHolder(@NonNull ItemLogAddedBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 로그 데이터를 셀에 바인딩
    /// gameTitle은 어댑터가 gameId로 GameRepository를 조회해 미리 넘겨준 값
    /// (ViewHolder가 Repository에 직접 의존하지 않도록 제목을 인자로 받음)
    /// </summary>
    public void bindLog(ActivityLog log, String gameTitle) {
        binding.textViewContent.setText(
                binding.getRoot().getContext().getString(
                        com.example.week8.R.string.timeline_added, gameTitle));
        binding.textViewTime.setText(TimeAgoFormatter.format(log.getTimestamp()));
    }
}
