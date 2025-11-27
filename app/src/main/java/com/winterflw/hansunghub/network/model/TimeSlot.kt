package com.winterflw.hansunghub.network.model

data class TimeSlot(
    val time: String,
    var isAvailable: Boolean = true,
    var isSelected: Boolean = false
)
