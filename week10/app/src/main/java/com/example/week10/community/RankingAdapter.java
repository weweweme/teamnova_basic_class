package com.example.week10.community;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week10.databinding.ItemRankingBinding;
import com.example.week10.model.AccountProfile;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// "유저 랭킹" 목록 RecyclerView 어댑터
/// 이미 정렬된 프로필 목록을 받아, 위치+1을 순위로 표시 (단일 뷰타입)
/// </summary>
public class RankingAdapter extends RecyclerView.Adapter<RankingViewHolder> {

    /// <summary>
    /// 순위대로 정렬된 프로필 목록 (넘어온 리스트를 복사해 보관)
    /// </summary>
    private final List<AccountProfile> profiles;

    /// <summary>
    /// 지금 로그인한 내 계정 아이디 ("(나)" 표시용, 없으면 null)
    /// </summary>
    private final String currentAccountId;

    /// <summary>
    /// 어댑터 생성
    /// </summary>
    public RankingAdapter(List<AccountProfile> profiles, String currentAccountId) {
        this.profiles = new ArrayList<>(profiles);
        this.currentAccountId = currentAccountId;
    }

    /// <summary>
    /// 한 줄 뷰 생성 (item_ranking.xml inflate)
    /// </summary>
    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRankingBinding binding = ItemRankingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RankingViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 프로필을 줄에 채움 (순위 = 위치 + 1)
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        AccountProfile profile = profiles.get(position);
        int rank = position + 1;
        boolean isMe = profile.getId().equals(currentAccountId);
        holder.bind(profile, rank, isMe);
    }

    /// <summary>
    /// 전체 줄 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return profiles.size();
    }
}
