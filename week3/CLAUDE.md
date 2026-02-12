# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 안내 문서입니다.

## 프로젝트 개요

팀노바 기초반 3주차 Java 학습 프로젝트입니다. IntelliJ IDEA를 사용합니다.

## 빌드 및 실행

IntelliJ IDEA의 내장 컴파일러를 사용합니다. 커맨드라인에서 실행하려면:

```bash
# 컴파일
javac -d out src/*.java

# 실행
java -cp out Main
```

## 프로젝트 구조

- `src/` - Java 소스 파일 (진입점: `Main.java`)
- `out/` - 컴파일된 클래스 파일 (gitignore 처리됨)

## 협업 방식

- **대화 기반 점진적 개발**: 한번에 전체 코드를 작성하지 않음
- **뼈대 먼저**: 프로그램 구조/설계를 먼저 대화로 확정
- **단계별 구현**: 설계 확정 후 기능 하나씩 구현하며 진행
- **매 단계 컨펌**: 각 단계마다 사용자 확인 후 다음 진행

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

## 과제 구조

메인 메뉴에서 1~4번으로 각 과제 진입, 0번으로 메뉴 복귀, 그 외 입력시 종료

1. 마름모 그리기 (print/printf 사용)
2. 원 그리기
3. 별 그리기
4. 보너스: 유사 스도쿠 (3x3, 0/1/2 중복 없이 배치)
