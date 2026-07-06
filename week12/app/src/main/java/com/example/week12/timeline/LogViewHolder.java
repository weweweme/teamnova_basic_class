package com.example.week12.timeline;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.model.ActivityLog;

/// <summary>
/// 타임라인 로그 뷰홀더들의 공통 부모 (추상 클래스)
///
/// ──── 왜 이 클래스가 있나 ────
/// 활동 종류별 뷰홀더(Added/Completed/Reviewed/Played)는 셀 모양만 다를 뿐
/// "로그 데이터를 받아 화면에 채운다"는 동작은 똑같음.
/// 이 공통 동작을 bindLog로 약속해두면, 어댑터는 holder가 정확히 어떤 종류인지
/// 몰라도 holder.bindLog(...) 한 줄로 채울 수 있음 (instanceof 분기 불필요).
///
/// Unity 비유: 추상 클래스 + abstract 메서드.
///   abstract class LogViewHolder { public abstract void BindLog(...); }
///   각 자식이 override 해서 자기 방식으로 채움 → 다형성
/// </summary>
public abstract class LogViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 자식 뷰홀더의 최상위 뷰(itemView)를 부모에 전달
    /// </summary>
    public LogViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /// <summary>
    /// 로그 데이터를 셀에 채움 (자식이 각자 구현)
    /// gameTitle은 어댑터가 gameId로 GameRepository를 조회해 미리 넘겨준 값
    /// (뷰홀더가 Repository에 직접 의존하지 않도록 제목을 인자로 받음)
    /// </summary>
    public abstract void bindLog(ActivityLog log, String gameTitle);
}
