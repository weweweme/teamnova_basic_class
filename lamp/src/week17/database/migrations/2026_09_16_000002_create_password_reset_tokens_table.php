<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// 비밀번호 재설정 토큰 표 — Laravel 이 정해 둔 모양 그대로다.
//
//   ★ 우리가 읽고 쓰지 않는다. 재설정을 요청하면 프레임워크가 여기에 토큰을 넣고,
//     링크를 눌러 들어오면 대조하고, 다 쓰면 지운다.
//   ★ 토큰은 해시로 저장된다. 표를 열어 봐도 원본 링크를 만들 수 없다.
// ============================================================
return new class extends Migration
{
    public function up(): void
    {
        Schema::create('password_reset_tokens', function (Blueprint $table) {
            $table->string('email')->primary();
            $table->string('token');
            $table->timestamp('created_at')->nullable();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('password_reset_tokens');
    }
};
