import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ========================================================
 * 4. 크레딧 패널 (Credits Panel)
 * ========================================================
 * 게임 제작진 정보를 보여주는 크레딧 화면입니다.
 * 텍스트가 아래에서 위로 스크롤되는 애니메이션 효과를 포함합니다.
 */
public class CreditsPanel extends JPanel {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private CrazyArcade_UI mainFrame;

    // 애니메이션 관련 변수
    private Timer scrollTimer; // 일정 간격으로 텍스트 위치 이동
    private int scrollY; // 텍스트 패널의 현재 Y 좌표
    private JPanel textPanel; // 실제로 움직이는 텍스트 컨테이너
    private JPanel scrollContainer; // 텍스트가 보여지는 뷰포트 영역

    private Image backgroundImage; // 배경 이미지

    // 크레딧 내용 (HTML 태그 사용 가능)
    private String creditsText = "<html><center>"
            + "<h1>Water Bomb Man</h1>"
            + "<p style='font-size:12px;'>물풍선 대작전!</p><br><br>"
            + "<h2>[ 개발팀 ]</h2>"
            + "<p><b>캐릭터 시스템</b></p>"
            + "<p>서승하</p>"
            + "<p style='font-size:11px;'>캐릭터 움직임, 히트박스 구현</p><br>"
            + "<p><b>물풍선 시스템</b></p>"
            + "<p>최이삭</p>"
            + "<p style='font-size:11px;'>물풍선 설치, 물줄기 구현</p><br>"
            + "<p><b>맵 & 아이템</b></p>"
            + "<p>장수호</p>"
            + "<p style='font-size:11px;'>맵 디자인, 아이템 시스템</p><br>"
            + "<p><b>UI & 설정</b></p>"
            + "<p>최수인</p>"
            + "<p style='font-size:11px;'>UI 디자인, 설정 저장 시스템</p><br><br>"
            + "<h2>[ 게임 정보 ]</h2>"
            + "<p>해상도: 800 x 600</p>"
            + "<p>맵 비율: 4 : 3</p><br><br>"
            + "<h3>Thank You for Playing!</h3>"
            + "</center></html>";

    /**
     * 생성자: 크레딧 화면 구성 및 타이머 설정
     */
    public CreditsPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null); // 절대 위치 사용

        // [배경 이미지 로드]
        String creditsPath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "creditss.png";
        File creditsFile = new File(creditsPath);
        if (creditsFile.exists()) {
            ImageIcon icon = new ImageIcon(creditsPath);
            backgroundImage = icon.getImage();
        } else {
            System.err.println("CreditsPanel: 이미지 로드 실패 - " + creditsPath);
            backgroundImage = null;
        }

        // [스크롤 영역 구성]
        int viewportHeight = 500; // 스크롤 될 영역의 높이
        scrollContainer = new JPanel();
        scrollContainer.setLayout(null);
        scrollContainer.setBounds(0, 0, PANEL_WIDTH, viewportHeight);
        scrollContainer.setOpaque(false); // 배경 투명하게
        add(scrollContainer);

        // [텍스트 내용 패널]
        textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.setBounds(0, viewportHeight, PANEL_WIDTH, 1000); // 넉넉한 높이 할당

        // HTML 텍스트 라벨 추가
        JLabel label = new JLabel(creditsText);
        label.setForeground(Color.WHITE); // 글자색 흰색
        label.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        textPanel.add(label, BorderLayout.NORTH);

        scrollContainer.add(textPanel);

        // [뒤로 가기 버튼]
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

        // [애니메이션 타이머]
        // 초기 시작 위치 설정
        scrollY = viewportHeight;

        // 50ms마다 실행
        scrollTimer = new Timer(50, e -> {
            scrollY -= 2; // 위로 2픽셀씩 이동
            textPanel.setLocation(0, scrollY);

            // 텍스트가 화면 위로 완전히 사라지면 다시 아래에서 시작 (무한 루프)
            if (scrollY + textPanel.getHeight() < 0) {
                scrollY = viewportHeight;
            }
            repaint();
        });
    }

    /**
     * 배경 이미지 그리기
     */
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

    /**
     * 스크롤 애니메이션 시작 (화면 진입 시 호출)
     */
    public void startScrolling() {
        scrollY = 500; // 위치 초기화
        textPanel.setLocation(0, scrollY);
        scrollTimer.start();
    }

    /**
     * 스크롤 애니메이션 정지 (화면 이탈 시 호출)
     */
    public void stopScrolling() {
        scrollTimer.stop();
    }
}
