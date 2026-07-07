package com.example.week12.rawg;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/// <summary>
/// 무한 스크롤 리스너 — 리스트 바닥 근처까지 스크롤하면 "다음 페이지 불러와" 신호를 보낸다
///
/// 검색·탐색 화면이 똑같이 쓰는 부품이라 공용으로 뺐다 (DRY).
/// "지금 로딩 중인지 / 다음 페이지가 있는지" 판단은 여기서 안 하고, 화면(onLoadMore) 쪽에서 한다
///   → 이 클래스는 "바닥 가까워졌다"만 알려주고, 실제 로딩 여부는 화면이 결정한다 (역할 분리)
/// </summary>
public class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    /// <summary>
    /// 남은 항목이 이 개수 이하로 보이면 미리 다음 페이지를 불러온다 (바닥에 닿기 전에 준비 → 끊김 줄임)
    /// </summary>
    private static final int LOAD_MORE_THRESHOLD = 4;

    /// <summary>
    /// 스크롤 위치를 계산하기 위한 LayoutManager (세로 리스트)
    /// </summary>
    private final LinearLayoutManager layoutManager;

    /// <summary>
    /// 바닥 근처에 도달했을 때 실행할 동작 (화면이 "다음 페이지 로딩"을 여기에 연결)
    /// </summary>
    private final Runnable onLoadMore;

    /// <summary>
    /// 리스너 생성
    /// layoutManager: 대상 RecyclerView의 LinearLayoutManager
    /// onLoadMore: 바닥 근처에서 부를 동작
    /// </summary>
    public InfiniteScrollListener(LinearLayoutManager layoutManager, Runnable onLoadMore) {
        this.layoutManager = layoutManager;
        this.onLoadMore = onLoadMore;
    }

    /// <summary>
    /// 스크롤될 때마다 호출 — 아래로 스크롤 중이고 바닥에 거의 다다랐으면 onLoadMore 실행
    /// </summary>
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        // 아래로 스크롤할 때만 (위로 올릴 때는 더 안 부름)
        if (dy <= 0) {
            return;
        }

        int totalCount = layoutManager.getItemCount();                 // 전체 항목 수
        int lastVisible = layoutManager.findLastVisibleItemPosition(); // 지금 화면에 보이는 마지막 항목 위치

        // 마지막으로 보이는 항목이 끝에서 THRESHOLD개 이내면 → 다음 페이지 준비
        boolean nearBottom = lastVisible + LOAD_MORE_THRESHOLD >= totalCount;
        if (nearBottom) {
            onLoadMore.run();
        }
    }
}
