package com.driveq.domain

import com.driveq.model.*

object RideEvaluator {

    private val weights = mapOf(
        CriteriaKey.TOTAL_VALUE      to 2.5,
        CriteriaKey.VALUE_PER_HOUR   to 2.5,
        CriteriaKey.VALUE_PER_KM     to 2.0,
        CriteriaKey.PASSENGER_RATING to 1.0,
        CriteriaKey.PICKUP_DISTANCE  to 1.0,
        CriteriaKey.PICKUP_TIME      to 0.5,
        CriteriaKey.TRIP_DURATION    to 0.5
    )

    fun evaluate(offer: RideOffer, criteria: FilterCriteria): RideEvaluation {
        val results = listOf(
            CriteriaResult(
                key    = CriteriaKey.TOTAL_VALUE,
                passed = offer.effectiveValue >= criteria.minTotalValue
            ),
            CriteriaResult(
                key    = CriteriaKey.VALUE_PER_HOUR,
                passed = offer.valuePerHour >= criteria.minValuePerHour
            ),
            CriteriaResult(
                key    = CriteriaKey.VALUE_PER_KM,
                passed = offer.valuePerKm >= criteria.minValuePerKm
            ),
            CriteriaResult(
                key    = CriteriaKey.PASSENGER_RATING,
                passed = offer.passengerRating >= criteria.minPassengerRating
            ),
            CriteriaResult(
                key    = CriteriaKey.PICKUP_DISTANCE,
                passed = offer.distanceToPickupKm <= criteria.maxPickupDistanceKm
            ),
            CriteriaResult(
                key    = CriteriaKey.PICKUP_TIME,
                passed = offer.minutesToPickup <= criteria.maxPickupMinutes
            ),
            CriteriaResult(
                key    = CriteriaKey.TRIP_DURATION,
                passed = offer.tripDurationMin <= criteria.maxTripDurationMin
            )
        )

        val score = results
            .filter { it.passed }
            .sumOf { weights[it.key] ?: 0.0 }

        val color = when {
            score > 8.0 -> EvaluationColor.GREEN
            score > 5.0 -> EvaluationColor.YELLOW
            else        -> EvaluationColor.RED
        }

        return RideEvaluation(score = score, color = color, results = results)
    }
}