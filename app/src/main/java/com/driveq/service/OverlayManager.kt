package com.driveq.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.outlined.GpsOff
import androidx.compose.material.icons.rounded.Warning
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
import com.driveq.model.CriteriaKey
import com.driveq.model.EvaluationColor
import com.driveq.model.RideEvaluation
import com.driveq.model.RideOffer
import kotlinx.coroutines.*

/**
 * Gerencia o overlay (popup) exibido sobre outros apps.
 * Usa WindowManager + ComposeView para renderizar Jetpack Compose fora de uma Activity.
 */
class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var hideJob: Job? = null

    // Estado reativo — atualizado sem recriar a view
    private val currentOffer      = mutableStateOf<RideOffer?>(null)
    private val currentEvaluation = mutableStateOf<RideEvaluation?>(null)
    private val isVisible         = mutableStateOf(false)

    // LifecycleOwner criado uma única vez
    private val lifecycleOwner = MyLifecycleOwner().also {
        it.performRestore(null)
        it.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        it.handleLifecycleEvent(Lifecycle.Event.ON_START)
        it.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    // ComposeView criado uma única vez
    private val composeView = ComposeView(context).apply {
        setViewTreeLifecycleOwner(lifecycleOwner)
        setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        setContent {
            val offer      by currentOffer
            val evaluation by currentEvaluation
            val visible    by isVisible

            if (visible && offer != null && evaluation != null) {
                if (evaluation!!.color == EvaluationColor.BLOCKED) {
                    BlockedDestinationPopup(
                        destination = offer!!.destination.ifBlank { "(destino não identificado)" },
                        blockedTerm = evaluation!!.blockedLocation ?: "?",
                        isGeofence = evaluation!!.blockedType == com.driveq.model.BlockedType.GEOFENCE
                    )
                } else {
                    RidePopup(offer = offer!!, evaluation = evaluation!!)
                }
            }
        }
    }

    private val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    else
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE

    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        layoutFlag,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        x = 0
        y = (8 * context.resources.displayMetrics.density).toInt()
    }

    private var viewAttached = false

    /**
     * Só anexa a view ao WindowManager no primeiro show(),
     * e apenas se a permissão SYSTEM_ALERT_WINDOW estiver concedida.
     * Retorna true se a view está pronta para exibição.
     */
    private fun ensureViewAttached(): Boolean {
        if (viewAttached) return true
        if (!Settings.canDrawOverlays(context)) return false
        return try {
            windowManager.addView(composeView, params)
            viewAttached = true
            true
        } catch (_: Exception) {
            false
        }
    }

    fun show(offer: RideOffer, evaluation: RideEvaluation) {
        if (!ensureViewAttached()) return  // permissão ausente → ignora silenciosamente

        hideJob?.cancel()
        currentOffer.value      = offer
        currentEvaluation.value = evaluation
        isVisible.value         = true

        val duration = if (evaluation.color == EvaluationColor.BLOCKED) 8_000L else 6_000L
        hideJob = scope.launch {
            delay(duration)
            dismiss()
        }
    }

    fun dismiss() {
        isVisible.value = false
        hideJob?.cancel()
    }

    fun destroy() {
        dismiss()
        scope.cancel()
        if (viewAttached) {
            runCatching { windowManager.removeView(composeView) }
            viewAttached = false
        }
    }
}

// ── Cores ──────────────────────────────────────────────────────────────────────

private fun evaluationBgColor(color: EvaluationColor) = when (color) {
    EvaluationColor.GREEN   -> Color(0xFF1B8C3E)
    EvaluationColor.YELLOW  -> Color(0xFFF9A825)
    EvaluationColor.RED     -> Color(0xFFB71C1C)
    EvaluationColor.BLOCKED -> Color(0xFF311B92)  // roxo escuro — local indesejado
}

// ── Popup ──────────────────────────────────────────────────────────────────────

@Composable
private fun RidePopup(offer: RideOffer, evaluation: RideEvaluation) {
    val bgColor = evaluationBgColor(evaluation.color)

    fun passed(key: CriteriaKey) = evaluation.results.first { it.key == key }.passed

    Box(
        modifier = Modifier
            .width(300.dp)
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

            MetricRow(
                label  = "Valor total",
                value  = "R$ ${"%.2f".format(offer.effectiveValue)}",
                passed = passed(CriteriaKey.TOTAL_VALUE)
            )
            MetricRow(
                label  = "R$/hora",
                value  = "R$ ${"%.2f".format(offer.valuePerHour)}",
                passed = passed(CriteriaKey.VALUE_PER_HOUR)
            )
            MetricRow(
                label  = "R$/km",
                value  = "R$ ${"%.2f".format(offer.valuePerKm)}",
                passed = passed(CriteriaKey.VALUE_PER_KM)
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FooterItem(
                    top       = "Distância",
                    bottom    = "${"%.1f".format(offer.distanceToPickupKm + offer.tripDistanceKm)} km",
                    passed    = passed(CriteriaKey.PICKUP_DISTANCE),
                    alignment = Alignment.Start
                )
                FooterItem(
                    top       = "Passageiro",
                    bottom    = "${"%.2f".format(offer.passengerRating)}",
                    passed    = passed(CriteriaKey.PASSENGER_RATING),
                    alignment = Alignment.CenterHorizontally
                )
                FooterItem(
                    top       = "Duração",
                    bottom    = "${offer.minutesToPickup + offer.tripDurationMin} min",
                    passed    = passed(CriteriaKey.TRIP_DURATION),
                    alignment = Alignment.End
                )
            }

        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, passed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            if (!passed) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
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
    passed: Boolean,
    alignment: Alignment.Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = top,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!passed) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = bottom,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Popup de Local Bloqueado ──────────────────────────────────────────────────

/**
 * Popup exibido quando o destino da corrida contém um local indesejado.
 * Layout: ícone + título + endereço + "contém: termo".
 * Fundo roxo escuro — totalmente distinto das cores financeiras.
 */
@Composable
private fun BlockedDestinationPopup(
    destination: String,
    blockedTerm: String,
    isGeofence: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .background(Color(0xFF311B92), RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título com ícone
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.GpsOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (isGeofence) "LOCAL INDESEJADO (região)"
                           else "LOCAL INDESEJADO",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Endereço de destino
            Text(
                text = destination,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            // Termo que disparou o alerta
            Text(
                text = if (isGeofence) "dentro do raio de: \"$blockedTerm\""
                       else "contém: \"$blockedTerm\"",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ── LifecycleOwner mínimo para ComposeView fora de Activity ───────────────────

private class MyLifecycleOwner : SavedStateRegistryOwner {
    private val lifecycleRegistry            = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) =
        lifecycleRegistry.handleLifecycleEvent(event)

    fun performRestore(savedState: android.os.Bundle?) =
        savedStateRegistryController.performRestore(savedState)
}