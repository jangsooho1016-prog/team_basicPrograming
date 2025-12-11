import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * 실행용 메인 클래스
 */
public class CrazyArcadeMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Crazy Arcade Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GamePanel panel = new GamePanel();
            frame.setContentPane(panel);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.requestFocusInWindow();
        });
    }
}