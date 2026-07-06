package com.example.week12.trash;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.databinding.ItemTrashBinding;
import com.example.week12.model.TrashEntry;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 휴지통 목록 RecyclerView 어댑터
/// 삭제됐지만 아직 영구삭제되지 않은 게임들을 한 줄씩(TrashViewHolder) 표시
/// 각 항목의 "버린 시각"으로 "N일 후 삭제"를 계산해 보여줌
/// </summary>
public class TrashAdapter extends RecyclerView.Adapter<TrashViewHolder> {

    /// <summary>
    /// 표시할 휴지통 항목 목록 (게임 + 버린 시각). 원본을 복사해 보관
    /// </summary>
    private final List<TrashEntry> entries;

    /// <summary>
    /// "N일 후 삭제" 계산 기준이 될 현재 시각 (화면 로드 시점)
    /// </summary>
    private long now;

    /// <summary>
    /// 복원 / 영구삭제 콜백
    /// </summary>
    private final OnTrashActionListener listener;

    /// <summary>
    /// 어댑터 생성
    /// </summary>
    public TrashAdapter(List<TrashEntry> entries, long now, OnTrashActionListener listener) {
        this.entries = new ArrayList<>(entries);
        this.now = now;
        this.listener = listener;
    }

    /// <summary>
    /// 표시 목록을 새 목록으로 교체 (복원/영구삭제 후 갱신)
    /// </summary>
    public void updateItems(List<TrashEntry> newItems, long now) {
        this.now = now;
        entries.clear();
        entries.addAll(newItems);
        notifyDataSetChanged();
    }

    /// <summary>
    /// 한 줄 뷰 생성 (item_trash.xml inflate)
    /// </summary>
    @NonNull
    @Override
    public TrashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTrashBinding binding = ItemTrashBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TrashViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 Game을 한 줄에 채움
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull TrashViewHolder holder, int position) {
        holder.bindEntry(entries.get(position), now, listener);
    }

    /// <summary>
    /// 전체 항목 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return entries.size();
    }
}
