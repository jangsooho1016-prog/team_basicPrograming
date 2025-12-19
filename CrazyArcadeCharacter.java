import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class CrazyArcadeCharacter extends JFrame {
    private GamePanel gamePanel;

    public CrazyArcadeCharacter() {
        setTitle("Crazy Arcade - Character System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CrazyArcadeCharacter::new);
    }
}

enum CharacterType {
    // 배찌: 기본 물풍선 1, 최대 6 / 기본 물줄기 1, 최대 7 / 기본 속도 5, 최대 9
    BAZZI(new Color(255, 200, 200), "배찌",
            5, 1, 1,    // 기본 속도, 물줄기, 물풍선
            9, 7, 6),   // 최대 속도, 물줄기, 물풍선

    // 디지니: 기본 물풍선 1, 최대 10 / 기본 물줄기 1, 최대 7 / 기본 속도 5, 최대 7
    DIZNI(new Color(200, 200, 255), "디지니",
            5, 1, 1,    // 기본 속도, 물줄기, 물풍선
            7, 7, 10);  // 최대 속도, 물줄기, 물풍선

    Color color;
    String name;

    double speed;      // 기본 속도
    int bombRange;     // 기본 물줄기
    int maxBombs;      // 기본 물풍선

    int maxSpeed;
    int maxRange;
    int maxBombLimit;

    CharacterType(Color color, String name,
    double baseSpeed, int baseBombRange, int baseMaxBombs,
    int maxSpeed, int maxRange, int maxBombLimit) {

        this.color = color;
        this.name = name;

        this.speed = baseSpeed;
        this.bombRange = baseBombRange;
        this.maxBombs = baseMaxBombs;

        this.maxSpeed = maxSpeed;
        this.maxRange = maxRange;
        this.maxBombLimit = maxBombLimit;
    }
}

class GamePanel extends JPanel implements Runnable {
    private static final int TILE_SIZE = 40;
    private static final int MAP_WIDTH = 15;
    private static final int MAP_HEIGHT = 13;
    private static final int PANEL_WIDTH = MAP_WIDTH * TILE_SIZE;
    private static final int PANEL_HEIGHT = MAP_HEIGHT * TILE_SIZE;

    private Thread gameThread;
    private boolean running = false;

    private Player player1, player2;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private int[][] map;

    public GamePanel() {
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
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                    map[i][j] = 1;
                } else if (i % 2 == 0 && j % 2 == 0) {
                    map[i][j] = 1;
                } else if (Math.random() < 0.5
                        && !((i <= 2 && j <= 2) || (i >= MAP_HEIGHT - 3 && j >= MAP_WIDTH - 3))) {
                    map[i][j] = 2;
                } else {
                    map[i][j] = 0;
                }
            }
        }

        // 1P: 배찌(레드)
        player1 = new Player(
                1, 1, CharacterType.BAZZI,
                KeyEvent.VK_W, KeyEvent.VK_S,
                KeyEvent.VK_A, KeyEvent.VK_D,
                KeyEvent.VK_SPACE, KeyEvent.VK_Q,
                true   // 1P = 레드
        );

        // 2P: 디지니(블루)
        player2 = new Player(
                MAP_WIDTH - 2, MAP_HEIGHT - 2, CharacterType.DIZNI,
                KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_ENTER, KeyEvent.VK_SHIFT,
                false  // 2P = 블루
        );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        running = true;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerUpdate = 1000000000.0 / 60.0;

        double delta = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            while (delta >= 1) {
                updateGame();
                delta--;
            }

            repaint();

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        player1.update(map, bombs);
        player2.update(map, bombs);

        List<Bomb> explodedBombs = new ArrayList<>();
        List<Explosion> newExplosions = new ArrayList<>();

        for (Bomb bomb : bombs) {
            bomb.update();
            if (bomb.shouldExplode()) {
                explodedBombs.add(bomb);
                newExplosions.addAll(bomb.explode(map));
            }
        }
        bombs.removeAll(explodedBombs);
        explosions.addAll(newExplosions);

        List<Explosion> finishedExplosions = new ArrayList<>();
        for (Explosion explosion : explosions) {
            explosion.update();
            if (explosion.isFinished()) {
                finishedExplosions.add(explosion);
            } else {
                if (explosion.hits(player1.getGridX(), player1.getGridY())) {
                    player1.setAlive(false);
                }
                if (explosion.hits(player2.getGridX(), player2.getGridY())) {
                    player2.setAlive(false);
                }
            }
        }
        explosions.removeAll(finishedExplosions);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                if (map[i][j] == 1) {
                    g.setColor(Color.GRAY);
                    g.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else if (map[i][j] == 2) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    g.setColor(new Color(150, 220, 150));
                    g.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
                g.setColor(Color.BLACK);
                g.drawRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        for (Bomb bomb : bombs) {
            bomb.draw(g);
        }

        for (Explosion explosion : explosions) {
            explosion.draw(g);
        }

        player1.draw(g);
        player2.draw(g);

        g.setColor(Color.BLACK);
        g.drawString("Player1 (배찌): " + (player1.isAlive() ? "Alive" : "Dead"), 10, 15);
        g.drawString("Player2 (디지니): " + (player2.isAlive() ? "Alive" : "Dead"), 10, 30);
    }

    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            player1.keyPressed(e, bombs, map);
            player2.keyPressed(e, bombs, map);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            player1.keyReleased(e);
            player2.keyReleased(e);
        }
    }
}

class Player {
    int x, y;
    double px, py;
    CharacterType characterType;
    boolean alive = true;
    int bombRange;
    int maxBombs;
    int currentBombs = 0;
    double speed;

    int skillCooldown = 0;
    int skillDuration = 0;
    boolean skillActive = false;

    // 방향키 눌림 상태
    boolean movingUp, movingDown, movingLeft, movingRight;
    int upKey, downKey, leftKey, rightKey, bombKey, skillKey;

    // 1P/2P 색 구분
    final boolean isRedPlayer;

    // 🔥 "먼저 눌린 방향키" 순서를 저장하는 리스트
    //   - 0번 인덱스: 가장 먼저 눌려서 아직 안 뗀 키
    //   - 마지막 인덱스: 가장 나중에 눌린 키
    private final List<Integer> moveKeyOrder = new ArrayList<>();

    // 스프라이트
    private static final Image RED_BAZZI_SPRITE  = new ImageIcon("RedBazzi.bmp").getImage();
    private static final Image RED_DIZNI_SPRITE  = new ImageIcon("RedDizni.bmp").getImage();
    private static final Image BLUE_BAZZI_SPRITE = new ImageIcon("BlueBazzi.bmp").getImage();
    private static final Image BLUE_DIZNI_SPRITE = new ImageIcon("BlueDizni.bmp").getImage();

    public Player(int x, int y, CharacterType type,
        int up, int down, int left, int right,
        int bomb, int skill,
        boolean isRedPlayer) {
        this.x = x;
        this.y = y;
        this.px = x;
        this.py = y;
        this.characterType = type;
        this.speed = type.speed;
        this.bombRange = type.bombRange;
        this.maxBombs = type.maxBombs;
        this.upKey = up;
        this.downKey = down;
        this.leftKey = left;
        this.rightKey = right;
        this.bombKey = bomb;
        this.skillKey = skill;
        this.isRedPlayer = isRedPlayer;
    }

    private Image getCurrentSprite() {
        if (isRedPlayer) {
            if (characterType == CharacterType.BAZZI) return RED_BAZZI_SPRITE;
            else return RED_DIZNI_SPRITE;
        } else {
            if (characterType == CharacterType.BAZZI) return BLUE_BAZZI_SPRITE;
            else return BLUE_DIZNI_SPRITE;
        }
    }

    public void update(int[][] map, List<Bomb> bombs) {
        if (!alive)
            return;

        if (skillCooldown > 0)
            skillCooldown--;
        if (skillDuration > 0) {
            skillDuration--;
            if (skillDuration == 0)
                skillActive = false;
        }

        // 🔹 moveKeyOrder 정리:
        //    리스트 맨 앞에 있는 키가 실제로는 더 이상 눌려있지 않다면 제거
        cleanMoveKeyOrder();

        double newPx = px;
        double newPy = py;

        double basePixelSpeed = speed * 0.02;
        double currentSpeed = skillActive ? basePixelSpeed * 1.5 : basePixelSpeed;

        // 🔥 "가장 먼저 눌린 방향키" 한 개만 보고 이동
        if (!moveKeyOrder.isEmpty()) {
            int firstKey = moveKeyOrder.get(0);

            if (firstKey == upKey && movingUp) {
                newPy -= currentSpeed;
            } else if (firstKey == downKey && movingDown) {
                newPy += currentSpeed;
            } else if (firstKey == leftKey && movingLeft) {
                newPx -= currentSpeed;
            } else if (firstKey == rightKey && movingRight) {
                newPx += currentSpeed;
            }
        }

        if (canMove(newPx, py, map, bombs))
            px = newPx;
        if (canMove(px, newPy, map, bombs))
            py = newPy;

        x = (int) Math.round(px);
        y = (int) Math.round(py);
    }

    // 🔹 리스트 맨 앞 키가 실제로 눌려있는지 확인하고, 아니면 제거
    private void cleanMoveKeyOrder() {
        while (!moveKeyOrder.isEmpty()) {
            int first = moveKeyOrder.get(0);
            boolean stillHeld =
                    (first == upKey && movingUp) ||
                    (first == downKey && movingDown) ||
                    (first == leftKey && movingLeft) ||
                    (first == rightKey && movingRight);

            if (!stillHeld) {
                moveKeyOrder.remove(0);
            } else {
                break;
            }
        }
    }

    private boolean canMove(double newPx, double newPy, int[][] map, List<Bomb> bombs) {
        int gridX = (int) Math.round(newPx);
        int gridY = (int) Math.round(newPy);

        if (gridX < 0 || gridX >= map[0].length || gridY < 0 || gridY >= map.length)
            return false;

        if (map[gridY][gridX] == 1 || map[gridY][gridX] == 2)
            return false;

        for (Bomb bomb : bombs) {
            if (bomb.getX() == gridX && bomb.getY() == gridY)
                return false;
        }

        return true;
    }

    // 🔹 이동키를 순서 리스트에 추가
    private void addMoveKey(int key) {
        if (key == upKey || key == downKey || key == leftKey || key == rightKey) {
            if (!moveKeyOrder.contains(key)) {
                moveKeyOrder.add(key);
            }
        }
    }

    // 🔹 이동키를 순서 리스트에서 제거
    private void removeMoveKey(int key) {
        moveKeyOrder.remove(Integer.valueOf(key));
    }

    public void keyPressed(KeyEvent e, List<Bomb> bombs, int[][] map) {
        if (!alive)
            return;

        int key = e.getKeyCode();

        if (key == upKey) {
            movingUp = true;
            addMoveKey(key);
        }
        if (key == downKey) {
            movingDown = true;
            addMoveKey(key);
        }
        if (key == leftKey) {
            movingLeft = true;
            addMoveKey(key);
        }
        if (key == rightKey) {
            movingRight = true;
            addMoveKey(key);
        }

        if (key == bombKey) {
            placeBomb(bombs, map);
        }

        if (key == skillKey) {
            useSkill();
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == upKey) {
            movingUp = false;
            removeMoveKey(key);
        }
        if (key == downKey) {
            movingDown = false;
            removeMoveKey(key);
        }
        if (key == leftKey) {
            movingLeft = false;
            removeMoveKey(key);
        }
        if (key == rightKey) {
            movingRight = false;
            removeMoveKey(key);
        }
    }

    private void placeBomb(List<Bomb> bombs, int[][] map) {
        if (currentBombs >= maxBombs)
            return;

        for (Bomb bomb : bombs) {
            if (bomb.getX() == x && bomb.getY() == y)
                return;
        }

        bombs.add(new Bomb(x, y, bombRange, this));
        currentBombs++;
    }

    public void useSkill() {
        if (skillCooldown > 0)
            return;

        skillCooldown = 180;
        skillDuration = 60;
        skillActive = true;

        if (characterType == CharacterType.BAZZI) {
            bombRange = Math.min(bombRange + 1, characterType.maxRange);
        } else if (characterType == CharacterType.DIZNI) {
            maxBombs = Math.min(maxBombs + 1, characterType.maxBombLimit);
        }
    }

    public void draw(Graphics g) {
        if (!alive) {
            g.setColor(Color.DARK_GRAY);
            g.fillOval((int) (px * 40 + 8), (int) (py * 40 + 8), 24, 24);
            return;
        }

        int screenX = (int)(px * 40);
        int screenY = (int)(py * 40);

        Image sprite = getCurrentSprite();
        if (sprite != null) {
            g.drawImage(sprite, screenX, screenY, 40, 40, null);
        } else {
            g.setColor(characterType.color);
            g.fillOval(screenX + 4, screenY + 4, 32, 32);
            g.setColor(Color.BLACK);
            g.drawOval(screenX + 4, screenY + 4, 32, 32);
        }

        g.setColor(Color.BLACK);
        g.drawString(characterType.name, screenX + 5, screenY + 50);
    }

    public int getGridX() {
        return x;
    }

    public int getGridY() {
        return y;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}

class Bomb {
    private int x, y;
    private int range;
    private int timer = 120;
    private Player owner;

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

    public List<Explosion> explode(int[][] map) {
        List<Explosion> explosions = new ArrayList<>();
        explosions.add(new Explosion(x, y));

        for (int dx = 1; dx <= range; dx++) {
            if (!addExplosionIfPossible(explosions, map, x + dx, y))
                break;
        }
        for (int dx = 1; dx <= range; dx++) {
            if (!addExplosionIfPossible(explosions, map, x - dx, y))
                break;
        }
        for (int dy = 1; dy <= range; dy++) {
            if (!addExplosionIfPossible(explosions, map, x, y + dy))
                break;
        }
        for (int dy = 1; dy <= range; dy++) {
            if (!addExplosionIfPossible(explosions, map, x, y - dy))
                break;
        }

        return explosions;
    }

    private boolean addExplosionIfPossible(List<Explosion> explosions, int[][] map, int x, int y) {
        if (x < 0 || x >= map[0].length || y < 0 || y >= map.length)
            return false;

        if (map[y][x] == 1)
            return false;

        explosions.add(new Explosion(x, y));

        if (map[y][x] == 2) {
            map[y][x] = 0;
            return false;
        }

        return true;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(x * 40 + 8, y * 40 + 8, 24, 24);
        g.setColor(Color.WHITE);
        g.fillOval(x * 40 + 14, y * 40 + 12, 6, 6);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

class Explosion {
    private int x, y;
    private int timer = 30;

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