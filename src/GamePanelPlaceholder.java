import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * ========================================================
 * GamePanelPlaceholder 클래스 - 게임 화면 플레이스홀더
 * ========================================================
 * 
 * [클래스 설명]
 * - 실제 게임 로직이 들어갈 자리를 표시하는 임시 패널입니다.
 * - 팀원들이 게임 로직을 구현할 때 이 클래스를 수정하거나 교체합니다.
 * - 현재는 안내 메시지만 표시합니다.
 * 
 * [화면 구성]
 * - 검정 배경
 * - 팀 프로젝트 안내 메시지
 * - ESC 키 안내
 * 
 * [조작법]
 * - ESC 키: 로비 화면으로 돌아가기
 * 
 * [팀원들에게]
 * - 이 클래스를 실제 게임 패널(GamePanel 등)로 교체하세요.
 * - 또는 이 클래스 안에 직접 게임 로직을 구현하세요.
 * - 캐릭터, 맵, 아이템 로직을 통합하는 곳입니다.
 * 
 * @author 팀원 공동 작업
 * @version 1.0 (플레이스홀더)
 */
public class GamePanelPlaceholder extends JPanel {

    // ============================================
    // 멤버 변수 (필드)
    // ============================================

    /** 메인 프레임 참조 (화면 전환에 사용) */
    private CrazyArcade_UI mainFrame;

    /**
     * ============================================
     * 생성자 - 게임 패널 플레이스홀더 초기화
     * ============================================
     * 
     * [수행 작업]
     * 1. 패널 기본 설정 (검정 배경)
     * 2. 안내 라벨 추가
     * 3. ESC 키 이벤트 리스너 등록
     * 
     * @param mainFrame 메인 프레임 참조 (화면 전환에 사용)
     */
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;

        // ----- 패널 기본 설정 -----
        setLayout(null); // 절대 좌표 레이아웃 사용
        setBackground(Color.BLACK); // 배경색: 검정

        // ===== 안내 라벨 1: 팀 프로젝트 안내 =====
        JLabel infoLabel = new JLabel("TEAM PROJECT: GAME LOGIC AREA");
        infoLabel.setForeground(Color.GREEN); // 초록색 (터미널 느낌)
        infoLabel.setFont(new Font("Courier New", Font.BOLD, 30)); // 고정폭 폰트
        infoLabel.setBounds(150, 200, 600, 50);
        add(infoLabel);

        // ===== 안내 라벨 2: 한글 설명 =====
        JLabel subLabel = new JLabel("팀원들이 작성한 게임 패널 코드가 들어갈 자리입니다.");
        subLabel.setForeground(Color.WHITE);
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
        subLabel.setBounds(180, 260, 500, 30);
        add(subLabel);

        // ===== 안내 라벨 3: 조작법 안내 =====
        JLabel guideLabel = new JLabel("Press [ESC] to return to Lobby");
        guideLabel.setForeground(Color.YELLOW); // 노란색 (강조)
        guideLabel.setBounds(300, 400, 300, 30);
        add(guideLabel);

        // ----- 키보드 이벤트 설정 -----
        // 키보드 입력을 받으려면 포커스 가능해야 함
        setFocusable(true);

        // ESC 키를 누르면 로비 화면으로 이동
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // ESC 키 감지
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
                }

                // ===== [TODO] 여기에 게임 조작키 처리 추가 =====
                // 예시:
                // if (e.getKeyCode() == GameSettings.p1_Up) {
                // player1.moveUp();
                // }
                // if (e.getKeyCode() == GameSettings.p1_Bomb) {
                // player1.placeBomb();
                // }
            }
        });
    }

    // ===== [TODO] 팀원들이 추가할 메서드 예시 =====

    /*
     * 게임 루프 예시:
     * 
     * private Timer gameTimer;
     * 
     * public void startGame() {
     * gameTimer = new Timer(16, e -> { // 약 60 FPS
     * updateGame(); // 게임 상태 업데이트
     * repaint(); // 화면 다시 그리기
     * });
     * gameTimer.start();
     * }
     * 
     * private void updateGame() {
     * // 캐릭터 이동, 충돌 검사, 폭발 처리 등
     * }
     * 
     * @Override
     * protected void paintComponent(Graphics g) {
     * super.paintComponent(g);
     * // 맵 그리기
     * // 캐릭터 그리기
     * // 물풍선 그리기
     * // 아이템 그리기
     * }
     */
}
