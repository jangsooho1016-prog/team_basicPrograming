import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ========================================================
 * GuidePanel 클래스 - 게임 가이드 화면
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임 조작법과 규칙을 설명하는 화면입니다.
 * - 가이드 이미지(game play.png)를 표시합니다.
 * - 이미지가 없으면 오류 메시지를 표시합니다.
 * 
 * [화면 구성]
 * - 중앙: 가이드 이미지 (비율 유지하며 확대/축소)
 * - 하단: 홈으로 돌아가기 버튼
 * 
 * [화면 흐름]
 * - 홈으로 → MenuPanel (메뉴)
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class GuidePanel extends JPanel {

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

    /** 가이드 이미지 */
    private Image guideImage;

    /** 메인 프레임 참조 (화면 전환에 사용) */
    private CrazyArcade_UI mainFrame;

    /**
     * ============================================
     * 생성자 - 가이드 패널 초기화
     * ============================================
     * 
     * [수행 작업]
     * 1. 패널 기본 설정
     * 2. 가이드 이미지 로드
     * 3. 홈으로 돌아가기 버튼 생성
     * 
     * @param mainFrame 메인 프레임 참조 (화면 전환에 사용)
     */
    public GuidePanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;

        // ----- 패널 기본 설정 -----
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null); // 절대 좌표 레이아웃 사용
        setBackground(ThemeColors.BG); // 바나나 테마 배경색

        // ----- 가이드 이미지 로드 (파일 시스템 경로 사용) -----
        // 프로젝트폴더/res/game play.png 파일을 로드합니다
        String imagePath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "game play.png";
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            guideImage = icon.getImage();
            System.out.println("가이드 이미지 로드 성공: " + imagePath);
        } else {
            // 이미지 로드 실패 시 디버깅 메시지 출력
            System.err.println("GuidePanel: 이미지를 찾을 수 없습니다: " + imagePath);
            guideImage = null; // paintComponent에서 실패 처리
        }

        // ----- 홈으로 돌아가기 버튼 -----
        JButton backBtn = createThemedButton("홈으로");
        backBtn.setBounds(300, 520, 200, 50);
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));
        add(backBtn);
    }

    /**
     * ============================================
     * createThemedButton() - 테마 버튼 생성
     * ============================================
     * 
     * [설명]
     * - 바나나 테마가 적용된 버튼을 생성합니다.
     * 
     * @param text 버튼 텍스트
     * @return 생성된 JButton 객체
     */
    private JButton createThemedButton(String text) {
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
     * ============================================
     * paintComponent() - 화면 그리기
     * ============================================
     * 
     * [설명]
     * - 가이드 이미지를 화면에 그립니다.
     * - 이미지 비율을 유지하면서 화면에 맞게 스케일링합니다.
     * - 이미지가 없으면 오류 메시지를 표시합니다.
     * 
     * [스케일링 방식]
     * - 이미지의 가로/세로 비율을 유지
     * - 화면에 맞는 최대 크기로 확대/축소
     * - 중앙에 배치
     * 
     * @param g Graphics 객체 (그리기 도구)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (guideImage != null && guideImage.getWidth(null) > 0) {
            // ----- 이미지 스케일링 계산 -----
            int imgWidth = guideImage.getWidth(null); // 원본 이미지 너비
            int imgHeight = guideImage.getHeight(null); // 원본 이미지 높이

            // 가로/세로 스케일 비율 계산
            double scaleX = (double) getWidth() / imgWidth;
            double scaleY = (double) getHeight() / imgHeight;

            // 비율을 유지하기 위해 더 작은 스케일 사용
            double scale = Math.min(scaleX, scaleY);

            // 스케일된 크기 계산
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);

            // 중앙 배치를 위한 좌표 계산
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;

            // 이미지 그리기
            g2.drawImage(guideImage, x, y, scaledWidth, scaledHeight, this);
        } else {
            // ----- 이미지 없을 때 오류 메시지 표시 -----
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 30));
            g2.drawString("이미지를 찾을 수 없습니다: game play.png", 150, 300);
        }
    }
}
