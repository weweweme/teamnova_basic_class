<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use Illuminate\Foundation\Auth\EmailVerificationRequest;

// ============================================================
// 이메일 주소 확인
//
//   설정에 이메일을 적으면 그 주소로 확인 메일이 나간다.
//   메일 속 링크를 눌러야 '주인이 맞다'로 기록되고, 그때부터 알림을 켤 수 있다.
//
//   ★ 링크 주소에는 서명과 만료 시각이 함께 들어 있다.
//     수신거부 링크와 같은 방식인데, 이쪽은 만료가 붙는다(기본 60분).
//     주소를 고치거나 시간이 지나면 미들웨어가 403 으로 막는다.
// ============================================================
class EmailVerificationController extends Controller
{
    // ── 메일 속 링크를 눌렀을 때 (GET /email/verify/{id}/{hash}) ──
    //   ★ 받는 자리에 EmailVerificationRequest 를 적어 두면
    //     '로그인한 사람 = 링크의 주인인가', '링크의 hash 가 지금 주소와 맞는가'를
    //     프레임워크가 먼저 확인하고, 아니면 403 으로 막는다.
    //
    //     hash 를 함께 보는 이유: 메일을 받은 뒤 주소를 다른 것으로 바꿨다면
    //     옛 링크로는 확인되면 안 된다. hash 는 보낼 당시의 주소로 만든 값이다.
    public function verify(EmailVerificationRequest $request)
    {
        // 이미 확인한 주소면 아무 일도 하지 않는다 (같은 링크를 두 번 눌러도 안전하다).
        $request->fulfill();

        return redirect('/settings#mail')->with('status', '이메일 인증이 끝났습니다. 이제 댓글 알림 메일을 켤 수 있습니다.');
    }
}
