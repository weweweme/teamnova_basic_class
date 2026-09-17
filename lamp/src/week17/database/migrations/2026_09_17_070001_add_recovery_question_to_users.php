<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ============================================================
// users 에 '비밀번호 찾기 질문'을 더한다.
//
//   ★ 왜 필요한가 —
//     이메일은 선택이다. 주소를 넣지 않은 회원은 비밀번호를 잊으면
//     되찾을 방법이 아예 없다. 그 사람들을 위한 두 번째 통로다.
//
//   ★ 답은 해시로 저장한다 —
//     비밀번호와 같은 취급이다. 표를 들여다봐도 답을 알 수 없어야 한다.
//     답을 그대로 두면 DB 가 새는 순간 모든 계정의 두 번째 열쇠가 함께 샌다.
//
//   ⚠ 이 방식의 한계를 알고 쓴다 —
//     "어머니 성함" 같은 답은 검색으로 알아낼 수 있다. 비밀번호보다 약하다.
//     그래서 이메일이 있으면 그쪽을 권하고, 이 통로에는 시도 횟수 제한을 건다.
// ============================================================
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('recovery_question', 200)->nullable()->after('google_id');
            $table->string('recovery_answer', 255)->nullable()->after('recovery_question');
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['recovery_question', 'recovery_answer']);
        });
    }
};
