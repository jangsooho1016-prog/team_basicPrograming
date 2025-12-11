import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * ========================================================
 * 5. 게임 패널 (Game Panel)
 * ========================================================
 * 실제 게임 플레이 화면입니다.
 * 
 * 화면 구성:
 * - 좌측 (570x570): 맵 + 게임 플레이 영역
 * - 우측 상단: 1P 캐릭터 정보 박스
 * - 우측 상단-중앙: 1P 아이템 박스
 * - 우측 중앙: 2P 캐릭터 정보 박스
 * - 우측 중앙-하단: 2P 아이템 박스
 * - 우측 하단: 나가기 버튼
 * 
 * 조작법:
 * - ESC 키: 로비로 돌아가기
 * - 마우스 클릭 (나가기 버튼): 로비로 돌아가기
 */
public class GamePanelPlaceholder extends JPanel {
    // 메인 프레임 참조 (화면 전환용)
    private CrazyArcade_UI mainFrame;

    // 게임 맵 이미지 (image/InGame/map2.bmp)
    private Image gameMapImage;

    // 캐릭터 이미지 (1P: 배찌, 2P: 다오)
    private Image bazziImg, daoImg;

    // ========== 레이아웃 상수 ==========
    // 맵 영역 좌표 및 크기
    private static final int MAP_X = 15; // 맵 시작 X 좌표
    private static final int MAP_Y = 15; // 맵 시작 Y 좌표
    private static final int MAP_WIDTH = 570; // 맵 너비
    private static final int MAP_HEIGHT = 570; // 맵 높이

    // 우측 패널 좌표 및 크기
    private static final int RIGHT_PANEL_X = 600; // 우측 패널 시작 X 좌표
    private static final int RIGHT_PANEL_WIDTH = 185; // 우측 패널 너비

    /**
     * 생성자: 게임 화면 초기화
     * 
     * @param mainFrame 메인 프레임 (화면 전환에 사용)
     */
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null); // 절대 좌표 레이아웃 사용
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(50, 50, 50)); // 진한 회색 배경

        // 이미지 리소스 로드
        loadCharacterImages();
        loadGameMapImage();

        // ESC 키 입력 시 로비로 복귀
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
                }
            }
        });

        // 나가기 버튼 클릭 시 로비로 복귀
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 나가기 버튼 영역 (x: 600, y: 540, width: 185, height: 45)
                Rectangle exitBounds = new Rectangle(RIGHT_PANEL_X, 540, RIGHT_PANEL_WIDTH, 45);
                if (exitBounds.contains(e.getPoint())) {
                    mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
                }
            }
        });
    }

    /**
     * 게임 맵 이미지 로드
     * 경로: image/InGame/map2.bmp
     */
    private void loadGameMapImage() {
        try {
            String mapPath = System.getProperty("user.dir")
                    + File.separator + "image"
                    + File.separator + "InGame"
                    + File.separator + "map2.bmp";
            File mapFile = new File(mapPath);
            if (mapFile.exists()) {
                gameMapImage = ImageIO.read(mapFile);
            }
        } catch (IOException e) {
            System.err.println("게임 맵 이미지 로드 실패: " + e.getMessage());
        }
    }

    /**
     * 캐릭터 이미지 로드
     * 경로: res/배찌.png, res/다오.png
     */
    private void loadCharacterImages() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;

            // 1P 캐릭터: 배찌
            File bazziFile = new File(basePath + "배찌.png");
            if (bazziFile.exists()) {
                bazziImg = ImageIO.read(bazziFile);
            }

            // 2P 캐릭터: 다오
            File daoFile = new File(basePath + "다오.png");
            if (daoFile.exists()) {
                daoImg = ImageIO.read(daoFile);
            }
        } catch (IOException e) {
            System.err.println("캐릭터 이미지 로드 실패: " + e.getMessage());
        }
    }

    /**
     * 화면 그리기 (paintComponent 오버라이드)
     * 모든 UI 요소를 그립니다.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 전체 배경색 (진한 회색)
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // ========== 좌측: 맵 + 게임 화면 ==========
        drawGameMap(g2);

        // ========== 우측: 1P/2P 정보 + 나가기 ==========
        // 1P 캐릭터 박스 (y: 15, 빨간색 테두리)
        drawPlayerBox(g2, RIGHT_PANEL_X, 15, RIGHT_PANEL_WIDTH, 120, "1P", bazziImg, new Color(220, 80, 80));

        // 1P 아이템 박스 (y: 145)
        drawItemBox(g2, RIGHT_PANEL_X, 145, RIGHT_PANEL_WIDTH, 100);

        // 2P 캐릭터 박스 (y: 260, 파란색 테두리)
        drawPlayerBox(g2, RIGHT_PANEL_X, 260, RIGHT_PANEL_WIDTH, 120, "2P", daoImg, new Color(80, 80, 220));

        // 2P 아이템 박스 (y: 390)
        drawItemBox(g2, RIGHT_PANEL_X, 390, RIGHT_PANEL_WIDTH, 100);

        // 나가기 버튼 (y: 540)
        drawExitButton(g2, RIGHT_PANEL_X, 540, RIGHT_PANEL_WIDTH, 45);
    }

    /**
     * 게임 맵 그리기
     * 좌측 영역에 맵 이미지를 표시합니다.
     * 이미지가 없으면 기본 타일 맵을 그립니다.
     */
    private void drawGameMap(Graphics2D g2) {
        // 검정색 외곽 테두리
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRect(MAP_X - 2, MAP_Y - 2, MAP_WIDTH + 4, MAP_HEIGHT + 4);

        if (gameMapImage != null) {
            // 맵 이미지 그리기
            g2.drawImage(gameMapImage, MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT, this);
        } else {
            // 이미지 없을 때 기본 타일 맵 표시
            drawDefaultMap(g2);
        }
    }

    /**
     * 기본 타일 맵 그리기 (이미지 없을 때)
     * 체스판 패턴의 타일을 그립니다.
     */
    private void drawDefaultMap(Graphics2D g2) {
        int cellSize = 38; // 타일 크기

        // 타일 그리기 (체스판 패턴)
        for (int row = 0; row < MAP_HEIGHT / cellSize; row++) {
            for (int col = 0; col < MAP_WIDTH / cellSize; col++) {
                // 홀짝에 따라 색상 결정
                if ((row + col) % 2 == 0) {
                    g2.setColor(new Color(200, 180, 140)); // 밝은 색
                } else {
                    g2.setColor(new Color(180, 160, 120)); // 어두운 색
                }
                g2.fillRect(MAP_X + col * cellSize, MAP_Y + row * cellSize, cellSize, cellSize);

                // 타일 테두리
                g2.setColor(new Color(150, 130, 100));
                g2.drawRect(MAP_X + col * cellSize, MAP_Y + row * cellSize, cellSize, cellSize);
            }
        }

        // 중앙에 안내 텍스트
        g2.setColor(new Color(100, 80, 60));
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        String text = "Map + 게임 화면";
        FontMetrics fm = g2.getFontMetrics();
        int textX = MAP_X + (MAP_WIDTH - fm.stringWidth(text)) / 2;
        int textY = MAP_Y + MAP_HEIGHT / 2;
        g2.drawString(text, textX, textY);
    }

    /**
     * 플레이어(1P/2P) 캐릭터 박스 그리기
     * 
     * @param g2          Graphics2D 객체
     * @param x           박스 X 좌표
     * @param y           박스 Y 좌표
     * @param w           박스 너비
     * @param h           박스 높이
     * @param player      플레이어 라벨 ("1P" 또는 "2P")
     * @param charImg     캐릭터 이미지
     * @param borderColor 테두리 색상 (1P: 빨강, 2P: 파랑)
     */
    private void drawPlayerBox(Graphics2D g2, int x, int y, int w, int h, String player, Image charImg,
            Color borderColor) {
        // 박스 배경 (진한 회색)
        g2.setColor(new Color(60, 60, 70));
        g2.fillRoundRect(x, y, w, h, 15, 15);

        // 테두리 (플레이어 색상: 1P=빨강, 2P=파랑)
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, h, 15, 15);

        // "캐릭터" 라벨 (중앙)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        g2.drawString("캐릭터", x + (w - 40) / 2, y + 20);

        // 캐릭터 이미지 (중앙)
        if (charImg != null) {
            int imgSize = 60;
            int imgX = x + (w - imgSize) / 2;
            int imgY = y + 30;
            g2.drawImage(charImg, imgX, imgY, imgSize, imgSize, this);
        } else {
            // 이미지 없을 때 "?" 표시
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 40));
            g2.drawString("?", x + w / 2 - 12, y + 75);
        }

        // 플레이어 라벨 (좌상단, 예: "1P", "2P")
        g2.setColor(borderColor);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString(player, x + 8, y + 18);
    }

    /**
     * 아이템 박스 그리기
     * 중앙에 1개의 아이템 슬롯을 표시합니다.
     * 
     * @param g2 Graphics2D 객체
     * @param x  박스 X 좌표
     * @param y  박스 Y 좌표
     * @param w  박스 너비
     * @param h  박스 높이
     */
    private void drawItemBox(Graphics2D g2, int x, int y, int w, int h) {
        // 박스 배경 (진한 회색)
        g2.setColor(new Color(60, 60, 70));
        g2.fillRoundRect(x, y, w, h, 15, 15);

        // 테두리 (하늘색)
        g2.setColor(new Color(100, 180, 255));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, w, h, 15, 15);

        // "아이템" 라벨 (상단 중앙)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        g2.drawString("아이템", x + (w - 45) / 2, y + 20);

        // 아이템 슬롯 (1개, 50x50 크기, 중앙 배치)
        int slotSize = 50;
        int slotX = x + (w - slotSize) / 2;
        int slotY = y + 35;

        g2.setColor(new Color(100, 100, 110));
        g2.fillRoundRect(slotX, slotY, slotSize, slotSize, 10, 10);
        g2.setColor(new Color(80, 80, 90));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(slotX, slotY, slotSize, slotSize, 10, 10);
    }

    /**
     * 나가기 버튼 그리기
     * 클릭 시 로비 화면으로 돌아갑니다.
     * 
     * @param g2 Graphics2D 객체
     * @param x  버튼 X 좌표
     * @param y  버튼 Y 좌표
     * @param w  버튼 너비
     * @param h  버튼 높이
     */
    private void drawExitButton(Graphics2D g2, int x, int y, int w, int h) {
        // 버튼 배경 (파란색 그라데이션)
        GradientPaint gp = new GradientPaint(
                x, y, new Color(80, 160, 240), // 상단: 밝은 파랑
                x, y + h, new Color(40, 120, 200)); // 하단: 진한 파랑
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, 15, 15);

        // 테두리 (연한 파랑)
        g2.setColor(new Color(120, 200, 255));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, h, 15, 15);

        // "나가기" 텍스트 (중앙)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        String text = "나가기";
        int textX = x + (w - fm.stringWidth(text)) / 2;
        int textY = y + (h + fm.getAscent()) / 2 - 4;
        g2.drawString(text, textX, textY);
    }
}
