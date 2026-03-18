package com.example.weak7_1;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Activity 속성 데모 화면
 *
 * 이 Activity는 AndroidManifest.xml에서 설정하는 3가지 주요 속성을 체험하기 위한 화면이다.
 *
 * [AndroidManifest.xml에 아래와 같이 등록해야 한다]
 * <activity
 *     android:name=".PortraitActivity"
 *     android:screenOrientation="portrait"
 *     android:configChanges="orientation|screenSize"
 *     android:windowSoftInputMode="adjustResize" />
 *
 * ──────────────────────────────────────────────────────
 * 1. screenOrientation="portrait"
 * ──────────────────────────────────────────────────────
 * Activity의 화면 방향을 세로(portrait)로 고정한다.
 * 기기를 옆으로 돌려도 화면이 회전하지 않는다.
 *
 * [Unity 비유]
 * Player Settings > Resolution and Presentation > Default Orientation을
 * "Portrait"으로 설정하는 것과 동일하다.
 * Unity에서도 Portrait으로 고정하면 기기를 돌려도 화면이 회전하지 않는다.
 *
 * ──────────────────────────────────────────────────────
 * 2. configChanges="orientation|screenSize"
 * ──────────────────────────────────────────────────────
 * Android의 기본 동작: 화면 회전 시 Activity를 파괴(onDestroy)하고 재생성(onCreate).
 * 이때 모든 인스턴스 변수가 초기화된다. (마치 씬을 다시 로드하는 것)
 *
 * configChanges를 지정하면: Activity를 파괴하지 않고 onConfigurationChanged() 콜백만 호출한다.
 * 변수 값이 유지되므로 상태 관리가 훨씬 간단해진다.
 *
 * [Unity 비유]
 * Unity는 기본적으로 화면 회전 시 씬을 다시 로드하지 않는다.
 * Screen.orientation이 바뀌어도 Awake()/Start()가 다시 호출되지 않는다.
 * Android에서 configChanges를 설정하면 Unity와 비슷한 동작이 된다.
 * 설정하지 않으면 마치 SceneManager.LoadScene(현재씬이름)이 매번 호출되는 것과 같다.
 *
 * [왜 Activity가 재생성되는가?]
 * Android는 화면 회전을 "설정 변경(Configuration Change)"으로 취급한다.
 * 설정이 바뀌면 리소스(레이아웃, 문자열 등)를 새로 로드해야 할 수 있으므로
 * 기본적으로 Activity를 처음부터 다시 만든다.
 * configChanges로 "이 설정 변경은 내가 직접 처리하겠다"고 시스템에 알리는 것이다.
 *
 * ──────────────────────────────────────────────────────
 * 3. windowSoftInputMode="adjustResize"
 * ──────────────────────────────────────────────────────
 * 소프트 키보드가 올라올 때 Activity의 레이아웃 크기를 키보드만큼 줄여서
 * 키보드 위에 콘텐츠가 보이도록 한다.
 *
 * 다른 옵션:
 * - adjustPan: 포커스된 입력 필드가 보이도록 화면 전체를 위로 밀어올린다.
 * - adjustNothing: 키보드가 레이아웃을 가려도 아무 조정을 하지 않는다.
 *
 * [Unity 비유]
 * TouchScreenKeyboard.Open() 호출 시의 동작과 유사하다.
 * Unity에서는 키보드가 올라와도 Canvas의 크기가 자동으로 줄어들지 않는다.
 * TouchScreenKeyboard.area를 읽어서 직접 UI를 조정해야 한다.
 * Android의 adjustResize는 이 과정을 시스템이 자동으로 처리해주는 것이다.
 */
public class PortraitActivity extends AppCompatActivity {

    /**
     * onCreate가 호출된 횟수를 기록한다.
     * configChanges를 설정했으므로 화면 회전 시 onCreate가 다시 호출되지 않는다.
     * 따라서 이 값은 Activity가 처음 생성될 때 1이 되고, 이후 변하지 않아야 정상이다.
     *
     * [Unity 비유]
     * MonoBehaviour.Awake() 또는 Start() 호출 횟수를 세는 것과 같다.
     * 씬이 다시 로드되지 않으면 Awake()도 다시 호출되지 않는 것처럼,
     * configChanges 설정 시 onCreate도 다시 호출되지 않는다.
     */
    private int onCreateCount = 0;

    /**
     * onConfigurationChanged가 호출된 횟수를 기록한다.
     * configChanges를 설정하면 화면 회전 시 이 콜백이 호출된다.
     * 단, screenOrientation="portrait"으로 고정했으므로
     * 실제로 화면 회전이 발생하지 않아 이 값은 0으로 유지될 수 있다.
     *
     * [Unity 비유]
     * Screen.orientation 변경 이벤트를 받는 것과 유사하다.
     * Portrait으로 고정하면 이벤트 자체가 발생하지 않는 것과 같은 원리.
     */
    private int configChangeCount = 0;

    // UI 요소 참조 변수들 (Unity의 [SerializeField] private Text 와 유사)
    private TextView tvOrientationInfo;
    private TextView tvOnCreateCount;
    private TextView tvConfigChangeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // EdgeToEdge: 시스템 바(상태바, 네비게이션 바) 영역까지 콘텐츠를 확장한다.
        // Unity 비유: Canvas의 Render Mode를 Screen Space - Overlay로 설정하고
        // Safe Area 밖까지 렌더링하는 것과 유사.
        EdgeToEdge.enable(this);

        // 레이아웃 XML을 이 Activity에 연결한다.
        // Unity 비유: Instantiate(prefab)으로 UI 프리팹을 생성하는 것과 유사.
        setContentView(R.layout.activity_portrait);

        // 시스템 바(상태바, 네비게이션 바)에 가려지지 않도록 패딩을 자동 적용한다.
        // Unity 비유: Screen.safeArea를 읽어서 UI 영역을 안전 영역 안으로 조정하는 것과 동일.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // onCreate 호출 횟수 증가
        onCreateCount++;

        // UI 요소를 찾아서 변수에 저장한다.
        // Unity 비유: GameObject.Find("이름").GetComponent<Text>() 와 동일한 개념.
        // Android에서는 XML에 지정한 id로 뷰를 찾는다.
        tvOrientationInfo = findViewById(R.id.tvOrientationInfo);
        tvOnCreateCount = findViewById(R.id.tvOnCreateCount);
        tvConfigChangeCount = findViewById(R.id.tvConfigChangeCount);

        // 화면에 현재 상태를 표시한다.
        updateUI();
    }

    /**
     * 설정 변경(Configuration Change) 시 시스템이 호출하는 콜백.
     * configChanges="orientation|screenSize"를 매니페스트에 지정했을 때만 호출된다.
     * 지정하지 않으면 이 메서드 대신 Activity가 파괴되고 onCreate가 다시 호출된다.
     *
     * [Unity 비유]
     * Unity에서 Screen.orientation이 바뀔 때 호출되는 이벤트 콜백과 유사하다.
     * 예를 들어 Screen.orientationChanged 같은 이벤트가 있다면 그것에 해당.
     * 실제 Unity에서는 Update()에서 Screen.width/height 변화를 감지하는 방식으로 처리한다.
     *
     * @param newConfig 변경된 설정 정보 (새로운 화면 방향, 크기 등을 담고 있다)
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 설정 변경 콜백 호출 횟수 증가
        configChangeCount++;

        // 변경된 설정으로 UI를 업데이트한다.
        updateUI();
    }

    /**
     * 현재 상태 정보를 화면에 표시하는 메서드.
     * onCreate와 onConfigurationChanged에서 공통으로 호출한다.
     *
     * [Unity 비유]
     * UI 갱신 로직을 별도 메서드로 분리하는 것은
     * Unity에서도 UpdateUI() 같은 메서드를 만들어 Start()와 이벤트 콜백에서
     * 공통으로 호출하는 패턴과 동일하다.
     */
    private void updateUI() {
        // 현재 화면 방향을 가져온다.
        // Configuration.ORIENTATION_PORTRAIT = 1 (세로)
        // Configuration.ORIENTATION_LANDSCAPE = 2 (가로)
        // Unity 비유: Screen.orientation 값을 읽는 것과 동일.
        int orientation = getResources().getConfiguration().orientation;
        String orientationText;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientationText = "PORTRAIT (세로, 값: " + orientation + ")";
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientationText = "LANDSCAPE (가로, 값: " + orientation + ")";
        } else {
            orientationText = "UNDEFINED (미정의, 값: " + orientation + ")";
        }

        // 각 TextView에 현재 상태값을 표시한다.
        tvOrientationInfo.setText("현재 방향: " + orientationText);
        tvOnCreateCount.setText("onCreate 호출 횟수: " + onCreateCount);
        tvConfigChangeCount.setText("onConfigurationChanged 호출 횟수: " + configChangeCount);
    }
}
