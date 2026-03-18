# Project: weak71 (Android Layout 학습 과제)

## 사용자 프로필
- Unity 클라이언트 프로그래머 2년차
- Android 개발환경 초보자
- 설명 시 Unity/C# 개념에 비유하면 이해가 빠름
- 한국어로 소통

## 5개 화면 데모 (전부 구현 완료)
| 화면 | Activity | 레이아웃 | 크기 조절 방식 | 상태 |
|------|----------|---------|-------------|------|
| 홈 (진입점) | HomeActivity | LinearLayout | - | 완료 |
| 1. 로그인 | LoginActivity | LinearLayout | 고정 dp (기본) | 완료 |
| 2. 프로필 | ProfileActivity | ConstraintLayout | Guideline (%) | 완료 |
| 3. 설정 | SettingActivity | ScrollView + LinearLayout | match_parent | 완료 |
| 4. 계산기 | CalculatorActivity | GridLayout + ConstraintLayout | 좌우 Guideline (5%/95%) | 완료 |
| 5. 카드 겹침 | CardActivity | FrameLayout | 고정 dp + elevation | 완료 |

## 프로젝트 특징
- Material3 + Edge-to-Edge 적용
- 모든 Java/XML 파일에 Unity 비유 포함 상세 주석 작성됨
- UI 데모 전용 (실제 비즈니스 로직 없음)
- ViewBinding 사용

## 학습 방식
- 가이드를 주면 사용자가 직접 구현하며 익히는 방식
- 각 화면마다 다른 크기 조절 방식을 체험
- 주석을 꼼꼼히 달면서 이해 내용 정리
