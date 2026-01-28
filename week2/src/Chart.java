import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;


// 1. 공통 라벨 클래스 (일반화 적용)
// 가격표든 시간표든 "글자와 위치"만 있으면 된다는 점에 착안
class AxisLabel {
    String text;     // 화면에 보여줄 글자 (예: "25,000" 또는 "12/21")
    Point position;  // 화면상의 정확한 좌표 (x, y)
}

// 2. 차트를 구성하는 최소 단위: 캔들
class Candle {
    // 가격 정보 (내부적으로 8바이트 double을 사용하여 정밀한 소수점 관리)
    double open;   // 시가
    double close;  // 종가
    double high;   // 고가
    double low;    // 저가

    // 시간 정보 (내부적으로 날짜/시간 정보를 객체로 관리)
    LocalDateTime timestamp;
}

// 3. 캔들을 관리하고 그리는 컨테이너: 차트 객체
public class Chart {
    // Group A. 원본 데이터 저장소 (Model)
    // ---------------------------------------------------------
    // 외부에서 받아온 수천 개의 캔들 데이터 원본 (내부적으로 배열이 늘어나는 구조)
    ArrayList<Candle> allCandles;


    // Group B. 화면 설정 및 외형 (Visuals)
    // ---------------------------------------------------------
    Point position;         // 차트의 화면 위치 (x, y)
    Dimension size;         // 차트의 전체 크기 (width, height)
    Color backgroundColor;  // 배경색 (ARGB)


    // Group C. 뷰포트 데이터 (View Port - 화면에 보이는 범위)
    // ---------------------------------------------------------
    // 이 값들이 있어야 데이터(Price)를 화면 좌표(Y 픽셀)로 변환 가능
    double maxDisplayPrice;   // 화면 천장의 가격 (예: 26,000)
    double minDisplayPrice;   // 화면 바닥의 가격 (예: 24,000)

    // 이 값들이 있어야 데이터(Time)를 화면 좌표(X 픽셀)로 변환 가능
    LocalDateTime viewStartTime; // 화면 왼쪽 끝 시간
    LocalDateTime viewEndTime;   // 화면 오른쪽 끝 시간


    // Group D. 상호작용 및 상태 (Interaction)
    // ---------------------------------------------------------
    double zoomLevel;     // 확대 배율 (캔들의 너비 결정)
    int scrollOffset;     // 스크롤 위치 (현재 얼마나 옆으로 이동했는가)


    // Group E. 축 렌더링 데이터 (Axis Labels)
    // ---------------------------------------------------------
    // 단순한 값의 목록이 아니라, '화면 어디에(x, y) 그릴지' 정보를 포함한 객체 리스트

    // Y축 라벨 목록 (가격)
    // -> Point(고정된 x, 계산된 y)를 가짐
    ArrayList<AxisLabel> yAxisLabels;

    // X축 라벨 목록 (시간)
    // -> Point(계산된 x, 고정된 y)를 가짐
    ArrayList<AxisLabel> xAxisLabels;
}

