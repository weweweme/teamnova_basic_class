<?php
// ============================================================
// level.php — 유저 등급(배지) 계산
//   작성 글 수를 5단계 등급으로 바꾼다. (스키마 변경 없이 글 수로 계산)
//   ★ 표시 전용 개념이라 여기 한 곳에 규칙을 모아둔다 → 등급표를 바꾸려면 여기만.
// ============================================================

require_once __DIR__ . '/util.php';   // level_badge_html의 e() 이스케이프용

// 등급 경계(글 수)와 이름·배지. 위에서부터 높은 등급 순으로 검사한다.
//   [최소 글 수, 배지, 이름]  (매직값 대신 표로 모아 관리)
const USER_LEVELS = [
    [10, '👑', '시네필'],
    [6,  '🎖️', '평론가'],
    [3,  '✍️', '리뷰어'],
    [1,  '🎬', '관람객'],
    [0,  '🌱', '새싹'],
];

// 글 수 → 등급 한 건. 반환: ['badge'=>'🎬', 'name'=>'관람객']
//   (매번 같은 표를 훑어 새 배열을 만들어 주므로 이름을 build… 대신 단순 user_level로 둔다)
function user_level(int $postCount): array {
    foreach (USER_LEVELS as [$min, $badge, $name]) {
        if ($postCount >= $min) {
            return ['badge' => $badge, 'name' => $name];
        }
    }
    // 표에 0 경계가 있어 여기까지 오지 않지만, 안전용 기본값
    return ['badge' => '🌱', 'name' => '새싹'];
}

// 작성자 이름 옆에 붙일 배지 HTML 한 조각을 만들어 준다.
//   예) <span class="lvl-badge" title="관람객">🎬</span>  (마우스 올리면 등급 이름)
//   배지·이름 모두 우리 상수라 위험 없지만, 습관적으로 title은 e()로 이스케이프.
function level_badge_html(int $postCount): string {
    $lv = user_level($postCount);
    return '<span class="lvl-badge" title="' . e($lv['name']) . '">' . $lv['badge'] . '</span>';
}
