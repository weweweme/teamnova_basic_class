package com.example.week12.rawg;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.databinding.ItemRawgResultBinding;
import com.example.week12.model.RawgGame;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// RAWG 검색 결과 RecyclerView 어댑터
/// RawgGame 목록과 결과 한 줄(RawgResultViewHolder)을 연결
///
/// 단일 뷰타입(한 줄 모양 1종) → getItemViewType 없이 onCreate/onBind만 구현
/// 처음엔 빈 목록으로 시작하고, 검색 성공 시 updateItems로 결과를 교체한다
/// </summary>
public class RawgResultAdapter extends RecyclerView.Adapter<RawgResultViewHolder> {

    /// <summary>
    /// 화면에 표시할 검색 결과 목록 (검색할 때마다 교체되는 가변 리스트)
    /// 생성자에서 받은 원본을 복사해 보관 → updateItems로 갈아끼워도 원본은 안전
    /// </summary>
    private final List<RawgGame> results;

    /// <summary>
    /// 항목 클릭 콜백 (클릭된 게임을 Activity에 전달)
    /// </summary>
    private final OnRawgResultClickListener clickListener;

    /// <summary>
    /// 어댑터 생성 (보통 빈 목록으로 시작 → 검색 후 updateItems로 채움)
    /// 넘어온 리스트를 그대로 참조하지 않고 복사 → 교체해도 원본 보호
    /// </summary>
    public RawgResultAdapter(List<RawgGame> results, OnRawgResultClickListener clickListener) {
        this.results = new ArrayList<>(results);
        this.clickListener = clickListener;
    }

    /// <summary>
    /// 표시 목록을 새 검색 결과로 교체
    /// 기존 내용을 비우고 새 항목으로 채운 뒤 전체 갱신
    /// (어떤 항목이 추가/삭제됐는지 추적하지 않으므로 notifyDataSetChanged)
    /// </summary>
    public void updateItems(List<RawgGame> newItems) {
        results.clear();
        results.addAll(newItems);
        notifyDataSetChanged();
    }

    /// <summary>
    /// 다음 페이지 결과를 기존 목록 "뒤에 이어붙인다" (무한 스크롤용)
    /// 전체 갱신(notifyDataSetChanged) 대신 추가된 범위만 알려 → 이미 보이는 항목은 다시 안 그림
    /// </summary>
    public void appendItems(List<RawgGame> moreItems) {
        int start = results.size();
        results.addAll(moreItems);
        notifyItemRangeInserted(start, moreItems.size());
    }

    /// <summary>
    /// 현재 표시 중인 결과 개수 (무한 스크롤에서 "비어있나?" 판단 등에 사용)
    /// </summary>
    public int getCurrentCount() {
        return results.size();
    }

    /// <summary>
    /// 결과 한 줄 뷰를 새로 생성 (item_rawg_result.xml inflate)
    /// </summary>
    @NonNull
    @Override
    public RawgResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRawgResultBinding binding = ItemRawgResultBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RawgResultViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 RawgGame 데이터를 결과 한 줄에 채움
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull RawgResultViewHolder holder, int position) {
        RawgGame game = results.get(position);
        holder.bind(game, clickListener);
    }

    /// <summary>
    /// 전체 결과 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return results.size();
    }
}
