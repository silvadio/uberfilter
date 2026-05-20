package com.uberfilter.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.uberfilter.model.RideEvaluation
import com.uberfilter.model.RideOffer
import kotlinx.coroutines.*

/**
 * Gerencia o overlay (popup) exibido sobre outros apps.
 * Usa WindowManager + ComposeView para renderizar Jetpack Compose fora de uma Activity.
 */
class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private var hideJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Tempo que o popup fica visível (ms)
    private val AUTO_DISMISS_MS = 6_000L

    fun show(offer: RideOffer, evaluation: RideEvaluation) {
        // Se já existe um overlay, remove antes
        dismiss()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            x = 0
            y = 0
        }

        val lifecycleOwner = MyLifecycleOwner().also {
            it.performRestore(null)
            it.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            it.handleLifecycleEvent(Lifecycle.Event.ON_START)
            it.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        val composeView = ComposeView(context).apply {
            // Necessário para ComposeView fora de Activity — usando extensões
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                RidePopup(offer = offer, evaluation = evaluation)
            }
        }

        overlayView = composeView
        windowManager.addView(composeView, params)

        // Auto-dismiss
        hideJob = scope.launch {
            delay(AUTO_DISMISS_MS)
            dismiss()
        }
    }

    fun dismiss() {
        hideJob?.cancel()
        overlayView?.let {
            runCatching { windowManager.removeView(it) }
        }
        overlayView = null
    }

    fun destroy() {
        dismiss()
        scope.cancel()
    }
}

// ── Composable do popup ────────────────────────────────────────────────────────

@Composable
private fun RidePopup(offer: RideOffer, evaluation: RideEvaluation) {
    val bgColor = if (evaluation.isGood) Color(0xFF2E7D32) else Color(0xFFB71C1C)

    Box(
        modifier = Modifier
            .width(300.dp)
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

            // ── Linha: Valor total ───────────────────────────────────────────
            MetricRow(
                label = "Valor total",
                value = "R$ ${"%.2f".format(offer.effectiveValue)}"
            )

            // ── Linha: R$/hora ───────────────────────────────────────────────
            MetricRow(
                label = "R$/hora",
                value = "R$ ${"%.2f".format(offer.valuePerHour)}"
            )

            // ── Linha: R$/km ─────────────────────────────────────────────────
            MetricRow(
                label = "R$/km",
                value = "R$ ${"%.2f".format(offer.valuePerKm)}"
            )

            Spacer(Modifier.height(12.dp))

            // ── Rodapé: Distância | Passageiro | Duração ─────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FooterItem(
                    top = "Distância",
                    bottom = "${"%.1f".format(offer.distanceToPickupKm + offer.tripDistanceKm)} km",
                    alignment = Alignment.Start
                )
                FooterItem(
                    top = "Passageiro",
                    bottom = "★ ${"%.2f".format(offer.passengerRating)}",
                    alignment = Alignment.CenterHorizontally
                )
                FooterItem(
                    top = "Duração",
                    bottom = "${offer.minutesToPickup + offer.tripDurationMin} min",
                    alignment = Alignment.End
                )
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.80f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FooterItem(
    top: String,
    bottom: String,
    alignment: Alignment.Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = top,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = bottom,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Lifecycle owner mínimo para ComposeView fora de Activity ──────────────────

private class MyLifecycleOwner : SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) = lifecycleRegistry.handleLifecycleEvent(event)
    fun performRestore(savedState: android.os.Bundle?) = savedStateRegistryController.performRestore(savedState)
}
