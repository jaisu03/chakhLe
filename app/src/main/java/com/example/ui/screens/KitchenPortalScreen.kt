package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCategoryType
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.Restaurant
import com.example.ui.theme.ChakhLeAmber
import com.example.ui.theme.ChakhLeAmberDark
import com.example.ui.theme.ChakhLeAmberLight
import com.example.ui.theme.ChakhLeBackground
import com.example.ui.theme.ChakhLeBorder
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class KitchenFilter(val label: String) {
    ALL("All Orders"),
    NEW("New (Placed/Accepted)"),
    COOKING("In Kitchen (Preparing)"),
    OUT_FOR_DELIVERY("Dispatched / In Route"),
    DELIVERED("Completed")
}

@Composable
fun KitchenPortalScreen(
    viewModel: ChakhLeViewModel,
    onNavigateBack: () -> Unit,
    onTrackOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    var selectedFilter by remember { mutableStateOf(KitchenFilter.ALL) }
    var showAddKitchenDialog by remember { mutableStateOf(false) }
    var showAddDishDialog by remember { mutableStateOf(false) }

    val filteredOrders = remember(allOrders, selectedFilter) {
        when (selectedFilter) {
            KitchenFilter.ALL -> allOrders
            KitchenFilter.NEW -> allOrders.filter { it.status == OrderStatus.PLACED || it.status == OrderStatus.ACCEPTED }
            KitchenFilter.COOKING -> allOrders.filter { it.status == OrderStatus.PREPARING }
            KitchenFilter.OUT_FOR_DELIVERY -> allOrders.filter { it.status == OrderStatus.OUT_FOR_DELIVERY }
            KitchenFilter.DELIVERED -> allOrders.filter { it.status == OrderStatus.DELIVERED }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChakhLeBackground)
    ) {
        // Top Kitchen Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1E293B),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ChakhLe Kitchen Manager",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                        }
                        Text(
                            text = "Multi-Vendor Order Dispatch Console",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF334155),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${allOrders.size} TOTAL",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Partner / Admin Quick Actions: Add Kitchen & Add Dish
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showAddKitchenDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("add_kitchen_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Register Kitchen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showAddDishDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("add_dish_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Dish", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Filter Tabs Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChakhLeSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(KitchenFilter.values()) { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedFilter = filter },
                    color = if (isSelected) ChakhLeRedPrimary else ChakhLeSurfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = filter.label,
                        color = if (isSelected) Color.White else ChakhLeTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = ChakhLeTextMuted,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Orders in this Queue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Incoming customer orders will appear here automatically.",
                        fontSize = 12.sp,
                        color = ChakhLeTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredOrders, key = { it.orderId }) { order ->
                    KitchenOrderCard(
                        order = order,
                        onUpdateStatus = { newStatus ->
                            viewModel.updateOrderStatusDirectly(order.orderId, newStatus)
                        },
                        onViewCustomerTracker = {
                            viewModel.selectOrderForTracking(order.orderId)
                            onTrackOrder(order.orderId)
                        }
                    )
                }
            }
        }

        if (showAddKitchenDialog) {
            AddKitchenDialog(
                onDismiss = { showAddKitchenDialog = false },
                onRegister = { name, cuisine, time, isVeg, address, price, dishName, dishPrice, category, desc ->
                    val parsedTime = time.filter { it.isDigit() }.toIntOrNull() ?: 25
                    viewModel.registerKitchen(
                        name = name,
                        cuisine = cuisine,
                        deliveryTime = parsedTime,
                        isPureVeg = isVeg,
                        address = address,
                        priceForTwo = price,
                        signatureDishName = dishName,
                        dishPrice = dishPrice,
                        dishCategory = category,
                        dishDescription = desc
                    )
                    showAddKitchenDialog = false
                }
            )
        }

        if (showAddDishDialog) {
            AddDishDialog(
                restaurants = restaurants,
                onDismiss = { showAddDishDialog = false },
                onAddDish = { restId, restName, name, price, category, isVeg, desc ->
                    viewModel.addDishToKitchen(
                        restaurantId = restId,
                        restaurantName = restName,
                        dishName = name,
                        price = price,
                        category = category,
                        isVeg = isVeg,
                        description = desc
                    )
                    showAddDishDialog = false
                }
            )
        }
    }
}

/**
 * Kitchen Order Item Card
 */
@Composable
fun KitchenOrderCard(
    order: OrderEntity,
    onUpdateStatus: (OrderStatus) -> Unit,
    onViewCustomerTracker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = remember(order.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(order.timestamp))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ORDER #${order.orderId}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = ChakhLeTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = timeFormatted,
                                fontSize = 11.sp,
                                color = ChakhLeTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = order.restaurantName,
                        fontSize = 12.sp,
                        color = ChakhLeRedPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Status Badge
                Surface(
                    color = when (order.status) {
                        OrderStatus.ACCEPTED, OrderStatus.PLACED -> ChakhLeAmberLight
                        OrderStatus.PREPARING -> Color(0xFFFFF7ED)
                        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFEFF6FF)
                        OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                        OrderStatus.CANCELLED -> ChakhLeRedContainer
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (order.status) {
                            OrderStatus.ACCEPTED, OrderStatus.PLACED -> ChakhLeAmberDark
                            OrderStatus.PREPARING -> Color(0xFFF97316)
                            OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF3B82F6)
                            OrderStatus.DELIVERED -> ChakhLeSuccess
                            OrderStatus.CANCELLED -> ChakhLeRedPrimary
                        }
                    )
                ) {
                    Text(
                        text = order.status.displayName,
                        color = when (order.status) {
                            OrderStatus.ACCEPTED, OrderStatus.PLACED -> ChakhLeAmberDark
                            OrderStatus.PREPARING -> Color(0xFFEA580C)
                            OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF2563EB)
                            OrderStatus.DELIVERED -> ChakhLeSuccess
                            OrderStatus.CANCELLED -> ChakhLeRedPrimary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 10.dp))

            // Items List
            Text(
                text = "KITCHEN DISHES (${order.itemsCount} items):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ChakhLeTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.itemsSummary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChakhLeTextPrimary
            )

            if (order.deliveryNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Special Request: ${order.deliveryNotes}",
                        color = Color(0xFF92400E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer & Delivery Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Customer: ${order.customerName} (${order.customerPhone})",
                        fontSize = 12.sp,
                        color = ChakhLeTextSecondary
                    )
                    Text(
                        text = "Address: ${order.deliveryAddress}",
                        fontSize = 11.sp,
                        color = ChakhLeTextMuted,
                        maxLines = 1
                    )
                }

                Text(
                    text = "₹${order.totalAmount.toInt()} (${order.paymentMethod})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeTextPrimary
                )
            }

            HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 12.dp))

            // Kitchen Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (order.status) {
                    OrderStatus.PLACED -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.ACCEPTED) },
                            colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("Accept Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.ACCEPTED -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PREPARING) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Cooking (In Kitchen)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.OUT_FOR_DELIVERY) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Handover to Rider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.OUT_FOR_DELIVERY -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                            colors = ButtonDefaults.buttonColors(containerColor = ChakhLeSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark as Delivered", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.DELIVERED -> {
                        OutlinedButton(
                            onClick = { onUpdateStatus(OrderStatus.ACCEPTED) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("Reset / Restart Order", fontSize = 11.sp, color = ChakhLeTextSecondary)
                        }
                    }
                    OrderStatus.CANCELLED -> {
                        Text(
                            text = "Order Cancelled",
                            fontSize = 12.sp,
                            color = ChakhLeRedPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onViewCustomerTracker,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeTextPrimary),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Customer View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddKitchenDialog(
    onDismiss: () -> Unit,
    onRegister: (
        name: String,
        cuisine: String,
        deliveryTime: String,
        isPureVeg: Boolean,
        address: String,
        priceForTwo: Int,
        signatureDishName: String,
        dishPrice: Double,
        dishCategory: FoodCategoryType,
        dishDescription: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("North Indian & Biryani") }
    var deliveryTime by remember { mutableStateOf("25-35 mins") }
    var address by remember { mutableStateOf("Main Market Road, City Centre") }
    var priceForTwo by remember { mutableStateOf("350") }
    var isPureVeg by remember { mutableStateOf(false) }

    var signatureDishName by remember { mutableStateOf("") }
    var dishPrice by remember { mutableStateOf("220") }
    var selectedCategory by remember { mutableStateOf(FoodCategoryType.BIRYANI) }
    var dishDescription by remember { mutableStateOf("Chef's special recipe prepared fresh with rich spices.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Register Kitchen Partner",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ChakhLeTextPrimary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ChakhLeTextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "RESTAURANT / KITCHEN DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeRedPrimary,
                    letterSpacing = 0.5.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kitchen Name *") },
                    placeholder = { Text("e.g. Punjabi Rasoi Express") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChakhLeRedPrimary,
                        unfocusedBorderColor = ChakhLeBorder
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cuisine,
                    onValueChange = { cuisine = it },
                    label = { Text("Cuisine Types") },
                    placeholder = { Text("e.g. North Indian, Mughlai, Biryani") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChakhLeRedPrimary,
                        unfocusedBorderColor = ChakhLeBorder
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryTime,
                        onValueChange = { deliveryTime = it },
                        label = { Text("Delivery Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChakhLeRedPrimary,
                            unfocusedBorderColor = ChakhLeBorder
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = priceForTwo,
                        onValueChange = { if (it.all { char -> char.isDigit() }) priceForTwo = it },
                        label = { Text("Price for 2 (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChakhLeRedPrimary,
                            unfocusedBorderColor = ChakhLeBorder
                        ),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Kitchen Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChakhLeRedPrimary,
                        unfocusedBorderColor = ChakhLeBorder
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pure Vegetarian Kitchen",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChakhLeTextPrimary
                    )
                    Switch(
                        checked = isPureVeg,
                        onCheckedChange = { isPureVeg = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ChakhLeVegGreen
                        )
                    )
                }

                HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "INITIAL SIGNATURE DISH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeRedPrimary,
                    letterSpacing = 0.5.sp
                )

                OutlinedTextField(
                    value = signatureDishName,
                    onValueChange = { signatureDishName = it },
                    label = { Text("Dish Name *") },
                    placeholder = { Text("e.g. Special Paneer Butter Masala") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChakhLeRedPrimary,
                        unfocusedBorderColor = ChakhLeBorder
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dishPrice,
                        onValueChange = { if (it.all { char -> char.isDigit() }) dishPrice = it },
                        label = { Text("Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChakhLeRedPrimary,
                            unfocusedBorderColor = ChakhLeBorder
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChakhLeRedPrimary,
                            unfocusedBorderColor = ChakhLeBorder
                        ),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = dishDescription,
                    onValueChange = { dishDescription = it },
                    label = { Text("Dish Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChakhLeRedPrimary,
                        unfocusedBorderColor = ChakhLeBorder
                    ),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPriceForTwo = priceForTwo.toIntOrNull() ?: 300
                    val finalDishPrice = dishPrice.toDoubleOrNull() ?: 180.0
                    val finalSignatureDish = if (signatureDishName.isNotBlank()) signatureDishName else "Special Combo Thali"
                    onRegister(
                        name,
                        cuisine,
                        deliveryTime,
                        isPureVeg,
                        address,
                        finalPriceForTwo,
                        finalSignatureDish,
                        finalDishPrice,
                        selectedCategory,
                        dishDescription
                    )
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Register & Launch Kitchen", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddDishDialog(
    restaurants: List<Restaurant>,
    onDismiss: () -> Unit,
    onAddDish: (
        restaurantId: String,
        restaurantName: String,
        dishName: String,
        price: Double,
        category: FoodCategoryType,
        isVeg: Boolean,
        description: String
    ) -> Unit
) {
    var selectedRestaurantIndex by remember { mutableStateOf(0) }
    var dishName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("199") }
    var isVeg by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf(FoodCategoryType.BIRYANI) }
    var description by remember { mutableStateOf("Prepared fresh in traditional spices with premium ingredients.") }

    val currentRestaurant = restaurants.getOrNull(selectedRestaurantIndex) ?: restaurants.firstOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add Dish to Menu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ChakhLeTextPrimary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ChakhLeTextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SELECT KITCHEN / RESTAURANT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeRedPrimary,
                    letterSpacing = 0.5.sp
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(restaurants) { rest ->
                        val isSelected = currentRestaurant?.id == rest.id
                        Surface(
                            color = if (isSelected) ChakhLeRedPrimary else ChakhLeSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                selectedRestaurantIndex = restaurants.indexOf(rest)
                            }
                        ) {
                            Text(
                                text = rest.name,
                                color = if (isSelected) Color.White else ChakhLeTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 4.dp))

                OutlinedTextField(
                    value = dishName,
                    onValueChange = { dishName = it },
                    label = { Text("Dish Name *") },
                    placeholder = { Text("e.g. Hyderabadi Dum Biryani") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChakhLeRedPrimary,
                        unfocusedBorderColor = ChakhLeBorder
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.all { char -> char.isDigit() }) price = it },
                        label = { Text("Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChakhLeRedPrimary,
                            unfocusedBorderColor = ChakhLeBorder
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChakhLeRedPrimary,
                            unfocusedBorderColor = ChakhLeBorder
                        ),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pure Vegetarian Dish",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChakhLeTextPrimary
                    )
                    Switch(
                        checked = isVeg,
                        onCheckedChange = { isVeg = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ChakhLeVegGreen
                        )
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Ingredients") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChakhLeRedPrimary,
                        unfocusedBorderColor = ChakhLeBorder
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentRestaurant != null && dishName.isNotBlank()) {
                        val finalPrice = price.toDoubleOrNull() ?: 150.0
                        onAddDish(
                            currentRestaurant.id,
                            currentRestaurant.name,
                            dishName,
                            finalPrice,
                            selectedCategory,
                            isVeg,
                            description
                        )
                    }
                },
                enabled = dishName.isNotBlank() && currentRestaurant != null,
                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add to Live Menu", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
