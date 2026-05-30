package com.driveq.ui.screens.destinos

import android.location.Geocoder
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.driveq.ui.components.BaseListCard
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
                text = { Text("Por Região", fontWeight = if (innerTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                selectedContentColor = WarmYellow,
                unselectedContentColor = WarmOnSurfaceVariant
            )
            Tab(
                selected = innerTab == 1,
                onClick = { innerTab = 1 },
                text = { Text("Por Local", fontWeight = if (innerTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
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
    var editingLocation: String? by remember { mutableStateOf(null) }
    var highlightTrigger by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(highlightTrigger) {
        if (highlightTrigger != null) {
            delay(120)
            val targetIndex = blockedLocations.indexOfFirst { it == highlightTrigger }
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex + 1) // +1 compensa o header
            }
            delay(1500)
            highlightTrigger = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (blockedLocations.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Outlined.LocationOff,
                title = "Nenhum local indesejado",
                subtitle = "Use o botão + para adicionar",
                description = "Defina locais específicos onde não deseja realizar corridas",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(end = 20.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionHeader("LOCAIS (${blockedLocations.size})")
                }
                itemsIndexed(
                    items = blockedLocations,
                    key = { _, location -> location }
                ) { _, location ->
                    val isHighlighted = highlightTrigger == location
                    TextLocationItem(
                        location = location,
                        isHighlighted = isHighlighted,
                        onClick = {
                            editingLocation = location
                            showAddDialog = true
                        },
                        onRemove = { vm.removeBlockedLocation(location) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingLocation = null
                showAddDialog = true
            },
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
            editingLocation = editingLocation,
            onDismiss = {
                showAddDialog = false
                editingLocation = null
            },
            onConfirm = { newValue ->
                if (editingLocation != null) {
                    vm.updateBlockedLocation(editingLocation!!, newValue)
                } else {
                    vm.addBlockedLocation(newValue)
                }
                highlightTrigger = newValue
                showAddDialog = false
                editingLocation = null
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
    var editingEntry: GeofenceEntry? by remember { mutableStateOf(null) }

    // Dialog map states
    var radiusKm by remember { mutableFloatStateOf(1.5f) }
    var centerLabel by remember { mutableStateOf("") }
    val centerLatLng = remember { mutableStateOf(Pair(-22.8267, -43.0519)) }

    // Highlight & scroll state
    val cardListState = rememberLazyListState()
    var highlightTrigger by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(highlightTrigger) {
        if (highlightTrigger != null) {
            delay(120)
            val targetIndex = geofences.indexOfFirst { it.addressLabel == highlightTrigger }
            if (targetIndex >= 0) {
                cardListState.animateScrollToItem(targetIndex + 1) // +1 compensa o header
            }
            delay(1500)
            highlightTrigger = null
        }
    }

    /** Abre o mapa pré-carregado com os dados da entry (modo edição) */
    fun openForEdit(entry: GeofenceEntry) {
        editingEntry = entry
        centerLatLng.value = Pair(entry.centerLat, entry.centerLng)
        radiusKm = entry.radiusKm.toFloat()
        centerLabel = entry.addressLabel
        showFullscreen = true
    }

    /** Abre o mapa em branco (modo criação) */
    fun openForCreate() {
        editingEntry = null
        centerLatLng.value = Pair(-22.8267, -43.0519)
        radiusKm = 1.5f
        centerLabel = ""
        showFullscreen = true
    }

    /** Fecha o dialog e limpa estado de edição */
    fun closeDialog() {
        showFullscreen = false
        editingEntry = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (geofences.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Outlined.TravelExplore,
                title = "Nenhuma região indesejada",
                subtitle = "Use o botão + para adicionar",
                description = "Defina as regiões onde não deseja realizar corridas",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = cardListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(end = 20.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionHeader("REGIÕES (${geofences.size})")
                }
                itemsIndexed(
                    items = geofences,
                    key = { _, entry -> entry.id }
                ) { _, entry ->
                    GeofenceItem(
                        entry = entry,
                        isHighlighted = highlightTrigger == entry.addressLabel,
                        onClick = { openForEdit(entry) },
                        onRemove = { vm.removeGeofence(entry) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { openForCreate() },
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
        val isEditing = editingEntry != null

        Dialog(
            onDismissRequest = { closeDialog() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                containerColor = WarmWhite,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                if (isEditing) "Editar Região" else "Nova Região",
                                color = WarmOnBg,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { closeDialog() }) {
                                Icon(
                                    Icons.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = WarmYellow
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { closeDialog() }) {
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
                                    val newEntry = GeofenceEntry(
                                        centerLat = centerLatLng.value.first,
                                        centerLng = centerLatLng.value.second,
                                        radiusKm = radiusKm.toDouble(),
                                        addressLabel = centerLabel.ifBlank {
                                            "${String.format("%.4f", centerLatLng.value.first)}, ${String.format("%.4f", centerLatLng.value.second)}"
                                        }
                                    )
                                    if (isEditing) {
                                        vm.updateGeofence(newEntry.copy(id = editingEntry!!.id))
                                    } else {
                                        vm.addGeofence(newEntry)
                                    }
                                    highlightTrigger = newEntry.addressLabel
                                    closeDialog()
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
                                Icon(
                                    if (isEditing) Icons.Filled.Save else Icons.Filled.Add,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isEditing) "Salvar Alterações" else "Confirmar Região",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
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
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) WarmYellow.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = if (isHighlighted) 0 else 800),
        label = "highlightGeofenceBg"
    )

    BaseListCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.addressLabel,
                        color = WarmOnBg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${fmt1(entry.radiusKm)} km",
                        color = WarmOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = WarmOnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Remover",
                        tint = RedFinance.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Itens da lista: Termo textual ──────────────────────────────────────────────

@Composable
private fun TextLocationItem(
    location: String,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) WarmYellow.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = if (isHighlighted) 0 else 800),
        label = "highlightTextBg"
    )

    BaseListCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = location,
                    color = WarmOnBg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = WarmOnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Remover",
                        tint = RedFinance.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Dialog de adição/edição de local textual ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLocationDialog(
    blockedLocations: List<String>,
    editingLocation: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val isEditing = editingLocation != null
    var newLocation by remember(editingLocation) { mutableStateOf(editingLocation ?: "") }
    var duplicateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = WarmWhite,
        title = {
            Text(
                if (isEditing) "Editar Local" else "Adicionar Local",
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
                    // Verifica duplicata excluindo o valor que está sendo editado
                    val alreadyExists = blockedLocations.any {
                        it != editingLocation && normalizeForComparison(it) == normalizeForComparison(trimmed)
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
                if (isEditing) {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isEditing) "Salvar" else "Adicionar",
                    fontWeight = FontWeight.Bold
                )
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
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = WarmOnSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun EmptyStatePlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    description: String? = null,
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
        if (description != null) {
            Text(
                text = description,
                color = WarmOnSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
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
