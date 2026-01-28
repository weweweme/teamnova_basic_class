public class 관심목록_대시보드 {



    // 1. 상태 데이터
    // 현재 담긴 종목의 개수
    int userItemCount;

    // 팁 박스("3개 이상 추가하여...") 노출 여부를 체크하는 개수
    byte showTipBoxCount;

    // 관심목록 크기 제한
    final byte MAX_ITEM_COUNT = 50;


    // 2. 내 관심목록 데이터
    // [내 관심종목 들] - 종목 리스트 요소
    // [레이더 이미지, 각종 아이, 오른쪽 툴바] - 정적 이미지
    // [변경되지 않는 고정 텍스트들] - 일반 텍스트 사


    // 3. 추천 관심종목 데이터
    // [추천 종목] - 종목 리스트 요소


    // 4. 검색창
    // 사용자가 타이핑하는 검색 문자열
    // 입력 길이를 예측할 수 없으므로 String 사용
    String inputKeyword;

    // [추가할 종목 검색] - 일반 텍스트 사용
    // [돋보기 아이콘, 입력창 배경] - 정적 이미지

    // 검색창이 배치될 좌표와 크기
    short fieldX;
    short fieldY;
    short fieldWidth;
    short fieldHeight;


    // 5. 레이아웃 데이터
    short offsetX;
    short offsetY;
    short width;
    short height;
    byte layoutPriority;
    boolean isVisible;



}
