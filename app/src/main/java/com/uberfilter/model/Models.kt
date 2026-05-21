package com.uberfilter.model

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

    val valuePerKm: Double get() =
        if (tripDistanceKm > 0) effectiveValue / tripDistanceKm else 0.0

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

enum class EvaluationColor { RED, YELLOW, GREEN }

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
    val results: List<CriteriaResult>
)