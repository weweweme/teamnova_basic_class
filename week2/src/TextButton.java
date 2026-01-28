import java.awt.*;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class TextButton {
    // Group A. 텍스트 관련 데이터
    // ---------------------------------------------------------
    String label;       // 내용 (내부적으로 byte[] 배열로 관리)

    // 폰트 정보 (내부적으로 폰트 이름 String, 스타일 int, 크기 int 등을 관리)
    Font font;

    // 글자 색상 (내부적으로 int 변수 하나에 ARGB를 비트 단위로 관리)
    Color defaultColor; // 평소 색상 (예: 검정)
    Color hoverColor;   // 마우스 올렸을 때 색상 (예: 파랑)


    // Group B. 영역 데이터 (눈에 안 보이는 히트박스)
    // ---------------------------------------------------------
    // 버튼의 시작 위치 (내부적으로 int x, y를 관리)
    Point position;

    // 클릭 가능한 범위 (배경 그림은 없지만, 클릭 판정을 위해 width, height는 필수)
    Dimension touchArea;


    // Group C. 행동
    // ---------------------------------------------------------
    // 클릭 시 발생한 사건 정보(시간 long, 키 조합 int 등)가 담긴
    // ActionEvent 객체를 받아 처리하는 리스너 인터페이스
    ActionListener onClick;
}

