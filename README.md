# Crazy Arcade UI Project

크레이지 아케이드 스타일의 게임 UI 프로젝트입니다.

## 프로젝트 구조

```
프기프 과제/
├── *.java                    # Java 소스 파일들
├── settings.properties       # 게임 설정 저장 파일
├── res/                      # 이미지 리소스
│   ├── 배찌.png
│   ├── 다오.png
│   ├── cursor.png
│   ├── lobby_bg.png
│   ├── game play.png
│   ├── start.png
│   └── creditss.png
└── sound/                    # 사운드 리소스
    ├── 노래.wav
    └── splash2.wav
```

## 필수 파일 목록

### Java 소스 파일 (11개)
1. `CrazyArcade_UI.java` - 메인 프레임
2. `SplashPanel.java` - 스플래시 화면
3. `MenuPanel.java` - 메인 메뉴
4. `LobbyPanel.java` - 대기실 (캐릭터 선택)
5. `GamePanelPlaceholder.java` - 게임 화면
6. `SettingsPanel.java` - 설정 화면
7. `GuidePanel.java` - 가이드 화면
8. `CreditsPanel.java` - 크레딧 화면
9. `GameSettings.java` - 게임 설정 관리
10. `ThemeColors.java` - 테마 색상
11. `BGMPlayer.java` - 배경음악 플레이어

### 리소스 파일
- **이미지** (res/): 배찌.png, 다오.png, cursor.png, lobby_bg.png, game play.png, start.png, creditss.png
- **사운드** (sound/): 노래.wav, splash2.wav
- **설정**: settings.properties

## 실행 방법

### 1. 컴파일
```bash
javac -encoding UTF-8 *.java
```

### 2. 실행
```bash
java CrazyArcade_UI
```

## 주요 기능

### 대기실 (Lobby)
- **캐릭터 선택**: 배찌, 다오, 랜덤 중 선택
- **우클릭**: 1P 캐릭터 선택 (빨강 테두리)
- **좌클릭**: 2P 캐릭터 선택 (파랑 테두리)
- **채팅**: 간단한 채팅 기능
- **커스텀 커서**: cursor.png 적용

### 게임 화면
- 좌측: 게임 맵
- 우측: 타이머, 캐릭터 정보, 아이템 패널
- **ESC**: 로비로 복귀

### 설정
- BGM/SFX 볼륨 조절
- 1P/2P 키 매핑 설정
- 설정 자동 저장/로드 (settings.properties)

## 시스템 요구사항
- Java 8 이상
- 해상도: 800x600 권장

## 개발 정보
- 언어: Java (Swing)
- 인코딩: UTF-8
- UI 스타일: Crazy Arcade
