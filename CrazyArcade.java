import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CrazyArcade extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPanel menuPanel;
    private CharacterSelectionPanel charSelectionPanel;
    private MapSelectionPanel mapSelectionPanel;
    private GamePanel gamePanel;

    public CrazyArcade() {
        setTitle("Crazy Arcade - Integrated");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        menuPanel = new MenuPanel(this);
        charSelectionPanel = new CharacterSelectionPanel(this);
        mapSelectionPanel = new MapSelectionPanel(this);

        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(charSelectionPanel, "CHAR_SELECT");
        mainPanel.add(mapSelectionPanel, "MAP_SELECT");

        add(mainPanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showCharacterSelect() {
        cardLayout.show(mainPanel, "CHAR_SELECT");
    }

    public void showMapSelect(CharacterType p1, CharacterType p2) {
        mapSelectionPanel.setSelectedCharacters(p1, p2);
        cardLayout.show(mainPanel, "MAP_SELECT");
    }

    public void startGame(int mapType, CharacterType p1Type, CharacterType p2Type) {
        if (gamePanel != null) {
            mainPanel.remove(gamePanel);
        }
        gamePanel = new GamePanel(this, mapType, p1Type, p2Type);
        mainPanel.add(gamePanel, "GAME");
        cardLayout.show(mainPanel, "GAME");
        gamePanel.requestFocusInWindow();
    }

    public void backToMenu() {
        cardLayout.show(mainPanel, "MENU");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CrazyArcade::new);
    }

    enum CharacterType {
        SPEED(Color.CYAN, "스피드", 0.15, 2, 1),
        POWER(Color.RED, "파워", 0.08, 3, 1),
        BOMBER(Color.ORANGE, "보머", 0.1, 2, 2),
        BALANCED(Color.BLUE, "밸런스", 0.1, 2, 1);

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

    enum ItemType {
        BOMB_UP("폭탄+", Color.YELLOW, 0),
        RANGE_UP("범위+", Color.CYAN, 1),
        SPEED_UP("이속+", Color.PINK, 2),
        SHIELD("실드", Color.GREEN, 3);

        String name;
        Color color;
        int id;

        ItemType(String name, Color color, int id) {
            this.name = name;
            this.color = color;
            this.id = id;
        }
    }
    
    static class MenuPanel extends JPanel {
        private CrazyArcade game;

        public MenuPanel(CrazyArcade game) {
            this.game = game;
            setPreferredSize(new Dimension(600, 520));
            setBackground(new Color(240, 248, 255)); // AliceBlue
            setLayout(null);

            JLabel titleLabel = new JLabel("Crazy Arcade");
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 60));
            titleLabel.setForeground(new Color(65, 105, 225));
            titleLabel.setBounds(100, 100, 400, 80);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(titleLabel);

            JButton startBtn = createButton("게임 시작", 200, 250);
            startBtn.addActionListener(e -> game.showCharacterSelect());
            add(startBtn);

            JButton exitBtn = createButton("종료", 200, 350);
            exitBtn.addActionListener(e -> System.exit(0));
            add(exitBtn);
        }

        private JButton createButton(String text, int x, int y) {
            JButton btn = new JButton(text);
            btn.setBounds(x, y, 200, 60);
            btn.setFont(new Font("맑은 고딕", Font.BOLD, 20));
            btn.setBackground(Color.WHITE);
            btn.setFocusPainted(false);
            return btn;
        }
    }

    static class CharacterSelectionPanel extends JPanel {
        private CrazyArcade game;
        private CharacterType p1Selected = CharacterType.SPEED;
        private CharacterType p2Selected = CharacterType.BALANCED;
        private JLabel p1Label, p2Label;

        public CharacterSelectionPanel(CrazyArcade game) {
            this.game = game;
            setPreferredSize(new Dimension(600, 520));
            setBackground(new Color(255, 250, 240));
            setLayout(null);

            JLabel title = new JLabel("캐릭터 선택");
            title.setFont(new Font("맑은 고딕", Font.BOLD, 40));
            title.setBounds(180, 20, 300, 50);
            add(title);

          
            createSelectionArea(50, 100, "플레이어 1 (WASD)", 1);
           
            createSelectionArea(320, 100, "플레이어 2 (방향키)", 2);

            JButton nextBtn = new JButton("맵 선택으로");
            nextBtn.setBounds(200, 420, 200, 60);
            nextBtn.setFont(new Font("맑은 고딕", Font.BOLD, 20));
            nextBtn.addActionListener(e -> game.showMapSelect(p1Selected, p2Selected));
            add(nextBtn);
        }

        private void createSelectionArea(int x, int y, String title, int playerNum) {
            JLabel label = new JLabel(title);
            label.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            label.setBounds(x, y, 200, 30);
            add(label);

            int currentY = y + 40;
            for (CharacterType type : CharacterType.values()) {
                JButton btn = new JButton(type.name);
                btn.setBounds(x, currentY, 200, 40);
                btn.setBackground(type.color);
                btn.addActionListener(e -> {
                    if (playerNum == 1)
                        p1Selected = type;
                    else
                        p2Selected = type;
                    updateLabels();
                });
                add(btn);
                currentY += 50;
            }

            if (playerNum == 1) {
                p1Label = new JLabel("선택: " + p1Selected.name);
                p1Label.setBounds(x, currentY, 200, 30);
                add(p1Label);
            } else {
                p2Label = new JLabel("선택: " + p2Selected.name);
                p2Label.setBounds(x, currentY, 200, 30);
                add(p2Label);
            }
        }

        private void updateLabels() {
            p1Label.setText("선택: " + p1Selected.name);
            p2Label.setText("선택: " + p2Selected.name);
        }
    }

    static class MapSelectionPanel extends JPanel {
        private CrazyArcade game;
        private CharacterType p1Type, p2Type;

        public MapSelectionPanel(CrazyArcade game) {
            this.game = game;
            setPreferredSize(new Dimension(600, 520));
            setBackground(new Color(135, 206, 235));
            setLayout(null);

            JLabel titleLabel = new JLabel("맵 선택");
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 40));
            titleLabel.setBounds(220, 30, 200, 50);
            add(titleLabel);

            JButton map1Btn = createMapButton("기본 맵", 50, 120);
            map1Btn.addActionListener(e -> game.startGame(1, p1Type, p2Type));
            add(map1Btn);

            JButton map2Btn = createMapButton("미로 맵", 320, 120);
            map2Btn.addActionListener(e -> game.startGame(2, p1Type, p2Type));
            add(map2Btn);

            JButton map3Btn = createMapButton("오픈 맵", 50, 280);
            map3Btn.addActionListener(e -> game.startGame(3, p1Type, p2Type));
            add(map3Btn);

            JButton map4Btn = createMapButton("대칭 맵", 320, 280);
            map4Btn.addActionListener(e -> game.startGame(4, p1Type, p2Type));
            add(map4Btn);
        }

        public void setSelectedCharacters(CharacterType p1, CharacterType p2) {
            this.p1Type = p1;
            this.p2Type = p2;
        }

        private JButton createMapButton(String text, int x, int y) {
            JButton btn = new JButton(text);
            btn.setBounds(x, y, 230, 120);
            btn.setFont(new Font("맑은 고딕", Font.BOLD, 24));
            btn.setFocusPainted(false);
            return btn;
        }
    }

    static class GamePanel extends JPanel implements Runnable {
        private static final int TILE_SIZE = 40;
        private static final int MAP_WIDTH = 15;
        private static final int MAP_HEIGHT = 13;
        private static final int PANEL_WIDTH = TILE_SIZE * MAP_WIDTH;
        private static final int PANEL_HEIGHT = TILE_SIZE * MAP_HEIGHT + 80;

        private Thread gameThread;
        private boolean isRunning = false;
        private CrazyArcade game;
        private int mapType;
        private CharacterType p1Type, p2Type;

        private Player player1, player2;
        private List<Bomb> bombs;
        private List<Explosion> explosions;
        private List<Item> items;
        private int[][] map;

        public GamePanel(CrazyArcade game, int mapType, CharacterType p1Type, CharacterType p2Type) {
            this.game = game;
            this.mapType = mapType;
            this.p1Type = p1Type;
            this.p2Type = p2Type;

            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            setBackground(Color.WHITE);
            setFocusable(true);

            initGame();
            addKeyListener(new GameKeyListener());

            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        private void initGame() {
            bombs = new ArrayList<>();
            explosions = new ArrayList<>();
            items = new ArrayList<>();
            map = new int[MAP_HEIGHT][MAP_WIDTH];

            createMap();

            player1 = new Player(1, 1, p1Type, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D,
                    KeyEvent.VK_SPACE, KeyEvent.VK_Q);
            player2 = new Player(MAP_WIDTH - 2, MAP_HEIGHT - 2, p2Type, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                    KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER, KeyEvent.VK_SHIFT);
        }

        private void createMap() {
            for (int i = 0; i < MAP_HEIGHT; i++) {
                for (int j = 0; j < MAP_WIDTH; j++) {
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        map[i][j] = 1; // Unbreakable Wall
                    } else if (i % 2 == 0 && j % 2 == 0) {
                        map[i][j] = 1; // Unbreakable Wall
                    } else {
                        // Safe zone
                        if ((i <= 2 && j <= 2) || (i >= MAP_HEIGHT - 3 && j >= MAP_WIDTH - 3)) {
                            map[i][j] = 0;
                            continue;
                        }

                        double blockRate = 0;
                        switch (mapType) {
                            case 1:
                                blockRate = 0.5;
                                break; // Basic
                            case 2: // Maze-like
                                if ((i % 4 == 1 && j % 3 == 0) || (j % 4 == 1 && i % 3 == 0))
                                    map[i][j] = 2;
                                continue;
                            case 3:
                                blockRate = 0.2;
                                break; // Open
                            case 4:
                                blockRate = 0.4;
                                break; // Symmetric
                        }

                        if (Math.random() < blockRate)
                            map[i][j] = 2; // Breakable Block
                        if (mapType == 4 && map[i][j] == 2) {
                            // Mirror for symmetric map
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

            while (isRunning) {
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
            player1.update(map, bombs, items);
            player2.update(map, bombs, items);

            // Update Bombs
            for (int i = bombs.size() - 1; i >= 0; i--) {
                Bomb bomb = bombs.get(i);
                bomb.update();
                if (bomb.shouldExplode()) {
                    createExplosion(bomb);
                    bombs.remove(i);
                }
            }

            // Update Explosions
            for (int i = explosions.size() - 1; i >= 0; i--) {
                Explosion exp = explosions.get(i);
                exp.update();
                if (exp.isFinished()) {
                    explosions.remove(i);
                }
            }

            // Update Items
            for (int i = items.size() - 1; i >= 0; i--) {
                Item item = items.get(i);
                item.update();
                if (item.isExpired()) {
                    items.remove(i);
                }
            }

            // Check Player Death
            for (Explosion exp : explosions) {
                if (exp.hits(player1.x, player1.y) && !player1.hasShield)
                    player1.alive = false;
                if (exp.hits(player2.x, player2.y) && !player2.hasShield)
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
                    if (map[ny][nx] == 1) // Unbreakable
                        break;

                    explosions.add(new Explosion(nx, ny));

                    if (map[ny][nx] == 2) { // Breakable
                        map[ny][nx] = 0;
                        // Chance to spawn item
                        if (Math.random() < 0.3) {
                            ItemType type = ItemType.values()[(int) (Math.random() * ItemType.values().length)];
                            items.add(new Item(nx, ny, type));
                        }
                        break;
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw Map
            for (int i = 0; i < MAP_HEIGHT; i++) {
                for (int j = 0; j < MAP_WIDTH; j++) {
                    int x = j * TILE_SIZE;
                    int y = i * TILE_SIZE;

                    if (map[i][j] == 1) {
                        g.setColor(Color.DARK_GRAY);
                        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        g.setColor(Color.GRAY);
                        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    } else if (map[i][j] == 2) {
                        g.setColor(new Color(210, 105, 30)); // Chocolate
                        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        g.setColor(new Color(139, 69, 19));
                        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    } else {
                        g.setColor(new Color(240, 240, 240));
                        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                }
            }

            for (Item item : items)
                item.draw(g);

            for (Bomb bomb : bombs)
                bomb.draw(g);

            for (Explosion exp : explosions)
                exp.draw(g);

            if (player1.alive)
                player1.draw(g);
            if (player2.alive)
                player2.draw(g);

            drawUI(g);

            if (!player1.alive || !player2.alive) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

                g.setColor(Color.WHITE);
                g.setFont(new Font("맑은 고딕", Font.BOLD, 40));
                String winner = !player1.alive && !player2.alive ? "무승부!"
                        : (!player1.alive ? player2.characterType.name + " 승리!" : player1.characterType.name + " 승리!");
                FontMetrics fm = g.getFontMetrics();
                g.drawString(winner, (PANEL_WIDTH - fm.stringWidth(winner)) / 2, PANEL_HEIGHT / 2);

                g.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
                String info = "ESC - 메뉴로 돌아가기";
                g.drawString(info, (PANEL_WIDTH - g.getFontMetrics().stringWidth(info)) / 2, PANEL_HEIGHT / 2 + 50);
            }
        }

        private void drawUI(Graphics g) {
            int uiY = MAP_HEIGHT * TILE_SIZE;
            g.setColor(new Color(50, 50, 50));
            g.fillRect(0, uiY, PANEL_WIDTH, 80);

            drawPlayerStats(g, player1, 10, uiY + 10);
            drawPlayerStats(g, player2, PANEL_WIDTH / 2 + 10, uiY + 10);
        }

        private void drawPlayerStats(Graphics g, Player p, int x, int y) {
            g.setColor(p.characterType.color);
            g.fillRect(x, y, 30, 30);
            g.setColor(Color.WHITE);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            g.drawString(p.characterType.name, x + 40, y + 20);

            g.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            g.drawString("폭탄: " + p.currentBombs + "/" + p.maxBombs, x + 100, y + 15);
            g.drawString("범위: " + p.bombRange, x + 100, y + 30);
            g.drawString("속도: " + String.format("%.2f", p.speed), x + 180, y + 15);

            if (p.skillCooldown > 0) {
                g.setColor(Color.ORANGE);
                g.fillRect(x, y + 40, (int) (p.skillCooldown / 180.0 * 200), 10);
                g.drawString("스킬 쿨다운", x + 210, y + 50);
            } else if (p.skillActive) {
                g.setColor(Color.GREEN);
                g.fillRect(x, y + 40, (int) (p.skillDuration / 60.0 * 200), 10);
                g.drawString("스킬 사용중!", x + 210, y + 50);
            } else {
                g.setColor(Color.WHITE);
                g.drawString("스킬 준비됨 (" + KeyEvent.getKeyText(p.skillKey) + ")", x, y + 50);
            }
        }

        private class GameKeyListener extends KeyAdapter {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ESCAPE) {
                    isRunning = false;
                    game.backToMenu();
                }

                if (player1.alive)
                    player1.keyPressed(key, bombs);
                if (player2.alive)
                    player2.keyPressed(key, bombs);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (player1.alive)
                    player1.keyReleased(key);
                if (player2.alive)
                    player2.keyReleased(key);
            }
        }
    }

    static class Player {
        int x, y;
        double px, py;
        CharacterType characterType;
        boolean alive = true;
        int bombRange;
        int maxBombs;
        int currentBombs = 0;
        double speed;
        boolean hasShield = false;

        boolean movingUp, movingDown, movingLeft, movingRight;
        int upKey, downKey, leftKey, rightKey, bombKey, skillKey;

        int skillCooldown = 0;
        int skillDuration = 0;
        boolean skillActive = false;

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

        public void update(int[][] map, List<Bomb> bombs, List<Item> items) {
            if (!alive)
                return;

            if (skillCooldown > 0)
                skillCooldown--;
            if (skillActive) {
                skillDuration--;
                if (skillDuration <= 0) {
                    skillActive = false;
                    // Reset stats if needed (balanced type)
                    if (characterType == CharacterType.BALANCED) {
                        speed /= 1.3;
                        bombRange -= 1;
                    } else if (characterType == CharacterType.POWER) {
                        bombRange -= 2;
                    } else if (characterType == CharacterType.BOMBER) {
                        maxBombs -= 2;
                    }
                }
            }

            double moveSpeed = skillActive && characterType == CharacterType.SPEED ? speed * 1.5 : speed;
            double newPx = px;
            double newPy = py;

            if (movingUp)
                newPy -= moveSpeed;
            if (movingDown)
                newPy += moveSpeed;
            if (movingLeft)
                newPx -= moveSpeed;
            if (movingRight)
                newPx += moveSpeed;

            if (canMove(newPx, py, map, bombs))
                px = newPx;
            if (canMove(px, newPy, map, bombs))
                py = newPy;

            x = (int) Math.round(px);
            y = (int) Math.round(py);

            for (Item item : items) {
                if (item.x == x && item.y == y) {
                    applyItem(item.type);
                    item.collected = true;
                }
            }
        }

        private void applyItem(ItemType type) {
            switch (type) {
                case BOMB_UP:
                    maxBombs++;
                    break;
                case RANGE_UP:
                    bombRange++;
                    break;
                case SPEED_UP:
                    speed += 0.02;
                    break;
                case SHIELD:
                    hasShield = true;
                    break;
            }
        }

        private boolean canMove(double newX, double newY, int[][] map, List<Bomb> bombs) {
            int gx = (int) Math.round(newX);
            int gy = (int) Math.round(newY);

            if (gx < 0 || gx >= 15 || gy < 0 || gy >= 13)
                return false;
            if (map[gy][gx] != 0)
                return false;

            for (Bomb b : bombs) {
                if (b.x == gx && b.y == gy && (this.x != gx || this.y != gy))
                    return false;
            }
            return true;
        }

        public void keyPressed(int key, List<Bomb> bombs) {
            if (key == upKey)
                movingUp = true;
            if (key == downKey)
                movingDown = true;
            if (key == leftKey)
                movingLeft = true;
            if (key == rightKey)
                movingRight = true;
            if (key == bombKey && currentBombs < maxBombs) {
                bombs.add(new Bomb(x, y, bombRange, this));
                currentBombs++;
            }
            if (key == skillKey)
                useSkill();
        }

        public void keyReleased(int key) {
            if (key == upKey)
                movingUp = false;
            if (key == downKey)
                movingDown = false;
            if (key == leftKey)
                movingLeft = false;
            if (key == rightKey)
                movingRight = false;
        }

        private void useSkill() {
            if (skillCooldown > 0)
                return;
            skillCooldown = 300; // 5 seconds
            skillDuration = 180; // 3 seconds
            skillActive = true;

            switch (characterType) {
                case POWER:
                    bombRange += 2;
                    break;
                case BOMBER:
                    maxBombs += 2;
                    break;
                case BALANCED:
                    speed *= 1.3;
                    bombRange += 1;
                    break;
                case SPEED:
                    break; // Handled in update
            }
        }

        public void draw(Graphics g) {
            g.setColor(characterType.color);
            g.fillOval(x * 40 + 5, y * 40 + 5, 30, 30);
            if (hasShield) {
                g.setColor(new Color(0, 255, 255, 100));
                g.drawOval(x * 40 + 2, y * 40 + 2, 36, 36);
            }
            // Eyes
            g.setColor(Color.WHITE);
            g.fillOval(x * 40 + 12, y * 40 + 12, 6, 6);
            g.fillOval(x * 40 + 22, y * 40 + 12, 6, 6);
            g.setColor(Color.BLACK);
            g.fillOval(x * 40 + 14, y * 40 + 14, 3, 3);
            g.fillOval(x * 40 + 24, y * 40 + 14, 3, 3);
        }
    }

    static class Bomb {
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
            int red = (int) (255 * (1.0 - timer / 120.0));
            g.setColor(new Color(255, red, 0));
            g.fillOval(x * 40 + 12, y * 40 + 12, 16, 16);
        }
    }

    static class Explosion {
        int x, y, timer = 30;

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
            g.setColor(new Color(255, 69, 0, 200));
            g.fillRect(x * 40, y * 40, 40, 40);
        }
    }

    static class Item {
        int x, y;
        ItemType type;
        boolean collected = false;
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
            return lifeTime <= 0 || collected;
        }

        public void draw(Graphics g) {
            if (collected)
                return;
            g.setColor(type.color);
            g.fillRect(x * 40 + 10, y * 40 + 10, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRect(x * 40 + 10, y * 40 + 10, 20, 20);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(type.name, x * 40 + 5, y * 40 + 40);
        }
    }
}

