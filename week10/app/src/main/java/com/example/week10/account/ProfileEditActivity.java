package com.example.week10.account;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week10.App;
import com.example.week10.R;
import com.example.week10.databinding.ActivityProfileEditBinding;

/// <summary>
/// 프로필 편집 화면 (계정별)
///
/// ──── 무엇을 하나 ────
/// 현재 로그인한 계정의 아바타 색 / 별명 / 한 줄 소개를 고쳐서 저장한다.
///   - 별명 → AccountManager (별명은 AccountManager가 소유하는 값)
///   - 아바타 색 / 한 줄 소개 → UserPrefs (그 외 개인 설정)
/// 저장하면 화면을 닫고, 돌아간 Home이 onResume에서 바뀐 내용을 다시 그린다.
///
/// ──── 색 선택 방식 ────
/// 미리 정해둔 색 팔레트(AVATAR_PALETTE)를 가로로 동그라미 버튼으로 깔고,
/// 고른 색은 진하게(불투명) / 안 고른 색은 흐리게(반투명) 보여 선택 상태를 표시한다.
/// 위쪽 큰 동그라미(미리보기)에 고른 색 + 별명 첫 글자를 실시간으로 비춰준다.
/// </summary>
public class ProfileEditActivity extends AppCompatActivity {

    /// <summary>
    /// 고를 수 있는 아바타 색 팔레트 (ARGB)
    /// 기본색(파랑)은 UserPrefs.DEFAULT_AVATAR_COLOR를 그대로 넣는다 → 아직 색을 안 고른 계정이
    /// 처음 들어왔을 때 그 기본 색이 팔레트에서 선택된 상태로 보임 (값 중복 제거)
    /// </summary>
    private static final int[] AVATAR_PALETTE = {
            0xFFE53935,                      // 빨강
            0xFFFB8C00,                      // 주황
            0xFF43A047,                      // 초록
            UserPrefs.DEFAULT_AVATAR_COLOR,  // 파랑 (기본값 — UserPrefs와 공유)
            0xFF8E24AA,                      // 보라
            0xFF757575,                      // 회색
    };

    /// <summary>
    /// activity_profile_edit.xml의 View 묶음
    /// </summary>
    private ActivityProfileEditBinding binding;

    /// <summary>
    /// 전역 계정 관리자 (별명 수정에 사용)
    /// </summary>
    private AccountManager accountManager;

    /// <summary>
    /// 현재 로그인 계정의 개인 설정 저장소 (아바타 색 / bio 저장에 사용)
    /// </summary>
    private UserPrefs userPrefs;

    /// <summary>
    /// 지금 로그인된 계정 아이디 (별명 수정 시 어느 계정인지 지정용)
    /// </summary>
    private String accountId;

    /// <summary>
    /// 현재 고른 아바타 색 (저장 버튼을 누르면 이 값이 저장됨)
    /// </summary>
    private int selectedColor;

    // ========== Lifecycle ==========

    /// <summary>
    /// 프로필 편집 화면 생성
    /// 현재 값 불러오기 → 입력칸 채우기 → 색 팔레트 만들기 → 미리보기/저장 연결
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        App app = (App) getApplication();
        accountManager = app.getAccountManager();
        userPrefs = app.getUserPrefs();
        accountId = accountManager.getCurrentAccountId();

        // 로그인되지 않은 상태로 잘못 들어오면(비정상) 화면을 닫는다
        boolean noSession = accountId == null || userPrefs == null;
        if (noSession) {
            finish();
            return;
        }

        // 현재 저장값을 입력칸에 채운다
        binding.editTextNickname.setText(accountManager.getNickname(accountId));
        binding.editTextBio.setText(userPrefs.getBio());
        selectedColor = userPrefs.getAvatarColor();

        // 색 팔레트 동그라미들을 만들어 가로 줄에 채운다
        buildColorSwatches();

        // 별명을 고치면 미리보기의 첫 글자도 바로 바뀌도록 감시
        binding.editTextNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력 전 — 할 일 없음
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력 중 — 할 일 없음
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 입력이 끝난 시점에 미리보기 글자 갱신
                updateAvatarPreview();
            }
        });

        // 첫 화면에 미리보기를 한 번 그려둔다
        updateAvatarPreview();

        binding.buttonSave.setOnClickListener(v -> onSaveClicked());
    }

    // ========== 색 팔레트 ==========

    /// <summary>
    /// 팔레트의 색마다 동그라미 View를 만들어 가로 줄(layoutColorSwatches)에 넣는다
    /// 각 동그라미를 누르면 그 색을 고르고 미리보기를 갱신
    /// </summary>
    private void buildColorSwatches() {
        // 색 동그라미 한 칸의 지름 (dp)
        final int SWATCH_SIZE_DP = 44;
        int sizePx = dpToPx(SWATCH_SIZE_DP);

        // 색 동그라미 사이 간격 (dp)
        final int SWATCH_MARGIN_DP = 8;
        int marginPx = dpToPx(SWATCH_MARGIN_DP);

        for (int color : AVATAR_PALETTE) {
            View swatch = new View(this);

            // 크기 + 오른쪽 간격
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMargins(0, 0, marginPx, 0);
            swatch.setLayoutParams(params);

            // 원 모양 배경 + 그 색으로 칠하기 (아바타와 같은 drawable 재사용)
            swatch.setBackgroundResource(R.drawable.bg_avatar_circle);
            swatch.setBackgroundTintList(ColorStateList.valueOf(color));

            // 어떤 색인지 View에 메모해둔다 (선택 표시를 다시 칠할 때 비교용)
            swatch.setTag(color);

            // 누르면 그 색을 선택
            swatch.setOnClickListener(v -> {
                selectedColor = (int) v.getTag();
                updateSwatchSelection();
                updateAvatarPreview();
            });

            binding.layoutColorSwatches.addView(swatch);
        }

        // 처음 선택 상태(현재 색) 표시
        updateSwatchSelection();
    }

    /// <summary>
    /// 동그라미들의 선택 상태를 다시 칠한다
    /// 고른 색은 진하게(불투명), 나머지는 흐리게(반투명)
    /// </summary>
    private void updateSwatchSelection() {
        int count = binding.layoutColorSwatches.getChildCount();
        for (int i = 0; i < count; i++) {
            View swatch = binding.layoutColorSwatches.getChildAt(i);
            int swatchColor = (int) swatch.getTag();
            boolean isSelected = swatchColor == selectedColor;

            // 선택 안 된 색 동그라미의 투명도 (0~1) — 흐리게 보여 "안 고름"을 표시
            final float SWATCH_DIM_ALPHA = 0.35f;
            swatch.setAlpha(isSelected ? 1.0f : SWATCH_DIM_ALPHA);
        }
    }

    // ========== 미리보기 ==========

    /// <summary>
    /// 위쪽 큰 동그라미(미리보기)를 현재 별명/색으로 갱신
    /// 색은 고른 색으로 칠하고, 글자는 별명 첫 글자(없으면 물음표)를 보여줌
    /// </summary>
    private void updateAvatarPreview() {
        binding.textViewAvatarPreview.setBackgroundTintList(
                ColorStateList.valueOf(selectedColor));
        binding.textViewAvatarPreview.setText(currentInitial());
    }

    /// <summary>
    /// 지금 입력된 별명의 첫 글자(대문자)를 반환, 비어 있으면 물음표
    /// </summary>
    private String currentInitial() {
        String nickname = binding.editTextNickname.getText().toString().trim();
        if (nickname.isEmpty()) {
            return getString(R.string.profile_avatar_placeholder);
        }
        // 첫 글자만 잘라 대문자로 (한글은 대문자 변환이 없어 그대로 나옴)
        char first = Character.toUpperCase(nickname.charAt(0));
        return String.valueOf(first);
    }

    // ========== 저장 ==========

    /// <summary>
    /// 저장 버튼 클릭 처리
    /// 별명이 비어 있으면 막고, 통과하면 별명/소개/색을 각각 저장한 뒤 화면을 닫는다
    /// </summary>
    private void onSaveClicked() {
        String nickname = binding.editTextNickname.getText().toString().trim();
        String bio = binding.editTextBio.getText().toString().trim();

        boolean nicknameEmpty = nickname.isEmpty();
        if (nicknameEmpty) {
            showToast(getString(R.string.profile_nickname_empty));
            return;
        }

        // 별명은 AccountManager, 소개/색은 UserPrefs에 각각 저장 (소유 역할대로)
        accountManager.updateNickname(accountId, nickname);
        userPrefs.setBio(bio);
        userPrefs.setAvatarColor(selectedColor);

        showToast(getString(R.string.profile_saved));
        // 화면을 닫으면 돌아간 Home이 onResume에서 새 프로필을 다시 그림
        finish();
    }

    // ========== 헬퍼 ==========

    /// <summary>
    /// dp 값을 실제 픽셀(px)로 바꾼다
    /// 화면 밀도(density)가 기기마다 달라, 코드로 크기를 줄 때는 dp×density로 환산해야
    /// 어느 기기에서도 같은 크기로 보인다
    /// </summary>
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    /// <summary>
    /// 짧은 안내 메시지(Toast)를 띄운다
    /// </summary>
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
