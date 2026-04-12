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

    // ========== 생성자 ==========

    /// <summary>
    /// 저장소 생성 및 더미 데이터 초기화
    /// rating과 review는 아직 사용자가 입력하지 않은 상태 (0점, 빈 문자열)
    /// </summary>
    public GameRepository() {
        this.games = new ArrayList<>();

        this.games.add(new Game(
                1,
                "젤다의 전설: 왕국의 눈물",
                "cover_zelda",
                Genre.ACTION,
                Platform.NINTENDO_SWITCH,
                "https://www.nintendo.co.kr/software/switch/ayn7a",
                0f,
                ""
        ));

        this.games.add(new Game(
                2,
                "엘든 링",
                "cover_eldenring",
                Genre.RPG,
                Platform.STEAM,
                "https://store.steampowered.com/app/1245620/ELDEN_RING/",
                0f,
                ""
        ));

        this.games.add(new Game(
                3,
                "발더스 게이트 3",
                "cover_baldursgate3",
                Genre.RPG,
                Platform.STEAM,
                "https://store.steampowered.com/app/1086940/Baldurs_Gate_3/",
                0f,
                ""
        ));

        this.games.add(new Game(
                4,
                "갓 오브 워 라그나로크",
                "cover_godofwar",
                Genre.ACTION,
                Platform.PLAYSTATION,
                "https://store.playstation.com/ko-kr/concept/10002456",
                0f,
                ""
        ));

        this.games.add(new Game(
                5,
                "할로우 나이트",
                "cover_hollowknight",
                Genre.PLATFORMER,
                Platform.STEAM,
                "https://store.steampowered.com/app/367520/Hollow_Knight/",
                0f,
                ""
        ));

        this.games.add(new Game(
                6,
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
}
