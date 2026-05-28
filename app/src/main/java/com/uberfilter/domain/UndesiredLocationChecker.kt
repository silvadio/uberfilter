package com.uberfilter.domain

import com.uberfilter.model.MatchResult

/**
 * Verifica se o destino de uma corrida contém algum dos locais bloqueados
 * cadastrados pelo motorista.
 *
 * Matching: case-insensitive, sem acentos, substring.
 * Ex: destination="Rua das Flores, 22 – BH", blocked="Flores" → match
 */
object UndesiredLocationChecker {

    /**
     * Retorna [MatchResult] se o destino bater com algum termo da lista,
     * ou null se nenhum match for encontrado.
     *
     * @param destination Texto do destino extraído do cartão de corrida
     * @param blockedLocations Lista de termos bloqueados pelo motorista
     */
    fun check(destination: String, blockedLocations: List<String>): MatchResult? {
        if (destination.isBlank() || blockedLocations.isEmpty()) return null

        val normalizedDest = normalize(destination)

        for (term in blockedLocations) {
            val normalizedTerm = normalize(term)
            if (normalizedTerm.isBlank()) continue

            if (normalizedDest.contains(normalizedTerm)) {
                return MatchResult(term = term.trim())
            }
        }

        return null
    }

    /** Remove acentos, lowercase, trim */
    private fun normalize(text: String): String {
        val stripped = java.text.Normalizer.normalize(text.trim(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return stripped.lowercase()
    }
}
