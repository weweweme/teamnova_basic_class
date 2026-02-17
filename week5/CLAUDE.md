# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 안내 문서입니다.

## 프로젝트 개요

팀노바 기초반 5주차 Java 학습 프로젝트입니다. IntelliJ IDEA를 사용합니다.

## 협업 방식

- **대화 기반 점진적 개발**: 한번에 전체 코드를 작성하지 않음
- **뼈대 먼저**: 프로그램 구조/설계를 먼저 대화로 확정
- **단계별 구현**: 설계 확정 후 기능 하나씩 구현하며 진행
- **매 단계 컨펌**: 각 단계마다 사용자 확인 후 다음 진행
- **커밋 메시지 제공**: 각 구현이 끝나면 커밋 메시지를 제공 (커밋은 사용자가 직접)

## 빌드 및 실행

IntelliJ IDEA의 **Terminal 탭**에서 실행합니다 (Run 콘솔 아님).

```bash
# 컴파일
javac -d out src/*.java

# 실행
java -cp out Main
```

**실행 환경**: 터미널 raw 모드 (`stty -icanon -echo`)
- 화살표 키 입력을 위해 터미널 raw 모드 사용
- 화면 클리어: ANSI 이스케이프 코드 (`\033[H\033[2J`)
- 프로그램 종료 시 자동으로 터미널 복원 (`stty sane`)

## 프로젝트 구조

- `src/` - Java 소스 파일 (진입점: `Main.java`)
- `out/` - 컴파일된 클래스 파일 (gitignore 처리됨)

## 개발 프로세스

1. **설계**: 구현 전 구조/로직 설계 및 컨펌
2. **구현**: 점진적으로 코드 작성, 각 단계마다 컨펌
3. **검증**: 조건에 맞는지 테스트 및 확인

## 개발 규칙

- **점진적 커밋**: 모든 커밋은 사용자 컨펌 후 진행 (커밋은 사용자가 직접, 메시지만 제공)
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
- **메서드 주석 스타일**: 메서드 설명은 `/// <summary>` 형식 사용
  ```java
  /// <summary>
  /// 메서드 설명
  /// </summary>
  void someMethod() {
      // ...
  }
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
- **Cell 접근 일관성**: 칸의 상태를 확인할 때는 항상 Cell의 메서드(`isEmpty()`, `hasPiece()`, `getPiece()`)를 사용
  ```java
  // 나쁜 예: Board의 래퍼 메서드로 null 반환에 의존
  Piece piece = board.getPiece(row, col);
  if (piece == null) { ... }

  // 좋은 예: Cell의 상태 확인 메서드를 직접 사용
  if (board.grid[row][col].isEmpty()) { ... }
  Piece piece = board.grid[row][col].getPiece();
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

## 5주차 과제

**목표**: 상속(abstract class, 메서드 오버라이딩, 다형성)을 활용한 프로그램 제작

**과제 내용**: 콘솔 체스 게임
- 기물마다 이동 규칙이 달라 상속을 자연스럽게 활용
- 2인 대전 + AI 대전 모드
- 기본 규칙 먼저 → 특수 규칙 단계적 추가

### 상속 구조

**1차 상속: Piece 계층 (기물)**

```
Piece (abstract)
 ├── King      (1칸 전방향)
 ├── Queen     (직선+대각선 무제한 - slideMoves 8방향)
 ├── Rook      (직선 4방향 - slideMoves)
 ├── Bishop    (대각선 4방향 - slideMoves)
 ├── Knight    (L자 이동 - slideMoves 미사용)
 └── Pawn      (전진, 첫 이동 2칸, 대각선 잡기)
```

- `Piece`에 추상 메서드 `getValidMoves(Piece[][] board)` 선언
- 각 하위 클래스가 오버라이딩하여 자기만의 이동 로직 구현
- `slideMoves()`: Piece에 protected 헬퍼 메서드로 직선/대각선 이동 공통 로직

**2차 상속: Player 계층**

```
Player (abstract)
 ├── HumanPlayer   (사용자 입력으로 수 선택)
 └── AiPlayer      (알고리즘으로 수 선택)
```

- `Player`에 추상 메서드 `chooseMove(Board board)` 선언
- `Game`이 `currentPlayer.chooseMove()` 호출 → 다형성

### 클래스 구조

| 파일 | 역할 |
|------|------|
| `Main` | 타이틀 화면, 모드 선택 (2인/AI), 게임 시작 |
| `Game` | 게임 루프, 턴 관리, 체크/체크메이트 판정 |
| `Board` | 8x8 격자, 기물 배치, 이동 실행, 보드 출력, 체크 판정 |
| `Piece` | **추상 클래스** - 기물 공통 필드/메서드, abstract getValidMoves() |
| `King` | 킹 - 1칸 전방향 (+ 캐슬링) |
| `Queen` | 퀸 - 8방향 slideMoves |
| `Rook` | 룩 - 상하좌우 slideMoves |
| `Bishop` | 비숍 - 대각선 slideMoves |
| `Knight` | 나이트 - L자 8칸 |
| `Pawn` | 폰 - 전진, 대각선 잡기 (+ 프로모션, 앙파상) |
| `Move` | 이동 데이터 (출발/도착 좌표) |
| `Player` | **추상 클래스** - 플레이어 공통, abstract chooseMove() |
| `HumanPlayer` | 사람 플레이어 - 콘솔 입력 |
| `AiPlayer` | AI 플레이어 - 우선순위 기반 전략 |
| `Util` | 유틸리티 (입력, 좌표 변환, 랜덤, 딜레이) |

### 좌표 체계

- 내부: `row 0` = 8번 줄(흑), `row 7` = 1번 줄(백), `col 0` = a열
- 변환: `"e2"` → `row=6, col=4` / `row=6, col=4` → `"e2"`

### AI 전략

1. 체크메이트 가능한 수 → 즉시 선택
2. 상대 기물 잡기 (퀸9 > 룩5 > 비숍3 = 나이트3 > 폰1)
3. 체크를 거는 수
4. 랜덤 선택

### 구현 순서

1. **Phase 1**: 기초 뼈대 (Main, Util, Move)
2. **Phase 2**: 기물 상속 계층 (Piece + 6개 하위 클래스)
3. **Phase 3**: 체스판 (Board)
4. **Phase 4**: 플레이어 + 게임 루프 (Player, HumanPlayer, Game)
5. **Phase 5**: AI 플레이어 (AiPlayer)
6. **Phase 6**: 특수 규칙 (프로모션 → 캐슬링 → 앙파상 → 스테일메이트)
7. **Phase 7**: 마무리 (하이라이트, 잡은 기물, AI 딜레이)

### UI

**기물 표기**: 이니셜 사용 (체스 표준 표기법)
- K(King), Q(Queen), R(Rook), B(Bishop), N(Knight), P(Pawn)
- 대문자 = 빨간팀, 소문자 = 파란팀

**색상**: ANSI 이스케이프 코드로 콘솔 색상 적용
- 빨간팀: `\033[31m` (RED)
- 파란팀: `\033[34m` (BLUE)
- 초기화: `\033[0m` (RESET)

**체스판**:

```
     a   b   c   d   e   f   g   h
   +---+---+---+---+---+---+---+---+
 8 | r | n | b | q | k | b | n | r | 8    ← 파란팀 (소문자, 파란색)
   +---+---+---+---+---+---+---+---+
 7 | p | p | p | p | p | p | p | p | 7
   +---+---+---+---+---+---+---+---+
 6 |   |   |   |   |   |   |   |   | 6
   +---+---+---+---+---+---+---+---+
 5 |   |   |   |   |   |   |   |   | 5
   +---+---+---+---+---+---+---+---+
 4 |   |   |   |   |   |   |   |   | 4
   +---+---+---+---+---+---+---+---+
 3 |   |   |   |   |   |   |   |   | 3
   +---+---+---+---+---+---+---+---+
 2 | P | P | P | P | P | P | P | P | 2
   +---+---+---+---+---+---+---+---+
 1 | R | N | B | Q | K | B | N | R | 1    ← 빨간팀 (대문자, 빨간색)
   +---+---+---+---+---+---+---+---+
     a   b   c   d   e   f   g   h
```

- 이동 가능한 칸: `·` 표시
- 선택된 기물: `[K]` 형태로 표시

**조작 방식**: 터미널 (화살표 키)
- 화살표 키 → 커서 즉시 이동 (Enter 불필요)
- Enter → 기물 선택 / 이동 확정
- `q` → 취소 / 뒤로가기
- ANSI 이스케이프 코드로 화면 즉시 갱신
