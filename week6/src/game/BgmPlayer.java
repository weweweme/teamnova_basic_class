package game;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

/// <summary>
/// MP3 BGM을 백그라운드 스레드에서 루프 재생하는 클래스
/// JLayer 라이브러리 사용
/// </summary>
public class BgmPlayer extends Thread {

    /// <summary>
    /// MP3 파일 경로
    /// </summary>
    private final String filePath;

    /// <summary>
    /// 재생 중 여부 (false가 되면 루프 종료)
    /// </summary>
    private volatile boolean playing;

    /// <summary>
    /// 현재 재생 중인 JLayer Player (중지 시 close() 호출용)
    /// </summary>
    private Player player;

    /// <summary>
    /// 지정한 MP3 파일로 BGM 플레이어 생성
    /// 데몬 스레드로 설정하여 메인 종료 시 자동 종료 보장
    /// </summary>
    public BgmPlayer(String filePath) {
        this.filePath = filePath;
        this.playing = true;
        setDaemon(true);
    }

    /// <summary>
    /// 루프 재생: 곡이 끝나면 처음부터 다시 재생
    /// stopPlaying() 호출 시 루프 탈출
    /// </summary>
    @Override
    public void run() {
        while (playing) {
            // 주의: FileInputStream/Player 생성은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                FileInputStream fis = new FileInputStream(filePath);
                BufferedInputStream bis = new BufferedInputStream(fis);
                player = new Player(bis);
                player.play();
                player.close();
            } catch (Exception e) {
                // 파일 없음/디코딩 오류 시 재생 포기
                playing = false;
            }
        }
    }

    /// <summary>
    /// 재생 중지 및 스레드 종료
    /// </summary>
    public void stopPlaying() {
        playing = false;
        if (player != null) {
            player.close();
        }
    }
}
