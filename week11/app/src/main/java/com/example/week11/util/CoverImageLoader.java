package com.example.week11.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/// <summary>
/// 표지 이미지를 백그라운드에서 디코딩해 ImageView에 반영하는 재사용 로더
/// App이 하나만 보유 → 모든 화면이 같은 캐시를 공유 (한 번 디코딩한 표지는 다음부터 즉시 표시)
///
/// 핵심 흐름:
///   ① 캐시에 있으면 → 바로 화면에 반영 (백그라운드도 딜레이도 없음)
///   ② 없으면 → 회색 박스(로딩) → 서브 스레드에서 디코딩 → Handler로 메인에 반영 + 캐시에 저장
///
/// Unity 비유: 텍스처를 한 번 로드하면 딕셔너리(캐시)에 담아두고 재사용하는 것과 같음
/// </summary>
public class CoverImageLoader {

    /// <summary>
    /// 로딩 중 잠깐 보여줄 회색 (표지와 확연히 구분되게)
    /// </summary>
    private static final int LOADING_COLOR = 0xFFBDBDBD;

    /// <summary>
    /// [관찰용] 디코딩을 일부러 느리게 해 로딩 과정을 눈에 보이게 하는 시간(ms)
    /// 실무의 "느린 디코딩 / 디스크 캐시 읽기"를 흉내 낸 것 — 확인이 끝나면 0으로 바꿔 끈다
    /// </summary>
    private static final long OBSERVE_DELAY_MS = 1500L;

    /// <summary>
    /// 디코딩 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// getMainLooper() = 메인(UI) 스레드 줄에 작업을 넣겠다는 뜻
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 메모리 캐시: 리소스 id → 이미 디코딩해 둔 Bitmap
    /// 읽기(loadCover)·쓰기(post 콜백) 모두 메인 스레드에서만 일어나므로 별도 동기화가 필요 없음
    /// </summary>
    private final Map<Integer, Bitmap> cache = new HashMap<>();

    /// <summary>
    /// 표지 이미지를 target ImageView에 표시
    /// resId가 0이면(해당 이미지가 없음) placeholderResId(기본 아이콘)을 대신 표시
    /// </summary>
    public void loadCover(ImageView target, int resId, int placeholderResId) {
        // 이미지가 없으면 디코딩할 것도 없으니 기본 아이콘만 바로 표시
        if (resId == 0) {
            target.setImageResource(placeholderResId);
            return;
        }

        // ① 캐시 히트: 이미 디코딩해 둔 게 있으면 백그라운드 없이 즉시 표시
        Bitmap cached = cache.get(resId);
        if (cached != null) {
            target.setImageBitmap(cached);
            return;
        }

        // ② 캐시 미스: 로딩(회색) 표시 + 이 ImageView가 지금 기다리는 이미지가 무엇인지 표시
        //    (그리드에서 셀이 재활용될 때 엉뚱한 표지가 덮이지 않게 하는 표식)
        target.setImageDrawable(new ColorDrawable(LOADING_COLOR));
        target.setTag(resId);

        // getResources()는 어느 스레드에서 불러도 안전 → 서브 스레드에 넘기려고 미리 확보
        Resources res = target.getResources();

        new Thread(() -> {                              // 서브(백그라운드) 스레드 시작
            // [관찰용] 디코딩을 일부러 느리게 (실무의 느린 디코딩/디스크 읽기를 흉내)
            if (OBSERVE_DELAY_MS > 0) {
                // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
                try {
                    Thread.sleep(OBSERVE_DELAY_MS);
                } catch (InterruptedException e) {
                    // 발생하지 않음 (컴파일러 요구사항)
                }
            }

            // ⏳ 무거운 일: JPG를 실제 Bitmap으로 디코딩 (여기서 시간이 걸림)
            Bitmap bmp = BitmapFactory.decodeResource(res, resId);

            // 결과 반영은 메인 줄로 (setImageBitmap·캐시 쓰기 모두 메인에서만)
            mainHandler.post(() -> {
                cache.put(resId, bmp);   // 다음부터는 캐시에서 즉시 꺼내 씀

                // 재활용 안전: 이 ImageView가 여전히 같은 이미지를 기다릴 때만 반영
                // (셀이 재활용돼 다른 게임을 기다리게 됐다면 이 결과는 버림)
                Object wanted = target.getTag();
                boolean stillWaiting = wanted != null && (int) wanted == resId;
                if (stillWaiting) {
                    target.setImageBitmap(bmp);
                }
            });
        }).start();                                     // 서브 스레드 실행
    }
}
