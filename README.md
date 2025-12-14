# HansungHub Android - 한성대학교 통합 시설 예약 앱

HansungHub는 한성대학교의 분산된 공간 예약 시스템을 하나로 통합하여, 학우들이 보다 편리하게 학교 시설을 이용할 수 있도록 돕는 프로젝트입니다.

**Note**: 이 리포지토리는 HansungHub의 Android 클라이언트 앱입니다. 백엔드 서버는 [HansungHub-Server](https://github.com/HSU-Reservation-System/BookingServer)에서 확인하실 수 있습니다.

## 🌟 프로젝트 목적

한성대학교의 공간 예약 시스템(상상베이스, 학술정보관, 코딩라운지 등)이 부서별로 파편화되어 있는 문제를 해결하고, 하나의 모바일 앱에서 모든 시설을 편리하게 예약할 수 있도록 합니다.

## ✨ 주요 기능

- **통합 로그인**: 한성대 포털 계정으로 로그인, 세션 쿠키 자동 관리
- **실시간 공간 조회**: 시설별 예약 가능 공간 및 시간대 실시간 확인
- **통합 예약**: 공간 선택 → 날짜/시간 선택 → 예약 완료
- **현대적인 UI**: 100% Jetpack Compose, Material Design 3, 한성대 브랜드 컬러(#2E5BFF)

## 🛠 기술 스택

- **Language**: Kotlin
- **UI**: Jetpack Compose (100% Compose, XML 미사용)
- **Networking**: Retrofit2 + OkHttp3 + Gson
- **Session**: OkHttp CookieJar
- **Async**: Kotlin Coroutines
- **Min SDK**: 26 (Android 8.0) / **Target SDK**: 34 (Android 14)

## 📁 파일 구조

```
app/src/main/java/com/winterflw/hansunghub/
├── login/
│   └── LoginActivity.kt                  # 로그인 화면
├── reservation/
│   ├── ReservationActivity.kt            # 예약 메인 Activity
│   ├── ReservationScreen.kt              # 시설 선택 화면
│   ├── ReservationDetailPreviewScreen.kt # 시설 상세 정보
│   ├── ReservationDetailScreen.kt        # 예약 입력 화면
│   └── model/FacilityType.kt             # 시설 타입 정의
├── network/
│   ├── RetrofitClient.kt                 # Retrofit + OkHttp 설정
│   ├── HansungApi.kt                     # API 인터페이스
│   └── model/                            # Request/Response 모델
└── ui/theme/                             # Material3 테마
```
## 실행 이미지
1. 로그인 화면
<img width="389" height="823" alt="로그인화면" src="https://github.com/user-attachments/assets/aa5a862a-f425-42e4-94cb-87e8b564b569" />

2. 메인 화면
<img width="389" height="820" alt="메인 화면" src="https://github.com/user-attachments/assets/ac515cd0-f62e-46d0-88a9-cc55c7f6bc83" />

3. 시설 프리뷰 화면
<img width="388" height="819" alt="시설프리뷰" src="https://github.com/user-attachments/assets/51351d51-db1c-472d-95d6-1a68c7ba425a" />

4. 예약 화면
<img width="397" height="823" alt="예약 화면" src="https://github.com/user-attachments/assets/a5693771-2dc0-45e0-b50a-57738b4e2776" />

5. 마이페이지
<img width="391" height="823" alt="마이페이지" src="https://github.com/user-attachments/assets/7c85a01d-c04c-4093-a6ac-dd2773f0714a" />


## 🚀 실행 방법

### 요구사항
- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17 이상
- Android SDK 34
- BookingServer레포지터리의 서버 코드를 먼저 실행시켜야 함

### 설치 및 실행
```bash
# 1. 클론
git clone https://github.com/yourusername/HansungHub-Android.git
cd HansungHub-Android

# 2. Android Studio에서 프로젝트 열기
# File → Open → 프로젝트 폴더 선택

# 4. 실행
# Run 버튼(▶) 클릭 또는 Shift + F10
```

## 📱 지원 시설

### 🖥️ 코딩라운지 (spaceSeq: 137~148)
- 위치: 공학관 A동 지하1층
- 1일 최대 6시간 예약 가능
- 문의: ☎ 02-760-8021

### 📚 학술정보관 (spaceSeq: 70~74)
- 위치: 학술정보관 3~6층
- 1일 최대 3시간 예약 가능
- 최소 인원 준수 필요

### 🎨 상상베이스 (spaceSeq: 52~64)
- 위치: 창업 지원 공간
- 1일 최대 3시간 예약 가능
- 최소 3명 이상 이용

## 🔐 주요 구현 사항

### 세션 관리
- OkHttp CookieJar로 쿠키 자동 저장/로드
- 메모리 기반 쿠키 저장소 (앱 실행 중 유지)
- 로그인 시 서버 세션 쿠키 자동 관리
