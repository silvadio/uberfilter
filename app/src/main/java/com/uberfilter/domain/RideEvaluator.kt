package com.uberfilter.domain

import com.uberfilter.model.FilterCriteria
import com.uberfilter.model.RideEvaluation
import com.uberfilter.model.RideOffer

object RideEvaluator {

    fun evaluate(offer: RideOffer, criteria: FilterCriteria): RideEvaluation {
        val issues = mutableListOf<String>()

        if (offer.effectiveValue < criteria.minTotalValue)
            issues += "Valor R${"%.2f".format(offer.effectiveValue)} < mínimo R${"%.2f".format(criteria.minTotalValue)}"

        if (offer.valuePerKm < criteria.minValuePerKm)
            issues += "R$/km ${"%.2f".format(offer.valuePerKm)} < mínimo ${"%.2f".format(criteria.minValuePerKm)}"

        if (offer.passengerRating < criteria.minPassengerRating)
            issues += "Passageiro ${"%.2f".format(offer.passengerRating)} < mínimo ${"%.2f".format(criteria.minPassengerRating)}"

        if (offer.distanceToPickupKm > criteria.maxPickupDistanceKm)
            issues += "Busca ${"%.1f".format(offer.distanceToPickupKm)} km > máximo ${"%.1f".format(criteria.maxPickupDistanceKm)} km"

        if (offer.minutesToPickup > criteria.maxPickupMinutes)
            issues += "Busca ${offer.minutesToPickup} min > máximo ${criteria.maxPickupMinutes} min"

        if (offer.tripDistanceKm < criteria.minTripDistanceKm)
            issues += "Viagem ${"%.1f".format(offer.tripDistanceKm)} km < mínimo ${"%.1f".format(criteria.minTripDistanceKm)} km"

        if (offer.tripDurationMin > criteria.maxTripDurationMin)
            issues += "Duração ${offer.tripDurationMin} min > máximo ${criteria.maxTripDurationMin} min"

        return RideEvaluation(
            isGood = issues.isEmpty(),
            reasons = issues.ifEmpty { listOf("Corrida dentro dos seus critérios ✓") }
        )
    }
}
