import java.awt.*;
import java.time.LocalTime;

public class StaticImage {
    // 1. 이미지 데이터
    byte[] rawData;

    // 2. 이미지가 가진 속성
    // 내부적으로 int로 x, y를 관리
    Point position;      // 위치 (x, y 내장)
    // 내부적으로 int로 width, height를 관리
    Dimension size;      // 크기 (width, height 내장)
}



