import java.awt.*;
import java.awt.event.ActionListener;

public class ImageButton {
    // Group A. 텍스트 관련 데이터
    // ---------------------------------------------------------
    String label;       // 내용 (내부적으로 byte[] 배열로 관리)

    // 폰트 정보 (내부적으로 폰트 이름 String, 스타일 int, 크기 int 등을 관리)
    Font font;

    // 글자 색상 (내부적으로 int 변수 하나에 ARGB를 비트 단위로 묶어서 관리)
    Color fontColor;

    // 글자 위치 (버튼 내부에서의 상대 좌표 x, y를 int로 관리)
    Point textPosition;


    // Group B. 배경 및 형태 데이터
    // ---------------------------------------------------------
    // 버튼 배경 이미지 (이미지 파일의 원본 byte[] 데이터)
    byte[] backgroundImage;

    // 버튼 자체의 화면상 위치 (내부적으로 int x, y를 관리)
    Point buttonPosition;

    // 버튼의 가로/세로 크기 (내부적으로 int width, height를 관리)
    Dimension size;


    // Group C. 행동
    // ---------------------------------------------------------
    // 클릭 시 발생한 사건 정보(시간 long, 키 조합 int, 명령어 String 등)가 담긴
    // ActionEvent 객체를 인자로 받아 처리하는 리스너 인터페이스
    ActionListener onClick;
}



