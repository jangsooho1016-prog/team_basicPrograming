import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteStore {

    private static BufferedImage itemsSheet;
    private static BufferedImage[] items;

    private static final int ITEM_WIDTH = 40;
    private static final int ITEM_HEIGHT = 47;
    private static final int ITEMS_COUNT = 4;
    private static final int BACKGROUND_COLOR = 0xFF00FF;

    public static void init() {
        if (items != null) return; // 이미 로드됨

        try {
            BufferedImage itemImage = ImageIO.read(new File("res/Items.png"));
            itemsSheet = convertToARGB(itemImage);
            loadItemsWithTransparency();
        } catch (IOException e) {
            System.err.println("SpriteStore 이미지 로드 실패");
            e.printStackTrace();
        }
    }

    private static BufferedImage convertToARGB(BufferedImage original) {
        BufferedImage argbImage = new BufferedImage(
            original.getWidth(),
            original.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        argbImage.getGraphics().drawImage(original, 0, 0, null);
        return argbImage;
    }

    private static void loadItemsWithTransparency() {
        if (itemsSheet == null) return;

        items = new BufferedImage[ITEMS_COUNT];
        
        int sheetWidth = itemsSheet.getWidth();

        for (int i = 0; i < ITEMS_COUNT; i++) {
            int x = i * ITEM_WIDTH;
            if (x + ITEM_WIDTH > sheetWidth) {
                System.err.println("아이템 인덱스 " + i + " 가 이미지 범위를 벗어남");
                break; // 또는 continue;
            }

            BufferedImage original = itemsSheet.getSubimage(x, 0, ITEM_WIDTH, ITEM_HEIGHT);
            items[i] = makeColorTransparent(original, BACKGROUND_COLOR);
        }
    }

    private static BufferedImage makeColorTransparent(BufferedImage image, int colorToRemove) {
        BufferedImage transparent = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );

        int targetRGB = colorToRemove & 0x00FFFFFF;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int pixelRGB = pixel & 0x00FFFFFF;

                if (pixelRGB == targetRGB) {
                    transparent.setRGB(x, y, 0x00000000); // 완전 투명
                } else {
                    transparent.setRGB(x, y, pixel | 0xFF000000); // 알파 채우기
                }
            }
        }
        return transparent;
    }

    public static BufferedImage getItem(int index) {
        if (items == null) return null;
        if (index < 0 || index >= items.length) return null;
        return items[index];
    }

    public static int getItemWidth() {
        return ITEM_WIDTH;
    }

    public static int getItemHeight() {
        return ITEM_HEIGHT;
    }
}