import javax.sound.sampled.*;
import java.io.File;

/**
 * ========================================================
 * [핵심] BGM 플레이어 클래스 (Singleton Pattern)
 * ========================================================
 * 배경음악(BGM) 재생을 전담하는 클래스입니다.
 * Java Sound API (Clip)를 사용하여 WAV 파일을 재생, 반복, 볼륨 조절합니다.
 * 싱글톤 패턴을 사용하여 어플리케이션 내에서 단 하나의 BGM 제어기만 존재하도록 합니다.
 */
public class BGMPlayer {
    private static BGMPlayer instance; // 싱글톤 인스턴스

    private Clip clip; // 오디오 클립 객체
    private FloatControl volumeControl; // 볼륨 컨트롤러
    private boolean initialized = false; // 초기화 여부 확인

    /**
     * private 생성자: 외부에서 직접 인스턴스 생성을 막음
     */
    private BGMPlayer() {
    }

    /**
     * 싱글톤 인스턴스 반환 메서드
     * 
     * @return BGMPlayer 유일한 인스턴스
     */
    public static BGMPlayer getInstance() {
        if (instance == null) {
            instance = new BGMPlayer();
        }
        return instance;
    }

    /**
     * BGM 파일 로드 및 재생 시작
     * 
     * @param filePath 재생할 WAV 파일의 경로 (절대 경로 권장)
     */
    public void loadAndPlay(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("BGM 파일을 찾을 수 없습니다: " + filePath);
                return;
            }

            // 오디오 스트림 열기
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // 볼륨 컨트롤 객체 획득 (MASTER_GAIN)
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // 현재 설정된 볼륨 값 적용
                setVolume(GameSettings.bgmVolume);
            }

            // 무한 반복 설정 및 재생 시작
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

            initialized = true;
            System.out.println("BGM 재생 시작: " + filePath);

        } catch (Exception e) {
            System.err.println("BGM 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 볼륨 설정 메서드
     * 
     * @param volume 0 ~ 100 사이의 정수 값
     */
    public void setVolume(int volume) {
        if (volumeControl != null) {
            // 사람이 듣는 소리 크기는 로그 스케일이므로 데시벨(dB) 변환이 필요하지만,
            // 여기서는 단순 선형 비율로 최소~최대 게인 값에 매핑합니다.
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();

            // value(0~100)를 min~max 사이의 값으로 변환
            float gain = min + (max - min) * (volume / 100.0f);
            volumeControl.setValue(gain);
        }
    }

    /**
     * 재생 정지
     */
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    // 일시 정지 (현재는 stop과 동일)
    public void pause() {
        stop();
    }

    // 재생 재개 (처음부터가 아닌 멈춘 곳부터 재생하려면 추가 구현 필요)
    public void resume() {
        if (clip != null) {
            clip.start();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}
