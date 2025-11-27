package com.winterflw.hansunghub.network.model

data class LoginResponse(
    val success: Boolean,
    val sessionId: String?
)
