<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// users 에 '이메일 주소를 확인한 시각' 칸을 더한다.
//
//   ★ 왜 필요한가 — 지금까지는 설정에 적은 주소를 그대로 믿었다.
//     그러면 남의 주소를 적어 둘 수 있고, 그 사람에게 내 알림 메일이 간다.
//     주소 칸이 unique 라 선점 문제도 생긴다(진짜 주인이 그 주소를 못 쓴다).
//     주인이 메일을 열어 링크를 눌러야만 이 칸이 채워지고, 그때부터 주소를 쓴다.
//
//   ★ 이름을 email_verified_at 으로 맞춘 이유 —
//     Laravel 의 MustVerifyEmail 이 이 칸 이름을 전제로 동작한다.
//     칸 이름만 맞추면 확인 여부 판단·기록을 프레임워크가 한다.
//
//   ★ 기존 회원 25명은 NULL 로 남는다 = 아직 확인하지 않음.
//     주소를 넣은 적이 없으니 맞는 상태다.
// ============================================================
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->timestamp('email_verified_at')->nullable()->after('email');
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('email_verified_at');
        });
    }
};
