package com.example.week8.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week8.databinding.ItemLibraryGridBinding;
import com.example.week8.model.Game;

import java.util.List;

/// <summary>
/// 라이브러리 그리드 RecyclerView 어댑터
/// Game 데이터 리스트와 그리드 셀(LibraryViewHolder)을 연결
///
/// GameCardAdapter(세로 리스트)와 구조는 같지만:
///   - 레이아웃이 다름 (item_library_grid)
///   - 클릭만 지원 (드래그/롱클릭 없음 — 그리드엔 불필요)
/// 배치 방식(2~3열 그리드)은 어댑터가 아니라 LayoutManager가 결정하므로
/// 어댑터 코드는 GameCardAdapter와 거의 동일 — LayoutManager만 GridLayoutManager로 바꾸면 됨
/// </summary>
public class LibraryAdapter extends RecyclerView.Adapter<LibraryViewHolder> {

    /// <summary>
    /// 표시할 게임 목록 (GameRepository의 리스트를 그대로 참조)
    /// </summary>
    private final List<Game> games;

    /// <summary>
    /// 셀 클릭 콜백 (DiaryActivity와 같은 OnGameClickListener 재사용)
    /// </summary>
    private final OnGameClickListener clickListener;

    /// <summary>
    /// 어댑터 생성
    /// </summary>
    public LibraryAdapter(List<Game> games, OnGameClickListener clickListener) {
        this.games = games;
        this.clickListener = clickListener;
    }

    /// <summary>
    /// 그리드 셀 뷰를 새로 생성 (item_library_grid.xml inflate)
    /// </summary>
    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLibraryGridBinding binding = ItemLibraryGridBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LibraryViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 Game 데이터를 셀에 채움
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bindGameData(game, clickListener);
    }

    /// <summary>
    /// 전체 항목 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return games.size();
    }
}
