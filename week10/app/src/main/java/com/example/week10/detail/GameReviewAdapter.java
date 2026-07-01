package com.example.week10.detail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week10.databinding.ItemGameReviewBinding;
import com.example.week10.model.GameReview;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// "다른 사람들의 평가" 목록 RecyclerView 어댑터
/// 리뷰 목록(GameReview)과 한 줄 뷰(GameReviewViewHolder)를 연결 (단일 뷰타입)
/// </summary>
public class GameReviewAdapter extends RecyclerView.Adapter<GameReviewViewHolder> {

    /// <summary>
    /// 화면에 보여줄 리뷰 목록 (넘어온 리스트를 복사해 보관)
    /// </summary>
    private final List<GameReview> reviews;

    /// <summary>
    /// 어댑터 생성
    /// </summary>
    public GameReviewAdapter(List<GameReview> reviews) {
        this.reviews = new ArrayList<>(reviews);
    }

    /// <summary>
    /// 한 줄 뷰 생성 (item_game_review.xml inflate)
    /// </summary>
    @NonNull
    @Override
    public GameReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGameReviewBinding binding = ItemGameReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GameReviewViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 리뷰를 줄에 채움
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull GameReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    /// <summary>
    /// 전체 리뷰 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return reviews.size();
    }
}
