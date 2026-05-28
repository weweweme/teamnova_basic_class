package com.example.week8.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/// <summary>
/// 게임 카드 드래그 정렬용 ItemTouchHelper 콜백
///
/// ──── ItemTouchHelper란? ────
/// androidx.recyclerview.widget.ItemTouchHelper — 안드로이드 공식 유틸리티 클래스
/// RecyclerView에 부착하면 터치 이벤트를 가로채서
/// 드래그(drag &amp; drop)와 스와이프(swipe-to-dismiss)를 자동으로 처리해준다.
/// 즉 어댑터/뷰홀더가 일일이 onTouchEvent를 다루지 않아도 표준 정렬 UX를 얻을 수 있음.
///
/// 사용 흐름 (3단계):
///   1. ItemTouchHelper.Callback (또는 SimpleCallback) 상속 → 정책 정의 (이 클래스)
///   2. new ItemTouchHelper(callback) 으로 인스턴스 생성
///   3. itemTouchHelper.attachToRecyclerView(recyclerView) 로 부착
///      → 부착 후부터 RecyclerView의 터치 이벤트를 ItemTouchHelper가 처리
///
/// SimpleCallback vs 일반 Callback:
///   Callback       : 모든 추상 메서드(getMovementFlags 등)를 직접 구현해야 함
///   SimpleCallback : 생성자에 dragDirs / swipeDirs 만 한 번에 전달 → 단순 케이스에 적합
///   → 우리는 세로 드래그만 필요하므로 SimpleCallback 사용
///
/// 공식 문서:
///   https://developer.android.com/reference/androidx/recyclerview/widget/ItemTouchHelper
///
/// ──── 동작 정책 ────
/// - 드래그 방향: 위/아래(UP|DOWN)만 허용 (세로 리스트)
/// - 스와이프(좌/우 밀어서 삭제) 비활성화
/// - "길게 누르면 자동 드래그" 비활성화 → 카드 길게 누름은 BottomSheet용
/// - 드래그는 카드 우측 핸들(ImageView)을 ACTION_DOWN으로 터치할 때만 시작됨
///   (시작 트리거는 ViewHolder에서 ItemTouchHelper.startDrag(holder) 명시 호출)
///
/// ──── 호출 흐름 ────
/// 사용자가 핸들을 누른 채 이동
///   → ItemTouchHelper가 onMove() 반복 호출 (인접 위치 교환 단위로)
///   → onMove → listener.onItemMove(from, to) 호출
///   → 어댑터가 데이터 리스트를 갱신하고 notifyItemMoved 호출
/// </summary>
public class GameCardItemTouchCallback extends ItemTouchHelper.SimpleCallback {

    /// <summary>
    /// 위치 이동 콜백 (보통 어댑터의 onItemMove 메서드 참조)
    /// </summary>
    private final OnItemMoveListener listener;

    /// <summary>
    /// 콜백 생성
    /// SimpleCallback 부모 생성자에 dragDirs=UP|DOWN, swipeDirs=0을 전달
    /// </summary>
    public GameCardItemTouchCallback(OnItemMoveListener listener) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        this.listener = listener;
    }

    /// <summary>
    /// 드래그 중 한 위치가 다른 위치 위로 지나갈 때 호출
    /// 진짜 위치 교환은 listener(어댑터)가 수행
    /// </summary>
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        int from = viewHolder.getBindingAdapterPosition();
        int to = target.getBindingAdapterPosition();
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
            return false;
        }
        listener.onItemMove(from, to);
        return true;
    }

    /// <summary>
    /// 스와이프 콜백 — 이 화면에선 사용하지 않음
    /// SimpleCallback의 swipeDirs=0으로 두면 실제로 호출되지 않지만 추상 메서드라 빈 구현 필요
    /// </summary>
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // no-op
    }

    /// <summary>
    /// false: 카드를 길게 눌러도 드래그 시작 안 함
    /// → 드래그는 오직 ViewHolder가 명시적으로 itemTouchHelper.startDrag(holder)를
    ///   호출할 때만 시작 (핸들 ACTION_DOWN 경로)
    /// → 길게 누름 제스처는 BottomSheet 메뉴 전용으로 분리됨
    /// </summary>
    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }
}
