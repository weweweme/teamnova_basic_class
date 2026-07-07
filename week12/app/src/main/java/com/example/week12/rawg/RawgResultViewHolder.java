package com.example.week12.rawg;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.App;
import com.example.week12.R;
import com.example.week12.data.GameRepository;
import com.example.week12.databinding.ItemRawgResultBinding;
import com.example.week12.model.RawgGame;
import com.example.week12.util.CoverImageLoader;

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

        // 표지 — RAWG는 https 원격 이미지. 공용 로더(loadUri)가 http면 백그라운드로 내려받아 디코딩
        // (셀 재활용 시 로더의 setTag가 엉뚱한 표지 덮어쓰기를 막아줌 — 보관함 그리드와 같은 원리)
        // 표지가 없는(null) 게임도 있으므로 그때는 기본 아이콘
        Context context = binding.getRoot().getContext();
        CoverImageLoader loader = ((App) context.getApplicationContext()).getCoverImageLoader();
        String coverUrl = game.getCoverImageUrl();
        boolean hasCover = coverUrl != null && !coverUrl.isEmpty();
        if (hasCover) {
            loader.loadUri(binding.imageViewCover, coverUrl);
        } else {
            // 표지 없음 → 기본 아이콘 (대기 중이던 이전 로드가 이 셀을 덮어쓰지 않게 태그 비움)
            binding.imageViewCover.setTag(null);
            binding.imageViewCover.setImageResource(R.mipmap.ic_launcher);
        }

        // 이미 보관함에 같은 게임(rawgId)이 있으면 "✓ 보관함에 있음" 배지 표시 (누르기 전에 미리 알림)
        GameRepository repository = ((App) context.getApplicationContext()).getGameRepository();
        boolean inLibrary = repository.findByRawgId(game.getRawgId()) != null;
        binding.textViewInLibrary.setVisibility(inLibrary ? View.VISIBLE : View.GONE);

        // 항목 클릭 리스너 (Activity 측 콜백 호출 — 이 게임을 보관함에 추가 등)
        binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onResultClick(game);
            }
        });
    }
}
