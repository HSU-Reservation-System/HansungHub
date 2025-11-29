package com.winterflw.hansunghub.network.model

data class DisabledTimesResponse(
    val disabled: List<String>,
    val details: SpaceDetails? = null
)

data class SpaceDetails(
    val mngr: String,
    val mngrTelno: String,
    val identityCode: String
)
