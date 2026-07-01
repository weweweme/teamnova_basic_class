package com.example.week10.community;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week10.App;
import com.example.week10.R;
import com.example.week10.account.UserPrefs;
import com.example.week10.data.CommunityRepository;
import com.example.week10.databinding.ActivityUserProfileBinding;
import com.example.week10.detail.GameDetailActivity;
import com.example.week10.model.AccountProfile;
import com.example.week10.model.Game;
import com.example.week10.model.ReviewFeedItem;

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

    /// <summary>이 유저의 리뷰 개수 (통계 줄 갱신용으로 보관)</summary>
    private int reviewCount;

    /// <summary>이 유저의 팔로잉 수 (통계 줄 갱신용으로 보관)</summary>
    private int followingCount;

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

        // ── 프로필 헤더 ──
        AccountProfile profile = community.getProfile(accountId);
        reviewCount = profile.getReviewCount();
        followingCount = community.getFollowingCount(accountId);

        binding.textViewProfileAvatar.setText(initialOf(profile.getNickname()));
        binding.textViewProfileAvatar.setBackgroundTintList(
                ColorStateList.valueOf(profile.getAvatarColor()));
        binding.textViewProfileNickname.setText(profile.getNickname());
        binding.textViewProfileBio.setText(profile.getBio());
        updateStats(community.getFollowerCount(accountId));

        // ── 팔로우 버튼 (내 프로필이면 숨김) ──
        boolean isMe = accountId.equals(currentId);
        if (isMe || myPrefs == null) {
            binding.buttonProfileFollow.setVisibility(View.GONE);
        } else {
            binding.buttonProfileFollow.setVisibility(View.VISIBLE);
            updateFollowButton(myPrefs.isFollowing(accountId));
            binding.buttonProfileFollow.setOnClickListener(v -> onToggleFollow());
        }

        // ── 그 유저가 작성한 리뷰 목록 ──
        List<ReviewFeedItem> reviews = community.getUserReviews(accountId);
        boolean noReviews = reviews.isEmpty();
        binding.textViewProfileNoReviews.setVisibility(noReviews ? View.VISIBLE : View.GONE);
        binding.recyclerProfileReviews.setVisibility(noReviews ? View.GONE : View.VISIBLE);
        binding.recyclerProfileReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProfileReviews.setAdapter(new ReviewFeedAdapter(reviews, this::openGame));
    }

    // ========== 팔로우 ==========

    /// <summary>
    /// 팔로우 버튼 클릭 → 토글하고 버튼·팔로워 수를 갱신
    /// </summary>
    private void onToggleFollow() {
        boolean newFollow = !myPrefs.isFollowing(accountId);
        myPrefs.setFollowing(accountId, newFollow);
        updateFollowButton(newFollow);
        // 내가 팔로우/언팔로우하면 이 유저의 팔로워 수가 즉시 바뀜 → 통계 줄 다시 계산
        updateStats(community.getFollowerCount(accountId));
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
    /// 통계 줄 갱신 (리뷰·팔로워·팔로잉 수) — 팔로워 수만 바뀌므로 인자로 받음
    /// </summary>
    private void updateStats(int followerCount) {
        binding.textViewProfileStats.setText(
                getString(R.string.user_profile_stats, reviewCount, followerCount, followingCount));
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
