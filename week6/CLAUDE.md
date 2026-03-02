# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 안내 문서입니다.

## 프로젝트 개요

팀노바 기초반 6주차 Java 학습 프로젝트입니다. IntelliJ IDEA를 사용합니다.

**게임**: "표류자들 - The Castaways" (타워 디펜스 서바이벌)
- 3명의 정착민이 바리케이드를 사이에 두고 적 웨이브를 방어
- 낮: 자원 관리 (수리, 치료, 무기 구매, 건설, 모집, 승격)
- 밤: 적 웨이브 전투 (자동 사격)
- 승리 조건: 난이도별 목표 일수 생존 (쉬움 7일 / 보통 10일 / 어려움 15일)

**맵 구조**: 100×20 문자
- 왼쪽 (0~14열): 안전지대 (정착민 배회, 탄약 상자)
- 바리케이드 (15~16열): 방벽
- 오른쪽 (17~99열): 전장 (적, 가시덫, 지뢰)

### 상속 구조

**unit 패키지 (유닛)**

GameEntity가 Thread를 상속하여 각 정착민/적이 독립 스레드로 동작.
ColonistState는 상태 패턴으로 낮(배회) ↔ 밤(사격) 행동 전환.

```
GameEntity (abstract, extends Thread — HP, 위치, 스레드 관리)
├── Colonist (무기, 상태 패턴, 사망 애니메이션, 5발마다 specialAttack 콜백)
│   ├── Gunner (속사 탄막 — 전체 적 소량 데미지)
│   ├── Sniper (정밀 저격 — HP 최고 적 큰 데미지)
│   └── Assault (충격파 — 전체 적 넉백 + 화면 흔들림)
└── Enemy (이동, 바리케이드/정착민 공격, 8틱마다 specialAbility 콜백)
    ├── ChargerEnemy (돌진 질주 — 3칸 추가 전진 + 잔상)
    ├── ArmoredEnemy (방패 충돌 — 바리케이드 2배 공격 또는 1회 피해 무효)
    └── RegeneratingEnemy (치유 포자 — 모든 적 HP 회복)
```

```
ColonistState (abstract — enter/update/exit)
├── WanderingState (낮: 자동회복, 안전지대 랜덤 이동)
└── ShootingState (밤: 바리케이드 이동 후 사격)
```

**데이터 계층 (Type → Spec → Factory)**

열거형은 순수 타입 식별자, 데이터는 Spec, 생성은 Factory가 담당.

```
ColonistType (BASIC, GUNNER, SNIPER, ASSAULT)
ColonistSpec (이름, 체력, 발사배율, 치명타, 넉백, 블록 템플릿)
ColonistFactory (Type → Spec 매핑)
ColonistSpawner (정착민 생성/승격 — 스레드 교체 포함)
NameProvider (30개 이름 풀에서 중복 없이 랜덤 선택)

EnemyType (WOLF, SPIDER, SKELETON, ZOMBIE, RAT, SLIME, BEAR, BANDIT, SCORPION, ORC, DRAGON, GOLEM)
EnemySpec (이름, 체력, 데미지, 이동속도, 보상, 특성, 블록)
EnemyFactory (Type → Spec 매핑, create()로 특성별 서브클래스 생성)

EnemyTrait (STANDARD, CHARGER, ARMORED, REGENERATING)
```

**gun 패키지 (무기)**

Gun이 공통 속성을 생성자로 받고, fire()만 서브클래스에서 구현.
fireBullet()이 공통 발사 로직 (조준, 크리티컬, 넉백, 총알 생성).
playFireSound()로 무기별 발사음을 다형적으로 재생 (샷건은 전용 폭발음).

```
Gun (abstract — name, cost, fireInterval, damage, bulletSpeed, bulletChar, bulletColor, playFireSound)
├── Pistol   (단발, 비용 0, 기본 무기)
├── Shotgun  (부채꼴 3발 ±2행, 비용 25, 전용 발사음)
├── Rifle    (관통 단발, 비용 20)
└── Minigun  (매 틱 단발, 비용 30)

Bullet (데이터 클래스 — 시작/목표 좌표, 데미지, 관통, 넉백, 치명타)
```

**structure 패키지 (구조물)**

Structure가 위치(행+열)/내구도, Buildable이 비용, Trap이 피해량 계층.
구조물은 단일 셀(1칸)로 배치되며, 배치 모드에서 커서로 위치를 선택한다.

```
Structure (row, column, maxHp — 위치 + 내구도 관리)
├── Barricade (전체 높이, 레벨업, 무적 모드, 피격/수리 깜빡임)
└── Buildable (cost — 건설 가능 구조물, 단일 셀)
    ├── AmmoBox (발사속도 30% 증가, 안전지대에 배치)
    └── Trap (damage — 피해를 주는 구조물, 전장에 배치)
        ├── Spike (내구도 10, 적 블록과 겹치면 3 데미지)
        └── Landmine (일회용, 반경 3칸 15 데미지 폭발)
```

**game 패키지 (게임 시스템)**

```
Main (진입점 — 타이틀↔게임 반복, 난이도 선택, 초기화, 메인 루프)
DayNightCycle (Thread — 낮/밤 전환, 적 스폰, 이벤트)
WaveBuilder (일차별 적 웨이브 구성)
GameWorld (게임 상태 컨테이너 — 엔티티/구조물/자원/적버프 관리)
ScreenEffects (화면 효과 — 흔들림, 웨이브 경고)
BulletSystem (총알 이동 + 충돌 처리)
Renderer (화면 렌더링 — 버퍼 기반)
PanelBuilder (오른쪽 정보 패널 생성)
InputHandler (키 입력 읽기 + 명령 디스패치 + 한글 자모→영문 변환)
Supply (보급품 자원 관리)
Cutscene (스토리 애니메이션)
BgmPlayer (Thread — JLayer MP3 루프 재생)
SfxPlayer (효과음 — javax.sound 사인파 합성)
HitEffect (명중 이펙트 데이터)
LogEntry (로그 메시지 데이터)
Util (터미널 모드, 화면 클리어, 딜레이, 랜덤)
DifficultySettings (난이도별 배율)
Difficulty (EASY, NORMAL, HARD)
DayEvent (SUPPLY_DROP, WANDERER, STORM_WARNING, CALM_DAY)
Direction (8방향 이동 벡터)
Position (행/열 좌표)
```

### 상속 구조 차트

```mermaid
graph TD
    subgraph "unit"
        T[Thread] --> GE[GameEntity]
        GE --> CO[Colonist]
        CO --> GU2[Gunner]
        CO --> SN[Sniper]
        CO --> AS[Assault]
        GE --> EN[Enemy]
        EN --> CE[ChargerEnemy]
        EN --> AE[ArmoredEnemy]
        EN --> RE[RegeneratingEnemy]
        CS[ColonistState] --> WS[WanderingState]
        CS --> SS[ShootingState]
    end
    subgraph "gun"
        GU[Gun] --> PI[Pistol]
        GU --> SG[Shotgun]
        GU --> RI[Rifle]
        GU --> MG[Minigun]
    end
    subgraph "structure"
        ST[Structure] --> BA[Barricade]
        ST --> BU[Buildable]
        BU --> AB[AmmoBox]
        BU --> TR[Trap]
        TR --> SP[Spike]
        TR --> LM[Landmine]
    end
    subgraph "data (Type → Spec → Factory)"
        CT[ColonistType] -.-|조회| CF[ColonistFactory]
        CF -.-|생성| CSP[ColonistSpec]
        ET[EnemyType] -.-|조회| EF[EnemyFactory]
        EF -.-|생성| ESP[EnemySpec]
    end
```

### 패키지별 클래스 관계

**unit 패키지**

```mermaid
classDiagram
    Thread <|-- GameEntity
    GameEntity *-- Position
    GameEntity --> GameWorld : 참조

    class GameEntity {
        Position position
        int hp
    }
    class Position {
        int row
        int col
    }
    class Direction {
        <<enum>>
        int deltaRow
        int deltaCol
    }
```

**unit/colonist 패키지**

```mermaid
classDiagram
    GameEntity <|-- Colonist
    Colonist <|-- Gunner
    Colonist <|-- Sniper
    Colonist <|-- Assault
    ColonistState <|-- WanderingState
    ColonistState <|-- ShootingState

    Colonist *-- ColonistType
    Colonist *-- ColonistSpec
    Colonist *-- ColonistState
    Colonist --> Gun : 장착

    ColonistFactory --> ColonistType
    ColonistFactory --> ColonistSpec : 생성

    ColonistSpawner --> ColonistFactory : Spec 조회
    ColonistSpawner --> NameProvider : 이름 생성
    ColonistSpawner ..> Colonist : 생성/승격

    class Colonist {
        ColonistType type
        ColonistSpec spec
        int shotCount
        +onShoot(Enemy) 카운팅+specialAttack
        #specialAttack(Enemy) 빈 구현
        +transferStateFrom(Colonist)
        +changeState()
    }
    class Gunner {
        #specialAttack() 속사 탄막
    }
    class Sniper {
        #specialAttack() 정밀 저격
    }
    class Assault {
        #specialAttack() 충격파
    }
    class ColonistSpawner {
        +spawn(GameWorld, Position)
        +promote(Colonist, ColonistType, GameWorld)
    }
```

**unit/enemy 패키지**

```mermaid
classDiagram
    GameEntity <|-- Enemy
    Enemy <|-- ChargerEnemy
    Enemy <|-- ArmoredEnemy
    Enemy <|-- RegeneratingEnemy
    Enemy *-- EnemyType
    Enemy *-- EnemySpec
    EnemySpec *-- EnemyTrait

    EnemyFactory --> EnemyType
    EnemyFactory --> EnemySpec : 생성
    EnemyFactory ..> Enemy : create()

    class Enemy {
        EnemyType type
        EnemySpec spec
        int abilityTick
        #getMoveAmount() 기본 1칸
        #onTick() 빈 구현
        #specialAbility() 빈 구현
    }
    class ChargerEnemy {
        #getMoveAmount() 근접 시 2칸
        #specialAbility() 돌진 질주
    }
    class ArmoredEnemy {
        boolean shieldActive
        +takeDamage() 피해 절반
        #specialAbility() 방패 충돌
    }
    class RegeneratingEnemy {
        int regenTick
        #onTick() 3틱마다 회복
        #specialAbility() 치유 포자
    }
    class EnemySpec {
        String displayName
        int maxHp
        int damage
        int tickDelay
        int reward
        EnemyTrait trait
    }
```

**gun 패키지**

```mermaid
classDiagram
    Gun <|-- Pistol
    Gun <|-- Shotgun
    Gun <|-- Rifle
    Gun <|-- Minigun
    Gun ..> Bullet : 생성

    class Gun {
        String name
        int cost
        int fireInterval
        int damage
        int bulletSpeed
        +fire()*
        +playFireSound()
        #fireBullet()
    }
    class Bullet {
        int startRow/Col
        int targetRow/Col
        int damage
        char shooterLabel
        String shooterName
        boolean piercing
        boolean crit
        +advance()
        +getRow()
    }
```

**structure 패키지**

```mermaid
classDiagram
    Structure <|-- Barricade
    Structure <|-- Buildable
    Buildable <|-- AmmoBox
    Buildable <|-- Trap
    Trap <|-- Spike
    Trap <|-- Landmine

    class Structure {
        int row
        int column
        int maxHp
        int hp
    }
    class Buildable {
        int cost
    }
    class Trap {
        int damage
    }
    class Barricade {
        int level
        boolean invincible
        +upgrade()
    }
    class Landmine {
        +explode()
    }
```

**game 패키지 — GameWorld 소유 관계**

```mermaid
classDiagram
    GameWorld *-- BulletSystem
    GameWorld *-- ScreenEffects
    GameWorld *-- Supply
    GameWorld *-- Barricade
    GameWorld *-- SfxPlayer
    GameWorld o-- "0..*" Colonist
    GameWorld o-- "0..*" Enemy
    GameWorld o-- "0..*" Spike
    GameWorld o-- "0..*" Landmine
    GameWorld o-- "0..*" AmmoBox
    GameWorld o-- "0..*" HitEffect
    GameWorld o-- "0..*" LogEntry

    class GameWorld {
        +addColonist()
        +addEnemy()
        +addBullet()
        +advanceBullets()
        +checkLandmines()
        +addLog()
    }
    class ScreenEffects {
        +triggerScreenShake()
        +getScreenShakeOffset()
        +getVerticalShakeOffset()
        +triggerWaveWarning()
        +isWaveWarningActive()
    }
    class SfxPlayer {
        +playShoot()
        +playShotgunBlast()
        +playHit()
        +playCrit()
        +playExplosion()
        +playDeath()
        +playWaveStart()
        +playBarrage()
        +playPrecisionShot()
        +playShockwave()
        +playChargeRush()
        +playShieldBash()
        +playHealingSpore()
    }
```

**game 패키지 — 시스템 간 참조**

```mermaid
classDiagram
    Renderer --> GameWorld
    Renderer --> PanelBuilder
    Renderer --> DayNightCycle
    Renderer --> InputHandler

    InputHandler --> GameWorld
    InputHandler --> Renderer
    InputHandler --> DayNightCycle
    InputHandler --> ColonistSpawner

    DayNightCycle --> GameWorld
    DayNightCycle --> EnemyFactory
    DayNightCycle *-- WaveBuilder
    DayNightCycle --> DifficultySettings

    PanelBuilder --> GameWorld
    PanelBuilder --> InputHandler
    PanelBuilder --> DayNightCycle

    WaveBuilder --> DifficultySettings
```

### 스레드 모델

| 스레드 | 클래스 | 틱 간격 | 역할 |
|--------|--------|---------|------|
| Main | Main | 16ms (입력) / 100ms (물리) | 입력 폴링, 총알 이동, 지뢰 체크, 렌더링 |
| DayNightCycle | DayNightCycle | 500ms | 낮↔밤 전환, 적 스폰, 이벤트 발생 |
| Colonist ×N | Colonist | 500ms | 배회/자동회복 (낮), 이동/사격 (밤) |
| Enemy ×N | Enemy | 200~700ms (종류별) | 이동, 공격, 특성 적용 |

### 게임 루프 흐름

```mermaid
graph TD
    START[Main] --> TITLE[타이틀 화면]
    TITLE -->|q| EXIT[프로그램 종료]
    TITLE -->|1/2/3| DIFF[난이도 선택]
    DIFF --> INTRO[인트로 컷씬]
    INTRO --> INIT[초기화: GameWorld + 정착민 3명 + 스레드 시작]
    INIT --> LOOP[메인 루프]

    LOOP --> INPUT{키 입력?}
    INPUT -->|있음| HANDLE[InputHandler.handleInput]
    INPUT -->|없음| PHYSICS
    HANDLE --> PHYSICS[100ms마다: 총알 이동 + 지뢰 체크]
    PHYSICS --> RENDER[Renderer.render]
    RENDER -->|게임 중| LOOP
    RENDER -->|승리/패배/종료| ENDSCENE[엔딩 컷씬 + 통계]
    ENDSCENE -->|아무 키| TITLE
```

```mermaid
graph TD
    subgraph "DayNightCycle 스레드 (500ms 틱)"
        DAY[낮 페이즈 30초] -->|25초| PREPARE[정착민 바리케이드로 이동]
        PREPARE -->|30초| NIGHT[밤 페이즈]
        NIGHT --> SPAWN[적 시차 스폰]
        SPAWN --> BATTLE[전투: 적 이동 + 정착민 사격]
        BATTLE -->|전멸| REWARD[보급품 지급 + 이벤트]
        REWARD --> CHECK{목표 일수 도달?}
        CHECK -->|아니오| DAY
        CHECK -->|예| VICTORY[승리]
    end
```

### 렌더링 파이프라인

매 프레임(~100ms) Main 루프에서 `Renderer.render()`를 호출하여 화면을 갱신한다.

**화면 구조** (한 줄에 좌측 + 패널 구분선 + 우측 패널)
```
┌─────────────────── 100칸 ───────────────────┐┌── 22칸 ──┐
│              맵 버퍼 (20행)                  │|||  패널   │
│  (바리케이드, 구조물, 정착민, 적, 총알, 이펙트)   │|||  (정보)  │
├─────────────── 구분선 (-) ──────────────────┤|||        │
│              로그 영역 (8행)                  │|||        │
│  (상단 정렬, 최신 로그가 위쪽 — 채팅 스타일)      │|||        │
└─────────────────────────────────────────────┘└──────────┘
```

**렌더링 순서** (뒤에 그린 것이 앞에 보임)

```mermaid
sequenceDiagram
    participant Main
    participant R as Renderer
    participant Buf as buffer/colorBuffer
    participant PB as PanelBuilder
    participant Out as System.out

    Main->>R: render()
    R->>Buf: clearBuffer() — 공백 + 색상 0으로 초기화

    alt 게임오버
        R->>Buf: drawGameOverScreen() — GAME OVER 아스키 아트
    else 정상 플레이
        R->>Buf: drawBarricade() — ## 세로벽, 피격/수리 색상
        R->>Buf: drawSpikes() — ^ 노란색 단일 셀
        R->>Buf: drawLandmines() — @ 빨간색 단일 셀
        R->>Buf: drawAmmoBoxes() — = 초록색 단일 셀
        R->>Buf: drawColonists() — 블록 + 사망 3단계 애니메이션
        R->>Buf: drawEnemies() — 속도순 정렬 (느린→빠른), 블록 + HP 빨강 그라데이션
        R->>Buf: drawBullets() — 무기별 문자/색상
        R->>Buf: drawEffects() — 명중/치명타/폭발 이펙트
        R->>Buf: drawPlacementCursor() — 배치 모드 커서 (500ms 깜빡임)
    end

    R->>R: flush() — 최종 화면 조립
    R->>PB: build() — 우측 패널 생성
    R->>R: 행 순회: 맵 버퍼 + ANSI 색상 + 흔들림 오프셋
    R->>R: 행 순회: 구분선 + 로그 (상단 정렬, 최신 먼저)
    R->>R: 각 행에 ||| + 패널 텍스트 결합
    R->>Out: StringBuilder 일괄 출력 (깜빡임 방지)
```

**버퍼 방식의 핵심**: `char[20][100]` 버퍼와 `int[20][100]` 색상 버퍼에 모든 오브젝트를 그린 뒤, flush()에서 한 번에 `StringBuilder`로 조립하여 `System.out.print()`로 출력한다. 행별 `println()` 대신 전체를 한 번에 출력하여 터미널 깜빡임을 방지한다.

**Z-order (겹침 우선순위)**: 같은 버퍼 위치에 여러 오브젝트가 그려지면 나중에 그린 것이 보인다. 렌더링 순서가 곧 Z-order이다. 적은 `drawEnemies()`에서 tickDelay 내림차순 정렬 후 그려서, 이동 속도가 빠른 적이 느린 적 위에 표시된다.

### 패키지 간 의존관계

```mermaid
graph TB
    Main["<b>Main</b><br/>진입점"]
    game["<b>game</b><br/>DayNightCycle, GameWorld<br/>ScreenEffects, BulletSystem<br/>Renderer, PanelBuilder<br/>InputHandler, WaveBuilder<br/>Supply, Cutscene<br/>BgmPlayer, SfxPlayer"]
    colonist["<b>unit/colonist</b><br/>Colonist, Gunner, Sniper, Assault<br/>ColonistState, WanderingState, ShootingState<br/>ColonistSpec, ColonistFactory<br/>ColonistSpawner, NameProvider"]
    enemy["<b>unit/enemy</b><br/>Enemy, ChargerEnemy<br/>ArmoredEnemy, RegeneratingEnemy<br/>EnemySpec, EnemyFactory, EnemyTrait"]
    gun["<b>gun</b><br/>Gun, Bullet<br/>Pistol, Shotgun<br/>Rifle, Minigun"]
    structure["<b>structure</b><br/>Structure, Buildable<br/>Barricade, Trap<br/>Spike, Landmine, AmmoBox"]
    unit["<b>unit</b><br/>GameEntity, Position<br/>Direction"]

    Main --> game
    Main --> colonist
    Main --> gun

    game --> colonist
    game --> enemy
    game --> gun
    game --> structure

    colonist --> unit
    colonist --> gun
    colonist --> structure
    colonist --> game

    enemy --> unit
    enemy --> structure
    enemy --> game

    gun --> colonist
    gun --> enemy
    gun --> game

    structure --> game
    structure --> enemy
```

### 게임 메카닉

**정착민 패시브 / 특수공격**

| 유형 | 패시브 | 특수공격 (5발마다) |
|------|--------|-------------------|
| 기본 (BASIC) | 없음 | 없음 |
| 사격수 (Gunner) | 속사 (발사 간격 20% 감소) | 속사 탄막 — 전체 적 3 데미지 + 적 위치 노란 X + 탄흔 12개 |
| 저격수 (Sniper) | 치명타 (30% 확률 2배) | 정밀 저격 — HP 최고 적 50 데미지 + 십자 조준선(X/+) + 빨간 탄도선 |
| 돌격수 (Assault) | 넉백 (명중 시 1칸) | 충격파 — 전체 적 5 데미지 + 5칸 넉백 + 적 위치 ! + 충격파 ~ 3줄 + 흔들림 |

**모집 / 승격**

| 명령 | 비용 | 효과 |
|------|------|------|
| 모집 | 40 | BASIC 정착민 즉시 합류 (최대 5명) |
| 승격 | 30 | BASIC → Gunner/Sniper/Assault 서브클래스로 교체 (체력·무기 이전) |

**무기**

| 무기 | 비용 | 간격 | 데미지 | 속도 | 패턴 |
|------|------|------|--------|------|------|
| 피스톨 | 0 | 4틱 | 5 | 3 | 단발 |
| 샷건 | 25 | 6틱 | 3 | 3 | 부채꼴 3발 (±2행) |
| 라이플 | 20 | 5틱 | 8 | 6 | 관통 단발 |
| 미니건 | 30 | 1틱 | 2 | 4 | 매 틱 단발 |

**적 특성 / 특수 스킬**

| 특성 | 서브클래스 | 패시브 | 특수 스킬 (8틱마다) |
|------|-----------|--------|-------------------|
| STANDARD | Enemy (기본) | 없음 | 없음 |
| CHARGER | ChargerEnemy | 바리케이드 8칸 이내 2칸 이동 | 돌진 질주 — 3칸 추가 전진 + 잔상 이펙트 |
| ARMORED | ArmoredEnemy | 받는 데미지 ÷2 | 방패 충돌 — 바리케이드 2배 공격 / 방어 강화 (1회 피해 무효) |
| REGENERATING | RegeneratingEnemy | 3틱마다 +1 HP | 치유 포자 — 모든 적 HP 5 회복 + 초록 이펙트 |

**구조물**

| 구조물 | 비용 | 배치 영역 | 효과 |
|--------|------|----------|------|
| 가시덫 | 20 | 전장 (커서) | 단일 셀, 내구도 10, 적 블록 겹침 시 3 데미지 |
| 지뢰 | 25 | 전장 (커서) | 단일 셀, 일회용, 적 블록 겹침 시 반경 3칸 15 데미지 |
| 탄약 상자 | 20 | 안전지대 (커서) | 단일 셀, 전체 발사속도 30% 증가 |
| 바리케이드 강화 | 15/25 | 즉시 | Lv1→2→3, HP 100→150→200 |

**난이도**

| 난이도 | 적 수 | 보급량 | 승리 일수 |
|--------|-------|--------|-----------|
| 쉬움 | 0.7배 | 1.5배 | 7일 |
| 보통 | 1.0배 | 1.0배 | 10일 |
| 어려움 | 1.3배 | 0.7배 | 15일 |

**낮 이벤트** (2일차부터 랜덤 발생)

| 이벤트 | 효과 |
|--------|------|
| 보급품 발견 | +15 보급품 |
| 떠돌이 합류 | 랜덤 정착민 +30 HP |
| 폭풍 경고 | 다음 밤 일반 몬스터 50% 증가 |
| 평온한 하루 | 효과 없음 |

### UI

**화면 구성**: 왼쪽 100칸 게임 맵 + 오른쪽 정보 패널

**조작**: 터미널 raw 모드 (화살표 키 즉시 입력)
- ↑↓: 정착민 선택
- 1~6: 수리/무기/치료/건설/모집/승격
- n: 밤으로 건너뛰기
- 0: 치트 모드
- q: 종료/취소
- 배치 모드 (건설→구조물 선택 후): ↑↓←→ 커서 이동, Enter 설치, q 취소

**로그 포맷**: `[라벨: 이름] 메시지` (예: `[A: 민수] 충격파!`, `[B: 수진] 늑대 처치!`)

**색상**: ANSI 이스케이프 코드
- 바리케이드: 피격 시 빨강, 수리 시 초록
- 적: HP 비율에 따라 위에서부터 빨간색 채움
- 정착민: 사망 시 회색 → 페이드 아웃 (800ms)
- 총알/이펙트: 무기별 색상
- 치명타: 밝은 빨강 `*` 3칸 (상/중/하)
- 지뢰 폭발: 다이아몬드 이펙트 + 화면 흔들림 (좌우+상하)
- 웨이브 경고: 밤 시작 시 중앙 텍스트 (2초)

**효과음**: javax.sound 사인파 합성 (8kHz/8bit/mono)
- 발사 (800Hz/30ms), 샷건 (150Hz 폭발+600→200Hz 파열), 명중 (300Hz/50ms)
- 치명타 (600→1200Hz/80ms), 폭발 (80Hz/200ms), 사망 (400→100Hz/120ms)
- 웨이브 시작 (600Hz×2)
- 정착민 스킬: 속사 탄막 (1000→800Hz/150ms), 정밀 저격 (800→1600Hz/100ms), 충격파 (100Hz/300ms)
- 적 스킬: 돌진 질주 (500→1000Hz/80ms), 방패 충돌 (200Hz/150ms), 치유 포자 (400→600Hz/120ms)

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
javac -cp lib/jl1.0.1.jar -d out $(find src -name "*.java")

# 실행
java -cp lib/jl1.0.1.jar:out Main
```

**외부 라이브러리**: `lib/jl1.0.1.jar` (JLayer — MP3 디코딩/재생)

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
  - `static` 메서드: 유틸리티 헬퍼(Util)나 팩토리 메서드(Cutscene.intro())에만 사용. 그 외에는 인스턴스 메서드 사용
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
- **열거형은 순수 타입 식별자**: 열거형(enum)에 데이터와 메서드를 넣지 않는다. 데이터는 별도의 Spec 데이터 클래스에, 조회는 Factory 클래스에서 담당
  ```java
  // 나쁜 예: 열거형이 데이터까지 보유
  public enum EnemyType {
      WOLF("늑대", 30, 3, 400, ...);
      private final String name;
      public String getName() { return name; }
  }

  // 좋은 예: 열거형은 타입만, 데이터는 분리
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
