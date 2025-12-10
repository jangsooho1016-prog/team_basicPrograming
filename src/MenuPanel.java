import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

// ========================================================
// 1. 메뉴 화면
// ========================================================
public class MenuPanel extends JPanel {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private Image backgroundImage;
    private CrazyArcade_UI mainFrame;

    public MenuPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null);

        // [수정된 부분 시작] 클래스 로더를 이용한 이미지 로드
        URL startUrl = getClass().getResource("/res/start.png");
        if (startUrl != null) {
            ImageIcon icon = new ImageIcon(startUrl);
            backgroundImage = icon.getImage();
        } else {
            // 이미지를 찾지 못했을 때 오류 메시지 출력 (디버깅 용)
            System.err.println("MenuPanel: Failed to load resource at /res/start.png");
            // 배경 이미지를 null로 두어 paintComponent에서 대체 배경이 표시되게 함
            backgroundImage = null;
        }
        // [수정된 부분 끝]

        int buttonWidth = 130;
        int buttonHeight = 45;
        int gap = 15;
        int startY = 500;
        int startX = (PANEL_WIDTH - ((buttonWidth * 5) + (gap * 4))) / 2;

        add(createRoundedButton("Game Start", startX, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY)));
        add(createRoundedButton("Guide", startX + (buttonWidth + gap) * 1, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_GUIDE)));
        add(createRoundedButton("Settings", startX + (buttonWidth + gap) * 2, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_SETTINGS)));
        add(createRoundedButton("Credits", startX + (buttonWidth + gap) * 3, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_CREDITS)));
        add(createRoundedButton("Exit", startX + (buttonWidth + gap) * 4, startY, buttonWidth, buttonHeight,
                e -> System.exit(0)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (backgroundImage != null)
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        else
            g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private JButton createRoundedButton(String text, int x, int y, int width, int height, ActionListener action) {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                // 테두리
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);
                super.paintComponent(g);
            }
        };
        btn.setBounds(x, y, width, height);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btn.setForeground(ThemeColors.DARK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.addActionListener(action);
        return btn;
    }
}
