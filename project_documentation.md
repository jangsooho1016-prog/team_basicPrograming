# 🎮 Water Bomb Man - 프로젝트 설명서

## 1. 프로젝트 개요

**Water Bomb Man**은 Java Swing을 활용한 크레이지 아케이드 스타일의 게임 UI 프로토타입입니다.

### 개발 환경
- **언어**: Java (JDK 24)
- **GUI 프레임워크**: Java Swing
- **사용 라이브러리**: Java 표준 라이브러리만 사용 (외부 라이브러리 없음)

---

## 2. 파일 구조

```
team_basicPrograming/
├── src/                          # 소스 코드
│   ├── CrazyArcade_UI.java       # 메인 프레임 (진입점)
│   ├── MenuPanel.java            # 메인 메뉴 화면
│   ├── LobbyPanel.java           # 로비 화면 (캐릭터/맵 선택)
│   ├── GamePanelPlaceholder.java # 게임 화면
│   ├── GuidePanel.java           # 가이드 화면 (조작법)
│   ├── SettingsPanel.java        # 설정 화면
│   ├── CreditsPanel.java         # 크레딧 화면
│   ├── ThemeColors.java          # 테마 색상 정의
│   ├── BGMPlayer.java            # BGM 재생
│   └── GameSettings.java         # 설정 저장/로드
├── image/                        # 이미지 리소스
├── res/                          # 캐릭터, GIF 등
└── sound/                        # 배경음악
```

---

## 3. 화면 흐름 (CardLayout)

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  MenuPanel  │───▶│ LobbyPanel  │───▶│ GamePanel   │
│  (메인 메뉴) │    │  (로비)     │    │  (게임)     │
└─────────────┘    └─────────────┘    └─────────────┘
       │                                     │
       ├──────────▶ GuidePanel (가이드)      │
       │                                     │
       ├──────────▶ SettingsPanel (설정)     │
       │                                     │
       └──────────▶ CreditsPanel (크레딧)    │
                                             │
                   ESC 또는 나가기 버튼 ◀────┘
```

**CardLayout 사용**: `CrazyArcade_UI.java`에서 CardLayout을 사용하여 화면 전환을 관리합니다.

---

## 4. 핵심 파일 설명

### 4.1 CrazyArcade_UI.java (메인 프레임)

**역할**: 프로그램의 진입점이자 모든 화면을 관리하는 컨테이너

```java
// 화면 전환을 위한 상수 정의
public static final String PANEL_MENU = "MENU";
public static final String PANEL_LOBBY = "LOBBY";
public static final String PANEL_GAME = "GAME";
// ...

// CardLayout으로 화면 전환
public void showPanel(String panelName) {
    cardLayout.show(mainContainer, panelName);
}
```

**작동 원리**:
1. `JFrame`을 생성하고 크기를 800x600으로 설정
2. `CardLayout`을 사용하여 여러 패널을 하나의 컨테이너에 추가
3. `showPanel()` 메서드로 화면 전환

---

### 4.2 LobbyPanel.java (로비 화면)

**역할**: 캐릭터와 맵을 선택하는 대기실 화면

#### 레이아웃 구조
```
┌──────────────────┬────────────────────┐
│  1P 캐릭터 정보   │    캐릭터 선택     │
│  [이미지][능력치] │  [배찌][다오][랜덤] │
├──────────────────┼────────────────────┤
│  2P 캐릭터 정보   │      맵 선택       │
│  [이미지][능력치] │  [맵1] [맵2]       │
├──────────────────┤  [게임 시작]       │
│     채팅창       │  [메인으로]        │
│                  │                    │
└──────────────────┴────────────────────┘
```

#### 캐릭터 선택 로직
```java
// 마우스 클릭 이벤트로 캐릭터 선택
addMouseListener(new MouseAdapter() {
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            // 우클릭 → 1P 캐릭터 선택
            selected1P = characterName;
        } else {
            // 좌클릭 → 2P 캐릭터 선택
            selected2P = characterName;
        }
        updateSelectionUI();  // UI 갱신
    }
});
```

#### 능력치 게이지 그리기
```java
private void drawStatBar(Graphics2D g2, String label, int x, int y, 
                         int width, int height, int value, Color color) {
    // 1. 라벨 텍스트 그리기
    g2.drawString(label, x, y);
    
    // 2. 8칸의 게이지 셀 그리기
    for (int i = 0; i < 8; i++) {
        if (i < value) {
            g2.setColor(color);      // 채워진 칸
        } else {
            g2.setColor(Color.GRAY); // 빈 칸
        }
        g2.fillRect(cellX, y, cellWidth, height);
    }
}
```

---

### 4.3 GamePanelPlaceholder.java (게임 화면)

**역할**: 실제 게임이 진행되는 화면

#### 레이아웃 구조
```
┌─────────────────────┬───────────┐
│                     │ 1P 캐릭터 │
│                     ├───────────┤
│                     │ 1P 아이템 │
│   맵 + 게임 화면    ├───────────┤
│    (570x570)        │ 2P 캐릭터 │
│                     ├───────────┤
│                     │ 2P 아이템 │
│                     ├───────────┤
│                     │  나가기   │
└─────────────────────┴───────────┘
```

#### ESC 키로 나가기
```java
addKeyListener(new KeyAdapter() {
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            mainFrame.showPanel(CrazyArcade_UI.PANEL_LOBBY);
        }
    }
});
```

---

### 4.4 GuidePanel.java (가이드 화면)

**역할**: 게임 조작법을 GIF 애니메이션과 함께 안내

#### GIF 애니메이션 표시
```java
// ImageIcon으로 GIF 자동 재생
String gifPath = "res/상하.gif";
ImageIcon gifIcon = new ImageIcon(gifPath);
JLabel gifLabel = new JLabel(gifIcon);  // GIF 자동 재생됨!
```

#### 스크롤 패널
```java
JScrollPane scrollPane = new JScrollPane(contentPanel);
scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
scrollPane.getVerticalScrollBar().setUnitIncrement(25);  // 스크롤 속도
```

---

### 4.5 BGMPlayer.java (배경음악)

**역할**: WAV 파일로 배경음악 재생

```java
// WAV 파일 재생
AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
Clip clip = AudioSystem.getClip();
clip.open(audioStream);
clip.loop(Clip.LOOP_CONTINUOUSLY);  // 무한 반복
clip.start();
```

---

### 4.6 GameSettings.java (설정 저장)

**역할**: 볼륨, 키 설정 등을 파일로 저장/로드

```java
// Properties로 설정 저장
Properties props = new Properties();
props.setProperty("bgmVolume", String.valueOf(bgmVolume));
props.store(new FileOutputStream("settings.properties"), "Game Settings");

// 설정 로드
props.load(new FileInputStream("settings.properties"));
bgmVolume = Integer.parseInt(props.getProperty("bgmVolume", "50"));
```

---

## 5. 수업 내용 적용 부분

| 수업 주제 | 적용 위치 | 설명 |
|-----------|-----------|------|
| **JFrame/JPanel** | 모든 파일 | 기본 GUI 구조 |
| **CardLayout** | CrazyArcade_UI.java | 화면 전환 |
| **이벤트 처리** | LobbyPanel.java, GamePanelPlaceholder.java | 마우스/키보드 입력 |
| **더블 버퍼링** | 모든 Panel | paintComponent 오버라이드 |
| **사운드 재생** | BGMPlayer.java | AudioClip 사용 |
| **파일 저장** | GameSettings.java | Properties 사용 |
| **이미지 로드** | LobbyPanel.java, GamePanelPlaceholder.java | ImageIO 사용 |

---

## 6. 조작법

### 메인 메뉴
- **마우스 클릭**: 버튼 선택

### 로비
- **우클릭**: 1P 캐릭터 선택
- **좌클릭**: 2P 캐릭터 선택

### 게임 (예정)
- **1P 이동**: W, A, S, D
- **2P 이동**: ↑, ←, ↓, →
- **1P 폭탄**: Shift
- **2P 폭탄**: NumPad 1
- **1P 아이템**: Ctrl
- **2P 아이템**: NumPad 0
- **ESC**: 로비로 돌아가기

---

## 7. 팀원 역할

| 담당 | 역할 |
|------|------|
| UI 담당 | 화면 디자인 및 구현 |
| 캐릭터 담당 | 캐릭터 로직 |
| 맵 담당 | 맵 구현 |
| 아이템 담당 | 아이템 시스템 |

---

## 8. 실행 방법

```bash
# 컴파일
javac -d out src/*.java

# 실행
java -cp out CrazyArcade_UI
```

또는 IDE에서 `CrazyArcade_UI.java`의 `main` 메서드를 실행하세요.

---

*작성일: 2024-12-11*
