import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

// ========================================================
// 5. 대기실 (Lobby) 화면
// ========================================================
public class LobbyPanel extends JPanel {
    private CrazyArcade_UI mainFrame;
    private String selectedCharacter = "배찌"; // 기본 선택 캐릭터

    public LobbyPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setBackground(ThemeColors.BG);

        JLabel titleLabel = new JLabel("게임 로비 / Game Lobby");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        titleLabel.setForeground(ThemeColors.DARK);
        titleLabel.setBounds(30, 20, 500, 40);
        add(titleLabel);

        JPanel charPanel = createPanel("캐릭터 선택", 30, 80, 250, 400);

        // 배찌 캐릭터 카드
        JPanel bazziCard = createCharacterCard("배찌", "/res/배찌.png");
        bazziCard.setBounds(15, 40, 220, 160);
        charPanel.add(bazziCard);

        // 다오 캐릭터 카드
        JPanel daoCard = createCharacterCard("다오", "/res/다오.png");
        daoCard.setBounds(15, 210, 220, 160);
        charPanel.add(daoCard);

        // 캐릭터 선택 상호 배타적 처리
        bazziCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedCharacter = "배찌";
                bazziCard.setBorder(BorderFactory.createLineBorder(ThemeColors.ACCENT, 4));
                daoCard.setBorder(BorderFactory.createLineBorder(ThemeColors.DARK, 2));
            }
        });
        daoCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedCharacter = "우니";
                daoCard.setBorder(BorderFactory.createLineBorder(ThemeColors.ACCENT, 4));
                bazziCard.setBorder(BorderFactory.createLineBorder(ThemeColors.DARK, 2));
            }
        });

        // 기본 선택: 배찌
        bazziCard.setBorder(BorderFactory.createLineBorder(ThemeColors.ACCENT, 4));

        add(charPanel);

        JPanel mapPanel = createPanel("맵 정보", 300, 80, 450, 200);
        JLabel mapText = new JLabel("맵: 숲속마을 01");
        mapText.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        mapText.setForeground(ThemeColors.TEXT);
        mapText.setBounds(20, 80, 300, 30);
        mapPanel.add(mapText);
        add(mapPanel);

        JPanel chatPanel = createPanel("채팅", 300, 300, 450, 180);

        // 채팅 메시지 표시 영역
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(255, 255, 250));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBounds(10, 30, 430, 100);
        chatScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.DARK, 1));
        chatPanel.add(chatScroll);

        // 입력 필드
        JTextField inputField = new JTextField();
        inputField.setBounds(10, 140, 350, 30);
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        chatPanel.add(inputField);

        // 전송 버튼
        JButton sendBtn = createThemedButton("전송", 370, 140, 70, 30);
        sendBtn.addActionListener(e -> {
            String msg = inputField.getText().trim();
            if (!msg.isEmpty()) {
                chatArea.append(selectedCharacter + ": " + msg + "\n");
                inputField.setText("");
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        });

        // 엔터키로도 전송
        inputField.addActionListener(e -> sendBtn.doClick());

        chatPanel.add(sendBtn);
        add(chatPanel);

        JButton backBtn = createThemedButton("뒤로", 30, 500, 150, 50);
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        add(backBtn);

        JButton startBtn = createStartButton("게임 시작!");
        startBtn.setBounds(600, 500, 150, 50);
        startBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_GAME));
        add(startBtn);
    }

    private JButton createThemedButton(String text, int x, int y, int w, int h) {
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
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btn.setForeground(ThemeColors.DARK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private JButton createStartButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2.setColor(new Color(255, 120, 0));
                else if (getModel().isRollover())
                    g2.setColor(ThemeColors.ACCENT);
                else
                    g2.setColor(new Color(255, 160, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btn.setForeground(ThemeColors.DARK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private JPanel createCharacterCard(String name, String imagePath) {
        JPanel card = new JPanel() {
            private Image charImage;
            {
                URL imgUrl = getClass().getResource(imagePath);
                if (imgUrl != null) {
                    charImage = new ImageIcon(imgUrl).getImage();
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 배경
                g2.setColor(new Color(255, 255, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 이미지 표시
                if (charImage != null) {
                    int imgSize = 100;
                    int x = (getWidth() - imgSize) / 2;
                    g2.drawImage(charImage, x, 10, imgSize, imgSize, this);
                }

                // 캐릭터 이름
                g2.setColor(ThemeColors.DARK);
                g2.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(name)) / 2;
                g2.drawString(name, textX, getHeight() - 20);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createLineBorder(ThemeColors.DARK, 2));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return card;
    }

    private JPanel createPanel(String title, int x, int y, int w, int h) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        p.setLayout(null);
        p.setBounds(x, y, w, h);
        p.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setBounds(10, 8, 200, 20);
        l.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        l.setForeground(ThemeColors.DARK);
        p.add(l);
        return p;
    }
}
