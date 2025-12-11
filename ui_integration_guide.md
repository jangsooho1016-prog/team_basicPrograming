# UI 담당자 가이드 :art:

UI/메인 통합 담당자로서 **지금 당장 할 수 있는 일**과 **팀원들에게 코드를 받았을 때 해야 할 일**을 정리했습니다.

---

## 1. 지금 당장 할 수 있는 일 (현재 코드 기반) :checkered_flag:

현재 코드는 `GamePanelPlaceholder.java` 안에 임시 로직(좌표 이동, 충돌 등)이 포함되어 있습니다. 이 상태에서 UI 완성도를 높일 수 있습니다.

### :one: 리소스 퀄리티 업그레이드
- **이미지 교체**: `res` 폴더 안의 `배찌.png`, `다오.png`, `map2.png` 등을 더 고화질이나 예쁜 이미지로 바꾸면 즉시 게임이 달라 보입니다.
- **사운드 추가**: `sound` 폴더에 `아이템획득.wav`, `물풍선놓기.wav` 등을 미리 준비해두고 코드에 `BGMPlayer`를 이용해 소리 나게 할 수 있습니다.

### :two: UI 디테일 다듬기
- **`LobbyPanel.java`**: 채팅창 글씨 크기나 색상, 캐릭터 선택 카드의 테두리 디자인 등을 더 예쁘게 수정해 보세요.
- **`GamePanelPlaceholder.java`**: 우측 '상태창'(`drawPlayerBox`) 디자인을 개선하거나, 남은 시간(`drawTimer`) 글씨체를 멋지게 바꿔보세요.

### :three: 주석 읽고 전체 흐름 파악하기
- 제가 달아드린 한글 주석을 쭉 읽어보세요. 특히 **`updateGame()` (로직)**과 **`paintComponent()` (그리기)**가 어떻게 연결되는지 이해하면 나중에 합칠 때 아주 편합니다.

---

## 2. 팀원들에게 파일을 받으면 할 일 (코드 통합) :handshake:

팀원들이 `Map.java`, `Player.java`, `Item.java`, `Bomb.java` 등을 완성해서 보내주면, **`GamePanelPlaceholder.java`를 수정해서 연결**해야 합니다.

### :file_folder: 1단계: 파일 넣기
1. 받은 `.java` 파일들을 `src` 폴더에 넣습니다.
2. 기존에 있던 파일과 이름이 겹치면 (내용 확인 후) 덮어씌우거나, 팀원 코드 내용을 복사해서 내 파일에 붙여넣습니다.

### :hammer_and_wrench: 2단계: GamePanelPlaceholder.java 수정하기 (핵심!)

이 파일이 **"조립 설명서"** 역할을 합니다. 기존의 '가짜 로직'을 지우고 '진짜 팀원 코드'로 교체해야 합니다.

#### (1) 변수 교체 (필드 선언부)
```java
// [기존] 단순 변수
// private int p1X, p1Y; 

// [변경] 팀원이 만든 클래스 사용
private Player player1;
private Player player2;
private Map realMap;
```

#### (2) 초기화 (생성자)
```java
// [기존] 변수 초기화
// p1X = 60; p1Y = 60;
// gameMap = new Map("image.png");

// [변경] 진짜 객체 생성
player1 = new Player("배찌", ...);
player2 = new Player("다오", ...);
realMap = new Map(); // 팀원의 맵 로딩 로직 호출
```

#### (3) 로직 연결 (updateGame 메서드)
```java
// [기존] 여기서 직접 계산
// if (key == UP) y -= 5;
// if (isColliding(...)) ...

// [변경] 팀원 객체에게 "일 해!"라고 명령
player1.move(); // 플레이어 내부에서 이동/충돌 계산하도록
player1.checkCollision(realMap); 
```

#### (4) 화면 그리기 (paintComponent 메서드)
```java
// [기존] 여기서 직접 그리기
// g.drawImage(img, p1X, p1Y, ...);

// [변경] 팀원 객체에게 "그려!"라고 명령
realMap.draw(g);   // 맵아, 네가 알아서 그려
player1.draw(g);   // 플레이어1아, 네 모습 그려
player2.draw(g);   // 플레이어2아, 네 모습 그려
```

---

## 결론 :bulb:

1.  **지금은**: 주석을 보며 흐름을 익히고, 이미지/소리를 더 멋지게 만드세요.
2.  **나중엔**: `GamePanelPlaceholder` 안에 있는 **임시 변수들(`p1X`, `tiles` 등)**을 지우고, **팀원이 만든 클래스(`Player`, `Map`)**로 갈아끼우는 "조립" 작업을 하시면 됩니다.

제가 작성해둔 주석 중에 **`[핵심]`** 이라고 표시된 부분들이 바로 나중에 교체해야 할 주요 지점들입니다! 화이팅!
