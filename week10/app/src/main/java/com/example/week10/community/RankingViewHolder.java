package com.example.week10.community;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week10.R;
import com.example.week10.account.UserPrefs;
import com.example.week10.databinding.ItemRankingBinding;
import com.example.week10.model.AccountProfile;

/// <summary>
/// "유저 랭킹" 목록 한 줄의 뷰 참조를 보관하는 ViewHolder
/// 순위 + 아바타 + 별명 + 리뷰 수를 채운다
/// </summary>
public class RankingViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 한 줄 레이아웃(item_ranking.xml)의 ViewBinding
    /// </summary>
    private final ItemRankingBinding binding;

    /// <summary>
    /// ViewHolder 생성 — itemView로 binding.getRoot()을 부모에 넘겨야 위치 관리가 됨
    /// </summary>
    public RankingViewHolder(@NonNull ItemRankingBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 랭킹 한 줄을 채운다
    /// </summary>
    /// <param name="profile">이 줄의 유저 프로필</param>
    /// <param name="rank">순위 (1부터)</param>
    /// <param name="isMe">지금 로그인한 내 계정이면 별명 뒤에 "(나)" 표시</param>
    /// <param name="myPrefs">지금 로그인한 내 저장소 — 이 유저를 내가 팔로우했는지 확인·토글</param>
    public void bind(AccountProfile profile, int rank, boolean isMe, UserPrefs myPrefs) {
        Context context = binding.getRoot().getContext();

        // 순위: 1~3위는 메달 이모지, 그 외는 숫자
        binding.textViewRank.setText(rankLabel(rank));

        // 아바타: 색 + 별명 첫 글자
        binding.textViewRankAvatar.setText(initialOf(profile.getNickname()));
        binding.textViewRankAvatar.setBackgroundTintList(
                ColorStateList.valueOf(profile.getAvatarColor()));

        // 별명 (내 계정이면 "(나)")
        String nickname = profile.getNickname();
        if (isMe) {
            nickname = nickname + " " + context.getString(R.string.ranking_me_badge);
        }
        binding.textViewRankNickname.setText(nickname);

        // 리뷰 수
        binding.textViewRankReviews.setText(
                context.getString(R.string.ranking_review_count, profile.getReviewCount()));

        // 아바타/별명 클릭 → 그 유저의 프로필로
        View.OnClickListener toProfile = v -> UserProfileActivity.start(context, profile.getId());
        binding.textViewRankAvatar.setOnClickListener(toProfile);
        binding.textViewRankNickname.setOnClickListener(toProfile);

        // 팔로우 버튼 — 내 계정 줄에는 안 보이고, 남의 줄에만 팔로우/팔로잉 토글
        if (isMe || myPrefs == null) {
            binding.buttonFollow.setVisibility(View.GONE);
        } else {
            binding.buttonFollow.setVisibility(View.VISIBLE);
            updateFollowButton(myPrefs.isFollowing(profile.getId()));
            binding.buttonFollow.setOnClickListener(v -> {
                boolean now = myPrefs.isFollowing(profile.getId());
                myPrefs.setFollowing(profile.getId(), !now);
                updateFollowButton(!now);
            });
        }
    }

    /// <summary>
    /// 팔로우 버튼 상태 갱신 — 팔로우 중이면 "팔로잉"(연하게), 아니면 "팔로우"
    /// </summary>
    private void updateFollowButton(boolean following) {
        binding.buttonFollow.setText(following ? R.string.ranking_following : R.string.ranking_follow);
        // 팔로우 중일 땐 살짝 흐리게 해서 "이미 팔로우함"을 표시
        binding.buttonFollow.setAlpha(following ? 0.5f : 1.0f);
    }

    /// <summary>
    /// 순위를 표시 문자열로 (1위 🥇, 2위 🥈, 3위 🥉, 그 외 숫자)
    /// </summary>
    private String rankLabel(int rank) {
        if (rank == 1) {
            return "🥇";
        }
        if (rank == 2) {
            return "🥈";
        }
        if (rank == 3) {
            return "🥉";
        }
        return String.valueOf(rank);
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
