package com.example.week8;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.data.GameRepository;
import com.example.week8.databinding.ActivityMainBinding;
import com.example.week8.model.Game;

import java.util.ArrayList;

/// <summary>
/// 메인 화면 (게임 라이브러리)
/// GameRepository에서 게임 목록을 읽어 ScrollView에 카드 형태로 표시
/// Unity로 비유하면 Prefab을 Instantiate하여 ScrollView Content에 붙이는 것
///
/// ──── Lifecycle 학습 (이후 단계에서 추가) ────
/// onResume: GameDetail/ReviewWrite 다녀온 뒤 변경된 데이터 반영
/// onRestart: 백키로 돌아왔을 때 관찰
/// onSaveInstanceState: ScrollView 스크롤 위치 보존
/// </summary>
public class MainActivity extends AppCompatActivity {

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityMainBinding binding;

    /// <summary>
    /// 게임 데이터 저장소
    /// Unity로 비유하면 GameData 목록을 들고 있는 매니저
    /// </summary>
    private GameRepository gameRepository;

    // ========== Lifecycle ==========

    /// <summary>
    /// 메인 화면 생성
    /// ViewBinding 연결 + GameRepository 초기화 + 게임 카드 리스트 생성
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // GameRepository 초기화
        gameRepository = new GameRepository();
    }

    /// <summary>
    /// Activity가 포그라운드로 돌아올 때마다 호출
    /// 다른 화면(GameDetail, ReviewWrite)에서 별점/리뷰를 변경하고 돌아오면
    /// 카드 리스트를 다시 그려서 변경사항을 반영
    /// Unity로 비유하면 OnEnable()에서 UI를 갱신하는 것과 동일
    ///
    /// ★ 호출 시점:
    /// - 앱 최초 실행 시 (onCreate → onStart → onResume)
    /// - 다른 Activity에서 백키로 돌아올 때
    /// - 홈키로 나갔다가 다시 들어올 때
    /// </summary>
    @Override
    protected void onResume() {
        super.onResume();

        // 카드 리스트 갱신 (변경된 별점/리뷰 반영)
        populateGameCards();
    }

    // ========== ActionBar 오버플로우 메뉴 (⋮) ==========

    /// <summary>
    /// ActionBar에 메뉴를 표시할 때 호출 (Activity 최초 생성 시 1번 호출됨)
    /// menu_main.xml에 정의된 항목들을 실제 메뉴로 부풀림(inflate)
    /// Unity로 비유하면 MenuPrefab을 MenuContainer에 Instantiate
    /// </summary>
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /// <summary>
    /// 메뉴 항목이 선택되면 호출
    /// item.getItemId()로 어떤 항목이 눌렸는지 판별
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            // "앱 정보" 메뉴 → AboutActivity로 이동
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ========== 카드 생성 ==========

    /// <summary>
    /// GameRepository의 전체 게임 목록을 읽어 카드를 동적으로 생성
    /// Unity로 비유하면 foreach(var data in list) Instantiate(prefab, content)
    /// </summary>
    private void populateGameCards() {
        // 기존 카드 전부 제거 (새로고침 시 중복 방지)
        binding.layoutGameList.removeAllViews();

        ArrayList<Game> games = gameRepository.getAllGames();

        for (Game game : games) {
            // 카드 View 생성 (item_game_card.xml을 inflate)
            // Unity로 비유하면 Instantiate(cardPrefab)
            View cardView = LayoutInflater.from(this)
                    .inflate(R.layout.item_game_card, binding.layoutGameList, false);

            // 카드 내부 View 참조
            TextView textTitle = cardView.findViewById(R.id.textViewTitle);
            TextView textGenrePlatform = cardView.findViewById(R.id.textViewGenrePlatform);
            TextView textRatingReview = cardView.findViewById(R.id.textViewRatingReview);
            ImageView imageCover = cardView.findViewById(R.id.imageViewCover);

            // 게임 정보 바인딩
            textTitle.setText(game.getTitle());

            // 장르(한국어) · 플랫폼(표시명)
            String genrePlatform = game.getGenre().getDisplayName()
                    + " · " + game.getPlatform().getDisplayName();
            textGenrePlatform.setText(genrePlatform);

            // 별점 + 한줄평 (아직 리뷰가 없으면 "리뷰 없음")
            boolean hasReview = game.getReview() != null && !game.getReview().isEmpty();
            String ratingReview;
            if (hasReview) {
                ratingReview = "★ " + game.getRating() + "  " + game.getReview();
            } else {
                ratingReview = "리뷰 없음";
            }
            textRatingReview.setText(ratingReview);

            // 표지 이미지 설정
            // coverAssetName으로 drawable 리소스 ID를 찾아서 설정
            // Unity로 비유하면 Resources.Load<Sprite>(coverAssetName)
            // 주의: getIdentifier()는 이름(문자열)으로 이미지를 찾아서 느림 (비권장 표시됨)
            // R.drawable.cover_zelda 처럼 직접 지정하면 빠르지만,
            // 게임마다 이미지 이름이 다르므로 문자열 검색이 불가피
            int coverResId = getResources().getIdentifier(
                    game.getCoverAssetName(), "drawable", getPackageName());
            if (coverResId != 0) {
                imageCover.setImageResource(coverResId);
            } else {
                // 이미지가 없으면 기본 아이콘 표시
                imageCover.setImageResource(R.mipmap.ic_launcher);
            }

            // 카드 클릭 시 GameDetailActivity로 이동
            // Unity로 비유하면 Button.onClick에 Scene 전환 + 데이터 전달 등록
            cardView.setOnClickListener(v -> {
                // 명시적 Intent 생성: "이 Activity에서 GameDetailActivity로 이동하겠다"
                Intent intent = new Intent(this, GameDetailActivity.class);

                // putExtra(키, 값)으로 Intent 봉투 안에 데이터를 넣음
                // 키: 받는 쪽과 동일한 문자열 상수 (키가 다르면 꺼낼 수 없음)
                // 값: Parcelable Game 객체 (내부적으로 writeToParcel 호출되어 직렬화됨)
                // Unity로 비유하면 PlayerPrefs.Set("키", 값) 후 Scene 전환하는 것
                intent.putExtra(GameDetailActivity.EXTRA_GAME, game);

                // Intent를 시스템에 전달하여 GameDetailActivity 실행
                startActivity(intent);
            });

            // 카드를 LinearLayout에 추가
            // Unity로 비유하면 card.transform.SetParent(content)
            binding.layoutGameList.addView(cardView);
        }
    }
}
