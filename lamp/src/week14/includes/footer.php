  </main>
  <!-- ============================================================
       footer.php — 모든 페이지 '맨 아래'에 공통으로 들어가는 조각.
       header.php에서 열어둔 <main> 을 여기서 닫고, body/html도 닫는다.
       ============================================================ -->
  <footer class="foot">
    <small>리뷰 커뮤니티 · week14 GET/POST 연습용 (데이터는 아직 더미)</small>
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
</body>
</html>
