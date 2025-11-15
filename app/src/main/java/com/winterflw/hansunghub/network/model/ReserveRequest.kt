package com.winterflw.hansunghub.network.model

data class ReserveRequest(
    val spaceSeq: Int,
    val spaceName: String,
    val date: String,
    val timeList: List<String>,   // ★ 여러 시간 전송
    val tel: String,
    val email: String
)

