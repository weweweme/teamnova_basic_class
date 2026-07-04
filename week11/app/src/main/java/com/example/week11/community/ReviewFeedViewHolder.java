package com.example.week11.community;

import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week11.databinding.ItemReviewFeedBinding;
import com.example.week11.model.ReviewFeedItem;
import com.example.week11.timeline.TimeAgoFormatter;

/// <summary>
/// 리뷰 피드 한 줄의 뷰 참조를 보관하는 ViewHolder
/// 피드 항목(작성자·게임·별점·한줄평·시각)을 채우고, 누르면 그 게임 상세로 보낸다
/// </summary>
public class ReviewFeedViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 한 줄 레이아웃(item_review_feed.xml)의 ViewBinding
    /// </summary>
    private final ItemReviewFeedBinding binding;

    /// <summary>
    /// ViewHolder 생성 — itemView로 binding.getRoot()을 부모에 넘겨야 위치 관리가 됨
    /// </summary>
    public ReviewFeedViewHolder(@NonNull ItemReviewFeedBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 피드 항목 한 개를 줄에 채운다
    /// </summary>
    public void bind(ReviewFeedItem item, OnReviewFeedClickListener clickListener) {
        // 작성자 아바타: 색 + 별명 첫 글자
        binding.textViewFeedAvatar.setText(initialOf(item.getNickname()));
        binding.textViewFeedAvatar.setBackgroundTintList(
                ColorStateList.valueOf(item.getAvatarColor()));

        // 닉네임 · 게임제목 · 별점
        binding.textViewFeedNickname.setText(item.getNickname());
        binding.textViewFeedGame.setText("· " + item.getGameTitle());
        binding.textViewFeedRating.setText("★ " + item.getRating());

        // 한줄평
        binding.textViewFeedReview.setText(item.getReview());

        // N분 전 (타임라인과 같은 포맷터 재사용)
        // 시각 기록이 없는(0) 옛 리뷰는 엉뚱한 "수십 년 전"이 나오므로 시간 표시를 숨김
        if (item.getTimestamp() > 0L) {
            binding.textViewFeedTime.setText(TimeAgoFormatter.format(item.getTimestamp()));
            binding.textViewFeedTime.setVisibility(View.VISIBLE);
        } else {
            binding.textViewFeedTime.setVisibility(View.GONE);
        }

        // 항목 클릭 → 그 게임 상세로
        binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReviewFeedClick(item.getGameId());
            }
        });
    }

    /// <summary>
    /// 별명 첫 글자(대문자)를 반환, 비어 있으면 물음표
    /// </summary>
    private String initialOf(String nickname) {
        if (nickname.isEmpty()) {
            return "?";
        }
        return String.valueOf(Character.toUpperCase(nickname.charAt(0)));
    }
}
