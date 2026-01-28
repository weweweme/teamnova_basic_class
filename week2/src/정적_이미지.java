public class 정적_이미지 {


    // 1. 이미지 데이터
    int[] imageData;

    short width;
    short height;


    // 2. 레이아웃 데이터 (Layout)
    short offsetX;
    short offsetY;
    byte zOrder;

    // 크기 제약 조건 (Size Constraints)
    // 이미지가 너무 작아지거나(식별 불가), 너무 커지는(레이아웃 파괴) 것을 방지
    // 0이면 제한 없음(Unlimited)으로 간주
    short minWidth;
    short minHeight;
    short maxWidth;
    short maxHeight;

    // [레이아웃 우선순위]
    // 화면이 좁아질 때 누구를 먼저 화면에서 치울지 결정하는 변수
    // 0: 필수 - 공간이 부족해도 어떻게든 구겨 넣거나 스크롤을 만들어서라도 보여줌 (예: 현재가, 메인 차트)
    // 1: 보통 - 필수 요소가 다 그려지고 자리가 남으면 그림 (예: 미니 지표)
    // 2: 장식 - 자리가 조금이라도 부족하면 바로 비활성화 (예: 광고)
    byte layoutPriority;

    // 보이게 할지 말지 결정
    boolean isVisible;



}
