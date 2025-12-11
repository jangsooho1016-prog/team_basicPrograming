import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ========================================================
 * 3. 가이드 패널 (Guide Panel)
 * ========================================================
 * 게임 조작법을 안내하는 화면입니다.
 * 스크롤로 아래로 내려가며 조작법과 GIF를 크게 볼 수 있습니다.
 */
public class GuidePanel extends JPanel {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private CrazyArcade_UI mainFrame;

    public GuidePanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 50));

        // 스크롤 가능한 내용 패널 (세로로 길게)
        JPanel contentPanel = new JPanel(null);
        contentPanel.setPreferredSize(new Dimension(760, 1400)); // 높이 조정
        contentPanel.setBackground(new Color(50, 50, 50));

        // 타이틀
        JLabel titleLabel = new JLabel("Guide", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 15, 780, 50);
        contentPanel.add(titleLabel);

        // ========== 조작법 + 큰 GIF (세로 배치) ==========
        int sectionWidth = 740;
        int sectionHeight = 280;
        int bigSectionHeight = 350; // 위.아래, 물풍선 설치용 큰 높이
        int gap = 20;
        int startY = 80;

        // 1. 위.아래 섹션 (크게)
        contentPanel.add(createControlSection("위 . 아래", "1P: W, S     2P: ↑, ↓", "상하.gif",
                20, startY, sectionWidth, bigSectionHeight));

        // 2. 좌우 섹션
        contentPanel.add(createControlSection("좌 우", "1P: A, D     2P: ←, →", "좌우.gif",
                20, startY + bigSectionHeight + gap, sectionWidth, sectionHeight));

        // 3. 물풍선 설치 섹션 (크게)
        contentPanel.add(createControlSection("물풍선 설치", "1P: Shift   2P: NumPad 1", "물풍선 설치.gif",
                20, startY + bigSectionHeight + sectionHeight + gap * 2, sectionWidth, bigSectionHeight));

        // 4. 아이템 사용 섹션
        int itemY = startY + bigSectionHeight * 2 + sectionHeight + gap * 3;
        contentPanel.add(createControlSection("아이템 사용", "1P: Ctrl    2P: NumPad 0", null,
                20, itemY, sectionWidth, 100));

        // 뒤로가기 버튼 (위치 조정)
        JButton backBtn = createStyledButton("뒤로가기", 330, itemY + 120, 120, 45);
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        contentPanel.add(backBtn);

        // 스크롤 패널 생성
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 조작법 섹션 생성 (큰 GIF 포함, 세로 배치)
     */
    private JPanel createControlSection(String title, String keys, String gifFile, int x, int y, int w, int h) {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(60, 60, 70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(100, 180, 255));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);
            }
        };
        panel.setBounds(x, y, w, h);
        panel.setOpaque(false);

        // 제목
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setForeground(new Color(255, 220, 100));
        titleLabel.setBounds(20, 15, 200, 30);
        panel.add(titleLabel);

        // 키 설명
        JLabel keysLabel = new JLabel(keys);
        keysLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        keysLabel.setForeground(Color.WHITE);
        keysLabel.setBounds(20, 50, 300, 22);
        panel.add(keysLabel);

        // GIF 애니메이션 (크게 표시)
        if (gifFile != null) {
            String gifPath = System.getProperty("user.dir") + File.separator + "res" + File.separator + gifFile;
            File file = new File(gifPath);

            if (file.exists()) {
                ImageIcon gifIcon = new ImageIcon(gifPath);
                int gifW = gifIcon.getIconWidth();
                int gifH = gifIcon.getIconHeight();

                // [중앙 정렬 계산]
                // 섹션 너비(w)에서 GIF 너비(gifW)를 뺀 나머지를 2로 나누어 X좌표를 구합니다.
                int xPos = (w - gifW) / 2;
                if (xPos < 0)
                    xPos = 0;

                // [핵심: 하단 노이즈 제거를 위한 클리핑 기법]
                // 원본 GIF 이미지의 하단에 불필요한 잔상(노이즈)이 포함되어 있습니다.
                // 이를 사용자에게 보여주지 않기 위해, JPanel을 액자처럼 사용하여 하단 45px을 잘라냅니다.

                // 표시할 최대 높이 설정 (섹션 높이 - 여백)
                int sectionLimitHeight = h - 90;

                // 실제 표시할 높이 = 원본 높이에서 45px을 뺀 값 (단, 섹션 높이를 넘지 않게 함)
                int clipHeight = Math.min(sectionLimitHeight, gifH - 45);

                // 클리핑용 부모 패널 (액자 역할)
                // 이 패널의 높이를 clipHeight로 제한하여, 자식 컴포넌트(이미지)의 아랫부분이 잘리게 합니다.
                JPanel clipPanel = new JPanel(null);
                clipPanel.setBounds(xPos, 80, gifW, clipHeight);
                clipPanel.setOpaque(false); // 배경 투명

                // 실제 GIF 이미지 라벨
                JLabel gifLabel = new JLabel(gifIcon);
                // 이미지는 원본 크기 그대로 배치합니다.
                // 부모인 clipPanel의 높이가 이미지보다 작으므로, 아래쪽 45px은 화면에 그려지지 않습니다.
                gifLabel.setBounds(0, 0, gifW, gifH);

                clipPanel.add(gifLabel);
                panel.add(clipPanel);
            } else {
                // 이미지가 없을 경우 대체 텍스트 표시
                JLabel placeholder = new JLabel(gifFile, SwingConstants.CENTER);
                placeholder.setFont(new Font("맑은 고딕", Font.BOLD, 18));
                placeholder.setForeground(Color.WHITE);
                placeholder.setOpaque(true);
                placeholder.setBackground(Color.BLACK);
                placeholder.setBounds(20, 80, w - 40, h - 100);
                panel.add(placeholder);
            }
        }

        return panel;
    }

    private JButton createStyledButton(String text, int x, int y, int w, int h) {
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

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                super.paintComponent(g);
            }
        };
        btn.setBounds(x, y, w, h);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }
}
