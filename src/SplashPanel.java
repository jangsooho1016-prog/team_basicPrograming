import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * ========================================================
 * SplashPanel 클래스 - 스플래시 화면 (게임 시작 화면)
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임 실행 시 가장 먼저 표시되는 화면입니다.
 * - 게임 로고/이미지를 페이드 인 효과와 함께 보여줍니다.
 * - 효과음(splash2.wav)을 재생합니다.
 * - 3초 후 자동으로 메뉴 화면으로 전환됩니다.
 * - 클릭하거나 아무 키를 누르면 바로 메뉴로 이동합니다.
 * 
 * [화면 흐름]
 * SplashPanel(스플래시) → MenuPanel(메뉴)
 * 
 * [사용된 기술]
 * - Timer: 애니메이션 및 자동 화면 전환
 * - AlphaComposite: 투명도를 이용한 페이드 인 효과
 * - javax.sound.sampled: 효과음 재생
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class SplashPanel extends JPanel {

    // ============================================
    // 상수 정의
    // ============================================

    /** 패널 너비 (픽셀) */
    private static final int PANEL_WIDTH = 800;

    /** 패널 높이 (픽셀) */
    private static final int PANEL_HEIGHT = 600;

    // ============================================
    // 멤버 변수 (필드)
    // ============================================

    /** 메인 프레임 참조 (화면 전환에 사용) */
    private CrazyArcade_UI mainFrame;

    /** 자동 화면 전환 타이머 (3초 후 메뉴로 이동) */
    private Timer transitionTimer;

    /** 현재 투명도 (0.0: 완전 투명 ~ 1.0: 완전 불투명) */
    private float alpha = 0f;

    /** 페이드 인 애니메이션 타이머 */
    private Timer fadeTimer;

    /** 스플래시 이미지 */
    private Image splashImage;

    /**
     * ============================================
     * 생성자 - 스플래시 패널 초기화
     * ============================================
     * 
     * [수행 작업]
     * 1. 패널 기본 설정 (크기, 레이아웃, 배경색)
     * 2. 스플래시 이미지 로드
     * 3. 마우스 클릭 이벤트 리스너 등록
     * 4. 키보드 이벤트 리스너 등록
     * 
     * @param mainFrame 메인 프레임 참조 (화면 전환에 사용)
     */
    public SplashPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;

        // ----- 패널 기본 설정 -----
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null); // 절대 좌표 레이아웃 사용
        setBackground(Color.BLACK); // 배경색: 검정

        // ----- 스플래시 이미지 로드 (파일 시스템 경로 사용) -----
        // 프로젝트폴더/res/start.png 파일을 로드합니다
        String imagePath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "start.png";
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            splashImage = new ImageIcon(imagePath).getImage();
            System.out.println("스플래시 이미지 로드 성공: " + imagePath);
        } else {
            System.err.println("스플래시 이미지를 찾을 수 없습니다: " + imagePath);
            splashImage = null; // 이미지 없으면 텍스트 로고 표시
        }

        // ----- 마우스 클릭 이벤트 -----
        // 화면 아무 곳이나 클릭하면 바로 메뉴로 이동
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goToMenu(); // 메뉴 화면으로 이동
            }
        });

        // ----- 키보드 이벤트 -----
        // 아무 키나 누르면 바로 메뉴로 이동
        setFocusable(true); // 키보드 입력을 받으려면 포커스 가능해야 함
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                goToMenu(); // 메뉴 화면으로 이동
            }
        });
    }

    /**
     * ============================================
     * addNotify() - 패널이 화면에 표시될 때 호출되는 메서드
     * ============================================
     * 
     * [설명]
     * - JPanel이 Container에 추가되어 화면에 표시될 때 자동 호출됩니다.
     * - 스플래시 효과(애니메이션, 효과음)를 시작합니다.
     * - 키보드 입력을 받기 위해 포커스를 요청합니다.
     * 
     * [호출 시점]
     * - mainContainer.add(new SplashPanel(this), PANEL_SPLASH) 이후
     * - showPanel(PANEL_SPLASH) 호출 시
     */
    @Override
    public void addNotify() {
        super.addNotify(); // 부모 클래스의 addNotify() 호출 (필수!)
        requestFocusInWindow(); // 키보드 포커스 요청
        startSplash(); // 스플래시 효과 시작
    }

    /**
     * ============================================
     * startSplash() - 스플래시 효과 시작
     * ============================================
     * 
     * [설명]
     * - 효과음을 재생합니다.
     * - 페이드 인 애니메이션을 시작합니다.
     * - 3초 후 자동으로 메뉴 화면으로 전환하는 타이머를 시작합니다.
     */
    private void startSplash() {
        // ----- 효과음 재생 -----
        playSplashSound();

        // ----- 페이드 인 애니메이션 -----
        // 30ms마다 투명도를 0.05씩 증가시켜 서서히 나타나는 효과
        alpha = 0f; // 초기 투명도: 완전 투명
        fadeTimer = new Timer(30, e -> {
            alpha += 0.05f; // 투명도 증가
            if (alpha >= 1.0f) {
                alpha = 1.0f; // 최대값 제한
                fadeTimer.stop(); // 애니메이션 종료
            }
            repaint(); // 화면 다시 그리기
        });
        fadeTimer.start();

        // ----- 자동 화면 전환 타이머 -----
        // 3000ms(3초) 후 메뉴 화면으로 자동 이동
        transitionTimer = new Timer(3000, e -> goToMenu());
        transitionTimer.setRepeats(false); // 한 번만 실행
        transitionTimer.start();
    }

    /**
     * ============================================
     * playSplashSound() - 스플래시 효과음 재생
     * ============================================
     * 
     * [설명]
     * - sound/splash2.wav 파일을 재생합니다.
     * - GameSettings의 sfxVolume 설정에 따라 볼륨이 조절됩니다.
     * 
     * [파일 경로]
     * - 프로젝트폴더/sound/splash2.wav
     */
    private void playSplashSound() {
        try {
            // 효과음 파일 경로 생성
            String soundPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator
                    + "splash2.wav";
            File soundFile = new File(soundPath);

            // 파일 존재 확인
            if (soundFile.exists()) {
                // 오디오 스트림 열기
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                // ----- SFX 볼륨 적용 -----
                // GameSettings.sfxVolume 값에 따라 볼륨 설정
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float min = volumeControl.getMinimum(); // 최소 볼륨 (데시벨)
                    float max = volumeControl.getMaximum(); // 최대 볼륨 (데시벨)
                    // 0~100을 min~max 범위로 변환
                    float gain = min + (max - min) * (GameSettings.sfxVolume / 100.0f);
                    volumeControl.setValue(gain);
                }

                clip.start(); // 재생 시작
                System.out.println("스플래시 효과음 재생: " + soundPath);
            } else {
                System.err.println("스플래시 효과음 파일을 찾을 수 없습니다: " + soundPath);
            }
        } catch (Exception e) {
            System.err.println("스플래시 효과음 재생 실패: " + e.getMessage());
        }
    }

    /**
     * ============================================
     * goToMenu() - 메뉴 화면으로 이동
     * ============================================
     * 
     * [설명]
     * - 모든 타이머를 중지합니다.
     * - BGM 재생을 시작합니다.
     * - 메뉴 화면으로 전환합니다.
     */
    private void goToMenu() {
        // 실행 중인 타이머 정지
        if (transitionTimer != null) {
            transitionTimer.stop();
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
        }

        // BGM 재생 시작 (메뉴 화면부터 배경음악 재생)
        mainFrame.startBGM();

        // 메뉴 화면으로 전환
        mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU);
    }

    /**
     * ============================================
     * paintComponent() - 화면 그리기
     * ============================================
     * 
     * [설명]
     * - 스플래시 화면의 모든 시각적 요소를 그립니다.
     * - 이미지가 있으면 이미지를 표시하고, 없으면 텍스트 로고를 표시합니다.
     * - 페이드 인 효과를 위해 AlphaComposite를 사용합니다.
     * 
     * [그리기 순서]
     * 1. 검정 배경
     * 2. 스플래시 이미지 또는 텍스트 로고 (페이드 인 효과 적용)
     * 3. 하단 안내 메시지
     * 
     * @param g Graphics 객체 (그리기 도구)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Graphics2D로 캐스팅 (더 많은 기능 사용 가능)
        Graphics2D g2 = (Graphics2D) g.create();

        // 안티앨리어싱 설정 (부드러운 그래픽)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ----- 1. 배경 -----
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // ----- 2. 페이드 인 효과 적용 -----
        // AlphaComposite: 투명도를 적용하여 그리기
        // alpha 값이 0이면 완전 투명, 1이면 완전 불투명
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (splashImage != null) {
            // ----- 스플래시 이미지 표시 -----
            // 이미지를 패널 전체 크기에 맞게 그리기
            g2.drawImage(splashImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // ----- 이미지 없을 때: 텍스트 로고 표시 -----

            // 그라데이션 배경 (노랑 → 주황)
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 200, 50), // 시작색 (노랑)
                    0, getHeight(), new Color(255, 100, 0)); // 끝색 (주황)
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // 게임 타이틀
            g2.setColor(ThemeColors.DARK);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 60));
            String title = "Water Bomb Man";
            FontMetrics fm = g2.getFontMetrics();
            int titleX = (getWidth() - fm.stringWidth(title)) / 2; // 중앙 정렬
            g2.drawString(title, titleX, 250);

            // 서브 타이틀
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 24));
            String subtitle = "물풍선 대작전!";
            fm = g2.getFontMetrics();
            int subX = (getWidth() - fm.stringWidth(subtitle)) / 2; // 중앙 정렬
            g2.drawString(subtitle, subX, 310);
        }

        // ----- 3. 하단 안내 메시지 -----
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        String hint = "클릭하거나 아무 키나 눌러서 시작";
        FontMetrics fm = g2.getFontMetrics();
        int hintX = (getWidth() - fm.stringWidth(hint)) / 2; // 중앙 정렬
        g2.drawString(hint, hintX, getHeight() - 50);

        g2.dispose(); // Graphics2D 리소스 해제
    }
}
