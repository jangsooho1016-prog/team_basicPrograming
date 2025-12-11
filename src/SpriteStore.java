import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * ========================================================
 * 스프라이트 저장소 클래스 (SpriteStore)
 * ========================================================
 * 아이템/블록 이미지(스프라이트)를 로드하고 관리하는 정적 클래스입니다.
 * 
 * Items.png 스프라이트 시트에서 개별 아이템 이미지를 추출합니다.
 * 마젠타 배경색(#FF00FF)을 투명하게 처리합니다.
 */
public class SpriteStore {

    // ========== 스프라이트 데이터 ==========
    private static BufferedImage itemsSheet; // 스프라이트 시트 원본
    private static BufferedImage[] items; // 개별 아이템 이미지 배열

    // ========== 스프라이트 상수 ==========
    private static final int ITEM_WIDTH = 40; // 아이템 이미지 너비
    private static final int ITEM_HEIGHT = 47; // 아이템 이미지 높이
    private static final int ITEMS_COUNT = 4; // 아이템 개수 (0~3)
    private static final int BACKGROUND_COLOR = 0xFF00FF; // 투명 처리할 배경색 (마젠타)

    /**
     * 스프라이트 스토어 초기화
     * Items.png 파일을 로드하고 개별 아이템 이미지를 추출합니다.
     */
    public static void init() {
        if (items != null)
            return; // 이미 로드됨 (중복 호출 방지)

        try {
            // 아이템 스프라이트 시트 로드
            String path = System.getProperty("user.dir") + File.separator + "res" + File.separator + "Items.png";
            BufferedImage itemImage = ImageIO.read(new File(path));
            System.out.println("아이템 스프라이트 로드 성공: " + path);

            // ARGB 형식으로 변환 (알파 채널 지원)
            itemsSheet = convertToARGB(itemImage);

            // 개별 아이템 이미지 추출 (투명 처리 포함)
            loadItemsWithTransparency();
        } catch (IOException e) {
            System.err.println("SpriteStore 이미지 로드 실패");
            e.printStackTrace();
        }
    }

    /**
     * 이미지를 ARGB 형식으로 변환
     * 알파(투명도) 채널을 지원하는 형식으로 변환합니다.
     * 
     * @param original 원본 이미지
     * @return ARGB 형식 이미지
     */
    private static BufferedImage convertToARGB(BufferedImage original) {
        BufferedImage argbImage = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        argbImage.getGraphics().drawImage(original, 0, 0, null);
        return argbImage;
    }

    /**
     * 스프라이트 시트에서 개별 아이템 이미지 추출
     * 마젠타 배경색을 투명하게 처리합니다.
     */
    private static void loadItemsWithTransparency() {
        if (itemsSheet == null)
            return;

        items = new BufferedImage[ITEMS_COUNT];

        int sheetWidth = itemsSheet.getWidth();

        // 각 아이템 이미지 추출 (좌에서 우로 순서대로)
        for (int i = 0; i < ITEMS_COUNT; i++) {
            int x = i * ITEM_WIDTH;

            // 이미지 범위 검사
            if (x + ITEM_WIDTH > sheetWidth) {
                System.err.println("아이템 인덱스 " + i + " 가 이미지 범위를 벗어남");
                break;
            }

            // 스프라이트 시트에서 해당 영역 잘라내기
            BufferedImage original = itemsSheet.getSubimage(x, 0, ITEM_WIDTH, ITEM_HEIGHT);

            // 마젠타 배경을 투명하게 처리
            items[i] = makeColorTransparent(original, BACKGROUND_COLOR);
        }
    }

    /**
     * 특정 색상을 투명하게 처리
     * 마젠타(#FF00FF) 배경을 완전 투명으로 변환합니다.
     * 
     * @param image         원본 이미지
     * @param colorToRemove 투명화할 색상 (0xRRGGBB)
     * @return 투명 처리된 이미지
     */
    private static BufferedImage makeColorTransparent(BufferedImage image, int colorToRemove) {
        BufferedImage transparent = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        // 비교할 RGB 값 (알파 제외)
        int targetRGB = colorToRemove & 0x00FFFFFF;

        // 모든 픽셀 순회
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int pixelRGB = pixel & 0x00FFFFFF;

                if (pixelRGB == targetRGB) {
                    // 마젠타 색상 → 완전 투명
                    transparent.setRGB(x, y, 0x00000000);
                } else {
                    // 다른 색상 → 불투명 유지
                    transparent.setRGB(x, y, pixel | 0xFF000000);
                }
            }
        }
        return transparent;
    }

    /**
     * 아이템 이미지 반환
     * 
     * @param index 아이템 인덱스 (0~3)
     * @return 아이템 이미지, 범위 밖이면 null
     */
    public static BufferedImage getItem(int index) {
        if (items == null)
            return null;
        if (index < 0 || index >= items.length)
            return null;
        return items[index];
    }

    /**
     * 아이템 이미지 너비 반환
     * 
     * @return 아이템 너비 (40)
     */
    public static int getItemWidth() {
        return ITEM_WIDTH;
    }

    /**
     * 아이템 이미지 높이 반환
     * 
     * @return 아이템 높이 (47)
     */
    public static int getItemHeight() {
        return ITEM_HEIGHT;
    }
}