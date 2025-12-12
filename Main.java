import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Crazy Arcade BnB - 갇힘 이미지 추가");
        
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);

        frame.addKeyListener(gamePanel);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        
        frame.setSize(800, 600); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        gamePanel.startGameLoop();
    }
}
