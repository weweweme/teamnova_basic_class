public class 구분선 {





    // 1. 기본 구성 요소
    // 앞서 정의한 기본 요소들을 이용하여 구현된 메인 차트 UI

    // [배경 이미지] - 정적 이미지
    // [가격 및 시간 텍스트] - 글자 내용이 고정된 텍스트
    // [매수/매도 버튼] - 버튼 이미지
    // [차트 하단 1일/1주/1달/3달 등 글자] - 정적 이미지 + 글자 내용이 고정된 텍스트 + 문자와 특수문자 숫자가 함께 사용되는 텍스트
    // [좌상단 1/5/15/13/1H 등 글자] - 위와 동일
    // [좌상단 종목이름과 현재 가격, 등락률] -


    // 1. 캔들 데이터
    final short CAPACITY = 100;
    long[] timestamps;    // size: 100
    float[] openPrices;   // size: 100
    float[] highPrices;   // size: 100
    float[] lowPrices;    // size: 100
    float[] closePrices;  // size: 100

    // 캔들 하나의 고정 너비 (width / 100)
    float candleWidth;

    // 스케일링 데이터 (Y-Axis Scaling)
    // 주가 등락폭에 따라
    // 캔들을 위아래로 늘리고 줄이는 'Y축 스케일링'은 필요함
    float minDisplayPrice; // 화면 내 최저가 (바닥 기준)
    float maxDisplayPrice; // 화면 내 최고가 (천장 기준)


    // 3. 레이아웃 데이터
    // 메인 차트 UI의 차트의 위치와 크기
    short offsetX;
    short offsetY;
    short width;
    short height;





}
