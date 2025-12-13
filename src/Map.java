import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * ========================================================
 * 게임 맵 클래스 (Map)
 * ========================================================
 * 게임 배경 맵 이미지를 로드하고 화면에 그리는 역할을 합니다.
 * 
 * 사용 가능한 맵:
 * - forest24.png: 숲 테마 맵
 * - map2.png: 기본 맵
 * 
 * 사용 예시:
 * Map gameMap = new Map("forest24.png");
 */
public class Map {
    // 맵 배경 이미지
    private BufferedImage mapImage;

    /**
     * 생성자: 맵 이미지 로드
     * 
     * @param imagePath 맵 이미지 파일명 (예: "map2.png", "forest24.png")
     *                  res/ 폴더에서 이미지를 로드합니다.
     */
    public Map(String imagePath) {
        try {
            // 프로젝트 루트/res/ 폴더에서 이미지 로드
            String path = System.getProperty("user.dir") + File.separator + "res" + File.separator + imagePath;
            File mapFile = new File(path);

            if (mapFile.exists()) {
                mapImage = ImageIO.read(mapFile);
                System.out.println("맵 이미지 로드 성공: " + path);
            } else {
                System.err.println("맵 파일이 존재하지 않습니다: " + path);
            }
        } catch (IOException e) {
            System.err.println("맵 이미지 로드 실패: " + imagePath);
            e.printStackTrace();
        }
    }

    /**
     * 맵 그리기
     * 지정된 위치와 크기로 맵 배경을 그립니다.
     * 
     * @param g       Graphics 객체
     * @param offsetX 맵 시작 X 좌표
     * @param offsetY 맵 시작 Y 좌표
     * @param width   맵 너비
     * @param height  맵 높이
     */
    public void drawMap(Graphics g, int offsetX, int offsetY, int width, int height) {
        if (mapImage == null)
            return;
        g.drawImage(mapImage, offsetX, offsetY, width, height, null);
    }

    /**
     * 맵 이미지 너비 반환
     * 
     * @return 맵 이미지 너비 (픽셀), 이미지 없으면 0
     */
    public int getImageWidth() {
        return (mapImage != null) ? mapImage.getWidth() : 0;
    }

    /**
     * 맵 이미지 높이 반환
     * 
     * @return 맵 이미지 높이 (픽셀), 이미지 없으면 0
     */
    public int getImageHeight() {
        return (mapImage != null) ? mapImage.getHeight() : 0;
    }

    /**
     * 맵 이미지가 로드되었는지 확인
     * 
     * @return 이미지 로드 성공 시 true
     */
    public boolean isLoaded() {
        return mapImage != null;
    }
}
