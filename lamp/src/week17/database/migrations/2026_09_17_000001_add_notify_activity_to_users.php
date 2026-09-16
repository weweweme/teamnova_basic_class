<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// users 에 '활동 알림 메일을 받을지' 칸을 더한다.
//
//   ★ 기본값은 꺼짐이다. 쿠키 동의를 기본 꺼짐으로 둔 것과 같은 이유다 —
//     묻지 않고 보내는 메일은 동의가 아니다.
//   ★ 보안 알림(새 기기 로그인 · 비밀번호 변경)은 이 칸과 무관하게 항상 나간다.
//     본인이 하지 않은 일을 알리는 것이라, 끌 수 있으면 의미가 없다.
// ============================================================
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->boolean('notify_activity')->default(false)->after('email');
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('notify_activity');
        });
    }
};
