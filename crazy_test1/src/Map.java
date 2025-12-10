import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Map {
    private BufferedImage mapImage;
    private int gap = 50;
    
    
    public Map(String imagePath) {
        try {
            mapImage = ImageIO.read(new File("res/forest24.png"));         
        } 
        catch (IOException e) {
            System.err.println("이미지 로드 실패");
            e.printStackTrace();
        }
    }
    public void drawMap(Graphics g, int canvasWidth, int canvasHeight) {
        if (mapImage == null) return;
        
        // 배경 맵 그리기
        g.drawImage(mapImage, gap, gap, null);
        
    }
    public int getImageWidth() {
        if (mapImage == null) return 0;
        return mapImage.getWidth();
    }
    
    public int getImageHeight() {
        if (mapImage == null) return 0;
        return mapImage.getHeight();
    }
}
