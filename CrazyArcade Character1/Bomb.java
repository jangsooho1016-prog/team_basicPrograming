import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * 물풍선(폭탄)
 * - 일정 시간 후 폭발
 * - 네 방향으로 물줄기(Explosion) 생성
 */
public class Bomb {
// 변수 final을 수정
    private static final int TILE_SIZE = 40;

    private final int gridX;
    private final int gridY;
    private final int range;
    private final Player owner;

    private int timer; // 남은 프레임
    private final HitBox hitBox;

    public Bomb(int gridX, int gridY, int range, Player owner) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.range = range;
        this.owner = owner;
        this.timer = 60; // 약 1초 (60FPS 가정)

        this.hitBox = HitBox.createBombHitBox(this.gridX, this.gridY, TILE_SIZE);
    }

    public void update() {
        this.timer--;
    }

    public boolean shouldExplode() {
        return this.timer <= 0;
    }

    public HitBox getHitBox() {
        return this.hitBox;
    }

    public int getGridX() {
        return this.gridX;
    }

    public int getGridY() {
        return this.gridY;
    }

    public List<Explosion> explode(int[][] map) {
        if (this.owner != null) {
            this.owner.onBombExploded();
        }

        List<Explosion> explosions = new ArrayList<>();
        explosions.add(new Explosion(this.gridX, this.gridY));

        int[][] directions = {
                { 1, 0 },  // 오른쪽
                { -1, 0 }, // 왼쪽
                { 0, 1 },  // 아래
                { 0, -1 }  // 위
        };

        for (int[] dir : directions) {
            int currentX = this.gridX;
            int currentY = this.gridY;

            for (int i = 0; i < this.range; i++) {
                currentX += dir[0];
                currentY += dir[1];

                if (currentY < 0 || currentY >= map.length ||
                    currentX < 0 || currentX >= map[0].length) {
                    break;
                }

                int cell = map[currentY][currentX];

                if (cell == 1) { // 단단한 벽
                    break;
                }

                explosions.add(new Explosion(currentX, currentY));

                if (cell == 2) { // 깨지는 벽
                    map[currentY][currentX] = 0;
                    break;
                }
            }
        }

        return explosions;
    }

    public void draw(Graphics g) {
        int screenX = this.gridX * TILE_SIZE;
        int screenY = this.gridY * TILE_SIZE;

        g.setColor(Color.BLACK);
        g.fillOval(screenX + 8, screenY + 8, 24, 24);

        g.setColor(Color.WHITE);
        g.fillOval(screenX + 14, screenY + 12, 6, 6);
    }
}