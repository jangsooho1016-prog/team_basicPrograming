import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * ========================================================
 * SettingsPanel 클래스 - 설정 화면
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임 설정을 변경할 수 있는 화면입니다.
 * - 두 개의 탭으로 구성: 사운드 설정, 조작키 설정
 * - 바나나 테마 디자인이 적용되어 있습니다.
 * 
 * [화면 구성]
 * - 상단: 타이틀 (Settings)
 * - 중앙: 탭 패널
 * - 사운드 탭: BGM/SFX 볼륨 슬라이더
 * - 조작키 탭: 1P/2P 키 매핑 설정
 * - 하단: 저장 후 돌아가기 버튼
 * 
 * [주의사항]
 * - 설정은 GameSettings 클래스에 저장됩니다.
 * - 프로그램 종료 시 설정은 초기화됩니다 (파일 저장 기능 없음).
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class SettingsPanel extends JPanel {

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

    // 바나나 테마 색상 정의 (ThemeColors와 동일하지만 로컬에서도 사용)
    private final Color COLOR_BG = new Color(255, 250, 205); // 배경 (연한 크림색)
    private final Color COLOR_MAIN = new Color(255, 225, 53); // 메인 노랑 (바나나)
    private final Color COLOR_DARK = new Color(139, 69, 19); // 갈색 (초코/껍질)
    private final Color COLOR_HIGHLIGHT = new Color(255, 240, 150); // 밝은 노랑

    /**
     * ============================================
     * 생성자 - 설정 패널 초기화
     * ============================================
     * 
     * [수행 작업]
     * 1. 패널 기본 설정
     * 2. 타이틀 라벨 생성
     * 3. 탭 패널 생성 (사운드, 조작키)
     * 4. 저장 후 돌아가기 버튼 생성
     * 
     * @param mainFrame 메인 프레임 참조 (화면 전환에 사용)
     */
    public SettingsPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;

        // ----- 패널 기본 설정 -----
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(new BorderLayout()); // 테두리 레이아웃 (상/중/하 배치)
        setBackground(COLOR_BG);

        // ===== 상단 타이틀 =====
        JLabel titleLabel = new JLabel("Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 45));
        titleLabel.setForeground(COLOR_DARK);
        titleLabel.setBorder(new EmptyBorder(25, 0, 25, 0)); // 상하 여백
        add(titleLabel, BorderLayout.NORTH);

        // ===== 중앙 탭 패널 =====
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        tabbedPane.setForeground(COLOR_DARK);
        tabbedPane.setBackground(COLOR_MAIN);

        // 탭 추가
        tabbedPane.addTab(" 사운드 (Sound) ", createSoundPanel());
        tabbedPane.addTab(" 조작키 (Controls) ", createKeyMappingPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // ===== 하단 버튼 패널 =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false); // 투명 (배경색 보이도록)
        bottomPanel.setBorder(new EmptyBorder(20, 0, 20, 0)); // 상하 여백

        // 저장 후 돌아가기 버튼
        JButton backBtn = createBananaButton("저장 후 돌아가기");
        backBtn.setFont(new Font("맑은 고딕", Font.BOLD, 20)); // 한글 폰트 강제 지정
        backBtn.setPreferredSize(new Dimension(250, 60));
        backBtn.addActionListener(e -> mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU));

        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * ============================================
     * createSoundPanel() - 사운드 설정 패널 생성
     * ============================================
     * 
     * [설명]
     * - BGM(배경음악)과 SFX(효과음) 볼륨을 조절하는 슬라이더를 제공합니다.
     * - 슬라이더 값 변경 시 GameSettings에 즉시 반영됩니다.
     * - BGM 슬라이더는 실시간으로 재생 중인 음악 볼륨도 변경합니다.
     * 
     * @return 사운드 설정 JPanel 객체
     */
    private JPanel createSoundPanel() {
        JPanel panel = new JPanel(null); // 절대 좌표 레이아웃
        panel.setBackground(COLOR_BG);

        // ===== BGM 볼륨 설정 =====
        JLabel bgmLabel = new JLabel("배경음 (BGM)", SwingConstants.LEFT);
        bgmLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        bgmLabel.setForeground(COLOR_DARK);
        bgmLabel.setBounds(150, 80, 200, 30);
        panel.add(bgmLabel);

        // BGM 슬라이더 (0~100, 현재값: GameSettings.bgmVolume)
        JSlider bgmSlider = createBananaSlider(GameSettings.bgmVolume);
        bgmSlider.setBounds(350, 70, 300, 60);
        // 슬라이더 값 변경 시 처리
        bgmSlider.addChangeListener(e -> {
            GameSettings.bgmVolume = bgmSlider.getValue(); // 설정값 저장
            BGMPlayer.getInstance().setVolume(bgmSlider.getValue()); // 실시간 볼륨 변경
        });
        panel.add(bgmSlider);

        // ===== SFX 볼륨 설정 =====
        JLabel sfxLabel = new JLabel("효과음 (SFX)", SwingConstants.LEFT);
        sfxLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        sfxLabel.setForeground(COLOR_DARK);
        sfxLabel.setBounds(150, 180, 200, 30);
        panel.add(sfxLabel);

        // SFX 슬라이더 (0~100, 현재값: GameSettings.sfxVolume)
        JSlider sfxSlider = createBananaSlider(GameSettings.sfxVolume);
        sfxSlider.setBounds(350, 170, 300, 60);
        // 슬라이더 값 변경 시 처리
        sfxSlider.addChangeListener(e -> GameSettings.sfxVolume = sfxSlider.getValue());
        panel.add(sfxSlider);

        return panel;
    }

    /**
     * ============================================
     * createBananaSlider() - 바나나 스타일 슬라이더 생성
     * ============================================
     * 
     * [설명]
     * - 바나나 테마가 적용된 커스텀 슬라이더를 생성합니다.
     * - 트랙(레일)과 썸(손잡이)을 직접 그립니다.
     * 
     * [디자인]
     * - 트랙: 연한 노랑 배경, 갈색 테두리, 채워진 부분은 진한 노랑
     * - 썸: 바나나 단면 모양(원형), 노랑 배경, 갈색 테두리
     * 
     * @param value 슬라이더 초기값 (0~100)
     * @return 생성된 JSlider 객체
     */
    private JSlider createBananaSlider(int value) {
        JSlider slider = new JSlider(0, 100, value); // 범위: 0~100
        slider.setOpaque(false); // 투명 배경

        // 커스텀 UI 적용 (BasicSliderUI 상속)
        slider.setUI(new BasicSliderUI(slider) {

            /**
             * 트랙(레일) 그리기
             */
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle t = trackRect; // 트랙 영역

                // 1. 트랙 배경 (연한 노랑)
                g2.setColor(new Color(255, 240, 180));
                g2.fillRoundRect(t.x, t.y + t.height / 3, t.width, t.height / 3, 15, 15);

                // 2. 트랙 테두리 (갈색)
                g2.setColor(COLOR_DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(t.x, t.y + t.height / 3, t.width, t.height / 3, 15, 15);

                // 3. 채워진 부분 (진한 노랑) - 현재 값까지 표시
                int fillWidth = (int) (t.width * ((double) slider.getValue() / slider.getMaximum()));
                g2.setColor(COLOR_MAIN);
                g2.fillRoundRect(t.x, t.y + t.height / 3 + 2, fillWidth, t.height / 3 - 4, 10, 10);
            }

            /**
             * 썸(손잡이) 그리기
             */
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 원형 손잡이 (바나나 단면 모양)
                g2.setColor(COLOR_MAIN);
                g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);

                // 테두리
                g2.setColor(COLOR_DARK);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            }

            /**
             * 썸 크기 지정
             */
            @Override
            protected Dimension getThumbSize() {
                return new Dimension(24, 24); // 24x24 픽셀
            }
        });
        return slider;
    }

    /**
     * ============================================
     * createKeyMappingPanel() - 조작키 설정 패널 생성
     * ============================================
     * 
     * [설명]
     * - 1P(Player 1)와 2P(Player 2)의 조작키를 설정합니다.
     * - 각 키 버튼을 클릭하면 새로운 키를 입력받습니다.
     * 
     * @return 조작키 설정 JPanel 객체
     */
    private JPanel createKeyMappingPanel() {
        // 1행 2열 레이아웃 (좌: 1P, 우: 2P)
        JPanel panel = new JPanel(new GridLayout(1, 2, 40, 0));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40)); // 패딩
        panel.setBackground(COLOR_BG);

        // ===== Player 1 키 설정 박스 =====
        JPanel p1Panel = createPlayerBox("1P");
        addKeyConfigRow(p1Panel, "위 (Up)", GameSettings.p1_Up, key -> GameSettings.p1_Up = key);
        addKeyConfigRow(p1Panel, "아래 (Down)", GameSettings.p1_Down, key -> GameSettings.p1_Down = key);
        addKeyConfigRow(p1Panel, "왼쪽 (Left)", GameSettings.p1_Left, key -> GameSettings.p1_Left = key);
        addKeyConfigRow(p1Panel, "오른쪽 (Right)", GameSettings.p1_Right, key -> GameSettings.p1_Right = key);
        addKeyConfigRow(p1Panel, "물풍선 (Bomb)", GameSettings.p1_Bomb, key -> GameSettings.p1_Bomb = key);
        addKeyConfigRow(p1Panel, "아이템 (Item)", GameSettings.p1_Item, key -> GameSettings.p1_Item = key);

        // ===== Player 2 키 설정 박스 =====
        JPanel p2Panel = createPlayerBox("2P");
        addKeyConfigRow(p2Panel, "위 (Up)", GameSettings.p2_Up, key -> GameSettings.p2_Up = key);
        addKeyConfigRow(p2Panel, "아래 (Down)", GameSettings.p2_Down, key -> GameSettings.p2_Down = key);
        addKeyConfigRow(p2Panel, "왼쪽 (Left)", GameSettings.p2_Left, key -> GameSettings.p2_Left = key);
        addKeyConfigRow(p2Panel, "오른쪽 (Right)", GameSettings.p2_Right, key -> GameSettings.p2_Right = key);
        addKeyConfigRow(p2Panel, "물풍선 (Bomb)", GameSettings.p2_Bomb, key -> GameSettings.p2_Bomb = key);
        addKeyConfigRow(p2Panel, "아이템 (Item)", GameSettings.p2_Item, key -> GameSettings.p2_Item = key);

        panel.add(p1Panel);
        panel.add(p2Panel);
        return panel;
    }

    /**
     * ============================================
     * createPlayerBox() - 플레이어 키 설정 박스 생성
     * ============================================
     * 
     * [설명]
     * - 둥근 모서리의 컨테이너 박스를 생성합니다.
     * - 7행 2열 그리드 레이아웃 (제목 + 6개 키 설정)
     * 
     * @param title 박스 제목 ("1P" 또는 "2P")
     * @return 생성된 JPanel 객체
     */
    private JPanel createPlayerBox(String title) {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 배경 박스 (둥근 사각형, 아이보리색)
                g2.setColor(new Color(255, 255, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                // 테두리 (갈색)
                g2.setColor(COLOR_DARK);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 40, 40);
            }
        };
        panel.setOpaque(false); // 투명 (커스텀 배경 사용)
        panel.setBorder(new EmptyBorder(25, 25, 25, 25)); // 내부 패딩

        // 타이틀 라벨 (첫 번째 행)
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setForeground(COLOR_DARK);
        panel.add(titleLabel);
        panel.add(new JLabel("")); // 빈 셀 (레이아웃 맞추기용)

        return panel;
    }

    /**
     * ============================================
     * addKeyConfigRow() - 키 설정 행 추가
     * ============================================
     * 
     * [설명]
     * - 하나의 키 설정 행(라벨 + 버튼)을 박스에 추가합니다.
     * - 버튼을 클릭하면 키 입력 대기 상태가 됩니다.
     * - 키를 누르면 해당 키가 새로운 설정값으로 저장됩니다.
     * 
     * @param parent     부모 패널 (키 설정 박스)
     * @param labelText  행의 라벨 텍스트 (예: "위 (Up)")
     * @param currentKey 현재 설정된 키 코드
     * @param callback   키 변경 시 호출되는 콜백 함수
     */
    private void addKeyConfigRow(JPanel parent, String labelText, int currentKey, KeyUpdateCallback callback) {
        // 라벨 (키 이름)
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        label.setForeground(new Color(100, 50, 0)); // 진한 갈색

        // 버튼 (현재 키 표시, 클릭하면 변경 가능)
        JButton btn = createBananaButton(KeyEvent.getKeyText(currentKey));
        btn.addActionListener(e -> {
            btn.setText("입력..."); // 키 입력 대기 표시

            // 키 입력 리스너 등록
            btn.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent k) {
                    int keyCode = k.getKeyCode(); // 눌린 키 코드
                    callback.update(keyCode); // 설정값 업데이트
                    btn.setText(KeyEvent.getKeyText(keyCode)); // 버튼 텍스트 변경
                    btn.removeKeyListener(this); // 리스너 제거 (한 번만 입력받음)
                }
            });
        });

        parent.add(label);
        parent.add(btn);
    }

    /**
     * ============================================
     * createBananaButton() - 바나나 스타일 버튼 생성
     * ============================================
     * 
     * [설명]
     * - 바나나 테마가 적용된 둥근 모서리 버튼을 생성합니다.
     * - 마우스 상태(일반/hover/클릭)에 따라 색상이 변합니다.
     * 
     * @param text 버튼 텍스트
     * @return 생성된 JButton 객체
     */
    private JButton createBananaButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 마우스 상태에 따른 색상 변화
                if (getModel().isPressed())
                    g2.setColor(new Color(230, 200, 40)); // 클릭 시
                else if (getModel().isRollover())
                    g2.setColor(COLOR_HIGHLIGHT); // 마우스 오버
                else
                    g2.setColor(COLOR_MAIN); // 일반 상태

                // 둥근 사각형 배경
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // 테두리
                g2.setColor(COLOR_DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);

                super.paintComponent(g); // 텍스트 그리기
            }
        };
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btn.setForeground(COLOR_DARK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    /**
     * ============================================
     * KeyUpdateCallback 인터페이스
     * ============================================
     * 
     * [설명]
     * - 키 설정 변경 시 호출되는 콜백 인터페이스입니다.
     * - 람다 표현식으로 간단히 구현할 수 있습니다.
     * - 함수형 인터페이스 (추상 메서드가 하나뿐)
     * 
     * [사용 예시]
     * KeyUpdateCallback callback = (keyCode) -> GameSettings.p1_Up = keyCode;
     */
    interface KeyUpdateCallback {
        /**
         * 키 코드 업데이트
         * 
         * @param keyCode 새로 설정된 키 코드 (KeyEvent.VK_XXX 형식)
         */
        void update(int keyCode);
    }
}
