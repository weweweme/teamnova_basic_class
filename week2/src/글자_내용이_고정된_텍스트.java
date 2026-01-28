public class 글자_내용이_고정된_텍스트 {


    // 1. 글자 데이터
    char[] textContent;

    // 2. 레이아웃 데이터
    // 화면 중앙(0,0)을 기준으로 한 상대 좌표
    // X축: 오른쪽(+), 왼쪽(-)
    // Y축: 아래쪽(+), 위쪽(-) <= 웹 표준 좌표계 https://www.w3.org/TR/css-transforms-1/
    short offsetX;
    short offsetY;

    // 렌더링 순서
    // 숫자가 커질수록 위에 그려진다
    byte zOrder;

    // 정렬 데이터 (비트마스크)
    // 상위 4비트: 세로 (16:Top, 32:Middle, 64:Bottom)
    // 하위 4비트: 가로 (1:Left, 2:Center, 4:Right)
    // 예: 우측 하단 = 4 + 64 = 68
    byte alignment;

    // 3. 스타일 데이터
    // W3C 스펙상 폰트 크기는 양의 숫자라면 무한대까지 가능
    // https://www.w3.org/TR/css-fonts-4/#propdef-font-size
    float fontSize;

    // 폰트 테이블 ID를 참조할 수 있게 byte로 관리 (0: Noto, 1: Arial...)
    byte fontIndex;

    // 비트마스크로 스타일 정보 관리
    // Bold, Italic, Underline 여부를 각각 boolean으로 만들면 낭비
    byte textStyle;

    // RGBA 값을 담기 색상 데이터
    int colorHex;




}
