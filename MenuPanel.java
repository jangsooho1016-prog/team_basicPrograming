import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

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
        // getClass().getResource()를 사용하여 JAR 파일 내부의 리소스도 읽을 수 있게 합니다.
        URL startUrl = getClass().getResource("/res/start.png");
        if (startUrl != null) {
            ImageIcon icon = new ImageIcon(startUrl);
            backgroundImage = icon.getImage();
        } else {
            // 이미지를 찾지 못했을 경우 콘솔에 에러 출력 (디버깅용)
            System.err.println("MenuPanel: 리소스를 찾을 수 없습니다 - /res/start.png");
            backgroundImage = null;
        }

        // [버튼 배치 설정]
        // 버튼 크기와 간격을 상수로 정의하여 유지보수 용이하게 함
        int buttonWidth = 130;
        int buttonHeight = 45;
        int gap = 15; // 버튼 사이 간격
        int startY = 500; // 버튼이 배치될 Y 좌표

        // 전체 버튼 그룹의 너비를 계산하여 화면 중앙에 배치
        // 총 너비 = (버튼갯수 * 버튼너비) + (간격갯수 * 간격)
        int startX = (PANEL_WIDTH - ((buttonWidth * 5) + (gap * 4))) / 2;

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

        // 4. 크레딧 버튼 -> 제작진 화면으로 이동
        add(createRoundedButton("Credits", startX + (buttonWidth + gap) * 3, startY, buttonWidth, buttonHeight,
                e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_CREDITS)));

        // 5. 종료 버튼 -> 프로그램 종료
        add(createRoundedButton("Exit", startX + (buttonWidth + gap) * 4, startY, buttonWidth, buttonHeight,
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

        if (backgroundImage != null)
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        else
            g2.fillRect(0, 0, getWidth(), getHeight()); // 이미지가 없으면 기본 배경색 사용
    }

    /**
     * 커스텀 디자인 버튼 생성 팩토리 메서드
     * 둥근 모서리와 마우스 오버/클릭 시 색상 변화 효과가 있는 버튼을 생성합니다.
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
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 버튼 상태에 따른 색상 변경 (상호작용 피드백)
                if (getModel().isPressed())
                    g2.setColor(ThemeColors.ACCENT); // 클릭 시 강조색
                else if (getModel().isRollover())
                    g2.setColor(ThemeColors.HIGHLIGHT); // 마우스 올렸을 때 밝은색
                else
                    g2.setColor(ThemeColors.MAIN); // 평상시 메인 색상

                // 둥근 사각형 배경 그리기 (반지름 25)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // 테두리 그리기
                g2.setColor(ThemeColors.DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);

                // 텍스트 그리기 (super 호출)
                super.paintComponent(g);
            }
        };

        // 버튼 기본 속성 설정
        btn.setBounds(x, y, width, height);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btn.setForeground(ThemeColors.DARK);
        btn.setFocusPainted(false); // 포커스 테두리 제거
        btn.setContentAreaFilled(false); // 기본 배경 제거 (paintComponent에서 직접 그림)
        btn.setBorderPainted(false); // 기본 테두리 제거
        btn.addActionListener(action); // 동작 연결
        return btn;
    }
}
