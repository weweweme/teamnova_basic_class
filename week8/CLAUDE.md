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
- **static 사용 기준**: 상수는 `static final`, 그 외에는 지양
  - `static final` 상수: **OK** — 고정값은 인스턴스마다 중복 저장할 필요 없음 (public/private 모두)
  - `final` 인스턴스 필드: 생성자에서 인스턴스마다 다른 값을 받는 경우에만 사용
  - 가변 `static` 필드: **금지** — 해당 데이터를 관리하는 객체의 인스턴스 필드로 이동
  - `static` 메서드: 유틸리티 헬퍼나 팩토리 메서드에만 사용. 그 외에는 인스턴스 메서드 사용
  ```java
  // 좋은 예: 외부에서 접근하는 고정값
  public static final int WIDTH = 100;

  // 좋은 예: 내부 고정값 (인스턴스마다 중복 저장 방지)
  private static final int SHOOT_COL = 12;

  // 좋은 예: 인스턴스마다 다른 값 (생성자에서 받음)
  private final String name;

  // 나쁜 예: 고정값인데 인스턴스 필드 (메모리 낭비)
  private final int SHOOT_COL = 12;

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

## 학습 방식
- 가이드를 주면 사용자가 직접 구현하며 익히는 방식
- 주석을 꼼꼼히 달면서 이해 내용 정리
- ★ 테스트 가이드: 속성을 바꿔보고 결과를 관찰

## 최종 작품: 게임 다이어리 앱 (가칭)

### 컨셉
- **"게임판 Letterboxd"** - 개인 게임 다이어리
- Backloggd(https://backloggd.com/) 벤치마킹, 단 소셜 기능 빼고 **개인 기록에 집중**
- Android 네이티브 앱 (기존 서비스들은 대부분 웹 only)

### 핵심 기능 3가지
1. **내 게임 라이브러리 관리** - Steam Web API로 자동 임포트 + RAWG API로 수동 검색/추가
2. **위시리스트 관리** - 하고 싶은 게임 관리 (RAWG 검색, 멀티플랫폼)
3. **한줄평 + 별점** - 게임마다 별점(0.5~5.0) + 짧은 감상 기록 (소셜 X, 순수 개인 기록)

### 사용 API
- **Steam Web API** (무료): `IPlayerService/GetOwnedGames` - 소유 게임 + 플레이타임 자동 임포트
- **RAWG API** (무료): 50만개+ 게임 DB - 메타데이터(표지, 장르, 평점, 플랫폼, 출시일)

### Backloggd 대비 차별점
- Android 네이티브 (Backloggd는 웹 only)
- Steam 자동 임포트 (Backloggd는 수동 추가만)
- 소셜 기능 없음 → 순수 "나를 위한 게임 일지"

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
| 2주차 | Activity, Intent | Splash→Onboarding→Main 흐름, GameDetail→ReviewWrite, 공유/카메라 Intent |
| 3주차 | RecyclerView | 홈 타임라인, 게임 목록(필터 탭), 검색 결과, 플레이 로그, 설정 항목 |
| 4주차 | 로컬 DB | Game, PlayLog, Wishlist, Screenshot, UserConfig 테이블 |
| 5주차 | 백그라운드 처리 | API 비동기 호출, 이미지 로딩, Steam 대량 임포트, 위시리스트 출시일 알림 |
| 6주차 | API | Steam Web API (라이브러리 임포트) + RAWG API (검색, 메타데이터) |

## 8주차 과제 설계 (Lifecycle + Intent 집중)

### 학습 목표
- Activity Lifecycle 9개 콜백 전부를 "단순 로그가 아닌 실제 기능"에 사용
- Intent 6가지 카테고리(송신/수신/결과반환/암시적VIEW/Chooser/Filter) 전부 실사용
- 특히 **onPause(영구 임시저장) vs onSaveInstanceState(회전 대응 휘발 저장)** 차이를 한 화면에서 직접 체험

### 8주차 제약 (다음 주차로 미룸)
- ❌ RecyclerView (9주차) → ScrollView + LinearLayout으로 카드 동적 추가
- ❌ Room DB (10주차) → SharedPreferences로 임시저장만
- ❌ API 호출 (12주차) → 게임 더미 데이터
- ❌ Fragment → 전부 Activity

### 구현 화면 (7개)
| # | Activity | 역할 | 핵심 학습 포인트 |
|---|---|---|---|
| 1 | SplashActivity | 1.5초 로고 후 분기 | launcher Intent Filter, FLAG_ACTIVITY_CLEAR_TASK/NO_HISTORY |
| 2 | OnboardingActivity | 앱 소개 3페이지 | onSaveInstanceState로 페이지 인덱스 보존 |
| 3 | MainActivity | 게임 카드 리스트 (ScrollView) | onResume에서 SharedPrefs 재로드, onRestart 관찰, SEND 수신 필터 |
| 4 | GameDetailActivity | 게임 상세, 암시적 Intent 4종 집결지 | SEND chooser, VIEW 브라우저, forResult 2건 |
| 5 | ReviewWriteActivity ★ | 별점+한줄평 | onPause(draft 영구) + onSaveInstanceState(회전) 동시 사용 |
| 6 | ScreenshotActivity | 카메라 호출 + 미리보기 | IMAGE_CAPTURE, 외부앱 호출 시 lifecycle 흐름 화면 표시 |
| 7 | AboutActivity | 앱 정보 | VIEW(브라우저) / SENDTO(메일) chooser |

### Lifecycle 콜백 9개 매핑
| 콜백 | 화면 | 동작 |
|---|---|---|
| onCreate | 전부 | ViewBinding, Intent extras 파싱 |
| onStart | Main | 포그라운드 진입 표시 |
| onResume | Main | SharedPrefs 재로드 → 리스트 갱신 |
| onPause | ReviewWrite, GameDetail | draft/마지막 본 게임 영구 저장 |
| onStop | Screenshot | 카메라 호출 흐름 관찰 |
| onRestart | Main | 백키 복귀 분기 |
| onDestroy | Splash | Handler 콜백 제거 |
| onSaveInstanceState | Onboarding(페이지), ReviewWrite(입력중), Main(스크롤) | 회전 대응 |
| onRestoreInstanceState | 동일 | Bundle 복원 |

### Intent 카테고리 매핑
| 카테고리 | 사용처 |
|---|---|
| 송신(명시적) | Splash→Onboarding/Main, Main→GameDetail (Parcelable Game) |
| 수신(필터) | Main의 SEND text/plain 필터, Splash의 LAUNCHER |
| 결과 반환 | GameDetail↔ReviewWrite, GameDetail↔Screenshot |
| 암시적 VIEW | GameDetail→Steam URL, About→RAWG 사이트 |
| Chooser | GameDetail SEND, About SENDTO 메일 |
| 외부 액션 | Screenshot의 IMAGE_CAPTURE |
| Flags | Splash→Main의 CLEAR_TASK/NO_HISTORY |

### 데이터 모델 (더미)
```
Game (Parcelable)
├─ int id
├─ String title
├─ String coverAssetName
├─ String genre
├─ String platform
├─ String storeUrl
├─ float rating (0~5)
└─ String review
```
- GameRepository: 더미 5~6개 하드코딩, rating/review만 SharedPrefs로 영속화
- SharedPrefs 키: `review_draft_{gameId}`, `last_seen_game_id`, `onboarding_done`

### 단계별 구현 순서 (최소 커밋 단위)
1. ✅ Game Parcelable 모델
2. ✅ GameRepository 더미 데이터
3. ✅ SplashActivity 뼈대 + Handler 분기
4. ✅ OnboardingActivity 뼈대 (3페이지)
5. ✅ Onboarding onSaveInstanceState 페이지 보존
6. ✅ MainActivity 뼈대 (ScrollView + 카드 동적 생성)
7. ✅ Main onResume 재로드
8. ✅ Main → GameDetail 명시적 Intent (Parcelable)
9. ✅ GameDetailActivity 화면 구성
10. ✅ GameDetail → ReviewWrite forResult
11. ReviewWrite onSaveInstanceState
12. ReviewWrite onPause draft 저장/복원
13. ✅ ReviewWrite setResult 반환 → 갱신 확인
14. GameDetail ACTION_SEND chooser
15. GameDetail ACTION_VIEW (Steam)
16. ScreenshotActivity + IMAGE_CAPTURE
17. Screenshot lifecycle 화면 표시
18. AboutActivity + VIEW/SENDTO chooser
19. Main SEND Intent Filter 수신
20. ✅ Splash FLAG 적용 + 정리
