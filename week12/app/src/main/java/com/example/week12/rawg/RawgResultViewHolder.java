package com.example.week12.rawg;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.R;
import com.example.week12.databinding.ItemRawgResultBinding;
import com.example.week12.model.RawgGame;

import java.util.Locale;

/// <summary>
/// RAWG 검색 결과 한 줄의 뷰 참조를 보관하는 ViewHolder
/// 항목 레이아웃은 가로형 (표지 썸네일 + 제목/출시일/평점)
/// 데이터를 뷰에 채우고(bind), 클릭을 콜백으로 Activity에 전달
/// </summary>
public class RawgResultViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 결과 한 줄의 ViewBinding (자식 뷰 참조 모음)
    /// </summary>
    private final ItemRawgResultBinding binding;

    /// <summary>
    /// ViewHolder 생성
    /// itemView로 binding.getRoot()을 부모 생성자에 넘겨야 RecyclerView가 위치 관리 가능
    /// </summary>
    public RawgResultViewHolder(@NonNull ItemRawgResultBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// RawgGame 데이터를 결과 한 줄에 채움 (제목 + 출시일 + 평점 + 표지)
    /// 항목 클릭 시 콜백으로 Activity에 알림
    /// </summary>
    public void bind(RawgGame game, OnRawgResultClickListener clickListener) {
        // 제목
        binding.textViewName.setText(game.getName());

        // 출시일 — 없으면(빈 문자열) "출시일 미정"으로 표시
        String released = game.getReleased();
        boolean hasReleased = released != null && !released.isEmpty();
        binding.textViewReleased.setText(hasReleased ? released : "출시일 미정");

        // RAWG 평균 평점 — 0이면 "평점 없음", 있으면 "★ 4.4" 형식
        float rating = game.getRating();
        if (rating > 0f) {
            binding.textViewRating.setText(
                    String.format(Locale.getDefault(), "★ %.1f", rating));
        } else {
            binding.textViewRating.setText("평점 없음");
        }

        // 표지 — P2에서는 기본 아이콘만 표시 (원격 이미지 로딩은 P3에서 추가)
        // RAWG 표지는 https 주소라 지금 로더로는 못 불러오므로, 우선 자리만 채운다
        binding.imageViewCover.setImageResource(R.mipmap.ic_launcher);

        // 항목 클릭 리스너 (Activity 측 콜백 호출 — 이 게임을 보관함에 추가 등)
        binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onResultClick(game);
            }
        });
    }
}
