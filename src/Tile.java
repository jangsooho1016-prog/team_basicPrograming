import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * ========================================================
 * 타일 클래스 (Tile)
 * ========================================================
 * 맵 위의 개별 타일(블록/아이템)을 관리하는 클래스입니다.
 * 
 * 타일 인덱스 (itemIndex):
 * - 0: 빈 타일 (아무것도 표시 안 함)
 * - 1, 2, 3: 아이템 (물풍선, 물줄기, 스케이트 등)
 * - 4: 파괴 가능한 블록 (박스)
 * - 5: 파괴된 상태
 */
public class Tile {

    // ========== 타일 속성 ==========
    private int centerX; // 타일 중심 X 좌표
    private int centerY; // 타일 중심 Y 좌표
    private int itemIndex; // 아이템/블록 종류 (0~5)
    private Boolean IS_BREAKABLE; // 파괴 가능 여부

    // 디버그 모드 (true: 테두리/좌표 표시, false: 표시 안 함)
    private boolean DEBUG_MODE = false;

    /**
     * 생성자: 타일 객체 생성
     * 
     * @param centerX      타일 중심 X 좌표
     * @param centerY      타일 중심 Y 좌표
     * @param itemIndex    아이템/블록 종류 (0~5)
     * @param IS_BREAKABLE 파괴 가능 여부
     */
    public Tile(int centerX, int centerY, int itemIndex, Boolean IS_BREAKABLE) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.itemIndex = itemIndex;
        this.IS_BREAKABLE = IS_BREAKABLE;
    }

    /**
     * 아이템 인덱스 설정
     * 
     * @param itemIndex 새로운 아이템 인덱스
     */
    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    /**
     * 타일 그리기
     * SpriteStore에서 해당 아이템 이미지를 가져와 화면에 그립니다.
     * 
     * @param g Graphics 객체
     */
    public void draw(Graphics g) {
        BufferedImage img = SpriteStore.getItem(itemIndex);
        if (img == null)
            return;

        int w = 40; // 타일 판정 너비
        int h = 40; // 타일 판정 높이

        // 스프라이트 이미지 실제 크기 (40x47)
        int imgW = SpriteStore.getItemWidth(); // 40
        int imgH = SpriteStore.getItemHeight(); // 47

        // Y축 오프셋 (이미지를 위로 올려서 그리기)
        int offsetY = imgH - h - 6; // 7

        // 이미지 그리기 좌표 계산
        int drawX = centerX - imgW / 2;
        int drawY = centerY - imgH / 2 - offsetY;

        // 아이템/블록 이미지 그리기
        g.drawImage(img, drawX, drawY, null);

        // ========== 디버그 모드: 테두리/좌표 표시 ==========
        if (DEBUG_MODE && g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;

            // 녹색 판정 박스 테두리
            g2d.setColor(new Color(0, 255, 0, 200));
            g2d.drawRect(centerX - w / 2, centerY - h / 2, w, h);

            // 빨간색 중심점 표시
            g2d.setColor(new Color(255, 0, 0, 255));
            int crossSize = 5;
            g2d.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY);
            g2d.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize);
            g2d.fillOval(centerX - 2, centerY - 2, 4, 4);

            // 노란색 좌표 텍스트
            g2d.setColor(new Color(255, 255, 0, 255));
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(centerX + "," + centerY, centerX + 8, centerY - 8);
        }
    }

    /**
     * 아이템 인덱스 반환
     * 
     * @return 현재 아이템 인덱스
     */
    public int getItemIndex() {
        return itemIndex;
    }

    /**
     * 블록 파괴 처리
     * 파괴 가능한 블록(4)이면 랜덤 아이템(1, 2)으로 변경,
     * 아이템(1, 2)이면 빈 상태(5)로 변경합니다.
     */
    public void breakBlock() {
        if (this.IS_BREAKABLE) {
            Random random = new Random();
            if (itemIndex == 4) {
                // [수정] 블록 → 랜덤 아이템 (1, 2 중에서 선택)
                // 기존 코드는 1~3이었으나, 3번은 '안 부서지는 벽'이므로 제외함.
                int randomItem = random.nextInt(2) + 1;
                setItemIndex(randomItem);
            } else if (itemIndex >= 1 && itemIndex <= 2) {
                // 아이템(1, 2) → 빈 상태(5)
                setItemIndex(5);
            }
        }
    }

    /**
     * 타일 중심 X 좌표 반환
     */
    public int getCenterX() {
        return centerX;
    }

    /**
     * 타일 중심 Y 좌표 반환
     */
    public int getCenterY() {
        return centerY;
    }

    /**
     * 파괴 가능 여부 반환
     */
    public boolean isBreakable() {
        return IS_BREAKABLE;
    }
}