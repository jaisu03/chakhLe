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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.PromoCoupon
import com.example.data.model.Restaurant
import com.example.data.model.UserRole
import com.example.ui.theme.CategoryBiryaniBg
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
import com.example.ui.theme.ChakhLeSlate600
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

enum class AdminTab(val title: String) {
    OVERVIEW("Overview"),
    ORDERS("All Orders"),
    KITCHENS("Kitchens"),
    MENU("Dishes"),
    COUPONS("Coupons")
}

@Composable
fun AdminDashboardScreen(
    viewModel: ChakhLeViewModel,
    onNavigateToCustomer: () -> Unit,
    onNavigateToKitchen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    val allDishes by viewModel.allDishes.collectAsState()
    val coupons by viewModel.promoCouponsFlow.collectAsState()

    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    var orderSearchQuery by remember { mutableStateOf("") }
    var orderStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    // Dialog States
    var showAddKitchenDialog by remember { mutableStateOf(false) }
    var createdKitchenForSharing by remember { mutableStateOf<Restaurant?>(null) }
    var showAddDishDialog by remember { mutableStateOf(false) }
    var showAddCouponDialog by remember { mutableStateOf(false) }
    var editingOrderForRider by remember { mutableStateOf<OrderEntity?>(null) }
    var editingOrderForStatus by remember { mutableStateOf<OrderEntity?>(null) }

    // Aggregate Platform Stats
    val totalRevenue = remember(allOrders) { allOrders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount } }
    val totalOrdersCount = allOrders.size
    val activeDeliveriesCount = remember(allOrders) {
        allOrders.count { it.status == OrderStatus.OUT_FOR_DELIVERY || it.status == OrderStatus.PREPARING }
    }
    val activeKitchensCount = restaurants.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChakhLeBackground)
    ) {
        // Admin Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ChakhLeSlate900,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChakhLeRedPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Khaibu Admin",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF15803D),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "SUPER ADMIN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Platform Master Control Console",
                                fontSize = 11.sp,
                                color = ChakhLeSlate400
                            )
                        }
                    }

                    // Role Switcher Actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.setUserRole(UserRole.KITCHEN)
                                onNavigateToKitchen(restaurants.firstOrNull()?.id ?: "rest_1")
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeAmberLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kitchen View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.setUserRole(UserRole.CUSTOMER)
                                onNavigateToCustomer()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Foodie App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Admin Horizontal Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = ChakhLeSlate800,
            contentColor = Color.White,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = ChakhLeRedPrimary,
                    height = 3.dp
                )
            }
        ) {
            AdminTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else ChakhLeSlate400
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            AdminTab.OVERVIEW -> {
                AdminOverviewTab(
                    totalRevenue = totalRevenue,
                    totalOrders = totalOrdersCount,
                    activeDeliveries = activeDeliveriesCount,
                    activeKitchens = activeKitchensCount,
                    recentOrders = allOrders.take(5),
                    onViewAllOrders = { selectedTab = AdminTab.ORDERS },
                    onAddKitchen = { showAddKitchenDialog = true },
                    onAddDish = { showAddDishDialog = true },
                    onAddCoupon = { showAddCouponDialog = true },
                    onAdvanceOrder = { orderId, status -> viewModel.advanceOrderStatus(orderId, status) }
                )
            }
            AdminTab.ORDERS -> {
                AdminOrdersTab(
                    orders = allOrders,
                    searchQuery = orderSearchQuery,
                    onSearchQueryChange = { orderSearchQuery = it },
                    statusFilter = orderStatusFilter,
                    onStatusFilterChange = { orderStatusFilter = it },
                    onChangeStatus = { editingOrderForStatus = it },
                    onAssignRider = { editingOrderForRider = it },
                    onCancelOrder = { viewModel.adminCancelOrder(it.orderId) },
                    onDeleteOrder = { viewModel.adminDeleteOrder(it.orderId) }
                )
            }
            AdminTab.KITCHENS -> {
                AdminKitchensTab(
                    restaurants = restaurants,
                    onToggleStoreStatus = { viewModel.kitchenToggleStoreStatus(it) },
                    onDeleteKitchen = { viewModel.adminDeleteRestaurant(it) },
                    onAddKitchen = { showAddKitchenDialog = true },
                    onShareCredentials = { createdKitchenForSharing = it },
                    onSelectKitchen = { restId ->
                        viewModel.setUserRole(UserRole.KITCHEN, restId)
                        onNavigateToKitchen(restId)
                    }
                )
            }
            AdminTab.MENU -> {
                AdminMenuTab(
                    dishes = allDishes,
                    restaurants = restaurants,
                    onDeleteDish = { viewModel.adminDeleteDish(it) },
                    onAddDish = { showAddDishDialog = true }
                )
            }
            AdminTab.COUPONS -> {
                AdminCouponsTab(
                    coupons = coupons,
                    onAddCoupon = { showAddCouponDialog = true },
                    onDeleteCoupon = { viewModel.adminDeleteCoupon(it) }
                )
            }
        }
    }

    // Dialogs
    if (showAddKitchenDialog) {
        AddKitchenDialog(
            onDismiss = { showAddKitchenDialog = false },
            onRegister = { name, cuisine, time, isVeg, address, price, ownerName, ownerPhone, userId, password, dishName, dishPrice, category, desc ->
                val parsedTime = time.filter { it.isDigit() }.toIntOrNull() ?: 25
                val newKitchen = viewModel.adminRegisterKitchen(
                    name = name,
                    cuisine = cuisine,
                    deliveryTime = parsedTime,
                    isPureVeg = isVeg,
                    address = address,
                    priceForTwo = price,
                    ownerName = ownerName,
                    ownerPhone = ownerPhone,
                    userId = userId,
                    password = password,
                    signatureDishName = dishName,
                    dishPrice = dishPrice,
                    dishCategory = category,
                    dishDescription = desc
                )
                showAddKitchenDialog = false
                createdKitchenForSharing = newKitchen
            }
        )
    }

    createdKitchenForSharing?.let { kitchen ->
        ShareKitchenCredentialsDialog(
            kitchen = kitchen,
            onDismiss = { createdKitchenForSharing = null }
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

    if (showAddCouponDialog) {
        AdminAddCouponDialog(
            onDismiss = { showAddCouponDialog = false },
            onAdd = { code, discount, maxDisc, minOrd, title, desc, maxUses, validityHours ->
                viewModel.adminAddCoupon(code, discount, maxDisc, minOrd, title, desc, maxUses, validityHours)
                showAddCouponDialog = false
            }
        )
    }

    editingOrderForRider?.let { order ->
        AdminAssignRiderDialog(
            order = order,
            onDismiss = { editingOrderForRider = null },
            onAssign = { name, phone ->
                viewModel.adminAssignRider(order.orderId, name, phone)
                editingOrderForRider = null
            }
        )
    }

    editingOrderForStatus?.let { order ->
        AdminChangeStatusDialog(
            order = order,
            onDismiss = { editingOrderForStatus = null },
            onSelectStatus = { newStatus ->
                viewModel.updateOrderStatusDirectly(order.orderId, newStatus)
                editingOrderForStatus = null
            }
        )
    }
}

// -------------------------------------------------------------
// 1. Admin Overview Tab
// -------------------------------------------------------------
@Composable
private fun AdminOverviewTab(
    totalRevenue: Double,
    totalOrders: Int,
    activeDeliveries: Int,
    activeKitchens: Int,
    recentOrders: List<OrderEntity>,
    onViewAllOrders: () -> Unit,
    onAddKitchen: () -> Unit,
    onAddDish: () -> Unit,
    onAddCoupon: () -> Unit,
    onAdvanceOrder: (String, OrderStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddKitchen,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Kitchen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAddDish,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Dish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAddCoupon,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.Discount, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Coupon", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Metrics Grid
        item {
            Text(
                text = "PLATFORM PERFORMANCE METRICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ChakhLeTextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "Platform Gross Sales",
                        value = "₹${totalRevenue.toInt()}",
                        subtitle = "Net completed & active",
                        icon = Icons.Default.LocalAtm,
                        accentColor = ChakhLeSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Total Orders",
                        value = totalOrders.toString(),
                        subtitle = "All-time placed",
                        icon = Icons.Default.ReceiptLong,
                        accentColor = ChakhLeRedPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "Active Kitchens",
                        value = activeKitchens.toString(),
                        subtitle = "Live Cloud Kitchens",
                        icon = Icons.Default.Store,
                        accentColor = Color(0xFFEA580C),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Fleet in Transit",
                        value = activeDeliveries.toString(),
                        subtitle = "Live on road",
                        icon = Icons.Default.DirectionsBike,
                        accentColor = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Orders Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ORDERS MONITOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeTextMuted,
                    letterSpacing = 1.sp
                )
                TextButton(onClick = onViewAllOrders) {
                    Text("View All (${totalOrders})", fontSize = 12.sp, color = ChakhLeRedPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (recentOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No orders placed yet. Place an order from Customer App!", color = ChakhLeTextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(recentOrders, key = { it.orderId }) { order ->
                AdminCompactOrderRow(
                    order = order,
                    onAdvance = { onAdvanceOrder(order.orderId, order.status) }
                )
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = ChakhLeTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = ChakhLeTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = ChakhLeTextMuted
            )
        }
    }
}

@Composable
fun AdminCompactOrderRow(
    order: OrderEntity,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ChakhLeTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${order.totalAmount.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ChakhLeRedPrimary
                    )
                }
                Text(
                    text = "${order.customerName} • ${order.restaurantName}",
                    fontSize = 11.sp,
                    color = ChakhLeTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = order.itemsSummary,
                    fontSize = 10.sp,
                    color = ChakhLeTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = when (order.status) {
                        OrderStatus.PLACED, OrderStatus.ACCEPTED -> ChakhLeAmberLight
                        OrderStatus.PREPARING -> Color(0xFFFFF7ED)
                        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFEFF6FF)
                        OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                        OrderStatus.CANCELLED -> ChakhLeRedContainer
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = order.status.displayName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            OrderStatus.PLACED, OrderStatus.ACCEPTED -> ChakhLeAmberDark
                            OrderStatus.PREPARING -> Color(0xFFEA580C)
                            OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF2563EB)
                            OrderStatus.DELIVERED -> ChakhLeSuccess
                            OrderStatus.CANCELLED -> ChakhLeRedPrimary
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onAdvance,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Advance ➔", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Admin All Orders Tab
// -------------------------------------------------------------
@Composable
private fun AdminOrdersTab(
    orders: List<OrderEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: OrderStatus?,
    onStatusFilterChange: (OrderStatus?) -> Unit,
    onChangeStatus: (OrderEntity) -> Unit,
    onAssignRider: (OrderEntity) -> Unit,
    onCancelOrder: (OrderEntity) -> Unit,
    onDeleteOrder: (OrderEntity) -> Unit
) {
    val filteredOrders = remember(orders, searchQuery, statusFilter) {
        orders.filter { order ->
            val matchesQuery = searchQuery.isBlank() ||
                order.orderId.contains(searchQuery, ignoreCase = true) ||
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                order.restaurantName.contains(searchQuery, ignoreCase = true) ||
                order.itemsSummary.contains(searchQuery, ignoreCase = true)
            val matchesStatus = (statusFilter == null) || (order.status == statusFilter)
            matchesQuery && matchesStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by Order ID, Customer, or Restaurant", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ChakhLeTextMuted) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = ChakhLeRedPrimary,
                unfocusedBorderColor = ChakhLeBorder
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onStatusFilterChange(null) },
                    color = if (statusFilter == null) ChakhLeRedPrimary else ChakhLeSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (statusFilter == null) ChakhLeRedPrimary else ChakhLeBorder)
                ) {
                    Text(
                        text = "All (${orders.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (statusFilter == null) Color.White else ChakhLeTextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            items(OrderStatus.values().filter { it != OrderStatus.CANCELLED }) { status ->
                val isSelected = statusFilter == status
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onStatusFilterChange(if (isSelected) null else status) },
                    color = if (isSelected) ChakhLeRedPrimary else ChakhLeSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ChakhLeRedPrimary else ChakhLeBorder)
                ) {
                    Text(
                        text = status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else ChakhLeTextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No orders match your filter criteria.", color = ChakhLeTextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.orderId }) { order ->
                    AdminDetailedOrderCard(
                        order = order,
                        onChangeStatus = { onChangeStatus(order) },
                        onAssignRider = { onAssignRider(order) },
                        onCancel = { onCancelOrder(order) },
                        onDelete = { onDeleteOrder(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDetailedOrderCard(
    order: OrderEntity,
    onChangeStatus: () -> Unit,
    onAssignRider: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = remember(order.timestamp) {
        SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()).format(Date(order.timestamp))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            fontSize = 14.sp,
                            color = ChakhLeTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = ChakhLeTextMuted
                        )
                    }
                    Text(
                        text = "${order.restaurantName} ➔ ${order.customerName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChakhLeRedPrimary
                    )
                }

                Surface(
                    color = when (order.status) {
                        OrderStatus.PLACED, OrderStatus.ACCEPTED -> ChakhLeAmberLight
                        OrderStatus.PREPARING -> Color(0xFFFFF7ED)
                        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFEFF6FF)
                        OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                        OrderStatus.CANCELLED -> ChakhLeRedContainer
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = order.status.displayName,
                        color = when (order.status) {
                            OrderStatus.PLACED, OrderStatus.ACCEPTED -> ChakhLeAmberDark
                            OrderStatus.PREPARING -> Color(0xFFEA580C)
                            OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF2563EB)
                            OrderStatus.DELIVERED -> ChakhLeSuccess
                            OrderStatus.CANCELLED -> ChakhLeRedPrimary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Items: ${order.itemsSummary}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ChakhLeTextPrimary
            )

            Text(
                text = "Deliver to: ${order.deliveryAddress} • Phone: ${order.customerPhone}",
                fontSize = 11.sp,
                color = ChakhLeTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Rider Info
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
                    Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "Rider: ${order.riderName} (${order.riderPhone})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Vehicle: ${order.riderVehicle}", fontSize = 10.sp, color = ChakhLeTextMuted)
                    }
                }

                OutlinedButton(
                    onClick = onAssignRider,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Change Rider", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ₹${order.totalAmount.toInt()} (${order.paymentMethod})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ChakhLeTextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onChangeStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeSlate800),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Force Status", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    if (order.status != OrderStatus.CANCELLED) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeRedPrimary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Cancel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ChakhLeTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Admin Kitchens Management Tab
// -------------------------------------------------------------
@Composable
private fun AdminKitchensTab(
    restaurants: List<Restaurant>,
    onToggleStoreStatus: (String) -> Unit,
    onDeleteKitchen: (String) -> Unit,
    onAddKitchen: () -> Unit,
    onShareCredentials: (Restaurant) -> Unit,
    onSelectKitchen: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REGISTERED CLOUD KITCHENS (${restaurants.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Manage kitchen active status, branches, & menus",
                        fontSize = 11.sp,
                        color = ChakhLeTextSecondary
                    )
                }

                Button(
                    onClick = onAddKitchen,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Kitchen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(restaurants, key = { it.id }) { restaurant ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = restaurant.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChakhLeTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (restaurant.isPureVeg) {
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "PURE VEG",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ChakhLeVegGreen,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = restaurant.cuisine,
                                fontSize = 11.sp,
                                color = ChakhLeTextSecondary
                            )
                            Text(
                                text = "📍 ${restaurant.address} • Delivery: ~${restaurant.deliveryTimeMinutes} mins",
                                fontSize = 10.sp,
                                color = ChakhLeTextMuted
                            )
                        }

                        // Store Online / Offline Toggle
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (restaurant.isOpen) "ONLINE" else "BUSY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (restaurant.isOpen) ChakhLeSuccess else ChakhLeAmberDark
                            )
                            Switch(
                                checked = restaurant.isOpen,
                                onCheckedChange = { onToggleStoreStatus(restaurant.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ChakhLeSuccess
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 10.dp))

                    // Hotel Owner & Credentials Banner
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "HOTEL OWNER & LOGIN CREDENTIALS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Owner: ${restaurant.ownerName} (${restaurant.ownerPhone})",
                                    fontSize = 11.sp,
                                    color = ChakhLeTextSecondary
                                )
                                Text(
                                    text = "User ID: ${restaurant.userId.ifBlank { "kitchen_${restaurant.name.take(6).lowercase()}" }} • Pass: ${restaurant.password.ifBlank { "CK@1234" }}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Button(
                                onClick = { onShareCredentials(restaurant) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⭐ ${restaurant.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ChakhLeAmberDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "₹${restaurant.priceForTwo} for two", fontSize = 11.sp, color = ChakhLeTextMuted)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onSelectKitchen(restaurant.id) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Open as Staff", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { onDeleteKitchen(restaurant.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ChakhLeRedPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Admin Menu & Dishes Tab
// -------------------------------------------------------------
@Composable
private fun AdminMenuTab(
    dishes: List<Dish>,
    restaurants: List<Restaurant>,
    onDeleteDish: (String) -> Unit,
    onAddDish: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                        text = "PLATFORM DISHES CATALOG (${dishes.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Global food items across all Cloud Kitchens",
                        fontSize = 11.sp,
                        color = ChakhLeTextSecondary
                    )
                }

                Button(
                    onClick = onAddDish,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Dish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Veg / NonVeg dot
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
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dish.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ChakhLeTextPrimary
                            )
                        }
                        Text(
                            text = "Restaurant: ${dish.restaurantName}",
                            fontSize = 11.sp,
                            color = ChakhLeRedPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Category: ${dish.category.displayName} • ₹${dish.price.toInt()}",
                            fontSize = 11.sp,
                            color = ChakhLeTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { onDeleteDish(dish.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete Dish", tint = ChakhLeTextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. Admin Coupons Tab
// -------------------------------------------------------------
@Composable
private fun AdminCouponsTab(
    coupons: List<PromoCoupon>,
    onAddCoupon: () -> Unit,
    onDeleteCoupon: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVE DISCOUNT COUPONS (${coupons.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Time-limited & user-capped promotion vouchers",
                        fontSize = 11.sp,
                        color = ChakhLeTextSecondary
                    )
                }

                Button(
                    onClick = onAddCoupon,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Offer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(coupons, key = { it.code }) { coupon ->
            val isExpired = coupon.isExpired

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpired) Color(0xFFF9FAFB) else ChakhLeSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isExpired) Color(0xFFE5E7EB) else ChakhLeBorder
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isExpired) Color(0xFFE5E7EB) else ChakhLeAmberLight,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isExpired) Color.Gray else ChakhLeAmberDark
                                )
                            ) {
                                Text(
                                    text = coupon.code,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (isExpired) Color.Gray else ChakhLeAmberDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${coupon.discountPercent}% OFF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isExpired) Color.Gray else ChakhLeSuccess
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isExpired) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = if (isExpired) "AUTO-EXPIRED" else "ACTIVE",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isExpired) Color(0xFFC62828) else Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onDeleteCoupon(coupon.code) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete coupon",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = coupon.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExpired) Color.Gray else ChakhLeTextPrimary
                    )

                    Text(
                        text = "${coupon.description} • Min Order: ₹${coupon.minOrder.toInt()} • Max Disc: ₹${coupon.maxDiscount.toInt()}",
                        fontSize = 11.sp,
                        color = ChakhLeTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quota and Validity Badges Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // 1. Quota Progress
                            if (coupon.maxUses != null) {
                                val progress = (coupon.usedCount.toFloat() / coupon.maxUses.toFloat()).coerceIn(0f, 1f)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔥 First ${coupon.maxUses} Users Quota",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (coupon.isQuotaExhausted) Color(0xFFC62828) else ChakhLeSlate800
                                    )
                                    Text(
                                        text = "${coupon.usedCount} / ${coupon.maxUses} Claimed (${coupon.remainingUses} left)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (coupon.isQuotaExhausted) Color(0xFFC62828) else ChakhLeTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (coupon.isQuotaExhausted) Color(0xFFC62828) else ChakhLeRedPrimary,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            } else {
                                Text(
                                    text = "👥 Unlimited Redemptions (${coupon.usedCount} used)",
                                    fontSize = 11.sp,
                                    color = ChakhLeTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // 2. Validity Time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (coupon.isTimeExpired) Color(0xFFC62828) else ChakhLeSlate600,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val validityText = when {
                                    coupon.isTimeExpired -> "Validity Expired (Auto-expired)"
                                    coupon.expiryTimestamp != null -> "Time Left: ${coupon.getTimeRemainingString()} (Auto-expires)"
                                    else -> "Validity: No expiration date"
                                }
                                Text(
                                    text = validityText,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (coupon.expiryTimestamp != null) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (coupon.isTimeExpired) Color(0xFFC62828) else ChakhLeSlate700
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Admin Dialogs
// -------------------------------------------------------------
@Composable
fun AdminAddCouponDialog(
    onDismiss: () -> Unit,
    onAdd: (
        code: String,
        discountPercent: Int,
        maxDiscount: Double,
        minOrder: Double,
        title: String,
        description: String,
        maxUses: Int?,
        validityHours: Int?
    ) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("40") }
    var maxDiscount by remember { mutableStateOf("120") }
    var minOrder by remember { mutableStateOf("199") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Quota limits: 20 is default for "First 20 Users"
    var selectedQuota by remember { mutableStateOf<Int?>(20) }

    // Validity duration: 24h by default
    var selectedValidityHours by remember { mutableStateOf<Int?>(24) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Promo / Flash Offer", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase().trim() },
                    label = { Text("Coupon Code (e.g. FIRST20, FLASH60)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountPercent,
                        onValueChange = { discountPercent = it },
                        label = { Text("Discount %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = { maxDiscount = it },
                        label = { Text("Max Cap (₹)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = minOrder,
                    onValueChange = { minOrder = it },
                    label = { Text("Min Order Value (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Offer Title") },
                    placeholder = { Text("First 20 Users: 40% OFF") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Offer Description") },
                    placeholder = { Text("Limited to the first 20 foodies only!") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Quota Selection Section
                Text(
                    text = "CLAIM LIMIT (AUTO-EXPIRES WHEN CLAIMED):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeRedPrimary,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quotaOptions = listOf(20 to "First 20", 50 to "First 50", 100 to "First 100", null to "Unlimited")
                    quotaOptions.forEach { (quota, label) ->
                        val isSelected = selectedQuota == quota
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) ChakhLeRedPrimary else Color(0xFFCBD5E1),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedQuota = quota },
                            color = if (isSelected) ChakhLeRedPrimary.copy(alpha = 0.12f) else Color.White
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ChakhLeRedPrimary else ChakhLeTextPrimary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Validity Period Section
                Text(
                    text = "VALIDITY DURATION (AUTO-EXPIRES AFTER TIME):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeRedPrimary,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val validityOptions = listOf(
                        2 to "2h Flash",
                        6 to "6 Hours",
                        24 to "24 Hours",
                        null to "No Expiry"
                    )
                    validityOptions.forEach { (hours, label) ->
                        val isSelected = selectedValidityHours == hours
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) ChakhLeAmberDark else Color(0xFFCBD5E1),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedValidityHours = hours },
                            color = if (isSelected) ChakhLeAmberLight else Color.White
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ChakhLeAmberDark else ChakhLeTextPrimary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = "ℹ️ Viewers will see live countdown & claim badges. The offer auto-expires as soon as the quota is filled or the timer runs out.",
                    fontSize = 10.5.sp,
                    color = ChakhLeTextSecondary,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank()) {
                        val disc = discountPercent.toIntOrNull() ?: 20
                        val maxD = maxDiscount.toDoubleOrNull() ?: 100.0
                        val minO = minOrder.toDoubleOrNull() ?: 199.0
                        onAdd(
                            code,
                            disc,
                            maxD,
                            minO,
                            title.ifBlank { "$code Discount" },
                            description.ifBlank { "Limited Time Offer" },
                            selectedQuota,
                            selectedValidityHours
                        )
                    }
                },
                enabled = code.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary)
            ) {
                Text("Publish Offer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminAssignRiderDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onAssign: (riderName: String, riderPhone: String) -> Unit
) {
    var riderName by remember { mutableStateOf(order.riderName) }
    var riderPhone by remember { mutableStateOf(order.riderPhone) }

    val presetRiders = listOf(
        "Rajesh Kumar" to "+91 98765 43210",
        "Vikram Singh" to "+91 98123 45678",
        "Amit Verma" to "+91 97654 32109",
        "Deepak Patil" to "+91 96543 21098"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Delivery Partner", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select from Active Fleet or enter custom rider details for Order #${order.orderId}:", fontSize = 12.sp, color = ChakhLeTextSecondary)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetRiders.forEach { (name, phone) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    riderName = name
                                    riderPhone = phone
                                },
                            color = if (riderName == name) ChakhLeAmberLight else ChakhLeSlate100
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = phone, fontSize = 11.sp, color = ChakhLeTextMuted)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = riderName,
                    onValueChange = { riderName = it },
                    label = { Text("Rider Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = riderPhone,
                    onValueChange = { riderPhone = it },
                    label = { Text("Rider Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAssign(riderName, riderPhone) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Confirm Dispatch Rider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminChangeStatusDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onSelectStatus: (OrderStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Override Order Status", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Force update status for Order #${order.orderId}:", fontSize = 12.sp, color = ChakhLeTextSecondary)

                OrderStatus.values().forEach { status ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectStatus(status) },
                        color = if (order.status == status) ChakhLeRedPrimary else ChakhLeSlate100
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = status.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (order.status == status) Color.White else ChakhLeTextPrimary
                            )
                            if (order.status == status) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Share Kitchen Credentials Dialog with Hotel Owner
 */
@Composable
fun ShareKitchenCredentialsDialog(
    kitchen: Restaurant,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val effectiveUserId = kitchen.userId.ifBlank { "kitchen_${kitchen.name.take(6).lowercase()}" }
    val effectivePassword = kitchen.password.ifBlank { "CK@1234" }
    val shareText = remember(kitchen) {
        """
        *ChakhLe Cloud Kitchen Partner Credentials* 👨‍🍳
        
        Hello ${kitchen.ownerName}!
        Your kitchen *${kitchen.name}* is officially registered on ChakhLe.
        
        🔑 *Staff Login Credentials:*
        • User ID: $effectiveUserId
        • Password: $effectivePassword
        
        📲 *How to Login:*
        1. Open the ChakhLe app
        2. Tap "Staff & Partner Login"
        3. Enter your User ID and Password
        
        You will be identified as ${kitchen.name} to track live orders: Received ➔ Accepted ➔ Cooking ➔ Delivered.
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF16A34A))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Kitchen Credentials", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "✅ KITCHEN ACCOUNT READY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Only Admin creates kitchens. Share these credentials with the hotel owner so they can log into their kitchen portal.",
                            fontSize = 11.sp,
                            color = Color(0xFF14532D)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Kitchen: ${kitchen.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Hotel Owner: ${kitchen.ownerName}", fontSize = 12.sp, color = ChakhLeTextSecondary)
                        Text(text = "Owner Phone: ${kitchen.ownerPhone}", fontSize = 12.sp, color = ChakhLeTextSecondary)
                        HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "User ID:", fontSize = 12.sp, color = ChakhLeTextMuted)
                            Text(text = effectiveUserId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Password:", fontSize = 12.sp, color = ChakhLeTextMuted)
                            Text(text = effectivePassword, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Kitchen Credentials", shareText)
                            clipboard?.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Credentials copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📋 Copy", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareChooser = android.content.Intent.createChooser(sendIntent, "Share Credentials with Hotel Owner")
                            context.startActivity(shareChooser)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📲 Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeSlate900),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done")
            }
        }
    )
}
