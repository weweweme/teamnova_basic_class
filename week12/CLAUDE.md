# Project: week8 (Android 학습 과제)

## 사용자 프로필
- Unity 클라이언트 프로그래머 2년차
- Android 개발환경 초보자
- 설명 시 Unity/C# 개념에 비유하면 이해가 빠름
- 한국어로 소통

## 코딩 스타일
- ViewBinding 사용
- 모든 Java/XML 파일에 Unity 비유 포함 상세 주석 작성

## 협업 방식

- **대화 기반 점진적 개발**: 한번에 전체 코드를 작성하지 않음
- **뼈대 먼저**: 프로그램 구조/설계를 먼저 대화로 확정
- **단계별 구현**: 설계 확정 후 기능 하나씩 구현하며 진행
- **매 단계 컨펌**: 각 단계마다 사용자 확인 후 다음 진행
- **커밋 메시지 제공**: 각 구현이 끝나면 커밋 메시지를 제공 (커밋은 사용자가 직접)
- **최소 커밋 단위**: 구현을 최대한 작게 나누어 최소한의 커밋 단위로 진행. 하나의 커밋에 여러 기능을 섞지 않는다

## 개발 규칙

- **점진적 커밋**: 모든 커밋은 사용자 컨펌 후 진행 (커밋은 사용자가 직접, 메시지만 제공)
- **클래스/인터페이스는 별도 파일로 분리**: 한 파일에 하나의 public 클래스/인터페이스만 둔다. inner class, static nested class, 보조 인터페이스도 가능한 한 별도 파일로 빼는 것을 우선. 파일 단위 탐색·재사용·리뷰가 쉬워짐. (예: Adapter / ViewHolder / 클릭 콜백 인터페이스는 셋 다 별도 파일)
  ```java
  // 나쁜 예: 한 파일에 어댑터·뷰홀더·콜백 인터페이스가 다 들어감
  public class GameCardAdapter ... {
      public static class GameCardViewHolder ... { }
      public interface OnGameClickListener { ... }
  }

  // 좋은 예: 파일 단위로 분리
  // GameCardAdapter.java
  // GameCardViewHolder.java
  // OnGameClickListener.java
  ```
- **상수는 해당 클래스에**: 상수 전용 클래스(Config 등)를 만들지 않고, 각 상수를 해당 개념을 소유하는 클래스에 둔다
  ```java
  // 나쁜 예: 중앙 집중 상수 클래스
  public class Config {
      public static final int MAP_WIDTH = 60;
      public static final int COLONIST_MAX_HP = 100;
  }

  // 좋은 예: 각 클래스가 자기 상수를 소유
  public class GameWorld {
      public static final int WIDTH = 60;
  }
  public class Colonist {
      private static final int MAX_HP = 100;
  }
  ```
- **static 사용 기준**: `static`은 "꼭 필요할 때만" 쓴다 — 즉 **인스턴스 없이 / 클래스 차원에서 공유**해야 할 때
  - **공유 상수**(다른 클래스에서 인스턴스 없이 참조): `public static final` 로 승격
    - 예: `AccountManager.FILE_USER_PREFIX`("user_"), `UserPrefs.DEFAULT_AVATAR_COLOR`
    - 한 클래스 안에서만 쓰는 상수는 `private`로 닫는다 (공개는 필요할 때만)
  - **인스턴스가 하나뿐인 클래스**(App이 보유하는 매니저 등)의 내부 전용 상수: `static`을 굳이 안 붙이고 인스턴스 `final`로 둬도 된다 — 인스턴스가 1개라 복제 비용이 없음 (`static final`도 무방, 차이 없음)
    - 예: `AccountManager`의 `KEY_NICKNAME`, `KEY_ACCOUNT_IDS` 등 → `private final`
  - **인스턴스가 여러 개 생기는 클래스**의 고정값: 반드시 `static final` (안 그러면 객체마다 같은 값이 중복 저장돼 메모리 낭비)
  - `final` 인스턴스 필드: 생성자에서 인스턴스마다 다른 값을 받는 경우에 사용
  - 가변 `static` 필드: **금지** — 해당 데이터를 관리하는 객체의 인스턴스 필드로 이동
  - `static` 메서드: 유틸리티 헬퍼나 팩토리 메서드에만 사용. 그 외에는 인스턴스 메서드 사용
  ```java
  // 좋은 예: 다른 클래스에서 인스턴스 없이 참조하는 공유 상수
  public static final String FILE_USER_PREFIX = "user_";

  // 좋은 예: 인스턴스가 하나뿐인 싱글톤의 내부 전용 상수 (static 생략 OK)
  private final String KEY_NICKNAME = "nickname";

  // 좋은 예: 인스턴스마다 다른 값 (생성자에서 받음)
  private final String name;

  // 나쁜 예: '인스턴스가 여러 개 생기는' 클래스에서 고정값을 인스턴스 필드로 (객체마다 중복 = 메모리 낭비)
  private final int SHOOT_COL = 12;   // → private static final 로

  // 나쁜 예: 가변 static 필드
  private static int count = 0;
  ```
- **열거형 데이터 기준**: 종류가 적고(5개 이하) 필드도 적은(5개 이하) 열거형은 데이터를 직접 포함. 종류가 많거나 필드가 많으면 Spec/Factory로 분리
  ```java
  // 좋은 예: 3종 × 4필드 → 열거형에 직접 포함
  public enum Difficulty {
      EASY("쉬움", 0.7, 1.5, 7),
      NORMAL("보통", 1.0, 1.0, 10),
      HARD("어려움", 1.3, 0.7, 15);
      private final String displayName;
      private final double enemyMultiplier;
      // ...
  }

  // 좋은 예: 12종 × 8필드 → Spec/Factory 분리
  public enum EnemyType { WOLF, SPIDER, ... }
  public class EnemySpec { /* 데이터 필드 */ }
  public class EnemyFactory { /* Type → Spec 매핑 */ }
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
- **모든 설명은 중학생도 이해할 수 있게**: 코드 주석뿐 아니라 문서·발표 자료·설명 텍스트 등 모든 산출물에 적용. 프로그래밍 전문 용어(0-based, 1-based, nullable 등) 대신 누구나 알 수 있는 한국어로 작성. API나 개념을 설명할 때 "~로 변환"처럼 추상적으로 쓰지 말고, 왜 변환이 필요한지 비유나 예시로 풀어서 설명
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
- **고정값은 부모 생성자 파라미터로 전달**: 서브클래스마다 같은 고정값을 상수로 두고 setter나 abstract 메서드로 부모에 전달하지 않는다. 부모 생성자 파라미터로 직접 넘기고, 진짜 다른 동작만 abstract로 남긴다
  ```java
  // 나쁜 예: 서브클래스가 상수를 들고 setter로 전달
  public abstract class Structure {
      protected Structure(int column) { this.column = column; }
      protected void setMaxHp(int maxHp) { this.maxHp = maxHp; }
  }
  public class Spike extends Structure {
      private static final int MAX_HP = 10;
      public Spike(int column) {
          super(column);
          setMaxHp(MAX_HP);
      }
  }

  // 좋은 예: 부모 생성자에서 한번에 설정
  public abstract class Structure {
      protected Structure(int column, int maxHp) {
          this.column = column;
          this.maxHp = maxHp;
      }
  }
  public class Spike extends Structure {
      public Spike(int column) {
          super(column, 10);
      }
  }
  ```
- **필수 의존성은 생성자로 주입**: 객체가 동작하는 데 반드시 필요한 의존성은 setter가 아닌 생성자 파라미터로 받는다. 객체는 생성 직후부터 사용 가능한 상태여야 한다
  ```java
  // 나쁜 예: setter로 나중에 주입 (호출 전까지 null 위험)
  GameWorld gameWorld = new GameWorld();
  gameWorld.setSfxPlayer(sfxPlayer);

  // 좋은 예: 생성자에서 받아서 즉시 사용 가능
  GameWorld gameWorld = new GameWorld(sfxPlayer);
  ```
- **반복 호출되는 메서드에서 컬렉션/배열 재생성 금지**: 매 프레임·매 틱 호출되는 메서드에서 `new ArrayList`, `new int[]` 등을 매번 생성하지 않는다. 클래스 필드로 선언하고 `clear()` / `Arrays.fill()`로 재사용
  ```java
  // 나쁜 예: 매 프레임마다 새 리스트 생성
  private ArrayList<String> buildPanel() {
      ArrayList<String> lines = new ArrayList<>();
      lines.add("...");
      return lines;
  }

  // 좋은 예: 필드로 재사용
  private final ArrayList<String> panelLines = new ArrayList<>();
  private void buildPanel() {
      panelLines.clear();
      panelLines.add("...");
  }
  ```
  단, **재사용해도 되는 건 "메서드 밖으로 나가지 않는 임시 버퍼"뿐이다.** 아래 경우엔 재사용(공유 필드+clear)하지 말고 매번 새로 만든다 — 안 그러면 데이터가 지워지는 버그가 난다:
  - 컬렉션을 **return하거나 콜백·다른 객체에 넘겨서** 그쪽이 계속 들고 있는 경우
    → 다음 호출에서 clear하면 상대가 들고 있던 내용까지 같이 지워짐 (예: 화면이 표시 중인 목록이 사라짐)
  - **여러 스레드가 동시에** 부를 수 있는 메서드 → 서로의 내용을 덮어써서 뒤죽박죽이 됨
  - 애초에 **매 프레임·매 틱이 아니라 가끔** 불리는 메서드 → 재사용 이득이 거의 없음. 그냥 지역 변수가 더 안전하고 읽기 쉬움
  ```java
  // 재사용 금지: 결과가 밖으로 나감(return → 콜백 → 화면) + 서브 스레드에서 돎 → 매번 새 리스트가 정답
  private List<RawgGame> parseResults(String json) {
      List<RawgGame> list = new ArrayList<>();  // 공유 필드로 만들면 다음 검색 때 화면 목록이 지워짐
      // ... 채우기 ...
      return list;
  }
  ```

## 학습 방식
- 가이드를 주면 사용자가 직접 구현하며 익히는 방식
- 주석을 꼼꼼히 달면서 이해 내용 정리
- ★ 테스트 가이드: 속성을 바꿔보고 결과를 관찰

## 최종 작품: 게임 다이어리 앱 (가칭)

### 컨셉
- **"게임판 Letterboxd"** - 개인 게임 다이어리
- Backloggd(https://backloggd.com/) 벤치마킹, **개인 기록을 중심에 두되 소셜은 감초처럼 가볍게** 곁들임
- Android 네이티브 앱 (기존 서비스들은 대부분 웹 only)

### 핵심 기능 3가지
1. **내 게임 다이어리 관리** - Steam Web API로 자동 임포트 + RAWG API로 수동 검색/추가 (기록 중심)
2. **위시리스트 관리** - 하고 싶은 게임 관리 (RAWG 검색, 멀티플랫폼)
3. **한줄평 + 별점** - 게임마다 별점(0.5~5.0) + 짧은 감상 기록 (개인 기록이 핵심, 커뮤니티는 보조)

### 사용 API
- **Steam Web API** (무료): `IPlayerService/GetOwnedGames` - 소유 게임 + 플레이타임 자동 임포트
- **RAWG API** (무료): 50만개+ 게임 DB - 메타데이터(표지, 장르, 평점, 플랫폼, 출시일)

### Backloggd 대비 차별점
- Android 네이티브 (Backloggd는 웹 only)
- Steam 자동 임포트 (Backloggd는 수동 추가만)
- "나를 위한 게임 일지"가 중심 · 팔로잉 피드·랭킹은 별도 커뮤니티 탭에 가볍게 (소셜은 감초)

### 전체 화면 맵 (Activity 12개 + Fragment 4개 = 16개 화면)

#### 진입 (2개)
| # | 화면 | 타입 | 설명 |
|---|------|------|------|
| 1 | SplashActivity | Activity | 앱 로고 + 로딩. 온보딩 여부 판단 |
| 2 | OnboardingActivity | Activity | 최초 실행 시 앱 소개 (ViewPager 슬라이드 3장) |

#### 메인 (BottomNavigation + 4 Fragment)
| # | 화면 | 타입 | 설명 |
|---|------|------|------|
| 3 | MainActivity | Activity | BottomNavigationView 컨테이너 |
| 4 | HomeFragment | Fragment | 최근 활동, 이어하기(플레이중), 위시리스트 픽업 |
| 5 | ExploreFragment | Fragment | RAWG 검색, 장르 칩 필터, 인기/신작 탭 |
| 6 | LibraryFragment | Fragment | 내 컬렉션 전체. 상태별 필터 탭 (전체/플레이중/완료/중단/백로그) |
| 7 | ProfileFragment | Fragment | 내 통계 대시보드, Steam 연동 상태, 설정 진입 |

#### 게임 관련 (4개)
| # | 화면 | 타입 | 설명 |
|---|------|------|------|
| 8 | GameDetailActivity | Activity | 게임 상세 (표지, 메타데이터, 내 리뷰, 플레이 로그) |
| 9 | ReviewWriteActivity | Activity | 별점 + 한줄평 작성/수정 |
| 10 | PlayLogActivity | Activity | 플레이 세션 기록 (날짜, 시간, 메모) |
| 11 | ScreenshotActivity | Activity | 게임별 스크린샷 갤러리 (카메라/갤러리에서 추가) |

#### 위시리스트 (1개)
| # | 화면 | 타입 | 설명 |
|---|------|------|------|
| 12 | WishlistActivity | Activity | 위시리스트 전체 보기 + 우선순위 정렬 |

#### 통계 (1개)
| # | 화면 | 타입 | 설명 |
|---|------|------|------|
| 13 | StatsActivity | Activity | 상세 통계 (장르 분포 차트, 월별 플레이 그래프, 평점 분포) |

#### 설정 계열 (5개)
| # | 화면 | 타입 | 설명 |
|---|------|------|------|
| 14 | SettingsActivity | Activity | 설정 메인 (항목 리스트) |
| 15 | SteamLinkActivity | Activity | Steam ID 입력 + 연동/해제 + 임포트 진행 |
| 16 | ThemeSettingActivity | Activity | 다크/라이트/시스템 테마 선택 |
| 17 | DataManageActivity | Activity | 백업/복원(JSON export), 캐시 삭제, 데이터 초기화 |
| 18 | AboutActivity | Activity | 앱 버전, 오픈소스 라이선스, 사용 API 크레딧(RAWG 필수) |

### DB 테이블 구조
```
Game (내 컬렉션)
├── id, rawgId, steamAppId
├── title, coverUrl, genre, platform, releaseDate
├── status (PLAYING/COMPLETED/DROPPED/BACKLOG)
├── rating (0.5~5.0), review (한줄평)
├── totalPlayTime, addedAt, updatedAt

PlayLog (플레이 세션)
├── id, gameId (FK)
├── date, duration, memo

Wishlist (위시리스트)
├── id, rawgId
├── title, coverUrl, releaseDate, priority
├── addedAt

Screenshot (스크린샷)
├── id, gameId (FK)
├── imagePath, capturedAt, memo

UserConfig (설정)
├── key, value
├── (steamId, theme, defaultTab, notifications...)
```

### 커리큘럼 매핑 (6주 과정 전체 활용)
| 주차 | 배우는 것 | 앱에 적용 |
|------|----------|----------|
| 2주차 | Activity, Intent | Splash→Onboarding→Main 흐름, GameDetail→ReviewWrite, 공유/갤러리 Intent |
| 3주차 | RecyclerView | 홈 타임라인, 게임 목록(필터 탭), 검색 결과, 플레이 로그, 설정 항목 |
| 4주차 | 로컬 DB | Game, PlayLog, Wishlist, Screenshot, UserConfig 테이블 |
| 5주차 | 백그라운드 처리 | API 비동기 호출, 이미지 로딩, Steam 대량 임포트, 위시리스트 출시일 알림 |
| 6주차 | API | Steam Web API (라이브러리 임포트) + RAWG API (검색, 메타데이터) |

### 현재 진행 중 제약 (다음 주차로 미룸)
- ❌ Room DB / SharedPreferences (10주차) → 영속 저장 없음
- ❌ API 호출 (12주차) → 게임 더미 데이터

## 9주차 과제 설계 (RecyclerView + 홈 화면 도입)

### 학습 목표
- RecyclerView의 핵심 구성요소(Adapter / ViewHolder / LayoutManager) 이해
- 서로 다른 LayoutManager 비교 체험 (Linear / Grid / 멀티 뷰타입)
- BottomSheetDialog로 카드 액션 메뉴 구현
- ItemTouchHelper로 드래그 정렬 구현

### 화면 구조 (현재 = 최종)
8주차에서는 Splash → Onboarding → Main(게임 리스트) 였으나, 9주차에서 **HomeActivity를 허브로 도입**.

```
SplashActivity → OnboardingActivity → HomeActivity (허브)
                                         ├─ LibraryActivity  (보관함 — 그리드, 메인 컬렉션)
                                         └─ TimelineActivity (최근 활동 — 활동 피드)
                                       → GameDetailActivity → ReviewWrite / Screenshot
```

> ⚠️ 진행 경과: 초기엔 구 MainActivity를 **DiaryActivity**(리스트 뷰)로 전환해 함께 뒀으나,
> 보관함과 "같은 데이터를 보여주는" 중복이라 **DiaryActivity는 제거**하고 기능을 보관함이 흡수함.
> (드래그 정렬은 그리드와 안 맞아 함께 제거 — 학습 코드는 git 히스토리에 보존)
> 아래 "단계별 구현 순서"의 DiaryActivity 관련 단계는 **당시 진행 기록**으로 남겨둠.

### 화면별 RecyclerView 학습 매핑 (현재)
| 화면 | LayoutManager | 학습 포인트 |
|---|---|---|
| HomeActivity | ScrollView(세로) + 자식 RecyclerView(가로) | **중첩 스크롤**(허브에 섹션별 미리보기, 어댑터 재사용) |
| LibraryActivity (보관함) | GridLayoutManager (2열) | 그리드 + 상태별 필터 탭 + 길게 누르기 BottomSheet |
| TimelineActivity (최근 활동) | LinearLayoutManager | **멀티 뷰타입** (`getItemViewType()` 4종 분기) |

### 벤치마킹 출처
- **HomeActivity 미리보기 허브**: Letterboxd, Spotify, Backloggd
- **LibraryActivity 그리드 + 상태 필터**: Steam 라이브러리, Backloggd
- **TimelineActivity 활동 피드**: Letterboxd 다이어리, Day One
- **카드 길게 누르기 → BottomSheet**: Spotify, YouTube, Letterboxd

### LibraryActivity(보관함) 기능 — 앱의 메인 컬렉션 화면
- **상태별 필터 탭** (전체 / 플레이중 / 완료 / 중단 / 찜 목록) + 빈 상태 안내
- **게임 추가(+ 메뉴) / 앱 정보(⋮ 메뉴)** — 구 DiaryActivity에서 이전
- **공유 수신** (다른 앱 → 우리 앱, ACTION_SEND intent-filter) — 구 DiaryActivity에서 이전
- **카드 길게 누르기 → BottomSheetDialog** (삭제 / 공유 / 상세 보기)
  - 삭제: Repository에서 제거 후 현재 탭 재필터
  - 공유: ACTION_SEND chooser

### TimelineActivity 데이터 모델 (더미)
```
ActivityLog
├─ enum Type { ADDED, COMPLETED, REVIEWED, PLAYED }
├─ Type type
├─ int gameId       (어떤 게임에 대한 로그인지)
├─ long timestamp
└─ String payload   (예: 리뷰 본문, 플레이 시간 등)
```
- ActivityLogRepository: 더미 8~12개 하드코딩, 4종 타입 섞어서

### 단계별 구현 순서 (최소 커밋 단위)

**Phase 0 — 사전 패치 (스크린샷 영속 + 데이터 writeback)**

문제 진단:
- Activity 간 Game 전달은 Parcelable 복사본(참조 아님) → 어떤 화면에서 Game을 수정해도 GameRepository 원본은 그대로
- 결과: 별점/리뷰/스크린샷 모두 Main으로 돌아오면 변경 사항이 사라져 보임 (현재 잠재 버그)

해결 방향:
- GameRepository를 Application 클래스에서 보유 → 전 Activity가 동일 인스턴스 공유
- 수정 결과를 받은 Activity가 명시적으로 `repository.updateGame()` 호출
- ScreenshotActivity가 변경된 Game을 setResult로 반환

구현 순서:
0-1. `Game` 모델에 `List<String> screenshots` 필드 추가 (Parcelable R/W 포함)
0-2. `GameRepository.updateGame(Game updated)` 추가 (id 매칭, rating/review/screenshots 반영)
0-3. `App` Application 서브클래스 도입 + Manifest 등록 (GameRepository 보유)
0-4. `MainActivity`가 App을 거쳐 GameRepository 접근하도록 전환
0-5. `GameDetailActivity` reviewLauncher 결과 처리에 `repository.updateGame()` 추가 (별점/리뷰 영속화)
0-6. `ScreenshotActivity` Game 통째로 받기 + finish 시 setResult로 반환
0-7. `GameDetailActivity` screenshotLauncher 등록 + 결과 처리 + repo 갱신

**Phase 1 — DiaryActivity 전환 (기본 RecyclerView)**
1. `item_game_card.xml` 레이아웃 분리
2. `GameCardAdapter` + `GameCardViewHolder` 작성
3. MainActivity → DiaryActivity 이름 변경
4. ScrollView/LinearLayout → RecyclerView 교체 (LinearLayoutManager)

**Phase 2 — DiaryActivity 컨텍스트 액션 (BottomSheet)**
5. 길게 누르기 리스너 + BottomSheetDialog 표시
6. 메뉴 구성 (삭제 / 공유 / 상세)
7. 삭제 액션 구현
8. 공유 액션 구현 (ACTION_SEND chooser 재사용)

**Phase 3 — DiaryActivity 드래그 정렬**
9. 드래그 핸들 아이콘 + UI 추가
10. `ItemTouchHelper.SimpleCallback` 작성
11. `onItemMove` 구현 + Repository 순서 반영

**Phase 4 — LibraryActivity (그리드 뷰)**
12. `LibraryActivity` 뼈대
13. `item_library_grid.xml` (커버 위주)
14. `LibraryAdapter` + `GridLayoutManager`
15. 그리드 아이템 클릭 → GameDetail 진입

**Phase 5 — TimelineActivity (멀티 뷰타입)**
16. `ActivityLog` 모델 + `ActivityLogRepository`
17. `TimelineActivity` 뼈대
18. 4종 뷰타입별 `item_log_*.xml` 레이아웃
19. `TimelineAdapter` 멀티 뷰타입 구현 (`getItemViewType` 4종)

**Phase 6 — HomeActivity 도입**
20. `HomeActivity` 뼈대 + Splash/Onboarding 진입 경로 변경 (FLAG_ACTIVITY_CLEAR_TASK)
21. 섹션별 미리보기용 가로 RecyclerView (Diary/Library/Timeline 일부)
22. "더 보기"로 각 자식 Activity 진입

### 9주차 확장 작업 (Phase 0~6 이후 추가)
RecyclerView 기본 학습을 마친 뒤 앱을 풍성하게 만든 후속 작업들:

- **게임 진행 상태 (`GameStatus`)**: PLAYING / COMPLETED / DROPPED / BACKLOG enum
  (Game에 status 필드 + Parcelable R/W, displayName + 배지색 ARGB 보유)
- **보관함 상태별 필터 탭**: TabLayout(전체 + 4상태) → 선택 시 필터링 + 빈 상태 안내
- **별점 시각화**: 텍스트 "★ 4.5" → `RatingBar`(small, stepSize 0.5) 별 아이콘
- **상태 변경 UI**: GameDetail에서 AlertDialog 단일 선택으로 상태 변경 → Repository 반영
- **상태별 색상 배지**: 상태색을 `backgroundTintList`로 적용 (배지 모양 drawable 1개 공유)
- **용어 순화** (중학생 기준): 백로그→찜 목록, 다이어리→일기, 라이브러리→보관함, 타임라인→최근 활동
  (코드 식별자/클래스명은 영어 유지, 화면 표시 텍스트만 변경)
- **일기(DiaryActivity) 제거**: 보관함과 데이터 중복이라 제거, 기능(게임추가/공유수신/BottomSheet)은
  보관함이 흡수. 드래그 정렬(ItemTouchHelper)은 그리드와 안 맞아 함께 제거
  → 최종 화면: Home → (보관함 / 최근 활동) → GameDetail

## 10주차 과제 설계 (SharedPreferences + 가상 계정 시스템)

### 학습 목표
- SharedPreferences 핵심(key-value 저장, `apply()` 비동기 쓰기) 이해 — Unity의 `PlayerPrefs`와 동일 개념
- **prefs 파일을 여러 개** 만들 수 있다는 점 활용 → 계정마다 별도 파일 = 가상 계정 시스템
- 서버 없이 로컬 prefs로 로그인/회원가입 흉내내기
- 앱이 튕기거나 나가도 작성 중이던 내용이 살아있게(실시간 자동저장)

### 사전 작업 (완료)
- 8주차 잔재 식별자 정리: 패키지/namespace/applicationId `com.example.week8` → `com.example.week10`,
  `rootProject.name` `week8` → `week10`, 테마 `Theme.Week8` → `Theme.Week10`
- 죽은 참조 수정: 매니페스트 AddGameActivity의 `parentActivityName` `.DiaryActivity`(9주차 제거됨) → `.library.LibraryActivity`

### 데이터 맵 — "어떤 파일에 어떤 key를 저장하나"

**① 전역 파일 `app_global`** (계정 무관, 앱 전체)
| key | 값 | 쓰는 기능 |
|---|---|---|
| `account_ids` | 가입된 계정 id 목록(StringSet) | 회원가입/삭제 |
| `current_account` | 지금 로그인된 계정 id | 로그인/로그아웃 |
| `auto_login` | 로그인 유지 on/off | 자동 로그인 |

**② 계정별 파일 `user_<id>`** (계정마다 1개 = 로그인하면 "내 PlayerPrefs")
| key | 값 | 쓰는 기능 |
|---|---|---|
| `nickname` `pin` | 가입 정보 | 회원가입/PIN검증 |
| `avatar_color` `bio` | 프로필 꾸미기 | 계정별 프로필 |
| ~~`theme`~~ | ~~다크/라이트/시스템~~ | ~~테마 설정~~ (제거됨 — 아래 Phase 8 참고) |
| `tutorial_seen` | 튜토리얼 봤는지 | 계정별 튜토리얼 |
| `visit_count` `last_visit_date` `streak` | 출석 | 방문 통계 |
| `draft_review_<게임id>` | 작성 중 리뷰 | 리뷰 자동저장 |
| `last_filter_tab` `last_sort` | 보관함 마지막 상태 | 상태 기억 |

### 클래스 설계 (App이 보유 → 전 Activity 공유)
- **`Account`** (model): id, nickname, avatarColor, bio
- **`AccountManager`**: `app_global` 담당 — 로그인/로그아웃/회원가입/삭제/자동로그인/계정목록
- **`UserPrefs`**: `user_<id>` 담당 — 프로필·튜토리얼·출석·드래프트·마지막상태 읽고쓰기 (테마는 Phase 8에서 제거)
- 키 상수는 각 클래스가 `private static final String KEY_...`로 소유 (중앙 Config 클래스 없음)
- `App`이 생성·보유 → Activity는 `((App) getApplication()).getAccountManager()`로 접근 (생성자 주입 정신)

### 화면 흐름
```
Splash ─ auto_login && current_account 있음? ─예─→ Home (그 계정)
   └─아니오─→ LoginActivity ─[계정선택+PIN] 또는 [회원가입]─→
                  tutorial_seen 없으면 → Onboarding(튜토리얼) → Home
Home/Profile: 닉네임·아바타·출석 연출 + 로그아웃/계정삭제 진입
```
- 기존 Onboarding(전역 최초 1회)을 **계정별 튜토리얼**(`tutorial_seen` 기준)로 의미 변경

### 단계별 구현 순서 (각 Phase = 최소 커밋 1개)
| # | Phase | 핵심 |
|---|---|---|
| 1 | `Account` 모델 + `AccountManager` 뼈대 | 전역 prefs 읽기/쓰기 (UI 없음, 데이터층만) |
| 2 | `LoginActivity` — 계정 선택 + PIN 검증 | 로그인 성공 → `current_account` 세팅 → Home |
| 3 | 회원가입 | 새 `user_<id>` 파일 생성 + `account_ids` 추가 |
| 4 | 로그아웃 + 계정 삭제 | 세션 비우기 / prefs 파일 통째 삭제 |
| 5 | 자동 로그인 | Splash 분기 + "로그인 유지" 체크박스 |
| 6 | `UserPrefs` 도입 + 계정별 튜토리얼 | Onboarding을 `tutorial_seen` 기준으로 |
| 7 | 계정별 프로필 | avatar 색 + bio → Home/Profile 표시 |
| ~~8~~ | ~~테마 설정~~ | ~~계정별 `theme` 저장/적용~~ — **제거됨** (레이아웃 색이 하드코딩돼 다크/라이트 전환이 화면에 반영 안 됨. `ThemeMode`/`UserPrefs.theme`/Home 테마 메뉴 삭제, 테마는 Light 고정) |
| 9 | 출석/방문 통계 | `visit_count`/`streak` 연출 |
| 10 | 리뷰 자동저장 | `draft_review_<게임id>` 실시간 저장/복원 |
| 11 | 보관함 마지막 상태 기억 | 필터 탭/정렬 저장/복원 |

**의존성 메모:** `UserPrefs`(6)가 7~11의 토대 → 6 전에 위치. 1→2→3 순서 고정, 4 이후는 비교적 자유.
