import javax.swing.*;
import java.awt.*;
import java.net.URL;

// ========================================================
// 3. 가이드 패널
// ========================================================
public class GuidePanel extends JPanel {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private Image guideImage;
    private CrazyArcade_UI mainFrame;

    public GuidePanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null);
        setBackground(ThemeColors.BG);

        // [수정된 부분 시작] 클래스 로더를 이용한 이미지 로드
        URL guideUrl = getClass().getResource("/res/game play.png");
        if (guideUrl != null) {
            ImageIcon icon = new ImageIcon(guideUrl);
            guideImage = icon.getImage();
        } else {
            // 이미지 로드 실패 시 디버깅 메시지 출력
            System.err.println("GuidePanel: Failed to load resource at /res/game play.png");
            guideImage = null; // paintComponent에서 실패 처리
        }
        // [수정된 부분 끝]

        JButton backBtn = createThemedButton("홈으로");
        backBtn.setBounds(300, 520, 200, 50);
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        add(backBtn);
    }

    private JButton createThemedButton(String text) {
        JButton btn = new JButton(text) {
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
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        btn.setForeground(ThemeColors.DARK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (guideImage != null && guideImage.getWidth(null) > 0) {
            int imgWidth = guideImage.getWidth(null);
            int imgHeight = guideImage.getHeight(null);
            double scaleX = (double) getWidth() / imgWidth;
            double scaleY = (double) getHeight() / imgHeight;
            double scale = Math.min(scaleX, scaleY);
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;
            g2.drawImage(guideImage, x, y, scaledWidth, scaledHeight, this);
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 30));
            g2.drawString("이미지를 찾을 수 없습니다: game play.png", 150, 300);
        }
    }
}
