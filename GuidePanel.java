import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * ========================================================
 * 3. 가이드 패널 (Guide Panel)
 * ========================================================
 * 게임 방법(조작법)을 안내하는 화면입니다.
 * 준비된 이미지(game play.png)를 화면에 맞게 비율을 유지하며 출력합니다.
 */
public class GuidePanel extends JPanel {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private Image guideImage;
    private CrazyArcade_UI mainFrame;

    /**
     * 생성자: 가이드 화면 초기화
     */
    public GuidePanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null);
        setBackground(ThemeColors.BG); // 배경색 설정

        // [이미지 로드]
        // res/game play.png 리소스를 안전하게 로드합니다.
        URL guideUrl = getClass().getResource("/res/game play.png");
        if (guideUrl != null) {
            ImageIcon icon = new ImageIcon(guideUrl);
            guideImage = icon.getImage();
        } else {
            System.err.println("GuidePanel: 리소스 로드 실패 - /res/game play.png");
            guideImage = null; // 실패 시 null 처리
        }

        // [홈으로 돌아가기 버튼]
        JButton backBtn = createThemedButton("홈으로");
        backBtn.setBounds(300, 520, 200, 50); // 하단 중앙 배치
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        add(backBtn);
    }

    /**
     * 테마가 적용된 버튼 생성 헬퍼 메서드
     */
    private JButton createThemedButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 마우스 상태에 따른 색상 변화
                if (getModel().isPressed())
                    g2.setColor(ThemeColors.ACCENT);
                else if (getModel().isRollover())
                    g2.setColor(ThemeColors.HIGHLIGHT);
                else
                    g2.setColor(ThemeColors.MAIN);

                // 둥근 사각형 배경
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // 테두리
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

    /**
     * 화면 그리기 메서드
     * 이미지가 있을 경우 화면 비율에 맞춰 중앙에 정렬하여 그립니다 (Letterboxing).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (guideImage != null && guideImage.getWidth(null) > 0) {
            // 원본 이미지 크기
            int imgWidth = guideImage.getWidth(null);
            int imgHeight = guideImage.getHeight(null);

            // 화면 크기 대비 비율 계산 (가로/세로 비율 중 더 작은 쪽을 기준)
            double scaleX = (double) getWidth() / imgWidth;
            double scaleY = (double) getHeight() / imgHeight;
            double scale = Math.min(scaleX, scaleY); // 비율 유지

            // 최종 출력 크기 계산
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);

            // 중앙 정렬 좌표 계산
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;

            // 스케일된 이미지 그리기
            g2.drawImage(guideImage, x, y, scaledWidth, scaledHeight, this);
        } else {
            // 이미지 로드 실패 시 텍스트 표시
            g2.setColor(Color.RED);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 30));
            g2.drawString("이미지를 찾을 수 없습니다: game play.png", 150, 300);
        }
    }
}
