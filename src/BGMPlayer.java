import javax.sound.sampled.*;
import java.io.File;

// ========================================================
// [핵심] BGM 플레이어 클래스 (Java 기본 라이브러리 - WAV 지원)
// ========================================================
public class BGMPlayer {
    private static BGMPlayer instance;
    private Clip clip;
    private FloatControl volumeControl;
    private boolean initialized = false;

    private BGMPlayer() {
    }

    public static BGMPlayer getInstance() {
        if (instance == null) {
            instance = new BGMPlayer();
        }
        return instance;
    }

    public void loadAndPlay(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("BGM 파일을 찾을 수 없습니다: " + filePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // 볼륨 컨트롤 설정
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(GameSettings.bgmVolume);
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY); // 무한 반복
            clip.start();
            initialized = true;
            System.out.println("BGM 재생 시작: " + filePath);

        } catch (Exception e) {
            System.err.println("BGM 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setVolume(int volume) {
        if (volumeControl != null) {
            // 0-100을 데시벨로 변환
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float gain = min + (max - min) * (volume / 100.0f);
            volumeControl.setValue(gain);
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void pause() {
        stop();
    }

    public void resume() {
        if (clip != null) {
            clip.start();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}
