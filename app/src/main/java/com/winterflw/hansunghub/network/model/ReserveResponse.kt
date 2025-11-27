package com.winterflw.hansunghub.network.model

data class ReserveResponse(
    val results: List<ReserveResultItem>
)

data class ReserveResultItem(
    val time: String,
    val success: Boolean,
    val status: Int? = null,
    val text: String? = null
)
