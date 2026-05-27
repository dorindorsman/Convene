package com.example.convene.storage

interface StorageApi {
    fun saveEvents(json: String)
    fun loadEvents(): String
}