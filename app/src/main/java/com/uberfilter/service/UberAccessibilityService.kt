package com.uberfilter.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.uberfilter.data.FilterCriteriaStore
import com.uberfilter.data.FinanceDatabase
import com.uberfilter.data.RideHistoryRepository
import com.uberfilter.data.UndesiredLocationStore
import com.uberfilter.domain.GeofenceChecker
import com.uberfilter.domain.RideEvaluator
import com.uberfilter.domain.UndesiredLocationChecker
import com.uberfilter.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class UberAccessibilityService : AccessibilityService() {

    private lateinit var overlayManager: OverlayManager
    private lateinit var criteriaStore: FilterCriteriaStore
    private lateinit var rideHistoryRepo: RideHistoryRepository
    private lateinit var locationStore: UndesiredLocationStore

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Volatile private var cachedCriteria: FilterCriteria = FilterCriteria()
    @Volatile private var cachedBlockedLocations: List<String> = emptyList()
    @Volatile private var cachedGeofences: List<GeofenceEntry> = emptyList()

    @Volatile private var assistantEnabled = true

    private var debounceJob: Job? = null
    private var lastOfferId: String? = null
    private var cardVisible = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(applicationContext)
        criteriaStore  = FilterCriteriaStore(applicationContext)
        rideHistoryRepo = RideHistoryRepository(
            FinanceDatabase.getInstance(applicationContext).rideHistoryDao()
        )
        locationStore = UndesiredLocationStore(applicationContext)

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

        scope.launch {
            criteriaStore.criteriaFlow.collect { criteria ->
                cachedCriteria = criteria
            }
        }

        scope.launch {
            criteriaStore.assistantEnabledFlow.collect { enabled ->
                assistantEnabled = enabled
            }
        }

        scope.launch {
            locationStore.textLocationsFlow.collect { locations ->
                cachedBlockedLocations = locations
            }
        }

        scope.launch {
            locationStore.geofencesFlow.collect { geofences ->
                cachedGeofences = geofences
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: ""

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

        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(300L)
            processCurrentScreen()
        }
    }

    private fun processCurrentScreen() {
        if (!assistantEnabled) return

        val root = rootInActiveWindow ?: run {
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
            if (cardVisible) {
                cardVisible = false
                lastOfferId = null
                overlayManager.dismiss()
            }
            return
        }

        val offerId = "${"%.2f".format(offer.totalValue)}|${"%.1f".format(offer.tripDistanceKm)}"
        if (offerId == lastOfferId) return

        lastOfferId = offerId
        cardVisible = true

        // 1. Verificação textual (rápida, <1ms)
        val textMatch = UndesiredLocationChecker.check(offer.destination, cachedBlockedLocations)

        // 2. Verificação por raio (geocoding, ~50-500ms)
        val geofenceMatch = if (textMatch == null && cachedGeofences.isNotEmpty()) {
            GeofenceChecker.check(offer.destination, cachedGeofences, applicationContext)
        } else null

        val evaluation = when {
            textMatch != null -> RideEvaluation(
                score = 0.0,
                color = EvaluationColor.BLOCKED,
                results = emptyList(),
                blockedLocation = textMatch.term,
                blockedType = BlockedType.TEXT
            )
            geofenceMatch != null -> RideEvaluation(
                score = 0.0,
                color = EvaluationColor.BLOCKED,
                results = emptyList(),
                blockedLocation = geofenceMatch.term,
                blockedType = BlockedType.GEOFENCE
            )
            else -> RideEvaluator.evaluate(offer, cachedCriteria)
        }

        overlayManager.show(offer, evaluation)

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
