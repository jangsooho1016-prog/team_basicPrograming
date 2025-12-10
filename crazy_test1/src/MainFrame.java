import javax.swing.JFrame;

public class MainFrame extends JFrame {

    private int width = 1024;
    private int height = 768;
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