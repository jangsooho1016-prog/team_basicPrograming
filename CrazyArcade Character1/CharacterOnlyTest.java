import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;   // ★ 스프라이트 이미지용
import java.io.File;
import java.io.IOException;           // ★ 예외 처리용
import java.util.ArrayList;           // ★ List 구현체
import java.util.List;                // ★ List 인터페이스
import javax.imageio.ImageIO;         // ★ 이미지 로딩용
import javax.swing.*;                 // ★ 스윙 UI

public class CharacterOnlyTest extends JFrame {

    // ✅ 기본 생성자: 예전처럼 그냥 실행할 때 사용 (디폴트 캐릭터)
    //    1P = BAZZI(파란 배찌), 2P = DIZNI(빨간 디지니)
    public CharacterOnlyTest() {
        this(CharacterType.BAZZI, CharacterType.DIZNI);
    }

    // ✅ 로비에서 선택한 캐릭터 타입을 넘겨받는 생성자
    //    여기로 CharacterType.BAZZI / CharacterType.DIZNI 를 넘겨주면 됨
    public CharacterOnlyTest(CharacterType p1Type, CharacterType p2Type) {
        setTitle("Crazy Arcade 2P Character Test");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        TestPanel panel = new TestPanel(p1Type, p2Type);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        // 단독 테스트용: 1P 배찌(파랑), 2P 디지니(빨강)
        new CharacterOnlyTest();
    }

    // ===================== Map 800 x 600 =====================
    static class TestPanel extends JPanel implements KeyListener, Runnable {

        static final int TILE_SIZE = 40;
        static final int MAP_WIDTH = 20;  // 800px
        static final int MAP_HEIGHT = 15; // 600px

        Player player1, player2;
        Thread gameThread;

        // ✅ 추가: 1P/2P 캐릭터 타입
        CharacterType p1Type;
        CharacterType p2Type;

        // ✅ 기본 생성자: 예전처럼 아무 인자 없이 쓸 때 (디폴트 캐릭터)
        public TestPanel() {
            this(CharacterType.BAZZI, CharacterType.DIZNI);
        }

        // ✅ 새로운 생성자: 1P/2P 타입을 외부에서 받아서 사용
        public TestPanel(CharacterType p1Type, CharacterType p2Type) {
            this.p1Type = p1Type;
            this.p2Type = p2Type;

            setPreferredSize(new Dimension(800, 600)); // 창=맵 동일
            setBackground(Color.WHITE);
            setFocusable(true);
            addKeyListener(this);

            // 🔵 1P — 파란색(Blue) 고정 (WASD)
            //     타입만 바뀌므로, p1Type 이 BAZZI면 파란 배찌, DIZNI면 파란 디지니
            player1 = new Player(
                    1, 1,
                    this.p1Type,
                    PlayerColor.BLUE,                      // ★ 1P는 BLUE 고정
                    KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D
            );

            // 🔴 2P — 빨간색(Red) 고정 (방향키)
            //     p2Type 이 BAZZI면 빨간 배찌, DIZNI면 빨간 디지니
            player2 = new Player(
                    MAP_WIDTH - 2, MAP_HEIGHT - 2,
                    this.p2Type,
                    PlayerColor.RED,                       // ★ 2P는 RED 고정
                    KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT
            );

            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public void run() {
            while (true) {
                player1.update();
                player2.update();
                repaint();
                try { Thread.sleep(10); } catch (Exception ignored) {}
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= MAP_WIDTH; i++)
                g.drawLine(i*TILE_SIZE, 0, i*TILE_SIZE, MAP_HEIGHT*TILE_SIZE);

            for (int j = 0; j <= MAP_HEIGHT; j++)
                g.drawLine(0, j*TILE_SIZE, MAP_WIDTH*TILE_SIZE, j*TILE_SIZE);

            player1.draw(g);
            player2.draw(g);
        }

        public void keyPressed(KeyEvent e){ player1.keyPressed(e); player2.keyPressed(e);}
        public void keyReleased(KeyEvent e){ player1.keyReleased(e); player2.keyReleased(e);}
        public void keyTyped(KeyEvent e){}
    }


    // ================= A+B 적용된 능력치 =================
    enum CharacterType {

        // baseSpeed 2 → 1.5 로 감소
        BAZZI(new Color(255,200,200),"배찌",1.5,1,1,9,7,6),
        DIZNI(new Color(200,200,255),"디지니",1.5,1,1,7,7,10);

        final Color color; final String nameKo;
        final double baseSpeed, maxSpeed;
        final int baseRange, baseMaxBombs, maxRange, maxMaxBombs;

        CharacterType(Color c,String n,double bs,int br,int bb,double ms,int mr,int mb){
            color=c; nameKo=n; baseSpeed=bs; baseRange=br; baseMaxBombs=bb;
            maxSpeed=ms; maxRange=mr; maxMaxBombs=mb;
        }
    }

    // ✅ 플레이어 색상 (블루 / 레드)
    enum PlayerColor {
        BLUE, RED
    }

    // ★ 이동 방향 enum 추가
    enum Direction {
        NONE, UP, DOWN, LEFT, RIGHT
    }

    // =============== 스프라이트 스트립 도우미 클래스 ===============
    // 한 줄에 프레임이 여러 개 붙어있는 스프라이트를 잘라 쓰기 위한 클래스
    static class SpriteStrip {
        BufferedImage sheet;
        int frameWidth;
        int frameHeight;
        int frameCount;

        SpriteStrip(String path, int frameCount) {
            try {
                File f = new File(path);
                System.out.println("[SpriteStrip] 시도 경로 = " + f.getAbsolutePath()
                                   + " / exists = " + f.exists());   // 디버그용

                BufferedImage loaded = ImageIO.read(f);
                System.out.println("[SpriteStrip] 로딩 성공: " + path);

                // ★ 원본 이미지 크기 및 가능한 프레임 수 후보 출력
                int w = loaded.getWidth();
                int h = loaded.getHeight();
                System.out.println("[SpriteStrip] " + path + " width=" + w + ", height=" + h);
                for (int n = 1; n <= 10; n++) {
                    if (w % n == 0) {
                        System.out.println("  - candidate frameCount=" + n
                                + " → frameWidth=" + (w / n));
                    }
                }

                // ★★ 여기서 배경색(마젠타)을 투명하게 변환 ★★
                //    그림판 스포이드로 찍어본 배경색이 (255,0,255) 라는 전제
                this.sheet = removeBackgroundColor(loaded, new Color(255, 0, 255));

            } catch (IOException e) {
                System.err.println("[SpriteStrip] 스프라이트 로딩 실패: " + path);
                e.printStackTrace();
            }

            this.frameCount = frameCount;
            if (sheet != null) {
                this.frameWidth = sheet.getWidth() / frameCount;
                this.frameHeight = sheet.getHeight();
            }
        }

        // ★ 슬라이드의 TransformColorToTransparency 아이디어를 코드로 옮긴 것
        private BufferedImage removeBackgroundColor(BufferedImage img, Color c1) {
            int r1 = c1.getRed();
            int g1 = c1.getGreen();
            int b1 = c1.getBlue();

            int w = img.getWidth();
            int h = img.getHeight();

            // 투명 채널을 가진 새 이미지
            BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = img.getRGB(x, y);

                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8)  & 0xFF;
                    int b =  rgb        & 0xFF;

                    if (r == r1 && g == g1 && b == b1) {
                        // 배경색이면 완전 투명
                        dest.setRGB(x, y, 0x00000000);
                    } else {
                        // 그 외 픽셀은 원래 색 + 알파 255 유지
                        dest.setRGB(x, y, 0xFF000000 | (rgb & 0x00FFFFFF));
                    }
                }
            }
            return dest;
        }

        BufferedImage getFrame(int index) {
            if (sheet == null) return null;
            if (index < 0 || index >= frameCount) return null;
            return sheet.getSubimage(index * frameWidth, 0, frameWidth, frameHeight);
        }

        int getFrameCount() {
            return frameCount;
        }
    }

    // ================= Player (이동계수 0.03, 먼저 누른 키 우선) =================
    static class Player {
        double px, py;
        int tileX, tileY;
        CharacterType type;
        PlayerColor colorVariant;   // 🔴🔵 레드/블루 구분
        double speed;
        int bombRange, maxBombs;

        // 현재 키 눌림 상태
        boolean up, down, left, right;
        final int upK, downK, leftK, rightK;

        // 현재 바라보는 방향 + 움직임 여부
        Direction dir = Direction.DOWN;
        boolean moving = false;

        // 스프라이트 스트립 (방향별)
        SpriteStrip walkUpStrip;
        SpriteStrip walkDownStrip;
        SpriteStrip walkLeftStrip;
        SpriteStrip walkRightStrip;

        // 애니메이션 상태
        int frameIndex = 0;
        int frameDelayMs = 100;     // 프레임 넘기는 속도 (ms)
        long lastFrameTime = 0;

        // 🔥 "먼저 누른 방향키" 순서를 저장하는 리스트
        //   - 0번 인덱스: 가장 먼저 눌려서 아직 안 뗀 키
        //   - 마지막 인덱스: 가장 나중에 눌린 키
        private final List<Integer> keyOrder = new ArrayList<>();

        Player(int tx,int ty,CharacterType t,PlayerColor colorVariant,
               int up,int down,int left,int right){
            px=tileX=tx; py=tileY=ty; type=t;
            this.colorVariant = colorVariant;
            speed=t.baseSpeed; bombRange=t.baseRange; maxBombs=t.baseMaxBombs;
            upK=up; downK=down; leftK=left; rightK=right;

            // 캐릭터 타입에 따라 스프라이트 로딩
            loadSpritesForType();
        }

        // 타입별 + 색상별 스프라이트 시트 로딩
        private void loadSpritesForType() {
            // BLUE → "res/Blue...", RED → "res/Red..."
            String colorPrefix = (colorVariant == PlayerColor.BLUE) ? "res/Blue" : "res/Red";

            if (type == CharacterType.BAZZI) {
                // ✅ 배찌: 프레임 5 그대로
                walkRightStrip = new SpriteStrip(colorPrefix + "Bazzi_Right.bmp", 5);
                walkLeftStrip  = new SpriteStrip(colorPrefix + "Bazzi_Left.bmp", 5);
                walkUpStrip    = new SpriteStrip(colorPrefix + "Bazzi_Up.bmp", 5);
                walkDownStrip  = new SpriteStrip(colorPrefix + "Bazzi_Down.bmp", 5);
            } else if (type == CharacterType.DIZNI) {
                // ✅ 디지니: 프레임 5 그대로
                walkRightStrip = new SpriteStrip(colorPrefix + "Dizni_Right.bmp", 5);
                walkLeftStrip  = new SpriteStrip(colorPrefix + "Dizni_Left.bmp", 5);
                walkUpStrip    = new SpriteStrip(colorPrefix + "Dizni_Up.bmp", 5);
                walkDownStrip  = new SpriteStrip(colorPrefix + "Dizni_Down.bmp", 5);
            }
        }

        void update(){
            // 🔹 keyOrder에서 더 이상 눌려 있지 않은 키는 앞에서부터 정리
            cleanKeyOrder();

            double dx=0, dy=0;

            // 🔥 "가장 먼저 눌린 방향키" 한 개만 보고 이동
            if (!keyOrder.isEmpty()) {
                int firstKey = keyOrder.get(0);

                if (firstKey == upK && up) {
                    dy = -1;
                } else if (firstKey == downK && down) {
                    dy = 1;
                } else if (firstKey == leftK && left) {
                    dx = -1;
                } else if (firstKey == rightK && right) {
                    dx = 1;
                }
            }

            // 방향/움직임 상태 갱신
            if (dx != 0 || dy != 0) {
                moving = true;

                if (dx < 0)      dir = Direction.LEFT;
                else if (dx > 0) dir = Direction.RIGHT;
                else if (dy < 0) dir = Direction.UP;
                else if (dy > 0) dir = Direction.DOWN;
            } else {
                moving = false;
            }

            // 속도 느리게 → 이동계수 0.03
            px += dx * 0.03 * speed;
            py += dy * 0.03 * speed;

            if(px<0)px=0; if(py<0)py=0;
            if(px>TestPanel.MAP_WIDTH-1)px=TestPanel.MAP_WIDTH-1;
            if(py>TestPanel.MAP_HEIGHT-1)py=TestPanel.MAP_HEIGHT-1;

            tileX=(int)Math.round(px);
            tileY=(int)Math.round(py);

            // ★ 애니메이션 프레임 갱신
            updateAnimation();
        }

        // 🔹 현재 keyOrder의 맨 앞 키가 실제로 눌려 있는지 검사, 아니면 제거
        private void cleanKeyOrder() {
            while (!keyOrder.isEmpty()) {
                int first = keyOrder.get(0);
                boolean stillHeld =
                        (first == upK && up) ||
                        (first == downK && down) ||
                        (first == leftK && left) ||
                        (first == rightK && right);

                if (!stillHeld) {
                    keyOrder.remove(0);
                } else {
                    break;
                }
            }
        }

        // ★ 애니메이션 업데이트
        void updateAnimation() {
            SpriteStrip strip = getCurrentStrip();
            if (strip == null) return;

            if (!moving) {
                frameIndex = 0; // 서 있을 때는 첫 프레임
                return;
            }

            long now = System.currentTimeMillis();
            if (now - lastFrameTime >= frameDelayMs) {
                lastFrameTime = now;
                frameIndex = (frameIndex + 1) % strip.getFrameCount();
            }
        }

        // ★ 현재 방향에 맞는 스프라이트 스트립 선택
        SpriteStrip getCurrentStrip() {
            switch (dir) {
                case UP:    return walkUpStrip;
                case DOWN:  return walkDownStrip;
                case LEFT:  return walkLeftStrip;
                case RIGHT: return walkRightStrip;
                default:    return walkDownStrip;
            }
        }

        void draw(Graphics g){
            int s = TestPanel.TILE_SIZE;
            int drawX = tileX*s;
            int drawY = tileY*s;

            SpriteStrip strip = getCurrentStrip();
            BufferedImage frame = (strip != null) ? strip.getFrame(frameIndex) : null;

            if (frame != null) {
                // ★ 스프라이트 이미지 그리기 (타일 크기에 맞게 스케일)
                g.drawImage(frame, drawX, drawY, s, s, null);
            } else {
                // ★ 스프라이트 로딩 실패 시 기존 동그라미 방식 유지
                g.setColor(type.color);
                g.fillOval(drawX+8, drawY+8, s-16, s-16);
                g.setColor(Color.WHITE);
                g.fillOval(drawX+14, drawY+12, 6, 6);
            }
        }

        // 🔹 이동키를 순서 리스트에 추가
        private void addMoveKey(int c) {
            if (c==upK || c==downK || c==leftK || c==rightK) {
                if (!keyOrder.contains(c)) {
                    keyOrder.add(c);
                }
            }
        }

        // 🔹 이동키를 순서 리스트에서 제거
        private void removeMoveKey(int c) {
            keyOrder.remove(Integer.valueOf(c));
        }

        void keyPressed(KeyEvent e){
            int c=e.getKeyCode();
            if(c==upK){   up=true;    addMoveKey(c); }
            if(c==downK){ down=true;  addMoveKey(c); }
            if(c==leftK){ left=true;  addMoveKey(c); }
            if(c==rightK){right=true; addMoveKey(c); }
        }

        void keyReleased(KeyEvent e){
            int c=e.getKeyCode();
            if(c==upK){   up=false;   removeMoveKey(c); }
            if(c==downK){ down=false; removeMoveKey(c); }
            if(c==leftK){ left=false; removeMoveKey(c); }
            if(c==rightK){right=false;removeMoveKey(c); }
        }
    }
}