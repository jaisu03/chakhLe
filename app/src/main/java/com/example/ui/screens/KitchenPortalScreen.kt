package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dish
import com.example.data.model.FoodCategoryType
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.Restaurant
import com.example.data.model.UserRole
import com.example.ui.theme.ChakhLeAmber
import com.example.ui.theme.ChakhLeAmberDark
import com.example.ui.theme.ChakhLeAmberLight
import com.example.ui.theme.ChakhLeBackground
import com.example.ui.theme.ChakhLeBorder
import com.example.ui.theme.ChakhLeRedContainer
import com.example.ui.theme.ChakhLeRedPrimary
import com.example.ui.theme.ChakhLeSlate100
import com.example.ui.theme.ChakhLeSlate200
import com.example.ui.theme.ChakhLeSlate400
import com.example.ui.theme.ChakhLeSlate700
import com.example.ui.theme.ChakhLeSlate800
import com.example.ui.theme.ChakhLeSlate900
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

/**
 * 4 Explicit Order Lifecycle Stages requested by user:
 * RECEIVED -> ACCEPTED -> COOKING -> DELIVERED
 */
enum class KitchenStage(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    RECEIVED("1. Received", Icons.Default.NotificationsActive),
    ACCEPTED("2. Accepted", Icons.Default.CheckCircle),
    COOKING("3. Cooking", Icons.Default.SoupKitchen),
    DELIVERED("4. Delivered", Icons.Default.DeliveryDining),
    MENU("Kitchen Menu", Icons.Default.Restaurant)
}

@Composable
fun KitchenPortalScreen(
    viewModel: ChakhLeViewModel,
    onNavigateBack: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    val currentKitchenId by viewModel.currentKitchenId.collectAsState()
    val allDishes by viewModel.allDishes.collectAsState()

    // Determine current active kitchen
    val currentKitchen = remember(restaurants, currentKitchenId) {
        restaurants.find { it.id == currentKitchenId } ?: restaurants.firstOrNull()
    }

    var selectedStage by remember { mutableStateOf(KitchenStage.RECEIVED) }
    var showKitchenDropdown by remember { mutableStateOf(false) }
    var showAddDishDialog by remember { mutableStateOf(false) }

    // Filter orders belonging to this kitchen (or all if fallback)
    val kitchenOrders = remember(allOrders, currentKitchen) {
        if (currentKitchen == null) {
            allOrders
        } else {
            allOrders.filter { order ->
                order.restaurantName.contains(currentKitchen.name, ignoreCase = true) ||
                    currentKitchen.name.contains(order.restaurantName, ignoreCase = true)
            }.ifEmpty { allOrders } // Fallback to all orders so test orders are always visible
        }
    }

    // Stage order groupings
    val receivedOrders = remember(kitchenOrders) { kitchenOrders.filter { it.status == OrderStatus.PLACED } }
    val acceptedOrders = remember(kitchenOrders) { kitchenOrders.filter { it.status == OrderStatus.ACCEPTED } }
    val cookingOrders = remember(kitchenOrders) { kitchenOrders.filter { it.status == OrderStatus.PREPARING } }
    val deliveredOrders = remember(kitchenOrders) {
        kitchenOrders.filter { it.status == OrderStatus.OUT_FOR_DELIVERY || it.status == OrderStatus.DELIVERED }
    }

    // Current Kitchen's dishes
    val kitchenDishes = remember(allDishes, currentKitchen) {
        if (currentKitchen == null) allDishes
        else allDishes.filter { it.restaurantId == currentKitchen.id || it.restaurantName == currentKitchen.name }
    }

    // Kitchen Financials
    val todayEarnings = remember(kitchenOrders) {
        kitchenOrders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChakhLeBackground)
    ) {
        // Kitchen Staff Master Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ChakhLeSlate900,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Top Row: Kitchen Title & Global Role Navigators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Kitchen Portal",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFEA580C),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "STAFF VIEW",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Order Lifecycle: Received ➔ Accepted ➔ Cooking ➔ Delivered",
                                fontSize = 10.sp,
                                color = ChakhLeSlate400
                            )
                        }
                    }

                    // Navigation to Admin or Customer
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.loginAsAdmin()
                                onNavigateToAdmin()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF60A5FA)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.loginAsCustomer()
                                onNavigateBack()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Foodie App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Kitchen Selector & Online Toggle Card
                currentKitchen?.let { kitchen ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ChakhLeSlate800,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Kitchen Dropdown Trigger
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { showKitchenDropdown = true }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = ChakhLeAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = kitchen.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ExpandMore,
                                                contentDescription = "Switch",
                                                tint = ChakhLeSlate400,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = "${kitchen.cuisine} • ${kitchen.address}",
                                            color = ChakhLeSlate400,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showKitchenDropdown,
                                    onDismissRequest = { showKitchenDropdown = false }
                                ) {
                                    Text(
                                        text = "SWITCH KITCHEN BRANCH",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ChakhLeTextMuted,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                    restaurants.forEach { rest ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(text = rest.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(text = "${rest.cuisine} • ${if (rest.isOpen) "Open" else "Closed"}", fontSize = 11.sp, color = ChakhLeTextSecondary)
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectKitchen(rest.id)
                                                showKitchenDropdown = false
                                            }
                                        )
                                    }
                                    HorizontalDivider()
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "🔒 Kitchen creation is restricted to Super Admin only.",
                                            fontSize = 11.sp,
                                            color = ChakhLeTextMuted,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Store Open/Busy Toggle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (kitchen.isOpen) "ONLINE" else "BUSY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (kitchen.isOpen) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                                    )
                                    Text(
                                        text = if (kitchen.isOpen) "Taking Orders" else "Paused",
                                        fontSize = 9.sp,
                                        color = ChakhLeSlate400
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = kitchen.isOpen,
                                    onCheckedChange = { viewModel.kitchenToggleStoreStatus(kitchen.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF16A34A)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4 Lifecycle Stages Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedStage.ordinal,
            containerColor = ChakhLeSlate800,
            contentColor = Color.White,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedStage.ordinal]),
                    color = when (selectedStage) {
                        KitchenStage.RECEIVED -> Color(0xFFEF4444)
                        KitchenStage.ACCEPTED -> Color(0xFFF59E0B)
                        KitchenStage.COOKING -> Color(0xFFEA580C)
                        KitchenStage.DELIVERED -> Color(0xFF10B981)
                        KitchenStage.MENU -> Color(0xFF3B82F6)
                    },
                    height = 3.dp
                )
            }
        ) {
            KitchenStage.values().forEach { stage ->
                val isSelected = selectedStage == stage
                val count = when (stage) {
                    KitchenStage.RECEIVED -> receivedOrders.size
                    KitchenStage.ACCEPTED -> acceptedOrders.size
                    KitchenStage.COOKING -> cookingOrders.size
                    KitchenStage.DELIVERED -> deliveredOrders.size
                    KitchenStage.MENU -> kitchenDishes.size
                }

                Tab(
                    selected = isSelected,
                    onClick = { selectedStage = stage },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = stage.icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isSelected) Color.White else ChakhLeSlate400)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stage.label} ($count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else ChakhLeSlate400
                            )
                        }
                    }
                )
            }
        }

        // Live Order Count & Kitchen Revenue Summary Strip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ChakhLeSurface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Orders in Line: ${kitchenOrders.count { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextPrimary
                    )
                    Text(
                        text = "Revenue: ₹${todayEarnings.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeSuccess
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { showAddDishDialog = true },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Add Dish", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Tab Screen Content
        when (selectedStage) {
            KitchenStage.RECEIVED -> {
                KitchenStageOrdersList(
                    orders = receivedOrders,
                    emptyTitle = "No New Incoming Orders",
                    emptySubtitle = "New customer orders will ring here with immediate alert.",
                    primaryButtonText = "Accept Order ✅",
                    primaryButtonColor = Color(0xFF16A34A),
                    onPrimaryAction = { order ->
                        viewModel.updateOrderStatusDirectly(order.orderId, OrderStatus.ACCEPTED)
                    },
                    secondaryButtonText = "Decline ❌",
                    onSecondaryAction = { order ->
                        viewModel.updateOrderStatusDirectly(order.orderId, OrderStatus.CANCELLED)
                    },
                    onTrackOrder = onTrackOrder
                )
            }
            KitchenStage.ACCEPTED -> {
                KitchenStageOrdersList(
                    orders = acceptedOrders,
                    emptyTitle = "No Orders in Accepted Queue",
                    emptySubtitle = "Orders you accept from 'Received' will wait here before cooking.",
                    primaryButtonText = "Send to Chef / Start Cooking 👨‍🍳",
                    primaryButtonColor = Color(0xFFEA580C),
                    onPrimaryAction = { order ->
                        viewModel.updateOrderStatusDirectly(order.orderId, OrderStatus.PREPARING)
                    },
                    secondaryButtonText = null,
                    onSecondaryAction = null,
                    onTrackOrder = onTrackOrder
                )
            }
            KitchenStage.COOKING -> {
                KitchenStageOrdersList(
                    orders = cookingOrders,
                    emptyTitle = "Chef Station is Clear",
                    emptySubtitle = "No dishes currently on stoves or in the tandoor.",
                    primaryButtonText = "Dish Prepared ➔ Hand to Rider 🛵",
                    primaryButtonColor = Color(0xFF2563EB),
                    onPrimaryAction = { order ->
                        viewModel.updateOrderStatusDirectly(order.orderId, OrderStatus.OUT_FOR_DELIVERY)
                    },
                    secondaryButtonText = null,
                    onSecondaryAction = null,
                    onTrackOrder = onTrackOrder
                )
            }
            KitchenStage.DELIVERED -> {
                KitchenDeliveredOrdersList(
                    orders = deliveredOrders,
                    onTrackOrder = onTrackOrder,
                    onMarkDelivered = { orderId ->
                        viewModel.updateOrderStatusDirectly(orderId, OrderStatus.DELIVERED)
                    }
                )
            }
            KitchenStage.MENU -> {
                KitchenMenuManagerTab(
                    dishes = kitchenDishes,
                    kitchenName = currentKitchen?.name ?: "Kitchen",
                    onAddDish = { showAddDishDialog = true },
                    onDeleteDish = { viewModel.adminDeleteDish(it) }
                )
            }
        }
    }

    // Add Dish Modal
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

/**
 * Renders orders in a specific stage with custom primary and secondary action triggers
 */
@Composable
private fun KitchenStageOrdersList(
    orders: List<OrderEntity>,
    emptyTitle: String,
    emptySubtitle: String,
    primaryButtonText: String,
    primaryButtonColor: Color,
    onPrimaryAction: (OrderEntity) -> Unit,
    secondaryButtonText: String?,
    onSecondaryAction: ((OrderEntity) -> Unit)?,
    onTrackOrder: (String) -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ChakhLeSlate100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OutdoorGrill,
                        contentDescription = null,
                        tint = ChakhLeTextMuted,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = emptyTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = emptySubtitle,
                    fontSize = 12.sp,
                    color = ChakhLeTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(orders, key = { it.orderId }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Order Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TICKET #${order.orderId}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = ChakhLeTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = ChakhLeSlate100,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(order.timestamp)),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ChakhLeTextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Customer: ${order.customerName} • ${order.deliveryAddress}",
                                    fontSize = 11.sp,
                                    color = ChakhLeTextSecondary
                                )
                            }

                            Text(
                                text = "₹${order.totalAmount.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = ChakhLeRedPrimary
                            )
                        }

                        HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 10.dp))

                        // Cooking Items Checklist
                        Text(
                            text = "ORDERED DISHES (PREP LIST):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChakhLeTextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = ChakhLeSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = order.itemsSummary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ChakhLeTextPrimary
                                )
                                if (order.deliveryNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Chef Note: \"${order.deliveryNotes}\"",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (secondaryButtonText != null && onSecondaryAction != null) {
                                OutlinedButton(
                                    onClick = { onSecondaryAction(order) },
                                    modifier = Modifier.weight(0.4f).height(42.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeRedPrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ChakhLeRedPrimary)
                                ) {
                                    Text(secondaryButtonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { onPrimaryAction(order) },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryButtonColor)
                            ) {
                                Text(primaryButtonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders stage 4 (Dispatched and Delivered)
 */
@Composable
private fun KitchenDeliveredOrdersList(
    orders: List<OrderEntity>,
    onTrackOrder: (String) -> Unit,
    onMarkDelivered: (String) -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = ChakhLeTextMuted, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Dispatched or Completed Orders", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Handover cooked orders to see active delivery status here.", fontSize = 12.sp, color = ChakhLeTextSecondary)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(orders, key = { it.orderId }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ORDER #${order.orderId}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Delivered to: ${order.customerName}",
                                    fontSize = 11.sp,
                                    color = ChakhLeTextSecondary
                                )
                            }

                            Surface(
                                color = if (order.status == OrderStatus.DELIVERED) Color(0xFFE8F5E9) else Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = order.status.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.status == OrderStatus.DELIVERED) ChakhLeSuccess else Color(0xFF2563EB),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = order.itemsSummary, fontSize = 12.sp, color = ChakhLeTextPrimary)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChakhLeSlate100)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsBike, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rider: ${order.riderName} (${order.riderPhone})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                                Button(
                                    onClick = { onMarkDelivered(order.orderId) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeSuccess),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Mark Delivered", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kitchen Menu & Dishes Manager Tab
 */
@Composable
private fun KitchenMenuManagerTab(
    dishes: List<Dish>,
    kitchenName: String,
    onAddDish: () -> Unit,
    onDeleteDish: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$kitchenName Menu (${dishes.size} Items)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextPrimary
                    )
                    Text(
                        text = "Dishes currently visible to customers on ChakhLe",
                        fontSize = 11.sp,
                        color = ChakhLeTextSecondary
                    )
                }

                Button(
                    onClick = onAddDish,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Dish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (dishes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No dishes added yet for this kitchen. Tap '+ Add Dish' to add items!", color = ChakhLeTextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(dishes, key = { it.id }) { dish ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .border(1.dp, if (dish.isVeg) ChakhLeVegGreen else ChakhLeRedPrimary, RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (dish.isVeg) ChakhLeVegGreen else ChakhLeRedPrimary)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = dish.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${dish.category.displayName} • ₹${dish.price.toInt()}", fontSize = 11.sp, color = ChakhLeTextSecondary)
                            }
                        }

                        IconButton(
                            onClick = { onDeleteDish(dish.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = ChakhLeTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Add Kitchen Dialog (Full Registration Form - Admin Exclusive)
 */
@Composable
fun AddKitchenDialog(
    onDismiss: () -> Unit,
    onRegister: (
        name: String,
        cuisine: String,
        deliveryTime: String,
        isVeg: Boolean,
        address: String,
        priceForTwo: Int,
        ownerName: String,
        ownerPhone: String,
        userId: String,
        password: String,
        signatureDishName: String,
        dishPrice: Double,
        dishCategory: FoodCategoryType,
        dishDescription: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("North Indian & Biryani") }
    var deliveryTime by remember { mutableStateOf("25") }
    var isPureVeg by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("Koramangala, Bengaluru") }
    var priceForTwo by remember { mutableStateOf("450") }

    // Hotel Owner & Credentials fields
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var signatureDishName by remember { mutableStateOf("") }
    var dishPrice by remember { mutableStateOf("199") }
    var dishCategory by remember { mutableStateOf(FoodCategoryType.BIRYANI) }
    var dishDescription by remember { mutableStateOf("") }

    // Auto-suggest userId & password when name changes if empty
    androidx.compose.runtime.LaunchedEffect(name) {
        if (name.isNotBlank()) {
            val base = name.lowercase().replace("[^a-zA-Z0-9]".toRegex(), "").take(8)
            if (userId.isBlank() || userId.startsWith("kitchen_")) {
                userId = "kitchen_$base"
            }
            if (password.isBlank()) {
                password = "CK@${(1000..9999).random()}"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, tint = ChakhLeRedPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Cloud Kitchen Partner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    text = "KITCHEN RESTAURANT DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeTextMuted
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kitchen Name *") },
                    placeholder = { Text("e.g. Behrouz Handi Kitchen") },
                    modifier = Modifier.fillMaxWidth().testTag("kitchen_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cuisine,
                    onValueChange = { cuisine = it },
                    label = { Text("Cuisine Specialties") },
                    placeholder = { Text("e.g. Awadhi Biryani, Kebabs, Rolls") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryTime,
                        onValueChange = { deliveryTime = it },
                        label = { Text("Prep Time (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceForTwo,
                        onValueChange = { priceForTwo = it },
                        label = { Text("Price for 2 (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Kitchen Address / Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
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

                // HOTEL OWNER & LOGIN CREDENTIALS
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🔑 HOTEL OWNER & LOGIN CREDENTIALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Only Admin creates kitchens. After creation, you can share these credentials with the hotel owner.",
                            fontSize = 10.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Hotel Owner / Partner Name *") },
                    placeholder = { Text("e.g. Ramesh Chandra") },
                    modifier = Modifier.fillMaxWidth().testTag("kitchen_owner_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ownerPhone,
                    onValueChange = { ownerPhone = it },
                    label = { Text("Owner Mobile Number *") },
                    placeholder = { Text("+91 98765 43210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("kitchen_owner_phone_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it.trim().lowercase() },
                    label = { Text("Staff Login User ID *") },
                    placeholder = { Text("e.g. kitchen_behrouz") },
                    modifier = Modifier.fillMaxWidth().testTag("kitchen_user_id_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.trim() },
                    label = { Text("Staff Password *") },
                    placeholder = { Text("e.g. CK@9842") },
                    modifier = Modifier.fillMaxWidth().testTag("kitchen_password_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "SIGNATURE DISH (ADDED TO LIVE MENU)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeTextMuted
                )

                OutlinedTextField(
                    value = signatureDishName,
                    onValueChange = { signatureDishName = it },
                    label = { Text("Dish Name *") },
                    placeholder = { Text("e.g. Royal Shahi Dum Biryani") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dishPrice,
                    onValueChange = { dishPrice = it },
                    label = { Text("Dish Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dishDescription,
                    onValueChange = { dishDescription = it },
                    label = { Text("Description") },
                    placeholder = { Text("Slow-cooked dum biryani prepared with authentic spices") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val parsedPriceForTwo = priceForTwo.toIntOrNull() ?: 400
                        val parsedDishPrice = dishPrice.toDoubleOrNull() ?: 199.0
                        val finalOwnerName = ownerName.ifBlank { "Hotel Partner" }
                        val finalOwnerPhone = ownerPhone.ifBlank { "+91 98765 00000" }
                        val finalUserId = userId.ifBlank { "kitchen_${name.take(6).lowercase()}" }
                        val finalPassword = password.ifBlank { "CK@${(1000..9999).random()}" }
                        onRegister(
                            name,
                            cuisine,
                            deliveryTime,
                            isPureVeg,
                            address,
                            parsedPriceForTwo,
                            finalOwnerName,
                            finalOwnerPhone,
                            finalUserId,
                            finalPassword,
                            signatureDishName,
                            parsedDishPrice,
                            dishCategory,
                            dishDescription
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Register Kitchen & Generate Login", fontWeight = FontWeight.Bold)
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

/**
 * Add Dish Dialog
 */
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
    var selectedRestIndex by remember { mutableStateOf(0) }
    val currentRestaurant = restaurants.getOrNull(selectedRestIndex) ?: restaurants.firstOrNull()

    var dishName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("199") }
    var selectedCategory by remember { mutableStateOf(FoodCategoryType.BIRYANI) }
    var isVeg by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = ChakhLeRedPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Dish to Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (restaurants.isNotEmpty()) {
                    Text("Select Target Kitchen:", fontSize = 11.sp, color = ChakhLeTextMuted, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(restaurants.indices.toList()) { index ->
                            val rest = restaurants[index]
                            val isSelected = index == selectedRestIndex
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedRestIndex = index },
                                color = if (isSelected) ChakhLeRedPrimary else ChakhLeSurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = rest.name,
                                    color = if (isSelected) Color.White else ChakhLeTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = dishName,
                    onValueChange = { dishName = it },
                    label = { Text("Dish Name *") },
                    placeholder = { Text("e.g. Hyderabadi Dum Biryani") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
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
