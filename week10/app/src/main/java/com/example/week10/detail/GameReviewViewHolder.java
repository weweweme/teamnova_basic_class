package com.example.week10.detail;

import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week10.databinding.ItemGameReviewBinding;
import com.example.week10.model.GameReview;

/// <summary>
/// "다른 사람들의 평가" 목록 한 줄의 뷰 참조를 보관하는 ViewHolder
/// 리뷰 한 개(GameReview)를 작성자 아바타·별명·별점·한줄평 칸에 채운다
/// </summary>
public class GameReviewViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 리뷰 한 줄 레이아웃(item_game_review.xml)의 ViewBinding
    /// </summary>
    private final ItemGameReviewBinding binding;

    /// <summary>
    /// ViewHolder 생성 — itemView로 binding.getRoot()을 부모에 넘겨야 위치 관리가 됨
    /// </summary>
    public GameReviewViewHolder(@NonNull ItemGameReviewBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 리뷰 한 개를 줄에 채운다
    /// </summary>
    public void bind(GameReview review) {
        // 작성자 아바타: 색 + 별명 첫 글자
        binding.textViewReviewerAvatar.setText(initialOf(review.getNickname()));
        binding.textViewReviewerAvatar.setBackgroundTintList(
                ColorStateList.valueOf(review.getAvatarColor()));

        // 별명
        binding.textViewReviewerNickname.setText(review.getNickname());

        // 별점 (예: "★ 4.5")
        binding.textViewReviewerRating.setText("★ " + review.getRating());

        // 한줄평
        binding.textViewReviewerReview.setText(review.getReview());
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
