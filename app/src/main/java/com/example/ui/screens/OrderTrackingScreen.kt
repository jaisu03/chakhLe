package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kotlinx.coroutines.delay

@Composable
fun OrderTrackingScreen(
    viewModel: ChakhLeViewModel,
    orderIdParam: String?,
    onNavigateHome: () -> Unit,
    onNavigateToKitchen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val latestActiveOrder by viewModel.latestActiveOrder.collectAsState()
    val selectedTrackingOrderId by viewModel.selectedTrackingOrderId.collectAsState()
    val context = LocalContext.current

    val activeOrder = remember(allOrders, selectedTrackingOrderId, orderIdParam, latestActiveOrder) {
        val targetId = orderIdParam ?: selectedTrackingOrderId
        if (targetId != null) {
            allOrders.find { it.orderId == targetId }
        } else {
            latestActiveOrder ?: allOrders.firstOrNull()
        }
    }

    val scrollState = rememberScrollState()

    // Arrival timer countdown state
    var minutesRemaining by remember(activeOrder?.orderId) {
        mutableIntStateOf(activeOrder?.estimatedArrivalMinutes ?: 18)
    }

    LaunchedEffect(activeOrder?.orderId, activeOrder?.status) {
        if (activeOrder?.status == OrderStatus.DELIVERED) {
            minutesRemaining = 0
        } else if (activeOrder?.status == OrderStatus.OUT_FOR_DELIVERY) {
            minutesRemaining = 8
        } else if (activeOrder?.status == OrderStatus.PREPARING) {
            minutesRemaining = 14
        } else {
            minutesRemaining = 18
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
                    IconButton(onClick = onNavigateHome) {
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

                OutlinedButton(
                    onClick = onNavigateToKitchen,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeAmberDark),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Kitchen",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kitchen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        text = "Place an order from the menu to track live preparation and delivery!",
                        fontSize = 13.sp,
                        color = ChakhLeTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateHome,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary)
                    ) {
                        Text("Explore Restaurants", fontWeight = FontWeight.Bold)
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
                // 1. Estimated Arrival Timer & Header Card
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
                                    text = if (activeOrder.status == OrderStatus.DELIVERED) "Order Delivered! 🎉" else "Estimated Arrival",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (activeOrder.status == OrderStatus.DELIVERED) ChakhLeSuccess else ChakhLeTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (activeOrder.status == OrderStatus.DELIVERED)
                                        "Delivered at ${activeOrder.deliveryAddress.take(24)}..."
                                    else
                                        "Arriving in $minutesRemaining mins",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (activeOrder.status == OrderStatus.DELIVERED) ChakhLeSuccess else ChakhLeRedPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (activeOrder.status == OrderStatus.DELIVERED) Color(0xFFE8F5E9) else ChakhLeRedContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (activeOrder.status == OrderStatus.DELIVERED) Icons.Default.CheckCircle else Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = if (activeOrder.status == OrderStatus.DELIVERED) ChakhLeSuccess else ChakhLeRedPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Delivering to: ${activeOrder.deliveryAddress}",
                            fontSize = 12.sp,
                            color = ChakhLeTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // 2. Dynamic Live GPS Route Visualizer
                LiveDeliveryRouteCanvas(
                    status = activeOrder.status,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )

                // 3. Step-by-Step Animated Progress Stepper
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ORDER STATUS PROGRESS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChakhLeTextMuted,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val steps = listOf(
                            OrderStatus.ACCEPTED to ("Order Confirmed by Restaurant" to "Restaurant accepted & sent to kitchen"),
                            OrderStatus.PREPARING to ("Food Being Prepared in Kitchen" to "Fresh spices & gourmet cooking in progress"),
                            OrderStatus.OUT_FOR_DELIVERY to ("Rider Picked Up & Out for Delivery" to "Rider is en route to your doorstep"),
                            OrderStatus.DELIVERED to ("Delivered at Doorstep" to "Order completed. Enjoy your meal!")
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

                // 4. Delivery Rider Details Card with Click-to-Call
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
                                // Rider Photo Avatar
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

                            // Working Click-to-Call Button
                            Button(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${activeOrder.riderPhone}")
                                    }
                                    try {
                                        context.startActivity(dialIntent)
                                    } catch (e: Exception) {
                                        // Ignore if dialer unavailable
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeVegGreen),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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

                // 5. Kitchen Dispatch & Live Status Controller
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChakhLeBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = ChakhLeRedPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Kitchen & Dispatch Actions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ChakhLeTextPrimary
                                )
                            }

                            Surface(
                                color = ChakhLeAmberLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LIVE ORDER",
                                    color = ChakhLeAmberDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Manage kitchen preparation milestones or transition directly to the Kitchen Manager console.",
                            fontSize = 11.sp,
                            color = ChakhLeTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.advanceOrderStatus(activeOrder.orderId, activeOrder.status)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("advance_order_status_btn")
                            ) {
                                Text(
                                    text = when (activeOrder.status) {
                                        OrderStatus.PLACED -> "Accept Order"
                                        OrderStatus.ACCEPTED -> "Send to Kitchen"
                                        OrderStatus.PREPARING -> "Rider Pickup"
                                        OrderStatus.OUT_FOR_DELIVERY -> "Mark Delivered"
                                        OrderStatus.DELIVERED -> "Order Completed"
                                        OrderStatus.CANCELLED -> "Restart Order"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onNavigateToKitchen,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeTextPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text("Kitchen Portal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 6. Ordered Items Summary Card
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

                Spacer(modifier = Modifier.height(16.dp))
            }
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
                            isCurrent -> ChakhLeAmberDark.copy(alpha = pulseAlpha)
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
                        .height(34.dp)
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
 * Animated Map Route Canvas
 */
@Composable
fun LiveDeliveryRouteCanvas(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bikeMovement")
    val bikeProgress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
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

                // Road grid lines background
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

                // Moving Bike / Status indicator
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
                        text = "🏪 Kitchen",
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
                        text = "🏠 Your Home",
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
