<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// users 에 탈퇴 관련 칸 두 개를 더한다.
//
//   deleted_at    탈퇴를 요청한 시각. 이 값이 있으면 '탈퇴한 상태'다.
//                 줄은 그대로 있고 화면에서만 사라진다(소프트 삭제).
//                 유예 기간 안에 다시 로그인하면 이 값을 지워 되돌린다.
//
//   anonymized_at 개인정보를 실제로 지운 시각. 여기까지 오면 되돌릴 수 없다.
//
//   ★ 왜 두 단계인가 —
//     deleted_at 만 찍는 것은 '감추기'일 뿐 '지우기'가 아니다.
//     아이디·이메일·구글 연결은 DB 에 그대로 읽을 수 있게 남아 있다.
//     유예 기간이 지나면 예약 작업이 그 값들을 실제로 비운다.
//
//   ★ 줄 자체를 지우지 않는 이유 —
//     posts.author_id 와 comments.author_id 가 RESTRICT 다. 글이 있는 회원은
//     DB 가 삭제를 거부한다. 그리고 우리는 글을 남기기로 했으므로,
//     글이 가리킬 대상이 계속 있어야 '탈퇴한 사용자'라고 보여 줄 수 있다.
//     개인정보만 비우면 그 줄은 더 이상 누구의 것도 아니게 된다.
// ============================================================
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->timestamp('deleted_at')->nullable()->after('joined_at');
            $table->timestamp('anonymized_at')->nullable()->after('deleted_at');
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['deleted_at', 'anonymized_at']);
        });
    }
};
