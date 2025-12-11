import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ========================================================
 * 메인 프레임 UI 클래스 (Main UI Class)
 * ========================================================
 * 애플리케이션의 메인 창을 담당하는 JFrame 클래스입니다.
 * CardLayout을 사용하여 다양한 화면(패널) 간의 전환을 관리합니다.
 * 
 * 주요 기능:
 * 1. 화면 전환 관리 (showPanel 메서드)
 * 2. 전체적인 프로그램 실행 진입점 (main 메서드)
 * 3. 각 패널(스플래시, 메뉴, 로비, 게임 등) 초기화 및 추가
 */
public class CrazyArcade_UI extends JFrame {

    // 카드 레이아웃 관리자: 패널을 겹쳐 놓고 하나씩 보여주는 레이아웃
    private CardLayout cardLayout;

    // 메인 컨테이너 패널: 모든 화면 패널이 이 위에 쌓임
    private JPanel mainContainer;

    // 화면 식별용 상수 (Panel Identifiers)
    // 각 패널을 구분하기 위한 고유한 키값입니다.
    public static final String PANEL_SPLASH = "SPLASH"; // 스플래시 화면 (초기 로딩)
    public static final String PANEL_MENU = "MENU"; // 메인 메뉴
    public static final String PANEL_LOBBY = "LOBBY"; // 게임 로비 (캐릭터 선택)
    public static final String PANEL_GAME = "GAME"; // 실제 게임 플레이 화면
    public static final String PANEL_GUIDE = "GUIDE"; // 게임 설명 화면
    public static final String PANEL_CREDITS = "CREDITS"; // 제작진 크레딧 화면
    public static final String PANEL_SETTINGS = "SETTINGS"; // 환경 설정 화면

    /**
     * 생성자: 메인 윈도우 설정 및 각 패널 초기화
     */
    public CrazyArcade_UI() {
        // 저장된 설정 로드
        GameSettings.loadSettings();

        setTitle("Water Bomb Man - UI Prototype"); // 윈도우 제목 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // X 버튼 클릭 시 프로그램 종료
        setResizable(false); // 창 크기 조절 불가능하게 설정 (게임 디자인 유지)

        // 레이아웃 초기화
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 각 패널 생성 및 메인 컨테이너에 추가
        // add(패널객체, 식별자) 형태로 추가하여 나중에 식별자로 화면을 전환함
        mainContainer.add(new SplashPanel(this), PANEL_SPLASH);
        mainContainer.add(new MenuPanel(this), PANEL_MENU);
        mainContainer.add(new LobbyPanel(this), PANEL_LOBBY);
        mainContainer.add(new GamePanelPlaceholder(this), PANEL_GAME);
        mainContainer.add(new GuidePanel(this), PANEL_GUIDE);
        mainContainer.add(new CreditsPanel(this), PANEL_CREDITS);
        mainContainer.add(new SettingsPanel(this), PANEL_SETTINGS);

        // 메인 컨테이너를 프레임에 부착
        add(mainContainer);

        // 컴포넌트 크기에 맞춰 창 크기 자동 조절
        pack();

        // 창을 화면 중앙에 배치 (pack() 호출 후에 해야 함)
        setLocationRelativeTo(null);

        // 창을 보이게 설정
        setVisible(true);

        // 프로그램 시작 시 스플래시 화면을 가장 먼저 표시
        showPanel(PANEL_SPLASH);
    }

    /**
     * 화면 전환 메서드
     * CardLayout을 사용하여 지정된 이름의 패널을 최상단으로 보여줍니다.
     * 
     * @param panelName 전환할 패널의 상수 이름 (예: PANEL_MENU)
     */
    public void showPanel(String panelName) {
        // 지정된 패널로 화면 전환
        cardLayout.show(mainContainer, panelName);

        // 게임 패널로 전환될 때 포커스를 요청하여 키보드 입력을 받을 수 있게 함
        // (주의: 컴포넌트 인덱스 3번이 GamePanelPlaceholder라고 가정 - 순서 변경 시 수정 필요)
        if (panelName.equals(PANEL_GAME)) {
            // 인덱스 기반 접근은 위험할 수 있으므로, 나중에 Map으로 관리하는 것이 좋을 수 있음
            // 현재는 add 순서상 3번째가 GamePanelPlaceholder임
            Component gamePanel = mainContainer.getComponent(3);
            if (gamePanel != null)
                gamePanel.requestFocusInWindow();
        }

        // 크레딧 화면 처리: 크레딧 화면이면 스크롤 시작, 아니면 정지
        // 인덱스 5번이 CreditsPanel이라고 가정
        try {
            Component loopComp = mainContainer.getComponent(5);
            if (loopComp instanceof CreditsPanel) {
                CreditsPanel cp = (CreditsPanel) loopComp;
                if (panelName.equals(PANEL_CREDITS))
                    cp.startScrolling();
                else
                    cp.stopScrolling();
            }
        } catch (Exception e) {
            System.err.println("크레딧 패널 제어 중 오류: " + e.getMessage());
        }
    }

    /**
     * 배경 음악(BGM) 재생 시작
     * 메뉴 화면으로 진입할 때 호출되어 배경 음악을 재생합니다.
     * BGMPlayer 싱글톤 인스턴스를 사용합니다.
     */
    public void startBGM() {
        // 현재 실행 경로를 기준으로 sound 폴더의 노래.wav 파일을 찾음
        String bgmPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator + "노래.wav";

        // BGM 로드 및 재생
        BGMPlayer.getInstance().loadAndPlay(bgmPath);
    }

    /**
     * 메인 메서드: 프로그램의 시작점
     * Swing 스레드 안전성을 위해 invokeLater를 사용하여 GUI를 생성합니다.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrazyArcade_UI());
    }
}