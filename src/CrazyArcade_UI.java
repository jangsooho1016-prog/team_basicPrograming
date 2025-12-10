import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ========================================================
 * CrazyArcade_UI 클래스 - 메인 UI 프레임 (게임의 진입점)
 * ========================================================
 * 
 * [클래스 설명]
 * - 이 클래스는 전체 게임의 메인 프레임(창)을 관리합니다.
 * - CardLayout을 사용하여 여러 패널(화면)을 전환합니다.
 * - 게임을 실행하면 가장 먼저 이 클래스의 main() 메서드가 호출됩니다.
 * 
 * [화면 구성]
 * - SPLASH: 스플래시 화면 (게임 시작 시 로고 표시)
 * - MENU: 메인 메뉴 화면 (게임 시작, 설정, 크레딧 등)
 * - LOBBY: 게임 로비 화면 (캐릭터 선택, 채팅)
 * - GAME: 실제 게임 플레이 화면
 * - GUIDE: 게임 가이드 화면 (조작법 설명)
 * - CREDITS: 크레딧 화면 (제작진 소개)
 * - SETTINGS: 설정 화면 (볼륨, 키 설정)
 * 
 * [사용된 디자인 패턴]
 * - CardLayout: 여러 패널을 카드처럼 쌓아두고 필요한 것만 보여주는 레이아웃
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class CrazyArcade_UI extends JFrame {

    // ============================================
    // 멤버 변수 (필드)
    // ============================================

    /**
     * CardLayout - 여러 패널을 겹쳐서 관리하고, 하나만 보여주는 레이아웃 매니저
     * show() 메서드를 호출하면 해당 이름의 패널이 앞으로 나옵니다.
     */
    private CardLayout cardLayout;

    /**
     * mainContainer - 모든 패널(화면)을 담는 컨테이너 패널
     * CardLayout이 적용되어 있어, 여러 패널 중 하나만 보입니다.
     */
    private JPanel mainContainer;

    // ============================================
    // 패널 이름 상수 (다른 클래스에서도 사용하기 위해 public static final)
    // ============================================

    /** 스플래시 화면 패널 이름 */
    public static final String PANEL_SPLASH = "SPLASH";

    /** 메인 메뉴 화면 패널 이름 */
    public static final String PANEL_MENU = "MENU";

    /** 게임 로비 화면 패널 이름 */
    public static final String PANEL_LOBBY = "LOBBY";

    /** 게임 플레이 화면 패널 이름 */
    public static final String PANEL_GAME = "GAME";

    /** 게임 가이드 화면 패널 이름 */
    public static final String PANEL_GUIDE = "GUIDE";

    /** 크레딧 화면 패널 이름 */
    public static final String PANEL_CREDITS = "CREDITS";

    /** 설정 화면 패널 이름 */
    public static final String PANEL_SETTINGS = "SETTINGS";

    /**
     * ============================================
     * 생성자 - 메인 프레임 초기화
     * ============================================
     * 
     * [수행 작업]
     * 1. JFrame 기본 설정 (타이틀, 닫기 동작, 크기 조절 불가)
     * 2. CardLayout과 메인 컨테이너 생성
     * 3. 각 화면(패널)을 생성하여 컨테이너에 추가
     * 4. 프레임을 화면 중앙에 배치하고 표시
     * 5. 스플래시 화면부터 시작
     */
    public CrazyArcade_UI() {
        // ----- JFrame 기본 설정 -----
        setTitle("Water Bomb Man - UI Prototype"); // 창 제목 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // X 버튼 클릭 시 프로그램 종료
        setResizable(false); // 창 크기 조절 불가 (고정 크기)

        // ----- CardLayout 및 컨테이너 생성 -----
        cardLayout = new CardLayout(); // 카드 레이아웃 생성
        mainContainer = new JPanel(cardLayout); // 컨테이너에 CardLayout 적용

        // ----- 각 화면(패널) 생성 및 추가 -----
        // add(컴포넌트, 이름) 형식으로 추가하면 나중에 이름으로 화면을 전환할 수 있음
        mainContainer.add(new SplashPanel(this), PANEL_SPLASH); // 스플래시 화면
        mainContainer.add(new MenuPanel(this), PANEL_MENU); // 메인 메뉴 화면
        mainContainer.add(new LobbyPanel(this), PANEL_LOBBY); // 로비 화면
        mainContainer.add(new GamePanelPlaceholder(this), PANEL_GAME); // 게임 화면 (플레이스홀더)
        mainContainer.add(new GuidePanel(this), PANEL_GUIDE); // 가이드 화면
        mainContainer.add(new CreditsPanel(this), PANEL_CREDITS); // 크레딧 화면
        mainContainer.add(new SettingsPanel(this), PANEL_SETTINGS); // 설정 화면

        // ----- 프레임 구성 완료 -----
        add(mainContainer); // 메인 컨테이너를 프레임에 추가
        pack(); // 내용물에 맞게 프레임 크기 자동 조절
        setLocationRelativeTo(null); // 화면 중앙에 배치
        setVisible(true); // 프레임 표시

        // ----- 스플래시 화면 먼저 표시 -----
        showPanel(PANEL_SPLASH);
    }

    /**
     * ============================================
     * showPanel() - 화면 전환 메서드
     * ============================================
     * 
     * [설명]
     * - 지정된 패널(화면)을 앞으로 가져와 보여줍니다.
     * - CardLayout의 show() 메서드를 사용합니다.
     * 
     * [특수 처리]
     * - GAME 패널로 전환 시: 키보드 입력을 받기 위해 포커스 요청
     * - CREDITS 패널로 전환 시: 스크롤 애니메이션 시작
     * - CREDITS 패널에서 벗어날 시: 스크롤 애니메이션 중지
     * 
     * @param panelName 표시할 패널의 이름 (PANEL_SPLASH, PANEL_MENU 등)
     */
    public void showPanel(String panelName) {
        // CardLayout을 사용하여 지정된 패널 표시
        cardLayout.show(mainContainer, panelName);

        // 게임 패널로 전환 시 포커스 요청 (키보드 입력을 받기 위해)
        // mainContainer.getComponent(3)은 GAME 패널 (추가 순서대로 0부터 시작)
        if (panelName.equals(PANEL_GAME))
            mainContainer.getComponent(3).requestFocusInWindow();

        // 크레딧 패널 스크롤 제어
        // mainContainer.getComponent(5)는 CREDITS 패널
        CreditsPanel cp = (CreditsPanel) mainContainer.getComponent(5);
        if (panelName.equals(PANEL_CREDITS))
            cp.startScrolling(); // 크레딧 화면이면 스크롤 시작
        else
            cp.stopScrolling(); // 다른 화면이면 스크롤 중지
    }

    /**
     * ============================================
     * startBGM() - 배경음악 재생 시작
     * ============================================
     * 
     * [설명]
     * - BGMPlayer 싱글톤 인스턴스를 사용하여 배경음악을 재생합니다.
     * - 메뉴 화면으로 이동할 때 호출됩니다.
     * 
     * [음악 파일 경로]
     * - 프로젝트폴더/sound/노래.wav
     * - System.getProperty("user.dir")은 현재 작업 디렉토리를 반환
     * - File.separator는 OS에 맞는 경로 구분자 (Windows: \, Mac/Linux: /)
     */
    public void startBGM() {
        // 음악 파일 경로 생성: 프로젝트폴더/sound/노래.wav
        String bgmPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator + "노래.wav";

        // BGMPlayer 싱글톤을 통해 음악 로드 및 재생
        BGMPlayer.getInstance().loadAndPlay(bgmPath);
    }

    /**
     * ============================================
     * main() - 프로그램 진입점
     * ============================================
     * 
     * [설명]
     * - Java 프로그램이 시작되는 지점입니다.
     * - SwingUtilities.invokeLater()를 사용하여 EDT(Event Dispatch Thread)에서 UI를 생성합니다.
     * - 이는 Swing의 thread-safe 실행을 보장합니다.
     * 
     * [EDT (Event Dispatch Thread)]
     * - Swing의 모든 UI 작업은 EDT에서 실행되어야 합니다.
     * - invokeLater()는 작업을 EDT 큐에 예약합니다.
     * 
     * @param args 명령줄 인자 (사용하지 않음)
     */
    public static void main(String[] args) {
        // EDT에서 UI 생성 (Swing 권장 방식)
        SwingUtilities.invokeLater(() -> new CrazyArcade_UI());
    }
}