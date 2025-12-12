import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Properties;

/**
 * ========================================================
 * [핵심] 게임 설정값 저장 클래스 (Global Settings)
 * ========================================================
 * 게임 전체에서 공유되는 설정값을 static 변수로 관리하는 클래스입니다.
 * 파일 입출력(I/O)을 통해 설정을 저장/로드하는 기능을 제공합니다.
 */
public class GameSettings {
    private static final String SETTINGS_FILE = "settings.properties";

    // 사운드 볼륨 (0 ~ 100)
    public static int bgmVolume = 50; // 배경음악 크기
    public static int sfxVolume = 50; // 효과음 크기

    // [Player 1] 키 매핑 설정
    public static int p1_Up = KeyEvent.VK_W;
    public static int p1_Down = KeyEvent.VK_S;
    public static int p1_Left = KeyEvent.VK_A;
    public static int p1_Right = KeyEvent.VK_D;
    public static int p1_Bomb = KeyEvent.VK_SHIFT; // 물풍선: Shift
    public static int p1_Item = KeyEvent.VK_CONTROL; // 아이템: Ctrl

    // [Player 2] 키 매핑 설정
    public static int p2_Up = KeyEvent.VK_UP;
    public static int p2_Down = KeyEvent.VK_DOWN;
    public static int p2_Left = KeyEvent.VK_LEFT;
    public static int p2_Right = KeyEvent.VK_RIGHT;
    public static int p2_Bomb = KeyEvent.VK_NUMPAD1; // 물풍선: NumPad 1
    public static int p2_Item = KeyEvent.VK_NUMPAD0; // 아이템: NumPad 0

    /**
     * 설정을 파일에서 로드합니다.
     * 프로그램 시작 시 호출하여 이전 설정을 복원합니다.
     */
    public static void loadSettings() {
        Properties props = new Properties();
        File settingsFile = new File(SETTINGS_FILE);

        if (!settingsFile.exists()) {
            return; // 파일이 없으면 기본값 사용
        }

        try (FileInputStream fis = new FileInputStream(settingsFile)) {
            props.load(fis);

            // 볼륨 설정 로드
            bgmVolume = parseValue(props.getProperty("bgmVolume", "50"));
            sfxVolume = parseValue(props.getProperty("sfxVolume", "50"));

            // Player 1 키 매핑 로드
            p1_Up = parseValue(props.getProperty("p1_Up", String.valueOf(KeyEvent.VK_W)));
            p1_Down = parseValue(props.getProperty("p1_Down", String.valueOf(KeyEvent.VK_S)));
            p1_Left = parseValue(props.getProperty("p1_Left", String.valueOf(KeyEvent.VK_A)));
            p1_Right = parseValue(props.getProperty("p1_Right", String.valueOf(KeyEvent.VK_D)));
            p1_Bomb = parseValue(props.getProperty("p1_Bomb", String.valueOf(KeyEvent.VK_SHIFT)));
            p1_Item = parseValue(props.getProperty("p1_Item", String.valueOf(KeyEvent.VK_CONTROL)));

            // Player 2 키 매핑 로드
            p2_Up = parseValue(props.getProperty("p2_Up", String.valueOf(KeyEvent.VK_UP)));
            p2_Down = parseValue(props.getProperty("p2_Down", String.valueOf(KeyEvent.VK_DOWN)));
            p2_Left = parseValue(props.getProperty("p2_Left", String.valueOf(KeyEvent.VK_LEFT)));
            p2_Right = parseValue(props.getProperty("p2_Right", String.valueOf(KeyEvent.VK_RIGHT)));
            p2_Bomb = parseValue(props.getProperty("p2_Bomb", String.valueOf(KeyEvent.VK_NUMPAD1)));
            p2_Item = parseValue(props.getProperty("p2_Item", String.valueOf(KeyEvent.VK_NUMPAD0)));

            System.out.println("설정 로드 완료: " + SETTINGS_FILE);
        } catch (IOException | NumberFormatException e) {
            System.err.println("설정 로드 실패: " + e.getMessage());
        }
    }

    /**
     * 값에서 주석(# 이후)을 제거하고 정수로 변환합니다.
     */
    private static int parseValue(String value) {
        if (value == null)
            return 0;
        // # 이후 주석 제거
        if (value.contains("#")) {
            value = value.substring(0, value.indexOf("#"));
        }
        return Integer.parseInt(value.trim());
    }

    /**
     * 현재 설정을 파일에 저장합니다.
     * 설정 변경 시 호출하여 영구적으로 저장합니다.
     * 키 코드 옆에 실제 키 이름을 주석으로 추가합니다.
     */
    public static void saveSettings() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(SETTINGS_FILE))) {
            writer.println("# CrazyArcade Settings");
            writer.println(
                    "# 저장 시간: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            writer.println();

            // 볼륨 설정 저장
            writer.println("# === 사운드 설정 ===");
            writer.println("bgmVolume=" + bgmVolume);
            writer.println("sfxVolume=" + sfxVolume);
            writer.println();

            // Player 1 키 매핑 저장
            writer.println("# === Player 1 조작키 ===");
            writer.println("p1_Up=" + p1_Up + "    # " + KeyEvent.getKeyText(p1_Up));
            writer.println("p1_Down=" + p1_Down + "    # " + KeyEvent.getKeyText(p1_Down));
            writer.println("p1_Left=" + p1_Left + "    # " + KeyEvent.getKeyText(p1_Left));
            writer.println("p1_Right=" + p1_Right + "    # " + KeyEvent.getKeyText(p1_Right));
            writer.println("p1_Bomb=" + p1_Bomb + "    # " + KeyEvent.getKeyText(p1_Bomb));
            writer.println("p1_Item=" + p1_Item + "    # " + KeyEvent.getKeyText(p1_Item));
            writer.println();

            // Player 2 키 매핑 저장
            writer.println("# === Player 2 조작키 ===");
            writer.println("p2_Up=" + p2_Up + "    # " + KeyEvent.getKeyText(p2_Up));
            writer.println("p2_Down=" + p2_Down + "    # " + KeyEvent.getKeyText(p2_Down));
            writer.println("p2_Left=" + p2_Left + "    # " + KeyEvent.getKeyText(p2_Left));
            writer.println("p2_Right=" + p2_Right + "    # " + KeyEvent.getKeyText(p2_Right));
            writer.println("p2_Bomb=" + p2_Bomb + "    # " + KeyEvent.getKeyText(p2_Bomb));
            writer.println("p2_Item=" + p2_Item + "    # " + KeyEvent.getKeyText(p2_Item));

            System.out.println("설정 저장 완료: " + SETTINGS_FILE);
        } catch (IOException e) {
            System.err.println("설정 저장 실패: " + e.getMessage());
        }
    }
}
