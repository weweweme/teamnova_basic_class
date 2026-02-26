# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 안내 문서입니다.

## 프로젝트 개요

팀노바 기초반 6주차 Java 학습 프로젝트입니다. IntelliJ IDEA를 사용합니다.

## 협업 방식

- **대화 기반 점진적 개발**: 한번에 전체 코드를 작성하지 않음
- **뼈대 먼저**: 프로그램 구조/설계를 먼저 대화로 확정
- **단계별 구현**: 설계 확정 후 기능 하나씩 구현하며 진행
- **매 단계 컨펌**: 각 단계마다 사용자 확인 후 다음 진행
- **커밋 메시지 제공**: 각 구현이 끝나면 커밋 메시지를 제공 (커밋은 사용자가 직접)
- **최소 커밋 단위**: 구현을 최대한 작게 나누어 최소한의 커밋 단위로 진행. 하나의 커밋에 여러 기능을 섞지 않는다
- **테스트 명령어 제공**: 구현 완료 시 항상 컴파일+실행 명령어를 함께 제공

## 빌드 및 실행

IntelliJ IDEA의 **Terminal 탭**에서 실행합니다 (Run 콘솔 아님).

```bash
# 컴파일
javac -d out $(find src -name "*.java")

# 실행
java -cp out Main
```

**실행 환경**: 터미널 raw 모드 (`stty -icanon -echo`)
- 화살표 키 입력을 위해 터미널 raw 모드 사용
- 화면 클리어: ANSI 이스케이프 코드 (`\033[H\033[2J`)
- 프로그램 종료 시 자동으로 터미널 복원 (`stty sane`)

## 개발 프로세스

1. **설계**: 구현 전 구조/로직 설계 및 컨펌
2. **구현**: 점진적으로 코드 작성, 각 단계마다 컨펌
3. **검증**: 조건에 맞는지 테스트 및 확인

## 용어 및 설계 결정

- **Supply**: 식민지 전체의 자원/물품 보유량을 관리하는 클래스 (인벤토리 대신 사용)
- **저장소**: 건물 이름. 건설하면 Supply 용량 증가 + 추가 물품 보관 가능

## 개발 규칙

- **점진적 커밋**: 모든 커밋은 사용자 컨펌 후 진행 (커밋은 사용자가 직접, 메시지만 제공)
- **상수는 해당 클래스에**: 상수 전용 클래스(Config 등)를 만들지 않고, 각 상수를 해당 개념을 소유하는 클래스에 둔다. static은 최대한 사용하지 않는다
  ```java
  // 나쁜 예: 중앙 집중 상수 클래스
  public class Config {
      public static final int MAP_WIDTH = 60;
      public static final int COLONIST_MAX_HP = 100;
  }

  // 좋은 예: 각 클래스가 자기 상수를 소유
  public class GameMap {
      public static final int WIDTH = 60;
  }
  public class Colonist {
      private static final int MAX_HP = 100;
  }
  ```
- **주석은 현재 모듈만 설명**: 주석은 "이 코드가 무엇을 하는지"만 담백하게 기술. 설계 경위(왜 이렇게 바꿨는지), 패턴 이름(조합 패턴, 팩토리 패턴 등), 리팩토링 히스토리는 주석에 남기지 않는다
- **주석 필수**: 변수명, 로직 전개, 타입 선택에 대한 근거를 주석으로 작성
- **단계별 진행**: 기능 하나씩 구현 후 확인
- **변수 선언**: 쉼표로 구분하지 말고 각 변수를 개별 라인에 선언
  ```java
  // 나쁜 예
  int a = 1, b = 2, c = 3;

  // 좋은 예
  int a = 1;
  int b = 2;
  int c = 3;
  ```
- **복합 조건은 boolean 변수로 분리**: if 조건에 `&&`/`||`가 2개 이상이면 각 조건을 의미 있는 이름의 boolean 지역변수로 분리
  ```java
  // 나쁜 예: 한 줄에 조건이 너무 많음
  if (piece instanceof Pawn && move.fromCol != move.toCol && grid[move.toRow][move.toCol].isEmpty()) {

  // 좋은 예: 각 조건에 이름을 부여
  boolean isPawn = piece instanceof Pawn;
  boolean movedDiagonally = move.fromCol != move.toCol;
  boolean destEmpty = grid[move.toRow][move.toCol].isEmpty();
  boolean isEnPassant = isPawn && movedDiagonally && destEmpty;

  if (isEnPassant) {
  ```
- **메뉴 번호 변수 관리**: 메뉴 출력 번호와 입력 체크 번호를 변수로 통일 관리하여 불일치 방지
  ```java
  // 나쁜 예: 번호가 따로 하드코딩되어 불일치 가능
  System.out.println("[1] 옵션A");
  System.out.println("[2] 옵션B");
  if (key == 1) { ... }
  if (key == 2) { ... }

  // 좋은 예: 변수로 관리하여 불일치 방지
  final int KEY_A = 1;
  final int KEY_B = 2;
  System.out.println("[" + KEY_A + "] 옵션A");
  System.out.println("[" + KEY_B + "] 옵션B");
  if (key == KEY_A) { ... }
  if (key == KEY_B) { ... }
  ```
- **조건문 선택 기준 (if-else vs switch)**:
  - `if-else`: 복합 조건 (범위 비교, AND/OR 조합, null 체크, 객체 비교)
  - `switch`: 단일 값으로 여러 분기 처리 (메뉴 선택, 열거형 등)
  ```java
  // if-else 사용: 범위/복합 조건
  if (score >= 90) {
      grade = "A";
  } else if (score >= 80) {
      grade = "B";
  }

  // switch 사용: 단일 값 → 여러 분기
  switch (choice) {
      case 1:
          goWholesaler();
          break;
      case 2:
          startBusiness();
          break;
  }
  ```
- **접근한정자 필수**: 모든 필드와 메서드에 적절한 접근한정자를 반드시 명시 (package-private 금지)
  - `private`: 클래스 내부에서만 사용하는 필드/메서드 (기본값으로 사용)
  - `public`: 외부 클래스에서 접근해야 하는 필드/메서드
  - 접근한정자 없이 선언하지 않는다 (Java의 package-private는 의도가 불명확)
  ```java
  // 나쁜 예: 접근한정자 생략 (package-private)
  int money;
  void startBusiness() { ... }

  // 좋은 예: 의도를 명확히
  private int money;
  public void startBusiness() { ... }
  ```
- **주석은 누구나 이해할 수 있게**: 프로그래밍 전문 용어(0-based, 1-based, nullable 등) 대신 누구나 알 수 있는 한국어로 작성
  ```java
  // 나쁜 예: 전문 용어 사용
  /// 번호(1-based)로 상품 찾기
  /// 유효하지 않은 번호면 null 반환

  // 좋은 예: 누구나 이해 가능
  /// 사용자가 입력한 번호(1번부터 시작)로 상품 찾기
  /// 존재하지 않는 번호면 null 반환
  ```
- **`/// <summary>` 주석 필수**: 메서드와 필드 모두 `/// <summary>` 형식으로 설명 작성 (IDE에서 마우스 커서를 올리면 바로 확인 가능)
  ```java
  /// <summary>
  /// 메서드 설명
  /// </summary>
  void someMethod() {
      // ...
  }

  /// <summary>
  /// 필드 설명
  /// </summary>
  private final ArrayList<Move> allMoves = new ArrayList<>();
  ```
- **try-catch 사용 원칙**: 실제 예외가 발생할 수 있는 상황에서만 사용
  - **사용하지 말 것**: 단순 입력 검증 (숫자 파싱 등) → `hasNextInt()` 같은 검증 메서드 활용
  - **불가피한 경우**: Java checked exception (Thread.sleep, System.in 등)
    - 컴파일러 요구사항임을 주석으로 명시
  ```java
  // 나쁜 예: 입력 검증에 try-catch 사용
  try {
      int num = Integer.parseInt(input);
  } catch (NumberFormatException e) {
      num = 0;
  }

  // 좋은 예: 검증 메서드 사용
  if (scanner.hasNextInt()) {
      int num = scanner.nextInt();
  } else {
      scanner.next();  // 잘못된 입력 소비
      int num = 0;
  }

  // 불가피한 경우: checked exception (주석 필수)
  // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
  try {
      Thread.sleep(ms);
  } catch (InterruptedException e) {
      // 단일 스레드 앱에서는 발생하지 않음 (컴파일러 요구사항)
  }
  ```
- **Tester-Doer 패턴**: 객체를 가져와서 null 체크하는 대신, 먼저 존재 여부를 확인하고 필요할 때만 가져오기
  ```java
  // 나쁜 예: 가져온 후 null 체크
  Piece piece = cell.getPiece();
  if (piece != null) {
      // piece 사용
  }

  // 좋은 예: 존재 여부 먼저 확인 (Tester), 있을 때만 가져오기 (Doer)
  if (cell.hasPiece()) {
      Piece piece = cell.getPiece();
      // piece 사용
  }

  // 없는 경우를 먼저 걸러내는 것도 동일한 패턴
  if (cell.isEmpty()) {
      return;
  }
  Piece piece = cell.getPiece();
  ```
- **null 대신 의미 있는 메서드 사용**: `setter(null)`로 제거하지 말고 전용 remove 메서드 사용
  ```java
  // 나쁜 예: null이 "제거"를 의미하는지 알 수 없음
  cell.setPiece(null);
  cell.setItem(null);

  // 좋은 예: 의도가 명확한 전용 메서드
  cell.removePiece();
  cell.removeItem();
  ```
