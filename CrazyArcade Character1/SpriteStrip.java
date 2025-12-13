import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 가로로 일렬로 붙어있는 스프라이트(strip)를
 * 일정 개수(frameCount)만큼 잘라서 쓰는 도우미 클래스.
 *
 * - 특정 색(예: 마젠타)을 완전히 투명하게 바꾼다.
 */
public class SpriteStrip {

    private BufferedImage sheet;
    private int frameWidth;
    private int frameHeight;
    private int frameCount;

    /**
     * @param path       예) "res/BlueBazzi_Right.bmp"
     * @param frameCount 프레임 개수 (보통 5)
     */
    public SpriteStrip(String path, int frameCount) {
        try {
            BufferedImage loaded = ImageIO.read(new File(path));
            if (loaded == null) {
                System.err.println("이미지 로딩 실패: " + path);
            } else {
                // ★ 여기서 배경색 제거 (지우고 싶은 색을 넣어 준다)
                //   배경색이 진짜로 무엇인지 그림판 스포이드로 찍어서 RGB 맞추면 더 정확함.
                Color bg = new Color(255, 0, 255);   // 예: 마젠타 배경(#FF00FF)
                this.sheet = removeColorToTransparent(loaded, bg);
            }
        } catch (IOException e) {
            System.err.println("스프라이트 로딩 실패: " + path);
            e.printStackTrace();
        }

        this.frameCount = frameCount;

        if (sheet != null) {
            this.frameWidth = sheet.getWidth() / frameCount;
            this.frameHeight = sheet.getHeight();
        }
    }

    /**
     * image 에서 target 색을 완전히 투명하게 바꾼 새 이미지를 만들어 반환한다.
     */
    private BufferedImage removeColorToTransparent(BufferedImage image, Color target) {
        int w = image.getWidth();
        int h = image.getHeight();

        int tr = target.getRed();
        int tg = target.getGreen();
        int tb = target.getBlue();

        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                int rgb = image.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8)  & 0xFF;
                int b =  rgb        & 0xFF;

                // ★ 완전 동일한 색이면 → 투명 처리
                if (r == tr && g == tg && b == tb) {
                    // A=0, RGB는 아무 색이나 상관 없음
                    dest.setRGB(x, y, 0x00000000);
                } else {
                    // 원래 색 유지 + 알파=255
                    dest.setRGB(x, y, (0xFF << 24) | (rgb & 0x00FFFFFF));
                }
            }
        }
        return dest;
    }

    public BufferedImage getFrame(int index) {
        if (sheet == null) return null;
        if (index < 0 || index >= frameCount) return null;
        return sheet.getSubimage(index * frameWidth, 0, frameWidth, frameHeight);
    }

    public int getFrameCount() {
        return frameCount;
    }
}