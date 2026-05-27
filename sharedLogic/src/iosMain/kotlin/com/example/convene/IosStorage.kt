package com.example.convene

import com.example.convene.storage.StorageApi

class IosStorage : StorageApi {
    private var stored: String = ""

    override fun saveEvents(json: String) {
        stored = json
    }

    override fun loadEvents(): String = stored
}