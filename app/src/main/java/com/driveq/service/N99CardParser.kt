package com.driveq.service

import android.view.accessibility.AccessibilityNodeInfo
import com.driveq.model.RideOffer

/**
 * Extrai dados do cartão de convite da 99 a partir da árvore de nós de acessibilidade.
 *
 * Diferente do cartão Uber, os dados da 99 vêm em blocos sequenciais (1º embarque, 2º embarque)
 * com campos distribuídos em nós separados, exigindo uma heurística de agrupamento por ordem.
 */
object N99CardParser : RideCardParser {

    // ── Regex patterns ────────────────────────────────────────────────────────

    // Valor: "R$ 7,30" (primeiro match)
    private val VALUE_RE = Regex("""R\$\s*([\d]+[.,][\d]{2})""")

    // Nota: "4,98" ou "4.98" (número decimal isolado, formato nota)
    private val RATING_RE = Regex("""(\d[.,]\d{2})""")

    // Contagem de corridas: "193 corridas" ou "1.234 corridas"
    private val RATING_COUNT_RE = Regex("""([\d.]+)\s*corridas""", RegexOption.IGNORE_CASE)

    // Minutos: "6 minutos" ou "6 min"
    private val MINUTES_RE = Regex("""(\d+)\s*min""", RegexOption.IGNORE_CASE)

    // Distância em km: "2,5 km" ou "2.5 km"
    private val DIST_KM_RE = Regex("""(\d+[.,]\d+?)\s*km""", RegexOption.IGNORE_CASE)

    // Distância em metros: "412 metros" ou "412 m"
    private val DIST_M_RE = Regex("""(\d+)\s*m(?!\s*in)""", RegexOption.IGNORE_CASE)

    // Endereço: começa com tipo de logradouro
    private val ADDRESS_RE = Regex(
        """^(Rua|Avenida|Av\.?|Tv\.?|Travessa|Alameda|Estrada|Rodovia|Praça|Pc\.?)\s""",
        RegexOption.IGNORE_CASE
    )

    // ── Public API ────────────────────────────────────────────────────────────

    override fun parse(root: AccessibilityNodeInfo): RideOffer? {
        val texts = mutableListOf<String>()
        collectTexts(root, texts)
        val fullText = texts.joinToString(" ")

        // Gatilho: presença de "embarque" (palavra exclusiva do cartão 99, não existe no Uber)
        if (!fullText.contains("embarque", ignoreCase = true)) return null
        // Também precisa ter valor monetário
        if (!fullText.contains("R$")) return null

        // Valor fixo (primeiro R$ encontrado)
        val totalValue = VALUE_RE.find(fullText)?.groupValues?.get(1)?.toDoubleLocale() ?: return null

        // Nota e contagem de corridas
        val rating      = RATING_RE.find(fullText)?.groupValues?.get(1)?.toDoubleLocale() ?: 0.0
        val ratingCount = RATING_COUNT_RE.find(fullText)?.groupValues?.get(1)
            ?.replace(".", "")?.toIntOrNull() ?: 0

        // ── Agrupamento por ordem: 1º embarque → pickup, 2º embarque → trip ──

        val minuteMatches = findMinutes(texts)
        val distMatches   = findDistances(texts)
        val addressMatches = findAddresses(texts)

        // 1º embarque = pickup (primeiro bloco)
        val pickupMin = minuteMatches.getOrElse(0) { 0 }
        val pickupKm  = distMatches.getOrElse(0) { 0.0 }
        val pickupAddr = addressMatches.getOrElse(0) { "" }

        // 2º embarque = viagem (segundo bloco)
        val tripMin = minuteMatches.getOrElse(1) { 0 }
        val tripKm  = distMatches.getOrElse(1) { 0.0 }
        val destination = addressMatches.getOrElse(1) { "" }

        return RideOffer(
            totalValue           = totalValue,
            bonusValue           = 0.0,
            passengerRating      = rating,
            passengerRatingCount = ratingCount,
            distanceToPickupKm   = pickupKm,
            minutesToPickup      = pickupMin,
            tripDurationMin      = tripMin,
            tripDistanceKm       = tripKm,
            pickupRegion         = pickupAddr,
            destination          = destination,
            isExclusive          = false
        )
    }

    // ── Heurísticas de agrupamento ────────────────────────────────────────────

    /** Encontra todos os valores de minutos na ordem em que aparecem nos textos */
    private fun findMinutes(texts: List<String>): List<Int> {
        return texts.mapNotNull { text ->
            MINUTES_RE.find(text)?.groupValues?.get(1)?.toIntOrNull()
        }
    }

    /**
     * Encontra todas as distâncias (km ou metros) na ordem em que aparecem.
     * Distâncias em metros são convertidas para km.
     */
    private fun findDistances(texts: List<String>): List<Double> {
        val distances = mutableListOf<Double>()

        for (text in texts) {
            // Tenta km primeiro (formato "2,5 km")
            val kmMatch = DIST_KM_RE.find(text)
            if (kmMatch != null) {
                distances += kmMatch.groupValues[1].toDoubleLocale()
                continue
            }
            // Depois metros ("412 m")
            val mMatch = DIST_M_RE.find(text)
            if (mMatch != null) {
                val meters = mMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                distances += meters / 1000.0
            }
        }

        return distances
    }

    /** Encontra todos os endereços (começam com tipo de logradouro) na ordem */
    private fun findAddresses(texts: List<String>): List<String> {
        return texts.filter { text ->
            ADDRESS_RE.containsMatchIn(text)
        }
    }
}
