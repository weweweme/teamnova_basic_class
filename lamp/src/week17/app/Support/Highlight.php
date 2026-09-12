<?php

namespace App\Support;

use Illuminate\Support\HtmlString;

// ============================================================
// Highlight — 검색 결과에서 찾은 글자에 형광펜을 칠한다
//   지금 includes/util.php 의 create_highlighted() 에 해당한다.
//
//   ★ 순서가 핵심이다: 먼저 e() 로 안전하게 만들고, 그 뒤에 <mark> 를 넣는다.
//     · 반대로 하면 <mark> 까지 글자로 변해 화면에 태그가 그대로 보인다.
//     · e() 를 아예 안 하면 사용자가 넣은 <script> 가 진짜 코드로 실행된다 (XSS).
// ============================================================
class Highlight
{
    public static function of(?string $text, string $q): HtmlString
    {
        $safe = e((string) $text);

        if ($q === '') {
            return new HtmlString($safe);   // 검색어가 없으면 칠할 것도 없다
        }

        // preg_quote = 검색어에 정규식 기호(. * ? + 등)가 섞여 있어도
        //   '특별한 의미'가 아니라 그냥 글자로 찾도록 막아 준다.
        //   i = 대소문자 구분 안 함 / u = 한글 같은 여러 바이트 글자를 제대로 처리
        $pattern = '/' . preg_quote(e($q), '/') . '/iu';

        return new HtmlString(preg_replace($pattern, '<mark>$0</mark>', $safe));
    }

    // ── 본문 미리보기 ───────────────────────────────────────
    //   검색어가 있는 부분을 앞뒤로 잘라서 보여준다.
    public static function snippet(?string $content, string $q, int $length = 120): HtmlString
    {
        $flat  = trim(preg_replace('/\s+/u', ' ', (string) $content));
        $found = $q === '' ? false : mb_stripos($flat, $q);

        // 검색어가 한참 뒤에 있으면 그 30글자 앞부터 자른다
        $start   = ($found === false || $found < 30) ? 0 : $found - 30;
        $snippet = mb_substr($flat, $start, $length);

        $prefix = $start > 0 ? '… ' : '';
        $suffix = mb_strlen($flat) > $start + $length ? ' …' : '';

        return new HtmlString($prefix . self::of($snippet, $q) . $suffix);
    }
}
