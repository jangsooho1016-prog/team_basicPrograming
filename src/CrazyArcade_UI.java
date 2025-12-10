import javax.swing.*;
import java.awt.*;
import java.io.File;

// ========================================================
// Main UI Class
// ========================================================
public class CrazyArcade_UI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    public static final String PANEL_SPLASH = "SPLASH";
    public static final String PANEL_MENU = "MENU";
    public static final String PANEL_LOBBY = "LOBBY";
    public static final String PANEL_GAME = "GAME";
    public static final String PANEL_GUIDE = "GUIDE";
    public static final String PANEL_CREDITS = "CREDITS";
    public static final String PANEL_SETTINGS = "SETTINGS";

    public CrazyArcade_UI() {
        setTitle("Water Bomb Man - UI Prototype");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(new SplashPanel(this), PANEL_SPLASH);
        mainContainer.add(new MenuPanel(this), PANEL_MENU);
        mainContainer.add(new LobbyPanel(this), PANEL_LOBBY);
        mainContainer.add(new GamePanelPlaceholder(this), PANEL_GAME);
        mainContainer.add(new GuidePanel(this), PANEL_GUIDE);
        mainContainer.add(new CreditsPanel(this), PANEL_CREDITS);
        mainContainer.add(new SettingsPanel(this), PANEL_SETTINGS);

        add(mainContainer);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // 스플래시 화면 먼저 표시
        showPanel(PANEL_SPLASH);
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainContainer, panelName);
        if (panelName.equals(PANEL_GAME))
            mainContainer.getComponent(3).requestFocusInWindow();
        CreditsPanel cp = (CreditsPanel) mainContainer.getComponent(5);
        if (panelName.equals(PANEL_CREDITS))
            cp.startScrolling();
        else
            cp.stopScrolling();
    }

    // BGM 재생 시작 (메뉴 화면으로 이동 시 호출)
    public void startBGM() {
        String bgmPath = System.getProperty("user.dir") + File.separator + "sound" + File.separator + "노래.wav";
        BGMPlayer.getInstance().loadAndPlay(bgmPath);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrazyArcade_UI());
    }
}