  </main>
  <!-- ============================================================
       footer.php — 모든 페이지 '맨 아래'에 공통으로 들어가는 조각.
       header.php에서 열어둔 <main> 을 여기서 닫고, body/html도 닫는다.
       ============================================================ -->
  <footer class="foot">
    <small>🎬 리뷰 커뮤니티 · 영화·드라마 리뷰 커뮤니티 · TMDB 제공</small>
    <?php // ★ 로그인과 무관한 자리에 둔다 — 동의는 비회원에게도 받으므로
          //   철회도 비회원이 할 수 있어야 한다. (settings/ 안에 있으면 회원만 거둘 수 있다) ?>
    <small><a href="/cookies.php">🍪 쿠키 설정</a></small>
  </footer>

  <!-- 삭제 확인 팝업 — 모든 페이지에 하나만 두고 재사용한다.
       예전엔 브라우저 기본 confirm() 창을 썼는데,
       신고 팝업과 생김새가 따로 놀아서 같은 <dialog> 방식으로 통일했다.
       ★ 실제 삭제는 여전히 각 삭제 폼의 POST가 한다 — 이 창은 '한 번 더 묻기'만 담당. -->
  <dialog id="confirm-dialog" class="modal">
    <h3>정말 삭제할까요?</h3>
    <p class="confirm-text" id="confirm-message">삭제하면 목록에서 사라집니다.</p>
    <p class="confirm-sub">삭제 후 알림의 '되돌리기'로 복구할 수 있어요.</p>
    <div class="modal-actions">
      <!-- 폼 밖의 버튼이지만, 습관적으로 type을 명시한다 -->
      <button type="button" id="confirm-cancel" class="btn-cancel">취소</button>
      <button type="button" id="confirm-ok" class="btn-danger">삭제</button>
    </div>
  </dialog>
  <?php // ── 쿠키 동의 배너 (아직 안 고른 사람에게만) ──────────────
        //   prefs.php를 부르지 않은 화면도 있으므로 함수가 있는지 먼저 확인한다.
        //
        //   ★ [확인] 버튼 하나짜리 '안내'에서 '동의'로 바꾼 자리다.
        //     거절할 수 없으면 물어본 게 아니기 때문에, 버튼이 셋이 됐다.
        //   ★ JS가 없다 — 폼으로 서버에 보낸다. 서버가 모르는 동의는 증명할 수 없으므로. ?>
  <?php if (function_exists('needs_cookie_consent') && needs_cookie_consent()): ?>
    <form class="cookie-notice" method="post" action="/consent.php">
      <?= csrf_field() ?>
      <?php // 누른 뒤 보던 화면으로 돌아가려고 지금 주소를 함께 보낸다. (받는 쪽에서 검증한다) ?>
      <input type="hidden" name="back" value="<?= e($_SERVER['REQUEST_URI'] ?? '/') ?>">

      <div class="cookie-notice-text">
        <p><strong>쿠키 사용에 동의해 주세요.</strong></p>

        <?php // 필수는 고를 수 없다 — 이게 없으면 로그인·글쓰기가 아예 안 된다. 대신 무엇인지 밝힌다. ?>
        <p class="muted cookie-required">
          <strong>필수</strong> — 로그인 유지, 위조 요청 방어, 화면 설정(정렬·감상·글 수),
          기기 번호(<strong>로그인 기기 목록</strong>).
          서비스가 돌아가려면 반드시 필요해서 <strong>끌 수 없습니다.</strong>
        </p>

        <?php // 여기부터가 진짜 '고르는' 부분. 기본값은 꺼짐 — 동의는 켜져 있으면 안 된다. ?>
        <?php foreach (CONSENT_ITEMS as $key => $label): ?>
          <label class="cookie-item">
            <input type="checkbox" name="item_<?= e($key) ?>" value="1">
            <span>선택 — <?= e($label) ?></span>
          </label>
        <?php endforeach; ?>

        <?php // ★ 지금 누르는 것이 '되돌릴 수 있는 선택'임을 그 자리에서 알린다.
              //   거둘 방법을 나중에 찾아야 한다면, 그건 있으나 마나다. ?>
        <p class="muted consent-hint">
          <a href="/cookies.php">쿠키 설정</a>에서 언제든 바꾸거나 <strong>철회</strong>할 수 있습니다.
        </p>
      </div>

      <?php // name="choice" 가 같고 value가 다르다 → 누른 버튼의 값만 서버로 간다. ?>
      <div class="cookie-notice-actions">
        <button type="submit" name="choice" value="all" class="btn-consent-all">모두 동의</button>
        <button type="submit" name="choice" value="selected">선택한 것만</button>
        <button type="submit" name="choice" value="none" class="btn-consent-none">거절</button>
      </div>
    </form>
  <?php endif; ?>
</body>
</html>
