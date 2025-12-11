import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 게임 맵 클래스
 * 배경 맵 이미지를 로드하고 화면에 그리는 역할을 합니다.
 */
public class Map {
    private BufferedImage mapImage; // 맵 배경 이미지

    /**
     * 생성자: 맵 이미지 로드
     * 
     * @param imagePath 맵 이미지 파일명 (예: "map2.png", "forest24.png")
     */
    public Map(String imagePath) {
        try {
            // 프로젝트 루트 기준 절대 경로로 맵 이미지 로드
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

        // 지정된 위치/크기로 맵 그리기
        g.drawImage(mapImage, offsetX, offsetY, width, height, null);
    }

    /**
     * 원본 크기로 맵 그리기 (기존 호환용)
     */
    public void drawMap(Graphics g, int canvasWidth, int canvasHeight) {
        if (mapImage == null)
            return;
        g.drawImage(mapImage, 0, 0, null);
    }

    /**
     * 맵 이미지 너비 반환
     */
    public int getImageWidth() {
        if (mapImage == null)
            return 0;
        return mapImage.getWidth();
    }

    /**
     * 맵 이미지 높이 반환
     */
    public int getImageHeight() {
        if (mapImage == null)
            return 0;
        return mapImage.getHeight();
    }

    /**
     * 맵 이미지가 로드되었는지 확인
     */
    public boolean isLoaded() {
        return mapImage != null;
    }
}
