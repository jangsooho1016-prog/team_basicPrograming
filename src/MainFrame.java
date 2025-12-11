import javax.swing.JFrame;

public class MainFrame extends JFrame {

    private int width = 800;
    private int height = 600;
    public MainFrame() {
        setTitle("Map Demo");
        setSize(width,height);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new Screen());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}