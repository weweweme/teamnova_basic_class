package com.example.week8.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week8.R;
import com.example.week8.databinding.ItemGameCardBinding;
import com.example.week8.model.Game;

/// <summary>
/// 게임 카드 한 칸의 뷰 참조를 보관하는 ViewHolder
///
/// ──── 역할 ────
/// item_game_card.xml의 자식 뷰들(제목/이미지/별점 등)을 ViewBinding으로 캐시
/// 처음에 약 12개만 생성되고, 이후엔 같은 인스턴스가 다른 위치의 데이터를 받아 재사용됨
/// bind()가 호출되면 받은 Game 데이터를 각 뷰에 채우고 클릭 리스너를 등록
/// </summary>
public class GameCardViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 카드 한 칸의 ViewBinding (자식 뷰 참조 모음)
    /// </summary>
    private final ItemGameCardBinding binding;

    /// <summary>
    /// ViewHolder 생성
    /// itemView로 binding.getRoot()을 부모 생성자에 넘겨야 RecyclerView가 위치 관리 가능
    /// </summary>
    public GameCardViewHolder(@NonNull ItemGameCardBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// Game 데이터를 카드 뷰의 각 자리에 채움
    /// 제목, 장르·플랫폼, 별점·한줄평, 표지 이미지, 클릭/롱클릭 리스너 설정
    /// </summary>
    public void bindGameData(Game game,
                         OnGameClickListener clickListener,
                         OnGameLongClickListener longClickListener) {
        Context context = binding.getRoot().getContext();

        // 제목
        binding.textViewTitle.setText(game.getTitle());

        // 장르 · 플랫폼
        String genrePlatform = game.getGenre().getDisplayName()
                + " · " + game.getPlatform().getDisplayName();
        binding.textViewGenrePlatform.setText(genrePlatform);

        // 별점 + 한줄평 (리뷰가 없으면 "리뷰 없음" 표시)
        boolean hasReview = game.getReview() != null && !game.getReview().isEmpty();
        String ratingReview;
        if (hasReview) {
            ratingReview = "★ " + game.getRating() + "  " + game.getReview();
        } else {
            ratingReview = "리뷰 없음";
        }
        binding.textViewRatingReview.setText(ratingReview);

        // 표지 이미지 (이름 문자열로 drawable 리소스 ID 조회)
        // 게임마다 이미지 이름이 다르므로 getIdentifier 사용이 불가피
        // 리소스가 없으면 기본 아이콘으로 대체
        int coverResId = context.getResources().getIdentifier(
                game.getCoverAssetName(), "drawable", context.getPackageName());
        if (coverResId != 0) {
            binding.imageViewCover.setImageResource(coverResId);
        } else {
            binding.imageViewCover.setImageResource(R.mipmap.ic_launcher);
        }

        // 카드 클릭 리스너 (Activity 측 콜백 호출)
        binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGameClick(game);
            }
        });

        // 카드 길게 누르기 리스너
        // return true: "이벤트를 내가 처리했음" → 짧은 클릭 이벤트로 전파되지 않음
        // return false: 짧은 클릭으로 이어짐 (long click과 click이 동시에 발생)
        binding.getRoot().setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onGameLongClick(game);
                return true;
            }
            return false;
        });
    }
}
