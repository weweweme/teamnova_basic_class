package com.example.week8.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week8.databinding.ItemLibraryGridBinding;
import com.example.week8.model.Game;

import java.util.ArrayList;
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
    /// 화면에 표시할 게임 목록 (필터 결과가 담기는 가변 리스트)
    /// 생성자에서 받은 원본을 복사해 보관 → 필터로 clear/addAll 해도 원본은 안전
    /// </summary>
    private final List<Game> games;

    /// <summary>
    /// 셀 클릭 콜백
    /// </summary>
    private final OnGameClickListener clickListener;

    /// <summary>
    /// 셀 길게 누르기 콜백 (BottomSheet 메뉴 표시용, 없으면 null)
    /// </summary>
    private final OnGameLongClickListener longClickListener;

    /// <summary>
    /// 셀 고정 폭(dp). 0이면 LayoutManager가 폭 결정 (그리드 = 화면 분할)
    /// 0보다 크면 셀 폭을 이 값으로 고정 → 가로 스크롤 미리보기에서 여러 개 보이게
    /// (홈 화면 가로 미리보기 전용. 보관함 그리드는 setter를 호출 안 해 0 유지)
    /// </summary>
    private int itemWidthDp = 0;

    /// <summary>
    /// 가로 미리보기용 셀 고정 폭 지정 (dp)
    /// </summary>
    public void setItemWidthDp(int dp) {
        this.itemWidthDp = dp;
    }

    /// <summary>
    /// 어댑터 생성
    /// 넘어온 리스트를 그대로 참조하지 않고 복사 → updateItems로 교체해도 원본 보호
    /// </summary>
    public LibraryAdapter(List<Game> games,
                          OnGameClickListener clickListener,
                          OnGameLongClickListener longClickListener) {
        this.games = new ArrayList<>(games);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    /// <summary>
    /// 표시 목록을 새 목록으로 교체 (상태별 필터 탭에서 사용)
    /// 기존 내용을 비우고 새 항목으로 채운 뒤 전체 갱신
    /// (어떤 항목이 추가/삭제됐는지 추적하지 않으므로 notifyDataSetChanged)
    /// </summary>
    public void updateItems(List<Game> newItems) {
        games.clear();
        games.addAll(newItems);
        notifyDataSetChanged();
    }

    /// <summary>
    /// 그리드 셀 뷰를 새로 생성 (item_library_grid.xml inflate)
    /// </summary>
    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLibraryGridBinding binding = ItemLibraryGridBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        // 고정 폭이 지정되면 셀 폭을 dp → px로 변환해 적용 (가로 미리보기용)
        // 지정 안 됐으면(0) 레이아웃 원래 값(match_parent) 유지 → 그리드에선 LayoutManager가 폭 결정
        if (itemWidthDp > 0) {
            float density = parent.getResources().getDisplayMetrics().density;
            int widthPx = (int) (itemWidthDp * density);
            ViewGroup.LayoutParams lp = binding.getRoot().getLayoutParams();
            lp.width = widthPx;
            binding.getRoot().setLayoutParams(lp);
        }

        return new LibraryViewHolder(binding);
    }

    /// <summary>
    /// 특정 위치의 Game 데이터를 셀에 채움
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bindGameData(game, clickListener, longClickListener);
    }

    /// <summary>
    /// 전체 항목 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return games.size();
    }
}
