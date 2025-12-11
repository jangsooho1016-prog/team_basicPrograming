import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

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

    // 게임 맵 이미지 (image/InGame/map2.bmp) - 기존 호환용
    private Image gameMapImage;

    // 팀원 맵 시스템 (새로 통합)
    private Map gameMap;

    // 타일 시스템 (블록/아이템 배치)
    private Tile[][] tiles;
    private static final int TILE_ROWS = 13; // 타일 행 수
    private static final int TILE_COLS = 15; // 타일 열 수

    // 캐릭터 이미지 (1P: 배찌, 2P: 다오)
    private Image bazziImg, daoImg;

    // 실제 선택된 캐릭터 (랜덤 적용 후)
    private String p1CharacterName = "배찌";
    private String p2CharacterName = "다오";

    // ========== 플레이어 위치 및 이동 ==========
    // Player 1 (배찌) 위치
    private int p1X = 60; // 1P X 좌표
    private int p1Y = 60; // 1P Y 좌표

    // Player 2 (다오) 위치
    private int p2X = 520; // 2P X 좌표
    private int p2Y = 520; // 2P Y 좌표

    // 캐릭터 크기 및 이동 속도
    private static final int PLAYER_SIZE = 40; // 캐릭터 크기
    private static final int MOVE_SPEED = 5; // 이동 속도 (픽셀)

    // 키 입력 상태 (동시 입력 처리용)
    private boolean p1UpPressed, p1DownPressed, p1LeftPressed, p1RightPressed;
    private boolean p2UpPressed, p2DownPressed, p2LeftPressed, p2RightPressed;

    // 게임 루프 타이머
    private javax.swing.Timer gameTimer;

    // ========== 물풍선 시스템 ==========
    private java.util.List<WaterBomb> bombs = new java.util.ArrayList<>(); // 설치된 물풍선 리스트
    private static final int BOMB_SIZE = 38; // 물풍선 크기
    private static final int BOMB_TIMER = 3000; // 물풍선 폭발 시간 (3초)

    // 타일 크기 (충돌 계산용)
    private int tileWidth;
    private int tileHeight;

    // ========== 게임 타이머 시스템 ==========
    private static final int GAME_TIME = 150; // 게임 시간 (2분 30초 = 150초)
    private int remainingTime = GAME_TIME; // 남은 시간 (초)
    private long lastTimerUpdate = 0; // 마지막 타이머 업데이트 시간

    // ========== 게임 상태 ==========
    private static final int STATE_PLAYING = 0; // 게임 진행 중
    private static final int STATE_P1_WIN = 1; // 1P 승리
    private static final int STATE_P2_WIN = 2; // 2P 승리
    private static final int STATE_DRAW = 3; // 무승부
    private int gameState = STATE_PLAYING;

    // 플레이어 생존 여부
    private boolean p1Alive = true;
    private boolean p2Alive = true;

    // 결과 이미지
    private Image winImg, loseImg, drawImg;

    // 결과 화면 표시 후 로비 복귀 타이머
    private long resultDisplayTime = 0;
    private static final int RESULT_DISPLAY_DURATION = 3000; // 결과 3초 표시 후 로비로

    // ========== 레이아웃 상수 ==========
    // 맵 영역 좌표 및 크기
    private static final int MAP_X = 15; // 맵 시작 X 좌표
    private static final int MAP_Y = 15; // 맵 시작 Y 좌표
    private static final int MAP_WIDTH = 570; // 맵 너비
    private static final int MAP_HEIGHT = 570; // 맵 높이

    // 우측 패널 좌표 및 크기
    private static final int RIGHT_PANEL_X = 600; // 우측 패널 시작 X 좌표
    private static final int RIGHT_PANEL_WIDTH = 185; // 우측 패널 너비

    // 로비 패널 참조 (맵 선택 정보)
    private LobbyPanel lobbyPanel;

    /**
     * 생성자: 게임 화면 초기화 (기존 호환용)
     * 
     * @param mainFrame 메인 프레임 (화면 전환에 사용)
     */
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame) {
        this(mainFrame, null);
    }

    /**
     * 생성자: 게임 화면 초기화 (맵 선택 지원)
     * 
     * @param mainFrame  메인 프레임 (화면 전환에 사용)
     * @param lobbyPanel 로비 패널 (맵 선택 정보 가져오기용)
     */
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame, LobbyPanel lobbyPanel) {
        this.mainFrame = mainFrame;
        this.lobbyPanel = lobbyPanel;
        setLayout(null); // 절대 좌표 레이아웃 사용
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(50, 50, 50)); // 진한 회색 배경

        // 이미지 리소스 로드
        loadCharacterImages();
        loadResultImages(); // 승리/패배/무승부 이미지

        // 팀원 맵 시스템 초기화 (기본 맵)
        initMapSystem();

        // 플레이어 초기 위치 설정
        initPlayerPositions();

        // 키 입력 처리 (이동 + ESC)
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

        // 나가기 버튼 클릭 시 로비로 복귀
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 나가기 버튼 영역 (x: 600, y: 540, width: 185, height: 45)
                Rectangle exitBounds = new Rectangle(RIGHT_PANEL_X, 540, RIGHT_PANEL_WIDTH, 45);
                if (exitBounds.contains(e.getPoint())) {
                    stopGameLoop();
                    mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
                }
            }
        });

        // 게임 루프 시작 (60 FPS)
        startGameLoop();
    }

    /**
     * 패널이 화면에 표시될 때 호출 (맵 선택 적용)
     */
    @Override
    public void addNotify() {
        super.addNotify();
        // 게임 화면 진입 시 선택된 맵 로드
        loadSelectedMap();

        // 게임 상태 리셋 (재시작 시)
        resetGame();

        // 키 입력 상태 초기화
        p1UpPressed = p1DownPressed = p1LeftPressed = p1RightPressed = false;
        p2UpPressed = p2DownPressed = p2LeftPressed = p2RightPressed = false;

        // 게임 루프 재시작
        startGameLoop();

        // 키보드 포커스 요청
        requestFocusInWindow();
    }

    /**
     * 게임 시작/재시작 (외부에서 호출)
     * 로비에서 게임 시작 버튼 클릭 시 호출됩니다.
     */
    public void startNewGame() {
        // 선택된 맵 로드
        loadSelectedMap();

        // 선택된 캐릭터 로드 (랜덤 처리 포함)
        loadSelectedCharacters();

        // 게임 상태 리셋
        resetGame();

        // 키 입력 상태 초기화
        p1UpPressed = p1DownPressed = p1LeftPressed = p1RightPressed = false;
        p2UpPressed = p2DownPressed = p2LeftPressed = p2RightPressed = false;

        // 게임 루프 재시작
        startGameLoop();

        // 키보드 포커스 요청
        requestFocusInWindow();

        System.out.println("새 게임 시작! 1P: " + p1CharacterName + ", 2P: " + p2CharacterName);
    }

    /**
     * 선택된 캐릭터 로드
     * 로비에서 선택한 캐릭터를 적용합니다. (랜덤 시 실제 캐릭터로 변환)
     */
    private void loadSelectedCharacters() {
        if (lobbyPanel != null) {
            p1CharacterName = lobbyPanel.getP1Character();
            p2CharacterName = lobbyPanel.getP2Character();
        }
    }

    /**
     * 선택된 맵 로드
     * 로비에서 선택한 맵에 따라 배경 이미지를 변경합니다.
     */
    private void loadSelectedMap() {
        if (lobbyPanel == null)
            return;

        String selectedMap = lobbyPanel.getSelectedMap();
        String mapFileName;

        // 맵 이름에 따라 파일명 결정
        if ("Map1".equals(selectedMap)) {
            mapFileName = "forest24.png"; // 숲 테마
        } else {
            mapFileName = "map2.png"; // 기본 맵
        }

        // 맵 객체 재생성 (선택된 맵 파일로)
        gameMap = new Map(mapFileName);
        System.out.println("선택된 맵 로드: " + selectedMap + " (" + mapFileName + ")");

        // 플레이어 위치 초기화
        initPlayerPositions();

        repaint();
    }

    /**
     * 플레이어 초기 위치 설정
     */
    private void initPlayerPositions() {
        // 1P: 좌상단 시작
        p1X = MAP_X + 40;
        p1Y = MAP_Y + 40;

        // 2P: 우하단 시작
        p2X = MAP_X + MAP_WIDTH - 60;
        p2Y = MAP_Y + MAP_HEIGHT - 60;
    }

    /**
     * 게임 루프 시작 (60 FPS)
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
        // 타일 크기 계산 (초기화 시 한 번만 필요하지만 안전하게)
        if (tileWidth == 0)
            tileWidth = MAP_WIDTH / TILE_COLS;
        if (tileHeight == 0)
            tileHeight = MAP_HEIGHT / TILE_ROWS;

        // Player 1 이동 (충돌 검사 포함)
        int newP1X = p1X, newP1Y = p1Y;
        if (p1UpPressed)
            newP1Y = Math.max(MAP_Y, p1Y - MOVE_SPEED);
        if (p1DownPressed)
            newP1Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p1Y + MOVE_SPEED);
        if (p1LeftPressed)
            newP1X = Math.max(MAP_X, p1X - MOVE_SPEED);
        if (p1RightPressed)
            newP1X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p1X + MOVE_SPEED);

        // 충돌 검사 후 이동 적용
        if (!isCollidingWithBlock(newP1X, p1Y, PLAYER_SIZE))
            p1X = newP1X;
        if (!isCollidingWithBlock(p1X, newP1Y, PLAYER_SIZE))
            p1Y = newP1Y;

        // Player 2 이동 (충돌 검사 포함)
        int newP2X = p2X, newP2Y = p2Y;
        if (p2UpPressed)
            newP2Y = Math.max(MAP_Y, p2Y - MOVE_SPEED);
        if (p2DownPressed)
            newP2Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p2Y + MOVE_SPEED);
        if (p2LeftPressed)
            newP2X = Math.max(MAP_X, p2X - MOVE_SPEED);
        if (p2RightPressed)
            newP2X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p2X + MOVE_SPEED);

        // 충돌 검사 후 이동 적용
        if (!isCollidingWithBlock(newP2X, p2Y, PLAYER_SIZE))
            p2X = newP2X;
        if (!isCollidingWithBlock(p2X, newP2Y, PLAYER_SIZE))
            p2Y = newP2Y;

        // 물풍선 폭발 처리
        updateBombs();

        // ========== 게임 타이머 및 결과 처리 ==========
        // 게임 진행 중일 때만 타이머 업데이트
        if (gameState == STATE_PLAYING) {
            // 1초마다 타이머 감소
            long currentTime = System.currentTimeMillis();
            if (lastTimerUpdate == 0) {
                lastTimerUpdate = currentTime;
            }
            if (currentTime - lastTimerUpdate >= 1000) {
                remainingTime--;
                lastTimerUpdate = currentTime;
            }

            // 시간 종료 시 무승부
            if (remainingTime <= 0) {
                gameState = STATE_DRAW;
                resultDisplayTime = System.currentTimeMillis();
            }

            // 승리 조건 확인 (둘 중 하나가 죽으면)
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
            // 결과 화면 표시 후 로비로 복귀
            if (System.currentTimeMillis() - resultDisplayTime >= RESULT_DISPLAY_DURATION) {
                stopGameLoop();
                resetGame();
                mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
            }
        }
    }

    /**
     * 게임 리셋 (새 게임 시작 시)
     */
    private void resetGame() {
        gameState = STATE_PLAYING;
        remainingTime = GAME_TIME;
        lastTimerUpdate = 0;
        p1Alive = true;
        p2Alive = true;
        bombs.clear();
        initPlayerPositions();
    }

    /**
     * 블록과의 충돌 검사
     * 
     * @param x    검사할 X 좌표
     * @param y    검사할 Y 좌표
     * @param size 캐릭터 크기
     * @return 블록과 충돌하면 true
     */
    private boolean isCollidingWithBlock(int x, int y, int size) {
        if (tiles == null)
            return false;

        // 캐릭터의 4개 모서리를 검사
        int[][] corners = {
                { x + 5, y + 5 }, // 좌상
                { x + size - 5, y + 5 }, // 우상
                { x + 5, y + size - 5 }, // 좌하
                { x + size - 5, y + size - 5 } // 우하
        };

        for (int[] corner : corners) {
            int col = (corner[0] - MAP_X) / tileWidth;
            int row = (corner[1] - MAP_Y) / tileHeight;

            // 범위 체크
            if (row >= 0 && row < TILE_ROWS && col >= 0 && col < TILE_COLS) {
                Tile tile = tiles[row][col];
                if (tile != null) {
                    int itemIndex = tile.getItemIndex();
                    // 블록(3, 4)은 통과 불가
                    if (itemIndex == 3 || itemIndex == 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 물풍선 업데이트 (폭발 처리)
     */
    private void updateBombs() {
        java.util.Iterator<WaterBomb> it = bombs.iterator();
        while (it.hasNext()) {
            WaterBomb bomb = it.next();
            if (bomb.shouldExplode()) {
                // 폭발 처리 (블록 파괴 등)
                explodeBomb(bomb);
                it.remove();
            }
        }
    }

    /**
     * 물풍선 폭발 처리
     */
    private void explodeBomb(WaterBomb bomb) {
        bomb.exploded = true;

        // 폭발 위치 주변 타일 파괴 (상하좌우 1칸)
        int[] dRows = { 0, -1, 1, 0, 0 }; // 중심, 위, 아래
        int[] dCols = { 0, 0, 0, -1, 1 }; // 중심, 왼쪽, 오른쪽

        int bombRow = (bomb.y - MAP_Y) / tileHeight;
        int bombCol = (bomb.x - MAP_X) / tileWidth;

        for (int i = 0; i < dRows.length; i++) {
            int r = bombRow + dRows[i];
            int c = bombCol + dCols[i];

            if (r >= 0 && r < TILE_ROWS && c >= 0 && c < TILE_COLS) {
                Tile tile = tiles[r][c];
                if (tile != null) {
                    tile.breakBlock(); // 블록 파괴 (아이템 드롭)
                }
            }
        }

        System.out.println("물풍선 폭발! 위치: (" + bombCol + ", " + bombRow + ")");
    }

    /**
     * 물풍선 설치
     * 
     * @param playerX 플레이어 X 좌표
     * @param playerY 플레이어 Y 좌표
     * @param owner   소유자 (1 또는 2)
     */
    private void placeBomb(int playerX, int playerY, int owner) {
        // 플레이어 중심 좌표 계산
        int centerX = playerX + PLAYER_SIZE / 2;
        int centerY = playerY + PLAYER_SIZE / 2;

        // 타일 그리드 위치 계산
        int col = (centerX - MAP_X) / tileWidth;
        int row = (centerY - MAP_Y) / tileHeight;

        // 범위 체크
        if (row < 0 || row >= TILE_ROWS || col < 0 || col >= TILE_COLS)
            return;

        // 해당 위치에 이미 물풍선이 있는지 확인
        for (WaterBomb bomb : bombs) {
            if (bomb.row == row && bomb.col == col) {
                return; // 이미 물풍선이 있으면 설치 불가
            }
        }

        // 타일 중심 좌표 계산
        int bombX = MAP_X + col * tileWidth + tileWidth / 2;
        int bombY = MAP_Y + row * tileHeight + tileHeight / 2;

        // 물풍선 생성 및 추가
        bombs.add(new WaterBomb(bombX, bombY, row, col, owner));
        System.out.println("물풍선 설치! " + owner + "P, 위치: (" + col + ", " + row + ")");
    }

    /**
     * 키 눌림 처리
     */
    private void handleKeyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // ESC: 로비로 복귀
        if (key == KeyEvent.VK_ESCAPE) {
            stopGameLoop();
            mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
            return;
        }

        // Player 1 이동 키 (GameSettings에서 설정된 키 사용)
        if (key == GameSettings.p1_Up)
            p1UpPressed = true;
        if (key == GameSettings.p1_Down)
            p1DownPressed = true;
        if (key == GameSettings.p1_Left)
            p1LeftPressed = true;
        if (key == GameSettings.p1_Right)
            p1RightPressed = true;

        // Player 2 이동 키
        if (key == GameSettings.p2_Up)
            p2UpPressed = true;
        if (key == GameSettings.p2_Down)
            p2DownPressed = true;
        if (key == GameSettings.p2_Left)
            p2LeftPressed = true;
        if (key == GameSettings.p2_Right)
            p2RightPressed = true;

        // 물풍선 설치 키
        if (key == GameSettings.p1_Bomb) {
            placeBomb(p1X, p1Y, 1); // 1P 물풍선 설치
        }
        if (key == GameSettings.p2_Bomb) {
            placeBomb(p2X, p2Y, 2); // 2P 물풍선 설치
        }
    }

    /**
     * 키 뗌 처리
     */
    private void handleKeyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        // Player 1 이동 키
        if (key == GameSettings.p1_Up)
            p1UpPressed = false;
        if (key == GameSettings.p1_Down)
            p1DownPressed = false;
        if (key == GameSettings.p1_Left)
            p1LeftPressed = false;
        if (key == GameSettings.p1_Right)
            p1RightPressed = false;

        // Player 2 이동 키
        if (key == GameSettings.p2_Up)
            p2UpPressed = false;
        if (key == GameSettings.p2_Down)
            p2DownPressed = false;
        if (key == GameSettings.p2_Left)
            p2LeftPressed = false;
        if (key == GameSettings.p2_Right)
            p2RightPressed = false;
    }

    /**
     * 팀원 맵 시스템 초기화
     * Map 클래스와 SpriteStore를 초기화하고, mapData.txt에서 타일 정보를 로드합니다.
     */
    private void initMapSystem() {
        try {
            // 맵 객체 생성 (기본 맵: map2.png)
            gameMap = new Map("map2.png");

            // 스프라이트 스토어 초기화 (아이템 이미지 로드)
            SpriteStore.init();

            // 타일 초기화 (mapData.txt에서 로드)
            loadTilesFromFile();

            System.out.println("맵 시스템 초기화 완료");
        } catch (Exception e) {
            System.err.println("맵 시스템 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * mapData.txt 파일에서 타일 정보를 로드합니다.
     * 각 숫자는 타일의 아이템 인덱스를 나타냅니다:
     * 0: 빈 타일, 1-3: 아이템, 4: 파괴 가능한 블록
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

            // 타일 배열 생성
            tiles = new Tile[TILE_ROWS][TILE_COLS];

            // 각 타일의 크기 계산 (맵 영역을 그리드로 분할)
            int cellWidth = MAP_WIDTH / TILE_COLS;
            int cellHeight = MAP_HEIGHT / TILE_ROWS;

            for (int r = 0; r < TILE_ROWS; r++) {
                for (int c = 0; c < TILE_COLS; c++) {
                    // 타일 중심 좌표 계산 (MAP_X, MAP_Y 오프셋 적용)
                    int centerX = MAP_X + c * cellWidth + cellWidth / 2;
                    int centerY = MAP_Y + r * cellHeight + cellHeight / 2;

                    // 타일 객체 생성 (블록(4)은 파괴 가능)
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
     * 결과 이미지 로드
     * 경로: res/win.bmp, res/lose.bmp, res/draw.bmp
     */
    private void loadResultImages() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;

            File winFile = new File(basePath + "win.bmp");
            if (winFile.exists()) {
                winImg = ImageIO.read(winFile);
            }

            File loseFile = new File(basePath + "lose.bmp");
            if (loseFile.exists()) {
                loseImg = ImageIO.read(loseFile);
            }

            File drawFile = new File(basePath + "draw.bmp");
            if (drawFile.exists()) {
                drawImg = ImageIO.read(drawFile);
            }
        } catch (IOException e) {
            System.err.println("결과 이미지 로드 실패: " + e.getMessage());
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
        // 1P 캐릭터 박스 (y: 15, 빨간색 테두리) - 선택된 캐릭터 표시
        Image p1Img = "다오".equals(p1CharacterName) ? daoImg : bazziImg;
        drawPlayerBox(g2, RIGHT_PANEL_X, 15, RIGHT_PANEL_WIDTH, 120, "1P", p1Img, new Color(220, 80, 80));

        // 1P 아이템 박스 (y: 145)
        drawItemBox(g2, RIGHT_PANEL_X, 145, RIGHT_PANEL_WIDTH, 100);

        // 2P 캐릭터 박스 (y: 260, 파란색 테두리) - 선택된 캐릭터 표시
        Image p2Img = "배찌".equals(p2CharacterName) ? bazziImg : daoImg;
        drawPlayerBox(g2, RIGHT_PANEL_X, 260, RIGHT_PANEL_WIDTH, 120, "2P", p2Img, new Color(80, 80, 220));

        // 2P 아이템 박스 (y: 390)
        drawItemBox(g2, RIGHT_PANEL_X, 390, RIGHT_PANEL_WIDTH, 100);

        // 타이머 표시 (2P 아이템 박스 아래, y: 495)
        drawTimer(g2, RIGHT_PANEL_X, 495, RIGHT_PANEL_WIDTH, 40);

        // 나가기 버튼 (y: 540)
        drawExitButton(g2, RIGHT_PANEL_X, 540, RIGHT_PANEL_WIDTH, 45);

        // 게임 종료 시 결과 오버레이 표시
        if (gameState != STATE_PLAYING) {
            drawResultOverlay(g2);
        }
    }

    /**
     * 타이머 표시
     */
    private void drawTimer(Graphics2D g2, int x, int y, int w, int h) {
        // 배경
        g2.setColor(new Color(40, 40, 40, 230));
        g2.fillRoundRect(x, y, w, h, 10, 10);

        // 테두리
        g2.setColor(new Color(255, 200, 0));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        // 시간 계산
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        String timeText = String.format("%d:%02d", minutes, seconds);

        // 시간 텍스트 (남은 시간이 30초 이하면 빨간색)
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
     * 게임 결과 오버레이 표시
     */
    private void drawResultOverlay(Graphics2D g2) {
        // 반투명 검정 배경
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 결과 이미지 선택
        Image resultImg = null;
        if (gameState == STATE_DRAW) {
            resultImg = drawImg;
        } else if (gameState == STATE_P1_WIN) {
            resultImg = winImg; // 1P 기준 승리
        } else if (gameState == STATE_P2_WIN) {
            resultImg = loseImg; // 1P 기준 패배
        }

        // 결과 이미지 그리기
        if (resultImg != null) {
            int imgW = resultImg.getWidth(this);
            int imgH = resultImg.getHeight(this);
            int imgX = (getWidth() - imgW) / 2;
            int imgY = (getHeight() - imgH) / 2;
            g2.drawImage(resultImg, imgX, imgY, this);
        } else {
            // 이미지 없을 때 텍스트로 표시
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 48));
            String text = "무승부";
            if (gameState == STATE_P1_WIN)
                text = "1P 승리!";
            else if (gameState == STATE_P2_WIN)
                text = "2P 승리!";

            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = getHeight() / 2;
            g2.drawString(text, textX, textY);
        }

        // 안내 텍스트
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        String guideText = "잠시 후 로비로 이동합니다...";
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(guideText)) / 2;
        g2.drawString(guideText, textX, getHeight() / 2 + 100);
    }

    /**
     * 게임 맵 그리기
     * 좌측 영역에 맵 이미지를 표시합니다.
     * 팀원의 Map 클래스를 우선 사용하고, 없으면 기존 이미지나 기본 맵을 표시합니다.
     */
    private void drawGameMap(Graphics2D g2) {
        // 검정색 외곽 테두리
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRect(MAP_X - 2, MAP_Y - 2, MAP_WIDTH + 4, MAP_HEIGHT + 4);

        // 우선순위: 팀원 Map 시스템 > 기존 맵 이미지 > 기본 타일 맵
        if (gameMap != null && gameMap.isLoaded()) {
            // 팀원 Map 클래스로 맵 배경 그리기
            gameMap.drawMap(g2, MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT);
        } else if (gameMapImage != null) {
            // 기존 맵 이미지 그리기
            g2.drawImage(gameMapImage, MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT, this);
        } else {
            // 이미지 없을 때 기본 타일 맵 표시
            drawDefaultMap(g2);
        }

        // 타일(블록/아이템) 그리기
        drawTiles(g2);

        // 물풍선 그리기
        drawBombs(g2);

        // 플레이어 캐릭터 그리기
        drawPlayers(g2);
    }

    /**
     * 물풍선 그리기
     */
    private void drawBombs(Graphics2D g2) {
        for (WaterBomb bomb : bombs) {
            bomb.draw(g2);
        }
    }

    /**
     * 플레이어 캐릭터 그리기
     * 선택된 캐릭터에 따라 1P와 2P를 현재 위치에 그립니다.
     */
    private void drawPlayers(Graphics2D g2) {
        // 1P 캐릭터 그리기 (선택된 캐릭터에 따라)
        Image p1Img = "다오".equals(p1CharacterName) ? daoImg : bazziImg;
        if (p1Img != null) {
            g2.drawImage(p1Img, p1X, p1Y, PLAYER_SIZE, PLAYER_SIZE, this);
        } else {
            // 이미지 없을 때 빨간 원으로 표시
            g2.setColor(new Color(220, 80, 80));
            g2.fillOval(p1X, p1Y, PLAYER_SIZE, PLAYER_SIZE);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            g2.drawString("1P", p1X + 8, p1Y + 26);
        }

        // 2P 캐릭터 그리기 (선택된 캐릭터에 따라)
        Image p2Img = "배찌".equals(p2CharacterName) ? bazziImg : daoImg;
        if (p2Img != null) {
            g2.drawImage(p2Img, p2X, p2Y, PLAYER_SIZE, PLAYER_SIZE, this);
        } else {
            // 이미지 없을 때 파란 원으로 표시
            g2.setColor(new Color(80, 80, 220));
            g2.fillOval(p2X, p2Y, PLAYER_SIZE, PLAYER_SIZE);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            g2.drawString("2P", p2X + 8, p2Y + 26);
        }
    }

    /**
     * 타일(블록/아이템) 그리기
     * mapData.txt에서 로드된 타일들을 맵 위에 그립니다.
     */
    private void drawTiles(Graphics2D g2) {
        if (tiles == null)
            return;

        for (int r = 0; r < TILE_ROWS; r++) {
            for (int c = 0; c < TILE_COLS; c++) {
                if (tiles[r][c] != null) {
                    tiles[r][c].draw(g2);
                }
            }
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

    // ========================================================
    // 물풍선 내부 클래스
    // ========================================================
    /**
     * 물풍선 클래스
     * 설치된 물풍선의 위치와 타이머를 관리합니다.
     */
    private class WaterBomb {
        int x, y; // 물풍선 위치 (타일 중심)
        int row, col; // 타일 그리드 위치
        int owner; // 소유자 (1: 1P, 2: 2P)
        long placeTime; // 설치 시간
        boolean exploded; // 폭발 여부

        public WaterBomb(int x, int y, int row, int col, int owner) {
            this.x = x;
            this.y = y;
            this.row = row;
            this.col = col;
            this.owner = owner;
            this.placeTime = System.currentTimeMillis();
            this.exploded = false;
        }

        /**
         * 폭발 시간이 되었는지 확인
         */
        public boolean shouldExplode() {
            return System.currentTimeMillis() - placeTime >= BOMB_TIMER;
        }

        /**
         * 물풍선 그리기
         */
        public void draw(Graphics2D g2) {
            if (exploded)
                return;

            // 물풍선 그리기 (파란색 원)
            int drawX = x - BOMB_SIZE / 2;
            int drawY = y - BOMB_SIZE / 2;

            // 물풍선 본체 (파란색 그라데이션)
            GradientPaint gp = new GradientPaint(
                    drawX, drawY, new Color(100, 150, 255),
                    drawX + BOMB_SIZE, drawY + BOMB_SIZE, new Color(50, 100, 200));
            g2.setPaint(gp);
            g2.fillOval(drawX, drawY, BOMB_SIZE, BOMB_SIZE);

            // 테두리
            g2.setColor(new Color(30, 80, 180));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(drawX, drawY, BOMB_SIZE, BOMB_SIZE);

            // 광택 효과
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillOval(drawX + 8, drawY + 5, 12, 8);

            // 소유자 표시 (작은 글씨)
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            String ownerText = owner == 1 ? "1P" : "2P";
            g2.drawString(ownerText, x - 8, y + 4);
        }
    }
}
