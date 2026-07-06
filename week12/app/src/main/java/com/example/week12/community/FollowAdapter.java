package com.example.week12.community;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.account.UserPrefs;
import com.example.week12.databinding.ItemFollowBinding;
import com.example.week12.model.AccountProfile;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 팔로우 목록(팔로잉/팔로워) RecyclerView 어댑터
/// 프로필 목록과 한 줄 뷰(FollowViewHolder)를 연결 (단일 뷰타입)
/// </summary>
public class FollowAdapter extends RecyclerView.Adapter<FollowViewHolder> {

    /// <summary>
    /// 화면에 보여줄 프로필 목록 (넘어온 리스트를 복사해 보관)
    /// </summary>
    private final List<AccountProfile> profiles;

    /// <summary>
    /// 지금 로그인한 내 계정 아이디 (나 자신엔 팔로우 버튼 숨김)
    /// </summary>
    private final String currentAccountId;

    /// <summary>
    /// 내 저장소 (팔로우 확인·토글에 사용)
    /// </summary>
    private final UserPrefs myPrefs;

    /// <summary>
    /// 어댑터 생성
    /// </summary>
    public FollowAdapter(List<AccountProfile> profiles, String currentAccountId, UserPrefs myPrefs) {
        this.profiles = new ArrayList<>(profiles);
        this.currentAccountId = currentAccountId;
        this.myPrefs = myPrefs;
    }

    /// <summary>
    /// 한 줄 뷰 생성 (item_follow.xml inflate)
    /// </summary>
    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFollowBinding binding = ItemFollowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FollowViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 프로필을 줄에 채움
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
        AccountProfile profile = profiles.get(position);
        boolean isMe = profile.getId().equals(currentAccountId);
        holder.bind(profile, isMe, myPrefs);
    }

    /// <summary>
    /// 전체 줄 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return profiles.size();
    }
}
