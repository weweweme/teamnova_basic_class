<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// users 에 이메일 칸을 더한다 — 비밀번호 찾기를 쓰기 위해서다.
//
//   ★ 이관 원칙에서 'DB 는 손대지 않는다' 를 정했었는데, 여기서 한 번 푼다.
//     칸 하나를 늘리는 대가로 토큰 발급 · 메일 발송 · 만료 처리 · 재설정 화면을
//     프레임워크에서 통째로 얻는다.
//
//   nullable — 기존 회원 25명은 이메일이 없다. 필수로 만들면 그 사람들이 막힌다.
//   unique   — 같은 주소로 두 계정을 만들지 못하게.
// ============================================================
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('email')->nullable()->unique()->after('username');
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropUnique(['email']);
            $table->dropColumn('email');
        });
    }
};
