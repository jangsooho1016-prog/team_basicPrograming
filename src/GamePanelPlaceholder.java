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
 * --------------------------------------------------------
 */
public class GamePanelPlaceholder extends JPanel {
    // ========== [1] 기본 설정 및 참조 변수 ==========
    private CrazyArcade_UI mainFrame; // 화면 전환을 위한 메인 프레임 참조
    private LobbyPanel lobbyPanel; // 로비 정보를 가져오기 위한 참조

    // ========== [2] 맵 시스템 ==========
    // 배경 이미지를 담당하는 Map 객체
    private Map gameMap;

    // 게임 맵을 격자(Grid)로 관리하기 위한 2차원 배열
    // tiles[행][열] 형태로 접근하며, 각 타일은 블록이나 아이템 정보를 가집니다.
    private Tile[][] tiles;
    private static final int TILE_ROWS = 13; // 세로 타일 개수
    private static final int TILE_COLS = 15; // 가로 타일 개수

    // ========== [3] 캐릭터 시스템 ==========
    // 이미지 리소스
    private Image bazziImg; // 배찌 이미지
    private Image daoImg; // 다오 이미지

    // 현재 플레이어의 선택된 캐릭터 이름
    private String p1CharacterName = "배찌";
    private String p2CharacterName = "다오";

    // ========== [4] 좌표 및 이동 시스템 ==========
    // Player 1 (왼쪽 상단 시작)
    private int p1X = 60;
    private int p1Y = 60;

    // Player 2 (오른쪽 하단 시작)
    private int p2X = 520;
    private int p2Y = 520;

    // 캐릭터 설정
    private static final int PLAYER_SIZE = 40; // 캐릭터 크기 (픽셀)
    private static final int MOVE_SPEED = 5; // 한 번에 이동하는 픽셀 수 (속도)

    // 키보드 동시 입력을 처리하기 위한 상태 플래그
    // true면 해당 방향키가 눌려있는 상태입니다.
    private boolean p1UpPressed, p1DownPressed, p1LeftPressed, p1RightPressed;
    private boolean p2UpPressed, p2DownPressed, p2LeftPressed, p2RightPressed;

    // ========== [5] 게임 루프 & 타이머 ==========
    // 게임의 심장 역할. 일정 시간마다 updateGame()과 repaint()를 호출합니다.
    private javax.swing.Timer gameTimer;

    // 물풍선 관리 리스트 (설치된 모든 물풍선을 저장)
    private java.util.List<WaterBomb> bombs = new java.util.ArrayList<>();
    private static final int BOMB_SIZE = 38;
    private static final int BOMB_TIMER = 3000; // 폭발까지 걸리는 시간 (ms)

    // 타일 하나의 실제 픽셀 크기 (충돌 계산에 사용)
    private int tileWidth;
    private int tileHeight;

    // ========== [6] 게임 상태 및 승패 판정 ==========
    private static final int GAME_TIME = 150; // 제한 시간 (초)
    private int remainingTime = GAME_TIME;
    private long lastTimerUpdate = 0;

    // 게임 상태 상수
    private static final int STATE_PLAYING = 0; // 진행 중
    private static final int STATE_P1_WIN = 1; // 1P 승리
    private static final int STATE_P2_WIN = 2; // 2P 승리
    private static final int STATE_DRAW = 3; // 무승부
    private int gameState = STATE_PLAYING;

    // 플레이어 생존 플래그
    private boolean p1Alive = true;
    private boolean p2Alive = true;

    // 결과 이미지 리소스
    private Image winImg, loseImg, drawImg;
    private long resultDisplayTime = 0; // 결과 화면 표시 시작 시간
    private static final int RESULT_DISPLAY_DURATION = 3000; // 결과 표시 지속 시간

    // ========== [7] 레이아웃 상수 ==========
    private static final int MAP_X = 15; // 맵 시작 X
    private static final int MAP_Y = 15; // 맵 시작 Y
    private static final int MAP_WIDTH = 570; // 맵 너비
    private static final int MAP_HEIGHT = 570; // 맵 높이

    private static final int RIGHT_PANEL_X = 600; // 우측 정보창 X
    private static final int RIGHT_PANEL_WIDTH = 185; // 우측 정보창 너비

    /**
     * 생성자: 게임 화면 초기화 (기존 호환용)
     */
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame) {
        this(mainFrame, null);
    }

    /**
     * 생성자: 게임 패널 초기화 및 리소스 로드
     * 게임에 필요한 이미지, 맵 데이터, 리스너 등을 설정합니다.
     * 
     * @param mainFrame  화면 전환을 위한 메인 프레임
     * @param lobbyPanel 맵/캐릭터 선택 정보를 가져올 로비 패널
     */
    public GamePanelPlaceholder(CrazyArcade_UI mainFrame, LobbyPanel lobbyPanel) {
        this.mainFrame = mainFrame;
        this.lobbyPanel = lobbyPanel;

        setLayout(null); // 컴포넌트 위치를 직접 지정하기 위해 null layout 사용
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(50, 50, 50)); // 배경색 설정

        // 1. 이미지 리소스 로드 (캐릭터, 승패 이미지 등)
        loadCharacterImages();
        loadResultImages();

        // 2. 맵 시스템 초기화 (타일 데이터 로드)
        initMapSystem();

        // 3. 플레이어 시작 위치 설정
        initPlayerPositions();

        // 4. 이벤트 리스너 등록
        // 키보드 입력을 받기 위해 포커스 설정
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e); // 키 누를 때 처리
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e); // 키 뗄 때 처리
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
                    playLobbyBGM(); // 로비 BGM으로 전환
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

        // 인게임 BGM 시작
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
     * [핵심] 게임 루프 시작 메서드
     * javax.swing.Timer를 사용하여 약 60 FPS(초당 60프레임)로 게임을 갱신합니다.
     * 16ms마다 updateGame(데이터 갱신)과 repaint(화면 다시 그리기)를 반복 호출합니다.
     */
    private void startGameLoop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }

        // 16ms = 약 60 Frame Per Second (1000ms / 60)
        gameTimer = new javax.swing.Timer(16, e -> {
            updateGame(); // 게임 로직(변수, 위치 등) 업데이트
            repaint(); // 화면 그래픽 다시 그리기 (paintComponent 호출)
        });
        gameTimer.start();
    }

    /**
     * 게임 루프 중지
     * 게임이 끝나거나 화면을 나갈 때 타이머를 멈춥니다.
     */
    private void stopGameLoop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    /**
     * [핵심] 게임 상태 업데이트 (매 프레임 호출됨)
     * 캐릭터 이동, 충돌 체크, 물풍선 처리, 승패 판정 등 모든 로직이 여기서 수행됩니다.
     */
    private void updateGame() {
        // [초기화] 타일 크기 계산 (화면 크기에 맞춰 동적으로 설정)
        if (tileWidth == 0)
            tileWidth = MAP_WIDTH / TILE_COLS;
        if (tileHeight == 0)
            tileHeight = MAP_HEIGHT / TILE_ROWS;

        // 1. [Player 1 이동 처리]
        // 입력된 키 상태에 따라 임시 위치(newP1X, newP1Y)를 먼저 계산합니다.
        int newP1X = p1X, newP1Y = p1Y;

        // 상하좌우 이동 (맵 경계선 밖으로 나가지 않도록 Math.max/min으로 제한)
        if (p1UpPressed)
            newP1Y = Math.max(MAP_Y, p1Y - MOVE_SPEED);
        if (p1DownPressed)
            newP1Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p1Y + MOVE_SPEED);
        if (p1LeftPressed)
            newP1X = Math.max(MAP_X, p1X - MOVE_SPEED);
        if (p1RightPressed)
            newP1X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p1X + MOVE_SPEED);

        // 충돌 검사: 이동하려는 위치에 블록이 없으면 실제 위치(p1X, p1Y)를 업데이트합니다.
        // X축과 Y축을 따로 검사하여, 벽에 비스듬히 부딪혀도 미끄러지듯 이동할 수 있게 합니다.
        if (!isCollidingWithBlock(newP1X, p1Y, PLAYER_SIZE))
            p1X = newP1X;
        if (!isCollidingWithBlock(p1X, newP1Y, PLAYER_SIZE))
            p1Y = newP1Y;

        // 2. [Player 2 이동 처리] (1P와 동일한 로직)
        int newP2X = p2X, newP2Y = p2Y;
        if (p2UpPressed)
            newP2Y = Math.max(MAP_Y, p2Y - MOVE_SPEED);
        if (p2DownPressed)
            newP2Y = Math.min(MAP_Y + MAP_HEIGHT - PLAYER_SIZE, p2Y + MOVE_SPEED);
        if (p2LeftPressed)
            newP2X = Math.max(MAP_X, p2X - MOVE_SPEED);
        if (p2RightPressed)
            newP2X = Math.min(MAP_X + MAP_WIDTH - PLAYER_SIZE, p2X + MOVE_SPEED);

        if (!isCollidingWithBlock(newP2X, p2Y, PLAYER_SIZE))
            p2X = newP2X;
        if (!isCollidingWithBlock(p2X, newP2Y, PLAYER_SIZE))
            p2Y = newP2Y;

        // 3. [물풍선 처리]
        // 설치된 물풍선의 시간을 체크하고 폭발시킵니다.
        updateBombs();

        // 4. [게임 타이머 및 결과 판정]
        // 게임 진행 중일 때만 로직을 수행합니다.
        if (gameState == STATE_PLAYING) {
            // [타이머 감소] 1초(1000ms)마다 남은 시간을 줄입니다.
            long currentTime = System.currentTimeMillis();
            if (lastTimerUpdate == 0) {
                lastTimerUpdate = currentTime;
            }
            if (currentTime - lastTimerUpdate >= 1000) {
                remainingTime--;
                lastTimerUpdate = currentTime;
            }

            // [시간 종료] 시간이 0이 되면 무승부 처리
            if (remainingTime <= 0) {
                gameState = STATE_DRAW;
                resultDisplayTime = System.currentTimeMillis();
            }

            // [승패 판정] 플레이어 생존 여부를 확인하여 결과 상태를 변경합니다.
            // (p1Alive, p2Alive 변수는 물풍선 폭발 로직에서 변경됩니다)
            if (!p1Alive && p2Alive) {
                gameState = STATE_P2_WIN; // 1P 사망 -> 2P 승리
                resultDisplayTime = System.currentTimeMillis();
            } else if (p1Alive && !p2Alive) {
                gameState = STATE_P1_WIN; // 2P 사망 -> 1P 승리
                resultDisplayTime = System.currentTimeMillis();
            } else if (!p1Alive && !p2Alive) {
                gameState = STATE_DRAW; // 둘 다 사망 -> 무승부
                resultDisplayTime = System.currentTimeMillis();
            }
        } else {
            // [게임 종료 상태]
            // 결과 화면을 3초간 보여준 뒤 로비로 자동으로 돌아갑니다.
            if (System.currentTimeMillis() - resultDisplayTime >= RESULT_DISPLAY_DURATION) {
                stopGameLoop();
                resetGame();
                playLobbyBGM(); // 로비 BGM으로 복귀
                mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
            }
        }
    }

    /**
     * 게임 상태 초기화 메서드
     * 새 게임을 시작할 때 모든 변수를 초기 상태로 되돌립니다.
     */
    private void resetGame() {
        gameState = STATE_PLAYING;
        remainingTime = GAME_TIME;
        lastTimerUpdate = 0;
        p1Alive = true;
        p2Alive = true;
        bombs.clear(); // 설치된 물풍선 제거
        initPlayerPositions(); // 위치 원위치
    }

    /**
     * [충돌 감지 핵심 로직]
     * 캐릭터가 이동하려는 위치에 블록(나무, 벽 등)이 있는지 검사합니다.
     * 
     * @param x    이동하려는 X 좌표
     * @param y    이동하려는 Y 좌표
     * @param size 캐릭터 크기
     * @return true면 이동 불가능(충돌), false면 이동 가능
     */
    private boolean isCollidingWithBlock(int x, int y, int size) {
        if (tiles == null)
            return false;

        // [4방향 모서리 검사 Point]
        // 캐릭터의 네 모서리가 블록 영역에 겹치는지 확인합니다.
        // 약간의 여백(+5픽셀)을 두어 판정을 부드럽게 합니다.
        int[][] corners = {
                { x + 5, y + 5 }, // 좌상단
                { x + size - 5, y + 5 }, // 우상단
                { x + 5, y + size - 5 }, // 좌하단
                { x + size - 5, y + size - 5 } // 우하단
        };

        for (int[] corner : corners) {
            // 픽셀 좌표를 타일 그리드 인덱스로 변환
            int col = (corner[0] - MAP_X) / tileWidth;
            int row = (corner[1] - MAP_Y) / tileHeight;

            // 맵 범위 내에 있는지 확인
            if (row >= 0 && row < TILE_ROWS && col >= 0 && col < TILE_COLS) {
                Tile tile = tiles[row][col];
                if (tile != null) {
                    int itemIndex = tile.getItemIndex();
                    // itemIndex 3(벽돌) 또는 4(나무 등 파괴 가능 블록)는 통과할 수 없습니다.
                    // (0, 1, 2는 아이템이나 빈 공간이므로 통과 가능)
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
     * 설치된 모든 물풍선을 검사하여 폭발 시간이 지났는지 확인합니다.
     */
    private void updateBombs() {
        java.util.Iterator<WaterBomb> it = bombs.iterator();
        while (it.hasNext()) {
            WaterBomb bomb = it.next();
            if (bomb.shouldExplode()) {
                // 폭발 처리 (블록 파괴 및 플레이어 피격 검사)
                explodeBomb(bomb);
                // 리스트에서 제거 (Iterator 안전 삭제)
                it.remove();
            }
        }
    }

    /**
     * [핵심] 물풍선 폭발 로직
     * 물풍선이 터질 때 상하좌우 타일을 파괴하고, 플레이어가 범위 내에 있으면 게임을 끝냅니다.
     * 
     * @param bomb 폭발하는 물풍선 객체
     */
    private void explodeBomb(WaterBomb bomb) {
        bomb.exploded = true;

        // 폭발 범위: 중심 + 상하좌우 1칸
        int[] dRows = { 0, -1, 1, 0, 0 }; // 중심, 위, 아래
        int[] dCols = { 0, 0, 0, -1, 1 }; // 중심, 왼쪽, 오른쪽

        // 물풍선이 위차한 타일 좌표
        int bombRow = (bomb.y - MAP_Y) / tileHeight;
        int bombCol = (bomb.x - MAP_X) / tileWidth;

        for (int i = 0; i < dRows.length; i++) {
            int r = bombRow + dRows[i];
            int c = bombCol + dCols[i];

            // 1. [블록 파괴] 맵 범위 내의 타일 확인
            if (r >= 0 && r < TILE_ROWS && c >= 0 && c < TILE_COLS) {
                Tile tile = tiles[r][c];
                if (tile != null) {
                    // 벽(3)이 아닌 경우 블록 파괴 시도 (Tile 내부에서 파괴 가능한지 체크함)
                    tile.breakBlock();
                }
            }

            // 2. [플레이어 피격] 폭발 범위에 플레이어가 있는지 계산
            // 타일의 실제 픽셀 좌표 영역
            int tileMinX = MAP_X + c * tileWidth;
            int tileMaxX = tileMinX + tileWidth;
            int tileMinY = MAP_Y + r * tileHeight;
            int tileMaxY = tileMinY + tileHeight;

            // Player 1 피격 검사 (캐릭터 중심점 기준)
            int p1CenterX = p1X + PLAYER_SIZE / 2;
            int p1CenterY = p1Y + PLAYER_SIZE / 2;
            if (p1Alive && p1CenterX >= tileMinX && p1CenterX <= tileMaxX &&
                    p1CenterY >= tileMinY && p1CenterY <= tileMaxY) {
                p1Alive = false; // 사망 처리
                System.out.println("1P 사망!");
            }

            // Player 2 피격 검사
            int p2CenterX = p2X + PLAYER_SIZE / 2;
            int p2CenterY = p2Y + PLAYER_SIZE / 2;
            if (p2Alive && p2CenterX >= tileMinX && p2CenterX <= tileMaxX &&
                    p2CenterY >= tileMinY && p2CenterY <= tileMaxY) {
                p2Alive = false; // 사망 처리
                System.out.println("2P 사망!");
            }
        }

        System.out.println("물풍선 폭발! 위치: (" + bombCol + ", " + bombRow + ")");
    }

    /**
     * 물풍선 설치 요청 처리
     * 현재 플레이어 위치의 타일 중심에 물풍선을 설치합니다.
     * 
     * @param playerX 플레이어 현재 X
     * @param playerY 플레이어 현재 Y
     * @param owner   설치한 플레이어 (1=1P, 2=2P)
     */
    private void placeBomb(int playerX, int playerY, int owner) {
        // 플레이어의 중심 좌표
        int centerX = playerX + PLAYER_SIZE / 2;
        int centerY = playerY + PLAYER_SIZE / 2;

        // 해당 좌표가 속한 타일(행, 열) 계산
        int col = (centerX - MAP_X) / tileWidth;
        int row = (centerY - MAP_Y) / tileHeight;

        // 맵 범위 체크
        if (row < 0 || row >= TILE_ROWS || col < 0 || col >= TILE_COLS)
            return;

        // 이미 같은 자리에 물풍선이 있는지 확인 (중복 설치 방지)
        for (WaterBomb bomb : bombs) {
            if (bomb.row == row && bomb.col == col) {
                return;
            }
        }

        // 물풍선을 타일의 정중앙에 위치시킴
        int bombX = MAP_X + col * tileWidth + tileWidth / 2;
        int bombY = MAP_Y + row * tileHeight + tileHeight / 2;

        // 리스트에 추가
        bombs.add(new WaterBomb(bombX, bombY, row, col, owner));
        System.out.println("물풍선 설치! " + owner + "P, 위치: (" + col + ", " + row + ")");
    }

    /**
     * 키보드 눌림 처리
     * GameSettings에서 정의된 키 매핑을 사용하여 이동 및 물풍선 설치를 처리합니다.
     */
    private void handleKeyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // ESC 키: 게임 중단 후 로비로 복귀
        if (key == KeyEvent.VK_ESCAPE) {
            stopGameLoop();
            mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
            return;
        }

        // 1P 조작키 확인
        if (key == GameSettings.p1_Up)
            p1UpPressed = true;
        if (key == GameSettings.p1_Down)
            p1DownPressed = true;
        if (key == GameSettings.p1_Left)
            p1LeftPressed = true;
        if (key == GameSettings.p1_Right)
            p1RightPressed = true;

        // 1P 물풍선 설치
        if (key == GameSettings.p1_Bomb) {
            // 사망 상태가 아닐 때만 설치 가능
            if (p1Alive)
                placeBomb(p1X, p1Y, 1);
        }

        // 2P 조작키 확인
        if (key == GameSettings.p2_Up)
            p2UpPressed = true;
        if (key == GameSettings.p2_Down)
            p2DownPressed = true;
        if (key == GameSettings.p2_Left)
            p2LeftPressed = true;
        if (key == GameSettings.p2_Right)
            p2RightPressed = true;

        // 2P 물풍선 설치
        if (key == GameSettings.p2_Bomb) {
            if (p2Alive)
                placeBomb(p2X, p2Y, 2);
        }
    }

    /**
     * 키보드 뗌 처리
     * 이동 멈춤 상태를 업데이트합니다.
     */
    private void handleKeyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        // 1P 이동 멈춤
        if (key == GameSettings.p1_Up)
            p1UpPressed = false;
        if (key == GameSettings.p1_Down)
            p1DownPressed = false;
        if (key == GameSettings.p1_Left)
            p1LeftPressed = false;
        if (key == GameSettings.p1_Right)
            p1RightPressed = false;

        // 2P 이동 멈춤
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
     * 마젠타 배경(0xFF00FF)을 투명 처리합니다.
     */
    private void loadResultImages() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "res" + File.separator;

            File winFile = new File(basePath + "win.bmp");
            if (winFile.exists()) {
                winImg = makeTransparent(ImageIO.read(winFile));
            }

            File loseFile = new File(basePath + "lose.bmp");
            if (loseFile.exists()) {
                loseImg = makeTransparent(ImageIO.read(loseFile));
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
     * 이미지의 마젠타(0xFF00FF) 색상을 투명하게 변환
     */
    private Image makeTransparent(java.awt.image.BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        java.awt.image.BufferedImage result = new java.awt.image.BufferedImage(width, height,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y) & 0xFFFFFF; // RGB만 추출
                if (rgb == 0xFF00FF) { // 마젠타 색상
                    result.setRGB(x, y, 0x00000000); // 완전 투명
                } else {
                    result.setRGB(x, y, img.getRGB(x, y) | 0xFF000000); // 불투명
                }
            }
        }
        return result;
    }

    /**
     * [렌더링 핵심] 화면 그리기 메서드
     * Swing의 더블 버퍼링을 통해 깜빡임 없이 매끄럽게 화면을 그립니다.
     * 모든 UI 요소(맵, 캐릭터, 물풍선, 상태바 등)는 여기서 순서대로 그려집니다.
     * 
     * [그리기 순서]
     * 1. 전체 배경
     * 2. 게임 맵 (타일, 물풍선, 캐릭터 포함)
     * 3. 우측 정보창 (플레이어 상태, 아이템 등)
     * 4. 결과 오버레이 (게임 종료 시)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // 고품질 렌더링 설정 (안티앨리어싱 활성화)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 전체 배경색 (진한 회색)
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 2. [좌측] 맵 + 게임 화면 그리기
        drawGameMap(g2);

        // 3. [우측] 1P/2P 정보 + 나가기 버튼 그리기
        // 1P 캐릭터 박스 (y: 15, 빨간색 테두리)
        Image p1Img = "다오".equals(p1CharacterName) ? daoImg : bazziImg;
        drawPlayerBox(g2, RIGHT_PANEL_X, 15, RIGHT_PANEL_WIDTH, 120, "1P", p1Img, new Color(220, 80, 80));

        // 1P 아이템 박스 (y: 145)
        drawItemBox(g2, RIGHT_PANEL_X, 145, RIGHT_PANEL_WIDTH, 100);

        // 2P 캐릭터 박스 (y: 260, 파란색 테두리)
        Image p2Img = "배찌".equals(p2CharacterName) ? bazziImg : daoImg;
        drawPlayerBox(g2, RIGHT_PANEL_X, 260, RIGHT_PANEL_WIDTH, 120, "2P", p2Img, new Color(80, 80, 220));

        // 2P 아이템 박스 (y: 390)
        drawItemBox(g2, RIGHT_PANEL_X, 390, RIGHT_PANEL_WIDTH, 100);

        // 타이머 표시 (2P 아이템 박스 아래, y: 495)
        drawTimer(g2, RIGHT_PANEL_X, 495, RIGHT_PANEL_WIDTH, 40);

        // 나가기 버튼 (y: 540)
        drawExitButton(g2, RIGHT_PANEL_X, 540, RIGHT_PANEL_WIDTH, 45);

        // 4. [게임 종료 결과 화면] 반투명 오버레이
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

        // 맵 배경 그리기 (Map 클래스 사용, 없으면 기본 맵)
        if (gameMap != null && gameMap.isLoaded()) {
            gameMap.drawMap(g2, MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT);
        } else {
            // 맵 이미지 없을 때 기본 타일 맵 표시
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
