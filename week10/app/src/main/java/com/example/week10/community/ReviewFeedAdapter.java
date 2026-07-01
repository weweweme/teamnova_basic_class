package com.example.week10.community;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week10.databinding.ItemReviewFeedBinding;
import com.example.week10.model.ReviewFeedItem;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 리뷰 피드 RecyclerView 어댑터
/// 피드 항목 목록(ReviewFeedItem)과 한 줄 뷰(ReviewFeedViewHolder)를 연결 (단일 뷰타입)
/// 항목 클릭은 콜백으로 화면에 위임 → 그 게임 상세로 이동
/// </summary>
public class ReviewFeedAdapter extends RecyclerView.Adapter<ReviewFeedViewHolder> {

    /// <summary>
    /// 화면에 보여줄 피드 항목 목록 (넘어온 리스트를 복사해 보관)
    /// </summary>
    private final List<ReviewFeedItem> items;

    /// <summary>
    /// 항목 클릭 콜백 (게임 id 전달)
    /// </summary>
    private final OnReviewFeedClickListener clickListener;

    /// <summary>
    /// 어댑터 생성
    /// </summary>
    public ReviewFeedAdapter(List<ReviewFeedItem> items, OnReviewFeedClickListener clickListener) {
        this.items = new ArrayList<>(items);
        this.clickListener = clickListener;
    }

    /// <summary>
    /// 한 줄 뷰 생성 (item_review_feed.xml inflate)
    /// </summary>
    @NonNull
    @Override
    public ReviewFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewFeedBinding binding = ItemReviewFeedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReviewFeedViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 피드 항목을 줄에 채움
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull ReviewFeedViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener);
    }

    /// <summary>
    /// 전체 항목 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return items.size();
    }
}
