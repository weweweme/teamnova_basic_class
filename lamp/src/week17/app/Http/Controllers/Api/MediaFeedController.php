<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\Tmdb;
use Illuminate\Http\Request;

// ============================================================
// MediaFeedController — 화면이 뜬 뒤 JS 가 불러 가는 작품 목록
//   지금 api/row.php · api/browse.php 두 파일에 해당한다.
//
//   ★ 왜 화면과 따로 두나
//     TMDB 호출은 느리다. 홈 HTML 에 넣고 기다리면 첫 화면이 그만큼 늦게 뜬다.
//     서버는 우리 DB 것만 담아 즉시 보내고, 무거운 가로줄은 JS 가 이 주소로 따로 받아 온다.
// ============================================================
class MediaFeedController extends Controller
{
    // ── 홈 가로줄 한 줄 (GET /api/row?kind=trending|movie|tv) ──
    public function row(Request $request, Tmdb $tmdb)
    {
        $kind = $request->query('kind', 'trending');
        if (! in_array($kind, ['trending', 'movie', 'tv'], true)) {
            $kind = 'trending';
        }

        return response()->json(['items' => $tmdb->row($kind)]);
    }

    // ── 작품 둘러보기 한 페이지 (GET /api/browse) ───────────
    public function browse(Request $request, Tmdb $tmdb)
    {
        $genre = (string) $request->query('genre', '');
        $media = (string) $request->query('media', 'all');
        if (! in_array($media, ['all', 'movie', 'tv', 'anime'], true)) {
            $media = 'all';
        }

        // 너무 깊이 들어가지 않는다 (TMDB 와 우리 서버 모두 보호)
        $page = max(1, min(20, (int) $request->query('page', 1)));

        return response()->json(['items' => $tmdb->discover($genre, $media, $page)]);
    }
}
