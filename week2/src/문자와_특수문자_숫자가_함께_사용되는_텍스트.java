public class 문자와_특수문자_숫자가_함께_사용되는_텍스트 {



    // 1. 핵심 데이터
    char[] textContent;

    // 주식 가격, 등락폭 등 실시간으로 변하는 숫자
    // 이 값에 따라 부호(+, -)와 색상(빨강, 파랑)이 결정된다
    float currentValue;

    // 소수점 몇째 자리까지 보여줄지 (예: 2 -> 0.00)
    byte decimalPlaces;


    // ==========================================
    // [Case 1] 단위(K, M)와 퍼센트(%)가 붙는 숫자 (-0.770, +21.7, +22.87K%)
    // ==========================================
    // 숫자 뒤에 붙일 단위 (0:없음, 1:K, 2:M, 3:B)
    byte unitSuffix;

    // 맨 뒤에 '%' 기호를 붙일지 여부
    boolean showPercentSymbol;

    // ==========================================
    // [Case 2] 괄호로 감싸진 숫자 (+0.08%)
    // ==========================================
    // 텍스트 전체를 괄호 '()'로 감쌀지 여부
    boolean useParentheses;


    // 그 외 공용으로 사용
    // 레이아웃 데이터
    short offsetX;
    short offsetY;
    byte zOrder;
    byte alignment;

    // 스타일 데이터
    float fontSize;
    byte fontIndex;
    byte textStyle;
    int colorHex;




}
