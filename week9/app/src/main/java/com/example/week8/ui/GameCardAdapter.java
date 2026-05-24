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
    /// 비싼 작업(XML 인플레이트)이라 "화면에 보일 만큼만" 호출되고 이후엔 재사용됨
    ///
    /// ──── 왜 "화면에 보일 만큼만" 만들어지는가? (공식 API 기준 흐름) ────
    /// 1. RecyclerView.onMeasure → 자신의 크기(가용 영역) 확정
    /// 2. RecyclerView.onLayout → LayoutManager.onLayoutChildren(Recycler, State) 호출
    /// 3. LinearLayoutManager.onLayoutChildren 내부 동작:
    ///    - 레이아웃 시작 기준점("anchor")을 결정 (첫 진입 시 position 0)
    ///    - fill() 메서드가 "남은 공간(remaining space)"을 채울 때까지
    ///      layoutChunk()를 반복 호출
    ///    - layoutChunk()는 recycler.getViewForPosition(position)로 뷰를 가져와
    ///      measure + layout(배치) 수행
    ///    - 다음 자식 뷰가 영역 경계를 넘어가면 fill 루프 종료 → 더 이상 뷰 요청 X
    /// 4. Recycler.getViewForPosition 내부 캐시 조회 순서:
    ///    Scrap → CachedViews → ViewCacheExtension → RecycledViewPool
    ///    → 모두 미스면 비로소 어댑터의 onCreateViewHolder(이 메서드) 호출됨
    ///
    /// → 결과: "보이는 영역에 들어가는 뷰 + 약간의 prefetch 버퍼"만큼만 새로 생성
    ///   이후 스크롤로 새 항목이 등장해도 재사용 풀에서 꺼내쓰므로 이 메서드는 거의 호출 X
    ///   (호출되는 건 onBindViewHolder만 — 데이터만 갈아끼움)
    ///
    /// 정확한 구현은 소스 참고: https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/recyclerview/recyclerview/src/main/java/androidx/recyclerview/widget/
    ///   LinearLayoutManager
    ///   RecyclerView
    ///
    /// ──── parent를 넘기지만 attachToParent=false 인 이유 ────
    /// inflate(layout, parent, attachToParent) 세 번째 인자 의미:
    ///   parent         : 부모 ViewGroup (= RecyclerView)
    ///   attachToParent : 인플레이트한 뷰를 즉시 parent에 addView로 붙일지
    ///
    /// RecyclerView는 뷰의 부착/해제/재사용을 자체적으로 관리하므로,
    /// 여기서 직접 부착하면(true) → RecyclerView가 또 붙이려다 충돌
    ///   → IllegalStateException: "already added to a parent" 크래시
    ///
    /// 그럼 왜 parent를 넘기긴 하나?
    /// → parent의 LayoutParams를 가져와 match_parent/wrap_content 같은 값을 정확히 계산하기 위함
    ///   parent를 null로 두면 카드 너비가 wrap_content로 작게 잡혀버림
    ///
    /// 즉, 부모 크기 측정을 위해 인자를 넘기고 다르게 사용하지 않음
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
        holder.bindGameData(game, clickListener, longClickListener);
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
