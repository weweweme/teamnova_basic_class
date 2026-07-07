package com.example.week12.community;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.App;
import com.example.week12.R;
import com.example.week12.account.UserPrefs;
import com.example.week12.databinding.ItemFollowBinding;
import com.example.week12.model.AccountProfile;
import com.example.week12.util.AvatarBinder;
import com.example.week12.util.CoverImageLoader;

/// <summary>
/// 팔로우 목록(팔로잉/팔로워) 한 줄의 뷰 참조를 보관하는 ViewHolder
/// 아바타·별명·소개를 채우고, 팔로우/팔로잉 버튼을 토글한다
/// </summary>
public class FollowViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 한 줄 레이아웃(item_follow.xml)의 ViewBinding
    /// </summary>
    private final ItemFollowBinding binding;

    /// <summary>
    /// ViewHolder 생성 — itemView로 binding.getRoot()을 부모에 넘겨야 위치 관리가 됨
    /// </summary>
    public FollowViewHolder(@NonNull ItemFollowBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 한 명을 줄에 채운다
    /// </summary>
    /// <param name="profile">표시할 유저</param>
    /// <param name="isMe">이 유저가 지금 로그인한 나 자신이면 팔로우 버튼 숨김</param>
    /// <param name="myPrefs">내 저장소 — 이 유저를 내가 팔로우했는지 확인·토글</param>
    public void bind(AccountProfile profile, boolean isMe, UserPrefs myPrefs) {
        // 아바타: 사진 있으면 사진, 없으면 색+첫글자 (공용 헬퍼)
        CoverImageLoader loader = ((App) binding.getRoot().getContext().getApplicationContext())
                .getCoverImageLoader();
        AvatarBinder.bind(binding.textViewFollowAvatar, binding.imageViewFollowAvatar,
                profile.getNickname(), profile.getAvatarColor(), profile.getAvatarImageUrl(), loader);

        // 별명
        binding.textViewFollowNickname.setText(profile.getNickname());

        // 소개 (없으면 빈 줄)
        binding.textViewFollowBio.setText(profile.getBio());

        // 아바타/별명 클릭 → 그 유저의 프로필로
        View.OnClickListener toProfile = v ->
                UserProfileActivity.start(v.getContext(), profile.getId());
        binding.textViewFollowAvatar.setOnClickListener(toProfile);
        binding.imageViewFollowAvatar.setOnClickListener(toProfile);
        binding.textViewFollowNickname.setOnClickListener(toProfile);

        // 팔로우 버튼 (나 자신에겐 안 보임)
        if (isMe || myPrefs == null) {
            binding.buttonFollow.setVisibility(View.GONE);
            return;
        }
        binding.buttonFollow.setVisibility(View.VISIBLE);
        updateFollowButton(myPrefs.isFollowing(profile.getId()));
        binding.buttonFollow.setOnClickListener(v -> {
            boolean now = myPrefs.isFollowing(profile.getId());
            myPrefs.setFollowing(profile.getId(), !now);
            updateFollowButton(!now);
        });
    }

    /// <summary>
    /// 팔로우 버튼 상태 갱신 — 팔로우 중이면 "팔로잉"(연하게), 아니면 "팔로우"
    /// </summary>
    private void updateFollowButton(boolean following) {
        binding.buttonFollow.setText(following ? R.string.ranking_following : R.string.ranking_follow);
        binding.buttonFollow.setAlpha(following ? 0.5f : 1.0f);
    }
}
