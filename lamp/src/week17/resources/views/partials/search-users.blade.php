{{-- 유저 검색 결과 — 사진(없으면 첫 글자) + 닉네임 + 활동 --}}
<ul class="user-list">
  @foreach ($users as $u)
    <li>
      <a href="/users/{{ $u->username }}">
        @if ($u->avatar)
          <img class="user-avatar" src="{{ $u->avatar }}" alt="" loading="lazy">
        @else
          {{-- 사진이 없으면 닉네임 첫 글자를 원 안에 (프로필·랭킹과 같은 규칙) --}}
          <span class="user-avatar user-avatar-empty">{{ mb_substr($u->nickname, 0, 1) }}</span>
        @endif
        <span class="user-info">
          <strong>{!! $u->levelBadge !!} @highlight($u->nickname, $q)</strong>
          <span class="user-meta">&#64;@highlight($u->username, $q)
            · ✍️ 글 {{ $u->posts_count }} · 💬 댓글 {{ $u->comments_count }}</span>
        </span>
      </a>
    </li>
  @endforeach
</ul>
