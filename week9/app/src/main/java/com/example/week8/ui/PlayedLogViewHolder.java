package com.example.week8.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week8.databinding.ItemLogPlayedBinding;
import com.example.week8.model.ActivityLog;

/// <summary>
/// 타임라인 - PLAYED(플레이 세션) 로그 뷰홀더
/// item_log_played.xml을 바인딩 (PLAY 배지 + 제목 + 플레이 시간)
/// payload에 플레이 시간 텍스트가 들어있어 textViewPlayTime에 표시
/// </summary>
public class PlayedLogViewHolder extends RecyclerView.ViewHolder {

    private final ItemLogPlayedBinding binding;

    public PlayedLogViewHolder(@NonNull ItemLogPlayedBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 로그 데이터를 셀에 바인딩
    /// 제목은 그대로, payload(플레이 시간)는 별도 TextView에 표시
    /// </summary>
    public void bindLog(ActivityLog log, String gameTitle) {
        binding.textViewContent.setText(gameTitle);
        binding.textViewPlayTime.setText(log.getPayload());
        binding.textViewTime.setText(TimeAgoFormatter.format(log.getTimestamp()));
    }
}
