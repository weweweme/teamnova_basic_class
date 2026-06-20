package com.example.week8.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week8.R;
import com.example.week8.databinding.ItemGameCardBinding;
import com.example.week8.model.Game;

/// <summary>
/// 게임 카드 한 칸의 뷰 참조를 보관하는 ViewHolder
///
/// ──── 역할 ────
/// item_game_card.xml의 자식 뷰들(제목/이미지/별점 등)을 ViewBinding으로 캐시
/// 처음에 약 12개만 생성되고, 이후엔 같은 인스턴스가 다른 위치의 데이터를 받아 재사용됨
/// bind()가 호출되면 받은 Game 데이터를 각 뷰에 채우고 클릭 리스너를 등록
/// </summary>
public class GameCardViewHolder extends RecyclerView.ViewHolder {

    /// <summary>
    /// 카드 한 칸의 ViewBinding (자식 뷰 참조 모음)
    /// </summary>
    private final ItemGameCardBinding binding;

    /// <summary>
    /// ViewHolder 생성
    /// itemView로 binding.getRoot()을 부모 생성자에 넘겨야 RecyclerView가 위치 관리 가능
    /// </summary>
    public GameCardViewHolder(@NonNull ItemGameCardBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /// <summary>
    /// Game 데이터를 카드 뷰의 각 자리에 채움
    /// 제목, 장르·플랫폼, 별점·한줄평, 표지 이미지, 클릭/롱클릭 리스너, 드래그 핸들 설정
    /// </summary>
    public void bindGameData(Game game,
                         OnGameClickListener clickListener,
                         OnGameLongClickListener longClickListener,
                         ItemTouchHelper itemTouchHelper) {
        Context context = binding.getRoot().getContext();

        // 제목
        binding.textViewTitle.setText(game.getTitle());

        // 장르 · 플랫폼
        String genrePlatform = game.getGenre().getDisplayName()
                + " · " + game.getPlatform().getDisplayName();
        binding.textViewGenrePlatform.setText(genrePlatform);

        // 별점 + 한줄평 (리뷰가 없으면 "리뷰 없음" 표시)
        boolean hasReview = game.getReview() != null && !game.getReview().isEmpty();
        String ratingReview;
        if (hasReview) {
            ratingReview = "★ " + game.getRating() + "  " + game.getReview();
        } else {
            ratingReview = "리뷰 없음";
        }
        binding.textViewRatingReview.setText(ratingReview);

        // 표지 이미지 (이름 문자열로 drawable 리소스 ID 조회)
        // 게임마다 이미지 이름이 다르므로 getIdentifier 사용이 불가피
        // 리소스가 없으면 기본 아이콘으로 대체
        int coverResId = context.getResources().getIdentifier(
                game.getCoverAssetName(), "drawable", context.getPackageName());
        if (coverResId != 0) {
            binding.imageViewCover.setImageResource(coverResId);
        } else {
            binding.imageViewCover.setImageResource(R.mipmap.ic_launcher);
        }

        // 카드 클릭 리스너 (Activity 측 콜백 호출)
        binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGameClick(game);
            }
        });

        // 카드 길게 누르기 리스너
        // return true: "이벤트를 내가 처리했음" → 짧은 클릭 이벤트로 전파되지 않음
        // return false: 짧은 클릭으로 이어짐 (long click과 click이 동시에 발생)
        binding.getRoot().setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onGameLongClick(game);
                return true;
            }
            return false;
        });

        // 드래그 핸들 터치 시 드래그 시작
        setupDragHandle(itemTouchHelper);
    }

    /// <summary>
    /// 드래그 핸들 ImageView에 터치 리스너 등록
    /// ACTION_DOWN 시 itemTouchHelper.startDrag(this)를 명시 호출하여 드래그 시작
    /// (콜백의 isLongPressDragEnabled=false이므로 이 경로 외에는 드래그 시작 안 됨)
    ///
    /// ──── @SuppressLint("ClickableViewAccessibility") ────
    /// setOnTouchListener는 ClickableViewAccessibility lint 경고를 일으킴.
    ///
    /// ──── 1) 안드로이드의 기본 클릭 처리 흐름 (OnTouchListener 없을 때) ────
    /// 1. 사용자가 화면 터치 (손가락 닿음 ACTION_DOWN, 뗌 ACTION_UP)
    /// 2. 시스템이 View.dispatchTouchEvent() 호출 → 이벤트가 뷰로 전달됨
    /// 3. View.onTouchEvent() 가 이벤트를 받아 "이건 클릭이다"라고 판단 (DOWN 후 UP)
    /// 4. View.performClick() 가 자동으로 호출됨 ← 클릭의 "공식 발화점"
    /// 5. performClick() 내부에서 등록된 OnClickListener.onClick() 실행
    ///
    /// 여기서 핵심은 performClick() 이 클릭 동작의 표준 진입점이라는 것.
    /// 그런데 왜 이 "표준 진입점"이 중요한가? → 접근성 도구(TalkBack) 때문.
    ///
    /// ─ TalkBack이란? ─
    /// 안드로이드의 접근성 서비스 중 하나. 시각이 불편한 사용자가 화면을 보지 않고도
    /// 앱을 쓸 수 있게 도와줌:
    ///   - 화면에 보이는 텍스트/버튼 이름을 음성으로 읽어줌
    ///   - 사용자가 음성/제스처로 "이 버튼 활성화"라고 명령하면 시스템이 대신 클릭
    ///
    /// ─ TalkBack은 클릭을 어떻게 발화시키나? ─
    /// 사용자가 화면을 직접 터치하지 않으므로 우회 경로를 씀:
    ///   사용자 명령 → TalkBack → 시스템에 "이 뷰의 클릭 동작 실행" 요청
    ///   → 시스템이 그 뷰의 performClick() 을 직접 호출 (손가락 터치 없이!)
    ///   → performClick() 내부에서 OnClickListener.onClick() 실행
    ///   → 결과: 일반 사용자가 직접 탭한 것과 동일한 동작 수행
    ///
    /// 즉 performClick() 은 "일반 터치 사용자와 TalkBack 사용자가 공유하는 공통 진입점".
    /// 이 공통 진입점이 끊기거나 우회되면 → TalkBack 사용자만 기능을 못 쓰게 됨.
    /// ClickableViewAccessibility lint 규칙은 정확히 이 끊김을 감시하는 검사기.
    ///
    /// ──── 2) OnTouchListener를 등록하면 흐름이 바뀐다 ────
    /// 1. 사용자가 화면 터치
    /// 2. View.dispatchTouchEvent() 호출
    /// 3. 등록된 OnTouchListener.onTouch() 가 가장 먼저 호출됨 ← 우리가 "가로챈" 지점
    /// 4. onTouch 반환값에 따라 흐름이 갈림:
    ///    return true  → "내가 다 처리했으니 더 이상 흐르지 마"
    ///                  → onTouchEvent로 안 넘어감 → performClick() 자동 호출 X
    ///    return false → "참고만 했어, 계속 흐르세요"
    ///                  → onTouchEvent → performClick() 정상 발화
    ///
    /// 문제는 return true 경로:
    /// 일반 사용자는 OnTouchListener 안에서 처리한 동작을 경험하지만,
    /// 같은 뷰를 TalkBack으로 "클릭" 활성화하면 시스템은 여전히 performClick() 만 부름.
    /// → performClick() 은 OnClickListener 만 실행함. OnTouchListener 는 안 부름.
    /// → "OnTouchListener에서만 처리한 동작"은 접근성 사용자에게 영영 일어나지 않음.
    ///
    /// 즉 lint의 진짜 우려:
    ///   "setOnTouchListener 안에서 클릭 같은 핵심 동작을 처리했는데,
    ///    거기서 명시적으로 v.performClick() 도 안 부르면,
    ///    TalkBack 사용자는 그 기능을 영영 사용하지 못한다"
    ///
    /// ──── 3) 드래그 핸들은 왜 이 lint 의도와 맞지 않나? ────
    /// 우리 OnTouchListener는 "클릭" 같은 동작을 처리하는 게 아님.
    /// "누른 채 끌기 시작"이라는 제스처의 시작 신호만 잡아서 ItemTouchHelper에 넘김.
    ///
    /// 클릭과 드래그는 본질적으로 다름:
    ///   클릭   = 닿았다가 떼는 1회성 동작 (performClick 으로 표현 가능)
    ///   드래그 = 닿은 채로 손가락을 계속 움직이는 연속 동작 (performClick 으로 표현 불가)
    ///
    /// TalkBack 사용자에게 performClick 을 호출해줘도 "드래그 시작"이라는 동작을
    /// 재현할 방법이 없음. 어차피 화면 위에서 손가락을 이동시켜야 하므로.
    /// → lint의 우려는 사실 맞지만, 해결할 수 없는 본질적 한계.
    /// → 우회: 정렬은 부가 기능. 핵심 기능(상세/삭제/공유)은 표준 클릭/길게누름 경로로 제공.
    ///
    /// ──── 안드로이드 공식 javadoc의 권장 예제 참고 ────
    /// ItemTouchHelper.startDrag(ViewHolder) 의 공식 javadoc에 다음 예제가 실려 있음
    /// (드래그 핸들 같은 자식 뷰로 드래그를 시작하는 방법으로 직접 권장):
    ///
    ///   viewHolder.dragButton.setOnTouchListener(new View.OnTouchListener() {
    ///       public boolean onTouch(View v, MotionEvent event) {
    ///           if (MotionEvent.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
    ///               mItemTouchHelper.startDrag(viewHolder);
    ///           }
    ///           return false;
    ///       }
    ///   });
    ///
    /// 공식 예제 자체에는 @SuppressLint 표기가 없음. 단, lint는 빌드 시점 정적 분석이라
    /// 어떤 코드든 setOnTouchListener를 보면 동일하게 경고를 발생시킴 (예제든 실무든).
    /// 즉 코드 패턴은 공식 권장 그대로이고, @SuppressLint는 그 패턴을 IDE에서 깨끗하게
    /// 보여주기 위한 명시적 lint 처리일 뿐.
    ///
    /// 안드로이드 공식 접근성 가이드는 이렇게 말함:
    ///   "앱의 기본 동작은 터치 제스처에 의존해서는 안 된다.
    ///    제스처가 모든 사용자에게 항상 가능하지는 않으므로."
    ///   → 핵심은 "대체 경로 제공"
    ///
    /// 우리 앱의 대체 경로 매핑:
    ///   상세 보기  → 카드 짧게 탭 (TalkBack 호환 기본 클릭)
    ///   삭제      → 길게 누름 → BottomSheet (표준 컨텍스트 메뉴 패턴)
    ///   공유      → 길게 누름 → BottomSheet
    ///   정렬      → 드래그 핸들 — 부가 기능, 없어도 사용에 지장 없음
    ///
    /// 따라서 SuppressLint는 lint를 무시하는 게 아니라
    /// "이 케이스는 가이드 원칙에 부합하므로 의도적으로 통과시킨다"는 명시적 선언.
    ///
    /// 참고 (실제 코드/문서를 확인할 수 있는 출처):
    ///   - Lint 규칙(ClickableViewAccessibility) 정의:
    ///     https://googlesamples.github.io/android-custom-lint-rules/checks/ClickableViewAccessibility.md.html
    ///   - Android 접근성 가이드(대체 경로 원칙):
    ///     https://developer.android.com/guide/topics/ui/accessibility
    ///   - ItemTouchHelper API 레퍼런스 (startDrag / isLongPressDragEnabled 공식 정의):
    ///     https://developer.android.com/reference/androidx/recyclerview/widget/ItemTouchHelper
    ///   - AOSP 소스 (startDrag javadoc과 수동 트리거 패턴 근거 — 파일 안에서 startDrag 메서드와 isLongPressDragEnabled() 부분을 보면 됨):
    ///     https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:recyclerview/recyclerview/src/main/java/androidx/recyclerview/widget/ItemTouchHelper.java
    /// </summary>
    @SuppressLint("ClickableViewAccessibility")
    private void setupDragHandle(ItemTouchHelper itemTouchHelper) {
        // itemTouchHelper가 없으면 드래그 정렬을 안 쓰는 화면(예: 홈 미리보기)
        // → 드래그 핸들 아이콘을 숨겨서 "끌 수 있는 것처럼" 보이지 않게 함
        if (itemTouchHelper == null) {
            binding.imageViewDragHandle.setVisibility(View.GONE);
            return;
        }

        // 드래그 정렬을 쓰는 화면(DiaryActivity)에선 핸들 표시 + 터치 시 드래그 시작
        binding.imageViewDragHandle.setVisibility(View.VISIBLE);
        binding.imageViewDragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(this);
            }

            // false: 이벤트 미소비 — 뷰의 ripple 같은 후속 처리에 영향 없음
            return false;
        });
    }
}
