import Explosion.ExplosionType;

public class Explosion {

    public enum ExplosionType {
        CENTER, 
        UP, 
        DOWN, 
        LEFT, 
        RIGHT
    }
    
    private static final int FRAME_DURATION_MS = 100; // 한 프레임당 표시 시간
    private static final int FRAME_COUNT = 5;         // 총 애니메이션 프레임 수
    
    private final int x, y;
    private final long startTime;
    private final ExplosionType type;
    private boolean active = true;

    public Explosion(int x, int y, long startTime, ExplosionType type) {
        this.x = x;
        this.y = y;
        this.startTime = startTime;
        this.type = type; 
    }

    public void update(long currentTime) {
        // 총 애니메이션 시간을 초과하면 폭발 종료
        if (currentTime - startTime >= FRAME_COUNT * FRAME_DURATION_MS) {
            this.active = false;
        }
    }


    public int getCurrentFrameIndex(long currentTime) {
        if (!active) return FRAME_COUNT - 1; 
        
        long elapsedTime = currentTime - startTime;
        int frameIndex = (int)(elapsedTime / FRAME_DURATION_MS);
        
        return Math.min(frameIndex, FRAME_COUNT - 1); // 인덱스가 범위를 초과하지 않도록 보장
    }
    
    public boolean isActive() {
        return active;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public ExplosionType getType() {
        return type;
    }
}
