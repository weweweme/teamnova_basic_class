package com.example.week10.library;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week10.R;
import com.example.week10.databinding.ItemLibraryGridBinding;
import com.example.week10.model.Game;

/// <summary>
/// 보관함 그리드 한 칸의 뷰 참조를 보관하는 ViewHolder
/// 셀 레이아웃은 세로형 포스터 (표지 + 제목)
/// 데이터를 뷰에 채우고(bindGameData), 클릭/길게 누르기를 콜백으로 Activity에 전달
/// </summary>
public class LibraryViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 그리드 셀 한 칸의 ViewBinding (자식 뷰 참조 모음)
    /// </summary>
    private final ItemLibraryGridBinding binding;

    /// <summary>
    /// ViewHolder 생성
    /// itemView로 binding.getRoot()을 부모 생성자에 넘겨야 RecyclerView가 위치 관리 가능
    /// </summary>
    public LibraryViewHolder(@NonNull ItemLibraryGridBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// Game 데이터를 그리드 셀에 채움 (표지 이미지 + 제목)
    /// 셀 클릭/길게 누르기 시 각 콜백을 통해 Activity에 알림
    /// </summary>
    public void bindGameData(Game game,
                             OnGameClickListener clickListener,
                             OnGameLongClickListener longClickListener) {
        Context context = binding.getRoot().getContext();

        // 제목
        binding.textViewTitle.setText(game.getTitle());

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

        // 셀 클릭 리스너 (Activity 측 콜백 호출)
        binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGameClick(game);
            }
        });

        // 셀 길게 누르기 리스너 (BottomSheet 메뉴 표시용)
        binding.getRoot().setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onGameLongClick(game);
                return true;
            }
            return false;
        });
    }
}
