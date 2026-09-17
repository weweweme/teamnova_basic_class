<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// jobs — '나중에 할 일'을 담아 두는 표
//
//   ★ 왜 필요한가 —
//     지금은 댓글이 달리면 그 자리에서 메일을 보낸다. Gmail 서버에 연결해
//     한 통 보내는 데 3.8초가 걸린다(실측). 그동안 댓글 쓴 사람의 화면은 멈춰 있다.
//     메일은 '지금 당장'일 필요가 없는 일인데, 사용자를 기다리게 하고 있다.
//
//     이 표에 '할 일'을 한 줄 적어 두면(0.01초) 화면은 곧바로 넘어가고,
//     뒤에서 도는 워커가 그 줄을 꺼내 실제로 보낸다.
//
//   ★ 칸의 뜻 —
//     payload      무슨 일을 할지 (직렬화된 작업 내용)
//     attempts     몇 번 시도했는지 — 실패하면 다시 꺼내 보므로 횟수를 센다
//     reserved_at  워커가 집어 든 시각. 비어 있으면 아무도 안 잡은 일이다
//                  (워커가 여럿이어도 같은 일을 두 번 하지 않게 하는 장치)
//     available_at 이 시각 전에는 꺼내지 않는다 — '10초 뒤에 해라'가 가능한 이유
// ============================================================
return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('jobs', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->string('queue')->index();
            $table->longText('payload');
            $table->unsignedSmallInteger('attempts');
            $table->unsignedInteger('reserved_at')->nullable();
            $table->unsignedInteger('available_at');
            $table->unsignedInteger('created_at');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('jobs');
    }
};
