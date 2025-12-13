import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * ========================================================
 * 2. 대기실 (Lobby) 화면 패널
 * ========================================================
 * 게임 시작 전 캐릭터와 맵을 선택하는 로비 화면입니다.
 * 
 * 화면 구성:
 * - 왼쪽 상단: 1P/2P 선택된 캐릭터 표시
 * - 왼쪽 하단: 채팅창
 * - 오른쪽 상단: 캐릭터 선택 (배찌, 다오, 랜덤)
 * - 오른쪽 하단: 맵 선택 및 게임 시작/메인으로 버튼
 */
public class LobbyPanel extends JPanel {
    private CrazyArcade_UI mainFrame;
    private String p1Character = "배찌"; // 기본값
    private String p2Character = "디지니"; // 기본값
    private Cursor customCursor;

    // 캐릭터 이미지
    private Image bazziImg;
    private Image daoImg;

    // UI 컴포넌트
    private JTextArea chatArea;
    private JTextField chatInput;

    // 선택 카드 (피드백용)
    private JPanel cardBazzi, cardDao, cardRandom;

    // 맵 선택
    private String selectedMap = "Map1"; // 기본값
    private Image map1Img, map2Img;
    private JPanel mapCard1, mapCard2;

    public LobbyPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setBackground(new Color(240, 240, 255)); // 밝은 배경색
        setPreferredSize(new Dimension(800, 600));

        loadCharacterImages();
        loadCustomCursor();

        // 1. 상단 타이틀
        JLabel titleLabel = new JLabel("Game Lobby");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        titleLabel.setBounds(30, 15, 300, 40);
        add(titleLabel);

        // ========== 왼쪽 영역 (1P/2P 표시 + 채팅) ==========

        // 왼쪽 상단: 1P/2P 캐릭터 표시 박스 (크레이지 아케이드 스타일)
        JPanel leftTopBox = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 50, 50, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(100, 180, 255));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        leftTopBox.setBounds(30, 60, 370, 200);
        leftTopBox.setOpaque(false);
        add(leftTopBox);

        // 1P 표시 (왼쪽 상단 박스 내부 왼쪽)
        JPanel p1Panel = createPlayerDisplayPanel("1P", 15, 15, 165, 170);
        leftTopBox.add(p1Panel);

        // 2P 표시 (왼쪽 상단 박스 내부 오른쪽)
        JPanel p2Panel = createPlayerDisplayPanel("2P", 190, 15, 165, 170);
        leftTopBox.add(p2Panel);

        // 왼쪽 하단: 채팅 영역 (크레이지 아케이드 스타일)
        JPanel chatBox = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 50, 50, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(100, 180, 255));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        chatBox.setBounds(30, 275, 370, 280);
        chatBox.setOpaque(false);
        add(chatBox);

        // 채팅 라벨
        JLabel chatLabel = new JLabel("채팅");
        chatLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        chatLabel.setForeground(Color.WHITE);
        chatLabel.setBounds(15, 10, 100, 25);
        chatBox.add(chatLabel);

        // 채팅 영역 구성
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBounds(15, 40, 340, 190);
        chatBox.add(scroll);

        chatInput = new JTextField();
        chatInput.setBounds(15, 240, 250, 28);
        chatBox.add(chatInput);

        JButton sendBtn = new JButton("전송");
        sendBtn.setBounds(275, 240, 70, 28);
        sendBtn.addActionListener(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                chatArea.append("User: " + text + "\n");
                chatInput.setText("");
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        });
        chatInput.addActionListener(e -> sendBtn.doClick()); // 엔터키 처리
        chatBox.add(sendBtn);

        // ========== 오른쪽 영역 (캐릭터 선택 + 버튼) ==========

        // 오른쪽 상단: 캐릭터 선택 영역
        JPanel rightTopBox = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 50, 50, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        rightTopBox.setBounds(420, 60, 350, 250);
        rightTopBox.setOpaque(false);
        add(rightTopBox);

        // 캐릭터 선택 라벨
        JLabel selectLabel = new JLabel("캐릭터 선택");
        selectLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        selectLabel.setForeground(Color.WHITE);
        selectLabel.setBounds(120, 15, 150, 25);
        rightTopBox.add(selectLabel);

        // 조작 안내
        JLabel hintLabel = new JLabel("(우클릭: 1P, 좌클릭: 2P)");
        hintLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        hintLabel.setForeground(Color.LIGHT_GRAY);
        hintLabel.setBounds(100, 42, 200, 20);
        rightTopBox.add(hintLabel);

        // 캐릭터 카드들 (배찌, 다오, 랜덤)
        int cardSize = 80;
        int cardY = 75;
        int gap = 25;
        int startX = (350 - (cardSize * 3 + gap * 2)) / 2;

        cardBazzi = createCharacterCard("배찌", bazziImg, startX, cardY, cardSize);
        rightTopBox.add(cardBazzi);

        cardDao = createCharacterCard("디지니", daoImg, startX + cardSize + gap, cardY, cardSize);
        rightTopBox.add(cardDao);

        cardRandom = createCharacterCard("랜덤", null, startX + (cardSize + gap) * 2, cardY, cardSize);
        rightTopBox.add(cardRandom);

        // 캐릭터 이름 라벨들
        JLabel bazziLabel = new JLabel("배찌", SwingConstants.CENTER);
        bazziLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        bazziLabel.setForeground(Color.WHITE);
        bazziLabel.setBounds(startX, cardY + cardSize + 5, cardSize, 20);
        rightTopBox.add(bazziLabel);

        JLabel daoLabel = new JLabel("디지니", SwingConstants.CENTER);
        daoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        daoLabel.setForeground(Color.WHITE);
        daoLabel.setBounds(startX + cardSize + gap, cardY + cardSize + 5, cardSize, 20);
        rightTopBox.add(daoLabel);

        JLabel randomLabel = new JLabel("랜덤", SwingConstants.CENTER);
        randomLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        randomLabel.setForeground(Color.WHITE);
        randomLabel.setBounds(startX + (cardSize + gap) * 2, cardY + cardSize + 5, cardSize, 20);
        rightTopBox.add(randomLabel);

        // 현재 선택 상태 표시
        JLabel statusLabel = new JLabel("1P: 배찌 / 2P: 다오", SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        statusLabel.setForeground(Color.YELLOW);
        statusLabel.setBounds(75, 200, 200, 20);
        statusLabel.setName("statusLabel");
        rightTopBox.add(statusLabel);

        // 오른쪽 하단: 맵 선택 + 버튼 영역
        JPanel rightBottomBox = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 50, 50, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        rightBottomBox.setBounds(420, 325, 350, 230);
        rightBottomBox.setOpaque(false);
        add(rightBottomBox);

        // 맵 선택 라벨
        JLabel mapSelectLabel = new JLabel("맵 선택", SwingConstants.CENTER);
        mapSelectLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        mapSelectLabel.setForeground(Color.WHITE);
        mapSelectLabel.setBounds(0, 10, 350, 20);
        rightBottomBox.add(mapSelectLabel);

        // 맵 이미지 로드
        loadMapImages();

        // 맵 선택 카드들
        int mapCardWidth = 100;
        int mapCardHeight = 70;
        int mapGap = 30;
        int mapStartX = (350 - (mapCardWidth * 2 + mapGap)) / 2;

        mapCard1 = createMapCard("Map1", map1Img, mapStartX, 35, mapCardWidth, mapCardHeight);
        rightBottomBox.add(mapCard1);

        mapCard2 = createMapCard("Map2", map2Img, mapStartX + mapCardWidth + mapGap, 35, mapCardWidth, mapCardHeight);
        rightBottomBox.add(mapCard2);

        // 맵 이름 라벨
        JLabel map1Label = new JLabel("Map 1", SwingConstants.CENTER);
        map1Label.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        map1Label.setForeground(Color.WHITE);
        map1Label.setBounds(mapStartX, 35 + mapCardHeight + 3, mapCardWidth, 15);
        rightBottomBox.add(map1Label);

        JLabel map2Label = new JLabel("Map 2", SwingConstants.CENTER);
        map2Label.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        map2Label.setForeground(Color.WHITE);
        map2Label.setBounds(mapStartX + mapCardWidth + mapGap, 35 + mapCardHeight + 3, mapCardWidth, 15);
        rightBottomBox.add(map2Label);

        // 게임 시작 버튼
        JButton startBtn = createStyledButton("게임 시작", 75, 130, 200, 45, new Color(255, 200, 0));
        startBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_GAME));
        rightBottomBox.add(startBtn);

        // 메인으로 버튼
        JButton backBtn = createStyledButton("메인으로", 75, 182, 200, 38, new Color(200, 200, 200));
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        rightBottomBox.add(backBtn);

        // 초기 UI 갱신 (선택 테두리 등)
        updateSelectionUI();
    }

    /**
     * 캐릭터 선택 카드 생성 메서드
     * 캐릭터 이미지와 선택 상태에 따른 테두리를 그립니다.
     * 
     * @param name 캐릭터 이름 (배찌, 다오, 랜덤)
     * @param img  캐릭터 이미지 (랜덤일 경우 null)
     * @param x    패널 X 좌표
     * @param y    패널 Y 좌표
     * @param size 패널 크기 (정사각형)
     * @return 생성된 JPanel 객체
     */
    private JPanel createCharacterCard(String name, Image img, int x, int y, int size) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 카드 배경 (흰색 둥근 사각형)
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // 이미지 그리기
                if (img != null) {
                    g2.drawImage(img, 8, 8, size - 16, size - 16, this);
                } else {
                    // 이미지가 없는 경우 (랜덤 선택) 물음표 표시
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("맑은 고딕", Font.BOLD, 40));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth("?")) / 2;
                    g2.drawString("?", textX, 55);
                }

                // [선택 상태 표시 테두리]
                // 1P는 빨간색, 2P는 파란색 테두리로 표시합니다.
                // 만약 1P와 2P가 같은 캐릭터를 선택했다면 겹쳐서 표시합니다.
                boolean isP1 = p1Character.equals(name);
                boolean isP2 = p2Character.equals(name);

                if (isP1 && isP2) {
                    // 둘 다 선택한 경우: 빨간색 바깥 테두리 + 파란색 안쪽 테두리
                    g2.setStroke(new BasicStroke(3f));
                    g2.setColor(Color.RED);
                    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
                    g2.setColor(Color.BLUE);
                    g2.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                } else if (isP1) {
                    // 1P만 선택: 빨간색 테두리
                    g2.setStroke(new BasicStroke(3f));
                    g2.setColor(Color.RED);
                    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
                } else if (isP2) {
                    // 2P만 선택: 파란색 테두리
                    g2.setStroke(new BasicStroke(3f));
                    g2.setColor(Color.BLUE);
                    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
                } else {
                    // 선택되지 않음: 회색 얇은 테두리
                    g2.setStroke(new BasicStroke(1f));
                    g2.setColor(Color.GRAY);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
            }
        };
        panel.setBounds(x, y, size, size);
        panel.setOpaque(false);

        // [마우스 클릭 이벤트 처리]
        // 왼쪽 클릭: 2P 캐릭터 변경
        // 오른쪽 클릭: 1P 캐릭터 변경
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    p2Character = name; // 2P 선택
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    p1Character = name; // 1P 선택
                }
                updateSelectionUI(); // 화면 갱신 (테두리 다시 그리기)
            }
        });

        return panel;
    }

    // 현재 선택된 플레이어 정보 표시 패널 (1P / 2P) + 캐릭터 능력치 표시
    private JPanel createPlayerDisplayPanel(String playerLabel, int x, int y, int width, int height) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 배경 박스
                g2.setColor(new Color(245, 245, 250));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 테두리
                g2.setStroke(new BasicStroke(2));
                if (playerLabel.equals("1P"))
                    g2.setColor(new Color(220, 80, 80));
                else
                    g2.setColor(new Color(80, 80, 220));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);

                // 상단 플레이어 라벨
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                if (playerLabel.equals("1P"))
                    g2.setColor(new Color(220, 80, 80));
                else
                    g2.setColor(new Color(80, 80, 220));
                int labelW = g2.getFontMetrics().stringWidth(playerLabel);
                g2.drawString(playerLabel, (getWidth() - labelW) / 2, 18);

                // 캐릭터 이미지 표시
                String charName = playerLabel.equals("1P") ? p1Character : p2Character;
                Image showImg = null;
                if (charName.equals("배찌"))
                    showImg = bazziImg;
                else if (charName.equals("디지니"))
                    showImg = daoImg;

                if (showImg != null) {
                    int imgSize = 50;
                    g2.drawImage(showImg, 10, 25, imgSize, imgSize, this);
                } else if (charName.equals("랜덤")) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("맑은 고딕", Font.BOLD, 30));
                    g2.drawString("?", 28, 60);
                }

                // 캐릭터 이름
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
                g2.drawString(charName, 65, 40);

                // 캐릭터 설명 (작은 글씨)
                g2.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
                g2.setColor(Color.GRAY);
                if (charName.equals("배찌")) {
                    g2.drawString("강점: Speed", 65, 52);
                    g2.drawString("빠른 속도의 캐릭터", 65, 63);
                } else if (charName.equals("디지니")) {
                    g2.drawString("강점: 2개의 물풍선", 65, 52);
                    g2.drawString("초반이 강한 캐릭터", 65, 63);
                } else {
                    g2.drawString("랜덤 선택", 65, 52);
                }

                // 능력치 게이지 표시
                int gaugeX = 10;
                int gaugeY = 85;
                int gaugeWidth = 100;
                int gaugeHeight = 12;
                int gap = 5;

                // 능력치 값 (캐릭터별)
                int bombCount, waterLength, speed;

                if (charName.equals("배찌")) {
                    bombCount = 1;  // 물풍선 개수
                    waterLength = 1; // 물줄기 길이
                    speed = 4;       // 속도
                } else if (charName.equals("디지니")) {
                    bombCount = 2;  // 물풍선 개수
                    waterLength = 1; // 물줄기 길이
                    speed = 4;       // 속도
                } else { // 랜덤
                    bombCount = 5;
                    waterLength = 5;
                    speed = 5;
                }

                // 스탯 바 그리기 (최대값 10 기준)
                drawStatBar(g2, "개수", gaugeX, gaugeY, gaugeWidth, gaugeHeight, bombCount, new Color(255, 180, 50));
                drawStatBar(g2, "물줄기", gaugeX, gaugeY + (gaugeHeight + gap), gaugeWidth, gaugeHeight, waterLength, new Color(100, 200, 100));
                drawStatBar(g2, "속도", gaugeX, gaugeY + (gaugeHeight + gap) * 2, gaugeWidth, gaugeHeight, speed, new Color(100, 150, 255));
                }

            // 능력치 바 그리기 헬퍼 메서드
            private void drawStatBar(Graphics2D g2, String label, int x, int y, int width, int height, int value, Color color) {
                // 레이블 그리기
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("맑은 고딕", Font.BOLD, 10));
                g2.drawString(label, x, y + height - 2);
            
                // 바 영역
                int barX = x + 30;
                int barWidth = width;
                int cellWidth = barWidth / 10; // 10칸으로 나눔
                int maxCells = 10; // 최대 10칸
            
                for (int i = 0; i < maxCells; i++) {
                    int cellX = barX + i * (cellWidth + 2);
            
                    if (i < value) {
                        // 채워진 칸
                        g2.setColor(color);
                        g2.fillRect(cellX, y, cellWidth, height);
                    } else {
                        // 빈 칸
                        g2.setColor(new Color(220, 220, 220));
                        g2.fillRect(cellX, y, cellWidth, height);
                    }
            
                    // 테두리
                    g2.setColor(new Color(180, 180, 180));
                    g2.drawRect(cellX, y, cellWidth, height);
                }
            }
        };
        panel.setBounds(x, y, width, height);
        panel.setOpaque(false);
        return panel;
    }

    private JButton createStyledButton(String text, int x, int y, int w, int h, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed())
                    g2.setColor(baseColor.darker());
                else if (getModel().isRollover())
                    g2.setColor(baseColor.brighter());
                else
                    g2.setColor(baseColor);

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);

                super.paintComponent(g);
            }
        };
        btn.setBounds(x, y, w, h);
        btn.setBackground(baseColor);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btn.setForeground(Color.DARK_GRAY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private void updateSelectionUI() {
        // 상태 라벨 업데이트
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                updateStatusLabelInPanel((JPanel) comp);
            }
        }
        repaint();
    }

    private void updateStatusLabelInPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel && "statusLabel".equals(comp.getName())) {
                ((JLabel) comp).setText("1P: " + p1Character + " / 2P: " + p2Character);
            } else if (comp instanceof JPanel) {
                updateStatusLabelInPanel((JPanel) comp);
            }
        }
    }

    private void loadCharacterImages() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;
            File bazziFile = new File(basePath + "배찌.png");
            if (bazziFile.exists())
                bazziImg = ImageIO.read(bazziFile);
            File daoFile = new File(basePath + "디지니.png");
            if (daoFile.exists())
                daoImg = ImageIO.read(daoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomCursor() {
        try {
            String cursorPath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "cursor.png";
            File cursorFile = new File(cursorPath);
            if (cursorFile.exists()) {
                Image cursorImg = ImageIO.read(cursorFile);
                customCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "C");
                setCursor(customCursor);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 맵 이미지 로드
     * res/ 폴더에서 forest24.png (Map1)과 map2.png (Map2)를 로드합니다.
     */
    private void loadMapImages() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;

            // Map1: forest24.png (숲 테마)
            File map1File = new File(basePath + "forest24.png");
            if (map1File.exists()) {
                map1Img = ImageIO.read(map1File);
                System.out.println("Map1 이미지 로드 성공: " + map1File.getPath());
            }

            // Map2: map2.png (기본 맵)
            File map2File = new File(basePath + "map2.png");
            if (map2File.exists()) {
                map2Img = ImageIO.read(map2File);
                System.out.println("Map2 이미지 로드 성공: " + map2File.getPath());
            }
        } catch (IOException e) {
            System.err.println("맵 이미지 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JPanel createMapCard(String mapName, Image img, int x, int y, int width, int height) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 배경
                g2.setColor(Color.DARK_GRAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // 이미지
                if (img != null) {
                    g2.drawImage(img, 4, 4, width - 8, height - 8, this);
                } else {
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    String text = mapName;
                    int textX = (getWidth() - fm.stringWidth(text)) / 2;
                    g2.drawString(text, textX, getHeight() / 2 + 5);
                }

                // 테두리 (선택 상태에 따라 변경)
                boolean isSelected = selectedMap.equals(mapName);
                if (isSelected) {
                    g2.setStroke(new BasicStroke(3f));
                    g2.setColor(new Color(255, 200, 0)); // 노란색 테두리
                    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
                } else {
                    g2.setStroke(new BasicStroke(1f));
                    g2.setColor(Color.GRAY);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
            }
        };
        panel.setBounds(x, y, width, height);
        panel.setOpaque(false);

        // 클릭 리스너
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedMap = mapName;
                repaint();
            }
        });

        return panel;
    }

    // 선택된 맵 이름 반환 (게임 시작 시 사용)
    public String getSelectedMap() {
        return selectedMap;
    }

    /**
     * 1P 캐릭터 반환 (랜덤 선택 시 실제 캐릭터로 변환)
     */
    public String getP1Character() {
        if ("랜덤".equals(p1Character)) {
            return Math.random() < 0.5 ? "배찌" : "디지니";
        }
        return p1Character;
    }

    /**
     * 2P 캐릭터 반환 (랜덤 선택 시 실제 캐릭터로 변환)
     */
    public String getP2Character() {
        if ("랜덤".equals(p2Character)) {
            return Math.random() < 0.5 ? "배찌" : "디지니";
        }
        return p2Character;
    }
}
