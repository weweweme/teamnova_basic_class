package com.example.week11.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

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
    /// 사용자가 직접 고른 표지 이미지 주소 (content:// URI 문자열). 없으면 null.
    /// 수동 추가 게임에서만 채워지며, 값이 있으면 coverAssetName(번들 drawable)보다 우선 표시
    /// (시드 게임은 null이라 기존 drawable 로직 그대로)
    /// </summary>
    private String coverUri;

    /// <summary>
    /// 장르 (Genre 열거형으로 고정 분류)
    /// </summary>
    private final Genre genre;

    /// <summary>
    /// 플랫폼 (Platform 열거형으로 고정 분류)
    /// </summary>
    private final Platform platform;

    /// <summary>
    /// 스토어 URL (GameDetail에서 암시적 VIEW Intent로 브라우저를 여는 데 사용)
    /// 예: "https://store.steampowered.com/app/12345"
    /// </summary>
    private final String storeUrl;

    /// <summary>
    /// 게임 진행 상태 (플레이중/완료/중단/백로그)
    /// LibraryActivity의 상태별 필터 탭에서 분류 기준으로 사용
    /// 사용자가 바꿀 수 있으므로 final 아님
    /// </summary>
    private GameStatus status;

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

    /// <summary>
    /// 사용자가 ScreenshotActivity에서 추가한 스크린샷 Uri 문자열 목록
    /// 갤러리에서 고른 이미지의 content:// Uri를 toString()으로 보관
    /// 표시할 때는 Uri.parse(...)로 다시 Uri 객체로 만들어 ImageView에 로드
    ///
    /// final로 두는 이유:
    ///   리스트 객체 자체는 인스턴스마다 한 번만 만들어지면 충분 (재할당 금지)
    ///   내용물(추가/삭제)은 add/clear로 변경 가능
    ///
    /// String으로 보관하는 이유:
    ///   Uri 클래스는 Parcelable이지만, 단순 문자열로 보관해도 같은 정보가 유지됨
    ///   writeStringList로 한 번에 직렬화 가능 → Parcel 순서가 단순해짐
    /// </summary>
    private final List<String> screenshots;

    // ========== 일반 생성자 ==========

    /// <summary>
    /// 게임 객체 생성
    /// GameRepository에서 더미 데이터를 만들 때 사용
    /// Unity로 비유하면 new GameData(id, title, ...) 호출과 동일
    /// </summary>
    public Game(int id, String title, String coverAssetName, Genre genre,
                Platform platform, String storeUrl, GameStatus status,
                float rating, String review) {
        this.id = id;
        this.title = title;
        this.coverAssetName = coverAssetName;
        this.genre = genre;
        this.platform = platform;
        this.storeUrl = storeUrl;
        this.status = status;
        this.rating = rating;
        this.review = review;
        // 스크린샷은 처음에는 비어 있고, 사용자가 ScreenshotActivity에서 갤러리로 추가하면 채워짐
        // 빈 ArrayList로 시작하여 기존 호출자(GameRepository)는 영향받지 않음
        this.screenshots = new ArrayList<>();
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
        // enum은 이름(String)으로 저장했으므로 valueOf()로 복원
        this.genre = Genre.valueOf(in.readString());
        this.platform = Platform.valueOf(in.readString());
        this.storeUrl = in.readString();
        // 상태도 enum이라 이름(String)으로 저장했으므로 valueOf()로 복원
        this.status = GameStatus.valueOf(in.readString());
        this.rating = in.readFloat();
        this.review = in.readString();
        // 스크린샷 Uri 문자열 목록 복원
        // createStringArrayList: writeStringList로 쓴 데이터를 통째로 ArrayList<String>으로 복원
        // 비어있게 저장됐다면 빈 ArrayList가 돌아옴 (null 아님)
        this.screenshots = in.createStringArrayList();
        // 사용자가 고른 표지 URI (없으면 null) — writeToParcel 맨 끝에서 쓴 것을 여기서 읽음
        this.coverUri = in.readString();
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
        // enum은 name()으로 문자열 변환하여 저장 (읽을 때 valueOf()로 복원)
        dest.writeString(genre.name());
        dest.writeString(platform.name());
        dest.writeString(storeUrl);
        // 상태 enum을 name()으로 문자열 변환하여 저장 (읽을 때 valueOf()로 복원)
        dest.writeString(status.name());
        dest.writeFloat(rating);
        dest.writeString(review);
        // 스크린샷 Uri 문자열 목록을 Parcel에 그대로 직렬화
        // writeStringList: List<String>을 한 번에 기록 (createStringArrayList로 복원)
        dest.writeStringList(screenshots);
        // 사용자가 고른 표지 URI (없으면 null) — 맨 끝에 붙임 (읽는 쪽도 맨 끝에서 읽음)
        dest.writeString(coverUri);
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
    /// 사용자가 고른 표지 이미지 URI 문자열 반환 (없으면 null)
    /// </summary>
    public String getCoverUri() {
        return coverUri;
    }

    /// <summary>
    /// 사용자가 고른 표지 이미지 URI 설정 (게임 추가 시 사용)
    /// </summary>
    public void setCoverUri(String coverUri) {
        this.coverUri = coverUri;
    }

    /// <summary>
    /// 장르 반환
    /// </summary>
    public Genre getGenre() {
        return genre;
    }

    /// <summary>
    /// 플랫폼 반환
    /// </summary>
    public Platform getPlatform() {
        return platform;
    }

    /// <summary>
    /// 스토어 URL 반환
    /// </summary>
    public String getStoreUrl() {
        return storeUrl;
    }

    /// <summary>
    /// 게임 진행 상태 반환
    /// </summary>
    public GameStatus getStatus() {
        return status;
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

    /// <summary>
    /// 스크린샷 Uri 문자열 목록 반환
    /// 호출자가 add/remove 등으로 직접 수정 가능 (방어 복사 안 함 — 의도된 공유)
    /// 표시할 때는 Uri.parse(...)로 다시 Uri 객체로 변환해서 ImageView에 로드
    /// </summary>
    public List<String> getScreenshots() {
        return screenshots;
    }

    // ========== Setter (변경 가능한 필드만) ==========

    /// <summary>
    /// 게임 진행 상태 설정 (사용자가 상태를 바꿀 때 사용)
    /// </summary>
    public void setStatus(GameStatus status) {
        this.status = status;
    }

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

    /// <summary>
    /// 스크린샷 Uri 문자열 하나를 추가
    /// ScreenshotActivity에서 갤러리로 이미지 한 장을 고를 때마다 호출
    /// </summary>
    public void addScreenshot(String uriString) {
        this.screenshots.add(uriString);
    }

    /// <summary>
    /// 스크린샷 목록 전체를 다른 목록으로 교체
    /// GameRepository.updateGame이 외부에서 받은 Game의 스크린샷을 원본 Game에 반영할 때 사용
    /// 리스트 객체 자체는 final이므로 재할당하지 않고 clear + addAll로 내용만 갱신
    /// </summary>
    public void replaceScreenshots(List<String> newList) {
        this.screenshots.clear();
        if (newList != null) {
            this.screenshots.addAll(newList);
        }
    }
}
