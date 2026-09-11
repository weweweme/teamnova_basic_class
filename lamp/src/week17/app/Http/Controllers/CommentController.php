<?php

namespace App\Http\Controllers;

use App\Models\Comment;
use App\Models\Notification;
use App\Models\Post;
use Illuminate\Foundation\Auth\Access\AuthorizesRequests;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

// ============================================================
// CommentController — 댓글 작성 · 수정 · 삭제
//   지금 comment/create.php · update.php · delete.php 세 파일에 해당한다.
// ============================================================
class CommentController extends Controller
{
    use AuthorizesRequests;

    // ── 댓글 작성 (POST /posts/{post}/comments) ─────────────
    public function store(Request $request, Post $post)
    {
        $data = $request->validate([
            'content'   => ['required', 'string', 'max:500'],
            // 답글이면 부모 댓글 번호가 온다.
            //   ★ 'exists:comments,id' 만으로는 부족하다 — 다른 글의 댓글 번호를 넣으면
            //     엉뚱한 글에 답글이 붙는다. 그래서 '이 글의 댓글인가'까지 확인한다.
            'parent_id' => [
                'nullable', 'integer',
                Rule::exists('comments', 'id')->where('post_id', $post->id),
            ],
        ]);

        $comment = $post->comments()->create([
            'author_id' => $request->user()->id,
            'parent_id' => $data['parent_id'] ?? null,
            'content'   => $data['content'],
        ]);

        // ── 알림 ───────────────────────────────────────────
        //   규칙 — 댓글은 '글 주인'에게, 답글은 '부모 댓글 주인'에게.
        //   ★ 답글일 때 글 주인에게도 보내면 한 번의 답글로 알림이 둘 생겨 도배가 된다.
        //     "누가 내게 말을 걸었나"를 기준으로 받는 사람을 정하면 규칙이 자연스럽다.
        //   (자기 자신에게 보내는 알림은 Notification::notify 안에서 걸러진다)
        if ($comment->parent_id === null) {
            Notification::notify($post->author_id, $comment->author_id, $comment, 'comment');
        } else {
            Notification::notify($comment->parent?->author_id, $comment->author_id, $comment, 'reply');
        }

        return $this->backToComment($comment, '댓글을 등록했습니다.');
    }

    // ── 댓글 수정 (PUT /comments/{comment}) ─────────────────
    public function update(Request $request, Comment $comment)
    {
        $this->authorize('update', $comment);

        $data = $request->validate(['content' => ['required', 'string', 'max:500']]);
        $comment->update($data);   // edited_at 이 자동으로 채워진다

        return $this->backToComment($comment, '댓글을 수정했습니다.');
    }

    // ── 댓글 삭제 (DELETE /comments/{comment}) ──────────────
    public function destroy(Comment $comment)
    {
        $this->authorize('delete', $comment);

        // ★ 소프트삭제라 행은 남는다 → 화면에 '삭제된 댓글입니다' 자리로 보인다.
        //   답글이 달려 있어도 고아가 되지 않는 이유다.
        $comment->delete();

        return $this->backToComment($comment, '댓글을 삭제했습니다.');
    }

    // ── 그 댓글이 보이는 페이지로 돌려보낸다 ────────────────
    //   ★ 댓글이 20줄을 넘어가면 1페이지로 보내도 방금 쓴 댓글이 화면에 없다.
    //     그래서 몇 페이지인지 계산해서 주소에 싣는다.
    //     1페이지면 cpage 를 아예 붙이지 않는다 — 같은 화면에 주소가 두 개 생기지 않게.
    private function backToComment(Comment $comment, string $status)
    {
        $page = $comment->pageNumber();
        $url  = "/posts/{$comment->post_id}"
              . ($page > 1 ? "?cpage={$page}" : '')
              . "#c{$comment->id}";

        return redirect($url)->with('status', $status);
    }
}
