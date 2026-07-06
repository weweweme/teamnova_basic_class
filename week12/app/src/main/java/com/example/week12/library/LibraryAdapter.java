package com.example.week12.library;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.account.UserPrefs;
import com.example.week12.databinding.ItemLibraryGridBinding;
import com.example.week12.model.Game;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 보관함 그리드 RecyclerView 어댑터
/// Game 데이터 리스트와 그리드 셀(LibraryViewHolder)을 연결
///
/// 단일 뷰타입 어댑터(셀 모양 1종) → getItemViewType 없이 onCreate/onBind만 구현
/// 배치 방식(2열 그리드 / 가로 스크롤)은 어댑터가 아니라 LayoutManager가 결정하므로
/// 같은 어댑터를 보관함(그리드)과 홈 미리보기(가로 스크롤)에서 그대로 재사용
/// 셀 클릭 → 상세, 셀 길게 누르기 → BottomSheet (콜백으로 Activity에 위임)
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
    /// 현재 로그인 계정의 저장소 — 카드마다 "내 별점"을 조회하는 데 사용
    /// (다이어리이므로 카드엔 커뮤니티 평균이 아니라 내가 준 별점을 표시. 안 준 게임은 배지 없음)
    /// </summary>
    private final UserPrefs userPrefs;

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
    /// 무한 순환(루프) 표시 여부 — true면 목록이 끝없이 반복되어 "끝 다음에 처음"이 이어짐
    /// (홈 가로 미리보기 전용. 보관함 그리드는 끄고 실제 개수만 표시)
    /// </summary>
    private boolean looping = false;

    /// <summary>
    /// 루프 모드에서 실제 목록을 몇 번 반복해 "사실상 무한"으로 보이게 할지
    /// (진짜 무한대는 스크롤 좌표가 넘칠 수 있어, 충분히 큰 유한 배수로 대신함)
    /// </summary>
    private static final int LOOP_REPEATS = 1000;

    /// <summary>
    /// 무한 순환 표시 켜기/끄기
    /// </summary>
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    /// <summary>
    /// 어댑터 생성
    /// 넘어온 리스트를 그대로 참조하지 않고 복사 → updateItems로 교체해도 원본 보호
    /// </summary>
    public LibraryAdapter(List<Game> games,
                          OnGameClickListener clickListener,
                          OnGameLongClickListener longClickListener,
                          UserPrefs userPrefs) {
        this.games = new ArrayList<>(games);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.userPrefs = userPrefs;
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
        // 루프 모드면 position이 실제 개수를 넘어가므로 나머지(%)로 실제 항목을 찾는다
        // (루프가 아니면 position < 개수라 position % 개수 == position → 그대로 동작)
        Game game = games.get(position % games.size());
        // 내가 이 게임에 준 별점 (안 줬으면 0 → 셀에서 배지 숨김) + 즐겨찾기 여부
        float myRating = (userPrefs != null) ? userPrefs.getRating(game.getId()) : 0f;
        boolean favorite = (userPrefs != null) && userPrefs.isFavorite(game.getId());
        holder.bindGameData(game, myRating, favorite, clickListener, longClickListener);
    }

    /// <summary>
    /// 전체 항목 개수 반환
    /// 루프 모드면 실제 개수를 여러 번 반복한 값 → 끝이 없어 무한 순환처럼 보인다
    /// </summary>
    @Override
    public int getItemCount() {
        if (games.isEmpty()) {
            return 0;
        }
        return looping ? games.size() * LOOP_REPEATS : games.size();
    }
}
