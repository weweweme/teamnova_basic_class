package com.example.week10.detail;

import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week10.account.UserPrefs;
import com.example.week10.community.UserProfileActivity;
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
    /// <param name="review">표시할 리뷰</param>
    /// <param name="myPrefs">지금 로그인한 내 저장소 — 하트를 눌렀을 때 좋아요를 저장하는 데 사용</param>
    public void bind(GameReview review, UserPrefs myPrefs) {
        // 작성자 아바타: 색 + 별명 첫 글자
        binding.textViewReviewerAvatar.setText(initialOf(review.getNickname()));
        binding.textViewReviewerAvatar.setBackgroundTintList(
                ColorStateList.valueOf(review.getAvatarColor()));

        // 별명
        binding.textViewReviewerNickname.setText(review.getNickname());

        // 작성자(아바타/별명) 클릭 → 그 유저의 프로필로
        View.OnClickListener toProfile = v ->
                UserProfileActivity.start(v.getContext(), review.getReviewerId());
        binding.textViewReviewerAvatar.setOnClickListener(toProfile);
        binding.textViewReviewerNickname.setOnClickListener(toProfile);

        // 별점 (예: "★ 4.5")
        binding.textViewReviewerRating.setText("★ " + review.getRating());

        // 한줄평
        binding.textViewReviewerReview.setText(review.getReview());

        // 좋아요(하트) 표시
        updateLike(review);

        // 하트 클릭 → 토글: 내 저장소에 좋아요 켜고/끄고, 이 항목 상태·화면 갱신
        binding.textViewReviewLike.setOnClickListener(v -> {
            if (myPrefs == null) {
                return;
            }
            boolean newLiked = !review.isLikedByMe();
            myPrefs.setLiked(review.getGameId(), review.getReviewerId(), newLiked);
            review.toggleLike();
            updateLike(review);
        });
    }

    /// <summary>
    /// 하트 아이콘/개수/색을 현재 상태로 갱신 (누름=빨강 ♥, 안 누름=회색 ♡)
    /// </summary>
    private void updateLike(GameReview review) {
        String heart = review.isLikedByMe() ? "♥" : "♡";
        binding.textViewReviewLike.setText(heart + " " + review.getLikeCount());
        int color = review.isLikedByMe() ? 0xFFE53935 : 0xFF999999;
        binding.textViewReviewLike.setTextColor(color);
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
