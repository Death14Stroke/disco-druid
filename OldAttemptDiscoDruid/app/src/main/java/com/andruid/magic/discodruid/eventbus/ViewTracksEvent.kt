package com.andruid.magic.discodruid.eventbus

data class ViewTracksEvent(
    val mode: Int,
    val query: String
)