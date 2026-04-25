package com.example.week8;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.databinding.ActivityAddGameBinding;
import com.example.week8.model.Genre;
import com.example.week8.model.Platform;

/// <summary>
/// 게임 추가 화면
/// 사용자가 제목/장르/플랫폼/스토어URL을 입력하면 결과 Intent에 담아 돌려줌
/// MainActivity가 이 결과를 받아 GameRepository.addGame 호출 → 리스트에 반영
///
/// 현재(8주차)는 수동 입력 폼. 12주차에 RAWG API 학습 후
/// "검색 → 자동 메타데이터 수집" 흐름으로 교체될 예정
///
/// ──── Intent 학습 ────
/// 수신: 없음 (빈 폼으로 시작)
/// 송신: 결과 Intent에 입력값을 담아 setResult(RESULT_OK) + finish()
/// </summary>
public class AddGameActivity extends AppCompatActivity {

    // ========== 결과 Intent 계약 (OUT) ==========

    /// <summary>
    /// 결과 Intent 키: 게임 제목
    /// </summary>
    public static final String EXTRA_TITLE = "extra_title";

    /// <summary>
    /// 결과 Intent 키: 장르 (Genre enum의 name() 문자열로 전달)
    /// </summary>
    public static final String EXTRA_GENRE = "extra_genre";

    /// <summary>
    /// 결과 Intent 키: 플랫폼 (Platform enum의 name() 문자열로 전달)
    /// </summary>
    public static final String EXTRA_PLATFORM = "extra_platform";

    /// <summary>
    /// 결과 Intent 키: 스토어 URL (선택 사항이라 빈 문자열일 수 있음)
    /// </summary>
    public static final String EXTRA_STORE_URL = "extra_store_url";

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityAddGameBinding binding;

    // ========== Lifecycle ==========

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 연결
        binding = ActivityAddGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Spinner에 enum 표시 이름 채우기
        setupGenreSpinner();
        setupPlatformSpinner();

        // "추가하기" 버튼 리스너
        binding.buttonAdd.setOnClickListener(v -> submitGame());
    }

    /// <summary>
    /// ActionBar ← 버튼 처리
    /// </summary>
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ========== Spinner 초기화 ==========

    /// <summary>
    /// 장르 Spinner에 Genre enum 값들의 displayName을 채움
    /// </summary>
    private void setupGenreSpinner() {
        // Genre.values()로 enum 전체 배열을 얻고, displayName을 뽑아 문자열 배열로 변환
        Genre[] genres = Genre.values();
        String[] genreNames = new String[genres.length];
        for (int i = 0; i < genres.length; i++) {
            genreNames[i] = genres[i].getDisplayName();
        }

        // ArrayAdapter: 문자열 배열을 Spinner에 보여줄 수 있게 연결해주는 어댑터
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genreNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGenre.setAdapter(adapter);
    }

    /// <summary>
    /// 플랫폼 Spinner에 Platform enum 값들의 displayName을 채움
    /// </summary>
    private void setupPlatformSpinner() {
        Platform[] platforms = Platform.values();
        String[] platformNames = new String[platforms.length];
        for (int i = 0; i < platforms.length; i++) {
            platformNames[i] = platforms[i].getDisplayName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                platformNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPlatform.setAdapter(adapter);
    }

    // ========== 결과 반환 ==========

    /// <summary>
    /// "추가하기" 버튼 클릭 시 호출
    /// 입력값을 모아 결과 Intent에 담고 setResult + finish
    /// </summary>
    private void submitGame() {
        // 제목 꺼내기
        String title = "";
        if (binding.editTextTitle.getText() != null) {
            title = binding.editTextTitle.getText().toString().trim();
        }

        // 제목은 필수 — 비어있으면 Toast로 안내하고 종료하지 않음
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.add_game_title_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Spinner에서 선택된 인덱스로 enum 값 찾기
        int genreIndex = binding.spinnerGenre.getSelectedItemPosition();
        int platformIndex = binding.spinnerPlatform.getSelectedItemPosition();
        Genre selectedGenre = Genre.values()[genreIndex];
        Platform selectedPlatform = Platform.values()[platformIndex];

        // 스토어 URL 꺼내기 (선택 사항)
        String storeUrl = "";
        if (binding.editTextStoreUrl.getText() != null) {
            storeUrl = binding.editTextStoreUrl.getText().toString().trim();
        }

        // 결과 Intent 생성
        // enum은 Parcelable이 아니어서 name() 문자열로 전달 (받는 쪽에서 Genre.valueOf로 복원)
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_TITLE, title);
        resultIntent.putExtra(EXTRA_GENRE, selectedGenre.name());
        resultIntent.putExtra(EXTRA_PLATFORM, selectedPlatform.name());
        resultIntent.putExtra(EXTRA_STORE_URL, storeUrl);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
