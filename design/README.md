# 말잇다 · 디자인 (Figma 전체 반영)

Figma **`rRl8r1ktRCzPcKl5jyu7uR`** (말잇다 · VOICE MATE | PDF 원안 · 와이어프레임 · 목업)의 전체 디자인을 이 폴더로 가져왔습니다. 2026-09-21 개발 인계본 기준. (원본 인계 안내: [HANDOFF.md](HANDOFF.md))

**전체를 한 페이지로 보기: [gallery.html](gallery.html)** (브라우저로 열기)

## 폴더

| 폴더 | 내용 |
|---|---|
| `figma/wireframes/` | **원안 위치 와이어프레임 49프레임** — 화면 27개(`S01`~`S27`) + 확인/편집 상태 22개(`O_*`). Figma에서 직접 렌더한 PNG(390×844) |
| `reference-screens/` | PDF 원안 목업 20개(`S01`~`S20`, 853×1844) — 디자이너 원본 이미지, 글자 포함 |
| `ui-assets/` | UI 컴포넌트 26변형(버튼·아이콘버튼·마이크·입력·체크박스·스위치·탭·후보 카드) SVG+PNG |
| `brand-assets/` | 캐릭터 10개 + 로고 원본(투명 PNG) |
| `previews/` | 대표 화면·컴포넌트 미리보기 |

## 문서

- `개발_구현계약.md` — 상태머신(Idle→…→Approved)·입력 검증·승인·저장·권한·오류 규칙
- `원안_위치_대응표.md` — 원안 좌표 → Figma 좌표 환산, 입력 영역 규칙
- `control-contract.json` — 143개 컨트롤의 화면·ID·종류·sourceRect·visualRect·touchRect·target·입력 제한
- `프레임_좌표_입력규칙.csv` — 위 계약의 표 형태
- `design-tokens.json` — 색·서체·간격·반경 토큰
- `figma-screen-map.json` — 화면키 → Figma 노드 ID
- `터치영역_충돌_주의.json` — 48dp 확장 시 겹치는 14쌍(행 높이·스크롤로 분리)

## 앱 구현과의 관계

`figma/wireframes/S01`~`S27`이 실제 앱 화면(`app/src/main/.../ui/screens/`, 화면 계약 S01~S27)과 1:1 대응합니다. `design-tokens.json`의 색은 앱 `ui/theme/Color.kt`에 반영돼 있고, `brand-assets`는 `app/src/main/res/drawable-nodpi/`에 캐릭터·로고로 들어가 있습니다.

## 라이선스

캐릭터·로고·화면 디자인은 팀 VOICE MATE 자산입니다. `figma/wireframes`의 배경 이미지는 디자이너 PDF 원안에서 추출한 것으로 재사용에 팀 허락이 필요합니다.
