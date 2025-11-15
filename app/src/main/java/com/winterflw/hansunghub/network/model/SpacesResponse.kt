package com.winterflw.hansunghub.network.model

data class SpacesResponse(
    val spaces: List<SpaceItem>
)

data class SpaceItem(
    val spaceSeq: Int,
    val spaceName: String
)
