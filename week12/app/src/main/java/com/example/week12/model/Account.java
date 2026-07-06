package com.example.week12.model;

/// <summary>
/// 계정 정보를 담는 그릇(모델)
///
/// ──── 무엇인가 ────
/// 가상 계정 한 명의 신원 정보를 묶어둔 데이터 클래스.
/// Unity로 비유하면 플레이어 한 명의 프로필을 담은 [Serializable] 데이터 클래스와 같음.
///
/// ──── 어디서 오나 ────
/// 실제 값은 SharedPreferences의 계정별 파일(user_<id>)에 흩어져 저장되어 있고,
/// AccountManager가 그 값들을 읽어 이 Account 객체 하나로 묶어 돌려줌.
///
/// 현재(10주차 Phase 1)는 id + nickname만 보유.
/// 아바타 색·한줄소개(avatarColor, bio)는 Phase 7(계정별 프로필)에서 추가 예정.
/// </summary>
public class Account {

    /// <summary>
    /// 계정 아이디 (로그인 시 입력하는 고유 식별자)
    /// 예: "alice" → 이 계정의 전용 저장 파일 이름은 "user_alice"가 됨
    /// 인스턴스마다 다른 값이라 생성자에서 받고, 바뀌지 않으므로 final
    /// </summary>
    private final String id;

    /// <summary>
    /// 화면에 보여줄 별명 (닉네임)
    /// id는 파일 이름·로그인용 식별자, nickname은 사람이 보는 표시용
    /// </summary>
    private final String nickname;

    /// <summary>
    /// 계정 객체 생성
    /// </summary>
    /// <param name="id">계정 아이디 (저장 파일 이름의 일부가 됨)</param>
    /// <param name="nickname">화면에 표시할 별명</param>
    public Account(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    /// <summary>
    /// 계정 아이디 반환
    /// </summary>
    public String getId() {
        return id;
    }

    /// <summary>
    /// 닉네임 반환
    /// </summary>
    public String getNickname() {
        return nickname;
    }
}
