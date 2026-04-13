package com.example.week8;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.databinding.ActivityGameDetailBinding;
import com.example.week8.model.Game;

/// <summary>
/// 게임 상세 화면
/// MainActivity에서 Parcelable로 전달받은 Game 데이터를 표시
/// 리뷰 작성 / 공유 / 스토어 열기 / 스크린샷 버튼 제공
/// Unity로 비유하면 아이템 상세 Panel에 데이터를 바인딩하는 것
///
/// ──── Intent 학습 (이후 단계에서 추가) ────
/// 수신: MainActivity에서 명시적 Intent + Parcelable extras로 Game 전달받음
/// 송신: ACTION_SEND chooser, ACTION_VIEW 브라우저
/// 결과 반환: ReviewWrite, Screenshot과 forResult 통신
/// </summary>
public class GameDetailActivity extends AppCompatActivity {

    /// <summary>
    /// Intent extras에서 Game 객체를 꺼낼 때 사용하는 키
    /// 보내는 쪽(MainActivity)과 받는 쪽(여기)이 같은 키를 써야 함
    /// </summary>
    public static final String EXTRA_GAME = "extra_game";

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityGameDetailBinding binding;

    /// <summary>
    /// Intent로 전달받은 게임 데이터
    /// </summary>
    private Game game;

    // ========== Lifecycle ==========

    /// <summary>
    /// 상세 화면 생성
    /// Intent extras에서 Game 객체를 꺼내고 화면에 바인딩
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityGameDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // MainActivity에서 intent.putExtra("extra_game", game)으로 보낸 Game 객체를 꺼냄
        // 흐름: MainActivity가 Game을 Parcel에 포장(writeToParcel)해서 Intent에 실음
        //       → 이 Activity가 Intent에서 Parcel을 꺼내고 CREATOR로 Game 복원
        //       → 결과적으로 MainActivity의 Game과 동일한 데이터를 가진 새 Game 객체가 됨
        game = getIntent().getParcelableExtra(EXTRA_GAME, Game.class);

        // 게임 데이터가 없으면 화면을 닫음 (비정상 진입 방지)
        if (game == null) {
            finish();
            return;
        }

        // 게임 정보를 화면에 표시
        bindGameData();
    }

    // ========== 데이터 바인딩 ==========

    /// <summary>
    /// Game 객체의 정보를 각 View에 표시
    /// Unity로 비유하면 UI 텍스트에 데이터를 세팅하는 것
    /// </summary>
    private void bindGameData() {
        // 제목
        binding.textViewTitle.setText(game.getTitle());

        // 장르 · 플랫폼
        String genrePlatform = game.getGenre().getDisplayName()
                + " · " + game.getPlatform().getDisplayName();
        binding.textViewGenrePlatform.setText(genrePlatform);

        // 별점 + 한줄평
        boolean hasReview = game.getReview() != null && !game.getReview().isEmpty();
        String ratingReview;
        if (hasReview) {
            ratingReview = "★ " + game.getRating() + "  " + game.getReview();
        } else {
            ratingReview = "리뷰 없음";
        }
        binding.textViewRatingReview.setText(ratingReview);

        // 표지 이미지
        // 주의: getIdentifier()는 이름(문자열)으로 이미지를 찾아서 느림 (비권장 표시됨)
        // R.drawable.cover_zelda 처럼 직접 지정하면 빠르지만,
        // 게임마다 이미지 이름이 다르므로 문자열 검색이 불가피
        int coverResId = getResources().getIdentifier(
                game.getCoverAssetName(), "drawable", getPackageName());
        if (coverResId != 0) {
            binding.imageViewCover.setImageResource(coverResId);
        } else {
            binding.imageViewCover.setImageResource(R.mipmap.ic_launcher);
        }
    }
}
