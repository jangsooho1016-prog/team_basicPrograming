import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * ========================================================
 * 5. 대기실 (Lobby) 화면 패널
 * ========================================================
 * 사용자 요청 반영:
 * 1. 배경 이미지 제거
 * 2. 캐릭터 선택 3개 (다오, 배찌, 랜덤)
 * 3. 선택된 캐릭터 표시 및 밑에 1P/2P 라벨 표시
 * 4. 채팅창 복구
 */
public class LobbyPanel extends JPanel {
    private CrazyArcade_UI mainFrame;
    private String p1Character = "배찌"; // 기본값
    private String p2Character = "다오"; // 기본값
    private Cursor customCursor;

    // 캐릭터 이미지
    private Image bazziImg;
    private Image daoImg;

    // UI 컴포넌트
    private JTextArea chatArea;
    private JTextField chatInput;

    // 선택 카드 (피드백용)
    private JPanel cardBazzi, cardDao, cardRandom;

    public LobbyPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setBackground(new Color(240, 240, 255)); // 밝은 배경색

        loadCharacterImages();
        loadCustomCursor();

        // 1. 상단 타이틀
        JLabel titleLabel = new JLabel("Game Lobby");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        titleLabel.setBounds(30, 20, 300, 40);
        add(titleLabel);

        // 2. 캐릭터 선택 영역 (중앙 상단)
        createSelectionArea();

        // 3. 현재 선택된 플레이어 표시 영역 (좌측/우측)
        // 1P 표시 (좌측)
        JPanel p1Panel = createPlayerDisplayPanel("1P", 50, 250);
        add(p1Panel);

        // 2P 표시 (우측)
        JPanel p2Panel = createPlayerDisplayPanel("2P", 450, 250);
        add(p2Panel);

        // 4. 채팅 영역 (하단)
        createChatPanel();

        // 5. 버튼 (뒤로가기, 시작)
        JButton backBtn = createStyledButton("뒤로", 30, 500, 100, 40, new Color(200, 200, 200));
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        add(backBtn);

        JButton startBtn = createStyledButton("게임 시작", 650, 500, 120, 40, new Color(255, 200, 0));
        startBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_GAME));
        add(startBtn);

        // 초기 UI 갱신 (선택 테두리 등)
        updateSelectionUI();
    }

    private void createSelectionArea() {
        JLabel label = new JLabel("캐릭터를 선택하세요 (우클릭: 1P, 좌클릭: 2P)");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        label.setBounds(200, 70, 400, 20);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label);

        int cardY = 100;
        int cardSize = 100;
        int gap = 30;
        int startX = (800 - (cardSize * 3 + gap * 2)) / 2;

        // 배찌 카드
        cardBazzi = createCharacterCard("배찌", bazziImg, startX, cardY, cardSize);
        add(cardBazzi);

        // 다오 카드
        cardDao = createCharacterCard("다오", daoImg, startX + cardSize + gap, cardY, cardSize);
        add(cardDao);

        // 랜덤 카드
        cardRandom = createCharacterCard("랜덤", null, startX + (cardSize + gap) * 2, cardY, cardSize);
        add(cardRandom);
    }

    private JPanel createCharacterCard(String name, Image img, int x, int y, int size) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 배경
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 이미지
                if (img != null) {
                    g2.drawImage(img, 10, 10, size - 20, size - 20, this);
                } else {
                    // 랜덤 물음표
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("맑은 고딕", Font.BOLD, 50));
                    g2.drawString("?", 35, 70);
                }

                // 테두리 (선택 상태에 따라 변경됨)
                float strokeWidth = 2f;
                Color borderColor = Color.LIGHT_GRAY;

                boolean isP1 = p1Character.equals(name);
                boolean isP2 = p2Character.equals(name);

                if (isP1 && isP2) {
                    // 둘 다 선택
                    g2.setStroke(new BasicStroke(4f));
                    g2.setColor(Color.RED);
                    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                    g2.setColor(Color.BLUE);
                    g2.drawRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 11, 11);
                } else if (isP1) {
                    g2.setStroke(new BasicStroke(4f));
                    g2.setColor(Color.RED);
                    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                } else if (isP2) {
                    g2.setStroke(new BasicStroke(4f));
                    g2.setColor(Color.BLUE);
                    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                } else {
                    g2.setStroke(new BasicStroke(1f));
                    g2.setColor(Color.GRAY);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                }
            }
        };
        panel.setBounds(x, y, size, size);
        panel.setOpaque(false);

        // 클릭 리스너
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    p2Character = name;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    p1Character = name;
                }
                updateSelectionUI(); // 화면 갱신
            }
        });

        return panel;
    }

    // 현재 선택된 플레이어 정보 표시 패널 (1P / 2P)
    private JPanel createPlayerDisplayPanel(String playerLabel, int x, int y) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 배경 박스
                g2.setColor(new Color(255, 255, 255, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // 테두리
                g2.setStroke(new BasicStroke(2));
                if (playerLabel.equals("1P"))
                    g2.setColor(Color.RED);
                else
                    g2.setColor(Color.BLUE);
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);

                // 캐릭터 이미지 표시
                String charName = playerLabel.equals("1P") ? p1Character : p2Character;
                Image showImg = null;
                if (charName.equals("배찌"))
                    showImg = bazziImg;
                else if (charName.equals("다오"))
                    showImg = daoImg;

                if (showImg != null) {
                    int imgSize = 100;
                    g2.drawImage(showImg, (getWidth() - imgSize) / 2, 30, imgSize, imgSize, this);
                } else if (charName.equals("랜덤")) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("맑은 고딕", Font.BOLD, 60));
                    g2.drawString("?", 125, 100);
                }

                // 캐릭터 이름 텍스트
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("맑은 고딕", Font.BOLD, 18));
                String displayName = charName;
                int textW = g2.getFontMetrics().stringWidth(displayName);
                g2.drawString(displayName, (getWidth() - textW) / 2, 160);

                // 하단 1P/2P 라벨
                g2.setFont(new Font("Arial", Font.BOLD, 24));
                if (playerLabel.equals("1P"))
                    g2.setColor(Color.RED);
                else
                    g2.setColor(Color.BLUE);
                int labelW = g2.getFontMetrics().stringWidth(playerLabel);
                g2.drawString(playerLabel, (getWidth() - labelW) / 2, 200);
            }
        };
        panel.setBounds(x, y, 300, 220);
        panel.setOpaque(false);
        return panel;
    }

    private void createChatPanel() {
        JPanel chatPanel = new JPanel(null);
        chatPanel.setBounds(150, 480, 500, 100);
        chatPanel.setOpaque(false);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBounds(0, 0, 400, 70);
        chatPanel.add(scroll);

        chatInput = new JTextField();
        chatInput.setBounds(0, 75, 320, 25);
        chatPanel.add(chatInput);

        JButton sendBtn = new JButton("전송");
        sendBtn.setBounds(330, 75, 70, 25);
        sendBtn.addActionListener(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                chatArea.append("User: " + text + "\n");
                chatInput.setText("");
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        });
        chatInput.addActionListener(e -> sendBtn.doClick()); // 엔터키 처리
        chatPanel.add(sendBtn);

        add(chatPanel);
    }

    private JButton createStyledButton(String text, int x, int y, int w, int h, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, w, h);
        btn.setBackground(baseColor);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        return btn;
    }

    private void updateSelectionUI() {
        repaint(); // 전체 다시 그리기 (카드 테두리 및 플레이어 표시 패널 갱신)
    }

    private void loadCharacterImages() {
        try {
            URL bazziUrl = getClass().getResource("/res/배찌.png");
            if (bazziUrl != null)
                bazziImg = ImageIO.read(bazziUrl);
            URL daoUrl = getClass().getResource("/res/다오.png");
            if (daoUrl != null)
                daoImg = ImageIO.read(daoUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomCursor() {
        try {
            URL cursorUrl = getClass().getResource("/res/cursor.png");
            if (cursorUrl != null) {
                Image cursorImg = ImageIO.read(cursorUrl);
                customCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "C");
                setCursor(customCursor);
            }
        } catch (Exception e) {
        }
    }
}
