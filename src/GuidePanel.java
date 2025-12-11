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
        contentPanel.setPreferredSize(new Dimension(760, 1550)); // 더 긴 높이
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

        // 아이템 목록
        JPanel itemListPanel = createItemListPanel(20, itemY + 120, sectionWidth, 150);
        contentPanel.add(itemListPanel);

        // 뒤로가기 버튼
        JButton backBtn = createStyledButton("뒤로가기", 330, itemY + 290, 120, 45);
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
                JLabel gifLabel = new JLabel(gifIcon);
                gifLabel.setBounds(20, 80, w - 40, h - 100);
                panel.add(gifLabel);
            } else {
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

    /**
     * 아이템 목록 패널
     */
    private JPanel createItemListPanel(int x, int y, int w, int h) {
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

        JLabel titleLabel = new JLabel("아이템 목록", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 220, 100));
        titleLabel.setBounds(0, 10, w, 28);
        panel.add(titleLabel);

        String[][] items = {
                { "물풍선", "개수+1" },
                { "물줄기", "길이+1" },
                { "스케이트", "속도+1" },
                { "쉴드", "방어1회" },
                { "바늘", "통과" },
                { "악마", "감소" }
        };

        int slotSize = 70;
        int gap = 40;
        int totalWidth = items.length * slotSize + (items.length - 1) * gap;
        int startX = (w - totalWidth) / 2;
        int startY = 45;

        for (int i = 0; i < items.length; i++) {
            int slotX = startX + i * (slotSize + gap);

            JPanel itemSlot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(100, 100, 110));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(new Color(80, 80, 90));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                }
            };
            itemSlot.setBounds(slotX, startY, slotSize, slotSize);
            itemSlot.setOpaque(false);
            panel.add(itemSlot);

            JLabel nameLabel = new JLabel(items[i][0]);
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setBounds(slotX - 5, startY + slotSize + 5, slotSize + 10, 16);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(nameLabel);
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
