## 프로젝트 개요
- 해외 여행 시, 사진 촬영을 통해 메뉴판 번역과 원화 변환 등록한 호/불호 음식에 대한 하이라이팅 처리를 해주는 어플리케이션

## 기획 목적

- 해외 여행 시, 구글 렌즈와 빅스비 비전을 많이 사용함. 해당 기능같은 경우 영어가 가장 번역이 잘 되고, 아시아권 언어 번역 시 자연스러운 문장이나 단어로 이어지지 않고, 직해하는 경우가 많아 어려움을 겪음.
    - 개인 경험: 동남아 로컬 맛집 같은 경우 영어 메뉴판 지원이 안되는 경우가 있어 메뉴 선택에 어려움을 겪는 경우가 있었음.
- 또한, 부모님 세대의 경우 고수와 같은 식자재에 대한 불호성이 강하고, 요즘 젊은 사람들 사이에서도 오이, 민트 등 특정 식자재에 대한 호/불호 이슈가 있음.
- 즉각적인 원화 변환으로 보다 현명한 소비를 권장하고자 함.
- 따라서, 아시아권 언어 친화적인 메뉴 번역 기능과 직관적인 원화 변환을 메인으로 설정하고, 호/불호 식자재에 대한 필터링 기능을 제공하여 해외 여행에 있어 편의성을 도모하고자 함.

## 주요 기능

#### **번역**

- 촬영한 메뉴판 사진의 언어를 감지하여 한국어로 번역
- 등록한 호/불호 음식을 감지하여 하이라이팅 처리

#### **원화 변환**

- 실시간으로 환율 데이터를 가져와 원화로 바꿔줌
- 또는, 환전 시 환율을 등록하면 해당 금액을 기준으로 원화로 변환

## 🗓️ 개발 기간
- 2023.09 ~ 2024.02

## 💻 Skills

- Language
    - Kotlin
    - PHP
    - Python
    - PlantUML
- **Design Patterns**
    - MVVM
- **서버 환경**
    - AWS EC2
    - Apache2
    - Mysql
    - Flask
- **API**
    - Retrofit2
    - Kakao Pay API
    - Kakao Login API
    - Google Login API
    - Firebase ML kit
    - Google Cloud Translator
- **Collaboration**
    - Github
    - Notion
    - Slack
    - Figma

## 🔨 IDE
- AndroidStudio
- Visual Studio Code
- MySQL Workbench
- SELAB
- PostMan

## 💡DEMO
<table>
    <tr>
    <th>로그인</th>
    <th>회원가입</th>
    <th>아이디 찾기</th>
    <th>비밀번호 찾기</th>
  </tr>
  <tr>
    <td><img width="360" alt="스크린샷 2024-02-23 오후 6 29 25" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/a360744c-2c10-4164-91c6-63ec426bde9c"></td>
    <td><img width="360" alt="회원가입" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/50bc24a7-ba0b-4a55-b424-c564d8c6f875"></td>
    <td><img width="360" alt="스크린샷 2024-02-23 오후 6 29 33" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/914a3f9e-9028-4442-bf73-246ab50b59e4"></td>
    <td><img width="360" alt="스크린샷 2024-02-23 오후 6 49 23" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/6f02ae7f-ed67-4596-9bd8-c81847b30c04"></td>
  </tr>
  <tr>
    <th>음식 등록 화면</th>
    <th>번역 국가 선택 화면</th>
    <th>환율 정보 확인 화면</th>
    <th>프로필 화면 </th>
  </tr>
  <tr>
    <td><img width="300" alt="스크린샷 2024-02-23 오후 6 10 42" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/e6646cd6-4dfd-4399-98d0-28ccc9438e43"></td>
    <td><img width="300" alt="스크린샷 2024-02-23 오후 6 09 22" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/8aca377b-eac0-4fd0-af5e-8d42c4a44dbb"></td>
    <td><img width="300" alt="스크린샷 2024-02-23 오후 6 13 27" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/90d988b7-ed17-44e4-865a-07386def80e1"></td>
    <td><img width="360" alt="스크린샷 2024-02-23 오후 6 29 09" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/014026ae-c72c-46b0-b1be-df31d71ce57a"></td>
  </tr>
  <tr>
    <th> 회원 탈퇴 화면 </th>
    <th> 메뉴판 번역 화면 </th>
      <th>티켓 구매 화면</th>
      <th>이용약관 화면 </th>
  </tr>
    <tr>
        <td><img width="360" alt="스크린샷 2024-02-23 오후 6 48 42" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/807c3fbe-d8b0-4a8f-9c7b-d9d7035e287d"></td>
        <td><img width="360" alt "번역 화면"src="https://github-production-user-asset-6210df.s3.amazonaws.com/102309691/307575361-76eb4a57-a3b8-46df-adb8-a8a5a5cb527f.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20240225%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240225T075141Z&X-Amz-Expires=300&X-Amz-Signature=5ef345760981d8ae17da7360235d333b64552974f9c10db09e2212a8a448db54&X-Amz-SignedHeaders=host&actor_id=102309691&key_id=0&repo_id=709688627"></td>
        <td><img width="360" alt="스크린샷 2024-02-23 오후 6 29 01" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/b917af81-cfe2-42e2-b75d-50302334d285"></td>
        <td><img width="360" alt="스크린샷 2024-02-23 오후 6 29 41" src="https://github.com/stellaArtois-yam/Menupop/assets/102309691/53d970d3-7196-4da5-ae55-cab63b7c5af6"></td>
    </tr>
</table>



