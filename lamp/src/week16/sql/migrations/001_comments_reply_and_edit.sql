-- ============================================================
-- 001_comments_reply_and_edit.sql
--   댓글에 '답글(대댓글)'과 '수정' 기능을 위한 칼럼을 추가한다.
--
--   [이 폴더는 무엇인가]
--     schema.sql은 '표를 처음 만들 때'만 실행된다. 이미 DB가 있는 컴퓨터에서는
--     schema.sql을 고쳐도 아무 일도 일어나지 않는다 — 표가 이미 있으니까.
--     그래서 '이미 만들어진 표를 고치는 명령'은 이 폴더에 따로 쌓는다.
--     entrypoint.sh가 컨테이너를 켤 때마다 번호 순서대로 확인해서, 아직 적용 안 된 것만 실행한다.
--
--   [파일 이름 규칙] 001_ 002_ 003_ … 처럼 앞에 번호를 붙인다.
--     번호 순서 = 적용 순서다. (뒤 번호가 앞 번호의 결과를 전제로 할 수 있으므로 순서가 중요)
--
--   [지켜야 할 것]
--     ① USE 문을 쓰지 않는다 — 어느 DB에 적용할지는 entrypoint가 정해준다.
--     ② IF NOT EXISTS를 붙여 '여러 번 실행해도 안전하게' 만든다.
--        새 컴퓨터는 schema.sql이 이 칼럼까지 갖춘 표를 처음부터 만들기 때문에,
--        그때 이 파일은 아무 일도 하지 않고 조용히 지나간다.
--     ③ schema.sql도 같이 고친다 — 새 컴퓨터가 처음부터 올바른 표를 갖도록.
-- ============================================================

-- parent_id : 이 댓글이 '어느 댓글의 답글'인지. NULL이면 원댓글.
-- edited_at : 수정한 시각. NULL이면 한 번도 안 고침 → 화면의 '(수정됨)' 표시에 쓴다.
ALTER TABLE comments
    ADD COLUMN IF NOT EXISTS parent_id INT      DEFAULT NULL AFTER author_id,
    ADD COLUMN IF NOT EXISTS edited_at DATETIME DEFAULT NULL AFTER created_at;

-- 자기참조 외래키 — comments가 자기 자신을 가리킨다.
--   원댓글을 '영구삭제'하면 그 답글도 함께 사라진다(CASCADE).
--   소프트삭제는 deleted_at에 시각만 찍는 UPDATE라 여기 영향을 받지 않는다.
--   ※ MariaDB는 IF NOT EXISTS를 FOREIGN KEY '뒤'에 쓴다.
--     (ADD CONSTRAINT IF NOT EXISTS … 로 쓰면 문법 오류)
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_parent FOREIGN KEY IF NOT EXISTS (parent_id)
        REFERENCES comments(id) ON DELETE CASCADE;
