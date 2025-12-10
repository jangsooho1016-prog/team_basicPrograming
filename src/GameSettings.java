import java.awt.event.KeyEvent;

// ========================================================
// [핵심] 게임 설정값 저장 클래스
// ========================================================
public class GameSettings {
    public static int bgmVolume = 50;
    public static int sfxVolume = 50;
    public static int p1_Up = KeyEvent.VK_W;
    public static int p1_Down = KeyEvent.VK_S;
    public static int p1_Left = KeyEvent.VK_A;
    public static int p1_Right = KeyEvent.VK_D;
    public static int p1_Bomb = KeyEvent.VK_SHIFT; // 물풍선: Shift
    public static int p1_Item = KeyEvent.VK_CONTROL; // 아이템: Ctrl
    public static int p2_Up = KeyEvent.VK_UP;
    public static int p2_Down = KeyEvent.VK_DOWN;
    public static int p2_Left = KeyEvent.VK_LEFT;
    public static int p2_Right = KeyEvent.VK_RIGHT;
    public static int p2_Bomb = KeyEvent.VK_NUMPAD1; // 물풍선: NumPad 1
    public static int p2_Item = KeyEvent.VK_NUMPAD0; // 아이템: NumPad 0
}
