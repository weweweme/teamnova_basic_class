package com.example.week8.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week8.databinding.ItemGameCardBinding;
import com.example.week8.model.Game;

import java.util.List;

/// <summary>
/// 게임 카드 RecyclerView 어댑터
/// Game 데이터 리스트와 화면에 그려지는 카드(ViewHolder)를 연결
///
/// ──── 호출 흐름 ────
/// onCreateViewHolder: 화면에 보일 만큼만 처음에 한 번씩 호출 (약 12개 정도)
/// onBindViewHolder:   카드 한 칸에 데이터를 채울 때마다 호출 (스크롤 시 매번)
/// getItemCount:       전체 항목 개수 반환 (RecyclerView가 스크롤 범위 계산에 사용)
/// </summary>
public class GameCardAdapter extends RecyclerView.Adapter<GameCardViewHolder> {

    /// <summary>
    /// 표시할 게임 목록 (GameRepository의 리스트를 그대로 참조)
    /// 같은 리스트 인스턴스를 공유하므로 Repository가 갱신되면 notifyXxx만 호출하면 됨
    /// </summary>
    private final List<Game> games;

    /// <summary>
    /// 카드 클릭 콜백
    /// </summary>
    private final OnGameClickListener clickListener;

    /// <summary>
    /// 카드 길게 누르기 콜백 (보통 컨텍스트 메뉴 표시에 사용)
    /// </summary>
    private final OnGameLongClickListener longClickListener;

    /// <summary>
    /// 어댑터 생성
    /// </summary>
    public GameCardAdapter(List<Game> games,
                           OnGameClickListener clickListener,
                           OnGameLongClickListener longClickListener) {
        this.games = games;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    // ========== RecyclerView.Adapter 콜백 ==========

    /// <summary>
    /// 카드 한 칸의 뷰를 새로 생성
    /// item_game_card.xml을 inflate해서 ViewHolder로 감싸 반환
    /// 비싼 작업(XML 인플레이트)이라 처음 약 12번만 호출되고 이후엔 재사용됨
    /// </summary>
    @NonNull
    @Override
    public GameCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGameCardBinding binding = ItemGameCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GameCardViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 Game 데이터를 ViewHolder에 채움
    /// 스크롤할 때마다 호출되며, 같은 ViewHolder에 새 데이터만 갈아끼움
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull GameCardViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bind(game, clickListener, longClickListener);
    }

    /// <summary>
    /// 전체 항목 개수 반환
    /// RecyclerView/LayoutManager가 "몇 개를 그려야 할지" 계산할 때 호출
    /// </summary>
    @Override
    public int getItemCount() {
        return games.size();
    }
}
