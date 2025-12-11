import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * ========================================================
 * 1. 메뉴 화면 패널 (Menu Panel)
 * ========================================================
 * 게임의 메인 메뉴를 담당하는 클래스입니다.
 * 시작, 가이드, 설정, 크레딧, 종료 버튼을 포함합니다.
 * 
 * 주요 특징:
 * 1. 커스텀 배경 이미지 그리기
 * 2. 둥근 모서리와 호버 효과가 있는 커스텀 버튼 (createRoundedButton)
 * 3. 중앙 정렬된 버튼 레이아웃
 */
public class MenuPanel extends JPanel {
    private static final int PANEL_WIDTH = 800; // 패널 가로 크기
    private static final int PANEL_HEIGHT = 600; // 패널 세로 크기

    private Image backgroundImage; // 배경 이미지 객체
    private CrazyArcade_UI mainFrame; // 화면 전환을 위한 메인 프레임 참조

    /**
     * 생성자: 메뉴 패널 UI 구성
     * 
     * @param mainFrame 메인 프레임 인스턴스
     */
    public MenuPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null); // 절대 위치 사용 (null layout)

        // [배경 이미지 로드]
        // res/start.png 이미지를 로드하여 배경으로 사용합니다.
        // 파일 시스템 기반 경로를 사용합니다.
        String startPath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "start.png";
        File startFile = new File(startPath);
        if (startFile.exists()) {
            ImageIcon icon = new ImageIcon(startPath);
            backgroundImage = icon.getImage();
        } else {
            // 이미지를 찾지 못했을 경우 콘솔에 에러 출력 (디버깅용)
            System.err.println("MenuPanel: 이미지를 찾을 수 없습니다 - " + startPath);
            backgroundImage = null;
        }

        // [버튼 배치 설정]
        // 버튼 크기와 간격을 상수로 정의하여 유지보수 용이하게 함
        int buttonWidth = 140;
        int buttonHeight = 45;
        int gap = 20; // 버튼 사이 간격
        int startY = 500; // 버튼이 배치될 Y 좌표

        // 전체 버튼 그룹의 너비를 계산하여 화면 중앙에 배치
        // 총 너비 = (버튼갯수 * 버튼너비) + (간격갯수 * 간격)
        int startX = (PANEL_WIDTH - ((buttonWidth * 4) + (gap * 3))) / 2;

        // [버튼 생성 및 추가]
        // createRoundedButton 메서드를 재사용하여 일관된 디자인 적용

        // 1. 게임 시작 버튼 -> 로비 화면으로 이동
        add(createRoundedButton("Game Start", startX, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY)));

        // 2. 가이드 버튼 -> 가이드 화면으로 이동
        add(createRoundedButton("Guide", startX + (buttonWidth + gap) * 1, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_GUIDE)));

        // 3. 설정 버튼 -> 설정 화면으로 이동
        add(createRoundedButton("Settings", startX + (buttonWidth + gap) * 2, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_SETTINGS)));

        // 4. 종료 버튼 -> 프로그램 종료
        add(createRoundedButton("Exit", startX + (buttonWidth + gap) * 3, startY, buttonWidth, buttonHeight,
                e -> System.exit(0)));
    }

    /**
     * 패널 화면 그리기 메서드
     * 배경 이미지를 그리거나, 이미지가 없을 경우 단색 배경을 채웁니다.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 그래픽 품질 향상을 위한 안티앨리어싱 설정
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (backgroundImage != null)
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        else
            g2.fillRect(0, 0, getWidth(), getHeight()); // 이미지가 없으면 기본 배경색 사용

        // 타이틀 텍스트 그리기 "Water Bomb Man"
        String title = "Water Bomb Man";
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 50));
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        int titleY = 75;

        // 텍스트 그림자 (부드러운 효과)
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(title, titleX + 4, titleY + 4);

        // 메인 텍스트 (밝은 하늘색/파란색 - 크레이지 아케이드 스타일)
        g2.setColor(new Color(100, 200, 255));
        g2.drawString(title, titleX, titleY);

        // 텍스트 하이라이트 (살짝 밝은 효과)
        g2.setColor(new Color(180, 230, 255, 180));
        g2.drawString(title, titleX - 1, titleY - 1);
    }

    /**
     * 커스텀 디자인 버튼 생성 팩토리 메서드
     * 둥근 모서리와 마우스 오버/클릭 시 색상 변화 효과가 있는 버튼을 생성합니다.
     * 일반 JButton을 확장하고 paintComponent를 오버라이드하여 직접 그립니다.
     * 
     * @param text   버튼 텍스트
     * @param x      X 좌표
     * @param y      Y 좌표
     * @param width  너비
     * @param height 높이
     * @param action 클릭 시 실행할 동작 (ActionListener)
     * @return 커스텀 설정된 JButton 객체
     */
    private JButton createRoundedButton(String text, int x, int y, int width, int height, ActionListener action) {
        JButton btn = new JButton(text) {
            // 버튼 모양 커스터마이징을 위해 paintComponent 오버라이드
            // 이 메서드는 버튼이 화면에 그려질 때마다 호출됩니다.
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                // 끊김 없는 부드러운 그래픽을 위한 안티앨리어싱 활성화
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 버튼 상태(눌림, 마우스 오버, 평상시)에 따른 색상 결정
                if (getModel().isPressed())
                    g2.setColor(ThemeColors.ACCENT); // 클릭 시 강조색
                else if (getModel().isRollover())
                    g2.setColor(ThemeColors.HIGHLIGHT); // 마우스 올렸을 때 밝은색
                else
                    g2.setColor(ThemeColors.MAIN); // 평상시 메인 색상

                // 둥근 사각형 배경 그리기 (모서리 반지름 25px)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // 테두리 그리기
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(2)); // 테두리 두께 2px
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);

                // 상위 클래스의 paintComponent를 호출하여 텍스트 라벨 등을 그립니다.
                // super를 호출하지 않으면 글자가 보이지 않습니다.
                super.paintComponent(g);
            }
        };

        // 버튼 기본 속성 설정
        btn.setBounds(x, y, width, height); // 위치 및 크기 설정
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14)); // 폰트 설정
        btn.setForeground(ThemeColors.DARK); // 글자색 설정
        btn.setFocusPainted(false); // 포커스 테두리(점선) 제거
        btn.setContentAreaFilled(false); // 기본 시스템 배경 제거 (위에서 직접 그렸으므로)
        btn.setBorderPainted(false); // 기본 시스템 테두리 제거
        btn.addActionListener(action); // 동작(클릭 이벤트) 연결
        return btn;
    }
}
