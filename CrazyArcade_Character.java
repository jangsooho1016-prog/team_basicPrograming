import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CrazyArcade_Character extends JFrame {
    private GamePanel gamePanel;

    public CrazyArcade_Character() {
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
        SwingUtilities.invokeLater(() -> new CrazyArcade_Character());
    }
}

// ============== 캐릭터 타입 ==============
enum CharacterType {
    SPEED(Color.CYAN, "스피드", 0.15, 2, 1), // 빠른 이동
    POWER(Color.RED, "파워", 0.08, 3, 1), // 넓은 폭발 범위
    BOMBER(Color.ORANGE, "보머", 0.1, 2, 2), // 많은 폭탄
    BALANCED(Color.BLUE, "밸런스", 0.1, 2, 1); // 균형잡힌 능력

    Color color;
    String name;
    double speed;
    int bombRange;
    int maxBombs;

    CharacterType(Color color, String name, double speed, int bombRange, int maxBombs) {
        this.color = color;
        this.name = name;
        this.speed = speed;
        this.bombRange = bombRange;
        this.maxBombs = maxBombs;
    }
}

class GamePanel extends JPanel implements Runnable {
    private static final int TILE_SIZE = 40;
    private static final int MAP_WIDTH = 15;
    private static final int MAP_HEIGHT = 13;
    private static final int PANEL_WIDTH = TILE_SIZE * MAP_WIDTH;
    private static final int PANEL_HEIGHT = TILE_SIZE * MAP_HEIGHT + 60; // UI 공간 추가

    private Thread gameThread;
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
                }
            }
        }

        // TODO: 여기서 캐릭터 타입을 선택할 수 있게 변경하세요!
        player1 = new Player(1, 1, CharacterType.SPEED, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D,
                KeyEvent.VK_SPACE, KeyEvent.VK_Q);
        player2 = new Player(MAP_WIDTH - 2, MAP_HEIGHT - 2, CharacterType.POWER, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER, KeyEvent.VK_SHIFT);
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

        // 맵 그리기
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

        // UI 그리기 (하단에 플레이어 정보)
        drawUI(g);

        if (!player1.alive || !player2.alive) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 40));
            String winner = !player1.alive && !player2.alive ? "무승부!"
                    : !player1.alive ? player2.characterType.name + " 승리!" : player1.characterType.name + " 승리!";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(winner);
            g.drawString(winner, (PANEL_WIDTH - textWidth) / 2, MAP_HEIGHT * TILE_SIZE / 2);
        }
    }

    private void drawUI(Graphics g) {
        int uiY = MAP_HEIGHT * TILE_SIZE;

        // 배경
        g.setColor(new Color(50, 50, 50));
        g.fillRect(0, uiY, PANEL_WIDTH, 60);

        // 플레이어 1 정보
        g.setColor(player1.characterType.color);
        g.fillRect(10, uiY + 10, 40, 40);
        g.setColor(Color.WHITE);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        g.drawString(player1.characterType.name, 60, uiY + 25);
        g.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        g.drawString("속도: " + String.format("%.2f", player1.speed), 60, uiY + 40);
        g.drawString("폭탄: " + player1.currentBombs + "/" + player1.maxBombs, 150, uiY + 25);
        g.drawString("범위: " + player1.bombRange, 150, uiY + 40);

        // 플레이어 2 정보
        g.setColor(player2.characterType.color);
        g.fillRect(PANEL_WIDTH - 50, uiY + 10, 40, 40);
        g.setColor(Color.WHITE);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        String p2Name = player2.characterType.name;
        g.drawString(p2Name, PANEL_WIDTH - 60 - fm.stringWidth(p2Name), uiY + 25);
        g.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        fm = g.getFontMetrics();
        String p2Speed = "속도: " + String.format("%.2f", player2.speed);
        g.drawString(p2Speed, PANEL_WIDTH - 60 - fm.stringWidth(p2Speed), uiY + 40);
        String p2Bomb = "폭탄: " + player2.currentBombs + "/" + player2.maxBombs;
        g.drawString(p2Bomb, PANEL_WIDTH - 210, uiY + 25);
        String p2Range = "범위: " + player2.bombRange;
        g.drawString(p2Range, PANEL_WIDTH - 210, uiY + 40);

        // 특수 능력 쿨다운 표시
        if (player1.skillCooldown > 0) {
            g.setColor(Color.YELLOW);
            g.fillRect(250, uiY + 20, (int) (player1.skillCooldown / 180.0 * 50), 20);
            g.setColor(Color.WHITE);
            g.drawString("P1 스킬: " + (player1.skillCooldown / 60), 250, uiY + 35);
        }

        if (player2.skillCooldown > 0) {
            g.setColor(Color.YELLOW);
            g.fillRect(PANEL_WIDTH - 300, uiY + 20, (int) (player2.skillCooldown / 180.0 * 50), 20);
            g.setColor(Color.WHITE);
            g.drawString("P2 스킬: " + (player2.skillCooldown / 60), PANEL_WIDTH - 350, uiY + 35);
        }
    }

    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

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
                if (key == player1.skillKey)
                    player1.useSkill(); // 특수 능력
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
                if (key == player2.skillKey)
                    player2.useSkill(); // 특수 능력
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
    CharacterType characterType;
    boolean alive = true;
    int bombRange;
    int maxBombs;
    int currentBombs = 0;
    double speed;

    // 특수 능력 관련
    int skillCooldown = 0;
    int skillDuration = 0;
    boolean skillActive = false;

    boolean movingUp, movingDown, movingLeft, movingRight;
    int upKey, downKey, leftKey, rightKey, bombKey, skillKey;

    public Player(int x, int y, CharacterType type, int up, int down, int left, int right, int bomb, int skill) {
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
    }

    public void update(int[][] map, List<Bomb> bombs) {
        if (!alive)
            return;

        // 스킬 쿨다운 감소
        if (skillCooldown > 0)
            skillCooldown--;
        if (skillDuration > 0) {
            skillDuration--;
            if (skillDuration == 0)
                skillActive = false;
        }

        double newPx = px;
        double newPy = py;

        double currentSpeed = skillActive ? speed * 1.5 : speed; // 스킬 사용시 속도 증가

        if (movingUp)
            newPy -= currentSpeed;
        if (movingDown)
            newPy += currentSpeed;
        if (movingLeft)
            newPx -= currentSpeed;
        if (movingRight)
            newPx += currentSpeed;

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

    // TODO: 캐릭터별 다른 특수 능력을 추가하세요!
    public void useSkill() {
        if (skillCooldown > 0)
            return; // 쿨다운 중

        skillCooldown = 180; // 3초 쿨다운
        skillDuration = 60; // 1초 지속
        skillActive = true;

        // 캐릭터 타입별 특수 능력
        switch (characterType) {
            case SPEED:
                // 이미 속도 증가 적용됨
                break;
            case POWER:
                bombRange += 2; // 일시적으로 범위 증가
                break;
            case BOMBER:
                maxBombs += 2; // 일시적으로 폭탄 개수 증가
                break;
            case BALANCED:
                speed *= 1.3;
                bombRange += 1;
                break;
        }
    }

    public void draw(Graphics g) {
        // 캐릭터 색상
        g.setColor(characterType.color);
        g.fillOval(x * 40 + 5, y * 40 + 5, 30, 30);

        // 스킬 사용 중일 때 효과
        if (skillActive) {
            g.setColor(new Color(255, 255, 0, 100));
            g.fillOval(x * 40, y * 40, 40, 40);
        }

        // 캐릭터 눈 그리기 (귀여운 효과)
        g.setColor(Color.WHITE);
        g.fillOval(x * 40 + 12, y * 40 + 12, 6, 6);
        g.fillOval(x * 40 + 22, y * 40 + 12, 6, 6);
        g.setColor(Color.BLACK);
        g.fillOval(x * 40 + 14, y * 40 + 14, 3, 3);
        g.fillOval(x * 40 + 24, y * 40 + 14, 3, 3);
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