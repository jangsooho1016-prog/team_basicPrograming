import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Crazy Arcade 스타일 플레이어
 * - 타일 단위(tileX, tileY)로 이동
 * - 눈알 히트박스를 이용해 충돌 처리
 * - 방향키 입력을 direction 변수에 반영하고
 *   update()에서만 direction에 따라 움직이도록 설계
 * - 여러 방향키가 동시에 눌렸을 때
 *   -> "가장 먼저 눌린 방향키"만 적용 (크아 스타일)
 * - 스프라이트 애니메이션(Left / Right / Up / Down) 적용
 *
 * ⚠️ SpriteStrip 클래스가 같은 프로젝트에 있어야 함.
 */
public class Player {

    public static final int TILE_SIZE = 40;

    // 이동 방향
    public enum Direction {
        NONE, UP, DOWN, LEFT, RIGHT
    }

    // 캐릭터 타입 (능력치 고정값 모음)
    public enum CharacterType {
        BAZZI(new Color(255, 200, 200), "배찌",
                4.0, 1, 1,
                9.0, 7, 6),
        DAO(new Color(200, 200, 255), "디지니",
                4.0, 1, 1,
                7.0, 7, 10);

        public final Color color;
        public final String nameKo;

        public final double baseSpeed;
        public final int baseRange;
        public final int baseMaxBombs;

        public final double maxSpeed;
        public final int maxRange;
        public final int maxMaxBombs;

        CharacterType(Color color,
            String nameKo,
            double baseSpeed,
            int baseRange,
            int baseMaxBombs,
            double maxSpeed,
            int maxRange,
            int maxMaxBombs) {
            this.color = color;
            this.nameKo = nameKo;
            this.baseSpeed = baseSpeed;
            this.baseRange = baseRange;
            this.baseMaxBombs = baseMaxBombs;
            this.maxSpeed = maxSpeed;
            this.maxRange = maxRange;
            this.maxMaxBombs = maxMaxBombs;
        }
    }

    // ==== 위치 & 상태 ====
    private double tileX;   // 타일 기준 X 위치
    private double tileY;   // 타일 기준 Y 위치

    private double speed;
    private int bombRange;
    private int maxBombs;
    private int activeBombs;

    private boolean alive;

    private HitBox hitBox;
    private final CharacterType type;

    // ==== 키 설정 ====
    private final int upKey;
    private final int downKey;
    private final int leftKey;
    private final int rightKey;
    private final int bombKey;

    // ==== 방향키 눌림 상태 ====
    private boolean upHeld;
    private boolean downHeld;
    private boolean leftHeld;
    private boolean rightHeld;

    // 🔥 먼저 눌린 방향키 순서를 기록하는 리스트
    //   - 0번 인덱스: 가장 먼저 눌려서 아직 떼지 않은 키
    //   - 마지막 인덱스: 가장 나중에 눌린 키
    private final List<Integer> keyOrder = new ArrayList<>();

    // ==== 현재 이동 방향 ====
    private Direction direction = Direction.NONE;

    // ==== 이동 템포 조절용 쿨타임 ====
    private long moveCooldownMs;      // 캐릭터 타입에 따라 설정
    private long lastMoveTime = 0;    // 마지막 이동 시간(ms)

    // ==== 스프라이트 애니메이션용 필드 ====
    // 방향별 스프라이트 스트립
    private SpriteStrip walkUpStrip;
    private SpriteStrip walkDownStrip;
    private SpriteStrip walkLeftStrip;
    private SpriteStrip walkRightStrip;

    // 애니메이션 상태
    private int frameIndex = 0;
    private long lastAnimTime = 0;
    private long animIntervalMs = 80; // 0.08초마다 프레임 변경

    public Player(CharacterType type,
        double startTileX,
        double startTileY,
        int upKey,
        int downKey,
        int leftKey,
        int rightKey,
        int bombKey) {

        this.type = type;
        this.tileX = startTileX;
        this.tileY = startTileY;

        this.speed = type.baseSpeed;
        this.bombRange = type.baseRange;
        this.maxBombs = type.baseMaxBombs;
        this.activeBombs = 0;
        this.alive = true;

        this.upKey = upKey;
        this.downKey = downKey;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.bombKey = bombKey;

        this.upHeld = false;
        this.downHeld = false;
        this.leftHeld = false;
        this.rightHeld = false;

        // 배찌/디지니 속도
        this.moveCooldownMs = 250;   // 0.25초마다 1칸

        // 스프라이트 로딩
        this.loadSprites();

        this.updateHitBox();
    }

    /**
     * 캐릭터 타입에 따라 사용할 스프라이트 시트를 로딩
     * 파일 경로는 실제 저장 위치에 맞게 수정해도 됨.
     * (예: "img/BlueBazzi_Right.bmp" 처럼.)
     */
    private void loadSprites() {
        if (type == CharacterType.BAZZI) {
            // 배찌용 4방향 스프라이트
            walkRightStrip = new SpriteStrip("BlueBazzi_Right.bmp", 5);
            walkLeftStrip  = new SpriteStrip("BlueBazzi_Left.bmp", 5);
            walkUpStrip    = new SpriteStrip("BlueBazzi_Up.bmp", 5);
            walkDownStrip  = new SpriteStrip("BlueBazzi_Down.bmp", 5);

        } else if (type == CharacterType.DAO) {
            // 디지니(DAO)용 4방향 스프라이트
            walkRightStrip = new SpriteStrip("BlueDizni_Right.bmp", 5);
            walkLeftStrip  = new SpriteStrip("BlueDizni_Left.bmp", 5);
            walkUpStrip    = new SpriteStrip("BlueDizni_Up.bmp", 5);
            walkDownStrip  = new SpriteStrip("BlueDizni_Down.bmp", 5);

            // 만약 아직 BlueDizni_*.bmp 를 안 만들어놨으면
            // 임시로 배찌 스프라이트 재사용해도 됨:
            // walkRightStrip = new SpriteStrip("BlueBazzi_Right.bmp", 5);
            // ...
        }
    }

    // 매 프레임 호출
    public void update(int[][] map, List<Bomb> bombs) {
        if (!this.alive) {
            return;
        }

        long now = System.currentTimeMillis();

        // === 1) 이동 처리 ===
        if (now - this.lastMoveTime >= this.moveCooldownMs) {

            int deltaTileX = 0;
            int deltaTileY = 0;

            switch (this.direction) {
                case LEFT -> deltaTileX = -1;
                case RIGHT -> deltaTileX = 1;
                case UP -> deltaTileY = -1;
                case DOWN -> deltaTileY = 1;
                case NONE -> {
                    // 이동 없음
                }
            }

            if (deltaTileX != 0 || deltaTileY != 0) {
                this.moveByTile(deltaTileX, deltaTileY, map, bombs);
                this.lastMoveTime = now;
            }
        }

        // === 2) 애니메이션 프레임 업데이트 ===
        boolean isMoving = (this.direction != Direction.NONE);
        this.updateAnimation(now, isMoving);

        this.updateHitBox();
    }

    // 애니메이션 진행
    private void updateAnimation(long now, boolean moving) {
        SpriteStrip strip = getCurrentStrip();
        if (strip == null) {
            return;
        }

        if (!moving) {
            // 멈춰 있으면 첫 프레임
            frameIndex = 0;
            return;
        }

        if (now - lastAnimTime >= animIntervalMs) {
            lastAnimTime = now;
            frameIndex++;
            int maxFrames = strip.getFrameCount();
            frameIndex %= maxFrames; // 0 ~ maxFrames-1
        }
    }

    // 현재 방향에 맞는 스프라이트 스트립
    private SpriteStrip getCurrentStrip() {
        switch (this.direction) {
            case UP:
                return walkUpStrip;
            case DOWN:
                return walkDownStrip;
            case LEFT:
                return walkLeftStrip;
            case RIGHT:
                return walkRightStrip;
            case NONE:
            default:
                return walkDownStrip; // 서 있을 때는 아래 보는 포즈
        }
    }

    // 타일 기준 1칸 이동
    private void moveByTile(int deltaTileX, int deltaTileY,
                            int[][] map, List<Bomb> bombs) {

        double nextTileX = this.tileX + deltaTileX;
        double nextTileY = this.tileY + deltaTileY;

        if (this.canMove(nextTileX, nextTileY, map, bombs)) {
            this.tileX = nextTileX;
            this.tileY = nextTileY;
        }
    }

    // 이동 가능한지 체크 (눈알 히트박스 기준)
    private boolean canMove(double nextTileX, double nextTileY,
                            int[][] map, List<Bomb> bombs) {

        HitBox nextHitBox = HitBox.createCharacterEyeHitBox(
                nextTileX, nextTileY, TILE_SIZE
        );

        int gridX = (int) Math.round(nextTileX);
        int gridY = (int) Math.round(nextTileY);

        // 맵 밖
        if (gridY < 0 || gridY >= map.length || gridX < 0 || gridX >= map[0].length) {
            return false;
        }

        int cell = map[gridY][gridX];

        // 1, 2를 벽/고정블럭으로 가정
        if (cell == 1 || cell == 2) {
            return false;
        }

        // 폭탄과 충돌 체크
        for (Bomb bomb : bombs) {
            if (bomb.getHitBox().intersects(nextHitBox)) {
                return false;
            }
        }

        return true;
    }

    private void updateHitBox() {
        this.hitBox = HitBox.createCharacterEyeHitBox(
                this.tileX, this.tileY, TILE_SIZE
        );
    }

    // ==== 방향 갱신 메서드 ====
    // 규칙: 여러 방향키가 눌려 있으면 "가장 먼저 눌린 방향키"를 따른다.
    private void updateDirectionFromHeldKeys() {

        Direction newDirection = Direction.NONE;

        // keyOrder 에 남아 있는 키들 중에서
        // 가장 앞(0번 인덱스)이 아직 눌려 있는 방향키라면
        // 그 키의 방향을 따른다.
        if (!keyOrder.isEmpty()) {
            int first = keyOrder.get(0);

            if (first == this.leftKey && this.leftHeld) {
                newDirection = Direction.LEFT;
            } else if (first == this.rightKey && this.rightHeld) {
                newDirection = Direction.RIGHT;
            } else if (first == this.upKey && this.upHeld) {
                newDirection = Direction.UP;
            } else if (first == this.downKey && this.downHeld) {
                newDirection = Direction.DOWN;
            } else {
                // 혹시 keyOrder에는 남아 있는데 Held는 false인 경우가 있다면
                // 일단 NONE으로 두고, 다음 입력에서 정리되도록 한다.
                newDirection = Direction.NONE;
            }
        }

        this.direction = newDirection;
    }

    // 🔹 keyOrder 에 방향키 추가
    private void addKeyToOrder(int code) {
        if (code == upKey || code == downKey || code == leftKey || code == rightKey) {
            if (!keyOrder.contains(code)) {
                keyOrder.add(code);
            }
        }
    }

    // 🔹 keyOrder 에서 방향키 제거
    private void removeKeyFromOrder(int code) {
        keyOrder.remove(Integer.valueOf(code));
    }

    // ===================== 키 입력 처리 =====================

    public void keyPressed(KeyEvent e, List<Bomb> bombs, int[][] map) {
        int code = e.getKeyCode();

        // 폭탄 키는 바로 처리
        if (code == this.bombKey) {
            this.placeBomb(bombs, map);
            return;
        }

        if (code == this.upKey) {
            this.upHeld = true;
            addKeyToOrder(code);
        }
        if (code == this.downKey) {
            this.downHeld = true;
            addKeyToOrder(code);
        }
        if (code == this.leftKey) {
            this.leftHeld = true;
            addKeyToOrder(code);
        }
        if (code == this.rightKey) {
            this.rightHeld = true;
            addKeyToOrder(code);
        }

        this.updateDirectionFromHeldKeys();
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == this.upKey) {
            this.upHeld = false;
            removeKeyFromOrder(code);
        }
        if (code == this.downKey) {
            this.downHeld = false;
            removeKeyFromOrder(code);
        }
        if (code == this.leftKey) {
            this.leftHeld = false;
            removeKeyFromOrder(code);
        }
        if (code == this.rightKey) {
            this.rightHeld = false;
            removeKeyFromOrder(code);
        }

        this.updateDirectionFromHeldKeys();
    }

    // ==== 물풍선 설치 ====

    private void placeBomb(List<Bomb> bombs, int[][] map) {
        if (!this.alive) {
            return;
        }
        if (this.activeBombs >= this.maxBombs) {
            return;
        }

        int gridX = this.getGridX();
        int gridY = this.getGridY();

        if (map[gridY][gridX] != 0) {
            return;
        }

        for (Bomb bomb : bombs) {
            if (bomb.getGridX() == gridX && bomb.getGridY() == gridY) {
                return;
            }
        }

        bombs.add(new Bomb(gridX, gridY, this.bombRange, this));
        this.activeBombs++;
    }

    // 폭탄이 터질 때 Bomb에서 호출
    public void onBombExploded() {
        if (this.activeBombs > 0) {
            this.activeBombs--;
        }
    }

    // ==== 그리기 ====

    public void draw(Graphics g) {
        if (!this.alive) {
            return;
        }

        int screenX = (int) Math.round(this.tileX * TILE_SIZE);
        int screenY = (int) Math.round(this.tileY * TILE_SIZE);

        SpriteStrip currentStrip = getCurrentStrip();
        BufferedImage frame = (currentStrip != null)
                ? currentStrip.getFrame(frameIndex)
                : null;

        if (frame != null) {
            // 스프라이트 이미지
            g.drawImage(frame, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
        } else {
            // 스프라이트 로딩 실패 시 기존 동그라미
            g.setColor(this.type.color);
            g.fillOval(screenX + 8, screenY + 8, TILE_SIZE - 16, TILE_SIZE - 16);

            g.setColor(Color.WHITE);
            g.fillOval(screenX + 14, screenY + 12, 6, 6);
        }
    }

    // ==== Getter / Setter ====

    public HitBox getHitBox() {
        return this.hitBox;
    }

    public int getGridX() {
        return (int) Math.round(this.tileX);
    }

    public int getGridY() {
        return (int) Math.round(this.tileY);
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public double getTileX() {
        return this.tileX;
    }

    public double getTileY() {
        return this.tileY;
    }

    public void decreaseActiveBombs() {
        if (this.activeBombs > 0) {
            this.activeBombs--;
        }
    }

    public Direction getDirection() {
        return this.direction;
    }
}