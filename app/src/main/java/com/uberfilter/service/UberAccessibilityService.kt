package com.uberfilter.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.uberfilter.data.FilterCriteriaStore
import com.uberfilter.data.FinanceDatabase
import com.uberfilter.data.RideHistoryRepository
import com.uberfilter.domain.RideEvaluator
import com.uberfilter.model.EvaluationColor
import com.uberfilter.model.FilterCriteria
import com.uberfilter.model.RideOffer
import com.uberfilter.model.RideRecord
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class UberAccessibilityService : AccessibilityService() {

    private lateinit var overlayManager: OverlayManager
    private lateinit var criteriaStore: FilterCriteriaStore
    private lateinit var rideHistoryRepo: RideHistoryRepository

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Critérios em memória — atualizados em background, nunca lidos do disco no evento
    @Volatile private var cachedCriteria: FilterCriteria = FilterCriteria()

    // Flag: assistente está habilitado pelo usuário?
    @Volatile private var assistantEnabled = true

    // Debounce — só processa após 300ms sem novos eventos
    private var debounceJob: Job? = null

    // Último cartão exibido — evita re-exibir o mesmo
    private var lastOfferId: String? = null

    // Flag: cartão está visível na tela?
    private var cardVisible = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(applicationContext)
        criteriaStore  = FilterCriteriaStore(applicationContext)
        rideHistoryRepo = RideHistoryRepository(
            FinanceDatabase.getInstance(applicationContext).rideHistoryDao()
        )

        serviceInfo = serviceInfo.also { info ->
            info.eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            info.flags =
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                        AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            info.notificationTimeout = 100
            info.packageNames = arrayOf("com.ubercab.driver", "com.app99.driver")
        }

        // Carrega critérios uma vez e mantém atualizado em memória
        scope.launch {
            criteriaStore.criteriaFlow.collect { criteria ->
                cachedCriteria = criteria
            }
        }

        // Monitora estado do switch assistente — zero I/O no evento
        scope.launch {
            criteriaStore.assistantEnabledFlow.collect { enabled ->
                assistantEnabled = enabled
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: ""

        // Se mudou de janela para fora da Uber/99, esconde popup imediatamente
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (!pkg.contains("ubercab") && !pkg.contains("app99")) {
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
        // Switch desligado — não processa nada, zero overhead
        if (!assistantEnabled) return

        val root = rootInActiveWindow ?: run {
            // Sem janela ativa — esconde
            if (cardVisible) {
                cardVisible = false
                lastOfferId = null
                overlayManager.dismiss()
            }
            return
        }

        val rootPkg = root.packageName?.toString() ?: ""
        val is99 = rootPkg.contains("app99")
        val parser: RideCardParser = if (is99) N99CardParser else UberCardParser
        val offer = parser.parse(root)

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

        // Auto-save fire-and-forget — a cor de fundo do popup já informa a classificação
        scope.launch {
            rideHistoryRepo.insert(
                RideRecord(
                    offerId = offerId,
                    totalValue = offer.totalValue,
                    bonusValue = offer.bonusValue,
                    passengerRating = offer.passengerRating,
                    passengerRatingCount = offer.passengerRatingCount,
                    distanceToPickupKm = offer.distanceToPickupKm,
                    minutesToPickup = offer.minutesToPickup,
                    tripDurationMin = offer.tripDurationMin,
                    tripDistanceKm = offer.tripDistanceKm,
                    pickupRegion = offer.pickupRegion,
                    destination = offer.destination,
                    isExclusive = offer.isExclusive,
                    score = evaluation.score,
                    color = evaluation.color
                )
            )
        }
    }

    override fun onInterrupt() {
        if (::overlayManager.isInitialized) overlayManager.dismiss()
    }

    override fun onDestroy() {
        if (::overlayManager.isInitialized) overlayManager.destroy()
        scope.cancel()
        super.onDestroy()
    }
}
