package com.example.week11.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    /// [관찰용] 로딩을 일부러 느리게 해 과정을 눈에 보이게 하는 지연 시간 범위(ms)
    /// 매번 MIN~MAX 사이 무작위 값을 써서, 인터넷 속도처럼 로딩 시간이 들쭉날쭉한 걸 흉내 낸다
    /// 확인이 끝나면 MAX를 0으로 바꿔 끈다
    /// </summary>
    private static final long OBSERVE_DELAY_MIN_MS = 300L;
    private static final long OBSERVE_DELAY_MAX_MS = 2000L;

    /// <summary>
    /// 동시에 돌릴 디코딩 스레드 개수 (스레드 풀 크기)
    /// 셀마다 new Thread를 만들면 스레드가 폭발하므로, 몇 개만 만들어 재사용한다
    /// 그리드 표지를 병렬로 몇 장씩 디코딩하기에 4개면 충분
    /// </summary>
    private static final int POOL_SIZE = 4;

    /// <summary>
    /// 디코딩 결과를 화면(메인 스레드)에 반영할 때 쓰는 Handler
    /// getMainLooper() = 메인(UI) 스레드 줄에 작업을 넣겠다는 뜻
    /// </summary>
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// 백그라운드 디코딩을 맡는 스레드 풀 (스레드 POOL_SIZE개를 만들어 재사용)
    /// 요청이 스레드 개수보다 많으면 대기했다가 빈 스레드가 생기면 처리 → 스레드 폭발 방지
    /// Unity 비유: 매번 워커를 새로 만드는 대신, 워커 몇 명을 두고 일감을 나눠 주는 것
    /// (앱이 하나만 보유하므로 종료 처리 없이 앱 생명주기 동안 그대로 사용)
    /// </summary>
    private final ExecutorService decodeExecutor = Executors.newFixedThreadPool(POOL_SIZE);

    /// <summary>
    /// 메모리 캐시: 리소스 id → 이미 디코딩해 둔 Bitmap
    /// 읽기(loadCover)·쓰기(post 콜백) 모두 메인 스레드에서만 일어나므로 별도 동기화가 필요 없음
    /// </summary>
    private final Map<Integer, Bitmap> cache = new HashMap<>();

    /// <summary>
    /// 표지 이미지를 target ImageView에 표시 (로딩 스피너 없이 — 그리드처럼 스피너가 필요 없는 곳용)
    /// resId가 0이면(해당 이미지가 없음) placeholderResId(기본 아이콘)을 대신 표시
    /// </summary>
    public void loadCover(ImageView target, int resId, int placeholderResId) {
        loadCover(target, null, resId, placeholderResId);
    }

    /// <summary>
    /// 표지 이미지를 target ImageView에 표시하면서, 로딩 중에는 spinner(ProgressBar 등)를 보여줌
    /// spinner가 null이면 스피너 없이 동작 (위의 3-인자 버전과 동일)
    /// - 캐시 히트/이미지 없음 → 스피너 숨김 + 즉시 표시
    /// - 캐시 미스 → 스피너 표시 + 백그라운드 디코딩 → 완료 시 스피너 숨김 + 표지 표시
    /// </summary>
    public void loadCover(ImageView target, View spinner, int resId, int placeholderResId) {
        // 이미지가 없으면 디코딩할 것도 없으니 기본 아이콘만 바로 표시 (스피너 숨김)
        if (resId == 0) {
            setSpinnerVisible(spinner, false);
            target.setImageResource(placeholderResId);
            return;
        }

        // ① 캐시 히트: 이미 디코딩해 둔 게 있으면 백그라운드 없이 즉시 표시 (스피너 불필요)
        Bitmap cached = cache.get(resId);
        if (cached != null) {
            setSpinnerVisible(spinner, false);
            target.setImageBitmap(cached);
            return;
        }

        // ② 캐시 미스: 스피너 표시 + 로딩(회색) + 이 ImageView가 지금 기다리는 이미지 표식
        //    (그리드에서 셀이 재활용될 때 엉뚱한 표지가 덮이지 않게 하는 표식)
        setSpinnerVisible(spinner, true);
        target.setImageDrawable(new ColorDrawable(LOADING_COLOR));
        target.setTag(resId);

        // getResources()는 어느 스레드에서 불러도 안전 → 서브 스레드에 넘기려고 미리 확보
        Resources res = target.getResources();

        // 스레드 풀에 디코딩 작업을 맡김 (빈 스레드가 처리, 없으면 대기)
        decodeExecutor.execute(() -> {                  // 백그라운드 스레드에서 실행
            // [관찰용] 로딩을 일부러 느리게 — 매번 랜덤 시간 (인터넷 속도가 매번 다른 걸 흉내)
            long delayMs = randomDelayMs();
            if (delayMs > 0) {
                // 주의: Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
                try {
                    Thread.sleep(delayMs);
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
                    setSpinnerVisible(spinner, false);   // 표지가 떴으니 스피너 숨김
                }
            });
        });                                             // 스레드 풀에 작업 제출
    }

    /// <summary>
    /// 스피너(ProgressBar 등) 표시/숨김 — spinner가 null이면 아무것도 안 함
    /// </summary>
    private void setSpinnerVisible(View spinner, boolean show) {
        if (spinner != null) {
            spinner.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /// <summary>
    /// [관찰용] MIN~MAX 사이의 무작위 지연 시간(ms)을 돌려줌 (로딩 속도가 매번 다른 걸 흉내)
    /// MAX가 0 이하면 0을 돌려줌 → 지연 없이 즉시 (관찰 모드 끄기)
    /// Math.random()은 0.0 이상 1.0 미만 → 범위 폭을 곱해 MIN을 더하면 MIN~MAX 사이 값
    /// </summary>
    private long randomDelayMs() {
        if (OBSERVE_DELAY_MAX_MS <= 0) {
            return 0;
        }
        long span = OBSERVE_DELAY_MAX_MS - OBSERVE_DELAY_MIN_MS;
        return OBSERVE_DELAY_MIN_MS + (long) (Math.random() * span);
    }
}
