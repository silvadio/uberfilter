package com.uberfilter.service

import android.view.accessibility.AccessibilityNodeInfo
import com.uberfilter.model.RideOffer

/**
 * Extrai dados do cartão de convite da Uber a partir da árvore de nós de acessibilidade.
 *
 * Estratégia: varrer todos os nós e aplicar regex para identificar cada campo.
 * Isso é resiliente a mudanças de layout, pois não depende de IDs de view fixos.
 */
object UberCardParser : RideCardParser {

    // ── Regex patterns ────────────────────────────────────────────────────────

    // Valor principal: "R$ 19,63" ou "R$19,63"
    private val MAIN_VALUE_RE = Regex("""R\$\s*([\d]+[.,][\d]{2})""")

    // Bônus/adicional: "+R$ 3,75" ou "+ R$ 3,75 incluído"
    private val BONUS_RE = Regex("""\+\s*R\$\s*([\d]+[.,][\d]{2})""")

    // Avaliação: "4,94 (280)" ou "4.94 (280)"
    private val RATING_RE = Regex("""(\d[.,]\d{2})\s*\((\d+)\)""")

    // Distância até passageiro: "1,3 km" precedido de contexto de tempo "5 minutos"
    private val PICKUP_RE = Regex("""(\d+)\s*minutos?\s*\(?(\d+[.,]\d)\s*km\)?""", RegexOption.IGNORE_CASE)

    // Duração da viagem: "Viagem de 23 minutos (7,1 km)"
    private val TRIP_RE = Regex("""viagem\s+de\s+(\d+)\s*minutos?\s*\(?(\d+[.,]\d)\s*km\)?""", RegexOption.IGNORE_CASE)

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Retorna um RideOffer se conseguir extrair dados suficientes, null caso contrário.
     */
    override fun parse(root: AccessibilityNodeInfo): RideOffer? {
        // Coleta todo o texto visível da árvore
        val texts = mutableListOf<String>()
        collectTexts(root, texts)
        val fullText = texts.joinToString(" ")

        // Detecta se é o cartão da Uber (deve conter R$ e pelo menos "minutos")
        if (!fullText.contains("R$") || !fullText.contains("minutos", ignoreCase = true)) return null

        val mainValue    = MAIN_VALUE_RE.find(fullText)?.groupValues?.get(1)?.toDoubleLocale() ?: return null
        val bonusValue   = BONUS_RE.find(fullText)?.groupValues?.get(1)?.toDoubleLocale() ?: 0.0
        val ratingMatch  = RATING_RE.find(fullText)
        val rating       = ratingMatch?.groupValues?.get(1)?.toDoubleLocale() ?: 0.0
        val ratingCount  = ratingMatch?.groupValues?.get(2)?.toIntOrNull() ?: 0
        val pickupMatch  = PICKUP_RE.find(fullText)
        val pickupMin    = pickupMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val pickupKm     = pickupMatch?.groupValues?.get(2)?.toDoubleLocale() ?: 0.0
        val tripMatch    = TRIP_RE.find(fullText)
        val tripMin      = tripMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val tripKm       = tripMatch?.groupValues?.get(2)?.toDoubleLocale() ?: 0.0
        val isExclusive  = fullText.contains("exclusiv", ignoreCase = true)

        // Tenta extrair região e destino pelo índice dos textos
        val region      = extractRegion(texts)
        val destination = extractDestination(texts)

        return RideOffer(
            totalValue          = mainValue,
            bonusValue          = bonusValue,
            passengerRating     = rating,
            passengerRatingCount = ratingCount,
            distanceToPickupKm  = pickupKm,
            minutesToPickup     = pickupMin,
            tripDurationMin     = tripMin,
            tripDistanceKm      = tripKm,
            pickupRegion        = region,
            destination         = destination,
            isExclusive         = isExclusive
        )
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Procura por nome de região/bairro (linha após o endereço de embarque) */
    private fun extractRegion(texts: List<String>): String {
        // Heurística: texto que contém "Região" ou está logo após "Rua"/"Av"
        return texts.firstOrNull { it.contains("Região", ignoreCase = true) }
            ?: texts.firstOrNull { it.matches(Regex(".*\\bRegião\\b.*", RegexOption.IGNORE_CASE)) }
            ?: ""
    }

    /**
     * Extrai o endereço completo de destino.
     *
     * Estratégia: localiza o bloco de textos que compõem o endereço de destino
     * (identificado por CEP ou padrão "Cidade – UF") e concatena os fragmentos
     * adjacentes para montar a string mais completa possível.
     */
    private fun extractDestination(texts: List<String>): String {
        // 1. Busca índice do texto âncora (CEP ou "Cidade – UF")
        val anchorIdx = texts.indexOfFirst { it.matches(Regex(".*\\d{5}-\\d{3}.*")) }
            .takeIf { it >= 0 }
            ?: texts.indexOfLast { it.contains(" – ") || it.contains(" - ") }
                .takeIf { it >= 0 }

        if (anchorIdx == null) return ""

        val parts = mutableListOf<String>()

        // 2. Inclui textos anteriores que pareçam parte do endereço
        //    (ex: "Rua das Flores, 22" antes do CEP)
        var i = anchorIdx - 1
        while (i >= 0) {
            val t = texts[i]
            if (looksLikeAddressFragment(t) && !looksLikeMetric(t)) {
                parts.add(0, t)
                i--
            } else {
                break
            }
        }

        // 3. Texto âncora
        parts.add(texts[anchorIdx])

        // 4. Textos posteriores (ex: "Uberlândia – MG" depois do CEP)
        i = anchorIdx + 1
        while (i < texts.size) {
            val t = texts[i]
            if (looksLikeAddressFragment(t) && !looksLikeMetric(t)) {
                parts.add(t)
                i++
            } else {
                break
            }
        }

        return parts.joinToString(", ")
    }

    /** Heurística: texto que parece fazer parte de um endereço */
    private fun looksLikeAddressFragment(text: String): Boolean {
        val t = text.trim()
        if (t.isBlank()) return false

        // Contém nome de logradouro, bairro, cidade, número, ou "–"
        return t.any { it.isLetter() } && (
            t.contains("Rua ", ignoreCase = true) ||
            t.contains("Av ", ignoreCase = true) ||
            t.contains("Avenida ", ignoreCase = true) ||
            t.contains("Bairro ", ignoreCase = true) ||
            t.contains("Região ", ignoreCase = true) ||
            t.contains(" – ") || t.contains(" - ") ||
            t.matches(Regex(".*[A-Z]{2}\\s*$")) ||       // termina com sigla de estado
            t.matches(Regex(".*\\d{5}-\\d{3}.*")) ||       // contém CEP
            t.matches(Regex("^[A-Z].*\\d+.*"))             // começa com maiúscula + contém número
        )
    }

    /** Descarta textos que são métricas da corrida, não endereços */
    private fun looksLikeMetric(text: String): Boolean {
        val t = text.trim()
        return t.matches(Regex("\\d+[.,]\\d+\\s*km", RegexOption.IGNORE_CASE)) ||
            t.matches(Regex("\\d+\\s*minutos?", RegexOption.IGNORE_CASE)) ||
            t.matches(Regex("R\\$\\s*[\\d.,]+")) ||
            t.matches(Regex("\\+\\s*R\\$\\s*[\\d.,]+")) ||
            t.matches(Regex("\\d[.,]\\d{2}\\s*\\(\\d+\\)"))  // rating
    }

}
