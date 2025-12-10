import javax.sound.sampled.*;
import java.io.File;

/**
 * ========================================================
 * BGMPlayer 클래스 - 배경음악(BGM) 재생기
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임의 배경음악(BGM)을 관리하고 재생합니다.
 * - 싱글톤(Singleton) 패턴을 사용하여 하나의 인스턴스만 존재합니다.
 * - WAV 파일만 지원합니다 (Java 기본 라이브러리 사용).
 * 
 * [싱글톤 패턴이란?]
 * - 프로그램 전체에서 오직 하나의 인스턴스만 존재하도록 보장하는 패턴
 * - BGM은 하나만 재생되어야 하므로 싱글톤이 적합합니다
 * - getInstance() 메서드를 통해서만 인스턴스에 접근 가능
 * 
 * [사용 방법]
 * - BGMPlayer.getInstance().loadAndPlay("경로"); // 음악 재생
 * - BGMPlayer.getInstance().setVolume(50); // 볼륨 조절 (0~100)
 * - BGMPlayer.getInstance().stop(); // 정지
 * 
 * [지원 형식]
 * - WAV 파일만 지원 (MP3는 지원하지 않음)
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class BGMPlayer {

    // ============================================
    // 멤버 변수 (필드)
    // ============================================

    /**
     * instance - 싱글톤 인스턴스
     * static으로 선언하여 클래스 레벨에서 관리
     * 최초 getInstance() 호출 시 생성됨
     */
    private static BGMPlayer instance;

    /**
     * clip - 오디오 재생을 담당하는 객체
     * javax.sound.sampled.Clip 인터페이스 사용
     * 음악 파일을 메모리에 로드하여 재생
     */
    private Clip clip;

    /**
     * volumeControl - 볼륨 제어 객체
     * FloatControl.Type.MASTER_GAIN을 사용하여 볼륨 조절
     * 데시벨(dB) 단위로 볼륨을 설정
     */
    private FloatControl volumeControl;

    /**
     * initialized - 초기화 완료 여부
     * 음악 파일이 성공적으로 로드되었는지 확인하는 플래그
     */
    private boolean initialized = false;

    /**
     * ============================================
     * 생성자 (private)
     * ============================================
     * 
     * [설명]
     * - private으로 선언하여 외부에서 직접 인스턴스 생성 불가
     * - 오직 getInstance()를 통해서만 인스턴스에 접근 가능
     * - 이것이 싱글톤 패턴의 핵심
     */
    private BGMPlayer() {
        // 빈 생성자 - 초기화는 loadAndPlay()에서 수행
    }

    /**
     * ============================================
     * getInstance() - 싱글톤 인스턴스 반환
     * ============================================
     * 
     * [설명]
     * - BGMPlayer의 유일한 인스턴스를 반환합니다.
     * - 인스턴스가 없으면 새로 생성하고, 있으면 기존 것을 반환합니다.
     * - 이를 "지연 초기화(Lazy Initialization)"라고 합니다.
     * 
     * @return BGMPlayer의 싱글톤 인스턴스
     */
    public static BGMPlayer getInstance() {
        // 인스턴스가 없으면 새로 생성
        if (instance == null) {
            instance = new BGMPlayer();
        }
        return instance;
    }

    /**
     * ============================================
     * loadAndPlay() - 음악 파일 로드 및 재생
     * ============================================
     * 
     * [설명]
     * - 지정된 경로의 WAV 파일을 로드하여 재생합니다.
     * - 음악은 무한 반복(LOOP_CONTINUOUSLY) 재생됩니다.
     * - GameSettings의 볼륨 설정이 자동으로 적용됩니다.
     * 
     * [처리 순서]
     * 1. 파일 존재 여부 확인
     * 2. AudioInputStream으로 파일 읽기
     * 3. Clip 객체 생성 및 열기
     * 4. 볼륨 컨트롤 설정
     * 5. 무한 반복 재생 시작
     * 
     * @param filePath 재생할 WAV 파일의 절대 경로
     */
    public void loadAndPlay(String filePath) {
        try {
            // 파일 객체 생성 및 존재 확인
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("BGM 파일을 찾을 수 없습니다: " + filePath);
                return; // 파일이 없으면 종료
            }

            // 오디오 입력 스트림 생성 (파일 읽기)
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);

            // Clip 객체 생성 및 열기
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // 볼륨 컨트롤 설정 (시스템이 지원하는 경우)
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(GameSettings.bgmVolume); // 초기 볼륨 적용
            }

            // 무한 반복 재생 설정 및 시작
            clip.loop(Clip.LOOP_CONTINUOUSLY); // 무한 루프
            clip.start(); // 재생 시작
            initialized = true; // 초기화 완료 표시

            System.out.println("BGM 재생 시작: " + filePath);

        } catch (Exception e) {
            // 오류 발생 시 에러 메시지 출력
            System.err.println("BGM 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ============================================
     * setVolume() - 볼륨 설정
     * ============================================
     * 
     * [설명]
     * - 0~100 범위의 정수를 받아 볼륨을 설정합니다.
     * - 내부적으로 데시벨(dB) 단위로 변환합니다.
     * 
     * [볼륨 변환 공식]
     * - min: 컨트롤의 최소값 (약 -80dB, 음소거)
     * - max: 컨트롤의 최대값 (약 6dB, 최대 볼륨)
     * - gain = min + (max - min) * (volume / 100)
     * 
     * @param volume 설정할 볼륨 (0~100)
     */
    public void setVolume(int volume) {
        if (volumeControl != null) {
            // 컨트롤의 최소/최대값 가져오기
            float min = volumeControl.getMinimum(); // 약 -80dB
            float max = volumeControl.getMaximum(); // 약 6dB

            // 0~100을 min~max 범위로 변환
            float gain = min + (max - min) * (volume / 100.0f);

            // 볼륨 적용
            volumeControl.setValue(gain);
        }
    }

    /**
     * ============================================
     * stop() - 재생 정지
     * ============================================
     * 
     * [설명]
     * - 현재 재생 중인 음악을 정지합니다.
     * - clip이 null이 아니고 재생 중일 때만 동작합니다.
     */
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    /**
     * ============================================
     * pause() - 일시 정지
     * ============================================
     * 
     * [설명]
     * - 현재 재생 중인 음악을 일시 정지합니다.
     * - 내부적으로 stop()을 호출합니다.
     * - resume()으로 다시 재생할 수 있습니다.
     */
    public void pause() {
        stop(); // stop()과 동일하게 동작
    }

    /**
     * ============================================
     * resume() - 재생 재개
     * ============================================
     * 
     * [설명]
     * - 일시 정지된 음악을 다시 재생합니다.
     * - pause() 이후에 호출하면 이어서 재생됩니다.
     */
    public void resume() {
        if (clip != null) {
            clip.start();
        }
    }

    /**
     * ============================================
     * isInitialized() - 초기화 상태 확인
     * ============================================
     * 
     * [설명]
     * - BGM이 성공적으로 로드되었는지 확인합니다.
     * - true: 재생 준비 완료
     * - false: 아직 로드되지 않음 또는 로드 실패
     * 
     * @return 초기화 완료 여부
     */
    public boolean isInitialized() {
        return initialized;
    }
}
