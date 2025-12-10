import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * ========================================================
 * LobbyPanel 클래스 - 게임 로비 화면
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임 시작 전 대기실 화면입니다.
 * - 캐릭터 선택, 맵 정보 확인, 채팅 기능을 제공합니다.
 * 
 * [화면 구성]
 * - 좌측: 캐릭터 선택 패널 (배찌, 다오)
 * - 우측 상단: 맵 정보 패널
 * - 우측 하단: 채팅 패널 (실시간 채팅)
 * - 하단: 뒤로가기, 게임 시작 버튼
 * 
 * [화면 흐름]
 * - 뒤로 → MenuPanel (메뉴)
 * - 게임 시작 → GamePanelPlaceholder (게임)
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class LobbyPanel extends JPanel {

    // ============================================
    // 멤버 변수 (필드)
    // ============================================

    /** 메인 프레임 참조 (화면 전환에 사용) */
    private CrazyArcade_UI mainFrame;

    /** 현재 선택된 캐릭터 이름 (기본: 배찌) */
    private String selectedCharacter = "배찌";

    /**
     * ============================================
     * 생성자 - 로비 패널 초기화
     * ============================================
     * 
     * [수행 작업]
     * 1. 타이틀 라벨 생성
     * 2. 캐릭터 선택 패널 생성
     * 3. 맵 정보 패널 생성
     * 4. 채팅 패널 생성
     * 5. 하단 버튼 생성 (뒤로, 게임 시작)
     * 
     * @param mainFrame 메인 프레임 참조 (화면 전환에 사용)
     */
    public LobbyPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;

        // ----- 패널 기본 설정 -----
        setLayout(null); // 절대 좌표 레이아웃 사용
        setBackground(ThemeColors.BG); // 바나나 테마 배경색

        // ===== 1. 타이틀 라벨 =====
        JLabel titleLabel = new JLabel("게임 로비 / Game Lobby");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        titleLabel.setForeground(ThemeColors.DARK);
        titleLabel.setBounds(30, 20, 500, 40);
        add(titleLabel);

        // ===== 2. 캐릭터 선택 패널 =====
        JPanel charPanel = createPanel("캐릭터 선택", 30, 80, 250, 400);

        // 배찌 캐릭터 카드 생성
        JPanel bazziCard = createCharacterCard("배찌", "배찌.png");
        bazziCard.setBounds(15, 40, 220, 160);
        charPanel.add(bazziCard);

        // 다오 캐릭터 카드 생성
        JPanel daoCard = createCharacterCard("다오", "다오.png");
        daoCard.setBounds(15, 210, 220, 160);
        charPanel.add(daoCard);

        // ----- 캐릭터 선택 이벤트 처리 -----
        // 상호 배타적 선택: 하나를 선택하면 다른 것은 해제
        bazziCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedCharacter = "배찌";
                // 선택된 카드에 강조 테두리, 미선택 카드에 일반 테두리
                bazziCard.setBorder(BorderFactory.createLineBorder(ThemeColors.ACCENT, 4));
                daoCard.setBorder(BorderFactory.createLineBorder(ThemeColors.DARK, 2));
            }
        });

        daoCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedCharacter = "다오";
                // 선택된 카드에 강조 테두리, 미선택 카드에 일반 테두리
                daoCard.setBorder(BorderFactory.createLineBorder(ThemeColors.ACCENT, 4));
                bazziCard.setBorder(BorderFactory.createLineBorder(ThemeColors.DARK, 2));
            }
        });

        // 기본 선택: 배찌 (강조 테두리 적용)
        bazziCard.setBorder(BorderFactory.createLineBorder(ThemeColors.ACCENT, 4));

        add(charPanel);

        // ===== 3. 맵 정보 패널 =====
        JPanel mapPanel = createPanel("맵 정보", 300, 80, 450, 200);

        JLabel mapText = new JLabel("맵: 숲속마을 01");
        mapText.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        mapText.setForeground(ThemeColors.TEXT);
        mapText.setBounds(20, 80, 300, 30);
        mapPanel.add(mapText);

        add(mapPanel);

        // ===== 4. 채팅 패널 =====
        JPanel chatPanel = createPanel("채팅", 300, 300, 450, 180);

        // ----- 채팅 메시지 표시 영역 -----
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false); // 읽기 전용 (직접 입력 불가)
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        chatArea.setLineWrap(true); // 자동 줄바꿈
        chatArea.setWrapStyleWord(true); // 단어 단위로 줄바꿈
        chatArea.setBackground(new Color(255, 255, 250));

        // 스크롤 가능하도록 JScrollPane으로 감싸기
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBounds(10, 30, 430, 100);
        chatScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.DARK, 1));
        chatPanel.add(chatScroll);

        // ----- 채팅 입력 필드 -----
        JTextField inputField = new JTextField();
        inputField.setBounds(10, 140, 350, 30);
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        chatPanel.add(inputField);

        // ----- 전송 버튼 -----
        JButton sendBtn = createThemedButton("전송", 370, 140, 70, 30);
        sendBtn.addActionListener(e -> {
            String msg = inputField.getText().trim(); // 입력 텍스트 (공백 제거)
            if (!msg.isEmpty()) {
                // 채팅 영역에 메시지 추가 (캐릭터이름: 메시지 형식)
                chatArea.append(selectedCharacter + ": " + msg + "\n");
                inputField.setText(""); // 입력 필드 초기화
                // 스크롤을 맨 아래로 이동 (최신 메시지 보이도록)
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        });

        // 엔터키로도 메시지 전송 가능
        inputField.addActionListener(e -> sendBtn.doClick());

        chatPanel.add(sendBtn);
        add(chatPanel);

        // ===== 5. 하단 버튼 =====

        // 뒤로가기 버튼 → 메뉴 화면으로 이동
        JButton backBtn = createThemedButton("뒤로", 30, 500, 150, 50);
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        add(backBtn);

        // 게임 시작 버튼 → 게임 화면으로 이동
        JButton startBtn = createStartButton("게임 시작!");
        startBtn.setBounds(600, 500, 150, 50);
        startBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_GAME));
        add(startBtn);
    }

    /**
     * ============================================
     * createThemedButton() - 테마 버튼 생성
     * ============================================
     * 
     * [설명]
     * - 바나나 테마가 적용된 버튼을 생성합니다.
     * - 일반 크기의 버튼에 사용됩니다.
     * 
     * @param text 버튼 텍스트
     * @param x    X 좌표
     * @param y    Y 좌표
     * @param w    너비
     * @param h    높이
     * @return 생성된 JButton 객체
     */
    private JButton createThemedButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 마우스 상태에 따른 색상
                if (getModel().isPressed())
                    g2.setColor(ThemeColors.ACCENT);
                else if (getModel().isRollover())
                    g2.setColor(ThemeColors.HIGHLIGHT);
                else
                    g2.setColor(ThemeColors.MAIN);

                // 둥근 사각형 배경
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 테두리
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

    /**
     * ============================================
     * createStartButton() - 게임 시작 버튼 생성
     * ============================================
     * 
     * [설명]
     * - 게임 시작 버튼은 특별히 눈에 띄는 주황색 테마를 사용합니다.
     * - 플레이어가 쉽게 찾을 수 있도록 강조됩니다.
     * 
     * @param text 버튼 텍스트
     * @return 생성된 JButton 객체
     */
    private JButton createStartButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 마우스 상태에 따른 주황색 계열 색상
                if (getModel().isPressed())
                    g2.setColor(new Color(255, 120, 0)); // 진한 주황 (클릭)
                else if (getModel().isRollover())
                    g2.setColor(ThemeColors.ACCENT); // 강조색 (호버)
                else
                    g2.setColor(new Color(255, 160, 0)); // 주황색 (일반)

                // 둥근 사각형 배경 (모서리 반지름 더 크게)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // 두꺼운 테두리
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

    /**
     * ============================================
     * createCharacterCard() - 캐릭터 카드 패널 생성
     * ============================================
     * 
     * [설명]
     * - 캐릭터 이미지와 이름을 표시하는 카드 형태의 패널을 생성합니다.
     * - 클릭하여 캐릭터를 선택할 수 있습니다.
     * 
     * [이미지 경로]
     * - 프로젝트폴더/res/캐릭터이름.png
     * 
     * @param name          캐릭터 이름 (예: "배찌", "다오")
     * @param imageFileName 이미지 파일명 (예: "배찌.png")
     * @return 생성된 캐릭터 카드 JPanel 객체
     */
    private JPanel createCharacterCard(String name, String imageFileName) {
        // 익명 클래스로 JPanel 상속하여 커스텀 그리기
        JPanel card = new JPanel() {
            private Image charImage; // 캐릭터 이미지

            // 인스턴스 초기화 블록 (생성자와 비슷한 역할)
            {
                // 이미지 파일 로드 (파일 시스템 경로 사용)
                String imagePath = System.getProperty("user.dir") + File.separator + "res" + File.separator
                        + imageFileName;
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    charImage = new ImageIcon(imagePath).getImage();
                    System.out.println("캐릭터 이미지 로드 성공: " + imagePath);
                } else {
                    System.err.println("캐릭터 이미지를 찾을 수 없습니다: " + imagePath);
                    charImage = null;
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // ----- 카드 배경 -----
                g2.setColor(new Color(255, 255, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // ----- 캐릭터 이미지 표시 -----
                if (charImage != null) {
                    int imgSize = 100; // 이미지 크기
                    int x = (getWidth() - imgSize) / 2; // 중앙 정렬
                    g2.drawImage(charImage, x, 10, imgSize, imgSize, this);
                }

                // ----- 캐릭터 이름 표시 -----
                g2.setColor(ThemeColors.DARK);
                g2.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(name)) / 2; // 중앙 정렬
                g2.drawString(name, textX, getHeight() - 20);
            }
        };
        card.setOpaque(false); // 투명 배경 (커스텀 배경 사용)
        card.setBorder(BorderFactory.createLineBorder(ThemeColors.DARK, 2)); // 기본 테두리
        card.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 마우스 커서: 손가락 모양
        return card;
    }

    /**
     * ============================================
     * createPanel() - 제목이 있는 패널 생성
     * ============================================
     * 
     * [설명]
     * - 둥근 모서리와 제목이 있는 컨테이너 패널을 생성합니다.
     * - 캐릭터 선택, 맵 정보, 채팅 영역 등에 사용됩니다.
     * 
     * @param title 패널 제목
     * @param x     X 좌표
     * @param y     Y 좌표
     * @param w     너비
     * @param h     높이
     * @return 생성된 JPanel 객체
     */
    private JPanel createPanel(String title, int x, int y, int w, int h) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 둥근 사각형 배경 (아이보리색)
                g2.setColor(new Color(255, 255, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // 테두리 (갈색)
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        p.setLayout(null); // 절대 좌표 레이아웃
        p.setBounds(x, y, w, h); // 위치와 크기 설정
        p.setOpaque(false); // 투명 배경

        // ----- 타이틀 라벨 -----
        JLabel l = new JLabel(title);
        l.setBounds(10, 8, 200, 20);
        l.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        l.setForeground(ThemeColors.DARK);
        p.add(l);

        return p;
    }
}
