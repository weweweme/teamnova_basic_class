<?php

use Illuminate\Foundation\Inspiring;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Schedule;

Artisan::command('inspire', function () {
    $this->comment(Inspiring::quote());
})->purpose('Display an inspiring quote');

// ============================================================
// 예약 작업 — '언제 무엇을 할지'를 여기 한 곳에 적는다
//
//   ★ 지금까지는 이런 일을 할 자리가 없었다. 사람이 기억해서 해야 했고,
//     그래서 결국 안 했다(버려진 업로드 사진이 쌓이고 있었다).
//
//   ★ 시각표는 여기 있고, 실제로 깨우는 일은 바깥에서 한다.
//     운영 서버라면 cron 이 1분마다 `artisan schedule:run` 을 부르고,
//     개발 중에는 `artisan schedule:work` 한 줄이 그 역할을 대신한다.
//     어느 쪽이든 '무엇을 언제' 는 이 파일만 보면 된다.
// ============================================================

// 새벽 4시 10분 — 어느 글에도 쓰이지 않는 업로드 사진을 지운다
Schedule::command('uploads:prune')
    ->dailyAt('04:10')
    ->withoutOverlapping();     // 앞 작업이 아직 돌고 있으면 건너뛴다

// 새벽 4시 30분 — 유예 기간이 지난 탈퇴 계정의 개인정보를 비운다
//   ★ '지우겠다'고 약속한 것을 실제로 지우는 자리다.
//     사람이 기억해서 하는 일로 두면 결국 안 하게 된다.
Schedule::command('accounts:anonymize')->dailyAt('04:30')->withoutOverlapping();

// 새벽 4시 20분 — 끝내 실패한 작업 기록을 일주일치만 남긴다
Schedule::command('queue:prune-failed --hours=168')->dailyAt('04:20');
