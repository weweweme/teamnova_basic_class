package com.example.week8.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/// <summary>
/// 게임 데이터 모델
/// 게임 다이어리에 기록할 게임 한 건의 정보를 담는 클래스
///
/// ──── Parcel이란? ────
/// Android에서 Activity끼리는 같은 앱이라도 객체를 참조로 직접 넘길 수 없음
/// 그래서 데이터를 바이트로 포장(직렬화)해서 전달해야 하는데,
/// 이 포장 컨테이너가 Parcel임
///
/// Unity 비유: MemoryStream + BinaryWriter/BinaryReader와 동일한 개념
/// - 보내는 쪽: writeToParcel()로 필드를 순서대로 Parcel에 기록 (BinaryWriter.Write)
/// - 받는 쪽: Game(Parcel in) 생성자로 같은 순서대로 읽어서 복원 (BinaryReader.Read)
/// - 순서가 어긋나면 데이터가 깨짐 (키/인덱스 없이 순서 기반으로 동작하기 때문)
///
/// 전달 흐름:
/// 보내는 Activity → intent.putExtra("game", game)
///   → Android가 내부적으로 game.writeToParcel(parcel) 호출
/// 받는 Activity → getIntent().getParcelableExtra("game")
///   → Android가 CREATOR.createFromParcel(parcel) → new Game(parcel) 호출
/// </summary>
public class Game implements Parcelable {

    // ========== 필드 ==========

    /// <summary>
    /// 게임 고유 ID (더미 데이터에서 1, 2, 3... 순서로 부여)
    /// </summary>
    private final int id;

    /// <summary>
    /// 게임 제목 (예: "젤다의 전설: 왕국의 눈물")
    /// </summary>
    private final String title;

    /// <summary>
    /// 표지 이미지 리소스 이름 (res/drawable에 넣을 파일명, 확장자 제외)
    /// 예: "cover_zelda" → R.drawable.cover_zelda로 변환하여 사용
    /// Unity로 비유하면 Resources.Load("cover_zelda")에 쓸 경로명
    /// </summary>
    private final String coverAssetName;

    /// <summary>
    /// 장르 (예: "액션 어드벤처", "RPG")
    /// </summary>
    private final String genre;

    /// <summary>
    /// 플랫폼 (예: "Steam", "Nintendo Switch")
    /// </summary>
    private final String platform;

    /// <summary>
    /// 스토어 URL (GameDetail에서 암시적 VIEW Intent로 브라우저를 여는 데 사용)
    /// 예: "https://store.steampowered.com/app/12345"
    /// </summary>
    private final String storeUrl;

    /// <summary>
    /// 사용자가 매긴 별점 (0.0 ~ 5.0, 초기값 0)
    /// ReviewWriteActivity에서 수정되며, SharedPreferences로 영속화됨
    /// </summary>
    private float rating;

    /// <summary>
    /// 사용자가 작성한 한줄평 (초기값 빈 문자열)
    /// ReviewWriteActivity에서 수정되며, SharedPreferences로 영속화됨
    /// </summary>
    private String review;

    // ========== 일반 생성자 ==========

    /// <summary>
    /// 게임 객체 생성
    /// GameRepository에서 더미 데이터를 만들 때 사용
    /// Unity로 비유하면 new GameData(id, title, ...) 호출과 동일
    /// </summary>
    public Game(int id, String title, String coverAssetName, String genre,
                String platform, String storeUrl, float rating, String review) {
        this.id = id;
        this.title = title;
        this.coverAssetName = coverAssetName;
        this.genre = genre;
        this.platform = platform;
        this.storeUrl = storeUrl;
        this.rating = rating;
        this.review = review;
    }

    // ========== Parcel 생성자 ==========

    /// <summary>
    /// Parcel에서 게임 객체를 복원하는 생성자
    /// Unity로 비유하면 BinaryReader로 저장된 순서대로 읽어오는 것
    /// writeToParcel에서 쓴 순서와 반드시 동일한 순서로 읽어야 함
    /// Android 프레임워크가 CREATOR를 통해 이 생성자를 호출함
    /// </summary>
    private Game(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.coverAssetName = in.readString();
        this.genre = in.readString();
        this.platform = in.readString();
        this.storeUrl = in.readString();
        this.rating = in.readFloat();
        this.review = in.readString();
    }

    // ========== Parcelable 구현 ==========

    /// <summary>
    /// 게임 데이터를 Parcel에 직렬화하여 기록
    /// Unity로 비유하면 BinaryWriter로 필드를 순서대로 Write하는 것
    /// Parcel 생성자에서 읽는 순서와 반드시 일치해야 함
    /// </summary>
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(coverAssetName);
        dest.writeString(genre);
        dest.writeString(platform);
        dest.writeString(storeUrl);
        dest.writeFloat(rating);
        dest.writeString(review);
    }

    /// <summary>
    /// 특수 직렬화 대상(파일 디스크립터 등)이 있는지 반환
    /// 일반 데이터만 담으므로 항상 0
    /// </summary>
    @Override
    public int describeContents() {
        return 0;
    }

    /// <summary>
    /// Parcel에서 Game 객체를 복원하는 팩토리
    /// Unity로 비유하면 ScriptableObject.CreateInstance<Game>()처럼
    /// 프레임워크가 Intent에서 Game을 꺼낼 때 이 팩토리를 통해 복원함
    /// </summary>
    public static final Creator<Game> CREATOR = new Creator<Game>() {
        /// <summary>
        /// Parcel 데이터로부터 Game 객체 하나를 생성
        /// </summary>
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        /// <summary>
        /// 지정된 크기의 Game 배열을 생성 (프레임워크 내부에서 사용)
        /// </summary>
        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    // ========== Getter ==========

    /// <summary>
    /// 게임 고유 ID 반환
    /// </summary>
    public int getId() {
        return id;
    }

    /// <summary>
    /// 게임 제목 반환
    /// </summary>
    public String getTitle() {
        return title;
    }

    /// <summary>
    /// 표지 이미지 리소스 이름 반환
    /// </summary>
    public String getCoverAssetName() {
        return coverAssetName;
    }

    /// <summary>
    /// 장르 반환
    /// </summary>
    public String getGenre() {
        return genre;
    }

    /// <summary>
    /// 플랫폼 반환
    /// </summary>
    public String getPlatform() {
        return platform;
    }

    /// <summary>
    /// 스토어 URL 반환
    /// </summary>
    public String getStoreUrl() {
        return storeUrl;
    }

    /// <summary>
    /// 별점 반환
    /// </summary>
    public float getRating() {
        return rating;
    }

    /// <summary>
    /// 한줄평 반환
    /// </summary>
    public String getReview() {
        return review;
    }

    // ========== Setter (변경 가능한 필드만) ==========

    /// <summary>
    /// 별점 설정 (ReviewWriteActivity에서 사용자가 입력한 값 반영)
    /// </summary>
    public void setRating(float rating) {
        this.rating = rating;
    }

    /// <summary>
    /// 한줄평 설정 (ReviewWriteActivity에서 사용자가 입력한 값 반영)
    /// </summary>
    public void setReview(String review) {
        this.review = review;
    }
}
