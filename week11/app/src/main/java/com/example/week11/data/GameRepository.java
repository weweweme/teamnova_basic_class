package com.example.week11.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.week11.model.Game;
import com.example.week11.model.GameStatus;
import com.example.week11.model.Genre;
import com.example.week11.model.Platform;
import com.example.week11.model.TrashEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    /// 휴지통 상태(삭제된 id + 버린 시각)는 prefs에 저장되므로 앱을 껐다 켜도 유지된다 →
    /// 앱 시작 시 이 기간이 지난 것이 자동 정리됨.
    /// 동작을 빨리 테스트하려면 이 값을 잠깐 짧게(예: 10_000L = 10초) 낮춰 확인하면 된다.
    /// </summary>
    public static final long TRASH_RETENTION_MS = 30L * 24 * 60 * 60 * 1000;

    /// <summary>
    /// 휴지통/삭제 상태를 저장하는 prefs 파일 이름 (게임은 시드로 매번 새로 만들어지므로,
    /// "무엇이 삭제됐는지"만 따로 저장해 앱을 껐다 켜도 삭제 상태가 유지되게 함)
    /// </summary>
    private static final String PREFS_FILE = "game_trash";

    /// <summary>
    /// 휴지통에 있는 항목들: "게임id|버린시각" 문자열 집합
    /// </summary>
    private static final String KEY_TRASHED = "trashed";

    /// <summary>
    /// 영구삭제된 게임 id 문자열 집합 (시드로 되살아나지 않게 시작 시 라이브러리에서 제외)
    /// </summary>
    private static final String KEY_DELETED = "deleted";

    /// <summary>
    /// 사용자가 추가한 게임들: 게임 하나를 JSON 문자열로 만든 것들의 집합
    /// (시드 20개는 코드로 매번 만들지만, 추가 게임은 코드에 없으니 통째로 저장해야 살아남음)
    /// </summary>
    private static final String KEY_ADDED = "added_games";

    /// <summary>
    /// 다음에 부여할 게임 id — 추가 게임 id가 재시작 후에도 안 겹치게 저장
    /// </summary>
    private static final String KEY_NEXT_ID = "next_id";

    /// <summary>
    /// 상태가 바뀐 게임들: "게임id|상태이름" 문자열 집합
    /// (시드 게임은 매번 기본 상태로 다시 만들어지므로, 바뀐 상태만 따로 저장해 시작 시 덮어씀)
    /// </summary>
    private static final String KEY_STATUS = "status_overrides";

    /// <summary>
    /// 상태 변경 override (게임id → 바뀐 상태). prefs와 동기화되는 메모리 상태
    /// 시드/추가 구분 없이, updateGame으로 상태가 바뀌면 여기에 기록하고 시작 시 다시 적용
    /// </summary>
    private final Map<Integer, GameStatus> statusOverrides = new HashMap<>();

    /// <summary>
    /// 스크린샷 변경을 저장하는 prefs 키 (하나의 JSON 문자열: {게임id: [uri...]})
    /// </summary>
    private static final String KEY_SCREENSHOTS = "screenshot_overrides";

    /// <summary>
    /// 스크린샷 override (게임id → uri 목록). updateGame으로 스크린샷이 바뀌면 기록하고 시작 시 적용
    /// 저장된 목록은 기본 스크린샷 + 사용자가 더한 것을 통째로 담음 (그대로 덮어씀)
    /// </summary>
    private final Map<Integer, List<String>> screenshotOverrides = new HashMap<>();

    /// <summary>
    /// 표지 URI 변경을 저장하는 prefs 키 (하나의 JSON 문자열: {게임id: "uri"})
    /// </summary>
    private static final String KEY_COVER = "cover_overrides";

    /// <summary>
    /// 표지 URI override (게임id → 새 표지 uri). 시드/추가 구분 없이 표지를 바꾸면 기록·적용
    /// </summary>
    private final Map<Integer, String> coverUriOverrides = new HashMap<>();

    /// <summary>
    /// 삭제 상태를 저장/불러오는 prefs (앱 껐다 켜도 삭제가 유지됨)
    /// </summary>
    private final SharedPreferences prefs;

    /// <summary>
    /// 앱 패키지명 — 번들된 기본 스크린샷을 android.resource:// URI로 참조할 때 사용
    /// </summary>
    private final String packageName;

    /// <summary>
    /// 영구삭제된 게임 id 목록 (메모리 상태 — prefs와 동기화)
    /// 앱 시작 시 시드된 20개 중 이 id들은 라이브러리에서 빠진다
    /// </summary>
    private final Set<Integer> deletedIds = new HashSet<>();

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
    public GameRepository(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        this.packageName = context.getPackageName();
        this.games = new ArrayList<>();
        seedDefaultGames();
        // 인기 시드 게임 몇 개에 번들된 기본 스크린샷을 붙임 (데모용 — 비어 보이지 않게)
        attachDefaultScreenshots();
        // 시드 뒤, 저장돼 있던 "추가 게임 / 삭제 / 휴지통 상태"를 반영 (앱을 껐다 켜도 유지됨)
        loadPersistedState();
    }

    /// <summary>
    /// 일부 시드 게임에 번들된 기본 스크린샷(res/drawable의 shot_*.jpg)을 붙인다
    /// android.resource:// URI로 넣으면 스크린샷 로더(loadUri)가 그대로 디코딩해준다
    /// (Steam에서 받아 번들한 데모용 이미지 — 실제 사용자 스크린샷은 여기에 더해짐)
    /// </summary>
    private void attachDefaultScreenshots() {
        addDefaultShots(1, "shot_eldenring_1", "shot_eldenring_2", "shot_eldenring_3",
                "shot_eldenring_4", "shot_eldenring_5", "shot_eldenring_6");
        addDefaultShots(3, "shot_hollowknight_1", "shot_hollowknight_2", "shot_hollowknight_3",
                "shot_hollowknight_4", "shot_hollowknight_5", "shot_hollowknight_6");
        addDefaultShots(4, "shot_celeste_1", "shot_celeste_2", "shot_celeste_3",
                "shot_celeste_4", "shot_celeste_5", "shot_celeste_6");
        addDefaultShots(5, "shot_stardewvalley_1", "shot_stardewvalley_2", "shot_stardewvalley_3",
                "shot_stardewvalley_4", "shot_stardewvalley_5", "shot_stardewvalley_6");
        addDefaultShots(6, "shot_hades_1", "shot_hades_2", "shot_hades_3",
                "shot_hades_4", "shot_hades_5", "shot_hades_6");
        addDefaultShots(17, "shot_cuphead_1", "shot_cuphead_2", "shot_cuphead_3",
                "shot_cuphead_4", "shot_cuphead_5", "shot_cuphead_6");
    }

    /// <summary>
    /// 특정 게임에 drawable 리소스 이름들을 android.resource:// URI로 스크린샷에 추가
    /// </summary>
    private void addDefaultShots(int gameId, String... resNames) {
        Game game = findById(gameId);
        if (game == null) {
            return;
        }
        for (String resName : resNames) {
            game.addScreenshot("android.resource://" + packageName + "/drawable/" + resName);
        }
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
        seedDefaultGames();   // games 20개 + nextId 21로 복구
        // 추가 게임·휴지통·영구삭제·상태변경을 전부 비워 "새 설치" 상태로 (저장된 것도 지움)
        trashedEntries.clear();
        deletedIds.clear();
        statusOverrides.clear();
        screenshotOverrides.clear();
        coverUriOverrides.clear();
        prefs.edit()
                .remove(KEY_TRASHED)
                .remove(KEY_DELETED)
                .remove(KEY_ADDED)
                .remove(KEY_NEXT_ID)
                .remove(KEY_STATUS)
                .remove(KEY_SCREENSHOTS)
                .remove(KEY_COVER)
                .apply();
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
    public Game addGame(String title, Genre genre, Platform platform, String storeUrl,
                        String coverUri) {
        Game newGame = new Game(
                this.nextId,
                title,
                "",                 // coverAssetName — 번들 표지는 없음 (coverUri를 대신 씀)
                genre,
                platform,
                storeUrl == null ? "" : storeUrl,
                GameStatus.BACKLOG, // 새로 추가한 게임은 "백로그"(하고 싶은 목록)로 시작
                0f,                 // 초기 별점
                ""                  // 초기 한줄평
        );
        // 사용자가 표지 이미지를 골랐으면 URI 설정 (없으면 null → 기본 아이콘)
        newGame.setCoverUri(coverUri);
        this.games.add(newGame);
        this.nextId++;
        // 추가 게임은 코드 시드에 없으니 통째로 저장해야 재시작 후에도 남음
        persistAddedGame(newGame);
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

        // 상태 변경을 delta로 저장 → 재시작해도 유지됨 (시드 게임도 바뀐 상태로 뜸)
        statusOverrides.put(original.getId(), original.getStatus());
        persistStatusOverrides();

        // 스크린샷 변경도 delta로 저장 (사용자가 추가/삭제한 스크린샷이 재시작해도 유지)
        screenshotOverrides.put(original.getId(), new ArrayList<>(original.getScreenshots()));
        persistScreenshots();

        // 표지 변경(새 URI가 있을 때만)도 delta로 저장 → 재시작해도 바뀐 표지 유지
        original.setCoverUri(updated.getCoverUri());
        String newCover = updated.getCoverUri();
        if (newCover != null && !newCover.isEmpty()) {
            coverUriOverrides.put(original.getId(), newCover);
            persistCoverOverrides();
        }
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

    // ========== 휴지통 (소프트 삭제, prefs에 영속) ==========

    /// <summary>
    /// 게임을 휴지통으로 이동 (영구삭제가 아니라 복원 가능한 상태)
    /// 삭제 스낵바가 실행취소 없이 닫힐 때 호출됨
    /// nowMillis: 버린 시각 (호출자가 System.currentTimeMillis()로 넘김 — 저장소는 시계에 직접 의존 안 함)
    /// </summary>
    public void moveToTrash(Game game, long nowMillis) {
        trashedEntries.add(new TrashEntry(game, nowMillis));
        persistTrashState();
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
                persistTrashState();
                return game;
            }
        }
        return null;
    }

    /// <summary>
    /// 휴지통의 게임 하나를 영구삭제 (완전히 제거)
    /// id를 "영구삭제 목록"에 넣어, 다음 앱 시작 때 시드로 되살아나지 않게 한다
    /// </summary>
    public void deleteFromTrashPermanently(int id) {
        for (int i = 0; i < trashedEntries.size(); i++) {
            if (trashedEntries.get(i).getGame().getId() == id) {
                trashedEntries.remove(i);
                deletedIds.add(id);
                persistTrashState();
                return;
            }
        }
    }

    /// <summary>
    /// 휴지통 비우기 (전부 영구삭제)
    /// </summary>
    public void emptyTrash() {
        for (TrashEntry entry : trashedEntries) {
            deletedIds.add(entry.getGame().getId());
        }
        trashedEntries.clear();
        persistTrashState();
    }

    /// <summary>
    /// 보관 기간(TRASH_RETENTION_MS)이 지난 휴지통 항목을 자동으로 영구삭제
    /// nowMillis: 현재 시각 (호출자가 System.currentTimeMillis()로 넘김)
    /// 앱 시작 시 / 휴지통 화면 열 때 호출해 "30일 지난 것"을 정리한다
    /// </summary>
    public void purgeExpiredTrash(long nowMillis) {
        boolean changed = false;
        // 뒤에서부터 지우면 인덱스가 안 밀린다
        for (int i = trashedEntries.size() - 1; i >= 0; i--) {
            TrashEntry entry = trashedEntries.get(i);
            if (nowMillis - entry.getTrashedAt() >= TRASH_RETENTION_MS) {
                deletedIds.add(entry.getGame().getId());   // 만료 → 영구삭제로 확정
                trashedEntries.remove(i);
                changed = true;
            }
        }
        if (changed) {
            persistTrashState();
        }
    }

    // ========== 휴지통 영속화 (prefs 저장/불러오기) ==========

    /// <summary>
    /// prefs에 저장돼 있던 추가게임/삭제/휴지통 상태를 메모리에 반영 (생성자에서 시드 뒤 호출)
    /// 순서가 중요:
    ///   1) 영구삭제 id 로드 → 2) nextId 로드 → 3) 추가 게임 복원(삭제된 건 제외)
    ///   → 4) 삭제된 게임을 라이브러리에서 제거 → 5) 휴지통 복원(시드+추가 게임 모두 대상)
    /// </summary>
    private void loadPersistedState() {
        // 1) 영구삭제된 id 집합
        for (String idStr : prefs.getStringSet(KEY_DELETED, new HashSet<>())) {
            int id = parseIntSafe(idStr, -1);
            if (id >= 0) {
                deletedIds.add(id);
            }
        }

        // 2) 다음 id 복원 (추가 게임 id가 재시작 후에도 안 겹치게). 없으면 시드가 정한 값 유지
        nextId = prefs.getInt(KEY_NEXT_ID, nextId);

        // 2-1) 상태 override 로드 (게임들을 다 채운 뒤 맨 마지막에 적용)
        for (String token : prefs.getStringSet(KEY_STATUS, new HashSet<>())) {
            int sep = token.indexOf('|');
            if (sep <= 0) {
                continue;
            }
            int id = parseIntSafe(token.substring(0, sep), -1);
            GameStatus status = parseStatusSafe(token.substring(sep + 1));
            if (id >= 0 && status != null) {
                statusOverrides.put(id, status);
            }
        }

        // 3) 사용자가 추가했던 게임 복원 (이미 삭제된 것은 제외)
        for (String json : prefs.getStringSet(KEY_ADDED, new HashSet<>())) {
            Game g = gameFromJson(json);
            if (g != null && !deletedIds.contains(g.getId())) {
                games.add(g);
            }
        }

        // 4) 삭제된 게임을 라이브러리에서 제거 (시드로 되살아난 것 등)
        for (int id : deletedIds) {
            removeGameFromLibrary(id);
        }

        // 5) 휴지통에 있던 것들: 라이브러리에서 빼서 TrashEntry로 되살림
        for (String token : prefs.getStringSet(KEY_TRASHED, new HashSet<>())) {
            // "id|시각" 형태로 저장돼 있음
            int sep = token.indexOf('|');
            if (sep <= 0) {
                continue;
            }
            int id = parseIntSafe(token.substring(0, sep), -1);
            long trashedAt = parseLongSafe(token.substring(sep + 1), 0L);
            if (id < 0 || deletedIds.contains(id)) {
                continue;
            }
            Game game = findById(id);   // 시드 또는 복원된 추가 게임 중에서 찾음
            if (game != null) {
                removeGameFromLibrary(id);
                trashedEntries.add(new TrashEntry(game, trashedAt));
            }
        }

        // 6) 저장된 상태 override를 라이브러리에 남은 게임들에 적용 (시드 게임의 상태 변경 유지)
        for (Map.Entry<Integer, GameStatus> entry : statusOverrides.entrySet()) {
            Game game = findById(entry.getKey());
            if (game != null) {
                game.setStatus(entry.getValue());
            }
        }

        // 7) 저장된 스크린샷 override 로드 + 적용 (기본 스크린샷을 덮어씀 = 사용자 변경 유지)
        loadScreenshotOverrides();
        for (Map.Entry<Integer, List<String>> entry : screenshotOverrides.entrySet()) {
            Game game = findById(entry.getKey());
            if (game != null) {
                game.replaceScreenshots(entry.getValue());
            }
        }

        // 8) 저장된 표지 override 로드 + 적용 (사용자가 바꾼 표지 유지)
        loadCoverOverrides();
        for (Map.Entry<Integer, String> entry : coverUriOverrides.entrySet()) {
            Game game = findById(entry.getKey());
            if (game != null) {
                game.setCoverUri(entry.getValue());
            }
        }
    }

    /// <summary>
    /// prefs에 저장된 표지 override(JSON {"게임id":"uri"})를 메모리 맵으로 읽어옴
    /// </summary>
    private void loadCoverOverrides() {
        String json = prefs.getString(KEY_COVER, null);
        if (json == null) {
            return;
        }
        try {
            JSONObject root = new JSONObject(json);
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                int id = parseIntSafe(key, -1);
                if (id >= 0) {
                    coverUriOverrides.put(id, root.getString(key));
                }
            }
        } catch (Exception e) {
            // 손상된 데이터는 무시
        }
    }

    /// <summary>
    /// 현재 표지 override 전체를 하나의 JSON 문자열로 prefs에 저장
    /// </summary>
    private void persistCoverOverrides() {
        JSONObject root = new JSONObject();
        try {
            for (Map.Entry<Integer, String> entry : coverUriOverrides.entrySet()) {
                root.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        } catch (JSONException e) {
            // 직렬화 실패는 무시
        }
        prefs.edit().putString(KEY_COVER, root.toString()).apply();
    }

    /// <summary>
    /// prefs에 저장된 스크린샷 override(JSON)를 메모리 맵으로 읽어옴
    /// 형식: {"게임id": ["uri1","uri2",...], ...}
    /// </summary>
    private void loadScreenshotOverrides() {
        String json = prefs.getString(KEY_SCREENSHOTS, null);
        if (json == null) {
            return;
        }
        // 저장된 JSON이 손상됐을 수 있어 넓게 catch
        try {
            JSONObject root = new JSONObject(json);
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                int id = parseIntSafe(key, -1);
                if (id < 0) {
                    continue;
                }
                JSONArray arr = root.optJSONArray(key);
                List<String> list = new ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(arr.getString(i));
                    }
                }
                screenshotOverrides.put(id, list);
            }
        } catch (Exception e) {
            // 손상된 데이터는 무시
        }
    }

    /// <summary>
    /// 현재 스크린샷 override 전체를 하나의 JSON 문자열로 prefs에 저장 (변경 시마다 호출)
    /// </summary>
    private void persistScreenshots() {
        JSONObject root = new JSONObject();
        try {
            for (Map.Entry<Integer, List<String>> entry : screenshotOverrides.entrySet()) {
                JSONArray arr = new JSONArray();
                for (String uri : entry.getValue()) {
                    arr.put(uri);
                }
                root.put(String.valueOf(entry.getKey()), arr);
            }
        } catch (JSONException e) {
            // 직렬화 실패는 무시 (거의 없음)
        }
        prefs.edit().putString(KEY_SCREENSHOTS, root.toString()).apply();
    }

    /// <summary>
    /// 현재 상태 override 목록을 prefs에 통째로 다시 저장 (updateGame에서 상태가 바뀔 때마다 호출)
    /// </summary>
    private void persistStatusOverrides() {
        Set<String> raw = new HashSet<>();
        for (Map.Entry<Integer, GameStatus> entry : statusOverrides.entrySet()) {
            raw.add(entry.getKey() + "|" + entry.getValue().name());
        }
        prefs.edit().putStringSet(KEY_STATUS, raw).apply();
    }

    /// <summary>
    /// 문자열을 GameStatus로 안전 변환 (저장된 이름이 손상된 경우 대비 — 실패하면 null)
    /// </summary>
    private GameStatus parseStatusSafe(String name) {
        try {
            return GameStatus.valueOf(name.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /// <summary>
    /// 추가한 게임 하나를 JSON으로 만들어 저장 목록(KEY_ADDED)에 넣고, nextId도 함께 저장
    /// (StringSet은 반환된 그대로 수정하면 안 돼서 복사본을 만들어 수정 후 저장)
    /// </summary>
    private void persistAddedGame(Game game) {
        String json = gameToJson(game);
        if (json == null) {
            return;
        }
        Set<String> added = new HashSet<>(prefs.getStringSet(KEY_ADDED, new HashSet<>()));
        added.add(json);
        prefs.edit()
                .putStringSet(KEY_ADDED, added)
                .putInt(KEY_NEXT_ID, nextId)
                .apply();
    }

    /// <summary>
    /// 게임 하나를 JSON 문자열로 직렬화 (추가 게임 저장용). 실패하면 null
    /// </summary>
    private String gameToJson(Game g) {
        // JSONObject.put은 JSONException(checked)을 던질 수 있어 try 필수
        try {
            JSONObject o = new JSONObject();
            o.put("id", g.getId());
            o.put("title", g.getTitle());
            o.put("coverAssetName", g.getCoverAssetName());
            o.put("coverUri", g.getCoverUri() == null ? JSONObject.NULL : g.getCoverUri());
            o.put("genre", g.getGenre().name());
            o.put("platform", g.getPlatform().name());
            o.put("storeUrl", g.getStoreUrl());
            o.put("status", g.getStatus().name());
            o.put("rating", g.getRating());
            o.put("review", g.getReview());
            JSONArray shots = new JSONArray();
            for (String s : g.getScreenshots()) {
                shots.put(s);
            }
            o.put("screenshots", shots);
            return o.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    /// <summary>
    /// JSON 문자열을 Game으로 복원 (실패하거나 손상됐으면 null)
    /// enum 이름이 잘못됐을 때의 IllegalArgumentException까지 잡으려고 넓게 catch
    /// </summary>
    private Game gameFromJson(String json) {
        try {
            JSONObject o = new JSONObject(json);
            Game g = new Game(
                    o.getInt("id"),
                    o.getString("title"),
                    o.optString("coverAssetName", ""),
                    Genre.valueOf(o.getString("genre")),
                    Platform.valueOf(o.getString("platform")),
                    o.optString("storeUrl", ""),
                    GameStatus.valueOf(o.getString("status")),
                    (float) o.optDouble("rating", 0),
                    o.optString("review", ""));
            g.setCoverUri(o.isNull("coverUri") ? null : o.optString("coverUri", null));
            JSONArray shots = o.optJSONArray("screenshots");
            if (shots != null) {
                for (int i = 0; i < shots.length(); i++) {
                    g.addScreenshot(shots.getString(i));
                }
            }
            return g;
        } catch (Exception e) {
            return null;
        }
    }

    /// <summary>
    /// 현재 휴지통/영구삭제 상태를 prefs에 통째로 다시 저장 (변경이 있을 때마다 호출)
    /// </summary>
    private void persistTrashState() {
        Set<String> trashedRaw = new HashSet<>();
        for (TrashEntry entry : trashedEntries) {
            trashedRaw.add(entry.getGame().getId() + "|" + entry.getTrashedAt());
        }
        Set<String> deletedRaw = new HashSet<>();
        for (int id : deletedIds) {
            deletedRaw.add(String.valueOf(id));
        }
        prefs.edit()
                .putStringSet(KEY_TRASHED, trashedRaw)
                .putStringSet(KEY_DELETED, deletedRaw)
                .apply();
    }

    /// <summary>
    /// 라이브러리 목록에서 특정 id 게임을 제거 (없으면 아무 일 안 함)
    /// </summary>
    private void removeGameFromLibrary(int id) {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getId() == id) {
                games.remove(i);
                return;
            }
        }
    }

    /// <summary>
    /// 문자열을 int로 안전 변환 (저장된 값이 손상된 경우 대비 — 실패하면 기본값)
    /// prefs 값은 외부(파일)에서 오므로 파싱 실패를 방어한다
    /// </summary>
    private int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /// <summary>
    /// 문자열을 long으로 안전 변환 (실패하면 기본값)
    /// </summary>
    private long parseLongSafe(String s, long fallback) {
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
