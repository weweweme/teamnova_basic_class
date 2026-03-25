# Project: weak71 (Android Layout + Manifest 학습 과제)

## 사용자 프로필
- Unity 클라이언트 프로그래머 2년차
- Android 개발환경 초보자
- 설명 시 Unity/C# 개념에 비유하면 이해가 빠름
- 한국어로 소통

## 13개 화면 데모 (전부 구현 완료)

### 섹션 1: 레이아웃 학습 - 실제 앱 화면 따라 만들기 (1~5번)
| 화면 | Activity | 레이아웃 | 따라 만드는 앱 | 상태 |
|------|----------|---------|-------------|------|
| 홈 (진입점) | HomeActivity | ScrollView + LinearLayout | - | 완료 |
| 1. 카카오톡 로그인 | KakaoLoginActivity | LinearLayout | 카카오톡 로그인 | 완료 |
| 2. 인스타그램 프로필 | InstaProfileActivity | ConstraintLayout | 인스타그램 마이페이지 | 완료 |
| 3. 쿠팡 상품 상세 | CoupangDetailActivity | ScrollView + LinearLayout | 쿠팡 상품 상세 페이지 | 완료 |
| 4. 계산기 | IosCalcActivity | GridLayout | iOS 계산기 | 완료 |
| 5. 뮤직 플레이어 | MusicPlayerActivity | FrameLayout | 뮤직 플레이어 (앨범아트 위에 컨트롤 겹침) | 완료 |

### 섹션 2: Manifest 학습 (6~13번)
| 화면 | Activity | 주요 Manifest 요소 | 상태 |
|------|----------|-------------------|------|
| 6. 카메라 | CameraActivity | uses-permission, uses-feature | 완료 |
| 7. 공유 수신 | ShareActivity | intent-filter ACTION_SEND, data mimeType | 완료 |
| 8. 딥링크 | DeepLinkActivity | data scheme/host/pathPrefix, BROWSABLE | 완료 |
| 9. 화면 속성 | PortraitActivity | screenOrientation, configChanges, windowSoftInputMode | 완료 |
| 10. 전체화면 | FullScreenActivity | Activity 단위 theme 오버라이드 | 완료 |
| 11. 실행 모드 | LaunchModeActivity | launchMode (standard vs singleTop FLAG) | 완료 |
| 12. 서비스/리시버 | ServiceReceiverActivity | service, receiver, meta-data | 완료 |
| 13. 패키지 가시성 | QueryActivity | queries (Android 11+) | 완료 |

### 비-Activity 컴포넌트
| 컴포넌트 | 타입 | 주요 요소 |
|----------|------|----------|
| DemoForegroundService | Service | foregroundServiceType, specialUse |
| CustomTickReceiver | BroadcastReceiver | 코드 등록 (커스텀 브로드캐스트) |

## 프로젝트 특징
- Material3 + Edge-to-Edge 적용
- 모든 Java/XML 파일에 Unity 비유 포함 상세 주석 작성됨
- UI 데모 전용 (실제 비즈니스 로직 없음)
- ViewBinding 사용
- AndroidManifest.xml에 30+개 요소/속성 커버 (상세 주석 포함)

## Manifest 커버 요소
uses-permission (6개), uses-feature, permission (커스텀), queries (package/intent),
meta-data (Application/Activity 레벨), intent-filter (LAUNCHER/SEND/VIEW),
action, category (DEFAULT/BROWSABLE/LAUNCHER), data (scheme/host/pathPrefix/mimeType),
screenOrientation, configChanges, windowSoftInputMode, launchMode, theme,
exported, service (foregroundServiceType/property), receiver

## 학습 방식
- 가이드를 주면 사용자가 직접 구현하며 익히는 방식
- 각 화면마다 다른 Manifest 요소를 체험
- 주석을 꼼꼼히 달면서 이해 내용 정리
- ★ 테스트 가이드: 속성을 바꿔보고 결과를 관찰

## 6주차 최종 작품: 게임 다이어리 앱 (가칭)

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
