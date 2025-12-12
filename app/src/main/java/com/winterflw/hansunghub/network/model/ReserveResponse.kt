package com.winterflw.hansunghub.network.model

data class ReserveResponse(
    val success: Boolean,
    val times: List<String>,
    val location: String? = null,
    val status: Int? = null,
    val error_file: String? = null
)

// 기존 ReserveResultItem은 UI 표시용으로 유지
data class ReserveResultItem(
    val time: String,
    val success: Boolean,
    val status: Int? = null,
    val text: String? = null
)
