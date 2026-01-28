public class 이미지 {


    // 1. 영상 저장
    // 재생 대기열에는 최대 600장의 이미지를 다운로드
    // 줄이 너무 길어지면 메모리가 부족하니까 제한을 둠
    final short QUEUE_CAPACITY = 600;

    // [0.5초 딜레이의 이유]
    // 대기열에 최소 180 프레임(3초 분량)은 들어와야 동영상 재생 가능
    final short START_THRESHOLD = 180;

    // [재생 대기열]
    // 다운로드 된 영상 프레임들이 차례대로 대기하는 공간
    int[][] playBuffer; // 앞쪽 배열은 QUEUE_CAPACITY만큼 크기를 가지고 있음


    // 2. 영상 재생과 관련된 데이터
    // 인터넷에서 새로운 프레임을 받아 대기열 끝에 추가함
    short downloadIndex;

    // [재생 위치]
    // 대기열 맨 앞에서부터 프레임을 꺼내 화면에 보여줌
    short playIndex;

    // 3. 상태
    boolean isPlaying;

    // 마우스가 영상 영역 안에 들어왔는지 여부
    boolean isMouseOver;


    // 4. UI 오버레이 데이터 (Composite UI)
    char[] videoTitle;
    int[] overlayBackgroundImage;

    int[] playIconImage;   // 재생(▶) 아이콘
    int[] pauseIconImage;  // 일시정지(||) 아이콘
    int[] volumeIconImage; // 스피커 아이콘


    // 5. 오디오 데이터
    // 0 ~ 1 사이의 값.
    float volumeLevel;

    // 샘플링 레이트
    // 타겟 브라우저 환경의 오디오 출력 주파수 계측한 결과: 44,100Hz
    final int SAMPLE_RATE = 44100;

    // 동기화 비율
    // 영상 1프레임(1/60초) 동안 소리는 얼마나 재생해야 하는가? => 44,100(Hz) / 60(FPS) = 735 samples
    // 소수점 없이 정확히 떨어지므로, 장시간 재생해도 싱크가 밀리지 않음
    final short SAMPLES_PER_FRAME = 735;

    // 오디오 표준 데이터(PCM)의 크기는 -32,768 ~ +32,767 범위
    // 이는 2Byte(16-bit) 크기에 해당
    short[][] audioBuffer;


    // 6. 오디오 UI
    // 회색의 얇은 가로 막대 이미지
    int[] volumeTrackImage;

    // 트랙 위를 움직이는 동그라미 아이콘 이미지
    int[] volumeKnobImage;

    // [레이아웃 데이터]
    // 볼륨바의 위치와 크기
    short trackOffsetX;
    short trackOffsetY;
    short trackWidth;


    // 7. 레이아웃 데이터
    short offsetX;
    short offsetY;
    boolean isVisible;
    byte layoutPriority;



}
