import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * ========================================================
 * 5. 게임 패널 (Game Panel) - [핵심 게임 로직]
 * ========================================================
 * 실제 게임 플레이가 이루어지는 핵심 클래스입니다.
 * 맵 렌더링, 캐릭터 이동, 물풍선 설치 및 폭발, 승패 판정 등
 * 게임의 모든 로직이 이 파일에서 처리됩니다.
 * 
 * --------------------------------------------------------
 * [화면 구성]
 * - 좌측 (570x570): 실제 게임이 진행되는 맵 영역 (13x15 타일 그리드)
 * - 우측: 플레이어 정보(캐릭터, 아이템 상태) 및 나가기 버튼
 * 
 * [핵심 기능]
 * 1. 게임 루프 (Swing Timer 이용, 초당 약 60회 업데이트)
 * 2. 충돌 감지 (캐릭터가 블록이나 맵 밖으로 나가지 못하게 함)
 * 3. 물풍선 로직 (설치 -> 3초 후 폭발 -> 블록 파괴 및 게임 결과 판정)
 * 4. 스프라이트 애니메이션 (8x4 그리드, 방향별 애니메이션)
 * --------------------------------------------------------
 */
public class GamePanelPlaceholder extends JPanel {
    // ========== [1] 기본 설정 및 참조 변수 ==========
    private CrazyArcade_UI mainFrame;
    private LobbyPanel lobbyPanel;

    // ========== [2] 맵 시스템 ==========
    private Map gameMap;
    private Tile[][] tiles;
    private static final int TILE_ROWS = 13;
    private static final int TILE_COLS = 15;

    // ========== [3] 캐릭터 시스템 (스프라이트 애니메이션) ==========
    private BufferedImage[][] p1Sprites; // 1P 스프라이트 시트 [행][열]
    private BufferedImage[][] p2Sprites; // 2P 스프라이트 시트 [행][열]
    private static final int SPRITE_ROWS = 4;
    private static final int SPRITE_COLS = 8;
    
    // 스프라이트 애니메이션 상태
    private int p1SpriteRow = 3; // 1P 현재 행 (방향)
    private int p1SpriteCol = 0; // 1P 현재 열 (애니메이션 프레임)
    private int p2SpriteRow = 3; // 2P 현재 행
    private int p2SpriteCol = 0; // 2P 현재 열
    
    private int animationSpeed = 3; // 애니메이션 속도 (값이 클수록 느림)
    private int p1FrameCounter = 0;
    private int p2FrameCounter = 0;
    
    // 현재 플레이어의 선택된 캐릭터 이름
    private String p1CharacterName = "배찌";
    private String p2CharacterName = "다오";

    // ========== [4] 좌표 및 이동 시스템 (수정) ==========
    private int p1X = 60;
    private int p1Y = 60;
    private int p2X = 520;
    private int p2Y = 520;

    private static final int PLAYER_SIZE = 40;
    private static final int SPRITE_WIDTH = 44;
    private static final int SPRITE_HEIGHT = 62;
    private static final int MOVE_SPEED = 5;

    private boolean p1UpPressed, p1DownPressed, p1LeftPressed, p1RightPressed;
    private boolean p2UpPressed, p2DownPressed, p2LeftPressed, p2RightPressed;

    // 마지막으로 누른 키 추적 (추가)
    private Integer p1LastKey = null;
    private Integer p2LastKey = null;

    // ========== [5] 게임 루프 & 타이머 ==========
    private javax.swing.Timer gameTimer;
    private java.util.List<WaterBomb> bombs = new java.util.ArrayList<>();
    private static final int BOMB_SIZE = 38;
    private static final int BOMB_TIMER = 3000;
    
    private int tileWidth;
    private int tileHeight;

    // ========== [6] 게임 상태 및 승패 판정 ==========
    private static final int GAME_TIME = 150;
    private int remainingTime = GAME_TIME;
    private long lastTimerUpdate = 0;
    
    private static final int STATE_PLAYING = 0;
    private static final int STATE_P1_WIN = 1;
    private static final int STATE_P2_WIN = 2;
    private static final int STATE_DRAW = 3;
    private int gameState = STATE_PLAYING;
    
    private boolean p1Alive = true;
    private boolean p2Alive = true;
    
    private Image winImg, drawImg;
    private long resultDisplayTime = 0;
    private static final int RESULT_DISPLAY_DURATION = 3000;

    // ========== [7] 레이아웃 상수 ==========
    private static final int MAP_X = 15;
    private static final int MAP_Y = 15;
    private static final int MAP_WIDTH = 570;
    private static final int MAP_HEIGHT = 570;
    private static final int RIGHT_PANEL_X = 600;
    private static final int RIGHT_PANEL_WIDTH = 185;

    /**
     * 생성자: 게임 화면 초기화 (기존 호환용)
     */
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame) {
        this(mainFrame, null);
    }

    /**
     * 생성자: 게임 패널 초기화 및 리소스 로드
     */
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame, LobbyPanel lobbyPanel) {
        this.mainFrame = mainFrame;
        this.lobbyPanel = lobbyPanel;
        setLayout(null);
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(50, 50, 50));

        // 1. 이미지 리소스 로드
        loadCharacterSprites(); // 스프라이트 시트 로드
        loadResultImages();

        // 2. 맵 시스템 초기화
        initMapSystem();

        // 3. 플레이어 시작 위치 설정
        initPlayerPositions();

        // 4. 이벤트 리스너 등록
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Rectangle exitBounds = new Rectangle(RIGHT_PANEL_X, 540, RIGHT_PANEL_WIDTH, 45);
                if (exitBounds.contains(e.getPoint())) {
                    stopGameLoop();
                    playLobbyBGM();
                    mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
                }
            }
        });

        startGameLoop();
    }

    /**
     * 캐릭터 스프라이트 시트 로드 (8x4 그리드)
     */
    private void loadCharacterSprites() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;
            System.out.println("스프라이트 로드 시작, 경로: " + basePath);
            
            // 1P 스프라이트 로드
            File p1File = new File(basePath + "BlueBazzi.png");
            System.out.println("1P 파일 확인: " + p1File.getAbsolutePath());
            System.out.println("1P 파일 존재: " + p1File.exists());
            
            if (p1File.exists()) {
                BufferedImage p1Sheet = ImageIO.read(p1File);
                // 마젠타 배경을 투명하게 처리
                BufferedImage p1Transparent = makeColorTransparent(p1Sheet, 0xFF00FF);
                p1Sprites = loadSpriteSheet(p1Transparent);
                System.out.println("1P 스프라이트 로드 성공! 크기: " + p1Sheet.getWidth() + "x" + p1Sheet.getHeight());
            } else {
                System.err.println("1P 스프라이트 파일을 찾을 수 없습니다: " + p1File.getPath());
            }
            
            // 2P 스프라이트 로드 (같은 파일 사용)
            File p2File = new File(basePath + "BlueBazzi.png");
            if (p2File.exists()) {
                BufferedImage p2Sheet = ImageIO.read(p2File);
                // 마젠타 배경을 투명하게 처리
                BufferedImage p2Transparent = makeColorTransparent(p2Sheet, 0xFF00FF);
                p2Sprites = loadSpriteSheet(p2Transparent);
                System.out.println("2P 스프라이트 로드 성공!");
            }
            
        } catch (IOException e) {
            System.err.println("스프라이트 로드 중 오류 발생!");
            e.printStackTrace();
        }
    }
    private BufferedImage[][] loadSpriteSheet(BufferedImage sheet) {
        if (sheet == null) {
            System.err.println("스프라이트 시트가 null입니다!");
            return null;
        }
        
        BufferedImage[][] sprites = new BufferedImage[SPRITE_ROWS][SPRITE_COLS];
        
        int spriteWidth = sheet.getWidth() / SPRITE_COLS;
        int spriteHeight = sheet.getHeight() / SPRITE_ROWS;
        
        System.out.println("개별 스프라이트 크기: " + spriteWidth + "x" + spriteHeight);
        
        for (int row = 0; row < SPRITE_ROWS; row++) {
            for (int col = 0; col < SPRITE_COLS; col++) {
                sprites[row][col] = sheet.getSubimage(
                    col * spriteWidth,
                    row * spriteHeight,
                    spriteWidth,
                    spriteHeight
                );
            }
        }
        
        System.out.println("스프라이트 시트 분할 완료: " + SPRITE_ROWS + "행 x " + SPRITE_COLS + "열");
        return sprites;
    }

    
    /**
     * 특정 색상을 투명하게 처리 (SpriteStore 방식)
     * @param image 원본 이미지
     * @param colorToRemove 투명화할 색상 (0xRRGGBB, 예: 0xFF00FF = 마젠타)
     * @return 투명 처리된 이미지
     */
    private BufferedImage makeColorTransparent(BufferedImage image, int colorToRemove) {
        BufferedImage transparent = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        
        // 비교할 RGB 값 (알파 제외)
        int targetRGB = colorToRemove & 0x00FFFFFF;
        
        // 모든 픽셀 순회
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int pixelRGB = pixel & 0x00FFFFFF;
                
                if (pixelRGB == targetRGB) {
                    // 마젠타 색상 → 완전 투명
                    transparent.setRGB(x, y, 0x00000000);
                } else {
                    // 다른 색상 → 불투명 유지
                    transparent.setRGB(x, y, pixel | 0xFF000000);
                }
            }
        }
        return transparent;
    }

    /**
     * 패널이 화면에 표시될 때 호출
     */
    @Override
    public void addNotify() {
        super.addNotify();
        loadSelectedMap();
        resetGame();
        p1UpPressed = p1DownPressed = p1LeftPressed = p1RightPressed = false;
        p2UpPressed = p2DownPressed = p2LeftPressed = p2RightPressed = false;
        startGameLoop();
        requestFocusInWindow();
    }

    /**
     * 게임 시작/재시작
     */
    public void startNewGame() {
        loadSelectedMap();
        loadSelectedCharacters();
        resetGame();
        p1UpPressed = p1DownPressed = p1LeftPressed = p1RightPressed = false;
        p2UpPressed = p2DownPressed = p2LeftPressed = p2RightPressed = false;
        startGameLoop();
        requestFocusInWindow();
        playInGameBGM();
        System.out.println("새 게임 시작! 1P: " + p1CharacterName + ", 2P: " + p2CharacterName);
    }

    /**
     * 인게임 BGM 재생
     */
    private void playInGameBGM() {
        String bgmPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator
                + "Crazy-Arcade-BGM-Patrit.wav";
        BGMPlayer.getInstance().loadAndPlay(bgmPath);
    }

    /**
     * 로비 BGM 재생
     */
    private void playLobbyBGM() {
        String bgmPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator
                + "Crazy-Arcade-BGM-Room.wav";
        BGMPlayer.getInstance().loadAndPlay(bgmPath);
    }

    /**
     * 선택된 캐릭터 로드
     */
    private void loadSelectedCharacters() {
        if (lobbyPanel != null) {
            p1CharacterName = lobbyPanel.getP1Character();
            p2CharacterName = lobbyPanel.getP2Character();
        }
    }

    /**
     * 선택된 맵 로드
     */
    private void loadSelectedMap() {
        if (lobbyPanel == null)
            return;
        String selectedMap = lobbyPanel.getSelectedMap();
        String mapFileName;
        if ("Map1".equals(selectedMap)) {
            mapFileName = "forest24.png";
        } else {
            mapFileName = "map2.png";
        }
        gameMap = new Map(mapFileName);
        System.out.println("선택된 맵 로드: " + selectedMap + " (" + mapFileName + ")");
        initPlayerPositions();
        repaint();
    }

    /**
     * 플레이어 초기 위치 설정
     */
    private void initPlayerPositions() {
        p1X = MAP_X + 40;
        p1Y = MAP_Y + 40;
        p2X = MAP_X + MAP_WIDTH - 80;
        p2Y = MAP_Y + MAP_HEIGHT - 80;
    }

    /**
     * 게임 루프 시작
     */
    private void startGameLoop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new javax.swing.Timer(16, e -> {
            updateGame();
            repaint();
        });
        gameTimer.start();
    }

    /**
     * 게임 루프 중지
     */
    private void stopGameLoop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    /**
     * 게임 상태 업데이트 (매 프레임 호출)
     */
    private void updateGame() {
        if (tileWidth == 0)
            tileWidth = MAP_WIDTH / TILE_COLS;
        if (tileHeight == 0)
            tileHeight = MAP_HEIGHT / TILE_ROWS;
    
        // 1. Player 1 이동 처리 (마지막에 누른 키만)
        int newP1X = p1X, newP1Y = p1Y;
        
        if (p1LastKey != null) {
            if (p1LastKey == GameSettings.p1_Up) {
                newP1Y = Math.max(MAP_Y, p1Y - MOVE_SPEED);
            } else if (p1LastKey == GameSettings.p1_Down) {
                newP1Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p1Y + MOVE_SPEED);
            } else if (p1LastKey == GameSettings.p1_Left) {
                newP1X = Math.max(MAP_X, p1X - MOVE_SPEED);
            } else if (p1LastKey == GameSettings.p1_Right) {
                newP1X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p1X + MOVE_SPEED);
            }
        }
    
        if (!isCollidingWithBlock(newP1X, p1Y, PLAYER_SIZE))
            p1X = newP1X;
        if (!isCollidingWithBlock(p1X, newP1Y, PLAYER_SIZE))
            p1Y = newP1Y;
    
        // 2. Player 2 이동 처리 (마지막에 누른 키만)
        int newP2X = p2X, newP2Y = p2Y;
        
        if (p2LastKey != null) {
            if (p2LastKey == GameSettings.p2_Up) {
                newP2Y = Math.max(MAP_Y, p2Y - MOVE_SPEED);
            } else if (p2LastKey == GameSettings.p2_Down) {
                newP2Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p2Y + MOVE_SPEED);
            } else if (p2LastKey == GameSettings.p2_Left) {
                newP2X = Math.max(MAP_X, p2X - MOVE_SPEED);
            } else if (p2LastKey == GameSettings.p2_Right) {
                newP2X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p2X + MOVE_SPEED);
            }
        }
    
        if (!isCollidingWithBlock(newP2X, p2Y, PLAYER_SIZE))
            p2X = newP2X;
        if (!isCollidingWithBlock(p2X, newP2Y, PLAYER_SIZE))
            p2Y = newP2Y;
    
        // 3. 스프라이트 애니메이션 업데이트
        updatePlayerAnimation(1);
        updatePlayerAnimation(2);
    
        // 4. 물풍선 처리
        updateBombs();
    
        // 5. 게임 타이머 및 결과 판정
        if (gameState == STATE_PLAYING) {
            long currentTime = System.currentTimeMillis();
            if (lastTimerUpdate == 0) {
                lastTimerUpdate = currentTime;
            }
            if (currentTime - lastTimerUpdate >= 1000) {
                remainingTime--;
                lastTimerUpdate = currentTime;
            }
            if (remainingTime <= 0) {
                gameState = STATE_DRAW;
                resultDisplayTime = System.currentTimeMillis();
            }
    
            if (!p1Alive && p2Alive) {
                gameState = STATE_P2_WIN;
                resultDisplayTime = System.currentTimeMillis();
            } else if (p1Alive && !p2Alive) {
                gameState = STATE_P1_WIN;
                resultDisplayTime = System.currentTimeMillis();
            } else if (!p1Alive && !p2Alive) {
                gameState = STATE_DRAW;
                resultDisplayTime = System.currentTimeMillis();
            }
        } else {
            if (System.currentTimeMillis() - resultDisplayTime >= RESULT_DISPLAY_DURATION) {
                stopGameLoop();
                resetGame();
                playLobbyBGM();
                mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
            }
        }
    }

    /**
     * 플레이어 애니메이션 업데이트
     */
    private void updatePlayerAnimation(int player) {
        if (player == 1) {
            // 1P 애니메이션 (움직이고 있을 때만)
            if (p1UpPressed || p1DownPressed || p1LeftPressed || p1RightPressed) {
                p1FrameCounter++;
                if (p1FrameCounter >= animationSpeed) {
                    p1FrameCounter = 0;
                    p1SpriteCol = (p1SpriteCol + 1) % 8; // 0~7 순환
                }
            }
        } else {
            // 2P 애니메이션
            if (p2UpPressed || p2DownPressed || p2LeftPressed || p2RightPressed) {
                p2FrameCounter++;
                if (p2FrameCounter >= animationSpeed) {
                    p2FrameCounter = 0;
                    p2SpriteCol = (p2SpriteCol + 1) % 8;
                }
            }
        }
    }

    /**
     * 게임 상태 초기화
     */
    private void resetGame() {
        gameState = STATE_PLAYING;
        remainingTime = GAME_TIME;
        lastTimerUpdate = 0;
        p1Alive = true;
        p2Alive = true;
        bombs.clear();
        initPlayerPositions();
        
        // 스프라이트 초기화
        p1SpriteRow = 3;
        p1SpriteCol = 0;
        p2SpriteRow = 3;
        p2SpriteCol = 0;
        p1FrameCounter = 0;
        p2FrameCounter = 0;
    }

    /**
     * 충돌 감지
     */
    private boolean isCollidingWithBlock(int x, int y, int size) {
        if (tiles == null)
            return false;

        int[][] corners = {
                { x + 5, y + 5 },
                { x + size - 5, y + 5 },
                { x + 5, y + size - 5 },
                { x + size - 5, y + size - 5 }
        };

        for (int[] corner : corners) {
            int col = (corner[0] - MAP_X) / tileWidth;
            int row = (corner[1] - MAP_Y) / tileHeight;

            if (row >= 0 && row < TILE_ROWS && col >= 0 && col < TILE_COLS) {
                Tile tile = tiles[row][col];
                if (tile != null) {
                    int itemIndex = tile.getItemIndex();
                    if (itemIndex == 3 || itemIndex == 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 물풍선 상태 업데이트
     */
    private void updateBombs() {
        java.util.Iterator<WaterBomb> it = bombs.iterator();
        while (it.hasNext()) {
            WaterBomb bomb = it.next();
            if (bomb.shouldExplode()) {
                explodeBomb(bomb);
                it.remove();
            }
        }
    }

    /**
     * 물풍선 폭발 로직
     */
    private void explodeBomb(WaterBomb bomb) {
        bomb.exploded = true;
        int[] dRows = { 0, -1, 1, 0, 0 };
        int[] dCols = { 0, 0, 0, -1, 1 };

        int bombRow = (bomb.y - MAP_Y) / tileHeight;
        int bombCol = (bomb.x - MAP_X) / tileWidth;

        for (int i = 0; i < dRows.length; i++) {
            int r = bombRow + dRows[i];
            int c = bombCol + dCols[i];

            if (r >= 0 && r < TILE_ROWS && c >= 0 && c < TILE_COLS) {
                Tile tile = tiles[r][c];
                if (tile != null) {
                    tile.breakBlock();
                }
            }

            int tileMinX = MAP_X + c * tileWidth;
            int tileMaxX = tileMinX + tileWidth;
            int tileMinY = MAP_Y + r * tileHeight;
            int tileMaxY = tileMinY + tileHeight;

            int p1CenterX = p1X + PLAYER_SIZE / 2;
            int p1CenterY = p1Y + PLAYER_SIZE / 2;
            if (p1Alive && p1CenterX >= tileMinX && p1CenterX <= tileMaxX &&
                    p1CenterY >= tileMinY && p1CenterY <= tileMaxY) {
                p1Alive = false;
                System.out.println("1P 사망!");
            }

            int p2CenterX = p2X + PLAYER_SIZE / 2;
            int p2CenterY = p2Y + PLAYER_SIZE / 2;
            if (p2Alive && p2CenterX >= tileMinX && p2CenterX <= tileMaxX &&
                    p2CenterY >= tileMinY && p2CenterY <= tileMaxY) {
                p2Alive = false;
                System.out.println("2P 사망!");
            }
        }
        System.out.println("물풍선 폭발! 위치: (" + bombCol + ", " + bombRow + ")");
    }

    /**
     * 물풍선 설치
     */
    private void placeBomb(int playerX, int playerY, int owner) {
        int centerX = playerX + PLAYER_SIZE / 2;
        int centerY = playerY + PLAYER_SIZE / 2;

        int col = (centerX - MAP_X) / tileWidth;
        int row = (centerY - MAP_Y) / tileHeight;

        if (row < 0 || row >= TILE_ROWS || col < 0 || col >= TILE_COLS)
            return;

        for (WaterBomb bomb : bombs) {
            if (bomb.row == row && bomb.col == col) {
                return;
            }
        }

        int bombX = MAP_X + col * tileWidth + tileWidth / 2;
        int bombY = MAP_Y + row * tileHeight + tileHeight / 2;

        bombs.add(new WaterBomb(bombX, bombY, row, col, owner));
        System.out.println("물풍선 설치! " + owner + "P, 위치: (" + col + ", " + row + ")");
    }

    /**
     * 키보드 눌림 처리 (스프라이트 방향 변경 포함)
     */
    private void handleKeyPressed(KeyEvent e) {
        int key = e.getKeyCode();
    
        if (key == KeyEvent.VK_ESCAPE) {
            stopGameLoop();
            mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
            return;
        }
    
        // 1P 조작키
        if (key == GameSettings.p1_Up) {
            if (!p1UpPressed) {
                p1SpriteRow = 1;
                p1SpriteCol = 0;
                p1FrameCounter = 0;
                p1LastKey = key; // 마지막 키 업데이트
            }
            p1UpPressed = true;
        }
        if (key == GameSettings.p1_Down) {
            if (!p1DownPressed) {
                p1SpriteRow = 3;
                p1SpriteCol = 0;
                p1FrameCounter = 0;
                p1LastKey = key;
            }
            p1DownPressed = true;
        }
        if (key == GameSettings.p1_Left) {
            if (!p1LeftPressed) {
                p1SpriteRow = 0;
                p1SpriteCol = 0;
                p1FrameCounter = 0;
                p1LastKey = key;
            }
            p1LeftPressed = true;
        }
        if (key == GameSettings.p1_Right) {
            if (!p1RightPressed) {
                p1SpriteRow = 2;
                p1SpriteCol = 0;
                p1FrameCounter = 0;
                p1LastKey = key;
            }
            p1RightPressed = true;
        }
        if (key == GameSettings.p1_Bomb) {
            if (p1Alive)
                placeBomb(p1X, p1Y, 1);
        }
    
        // 2P 조작키
        if (key == GameSettings.p2_Up) {
            if (!p2UpPressed) {
                p2SpriteRow = 1;
                p2SpriteCol = 0;
                p2FrameCounter = 0;
                p2LastKey = key;
            }
            p2UpPressed = true;
        }
        if (key == GameSettings.p2_Down) {
            if (!p2DownPressed) {
                p2SpriteRow = 3;
                p2SpriteCol = 0;
                p2FrameCounter = 0;
                p2LastKey = key;
            }
            p2DownPressed = true;
        }
        if (key == GameSettings.p2_Left) {
            if (!p2LeftPressed) {
                p2SpriteRow = 0;
                p2SpriteCol = 0;
                p2FrameCounter = 0;
                p2LastKey = key;
            }
            p2LeftPressed = true;
        }
        if (key == GameSettings.p2_Right) {
            if (!p2RightPressed) {
                p2SpriteRow = 2;
                p2SpriteCol = 0;
                p2FrameCounter = 0;
                p2LastKey = key;
            }
            p2RightPressed = true;
        }
        if (key == GameSettings.p2_Bomb) {
            if (p2Alive)
                placeBomb(p2X, p2Y, 2);
        }
    }
    
    /**
     * 키보드 뗌 처리 (마지막 키 리셋)
     */
    private void handleKeyReleased(KeyEvent e) {
        int key = e.getKeyCode();
    
        // 1P 이동 멈춤
        if (key == GameSettings.p1_Up) {
            p1UpPressed = false;
            if (p1LastKey != null && p1LastKey == key) p1LastKey = null;
        }
        if (key == GameSettings.p1_Down) {
            p1DownPressed = false;
            if (p1LastKey != null && p1LastKey == key) p1LastKey = null;
        }
        if (key == GameSettings.p1_Left) {
            p1LeftPressed = false;
            if (p1LastKey != null && p1LastKey == key) p1LastKey = null;
        }
        if (key == GameSettings.p1_Right) {
            p1RightPressed = false;
            if (p1LastKey != null && p1LastKey == key) p1LastKey = null;
        }
    
        // 2P 이동 멈춤
        if (key == GameSettings.p2_Up) {
            p2UpPressed = false;
            if (p2LastKey != null && p2LastKey == key) p2LastKey = null;
        }
        if (key == GameSettings.p2_Down) {
            p2DownPressed = false;
            if (p2LastKey != null && p2LastKey == key) p2LastKey = null;
        }
        if (key == GameSettings.p2_Left) {
            p2LeftPressed = false;
            if (p2LastKey != null && p2LastKey == key) p2LastKey = null;
        }
        if (key == GameSettings.p2_Right) {
            p2RightPressed = false;
            if (p2LastKey != null && p2LastKey == key) p2LastKey = null;
        }
    }

    /**
     * 맵 시스템 초기화
     */
    private void initMapSystem() {
        try {
            gameMap = new Map("map2.png");
            SpriteStore.init();
            loadTilesFromFile();
            System.out.println("맵 시스템 초기화 완료");
        } catch (Exception e) {
            System.err.println("맵 시스템 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * mapData.txt 파일에서 타일 정보 로드
     */
    private void loadTilesFromFile() {
        try {
            String path = System.getProperty("user.dir") + File.separator + "mapData.txt";
            int[][] data = new int[TILE_ROWS][TILE_COLS];
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            int row = 0;

            while ((line = br.readLine()) != null && row < TILE_ROWS) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("\\s+");
                for (int col = 0; col < TILE_COLS && col < parts.length; col++) {
                    data[row][col] = Integer.parseInt(parts[col]);
                }
                row++;
            }
            br.close();

            tiles = new Tile[TILE_ROWS][TILE_COLS];
            int cellWidth = MAP_WIDTH / TILE_COLS;
            int cellHeight = MAP_HEIGHT / TILE_ROWS;

            for (int r = 0; r < TILE_ROWS; r++) {
                for (int c = 0; c < TILE_COLS; c++) {
                    int centerX = MAP_X + c * cellWidth + cellWidth / 2;
                    int centerY = MAP_Y + r * cellHeight + cellHeight / 2;
                    boolean isBreakable = (data[r][c] == 4);
                    tiles[r][c] = new Tile(centerX, centerY, data[r][c], isBreakable);
                }
            }
            System.out.println("타일 로드 완료: " + TILE_ROWS + "x" + TILE_COLS);
        } catch (IOException e) {
            System.err.println("mapData.txt 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 결과 이미지 로드
     */
    private void loadResultImages() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;
            File winFile = new File(basePath + "win.bmp");
            if (winFile.exists()) {
                winImg = makeTransparent(ImageIO.read(winFile));
            }
            File drawFile = new File(basePath + "draw.bmp");
            if (drawFile.exists()) {
                drawImg = makeTransparent(ImageIO.read(drawFile));
            }
        } catch (IOException e) {
            System.err.println("결과 이미지 로드 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지의 마젠타 색상을 투명하게 변환
     */
    private Image makeTransparent(java.awt.image.BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        java.awt.image.BufferedImage result = new java.awt.image.BufferedImage(width, height,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y) & 0xFFFFFF;
                if (rgb == 0xFF00FF) {
                    result.setRGB(x, y, 0x00000000);
                } else {
                    result.setRGB(x, y, img.getRGB(x, y) | 0xFF000000);
                }
            }
        }
        return result;
    }

    /**
     * 화면 그리기
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(0, 0, getWidth(), getHeight());

        drawGameMap(g2);

        // 우측 정보창
        drawPlayerBox(g2, RIGHT_PANEL_X, 15, RIGHT_PANEL_WIDTH, 120, "1P", null, new Color(220, 80, 80));
        drawItemBox(g2, RIGHT_PANEL_X, 145, RIGHT_PANEL_WIDTH, 100);
        drawPlayerBox(g2, RIGHT_PANEL_X, 260, RIGHT_PANEL_WIDTH, 120, "2P", null, new Color(80, 80, 220));
        drawItemBox(g2, RIGHT_PANEL_X, 390, RIGHT_PANEL_WIDTH, 100);
        drawTimer(g2, RIGHT_PANEL_X, 495, RIGHT_PANEL_WIDTH, 40);
        drawExitButton(g2, RIGHT_PANEL_X, 540, RIGHT_PANEL_WIDTH, 45);

        if (gameState != STATE_PLAYING) {
            drawResultOverlay(g2);
        }
    }

    /**
     * 게임 맵 그리기 (스프라이트 애니메이션 포함)
     */
    private void drawGameMap(Graphics2D g2) {
        // 맵 배경
        if (gameMap != null) {
            gameMap.drawMap(g2, MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT);
        }

        // 타일 그리기
        if (tiles != null) {
            for (int r = 0; r < TILE_ROWS; r++) {
                for (int c = 0; c < TILE_COLS; c++) {
                    if (tiles[r][c] != null) {
                        tiles[r][c].draw(g2);
                    }
                }
            }
        }

        // 물풍선 그리기
        for (WaterBomb bomb : bombs) {
            bomb.draw(g2);
        }

        // 플레이어 1 그리기 (44x62 크기)
        if (p1Alive) {
            if (p1Sprites != null && p1Sprites[p1SpriteRow] != null && p1Sprites[p1SpriteRow][p1SpriteCol] != null) {
                BufferedImage p1Frame = p1Sprites[p1SpriteRow][p1SpriteCol];
                // 중앙 정렬: 판정 박스(40x40) 중심에 스프라이트(44x62)를 맞춤
                int drawX = p1X - (SPRITE_WIDTH - PLAYER_SIZE) / 2;
                int drawY = p1Y - (SPRITE_HEIGHT - PLAYER_SIZE);
                g2.drawImage(p1Frame, drawX, drawY, SPRITE_WIDTH, SPRITE_HEIGHT, null);
            } else {
                // 스프라이트 로드 실패 시 빨간 사각형으로 대체
                g2.setColor(Color.RED);
                g2.fillRect(p1X, p1Y, PLAYER_SIZE, PLAYER_SIZE);
                g2.setColor(Color.WHITE);
                g2.drawString("1P", p1X + 10, p1Y + 25);
            }
        }

        // 플레이어 2 그리기 (44x62 크기)
        if (p2Alive) {
            if (p2Sprites != null && p2Sprites[p2SpriteRow] != null && p2Sprites[p2SpriteRow][p2SpriteCol] != null) {
                BufferedImage p2Frame = p2Sprites[p2SpriteRow][p2SpriteCol];
                // 중앙 정렬
                int drawX = p2X - (SPRITE_WIDTH - PLAYER_SIZE) / 2;
                int drawY = p2Y - (SPRITE_HEIGHT - PLAYER_SIZE);
                g2.drawImage(p2Frame, drawX, drawY, SPRITE_WIDTH, SPRITE_HEIGHT, null);
            } else {
                // 스프라이트 로드 실패 시 파란 사각형으로 대체
                g2.setColor(Color.BLUE);
                g2.fillRect(p2X, p2Y, PLAYER_SIZE, PLAYER_SIZE);
                g2.setColor(Color.WHITE);
                g2.drawString("2P", p2X + 10, p2Y + 25);
            }
        }
    }

    /**
     * 플레이어 박스 그리기
     */
    private void drawPlayerBox(Graphics2D g2, int x, int y, int w, int h, String label, Image charImg,
            Color borderColor) {
        g2.setColor(new Color(40, 40, 40, 230));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        g2.drawString(label, x + 10, y + 25);

        if (charImg != null) {
            g2.drawImage(charImg, x + (w - 60) / 2, y + 35, 60, 60, null);
        }
    }

    /**
     * 아이템 박스 그리기
     */
    private void drawItemBox(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(40, 40, 40, 230));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        int slotSize = 35;
        int slotX = x + (w - slotSize) / 2;
        int slotY = y + (h - slotSize) / 2;
        g2.setColor(new Color(60, 60, 70));
        g2.fillRoundRect(slotX, slotY, slotSize, slotSize, 10, 10);
        g2.setColor(new Color(80, 80, 90));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(slotX, slotY, slotSize, slotSize, 10, 10);
    }

    /**
     * 타이머 표시
     */
    private void drawTimer(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(40, 40, 40, 230));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(255, 200, 0));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        String timeText = String.format("%d:%02d", minutes, seconds);

        if (remainingTime <= 30) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.WHITE);
        }

        g2.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (w - fm.stringWidth(timeText)) / 2;
        int textY = y + (h + fm.getAscent()) / 2 - 4;
        g2.drawString(timeText, textX, textY);
    }

    /**
     * 나가기 버튼 그리기
     */
    private void drawExitButton(Graphics2D g2, int x, int y, int w, int h) {
        GradientPaint gp = new GradientPaint(
                x, y, new Color(80, 160, 240),
                x, y + h, new Color(40, 120, 200));
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, 15, 15);

        g2.setColor(new Color(120, 200, 255));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, h, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        String text = "나가기";
        int textX = x + (w - fm.stringWidth(text)) / 2;
        int textY = y + (h + fm.getAscent()) / 2 - 4;
        g2.drawString(text, textX, textY);
    }

    /**
     * 게임 결과 오버레이 표시
     */
    private void drawResultOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        Image resultImg = null;
        if (gameState == STATE_DRAW) {
            resultImg = drawImg;
        } else if (gameState == STATE_P1_WIN || gameState == STATE_P2_WIN) {
            resultImg = winImg;
        }

        if (resultImg != null) {
            int imgW = resultImg.getWidth(this);
            int imgH = resultImg.getHeight(this);
            int imgX = (getWidth() - imgW) / 2;
            int imgY = (getHeight() - imgH) / 2;
            g2.drawImage(resultImg, imgX, imgY, this);
        }

        g2.setFont(new Font("맑은 고딕", Font.BOLD, 60));
        String text = "";
        Color textColor = Color.WHITE;

        if (gameState == STATE_P1_WIN) {
            text = "1P 승리!";
            textColor = new Color(255, 100, 100);
        } else if (gameState == STATE_P2_WIN) {
            text = "2P 승리!";
            textColor = new Color(100, 100, 255);
        } else if (gameState == STATE_DRAW) {
            text = "무승부";
            textColor = Color.LIGHT_GRAY;
        }

        if (!text.isEmpty()) {
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2 + 100;

            // 그림자
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(text, textX + 3, textY + 3);

            // 텍스트
            g2.setColor(textColor);
            g2.drawString(text, textX, textY);
        }
    }

    /**
     * 물풍선 클래스
     */
    private class WaterBomb {
        int x, y;
        int row, col;
        int owner;
        long placeTime;
        boolean exploded;

        public WaterBomb(int x, int y, int row, int col, int owner) {
            this.x = x;
            this.y = y;
            this.row = row;
            this.col = col;
            this.owner = owner;
            this.placeTime = System.currentTimeMillis();
            this.exploded = false;
        }

        public boolean shouldExplode() {
            return System.currentTimeMillis() - placeTime >= BOMB_TIMER;
        }

        public void draw(Graphics2D g2) {
            if (exploded)
                return;

            int drawX = x - BOMB_SIZE / 2;
            int drawY = y - BOMB_SIZE / 2;

            GradientPaint gp = new GradientPaint(
                    drawX, drawY, new Color(100, 150, 255),
                    drawX + BOMB_SIZE, drawY + BOMB_SIZE, new Color(50, 100, 200));
            g2.setPaint(gp);
            g2.fillOval(drawX, drawY, BOMB_SIZE, BOMB_SIZE);

            g2.setColor(new Color(30, 80, 180));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(drawX, drawY, BOMB_SIZE, BOMB_SIZE);

            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillOval(drawX + 8, drawY + 5, 12, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            String ownerText = owner == 1 ? "1P" : "2P";
            g2.drawString(ownerText, x - 8, y + 4);
        }
    }
}
