package com.driveq.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.driveq.model.GeofenceEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.locationDataStore by preferencesDataStore(name = "undesired_locations")

/**
 * Persiste a lista de locais indesejados: termos textuais e regiões com raio.
 *
 * Estratégia: JSON via Gson em chaves separadas do DataStore.
 *   - "text_locations"  → JSON array de strings
 *   - "geofence_entries" → JSON array de GeofenceEntry
 */
class UndesiredLocationStore(private val context: Context) {

    companion object {
        private val TEXT_KEY      = stringPreferencesKey("text_locations")
        private val GEOFENCE_KEY  = stringPreferencesKey("geofence_entries")
        private val gson = Gson()
    }

    /** Flow reativo com a lista de termos textuais sempre atualizada */
    val textLocationsFlow: Flow<List<String>> = context.locationDataStore.data.map { prefs ->
        val raw = prefs[TEXT_KEY] ?: ""
        if (raw.isBlank()) emptyList()
        else runCatching {
            gson.fromJson<List<String>>(raw, object : TypeToken<List<String>>() {}.type)
        }.getOrDefault(emptyList()).filter { it.isNotBlank() }
    }

    /** Flow reativo com a lista de geofences sempre atualizada */
    val geofencesFlow: Flow<List<GeofenceEntry>> = context.locationDataStore.data.map { prefs ->
        val raw = prefs[GEOFENCE_KEY] ?: ""
        if (raw.isBlank()) emptyList()
        else runCatching {
            gson.fromJson<List<GeofenceEntry>>(raw, object : TypeToken<List<GeofenceEntry>>() {}.type)
        }.getOrDefault(emptyList()).map { it.withId() }
    }

    // ── Termos textuais ────────────────────────────────────────────────────────

    suspend fun addTextLocation(location: String) {
        val normalized = normalize(location)
        if (normalized.isBlank()) return

        context.locationDataStore.edit { prefs ->
            val current = readTextList(prefs[TEXT_KEY])
            if (current.any { normalize(it) == normalized }) return@edit
            val updated = current + location.trim()
            prefs[TEXT_KEY] = gson.toJson(updated)
        }
    }

    suspend fun removeTextLocation(location: String) {
        context.locationDataStore.edit { prefs ->
            val current = readTextList(prefs[TEXT_KEY])
            prefs[TEXT_KEY] = gson.toJson(current.filter { it != location })
        }
    }

    suspend fun updateTextLocation(old: String, new: String) {
        val normalized = normalize(new)
        if (normalized.isBlank()) return
        context.locationDataStore.edit { prefs ->
            val current = readTextList(prefs[TEXT_KEY])
            // Verifica duplicata excluindo o valor que está sendo editado
            if (current.any { it != old && normalize(it) == normalized }) return@edit
            prefs[TEXT_KEY] = gson.toJson(
                current.map { if (it == old) new.trim() else it }
            )
        }
    }

    // ── Geofences ──────────────────────────────────────────────────────────────

    suspend fun addGeofence(entry: GeofenceEntry) {
        context.locationDataStore.edit { prefs ->
            val current = readGeofenceList(prefs[GEOFENCE_KEY])
            prefs[GEOFENCE_KEY] = gson.toJson(current + entry)
        }
    }

    suspend fun removeGeofence(entry: GeofenceEntry) {
        context.locationDataStore.edit { prefs ->
            val current = readGeofenceList(prefs[GEOFENCE_KEY])
            prefs[GEOFENCE_KEY] = gson.toJson(
                current.filter { it.id != entry.id }
            )
        }
    }

    suspend fun updateGeofence(updated: GeofenceEntry) {
        context.locationDataStore.edit { prefs ->
            val current = readGeofenceList(prefs[GEOFENCE_KEY])
            prefs[GEOFENCE_KEY] = gson.toJson(
                current.map { if (it.id == updated.id) updated else it }
            )
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun readTextList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<String>>(raw, object : TypeToken<List<String>>() {}.type)
        }.getOrDefault(emptyList()).filter { it.isNotBlank() }
    }

    private fun readGeofenceList(raw: String?): List<GeofenceEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<GeofenceEntry>>(raw, object : TypeToken<List<GeofenceEntry>>() {}.type)
        }.getOrDefault(emptyList()).map { it.withId() }
    }

    /** Garante que a entry tenha ID estável (migração de versões antigas sem o campo) */
    private fun GeofenceEntry.withId(): GeofenceEntry {
        if (id.isNotBlank()) return this
        // ID determinístico baseado no conteúdo – garante estabilidade entre leituras
        val stableId = "geo_${centerLat}_${centerLng}_${addressLabel.hashCode()}"
        return copy(id = stableId)
    }

    private fun normalize(text: String): String {
        val stripped = java.text.Normalizer.normalize(text.trim(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return stripped.lowercase()
    }
}
