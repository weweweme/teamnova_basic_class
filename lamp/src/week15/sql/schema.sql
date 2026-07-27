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
    username   VARCHAR(20)  NOT NULL UNIQUE,      -- 아이디. 비면 거부, 중복도 거부
    password   VARCHAR(255) NOT NULL,             -- password_hash() 결과 (해시라 길어서 255)
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
    poster_url VARCHAR(255)                       -- 포스터 이미지 주소 (TMDB가 제공)
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
CREATE TABLE comments (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    post_id    INT NOT NULL,                    -- 어느 글 → posts.id
    author_id  INT NOT NULL,                    -- 누가 → users.id
    content    VARCHAR(500) NOT NULL,           -- 댓글 내용. 최대 500자를 아니까 VARCHAR
    created_at DATETIME DEFAULT NOW(),

    FOREIGN KEY (post_id)   REFERENCES posts(id) ON DELETE CASCADE,  -- 글 지우면 댓글도 삭제
    FOREIGN KEY (author_id) REFERENCES users(id)                     -- 회원은 함부로 못 지우게 기본값
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
