-- ============================================================
-- schema.sql — 리뷰 커뮤니티 DB 표(테이블) 설계
--   이 파일을 DBeaver에서 실행하면 표가 만들어진다.
--   ★ Git으로 이 파일을 공유하면, 다른 컴퓨터에서도 똑같은 표를 재생성할 수 있다.
--     (DB 데이터 자체는 Git으로 안 옮겨지므로, '설계도'인 이 파일을 공유한다)
-- ============================================================

-- 어느 DB에 표를 만들지 지정 (없으면 엉뚱한 DB에 만들어짐)
USE review_community;

-- ── users : 회원 ────────────────────────────────────────────
--   다른 표(posts·comments·likes·votes)가 참조하는 '뿌리' 표라 제일 먼저 만든다.
CREATE TABLE users (
    id         INT AUTO_INCREMENT PRIMARY KEY,   -- 기본키. 안 넣으면 1,2,3… 자동 부여
    username   VARCHAR(20)  NOT NULL UNIQUE,      -- 아이디(로그인·신원 키). 비면 거부, 중복도 거부. 안 바뀜.
    nickname   VARCHAR(20)  NOT NULL,             -- 표시 이름(닉네임). 화면에 보이는 이름. 바꿀 수 있음. 가입 땐 아이디로 시작.
    password   VARCHAR(255) NOT NULL,             -- password_hash() 결과 (해시라 길어서 255)
    avatar     VARCHAR(255) DEFAULT NULL,         -- 프로필 이미지 주소 (uploads/avatars/). 없으면 NULL
    joined_at  DATETIME     DEFAULT NOW()         -- 가입 시각. 안 넣으면 '지금'이 자동으로 들어감
);

-- ── media : 작품(영화·드라마) ──────────────────────────────
--   posts가 참조하므로 posts보다 먼저 만든다.
CREATE TABLE media (
    id         INT AUTO_INCREMENT PRIMARY KEY,  -- 기본키
    slug       VARCHAR(50)  NOT NULL UNIQUE,     -- 주소용 영문이름 (?work=parasite). 겹치면 안 되니 UNIQUE
    title      VARCHAR(100) NOT NULL,            -- 작품명 (예: 기생충)
    director   VARCHAR(50),                       -- 감독 (없어도 됨 → NOT NULL 안 붙임)
    genre      VARCHAR(20),                       -- 영화 / 드라마
    year       INT,                               -- 개봉년도. 숫자라 INT (범위 검색 가능하게)
    tmdb_id    INT UNIQUE,                        -- TMDB 작품번호. 같은 작품 중복 저장 방지 + API 재조회용
    poster_url VARCHAR(255),                      -- 포스터 이미지 주소 (TMDB가 제공)
    overview   TEXT                               -- 줄거리 (TMDB 제공, 길어서 TEXT). 게시판 작품 정보에 표시
);

-- ── posts : 글 ──────────────────────────────────────────────
--   ★ 외래키가 처음 등장. 글이 '누가(users)' '어느 작품에(media)' 썼는지를 번호로 가리킨다.
--   그래서 users·media가 먼저 있어야 이 표를 만들 수 있다 (참조 대상이 존재해야 하므로).
CREATE TABLE posts (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    author_id  INT NOT NULL,                          -- 누가 → users.id
    media_id   INT NOT NULL,                          -- 어느 작품 → media.id
    title      VARCHAR(100) NOT NULL,                 -- 글 제목 (짧으니 VARCHAR)
    content    TEXT,                                   -- 본문 (길이 들쭉날쭉하니 TEXT)
    sentiment  ENUM('호평','보통','혹평') NOT NULL,   -- 감상. 셋 중 하나만 허용 (DB가 검사)
    views      INT DEFAULT 0,                          -- 조회수. 새 글은 0
    created_at DATETIME DEFAULT NOW(),
    edited_at  DATETIME DEFAULT NULL,                  -- 수정한 시각. NULL=한 번도 안 고침 → 화면의 '(수정됨)'
    deleted_at DATETIME DEFAULT NULL,                  -- 소프트삭제. NULL=살아있음, 값=지워짐(되돌리기 가능)

    -- 외래키 선언: 이 열이 다른 표의 기본키를 가리킨다고 DB에 알린다.
    --   → 없는 회원/작품 번호를 넣으면 DB가 거부한다 (데이터 무결성을 DB가 지켜줌).
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (media_id)  REFERENCES media(id)
);

-- ── comments : 댓글 ─────────────────────────────────────────
--   댓글이 '어느 글에(posts)' '누가(users)' 달았는지를 번호로 가리킨다.
--   ★ ON DELETE CASCADE: 글이 지워지면 그 글의 댓글도 자동으로 함께 삭제된다.
--     (게시판에선 이게 자연스러움. 없으면 "댓글부터 다 지워야 글 삭제 가능")
--   ★ parent_id: comments가 '자기 자신'을 가리키는 외래키(자기참조).
--     대댓글은 새 표를 만들지 않는다 — 답글도 결국 댓글이라 같은 표에 담고,
--     "누구의 답글인지"만 한 칸 더 적으면 된다. (표가 자기 가계도를 갖는 셈)
CREATE TABLE comments (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    post_id    INT NOT NULL,                    -- 어느 글 → posts.id
    author_id  INT NOT NULL,                    -- 누가 → users.id
    parent_id  INT DEFAULT NULL,                -- 어느 댓글의 답글인가 → comments.id (NULL = 원댓글)
    content    VARCHAR(500) NOT NULL,           -- 댓글 내용. 최대 500자를 아니까 VARCHAR
    created_at DATETIME DEFAULT NOW(),
    edited_at  DATETIME DEFAULT NULL,            -- 수정한 시각 (NULL = 한 번도 안 고침)
    deleted_at DATETIME DEFAULT NULL,            -- 소프트삭제 (글과 동일)

    FOREIGN KEY (post_id)   REFERENCES posts(id) ON DELETE CASCADE,  -- 글 지우면 댓글도 삭제
    FOREIGN KEY (author_id) REFERENCES users(id),                    -- 회원은 함부로 못 지우게 기본값
    -- 원댓글이 '영구삭제'되면 그 답글도 함께 사라진다. (소프트삭제는 표식만 남기므로 영향 없음)
    --   ★ 이 제약에만 이름(CONSTRAINT fk_…)을 붙인 이유:
    --     sql/migrations/ 의 파일이 "이 외래키가 이미 있나?"를 IF NOT EXISTS로 판단하는데,
    --     그 판단 기준이 '칼럼'이 아니라 '제약 이름'이다. 이름을 안 주면 DB가 1·2·3 같은
    --     임의 이름을 붙여버려서, 마이그레이션이 못 알아보고 똑같은 외래키를 하나 더 만든다.
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

-- ── likes : 글 추천 (1인 1회) ───────────────────────────────
--   ★ 복합 기본키: (user_id, post_id) 조합이 겹치면 안 된다 → 같은 사람이 같은 글을 두 번 추천 불가.
--     week14에서 코드로 관리하던 '1인 1회'를 DB 구조가 대신 보장한다.
--   ★ id 열이 없다: '누가 뭘 눌렀나'가 전부라, 그 조합 자체가 줄을 유일하게 규정한다.
CREATE TABLE likes (
    user_id  INT NOT NULL,                    -- 누가 → users.id
    post_id  INT NOT NULL,                    -- 어느 글 → posts.id

    PRIMARY KEY (user_id, post_id),           -- 두 열을 묶은 기본키 = 조합이 유일

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,  -- 회원 지우면 그 추천도 삭제
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE   -- 글 지우면 그 추천도 삭제
);

-- ── votes : 작품 추천/비추천 투표 (1인 1표) ─────────────────
--   likes와 같은 구조 + '어느 쪽에 투표했나(추천/비추천)'를 담는 choice 열 추가.
--   갈아타기(추천→비추천)는 choice만 UPDATE하면 되므로, 조합은 여전히 (user, media) 하나.
CREATE TABLE votes (
    user_id  INT NOT NULL,                          -- 누가 → users.id
    media_id INT NOT NULL,                          -- 어느 작품 → media.id
    choice   ENUM('추천','비추천') NOT NULL,        -- 어느 쪽 (둘 중 하나만)

    PRIMARY KEY (user_id, media_id),                -- (사람, 작품) 조합이 유일 = 1인 1표

    FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE
);

-- ── reports : 글 신고 ───────────────────────────────────────
--   likes와 달리 id(기본키)를 따로 둔다: 같은 신고라도 '사유·시각' 같은 자기 정보를 가지므로.
--   단, 같은 사람이 같은 글을 도배 신고하지 못하게 (reporter_id, post_id)에 UNIQUE를 건다.
CREATE TABLE reports (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    post_id     INT NOT NULL,                        -- 어느 글 → posts.id
    reporter_id INT NOT NULL,                        -- 누가 신고 → users.id
    reason      ENUM('스팸/광고','욕설/비방','스포일러','기타') NOT NULL,
    created_at  DATETIME DEFAULT NOW(),

    UNIQUE KEY uq_report (reporter_id, post_id),     -- 1인 1글 1회 (도배 방지)
    FOREIGN KEY (post_id)     REFERENCES posts(id) ON DELETE CASCADE,  -- 글 지우면 신고도 삭제
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── notifications : 알림 ('내 글에 댓글' · '내 댓글에 답글') ──
--   받는사람(user_id)에게, 누가(actor_id) 어느 글(post_id)의 어느 댓글(comment_id)을
--   달았는지 기록한다. is_read로 안읽음/읽음을 구분(상단바 뱃지 계산에 사용).
--   ★ type = 알림의 종류. 한 표에 두 종류가 섞이므로, 화면이 이 값을 보고 문구를 고른다.
--     받는 사람도 달라진다 — 댓글은 '글 주인'에게, 답글은 '댓글 주인'에게.
CREATE TABLE notifications (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,                         -- 받는 사람 → users.id
    actor_id    INT NOT NULL,                         -- 알림을 일으킨 사람(댓글·답글 단 사람) → users.id
    type        ENUM('comment','reply') NOT NULL DEFAULT 'comment',  -- 알림 종류
    post_id     INT NOT NULL,                         -- 어느 글 → posts.id
    comment_id  INT NOT NULL,                         -- 어느 댓글(답글) → comments.id
    is_read     TINYINT(1) NOT NULL DEFAULT 0,        -- 0=안읽음, 1=읽음
    created_at  DATETIME DEFAULT NOW(),

    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (actor_id)   REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (post_id)    REFERENCES posts(id)    ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
);




-- ── sessions : 세션 저장소 ────────────────────────────────
--   week16에서 추가. PHP 기본값은 '서버의 임시 파일'인데, 그걸 이 표로 옮겼다.
--   ★ 옮긴 이유 셋: ①서버를 늘리면 파일은 공유가 안 된다 ②'이 회원의 세션 전부 끊기'가
--     가능해진다(user_id 칼럼) ③세션이 눈에 보인다.
--   ★ 세션 ID도 원본이 아니라 지문(SHA-256)으로 넣는다 — 유출돼도 되돌릴 수 없게.
--   자세한 설계 근거는 sql/migrations/005_sessions.sql 주석 참고.
CREATE TABLE sessions (
    id_hash     CHAR(64) PRIMARY KEY,          -- SHA-256(세션 ID). 원본은 어디에도 안 남긴다
    user_id     INT          NULL,             -- 비로그인 방문자도 세션이 있으므로 NULL 허용
    payload     TEXT         NOT NULL,         -- PHP가 직렬화한 세션 내용
    ip_address  VARCHAR(45)  NULL,             -- IPv6까지 담으려면 45글자
    user_agent  VARCHAR(255) NULL,
    last_active DATETIME     NOT NULL,         -- 새로고침할 때마다 갱신
    expires_at  DATETIME     NOT NULL,         -- 청소(gc)의 기준
    created_at  DATETIME     DEFAULT NOW(),

    INDEX idx_sessions_expires (expires_at),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


-- ── drafts : 글쓰기 임시저장(초안) ────────────────────────
--   week16에서 추가. 처음엔 세션에 담았다가 표로 옮겼다 —
--   세션은 창을 닫으면 사라지는데, 임시저장은 그때도 살아 있어야 쓸모가 있기 때문.
--   ★ 복합 기본키(user_id, work_slug) = "한 사람이 한 작품에 초안 하나".
--   자세한 설계 근거는 sql/migrations/006_drafts.sql 주석 참고.
CREATE TABLE drafts (
    user_id    INT          NOT NULL,        -- 누구의 초안인가 → users.id
    work_slug  VARCHAR(100) NOT NULL,        -- 어느 작품 게시판에 쓰던 글인가
    title      VARCHAR(200) NOT NULL DEFAULT '',
    content    TEXT         NOT NULL,
    sentiment  VARCHAR(10)  NOT NULL DEFAULT '',
    updated_at DATETIME     NOT NULL,        -- 오래된 초안 정리 기준

    PRIMARY KEY (user_id, work_slug),
    INDEX idx_drafts_updated (updated_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


-- ── consent_log : 쿠키 동의 증빙 ──────────────────────────
--   week16에서 추가. **고치지 않고 쌓기만 하는(append-only) 표.**
--   ★ 왜 쿠키만으로는 안 되나: 동의를 받았다는 사실을 증명할 책임은 우리에게 있는데,
--     쿠키는 사용자가 지울 수도 만들 수도 있어 증거가 못 된다.
--     → 쿠키는 '배너를 띄울까'를 빨리 답하는 캐시, 이 표가 증빙 원본이다.
--   ★ 철회해도 지난 줄을 안 지운다 — '동의했다가 철회함'과 '동의한 적 없음'은 다른 사실이다.
--   ★ IP는 일부러 안 담는다 — 증빙을 남기려고 개인정보를 더 모으면 본말이 전도된다.
--   자세한 설명은 migrations/007_consent_log.sql
CREATE TABLE consent_log (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    consent_id     CHAR(32)     NOT NULL,     -- 이 브라우저의 무작위 번호 (열쇠가 아니라 지문을 안 쓴다)
    user_id        INT          NULL,         -- 비로그인이면 NULL — 동의는 로그인보다 먼저 받는다
    action         VARCHAR(10)  NOT NULL,     -- grant | revoke | reset | link
    source         VARCHAR(10)  NOT NULL,     -- banner | settings | login
    policy_version SMALLINT     NOT NULL,     -- 어떤 안내문에 동의했나
    items          VARCHAR(255) NOT NULL,     -- {"view":1,"search":0} — 그 시점의 선택 통째로
    user_agent     VARCHAR(255) NOT NULL DEFAULT '',
    created_at     DATETIME     NOT NULL DEFAULT NOW(),

    INDEX idx_consent_browser (consent_id, id),
    INDEX idx_consent_user (user_id, id)
    -- ★ users로 가는 FOREIGN KEY를 안 건다 — 탈퇴하면 증빙이 사라지면 증빙이 아니다.
);
