package com.example.week12.trash;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.App;
import com.example.week12.R;
import com.example.week12.data.GameRepository;
import com.example.week12.databinding.ItemTrashBinding;
import com.example.week12.model.Game;
import com.example.week12.model.TrashEntry;
import com.example.week12.util.CoverImageLoader;

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
    /// 휴지통 항목을 한 줄에 채움 (표지 + 제목 + 남은 일수 + 버튼 리스너)
    /// nowMillis: 현재 시각 — 버린 시각과 비교해 "N일 후 삭제"를 계산
    /// </summary>
    public void bindEntry(TrashEntry entry, long nowMillis, OnTrashActionListener listener) {
        Context context = binding.getRoot().getContext();
        Game game = entry.getGame();

        // 제목
        binding.textViewTrashTitle.setText(game.getTitle());

        // 남은 일수 = (보관기간 - 이미 지난 시간)을 하루 단위로 올림 (최소 1일로 표시)
        long elapsed = nowMillis - entry.getTrashedAt();
        long remainingMs = GameRepository.TRASH_RETENTION_MS - elapsed;
        long dayMs = 24L * 60 * 60 * 1000;
        int daysLeft = (int) Math.max(1, Math.ceil(remainingMs / (double) dayMs));
        binding.textViewTrashDaysLeft.setText(
                context.getString(R.string.trash_days_left, daysLeft));

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
