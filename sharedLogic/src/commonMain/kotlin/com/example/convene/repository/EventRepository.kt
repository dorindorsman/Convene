package com.example.convene.repository

import com.example.convene.storage.StorageApi
import com.example.convene.model.Event
import kotlinx.serialization.json.Json

class EventRepository(private val storage: StorageApi) {

    fun getAllEvents(): List<Event> {
        val json = storage.loadEvents()
        return if (json.isEmpty()) emptyList()
        else Json.Default.decodeFromString(json)
    }

    fun saveEvent(event: Event) {
        val current = getAllEvents().toMutableList()
        current.removeAll { it.id == event.id }
        current.add(event)
        storage.saveEvents(Json.Default.encodeToString(current))
    }

    fun toggleFavorite(eventId: String) {
        val current = getAllEvents().toMutableList()
        val index = current.indexOfFirst { it.id == eventId }
        if (index != -1) {
            current[index] = current[index].copy(
                isFavorite = !current[index].isFavorite
            )
            storage.saveEvents(Json.Default.encodeToString(current))
        }
    }

    fun seedDemoData() {
        if (getAllEvents().isNotEmpty()) return
        listOf(
            Event(
                "1", "Opening Keynote", "Efrat Dubi", "Hall A",
                1_000_000L
            ),
            Event(
                "2", "KMP in Production", "Yael Mass", "Room 2",
                2_000_000L
            ),
            Event(
                "3", "Jetpack Compose Deep Dive", "Yakov Slushtz", "Room 3",
                3_000_000L
            )
        ).forEach { saveEvent(it) }
    }
}