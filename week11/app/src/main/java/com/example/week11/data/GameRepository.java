package com.example.week11.data;

import com.example.week11.model.Game;
import com.example.week11.model.GameStatus;
import com.example.week11.model.Genre;
import com.example.week11.model.Platform;
import com.example.week11.model.TrashEntry;

import java.util.ArrayList;

/// <summary>
/// 게임 저장소
/// 더미 게임 데이터를 제공하고, ID로 게임을 찾는 기능을 담당
/// Unity로 비유하면 테스트용 ScriptableObject 리스트를 하드코딩해둔 매니저 클래스
///
/// 현재(8주차)는 하드코딩 더미 데이터만 사용
/// </summary>
public class GameRepository {

    /// <summary>
    /// 더미 게임 목록
    /// Unity로 비유하면 Inspector에 직접 드래그해 넣어둔 GameData 배열
    /// </summary>
    private final ArrayList<Game> games;

    /// <summary>
    /// 휴지통 — 삭제됐지만 아직 영구삭제되지 않은 게임들 (복원 가능)
    /// 짧은 실행취소 창이 지나면 여기로 옮겨지고, 휴지통 화면에서 복원/영구삭제한다
    /// 각 항목에 "버려진 시각"을 함께 저장해 30일 지난 것은 자동으로 정리한다
    /// </summary>
    private final ArrayList<TrashEntry> trashedEntries = new ArrayList<>();

    /// <summary>
    /// 휴지통 보관 기간 — 이 기간이 지난 항목은 자동으로 영구삭제 (30일)
    /// 주의: 게임 데이터는 현재 메모리에만 있어 앱을 완전히 끄면 초기화됨 →
    ///       실제로 30일을 채우려면 앱이 그동안 살아있어야 함. 동작을 테스트하려면
    ///       이 값을 잠깐 짧게(예: 10_000L = 10초) 낮춰 확인하면 된다.
    /// </summary>
    public static final long TRASH_RETENTION_MS = 30L * 24 * 60 * 60 * 1000;

    /// <summary>
    /// 새로 추가될 게임에 부여할 ID
    /// 초기 더미 4개가 1~4를 쓰므로 5부터 시작
    /// 게임 추가 시마다 1씩 증가 (간단한 자동 증가 방식)
    /// 10주차에 Room DB 도입하면 DB의 AUTO_INCREMENT로 대체될 예정
    /// </summary>
    private int nextId;

    // ========== 생성자 ==========

    /// <summary>
    /// 저장소 생성 및 더미 데이터 초기화
    /// 모두 Steam 게임으로 통일 (Steam CDN에서 표지 이미지 직접 가져오기 용이)
    /// 발표 데모용으로 리뷰가 있는 것(완료)과 없는 것(백로그)을 섞어둠
    /// </summary>
    public GameRepository() {
        this.games = new ArrayList<>();
        seedDefaultGames();
    }

    /// <summary>
    /// 게임 목록을 하드코딩 초기 데이터로 (다시) 채운다
    /// 생성자에서 처음 채울 때 + 전체 초기화로 원상복구할 때 공용으로 사용한다
    /// 기존 목록을 비우고 20개를 처음 그대로 다시 넣으므로,
    /// 세션 중 바뀐 상태/별점/스크린샷/추가한 게임이 모두 초기값으로 되돌아간다
    /// </summary>
    private void seedDefaultGames() {
        this.games.clear();
        this.nextId = 21;  // 더미 20개가 1~20 사용하므로 21부터 시작

        this.games.add(new Game(
                1,
                "엘든 링",
                "cover_eldenring",
                Genre.RPG,
                Platform.STEAM,
                "https://store.steampowered.com/app/1245620/ELDEN_RING/",
                GameStatus.COMPLETED,
                5.0f,
                "프롬소프트의 정점. 죽고 배우는 쾌감이 끝내준다"
        ));

        this.games.add(new Game(
                2,
                "발더스 게이트 3",
                "cover_baldursgate3",
                Genre.RPG,
                Platform.STEAM,
                "https://store.steampowered.com/app/1086940/Baldurs_Gate_3/",
                GameStatus.PLAYING,
                4.5f,
                "선택 하나하나가 결과로 이어지는 진짜 TRPG 경험"
        ));

        this.games.add(new Game(
                3,
                "할로우 나이트",
                "cover_hollowknight",
                Genre.PLATFORMER,
                Platform.STEAM,
                "https://store.steampowered.com/app/367520/Hollow_Knight/",
                GameStatus.COMPLETED,
                5.0f,
                "인디 메트로배니아의 정점. 아트와 음악이 압도적"
        ));

        // 아직 리뷰 안 쓴 게임 — 백로그 상태 시연용
        this.games.add(new Game(
                4,
                "셀레스테",
                "cover_celeste",
                Genre.PLATFORMER,
                Platform.STEAM,
                "https://store.steampowered.com/app/504230/Celeste/",
                GameStatus.BACKLOG,
                0f,
                ""
        ));

        // ──── RecyclerView 테스트용 추가 더미 16종 (상태/장르/별점 다양하게) ────
        this.games.add(new Game(5, "스타듀 밸리", "cover_stardewvalley",
                Genre.SIMULATION, Platform.STEAM,
                "https://store.steampowered.com/app/413150/",
                GameStatus.PLAYING, 4.5f, "힐링과 중독의 완벽한 균형"));

        this.games.add(new Game(6, "하데스", "cover_hades",
                Genre.ACTION, Platform.STEAM,
                "https://store.steampowered.com/app/1145360/",
                GameStatus.COMPLETED, 5.0f, "로그라이크의 교과서"));

        this.games.add(new Game(7, "디스코 엘리시움", "cover_discoelysium",
                Genre.RPG, Platform.STEAM,
                "https://store.steampowered.com/app/632470/",
                GameStatus.COMPLETED, 4.5f, "텍스트로 이렇게 몰입될 수 있다니"));

        this.games.add(new Game(8, "테라리아", "cover_terraria",
                Genre.ADVENTURE, Platform.STEAM,
                "https://store.steampowered.com/app/105600/",
                GameStatus.DROPPED, 4.0f, "자유도는 최고인데 끝이 없어 지침"));

        this.games.add(new Game(9, "포탈 2", "cover_portal2",
                Genre.PUZZLE, Platform.STEAM,
                "https://store.steampowered.com/app/620/",
                GameStatus.COMPLETED, 5.0f, "퍼즐 게임의 정점"));

        this.games.add(new Game(10, "위쳐 3", "cover_witcher3",
                Genre.RPG, Platform.STEAM,
                "https://store.steampowered.com/app/292030/",
                GameStatus.BACKLOG, 0f, ""));

        this.games.add(new Game(11, "사이버펑크 2077", "cover_cyberpunk2077",
                Genre.RPG, Platform.STEAM,
                "https://store.steampowered.com/app/1091500/",
                GameStatus.PLAYING, 4.0f, "패치 후 정말 좋아졌다"));

        this.games.add(new Game(12, "다크 소울 3", "cover_darksouls3",
                Genre.RPG, Platform.STEAM,
                "https://store.steampowered.com/app/374320/",
                GameStatus.COMPLETED, 4.5f, "소울 시리즈의 완성형"));

        this.games.add(new Game(13, "림월드", "cover_rimworld",
                Genre.SIMULATION, Platform.STEAM,
                "https://store.steampowered.com/app/294100/",
                GameStatus.PLAYING, 4.5f, "내 식민지 이야기는 끝이 없다"));

        this.games.add(new Game(14, "팩토리오", "cover_factorio",
                Genre.SIMULATION, Platform.STEAM,
                "https://store.steampowered.com/app/427520/",
                GameStatus.DROPPED, 4.0f, "공장 자동화 중독, 시간 순삭"));

        this.games.add(new Game(15, "슬레이 더 스파이어", "cover_slaythespire",
                Genre.STRATEGY, Platform.STEAM,
                "https://store.steampowered.com/app/646570/",
                GameStatus.BACKLOG, 0f, ""));

        this.games.add(new Game(16, "언더테일", "cover_undertale",
                Genre.RPG, Platform.STEAM,
                "https://store.steampowered.com/app/391540/",
                GameStatus.COMPLETED, 5.0f, "한 번쯤은 꼭 해봐야 할 게임"));

        this.games.add(new Game(17, "컵헤드", "cover_cuphead",
                Genre.PLATFORMER, Platform.STEAM,
                "https://store.steampowered.com/app/268910/",
                GameStatus.DROPPED, 3.5f, "너무 어려워서 잠시 중단"));

        this.games.add(new Game(18, "오리와 눈먼 숲", "cover_oriblindforest",
                Genre.PLATFORMER, Platform.STEAM,
                "https://store.steampowered.com/app/387290/",
                GameStatus.COMPLETED, 4.5f, "아트와 음악이 동화 같다"));

        this.games.add(new Game(19, "데드 셀", "cover_deadcells",
                Genre.PLATFORMER, Platform.STEAM,
                "https://store.steampowered.com/app/588650/",
                GameStatus.PLAYING, 4.0f, "죽고 또 죽어도 또 한판"));

        this.games.add(new Game(20, "발헤임", "cover_valheim",
                Genre.ADVENTURE, Platform.STEAM,
                "https://store.steampowered.com/app/892970/",
                GameStatus.BACKLOG, 0f, ""));
    }

    // ========== 전체 초기화 (시연용) ==========

    /// <summary>
    /// 전체 초기화: 세션 중 바뀐 게임 상태/별점/스크린샷/추가한 게임을
    /// 하드코딩 초기 데이터로 되돌린다
    ///
    /// GameRepository는 메모리 위의 더미 데이터라 SharedPreferences clear로는 안 지워진다.
    /// (앱 프로세스를 완전히 죽여야만 생성자가 다시 불려 초기화됨)
    /// → 프로세스를 죽이지 않고도 "새 설치" 상태를 만들기 위해 여기서 직접 다시 채운다
    /// </summary>
    public void resetToDefault() {
        seedDefaultGames();
        trashedEntries.clear();   // 휴지통도 함께 비운다 (완전한 "새 설치" 상태)
    }

    // ========== 조회 ==========

    /// <summary>
    /// 전체 게임 목록 반환
    /// 카드/그리드 리스트를 만들 때 사용
    /// </summary>
    public ArrayList<Game> getAllGames() {
        return this.games;
    }

    /// <summary>
    /// 전체 게임 개수 반환 (홈 통계 카드 등에서 사용)
    /// </summary>
    public int getTotalCount() {
        return this.games.size();
    }

    /// <summary>
    /// 특정 상태인 게임 개수 반환 (홈 통계 카드에서 상태별 집계에 사용)
    /// </summary>
    public int countByStatus(GameStatus status) {
        int count = 0;
        for (Game game : this.games) {
            if (game.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    /// <summary>
    /// 별점 분포 집계 (홈 별점 분포 그래프에 사용)
    /// 0.5 단위 10단계로 세어 배열로 반환
    ///   index 0 = 0.5점, index 1 = 1.0점, ... index 9 = 5.0점
    /// 미평가(별점 0) 게임은 분포에서 제외 (아직 평가 안 한 것이므로)
    /// </summary>
    public int[] getRatingDistribution() {
        int[] distribution = new int[10];
        for (Game game : this.games) {
            float rating = game.getRating();
            if (rating <= 0f) {
                continue;  // 미평가 제외
            }
            // 0.5 → 0, 1.0 → 1, ... 5.0 → 9 로 변환
            int index = Math.round(rating / 0.5f) - 1;
            if (index >= 0 && index < distribution.length) {
                distribution[index]++;
            }
        }
        return distribution;
    }

    /// <summary>
    /// 특정 장르의 게임 개수 반환 (통계 화면 장르 분포에 사용)
    /// </summary>
    public int countByGenre(Genre genre) {
        int count = 0;
        for (Game game : this.games) {
            if (game.getGenre() == genre) {
                count++;
            }
        }
        return count;
    }

    /// <summary>
    /// 아직 별점을 매기지 않은(미평가) 게임 개수 반환
    /// 별점 분포에서 제외된 게임들을 "미평가" 항목으로 따로 보여줄 때 사용
    /// </summary>
    public int countUnrated() {
        int count = 0;
        for (Game game : this.games) {
            if (game.getRating() <= 0f) {
                count++;
            }
        }
        return count;
    }

    /// <summary>
    /// ID로 게임 찾기
    /// 해당 ID의 게임이 없으면 null 반환
    /// </summary>
    public Game findById(int id) {
        for (Game game : this.games) {
            if (game.getId() == id) {
                return game;
            }
        }
        return null;
    }

    // ========== 추가 ==========

    /// <summary>
    /// 새 게임을 라이브러리에 추가
    /// AddGameActivity에서 사용자가 입력한 정보로 호출됨
    ///
    /// ID는 내부적으로 nextId 값이 자동 부여되고 1 증가
    /// 표지 이미지는 아직 API 연동 전이라 빈 문자열 (기본 아이콘 표시됨)
    /// 별점과 한줄평은 초기값 (0, 빈 문자열) — 사용자가 추후 ReviewWrite에서 입력
    ///
    /// 현재(8주차)는 메모리에만 저장되므로 앱 재시작 시 사라짐
    /// → 10주차에 Room DB 도입하면 영속 저장으로 교체 예정
    /// </summary>
    public Game addGame(String title, Genre genre, Platform platform, String storeUrl) {
        Game newGame = new Game(
                this.nextId,
                title,
                "",                 // coverAssetName — 아직 없음, 기본 아이콘 사용
                genre,
                platform,
                storeUrl == null ? "" : storeUrl,
                GameStatus.BACKLOG, // 새로 추가한 게임은 "백로그"(하고 싶은 목록)로 시작
                0f,                 // 초기 별점
                ""                  // 초기 한줄평
        );
        this.games.add(newGame);
        this.nextId++;
        return newGame;
    }

    // ========== 갱신 ==========

    /// <summary>
    /// 외부에서 수정된 Game 사본의 변경사항을 원본에 반영
    /// id로 원본을 찾아 가변 필드(rating/review/screenshots)를 갱신
    ///
    /// ──── 왜 필요한가 ────
    /// Activity끼리는 Game을 Parcelable로 주고받는데, Parcelable은 매번 새 객체로 복원되므로
    /// 받는 Activity의 Game은 Repository 원본과 별개의 사본임
    /// → 사본을 수정해도 Repository는 그대로
    /// → MainActivity의 onResume에서 Repository를 다시 읽으면 변경사항이 보이지 않는 문제 발생
    ///
    /// 그래서 Game을 수정한 Activity가 finish 직전(또는 결과를 받은 호출자가 결과 처리 시점)에
    /// 이 메서드를 호출해서 원본에 명시적으로 변경사항을 적어야 함
    ///
    /// ──── 무엇을 갱신하는가 ────
    /// 가변 필드: rating, review, screenshots
    /// 불변 필드(id/title/coverAsset/genre/platform/storeUrl)는 final이라 변경 불가 → 무시
    ///
    /// id 매칭이 안 되면(있을 수 없는 상황) 조용히 무시 — 호출자 측 버그를 의미
    /// </summary>
    public void updateGame(Game updated) {
        if (updated == null) {
            return;
        }

        Game original = findById(updated.getId());
        if (original == null) {
            return;
        }

        original.setStatus(updated.getStatus());
        original.setRating(updated.getRating());
        original.setReview(updated.getReview());
        original.replaceScreenshots(updated.getScreenshots());
    }

    // ========== 삭제 ==========

    /// <summary>
    /// id로 게임을 라이브러리에서 제거
    /// 호출자(어댑터)가 notifyItemRemoved에 쓸 수 있도록 제거된 position을 반환
    ///
    /// 반환값
    ///   ≥ 0 : 제거에 성공한 경우 그 항목이 있던 인덱스
    ///   -1  : 해당 id를 찾지 못한 경우 (이미 지워졌거나 잘못된 id)
    /// </summary>
    public int removeGame(int id) {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getId() == id) {
                games.remove(i);
                return i;
            }
        }
        return -1;
    }

    /// <summary>
    /// 게임을 원래 위치(index)에 다시 끼워 넣는다 (삭제 → 실행취소 복원용)
    /// index가 범위를 벗어나면 맨 뒤에 붙인다 → 항상 안전
    /// </summary>
    public void insertGame(int index, Game game) {
        if (index < 0 || index > games.size()) {
            games.add(game);
        } else {
            games.add(index, game);
        }
    }

    // ========== 휴지통 (소프트 삭제) ==========

    /// <summary>
    /// 게임을 휴지통으로 이동 (영구삭제가 아니라 복원 가능한 상태)
    /// 삭제 스낵바가 실행취소 없이 닫힐 때 호출됨
    /// nowMillis: 버린 시각 (호출자가 System.currentTimeMillis()로 넘김 — 저장소는 시계에 직접 의존 안 함)
    /// </summary>
    public void moveToTrash(Game game, long nowMillis) {
        trashedEntries.add(new TrashEntry(game, nowMillis));
    }

    /// <summary>
    /// 휴지통 항목 목록 반환 (게임 + 버린 시각) — 휴지통 화면 표시용
    /// </summary>
    public ArrayList<TrashEntry> getTrashedEntries() {
        return this.trashedEntries;
    }

    /// <summary>
    /// 휴지통의 게임을 보관함으로 복원 (맨 뒤에 추가) — 성공 시 그 Game, 없으면 null
    /// </summary>
    public Game restoreFromTrash(int id) {
        for (int i = 0; i < trashedEntries.size(); i++) {
            if (trashedEntries.get(i).getGame().getId() == id) {
                Game game = trashedEntries.remove(i).getGame();
                games.add(game);
                return game;
            }
        }
        return null;
    }

    /// <summary>
    /// 휴지통의 게임 하나를 영구삭제 (완전히 제거)
    /// </summary>
    public void deleteFromTrashPermanently(int id) {
        for (int i = 0; i < trashedEntries.size(); i++) {
            if (trashedEntries.get(i).getGame().getId() == id) {
                trashedEntries.remove(i);
                return;
            }
        }
    }

    /// <summary>
    /// 휴지통 비우기 (전부 영구삭제)
    /// </summary>
    public void emptyTrash() {
        trashedEntries.clear();
    }

    /// <summary>
    /// 보관 기간(TRASH_RETENTION_MS)이 지난 휴지통 항목을 자동으로 영구삭제
    /// nowMillis: 현재 시각 (호출자가 System.currentTimeMillis()로 넘김)
    /// 휴지통 화면을 열 때 호출해 "30일 지난 것"을 정리한다
    /// </summary>
    public void purgeExpiredTrash(long nowMillis) {
        trashedEntries.removeIf(
                entry -> nowMillis - entry.getTrashedAt() >= TRASH_RETENTION_MS);
    }
}
