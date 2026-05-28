package com.uberfilter.ui.screens.home

import android.location.Geocoder
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
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.uberfilter.model.GeofenceEntry
import com.uberfilter.ui.SettingsViewModel
import com.uberfilter.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinosScreen(vm: SettingsViewModel) {
    val blockedLocations by vm.blockedLocations.collectAsState()
    val geofences by vm.geofences.collectAsState()
    var innerTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Destinos",
            color = WarmYellow,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
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

        when (innerTab) {
            0 -> RaioTab(vm, geofences)
            1 -> TextoTab(vm, blockedLocations)
        }
    }
}

// ── Sub-aba: Bloqueio textual ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextoTab(vm: SettingsViewModel, blockedLocations: List<String>) {
    var newLocation by remember { mutableStateOf("") }
    var duplicateError by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Locais Indesejados")

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = LightYellowContainer)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (blockedLocations.isEmpty()) {
                    Text(
                        text = "Nenhum local cadastrado.",
                        color = WarmOnSurfaceVariant,
                        fontSize = 13.sp
                    )
                } else {
                    blockedLocations.forEach { location ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                onClick = { vm.removeBlockedLocation(location) },
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
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        modifier = Modifier.weight(1f),
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
                    IconButton(
                        onClick = {
                            val trimmed = newLocation.trim()
                            if (trimmed.isBlank()) return@IconButton
                            val alreadyExists = blockedLocations.any {
                                normalizeForComparison(it) == normalizeForComparison(trimmed)
                            }
                            if (alreadyExists) {
                                duplicateError = true
                                return@IconButton
                            }
                            vm.addBlockedLocation(trimmed)
                            newLocation = ""
                        },
                        enabled = newLocation.isNotBlank(),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Adicionar",
                            tint = if (newLocation.isNotBlank()) WarmYellow else WarmOutline
                        )
                    }
                }
            }
        }

        Text(
            text = "O alerta dispara quando o destino contém o termo cadastrado.",
            color = WarmOnSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

// ── Sub-aba: Bloqueio por raio (mapa osmdroid) ────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RaioTab(vm: SettingsViewModel, geofences: List<GeofenceEntry>) {
    val context = LocalContext.current
    var radiusKm by remember { mutableFloatStateOf(1.5f) }
    var centerLabel by remember { mutableStateOf("") }
    val centerLatLng = remember { mutableStateOf(Pair(-22.8267, -43.0519)) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Regiões Bloqueadas")

        var showFullscreen by remember { mutableStateOf(false) }

        // Mapa osmdroid com botão expandir
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
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
                            }.ifBlank { "${"%.4f".format(lat)}, ${"%.4f".format(lng)}" }
                        } else {
                            centerLabel = "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                        }
                    } catch (_: Exception) {
                        centerLabel = "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Botão expandir
            IconButton(
                onClick = { showFullscreen = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fullscreen,
                    contentDescription = "Expandir mapa",
                    tint = WarmOnBg,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Slider de raio
        Text(
            text = "Raio: ${"%.1f".format(radiusKm)} km",
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

        // Botão adicionar
        Button(
            onClick = {
                vm.addGeofence(
                    GeofenceEntry(
                        centerLat = centerLatLng.value.first,
                        centerLng = centerLatLng.value.second,
                        radiusKm = radiusKm.toDouble(),
                        addressLabel = centerLabel.ifBlank {
                            "${"%.4f".format(centerLatLng.value.first)}, ${"%.4f".format(centerLatLng.value.second)}"
                        }
                    )
                )
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
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Adicionar Região", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Fullscreen dialog
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
                                    text = "Raio: ${"%.1f".format(radiusKm)} km",
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
                                        vm.addGeofence(
                                            GeofenceEntry(
                                                centerLat = centerLatLng.value.first,
                                                centerLng = centerLatLng.value.second,
                                                radiusKm = radiusKm.toDouble(),
                                                addressLabel = centerLabel.ifBlank {
                                                    "${"%.4f".format(centerLatLng.value.first)}, ${"%.4f".format(centerLatLng.value.second)}"
                                                }
                                            )
                                        )
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
                                    }.ifBlank { "${"%.4f".format(lat)}, ${"%.4f".format(lng)}" }
                                } else {
                                    centerLabel = "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                                }
                            } catch (_: Exception) {
                                centerLabel = "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                            }
                        },
                        modifier = Modifier.fillMaxSize().padding(padding)
                    )
                }
            }
        }

        // Lista de geofences cadastrados
        if (geofences.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightYellowContainer)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    geofences.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.addressLabel,
                                    color = WarmOnBg,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${"%.1f".format(entry.radiusKm)} km",
                                    color = WarmOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = { vm.removeGeofence(entry) },
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
                }
            }
        }
    }
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
private fun SectionTitle(text: String) {
    Text(
        text,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        color = WarmOnSurfaceVariant,
        letterSpacing = 0.8.sp
    )
}

/** Normaliza string para comparação: remove acentos, lowercase, trim */
private fun normalizeForComparison(text: String): String {
    val stripped = java.text.Normalizer.normalize(text.trim(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    return stripped.lowercase()
}
