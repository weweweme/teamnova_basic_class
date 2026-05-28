package com.example.week8.ui;

/// <summary>
/// RecyclerView 항목 위치 이동 콜백
/// ItemTouchHelper.SimpleCallback이 드래그로 위치가 바뀔 때마다 호출
///
/// 어댑터 또는 Activity 측이 이 콜백을 구현하여
///   1. 데이터 리스트의 from → to 위치 변경
///   2. adapter.notifyItemMoved(from, to) 호출
/// 두 가지를 처리하게 함
/// </summary>
public interface OnItemMoveListener {

    /// <summary>
    /// 드래그로 항목이 한 칸 이동했을 때 호출
    /// ItemTouchHelper는 인접 위치 교환만 호출하므로
    /// (from, to)는 보통 차이가 1인 값이지만, 빠른 드래그 시 더 클 수도 있음
    /// </summary>
    void onItemMove(int fromPosition, int toPosition);
}
