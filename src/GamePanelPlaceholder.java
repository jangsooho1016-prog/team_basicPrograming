import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// ========================================================
// 6. 게임 패널 플레이스홀더
// ========================================================
public class GamePanelPlaceholder extends JPanel {
    private CrazyArcade_UI mainFrame;

    public GamePanelPlaceholder(CrazyArcade_UI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setBackground(Color.BLACK);

        JLabel infoLabel = new JLabel("TEAM PROJECT: GAME LOGIC AREA");
        infoLabel.setForeground(Color.GREEN);
        infoLabel.setFont(new Font("Courier New", Font.BOLD, 30));
        infoLabel.setBounds(150, 200, 600, 50);
        add(infoLabel);

        JLabel subLabel = new JLabel("팀원들이 작성한 게임 패널 코드가 들어갈 자리입니다.");
        subLabel.setForeground(Color.WHITE);
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
        subLabel.setBounds(180, 260, 500, 30);
        add(subLabel);

        JLabel guideLabel = new JLabel("Press [ESC] to return to Lobby");
        guideLabel.setForeground(Color.YELLOW);
        guideLabel.setBounds(300, 400, 300, 30);
        add(guideLabel);

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
                }
            }
        });
    }
}
