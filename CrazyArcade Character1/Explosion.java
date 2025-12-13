import java.awt.Color;
import java.awt.Graphics;

/**
 * 물줄기(폭발)
 * - 한 타일에서 잠깐 유지되다가 사라짐
 */
public class Explosion {

    private static final int TILE_SIZE = 40;

    private final int gridX;
    private final int gridY;

    private int timer; // 수명 (프레임 단위)

    public Explosion(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.timer = 30; // 약 0.5초 (60FPS 가정)
    }

    public void update() {
        this.timer--;
    }

    public boolean isFinished() {
        return this.timer <= 0;
    }

    public HitBox getHitBox() {
        return HitBox.createExplosionTileHitBox(this.gridX, this.gridY, TILE_SIZE);
    }

    public boolean hits(Player player) {
        if (!player.isAlive()) {
            return false;
        }
        return this.getHitBox().intersects(player.getHitBox());
    }

    public void draw(Graphics g) {
        int screenX = this.gridX * TILE_SIZE;
        int screenY = this.gridY * TILE_SIZE;

        g.setColor(new Color(255, 165, 0, 200)); // 반투명 주황
        g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
    }
}