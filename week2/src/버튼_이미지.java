public class 버튼_이미지 {



    // 1. 배경 이미지 데이터
    // 평상시 모습
    int[] normalPixelBuffer;
    // 마우스 올렸을 때 모습
    int[] hoverPixelBuffer;

    // 이미지의 크기
    short width;
    short height;

    // 이미지의 렌더링 순서
    byte imageZOrder;


    // 2. 내부 텍스트 데이터
    // 배경 이미지 위에 얹어지는 글자
    char[] labelText;

    // 텍스트 속성
    int labelColor;
    float fontSize;
    byte fontIndex;
    byte alignment;
    byte textStyle;
    int colorHex;

    // 이미지 위에서의 텍스트의 위치
    short textOffsetX;
    short textOffsetY;

    // 텍스트의 렌더링 순서
    byte textZOrder;


    // 3. 레이아웃 데이터
    // 버튼 전체 덩어리의 화면상 위치
    short offsetX;
    short offsetY;









}
