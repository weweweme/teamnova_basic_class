#!/bin/sh
# ============================================================
# 큐 워커 + 예약 작업을 컨테이너 안에서 띄운다 / 끈다 / 상태를 본다
#
#   이 컨테이너에는 cron 도 supervisor 도 없다(손으로 만든 컨테이너라 그렇다).
#   운영 서버라면 systemd 나 supervisor 가 할 일을, 개발 중에는 이 스크립트가 한다.
#
#   쓰는 법
#     scripts/worker.sh start    워커와 예약 작업을 띄운다
#     scripts/worker.sh stop     둘 다 끈다
#     scripts/worker.sh status   지금 돌고 있는지 본다
#     scripts/worker.sh log      워커가 남긴 기록을 본다
#
#   ★ 컨테이너를 다시 시작하면 꺼진다. 그때는 start 를 한 번 더 부르면 된다.
# ============================================================
set -e

C=manual_lamp
APP=/var/www/html/week17
PHP=/usr/local/php/bin/php
LOG=$APP/storage/logs/worker.log

case "${1:-status}" in
  start)
    # --tries=3      실패하면 3번까지 다시 해 본다 (메일 서버가 잠깐 안 될 수 있다)
    # --backoff=10   실패 후 10초 쉬었다 다시 — 곧바로 다시 하면 또 실패한다
    # --max-time     한 시간마다 스스로 끝낸다. 오래 도는 프로세스가 메모리를 쥐고 있지 않게.
    docker exec -d $C sh -c "cd $APP && while true; do $PHP artisan queue:work --tries=3 --backoff=10 --max-time=3600 >> $LOG 2>&1; sleep 2; done"
    docker exec -d $C sh -c "cd $APP && $PHP artisan schedule:work >> $LOG 2>&1"
    echo "띄웠습니다. (기록: src/week17/storage/logs/worker.log)"
    ;;
  stop)
    docker exec $C sh -c "pkill -f 'artisan queue:work' || true; pkill -f 'artisan schedule:work' || true; pkill -f 'while true; do' || true"
    echo "껐습니다."
    ;;
  status)
    echo "-- 돌고 있는 프로세스 --"
    docker exec $C sh -c "ps -eo pid,args | grep -E 'artisan (queue:work|schedule:work)' | grep -v grep" || echo "  (없음)"
    echo "-- 처리를 기다리는 작업 --"
    docker exec $C sh -c "cd $APP && $PHP artisan tinker --execute=\"echo DB::table('jobs')->count();\"" 2>/dev/null | tail -1
    ;;
  log)
    docker exec $C sh -c "tail -40 $LOG" 2>/dev/null || echo "(아직 기록 없음)"
    ;;
  *)
    echo "쓰는 법: $0 {start|stop|status|log}"
    exit 1
    ;;
esac
