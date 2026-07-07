package com.example.week12.rawg;

import com.example.week12.model.Genre;
import com.example.week12.model.Platform;

/// <summary>
/// RAWG의 장르·플랫폼 코드값(slug)을 우리 앱의 Genre/Platform enum으로 바꾸는 변환기
///
/// ──── 왜 필요한가 ────
/// RAWG는 장르를 "action", "role-playing-games-rpg" 같은 자유로운 코드로 준다.
/// 그런데 우리 앱의 Genre/Platform은 고정된 몇 종의 enum이다.
/// → RAWG 값을 우리 enum "가장 가까운 것"으로 근사해서 맞춰준다. 딱 맞는 게 없으면 ETC(기타).
///
/// 인스턴스 상태가 없는 순수 변환 함수라 static 유틸리티로 둔다 (new 할 필요 없음).
/// </summary>
public class RawgGameMapper {

    /// <summary>
    /// 유틸리티 클래스라 인스턴스를 만들 이유가 없어 생성자를 막아둠 (new RawgGameMapper() 금지)
    /// </summary>
    private RawgGameMapper() {
    }

    /// <summary>
    /// RAWG 장르 slug → 우리 Genre enum (딱 맞는 게 없으면 ETC)
    /// slug는 소문자 고정값이라 단일 값 분기(switch)로 매핑
    /// </summary>
    public static Genre toGenre(String slug) {
        if (slug == null) {
            return Genre.ETC;
        }
        switch (slug) {
            case "action":
                return Genre.ACTION;
            case "adventure":
                return Genre.ADVENTURE;
            case "role-playing-games-rpg":
                return Genre.RPG;
            case "strategy":
                return Genre.STRATEGY;
            case "simulation":
                return Genre.SIMULATION;
            case "sports":
                return Genre.SPORTS;
            case "racing":
                return Genre.RACING;
            case "puzzle":
                return Genre.PUZZLE;
            case "platformer":
                return Genre.PLATFORMER;
            default:
                // shooter/indie/casual/arcade 등 우리 enum에 없는 장르는 기타로
                return Genre.ETC;
        }
    }

    /// <summary>
    /// RAWG 플랫폼 slug → 우리 Platform enum (딱 맞는 게 없으면 ETC)
    /// slug가 "playstation5"/"xbox-one"처럼 접두어로 갈라지므로 접두어 비교(if-else)로 매핑
    /// </summary>
    public static Platform toPlatform(String slug) {
        if (slug == null || slug.isEmpty()) {
            return Platform.ETC;
        }

        boolean isPc = slug.equals("pc");
        boolean isPlaystation = slug.startsWith("playstation") || slug.startsWith("ps");
        boolean isXbox = slug.startsWith("xbox");
        boolean isNintendo = slug.startsWith("nintendo");
        boolean isMobile = slug.equals("ios") || slug.equals("android");

        if (isPc) {
            // 우리 앱은 Steam 중심이라 PC 게임은 Steam으로 근사 (가장 가까운 선택지)
            return Platform.STEAM;
        }
        if (isPlaystation) {
            return Platform.PLAYSTATION;
        }
        if (isXbox) {
            return Platform.XBOX;
        }
        if (isNintendo) {
            return Platform.NINTENDO_SWITCH;
        }
        if (isMobile) {
            return Platform.MOBILE;
        }
        return Platform.ETC;
    }
}
