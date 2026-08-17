// ============================================================
// device-key.js — 이 브라우저의 '꺼낼 수 없는 도장'을 만들고, 서버가 던진 숫자에 찍어준다
//
//   [★ 왜 JS여야만 하나]
//     도장은 **브라우저 안에서만** 만들어지고 브라우저 안에서만 쓰인다.
//     서버 폼으로는 대신할 수 없다 — 그게 이 방식의 핵심이자, 우리가 치른 대가다.
//     (쿠키 동의는 'JS 0줄'로 만들었는데, 여기서는 그럴 수가 없다)
//
//   [★★ 핵심은 딱 한 글자다 — extractable: false]
//     이 값이 false면 **우리가 짠 JS로도 개인키를 읽을 수 없다.**
//     IndexedDB에 넣어도 저장되는 건 값이 아니라 '손잡이'다.
//       · 쓸 수는 있다   → sign() 은 된다
//       · 꺼낼 수는 없다 → exportKey() 가 거부된다
//     → 사이트에 XSS가 나도 **훔쳐갈 실체가 없다.** 쿠키와 결정적으로 다른 점이다.
//
//   [흐름]
//     ① 도장 만들기(최초 1회) → 공개된 짝만 서버에 등록
//     ② 서버가 아무 숫자나 던짐 → 도장으로 찍어서 보냄 → 세션 연장
//     ③ 만료 전에 미리 ②를 반복 (화면이 끊기지 않게)
// ============================================================

(() => {
  'use strict';

  const DB_NAME = 'device-key';
  const STORE   = 'keys';
  const RECORD  = 'current';

  // ── IndexedDB: 도장을 넣어두는 서랍 ────────────────────────
  //   ★ localStorage가 아니라 IndexedDB인 이유: localStorage는 **문자열만** 담는다.
  //     도장은 문자열로 꺼낼 수 없는 물건이라(그게 요점이다) 애초에 담기지 않는다.
  //     IndexedDB는 CryptoKey 객체를 그대로 담아준다 — 값이 아니라 손잡이째로.
  function rawOpen() {
    return new Promise((resolve, reject) => {
      const req = indexedDB.open(DB_NAME, 1);
      req.onupgradeneeded = () => req.result.createObjectStore(STORE);
      req.onsuccess = () => resolve(req.result);
      req.onerror   = () => reject(req.error);
    });
  }

  // ★ '같은 이름 · 같은 버전인데 저장소가 없는' DB가 있으면 스스로 고친다.
  //   [어쩌다 그런 게 생기나]
  //     누가 `indexedDB.open('device-key')` 를 버전 없이 한 번 부르면
  //     **저장소가 하나도 없는 버전 1 DB**가 만들어진다.
  //     그 뒤에는 우리가 `open(이름, 1)` 로 열어도 이미 버전 1이라
  //     onupgradeneeded가 안 뜨고 → 저장소는 영영 안 생긴다.
  //   ★ 사용자가 직접 지우기 전에는 안 풀리는 잠금이라, 여기서 한 번 되돌려준다.
  //     (한 번 지우고 다시 만들 뿐이므로, 잃는 것은 어차피 못 쓰던 빈 DB다)
  async function openDb() {
    let db = await rawOpen();
    if (db.objectStoreNames.contains(STORE)) return db;

    db.close();
    await new Promise((resolve) => {
      const req = indexedDB.deleteDatabase(DB_NAME);
      req.onsuccess = req.onerror = req.onblocked = () => resolve();
    });
    return rawOpen();
  }

  function dbGet(db, key) {
    return new Promise((resolve, reject) => {
      const req = db.transaction(STORE, 'readonly').objectStore(STORE).get(key);
      req.onsuccess = () => resolve(req.result);
      req.onerror   = () => reject(req.error);
    });
  }

  function dbPut(db, key, value) {
    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE, 'readwrite');
      tx.objectStore(STORE).put(value, key);
      tx.oncomplete = () => resolve();
      tx.onerror    = () => reject(tx.error);
    });
  }

  // ── 도장 만들기 / 꺼내기 ───────────────────────────────────
  async function loadKeyPair() {
    const db = await openDb();
    return (await dbGet(db, RECORD)) || null;
  }

  async function createKeyPair() {
    const pair = await crypto.subtle.generateKey(
      { name: 'ECDSA', namedCurve: 'P-256' },
      false,                       // ★ extractable = false — 개인키는 꺼낼 수 없다
      ['sign', 'verify']
    );
    const db = await openDb();
    await dbPut(db, RECORD, pair);
    return pair;
  }

  const toBase64 = (buffer) =>
    btoa(String.fromCharCode(...new Uint8Array(buffer)));

  // 공개된 짝만 밖으로 꺼낸다. (개인키였다면 이 줄에서 예외가 난다 — 그게 정상이다)
  async function exportPublicKey(pair) {
    return toBase64(await crypto.subtle.exportKey('spki', pair.publicKey));
  }

  // 서버가 던진 숫자에 도장을 찍는다.
  //   ★ 결과는 r·s를 이어붙인 64바이트다. PHP 쪽에서 DER로 포장을 바꿔 대조한다.
  async function sign(pair, challenge) {
    const data = new TextEncoder().encode(challenge);
    const raw  = await crypto.subtle.sign({ name: 'ECDSA', hash: 'SHA-256' }, pair.privateKey, data);
    return toBase64(raw);
  }

  // ── 서버와 주고받기 ────────────────────────────────────────
  const csrfToken = () =>
    document.querySelector('meta[name="csrf-token"]')?.content || '';

  async function fetchChallenge() {
    const res = await fetch('/session/challenge.php', { credentials: 'same-origin' });
    if (!res.ok) throw new Error('challenge ' + res.status);
    return (await res.json()).challenge;
  }

  async function post(url, fields) {
    const body = new URLSearchParams({ ...fields, _token: csrfToken() });
    const res  = await fetch(url, { method: 'POST', body, credentials: 'same-origin' });
    if (!res.ok) throw new Error(url + ' ' + res.status);
    return res.json();
  }

  // ── 바깥에서 쓰는 두 가지 ──────────────────────────────────

  // 등록: 도장이 없으면 만들고, 공개된 짝 + 첫 자국을 함께 보낸다.
  //   ★ 공개키만 보내면 안 된다 — **그 도장을 실제로 갖고 있다는 것까지** 같이 보여야
  //     남의 공개키를 대신 등록해 놓는 장난을 막을 수 있다.
  async function enroll() {
    const pair      = (await loadKeyPair()) || (await createKeyPair());
    const challenge = await fetchChallenge();

    return post('/session/enroll.php', {
      public_key: await exportPublicKey(pair),
      signature:  await sign(pair, challenge),
    });
  }

  // 연장: 이미 등록된 도장으로 자국만 찍어 보낸다.
  async function refresh() {
    const pair = await loadKeyPair();
    if (!pair) throw new Error('no key');

    const challenge = await fetchChallenge();
    return post('/session/refresh.php', { signature: await sign(pair, challenge) });
  }

  // ── 만료 전에 미리 찍어두기 ────────────────────────────────
  //   [왜 미리 하나]
  //     만료된 **뒤에** 하면 사용자가 '기기 확인 중' 화면을 보게 된다.
  //     남은 시간을 서버가 알려주므로, 1분 전에 조용히 찍어두면 화면이 끊기지 않는다.
  //   ★ DBSC도 같은 방식이다 — 브라우저가 쿠키 만료를 보고 알아서 갱신한다.
  function scheduleRefresh() {
    const meta = document.querySelector('meta[name="key-proof-left"]');
    if (!meta) return;                       // 로그인 안 했거나 도장이 필요 없는 화면

    // ★ 여유 시간도 서버가 알려준다. 여기서 60초로 고정하면
    //   수명이 60초일 때 **매 요청 도장을 찍게 되어** "평소엔 쿠키로"가 무너진다.
    const marginMeta = document.querySelector('meta[name="key-proof-margin"]');
    const margin = parseInt(marginMeta?.content, 10) || 20;

    const left  = parseInt(meta.content, 10) || 0;
    const delay = Math.max(0, left - margin) * 1000;

    setTimeout(() => {
      refresh().catch(() => {
        // 실패하면 확인 화면으로 보낸다. 거기서 다시 등록하거나 로그아웃된다.
        location.href = '/session/verify.php?back=' + encodeURIComponent(location.pathname + location.search);
      });
    }, delay);
  }

  window.deviceKey = { enroll, refresh };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', scheduleRefresh);
  } else {
    scheduleRefresh();
  }
})();
