package com.example.week12.rawg;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.week12.App;
import com.example.week12.account.UserPrefs;
import com.example.week12.data.GameRepository;
import com.example.week12.detail.GameDetailActivity;
import com.example.week12.model.ActivityLogType;
import com.example.week12.model.Game;
import com.example.week12.model.GameStatus;
import com.example.week12.model.Genre;
import com.example.week12.model.Platform;
import com.example.week12.model.RawgGame;

/// <summary>
/// RAWG 게임 하나를 "상태 선택 → 보관함에 추가"하는 흐름을 담은 공용 헬퍼
///
/// ──── 왜 만드나 ────
/// 검색 화면(RawgSearchActivity)과 탐색 화면(ExploreActivity)이 "결과 탭 → 추가"를 똑같이 한다.
/// 같은 코드를 두 군데 두지 않도록 여기 한곳에 모아 재사용한다 (DRY).
///
/// 상태 없는 값이 아니라 순수 동작 모음이라 static 유틸리티로 둔다.
/// </summary>
public final class RawgGameAdder {

    /// <summary>
    /// 인스턴스를 만들 이유가 없어 생성자를 막아둠
    /// </summary>
    private RawgGameAdder() {
    }

    /// <summary>
    /// "어떤 상태로 추가할까요?"를 물어본 뒤, 고른 상태로 게임을 보관함에 추가한다.
    ///
    /// activity: 다이얼로그·토스트·저장소 접근에 쓸 화면
    /// game: 추가할 RAWG 게임
    /// onAdded: 추가 완료 후 할 일 (예: 검색 화면은 finish로 보관함 복귀 / 탐색 화면은 그대로 머무름 → null)
    /// </summary>
    public static void promptAddToLibrary(AppCompatActivity activity, RawgGame game, Runnable onAdded) {
        App app = (App) activity.getApplication();
        GameRepository repository = app.getGameRepository();

        // 중복 방지: 같은 게임(rawgId)이 이미 보관함에 있으면 새로 추가하지 않고 안내 + 상세 보기 제안
        Game existing = repository.findByRawgId(game.getRawgId());
        if (existing != null) {
            showAlreadyExists(activity, existing);
            return;
        }

        GameStatus[] statuses = GameStatus.values();
        String[] labels = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            labels[i] = statuses[i].getDisplayName();
        }

        new AlertDialog.Builder(activity)
                .setTitle("어떤 상태로 추가할까요?")
                .setItems(labels, (dialog, which) -> addWithStatus(activity, game, statuses[which], onAdded))
                .setNegativeButton("취소", null)
                .show();
    }

    /// <summary>
    /// 이미 보관함에 있는 게임을 또 추가하려 할 때: 안내 + "상세 보기"로 기존 게임으로 데려간다
    /// (중복 항목을 새로 만들지 않는다 — 메이저 앱들의 "이미 있음" 처리 방식)
    /// </summary>
    private static void showAlreadyExists(AppCompatActivity activity, Game existing) {
        new AlertDialog.Builder(activity)
                .setTitle("이미 보관함에 있어요")
                .setMessage("\"" + existing.getTitle() + "\"은(는) 이미 보관함에 있어요.")
                .setPositiveButton("상세 보기", (dialog, which) -> {
                    // 기존 게임의 상세로 이동 (LibraryActivity가 상세를 여는 방식과 동일)
                    Intent intent = new Intent(activity, GameDetailActivity.class);
                    intent.putExtra(GameDetailActivity.EXTRA_GAME, existing);
                    activity.startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /// <summary>
    /// 고른 상태로 게임을 보관함에 실제로 추가하고, 최근 활동 기록 + 토스트 후 onAdded 실행
    ///
    /// 흐름: RawgGame → (장르/플랫폼 매핑) → repository.addGame(상태 + rawgId) → 최근 활동 기록 → 토스트 → onAdded
    /// rawgId는 나중에 서버/소셜에서 "같은 게임"을 사용자 간에 맞추는 공통 키로 심어둔다.
    /// </summary>
    private static void addWithStatus(AppCompatActivity activity, RawgGame game,
                                      GameStatus status, Runnable onAdded) {
        App app = (App) activity.getApplication();
        GameRepository repository = app.getGameRepository();

        // RAWG 슬러그 목록 → 우리 enum (매핑되는 첫 항목, 없으면 기타)
        Genre genre = RawgGameMapper.toGenre(game.getGenreSlugs());
        Platform platform = RawgGameMapper.toPlatform(game.getPlatformSlugs());

        // 표지는 https 원격 주소를 coverUri로 저장 → 보관함 그리드가 loadUri로 표시 (표지 없으면 null)
        Game added = repository.addGame(
                game.getName(), genre, platform, "", game.getCoverImageUrl(), status,
                game.getRawgId());

        // 최근 활동 피드에 "추가함" 기록 (로그인 상태에서만 userPrefs 존재)
        UserPrefs userPrefs = app.getUserPrefs();
        if (userPrefs != null) {
            userPrefs.addActivityLog(ActivityLogType.ADDED, added.getId(), "");
        }

        Toast.makeText(activity, "보관함에 추가됨: " + added.getTitle(), Toast.LENGTH_SHORT).show();

        if (onAdded != null) {
            onAdded.run();
        }
    }
}
