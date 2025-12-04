package com.winterflw.hansunghub.reservation.model

import com.winterflw.hansunghub.R

enum class FacilityType(
    val displayName: String,
    val description: String,
    val imgResId : Int,
    val detaildescription: String,
    val color: Long,
    val spaceSeqRange: IntRange
) {
    CODING_LOUNGE(
        displayName = "코딩 라운지",
        description = "IT·공학관 코딩 공간",
        imgResId = R.drawable.co_lo,
        detaildescription =
            """
📘 예약 및 이용 규칙
    세미나실:
        101~105호 → 6인용 테이블 2개 (최대 12명)
        106~113호 → 8인용 테이블 1개 (최대 8명)
    코딩라운지: TV 자유 사용, 무선 연결 지원(단, 기기 호환 제한 가능)
    위치: 공학관 A동 지하1층 (상상파크 플러스)
    시설: 세미나실(테이블·의자), 코딩라운지(TV, 무선 연결 지원)
    예약: 1일 최대 6시간 가능, 초과 신청 시 자동 취소
    이용 절차: 
        예약 후 공학관 A동 지하1층 상상파크 플러스 안내데스크 방문
        → 관리자 승인 및 출입문 개방

📞 문의
    09:00~17:00: 상상파크 플러스 안내데스크
    17:00~21:00: 연구관 지하1층 안내데스크 또는 ☎ 02-760-8006
    기타 문의: ☎ 02-760-8021

⚠️ 주의사항
    신분증 지참 및 관리자 확인 필수
            """,
        color = 0xFF6366F1, // Indigo
        spaceSeqRange = 137..148
    ),
    LIBRARY(
        displayName = "학술정보관",
        description = "도서관 열람실 및 세미나실",
        imgResId = R.drawable.hak_jeong,
        detaildescription =
            """
📘 예약 및 이용 규칙
    3층 러닝커먼스
        3F-1: 최소 3명 / 최대 7명
        3F-2: 최소 3명 / 최대 6명
    4층 사회과학자료실: 최소 5명 / 최대 11명
    5층 인문·자연과학자료실: 최소 3명 / 최대 6명
    6층 Design&IT 정보센터: 최소 3명 / 최대 6명
    시설: 테이블·의자, 화이트보드, TV / 4층은 전자칠판 구비
    1일 최대 3시간 예약 가능
    예약은 이용일 1주일 전부터 가능
    예약 취소는 전화 문의 필요
    당일 학생증 확인 및 최소 인원 점검

📞 문의
    예약 취소 및 관련 문의: 전화 문의 필요 (담당 안내데스크)
            
⚠️ 주의사항
    기자재·비품 파손 시 신청자 변상
    30분 이상 지각·미입실 시 당일 이용 제한
    동일 사유 3회 이상 발생 시 한 학기 이용 제한
        """,
        color = 0xFF10B981, // Green
        spaceSeqRange = 70..74
    ),
    SANGSANG_BASE(
        displayName = "상상베이스",
        description = "창업 지원 공간",
        imgResId = R.drawable.sang_ba,
        detaildescription =
            """
📘 예약 및 이용 규칙
    기본 이용 인원: 3~4인
    단독룸: 최소 3명 이상 이용 가능
    합실 이용:
        IB101 + IB102, IB103 + IB104 → 접이식 가벽 이동으로 합실 가능
        최소 5명 이상, 최대 8명까지 이용 가능
        단, 두 룸 모두 각각 다른 계정으로 예약 필요
    예약 시 전체 이용자의 학번 및 이름 입력 필수
    하루 최대 3시간 예약 가능
    예약 후 미사용, 최소 인원 미달 등 부정 이용 시 제재 가능

📞 문의
    간이벽 탈착 필요 시 글로벌원스톱센터 ☎ 760-8000

⚠️ 주의사항
    예약 후 미사용, 최소 인원 미달 등 규칙 위반 시 제재 가능
            """,
        color = 0xFFF59E0B, // Amber
        spaceSeqRange = 52..64
    )
}
