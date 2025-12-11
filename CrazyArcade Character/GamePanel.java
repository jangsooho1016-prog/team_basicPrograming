import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Crazy Arcade 데모용 게임 패널
 * - 15 x 13 맵
 * - 플레이어 2명
 * - 폭탄, 폭발, 위치 행렬(playerPositionMap) 관리
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    public static final int TILE_SIZE = Player.TILE_SIZE;
    public static final int MAP_COLS = 15;
    public static final int MAP_ROWS = 13;

    // 0: 빈칸, 1: 단단한 벽, 2: 깨지는 벽
    private int[][] map = new int[MAP_ROWS][MAP_COLS];

    // 플레이어의 현재 위치를 저장하는 15 x 13 배열 (0: 없음, 1: P1, 2: P2)
    private int[][] playerPositionMap = new int[MAP_ROWS][MAP_COLS];

    private Player player1;
    private Player player2;

    private List<Bomb> bombs = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();

    private Timer timer;

    public GamePanel() {
        this.setPreferredSize(new Dimension(MAP_COLS * TILE_SIZE, MAP_ROWS * TILE_SIZE));
        this.setBackground(new Color(120, 190, 255));

        this.initMap();

        this.player1 = new Player(
                Player.CharacterType.BAZZI,
                1, 1,
                KeyEvent.VK_W, KeyEvent.VK_S,
                KeyEvent.VK_A, KeyEvent.VK_D,
                KeyEvent.VK_SPACE
        );

        this.player2 = new Player(
                Player.CharacterType.DAO,
                MAP_COLS - 2, MAP_ROWS - 2,
                KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_ENTER
        );

        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);

        this.timer = new Timer(16, this); // 약 60FPS
        this.timer.start();
    }

    private void initMap() {
        for (int y = 0; y < MAP_ROWS; y++) {
            for (int x = 0; x < MAP_COLS; x++) {

                if (y == 0 || y == MAP_ROWS - 1 || x == 0 || x == MAP_COLS - 1) {
                    this.map[y][x] = 1; // 바깥 테두리
                } else if (y % 2 == 0 && x % 2 == 0) {
                    this.map[y][x] = 1; // 고정 블럭
                } else {
                    if (Math.random() < 0.3) {
                        this.map[y][x] = 2; // 깨지는 블럭
                    } else {
                        this.map[y][x] = 0; // 빈칸
                    }
                }
            }
        }

        this.clearStartArea(1, 1);
        this.clearStartArea(MAP_COLS - 2, MAP_ROWS - 2);
    }

    private void clearStartArea(int startX, int startY) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int x = startX + dx;
                int y = startY + dy;
                if (y > 0 && y < MAP_ROWS - 1 && x > 0 && x < MAP_COLS - 1) {
                    this.map[y][x] = 0;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 맵 타일 그리기
        for (int y = 0; y < MAP_ROWS; y++) {
            for (int x = 0; x < MAP_COLS; x++) {
                int cell = this.map[y][x];
                int screenX = x * TILE_SIZE;
                int screenY = y * TILE_SIZE;

                if (cell == 1) {
                    g.setColor(new Color(90, 90, 90)); // 단단한 벽
                } else if (cell == 2) {
                    g.setColor(new Color(200, 140, 80)); // 깨지는 벽
                } else {
                    g.setColor(new Color(150, 220, 150)); // 잔디
                }

                g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
            }
        }

        // 폭탄
        for (Bomb bomb : this.bombs) {
            bomb.draw(g);
        }

        // 폭발
        for (Explosion explosion : this.explosions) {
            explosion.draw(g);
        }

        // 플레이어
        this.player1.draw(g);
        this.player2.draw(g);

        // 상태 텍스트
        g.setColor(Color.BLACK);
        g.drawString("P1: " + (this.player1.isAlive() ? "Alive" : "Dead"), 10, 15);
        g.drawString("P2: " + (this.player2.isAlive() ? "Alive" : "Dead"), 100, 15);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 플레이어 업데이트
        this.player1.update(this.map, this.bombs);
        this.player2.update(this.map, this.bombs);

        // 폭탄 업데이트 및 폭발 생성
        Iterator<Bomb> bombIterator = this.bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.update();
            if (bomb.shouldExplode()) {
                this.explosions.addAll(bomb.explode(this.map));
                bombIterator.remove();
            }
        }

        // 폭발 업데이트 및 피격 체크
        Iterator<Explosion> explosionIterator = this.explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update();

            if (explosion.hits(this.player1)) {
                this.player1.setAlive(false);
            }
            if (explosion.hits(this.player2)) {
                this.player2.setAlive(false);
            }

            if (explosion.isFinished()) {
                explosionIterator.remove();
            }
        }

        // ===== 15 x 13 캐릭터 위치 행렬 갱신 =====
        for (int y = 0; y < MAP_ROWS; y++) {
            for (int x = 0; x < MAP_COLS; x++) {
                this.playerPositionMap[y][x] = 0;
            }
        }

        if (this.player1.isAlive()) {
            int gx1 = this.player1.getGridX();
            int gy1 = this.player1.getGridY();
            if (gy1 >= 0 && gy1 < MAP_ROWS && gx1 >= 0 && gx1 < MAP_COLS) {
                this.playerPositionMap[gy1][gx1] = 1;
            }
        }

        if (this.player2.isAlive()) {
            int gx2 = this.player2.getGridX();
            int gy2 = this.player2.getGridY();
            if (gy2 >= 0 && gy2 < MAP_ROWS && gx2 >= 0 && gx2 < MAP_COLS) {
                // 둘이 같은 칸이면 나중에 규칙에 따라 3 같은 값으로 바꿀 수도 있음
                this.playerPositionMap[gy2][gx2] = 2;
            }
        }

        this.repaint();
    }

    // ==== KeyListener 구현 ====

    @Override
    public void keyPressed(KeyEvent e) {
        this.player1.keyPressed(e, this.bombs, this.map);
        this.player2.keyPressed(e, this.bombs, this.map);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        this.player1.keyReleased(e);
        this.player2.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // 사용하지 않음
    }
}
