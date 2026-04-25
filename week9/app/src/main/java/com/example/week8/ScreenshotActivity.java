package com.example.week8;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.databinding.ActivityScreenshotBinding;

/// <summary>
/// 스크린샷 화면
/// 갤러리 앱을 호출해서 이미지를 선택하고, 선택한 사진을 미리보기로 표시
/// Unity로 비유하면 NativeGallery 플러그인을 호출해서 이미지 경로를 받아오는 것
///
/// ──── 이 화면의 학습 포인트 ────
/// 1. 외부 앱 호출: 시스템 Documents UI에게 "이미지 하나 골라서 돌려줘" 요청 (ACTION_OPEN_DOCUMENT)
/// 2. Lifecycle 흐름 관찰: 선택기가 열리는 동안 onPause → onStop,
///    돌아올 때 onRestart → onStart → onResume
///
/// ──── 왜 카메라가 아니라 갤러리인가 ────
/// 게임 스크린샷은 보통 PC/콘솔에서 찍어서 폰으로 옮긴 뒤 업로드하는 흐름
/// → 모바일 카메라로 직접 찍는 건 부자연스러움
/// → 갤러리에서 선택하는 방식이 실제 게임 다이어리 앱 UX에 맞음
///
/// ──── Intent 학습 ────
/// 수신: GameDetail에서 게임 제목을 Intent extras로 받음
/// 송신: ACTION_OPEN_DOCUMENT로 Documents UI 호출 → 선택 결과(Uri)를 갤러리 런처로 수신
/// </summary>
public class ScreenshotActivity extends AppCompatActivity {

    /// <summary>
    /// Logcat 필터용 태그
    /// Android Studio 하단 Logcat 패널에서 이 문자열로 필터링하면
    /// 이 Activity의 Lifecycle 흐름만 모아서 볼 수 있음
    /// </summary>
    private static final String TAG = "ScreenshotActivity";

    /// <summary>
    /// Intent extras에서 게임 제목을 꺼낼 때 사용하는 키
    /// GameDetailActivity(보내는 쪽)와 같은 키를 써야 함
    /// </summary>
    public static final String EXTRA_GAME_TITLE = "extra_game_title";

    /// <summary>
    /// ViewBinding 객체
    /// </summary>
    private ActivityScreenshotBinding binding;

    /// <summary>
    /// 갤러리에서 이미지를 선택하고 그 결과를 받는 "런처"
    ///
    /// ──── ActivityResultLauncher란? ──── (공식 문서: https://developer.android.com/training/basics/intents/result)
    /// 예전에는 startActivityForResult + onActivityResult 콜백으로 결과를 받았는데,
    /// 지금은 이 방식이 deprecated(더 이상 권장하지 않음)됨
    /// → 새 방식: 런처를 미리 등록해두고, launch()로 실행 + 결과는 등록할 때 준 람다로 받음
    /// </summary>
    private ActivityResultLauncher<Intent> galleryLauncher;

    // ========== Lifecycle ==========

    /// <summary>
    /// 화면 생성
    /// Intent에서 게임 제목을 받아 상단에 표시하고,
    /// 갤러리 런처 등록 + "갤러리에서 선택" 버튼 리스너 등록
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // ViewBinding 연결
        binding = ActivityScreenshotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // GameDetail에서 전달받은 게임 제목을 상단에 표시
        String gameTitle = getIntent().getStringExtra(EXTRA_GAME_TITLE);
        if (gameTitle != null) {
            binding.textViewGameTitle.setText(gameTitle);
        }

        // ──────── 갤러리 런처 등록 ────────
        // 상세 개념 설명은 GameDetailActivity.onCreate의 reviewLauncher 등록 부분 참고
        // (Launcher란 / Contract / Lifecycle 연동 / 람다 콜백 / launch 내부 6단계)
        //
        // 핵심 규칙 (공식 문서): https://developer.android.com/training/basics/intents/result
        //   Activity가 STARTED 상태가 되기 전에 등록해야 함 (STARTED 이후 호출 시 IllegalStateException)
        //   → onCreate가 표준 위치 (onCreate 완료 시 CREATED 상태, onStart 호출 시 STARTED 상태)
        //   → 버튼 클릭 시점에 등록하면 회전 후 결과 누락 가능
        //
        // 첫 번째 인자: Contract (계약서)
        //   StartActivityForResult = "입력은 Intent, 출력은 ActivityResult" 범용 계약
        //
        // 두 번째 인자: 결과 도착 시 실행될 람다 콜백
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Documents UI가 닫히면서 결과가 돌아왔을 때 실행됨
                    //   - 사용자가 이미지 선택 → resultCode = RESULT_OK, data = 선택한 Uri 담긴 Intent
                    //   - 사용자가 ← 뒤로가기로 취소 → resultCode = RESULT_CANCELED, data = null
                    boolean isOk = result.getResultCode() == RESULT_OK;
                    boolean hasData = result.getData() != null;
                    if (isOk && hasData) {
                        // 선택한 이미지의 Uri 꺼내기
                        // 형태 예: content://com.android.providers.media.documents/document/image%3A123
                        // → content:// 스키마이므로 ImageView.setImageURI로 바로 로드 가능
                        Uri imageUri = result.getData().getData();
                        showPickedImage(imageUri);
                    }
                }
        );

        // 갤러리에서 선택 버튼 클릭 → 갤러리 앱 호출
        binding.buttonPickImage.setOnClickListener(v -> openGallery());
    }

    // ========== 암시적 Intent: 갤러리/파일 선택기 호출 ==========

    /// <summary>
    /// 시스템 문서 선택기(Documents UI)를 호출해서 이미지 하나를 선택
    /// 사진 앨범 + 다운로드 + 파일 시스템을 한 화면에서 모두 탐색 가능
    ///
    /// ──── ACTION_OPEN_DOCUMENT 학습 ────
    /// 공식 문서: https://developer.android.com/guide/topics/providers/document-provider
    ///
    /// ACTION_OPEN_DOCUMENT: "문서 파일 선택기 열어줘" 요청
    ///   → 시스템이 제공하는 Documents UI가 열림
    ///   → ☰ 햄버거 메뉴로 Recent/Images/Downloads/Google Photos/Files 등 전환 가능
    ///
    /// setType("image/*"): "이미지 파일만 선택 가능하게 필터"
    ///   - "image/png" 면 PNG만, "image/jpeg" 면 JPG만
    ///   - "image/*" 면 PNG/JPG/WEBP 등 모든 이미지 종류 허용
    ///
    /// ──── ACTION_GET_CONTENT vs ACTION_OPEN_DOCUMENT ────
    /// ACTION_GET_CONTENT: Android 13+ 에서 Photo Picker로 제한됨 (앨범만 보임)
    /// ACTION_OPEN_DOCUMENT: 항상 Documents UI → 앨범 + 다운로드 + 파일 전부 탐색
    ///   → 실제 프로덕트에서 "앨범도 파일도 고를 수 있게" 하고 싶을 때 이걸 씀
    ///
    /// 실제 Intent 내부 모습:
    ///   action = "android.intent.action.OPEN_DOCUMENT"
    ///   type   = "image/*"
    ///   category = "android.intent.category.OPENABLE"
    /// → Android가 Documents UI를 띄워서 이미지 파일을 선택하게 함
    ///
    /// CATEGORY_OPENABLE: "열 수 있는 파일만 돌려줘"
    ///   → 선택한 파일이 openInputStream으로 읽을 수 있음을 보장
    /// </summary>
    private void openGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pickIntent.setType("image/*");
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // try-catch로 launcher 실행
        //
        // 원래 resolveActivity()로 "처리 가능한 앱이 있는지" 미리 확인하는 방식을 썼지만,
        // Android 11+ 부터는 "패키지 가시성 제한"이 생겨서
        // 시스템 앱조차 resolveActivity()에서 null이 나오는 경우가 있음
        // (앱이 맘대로 다른 앱 목록을 볼 수 없게 보안 강화된 것)
        //
        // ACTION_OPEN_DOCUMENT는 시스템 Documents UI가 처리하므로 거의 항상 성공함
        // 혹시 실패하면 ActivityNotFoundException이 발생 → try-catch로 안전하게 처리
        // (try-catch는 "정말 예외가 날 수 있는 경우"에만 쓴다는 원칙에 부합)
        try {
            galleryLauncher.launch(pickIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.screenshot_no_gallery, Toast.LENGTH_SHORT).show();
        }
    }

    // ========== 미리보기 표시 ==========

    /// <summary>
    /// 선택한 이미지의 Uri를 ImageView에 표시하고, 안내 문구(textViewEmpty)를 숨김
    /// </summary>
    private void showPickedImage(Uri imageUri) {
        if (imageUri == null) {
            return;
        }
        // ImageView는 Uri를 받아서 이미지를 로드할 수 있음
        binding.imageViewPreview.setImageURI(imageUri);
        // 사진이 생겼으니 "아직 추가한 사진이 없습니다" 문구는 숨김
        binding.textViewEmpty.setVisibility(View.GONE);
    }

    // ========== Lifecycle 관찰용 로그 ==========
    // Logcat에서 "ScreenshotActivity" 태그로 필터링하면
    // 갤러리 앱 호출 시 이 Activity가 어떤 순서로 콜백을 거치는지 확인 가능
    //
    // 예상 흐름 (갤러리 열었다가 사진 선택 후 복귀):
    //   1. 처음 진입:     onCreate → onStart → onResume
    //   2. 갤러리 열림:   onPause → onStop
    //   3. 갤러리 복귀:   onRestart → onStart → onResume
    //   4. ← 뒤로가기:   onPause → onStop → onDestroy

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    /// <summary>
    /// ActionBar의 ← 버튼 클릭 처리
    ///
    /// finish() 동작:
    ///   - 이 Activity를 "종료해달라"고 Android에 요청 (즉시 죽는 게 아니라 종료 예약)
    ///   - 호출 직후 onPause → onStop → onDestroy 순서로 콜백 자동 실행
    ///   - 백스택에서 제거되고 호출자(GameDetail)로 돌아감
    ///   - 별도의 setResult를 호출하지 않으므로 호출자는 "취소"로 간주 (이 화면은 결과 반환 없음)
    ///
    /// finish()가 호출되는 경로:
    ///   ① 사용자가 ActionBar ← 버튼 클릭 (이 메서드)
    ///   ② 사용자가 뒤로가기 버튼 클릭 (Android가 내부적으로 finish() 호출)
    ///
    /// 반환값 의미:
    ///   true  → "내가 이 항목을 처리했으니 다른 곳에 전달 마"
    ///   super 호출 → "모르는 항목이니 부모가 처리하도록 위임" (기본값 false 효과)
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
