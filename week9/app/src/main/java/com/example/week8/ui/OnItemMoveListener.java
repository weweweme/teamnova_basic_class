package com.example.week8.ui;

/// <summary>
/// RecyclerView 항목 위치 이동 콜백
/// ItemTouchHelper.SimpleCallback이 드래그로 위치가 바뀔 때마다 호출
///
/// 어댑터 또는 Activity 측이 이 콜백을 구현하여
///   1. 데이터 리스트의 from → to 위치 변경
///   2. adapter.notifyItemMoved(from, to) 호출
/// 두 가지를 처리하게 함
///
/// ──── @FunctionalInterface 란? ────
/// 추상 메서드가 정확히 1개뿐인 인터페이스에 붙이는 표시.
/// 이 어노테이션을 달면:
///   - 메서드를 실수로 2개 이상 만들면 컴파일 에러로 막아줌 (의도 보호)
///   - "이건 람다/메서드 참조로 넘길 수 있는 함수형 인터페이스다"라는 의도를 명시
/// 덕분에 호출부에서 adapter::onItemMove 같은 메서드 참조로 바로 넘길 수 있음
/// (DiaryActivity의 드래그 정렬 설정 부분 참고)
/// </summary>
@FunctionalInterface
public interface OnItemMoveListener {

    /// <summary>
    /// 드래그로 항목이 한 칸 이동했을 때 호출
    /// ItemTouchHelper는 인접 위치 교환만 호출하므로
    /// (from, to)는 보통 차이가 1인 값이지만, 빠른 드래그 시 더 클 수도 있음
    /// </summary>
    void onItemMove(int fromPosition, int toPosition);
}
