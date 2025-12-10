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
import java.net.URL;

// ========================================================
// 7. 스플래시 (Splash) 화면 - splash2.wav 효과음 재생
// ========================================================
public class SplashPanel extends JPanel {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private CrazyArcade_UI mainFrame;
    private Timer transitionTimer;
    private float alpha = 0f; // 페이드 인 효과용
    private Timer fadeTimer;
    private Image splashImage;

    public SplashPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null);
        setBackground(Color.BLACK);

        // 스플래시 이미지 로드 시도 (res/splash.png)
        URL splashUrl = getClass().getResource("/res/splash.png");
        if (splashUrl != null) {
            splashImage = new ImageIcon(splashUrl).getImage();
        }

        // 마우스 클릭 시 바로 메뉴로 이동
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goToMenu();
            }
        });

        // 키보드 입력 시 바로 메뉴로 이동
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                goToMenu();
            }
        });
    }

    // 패널이 화면에 표시될 때 호출
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
        startSplash();
    }

    private void startSplash() {
        // splash2.wav 효과음 재생
        playSplashSound();

        // 페이드 인 효과
        alpha = 0f;
        fadeTimer = new Timer(30, e -> {
            alpha += 0.05f;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();
            }
            repaint();
        });
        fadeTimer.start();

        // 3초 후 메뉴 화면으로 자동 전환
        transitionTimer = new Timer(3000, e -> goToMenu());
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }

    private void playSplashSound() {
        try {
            String soundPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator
                    + "splash2.wav";
            File soundFile = new File(soundPath);
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                // SFX 볼륨 적용
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float min = volumeControl.getMinimum();
                    float max = volumeControl.getMaximum();
                    float gain = min + (max - min) * (GameSettings.sfxVolume / 100.0f);
                    volumeControl.setValue(gain);
                }

                clip.start();
                System.out.println("Splash 효과음 재생: " + soundPath);
            } else {
                System.err.println("Splash 효과음 파일을 찾을 수 없습니다: " + soundPath);
            }
        } catch (Exception e) {
            System.err.println("Splash 효과음 재생 실패: " + e.getMessage());
        }
    }

    private void goToMenu() {
        if (transitionTimer != null) {
            transitionTimer.stop();
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
        }
        // BGM 재생 시작
        mainFrame.startBGM();
        // 메뉴 화면으로 이동
        mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 배경
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 페이드 인 효과 적용
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (splashImage != null) {
            // 스플래시 이미지가 있으면 표시
            g2.drawImage(splashImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // 이미지가 없으면 텍스트 로고 표시
            // 그라데이션 배경
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 200, 50),
                    0, getHeight(), new Color(255, 100, 0));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // 게임 타이틀
            g2.setColor(ThemeColors.DARK);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 60));
            String title = "Water Bomb Man";
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

        // 하단 안내 메시지
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        String hint = "클릭하거나 아무 키나 눌러서 시작";
        FontMetrics fm = g2.getFontMetrics();
        int hintX = (getWidth() - fm.stringWidth(hint)) / 2;
        g2.drawString(hint, hintX, getHeight() - 50);
        g2.dispose();
    }
}
