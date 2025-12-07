import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CrazyArcade_Item extends JFrame {
    private GamePanel gamePanel;

    public CrazyArcade_Item() {
        setTitle("Crazy Arcade - Item System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrazyArcade_Item());
    }
}


enum ItemType {
    BOMB_UP("폭탄+", Color.YELLOW, 0), 
    RANGE_UP("범위+", Color.CYAN, 1), 
    SPEED_UP("속도+", Color.GREEN, 2), 
    SHIELD("방어막", Color.MAGENTA, 3); 

    String name;
    Color color;
    int id;

    ItemType(String name, Color color, int id) {
        this.name = name;
        this.color = color;
        this.id = id;
    }
}

class Item {
    int x, y;
    ItemType type;
    int lifeTime = 600; 

    public Item(int x, int y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update() {
        lifeTime--;
    }

    public boolean isExpired() {
        return lifeTime <= 0;
    }

    public void draw(Graphics g) {
  
        g.setColor(type.color);
        g.fillRoundRect(x * 40 + 5, y * 40 + 5, 30, 30, 10, 10);

        g.setColor(Color.WHITE);
        g.drawRoundRect(x * 40 + 5, y * 40 + 5, 30, 30, 10, 10);

        g.setColor(Color.BLACK);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(type.name);
        g.drawString(type.name, x * 40 + 20 - textWidth / 2, y * 40 + 25);

        if (lifeTime < 120 && lifeTime % 20 < 10) {
            g.setColor(new Color(255, 255, 255, 150));
            g.fillRoundRect(x * 40 + 5, y * 40 + 5, 30, 30, 10, 10);
        }
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
    private List<Item> items;
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
        items = new ArrayList<>(); 

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

        player1 = new Player(1, 1, Color.BLUE, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D,
                KeyEvent.VK_SPACE);
        player2 = new Player(MAP_WIDTH - 2, MAP_HEIGHT - 2, Color.RED, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER);
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

    
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            item.update();
            if (item.isExpired()) {
                items.remove(i);
            }
        }


        checkItemCollection();

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
            if (exp.hits(player1.x, player1.y)) {
                if (player1.hasShield) {
                    player1.hasShield = false; 
                } else {
                    player1.alive = false;
                }
            }
            if (exp.hits(player2.x, player2.y)) {
                if (player2.hasShield) {
                    player2.hasShield = false; 
                } else {
                    player2.alive = false;
                }
            }
        }
    }

    private void checkItemCollection() {
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);

            if (player1.x == item.x && player1.y == item.y) {
                applyItemEffect(player1, item.type);
                items.remove(i);
                continue;
            }

   
            if (player2.x == item.x && player2.y == item.y) {
                applyItemEffect(player2, item.type);
                items.remove(i);
            }
        }
    }


    private void applyItemEffect(Player player, ItemType type) {
        switch (type) {
            case BOMB_UP:
                player.maxBombs++;
                break;
            case RANGE_UP:
                player.bombRange++;
                break;
            case SPEED_UP:
                player.speed += 0.02;
                if (player.speed > 0.2)
                    player.speed = 0.2; 
                break;
            case SHIELD:
                player.hasShield = true;
                break;
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

                  
                    if (Math.random() < 0.5) { 
                        ItemType[] types = ItemType.values();
                        ItemType randomType = types[(int) (Math.random() * types.length)];
                        items.add(new Item(nx, ny, randomType));
                    }
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

    
        for (Item item : items) {
            item.draw(g);
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

    
        drawPlayerStats(g);

        if (!player1.alive || !player2.alive) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 40));
            String winner = !player1.alive && !player2.alive ? "무승부!" : !player1.alive ? "빨강 승리!" : "파랑 승리!";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(winner);
            g.drawString(winner, (PANEL_WIDTH - textWidth) / 2, PANEL_HEIGHT / 2);
        }
    }

    private void drawPlayerStats(Graphics g) {
      
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(5, 5, 200, 60);

        g.setColor(player1.color);
        g.fillOval(10, 10, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g.drawString("플레이어 1", 35, 20);
        g.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        g.drawString("폭탄: " + (player1.maxBombs - player1.currentBombs) + "/" + player1.maxBombs, 35, 35);
        g.drawString("범위: " + player1.bombRange, 110, 35);
        g.drawString("속도: " + String.format("%.2f", player1.speed), 35, 50);
        if (player1.hasShield) {
            g.setColor(Color.MAGENTA);
            g.drawString("방어막 ●", 110, 50);
        }

 
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(PANEL_WIDTH - 205, 5, 200, 60);

        g.setColor(player2.color);
        g.fillOval(PANEL_WIDTH - 30, 10, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g.drawString("플레이어 2", PANEL_WIDTH - 95, 20);
        g.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        g.drawString("폭탄: " + (player2.maxBombs - player2.currentBombs) + "/" + player2.maxBombs, PANEL_WIDTH - 195,
                35);
        g.drawString("범위: " + player2.bombRange, PANEL_WIDTH - 120, 35);
        g.drawString("속도: " + String.format("%.2f", player2.speed), PANEL_WIDTH - 195, 50);
        if (player2.hasShield) {
            g.setColor(Color.MAGENTA);
            g.drawString("방어막 ●", PANEL_WIDTH - 120, 50);
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
    boolean hasShield = false; 

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

 
        if (hasShield) {
            g.setColor(new Color(255, 0, 255, 100));
            g.drawOval(x * 40 + 2, y * 40 + 2, 36, 36);
            g.drawOval(x * 40 + 3, y * 40 + 3, 34, 34);
        }
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
