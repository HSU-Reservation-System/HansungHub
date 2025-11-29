package com.winterflw.hansunghub.reservation.model

enum class FacilityType(
    val displayName: String,
    val description: String,
    val color: Long,
    val spaceSeqRange: IntRange
) {
    CODING_LOUNGE(
        displayName = "코딩 라운지",
        description = "IT·공학관 코딩 공간",
        color = 0xFF6366F1, // Indigo
        spaceSeqRange = 137..148
    ),
    LIBRARY(
        displayName = "학술정보관",
        description = "도서관 열람실 및 세미나실",
        color = 0xFF10B981, // Green
        spaceSeqRange = 70..74
    ),
    SANGSANG_BASE(
        displayName = "상상베이스",
        description = "창업 지원 공간",
        color = 0xFFF59E0B, // Amber
        spaceSeqRange = 52..64
    )
}
