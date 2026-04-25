package com.example.week8.data;

import com.example.week8.model.Game;
import com.example.week8.model.Genre;
import com.example.week8.model.Platform;

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
        this.nextId = 5;  // 더미 4개가 1~4 사용하므로 5부터 시작

        this.games.add(new Game(
                1,
                "엘든 링",
                "cover_eldenring",
                Genre.RPG,
                Platform.STEAM,
                "https://store.steampowered.com/app/1245620/ELDEN_RING/",
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
                0f,
                ""
        ));
    }

    // ========== 조회 ==========

    /// <summary>
    /// 전체 게임 목록 반환
    /// MainActivity에서 카드 리스트를 만들 때 사용
    /// </summary>
    public ArrayList<Game> getAllGames() {
        return this.games;
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
                0f,                 // 초기 별점
                ""                  // 초기 한줄평
        );
        this.games.add(newGame);
        this.nextId++;
        return newGame;
    }
}
