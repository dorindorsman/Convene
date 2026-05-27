package com.example.convene.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class AndroidStorage(context: Context) : StorageApi {

    private val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs = EncryptedSharedPreferences.create(
        "convene_secure_prefs",
        keyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveEvents(json: String) {
        prefs.edit().putString("events_data", json).apply()
    }

    override fun loadEvents(): String {
        return prefs.getString("events_data", "") ?: ""
    }
}