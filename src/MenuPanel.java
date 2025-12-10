import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * ========================================================
 * MenuPanel 클래스 - 메인 메뉴 화면
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임의 메인 메뉴 화면입니다.
 * - 스플래시 화면 이후에 표시됩니다.
 * - 게임 시작, 가이드, 설정, 크레딧, 종료 버튼을 제공합니다.
 * 
 * [화면 구성]
 * - 배경: start.png 이미지 (없으면 단색 배경)
 * - 하단: 5개의 메뉴 버튼 (Game Start, Guide, Settings, Credits, Exit)
 * 
 * [화면 흐름]
 * - Game Start → LobbyPanel (로비)
 * - Guide → GuidePanel (가이드)
 * - Settings → SettingsPanel (설정)
 * - Credits → CreditsPanel (크레딧)
 * - Exit → 프로그램 종료
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class MenuPanel extends JPanel {

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

    /** 배경 이미지 */
    private Image backgroundImage;

    /** 메인 프레임 참조 (화면 전환에 사용) */
    private CrazyArcade_UI mainFrame;

    /**
     * ============================================
     * 생성자 - 메뉴 패널 초기화
     * ============================================
     * 
     * [수행 작업]
     * 1. 패널 기본 설정
     * 2. 배경 이미지 로드
     * 3. 메뉴 버튼 생성 및 배치
     * 
     * @param mainFrame 메인 프레임 참조 (화면 전환에 사용)
     */
    public MenuPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;

        // ----- 패널 기본 설정 -----
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null); // 절대 좌표 레이아웃 사용

        // ----- 배경 이미지 로드 (파일 시스템 경로 사용) -----
        // 프로젝트폴더/res/start.png 파일을 로드합니다
        String imagePath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "start.png";
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            backgroundImage = icon.getImage();
            System.out.println("메뉴 배경 이미지 로드 성공: " + imagePath);
        } else {
            // 이미지를 찾지 못했을 때 오류 메시지 출력 (디버깅 용)
            System.err.println("MenuPanel: 이미지를 찾을 수 없습니다: " + imagePath);
            backgroundImage = null; // paintComponent에서 대체 배경이 표시됨
        }

        // ----- 버튼 레이아웃 계산 -----
        int buttonWidth = 130; // 버튼 너비
        int buttonHeight = 45; // 버튼 높이
        int gap = 15; // 버튼 사이 간격
        int startY = 500; // 버튼 Y 좌표 (하단에 배치)

        // 버튼들을 중앙에 정렬하기 위한 시작 X 좌표 계산
        // 전체 버튼 너비 = (버튼너비 × 5) + (간격 × 4)
        int startX = (PANEL_WIDTH - ((buttonWidth * 5) + (gap * 4))) / 2;

        // ----- 메뉴 버튼 생성 및 추가 -----
        // 각 버튼을 생성하고 클릭 시 해당 화면으로 이동하도록 설정

        // 1. Game Start 버튼 → 로비 화면으로 이동
        add(createRoundedButton("Game Start", startX, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY)));

        // 2. Guide 버튼 → 가이드 화면으로 이동
        add(createRoundedButton("Guide", startX + (buttonWidth + gap) * 1, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_GUIDE)));

        // 3. Settings 버튼 → 설정 화면으로 이동
        add(createRoundedButton("Settings", startX + (buttonWidth + gap) * 2, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_SETTINGS)));

        // 4. Credits 버튼 → 크레딧 화면으로 이동
        add(createRoundedButton("Credits", startX + (buttonWidth + gap) * 3, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_CREDITS)));

        // 5. Exit 버튼 → 프로그램 종료
        add(createRoundedButton("Exit", startX + (buttonWidth + gap) * 4, startY, buttonWidth, buttonHeight,
                e -> System.exit(0))); // 0: 정상 종료
    }

    /**
     * ============================================
     * paintComponent() - 화면 그리기
     * ============================================
     * 
     * [설명]
     * - 배경 이미지를 그립니다.
     * - 이미지가 없으면 단색 배경을 표시합니다.
     * 
     * @param g Graphics 객체 (그리기 도구)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) {
            // 배경 이미지를 패널 전체 크기에 맞게 그리기
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // 이미지 없으면 기본 배경색으로 채우기
            g2.setColor(ThemeColors.BG);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * ============================================
     * createRoundedButton() - 둥근 테마 버튼 생성
     * ============================================
     * 
     * [설명]
     * - 바나나 테마가 적용된 둥근 모서리 버튼을 생성합니다.
     * - 마우스 상태(일반/hover/클릭)에 따라 색상이 변합니다.
     * 
     * [버튼 스타일]
     * - 일반 상태: MAIN 색상 (노란색)
     * - 마우스 오버: HIGHLIGHT 색상 (밝은 노란색)
     * - 클릭 시: ACCENT 색상 (진한 노란색)
     * - 테두리: DARK 색상 (갈색)
     * 
     * @param text   버튼에 표시할 텍스트
     * @param x      버튼 X 좌표
     * @param y      버튼 Y 좌표
     * @param width  버튼 너비
     * @param height 버튼 높이
     * @param action 버튼 클릭 시 실행할 동작 (ActionListener)
     * @return 생성된 JButton 객체
     */
    private JButton createRoundedButton(String text, int x, int y, int width, int height, ActionListener action) {
        // 익명 클래스로 JButton을 상속하여 커스텀 버튼 생성
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // ----- 마우스 상태에 따른 배경색 설정 -----
                if (getModel().isPressed()) {
                    // 클릭 중: 강조색
                    g2.setColor(ThemeColors.ACCENT);
                } else if (getModel().isRollover()) {
                    // 마우스 오버: 하이라이트색
                    g2.setColor(ThemeColors.HIGHLIGHT);
                } else {
                    // 일반 상태: 메인색
                    g2.setColor(ThemeColors.MAIN);
                }

                // 둥근 사각형 배경 그리기 (모서리 반지름: 25px)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // ----- 테두리 그리기 -----
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(2)); // 선 굵기: 2px
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);

                // 부모 클래스의 paintComponent 호출 (텍스트 그리기)
                super.paintComponent(g);
            }
        };

        // ----- 버튼 속성 설정 -----
        btn.setBounds(x, y, width, height); // 위치와 크기
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14)); // 폰트
        btn.setForeground(ThemeColors.DARK); // 글자색
        btn.setFocusPainted(false); // 포커스 테두리 제거
        btn.setContentAreaFilled(false); // 기본 배경 제거 (커스텀 배경 사용)
        btn.setBorderPainted(false); // 기본 테두리 제거 (커스텀 테두리 사용)
        btn.addActionListener(action); // 클릭 이벤트 등록

        return btn;
    }
}
