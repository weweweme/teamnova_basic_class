import java.awt.*;
import java.time.Duration;
import java.util.*;


class VideoFrame {
    byte[] imageData;      // 실제 영상 이미지
    Duration timestamp;    // "이 프레임은 1분 30초 05에 보여주세요"라는 시간표
}

// 영상의 현재 상태를 정의하는 열거형
enum VideoStatus {
    PLAYING,    // 재생 중
    PAUSED,     // 일시정지
    BUFFERING,  // 로딩 중
    STOPPED     // 정지
}

public class VideoPlayer {
    // Group A. 미디어 데이터
    // ---------------------------------------------------------
    String sourceUrl;

    // [1] 데이터 저장소
    // 한 번 다운로드된 데이터는 Map에 저장 (Key: 프레임 번호)
    // "1500번째 프레임 내놔" 하면 바로 줄 수 있음 (Random Access)
    Map<Long, byte[]> downloadedCache;

    // [2] 재생 대기열
    // 화면에 송출하기 위해 Cache에서 꺼내와서 순서대로 줄 세운 곳
    // 재생 시 여기서 하나씩 소비됨 (FIFO - Queue)
    Queue<VideoFrame> activeBuffer;

    // 프레임 정보
    long totalFrames;       // 전체 프레임 수 (예: 3600)
    long currentFrameIndex; // 현재 보여주는 프레임 번호 (예: 150)

    // 시간 및 상태 정보
    Duration currentTime;   // 현재 시간
    Duration totalDuration; // 전체 시간
    VideoStatus status;     // 상태 (Enum)
    double volume;          // 볼륨


    // Group B. 메인 화면 영역
    // ---------------------------------------------------------
    Point position;     // 플레이어 전체 위치
    Dimension size;     // 플레이어 전체 크기

    // 화면에 '지금 당장' 출력되고 있는 단 한 장의 이미지
    // (Queue의 맨 앞에서 꺼낸 녀석이 여기에 잠시 머물다 사라짐)
    byte[] currentDisplayingFrame;


    // Group C. 제어 UI
    // ---------------------------------------------------------

    // 1. 버튼 (Slide 5에서 설계한 Button 클래스 재사용)
    Button playPauseButton;
    Button fullScreenButton;

    // 2. 진행 바 (Time Bar)
    Point timeBarPos;
    Dimension timeBarSize;
    double playProgress;    // 0.0 ~ 1.0 (현재 몇 % 재생했는지)

    // 3. 볼륨 바 (Volume Bar)
    Point volBarPos;
    Dimension volBarSize;
    double volumeLevel;     // 0.0 ~ 1.0 (현재 볼륨 크기)
}
