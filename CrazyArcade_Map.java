import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CrazyArcade_Map extends JFrame {
    private MapSelectionPanel mapSelectionPanel;
    private GamePanel gamePanel;

    public CrazyArcade_Map() {
        setTitle("Crazy Arcade - Map System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        mapSelectionPanel = new MapSelectionPanel(this);
        add(mapSelectionPanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void startGame(int mapType) {
        getContentPane().removeAll();
        gamePanel = new GamePanel(mapType, this);
        add(gamePanel);
        revalidate();
        repaint();
        gamePanel.requestFocusInWindow();
    }

    public void backToMenu() {
        getContentPane().removeAll();
        mapSelectionPanel = new MapSelectionPanel(this);
        add(mapSelectionPanel);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrazyArcade_Map());
    }
}

class MapSelectionPanel extends JPanel {
    private static final int PANEL_WIDTH = 600;
    private static final int PANEL_HEIGHT = 520;

    public MapSelectionPanel(CrazyArcade_Map game) {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(135, 206, 235));
        setLayout(null);

        JLabel titleLabel = new JLabel("맵 선택");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 40));
        titleLabel.setBounds(220, 30, 200, 50);
        add(titleLabel);

        // 맵 1 버튼
        JButton map1Btn = createMapButton("기본 맵", 50, 120);
        map1Btn.addActionListener(e -> game.startGame(1));
        add(map1Btn);

        // 맵 2 버튼
        JButton map2Btn = createMapButton("미로 맵", 320, 120);
        map2Btn.addActionListener(e -> game.startGame(2));
        add(map2Btn);

        // 맵 3 버튼
        JButton map3Btn = createMapButton("오픈 맵", 50, 280);
        map3Btn.addActionListener(e -> game.startGame(3));
        add(map3Btn);

        // 맵 4 버튼
        JButton map4Btn = createMapButton("대칭 맵", 320, 280);
        map4Btn.addActionListener(e -> game.startGame(4));
        add(map4Btn);
    }

    private JButton createMapButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 230, 120);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        btn.setFocusPainted(false);
        return btn;
    }
}

class GamePanel extends JPanel implements Runnable {
    private static final int TILE_SIZE = 40;
    private static final int MAP_WIDTH = 15;
    private static final int MAP_HEIGHT = 13;
    private static final int PANEL_WIDTH = TILE_SIZE * MAP_WIDTH;
    private static final int PANEL_HEIGHT = TILE_SIZE * MAP_HEIGHT;

    private Thread gameThread;
    private Player player1, player2;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private int[][] map;
    private CrazyArcade_Map game;
    private int mapType;

    public GamePanel(int mapType, CrazyArcade_Map game) {
        this.mapType = mapType;
        this.game = game;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);

        initGame();
        addKeyListener(new GameKeyListener());

        gameThread = new Thread(this);
        gameThread.start();
    }

    private void initGame() {
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
        map = new int[MAP_HEIGHT][MAP_WIDTH];

        switch (mapType) {
            case 1:
                createBasicMap();
                break;
            case 2:
                createMazeMap();
                break;
            case 3:
                createOpenMap();
                break;
            case 4:
                createSymmetricMap();
                break;
        }

        player1 = new Player(1, 1, Color.BLUE, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D,
                KeyEvent.VK_SPACE);
        player2 = new Player(MAP_WIDTH - 2, MAP_HEIGHT - 2, Color.RED, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER);
    }

    private void createBasicMap() {
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                    map[i][j] = 1;
                } else if (i % 2 == 0 && j % 2 == 0) {
                    map[i][j] = 1;
                } else if (Math.random() < 0.5
                        && !((i <= 2 && j <= 2) || (i >= MAP_HEIGHT - 3 && j >= MAP_WIDTH - 3))) {
                    map[i][j] = 2;
                }
            }
        }
    }

    private void createMazeMap() {
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                    map[i][j] = 1;
                } else if (i % 2 == 0 && j % 2 == 0) {
                    map[i][j] = 1;
                } else if ((i % 4 == 1 && j % 3 == 0) || (j % 4 == 1 && i % 3 == 0)) {
                    if (!((i <= 2 && j <= 2) || (i >= MAP_HEIGHT - 3 && j >= MAP_WIDTH - 3))) {
                        map[i][j] = 2;
                    }
                }
            }
        }
    }

    private void createOpenMap() {
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                    map[i][j] = 1;
                } else if (i % 2 == 0 && j % 2 == 0) {
                    map[i][j] = 1;
                } else if (Math.random() < 0.2) {
                    if (!((i <= 2 && j <= 2) || (i >= MAP_HEIGHT - 3 && j >= MAP_WIDTH - 3))) {
                        map[i][j] = 2;
                    }
                }
            }
        }
    }

    private void createSymmetricMap() {
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH / 2 + 1; j++) {
                if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                    map[i][j] = 1;
                    map[i][MAP_WIDTH - 1 - j] = 1;
                } else if (i % 2 == 0 && j % 2 == 0) {
                    map[i][j] = 1;
                    map[i][MAP_WIDTH - 1 - j] = 1;
                } else if (Math.random() < 0.4) {
                    if (!((i <= 2 && j <= 2) || (i >= MAP_HEIGHT - 3 && j >= MAP_WIDTH - 3))) {
                        map[i][j] = 2;
                        map[i][MAP_WIDTH - 1 - j] = 2;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000.0 / 60.0;
        double delta = 0;

        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        player1.update(map, bombs);
        player2.update(map, bombs);

        for (int i = bombs.size() - 1; i >= 0; i--) {
            Bomb bomb = bombs.get(i);
            bomb.update();
            if (bomb.shouldExplode()) {
                createExplosion(bomb);
                bombs.remove(i);
            }
        }

        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion exp = explosions.get(i);
            exp.update();
            if (exp.isFinished()) {
                explosions.remove(i);
            }
        }

        for (Explosion exp : explosions) {
            if (exp.hits(player1.x, player1.y))
                player1.alive = false;
            if (exp.hits(player2.x, player2.y))
                player2.alive = false;
        }
    }

    private void createExplosion(Bomb bomb) {
        int range = bomb.range;
        explosions.add(new Explosion(bomb.x, bomb.y));

        int[][] dirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
        for (int[] dir : dirs) {
            for (int i = 1; i <= range; i++) {
                int nx = bomb.x + dir[0] * i;
                int ny = bomb.y + dir[1] * i;

                if (nx < 0 || nx >= MAP_WIDTH || ny < 0 || ny >= MAP_HEIGHT)
                    break;
                if (map[ny][nx] == 1)
                    break;

                explosions.add(new Explosion(nx, ny));

                if (map[ny][nx] == 2) {
                    map[ny][nx] = 0;
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                int x = j * TILE_SIZE;
                int y = i * TILE_SIZE;

                if (map[i][j] == 1) {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                } else if (map[i][j] == 2) {
                    g.setColor(Color.ORANGE);
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }

        for (Explosion exp : explosions) {
            exp.draw(g);
        }

        for (Bomb bomb : bombs) {
            bomb.draw(g);
        }

        if (player1.alive)
            player1.draw(g);
        if (player2.alive)
            player2.draw(g);

        if (!player1.alive || !player2.alive) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 40));
            String winner = !player1.alive && !player2.alive ? "무승부!" : !player1.alive ? "빨강 승리!" : "파랑 승리!";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(winner);
            g.drawString(winner, (PANEL_WIDTH - textWidth) / 2, PANEL_HEIGHT / 2 - 30);

            g.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
            String info = "ESC - 메뉴로 돌아가기";
            textWidth = g.getFontMetrics().stringWidth(info);
            g.drawString(info, (PANEL_WIDTH - textWidth) / 2, PANEL_HEIGHT / 2 + 30);
        }
    }

    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_ESCAPE) {
                game.backToMenu();
                return;
            }

            if (player1.alive) {
                if (key == player1.upKey)
                    player1.movingUp = true;
                if (key == player1.downKey)
                    player1.movingDown = true;
                if (key == player1.leftKey)
                    player1.movingLeft = true;
                if (key == player1.rightKey)
                    player1.movingRight = true;
                if (key == player1.bombKey)
                    player1.placeBomb(bombs);
            }

            if (player2.alive) {
                if (key == player2.upKey)
                    player2.movingUp = true;
                if (key == player2.downKey)
                    player2.movingDown = true;
                if (key == player2.leftKey)
                    player2.movingLeft = true;
                if (key == player2.rightKey)
                    player2.movingRight = true;
                if (key == player2.bombKey)
                    player2.placeBomb(bombs);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == player1.upKey)
                player1.movingUp = false;
            if (key == player1.downKey)
                player1.movingDown = false;
            if (key == player1.leftKey)
                player1.movingLeft = false;
            if (key == player1.rightKey)
                player1.movingRight = false;

            if (key == player2.upKey)
                player2.movingUp = false;
            if (key == player2.downKey)
                player2.movingDown = false;
            if (key == player2.leftKey)
                player2.movingLeft = false;
            if (key == player2.rightKey)
                player2.movingRight = false;
        }
    }
}

class Player {
    int x, y;
    double px, py;
    Color color;
    boolean alive = true;
    int bombRange = 2;
    int maxBombs = 1;
    int currentBombs = 0;
    double speed = 0.1;

    boolean movingUp, movingDown, movingLeft, movingRight;
    int upKey, downKey, leftKey, rightKey, bombKey;

    public Player(int x, int y, Color color, int up, int down, int left, int right, int bomb) {
        this.x = x;
        this.y = y;
        this.px = x;
        this.py = y;
        this.color = color;
        this.upKey = up;
        this.downKey = down;
        this.leftKey = left;
        this.rightKey = right;
        this.bombKey = bomb;
    }

    public void update(int[][] map, List<Bomb> bombs) {
        if (!alive)
            return;

        double newPx = px;
        double newPy = py;

        if (movingUp)
            newPy -= speed;
        if (movingDown)
            newPy += speed;
        if (movingLeft)
            newPx -= speed;
        if (movingRight)
            newPx += speed;

        if (canMove(newPx, py, map, bombs))
            px = newPx;
        if (canMove(px, newPy, map, bombs))
            py = newPy;

        x = (int) Math.round(px);
        y = (int) Math.round(py);
    }

    private boolean canMove(double newX, double newY, int[][] map, List<Bomb> bombs) {
        int gridX = (int) Math.round(newX);
        int gridY = (int) Math.round(newY);

        if (gridX < 0 || gridX >= map[0].length || gridY < 0 || gridY >= map.length)
            return false;
        if (map[gridY][gridX] != 0)
            return false;

        for (Bomb bomb : bombs) {
            if (bomb.x == gridX && bomb.y == gridY) {
                if (!(this.x == gridX && this.y == gridY)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void placeBomb(List<Bomb> bombs) {
        if (currentBombs < maxBombs) {
            bombs.add(new Bomb(x, y, bombRange, this));
            currentBombs++;
        }
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x * 40 + 5, y * 40 + 5, 30, 30);
    }
}

class Bomb {
    int x, y, range;
    int timer = 120;
    Player owner;

    public Bomb(int x, int y, int range, Player owner) {
        this.x = x;
        this.y = y;
        this.range = range;
        this.owner = owner;
    }

    public void update() {
        timer--;
    }

    public boolean shouldExplode() {
        if (timer <= 0) {
            owner.currentBombs--;
            return true;
        }
        return false;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(x * 40 + 8, y * 40 + 8, 24, 24);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(String.valueOf(timer / 60 + 1), x * 40 + 15, y * 40 + 25);
    }
}

class Explosion {
    int x, y;
    int timer = 30;

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        timer--;
    }

    public boolean isFinished() {
        return timer <= 0;
    }

    public boolean hits(int px, int py) {
        return x == px && y == py;
    }

    public void draw(Graphics g) {
        g.setColor(new Color(255, 165, 0, 200));
        g.fillRect(x * 40, y * 40, 40, 40);
    }

}
