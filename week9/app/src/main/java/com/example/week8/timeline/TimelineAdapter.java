package com.example.week8.timeline;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ItemLogAddedBinding;
import com.example.week8.databinding.ItemLogCompletedBinding;
import com.example.week8.databinding.ItemLogPlayedBinding;
import com.example.week8.databinding.ItemLogReviewedBinding;
import com.example.week8.model.ActivityLog;
import com.example.week8.model.ActivityLogType;
import com.example.week8.model.Game;

import java.util.List;

/// <summary>
/// 타임라인 RecyclerView 어댑터 (멀티 뷰타입)
///
/// ──── 멀티 뷰타입의 핵심 ────
/// 지금까지 어댑터(GameCardAdapter, LibraryAdapter)는 모든 항목이 같은 모양이었음.
/// 타임라인은 활동 종류(ADDED/COMPLETED/REVIEWED/PLAYED)마다 다른 레이아웃을 써야 함.
///
/// 이를 위해 RecyclerView.Adapter의 두 메서드가 협력:
///   1. getItemViewType(position) → 이 위치 항목이 "몇 번 뷰타입"인지 int로 반환
///   2. onCreateViewHolder(parent, viewType) → 그 뷰타입에 맞는 레이아웃/ViewHolder 생성
///
/// RecyclerView는 뷰타입별로 재사용 풀을 따로 관리함 (같은 타입끼리만 재사용)
/// → 스크롤 시 ADDED 셀은 ADDED끼리, COMPLETED는 COMPLETED끼리 재활용
///
/// ──── 게임 제목은 어떻게 가져오나 ────
/// ActivityLog는 gameId만 들고 있음 (제목 X).
/// 어댑터가 GameRepository.findById(gameId)로 게임을 찾아 제목을 ViewHolder에 넘김.
/// → ViewHolder는 Repository를 모르고 제목 문자열만 받음 (의존성 최소화)
/// </summary>
public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /// <summary>
    /// 뷰타입 상수
    /// getItemViewType이 반환하고 onCreateViewHolder가 비교에 사용
    /// 숫자 자체는 의미 없고 "서로 다른 값"이기만 하면 됨 → 의미 있는 이름의 상수로 관리
    /// </summary>
    private static final int VIEW_TYPE_ADDED = 0;
    private static final int VIEW_TYPE_COMPLETED = 1;
    private static final int VIEW_TYPE_REVIEWED = 2;
    private static final int VIEW_TYPE_PLAYED = 3;

    /// <summary>
    /// 표시할 활동 로그 목록 (ActivityLogRepository의 리스트를 그대로 참조)
    /// </summary>
    private final List<ActivityLog> logs;

    /// <summary>
    /// 게임 제목 조회용 저장소 (gameId → 게임 제목)
    /// </summary>
    private final GameRepository gameRepository;

    /// <summary>
    /// 어댑터 생성
    /// </summary>
    public TimelineAdapter(List<ActivityLog> logs, GameRepository gameRepository) {
        this.logs = logs;
        this.gameRepository = gameRepository;
    }

    // ========== 멀티 뷰타입 핵심 ==========

    /// <summary>
    /// 이 위치의 항목이 어떤 뷰타입인지 반환
    /// 로그의 활동 종류(enum)를 뷰타입 상수(int)로 변환
    /// → onCreateViewHolder가 이 값으로 어떤 레이아웃을 만들지 결정
    /// </summary>
    @Override
    public int getItemViewType(int position) {
        ActivityLogType type = logs.get(position).getType();
        switch (type) {
            case ADDED:
                return VIEW_TYPE_ADDED;
            case COMPLETED:
                return VIEW_TYPE_COMPLETED;
            case REVIEWED:
                return VIEW_TYPE_REVIEWED;
            case PLAYED:
                return VIEW_TYPE_PLAYED;
            default:
                // enum에 새 타입이 추가됐는데 여기 분기를 안 넣으면 여기로 옴 (방어)
                return VIEW_TYPE_ADDED;
        }
    }

    /// <summary>
    /// 뷰타입에 맞는 ViewHolder 생성
    /// getItemViewType이 돌려준 viewType으로 분기하여 알맞은 레이아웃을 inflate
    /// </summary>
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_COMPLETED:
                return new CompletedLogViewHolder(
                        ItemLogCompletedBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_REVIEWED:
                return new ReviewedLogViewHolder(
                        ItemLogReviewedBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_PLAYED:
                return new PlayedLogViewHolder(
                        ItemLogPlayedBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_ADDED:
            default:
                return new AddedLogViewHolder(
                        ItemLogAddedBinding.inflate(inflater, parent, false));
        }
    }

    /// <summary>
    /// 위치에 맞는 데이터를 ViewHolder에 바인딩
    /// holder는 onCreateViewHolder에서 만든 타입이므로 instanceof로 구분해 캐스팅
    /// 게임 제목은 gameId로 조회해서 각 ViewHolder의 bindLog에 넘김
    /// </summary>
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ActivityLog log = logs.get(position);
        String gameTitle = findGameTitle(log.getGameId());

        if (holder instanceof CompletedLogViewHolder) {
            ((CompletedLogViewHolder) holder).bindLog(log, gameTitle);
        } else if (holder instanceof ReviewedLogViewHolder) {
            ((ReviewedLogViewHolder) holder).bindLog(log, gameTitle);
        } else if (holder instanceof PlayedLogViewHolder) {
            ((PlayedLogViewHolder) holder).bindLog(log, gameTitle);
        } else if (holder instanceof AddedLogViewHolder) {
            ((AddedLogViewHolder) holder).bindLog(log, gameTitle);
        }
    }

    /// <summary>
    /// 전체 항목 개수 반환
    /// </summary>
    @Override
    public int getItemCount() {
        return logs.size();
    }

    // ========== 내부 헬퍼 ==========

    /// <summary>
    /// gameId로 게임 제목 찾기 (없으면 대체 문자열)
    /// Tester-Doer 대신 findById 결과를 null 체크 (Repository가 null 반환 가능 API라서)
    /// </summary>
    private String findGameTitle(int gameId) {
        Game game = gameRepository.findById(gameId);
        if (game == null) {
            return "(알 수 없는 게임)";
        }
        return game.getTitle();
    }
}
