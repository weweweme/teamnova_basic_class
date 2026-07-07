package com.example.week12.util;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/// <summary>
/// 아바타를 화면에 그리는 공용 헬퍼 — "사진 있으면 사진, 없으면 색깔 원 + 첫 글자"
///
/// ──── 왜 만드나 ────
/// 홈·랭킹·팔로우·리뷰·프로필 등 여러 화면이 아바타를 똑같은 방식으로 그린다.
/// 같은 코드를 곳곳에 두지 않도록 여기 한곳에 모아 재사용한다 (DRY).
///
/// 각 화면 레이아웃엔 "색깔 원(TextView)" 위에 "사진(ImageView, 기본 숨김)"을 겹쳐 두고,
/// 이 메서드가 사진 유무에 따라 둘 중 하나를 보여준다.
/// </summary>
public final class AvatarBinder {

    /// <summary>
    /// 인스턴스를 만들 이유가 없어 생성자를 막아둠
    /// </summary>
    private AvatarBinder() {
    }

    /// <summary>
    /// 아바타를 그린다
    ///
    /// circleText: 색깔 원 + 첫 글자를 표시할 TextView (원형 배경 drawable을 가진 뷰)
    /// photo: 사진을 얹을 ImageView (circleText 위에 겹쳐 둔, 기본 visibility=gone)
    /// nickname: 첫 글자를 딸 별명
    /// avatarColor: 색깔 원의 색 (ARGB)
    /// imageUrl: 프로필 사진 주소(https). 없으면 빈 문자열/null → 색깔 원만 보임
    /// loader: 원격 이미지 로더 (http 이미지를 백그라운드로 로딩)
    /// </summary>
    public static void bind(TextView circleText, ImageView photo, String nickname,
                            int avatarColor, String imageUrl, CoverImageLoader loader) {
        // 기본: 색깔 원 + 별명 첫 글자
        circleText.setText(initialOf(nickname));
        circleText.setBackgroundTintList(ColorStateList.valueOf(avatarColor));

        // 사진이 있으면 원형으로 얹어 보여줌 (없으면 사진 숨김 → 색깔 원이 보임)
        boolean hasPhoto = imageUrl != null && !imageUrl.isEmpty();
        if (hasPhoto) {
            // 원형 배경(bg_avatar_circle) 외곽선으로 사진을 동그랗게 자른다
            photo.setClipToOutline(true);
            loader.loadUri(photo, imageUrl);
            photo.setVisibility(View.VISIBLE);
        } else {
            photo.setVisibility(View.GONE);
        }
    }

    /// <summary>
    /// 별명 첫 글자(대문자)를 반환, 비어 있으면 물음표
    /// </summary>
    private static String initialOf(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return "?";
        }
        return String.valueOf(Character.toUpperCase(nickname.charAt(0)));
    }
}
