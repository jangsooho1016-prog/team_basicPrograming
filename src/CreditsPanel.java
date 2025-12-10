import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ========================================================
 * CreditsPanel 클래스 - 크레딧 화면
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임 제작진을 소개하는 크레딧 화면입니다.
 * - 영화 엔딩 크레딧처럼 텍스트가 아래에서 위로 스크롤됩니다.
 * - 배경 이미지(creditss.png)가 표시됩니다.
 * 
 * [화면 구성]
 * - 배경: creditss.png 이미지 (없으면 검정 배경)
 * - 중앙: 스크롤되는 크레딧 텍스트 (HTML 형식)
 * - 하단: 홈으로 돌아가기 버튼
 * 
 * [애니메이션]
 * - Timer를 사용하여 50ms마다 텍스트를 2픽셀씩 위로 이동
 * - 텍스트가 화면 위로 완전히 벗어나면 다시 아래에서 시작
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class CreditsPanel extends JPanel {

    // ============================================
    // 상수 정의
    // ============================================

    /** 패널 너비 (픽셀) */
    private static final int PANEL_WIDTH = 800;

    /** 패널 높이 (픽셀) */
    private static final int PANEL_HEIGHT = 600;

    // ============================================
    // 멤버 변수 (필드)
    // ============================================

    /** 메인 프레임 참조 (화면 전환에 사용) */
    private CrazyArcade_UI mainFrame;

    /** 스크롤 애니메이션을 위한 타이머 */
    private Timer scrollTimer;

    /** 현재 텍스트 Y 좌표 (스크롤 위치) */
    private int scrollY;

    /** 크레딧 텍스트를 담는 패널 */
    private JPanel textPanel;

    /** 스크롤 영역을 담는 컨테이너 */
    private JPanel scrollContainer;

    /** 배경 이미지 */
    private Image backgroundImage;

    /**
     * 크레딧에 표시할 텍스트 (HTML 형식)
     * HTML을 사용하면 다양한 스타일링이 가능합니다.
     * <h1>,
     * <h2>,
     * <p>
     * 태그로 제목과 내용을 구분합니다.
     */
    private String creditsText = "<html><center>"
            + "<h1>Water Bomb Man</h1><br><br>"
            + "<h2>[ 개발팀 ]</h2>"
            + "<p>총괄 디렉터: 장수호</p>"
            + "<p>메인 프로그래머: 홍길동</p>"
            + "<p>UI/UX 디자인: 김철수</p><br>"
            + "<h2>[ 아트 & 사운드 ]</h2>"
            + "<p>캐릭터 디자인: 서승하</p>"
            + "<p>배경 및 이펙트: 이영희</p>"
            + "<p>사운드 디자인: 박민수</p><br>"
            + "<h2>[ 스페셜 땡스 ]</h2>"
            + "<p>물풍선 아이디어: 최이삭</p>"
            + "<p>QA 테스터: 팀원 전원</p><br><br>"
            + "<h3>Thank You for Playing!</h3>"
            + "</center></html>";

    /**
     * ============================================
     * 생성자 - 크레딧 패널 초기화
     * ============================================
     * 
     * [수행 작업]
     * 1. 배경 이미지 로드
     * 2. 스크롤 컨테이너 설정
     * 3. 크레딧 텍스트 패널 생성
     * 4. 홈으로 버튼 생성
     * 5. 스크롤 타이머 설정
     * 
     * @param mainFrame 메인 프레임 참조 (화면 전환에 사용)
     */
    public CreditsPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;

        // ----- 패널 기본 설정 -----
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null); // 절대 좌표 레이아웃

        // ----- 배경 이미지 로드 (파일 시스템 경로 사용) -----
        String imagePath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "creditss.png";
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            backgroundImage = icon.getImage();
            System.out.println("크레딧 배경 이미지 로드 성공: " + imagePath);
        } else {
            // 이미지 로드 실패 시 디버깅 메시지 출력
            System.err.println("CreditsPanel: 이미지를 찾을 수 없습니다: " + imagePath);
            backgroundImage = null; // paintComponent에서 검정 배경으로 대체
        }

        // ===== 스크롤 컨테이너 설정 =====
        // 스크롤 영역의 높이 (하단 버튼 영역 제외)
        int viewportHeight = 500;

        scrollContainer = new JPanel();
        scrollContainer.setLayout(null);
        scrollContainer.setBounds(0, 0, PANEL_WIDTH, viewportHeight);
        scrollContainer.setOpaque(false); // 투명 (배경 이미지 보이도록)
        add(scrollContainer);

        // ===== 크레딧 텍스트 패널 =====
        textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setOpaque(false); // 투명
        // 초기 위치: 화면 아래 (viewportHeight)에서 시작
        textPanel.setBounds(0, viewportHeight, PANEL_WIDTH, 1000);

        // 크레딧 텍스트 라벨 생성
        JLabel label = new JLabel(creditsText);
        label.setForeground(Color.WHITE); // 흰색 텍스트
        label.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER); // 중앙 정렬
        textPanel.add(label, BorderLayout.NORTH);

        scrollContainer.add(textPanel);

        // ===== 홈으로 버튼 =====
        JButton backBtn = new JButton("홈으로") {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // 테두리
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

        // ===== 스크롤 타이머 설정 =====
        // 스크롤 시작 위치 초기화
        scrollY = viewportHeight;

        // 50ms마다 텍스트를 2픽셀 위로 이동
        scrollTimer = new Timer(50, e -> {
            scrollY -= 2; // 위로 이동
            textPanel.setLocation(0, scrollY); // 위치 업데이트

            // 텍스트가 완전히 위로 벗어나면 다시 아래에서 시작
            if (scrollY + textPanel.getHeight() < 0) {
                scrollY = viewportHeight;
            }
            repaint(); // 화면 갱신
        });
    }

    /**
     * ============================================
     * paintComponent() - 화면 그리기
     * ============================================
     * 
     * [설명]
     * - 배경 이미지를 그립니다.
     * - 이미지가 없으면 검정 배경을 표시합니다.
     * 
     * @param g Graphics 객체 (그리기 도구)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            // 배경 이미지를 패널 전체 크기에 맞게 그리기
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // 이미지 없으면 검정 배경
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * ============================================
     * startScrolling() - 스크롤 애니메이션 시작
     * ============================================
     * 
     * [설명]
     * - 크레딧 화면으로 전환될 때 호출됩니다.
     * - 텍스트 위치를 초기화하고 스크롤 타이머를 시작합니다.
     * 
     * [호출 시점]
     * - CrazyArcade_UI.showPanel(PANEL_CREDITS) 호출 시
     */
    public void startScrolling() {
        scrollY = 500; // 시작 위치 초기화
        textPanel.setLocation(0, scrollY); // 텍스트 위치 설정
        scrollTimer.start(); // 타이머 시작
    }

    /**
     * ============================================
     * stopScrolling() - 스크롤 애니메이션 중지
     * ============================================
     * 
     * [설명]
     * - 크레딧 화면에서 벗어날 때 호출됩니다.
     * - 스크롤 타이머를 중지하여 리소스를 절약합니다.
     * 
     * [호출 시점]
     * - 다른 패널로 전환될 때 자동 호출
     */
    public void stopScrolling() {
        scrollTimer.stop();
    }
}
