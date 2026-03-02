package game;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/// <summary>
/// 사인파 기반 합성 효과음 재생기
/// 게임 이벤트마다 주파수/길이가 다른 8bit 레트로 효과음을 생성
/// 각 효과음은 데몬 스레드에서 재생 (비차단, 중첩 가능)
///
/// 핵심 아이디어:
///   - MP3/WAV 파일 없이, 수학 공식(사인파)으로 소리를 실시간 합성
///   - 주파수가 높으면 고음, 낮으면 저음 → 이벤트별 주파수/길이 조합으로 음색 구분
///   - 주파수를 시간에 따라 변화시키면(스윕) 상승감/하강감 연출 가능
///
/// 소리 생성 원리:
///   1) 원하는 길이만큼 byte[] 배열을 생성 (8kHz × 길이 = 샘플 수)
///   2) 각 샘플을 Math.sin(2π × 주파수 × i / 샘플레이트) × 진폭으로 계산
///   3) Java 사운드 API(SourceDataLine)에 배열을 넘겨 스피커로 출력
///   4) 데몬 스레드에서 재생하여 게임 루프를 멈추지 않음
/// </summary>
public class SfxPlayer {

    /// <summary>
    /// 샘플링 레이트 (초당 샘플 수)
    /// </summary>
    private static final int SAMPLE_RATE = 8000;

    /// <summary>
    /// 오디오 포맷 (8kHz, 8bit, 모노)
    /// </summary>
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);

    /// <summary>
    /// 고정 주파수 사인파를 데몬 스레드에서 재생
    /// 발사음, 명중음, 폭발음 등 단순한 효과음에 사용
    /// </summary>
    private void playTone(int frequency, int durationMs) {
        Thread thread = new Thread(() -> {
            // 주의: AudioSystem.getSourceDataLine()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                // 오디오 출력 라인을 열고 재생 시작
                SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT);
                line.open(FORMAT);
                line.start();

                // 재생할 총 샘플 수 = 초당 샘플 수 × 길이(초)
                // 예) 8000Hz × 0.03초 = 240 샘플
                int totalSamples = SAMPLE_RATE * durationMs / 1000;
                byte[] buffer = new byte[totalSamples];

                for (int i = 0; i < totalSamples; i++) {
                    // 사인파 공식: sin(2π × 주파수 × 시간)
                    // 진폭 80 (8bit 범위 -128~127 중 약 63% 사용)
                    double angle = 2.0 * Math.PI * frequency * i / SAMPLE_RATE;
                    buffer[i] = (byte) (Math.sin(angle) * 80);
                }

                // 배열 전체를 오디오 장치에 전송하고, 재생 완료까지 대기 후 닫음
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                // 오디오 장치 없거나 열 수 없을 때 무시 (게임 진행에 영향 없음)
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /// <summary>
    /// 시작→끝 주파수로 스윕하는 사인파를 데몬 스레드에서 재생
    /// 주파수 상승(저→고) = 긴장감/강해지는 느낌 (치명타, 돌진)
    /// 주파수 하강(고→저) = 무너지는/약해지는 느낌 (사망, 탄막)
    /// </summary>
    private void playSweep(int startFreq, int endFreq, int durationMs) {
        Thread thread = new Thread(() -> {
            // 주의: AudioSystem.getSourceDataLine()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT);
                line.open(FORMAT);
                line.start();

                int totalSamples = SAMPLE_RATE * durationMs / 1000;
                byte[] buffer = new byte[totalSamples];

                for (int i = 0; i < totalSamples; i++) {
                    // 진행률(0→1)에 따라 주파수를 시작값에서 끝값으로 선형 보간
                    // 예) 400→100Hz 하강: progress 0.5 → freq 250Hz
                    double progress = (double) i / totalSamples;
                    double freq = startFreq + (endFreq - startFreq) * progress;
                    double angle = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
                    buffer[i] = (byte) (Math.sin(angle) * 80);
                }

                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                // 오디오 장치 없거나 열 수 없을 때 무시
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /// <summary>
    /// 발사음 (800Hz, 30ms)
    /// </summary>
    public void playShoot() {
        playTone(800, 30);
    }

    /// <summary>
    /// 샷건 발사음 — 저음 폭발 + 고음 파열 2연발 (팡! 느낌)
    /// 2개의 음을 순서대로 이어붙여 복합 효과음을 만듦
    /// </summary>
    public void playShotgunBlast() {
        Thread thread = new Thread(() -> {
            // 주의: AudioSystem.getSourceDataLine()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT);
                line.open(FORMAT);
                line.start();

                // 1단: 저음 폭발 (150Hz, 40ms)
                // decay(1→0)를 곱해 진폭이 점차 줄어듦 → "쿵" 하고 빠르게 사라지는 느낌
                int samples1 = SAMPLE_RATE * 40 / 1000;
                byte[] burst1 = new byte[samples1];
                for (int i = 0; i < samples1; i++) {
                    double angle = 2.0 * Math.PI * 150 * i / SAMPLE_RATE;
                    double decay = 1.0 - (double) i / samples1;
                    burst1[i] = (byte) (Math.sin(angle) * 100 * decay);
                }
                line.write(burst1, 0, burst1.length);

                // 짧은 간격 (10ms 무음) — 1단과 2단 사이의 분리감
                int silenceSamples = SAMPLE_RATE * 10 / 1000;
                byte[] silence = new byte[silenceSamples];
                line.write(silence, 0, silence.length);

                // 2단: 고음 파열 (600→200Hz 하강 스윕, 50ms)
                // 높은 주파수에서 시작해 빠르게 내려가며 감쇠 → "탕!" 잔향
                int samples2 = SAMPLE_RATE * 50 / 1000;
                byte[] burst2 = new byte[samples2];
                for (int i = 0; i < samples2; i++) {
                    double progress = (double) i / samples2;
                    double freq = 600 + (200 - 600) * progress;
                    double angle = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
                    double decay = 1.0 - progress;
                    burst2[i] = (byte) (Math.sin(angle) * 90 * decay);
                }
                line.write(burst2, 0, burst2.length);

                line.drain();
                line.close();
            } catch (Exception e) {
                // 오디오 장치 없거나 열 수 없을 때 무시 (게임 진행에 영향 없음)
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /// <summary>
    /// 명중음 (300Hz, 50ms)
    /// </summary>
    public void playHit() {
        playTone(300, 50);
    }

    /// <summary>
    /// 치명타음 (600→1200Hz 상승 스윕, 80ms)
    /// </summary>
    public void playCrit() {
        playSweep(600, 1200, 80);
    }

    /// <summary>
    /// 폭발음 (80Hz 저음, 200ms)
    /// </summary>
    public void playExplosion() {
        playTone(80, 200);
    }

    /// <summary>
    /// 적 사망음 (400→100Hz 하강 스윕, 120ms)
    /// </summary>
    public void playDeath() {
        playSweep(400, 100, 120);
    }

    /// <summary>
    /// 속사 탄막 효과음 (1000→800Hz 하강 스윕, 150ms)
    /// </summary>
    public void playBarrage() {
        playSweep(1000, 800, 150);
    }

    /// <summary>
    /// 정밀 저격 효과음 (800→1600Hz 상승 스윕, 100ms)
    /// </summary>
    public void playPrecisionShot() {
        playSweep(800, 1600, 100);
    }

    /// <summary>
    /// 충격파 효과음 (100Hz 저음, 300ms)
    /// </summary>
    public void playShockwave() {
        playTone(100, 300);
    }

    /// <summary>
    /// 돌진 질주 효과음 (500→1000Hz 상승 스윕, 80ms)
    /// </summary>
    public void playChargeRush() {
        playSweep(500, 1000, 80);
    }

    /// <summary>
    /// 방패 충돌 효과음 (200Hz 저음, 150ms)
    /// </summary>
    public void playShieldBash() {
        playTone(200, 150);
    }

    /// <summary>
    /// 치유 포자 효과음 (400→600Hz 상승 스윕, 120ms)
    /// </summary>
    public void playHealingSpore() {
        playSweep(400, 600, 120);
    }

    /// <summary>
    /// 밤 시작 경보음 (600Hz 비프 2회)
    /// </summary>
    public void playWaveStart() {
        Thread thread = new Thread(() -> {
            // 주의: AudioSystem.getSourceDataLine()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT);
                line.open(FORMAT);
                line.start();

                // 첫 번째 비프 (600Hz, 100ms)
                int samplesPerBeep = SAMPLE_RATE * 100 / 1000;
                byte[] beep = new byte[samplesPerBeep];
                for (int i = 0; i < samplesPerBeep; i++) {
                    double angle = 2.0 * Math.PI * 600 * i / SAMPLE_RATE;
                    beep[i] = (byte) (Math.sin(angle) * 80);
                }
                line.write(beep, 0, beep.length);

                // 간격 (50ms 무음)
                int silenceSamples = SAMPLE_RATE * 50 / 1000;
                byte[] silence = new byte[silenceSamples];
                line.write(silence, 0, silence.length);

                // 두 번째 비프 (600Hz, 100ms)
                line.write(beep, 0, beep.length);

                line.drain();
                line.close();
            } catch (Exception e) {
                // 오디오 장치 없거나 열 수 없을 때 무시
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
