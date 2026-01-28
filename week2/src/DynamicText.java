import java.awt.*;
import java.time.LocalTime;

public class DynamicText {
    // 1. 가격 데이터
    double currentPrice;
    double basePrice;

    // 2. 시각적 속성
    // 내부적으로 byte[]로 문자열를 관리
    String value;
    // 내부적으로 int 변수 하나에 ARGB(투명도, 빨강, 초록, 파랑)를 비트 단위로 묶어서 관리
    Color color;
    // 내부적으로 int로 x, y를 관리
    Point position;

    // 3. 마지막 데이터 업데이트 시간
    // 내부적으로 byte로 시/분/초를 관리하고 nanoSecond만 int로 관리
    LocalTime lastUpdated;
}


