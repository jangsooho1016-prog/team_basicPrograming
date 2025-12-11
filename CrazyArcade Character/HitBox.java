import java.awt.Rectangle;

/**
 * 보이지 않는 히트박스(충돌 판정 전용) 클래스
 * - 캐릭터, 폭탄, 폭발(물줄기) 공통으로 사용
 */
public class HitBox {

    private Rectangle rect;

    public HitBox(int x, int y, int width, int height) {
        this.rect = new Rectangle(x, y, width, height);
    }

    public boolean intersects(HitBox other) {
        return this.rect.intersects(other.rect);
    }

    public boolean contains(int x, int y) {
        return this.rect.contains(x, y);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.rect.setBounds(x, y, width, height);
    }

    public Rectangle toRectangle() {
        return this.rect;
    }

    // ===== 정적 팩토리 메서드들 =====

    /**
     * 캐릭터 눈알 크기 히트박스 (6x6)
     * - tileX, tileY : 타일 좌표
     * - tileSize : 타일 한 칸 픽셀 크기 (예: 40)
     */
    public static HitBox createCharacterEyeHitBox(double tileX, double tileY, int tileSize) {
        int baseX = (int) Math.round(tileX * tileSize);
        int baseY = (int) Math.round(tileY * tileSize);

        int offsetX = 14;
        int offsetY = 12;
        int width   = 6;
        int height  = 6;

        return new HitBox(baseX + offsetX, baseY + offsetY, width, height);
    }

    /**
     * 폭탄(물풍선) 히트박스
     * - gridX, gridY : 타일 좌표
     */
    public static HitBox createBombHitBox(int gridX, int gridY, int tileSize) {
        int x = gridX * tileSize + 8;
        int y = gridY * tileSize + 8;
        int size = 24;
        return new HitBox(x, y, size, size);
    }

    /**
     * 폭발(물줄기) 한 타일 전체 히트박스
     */
    public static HitBox createExplosionTileHitBox(int gridX, int gridY, int tileSize) {
        int x = gridX * tileSize;
        int y = gridY * tileSize;
        return new HitBox(x, y, tileSize, tileSize);
    }
}