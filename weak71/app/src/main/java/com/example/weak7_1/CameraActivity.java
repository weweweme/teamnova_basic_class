package com.example.weak7_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 카메라 권한 데모 화면
 * Manifest 학습 포인트: uses-permission, uses-feature, 런타임 권한 요청
 *
 * ============================================================
 * [uses-permission = 권한 선언]
 * ============================================================
 * AndroidManifest.xml에 <uses-permission android:name="android.permission.CAMERA"/> 를 선언하면
 * "이 앱은 카메라 기능을 사용합니다"라고 시스템에 알리는 것이다.
 *
 * Unity 비유: Player Settings > Other Settings 에서 Camera Usage Description을 설정하거나,
 *            Android 탭에서 권한을 체크하는 것과 같다.
 *            Unity가 빌드할 때 자동으로 AndroidManifest에 해당 권한을 추가해준다.
 *            Android 네이티브에서는 개발자가 직접 Manifest에 선언해야 한다.
 *
 * ============================================================
 * [uses-feature = 하드웨어 기능 요구]
 * ============================================================
 * <uses-feature android:name="android.hardware.camera.any" android:required="false"/>
 * → required="true"면 카메라 없는 기기에서는 Play Store에서 앱이 안 보인다.
 * → required="false"면 카메라 없어도 설치 가능하지만, 앱 내에서 기능 제한 처리 필요.
 *
 * Unity 비유: AR Foundation 사용 시 ARCore가 필수인데,
 *            ARCore를 지원하지 않는 기기에서는 앱 설치가 안 되도록
 *            Play Store가 자동 필터링하는 것과 동일한 원리.
 *
 * ============================================================
 * [런타임 권한 = 실행 중 사용자에게 물어보기]
 * ============================================================
 * Android 6.0(API 23) 이후부터 Manifest에 선언만으로는 부족하다.
 * "위험한 권한"(카메라, 위치, 마이크 등)은 앱 실행 중에 사용자에게 직접 물어야 한다.
 *
 * 흐름:
 * 1. Manifest에 <uses-permission> 선언 (설치 시 시스템이 인지)
 * 2. 런타임에 checkSelfPermission()으로 현재 상태 확인
 * 3. 권한이 없으면 requestPermission()으로 다이얼로그 표시
 * 4. 사용자가 허용/거부 선택 → 콜백으로 결과 수신
 *
 * Unity 비유:
 * Unity에서도 Permission.HasUserAuthorizedPermission("android.permission.CAMERA")로 확인하고,
 * Permission.RequestUserPermission("android.permission.CAMERA")로 요청한다.
 * 같은 원리인데, Android 네이티브에서는 ActivityResultLauncher를 사용한다.
 */
public class CameraActivity extends AppCompatActivity {

    /*
     * [ActivityResultLauncher - 결과를 받는 새로운 방식]
     *
     * 과거에는 startActivityForResult() + onActivityResult()를 사용했지만,
     * 현재는 ActivityResultLauncher가 권장된다. (deprecated 대체)
     *
     * Unity 비유: 콜백 패턴.
     *   과거: SendMessage("OnResult", data)  → 문자열 기반이라 타입 안전하지 않음
     *   현재: Action<Result> callback  → 타입이 명확한 콜백
     *
     * ActivityResultContracts.RequestPermission()
     *   → 권한 요청 전용 계약. launch("android.permission.CAMERA") 호출 시
     *     시스템 권한 다이얼로그가 뜨고, 결과(Boolean)가 콜백으로 전달된다.
     */
    private ActivityResultLauncher<String> permissionLauncher;

    /*
     * ActivityResultContracts.StartActivityForResult()
     *   → 다른 Activity를 실행하고 결과를 받는 계약.
     *     카메라 앱을 실행하고 촬영 결과(Bitmap)를 받는 데 사용한다.
     *
     * Unity 비유: NativeGallery나 NativeCamera 플러그인에서
     *            카메라 호출 후 콜백으로 사진 데이터를 받는 것과 같다.
     */
    private ActivityResultLauncher<Intent> cameraLauncher;

    /* UI 요소 참조 변수 */
    private ImageView ivPhoto;
    private TextView tvPermissionStatus;
    private TextView tvFeatureStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * EdgeToEdge + WindowInsets: 상태바/네비게이션바 영역까지 콘텐츠를 확장하되,
         * 시스템 UI와 겹치지 않도록 패딩을 자동 적용.
         * Unity 비유: Safe Area 설정.
         */
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===== UI 요소 바인딩 =====
        ivPhoto = findViewById(R.id.ivPhoto);
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
        tvFeatureStatus = findViewById(R.id.tvFeatureStatus);
        Button btnOpenCamera = findViewById(R.id.btnOpenCamera);
        Button btnCheckPermission = findViewById(R.id.btnCheckPermission);

        // ===== Launcher 초기화 (onCreate에서 반드시 등록해야 함) =====

        /*
         * [권한 요청 Launcher]
         *
         * registerForActivityResult()는 반드시 onCreate()에서 호출해야 한다.
         * Activity가 STARTED 상태 이후에 등록하면 IllegalStateException 발생.
         *
         * Unity 비유: Awake()/Start()에서 이벤트 리스너를 등록하는 것과 같다.
         *            Update()에서 등록하면 타이밍 문제가 생기는 것처럼,
         *            Android에서도 생명주기 초기에 등록해야 안전하다.
         *
         * isGranted == true : 사용자가 "허용"을 누름 → 카메라 실행
         * isGranted == false: 사용자가 "거부"를 누름 → 상태 텍스트 업데이트
         */
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 권한이 허용됨 → 카메라 실행
                        tvPermissionStatus.setText("권한 상태: ✅ 허용됨 (GRANTED)");
                        launchCamera();
                    } else {
                        // 권한이 거부됨
                        tvPermissionStatus.setText("권한 상태: ❌ 거부됨 (DENIED)");
                    }
                }
        );

        /*
         * [카메라 결과 Launcher]
         *
         * ACTION_IMAGE_CAPTURE로 카메라 앱을 실행하면,
         * 촬영 결과가 Intent의 extras에 "data"라는 키로 Bitmap 썸네일이 담겨 돌아온다.
         *
         * 주의: 이 방식은 썸네일(작은 이미지)만 반환한다.
         * 고해상도 원본이 필요하면 FileProvider + URI 방식을 사용해야 한다.
         *
         * Unity 비유: NativeCamera.TakePicture()의 콜백에서
         *            Texture2D를 받는 것과 같다.
         *            다만 여기서는 저해상도 썸네일이라는 점이 다르다.
         */
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        /*
                         * result.getData().getExtras().get("data")
                         * → 카메라 앱이 돌려준 Intent의 extras에서 "data" 키로 Bitmap을 꺼낸다.
                         *
                         * (Bitmap) 캐스팅:
                         * → get("data")의 반환 타입이 Object이므로 Bitmap으로 명시적 캐스팅 필요.
                         *   Unity의 (Texture2D)Resources.Load("path") 캐스팅과 같다.
                         */
                        Bitmap thumbnail = (Bitmap) result.getData().getExtras().get("data");
                        ivPhoto.setImageBitmap(thumbnail);
                    }
                }
        );

        // ===== 버튼 클릭 리스너 =====

        /*
         * [카메라 열기 버튼]
         *
         * 1단계: 현재 권한 상태 확인 (checkSelfPermission)
         * 2단계: 이미 허용이면 바로 카메라 실행
         * 3단계: 미허용이면 권한 요청 다이얼로그 표시
         *
         * Unity 비유:
         *   if (Permission.HasUserAuthorizedPermission(Permission.Camera))
         *       OpenCamera();
         *   else
         *       Permission.RequestUserPermission(Permission.Camera);
         */
        btnOpenCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                // 이미 권한이 있음 → 바로 카메라 실행
                tvPermissionStatus.setText("권한 상태: ✅ 허용됨 (GRANTED)");
                launchCamera();
            } else {
                // 권한이 없음 → 시스템 다이얼로그로 요청
                // launch()에 요청할 권한 문자열을 전달한다
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        /*
         * [권한 상태 확인 버튼]
         *
         * 권한을 요청하지 않고, 현재 상태만 확인하여 표시한다.
         *
         * ContextCompat.checkSelfPermission():
         * → PackageManager.PERMISSION_GRANTED (0) 또는 PERMISSION_DENIED (-1) 반환.
         *
         * Unity 비유: Permission.HasUserAuthorizedPermission()으로
         *            bool 결과만 확인하는 것과 같다.
         */
        btnCheckPermission.setOnClickListener(v -> updatePermissionStatus());

        // ===== 하드웨어 기능 확인 (앱 시작 시 바로 체크) =====
        checkCameraFeature();
    }

    /**
     * 카메라 앱을 실행한다.
     *
     * MediaStore.ACTION_IMAGE_CAPTURE:
     * → "사진을 찍어주세요"라는 암시적 Intent.
     *   Android 시스템이 이 Intent를 처리할 수 있는 앱(기본 카메라 등)을 찾아 실행한다.
     *
     * Unity 비유:
     *   NativeCamera.TakePicture()를 호출하면
     *   OS의 기본 카메라 앱이 열리는 것과 같다.
     *   Android에서는 Intent를 통해 시스템에 위임하는 방식이다.
     */
    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    /**
     * 현재 카메라 권한 상태를 확인하고 TextView에 표시한다.
     *
     * checkSelfPermission은 "현재 이 순간" 권한이 있는지만 확인한다.
     * 사용자가 설정 앱에서 권한을 취소할 수도 있으므로,
     * 카메라를 사용하기 전에 매번 확인하는 것이 안전하다.
     */
    private void updatePermissionStatus() {
        int permissionResult = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA);

        if (permissionResult == PackageManager.PERMISSION_GRANTED) {
            tvPermissionStatus.setText("권한 상태: ✅ 허용됨 (GRANTED)");
        } else {
            tvPermissionStatus.setText("권한 상태: ❌ 거부됨 (DENIED)");
        }
    }

    /**
     * 기기에 카메라 하드웨어가 있는지 확인하고 TextView에 표시한다.
     *
     * PackageManager.hasSystemFeature(FEATURE_CAMERA_ANY):
     * → 전면/후면 중 하나라도 카메라가 있으면 true.
     *
     * 이것은 런타임 체크이다. Manifest의 uses-feature와는 다르다:
     * - uses-feature (required="true"): Play Store에서 설치 전 필터링 (정적)
     * - hasSystemFeature(): 앱 실행 중 기기 기능을 직접 확인 (동적)
     *
     * 두 가지를 함께 사용하는 것이 안전하다:
     * - uses-feature required="false"로 설치는 허용하되
     * - hasSystemFeature()로 런타임에 기능 유무를 확인하여 UI를 분기
     *
     * Unity 비유:
     *   if (ARSession.state == ARSessionState.Unsupported)
     *       ShowARNotSupportedMessage();
     *   처럼 AR 지원 여부를 런타임에 체크하는 것과 같다.
     */
    private void checkCameraFeature() {
        boolean hasCamera = getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

        if (hasCamera) {
            tvFeatureStatus.setText("카메라 하드웨어: ✅ 있음 (FEATURE_CAMERA_ANY = true)");
        } else {
            tvFeatureStatus.setText("카메라 하드웨어: ❌ 없음 (FEATURE_CAMERA_ANY = false)");
        }
    }
}
