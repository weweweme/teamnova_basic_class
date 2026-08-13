  </main>
  <!-- ============================================================
       footer.php — 모든 페이지 '맨 아래'에 공통으로 들어가는 조각.
       header.php에서 열어둔 <main> 을 여기서 닫고, body/html도 닫는다.
       ============================================================ -->
  <footer class="foot">
    <small>🎬 리뷰 커뮤니티 · 영화·드라마 리뷰 커뮤니티 · TMDB 제공</small>
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
  <?php // ── 쿠키 안내 배너 (아직 확인 안 한 사람에게만) ────────────
        //   prefs.php를 부르지 않은 화면도 있으므로 함수가 있는지 먼저 확인한다. ?>
  <?php if (function_exists('has_seen_cookie_notice') && !has_seen_cookie_notice()): ?>
    <div class="cookie-notice" id="cookie-notice">
      <p>
        이 사이트는 <strong>로그인 유지·정렬 취향·최근 본 글</strong>을 기억하려고 쿠키를 사용합니다.
        <span class="muted">개인정보를 담지는 않아요.</span>
      </p>
      <button type="button" id="cookie-notice-ok">확인</button>
    </div>
    <script>
    // ★ 이 쿠키만은 **브라우저가 직접** 심는다 (다른 쿠키는 전부 서버가 setcookie로 심는다).
    //   가능한 이유: 취향 쿠키들은 httponly를 안 켜서 JS가 읽고 쓸 수 있기 때문.
    //   로그인 토큰은 정반대다 — httponly라 JS가 아예 못 만진다.
    document.getElementById('cookie-notice-ok').addEventListener('click', function () {
      // max-age = 초 단위 수명(90일). path=/ 로 사이트 전체에서 같은 값을 본다.
      // samesite=Lax = 다른 사이트가 시작한 요청엔 안 실린다.
      document.cookie = 'cookie_notice=1; path=/; max-age=7776000; samesite=Lax';
      document.getElementById('cookie-notice').remove();   // 서버를 안 거치고 즉시 사라진다
    });
    </script>
  <?php endif; ?>
</body>
</html>
