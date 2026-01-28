public class 메인_차트 {

    // 1. 기본 구성 요소
    // 앞서 정의한 기본 요소들을 이용하여 구현된 메인 차트 UI

    // [배경 이미지] - 정적 이미지
    // [가격 및 시간 텍스트] - 일반 텍스트
    // [매수/매도 버튼] - 버튼 이미지
    // [차트 하단 1일/1주/1달/3달 등 글자] - 정적 이미지 + 일반 텍스트 + 등락 수치 텍스트
    // [좌상단 1/5/15/13/1H 등 글자] - 위와 동일
    // [좌상단 종목이름과 현재 가격, 등락률] - 위와 동일
    // [현재가 지표] - 정적 이미지(점선 이미지) + 정적 이미지(텍스트의 배경) + 등락 수치 텍스트


    // 2. 레이아웃 데이터
    // 메인 차트 UI의 차트의 위치와 크기
    short offsetX;
    short offsetY;
    short width;
    short height;
    boolean isVisible;
    byte layoutPriority;


    // 3. 캔들 데이터. 해당 데이터들을 사용하여 차트를 그린다
    final short CAPACITY = 100;
    long[] timestamps;    // 캔들 이미지가 배치될 x 좌표

    // [크기 조정 데이터]
    // 이 값들을 이용해 원본 이미지를 세로로 늘리거나 줄여서 렌더링함
    float[] openPrices;   // 시가
    float[] highPrices;   // 고가
    float[] lowPrices;    // 저가
    float[] closePrices;  // 종가

    // 기본 직사각형 픽셀 데이터
    // 가격 등락폭(Body Size)에 맞춰 세로 길이가 늘어남 (Stretch)
    int[] candleBoxImages;

    // 100개의 캔들 박스 각각에 적용될 색상 배열
    int[] candleBoxColors;

    // 캔들 하나의 고정 너비 (width / 100)
    float candleWidth;

    // 주가 등락폭에 따라
    // 캔들을 위아래로 늘리고 줄이는 Y축 스케일링 기준 가격
    float minDisplayPrice; // 화면 내 최저가 (바닥 기준)
    float maxDisplayPrice; // 화면 내 최고가 (천장 기준)

    // 캔들 스타일 데이터
    int bullColor; // 양봉 색상 데이터
    int bearColor; // 음봉 색상 데이터







}
