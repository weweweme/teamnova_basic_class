#!/bin/bash
# ============================================================
# entrypoint.sh — 컨테이너가 켜질 때마다 실행되는 시작 스크립트
#
#   목적: 새 컴퓨터에서 `docker compose up` 한 번이면 바로 사이트가 뜨게 한다.
#         (전에는 MariaDB 설치·계정 생성·SQL 실행을 매번 손으로 했다)
#
#   ★ 모든 단계가 "없으면 만들고, 있으면 건너뛴다".
#     그래서 몇 번을 껐다 켜도 안전하고, 이미 있는 데이터를 덮어쓰지 않는다.
#     (이런 성질을 멱등성이라고 한다 — 몇 번 눌러도 결과가 같은 엘리베이터 버튼처럼)
# ============================================================

# 설정값들 — 환경변수로 바꿀 수 있게 하되, 없으면 기본값을 쓴다
APP_DIR="${APP_DIR:-/var/www/html/week16}"       # 이번 주차 폴더 (주차가 바뀌면 여기만 바꾼다)
DB_NAME="${DB_NAME:-review_community}"
DB_USER="${DB_USER:-dev}"
DB_PASS="${DB_PASS:-dev1234}"
DB_ROOT_PASS="${DB_ROOT_PASS:-root1234}"

MARIADB_DIR="/usr/local/mariadb"
MARIADB_DATA="$MARIADB_DIR/data"
MARIADB_SOCK="/tmp/mysql.sock"
APACHE_DIR="/usr/local/apache2"

# 로그를 눈에 띄게 (docker compose logs 에서 찾기 쉽게)
say() { echo "[entrypoint] $*"; }

# ── 1. Apache 설정 파일 반영 ────────────────────────────────
#   설정을 컨테이너 안이 아니라 호스트(apache/httpd.conf)에 두고 Git으로 관리한다.
#   컨테이너가 날아가도 설정은 남고, 변경 이력도 남는다.
#   ※ 맥 도커는 '파일 하나만 마운트'하면 편집 시 연결이 끊기므로, 폴더를 마운트해 복사하는 방식을 쓴다.
if [ -f /host-apache/httpd.conf ]; then
    cp /host-apache/httpd.conf "$APACHE_DIR/conf/httpd.conf"
    say "Apache 설정 반영 완료"
else
    say "경고: /host-apache/httpd.conf 가 없다 — 컨테이너 안 기존 설정을 그대로 쓴다"
fi

# ── 2. MariaDB '설치' (데이터 폴더가 비었을 때만) ───────────
#   mariadb-install-db 는 DB 서버가 돌아가는 데 필요한 시스템 표(계정 목록 등)를 만든다.
#   게임으로 치면 세이브 파일이 아니라 '게임 설치' 단계. 한 번만 하면 된다.
if [ -z "$(ls -A "$MARIADB_DATA" 2>/dev/null)" ]; then
    say "MariaDB 데이터 폴더가 비어 있다 → 설치 시작"
    "$MARIADB_DIR/scripts/mariadb-install-db" \
        --basedir="$MARIADB_DIR" --datadir="$MARIADB_DATA" --user=root > /tmp/install-db.log 2>&1
    if [ $? -ne 0 ]; then
        say "오류: MariaDB 설치 실패. 자세한 내용은 컨테이너 안 /tmp/install-db.log 참고"
        exit 1
    fi
    say "MariaDB 설치 완료"
else
    say "MariaDB 데이터 폴더에 내용이 있다 → 설치 건너뜀"
fi

# ── 3. MariaDB 서버 기동 + 접속될 때까지 대기 ───────────────
#   백그라운드(&)로 띄운다. 서버가 준비되기 전에 다음 단계로 가면 실패하므로,
#   실제로 접속이 될 때까지 최대 60초 기다린다. (그냥 sleep 하면 느린 PC에서 깨진다)
# 켜기 전에, 이전에 비정상 종료돼서 남은 socket 파일을 치운다.
#   socket 파일은 "DB가 여기 있다"고 알려주는 문 앞의 명패다.
#   정상 종료하면 서버가 스스로 떼는데, 강제 종료되면 명패만 남는다.
#   그 상태로 켜면 "이미 누가 쓰는 중"이라고 판단해 기동을 거부할 수 있다.
#   ★ 지금 실제로 접속되는지 먼저 확인하고, 응답이 없을 때만 지운다.
#     (돌고 있는 DB의 명패를 떼면 접속이 끊기므로 순서가 중요하다)
if [ -e "$MARIADB_SOCK" ]; then
    if ! "$MARIADB_DIR/bin/mariadb" --socket="$MARIADB_SOCK" -uroot -e "SELECT 1" > /dev/null 2>&1 \
       && ! "$MARIADB_DIR/bin/mariadb" --socket="$MARIADB_SOCK" -uroot -p"$DB_ROOT_PASS" -e "SELECT 1" > /dev/null 2>&1; then
        say "응답 없는 socket 파일이 남아 있다 → 정리"
        rm -f "$MARIADB_SOCK"
    fi
fi

say "MariaDB 기동 중..."
"$MARIADB_DIR/bin/mariadbd-safe" \
    --datadir="$MARIADB_DATA" --user=root \
    --socket="$MARIADB_SOCK" --port=3306 --bind-address=0.0.0.0 \
    > /tmp/mariadb-safe.log 2>&1 &

# root 접속 방식 결정: 갓 설치했으면 비밀번호가 없고, 이미 쓰던 DB면 비밀번호가 걸려 있다.
#   두 경우를 모두 처리하려고, 먼저 비번 없이 시도해보고 안 되면 비번을 붙인다.
ROOT_ARGS=(-uroot)
for i in $(seq 1 30); do
    if "$MARIADB_DIR/bin/mariadb" --socket="$MARIADB_SOCK" -uroot -e "SELECT 1" > /dev/null 2>&1; then
        ROOT_ARGS=(-uroot)
        break
    fi
    if "$MARIADB_DIR/bin/mariadb" --socket="$MARIADB_SOCK" -uroot -p"$DB_ROOT_PASS" -e "SELECT 1" > /dev/null 2>&1; then
        ROOT_ARGS=(-uroot "-p$DB_ROOT_PASS")
        break
    fi
    sleep 2
done

# 위 반복이 끝나도 접속이 안 되면 더 진행할 수 없다
if ! "$MARIADB_DIR/bin/mariadb" --socket="$MARIADB_SOCK" "${ROOT_ARGS[@]}" -e "SELECT 1" > /dev/null 2>&1; then
    say "오류: MariaDB에 접속할 수 없다. 컨테이너 안 /tmp/mariadb-safe.log 참고"
    exit 1
fi
say "MariaDB 기동 완료"

# 짧게 쓰기 위한 도우미 함수 (root로 SQL 한 줄 실행)
run_root_sql() {
    "$MARIADB_DIR/bin/mariadb" --socket="$MARIADB_SOCK" "${ROOT_ARGS[@]}" \
        --default-character-set=utf8mb4 -N -B -e "$1" 2>/dev/null
}

# ── 4. DB · 계정 만들기 (없을 때만) ─────────────────────────
#   ★ 문자셋을 utf8mb4로 지정하는 게 중요하다.
#     schema.sql 의 CREATE TABLE 에는 문자셋 지정이 없어서, 표는 DB의 기본 문자셋을 물려받는다.
#     여기서 utf8mb4를 안 주면 한글이 전부 ??? 로 깨진다.
DB_EXISTS=$(run_root_sql "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME';")
if [ "$DB_EXISTS" != "1" ]; then
    say "DB '$DB_NAME' 생성 중..."
    run_root_sql "CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    say "DB 생성 완료"
fi

# 앱이 쓰는 계정. '%' = 어디서 접속하든 허용 (컨테이너 밖 DBeaver에서도 붙게)
run_root_sql "CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASS';"
run_root_sql "GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';"

# 기본 설치 상태에 남는 익명 계정과 test DB는 지운다 (아무나 접속 가능해서 위험)
run_root_sql "DELETE FROM mysql.user WHERE User='';"
run_root_sql "DROP DATABASE IF EXISTS test;"

# root 비밀번호 설정 (아직 안 걸려 있을 때만)
if [ "${ROOT_ARGS[*]}" = "-uroot" ]; then
    run_root_sql "ALTER USER 'root'@'localhost' IDENTIFIED BY '$DB_ROOT_PASS';"
    ROOT_ARGS=(-uroot "-p$DB_ROOT_PASS")
    say "root 비밀번호 설정 완료"
fi
run_root_sql "FLUSH PRIVILEGES;"

# ── 5. 표·데이터 넣기 (표가 없거나 비었을 때만) ─────────────
#   DB 데이터는 Git으로 안 옮겨진다 → '설계도(schema.sql)'와 '데이터(seed.sql)'로 재생성한다.
#   --default-character-set=utf8mb4 : 보내는 쪽도 utf8mb4라고 알려줘야 한글이 안 깨진다.
#     (DB만 utf8mb4로 해두고 이걸 빠뜨리면, 서버가 다른 문자셋으로 알아듣고 변환해버린다)
SQL_DIR="$APP_DIR/sql"
load_sql() {
    "$MARIADB_DIR/bin/mariadb" --socket="$MARIADB_SOCK" "${ROOT_ARGS[@]}" \
        --default-character-set=utf8mb4 "$DB_NAME" < "$1"
}

USER_COUNT=$(run_root_sql "SELECT COUNT(*) FROM $DB_NAME.users;")
if [ -z "$USER_COUNT" ]; then
    # 표 자체가 없어서 조회가 실패한 경우 → 설계도부터 만든다
    if [ -f "$SQL_DIR/schema.sql" ]; then
        say "표가 없다 → schema.sql 실행"
        load_sql "$SQL_DIR/schema.sql" && say "표 생성 완료"
        USER_COUNT=0
    else
        say "경고: $SQL_DIR/schema.sql 을 찾을 수 없다"
    fi
fi

if [ "$USER_COUNT" = "0" ] && [ -f "$SQL_DIR/seed.sql" ]; then
    say "데이터가 비어 있다 → seed.sql 실행 (시간이 조금 걸린다)"
    load_sql "$SQL_DIR/seed.sql" && say "데이터 입력 완료"
elif [ -n "$USER_COUNT" ] && [ "$USER_COUNT" != "0" ]; then
    say "이미 데이터가 있다 (회원 ${USER_COUNT}명) → seed 건너뜀"
fi

# ── 6. 표 변경(마이그레이션) 적용 ───────────────────────────
#   [왜 필요한가]
#     schema.sql은 '표가 아예 없을 때'만 실행된다(위 5단계). 그래서 이미 쓰던 DB가 있는
#     컴퓨터에서는 schema.sql에 칼럼을 추가해도 아무 일도 일어나지 않는다 — 표가 이미 있으니까.
#     "새 PC는 되는데 내 PC만 안 되네"가 딱 이 상황이다.
#     → 이미 만들어진 표를 고치는 명령(ALTER TABLE)은 sql/migrations/ 에 파일로 쌓고,
#       여기서 '아직 적용 안 된 것만' 골라 실행한다.
#
#   [어떻게 '이미 했는지'를 아는가]
#     적용한 파일 이름을 schema_migrations 표에 적어둔다. DB 안에 기록이 남으므로,
#     컨테이너를 지우고 다시 만들어도(볼륨은 살아있으니) 두 번 실행되지 않는다.
#
#   비유: 게임 패치 노트. 이미 깐 패치는 건너뛰고 새 패치만 순서대로 적용한다.
MIGRATIONS_DIR="$SQL_DIR/migrations"
if [ -d "$MIGRATIONS_DIR" ]; then
    # 적용 기록표 (없으면 만든다)
    run_root_sql "CREATE TABLE IF NOT EXISTS \`$DB_NAME\`.schema_migrations (
        filename   VARCHAR(255) NOT NULL PRIMARY KEY,
        applied_at DATETIME DEFAULT NOW()
    );"

    MIG_APPLIED=0
    # *.sql 은 이름 순으로 펼쳐지므로 001_ → 002_ → … 순서가 보장된다.
    for MIG_FILE in "$MIGRATIONS_DIR"/*.sql; do
        # 폴더가 비어 있으면 위 패턴이 그대로 문자열로 남는다 → 실제 파일일 때만 진행
        [ -f "$MIG_FILE" ] || continue

        MIG_NAME=$(basename "$MIG_FILE")
        MIG_DONE=$(run_root_sql "SELECT COUNT(*) FROM \`$DB_NAME\`.schema_migrations WHERE filename='$MIG_NAME';")
        if [ "$MIG_DONE" = "1" ]; then
            continue   # 이미 적용함
        fi

        say "표 변경 적용: $MIG_NAME"
        if load_sql "$MIG_FILE"; then
            run_root_sql "INSERT INTO \`$DB_NAME\`.schema_migrations (filename) VALUES ('$MIG_NAME');"
            MIG_APPLIED=$((MIG_APPLIED + 1))
        else
            # 표가 어중간하게 바뀐 상태로 사이트를 띄우면 원인을 찾기 더 어려워진다.
            #   기록을 남기지 않으므로, 파일을 고쳐서 다시 켜면 이 파일부터 다시 시도한다.
            say "오류: $MIG_NAME 적용 실패 → 중단한다. 파일의 SQL을 확인할 것"
            exit 1
        fi
    done

    if [ "$MIG_APPLIED" = "0" ]; then
        say "표 변경 사항 없음 (마이그레이션 최신)"
    else
        say "표 변경 ${MIG_APPLIED}건 적용 완료"
    fi
fi

# ── 7. TMDB 캐시 폴더 만들기 ────────────────────────────────
#   Git은 빈 폴더를 기록하지 않아서, 새 컴퓨터에는 이 폴더가 없다.
#   없으면 캐시 저장이 조용히 실패해서 매번 TMDB를 새로 부르게 된다(홈이 크게 느려짐).
#   PHP 코드에도 같은 안전장치가 있지만, 여기서 미리 만들어 두면 첫 요청부터 빠르다.
mkdir -p "$APP_DIR/cache/tmdb"
say "TMDB 캐시 폴더 준비 완료"

# ── 8. 종료 신호를 받으면 DB부터 안전하게 닫기 ──────────────
#   `docker compose down` 은 이 스크립트에 '그만' 신호(SIGTERM)를 보낸다.
#   그때 MariaDB를 정식으로 닫아야 쓰다 만 데이터가 깨지지 않는다.
#   (게임을 강제종료하지 않고 '저장 후 종료'를 누르는 것과 같다)
shutdown_all() {
    say "종료 신호 수신 → MariaDB 안전 종료 중..."
    "$MARIADB_DIR/bin/mariadb-admin" --socket="$MARIADB_SOCK" "${ROOT_ARGS[@]}" shutdown 2>/dev/null
    kill -TERM "$HTTPD_PID" 2>/dev/null
}
trap shutdown_all SIGTERM SIGINT

# ── 9. Apache 실행 ──────────────────────────────────────────
#   백그라운드로 띄우고 wait 로 붙잡는다. 그래야 위의 종료 처리가 동작할 수 있다.
#   (바로 포그라운드로 띄우면 이 스크립트가 신호를 못 받는다)
say "Apache 시작 — http://localhost:8080/ 에서 확인"
"$APACHE_DIR/bin/httpd" -DFOREGROUND &
HTTPD_PID=$!
wait "$HTTPD_PID"
