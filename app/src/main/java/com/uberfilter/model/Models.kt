package com.uberfilter.model

/**
 * Dados extraídos do cartão de convite da Uber.
 */
data class RideOffer(
    val totalValue: Double,          // R$ 19,63
    val bonusValue: Double,          // R$ 3,75 (adicional incluído)
    val passengerRating: Double,     // 4,94
    val passengerRatingCount: Int,   // 280
    val distanceToPickupKm: Double,  // 1,3 km
    val minutesToPickup: Int,        // 5 min
    val tripDurationMin: Int,        // 23 min
    val tripDistanceKm: Double,      // 7,1 km
    val pickupRegion: String,        // "Região Centro Sul"
    val destination: String,         // "Rua Fósforo, 22 – BH"
    val isExclusive: Boolean         // selo "Exclusiva"
) {
    /** Valor total real (corrida + bônus) */
    val effectiveValue: Double get() = totalValue + bonusValue

    /** Receita por km rodado */
    val valuePerKm: Double get() =
        if (tripDistanceKm > 0) effectiveValue / tripDistanceKm else 0.0

    /** Receita por minuto */
    val valuePerMin: Double get() =
        if (tripDurationMin > 0) effectiveValue / tripDurationMin else 0.0
}

/**
 * Critérios configuráveis pelo motorista.
 * Todos os limites são MÍNIMOS (ou MÁXIMOS onde indicado).
 */
data class FilterCriteria(
    // Valor
    val minTotalValue: Double = 15.0,          // valor mínimo da corrida (R$)
    val minValuePerKm: Double = 2.0,           // receita mínima por km (R$/km)

    // Passageiro
    val minPassengerRating: Double = 4.5,      // avaliação mínima do passageiro

    // Distância até o passageiro
    val maxPickupDistanceKm: Double = 3.0,     // distância máxima para buscar (km)
    val maxPickupMinutes: Int = 8,             // tempo máximo para buscar (min)

    // Viagem
    val minTripDistanceKm: Double = 3.0,       // distância mínima da viagem (km)
    val maxTripDurationMin: Int = 40           // duração máxima da viagem (min)
)

/**
 * Resultado da avaliação de uma corrida.
 */
data class RideEvaluation(
    val isGood: Boolean,
    val reasons: List<String>   // motivos que reprovaram ou aprovaram
)
