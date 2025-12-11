import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Random;
public class Tile {

    private int centerX;
    private int centerY;
    private int itemIndex;
    private Boolean IS_BREAKABLE;
    // 디버그용
    private boolean DEBUG_MODE = true;

    public Tile(int centerX, int centerY, int itemIndex, Boolean IS_BREAKABLE) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.itemIndex = itemIndex;
            this.IS_BREAKABLE = IS_BREAKABLE;
            
    }
    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public void draw(Graphics g) {
        BufferedImage img = SpriteStore.getItem(itemIndex);
        if (img == null) return;
    
        int w = 40;   // 실제 판정 너비
        int h = 40;   // 실제 판정 높이
    
        // 이미지 실제 크기 (40x47)
        int imgW = SpriteStore.getItemWidth();   // 40
        int imgH = SpriteStore.getItemHeight();  // 47
    
        int offsetY = imgH - h -6;  // 7
    
        // 판정 기준은 centerX, centerY 의 40x40, 이미지는 위로 7px 올려서 그리기
        int drawX = centerX - imgW / 2;
        int drawY = centerY - imgH / 2 - offsetY;
    
        g.drawImage(img, drawX, drawY, null);

        if (DEBUG_MODE && g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 255, 0, 200));
            g2d.drawRect(centerX - w / 2, centerY - h / 2, w, h);

            g2d.setColor(new Color(255, 0, 0, 255));
            int crossSize = 5;
            g2d.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY);
            g2d.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize);

            g2d.fillOval(centerX - 2, centerY - 2, 4, 4);

            g2d.setColor(new Color(255, 255, 0, 255));
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(centerX + "," + centerY, centerX + 8, centerY - 8);
        }
    }
    public int getItemIndex() { return itemIndex; }

    public void breakBlock() {
        if (this.IS_BREAKABLE) {
            Random random = new Random();
            if (itemIndex == 4) {
                int randomItem = random.nextInt(3) + 1;  // 1, 2, 3 중 랜덤
                setItemIndex(randomItem);
            }
            else if (itemIndex >= 1 && itemIndex <= 3) {
                setItemIndex(5);
            }
        }
    }
}