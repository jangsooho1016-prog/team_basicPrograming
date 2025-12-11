import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Screen extends Canvas implements KeyListener {

    private Map map;
    private Tile[][] tiles;
    private int selectedRow = 0;
    private int selectedCol = 0;
    private int rows = 13;
    private int cols = 15;
    private int gap = 25;
    // 🔴 더블 버퍼용
    private BufferedImage backBuffer;
    // making mode, load mod
    private Boolean ISLOADMAP = true;

    public Screen() {
        setPreferredSize(new Dimension(600, 600));

        map = new Map("res/forest24.png");
        SpriteStore.init();
        initTiles();
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    private void initTiles() {
        if (ISLOADMAP) {
            int[][] data = new int[rows][cols];
            try (BufferedReader br = new BufferedReader(new FileReader("mapData.txt"))) {
                String line;
                int r = 0;
        
                while ((line = br.readLine()) != null && r < rows) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
        
                    String[] parts = line.split("\\s+"); // 공백 기준 분리
        
                    for (int c = 0; c < cols && c < parts.length; c++) {
                        data[r][c] = Integer.parseInt(parts[c]);
                    }
                    r++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            int cellWidth = map.getImageWidth() / cols;
            int cellHeight = map.getImageHeight() / rows;

            tiles = new Tile[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int cx = gap + c * cellWidth + cellWidth / 2;
                    int cy = gap + r * cellHeight + cellHeight / 2;
        
                    int value = data[r][c];
        
                    Tile t = new Tile(cx, cy ,0, false);

                    t.setItemIndex(value); // 0,1,2,... 아이템 인덱스
                    tiles[r][c] = t;
                    }
                }
            }
        else {
            tiles = new Tile[rows][cols];
            int cellWidth = map.getImageWidth() / cols;
            int cellHeight = map.getImageHeight() / rows;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int cx = gap + c * cellWidth + cellWidth / 2;
                    int cy = gap + r * cellHeight + cellHeight / 2;
                    tiles[r][c] = new Tile(cx+5, cy-2, 0, false); 
                }
            }
        }
    }

    // 🔴 화면 크기 변동 시 버퍼 생성/재생성
    private void ensureBackBuffer() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (backBuffer == null
                || backBuffer.getWidth() != w
                || backBuffer.getHeight() != h) {
            backBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
    }
    public void exportTilesToTxt(String path) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
    
            for (int r = 0; r < rows; r++) {
                StringBuilder line = new StringBuilder();
    
                for (int c = 0; c < cols; c++) {
                    Tile t = tiles[r][c];
    
                    int value;
                    value = t.getItemIndex(); // 아이템 인덱스
    
                    line.append(value);
    
                    if (c < cols - 1) {
                        line.append(' ');        // 칸 사이 공백
                    }
                }
                bw.write(line.toString());
                bw.newLine();                    // 행마다 줄바꿈
            }
    
            bw.flush();
            System.out.println("타일 정보 저장 완료: " + path);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Graphics g) {
        // 기본 update() 는 화면을 지우고 paint() 를 호출하기 때문에
        // 깜빡임을 줄이기 위해 곧바로 paint() 만 호출
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        ensureBackBuffer();
        if (backBuffer == null) return;

        Graphics bg = backBuffer.getGraphics();

        // 1) 백버퍼를 지움 (배경색 등)
        bg.clearRect(0, 0, backBuffer.getWidth(), backBuffer.getHeight());

        // 2) 백버퍼에 실제 장면을 모두 그림
        map.drawMap(bg, getWidth(), getHeight());
        if (tiles != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    tiles[r][c].draw(bg);
                }
            }
        }

        bg.dispose();

        // 3) 완성된 백버퍼를 한 번에 화면에 복사
        g.drawImage(backBuffer, 0, 0, null);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // 예: 방향키로 선택 타일 이동
        if (code == KeyEvent.VK_RIGHT) selectedCol = Math.min(selectedCol + 1, cols - 1);
        if (code == KeyEvent.VK_LEFT)  selectedCol = Math.max(selectedCol - 1, 0);
        if (code == KeyEvent.VK_DOWN)  selectedRow = Math.min(selectedRow + 1, rows - 1);
        if (code == KeyEvent.VK_UP)    selectedRow = Math.max(selectedRow - 1, 0);

        // 예: 숫자 1~3 으로 아이템 종류 바꾸기
        if (code == KeyEvent.VK_1) tiles[selectedRow][selectedCol].setItemIndex(0);
        if (code == KeyEvent.VK_2) tiles[selectedRow][selectedCol].setItemIndex(1);
        if (code == KeyEvent.VK_3) tiles[selectedRow][selectedCol].setItemIndex(2);
        if (code == KeyEvent.VK_4) tiles[selectedRow][selectedCol].setItemIndex(3);
        if (code == KeyEvent.VK_5) tiles[selectedRow][selectedCol].setItemIndex(4);
        // S 키로 저장
        if (code == KeyEvent.VK_S) {
            exportTilesToTxt("mapData.txt");
        }
        repaint();
    }
    @Override
    public void keyReleased(KeyEvent e) {}
}