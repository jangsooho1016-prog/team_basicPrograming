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
 * 7. 스플래시 (Splash) 화면 패널
 * ========================================================
 * 프로그램 시작 시 처음 보여지는 인트로 화면입니다.
 * 
 * 주요 기능:
 * 1. 로고 이미지 표시 또는 텍스트 로고 그리기
 * 2. 페이드 인(Fade-in) 애니메이션 효과
 * 3. 시작 효과음(splash2.wav) 재생
 * 4. 일정 시간 후 또는 사용자 입력 시 메뉴 화면으로 자동 전환
 */
public class SplashPanel extends JPanel {
    // 패널 크기 상수
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;

    private CrazyArcade_UI mainFrame; // 화면 전환을 위한 메인 프레임 참조

    private Timer transitionTimer; // 일정 시간 후 자동 전환을 위한 타이머
    private float alpha = 0f; // 현재 투명도 (0.0: 완전 투명 ~ 1.0: 완전 불투명) - 페이드 인 효과용
    private Timer fadeTimer; // 페이드 인 애니메이션 타이머
    private Image splashImage; // 스플래시 이미지 객체

    /**
     * 생성자: 스플래시 패널 초기화 및 이벤트 리스너 설정
     * 
     * @param mainFrame 메인 프레임 인스턴스 (화면 전환용)
     */
    public SplashPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null);
        setBackground(Color.BLACK); // 배경색 검정

        // 1. 스플래시 이미지 로드 (res/start.png)
        // 파일 시스템 기반 경로를 사용하여 이미지를 로드합니다.
        String splashPath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "start.png";
        File splashFile = new File(splashPath);
        if (splashFile.exists()) {
            splashImage = new ImageIcon(splashPath).getImage();
        }

        // 2. 마우스 클릭 이벤트: 클릭 시 바로 메뉴로 이동 (스킵 기능)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goToMenu();
            }
        });

        // 3. 키보드 입력 이벤트: 아무 키나 누르면 바로 메뉴로 이동 (스킵 기능)
        setFocusable(true); // 키 입력을 받기 위해 포커스 가능 설정
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                goToMenu();
            }
        });
    }

    /**
     * 패널이 화면에 표시될 때 프레임워크에 의해 호출되는 메서드
     * 이 시점에 컴포넌트가 준비되므로 애니메이션과 사운드를 시작합니다.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow(); // 키 입력을 받기 위해 포커스 요청
        startSplash(); // 스플래시 시퀀스 시작
    }

    /**
     * 스플래시 시퀀스 시작
     * 사운드 재생, 페이드 인 효과, 자동 전환 타이머를 시작합니다.
     */
    private void startSplash() {
        // 1. 효과음 재생 (splash2.wav)
        playSplashSound();

        // 2. 페이드 인 애니메이션 설정
        // 30ms마다 알파값을 0.05씩 증가시켜 점점 선명해지게 함
        alpha = 0f;
        fadeTimer = new Timer(30, e -> {
            alpha += 0.05f;
            if (alpha >= 1.0f) {
                alpha = 1.0f; // 최대값 제한
                fadeTimer.stop(); // 애니메이션 종료
            }
            repaint(); // 화면 갱신 요청 -> paintComponent 호출됨
        });
        fadeTimer.start();

        // 3. 자동 화면 전환 타이머 설정
        // 3초(3000ms) 후에 goToMenu()를 실행
        transitionTimer = new Timer(3000, e -> goToMenu());
        transitionTimer.setRepeats(false); // 한 번만 실행
        transitionTimer.start();
    }

    /**
     * 스플래시 사운드 재생 메서드
     * Java Swing의 AudioSystem을 사용하여 wav 파일을 재생합니다.
     */
    private void playSplashSound() {
        try {
            // 현재 작업 디렉토리 하위의 sound 폴더에서 splash2.wav 파일을 찾음
            String soundPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator
                    + "splash2.wav";
            File soundFile = new File(soundPath);

            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                // SFX(효과음) 볼륨 설정 적용
                // GameSettings 클래스의 전역 설정값을 반영
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float min = volumeControl.getMinimum();
                    float max = volumeControl.getMaximum();
                    // 0~100 사이의 설정을 데시벨(dB) 게인으로 변환하여 적용
                    float gain = min + (max - min) * (GameSettings.sfxVolume / 100.0f);
                    volumeControl.setValue(gain);
                }

                clip.start(); // 재생 시작
                System.out.println("Splash 효과음 재생: " + soundPath);
            } else {
                System.err.println("Splash 효과음 파일을 찾을 수 없습니다: " + soundPath);
            }
        } catch (Exception e) {
            System.err.println("Splash 효과음 재생 실패: " + e.getMessage());
        }
    }

    /**
     * 메뉴 화면으로 전환하는 메서드
     * 타이머를 정리하고 메인 프레임에 메뉴 화면 전환을 요청합니다.
     */
    private void goToMenu() {
        // 실행 중인 타이머 정지 (중복 실행 방지)
        if (transitionTimer != null) {
            transitionTimer.stop();
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
        }

        // 메인 BGM 재생 시작
        mainFrame.startBGM();

        // 메뉴 화면으로 전환 요청
        mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU);
    }

    /**
     * 화면 그리기 메서드
     * 스플래시 이미지나 텍스트를 알파값(투명도)을 적용하여 그립니다.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // 안티앨리어싱 설정 (부드러운 그래픽)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 배경 그리기
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 현재 알파값(투명도) 적용
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (splashImage != null) {
            // 이미지가 로드된 경우 이미지 표시
            g2.drawImage(splashImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // 이미지가 없는 경우 기본 디자인(그라데이션 + 텍스트) 그리기

            // 그라데이션 배경
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 200, 50),
                    0, getHeight(), new Color(255, 100, 0));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // 게임 타이틀 텍스트
            g2.setColor(ThemeColors.DARK);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 60));
            String title = "Water Bomb Man";

            // 텍스트 중앙 정렬 계산
            FontMetrics fm = g2.getFontMetrics();
            int titleX = (getWidth() - fm.stringWidth(title)) / 2;
            g2.drawString(title, titleX, 250);

            // 서브 타이틀
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 24));
            String subtitle = "물풍선 대작전!";
            fm = g2.getFontMetrics();
            int subX = (getWidth() - fm.stringWidth(subtitle)) / 2;
            g2.drawString(subtitle, subX, 310);
        }

        // 하단 안내 메시지 (항상 표시)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        String hint = "클릭하거나 아무 키나 눌러서 시작";
        FontMetrics fm = g2.getFontMetrics();
        int hintX = (getWidth() - fm.stringWidth(hint)) / 2;
        g2.drawString(hint, hintX, getHeight() - 50);

        g2.dispose(); // 그래픽 자원 해제
    }
}
