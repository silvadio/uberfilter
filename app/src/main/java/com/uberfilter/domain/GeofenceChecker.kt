package com.uberfilter.domain

import android.content.Context
import android.location.Geocoder
import com.uberfilter.model.GeofenceEntry
import com.uberfilter.model.MatchResult
import kotlin.math.*

/**
 * Verifica se o destino da corrida está dentro do raio de alguma região bloqueada.
 * Usa Android Geocoder (gratuito) para converter endereço em coordenadas.
 */
object GeofenceChecker {

    /**
     * Verifica se o destino está dentro de algum geofence.
     * Retorna o match se estiver, ou null.
     *
     * @param destination Endereço do destino extraído do cartão
     * @param geofences Lista de regiões bloqueadas com raio
     * @param context Contexto para o Geocoder
     */
    fun check(
        destination: String,
        geofences: List<GeofenceEntry>,
        context: Context
    ): MatchResult? {
        if (destination.isBlank() || geofences.isEmpty()) return null

        // Geocoding do destino → coordenadas
        val destCoords = geocode(destination, context) ?: return null

        for (geofence in geofences) {
            val distanceKm = haversineKm(
                lat1 = geofence.centerLat,
                lng1 = geofence.centerLng,
                lat2 = destCoords.first,
                lng2 = destCoords.second
            )

            if (distanceKm <= geofence.radiusKm) {
                return MatchResult(
                    term = geofence.addressLabel
                )
            }
        }

        return null
    }

    /** Haversine formula — distância em km entre dois pontos */
    fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    /** Converte endereço em (lat, lng) via Android Geocoder */
    private fun geocode(address: String, context: Context): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context)
            val results = geocoder.getFromLocationName(address, 1)
            if (results.isNullOrEmpty()) null
            else Pair(results[0].latitude, results[0].longitude)
        } catch (_: Exception) {
            null
        }
    }
}
