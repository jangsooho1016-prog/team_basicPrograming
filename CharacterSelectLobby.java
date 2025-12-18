// package your.package.name;  // 패키지 쓰고 있다면 여기에 맞게 맞춰줄 것!

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CharacterSelectLobby extends JFrame {

    private JComboBox<String> p1Combo;
    private JComboBox<String> p2Combo;

    public CharacterSelectLobby() {
        setTitle("캐릭터 선택 로비");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 상단 제목
        JLabel titleLabel = new JLabel("캐릭터 선택", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // 중앙에 1P / 2P 선택 영역
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1P 선택
        JLabel p1Label = new JLabel("1P 캐릭터 (파란색):");
        p1Combo = new JComboBox<>(new String[] { "배찌 (BAZZI)", "디지니 (DIZNI)" });

        // 2P 선택
        JLabel p2Label = new JLabel("2P 캐릭터 (빨간색):");
        p2Combo = new JComboBox<>(new String[] { "배찌 (BAZZI)", "디지니 (DIZNI)" });

        centerPanel.add(p1Label);
        centerPanel.add(p1Combo);
        centerPanel.add(p2Label);
        centerPanel.add(p2Combo);

        add(centerPanel, BorderLayout.CENTER);

        // 하단 버튼 영역
        JPanel bottomPanel = new JPanel();
        JButton startButton = new JButton("게임 시작");
        JButton exitButton = new JButton("종료");

        bottomPanel.add(startButton);
        bottomPanel.add(exitButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // 버튼 이벤트
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    // "게임 시작" 눌렀을 때 실행되는 메서드
    private void startGame() {
        CharacterType p1Type = convertToCharacterType((String) p1Combo.getSelectedItem());
        CharacterType p2Type = convertToCharacterType((String) p2Combo.getSelectedItem());

        // ✅ 1P 파란색 / 2P 빨간색은 CharacterOnlyTest 안에서 이미 고정해 둠
        new CharacterOnlyTest(p1Type, p2Type);

        // 로비 창은 닫기
        dispose();
    }

    // 콤보박스 텍스트 → CharacterType enum으로 변환
    private CharacterType convertToCharacterType(String selected) {
        if (selected == null) return CharacterType.BAZZI;

        if (selected.contains("BAZZI") || selected.contains("배찌")) {
            return CharacterType.BAZZI;
        } else if (selected.contains("DIZNI") || selected.contains("디지니")) {
            return CharacterType.DIZNI;
        }

        // 기본값
        return CharacterType.BAZZI;
    }

    // 프로그램 시작 지점
    public static void main(String[] args) {
        // 로비부터 시작
        SwingUtilities.invokeLater(CharacterSelectLobby::new);
    }
}