<?php

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
    return '라우트가 동작한다';
});
