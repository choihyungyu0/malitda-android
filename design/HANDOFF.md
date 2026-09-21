# 말잇다 · 개발 인계 패키지 v2

## 바로 열기

- [Figma — 원안 위치 와이어플로우](https://www.figma.com/design/rRl8r1ktRCzPcKl5jyu7uR?node-id=28-4)
- [입력 프레임에서 재생](https://www.figma.com/proto/rRl8r1ktRCzPcKl5jyu7uR?node-id=54-62&starting-point-node-id=54%3A62&scaling=scale-down&content-scaling=fixed)
- [핵심 말하기 흐름](https://www.figma.com/proto/rRl8r1ktRCzPcKl5jyu7uR?node-id=54-83&starting-point-node-id=54%3A83&scaling=scale-down&content-scaling=fixed)
- [버튼·마이크 컴포넌트](https://www.figma.com/design/rRl8r1ktRCzPcKl5jyu7uR?node-id=28-6)
- [입력·선택 컴포넌트](https://www.figma.com/design/rRl8r1ktRCzPcKl5jyu7uR?node-id=28-7)
- [개발 인계 안내](https://www.figma.com/design/rRl8r1ktRCzPcKl5jyu7uR?node-id=28-8)

## 이번에 마감한 범위

기존 디자이너 목업의 위치를 기준으로 버튼·선택·입력 영역 143곳을 독립 프레임으로 만들었다. 입력창 5곳과 카테고리 선택창 1곳을 포함한다. 27개 화면과 22개 확인/편집 상태, 총 49개 프레임을 연결했다. 원안 이미지는 보존했다.

- 03 개발용 목업: 원안 이미지와 동작 프레임.
- 05 원안 위치 와이어플로우: 같은 좌표의 버튼은 보라, 입력은 청록 테두리로 표시. 배경만 옅게 처리했다.
- 07·08: 버튼·아이콘 버튼·마이크·입력칸·체크박스·스위치·탭·후보 카드, 8종 26개 변형. Label을 편집할 수 있다. IconButton은 Icon 종류, 나머지는 State 상태로 구분한다.
- 09: 구현 기준과 남은 실기기 검증.
- 02는 초기 구조 설명본, 99는 수정 전 연결 보존본이다.

## 개발자가 읽을 순서

1. 개발_구현계약.md: 상태·입력 검증·승인·저장·권한·오류 규칙.
2. 프레임_좌표_입력규칙.csv 및 control-contract.json: 원본 px와 Figma 좌표, 필드·버튼 ID, 다음 화면, 입력 제한.
3. design-tokens.json 및 ui-assets: 색·서체·간격, 재사용 SVG/PNG.
4. 검수_결과.md와 브라우저_확인기록.json: 확인한 범위와 미검증 항목.

## 원안과 분리 자산

원안 20개 화면과 로고는 PDF에서 추출한 그대로다. 캐릭터 10개는 앞선 AI 배경 제거 결과로 미세한 윤곽·질감 차이가 있을 수 있다. 버튼·입력 UI는 텍스트까지 편집할 수 있도록 원안 스타일로 재구성한 Figma 네이티브 요소이며 SVG/PNG로 내보냈다. PDF에서 모든 UI를 픽셀 그대로 자동 분해한 것은 아니다. 글꼴은 사용자 승인으로 가장 가까운 Noto Sans KR을 사용한다.

## 검증

원안 좌표표 대비 143개 프레임 오차 0.001px 미만. 지정 위치 측정 자체는 경계·그림자 선택에 약 1–2 원본 px 한계가 있다. 두 페이지 모두 206개 상호작용 요소, 빠진 연결·잘못된 목적지·프레임 넘침 0. UI SVG 26개 파싱과 투명 PNG 26개 알파를 검사했다. 실제 브라우저에서 대표 동작 20개를 확인했다.

재생은 너비와 높이 맞춤(Z) 사용. 위 재생 링크에 배율을 포함했다. 전체 143개 영역을 각각 브라우저로 클릭한 것은 아니다.

## 구현 경계

Figma 입력은 정해 둔 예시 문장을 적용하는 시뮬레이션이다. 실제 키보드·STT/TTS·저장·삭제·외부 공유·APK를 구현한 것은 아니다. 저장 후 목록은 원안의 정적 예시이므로 실제 저장 결과나 사용 횟수를 나타내지 않는다. 필터·즐겨찾기·일부 설정은 연결/상태값 설계이며 모든 결과 UI가 동적으로 재구성되지는 않는다.

원안의 작은 요소를 48dp로 일괄 확대하면 14쌍이 겹친다. 터치영역_충돌_주의.json을 참고해 앱에서 행 높이·스크롤·터치 분할을 조정한다. 실기기 접근성, 저장 실패, 온디바이스 성능은 APK 단계에서 검증한다.
