<?php

use App\Models\Post;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

// ── 연습 ① 주소 등록해 보기 ─────────────────────────────────
//   ★ 여기에 적지 않은 주소는 '존재하지 않는다'.
//     지금까지 우리 프로젝트는 파일을 만들면 그게 곧 주소였지만,
//     Laravel에서는 이 목록에 적어야 주소가 생긴다.
//     (안드로이드에서 매니페스트에 등록하지 않은 화면을 띄울 수 없는 것과 같다)
//
//   Route::get('주소', 실행할 코드)
//     · get  = 브라우저가 주소창으로 들어오는 방식(GET). 폼 전송은 Route::post
//     · 두 번째 인자는 지금은 함수지만, 실제로는 컨트롤러의 메서드를 지정한다
Route::get('/hello', function () {
    // ── 연습 ② 화면 파일에 값 넘기기 ─────────────────────────
    //   view('파일이름', ['변수명' => 값])
    //     · 'hello' → resources/views/hello.blade.php 를 찾는다
    //     · 넘긴 이름이 그 파일 안에서 $name · $danger 로 쓰인다
    //   ★ 화면을 '직접 출력'하지 않고 '돌려준다'는 점이 지금과 다르다.
    //     출력은 프레임워크가 한다. (Unity에서 화면을 직접 그리지 않는 것과 같다)
    return view('hello', [
        'name'   => '진수',
        'danger' => '<script>alert(1)</script>',   // escape 확인용
    ]);
});

// ── 연습 ④ 목록을 페이지로 자르기 ───────────────────────────
//   ★ paginate(15) 한 줄이 세 가지를 한다.
//     1) 총 몇 개인지 센다        → select count(*) ...
//     2) 그 페이지 분량만 가져온다 → ... limit 15 offset 0
//     3) 지금 몇 페이지인지 판단   → 주소의 ?page= 를 알아서 읽는다
//
//   지금 우리 코드(board/index.php)는 get_posts()로 190개를 전부 배열에 올린 뒤
//   array_slice로 15개를 잘랐다. 여기서는 DB가 자른다.
//
//   latest('id') = order by id desc. 최신 글이 위로.
Route::get('/posts', function () {
    $posts = Post::latest('id')->paginate(15);

    // 화면에는 '자른 결과'만 넘긴다. 총 개수·현재 페이지·링크는 $posts 안에 함께 들어 있다.
    return view('posts.index', ['posts' => $posts]);
});
