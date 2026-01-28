public class 범위_슬라이더 {



    // 1. 슬라이더가 표현하는 데이터
    float minValue;     // 범위의 시작 (저가)
    float maxValue;     // 범위의 끝 (고가)
    float currentValue; // 현재 위치 (현재가)


    // 2. 슬라이더를 구성하는 이미지들
    // [마커 이미지]
    // 움직이는 주체. 작은 크기의 int[] 픽셀 배열
    int[] markerImage;
    short markerWidth;
    short markerHeight;
    byte markerZOrder;

    // 마커 좌표
    short markerCoordinateX;
    short markerCoordinateY;

    // [긴 막대 이미지]
    int[] rodImage;
    short rodWidth;
    short rodHeight;
    byte rodZOrder;


    // 3. 슬라이더 좌우에 붙는 텍스트들
    char[] titleTxt;
    char[] minTxt;
    char[] maxTxt;


    // 4. 레이아웃 데이터
    short offsetX;
    short offsetY;
    boolean isVisible;
    byte layoutPriority;




}
