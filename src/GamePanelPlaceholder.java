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
 * 5. 캐릭터별 스탯 시스템 (물풍선 개수, 물줄기 범위, 속도)
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
    private String currentMapDataFile = "mapData2.txt"; // 현재 사용 중인 맵 데이터 파일

    // ========== [3] 캐릭터 시스템 (스프라이트 애니메이션) ==========
    private BufferedImage[][] p1Sprites;
    private BufferedImage[][] p2Sprites;
    private static final int SPRITE_ROWS = 4;
    private static final int SPRITE_COLS = 8;
    
    // 스프라이트 크기
    private static final int SPRITE_WIDTH = 44;
    private static final int SPRITE_HEIGHT = 62;
    
    // 스프라이트 애니메이션 상태
    private int p1SpriteRow = 3;
    private int p1SpriteCol = 0;
    private int p2SpriteRow = 3;
    private int p2SpriteCol = 0;
    
    private int animationSpeed = 3;
    private int p1FrameCounter = 0;
    private int p2FrameCounter = 0;
    
    // 현재 플레이어의 선택된 캐릭터 이름
    private String p1CharacterName = "배찌";
    private String p2CharacterName = "디지니"; // 다오 -> 디지니로 변경

    // ========== [3-2] 캐릭터 스탯 시스템 ==========
    // 플레이어 1 스탯
    private int p1BombCount = 1;      // 물풍선 설치 개수
    private int p1BombRange = 1;      // 물줄기 길이
    private int p1Speed = 4;          // 속도

    // 플레이어 2 스탯
    private int p2BombCount = 1;
    private int p2BombRange = 1;
    private int p2Speed = 4;

    // 캐릭터별 최대 스탯
    private int p1MaxBombCount = 6;
    private int p1MaxBombRange = 7;
    private int p1MaxSpeed = 9;

    private int p2MaxBombCount = 7;
    private int p2MaxBombRange = 9;
    private int p2MaxSpeed = 8;

    // 현재 설치된 물풍선 개수 추적
    private int p1CurrentBombs = 0;
    private int p2CurrentBombs = 0;

    // ========== [4] 좌표 및 이동 시스템 ==========
    private int p1X = 60;
    private int p1Y = 60;
    private int p2X = 520;
    private int p2Y = 520;
    
    private static final int PLAYER_SIZE = 40; // 충돌 판정 크기
    
    private boolean p1UpPressed, p1DownPressed, p1LeftPressed, p1RightPressed;
    private boolean p2UpPressed, p2DownPressed, p2LeftPressed, p2RightPressed;
    
    // 마지막으로 누른 키 추적
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
    private static final int MAP_WIDTH = 600; 
    private static final int MAP_HEIGHT = 520;
    private static final int RIGHT_PANEL_X = 630;
    private static final int RIGHT_PANEL_WIDTH = 155;
    // 아이템 충돌 플레그
    private boolean itemCollisionEnabled = false;
    private int startupFrameCount = 0;
    private static final int STARTUP_DELAY_FRAMES = 10; // 약 0.16초 (10프레임)

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
        loadCharacterSprites();
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
     * 캐릭터 스프라이트 시트 로드 (마젠타 배경 투명 처리)
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
                BufferedImage p1Transparent = makeColorTransparent(p1Sheet, 0xFF00FF);
                p1Sprites = loadSpriteSheet(p1Transparent);
                System.out.println("1P 스프라이트 로드 성공! 크기: " + p1Sheet.getWidth() + "x" + p1Sheet.getHeight());
            } else {
                System.err.println("1P 스프라이트 파일을 찾을 수 없습니다: " + p1File.getPath());
            }
            
            // 2P 스프라이트 로드
            File p2File = new File(basePath + "BlueBazzi.jpg");
            if (p2File.exists()) {
                BufferedImage p2Sheet = ImageIO.read(p2File);
                BufferedImage p2Transparent = makeColorTransparent(p2Sheet, 0xFF00FF);
                p2Sprites = loadSpriteSheet(p2Transparent);
                System.out.println("2P 스프라이트 로드 성공!");
            }
            
        } catch (IOException e) {
            System.err.println("스프라이트 로드 중 오류 발생!");
            e.printStackTrace();
        }
    }

    /**
     * 스프라이트 시트를 8x4 그리드로 분할
     */
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
     * 특정 색상을 투명하게 처리
     */
    private BufferedImage makeColorTransparent(BufferedImage image, int colorToRemove) {
        BufferedImage transparent = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        
        int targetRGB = colorToRemove & 0x00FFFFFF;
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int pixelRGB = pixel & 0x00FFFFFF;
                
                if (pixelRGB == targetRGB) {
                    transparent.setRGB(x, y, 0x00000000);
                } else {
                    transparent.setRGB(x, y, pixel | 0xFF000000);
                }
            }
        }
        return transparent;
    }

    /**
     * 캐릭터별 초기 스탯 설정
     */
    private void initCharacterStats() {
        // 1P 스탯 초기화
        if ("배찌".equals(p1CharacterName)) {
            p1BombCount = 1;
            p1BombRange = 1;
            p1Speed = 4;
            p1MaxBombCount = 6;
            p1MaxBombRange = 7;
            p1MaxSpeed = 9;
        } else if ("디지니".equals(p1CharacterName)) {
            p1BombCount = 2;
            p1BombRange = 1;
            p1Speed = 4;
            p1MaxBombCount = 7;
            p1MaxBombRange = 9;
            p1MaxSpeed = 8;
        }
        
        // 2P 스탯 초기화
        if ("배찌".equals(p2CharacterName)) {
            p2BombCount = 1;
            p2BombRange = 1;
            p2Speed = 4;
            p2MaxBombCount = 6;
            p2MaxBombRange = 7;
            p2MaxSpeed = 9;
        } else if ("디지니".equals(p2CharacterName)) {
            p2BombCount = 2;
            p2BombRange = 1;
            p2Speed = 4;
            p2MaxBombCount = 7;
            p2MaxBombRange = 9;
            p2MaxSpeed = 8;
        }
        
        p1CurrentBombs = 0;
        p2CurrentBombs = 0;
        
        System.out.println("1P(" + p1CharacterName + ") 스탯 - 개수:" + p1BombCount 
                         + ", 물줄기:" + p1BombRange + ", 속도:" + p1Speed);
        System.out.println("2P(" + p2CharacterName + ") 스탯 - 개수:" + p2BombCount 
                         + ", 물줄기:" + p2BombRange + ", 속도:" + p2Speed);
    }

    /**
     * 패널이 화면에 표시될 때 호출
     */
    @Override
    public void addNotify() {
        super.addNotify();
        loadSelectedMap();
        loadTilesFromFile(); // 타일 재로드 추가
        resetGame();
        p1UpPressed = p1DownPressed = p1LeftPressed = p1RightPressed = false;
        p2UpPressed = p2DownPressed = p2LeftPressed = p2RightPressed = false;
        p1LastKey = null;
        p2LastKey = null;
        itemCollisionEnabled = false; // 초기화
        startupFrameCount = 0;
        startGameLoop();
        requestFocusInWindow();
    }
    /**
     * 게임 시작/재시작
     */
    public void startNewGame() {
        loadSelectedMap();
        loadSelectedCharacters();
        
        // 타일 재로드 (맵 초기화)
        loadTilesFromFile();
        
        resetGame();
        p1UpPressed = p1DownPressed = p1LeftPressed = p1RightPressed = false;
        p2UpPressed = p2DownPressed = p2LeftPressed = p2RightPressed = false;
        p1LastKey = null;
        p2LastKey = null;
        
        // 게임 루프 시작 전에 강제로 한 프레임 대기 (아이템 즉시 획득 방지)
        // 초기 위치에서 아이템 획득 방지를 위한 딜레이 플래그 설정
        itemCollisionEnabled = false;
        
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
        String mapDataFileName;
        
        if ("Map1".equals(selectedMap)) {
            mapFileName = "forest24.png";
            mapDataFileName = "mapData1.txt"; // Map1 전용 데이터
        } else {
            mapFileName = "map2.png";
            mapDataFileName = "mapData2.txt"; // Map2 전용 데이터
        }
        
        gameMap = new Map(mapFileName);
        currentMapDataFile = mapDataFileName; // 현재 맵 데이터 파일 저장
        
        System.out.println("선택된 맵 로드: " + selectedMap + " (" + mapFileName + ", " + mapDataFileName + ")");
        
        loadTilesFromFile(); // 타일 로드
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

    // 게임 시작 초기 프레임 카운트 (아이템 즉시 획득 방지)
    if (!itemCollisionEnabled) {
        startupFrameCount++;
        if (startupFrameCount >= STARTUP_DELAY_FRAMES) {
            itemCollisionEnabled = true;
            startupFrameCount = 0;
            System.out.println("아이템 충돌 활성화");
        }
    }

    // 플레이어별 이동 속도 (스탯 기반)
    int p1MoveSpeed = p1Speed;
    int p2MoveSpeed = p2Speed;

    // 1. Player 1 이동 처리
    int newP1X = p1X, newP1Y = p1Y;
    
    if (p1LastKey != null) {
        if (p1LastKey == GameSettings.p1_Up) {
            newP1Y = Math.max(MAP_Y, p1Y - p1MoveSpeed);
        } else if (p1LastKey == GameSettings.p1_Down) {
            newP1Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p1Y + p1MoveSpeed);
        } else if (p1LastKey == GameSettings.p1_Left) {
            newP1X = Math.max(MAP_X, p1X - p1MoveSpeed);
        } else if (p1LastKey == GameSettings.p1_Right) {
            newP1X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p1X + p1MoveSpeed);
        }
    }

    if (!isCollidingWithBlock(newP1X, p1Y, PLAYER_SIZE))
        p1X = newP1X;
    if (!isCollidingWithBlock(p1X, newP1Y, PLAYER_SIZE))
        p1Y = newP1Y;

    // 2. Player 2 이동 처리
    int newP2X = p2X, newP2Y = p2Y;
    
    if (p2LastKey != null) {
        if (p2LastKey == GameSettings.p2_Up) {
            newP2Y = Math.max(MAP_Y, p2Y - p2MoveSpeed);
        } else if (p2LastKey == GameSettings.p2_Down) {
            newP2Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p2Y + p2MoveSpeed);
        } else if (p2LastKey == GameSettings.p2_Left) {
            newP2X = Math.max(MAP_X, p2X - p2MoveSpeed);
        } else if (p2LastKey == GameSettings.p2_Right) {
            newP2X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p2X + p2MoveSpeed);
        }
    }

    if (!isCollidingWithBlock(newP2X, p2Y, PLAYER_SIZE))
        p2X = newP2X;
    if (!isCollidingWithBlock(p2X, newP2Y, PLAYER_SIZE))
        p2Y = newP2Y;

    // 3. 아이템 충돌 체크 (활성화된 경우만)
    if (itemCollisionEnabled) {
        checkItemCollision();
    }

    // 4. 스프라이트 애니메이션 업데이트
    updatePlayerAnimation(1);
    updatePlayerAnimation(2);

    // 5. 물풍선 처리
    updateBombs();

    // 6. 게임 타이머 및 결과 판정
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
            if (p1UpPressed || p1DownPressed || p1LeftPressed || p1RightPressed) {
                p1FrameCounter++;
                if (p1FrameCounter >= animationSpeed) {
                    p1FrameCounter = 0;
                    p1SpriteCol = (p1SpriteCol + 1) % 8;
                }
            }
        } else {
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
        
        // 캐릭터 스탯 초기화
        initCharacterStats();
        
        // 타일 재로드 (맵 초기화) - 추가!
        loadTilesFromFile();
        
        System.out.println("게임 초기화 완료");
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
                
                // 폭발 시 설치된 물풍선 개수 감소
                if (bomb.owner == 1) {
                    p1CurrentBombs--;
                } else {
                    p2CurrentBombs--;
                }
                
                it.remove();
            }
        }
    }

    /**
     * 물풍선 폭발 로직
     */
    private void explodeBomb(WaterBomb bomb) {
        bomb.exploded = true;
        
        int bombRow = (bomb.y - MAP_Y) / tileHeight;
        int bombCol = (bomb.x - MAP_X) / tileWidth;
        
        // 중심 폭발
        explodeAtPosition(bombRow, bombCol);
        
        // 상하좌우로 bomb.range만큼 폭발
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            for (int i = 1; i <= bomb.range; i++) {
                int r = bombRow + dir[0] * i;
                int c = bombCol + dir[1] * i;
                
                if (r < 0 || r >= TILE_ROWS || c < 0 || c >= TILE_COLS) {
                    break;
                }
                
                // 벽에 막히면 중단
                if (tiles[r][c] != null && tiles[r][c].getItemIndex() == 3) {
                    break;
                }
                
                explodeAtPosition(r, c);
                
                // 파괴 가능한 블록이 있으면 파괴 후 중단
                if (tiles[r][c] != null && tiles[r][c].getItemIndex() == 4) {
                    tiles[r][c].breakBlock();
                    break;
                }
            }
        }
        
        System.out.println("물풍선 폭발! 위치: (" + bombCol + ", " + bombRow + "), 범위: " + bomb.range);
    }

    /**
     * 특정 위치에서 폭발 처리
     */
    private void explodeAtPosition(int row, int col) {
        if (row < 0 || row >= TILE_ROWS || col < 0 || col >= TILE_COLS) {
            return;
        }
        
        if (tiles[row][col] != null) {
            tiles[row][col].breakBlock();
        }
        
        int tileMinX = MAP_X + col * tileWidth;
        int tileMaxX = tileMinX + tileWidth;
        int tileMinY = MAP_Y + row * tileHeight;
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

    /**
     * 물풍선 설치
     */
    private void placeBomb(int playerX, int playerY, int owner) {
        // 설치 개수 제한 확인
        if (owner == 1 && p1CurrentBombs >= p1BombCount) {
            return;
        }
        if (owner == 2 && p2CurrentBombs >= p2BombCount) {
            return;
        }
        
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

        int range = (owner == 1) ? p1BombRange : p2BombRange;
        
        bombs.add(new WaterBomb(bombX, bombY, row, col, owner, range));
        
        if (owner == 1) {
            p1CurrentBombs++;
        } else {
            p2CurrentBombs++;
        }
        
        System.out.println("물풍선 설치! " + owner + "P, 위치: (" + col + ", " + row + "), 범위: " + range);
    }

    /**
     * 키보드 눌림 처리
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
                p1LastKey = key;
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
     * 키보드 뗌 처리
     */
    private void handleKeyReleased(KeyEvent e) {
        int key = e.getKeyCode();

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
            currentMapDataFile = "mapData2.txt"; // 기본 맵 데이터
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
            // currentMapDataFile에 저장된 파일명 사용
            String path = System.getProperty("user.dir") + File.separator + currentMapDataFile;
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
            System.out.println("타일 로드 완료: " + TILE_ROWS + "x" + TILE_COLS + " (파일: " + currentMapDataFile + ")");
        } catch (IOException e) {
            System.err.println("맵 데이터 로드 실패 (" + currentMapDataFile + "): " + e.getMessage());
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
     * 이미지의 마젠타 색상을 투명하게 변환 (결과 이미지용)
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
     * 게임 맵 그리기
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

        // 플레이어 1 그리기
        if (p1Alive) {
            if (p1Sprites != null && p1SpriteRow < SPRITE_ROWS && p1SpriteCol < SPRITE_COLS
                && p1Sprites[p1SpriteRow] != null && p1Sprites[p1SpriteRow][p1SpriteCol] != null) {
                
                BufferedImage p1Frame = p1Sprites[p1SpriteRow][p1SpriteCol];
                int drawX = p1X - (SPRITE_WIDTH - PLAYER_SIZE) / 2;
                int drawY = p1Y - (SPRITE_HEIGHT - PLAYER_SIZE);
                
                g2.drawImage(p1Frame, drawX, drawY, SPRITE_WIDTH, SPRITE_HEIGHT, null);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect(p1X, p1Y, PLAYER_SIZE, PLAYER_SIZE);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString("1P", p1X + 12, p1Y + 25);
            }
        }

        // 플레이어 2 그리기
        if (p2Alive) {
            if (p2Sprites != null && p2SpriteRow < SPRITE_ROWS && p2SpriteCol < SPRITE_COLS
                && p2Sprites[p2SpriteRow] != null && p2Sprites[p2SpriteRow][p2SpriteCol] != null) {
                
                BufferedImage p2Frame = p2Sprites[p2SpriteRow][p2SpriteCol];
                int drawX = p2X - (SPRITE_WIDTH - PLAYER_SIZE) / 2;
                int drawY = p2Y - (SPRITE_HEIGHT - PLAYER_SIZE);
                
                g2.drawImage(p2Frame, drawX, drawY, SPRITE_WIDTH, SPRITE_HEIGHT, null);
            } else {
                g2.setColor(Color.BLUE);
                g2.fillRect(p2X, p2Y, PLAYER_SIZE, PLAYER_SIZE);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString("2P", p2X + 12, p2Y + 25);
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

            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(text, textX + 3, textY + 3);

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
        int range;
        long placeTime;
        boolean exploded;

        public WaterBomb(int x, int y, int row, int col, int owner, int range) {
            this.x = x;
            this.y = y;
            this.row = row;
            this.col = col;
            this.owner = owner;
            this.range = range;
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
    /**
 * 아이템 충돌 체크 및 획득 처리
 * updateGame() 메서드에서 매 프레임마다 호출
 */
private void checkItemCollision() {
    if (tiles == null) return;
    
    // Player 1 아이템 체크
    if (p1Alive) {
        checkPlayerItemCollision(p1X, p1Y, 1);
    }
    
    // Player 2 아이템 체크
    if (p2Alive) {
        checkPlayerItemCollision(p2X, p2Y, 2);
    }
}

/**
 * 플레이어별 아이템 충돌 체크
 */
private void checkPlayerItemCollision(int playerX, int playerY, int playerNum) {
    // 플레이어 중심점 계산
    int centerX = playerX + PLAYER_SIZE / 2;
    int centerY = playerY + PLAYER_SIZE / 2;
    
    // 해당하는 타일 좌표
    int col = (centerX - MAP_X) / tileWidth;
    int row = (centerY - MAP_Y) / tileHeight;
    
    // 맵 범위 체크
    if (row < 0 || row >= TILE_ROWS || col < 0 || col >= TILE_COLS) {
        return;
    }
    
    Tile tile = tiles[row][col];
    if (tile == null) return;
    
    int itemIndex = tile.getItemIndex();
    
    // 아이템 타일 (0, 1, 2)과 충돌 시 처리
    if (itemIndex >= 0 && itemIndex <= 2) {
        // 아이템 획득 처리
        acquireItem(playerNum, itemIndex);
        
        // 타일을 5번 (빈 공간)으로 변경
        tile.setItemIndex(5);
        
        System.out.println(playerNum + "P가 아이템 " + itemIndex + " 획득!");
    }
}

    /**
     * 아이템 획득 처리
     * @param playerNum 플레이어 번호 (1 또는 2)
     * @param itemType 아이템 타입 (0: 물풍선, 1: 물줄기, 2: 속도)
     */
    private void acquireItem(int playerNum, int itemType) {
        if (playerNum == 1) {
            switch (itemType) {
                case 0: // 물풍선 개수 증가
                    if (p1BombCount < p1MaxBombCount) {
                        p1BombCount++;
                        System.out.println("1P 물풍선 개수: " + p1BombCount + "/" + p1MaxBombCount);
                    } else {
                        System.out.println("1P 물풍선 최대치!");
                    }
                    break;
                case 1: // 물줄기 길이 증가
                    if (p1BombRange < p1MaxBombRange) {
                        p1BombRange++;
                        System.out.println("1P 물줄기 길이: " + p1BombRange + "/" + p1MaxBombRange);
                    } else {
                        System.out.println("1P 물줄기 최대치!");
                    }
                    break;
                case 2: // 속도 증가
                    if (p1Speed < p1MaxSpeed) {
                        p1Speed++;
                        System.out.println("1P 속도: " + p1Speed + "/" + p1MaxSpeed);
                    } else {
                        System.out.println("1P 속도 최대치!");
                    }
                    break;
            }
        } else if (playerNum == 2) {
            switch (itemType) {
                case 0: // 물풍선 개수 증가
                    if (p2BombCount < p2MaxBombCount) {
                        p2BombCount++;
                        System.out.println("2P 물풍선 개수: " + p2BombCount + "/" + p2MaxBombCount);
                    } else {
                        System.out.println("2P 물풍선 최대치!");
                    }
                    break;
                case 1: // 물줄기 길이 증가
                    if (p2BombRange < p2MaxBombRange) {
                        p2BombRange++;
                        System.out.println("2P 물줄기 길이: " + p2BombRange + "/" + p2MaxBombRange);
                    } else {
                        System.out.println("2P 물줄기 최대치!");
                    }
                    break;
                case 2: // 속도 증가
                    if (p2Speed < p2MaxSpeed) {
                        p2Speed++;
                        System.out.println("2P 속도: " + p2Speed + "/" + p2MaxSpeed);
                    } else {
                        System.out.println("2P 속도 최대치!");
                    }
                    break;
            }
        }
    }
}
