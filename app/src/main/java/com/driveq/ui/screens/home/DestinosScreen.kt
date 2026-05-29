package com.driveq.ui.screens.home

import android.location.Geocoder
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.driveq.model.GeofenceEntry
import com.driveq.ui.SettingsViewModel
import com.driveq.ui.theme.*
import kotlinx.coroutines.delay

// ── Formatting helpers ────────────────────────────────────────────────────────

private fun fmt1(d: Double) = "%.1f".format(d)
private fun fmt4(d: Double) = "%.4f".format(d)
private fun fmtCoord(lat: Double, lng: Double) = fmt4(lat) + ", " + fmt4(lng)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinosScreen(vm: SettingsViewModel) {
    val blockedLocations by vm.blockedLocations.collectAsState()
    val geofences by vm.geofences.collectAsState()
    var innerTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Destinos",
            color = WarmYellow,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(end = 20.dp)
        )

        // Sub-abas: Raio | Texto
        TabRow(
            selectedTabIndex = innerTab,
            containerColor = PureWhite,
            contentColor = WarmYellow,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[innerTab]),
                    color = WarmYellow
                )
            },
            divider = { HorizontalDivider(color = WarmOutline.copy(alpha = 0.3f), thickness = 0.5.dp) }
        ) {
            Tab(
                selected = innerTab == 0,
                onClick = { innerTab = 0 },
                text = { Text("Raio", fontWeight = if (innerTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                selectedContentColor = WarmYellow,
                unselectedContentColor = WarmOnSurfaceVariant
            )
            Tab(
                selected = innerTab == 1,
                onClick = { innerTab = 1 },
                text = { Text("Texto", fontWeight = if (innerTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                selectedContentColor = WarmYellow,
                unselectedContentColor = WarmOnSurfaceVariant
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (innerTab) {
                0 -> RaioTab(vm, geofences)
                1 -> TextoTab(vm, blockedLocations)
            }
        }
    }
}

// ── Sub-aba: Bloqueio textual ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextoTab(vm: SettingsViewModel, blockedLocations: List<String>) {
    var showAddDialog by remember { mutableStateOf(false) }
    var highlightTrigger by remember { mutableStateOf<String?>(null) }
    val listScrollState = rememberScrollState()

    LaunchedEffect(highlightTrigger) {
        if (highlightTrigger != null) {
            delay(120)
            listScrollState.animateScrollTo(listScrollState.maxValue)
            delay(1500)
            highlightTrigger = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (blockedLocations.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Outlined.LocationOff,
                title = "Nenhum local cadastrado",
                subtitle = "Use o botão + para adicionar",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(listScrollState)
                    .padding(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                SectionDivider("Locais (${blockedLocations.size})")
                Spacer(Modifier.height(8.dp))
                blockedLocations.forEachIndexed { index, location ->
                    val isHighlighted = highlightTrigger == location
                    TextLocationItem(
                        location = location,
                        isHighlighted = isHighlighted,
                        onRemove = { vm.removeBlockedLocation(location) }
                    )
                    if (index < blockedLocations.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = WarmOutline.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = WarmYellow,
            contentColor = OnWarmYellow,
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Adicionar Local")
        }
    }

    if (showAddDialog) {
        AddLocationDialog(
            blockedLocations = blockedLocations,
            onDismiss = { showAddDialog = false },
            onConfirm = { location ->
                vm.addBlockedLocation(location)
                highlightTrigger = location
                showAddDialog = false
            }
        )
    }
}

// ── Sub-aba: Bloqueio por raio ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RaioTab(vm: SettingsViewModel, geofences: List<GeofenceEntry>) {
    val context = LocalContext.current
    var showFullscreen by remember { mutableStateOf(false) }

    // Dialog map states
    var radiusKm by remember { mutableFloatStateOf(1.5f) }
    var centerLabel by remember { mutableStateOf("") }
    val centerLatLng = remember { mutableStateOf(Pair(-22.8267, -43.0519)) }

    // Highlight & scroll state
    val cardScrollState = rememberScrollState()
    var highlightTrigger by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(highlightTrigger) {
        if (highlightTrigger != null) {
            delay(120)
            cardScrollState.animateScrollTo(cardScrollState.maxValue)
            delay(1500)
            highlightTrigger = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (geofences.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Outlined.TravelExplore,
                title = "Nenhuma região bloqueada",
                subtitle = "Use o botão + para adicionar",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(cardScrollState)
                    .padding(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                SectionDivider("Regiões (${geofences.size})")
                Spacer(Modifier.height(8.dp))
                geofences.forEachIndexed { index, entry ->
                    GeofenceItem(
                        entry = entry,
                        isHighlighted = highlightTrigger == entry.addressLabel,
                        onRemove = { vm.removeGeofence(entry) }
                    )
                    if (index < geofences.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = WarmOutline.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showFullscreen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = WarmYellow,
            contentColor = OnWarmYellow,
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Adicionar Região")
        }
    }

    // Fullscreen map dialog
    if (showFullscreen) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                containerColor = WarmWhite,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Região Bloqueada",
                                color = WarmOnBg,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { showFullscreen = false }) {
                                Icon(
                                    Icons.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = WarmYellow
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showFullscreen = false }) {
                                Icon(
                                    Icons.Outlined.CloseFullscreen,
                                    contentDescription = "Recolher",
                                    tint = WarmYellow
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = WarmWhite
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        color = PureWhite,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Raio: ${String.format("%.1f", radiusKm)} km",
                                color = WarmOnBg,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Slider(
                                value = radiusKm,
                                onValueChange = { radiusKm = it },
                                valueRange = 0.5f..10f,
                                steps = 18,
                                colors = SliderDefaults.colors(
                                    thumbColor = WarmYellow,
                                    activeTrackColor = WarmYellow,
                                    inactiveTrackColor = WarmOutline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (centerLabel.isNotBlank()) {
                                Text(
                                    text = "📍 $centerLabel",
                                    color = WarmOnSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                            Button(
                                onClick = {
                                    val entry = GeofenceEntry(
                                        centerLat = centerLatLng.value.first,
                                        centerLng = centerLatLng.value.second,
                                        radiusKm = radiusKm.toDouble(),
                                        addressLabel = centerLabel.ifBlank {
                                            "${String.format("%.4f", centerLatLng.value.first)}, ${String.format("%.4f", centerLatLng.value.second)}"
                                        }
                                    )
                                    vm.addGeofence(entry)
                                    highlightTrigger = entry.addressLabel
                                    showFullscreen = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarmYellow,
                                    contentColor = OnWarmYellow
                                )
                            ) {
                                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Confirmar Região", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            ) { padding ->
                OsmMapView(
                    center = centerLatLng.value,
                    radiusKm = radiusKm.toDouble(),
                    onCenterChanged = { lat, lng ->
                        centerLatLng.value = Pair(lat, lng)
                        try {
                            val geocoder = Geocoder(context)
                            val addresses = geocoder.getFromLocation(lat, lng, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                centerLabel = buildString {
                                    if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                                    if (!addr.subLocality.isNullOrBlank()) {
                                        if (isNotEmpty()) append(", ")
                                        append(addr.subLocality)
                                    }
                                    if (!addr.locality.isNullOrBlank()) {
                                        if (isNotEmpty()) append(" - ")
                                        append(addr.locality)
                                    }
                                    if (!addr.adminArea.isNullOrBlank()) {
                                        if (isNotEmpty()) append("/")
                                        append(addr.adminArea)
                                    }
                                }.ifBlank { "${fmt4(lat)}, ${fmt4(lng)}" }
                            } else {
                                centerLabel = "${fmt4(lat)}, ${fmt4(lng)}"
                            }
                        } catch (_: Exception) {
                            centerLabel = "${fmt4(lat)}, ${fmt4(lng)}"
                        }
                    },
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }
        }
    }
}

// ── Itens da lista: Geofence (Raio) ────────────────────────────────────────────

@Composable
private fun GeofenceItem(
    entry: GeofenceEntry,
    isHighlighted: Boolean,
    onRemove: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) WarmYellow.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = if (isHighlighted) 0 else 800),
        label = "highlightGeofenceBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.addressLabel,
                color = WarmOnBg,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${fmt1(entry.radiusKm)} km",
                color = WarmOnSurfaceVariant,
                fontSize = 11.sp
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Remover",
                tint = RedFinance,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Itens da lista: Termo textual ──────────────────────────────────────────────

@Composable
private fun TextLocationItem(
    location: String,
    isHighlighted: Boolean,
    onRemove: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) WarmYellow.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = if (isHighlighted) 0 else 800),
        label = "highlightTextBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = location,
            color = WarmOnBg,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Remover",
                tint = RedFinance,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Dialog de adição de local textual ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLocationDialog(
    blockedLocations: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newLocation by remember { mutableStateOf("") }
    var duplicateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = WarmWhite,
        title = {
            Text(
                "Adicionar Local",
                fontWeight = FontWeight.Bold,
                color = WarmOnBg,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "O alerta dispara quando o destino contém o termo cadastrado.",
                    color = WarmOnSurfaceVariant,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = newLocation,
                    onValueChange = {
                        newLocation = it
                        duplicateError = false
                    },
                    placeholder = {
                        Text("Bairro, rua ou cidade", fontSize = 13.sp, color = WarmPlaceholder)
                    },
                    singleLine = true,
                    isError = duplicateError,
                    supportingText = if (duplicateError) {
                        { Text("Este local já está na lista", color = RedFinance, fontSize = 11.sp) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WarmOnBg,
                        unfocusedTextColor = WarmOnBg,
                        cursorColor = WarmYellow,
                        focusedBorderColor = WarmYellow,
                        unfocusedBorderColor = WarmOutline,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = newLocation.trim()
                    if (trimmed.isBlank()) return@Button
                    val alreadyExists = blockedLocations.any {
                        normalizeForComparison(it) == normalizeForComparison(trimmed)
                    }
                    if (alreadyExists) {
                        duplicateError = true
                        return@Button
                    }
                    onConfirm(trimmed)
                },
                enabled = newLocation.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmYellow,
                    contentColor = OnWarmYellow
                )
            ) {
                Text("Adicionar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = WarmOnSurfaceVariant)
            }
        }
    )
}

// ── Componente: Mapa osmdroid via AndroidView ──────────────────────────────────

@Composable
private fun OsmMapView(
    center: Pair<Double, Double>,
    radiusKm: Double,
    onCenterChanged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnCenterChanged by rememberUpdatedState(onCenterChanged)

    AndroidView(
        factory = { ctx ->
            org.osmdroid.config.Configuration.getInstance().apply {
                userAgentValue = ctx.packageName
                osmdroidTileCache = ctx.cacheDir
            }
            org.osmdroid.views.MapView(ctx).apply {
                clipChildren = true
                setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                zoomController.setVisibility(
                    org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS
                )

                val initialCenter = org.osmdroid.util.GeoPoint(center.first, center.second)
                controller.setCenter(initialCenter)
                controller.setZoom(15.0)

                addMapListener(object : org.osmdroid.events.MapListener {
                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                        event ?: return false
                        val newCenter = mapCenter
                        currentOnCenterChanged(newCenter.latitude, newCenter.longitude)
                        return false
                    }
                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean = false
                })
            }
        },
        modifier = modifier.clipToBounds(),
        update = { mapView ->
            val mapCenter = org.osmdroid.util.GeoPoint(center.first, center.second)
            mapView.controller.setCenter(mapCenter)

            mapView.overlays.clear()

            val circle = org.osmdroid.views.overlay.Polygon()
            circle.points = org.osmdroid.views.overlay.Polygon.pointsAsCircle(mapCenter, radiusKm * 1000.0)
            circle.fillPaint.apply {
                color = android.graphics.Color.argb(40, 249, 168, 37)
                style = android.graphics.Paint.Style.FILL
            }
            circle.outlinePaint.apply {
                color = android.graphics.Color.argb(200, 249, 168, 37)
                strokeWidth = 2f
                style = android.graphics.Paint.Style.STROKE
            }
            mapView.overlays.add(circle)

            val marker = org.osmdroid.views.overlay.Marker(mapView)
            marker.position = mapCenter
            marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
            marker.title = "Centro"
            mapView.overlays.add(marker)

            mapView.invalidate()
        }
    )
}

// ── Componentes utilitários ────────────────────────────────────────────────────

@Composable
private fun SectionDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = WarmOutline.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )
        Text(
            text = text,
            color = WarmOnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = WarmOutline.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun EmptyStatePlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WarmOnSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = title,
            color = WarmOnSurfaceVariant,
            fontSize = 13.sp
        )
        Text(
            text = subtitle,
            color = WarmOnSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
    }
}

/** Normaliza string para comparação: remove acentos, lowercase, trim */
private fun normalizeForComparison(text: String): String {
    val stripped = java.text.Normalizer.normalize(text.trim(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    return stripped.lowercase()
}
