import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * ========================================================
 * 6. 설정 (Settings) 패널
 * ========================================================
 * 게임의 환경설정을 담당하는 화면입니다.
 * 사운드 볼륨 조절과 키보드 조작키 변경 기능을 제공합니다.
 * 
 * 주요 기능:
 * 1. 탭 UI를 이용한 사운드/조작키 설정 분리
 * 2. BGM/SFX 볼륨 슬라이더
 * 3. 키 매핑 변경 시스템
 */
public class SettingsPanel extends JPanel {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private CrazyArcade_UI mainFrame;

    // 바나나 테마 색상 정의 (내부적으로만 사용하는 색상 상수가 있을 경우)
    private final Color COLOR_BG = new Color(255, 250, 205); // 배경 (연한 크림색)
    private final Color COLOR_MAIN = new Color(255, 225, 53); // 메인 노랑 (바나나)
    private final Color COLOR_DARK = new Color(139, 69, 19); // 갈색 (초코/껍질)
    private final Color COLOR_HIGHLIGHT = new Color(255, 240, 150);

    /**
     * 생성자: 설정 화면 UI 구성 (탭 패널 포함)
     */
    public SettingsPanel(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(new BorderLayout()); // BorderLayout 사용 (상단: 타이틀, 중앙: 탭, 하단: 버튼)
        setBackground(COLOR_BG);

        // [상단] 타이틀 라벨
        JLabel titleLabel = new JLabel("Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 45));
        titleLabel.setForeground(COLOR_DARK);
        titleLabel.setBorder(new EmptyBorder(25, 0, 25, 0)); // 여백 추가
        add(titleLabel, BorderLayout.NORTH);

        // [중앙] 탭 패널 (사운드 설정 / 조작키 설정)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        tabbedPane.setForeground(COLOR_DARK);
        tabbedPane.setBackground(COLOR_MAIN);

        // 탭 추가
        tabbedPane.addTab(" 사운드 (Sound) ", createSoundPanel());
        tabbedPane.addTab(" 조작키 (Controls) ", createKeyMappingPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // [하단] 뒤로 가기 버튼 패널
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // 뒤로가기 버튼
        JButton backBtn = createBananaButton("저장 후 돌아가기");
        backBtn.setFont(new Font("맑은 고딕", Font.BOLD, 20)); // 한글 폰트 강제 지정
        backBtn.setPreferredSize(new Dimension(250, 60));
        backBtn.addActionListener(e -> {
            GameSettings.saveSettings(); // 설정 저장
            mainFrame.showPanel(CrazyArcade_UI.PANEL_MENU);
        });

        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 사운드 설정 패널 생성 메서드
     * BGM 및 SFX 볼륨을 조절하는 슬라이더를 포함합니다.
     */
    private JPanel createSoundPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COLOR_BG);

        // 1. 배경음(BGM) 설정
        JLabel bgmLabel = new JLabel("배경음 (BGM)", SwingConstants.LEFT);
        bgmLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        bgmLabel.setForeground(COLOR_DARK);
        bgmLabel.setBounds(150, 80, 200, 30);
        panel.add(bgmLabel);

        // BGM 슬라이더
        JSlider bgmSlider = createBananaSlider(GameSettings.bgmVolume);
        bgmSlider.setBounds(350, 70, 300, 60);
        bgmSlider.addChangeListener(e -> {
            // 슬라이더 바뀔 때마다 전역 설정값 업데이트 및 실제 볼륨 반영
            GameSettings.bgmVolume = bgmSlider.getValue();
            BGMPlayer.getInstance().setVolume(bgmSlider.getValue());
        });
        panel.add(bgmSlider);

        // 2. 효과음(SFX) 설정
        JLabel sfxLabel = new JLabel("효과음 (SFX)", SwingConstants.LEFT);
        sfxLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        sfxLabel.setForeground(COLOR_DARK);
        sfxLabel.setBounds(150, 180, 200, 30);
        panel.add(sfxLabel);

        // SFX 슬라이더
        JSlider sfxSlider = createBananaSlider(GameSettings.sfxVolume);
        sfxSlider.setBounds(350, 170, 300, 60);
        sfxSlider.addChangeListener(e -> GameSettings.sfxVolume = sfxSlider.getValue());
        panel.add(sfxSlider);

        return panel;
    }

    /**
     * 커스텀 디자인 슬라이더 (Banana Slider) 생성 팩토리
     * BasicSliderUI를 커스터마이징하여 바나나 컨셉의 슬라이더를 그립니다.
     */
    private JSlider createBananaSlider(int value) {
        JSlider slider = new JSlider(0, 100, value);
        slider.setOpaque(false);
        slider.setUI(new BasicSliderUI(slider) {
            // 슬라이더 트랙(바) 그리기
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle t = trackRect;

                // 트랙 배경 (연한 노랑)
                g2.setColor(new Color(255, 240, 180));
                g2.fillRoundRect(t.x, t.y + t.height / 3, t.width, t.height / 3, 15, 15);

                // 트랙 테두리 (갈색)
                g2.setColor(COLOR_DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(t.x, t.y + t.height / 3, t.width, t.height / 3, 15, 15);

                // 채워진 부분 (진한 노랑 - 현재 값)
                int fillWidth = (int) (t.width * ((double) slider.getValue() / slider.getMaximum()));
                g2.setColor(COLOR_MAIN);
                g2.fillRoundRect(t.x, t.y + t.height / 3 + 2, fillWidth, t.height / 3 - 4, 10, 10);
            }

            // 슬라이더 손잡이(Thumb) 그리기
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 손잡이 모양 (타원형)
                g2.setColor(COLOR_MAIN);
                g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
                g2.setColor(COLOR_DARK);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            }

            @Override
            protected Dimension getThumbSize() {
                return new Dimension(24, 24); // 손잡이 크기
            }
        });
        return slider;
    }

    /**
     * 키 매핑 설정 패널 생성 메서드
     * 1P와 2P의 조작키를 설정할 수 있는 패널을 생성합니다.
     */
    private JPanel createKeyMappingPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 40, 0)); // 2열 그리드 (1P / 2P)
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        panel.setBackground(COLOR_BG);

        // Player 1 설정 박스
        JPanel p1Panel = createPlayerBox("1p");
        // 람다식을 사용하여 GameSettings의 static 변수를 직접 수정하도록 콜백 연결
        addKeyConfigRow(p1Panel, "위 (Up)", GameSettings.p1_Up, key -> GameSettings.p1_Up = key);
        addKeyConfigRow(p1Panel, "아래 (Down)", GameSettings.p1_Down, key -> GameSettings.p1_Down = key);
        addKeyConfigRow(p1Panel, "왼쪽 (Left)", GameSettings.p1_Left, key -> GameSettings.p1_Left = key);
        addKeyConfigRow(p1Panel, "오른쪽 (Right)", GameSettings.p1_Right, key -> GameSettings.p1_Right = key);
        addKeyConfigRow(p1Panel, "물풍선 (Bomb)", GameSettings.p1_Bomb, key -> GameSettings.p1_Bomb = key);
        addKeyConfigRow(p1Panel, "아이템 (Item)", GameSettings.p1_Item, key -> GameSettings.p1_Item = key);

        // Player 2 설정 박스
        JPanel p2Panel = createPlayerBox("2p");
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
     * 플레이어별 설정 박스 배경 생성
     */
    private JPanel createPlayerBox(String title) {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 배경 박스 (둥근 사각형)
                g2.setColor(new Color(255, 255, 240)); // 아주 연한 아이보리
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                // 테두리
                g2.setColor(COLOR_DARK);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 40, 40);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setForeground(COLOR_DARK);
        panel.add(titleLabel);
        panel.add(new JLabel("")); // 그리드 채우기용 빈 라벨
        return panel;
    }

    /**
     * 키 설정 행(Row) 추가 메서드
     * 설정 라벨과 현재 키 값이 표시된 버튼을 생성하여 패널에 추가합니다.
     */
    private void addKeyConfigRow(JPanel parent, String labelText, int currentKey, KeyUpdateCallback callback) {
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        label.setForeground(new Color(100, 50, 0)); // 진한 갈색

        JButton btn = createBananaButton(KeyEvent.getKeyText(currentKey));

        // 버튼 클릭 시 키 입력 대기 모드로 전환
        btn.addActionListener(e -> {
            btn.setText("입력..."); // 사용자에게 입력 요청
            btn.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent k) {
                    int keyCode = k.getKeyCode();
                    callback.update(keyCode); // 콜백을 통해 실제 설정값 업데이트
                    btn.setText(KeyEvent.getKeyText(keyCode)); // 버튼 텍스트 갱신
                    btn.removeKeyListener(this); // 리스너 제거 (일회성)
                }
            });
        });

        parent.add(label);
        parent.add(btn);
    }

    /**
     * 커스텀 버튼 (바나나 스타일) 생성
     */
    private JButton createBananaButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 마우스 상태에 따른 색상 변화
                if (getModel().isPressed())
                    g2.setColor(new Color(230, 200, 40));
                else if (getModel().isRollover())
                    g2.setColor(COLOR_HIGHLIGHT);
                else
                    g2.setColor(COLOR_MAIN);

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(COLOR_DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);

                super.paintComponent(g);
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
     * 키 설정 업데이트를 위한 콜백 인터페이스
     */
    interface KeyUpdateCallback {
        void update(int keyCode);
    }
}
