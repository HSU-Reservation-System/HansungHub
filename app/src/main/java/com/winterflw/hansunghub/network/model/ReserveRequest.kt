package com.winterflw.hansunghub.network.model

data class ReserveRequest(
    val spaceSeq: Int,
    val spaceName: String,
    val date: String,
    val timeList: List<String>,
    val tel: String,
    val email: String,
    val addItem1: String? = null,  // 상상베이스/학술정보관 추가 항목
    val addItem2: String? = null   // 상상베이스/학술정보관 추가 항목
)

