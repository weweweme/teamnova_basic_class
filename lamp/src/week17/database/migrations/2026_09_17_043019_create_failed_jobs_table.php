<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// failed_jobs — 여러 번 시도해도 끝내 실패한 일을 남겨 두는 표
//
//   ★ 실패한 일이 조용히 사라지면 안 된다. 메일이 안 갔는데 아무도 모르는 상황이
//     제일 나쁘다. 여기 남겨 두면 나중에 원인을 보고 다시 시도할 수 있다.
//     (artisan queue:failed 로 목록, queue:retry 로 재시도)
// ============================================================
return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('failed_jobs', function (Blueprint $table) {
            $table->id();
            $table->string('uuid')->unique();
            $table->string('connection');
            $table->string('queue');
            $table->longText('payload');
            $table->longText('exception');
            $table->timestamp('failed_at')->useCurrent();

            $table->index(['connection', 'queue', 'failed_at']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('failed_jobs');
    }
};
