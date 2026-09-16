<?php

namespace App\Support;

use Illuminate\Support\HtmlString;
use Illuminate\Support\Str;

// ============================================================
// PostBody — 글 본문을 화면에 그릴 HTML 로 바꾼다
//
//   지금까지는 nl2br(e(...)) 로 줄바꿈만 처리했다. 본문 사이에 사진을 넣으려면
//   '이 자리에 이 사진' 이라고 적을 표기가 필요해서 마크다운으로 바꾼다.
//
//   ★ 위험한 자리라 옵션 세 개를 못 박는다.
//     · html_input=escape      — 사용자가 쓴 <script> 는 실행하지 않고 글자로 보여준다
//     · allow_unsafe_links=false — javascript: 같은 주소를 링크로 만들지 않는다
//     · soft_break=<br>        — 한 번 친 줄바꿈을 그대로 살린다
//                                (마크다운 기본은 줄바꿈을 공백으로 합친다. 그러면 기존 글이 다 뭉친다)
// ============================================================
class PostBody
{
    private const OPTIONS = [
        'html_input'         => 'escape',
        'allow_unsafe_links' => false,
        'renderer'           => ['soft_break' => '<br>'],
    ];

    public static function render(?string $content): HtmlString
    {
        return new HtmlString(Str::markdown((string) $content, self::OPTIONS));
    }
}
