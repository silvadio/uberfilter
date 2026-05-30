package com.driveq.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class RideOffer(
    val totalValue: Double,
    val bonusValue: Double,
    val passengerRating: Double,
    val passengerRatingCount: Int,
    val distanceToPickupKm: Double,
    val minutesToPickup: Int,
    val tripDurationMin: Int,
    val tripDistanceKm: Double,
    val pickupRegion: String,
    val destination: String,
    val isExclusive: Boolean
) {
    val effectiveValue: Double get() = totalValue + bonusValue

    val valuePerKm: Double get() {
        val totalKm = tripDistanceKm + distanceToPickupKm
        return if (totalKm > 0) effectiveValue / totalKm else 0.0
    }

    val valuePerMin: Double get() =
        if (tripDurationMin > 0) effectiveValue / tripDurationMin else 0.0

    val valuePerHour: Double get() {
        val totalMin = tripDurationMin + minutesToPickup
        return if (totalMin > 0) effectiveValue / totalMin * 60.0 else 0.0
    }
}

data class FilterCriteria(
    val minTotalValue: Double = 15.0,
    val minValuePerKm: Double = 2.0,
    val minValuePerHour: Double = 30.0,
    val minPassengerRating: Double = 4.5,
    val maxPickupDistanceKm: Double = 3.0,
    val maxPickupMinutes: Int = 8,
    val minTripDistanceKm: Double = 3.0,
    val maxTripDurationMin: Int = 40
)

enum class EvaluationColor { RED, YELLOW, GREEN, BLOCKED }

enum class CriteriaKey {
    TOTAL_VALUE,
    VALUE_PER_HOUR,
    VALUE_PER_KM,
    PASSENGER_RATING,
    PICKUP_DISTANCE,
    PICKUP_TIME,
    TRIP_DURATION
}

data class CriteriaResult(
    val key: CriteriaKey,
    val passed: Boolean
)

data class RideEvaluation(
    val score: Double,
    val color: EvaluationColor,
    val results: List<CriteriaResult>,
    val blockedLocation: String? = null,   // termo que bateu na lista de locais indesejados
    val blockedType: BlockedType? = null    // TEXT ou GEOFENCE
)

/** Resultado do matching de local indesejado contra o destino da corrida */
data class MatchResult(
    val term: String       // termo da lista que bateu (ex: "Flores")
)

/** Tipo de bloqueio que disparou o alerta */
enum class BlockedType { TEXT, GEOFENCE }

/** Região bloqueada por raio geográfico */
data class GeofenceEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val centerLat: Double,
    val centerLng: Double,
    val radiusKm: Double,
    val addressLabel: String   // "Rua Cap. João Manoel, Porto Novo - SG/RJ"
)

// ── Histórico de corridas avaliadas ────────────────────────────────────────────

@Entity(tableName = "ride_history")
data class RideRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val offerId: String,               // chave de deduplicação (valor|distância)
    val totalValue: Double,
    val bonusValue: Double,
    val passengerRating: Double,
    val passengerRatingCount: Int,
    val distanceToPickupKm: Double,
    val minutesToPickup: Int,
    val tripDurationMin: Int,
    val tripDistanceKm: Double,
    val pickupRegion: String,
    val destination: String,
    val isExclusive: Boolean,
    val score: Double,                 // score do RideEvaluator
    val color: EvaluationColor,        // RED / YELLOW / GREEN
    val timestamp: Long = System.currentTimeMillis()
)

// ── Estatísticas da Home ───────────────────────────────────────────────────────

data class RideStats(
    val redCount: Int,
    val yellowCount: Int,
    val greenCount: Int,
    val redPct: Int,
    val yellowPct: Int,
    val greenPct: Int
)