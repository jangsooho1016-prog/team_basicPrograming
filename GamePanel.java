import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import java.awt.image.ImageProducer;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;


class Explosion {
    public enum ExplosionType {
        CENTER, UP, DOWN, LEFT, RIGHT
    }
    
    private final int x, y;
    private final long startTime;
    private final ExplosionType type;
    private boolean active = true;
    private static final int FRAME_DURATION_MS = 100;
    private static final int FRAME_COUNT = 5; 

    public Explosion(int x, int y, long startTime, ExplosionType type) {
        this.x = x;
        this.y = y;
        this.startTime = startTime;
        this.type = type;
    }
    
    public void update(long currentTime) {
        if (currentTime - startTime >= FRAME_COUNT * FRAME_DURATION_MS) {
            this.active = false;
        }
    }
    
    public int getCurrentFrameIndex(long currentTime) {
        if (!active) return FRAME_COUNT - 1; 
        long elapsedTime = currentTime - startTime;
        int frameIndex = (int)(elapsedTime / FRAME_DURATION_MS);
        return Math.min(frameIndex, FRAME_COUNT - 1);
    }

    public boolean isActive() { return active; }
    public int getX() { return x; }
    public int getY() { return y; }
    public ExplosionType getType() { return type; } 
}

class WaterBalloon {
    private final int x, y, range;
    private final long explodeTime;
    private static final int ANIMATION_DURATION_MS = 1000;
    private long startTime;

    public WaterBalloon(int x, int y, long explodeTime, int range) {
        this.x = x;
        this.y = y;
        this.explodeTime = explodeTime;
        this.range = range;
        this.startTime = System.currentTimeMillis();
    }
    
    public void update(long currentTime) {}
    
    public double getScaleFactor() {
        long remaining = explodeTime - System.currentTimeMillis();
        double scale = 1.0;
        if (remaining < 500) { 
            scale = 0.5 + (remaining / 500.0) * 0.5;
        }
        return Math.max(0.5, scale);
    }
    
    public int getCurrentFrameIndex() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        return (int) (elapsedTime / (ANIMATION_DURATION_MS / 7)); 
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public long getExplodeTime() { return explodeTime; }
    public int getRange() { return range; }
}


public class GamePanel extends JPanel implements KeyListener {

    private final int TILE_SIZE = 40;       
    private final int MAP_WIDTH = 40;       
    private final int MAP_HEIGHT = 30;      
    private final int BALLOON_DELAY_MS = 2500;
    private final int GAME_TICK_MS = 16;    
    
    private final int BALLOON_DRAW_SIZE = TILE_SIZE; 
    private final int BALLOON_OFFSET = (BALLOON_DRAW_SIZE - TILE_SIZE) / 2; 
    
    private final int BALLOON_FRAME_WIDTH = TILE_SIZE; 
    private final int BALLOON_FRAME_COUNT = 7;  
    
    private final int EXP_FRAME_COUNT = 5;  
    private final int EXP_FRAME_WIDTH = TILE_SIZE;  
    
    private int[][] mapData = new int[MAP_HEIGHT][MAP_WIDTH]; 

    private Point playerPos;    
    private List<WaterBalloon> balloons;
    private List<Explosion> explosions;
    
    private int playerRange = 1;    
    private boolean isPlayerAlive = true;   
    private boolean isTrapped = false;  

    private WaterBalloon activePlayerBalloon = null; 

    private BufferedImage backBuffer;
    private Graphics2D backGraphics;

    private BufferedImage waterBalloonSpriteSheet; 
    private BufferedImage trappedImage;   
    
    // 5개의 개별 폭발 이미지 필드
    private BufferedImage explosionCenterImage;
    private BufferedImage explosionUpImage;
    private BufferedImage explosionDownImage;
    private BufferedImage explosionLeftImage;
    private BufferedImage explosionRightImage;


    public GamePanel() {
        setPreferredSize(new Dimension(MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE));
        
        addKeyListener(this);
        setFocusable(true);
        
        setBackground(Color.DARK_GRAY);
        
        playerPos = new Point(1, 1);
        balloons = new ArrayList<>();
        explosions = new ArrayList<>();
        
        for(int i=0; i<MAP_HEIGHT; i++) {
            for(int j=0; j<MAP_WIDTH; j++) {
                mapData[i][j] = 0;
            }
        }

        int panelWidth = MAP_WIDTH * TILE_SIZE;
        int panelHeight = MAP_HEIGHT * TILE_SIZE;
        backBuffer = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
        backGraphics = backBuffer.createGraphics();

        backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        try {
            waterBalloonSpriteSheet = loadAndTransformImage("/BlueBub.bmp", Color.BLACK);

            explosionCenterImage = loadAndTransformImage("/explosion_center.bmp", Color.BLACK);
            explosionUpImage = loadAndTransformImage("/explosion_up.bmp", Color.BLACK);
            explosionDownImage = loadAndTransformImage("/explosion_down.bmp", Color.BLACK);
            explosionLeftImage = loadAndTransformImage("/explosion_left.bmp", Color.BLACK);
            explosionRightImage = loadAndTransformImage("/explosion_right.bmp", Color.BLACK);

            trappedImage = loadAndTransformImage("/trapped_bubble.png", Color.BLACK);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("이미지 파일을 로드할 수 없습니다. Classpath를 확인하세요.");
        }
    }
    
    private BufferedImage loadAndTransformImage(String path, Color colorToMakeTransparent) throws IOException {
        BufferedImage image = ImageIO.read(getClass().getResourceAsStream(path));
        if (image == null) {
            System.err.println(path + " 로드 실패");
            return null;
        }
        
        if (path.toLowerCase().endsWith(".bmp")) {
             BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
             Graphics g = convertedImage.getGraphics();
             g.drawImage(image, 0, 0, null);
             g.dispose();
             image = convertedImage;
        }
        
        return TransformColorToTransparency(image, colorToMakeTransparent);
    }

    public void startGameLoop() {
        Timer timer = new Timer(GAME_TICK_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                repaint();  
            }
        });
        timer.start();
    }

    private void updateGame() {
        long currentTime = System.currentTimeMillis();

        List<WaterBalloon> explodedBalloons = new ArrayList<>();
        for (WaterBalloon balloon : balloons) {
            balloon.update(currentTime);  

            if (currentTime >= balloon.getExplodeTime()) {
                explodedBalloons.add(balloon);
                createExplosion(balloon.getX(), balloon.getY(), balloon.getRange());
                if (balloon == activePlayerBalloon) activePlayerBalloon = null;
            }
        }
        balloons.removeAll(explodedBalloons); 

        List<Explosion> finishedExplosions = new ArrayList<>();
        for (Explosion explosion : explosions) {
            explosion.update(currentTime); 
            if (!explosion.isActive()) finishedExplosions.add(explosion); 
        }
        explosions.removeAll(finishedExplosions); 
        
        if (isPlayerAlive && !isTrapped) { 
            for (Explosion exp : explosions) {
                if (exp.getX() == playerPos.x && exp.getY() == playerPos.y) {
                    isTrapped = true; break;
                }
            }
        }
    }


    private void createExplosion(int centerX, int centerY, int range) {
        long startTime = System.currentTimeMillis();
        
        explosions.add(new Explosion(centerX, centerY, startTime, Explosion.ExplosionType.CENTER));
        mapData[centerY][centerX] = 0; 
        
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; 
        
        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];
            
            Explosion.ExplosionType currentExplosionType;
            if (dy == -1) currentExplosionType = Explosion.ExplosionType.UP;
            else if (dy == 1) currentExplosionType = Explosion.ExplosionType.DOWN;
            else if (dx == -1) currentExplosionType = Explosion.ExplosionType.LEFT;
            else currentExplosionType = Explosion.ExplosionType.RIGHT;

            for (int i = 1; i <= range; i++) {
                int newX = centerX + dx * i;
                int newY = centerY + dy * i;
                
                if (newX < 0 || newX >= MAP_WIDTH || newY < 0 || newY >= MAP_HEIGHT) break; 
                
                explosions.add(new Explosion(newX, newY, startTime, currentExplosionType));
                
                if (isWaterBalloonAt(newX, newY)) break; 
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        drawToBuffer();
        g.drawImage(backBuffer, 0, 0, this);
    }
    
    private void drawToBuffer() {
        backGraphics.setColor(Color.DARK_GRAY);
        backGraphics.fillRect(0, 0, backBuffer.getWidth(), backBuffer.getHeight());
        backGraphics.setColor(new Color(50, 50, 50));
        
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                backGraphics.drawRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        long currentTime = System.currentTimeMillis();
        for (Explosion explosion : explosions) {
            int x = explosion.getX() * TILE_SIZE; 
            int y = explosion.getY() * TILE_SIZE; 
            
            BufferedImage currentImage = null;
            switch (explosion.getType()) {
                case CENTER: currentImage = explosionCenterImage; break;
                case UP: currentImage = explosionUpImage; break;
                case DOWN: currentImage = explosionDownImage; break;
                case LEFT: currentImage = explosionLeftImage; break;
                case RIGHT: currentImage = explosionRightImage; break;
            }

            if (currentImage != null) {
                int frameIndex = explosion.getCurrentFrameIndex(currentTime); 
                int srcX = frameIndex * EXP_FRAME_WIDTH; 

                backGraphics.drawImage(
                    currentImage,
                    x, y, x + TILE_SIZE, y + TILE_SIZE, 
                    srcX, 0, srcX + EXP_FRAME_WIDTH, currentImage.getHeight(),
                    null
                );
            } else {
                backGraphics.setColor(Color.RED);
                backGraphics.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }
        
        for (WaterBalloon balloon : balloons) {
            int x = balloon.getX() * TILE_SIZE; 
            int y = balloon.getY() * TILE_SIZE; 

            double scale = balloon.getScaleFactor(); 
            int scaledDrawSize = (int)(BALLOON_DRAW_SIZE * scale); 
            int scaledOffset = (scaledDrawSize - TILE_SIZE) / 2;
            
            int drawX = x - scaledOffset;
            int drawY = y - scaledOffset;
            
            int frameIndex = balloon.getCurrentFrameIndex() % BALLOON_FRAME_COUNT; 
            int srcX = frameIndex * BALLOON_FRAME_WIDTH; 

            if (waterBalloonSpriteSheet != null) {
                backGraphics.drawImage(
                    waterBalloonSpriteSheet,
                    drawX, drawY, drawX + scaledDrawSize, drawY + scaledDrawSize, 
                    srcX, 0, srcX + BALLOON_FRAME_WIDTH, waterBalloonSpriteSheet.getHeight(),
                    null
                );
            } else {
                backGraphics.setColor(new Color(0, 191, 255)); 
                backGraphics.fillOval(drawX, drawY, scaledDrawSize, scaledDrawSize);
            }
        }

        // 플레이어 및 갇힘 상태 그리기
        if (isPlayerAlive) {
            int px = playerPos.x * TILE_SIZE;
            int py = playerPos.y * TILE_SIZE;

            if (isTrapped) {
                int drawX = px - BALLOON_OFFSET; 
                int drawY = py - BALLOON_OFFSET;
                
                if (trappedImage != null) {
                    backGraphics.drawImage(trappedImage, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                } else {
                    backGraphics.setColor(new Color(173, 216, 230, 200)); 
                    backGraphics.fillOval(drawX, drawY, BALLOON_DRAW_SIZE, BALLOON_DRAW_SIZE);
                    backGraphics.setColor(Color.YELLOW);
                    backGraphics.fillOval(drawX + BALLOON_DRAW_SIZE / 4, drawY + BALLOON_DRAW_SIZE / 4, BALLOON_DRAW_SIZE / 2, BALLOON_DRAW_SIZE / 2);
                }
            } else {
                backGraphics.setColor(Color.YELLOW);
                backGraphics.fillOval(px + TILE_SIZE / 8, py + TILE_SIZE / 8, TILE_SIZE * 3 / 4, TILE_SIZE * 3 / 4);
            }
        } else {
            backGraphics.setColor(Color.RED);
            backGraphics.setFont(new Font("Arial", Font.BOLD, TILE_SIZE));
            backGraphics.drawString("X", playerPos.x * TILE_SIZE + TILE_SIZE/4, playerPos.y * TILE_SIZE + TILE_SIZE * 3/4);
        }
    }
    
    private boolean isWaterBalloonAt(int x, int y) {
        for (WaterBalloon balloon : balloons) {
            if (balloon.getX() == x && balloon.getY() == y) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isPlayerAlive || isTrapped) return; 

        int newX = playerPos.x;
        int newY = playerPos.y;

        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP) newY--;
        else if (keyCode == KeyEvent.VK_DOWN) newY++;
        else if (keyCode == KeyEvent.VK_LEFT) newX--;
        else if (keyCode == KeyEvent.VK_RIGHT) newX++;
        
        if (newX >= 0 && newX < MAP_WIDTH && newY >= 0 && newY < MAP_HEIGHT) {
            if (!isWaterBalloonAt(newX, newY)) {
                playerPos.setLocation(newX, newY);
            }
        }

        if (keyCode == KeyEvent.VK_SPACE) {
            placeWaterBalloon(playerPos.x, playerPos.y);
        }
    }

    private void placeWaterBalloon(int x, int y) {
        if (activePlayerBalloon != null) {
            return;
        }

        for (WaterBalloon balloon : balloons) {
            if (balloon.getX() == x && balloon.getY() == y) {
                return; 
            }
        }
        
        long explodeTime = System.currentTimeMillis() + BALLOON_DELAY_MS;
        WaterBalloon newBalloon = new WaterBalloon(x, y, explodeTime, playerRange);

        balloons.add(newBalloon);   
        activePlayerBalloon = newBalloon;   
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}


    protected BufferedImage TransformColorToTransparency(BufferedImage image, Color c1) {
        final int r1 = c1.getRed();
        final int g1 = c1.getGreen();
        final int b1 = c1.getBlue();
        
        ImageFilter filter = new RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
                int r = ( rgb & 0xFF0000 ) >> 16;
                int g = ( rgb & 0xFF00 ) >> 8;
                int b = ( rgb & 0xFF );
                
                if( r == r1 && g == g1 && b == b1) {
                    return 0x00000000; 
                }
                return rgb;
            }
        };
        
        ImageProducer ip = new FilteredImageSource( image.getSource(), filter );
        Image img = Toolkit.getDefaultToolkit().createImage(ip);
        
        BufferedImage dest = new BufferedImage(img.getWidth(null),
                                              img.getHeight(null), 
                                              BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return dest;
    }
}
