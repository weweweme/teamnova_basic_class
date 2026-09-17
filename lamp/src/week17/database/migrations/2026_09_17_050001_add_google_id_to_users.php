<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// users 에 '구글 계정 번호' 칸을 더한다.
//
//   ★ 왜 이메일로 잇지 않고 번호를 따로 두나 —
//     사람은 구글 계정의 이메일 주소를 바꿀 수 있다. 주소로 이어 두면
//     그 순간 남이 되어 버린다. 구글이 계정마다 붙여 주는 번호(sub)는 바뀌지 않는다.
//     주소는 '처음 가입할 때 누구인지 찾는 용도'로만 쓰고, 연결은 이 번호로 잡는다.
//
//   ★ unique — 한 구글 계정은 한 사람에게만 이어진다.
//   ★ nullable — 아이디·비밀번호로 가입한 기존 회원 25명은 비어 있다. 그대로 쓴다.
// ============================================================
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('google_id', 40)->nullable()->unique()->after('email_verified_at');
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('google_id');
        });
    }
};
