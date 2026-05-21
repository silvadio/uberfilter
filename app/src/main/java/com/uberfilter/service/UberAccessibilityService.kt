package com.uberfilter.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.uberfilter.data.FilterCriteriaStore
import com.uberfilter.domain.RideEvaluator
import com.uberfilter.model.FilterCriteria
import com.uberfilter.model.RideOffer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class UberAccessibilityService : AccessibilityService() {

    private lateinit var overlayManager: OverlayManager
    private lateinit var criteriaStore: FilterCriteriaStore

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Critérios em memória — atualizados em background, nunca lidos do disco no evento
    @Volatile private var cachedCriteria: FilterCriteria = FilterCriteria()

    // Debounce — só processa após 300ms sem novos eventos
    private var debounceJob: Job? = null

    // Último cartão exibido — evita re-exibir o mesmo
    private var lastOfferId: String? = null

    // Flag: cartão da Uber está visível na tela?
    private var cardVisible = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(applicationContext)
        criteriaStore  = FilterCriteriaStore(applicationContext)

        serviceInfo = serviceInfo.also { info ->
            info.eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            info.flags =
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                        AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            info.notificationTimeout = 100
            info.packageNames = arrayOf("com.ubercab.driver")
        }

        // Carrega critérios uma vez e mantém atualizado em memória
        scope.launch {
            criteriaStore.criteriaFlow.collect { criteria ->
                cachedCriteria = criteria
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // Se mudou de janela para fora da Uber, esconde popup imediatamente
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: ""
            if (!pkg.contains("ubercab")) {
                if (cardVisible) {
                    cardVisible = false
                    lastOfferId = null
                    overlayManager.dismiss()
                }
                return
            }
        }

        // Debounce: cancela job anterior e agenda novo após 300ms
        // Isso garante que só processa quando a tela parou de mudar
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(300L)
            processCurrentScreen()
        }
    }

    private fun processCurrentScreen() {
        val root = rootInActiveWindow ?: run {
            // Sem janela ativa — esconde
            if (cardVisible) {
                cardVisible = false
                lastOfferId = null
                overlayManager.dismiss()
            }
            return
        }

        val offer = UberCardParser.parse(root)

        if (offer == null) {
            // Cartão sumiu da tela
            if (cardVisible) {
                cardVisible = false
                lastOfferId = null
                overlayManager.dismiss()
            }
            return
        }

        // ID do cartão atual — valor + distância da viagem
        val offerId = "${"%.2f".format(offer.totalValue)}|${"%.1f".format(offer.tripDistanceKm)}"

        // Mesmo cartão já exibido — ignora
        if (offerId == lastOfferId) return

        lastOfferId = offerId
        cardVisible = true

        // Usa critérios já em memória — zero I/O
        val evaluation = RideEvaluator.evaluate(offer, cachedCriteria)
        overlayManager.show(offer, evaluation)
    }

    override fun onInterrupt() {
        overlayManager.dismiss()
    }

    override fun onDestroy() {
        overlayManager.destroy()
        scope.cancel()
        super.onDestroy()
    }
}