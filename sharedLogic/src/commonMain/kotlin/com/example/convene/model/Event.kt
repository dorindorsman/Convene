package com.example.convene.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val title: String,
    val speaker: String,
    val room: String,
    val timeMs: Long,
    val isFavorite: Boolean = false
)