import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * ========================================================
 * 5. 게임 패널 (Game Panel)
 * ========================================================
 * 실제 게임 플레이 화면입니다.
 * - 좌측: 게임 맵 표시
 * - 우측: 타이머, 캐릭터 정보, 아이템 정보
 * ESC 키로 로비로 돌아갈 수 있습니다.
 */
public class GamePanelPlaceholder extends JPanel {
    private CrazyArcade_UI mainFrame;
    private Cursor customCursor;
    private Image gameMapImage; // 게임 맵 이미지 (교체 가능)

    // 캐릭터 이미지
    private Image bazziImg;
    private Image daoImg;

    // 레이아웃 크기
    private static final int MAP_WIDTH = 630;
    private static final int MAP_HEIGHT = 560;
    private static final int RIGHT_PANEL_X = 645;
    private static final int RIGHT_PANEL_WIDTH = 145;

    public GamePanelPlaceholder(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(30, 100, 180)); // 파란 배경

        // 커스텀 커서 로드
        loadCustomCursor();

        // 캐릭터 이미지 로드
        loadCharacterImages();

        // 게임 맵 이미지 로드 (game play.png 사용)
        loadGameMapImage();

        // 키 리스너: ESC로 로비 복귀
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
                }
            }
        });
    }

    /**
     * 게임 맵 이미지 로드
     */
    private void loadGameMapImage() {
        try {
            String mapPath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "game play.png";
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
     */
    private void loadCharacterImages() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;
            File bazziFile = new File(basePath + "배찌.png");
            if (bazziFile.exists()) {
                bazziImg = ImageIO.read(bazziFile);
            }
            File daoFile = new File(basePath + "다오.png");
            if (daoFile.exists()) {
                daoImg = ImageIO.read(daoFile);
            }
        } catch (IOException e) {
            System.err.println("캐릭터 이미지 로드 실패: " + e.getMessage());
        }
    }

    /**
     * 커스텀 커서 로드
     */
    private void loadCustomCursor() {
        try {
            String cursorPath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "cursor.png";
            File cursorFile = new File(cursorPath);
            if (cursorFile.exists()) {
                Image cursorImg = ImageIO.read(cursorFile);
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                customCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "CustomCursor");
                setCursor(customCursor);
            }
        } catch (IOException e) {
            System.err.println("커스텀 커서 로드 실패: " + e.getMessage());
        }
    }

    /**
     * 화면 그리기
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 배경색
        g2.setColor(new Color(30, 100, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 좌측 게임 맵 그리기
        drawGameMap(g2);

        // 우측 UI 패널 그리기
        drawRightPanel(g2);
    }

    /**
     * 게임 맵 그리기
     */
    private void drawGameMap(Graphics2D g2) {
        // 맵 배경 (검정 테두리)
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(5));
        g2.drawRect(10, 10, MAP_WIDTH, MAP_HEIGHT);

        if (gameMapImage != null) {
            // 게임 맵 이미지 그리기
            g2.drawImage(gameMapImage, 13, 13, MAP_WIDTH - 6, MAP_HEIGHT - 6, this);
        } else {
            // 이미지 없으면 타일 그리기
            drawTiledMap(g2);
        }
    }

    /**
     * 타일 맵 그리기 (이미지 없을 때)
     */
    private void drawTiledMap(Graphics2D g2) {
        int cellSize = 40;
        int startX = 13;
        int startY = 13;

        for (int row = 0; row < MAP_HEIGHT / cellSize; row++) {
            for (int col = 0; col < MAP_WIDTH / cellSize; col++) {
                if ((row + col) % 2 == 0) {
                    g2.setColor(new Color(200, 150, 50));
                } else {
                    g2.setColor(new Color(180, 130, 40));
                }
                g2.fillRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);

                g2.setColor(new Color(160, 110, 30));
                g2.drawRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);
            }
        }
    }

    /**
     * 우측 UI 패널 그리기
     */
    private void drawRightPanel(Graphics2D g2) {
        // 1. 타이머 패널
        drawTimerPanel(g2, RIGHT_PANEL_X, 10, RIGHT_PANEL_WIDTH, 60);

        // 2. 캐릭터 정보 패널들 (8개 슬롯)
        int charSlotY = 75;
        int charSlotHeight = 48;
        int charSlotGap = 2;

        for (int i = 0; i < 8; i++) {
            int y = charSlotY + i * (charSlotHeight + charSlotGap);
            drawCharacterSlot(g2, RIGHT_PANEL_X, y, RIGHT_PANEL_WIDTH, charSlotHeight, i);
        }

        // 3. 아이템 패널
        drawItemPanel(g2, RIGHT_PANEL_X, 465, RIGHT_PANEL_WIDTH, 95);

        // 4. 나가기 버튼
        drawExitButton(g2, RIGHT_PANEL_X, 567, RIGHT_PANEL_WIDTH, 30);
    }

    /**
     * 타이머 패널 그리기
     */
    private void drawTimerPanel(Graphics2D g2, int x, int y, int w, int h) {
        // 외곽 테두리 (밝은 파랑)
        g2.setColor(new Color(100, 180, 255));
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(x, y, w, h);

        // 배경 (진한 파랑)
        g2.setColor(new Color(40, 90, 160));
        g2.fillRect(x + 3, y + 3, w - 6, h - 6);

        // 타이틀
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.drawString("TIMER", x + w / 2 - 20, y + 15);

        // 타이머 표시
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        g2.drawString("0:00", x + w / 2 - 25, y + 45);
    }

    /**
     * 캐릭터 슬롯 그리기
     */
    private void drawCharacterSlot(Graphics2D g2, int x, int y, int w, int h, int index) {
        // 슬롯 배경 (그라데이션)
        GradientPaint gp = new GradientPaint(
                x, y, new Color(80, 140, 220),
                x, y + h, new Color(50, 100, 170));
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, 8, 8);

        // 테두리
        g2.setColor(new Color(120, 180, 255));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, w, h, 8, 8);

        // 캐릭터 이미지 (첫 2개 슬롯만 표시)
        if (index == 0 && bazziImg != null) {
            // 1P - 배찌
            g2.drawImage(bazziImg, x + 5, y + 5, 38, 38, this);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 10));
            g2.drawString("조준코비", x + 48, y + 20);
        } else if (index == 1 && daoImg != null) {
            // 2P - 다오
            g2.drawImage(daoImg, x + 5, y + 5, 38, 38, this);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 10));
            g2.drawString("플레이어2", x + 48, y + 20);
        }

        // 체력바 (활성 슬롯만)
        if (index < 2) {
            g2.setColor(new Color(255, 80, 80));
            g2.fillRect(x + 48, y + 28, w - 55, 8);
            g2.setColor(new Color(150, 40, 40));
            g2.drawRect(x + 48, y + 28, w - 55, 8);
        }
    }

    /**
     * 아이템 패널 그리기
     */
    private void drawItemPanel(Graphics2D g2, int x, int y, int w, int h) {
        // 외곽 테두리
        g2.setColor(new Color(100, 180, 255));
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(x, y, w, h);

        // 배경
        g2.setColor(new Color(40, 90, 160));
        g2.fillRect(x + 3, y + 3, w - 6, h - 6);

        // 타이틀
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.drawString("ITEM", x + w / 2 - 18, y + 15);

        // 아이템 슬롯 (회색 박스)
        g2.setColor(new Color(150, 150, 150));
        g2.fillRoundRect(x + 10, y + 25, w - 20, h - 35, 8, 8);
        g2.setColor(new Color(100, 100, 100));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x + 10, y + 25, w - 20, h - 35, 8, 8);
    }

    /**
     * 나가기 버튼 그리기
     */
    private void drawExitButton(Graphics2D g2, int x, int y, int w, int h) {
        // 버튼 배경 (그라데이션 - 파란색)
        GradientPaint gp = new GradientPaint(
                x, y, new Color(80, 160, 240),
                x, y + h, new Color(40, 120, 200));
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, 10, 10);

        // 테두리
        g2.setColor(new Color(120, 200, 255));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        // 텍스트
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        String text = "나가기";
        int textX = x + (w - fm.stringWidth(text)) / 2;
        int textY = y + (h + fm.getAscent()) / 2 - 2;
        g2.drawString(text, textX, textY);

        // 클릭 영역 설정 (마우스 리스너 추가를 위해)
        if (!hasExitButtonListener) {
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    Rectangle exitBtnBounds = new Rectangle(RIGHT_PANEL_X, 567, RIGHT_PANEL_WIDTH, 30);
                    if (exitBtnBounds.contains(e.getPoint())) {
                        mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
                    }
                }
            });
            hasExitButtonListener = true;
        }
    }

    private boolean hasExitButtonListener = false;
}
