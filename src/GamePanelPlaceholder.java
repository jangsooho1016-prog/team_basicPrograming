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
import java.util.ArrayList;
import java.util.List;
import java.awt.image.ImageProducer;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;

/**
 * ===== 폭탄 및 폭발 관련 내부 클래스 =====
 */

// 폭발 효과 클래스
class Explosion {
    public enum ExplosionType {
        CENTER, UP, DOWN, LEFT, RIGHT
    }
    
    private final int row, col;  // 타일 좌표
    private final long startTime;
    private final ExplosionType type;
    private boolean active = true;
    private static final int FRAME_DURATION_MS = 100;
    private static final int FRAME_COUNT = 5;
    
    public Explosion(int row, int col, long startTime, ExplosionType type) {
        this.row = row;
        this.col = col;
        this.startTime = startTime;
        this.type = type;
    }
    
    public void update(long currentTime) {
        if (currentTime - startTime >= FRAME_COUNT * FRAME_DURATION_MS) {
            this.active = false;
        }
    }
    
    public int getCurrentFrameIndex(long currentTime) {
        if (!active) return FRAME_COUNT - 1;
        long elapsedTime = currentTime - startTime;
        int frameIndex = (int)(elapsedTime / FRAME_DURATION_MS);
        return Math.min(frameIndex, FRAME_COUNT - 1);
    }
    
    public boolean isActive() { return active; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public ExplosionType getType() { return type; }
}

// 물풍선 클래스
class WaterBalloon {
    private final int row, col;  // 타일 좌표
    private final int range;
    private final long explodeTime;
    private final long startTime;
    private final int ownerPlayer;  // 1 또는 2
    private static final int FRAME_COUNT = 7;
    private boolean collisionEnabled = false;  // ← 추가: 충돌 판정 활성화 여부

    private final int installPlayerX;
    private final int installPlayerY;
    
    public WaterBalloon(int row, int col, long explodeTime, int range, int ownerPlayer, int playerX, int playerY) {
        this.row = row;
        this.col = col;
        this.explodeTime = explodeTime;
        this.range = range;
        this.ownerPlayer = ownerPlayer;
        this.startTime = System.currentTimeMillis();
        this.collisionEnabled = false;
        this.installPlayerX = playerX;
        this.installPlayerY = playerY;
    }
    
    
    public int getCurrentFrameIndex() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long totalDuration = explodeTime - startTime;
        
        if (totalDuration <= 0) return FRAME_COUNT - 1;
        
        // 폭탄이 터질 때까지 애니메이션 반복
        int frameIndex = (int)((elapsedTime % 1000) * FRAME_COUNT / 1000);
        return frameIndex % FRAME_COUNT;
    }
    
    // ← 추가: 충돌 판정 활성화
    public void enableCollision() {
        this.collisionEnabled = true;
    }
    
    // ← 추가: 충돌 판정 상태 확인
    public boolean isCollisionEnabled() {
        return collisionEnabled;
    }
    public int getInstallPlayerX() {
        return installPlayerX;
    }
    public int getInstallPlayerY() {
        return installPlayerY;
    }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public long getExplodeTime() { return explodeTime; }
    public int getRange() { return range; }
    public int getOwnerPlayer() { return ownerPlayer; }
}


/**
 * ===== 메인 게임 패널 =====
 */
public class GamePanelPlaceholder extends JPanel {
    
    // 1) UI 프레임 참조
    private CrazyArcade_UI mainFrame;
    private LobbyPanel lobbyPanel;
    
    // 2) 맵 및 타일 시스템
    private Map gameMap;
    private Tile[][] tiles;
    private static final int TILE_ROWS = 13;
    private static final int TILE_COLS = 15;
    private String currentMapDataFile = "mapData2.txt";
    
    // 3) 캐릭터 스프라이트 시스템
    private BufferedImage[][] p1Sprites;
    private BufferedImage[][] p2Sprites;
    private static final int SPRITE_ROWS = 4;
    private static final int SPRITE_COLS = 8;
    
    private int p1SpriteWidth = 44;
    private int p1SpriteHeight = 62;
    private int p2SpriteWidth = 44;
    private int p2SpriteHeight = 62;
    
    private int p1SpriteRow = 3;
    private int p1SpriteCol = 0;
    private int p2SpriteRow = 3;
    private int p2SpriteCol = 0;
    private int animationSpeed = 3;
    private int p1FrameCounter = 0;
    private int p2FrameCounter = 0;
    
    private String p1CharacterName = "배찌";
    private String p2CharacterName = "다오";
    
    // 3-2) 플레이어 능력치
    private int p1BombCount = 1;
    private int p1BombRange = 1;
    private int p1Speed = 4;
    
    private int p2BombCount = 1;
    private int p2BombRange = 1;
    private int p2Speed = 4;
    
    private int p1MaxBombCount = 6;
    private int p1MaxBombRange = 7;
    private int p1MaxSpeed = 9;
    
    private int p2MaxBombCount = 7;
    private int p2MaxBombRange = 9;
    private int p2MaxSpeed = 8;
    
    // 4) 플레이어 위치 및 입력 상태
    private int p1X = 60;
    private int p1Y = 60;
    private int p2X = 520;
    private int p2Y = 520;
    private static final int PLAYER_SIZE = 40;
    
    private boolean p1UpPressed, p1DownPressed, p1LeftPressed, p1RightPressed;
    private boolean p2UpPressed, p2DownPressed, p2LeftPressed, p2RightPressed;
    private Integer p1LastKey = null;
    private Integer p2LastKey = null;
    
    // 5) 게임 루프
    private javax.swing.Timer gameTimer;
    private int tileWidth;
    private int tileHeight;
    
    // 6) 게임 시간 및 상태
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
    
    // 7) 맵 영역 설정
    private static final int MAP_X = 15;
    private static final int MAP_Y = 15;
    private static final int MAP_WIDTH = 600;
    private static final int MAP_HEIGHT = 520;
    private static final int RIGHT_PANEL_X = 630;
    private static final int RIGHT_PANEL_WIDTH = 155;
    
    private boolean itemCollisionEnabled = false;
    private int startupFrameCount = 0;
    private static final int STARTUP_DELAY_FRAMES = 10;
    
    // ===== 폭탄 시스템 추가 =====
    private List<WaterBalloon> p1Balloons;
    private List<WaterBalloon> p2Balloons;
    private List<Explosion> explosions;
    private static final int BALLOON_DELAY_MS = 3800;
    
    // 폭탄/폭발 스프라이트
    private BufferedImage waterBalloonSpriteSheet;
    private BufferedImage trappedImage;
    private BufferedImage explosionCenterImage;
    private BufferedImage explosionUpImage;
    private BufferedImage explosionDownImage;
    private BufferedImage explosionLeftImage;
    private BufferedImage explosionRightImage;
    
    private boolean p1Trapped = false;
    private boolean p2Trapped = false;
    
    private static final int BALLOON_DRAW_SIZE = 40;
    private static final int BALLOON_FRAME_WIDTH = 40;
    private static final int BALLOON_FRAME_COUNT = 7;
    private static final int EXP_FRAME_COUNT = 5;
    private static final int EXP_FRAME_WIDTH = 40;

    // Trapped/Die 스프라이트
    private BufferedImage[][] trappedSprites;  // 2행 8열 (1행: 배찌, 2행: 디지니)
    private BufferedImage[][] dieSprites;      // 4행 8열 (RedBazzi, BlueBazzi, RedDizni, BlueDizni)

    // Trapped 시작 시간
    private long p1TrappedStartTime = 0;
    private long p2TrappedStartTime = 0;

    // Die 애니메이션 시작 시간
    private long p1DieStartTime = 0;
    private long p2DieStartTime = 0;

    // 상태 관리
    private static final int PLAYER_STATE_ALIVE = 0;
    private static final int PLAYER_STATE_TRAPPED = 1;
    private static final int PLAYER_STATE_DYING = 2;
    private static final int PLAYER_STATE_DEAD = 3;
    
    // 시간 상수
    private static final long TRAPPED_DURATION = 6000; // 6초
    private static final long DIE_ANIMATION_DURATION = 1000; // 1초

    // 애니메이션 프레임 설정
    private static final int TRAPPED_SPRITE_ROWS = 2;
    private static final int TRAPPED_SPRITE_COLS = 16;
    private static final int DIE_SPRITE_ROWS = 4;
    private static final int DIE_SPRITE_COLS = 8;

    // 추가: 플레이어 상태 변수
    private int p1State = PLAYER_STATE_ALIVE;
    private int p2State = PLAYER_STATE_ALIVE;
    

    public GamePanelPlaceholder(CrazyArcade_UI mainFrame) {
        this(mainFrame, null);
    }
    
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame, LobbyPanel lobbyPanel) {
        this.mainFrame = mainFrame;
        this.lobbyPanel = lobbyPanel;

        setLayout(null);
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(50, 50, 50));
        
        // 폭탄 리스트 초기화
        p1Balloons = new ArrayList<>();
        p2Balloons = new ArrayList<>();
        explosions = new ArrayList<>();
        
        loadCharacterSprites();
        loadBombSprites();
        loadResultImages();
        initMapSystem();
        initPlayerPositions();
        
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
    
    private void loadCharacterSprites() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;
            
            // ⭐ 1P 스프라이트 로드 (캐릭터에 따라 다른 파일)
            String p1FileName;
            if ("배찌".equals(p1CharacterName)) {
                p1FileName = "RedBazzi.png";
                p1SpriteWidth = 44;
                p1SpriteHeight = 62;
            } else if ("디지니".equals(p1CharacterName)) {
                p1FileName = "RedDizni.png";
                p1SpriteWidth = 42;
                p1SpriteHeight = 57;
            } else {
                p1FileName = "RedBazzi.png"; // 기본값
                p1SpriteWidth = 44;
                p1SpriteHeight = 62;
            }
            
            File p1File = new File(basePath + p1FileName);
            if (p1File.exists()) {
                BufferedImage p1Sheet = ImageIO.read(p1File);
                BufferedImage p1Transparent = makeColorTransparent(p1Sheet, 0xFF00FF);
                p1Sprites = loadSpriteSheet(p1Transparent, SPRITE_ROWS, SPRITE_COLS);
            } else {
                System.err.println("1P 스프라이트 파일 없음: " + p1FileName);
            }
            
            // ⭐ 2P 스프라이트 로드 (캐릭터에 따라 다른 파일)
            String p2FileName;
            if ("배찌".equals(p2CharacterName)) {
                p2FileName = "BlueBazzi.png";
                p2SpriteWidth = 44;
                p2SpriteHeight = 62;
            } else if ("디지니".equals(p2CharacterName)) {
                p2FileName = "BlueDizni.png";
                p2SpriteWidth = 42;
                p2SpriteHeight = 57;
            } else {
                p2FileName = "BlueBazzi.png"; // 기본값
                p2SpriteWidth = 44;
                p2SpriteHeight = 62;
            }
            
            File p2File = new File(basePath + p2FileName);
            if (p2File.exists()) {
                BufferedImage p2Sheet = ImageIO.read(p2File);
                BufferedImage p2Transparent = makeColorTransparent(p2Sheet, 0xFF00FF);
                p2Sprites = loadSpriteSheet(p2Transparent, SPRITE_ROWS, SPRITE_COLS);
            } else {
                System.err.println("2P 스프라이트 파일 없음: " + p2FileName);
            }
            
        } catch (IOException e) {
            System.err.println("캐릭터 스프라이트 로드 실패!");
            e.printStackTrace();
        }
    }
    
    private void loadBombSprites() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;
            
            waterBalloonSpriteSheet = loadAndTransformImage(basePath + "BlueBub.bmp", Color.BLACK);
            explosionCenterImage = loadAndTransformImage(basePath + "explosion_center.bmp", Color.BLACK);
            explosionUpImage = loadAndTransformImage(basePath + "explosion_up.bmp", Color.BLACK);
            explosionDownImage = loadAndTransformImage(basePath + "explosion_down.bmp", Color.BLACK);
            explosionLeftImage = loadAndTransformImage(basePath + "explosion_left.bmp", Color.BLACK);
            explosionRightImage = loadAndTransformImage(basePath + "explosion_right.bmp", Color.BLACK);
            
            // ⭐ Trapped 스프라이트 로드 (보라색 배경 제거)
            BufferedImage trappedSheet = loadAndTransformImage(basePath + "Trapped.png", new Color(255, 0, 255));  // 보라색 #FF00FF
            if (trappedSheet != null) {
                trappedSprites = loadSpriteSheet(trappedSheet, TRAPPED_SPRITE_ROWS, TRAPPED_SPRITE_COLS);
                System.out.println("Trapped 스프라이트 로드 완료");
            }
            
            // ⭐ Die 스프라이트 로드 (보라색 배경 제거)
            BufferedImage dieSheet = loadAndTransformImage(basePath + "Die.png", new Color(255, 0, 255));  // 보라색 #FF00FF
            if (dieSheet != null) {
                dieSprites = loadSpriteSheet(dieSheet, DIE_SPRITE_ROWS, DIE_SPRITE_COLS);
                System.out.println("Die 스프라이트 로드 완료");
            }
            
        } catch (IOException e) {
            System.err.println("폭탄 스프라이트 로드 실패!");
            e.printStackTrace();
        }
    }
    
    private BufferedImage loadAndTransformImage(String path, Color colorToMakeTransparent) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("파일 없음: " + path);
            return null;
        }
        
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            System.err.println("이미지 로드 실패: " + path);
            return null;
        }
        
        // BMP 파일인 경우 ARGB로 변환
        if (path.toLowerCase().endsWith(".bmp")) {
            BufferedImage convertedImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = convertedImage.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = convertedImage;
        }
        
        return transformColorToTransparency(image, colorToMakeTransparent);
    }
    
    private BufferedImage transformColorToTransparency(BufferedImage image, Color c1) {
        final int r1 = c1.getRed();
        final int g1 = c1.getGreen();
        final int b1 = c1.getBlue();
        
        ImageFilter filter = new RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
                int r = (rgb & 0xFF0000) >> 16;
                int g = (rgb & 0xFF00) >> 8;
                int b = (rgb & 0xFF);
                
                if (r == r1 && g == g1 && b == b1) {
                    return 0x00000000;  // 투명하게
                }
                return rgb;
            }
        };
        
        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        Image img = Toolkit.getDefaultToolkit().createImage(ip);
        
        BufferedImage dest = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return dest;
    }
    private BufferedImage[][] loadSpriteSheet(BufferedImage sheet) {
        return loadSpriteSheet(sheet, SPRITE_ROWS, SPRITE_COLS);
    }

    private BufferedImage[][] loadSpriteSheet(BufferedImage sheet, int rows, int cols) {
    if (sheet == null) {
        System.err.println("스프라이트 시트가 null입니다!");
        return null;
    }
    
    BufferedImage[][] sprites = new BufferedImage[rows][cols];
    int spriteWidth = sheet.getWidth() / cols;
    int spriteHeight = sheet.getHeight() / rows;
    
    for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
            sprites[row][col] = sheet.getSubimage(
                col * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight);
        }
    }
    
    return sprites;
}
    
    
    private BufferedImage makeColorTransparent(BufferedImage image, int colorToRemove) {
        BufferedImage transparent = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
        int targetRGB = colorToRemove & 0x00FFFFFF;
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int pixelRGB = pixel & 0x00FFFFFF;
                
                if (pixelRGB == targetRGB) {
                    transparent.setRGB(x, y, 0x00000000);
                } else {
                    transparent.setRGB(x, y, (pixel & 0xFF000000) | pixelRGB);
                }
            }
        }
        return transparent;
    }
    
    private void initCharacterStats() {
        if ("배찌".equals(p1CharacterName)) {
            p1BombCount = 1;
            p1BombRange = 1;
            p1Speed = 4;
            p1MaxBombCount = 6;
            p1MaxBombRange = 7;
            p1MaxSpeed = 9;
        } else if ("다오".equals(p1CharacterName)) {
            p1BombCount = 2;
            p1BombRange = 1;
            p1Speed = 4;
            p1MaxBombCount = 7;
            p1MaxBombRange = 9;
            p1MaxSpeed = 8;
        }
        
        if ("배찌".equals(p2CharacterName)) {
            p2BombCount = 1;
            p2BombRange = 1;
            p2Speed = 4;
            p2MaxBombCount = 6;
            p2MaxBombRange = 7;
            p2MaxSpeed = 9;
        } else if ("다오".equals(p2CharacterName)) {
            p2BombCount = 2;
            p2BombRange = 1;
            p2Speed = 4;
            p2MaxBombCount = 7;
            p2MaxBombRange = 9;
            p2MaxSpeed = 8;
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        loadSelectedMap();
        loadTilesFromFile();
        resetGame();
        p1UpPressed = p1DownPressed = p1LeftPressed = p1RightPressed = false;
        p2UpPressed = p2DownPressed = p2LeftPressed = p2RightPressed = false;
        p1LastKey = null;
        p2LastKey = null;
        itemCollisionEnabled = false;
        startupFrameCount = 0;
        startGameLoop();
        requestFocusInWindow();
    }
    
    public void startNewGame() {
        loadSelectedMap();
        loadSelectedCharacters();
        loadTilesFromFile();
        loadCharacterSprites();
        resetGame();
        p1UpPressed = p1DownPressed = p1LeftPressed = p1RightPressed = false;
        p2UpPressed = p2DownPressed = p2LeftPressed = p2RightPressed = false;
        p1LastKey = null;
        p2LastKey = null;
        itemCollisionEnabled = false;
        startGameLoop();
        requestFocusInWindow();
        playInGameBGM();
        System.out.println("게임 시작! 1P: " + p1CharacterName + ", 2P: " + p2CharacterName);
    }
    
    private void playInGameBGM() {
        String bgmPath = System.getProperty("user.dir") + File.separator 
            + "sound" + File.separator + "Crazy-Arcade-BGM-Patrit.wav";
        BGMPlayer.getInstance().loadAndPlay(bgmPath);
    }
    
    private void playLobbyBGM() {
        String bgmPath = System.getProperty("user.dir") + File.separator 
            + "sound" + File.separator + "Crazy-Arcade-BGM-Room.wav";
        BGMPlayer.getInstance().loadAndPlay(bgmPath);
    }
    
    private void loadSelectedCharacters() {
        if (lobbyPanel != null) {
            p1CharacterName = lobbyPanel.getP1Character();
            p2CharacterName = lobbyPanel.getP2Character();
        }
    }
    
    private void loadSelectedMap() {
        if (lobbyPanel == null) return;
        
        String selectedMap = lobbyPanel.getSelectedMap();
        String mapFileName;
        String mapDataFileName;
        
        if ("Map1".equals(selectedMap)) {
            mapFileName = "forest24.png";
            mapDataFileName = "mapData1.txt";
        } else {
            mapFileName = "map2.png";
            mapDataFileName = "mapData2.txt";
        }
        
        gameMap = new Map(mapFileName);
        currentMapDataFile = mapDataFileName;
        loadTilesFromFile();
        initPlayerPositions();
        repaint();
    }
    
    private void initPlayerPositions() {
        p1X = MAP_X + 40;
        p1Y = MAP_Y + 40;
        p2X = MAP_X + MAP_WIDTH - 80;
        p2Y = MAP_Y + MAP_HEIGHT - 80;
    }
    
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
    
    private void stopGameLoop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
    
    private void updateGame() {
        if (tileWidth == 0) tileWidth = MAP_WIDTH / TILE_COLS;
        if (tileHeight == 0) tileHeight = MAP_HEIGHT / TILE_ROWS;
        
        if (!itemCollisionEnabled) {
            startupFrameCount++;
            if (startupFrameCount >= STARTUP_DELAY_FRAMES) {
                itemCollisionEnabled = true;
                startupFrameCount = 0;
                System.out.println("아이템 충돌 활성화");
            }
        }
        
        updateBalloonCollisions();
        
        // ⭐ 수정: 상태에 따라 속도 조정
    int p1MoveSpeed;
        if (p1State == PLAYER_STATE_ALIVE) {
            p1MoveSpeed = p1Speed;
        } else if (p1State == PLAYER_STATE_TRAPPED) {
            p1MoveSpeed = 1;  // Trapped 상태에서만 속도 1
        } else {
            p1MoveSpeed = 0;  // Dying/Dead 상태에서는 이동 불가
        }
        
        int p2MoveSpeed;
        if (p2State == PLAYER_STATE_ALIVE) {
            p2MoveSpeed = p2Speed;
        } else if (p2State == PLAYER_STATE_TRAPPED) {
            p2MoveSpeed = 1;  // Trapped 상태에서만 속도 1
        } else {
            p2MoveSpeed = 0;  // Dying/Dead 상태에서는 이동 불가
        }
        
        // 1. Player 1 이동 처리
        int newP1X = p1X, newP1Y = p1Y;
        
        // ⭐ 수정: ALIVE 또는 TRAPPED 상태일 때만 이동 가능
        if ((p1State == PLAYER_STATE_ALIVE || p1State == PLAYER_STATE_TRAPPED) && p1LastKey != null) {
            if (p1LastKey == GameSettings.p1_Up) {
                newP1Y = Math.max(MAP_Y, p1Y - p1MoveSpeed);
            } else if (p1LastKey == GameSettings.p1_Down) {
                newP1Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p1Y + p1MoveSpeed);
            } else if (p1LastKey == GameSettings.p1_Left) {
                newP1X = Math.max(MAP_X, p1X - p1MoveSpeed);
            } else if (p1LastKey == GameSettings.p1_Right) {
                newP1X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p1X + p1MoveSpeed);
            }
            
            if (!isCollidingWithBlock(newP1X, p1Y, PLAYER_SIZE) && !isCollidingWithBalloon(newP1X, p1Y, PLAYER_SIZE))
                p1X = newP1X;
            if (!isCollidingWithBlock(p1X, newP1Y, PLAYER_SIZE) && !isCollidingWithBalloon(p1X, newP1Y, PLAYER_SIZE))
                p1Y = newP1Y;
        }
    
    // 2. Player 2 이동 처리
    int newP2X = p2X, newP2Y = p2Y;
    
    // ⭐ 수정: ALIVE 또는 TRAPPED 상태일 때만 이동 가능
    if ((p2State == PLAYER_STATE_ALIVE || p2State == PLAYER_STATE_TRAPPED) && p2LastKey != null) {
        if (p2LastKey == GameSettings.p2_Up) {
            newP2Y = Math.max(MAP_Y, p2Y - p2MoveSpeed);
        } else if (p2LastKey == GameSettings.p2_Down) {
            newP2Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p2Y + p2MoveSpeed);
        } else if (p2LastKey == GameSettings.p2_Right) {
            newP2X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p2X + p2MoveSpeed);
        } else if (p2LastKey == GameSettings.p2_Left) {
            newP2X = Math.max(MAP_X, p2X - p2MoveSpeed);
        }
        
        if (!isCollidingWithBlock(newP2X, p2Y, PLAYER_SIZE) && !isCollidingWithBalloon(newP2X, p2Y, PLAYER_SIZE))
            p2X = newP2X;
        if (!isCollidingWithBlock(p2X, newP2Y, PLAYER_SIZE) && !isCollidingWithBalloon(p2X, newP2Y, PLAYER_SIZE))
            p2Y = newP2Y;
    }
        
        // 3. 아이템 충돌 체크 (살아있을 때만)
        if (itemCollisionEnabled) {
            if (p1State == PLAYER_STATE_ALIVE) {
                checkPlayerItemCollision(p1X, p1Y, 1);
            }
            if (p2State == PLAYER_STATE_ALIVE) {
                checkPlayerItemCollision(p2X, p2Y, 2);
            }
        }
        
        // 4. 스프라이트 애니메이션 업데이트 (살아있을 때만)
        if (p1State == PLAYER_STATE_ALIVE) {
            updatePlayerAnimation(1);
        }
        if (p2State == PLAYER_STATE_ALIVE) {
            updatePlayerAnimation(2);
        }
        
        // 5. 폭탄 시스템 업데이트
        updateBombSystem();
        checkPlayerToPlayerCollision();
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
            
            // ⭐ 수정: Dead 상태 체크
            if (p1State == PLAYER_STATE_DEAD && p2State != PLAYER_STATE_DEAD) {
                gameState = STATE_P2_WIN;
                resultDisplayTime = System.currentTimeMillis();
            } else if (p2State == PLAYER_STATE_DEAD && p1State != PLAYER_STATE_DEAD) {
                gameState = STATE_P1_WIN;
                resultDisplayTime = System.currentTimeMillis();
            } else if (p1State == PLAYER_STATE_DEAD && p2State == PLAYER_STATE_DEAD) {
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
    
    
    
    
    // ===== 폭탄 시스템 업데이트 =====
    private void updateBombSystem() {
        long currentTime = System.currentTimeMillis();
        
        // ⭐ 수정: 복사본 리스트 생성하여 안전하게 순회
        List<WaterBalloon> p1ToExplode = new ArrayList<>();
        List<WaterBalloon> p2ToExplode = new ArrayList<>();
        
        // 1P 폭탄 폭발 확인 (터질 물풍선만 수집)
        for (WaterBalloon balloon : p1Balloons) {
            if (currentTime >= balloon.getExplodeTime()) {
                p1ToExplode.add(balloon);
            }
        }
        
        // 2P 폭탄 폭발 확인 (터질 물풍선만 수집)
        for (WaterBalloon balloon : p2Balloons) {
            if (currentTime >= balloon.getExplodeTime()) {
                p2ToExplode.add(balloon);
            }
        }
        
        // ⭐ 실제 폭발 처리 (수집한 물풍선들을 한 번에 처리)
        for (WaterBalloon balloon : p1ToExplode) {
            // 연쇄 폭발로 이미 삭제되었을 수 있으므로 다시 확인
            if (p1Balloons.contains(balloon)) {
                p1Balloons.remove(balloon);
                createExplosion(balloon.getRow(), balloon.getCol(), balloon.getRange());
            }
        }
        
        for (WaterBalloon balloon : p2ToExplode) {
            // 연쇄 폭발로 이미 삭제되었을 수 있으므로 다시 확인
            if (p2Balloons.contains(balloon)) {
                p2Balloons.remove(balloon);
                createExplosion(balloon.getRow(), balloon.getCol(), balloon.getRange());
            }
        }
        
        // 폭발 애니메이션 업데이트
        for (int i = explosions.size() - 1; i >= 0; i--) {
            if (i >= explosions.size()) continue;
            Explosion explosion = explosions.get(i);
            explosion.update(currentTime);
            if (!explosion.isActive()) {
                explosions.remove(i);
            }
        }
        
        // 플레이어가 폭발에 맞았는지 확인
        checkExplosionCollision();
    }
    
    private void createExplosion(int centerRow, int centerCol, int range) {
        playSoundEffect("explosionBallon_less.wav");
        long startTime = System.currentTimeMillis();
        
        // 중심 폭발
        explosions.add(new Explosion(centerRow, centerCol, startTime, Explosion.ExplosionType.CENTER));
        
        // 중심 위치의 블록 파괴
        if (tiles != null && centerRow >= 0 && centerRow < TILE_ROWS && centerCol >= 0 && centerCol < TILE_COLS) {
            Tile centerTile = tiles[centerRow][centerCol];
            if (centerTile != null && centerTile.isBreakable()) {
                centerTile.breakBlock();
            }
        }
        
        // 4방향 폭발
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        List<int[]> chainExplosions = new ArrayList<>();
        
        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];
            
            Explosion.ExplosionType currentExplosionType;
            if (dRow == -1) currentExplosionType = Explosion.ExplosionType.UP;
            else if (dRow == 1) currentExplosionType = Explosion.ExplosionType.DOWN;
            else if (dCol == -1) currentExplosionType = Explosion.ExplosionType.LEFT;
            else currentExplosionType = Explosion.ExplosionType.RIGHT;
            
            for (int i = 1; i <= range; i++) {
                int newRow = centerRow + dRow * i;
                int newCol = centerCol + dCol * i;
                
                if (newRow < 0 || newRow >= TILE_ROWS || newCol < 0 || newCol >= TILE_COLS) {
                    break;
                }
                
                explosions.add(new Explosion(newRow, newCol, startTime, currentExplosionType));
                
                if (tiles != null) {
                    Tile tile = tiles[newRow][newCol];
                    if (tile != null) {
                        int itemIndex = tile.getItemIndex();

                        
                        // 파괴 가능한 블록(3)에 막힘 → 파괴 후 전파 중단
                        if (itemIndex == 3) {
                            if (tile.isBreakable()) {
                                tile.breakBlock();
                            }
                            break;
                        }
                        if (itemIndex >= 0 && itemIndex <= 2) {
                            tile.breakBlock();
                            //break;
                        }
                    }
                }
                
                // 연쇄 폭발 확인
                WaterBalloon hitBalloon = getBalloonAt(newRow, newCol);
                if (hitBalloon != null) {
                    chainExplosions.add(new int[]{newRow, newCol, hitBalloon.getRange()});
                    break;
                }
            }
        }
        
        // 연쇄 폭발 처리
        for (int[] explosion : chainExplosions) {
            // getBalloonAt으로 다시 확인해서 존재하는 경우에만 삭제
            WaterBalloon balloon = getBalloonAt(explosion[0], explosion[1]);
            if (balloon != null) {
                removeBalloonAt(explosion[0], explosion[1]);
                createExplosion(explosion[0], explosion[1], explosion[2]);
            }
        }
    }
    
    private WaterBalloon getBalloonAt(int row, int col) {
        for (WaterBalloon balloon : p1Balloons) {
            if (balloon.getRow() == row && balloon.getCol() == col) {
                return balloon;
            }
        }
        for (WaterBalloon balloon : p2Balloons) {
            if (balloon.getRow() == row && balloon.getCol() == col) {
                return balloon;
            }
        }
        return null;
    }
    
    private void removeBalloonAt(int row, int col) {
        for (int i = p1Balloons.size() - 1; i >= 0; i--) {
            if (i >= p1Balloons.size()) continue;
            WaterBalloon balloon = p1Balloons.get(i);
            if (balloon.getRow() == row && balloon.getCol() == col) {
                p1Balloons.remove(i);
                return;
            }
        }
        for (int i = p2Balloons.size() - 1; i >= 0; i--) {
            if (i >= p2Balloons.size()) continue;
            WaterBalloon balloon = p2Balloons.get(i);
            if (balloon.getRow() == row && balloon.getCol() == col) {
                p2Balloons.remove(i);
                return;
            }
        }
    }
    
    private void checkExplosionCollision() {
        if (p1State == PLAYER_STATE_DEAD && p2State == PLAYER_STATE_DEAD) return;
        
        long currentTime = System.currentTimeMillis();
        
        for (Explosion exp : explosions) {
            int expRow = exp.getRow();
            int expCol = exp.getCol();
            
            // 1P 충돌 확인
            if (p1State == PLAYER_STATE_ALIVE) {
                int p1TileRow = getTileRow(p1Y + PLAYER_SIZE / 2);
                int p1TileCol = getTileCol(p1X + PLAYER_SIZE / 2);
                
                if (p1TileRow == expRow && p1TileCol == expCol) {
                    // ⭐ Trapped 상태로 전환
                    p1State = PLAYER_STATE_TRAPPED;
                    p1Trapped = true;
                    p1TrappedStartTime = currentTime;
                    System.out.println("1P Trapped!");
                }
            }
            
            // 2P 충돌 확인
            if (p2State == PLAYER_STATE_ALIVE) {
                int p2TileRow = getTileRow(p2Y + PLAYER_SIZE / 2);
                int p2TileCol = getTileCol(p2X + PLAYER_SIZE / 2);
                
                if (p2TileRow == expRow && p2TileCol == expCol) {
                    // ⭐ Trapped 상태로 전환
                    p2State = PLAYER_STATE_TRAPPED;
                    p2Trapped = true;
                    p2TrappedStartTime = currentTime;
                    System.out.println("2P Trapped!");
                }
            }
        }
        
        // ⭐ Trapped 상태 체크 (6초 경과 시 Die 애니메이션 시작)
        if (p1State == PLAYER_STATE_TRAPPED) {
            if (currentTime - p1TrappedStartTime >= TRAPPED_DURATION) {
                p1State = PLAYER_STATE_DYING;
                p1DieStartTime = currentTime;
                System.out.println("1P Dying...");
            }
        }
        
        if (p2State == PLAYER_STATE_TRAPPED) {
            if (currentTime - p2TrappedStartTime >= TRAPPED_DURATION) {
                p2State = PLAYER_STATE_DYING;
                p2DieStartTime = currentTime;
                System.out.println("2P Dying...");
            }
        }
        
        // ⭐ Dying 상태 체크 (1초 경과 시 Dead 상태로 전환)
        if (p1State == PLAYER_STATE_DYING) {
            if (currentTime - p1DieStartTime >= DIE_ANIMATION_DURATION) {
                p1State = PLAYER_STATE_DEAD;
                p1Alive = false;
                System.out.println("1P Dead!");
            }
        }
        
        if (p2State == PLAYER_STATE_DYING) {
            if (currentTime - p2DieStartTime >= DIE_ANIMATION_DURATION) {
                p2State = PLAYER_STATE_DEAD;
                p2Alive = false;
                System.out.println("2P Dead!");
            }
        }
    }
    
    private int getTileRow(int pixelY) {
        return (pixelY - MAP_Y) / tileHeight;
    }
    
    private int getTileCol(int pixelX) {
        return (pixelX - MAP_X) / tileWidth;
    }
    
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
    
    private void resetGame() {
        gameState = STATE_PLAYING;
        remainingTime = GAME_TIME;
        lastTimerUpdate = 0;
        
        p1Alive = true;
        p2Alive = true;
        p1Trapped = false;
        p2Trapped = false;
        
        // ⭐ 상태 초기화
        p1State = PLAYER_STATE_ALIVE;
        p2State = PLAYER_STATE_ALIVE;
        p1TrappedStartTime = 0;
        p2TrappedStartTime = 0;
        p1DieStartTime = 0;
        p2DieStartTime = 0;
        
        initPlayerPositions();
        p1SpriteRow = 3;
        p1SpriteCol = 0;
        p2SpriteRow = 3;
        p2SpriteCol = 0;
        p1FrameCounter = 0;
        p2FrameCounter = 0;
        
        initCharacterStats();
        loadTilesFromFile();
        
        p1Balloons.clear();
        p2Balloons.clear();
        explosions.clear();
        
        System.out.println("게임 초기화 완료");
    }
    
    private boolean isCollidingWithBlock(int x, int y, int size) {
        if (tiles == null) return false;
        
        int[][] corners = {
            {x + 5, y + 5},
            {x + size - 5, y + 5},
            {x + 5, y + size - 5},
            {x + size - 5, y + size - 5}
        };
        
        for (int[] corner : corners) {
            int col = (corner[0] - MAP_X) / tileWidth;
            int row = (corner[1] - MAP_Y) / tileHeight;
            
            if (row < 0 || row >= TILE_ROWS || col < 0 || col >= TILE_COLS) continue;
            
            Tile tile = tiles[row][col];
            if (tile != null) {
                int itemIndex = tile.getItemIndex();
                if (itemIndex == 3) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void handleKeyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_ESCAPE) {
            stopGameLoop();
            mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
            return;
        }
        
        // 1P 조작
        if (key == GameSettings.p1_Up) {
            if (!p1UpPressed) {
                p1SpriteRow = 1;
                p1SpriteCol = 0;
                p1FrameCounter = 0;
            }
            p1LastKey = key;
            p1UpPressed = true;
        }
        if (key == GameSettings.p1_Down) {
            if (!p1DownPressed) {
                p1SpriteRow = 3;
                p1SpriteCol = 0;
                p1FrameCounter = 0;
            }
            p1LastKey = key;
            p1DownPressed = true;
        }
        if (key == GameSettings.p1_Left) {
            if (!p1LeftPressed) {
                p1SpriteRow = 0;
                p1SpriteCol = 0;
                p1FrameCounter = 0;
            }
            p1LastKey = key;
            p1LeftPressed = true;
        }
        if (key == GameSettings.p1_Right) {
            if (!p1RightPressed) {
                p1SpriteRow = 2;
                p1SpriteCol = 0;
                p1FrameCounter = 0;
            }
            p1LastKey = key;
            p1RightPressed = true;
        }
        
        // 1P 폭탄 설치
        if (key == GameSettings.p1_Bomb) {
            placeWaterBalloon(1);
        }
        
        // 2P 조작
        if (key == GameSettings.p2_Up) {
            if (!p2UpPressed) {
                p2SpriteRow = 1;
                p2SpriteCol = 0;
                p2FrameCounter = 0;
            }
            p2LastKey = key;
            p2UpPressed = true;
        }
        if (key == GameSettings.p2_Down) {
            if (!p2DownPressed) {
                p2SpriteRow = 3;
                p2SpriteCol = 0;
                p2FrameCounter = 0;
            }
            p2LastKey = key;
            p2DownPressed = true;
        }
        if (key == GameSettings.p2_Left) {
            if (!p2LeftPressed) {
                p2SpriteRow = 0;
                p2SpriteCol = 0;
                p2FrameCounter = 0;
            }
            p2LastKey = key;
            p2LeftPressed = true;
        }
        if (key == GameSettings.p2_Right) {
            if (!p2RightPressed) {
                p2SpriteRow = 2;
                p2SpriteCol = 0;
                p2FrameCounter = 0;
            }
            p2LastKey = key;
            p2RightPressed = true;
        }
        
        // 2P 폭탄 설치
        if (key == GameSettings.p2_Bomb) {
            placeWaterBalloon(2);
        }
    }
    
    // 물풍선 설치
    private void placeWaterBalloon(int player) {
        if (player == 1) {
            if (p1State != PLAYER_STATE_ALIVE) return;
            if (p1Balloons.size() >= p1BombCount) return;
            
            int p1TileRow = getTileRow(p1Y + PLAYER_SIZE / 2);
            int p1TileCol = getTileCol(p1X + PLAYER_SIZE / 2);
            
            // 이미 폭탄이 있는지 확인
            if (getBalloonAt(p1TileRow, p1TileCol) != null) return;
            
            long explodeTime = System.currentTimeMillis() + BALLOON_DELAY_MS;
            WaterBalloon newBalloon = new WaterBalloon(p1TileRow, p1TileCol, explodeTime, p1BombRange, 1, p1X, p1Y);
            p1Balloons.add(newBalloon);
            playSoundEffect("installationBallon.wav");
            System.out.println("1P 물풍선 설치: (" + p1TileRow + ", " + p1TileCol + ")");
            
        } else if (player == 2) {
            if (p2State != PLAYER_STATE_ALIVE) return;
            if (p2Balloons.size() >= p2BombCount) return;
            
            int p2TileRow = getTileRow(p2Y + PLAYER_SIZE / 2);
            int p2TileCol = getTileCol(p2X + PLAYER_SIZE / 2);
            
            // 이미 폭탄이 있는지 확인
            if (getBalloonAt(p2TileRow, p2TileCol) != null) return;
            
            long explodeTime = System.currentTimeMillis() + BALLOON_DELAY_MS;
            WaterBalloon newBalloon = new WaterBalloon(p2TileRow, p2TileCol, explodeTime, p2BombRange, 2, p2X, p2Y);
            p2Balloons.add(newBalloon);
            playSoundEffect("installationBallon.wav");
            System.out.println("2P 물풍선 설치: (" + p2TileRow + ", " + p2TileCol + ")");
        }
    }
    
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
    
    private void initMapSystem() {
        try {
            gameMap = new Map("map2.png");
            currentMapDataFile = "mapData2.txt";
            SpriteStore.init();
            loadTilesFromFile();
            System.out.println("맵 시스템 초기화 완료");
        } catch (Exception e) {
            System.err.println("맵 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadTilesFromFile() {
        try {
            String path = System.getProperty("user.dir") + File.separator + currentMapDataFile;
            int[][] data = new int[TILE_ROWS][TILE_COLS];
            
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            int row = 0;
            
            while ((line = br.readLine()) != null && row < TILE_ROWS) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // 공백 또는 쉼표로 구분 (유연하게 처리)
                String[] parts = line.split("[,\\s]+");  // ← 수정: 쉼표와 공백 모두 처리
                
                for (int col = 0; col < TILE_COLS && col < parts.length; col++) {
                    try {
                        data[row][col] = Integer.parseInt(parts[col].trim());  // ← trim() 추가
                    } catch (NumberFormatException e) {
                        System.err.println("숫자 파싱 오류 - 행:" + row + ", 열:" + col + ", 값:'" + parts[col] + "'");
                        data[row][col] = 0;  // 기본값 설정
                    }
                }
                row++;
            }
            br.close();
            
            // 타일 객체 생성
            tiles = new Tile[TILE_ROWS][TILE_COLS];
            int cellWidth = MAP_WIDTH / TILE_COLS;
            int cellHeight = MAP_HEIGHT / TILE_ROWS;
            
            for (int r = 0; r < TILE_ROWS; r++) {
                for (int c = 0; c < TILE_COLS; c++) {
                    int centerX = MAP_X + c * cellWidth + cellWidth / 2;
                    int centerY = MAP_Y + r * cellHeight + cellHeight / 2;
                    boolean isBreakable = (data[r][c] >= 0 && data[r][c] <= 3);
                    tiles[r][c] = new Tile(centerX, centerY, data[r][c], isBreakable);
                }
            }
            
            System.out.println("타일 로드 완료: " + TILE_ROWS + "x" + TILE_COLS + " (파일: " + currentMapDataFile + ")");
            
        } catch (IOException e) {
            System.err.println("맵 데이터 로드 실패 (" + currentMapDataFile + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
    
    private Image makeTransparent(java.awt.image.BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        java.awt.image.BufferedImage result = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y) & 0xFFFFFF;
                if (rgb == 0xFF00FF) {
                    result.setRGB(x, y, 0x00000000);
                } else {
                    result.setRGB(x, y, (img.getRGB(x, y) & 0xFF000000) | rgb);
                }
            }
        }
        return result;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 배경 채우기
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // 맵 그리기
        drawGameMap(g2);
        
        // ==========================================
        // ⭐ 수정: 프로필용 이미지 준비 (정면 모습 사용)
        // ==========================================
        BufferedImage p1ProfileImg = null;
        BufferedImage p2ProfileImg = null;
    
        // 1P 이미지 (3행 0열: 정면 서 있는 모습)
        if (p1Sprites != null && p1Sprites.length > 3 && p1Sprites[3] != null) {
            p1ProfileImg = p1Sprites[3][0]; 
        }
        
        // 2P 이미지 (3행 0열: 정면 서 있는 모습)
        if (p2Sprites != null && p2Sprites.length > 3 && p2Sprites[3] != null) {
            p2ProfileImg = p2Sprites[3][0];
        }
    
        // ==========================================
        // ⭐ 수정: drawPlayerBox 호출 시 이미지 전달 (null -> p1ProfileImg)
        // ==========================================
        
        // 1P 상자 그리기
        drawPlayerBox(g2, RIGHT_PANEL_X, 15, RIGHT_PANEL_WIDTH, 120, "1P", p1ProfileImg, new Color(220, 80, 80));
        
        // 아이템 상자 등 나머지 그리기...
        drawItemBox(g2, RIGHT_PANEL_X, 145, RIGHT_PANEL_WIDTH, 100);
        
        // 2P 상자 그리기 (null -> p2ProfileImg)
        drawPlayerBox(g2, RIGHT_PANEL_X, 260, RIGHT_PANEL_WIDTH, 120, "2P", p2ProfileImg, new Color(80, 80, 220));
        
        drawItemBox(g2, RIGHT_PANEL_X, 390, RIGHT_PANEL_WIDTH, 100);
        drawTimer(g2, RIGHT_PANEL_X, 495, RIGHT_PANEL_WIDTH, 40);
        drawExitButton(g2, RIGHT_PANEL_X, 540, RIGHT_PANEL_WIDTH, 45);
        
        if (gameState != STATE_PLAYING) {
            drawResultOverlay(g2);
        }
    }
    
    private void drawGameMap(Graphics2D g2) {
        // 배경 맵 그리기
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
        drawBalloons(g2);
        
        // 폭발 그리기
        drawExplosions(g2);
        
        // 플레이어 그리기
        drawPlayers(g2);
    }
    
    private void drawBalloons(Graphics2D g2) {
        long currentTime = System.currentTimeMillis();
        
        // 1P 물풍선 그리기
        for (WaterBalloon balloon : p1Balloons) {
            int tileX = MAP_X + balloon.getCol() * tileWidth;
            int tileY = MAP_Y + balloon.getRow() * tileHeight;
            int centerX = tileX + tileWidth / 2;
            int centerY = tileY + tileHeight / 2;
            
            int drawX = centerX - BALLOON_DRAW_SIZE / 2;
            int drawY = centerY - BALLOON_DRAW_SIZE / 2;
            
            int frameIndex = balloon.getCurrentFrameIndex() % BALLOON_FRAME_COUNT;
            int srcX = frameIndex * BALLOON_FRAME_WIDTH;
            
            if (waterBalloonSpriteSheet != null) {
                g2.drawImage(waterBalloonSpriteSheet,
                    drawX, drawY, drawX + BALLOON_DRAW_SIZE, drawY + BALLOON_DRAW_SIZE,
                    srcX, 0, srcX + BALLOON_FRAME_WIDTH, waterBalloonSpriteSheet.getHeight(),
                    null);
            } else {
                g2.setColor(new Color(0, 191, 255));
                g2.fillOval(drawX, drawY, BALLOON_DRAW_SIZE, BALLOON_DRAW_SIZE);
            }
        }
        
        // 2P 물풍선 그리기
        for (WaterBalloon balloon : p2Balloons) {
            int tileX = MAP_X + balloon.getCol() * tileWidth;
            int tileY = MAP_Y + balloon.getRow() * tileHeight;
            int centerX = tileX + tileWidth / 2;
            int centerY = tileY + tileHeight / 2;
            
            int drawX = centerX - BALLOON_DRAW_SIZE / 2;
            int drawY = centerY - BALLOON_DRAW_SIZE / 2;
            
            int frameIndex = balloon.getCurrentFrameIndex() % BALLOON_FRAME_COUNT;
            int srcX = frameIndex * BALLOON_FRAME_WIDTH;
            
            if (waterBalloonSpriteSheet != null) {
                g2.drawImage(waterBalloonSpriteSheet,
                    drawX, drawY, drawX + BALLOON_DRAW_SIZE, drawY + BALLOON_DRAW_SIZE,
                    srcX, 0, srcX + BALLOON_FRAME_WIDTH, waterBalloonSpriteSheet.getHeight(),
                    null);
            } else {
                g2.setColor(new Color(0, 191, 255));
                g2.fillOval(drawX, drawY, BALLOON_DRAW_SIZE, BALLOON_DRAW_SIZE);
            }
        }
    }
    
    private void drawExplosions(Graphics2D g2) {
        long currentTime = System.currentTimeMillis();
        
        for (Explosion explosion : explosions) {
            int tileX = MAP_X + explosion.getCol() * tileWidth;
            int tileY = MAP_Y + explosion.getRow() * tileHeight;
            
            BufferedImage currentImage = null;
            switch (explosion.getType()) {
                case CENTER:
                    currentImage = explosionCenterImage;
                    break;
                case UP:
                    currentImage = explosionUpImage;
                    break;
                case DOWN:
                    currentImage = explosionDownImage;
                    break;
                case LEFT:
                    currentImage = explosionLeftImage;
                    break;
                case RIGHT:
                    currentImage = explosionRightImage;
                    break;
            }
            
            if (currentImage != null) {
                int frameIndex = explosion.getCurrentFrameIndex(currentTime);
                int srcX = frameIndex * EXP_FRAME_WIDTH;
                
                g2.drawImage(currentImage,
                    tileX, tileY, tileX + tileWidth, tileY + tileHeight,
                    srcX, 0, srcX + EXP_FRAME_WIDTH, currentImage.getHeight(),
                    null);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect(tileX, tileY, tileWidth, tileHeight);
            }
        }
    }
    
    private void drawPlayers(Graphics2D g2) {
        long currentTime = System.currentTimeMillis();
        
        // ===== 1P 그리기 =====
        if (p1State != PLAYER_STATE_DEAD) {
            BufferedImage p1Frame = null;
            int drawWidth = p1SpriteWidth;
            int drawHeight = p1SpriteHeight;
            
            if (p1State == PLAYER_STATE_ALIVE) {
                // 일반 상태
                if (p1Sprites != null && p1SpriteRow < SPRITE_ROWS && p1SpriteCol < SPRITE_COLS
                        && p1Sprites[p1SpriteRow] != null && p1Sprites[p1SpriteRow][p1SpriteCol] != null) {
                    p1Frame = p1Sprites[p1SpriteRow][p1SpriteCol];
                }
            } else if (p1State == PLAYER_STATE_TRAPPED && trappedSprites != null) {
                // ⭐ Trapped 상태 (애니메이션)
                long elapsed = currentTime - p1TrappedStartTime;
                int frameIndex = (int)((elapsed % 1000) * TRAPPED_SPRITE_COLS / 1000); // 1초당 8프레임
                
                // 1행: 배찌, 2행: 디지니
                int row = "배찌".equals(p1CharacterName) ? 0 : 1;
                
                if (row < TRAPPED_SPRITE_ROWS && frameIndex < TRAPPED_SPRITE_COLS) {
                    p1Frame = trappedSprites[row][frameIndex];
                    drawWidth = p1Frame.getWidth();
                    drawHeight = p1Frame.getHeight();
                }
            } else if (p1State == PLAYER_STATE_DYING && dieSprites != null) {
                // ⭐ Die 애니메이션
                long elapsed = currentTime - p1DieStartTime;
                int frameIndex = (int)((elapsed % DIE_ANIMATION_DURATION) * DIE_SPRITE_COLS / DIE_ANIMATION_DURATION);
                
                // 1행: RedBazzi, 2행: BlueBazzi, 3행: RedDizni, 4행: BlueDizni
                int row;
                if ("배찌".equals(p1CharacterName)) {
                    row = 0; // RedBazzi (1P는 항상 Red)
                } else {
                    row = 2; // RedDizni
                }
                
                if (row < DIE_SPRITE_ROWS && frameIndex < DIE_SPRITE_COLS) {
                    p1Frame = dieSprites[row][frameIndex];
                    drawWidth = p1Frame.getWidth();
                    drawHeight = p1Frame.getHeight();
                }
            }
            
            if (p1Frame != null) {
                int drawX = p1X - (drawWidth - PLAYER_SIZE) / 2;
                int drawY = p1Y - (drawHeight - PLAYER_SIZE);
                g2.drawImage(p1Frame, drawX, drawY, drawWidth, drawHeight, null);
            } else {
                // 기본 사각형
                g2.setColor(Color.RED);
                g2.fillRect(p1X, p1Y, PLAYER_SIZE, PLAYER_SIZE);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString("1P", p1X + 12, p1Y + 25);
            }
        }
        
        // ===== 2P 그리기 =====
        if (p2State != PLAYER_STATE_DEAD) {
            BufferedImage p2Frame = null;
            int drawWidth = p2SpriteWidth;
            int drawHeight = p2SpriteHeight;
            
            if (p2State == PLAYER_STATE_ALIVE) {
                // 일반 상태
                if (p2Sprites != null && p2SpriteRow < SPRITE_ROWS && p2SpriteCol < SPRITE_COLS
                        && p2Sprites[p2SpriteRow] != null && p2Sprites[p2SpriteRow][p2SpriteCol] != null) {
                    p2Frame = p2Sprites[p2SpriteRow][p2SpriteCol];
                }
            } else if (p2State == PLAYER_STATE_TRAPPED && trappedSprites != null) {
                // ⭐ Trapped 상태 (애니메이션)
                long elapsed = currentTime - p2TrappedStartTime;
                int frameIndex = (int)((elapsed % 1000) * TRAPPED_SPRITE_COLS / 1000);
                
                // 1행: 배찌, 2행: 디지니
                int row = "배찌".equals(p2CharacterName) ? 0 : 1;
                
                if (row < TRAPPED_SPRITE_ROWS && frameIndex < TRAPPED_SPRITE_COLS) {
                    p2Frame = trappedSprites[row][frameIndex];
                    drawWidth = p2Frame.getWidth();
                    drawHeight = p2Frame.getHeight();
                }
            } else if (p2State == PLAYER_STATE_DYING && dieSprites != null) {
                // ⭐ Die 애니메이션
                long elapsed = currentTime - p2DieStartTime;
                int frameIndex = (int)((elapsed % DIE_ANIMATION_DURATION) * DIE_SPRITE_COLS / DIE_ANIMATION_DURATION);
                
                // 1행: RedBazzi, 2행: BlueBazzi, 3행: RedDizni, 4행: BlueDizni
                int row;
                if ("배찌".equals(p2CharacterName)) {
                    row = 1; // BlueBazzi (2P는 항상 Blue)
                } else {
                    row = 3; // BlueDizni
                }
                
                if (row < DIE_SPRITE_ROWS && frameIndex < DIE_SPRITE_COLS) {
                    p2Frame = dieSprites[row][frameIndex];
                    drawWidth = p2Frame.getWidth();
                    drawHeight = p2Frame.getHeight();
                }
            }
            
            if (p2Frame != null) {
                int drawX = p2X - (drawWidth - PLAYER_SIZE) / 2;
                int drawY = p2Y - (drawHeight - PLAYER_SIZE);
                g2.drawImage(p2Frame, drawX, drawY, drawWidth, drawHeight, null);
            } else {
                // 기본 사각형
                g2.setColor(Color.BLUE);
                g2.fillRect(p2X, p2Y, PLAYER_SIZE, PLAYER_SIZE);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString("2P", p2X + 12, p2Y + 25);
            }
        }
    }
    
    
    private void drawPlayerBox(Graphics2D g2, int x, int y, int w, int h, String label, Image charImg, Color borderColor) {
        g2.setColor(new Color(40, 40, 40, 230));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, h, 10, 10);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        g2.drawString(label, x + 10, y + 25);
        
        if (charImg != null) {
            // 상자 너비(w)의 중간에서 이미지 크기(60)의 절반만큼 뺌 -> 중앙 정렬
            int imgX = x + (w - 60) / 2; 
            g2.drawImage(charImg, imgX, y + 35, 45, 60, null);
        }
    }
    
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
    
    private void drawExitButton(Graphics2D g2, int x, int y, int w, int h) {
        GradientPaint gp = new GradientPaint(
            x, y, new Color(80, 160, 240),
            x, y + h, new Color(40, 120, 200)
        );
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
    
    private void checkItemCollision() {
        if (tiles == null) return;
        
        if (p1Alive) {
            checkPlayerItemCollision(p1X, p1Y, 1);
        }
        if (p2Alive) {
            checkPlayerItemCollision(p2X, p2Y, 2);
        }
    }
    
    private void checkPlayerItemCollision(int playerX, int playerY, int playerNum) {
        int centerX = playerX + PLAYER_SIZE / 2;
        int centerY = playerY + PLAYER_SIZE / 2;
        
        int col = (centerX - MAP_X) / tileWidth;
        int row = (centerY - MAP_Y) / tileHeight;
        
        if (row < 0 || row >= TILE_ROWS || col < 0 || col >= TILE_COLS) {
            return;
        }
        
        Tile tile = tiles[row][col];
        if (tile == null) return;
        
        int itemIndex = tile.getItemIndex();
        if (itemIndex >= 0 && itemIndex <= 2) {
            acquireItem(playerNum, itemIndex);
            tile.setItemIndex(5);
            System.out.println(playerNum + "P가 아이템 " + itemIndex + "을 획득!");
        }
    }
    
    private void acquireItem(int playerNum, int itemType) {
        if (playerNum == 1) {
            switch (itemType) {
                case 0:  // 물풍선 개수 증가
                    if (p1BombCount < p1MaxBombCount) {
                        p1BombCount++;
                        System.out.println("1P 물풍선 개수: " + p1BombCount + "/" + p1MaxBombCount);
                    } else {
                        System.out.println("1P 물풍선 개수 최대치!");
                    }
                    break;
                case 1:  // 물풍선 범위 증가
                    if (p1BombRange < p1MaxBombRange) {
                        p1BombRange++;
                        System.out.println("1P 물풍선 범위: " + p1BombRange + "/" + p1MaxBombRange);
                    } else {
                        System.out.println("1P 물풍선 범위 최대치!");
                    }
                    break;
                case 2:  // 이동속도 증가
                    if (p1Speed < p1MaxSpeed) {
                        p1Speed++;
                        System.out.println("1P 이동속도: " + p1Speed + "/" + p1MaxSpeed);
                    } else {
                        System.out.println("1P 이동속도 최대치!");
                    }
                    break;
            }
        } else if (playerNum == 2) {
            switch (itemType) {
                case 0:  // 물풍선 개수 증가
                    if (p2BombCount < p2MaxBombCount) {
                        p2BombCount++;
                        System.out.println("2P 물풍선 개수: " + p2BombCount + "/" + p2MaxBombCount);
                    } else {
                        System.out.println("2P 물풍선 개수 최대치!");
                    }
                    break;
                case 1:  // 물풍선 범위 증가
                    if (p2BombRange < p2MaxBombRange) {
                        p2BombRange++;
                        System.out.println("2P 물풍선 범위: " + p2BombRange + "/" + p2MaxBombRange);
                    } else {
                        System.out.println("2P 물풍선 범위 최대치!");
                    }
                    break;
                case 2:  // 이동속도 증가
                    if (p2Speed < p2MaxSpeed) {
                        p2Speed++;
                        System.out.println("2P 이동속도: " + p2Speed + "/" + p2MaxSpeed);
                    } else {
                        System.out.println("2P 이동속도 최대치!");
                    }
                    break;
            }
        }
    }
    private void updateBalloonCollisions() {
        // 충분히 멀어진 거리 기준 (타일 크기의 60% 정도)
        final int SAFE_DISTANCE = (int)(Math.min(tileWidth, tileHeight) * 1.2);
        
        // 1P 물풍선 체크
        for (WaterBalloon balloon : p1Balloons) {
            if (!balloon.isCollisionEnabled()) {
                // 1P가 설치 위치에서 충분히 멀어졌는지 확인 (거리 기반)
                int dx = (p1X + PLAYER_SIZE / 2) - (balloon.getInstallPlayerX() + PLAYER_SIZE / 2);
                int dy = (p1Y + PLAYER_SIZE / 2) - (balloon.getInstallPlayerY() + PLAYER_SIZE / 2);
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance > SAFE_DISTANCE) {
                    balloon.enableCollision();
                    System.out.println("1P 물풍선 충돌 활성화: 거리=" + (int)distance + " (기준: " + SAFE_DISTANCE + ")");
                }
            }
        }
        
        // 2P 물풍선 체크
        for (WaterBalloon balloon : p2Balloons) {
            if (!balloon.isCollisionEnabled()) {
                // 2P가 설치 위치에서 충분히 멀어졌는지 확인 (거리 기반)
                int dx = (p2X + PLAYER_SIZE / 2) - (balloon.getInstallPlayerX() + PLAYER_SIZE / 2);
                int dy = (p2Y + PLAYER_SIZE / 2) - (balloon.getInstallPlayerY() + PLAYER_SIZE / 2);
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance > SAFE_DISTANCE) {
                    balloon.enableCollision();
                    System.out.println("2P 물풍선 충돌 활성화: 거리=" + (int)distance + " (기준: " + SAFE_DISTANCE + ")");
                }
            }
        }
    }
    
    /**
     * 물풍선과의 충돌 감지 (충돌 판정이 활성화된 물풍선만)
     */
    private boolean isCollidingWithBalloon(int x, int y, int size) {
        if (tileWidth == 0 || tileHeight == 0) return false;
        
        int[][] corners = {
            {x + 5, y + 5},
            {x + size - 5, y + 5},
            {x + 5, y + size - 5},
            {x + size - 5, y + size - 5}
        };
        
        for (int[] corner : corners) {
            int col = (corner[0] - MAP_X) / tileWidth;
            int row = (corner[1] - MAP_Y) / tileHeight;
            
            if (row < 0 || row >= TILE_ROWS || col < 0 || col >= TILE_COLS) continue;
            
            // 1P 물풍선 체크
            for (WaterBalloon balloon : p1Balloons) {
                if (balloon.isCollisionEnabled() && 
                    balloon.getRow() == row && balloon.getCol() == col) {
                    return true;
                }
            }
            
            // 2P 물풍선 체크
            for (WaterBalloon balloon : p2Balloons) {
                if (balloon.isCollisionEnabled() && 
                    balloon.getRow() == row && balloon.getCol() == col) {
                    return true;
                }
            }
        }
        
        return false;
    }
    private void playSoundEffect(String soundFileName) {
        try {
            String soundPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator + soundFileName;
            // BGMPlayer를 사용하여 효과음 재생 (효과음은 한 번만 재생하므로 반복 없이)
            
            File soundFile = new File(soundPath);
            if (soundFile.exists()) {
                javax.sound.sampled.AudioInputStream audioInputStream = javax.sound.sampled.AudioSystem.getAudioInputStream(soundFile);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } else {
                System.err.println("사운드 파일 없음: " + soundFileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 캐릭터 간 충돌 확인 및 처형(Kill) 처리
private void checkPlayerToPlayerCollision() {
    // 둘 중 하나라도 죽었거나 없으면 체크 안 함
    if (p1State == PLAYER_STATE_DEAD || p2State == PLAYER_STATE_DEAD) return;

    // 히트박스 계산 (약간 여유를 둠)
    Rectangle p1Bounds = new Rectangle(p1X, p1Y, PLAYER_SIZE, PLAYER_SIZE);
    Rectangle p2Bounds = new Rectangle(p2X, p2Y, PLAYER_SIZE, PLAYER_SIZE);

    // 두 캐릭터가 겹쳤는지 확인
    if (p1Bounds.intersects(p2Bounds)) {
        
        // 상황 1: 1P가 갇혀있고, 2P가 살아있을 때 -> 1P 사망
        if (p1State == PLAYER_STATE_TRAPPED && p2State == PLAYER_STATE_ALIVE) {
            killPlayer(1);
        }
        
        // 상황 2: 2P가 갇혀있고, 1P가 살아있을 때 -> 2P 사망
        if (p2State == PLAYER_STATE_TRAPPED && p1State == PLAYER_STATE_ALIVE) {
            killPlayer(2);
        }
    }
}

// 플레이어 즉시 사망 처리 (터지는 효과)
private void killPlayer(int playerNum) {
    long currentTime = System.currentTimeMillis();
    
    if (playerNum == 1) {
        if (p1State != PLAYER_STATE_DYING && p1State != PLAYER_STATE_DEAD) {
            p1State = PLAYER_STATE_DYING;
            p1DieStartTime = currentTime;
            System.out.println("1P가 터졌습니다! (접촉 사망)");
        }
    } else if (playerNum == 2) {
        if (p2State != PLAYER_STATE_DYING && p2State != PLAYER_STATE_DEAD) {
            p2State = PLAYER_STATE_DYING;
            p2DieStartTime = currentTime;
            System.out.println("2P가 터졌습니다! (접촉 사망)");
        }
    }
}

}
