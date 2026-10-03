package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.FirestoreSyncState
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.ChakhLeAmber
import com.example.ui.theme.ChakhLeAmberDark
import com.example.ui.theme.ChakhLeAmberLight
import com.example.ui.theme.ChakhLeBackground
import com.example.ui.theme.ChakhLeBorder
import com.example.ui.theme.ChakhLeGold
import com.example.ui.theme.ChakhLeRedContainer
import com.example.ui.theme.ChakhLeRedPrimary
import com.example.ui.theme.ChakhLeSuccess
import com.example.ui.theme.ChakhLeSurface
import com.example.ui.theme.ChakhLeSurfaceVariant
import com.example.ui.theme.ChakhLeTextMuted
import com.example.ui.theme.ChakhLeTextPrimary
import com.example.ui.theme.ChakhLeTextSecondary
import com.example.ui.theme.ChakhLeVegGreen
import com.example.ui.viewmodel.ChakhLeViewModel

@Composable
fun OrderTrackingScreen(
    viewModel: ChakhLeViewModel,
    orderIdParam: String?,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val latestActiveOrder by viewModel.latestActiveOrder.collectAsState()
    val selectedTrackingOrderId by viewModel.selectedTrackingOrderId.collectAsState()
    val firestoreUpdate by viewModel.firestoreOrderUpdate.collectAsState()
    val context = LocalContext.current

    // Determine initial target order
    val initialTarget = remember(allOrders, selectedTrackingOrderId, orderIdParam, latestActiveOrder) {
        val targetId = orderIdParam ?: selectedTrackingOrderId
        if (targetId != null) {
            allOrders.find { it.orderId == targetId }
        } else {
            latestActiveOrder ?: allOrders.firstOrNull()
        }
    }

    // Connect real-time Firestore Snapshot Listener when order changes
    LaunchedEffect(initialTarget?.orderId) {
        initialTarget?.orderId?.let { id ->
            viewModel.startObservingOrderInFirestore(id)
        }
    }

    // Active order priority: Real-time Firestore snapshot > Local Room entity
    val activeOrder = firestoreUpdate?.order ?: initialTarget
    val syncState = firestoreUpdate?.syncState ?: FirestoreSyncState.CONNECTING
    val updateCount = firestoreUpdate?.updateCount ?: 0
    val dynamicStageNote = firestoreUpdate?.stageNote ?: ""

    val scrollState = rememberScrollState()

    // Arrival timer countdown state
    var minutesRemaining by remember(activeOrder?.orderId) {
        mutableIntStateOf(activeOrder?.estimatedArrivalMinutes ?: 18)
    }

    LaunchedEffect(activeOrder?.orderId, activeOrder?.status) {
        minutesRemaining = when (activeOrder?.status) {
            OrderStatus.DELIVERED -> 0
            OrderStatus.OUT_FOR_DELIVERY -> 6
            OrderStatus.PREPARING -> 12
            OrderStatus.ACCEPTED -> 18
            OrderStatus.PLACED -> 22
            else -> 0
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChakhLeBackground)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ChakhLeSurface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateHome,
                        modifier = Modifier.testTag("tracking_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ChakhLeTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Live Order Tracking",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ChakhLeTextPrimary
                        )
                        if (activeOrder != null) {
                            Text(
                                text = "Order #${activeOrder.orderId} • ${activeOrder.restaurantName}",
                                fontSize = 12.sp,
                                color = ChakhLeTextSecondary
                            )
                        }
                    }
                }

                if (activeOrder != null) {
                    IconButton(
                        onClick = {
                            viewModel.startObservingOrderInFirestore(activeOrder.orderId)
                            Toast.makeText(context, "Reconnected Firestore listener", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("refresh_firestore_listener")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Real-time Listener",
                            tint = ChakhLeRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (activeOrder == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = ChakhLeTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Active Orders to Track",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Place an order from any cloud kitchen to track live preparation and delivery!",
                        fontSize = 13.sp,
                        color = ChakhLeTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateHome,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                        modifier = Modifier.testTag("explore_kitchens_button")
                    ) {
                        Text("Explore Kitchens", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Live Firestore Real-Time Listener Status Card
                FirestoreListenerStatusBanner(
                    syncState = syncState,
                    orderId = activeOrder.orderId,
                    updateCount = updateCount,
                    onReconnect = { viewModel.startObservingOrderInFirestore(activeOrder.orderId) }
                )

                // 3. Estimated Arrival Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = when (activeOrder.status) {
                                        OrderStatus.DELIVERED -> "Order Delivered! 🎉"
                                        OrderStatus.OUT_FOR_DELIVERY -> "Rider Out For Delivery 🛵"
                                        OrderStatus.PREPARING -> "Kitchen Preparing Meal 👨‍🍳"
                                        else -> "Order Confirmed by Restaurant"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (activeOrder.status == OrderStatus.DELIVERED) ChakhLeSuccess else ChakhLeTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (activeOrder.status == OrderStatus.DELIVERED)
                                        "Delivered to your address"
                                    else
                                        "Arriving in ~$minutesRemaining mins",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (activeOrder.status == OrderStatus.DELIVERED) ChakhLeSuccess else ChakhLeRedPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (activeOrder.status) {
                                            OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                                            OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFE0F2FE)
                                            OrderStatus.PREPARING -> Color(0xFFFEF3C7)
                                            else -> ChakhLeRedContainer
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (activeOrder.status) {
                                        OrderStatus.DELIVERED -> Icons.Default.CheckCircle
                                        OrderStatus.OUT_FOR_DELIVERY -> Icons.Default.DeliveryDining
                                        OrderStatus.PREPARING -> Icons.Default.Restaurant
                                        else -> Icons.Default.Timer
                                    },
                                    contentDescription = "Status Icon",
                                    tint = when (activeOrder.status) {
                                        OrderStatus.DELIVERED -> ChakhLeSuccess
                                        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF0284C7)
                                        OrderStatus.PREPARING -> ChakhLeAmberDark
                                        else -> ChakhLeRedPrimary
                                    },
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Dynamic Firestore Stage Note Banner
                        if (dynamicStageNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = ChakhLeSurfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = ChakhLeAmberDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dynamicStageNote,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ChakhLeTextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Delivering to: ${activeOrder.deliveryAddress}",
                            fontSize = 12.sp,
                            color = ChakhLeTextSecondary,
                            maxLines = 2
                        )

                        // Delivery Handover PIN
                        if (activeOrder.status != OrderStatus.DELIVERED && activeOrder.status != OrderStatus.CANCELLED) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Handover OTP",
                                            tint = ChakhLeVegGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Delivery Handover OTP",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF166534)
                                            )
                                            Text(
                                                text = "Share with rider only after food is received",
                                                fontSize = 10.sp,
                                                color = Color(0xFF15803D)
                                            )
                                        }
                                    }

                                    val deliveryPin = activeOrder.orderId.filter { it.isDigit() }.takeLast(4).ifBlank { "5921" }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                        ) {
                                            Text(
                                                text = deliveryPin,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                letterSpacing = 2.sp,
                                                color = Color(0xFF166534),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                clipboard?.setPrimaryClip(ClipData.newPlainText("Delivery OTP", deliveryPin))
                                                Toast.makeText(context, "Delivery OTP $deliveryPin copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Copy OTP",
                                                tint = Color(0xFF166534),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Special Focus: PREPARING IN KITCHEN Spotlight Card
                AnimatedVisibility(
                    visible = activeOrder.status == OrderStatus.PREPARING,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    PreparingStageSpotlightCard(
                        restaurantName = activeOrder.restaurantName,
                        itemsSummary = activeOrder.itemsSummary
                    )
                }

                // 5. Special Focus: OUT FOR DELIVERY Spotlight Card
                AnimatedVisibility(
                    visible = activeOrder.status == OrderStatus.OUT_FOR_DELIVERY,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutForDeliverySpotlightCard(
                        riderName = activeOrder.riderName,
                        riderVehicle = activeOrder.riderVehicle
                    )
                }

                // 6. Dynamic Live GPS Route Visualizer
                LiveDeliveryRouteCanvas(
                    status = activeOrder.status,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )

                // 7. Step-by-Step Animated Real-Time Stepper (Showing Preparing ➔ Out for Delivery)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REAL-TIME ORDER STATUS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeTextMuted,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ChakhLeSurfaceVariant
                            ) {
                                Text(
                                    text = "Firestore Sync",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChakhLeRedPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val steps = listOf(
                            OrderStatus.ACCEPTED to ("1. Order Confirmed by Kitchen" to "Restaurant verified details and accepted order"),
                            OrderStatus.PREPARING to ("2. Food Being Prepared" to "Master Chef cooking authentic meal with fresh spices"),
                            OrderStatus.OUT_FOR_DELIVERY to ("3. Out for Delivery" to "Rider picked up hot parcel & en route to you"),
                            OrderStatus.DELIVERED to ("4. Delivered at Doorstep" to "Order completed. Enjoy your feast!")
                        )

                        steps.forEachIndexed { index, (stepStatus, textPair) ->
                            val (title, subtitle) = textPair
                            val isCompleted = activeOrder.status.stepIndex > stepStatus.stepIndex || (activeOrder.status == OrderStatus.DELIVERED)
                            val isCurrent = activeOrder.status == stepStatus && activeOrder.status != OrderStatus.DELIVERED

                            TrackingStepRow(
                                title = title,
                                subtitle = subtitle,
                                isCompleted = isCompleted,
                                isCurrent = isCurrent,
                                isLast = index == steps.size - 1
                            )
                        }
                    }
                }

                // 8. Delivery Rider Details Card with Click-to-Call & WhatsApp
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DELIVERY PARTNER DETAILS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChakhLeTextMuted,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(ChakhLeRedPrimary, ChakhLeAmber)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeliveryDining,
                                        contentDescription = "Rider Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = activeOrder.riderName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = ChakhLeTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = ChakhLeVegGreen,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${activeOrder.riderRating}",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(9.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = activeOrder.riderVehicle,
                                        fontSize = 12.sp,
                                        color = ChakhLeTextSecondary
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // WhatsApp rider button
                                Button(
                                    onClick = {
                                        val riderDigits = activeOrder.riderPhone.filter { it.isDigit() }
                                        val phone = if (riderDigits.startsWith("91") && riderDigits.length == 12) riderDigits else "91$riderDigits"
                                        val msg = "Hi ${activeOrder.riderName}, regarding my ChakhLe order #${activeOrder.orderId.takeLast(6)}"
                                        val encoded = Uri.encode(msg)
                                        val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encoded")
                                            setPackage("com.whatsapp")
                                        }
                                        try {
                                            context.startActivity(sendIntent)
                                        } catch (e: Exception) {
                                            val fallback = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encoded")
                                            }
                                            try {
                                                context.startActivity(fallback)
                                            } catch (_: Exception) {}
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .testTag("whatsapp_rider_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "WhatsApp Rider",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Click-to-Call Button
                                Button(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${activeOrder.riderPhone}")
                                        }
                                        try {
                                            context.startActivity(dialIntent)
                                        } catch (_: Exception) {}
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeVegGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .testTag("call_rider_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // 9. Ordered Items Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ORDER SUMMARY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeTextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Paid: ₹${activeOrder.totalAmount.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = ChakhLeTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = activeOrder.itemsSummary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChakhLeTextPrimary
                        )

                        if (activeOrder.deliveryNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notes: ${activeOrder.deliveryNotes}",
                                fontSize = 11.sp,
                                color = ChakhLeTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Real-time Firestore Listener Connection Status Banner with Animated Radar Dot
 */
@Composable
fun FirestoreListenerStatusBanner(
    syncState: FirestoreSyncState,
    orderId: String,
    updateCount: Int,
    onReconnect: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_alpha"
    )

    val bannerBg = when (syncState) {
        FirestoreSyncState.REALTIME_CONNECTED -> Color(0xFFF0FDF4)
        FirestoreSyncState.CONNECTING -> Color(0xFFFEFCE8)
        FirestoreSyncState.CACHE_SYNC, FirestoreSyncState.OFFLINE_SYNC -> Color(0xFFF0F9FF)
        else -> Color(0xFFFFF1F2)
    }

    val bannerBorder = when (syncState) {
        FirestoreSyncState.REALTIME_CONNECTED -> Color(0xFF86EFAC)
        FirestoreSyncState.CONNECTING -> Color(0xFFFDE047)
        FirestoreSyncState.CACHE_SYNC, FirestoreSyncState.OFFLINE_SYNC -> Color(0xFFBAE6FD)
        else -> Color(0xFFFECDD3)
    }

    val dotColor = when (syncState) {
        FirestoreSyncState.REALTIME_CONNECTED -> Color(0xFF16A34A)
        FirestoreSyncState.CONNECTING -> Color(0xFFCA8A04)
        FirestoreSyncState.CACHE_SYNC, FirestoreSyncState.OFFLINE_SYNC -> Color(0xFF0284C7)
        else -> Color(0xFFE11D48)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("firestore_status_banner"),
        shape = RoundedCornerShape(12.dp),
        color = bannerBg,
        border = BorderStroke(1.dp, bannerBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Radar Pulse Indicator
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (syncState == FirestoreSyncState.REALTIME_CONNECTED) {
                        Box(
                            modifier = Modifier
                                .size((12 * pulseScale).dp)
                                .clip(CircleShape)
                                .background(dotColor.copy(alpha = pulseAlpha))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = syncState.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (syncState == FirestoreSyncState.REALTIME_CONNECTED) Color(0xFF166534) else Color(0xFF854D0E)
                        )
                        if (updateCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = dotColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Event #$updateCount",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dotColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Listening to path: orders/$orderId",
                        fontSize = 10.sp,
                        color = ChakhLeTextSecondary
                    )
                }
            }

            IconButton(
                onClick = onReconnect,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync",
                    tint = dotColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


/**
 * Dedicated Visual Spotlight when Order is 'PREPARING IN KITCHEN'
 */
@Composable
fun PreparingStageSpotlightCard(
    restaurantName: String,
    itemsSummary: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chef_steam")
    val steamAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "steam_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("preparing_spotlight_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.5.dp, ChakhLeAmber)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ChakhLeAmber.copy(alpha = steamAlpha)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Cooking",
                            tint = Color(0xFF78350F),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "👨‍🍳 CHEF IS PREPARING YOUR ORDER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF78350F),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = restaurantName,
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "Cooking ~12m",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { 0.55f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ChakhLeAmberDark,
                trackColor = Color(0xFFFDE68A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cooking Milestones Checklist
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CookingMilestoneItem(
                    label = "Fresh gourmet ingredients chopped & prepped",
                    isDone = true
                )
                CookingMilestoneItem(
                    label = "Slow-cooking with authentic spices in progress",
                    isDone = true,
                    isActive = true
                )
                CookingMilestoneItem(
                    label = "Tamper-proof safety sealing & parcel packaging",
                    isDone = false
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Dishes in pan: $itemsSummary",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF78350F),
                maxLines = 1
            )
        }
    }
}

@Composable
fun CookingMilestoneItem(label: String, isDone: Boolean, isActive: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone && !isActive -> Color(0xFF16A34A)
                        isActive -> ChakhLeAmberDark
                        else -> Color(0xFFD1D5DB)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone && !isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            } else if (isActive) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color(0xFF78350F) else Color(0xFF4B5563)
        )
    }
}

/**
 * Dedicated Visual Spotlight when Order is 'OUT FOR DELIVERY'
 */
@Composable
fun OutForDeliverySpotlightCard(
    riderName: String,
    riderVehicle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("out_for_delivery_spotlight_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        border = BorderStroke(1.5.dp, Color(0xFF38BDF8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = "Rider en route",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🛵 RIDER DISPATCHED & EN ROUTE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0369A1),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$riderName • $riderVehicle",
                            fontSize = 11.sp,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFBAE6FD)
                ) {
                    Text(
                        text = "ETA ~6 mins",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Your food parcel is secured in an insulated thermal carrier to keep it steaming hot. Rider is en route to your doorstep!",
                fontSize = 11.sp,
                color = Color(0xFF075985),
                lineHeight = 15.sp
            )
        }
    }
}

/**
 * Step row in tracker
 */
@Composable
fun TrackingStepRow(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Circle indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> ChakhLeSuccess
                            isCurrent -> ChakhLeRedPrimary.copy(alpha = pulseAlpha)
                            else -> ChakhLeBorder
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(if (isCompleted) ChakhLeSuccess else ChakhLeBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 18.dp)) {
            Text(
                text = title,
                fontWeight = if (isCompleted || isCurrent) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = when {
                    isCompleted -> ChakhLeSuccess
                    isCurrent -> ChakhLeRedPrimary
                    else -> ChakhLeTextMuted
                }
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isCurrent) ChakhLeTextPrimary else ChakhLeTextSecondary
            )
        }
    }
}

/**
 * Animated Map Route Canvas with Moving Vehicle Marker
 */
@Composable
fun LiveDeliveryRouteCanvas(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bikeMovement")
    val bikeProgress by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bikeProgress"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Road grid background
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, h * 0.5f),
                    end = Offset(w, h * 0.5f),
                    strokeWidth = 32f
                )

                // Dotted route line
                val routePath = Path().apply {
                    moveTo(w * 0.15f, h * 0.5f)
                    cubicTo(w * 0.35f, h * 0.25f, w * 0.65f, h * 0.75f, w * 0.85f, h * 0.5f)
                }

                drawPath(
                    path = routePath,
                    color = ChakhLeRedPrimary,
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                )

                // Restaurant Origin Pin
                drawCircle(
                    color = Color(0xFFE23744),
                    radius = 16f,
                    center = Offset(w * 0.15f, h * 0.5f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(w * 0.15f, h * 0.5f)
                )

                // Customer Destination Pin
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 18f,
                    center = Offset(w * 0.85f, h * 0.5f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(w * 0.85f, h * 0.5f)
                )

                // Dynamic Bike / Vehicle Progress
                val currentBikePos = when (status) {
                    OrderStatus.PLACED, OrderStatus.ACCEPTED -> 0.15f
                    OrderStatus.PREPARING -> 0.28f
                    OrderStatus.OUT_FOR_DELIVERY -> bikeProgress
                    OrderStatus.DELIVERED -> 0.85f
                    OrderStatus.CANCELLED -> 0.15f
                }

                val currentBikeX = w * currentBikePos
                val currentBikeY = h * 0.5f + (if (currentBikePos > 0.3f && currentBikePos < 0.7f) (currentBikePos - 0.5f) * 40f else 0f)

                drawCircle(
                    color = ChakhLeAmber.copy(alpha = 0.25f),
                    radius = 28f,
                    center = Offset(currentBikeX, currentBikeY)
                )
                drawCircle(
                    color = ChakhLeAmberDark,
                    radius = 14f,
                    center = Offset(currentBikeX, currentBikeY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = Offset(currentBikeX, currentBikeY)
                )
            }

            // Pin Labels Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "🏪 Cloud Kitchen",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "🏠 Delivery Address",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
