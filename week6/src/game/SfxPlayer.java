package game;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/// <summary>
/// 사인파 기반 합성 효과음 재생기
/// 게임 이벤트마다 주파수/길이가 다른 8bit 레트로 효과음을 생성
/// 각 효과음은 데몬 스레드에서 재생 (비차단, 중첩 가능)
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
    /// </summary>
    private void playTone(int frequency, int durationMs) {
        Thread thread = new Thread(() -> {
            // 주의: AudioSystem.getSourceDataLine()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT);
                line.open(FORMAT);
                line.start();

                int totalSamples = SAMPLE_RATE * durationMs / 1000;
                byte[] buffer = new byte[totalSamples];

                for (int i = 0; i < totalSamples; i++) {
                    // 사인파 생성 (진폭 80, 8bit 범위 -128~127)
                    double angle = 2.0 * Math.PI * frequency * i / SAMPLE_RATE;
                    buffer[i] = (byte) (Math.sin(angle) * 80);
                }

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
    /// 치명타(상승), 사망(하강) 등에 사용
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
                    // 진행률에 따라 주파수 선형 보간
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
    /// </summary>
    public void playShotgunBlast() {
        Thread thread = new Thread(() -> {
            // 주의: AudioSystem.getSourceDataLine()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT);
                line.open(FORMAT);
                line.start();

                // 1단: 저음 폭발 (150Hz, 40ms)
                int samples1 = SAMPLE_RATE * 40 / 1000;
                byte[] burst1 = new byte[samples1];
                for (int i = 0; i < samples1; i++) {
                    double angle = 2.0 * Math.PI * 150 * i / SAMPLE_RATE;
                    // 진폭을 점차 감쇠시켜 폭발감
                    double decay = 1.0 - (double) i / samples1;
                    burst1[i] = (byte) (Math.sin(angle) * 100 * decay);
                }
                line.write(burst1, 0, burst1.length);

                // 짧은 간격 (10ms 무음)
                int silenceSamples = SAMPLE_RATE * 10 / 1000;
                byte[] silence = new byte[silenceSamples];
                line.write(silence, 0, silence.length);

                // 2단: 고음 파열 (600→200Hz 하강 스윕, 50ms)
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
