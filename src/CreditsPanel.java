import javax.swing.*;
import java.awt.*;
import java.net.URL;

// ========================================================
// 4. 크레딧 패널
// ========================================================
public class CreditsPanel extends JPanel {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private CrazyArcade_UI mainFrame;
    private Timer scrollTimer;
    private int scrollY;
    private JPanel textPanel;
    private JPanel scrollContainer;
    private Image backgroundImage;

    private String creditsText = "<html><center>"
            + "<h1>Water Bomb Man</h1><br><br>"
            + "<h2>[ 개발팀 ]</h2>"
            + "<p>총괄 디렉터: 장수호</p>"
            + "<p>메인 프로그래머: 홍길동</p>"
            + "<p>UI/UX 디자인: 김철수</p><br>"
            + "<h2>[ 아트 & 사운드 ]</h2>"
            + "<p>캐릭터 디자인: 서승하</p>"
            + "<p>배경 및 이펙트: 이영희</p>"
            + "<p>사운드 디자인: 박민수</p><br>"
            + "<h2>[ 스페셜 땡스 ]</h2>"
            + "<p>물풍선 아이디어: 최이삭</p>"
            + "<p>QA 테스터: 팀원 전원</p><br><br>"
            + "<h3>Thank You for Playing!</h3>"
            + "</center></html>";

    public CreditsPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null);

        // [수정된 부분 시작] 클래스 로더를 이용한 이미지 로드
        URL creditsUrl = getClass().getResource("/res/creditss.png");
        if (creditsUrl != null) {
            ImageIcon icon = new ImageIcon(creditsUrl);
            backgroundImage = icon.getImage();
        } else {
            // 이미지 로드 실패 시 디버깅 메시지 출력
            System.err.println("CreditsPanel: Failed to load resource at /res/creditss.png");
            backgroundImage = null; // paintComponent에서 실패 처리
        }
        // [수정된 부분 끝]

        int viewportHeight = 500;
        scrollContainer = new JPanel();
        scrollContainer.setLayout(null);
        scrollContainer.setBounds(0, 0, PANEL_WIDTH, viewportHeight);
        scrollContainer.setOpaque(false);
        add(scrollContainer);

        textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.setBounds(0, viewportHeight, PANEL_WIDTH, 1000);

        JLabel label = new JLabel(creditsText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        textPanel.add(label, BorderLayout.NORTH);

        scrollContainer.add(textPanel);

        JButton backBtn = new JButton("홈으로") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2.setColor(ThemeColors.ACCENT);
                else if (getModel().isRollover())
                    g2.setColor(ThemeColors.HIGHLIGHT);
                else
                    g2.setColor(ThemeColors.MAIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                super.paintComponent(g);
            }
        };
        backBtn.setBounds(300, 520, 200, 50);
        backBtn.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        backBtn.setForeground(ThemeColors.DARK);
        backBtn.setFocusPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        add(backBtn);

        scrollY = viewportHeight;

        scrollTimer = new Timer(50, e -> {
            scrollY -= 2;
            textPanel.setLocation(0, scrollY);
            if (scrollY + textPanel.getHeight() < 0) {
                scrollY = viewportHeight;
            }
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public void startScrolling() {
        scrollY = 500;
        textPanel.setLocation(0, scrollY);
        scrollTimer.start();
    }

    public void stopScrolling() {
        scrollTimer.stop();
    }
}
