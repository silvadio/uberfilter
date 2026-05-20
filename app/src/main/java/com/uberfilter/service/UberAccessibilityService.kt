package com.uberfilter.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.uberfilter.data.FilterCriteriaStore
import com.uberfilter.domain.RideEvaluator
import com.uberfilter.model.FilterCriteria
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * Serviço de acessibilidade que escuta eventos do app da Uber (motorista)
 * e aciona o overlay quando um cartão de corrida é detectado.
 */
class UberAccessibilityService : AccessibilityService() {

    private lateinit var overlayManager: OverlayManager
    private lateinit var criteriaStore: FilterCriteriaStore
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Cache: evita processar o mesmo cartão duas vezes em sequência
    private var lastOfferId: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(applicationContext)
        criteriaStore  = FilterCriteriaStore(applicationContext)

        // Garante que o serviço escuta janelas do app da Uber Driver
        serviceInfo = serviceInfo.also { info ->
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            info.notificationTimeout = 150
            info.packageNames = arrayOf("com.ubercab.driver")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val root = rootInActiveWindow ?: return

        scope.launch {
            val offer = UberCardParser.parse(root) ?: return@launch

            // Cria um ID simples baseado no valor + destino para deduplicar
            val offerId = "${offer.totalValue}|${offer.destination}"
            if (offerId == lastOfferId) return@launch
            lastOfferId = offerId

            val criteria: FilterCriteria = criteriaStore.criteriaFlow.first()
            val evaluation = RideEvaluator.evaluate(offer, criteria)

            overlayManager.show(offer, evaluation)
        }
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
