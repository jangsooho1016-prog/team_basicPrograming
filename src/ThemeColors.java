import java.awt.Color;

/**
 * ========================================================
 * ThemeColors 클래스 - 공통 테마 색상 정의 (바나나 테마)
 * ========================================================
 * 
 * [클래스 설명]
 * - 게임 전체에서 사용되는 UI 색상을 한 곳에서 관리합니다.
 * - 모든 패널(화면)에서 이 색상들을 공유하여 통일된 디자인을 유지합니다.
 * - 색상을 변경하고 싶다면 이 파일만 수정하면 됩니다.
 * 
 * [바나나 테마 컨셉]
 * - 노란색(바나나) 계열을 메인 색상으로 사용
 * - 갈색(바나나 껍질/초콜릿)을 테두리와 텍스트에 사용
 * - 밝고 따뜻한 느낌의 컬러 팔레트
 * 
 * [사용 방법]
 * - 다른 클래스에서: ThemeColors.MAIN, ThemeColors.DARK 등으로 접근
 * - 예시: setBackground(ThemeColors.BG);
 * 
 * @author 팀원 공동 작업
 * @version 1.0
 */
public class ThemeColors {

    /**
     * BG (Background) - 배경색
     * RGB: (255, 250, 205) - 연한 크림색/레몬치폰
     * 사용처: 패널 배경, 전체 화면 배경
     */
    public static final Color BG = new Color(255, 250, 205);

    /**
     * MAIN - 메인 색상
     * RGB: (255, 225, 53) - 바나나 노란색
     * 사용처: 버튼 배경, 슬라이더, 주요 UI 요소
     */
    public static final Color MAIN = new Color(255, 225, 53);

    /**
     * DARK - 어두운 색상
     * RGB: (139, 69, 19) - 새들브라운 갈색
     * 사용처: 테두리, 텍스트, 그림자 효과
     */
    public static final Color DARK = new Color(139, 69, 19);

    /**
     * HIGHLIGHT - 하이라이트 색상
     * RGB: (255, 240, 150) - 밝은 노란색
     * 사용처: 마우스 오버(hover) 효과
     */
    public static final Color HIGHLIGHT = new Color(255, 240, 150);

    /**
     * ACCENT - 강조 색상
     * RGB: (255, 180, 0) - 진한 노란색/주황색
     * 사용처: 버튼 클릭 시, 선택된 상태, 강조 표시
     */
    public static final Color ACCENT = new Color(255, 180, 0);

    /**
     * TEXT - 텍스트 색상
     * RGB: (100, 50, 0) - 진한 갈색
     * 사용처: 일반 텍스트, 라벨
     */
    public static final Color TEXT = new Color(100, 50, 0);
}
