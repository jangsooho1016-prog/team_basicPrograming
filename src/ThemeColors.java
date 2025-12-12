import java.awt.Color;

/**
 * ========================================================
 * [핵심] 공통 테마 색상 (Theme Colors)
 * ========================================================
 * 어플리케이션 전체에서 사용되는 색상 팔레트입니다.
 * 크레이지 아케이드 스타일의 파란색/오렌지 계열 색상을 정의합니다.
 * 유지보수를 위해 색상 코드를 이곳에서 한 번에 관리합니다.
 */
public class ThemeColors {
    // 배경색 (연한 하늘색)
    public static final Color BG = new Color(230, 245, 255);

    // 메인 색상 (밝은 파란색 - 크레이지 아케이드 UI 기본색)
    public static final Color MAIN = new Color(80, 160, 255);

    // 어두운 색상 (진한 파란색 - 테두리/강조)
    public static final Color DARK = new Color(40, 80, 160);

    // 하이라이트 색상 (연한 파란색 - 호버 효과)
    public static final Color HIGHLIGHT = new Color(140, 200, 255);

    // 강조 색상 (오렌지 - 버튼 클릭/활성화)
    public static final Color ACCENT = new Color(255, 150, 50);

    // 텍스트 색상 (흰색/라이트)
    public static final Color TEXT = new Color(255, 255, 255);

    // 추가: 보조 색상들
    public static final Color ORANGE = new Color(255, 180, 50); // 오렌지 버튼
    public static final Color GREEN = new Color(100, 200, 100); // 시작 버튼
    public static final Color RED = new Color(220, 80, 80); // 1P 색상
    public static final Color BLUE_DARK = new Color(30, 100, 180); // 게임 배경
}
