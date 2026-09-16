<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

// ============================================================
// ShowQueries — 이 화면을 그리는 동안 실제로 나간 SQL 을 화면 아래에 찍는다
//
//   [무엇에 쓰나] 발표 시연. "DB 가 15개만 가져온다"를 말이 아니라 눈으로 보이게 한다.
//
//   ★ 켜지는 조건이 두 개다 — 개발 모드(APP_DEBUG=true) 이고, 주소에 ?sql=1 이 있을 때.
//     둘 중 하나라도 아니면 아무 일도 하지 않는다. 운영에서는 APP_DEBUG 가 꺼져 있다.
// ============================================================
class ShowQueries
{
    public function handle(Request $request, Closure $next)
    {
        if (! config('app.debug') || ! $request->boolean('sql')) {
            return $next($request);
        }

        // ★ 컨트롤러가 돌기 '전에' 듣기 시작해야 그 안에서 나간 SQL 을 잡는다.
        $queries = [];
        DB::listen(function ($q) use (&$queries) {
            $sql = $q->sql;
            foreach ($q->bindings as $b) {                       // ? 자리에 실제 값을 끼워 보여준다
                $sql = preg_replace('/\?/', is_numeric($b) ? $b : "'" . $b . "'", $sql, 1);
            }
            $queries[] = ['sql' => $sql, 'ms' => $q->time];
        });

        $response = $next($request);

        // HTML 화면일 때만 덧붙인다 (JSON 응답은 건드리지 않는다)
        if (! str_contains((string) $response->headers->get('Content-Type'), 'text/html')) {
            return $response;
        }

        $rows = '';
        foreach ($queries as $i => $q) {
            $rows .= '<p style="margin:6px 0"><b>' . ($i + 1) . '.</b> <code>' . e($q['sql']) . '</code>'
                   . ' <span style="opacity:.6">(' . $q['ms'] . 'ms)</span></p>';
        }

        $panel = '<div style="margin:24px;padding:16px;border:2px solid #e74c3c;border-radius:8px;'
               . 'background:#1a1a1a;color:#eee;font-size:13px;line-height:1.5;overflow-x:auto">'
               . '<b style="color:#e74c3c">이 화면이 실행한 SQL — ' . count($queries) . '개</b>'
               . $rows . '</div>';

        $response->setContent(str_replace('</body>', $panel . '</body>', $response->getContent()));

        return $response;
    }
}
