<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

// ============================================================
// Post — posts 표 하나를 나타내는 클래스
//
//   [모델이 무엇인가]
//     지금까지 우리 코드는 둘로 나뉘어 있었다.
//       · 데이터 — $post['title'] 같은 '배열'. 값 덩어리라 기능이 없다
//       · 동작   — get_posts() 같은 '함수'. includes/posts.php 에 모여 있다
//     모델은 그 둘을 한 클래스에 합친 것이다.
//
//   [클래스로 쓰면 표 전체, 객체로 쓰면 한 줄]
//       Post::count()      ← 클래스에 대고 부르면 '표 전체'. 조회의 시작점
//       $post->title       ← 객체 하나는 '그 표의 한 행'
//     :: 냐 -> 냐로 구분된다.
//
//   [모델이 아닌 것]
//     · 화면과 무관하다. HTML을 만들지 않는다 — 그건 Blade의 일이다
//     · SQL을 없애지 않는다. 대신 만들어 줄 뿐이다 (아래 SoftDeletes 주석 참고)
//     · 강제되지 않는다. DB::table('posts') 로 우회하면 아래 안전장치가 전부 꺼진다
// ============================================================
class Post extends Model
{
    // ── 표 이름은 적지 않는다 ───────────────────────────────
    //   클래스 이름 → snake_case → 영어 복수형 순으로 추측한다.
    //     Post → post → posts   (우리 표 이름과 일치하므로 적을 필요가 없다)
    //   ★ 표 이름만 추측하는 게 아니다. 기본키가 id 이고 자동증가 정수라는 것도 가정한다.
    //     어긋나는 표만 $table · $primaryKey · $incrementing 을 적어 준다.
    //     (우리 likes·votes 는 id 없는 복합키라 모델 대신 '관계'로 다룰 예정)
    //   확인법: (new Post)->getTable()

    // ── 지운 글 자동 제외 ───────────────────────────────────
    //   ★ 이 한 줄은 '글로벌 스코프'다. 이 모델에서 만들어지는 모든 쿼리에
    //     조건이 자동으로 끼워진다. 실제로 생성되는 SQL:
    //
    //       Post::query()          → select * from `posts` where `deleted_at` is null
    //       Post::withTrashed()    → select * from `posts`
    //       Post::onlyTrashed()    → select * from `posts` where `deleted_at` is not null
    //
    //     즉 SQL이 사라진 게 아니라 프레임워크가 붙여 준다.
    //     지금 우리가 조회할 때마다 손으로 붙이던 WHERE deleted_at IS NULL 20곳이 이것이다.
    //     한 곳만 빠뜨려도 지운 글이 화면에 나왔는데, 여기서는 사람이 개입할 자리가 없다.
    //
    //   ★ 삭제 동작도 바뀐다 — $post->delete() 는 DELETE 가 아니라 UPDATE 를 실행한다.
    //     (deleted_at 에 지금 시각을 넣는다)
    //       진짜 삭제 → forceDelete()   /  되살리기 → restore()
    //     우리 post/delete.php · restore.php · purge.php 가 이 셋에 그대로 대응된다.
    //
    //   ⚠ 한계 — 모델을 거치지 않으면 안 걸린다. DB::table('posts') 로 직접 쿼리하면
    //     조건이 안 붙어 지운 글까지 나온다. "빠르니까 쿼리 빌더로" 가 안전장치를 끄는 순간이다.
    use SoftDeletes;

    // ── 수정 시각 컬럼 이름 맞추기 ──────────────────────────
    //   Eloquent 는 저장할 때마다 '생성 시각·수정 시각' 두 칸을 알아서 채운다.
    //   그 칸 이름의 기본값이 created_at / updated_at 인데, 우리 표는 edited_at 이다.
    //   이 한 줄로 맞춘다. (created_at 은 이름이 같아 그대로 둔다)
    //
    //   [끄는 방법도 있다]
    //     const UPDATED_AT = null;      → 수정 시각을 안 쓴다
    //     public $timestamps = false;   → 둘 다 안 쓴다
    //     ★ 우리 users 표가 후자다 (시각 컬럼이 joined_at 하나뿐)
    //
    //   [덤] 시각 컬럼은 문자열이 아니라 Carbon 객체로 바뀐다.
    //     $post->created_at->diffForHumans() → "1 month ago"
    //     지금 util.php 의 '몇 분 전' 계산 함수를 이걸로 대체할 수 있다.
    //
    //   ⚠ 위험 — 우리 코드에서 edited_at 은 '고쳤을 때만' 값이 들어가는데,
    //     Eloquent 는 새로 만들 때도 채운다. 그대로 두면 모든 글에 (수정됨)이 붙는다.
    //     글쓰기를 옮기는 3단계에서 반드시 처리한다.
    const UPDATED_AT = 'edited_at';

    // ── 대량 할당 허용 목록 ─────────────────────────────────
    //   Post::create($요청값) 처럼 통째로 넘길 때 '어떤 칸까지 채워도 되는가'를 정한다.
    //   목록에 없는 칸은 조용히 무시된다 → 사용자가 몰래 author_id 나 views 를
    //   섞어 보내도 반영되지 않는다.
    //   ★ 지금은 읽기만 하므로 안 쓰이지만, 글쓰기를 옮길 때(3단계) 쓴다.
    protected $fillable = ['author_id', 'media_id', 'title', 'content', 'sentiment'];

    // ── 앞으로 이 아래에 올 것들 ───────────────────────────
    //   · 관계   — comments() · author() · media()
    //              $post->comments 로 그 글의 댓글을 가져온다. JOIN을 우리가 쓰지 않는다
    //   · 접근자 — 표에 없는 값을 속성처럼 만든다 (includes/level.php 의 등급 배지)
    //   · 스코프 — 자주 쓰는 조건에 이름을 붙인다 (sort_posts · filter_posts_by_work)
}
