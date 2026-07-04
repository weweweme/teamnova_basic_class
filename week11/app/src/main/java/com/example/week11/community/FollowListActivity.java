package com.example.week11.community;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.databinding.ActivityFollowListBinding;
import com.example.week11.model.AccountProfile;

import java.util.List;

/// <summary>
/// 팔로우 목록 화면 (팔로잉 또는 팔로워)
///
/// ──── 무엇을 하나 ────
/// 지금 로그인한 계정의 "팔로잉 목록"(내가 팔로우한 사람들) 또는
/// "팔로워 목록"(나를 팔로우한 사람들)을 보여준다. mode 값으로 둘 중 무엇을 볼지 정한다.
/// 각 유저에는 팔로우/팔로잉 버튼이 있어 여기서 바로 팔로우/언팔로우 가능.
/// </summary>
public class FollowListActivity extends AppCompatActivity {

    /// <summary>입력: 어떤 목록을 볼지 ("following" 또는 "followers")</summary>
    public static final String EXTRA_MODE = "extra_mode";

    /// <summary>입력: 누구의 목록을 볼지 (계정 id). 없으면 지금 로그인한 나</summary>
    public static final String EXTRA_ACCOUNT_ID = "extra_account_id";

    /// <summary>mode 값: 팔로잉(내가 팔로우한 사람들)</summary>
    public static final String MODE_FOLLOWING = "following";

    /// <summary>mode 값: 팔로워(나를 팔로우한 사람들)</summary>
    public static final String MODE_FOLLOWERS = "followers";

    /// <summary>
    /// activity_follow_list.xml의 View 묶음
    /// </summary>
    private ActivityFollowListBinding binding;

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성 — mode에 따라 팔로잉/팔로워 목록을 채운다
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFollowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 팔로워 모드가 아니면 기본은 팔로잉
        boolean followersMode = MODE_FOLLOWERS.equals(getIntent().getStringExtra(EXTRA_MODE));

        App app = (App) getApplication();
        // 나(팔로우 버튼 판단·토글용)와, 목록을 볼 대상 유저(없으면 나)를 구분
        String currentId = app.getAccountManager().getCurrentAccountId();
        String viewedId = getIntent().getStringExtra(EXTRA_ACCOUNT_ID);
        if (viewedId == null) {
            viewedId = currentId;
        }

        // 제목 + 목록 + 빈 안내를 모드에 맞춰 준비 (목록은 "본 유저"의 것)
        List<AccountProfile> list;
        if (followersMode) {
            setTitle(R.string.follow_list_followers_title);
            binding.textViewFollowEmpty.setText(R.string.follow_list_followers_empty);
            list = app.getCommunityRepository().getFollowers(viewedId);
        } else {
            setTitle(R.string.follow_list_following_title);
            binding.textViewFollowEmpty.setText(R.string.follow_list_following_empty);
            list = app.getCommunityRepository().getFollowing(viewedId);
        }

        boolean isEmpty = list.isEmpty();
        binding.textViewFollowEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerFollow.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        binding.recyclerFollow.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFollow.setAdapter(new FollowAdapter(list, currentId, app.getUserPrefs()));
    }

    /// <summary>
    /// ActionBar ← 버튼 → 화면 닫기 (홈으로 복귀)
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
