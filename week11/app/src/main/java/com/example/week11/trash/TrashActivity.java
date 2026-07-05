package com.example.week11.trash;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.week11.App;
import com.example.week11.R;
import com.example.week11.data.GameRepository;
import com.example.week11.databinding.ActivityTrashBinding;
import com.example.week11.model.Game;

/// <summary>
/// 휴지통 화면
/// 보관함에서 삭제된(아직 영구삭제 전) 게임을 목록으로 보여주고, 복원 / 영구삭제한다
/// (짧은 실행취소 스낵바가 닫히면 게임이 여기로 옮겨짐)
/// </summary>
public class TrashActivity extends AppCompatActivity implements OnTrashActionListener {

    /// <summary>
    /// activity_trash.xml의 View 묶음
    /// </summary>
    private ActivityTrashBinding binding;

    /// <summary>
    /// 게임 저장소 (App 공용 인스턴스) — 휴지통 목록/복원/영구삭제에 사용
    /// </summary>
    private GameRepository gameRepository;

    /// <summary>
    /// 휴지통 목록 어댑터
    /// </summary>
    private TrashAdapter adapter;

    // ========== Lifecycle ==========

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTrashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar에 ← 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        gameRepository = ((App) getApplication()).getGameRepository();

        adapter = new TrashAdapter(gameRepository.getTrashedGames(), this);
        binding.recyclerTrash.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTrash.setAdapter(adapter);

        refresh();
    }

    /// <summary>
    /// 휴지통 목록을 다시 읽어 어댑터에 반영하고, 빈 상태 안내를 갱신
    /// </summary>
    private void refresh() {
        adapter.updateItems(gameRepository.getTrashedGames());

        boolean isEmpty = gameRepository.getTrashedGames().isEmpty();
        binding.textViewTrashEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerTrash.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // ========== 휴지통 항목 콜백 ==========

    /// <summary>
    /// "복원" → 게임을 보관함으로 되돌린 뒤 목록 갱신
    /// </summary>
    @Override
    public void onRestore(Game game) {
        gameRepository.restoreFromTrash(game.getId());
        refresh();
    }

    /// <summary>
    /// "영구삭제" → 확인 후 완전히 제거하고 목록 갱신
    /// 되돌릴 수 없는 동작이라 제목을 넣어 확인 다이얼로그를 띄운다
    /// </summary>
    @Override
    public void onDeletePermanently(Game game) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.trash_delete_confirm_message, game.getTitle()))
                .setPositiveButton(R.string.trash_delete, (dialog, which) -> {
                    gameRepository.deleteFromTrashPermanently(game.getId());
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ========== 메뉴 (비우기) ==========

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_empty_trash) {
            confirmEmptyTrash();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /// <summary>
    /// "비우기" → 확인 후 휴지통 전체를 영구삭제
    /// </summary>
    private void confirmEmptyTrash() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.trash_empty_confirm_title)
                .setMessage(R.string.trash_empty_confirm_message)
                .setPositiveButton(R.string.trash_empty_action, (dialog, which) -> {
                    gameRepository.emptyTrash();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
