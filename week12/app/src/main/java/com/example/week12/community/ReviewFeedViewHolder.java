package com.example.week12.community;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week12.R;
import com.example.week12.databinding.ItemReviewFeedBinding;
import com.example.week12.model.ReviewFeedItem;
import com.example.week12.timeline.TimeAgoFormatter;

/// <summary>
/// 리뷰 피드 한 줄의 뷰 참조를 보관하는 ViewHolder
/// 피드 항목(작성자·게임·별점·한줄평·시각·좋아요)을 채우고, 누르면 그 게임 상세로 보낸다
/// </summary>
public class ReviewFeedViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 한 줄 레이아웃(item_review_feed.xml)의 ViewBinding
    /// </summary>
    private final ItemReviewFeedBinding binding;

    /// <summary>
    /// 하트 '팝' 애니메이션을 프레임 단위로 굴리는 Handler
    /// (Unity 비유: 매 프레임 scale을 조금씩 바꾸는 코루틴을 Handler로 흉내)
    /// </summary>
    private final Handler popHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// ViewHolder 생성 — itemView로 binding.getRoot()을 부모에 넘겨야 위치 관리가 됨
    /// </summary>
    public ReviewFeedViewHolder(@NonNull ItemReviewFeedBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// 피드 항목 한 개를 줄에 채운다
    /// </summary>
    public void bind(ReviewFeedItem item,
                     OnReviewFeedClickListener clickListener,
                     OnReviewLikeToggleListener likeListener) {
        // 재활용된 뷰라면 이전 항목의 팝 애니메이션이 돌고 있을 수 있으니 멈추고 크기 복구
        popHandler.removeCallbacksAndMessages(null);
        binding.imageViewFeedLike.setScaleX(1f);
        binding.imageViewFeedLike.setScaleY(1f);

        // 재활용 뷰 초기화 — 이전 등장 애니메이션 잔상(투명/이동) 제거
        binding.getRoot().animate().cancel();
        binding.getRoot().setAlpha(1f);
        binding.getRoot().setTranslationY(0f);

        // 작성자 아바타: 색 + 별명 첫 글자
        binding.textViewFeedAvatar.setText(initialOf(item.getNickname()));
        binding.textViewFeedAvatar.setBackgroundTintList(
                ColorStateList.valueOf(item.getAvatarColor()));

        // 닉네임 · 게임제목 · 별점
        binding.textViewFeedNickname.setText(item.getNickname());
        binding.textViewFeedGame.setText("· " + item.getGameTitle());
        binding.textViewFeedRating.setText("★ " + item.getRating());

        // 한줄평
        binding.textViewFeedReview.setText(item.getReview());

        // N분 전 (타임라인과 같은 포맷터 재사용)
        // 시각 기록이 없는(0) 옛 리뷰는 엉뚱한 "수십 년 전"이 나오므로 시간 표시를 숨김
        if (item.getTimestamp() > 0L) {
            binding.textViewFeedTime.setText(TimeAgoFormatter.format(item.getTimestamp()));
            binding.textViewFeedTime.setVisibility(View.VISIBLE);
        } else {
            binding.textViewFeedTime.setVisibility(View.GONE);
        }

        // 좋아요(하트 아이콘 + 개수) 현재 상태 표시
        bindLike(item);

        // 하트 탭 → 즉시 토글(화면) + 팝 애니메이션 + 저장(콜백)
        binding.imageViewFeedLike.setOnClickListener(v -> {
            item.toggleLikedByMe();     // 항목 값(내 누름/개수)을 뒤집음
            bindLike(item);             // 바뀐 값으로 하트/숫자 다시 그림
            popHeart(binding.imageViewFeedLike);
            if (likeListener != null) {
                likeListener.onReviewLikeToggle(item);   // 실제 저장은 화면에 위임
            }
        });

        // 항목(카드) 클릭 → 그 게임 상세로 (하트는 자식 뷰라 하트 탭은 여기로 안 옴)
        binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReviewFeedClick(item.getGameId());
            }
        });

        // 방금 실시간으로 끼워넣은 항목이면 "위에서 슬라이드+페이드" 등장 애니메이션을 한 번 재생
        if (item.isJustAdded()) {
            playEntrance(binding.getRoot());
            item.setJustAdded(false);   // 한 번만 재생 (재활용/재바인딩 때 반복 방지)
        }
    }

    /// <summary>
    /// 새로 등장하는 카드를 위쪽에서 스르륵 내려오며(translationY) 서서히 나타나게(alpha) 한다
    /// </summary>
    private void playEntrance(View card) {
        float offsetPx = 48f * card.getResources().getDisplayMetrics().density;   // 위로 48dp
        card.setAlpha(0f);
        card.setTranslationY(-offsetPx);
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(380)
                .start();
    }

    /// <summary>
    /// 좋아요 상태를 하트 아이콘(꽉/빈)과 숫자로 표시 (0이면 숫자 숨김)
    /// </summary>
    private void bindLike(ReviewFeedItem item) {
        binding.imageViewFeedLike.setImageResource(
                item.isLikedByMe() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        int count = item.getLikeCount();
        binding.textViewFeedLikeCount.setText(count > 0 ? String.valueOf(count) : "");
    }

    /// <summary>
    /// 하트를 살짝 튀게 하는 '팝' 애니메이션 (Handler로 매 프레임 scale 갱신)
    /// sin(πt) 곡선: t가 0→1 갈 동안 크기가 1.0 → 최대 → 1.0 으로 튀었다 돌아온다
    /// </summary>
    private void popHeart(View heart) {
        final long DURATION_MS = 260L;   // 전체 애니메이션 길이
        final long FRAME_MS = 16L;       // 프레임 간격(약 60fps)
        final float PEAK_SCALE = 1.35f;  // 최대로 커지는 배율

        popHandler.removeCallbacksAndMessages(null);
        final long start = SystemClock.uptimeMillis();
        popHandler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.min(1f, elapsed / (float) DURATION_MS);   // 0~1 진행도
                // sin(πt): 0→1→0 → 크기가 1.0에서 PEAK로 튀었다가 다시 1.0
                float scale = 1f + (PEAK_SCALE - 1f) * (float) Math.sin(t * Math.PI);
                heart.setScaleX(scale);
                heart.setScaleY(scale);
                if (t < 1f) {
                    popHandler.postDelayed(this, FRAME_MS);
                } else {
                    heart.setScaleX(1f);
                    heart.setScaleY(1f);
                }
            }
        });
    }

    /// <summary>
    /// 별명 첫 글자(대문자)를 반환, 비어 있으면 물음표
    /// </summary>
    private String initialOf(String nickname) {
        if (nickname.isEmpty()) {
            return "?";
        }
        return String.valueOf(Character.toUpperCase(nickname.charAt(0)));
    }
}
