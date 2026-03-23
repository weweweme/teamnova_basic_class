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
| 1. 카카오톡 로그인 | LoginActivity | LinearLayout | 카카오톡 로그인 | 완료 |
| 2. 인스타그램 프로필 | ProfileActivity | ConstraintLayout | 인스타그램 마이페이지 | 완료 |
| 3. 쿠팡 상품 상세 | SettingActivity | ScrollView + LinearLayout | 쿠팡 상품 상세 페이지 | 미구현 |
| 4. 계산기 | CalculatorActivity | GridLayout | iOS 계산기 | 미구현 |
| 5. 뮤직 플레이어 | CardActivity | FrameLayout | 뮤직 플레이어 (앨범아트 위에 컨트롤 겹침) | 미구현 |

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
