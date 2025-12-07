import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CrazyArcade_UI extends JFrame {
    private MenuPanel menuPanel;
    private GamePanel gamePanel;

    public CrazyArcade_UI() {
        setTitle("Crazy Arcade - UI System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        showMenu();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showMenu() {
        getContentPane().removeAll();
        menuPanel = new MenuPanel(this);
        add(menuPanel);
        revalidate();
        repaint();
    }

    public void startGame(int timeLimit) {
        getContentPane().removeAll();
        gamePanel = new GamePanel(this, timeLimit);
        add(gamePanel);
        revalidate();
        repaint();
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrazyArcade_UI());
    }
}

// ============== 메뉴 화면 ==============
class MenuPanel extends JPanel {
    private static final int PANEL_WIDTH = 600;
    private static final int PANEL_HEIGHT = 520;

    public MenuPanel(CrazyArcade_UI game) {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(30, 30, 50));
        setLayout(null);

        // 타이틀
        JLabel titleLabel = new JLabel("CRAZY ARCADE");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 50));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBounds(100, 50, 500, 60);
        add(titleLabel);

        // 부제목
        JLabel subLabel = new JLabel("폭탄을 설치하여 상대를 이기세요!");
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        subLabel.setForeground(Color.WHITE);
        subLabel.setBounds(150, 120, 400, 30);
        add(subLabel);

        // 시간 제한 선택 버튼
        JButton btn1 = createButton("1분 게임", 150, 200);
        btn1.addActionListener(e -> game.startGame(60));
        add(btn1);

        JButton btn2 = createButton("3분 게임", 150, 270);
        btn2.addActionListener(e -> game.startGame(180));
        add(btn2);

        JButton btn3 = createButton("무제한", 150, 340);
        btn3.addActionListener(e -> game.startGame(0));
        add(btn3);

        // 조작법 안내
        JLabel controlLabel = new JLabel("<html>조작법:<br>플레이어1: WASD + Space<br>플레이어2: 방향키 + Enter</html>");
        controlLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        controlLabel.setForeground(Color.LIGHT_GRAY);
        controlLabel.setBounds(200, 420, 300, 70);
        add(controlLabel);
    }

    private JButton createButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 300, 50);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(100, 160, 210));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(70, 130, 180));
            }
        });

        return btn;
    }
}

// ============== 게임 패널 ==============
class GamePanel extends JPanel implements Runnable {
    private static final int TILE_SIZE = 40;
    private static final int MAP_WIDTH = 15;
    private static final int MAP_HEIGHT = 13;
    private static final int PANEL_WIDTH = TILE_SIZE * MAP_WIDTH;
    private static final int PANEL_HEIGHT = TILE_SIZE * MAP_HEIGHT + 80; // UI 공간

    private Thread gameThread;
    private Player player1, player2;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private int[][] map;
    private CrazyArcade_UI game;

    private int timeLimit; // 0이면 무제한
    private int remainingTime;
    private boolean isPaused = false;
    private boolean gameOver = false;

    // 점수 시스템
    private int player1Score = 0;
    private int player2Score = 0;

    public GamePanel(CrazyArcade_UI game, int timeLimit) {
        this.game = game;
        this.timeLimit = timeLimit;
        this.remainingTime = timeLimit * 60; // 초를 프레임으로 변환

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
                if (!isPaused && !gameOver) {
                    update();
                }
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        // 시간 감소
        if (timeLimit > 0 && remainingTime > 0) {
            remainingTime--;
            if (remainingTime == 0) {
                gameOver = true;
            }
        }

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
            if (exp.hits(player1.x, player1.y)) {
                player1.alive = false;
                player2Score += 100; // 점수 증가
                gameOver = true;
            }
            if (exp.hits(player2.x, player2.y)) {
                player2.alive = false;
                player1Score += 100; // 점수 증가
                gameOver = true;
            }
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
                    bomb.owner.blocksDestroyed++; // 파괴한 블록 수 증가
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
                int y = i * TILE_SIZE + 80; // UI 공간만큼 아래로

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

        // 게임 객체 그리기 (y좌표에 80 더하기)
        for (Explosion exp : explosions) {
            exp.draw(g, 80);
        }

        for (Bomb bomb : bombs) {
            bomb.draw(g, 80);
        }

        if (player1.alive)
            player1.draw(g, 80);
        if (player2.alive)
            player2.draw(g, 80);

        // TODO: 상단 UI를 더 예쁘게 꾸미세요!
        drawTopUI(g);

        // 일시정지 화면
        if (isPaused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 50));
            String pauseText = "일시정지";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(pauseText, (PANEL_WIDTH - fm.stringWidth(pauseText)) / 2, PANEL_HEIGHT / 2);

            g.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
            String resumeText = "P - 계속하기 | ESC - 메뉴";
            g.drawString(resumeText, (PANEL_WIDTH - g.getFontMetrics().stringWidth(resumeText)) / 2,
                    PANEL_HEIGHT / 2 + 50);
        }

        // 게임 오버 화면
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            g.setColor(Color.YELLOW);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 45));
            String resultText;
            if (!player1.alive && !player2.alive) {
                resultText = "무승부!";
            } else if (remainingTime == 0) {
                resultText = "시간 종료!";
            } else {
                resultText = !player1.alive ? "빨강 승리!" : "파랑 승리!";
            }
            FontMetrics fm = g.getFontMetrics();
            g.drawString(resultText, (PANEL_WIDTH - fm.stringWidth(resultText)) / 2, PANEL_HEIGHT / 2 - 50);

            // 최종 점수
            g.setColor(Color.WHITE);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 25));
            String scoreText = String.format("파랑 %d : %d 빨강", player1Score + player1.blocksDestroyed * 10,
                    player2Score + player2.blocksDestroyed * 10);
            g.drawString(scoreText, (PANEL_WIDTH - g.getFontMetrics().stringWidth(scoreText)) / 2,
                    PANEL_HEIGHT / 2 + 10);

            g.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            String infoText = "R - 다시하기 | ESC - 메뉴";
            g.drawString(infoText, (PANEL_WIDTH - g.getFontMetrics().stringWidth(infoText)) / 2, PANEL_HEIGHT / 2 + 60);
        }
    }

    private void drawTopUI(Graphics g) {
        // 배경
        g.setColor(new Color(40, 40, 60));
        g.fillRect(0, 0, PANEL_WIDTH, 80);

        // 플레이어 1 정보
        g.setColor(player1.color);
        g.fillRoundRect(10, 10, 60, 60, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        g.drawString("P1", 30, 45);

        g.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        g.drawString("점수: " + (player1Score + player1.blocksDestroyed * 10), 80, 30);
        g.drawString("블록: " + player1.blocksDestroyed, 80, 50);

        // 중앙 타이머
        g.setColor(Color.YELLOW);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        String timeText = timeLimit == 0 ? "∞"
                : String.format("%d:%02d", remainingTime / 3600, (remainingTime / 60) % 60);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(timeText, (PANEL_WIDTH - fm.stringWidth(timeText)) / 2, 45);

        // 플레이어 2 정보
        g.setColor(player2.color);
        g.fillRoundRect(PANEL_WIDTH - 70, 10, 60, 60, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        g.drawString("P2", PANEL_WIDTH - 50, 45);

        g.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        fm = g.getFontMetrics();
        String p2Score = "점수: " + (player2Score + player2.blocksDestroyed * 10);
        g.drawString(p2Score, PANEL_WIDTH - 80 - fm.stringWidth(p2Score), 30);
        String p2Blocks = "블록: " + player2.blocksDestroyed;
        g.drawString(p2Blocks, PANEL_WIDTH - 80 - fm.stringWidth(p2Blocks), 50);
    }

    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            // 일시정지 토글
            if (key == KeyEvent.VK_P) {
                isPaused = !isPaused;
                return;
            }

            // 메뉴로 돌아가기
            if (key == KeyEvent.VK_ESCAPE) {
                game.showMenu();
                return;
            }

            // 게임 재시작
            if (key == KeyEvent.VK_R && gameOver) {
                gameOver = false;
                player1Score = 0;
                player2Score = 0;
                remainingTime = timeLimit * 60;
                initGame();
                return;
            }

            if (isPaused || gameOver)
                return;

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
    int blocksDestroyed = 0; // 파괴한 블록 수

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

    public void draw(Graphics g, int offsetY) {
        g.setColor(color);
        g.fillOval(x * 40 + 5, y * 40 + 5 + offsetY, 30, 30);
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

    public void draw(Graphics g, int offsetY) {
        g.setColor(Color.BLACK);
        g.fillOval(x * 40 + 8, y * 40 + 8 + offsetY, 24, 24);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(String.valueOf(timer / 60 + 1), x * 40 + 15, y * 40 + 25 + offsetY);
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

    public void draw(Graphics g, int offsetY) {
        g.setColor(new Color(255, 165, 0, 200));
        g.fillRect(x * 40, y * 40 + offsetY, 40, 40);
    }
}