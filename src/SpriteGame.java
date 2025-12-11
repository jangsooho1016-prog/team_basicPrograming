import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class SpriteGame extends JPanel implements KeyListener {
    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 520;
    private static final int SPRITE_DISPLAY_WIDTH = 44;
    private static final int SPRITE_DISPLAY_HEIGHT = 62;
    private static final int COLLISION_SIZE = 40;
    private static final int GRID_COLS = 8;
    private static final int GRID_ROWS = 4;
    
    private BufferedImage spriteSheet;
    private BufferedImage[][] sprites;
    private int playerX = 0;
    private int playerY = 0;
    private int moveSpeed = 5;
    
    // 마지막으로 누른 키 하나만 추적
    private Integer currentKey = null;
    private Timer gameTimer;
    
    // 현재 플레이어 스프라이트 인덱스
    private int currentSpriteRow = 3;
    private int currentSpriteCol = 0;  // 시작은 [3][0]
    
    // 애니메이션 프레임 카운터
    private int animationSpeed = 6;  // 값이 클수록 애니메이션이 느림
    private int frameCounter = 0;
    
    public SpriteGame() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        
        loadSprites();
        startGameLoop();
    }
    
    private void loadSprites() {
        try {
            spriteSheet = ImageIO.read(new File("res/BlueBazzi.png"));
            sprites = new BufferedImage[GRID_ROWS][GRID_COLS];
            
            // 이미지를 8x4 그리드로 분할
            int spriteWidth = spriteSheet.getWidth() / GRID_COLS;
            int spriteHeight = spriteSheet.getHeight() / GRID_ROWS;
            
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    sprites[row][col] = spriteSheet.getSubimage(
                        col * spriteWidth,
                        row * spriteHeight,
                        spriteWidth,
                        spriteHeight
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("스프라이트 이미지를 로드할 수 없습니다.");
        }
    }
    
    private void startGameLoop() {
        // 60 FPS 게임 루프 (약 16ms마다 업데이트)
        gameTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePlayerPosition();
                updateAnimation();
                repaint();
            }
        });
        gameTimer.start();
    }
    
    private void updatePlayerPosition() {
        if (currentKey == null) {
            return;
        }
        
        int newX = playerX;
        int newY = playerY;
        
        // 현재 활성화된 키 하나만 적용
        switch (currentKey) {
            case KeyEvent.VK_W:
                newY -= moveSpeed;
                break;
            case KeyEvent.VK_S:
                newY += moveSpeed;
                break;
            case KeyEvent.VK_A:
                newX -= moveSpeed;
                break;
            case KeyEvent.VK_D:
                newX += moveSpeed;
                break;
        }
        
        // 화면 경계 체크
        if (newX >= 0 && newX <= SCREEN_WIDTH - SPRITE_DISPLAY_WIDTH) {
            playerX = newX;
        }
        if (newY >= 0 && newY <= SCREEN_HEIGHT - SPRITE_DISPLAY_HEIGHT) {
            playerY = newY;
        }
    }
    
    private void updateAnimation() {
        // 움직이고 있을 때만 애니메이션
        if (currentKey != null) {
            frameCounter++;
            if (frameCounter >= animationSpeed) {
                frameCounter = 0;
                // 0~7로 순환
                currentSpriteCol = (currentSpriteCol + 1) % 8;
            }
        }
    }
    
    private void updateSpriteDirection(int key) {
        // 키에 따라 스프라이트 행 변경
        switch (key) {
            case KeyEvent.VK_A:
                currentSpriteRow = 0;  // [0][0~7]
                break;
            case KeyEvent.VK_W:
                currentSpriteRow = 1;  // [1][0~7]
                break;
            case KeyEvent.VK_D:
                currentSpriteRow = 2;  // [2][0~7]
                break;
            case KeyEvent.VK_S:
                currentSpriteRow = 3;  // [3][0~7]
                break;
        }
        // 방향 전환 시 애니메이션 프레임 리셋
        currentSpriteCol = 0;
        frameCounter = 0;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 그리드에 스프라이트 배치
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (sprites[row][col] != null) {
                    int x = col * SPRITE_DISPLAY_WIDTH + 50;
                    int y = row * SPRITE_DISPLAY_HEIGHT + 50;
                    g2d.drawImage(sprites[row][col], x, y, 
                                SPRITE_DISPLAY_WIDTH, SPRITE_DISPLAY_HEIGHT, null);
                }
            }
        }
        
        // 플레이어 스프라이트 그리기 (현재 방향에 따른 스프라이트)
        if (sprites[currentSpriteRow][currentSpriteCol] != null) {
            g2d.drawImage(sprites[currentSpriteRow][currentSpriteCol], playerX, playerY-10, 
                        SPRITE_DISPLAY_WIDTH, SPRITE_DISPLAY_HEIGHT, null);
            
            // 충돌 박스 표시 (디버깅용)
            g2d.setColor(new Color(255, 0, 0, 100));
            int collisionX = playerX + (SPRITE_DISPLAY_WIDTH - COLLISION_SIZE) / 2;
            int collisionY = playerY + (SPRITE_DISPLAY_HEIGHT - COLLISION_SIZE) / 2;
            g2d.drawRect(collisionX, collisionY, COLLISION_SIZE, COLLISION_SIZE);
        }
        
        // 정보 표시
        g2d.setColor(Color.BLACK);
        g2d.drawString("WASD로 이동 | 위치: (" + playerX + ", " + playerY + ")", 10, 20);
        g2d.drawString("스프라이트: [" + currentSpriteRow + "][" + currentSpriteCol + "]", 10, 35);
        g2d.drawString("충돌 판정: " + COLLISION_SIZE + "x" + COLLISION_SIZE, 10, 50);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // WASD 키만 허용하고 마지막에 누른 키로 업데이트
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_A || 
            key == KeyEvent.VK_S || key == KeyEvent.VK_D) {
            // 같은 키를 계속 누르고 있는 경우 방향 업데이트 하지 않음
            if (currentKey == null || currentKey != key) {
                currentKey = key;
                updateSpriteDirection(key);  // 스프라이트 방향 업데이트
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        // 떼어진 키가 현재 활성화된 키와 같으면 null로 설정
        // 스프라이트는 마지막 프레임 유지
        if (currentKey != null && currentKey == key) {
            currentKey = null;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // 충돌 판정용 메서드
    public Rectangle getCollisionBox(int x, int y) {
        int collisionX = x + (SPRITE_DISPLAY_WIDTH - COLLISION_SIZE) / 2;
        int collisionY = y + (SPRITE_DISPLAY_HEIGHT - COLLISION_SIZE) / 2;
        return new Rectangle(collisionX, collisionY, COLLISION_SIZE, COLLISION_SIZE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("스프라이트 게임");
            SpriteGame game = new SpriteGame();
            
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
