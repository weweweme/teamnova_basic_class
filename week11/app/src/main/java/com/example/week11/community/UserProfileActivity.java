package com.example.week11.community;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.account.UserPrefs;
import com.example.week11.data.CommunityRepository;
import com.example.week11.databinding.ActivityUserProfileBinding;
import com.example.week11.detail.GameDetailActivity;
import com.example.week11.model.AccountProfile;
import com.example.week11.model.Game;
import com.example.week11.model.ReviewFeedItem;

import java.util.List;

/// <summary>
/// 유저 프로필 화면 (커뮤니티)
///
/// ──── 무엇을 하나 ────
/// 다른 유저(또는 나)의 프로필을 보여준다: 아바타·별명·소개·통계(리뷰/팔로워/팔로잉)
/// + 팔로우 버튼 + 그 유저가 작성한 리뷰 목록.
/// 리뷰/랭킹/팔로우목록 등에서 유저를 누르면 이 화면으로 온다.
/// </summary>
public class UserProfileActivity extends AppCompatActivity {

    /// <summary>입력: 볼 유저의 계정 id</summary>
    public static final String EXTRA_ACCOUNT_ID = "extra_account_id";

    /// <summary>
    /// 어디서든(리뷰 카드·랭킹·팔로우 목록) 이 유저 프로필 화면을 여는 진입 헬퍼
    /// </summary>
    public static void start(android.content.Context context, String accountId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(EXTRA_ACCOUNT_ID, accountId);
        context.startActivity(intent);
    }

    /// <summary>
    /// activity_user_profile.xml의 View 묶음
    /// </summary>
    private ActivityUserProfileBinding binding;

    /// <summary>보고 있는 유저의 계정 id</summary>
    private String accountId;

    /// <summary>내 저장소 (팔로우 토글용)</summary>
    private UserPrefs myPrefs;

    /// <summary>커뮤니티 저장소 (팔로워 수 등 집계)</summary>
    private CommunityRepository community;

    /// <summary>
    /// 프로필 집계 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// 계정마다 파일을 읽는 무거운 집계는 서브 스레드에서, 화면 그리기만 메인에 넘긴다
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성 — 유저 프로필과 리뷰 목록을 채운다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        accountId = getIntent().getStringExtra(EXTRA_ACCOUNT_ID);
        if (accountId == null) {
            finish();
            return;
        }

        App app = (App) getApplication();
        community = app.getCommunityRepository();
        myPrefs = app.getUserPrefs();
        String currentId = app.getAccountManager().getCurrentAccountId();
        boolean isMe = accountId.equals(currentId);

        // 로딩 스피너 표시, 내용(헤더+리뷰) 숨김 (아직 집계 전)
        binding.progressProfile.setVisibility(View.VISIBLE);
        binding.layoutProfileContent.setVisibility(View.GONE);

        // 프로필·팔로워/팔로잉 수·작성 리뷰는 계정마다 파일을 읽는 디스크 작업 → 서브 스레드에서
        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 스피너가 보이게 잠깐 지연 (확인 후 삭제)
            // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                // 발생하지 않음 (컴파일러 요구사항)
            }

            // 무거운 일: 프로필 + 팔로잉/팔로워 수 + 작성 리뷰 모으기 (디스크 읽기)
            // 서버에서 데이터를 가져오는 것과 같음
            AccountProfile profile = community.getProfile(accountId);
            int followingCount = community.getFollowingCount(accountId);
            int followerCount = community.getFollowerCount(accountId);
            List<ReviewFeedItem> reviews = community.getUserReviews(accountId);
            boolean following = myPrefs != null && myPrefs.isFollowing(accountId);

            // 화면 그리기는 메인 줄로 (뷰 조작은 메인에서만)
            mainHandler.post(() -> {
                renderProfile(profile, followingCount, followerCount, reviews, isMe, following);
                binding.progressProfile.setVisibility(View.GONE);
                binding.layoutProfileContent.setVisibility(View.VISIBLE);
            });
        }).start();                                     // 서브 스레드 실행
    }

    /// <summary>
    /// 화면이 사라질 때, 아직 메인 큐에 남아있는 프로필 반영 작업을 취소 (누수 방지)
    /// </summary>
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    /// <summary>
    /// 미리 집계한 값으로 프로필 헤더 + 리뷰 목록을 그린다 (모두 메인 스레드에서)
    /// </summary>
    private void renderProfile(AccountProfile profile, int followingCount, int followerCount,
                               List<ReviewFeedItem> reviews, boolean isMe, boolean following) {
        // ── 프로필 헤더 ──
        binding.textViewProfileAvatar.setText(initialOf(profile.getNickname()));
        binding.textViewProfileAvatar.setBackgroundTintList(
                ColorStateList.valueOf(profile.getAvatarColor()));
        binding.textViewProfileNickname.setText(profile.getNickname());
        binding.textViewProfileBio.setText(profile.getBio());

        // 통계: 리뷰·팔로잉은 고정, 팔로워는 내가 팔로우/언팔로우하면 바뀌므로 헬퍼로 갱신
        binding.textViewProfileReviews.setText(
                getString(R.string.ranking_review_count, profile.getReviewCount()));
        binding.textViewProfileFollowing.setText(
                getString(R.string.profile_following_count, followingCount));
        updateFollowers(followerCount);

        // 팔로워/팔로잉 탭 → 이 유저의 목록
        binding.textViewProfileFollowers.setOnClickListener(v ->
                openFollowList(FollowListActivity.MODE_FOLLOWERS));
        binding.textViewProfileFollowing.setOnClickListener(v ->
                openFollowList(FollowListActivity.MODE_FOLLOWING));

        // ── 팔로우 버튼 (내 프로필이면 숨김) ──
        if (isMe || myPrefs == null) {
            binding.buttonProfileFollow.setVisibility(View.GONE);
        } else {
            binding.buttonProfileFollow.setVisibility(View.VISIBLE);
            updateFollowButton(following);
            binding.buttonProfileFollow.setOnClickListener(v -> onToggleFollow());
        }

        // ── 그 유저가 작성한 리뷰 목록 ──
        boolean noReviews = reviews.isEmpty();
        binding.textViewProfileNoReviews.setVisibility(noReviews ? View.VISIBLE : View.GONE);
        binding.recyclerProfileReviews.setVisibility(noReviews ? View.GONE : View.VISIBLE);
        binding.recyclerProfileReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProfileReviews.setAdapter(
                new ReviewFeedAdapter(reviews, this::openGame, this::onLikeToggle));
    }

    /// <summary>
    /// 하트 토글 결과를 내(현재 로그인) 계정 파일에 저장
    /// (ViewHolder가 항목을 이미 토글했으므로 item.isLikedByMe()가 새 상태)
    /// </summary>
    private void onLikeToggle(ReviewFeedItem item) {
        myPrefs.setLiked(item.getGameId(), item.getReviewerId(), item.isLikedByMe());
    }

    // ========== 팔로우 ==========

    /// <summary>
    /// 팔로우 버튼 클릭 → 토글하고 버튼·팔로워 수를 갱신
    /// </summary>
    private void onToggleFollow() {
        boolean newFollow = !myPrefs.isFollowing(accountId);
        myPrefs.setFollowing(accountId, newFollow);
        updateFollowButton(newFollow);
        // 내가 팔로우/언팔로우하면 이 유저의 팔로워 수가 즉시 바뀜 → 팔로워 표시 갱신
        updateFollowers(community.getFollowerCount(accountId));
    }

    /// <summary>
    /// 팔로우 버튼 상태 갱신 (팔로우 중이면 "팔로잉"·연하게)
    /// </summary>
    private void updateFollowButton(boolean following) {
        binding.buttonProfileFollow.setText(
                following ? R.string.ranking_following : R.string.ranking_follow);
        binding.buttonProfileFollow.setAlpha(following ? 0.5f : 1.0f);
    }

    /// <summary>
    /// 팔로워 수 표시 갱신 (내가 이 유저를 팔로우/언팔로우하면 바뀜)
    /// </summary>
    private void updateFollowers(int followerCount) {
        binding.textViewProfileFollowers.setText(
                getString(R.string.profile_follower_count, followerCount));
    }

    /// <summary>
    /// 이 유저의 팔로잉/팔로워 목록 화면을 연다 (본 유저 기준)
    /// </summary>
    private void openFollowList(String mode) {
        Intent intent = new Intent(this, FollowListActivity.class);
        intent.putExtra(FollowListActivity.EXTRA_MODE, mode);
        intent.putExtra(FollowListActivity.EXTRA_ACCOUNT_ID, accountId);
        startActivity(intent);
    }

    // ========== 리뷰 클릭 ==========

    /// <summary>
    /// 리뷰 항목 클릭 → 그 게임 상세로 이동
    /// </summary>
    private void openGame(int gameId) {
        Game game = ((App) getApplication()).getGameRepository().findById(gameId);
        if (game == null) {
            return;
        }
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME, game);
        startActivity(intent);
    }

    // ========== 헬퍼 ==========

    /// <summary>별명 첫 글자(대문자), 비어 있으면 물음표</summary>
    private String initialOf(String nickname) {
        if (nickname.isEmpty()) {
            return "?";
        }
        return String.valueOf(Character.toUpperCase(nickname.charAt(0)));
    }

    /// <summary>
    /// ActionBar ← 버튼 → 화면 닫기
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
