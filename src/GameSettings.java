import java.awt.event.KeyEvent;

/**
 * ========================================================
 * GameSettings 클래스 - 게임 설정값 저장
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임에서 사용되는 설정값들을 static 변수로 관리합니다.
 * - 볼륨 설정, 키 매핑 등 게임 전반의 설정을 저장합니다.
 * - 모든 변수가 static이므로 인스턴스 생성 없이 접근 가능합니다.
 * 
 * [사용 방법]
 * - 다른 클래스에서: GameSettings.bgmVolume, GameSettings.p1_Up 등으로 접근
 * - 예시: int volume = GameSettings.sfxVolume;
 * - 예시: if (keyCode == GameSettings.p1_Bomb) { ... }
 * 
 * [주의사항]
 * - 현재는 프로그램 실행 중에만 설정이 유지됩니다.
 * - 프로그램을 종료하면 설정이 초기화됩니다.
 * - (추후 파일 저장 기능 추가 필요)
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class GameSettings {

    // ============================================
    // 볼륨 설정 (0 ~ 100 범위)
    // ============================================

    /**
     * bgmVolume - 배경음악(BGM) 볼륨
     * 범위: 0(음소거) ~ 100(최대)
     * 기본값: 50 (중간)
     */
    public static int bgmVolume = 50;

    /**
     * sfxVolume - 효과음(SFX) 볼륨
     * 범위: 0(음소거) ~ 100(최대)
     * 기본값: 50 (중간)
     * 사용처: 물풍선 설치, 폭발, 아이템 획득 등의 효과음
     */
    public static int sfxVolume = 50;

    // ============================================
    // Player 1 (1P) 키 설정
    // ============================================
    // 기본 조작: W/A/S/D (이동), Shift (물풍선), Ctrl (아이템)
    // KeyEvent.VK_XXX 상수를 사용하여 키 코드 저장

    /** 1P 위쪽 이동 키 (기본: W) */
    public static int p1_Up = KeyEvent.VK_W;

    /** 1P 아래쪽 이동 키 (기본: S) */
    public static int p1_Down = KeyEvent.VK_S;

    /** 1P 왼쪽 이동 키 (기본: A) */
    public static int p1_Left = KeyEvent.VK_A;

    /** 1P 오른쪽 이동 키 (기본: D) */
    public static int p1_Right = KeyEvent.VK_D;

    /** 1P 물풍선 설치 키 (기본: Shift) */
    public static int p1_Bomb = KeyEvent.VK_SHIFT;

    /** 1P 아이템 사용 키 (기본: Ctrl) */
    public static int p1_Item = KeyEvent.VK_CONTROL;

    // ============================================
    // Player 2 (2P) 키 설정
    // ============================================
    // 기본 조작: 방향키 (이동), NumPad 1 (물풍선), NumPad 0 (아이템)
    // 2인 플레이 시 1P와 키가 겹치지 않도록 설정

    /** 2P 위쪽 이동 키 (기본: ↑ 방향키) */
    public static int p2_Up = KeyEvent.VK_UP;

    /** 2P 아래쪽 이동 키 (기본: ↓ 방향키) */
    public static int p2_Down = KeyEvent.VK_DOWN;

    /** 2P 왼쪽 이동 키 (기본: ← 방향키) */
    public static int p2_Left = KeyEvent.VK_LEFT;

    /** 2P 오른쪽 이동 키 (기본: → 방향키) */
    public static int p2_Right = KeyEvent.VK_RIGHT;

    /** 2P 물풍선 설치 키 (기본: 숫자패드 1) */
    public static int p2_Bomb = KeyEvent.VK_NUMPAD1;

    /** 2P 아이템 사용 키 (기본: 숫자패드 0) */
    public static int p2_Item = KeyEvent.VK_NUMPAD0;
}
