package com.uberfilter.service

import android.view.accessibility.AccessibilityNodeInfo
import com.uberfilter.model.RideOffer

/**
 * Interface comum para parsers de cartão de corrida.
 * Cada implementação sabe extrair dados de um app específico (Uber, 99, InDriver, etc).
 */
interface RideCardParser {
    fun parse(root: AccessibilityNodeInfo): RideOffer?
}

/**
 * Varre a árvore de nós de acessibilidade e coleta todo texto visível em ordem.
 * Compartilhada por todos os parsers.
 */
fun collectTexts(node: AccessibilityNodeInfo?, out: MutableList<String>) {
    node ?: return
    val text = node.text?.toString()?.trim()
    if (!text.isNullOrBlank()) out += text
    val desc = node.contentDescription?.toString()?.trim()
    if (!desc.isNullOrBlank() && desc != text) out += desc
    for (i in 0 until node.childCount) collectTexts(node.getChild(i), out)
}

/** Converte "19,63" ou "19.63" para Double */
fun String.toDoubleLocale(): Double =
    this.replace(",", ".").toDoubleOrNull() ?: 0.0
