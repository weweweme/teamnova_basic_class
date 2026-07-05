package com.example.week11.trash;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.databinding.ItemTrashBinding;
import com.example.week11.model.Game;
import com.example.week11.util.CoverImageLoader;

/// <summary>
/// 휴지통 한 줄의 뷰 참조를 보관하는 ViewHolder
/// 표지 + 제목을 채우고, 복원/영구삭제 버튼을 콜백으로 Activity에 전달
/// </summary>
public class TrashViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 한 줄의 ViewBinding
    /// </summary>
    private final ItemTrashBinding binding;

    /// <summary>
    /// ViewHolder 생성 (itemView로 binding.getRoot()을 부모 생성자에 넘김)
    /// </summary>
    public TrashViewHolder(@NonNull ItemTrashBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// Game 데이터를 한 줄에 채움 (표지 + 제목 + 버튼 리스너)
    /// </summary>
    public void bindGameData(Game game, OnTrashActionListener listener) {
        Context context = binding.getRoot().getContext();

        // 제목
        binding.textViewTrashTitle.setText(game.getTitle());

        // 표지 (공용 로더: 백그라운드 디코딩 + 캐시)
        // 게임마다 이미지 이름이 달라 getIdentifier 사용 (리소스 없으면 0 → 기본 아이콘)
        int coverResId = context.getResources().getIdentifier(
                game.getCoverAssetName(), "drawable", context.getPackageName());
        CoverImageLoader loader = ((App) context.getApplicationContext()).getCoverImageLoader();
        loader.loadCover(binding.imageViewTrashCover, coverResId, R.mipmap.ic_launcher);

        // 복원 / 영구삭제 → 콜백으로 Activity에 위임
        binding.buttonTrashRestore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestore(game);
            }
        });
        binding.buttonTrashDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeletePermanently(game);
            }
        });
    }
}
