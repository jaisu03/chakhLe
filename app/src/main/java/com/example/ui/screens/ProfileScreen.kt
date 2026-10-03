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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AddressEntity
import com.example.data.model.ChatMessage
import com.example.data.model.FaqItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.ui.components.AddressSelectionBottomSheet
import com.example.ui.theme.ChakhLeAmber
import com.example.ui.theme.ChakhLeAmberDark
import com.example.ui.theme.ChakhLeAmberLight
import com.example.ui.theme.ChakhLeBackground
import com.example.ui.theme.ChakhLeBorder
import com.example.ui.theme.ChakhLeGold
import com.example.ui.theme.ChakhLeRedContainer
import com.example.ui.theme.ChakhLeRedLight
import com.example.ui.theme.ChakhLeRedPrimary
import com.example.ui.theme.ChakhLeSuccess
import com.example.ui.theme.ChakhLeSurface
import com.example.ui.theme.ChakhLeSurfaceVariant
import com.example.ui.theme.ChakhLeTextMuted
import com.example.ui.theme.ChakhLeTextPrimary
import com.example.ui.theme.ChakhLeTextSecondary
import com.example.ui.viewmodel.ChakhLeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ChakhLeViewModel,
    onNavigateBack: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToKitchen: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val firebaseUid by viewModel.firebaseUid.collectAsState()
    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val faqs = viewModel.supportFaqs

    val userRole by viewModel.userRole.collectAsState()
    val currentKitchenId by viewModel.currentKitchenId.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    val currentKitchen = remember(restaurants, currentKitchenId) {
        restaurants.find { it.id == currentKitchenId } ?: restaurants.firstOrNull()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Order History", "Saved Addresses", "Support & FAQs")

    var showAddressSheet by remember { mutableStateOf(false) }
    var showStaffLoginDialog by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChakhLeBackground)
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ChakhLeSurface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ChakhLeTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "My Profile & Orders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ChakhLeTextPrimary
                )
            }
        }

        // Profile Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ChakhLeRedPrimary, ChakhLeAmber)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = userName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .ifBlank { "CL" }
                    Text(
                        text = initials,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName.ifBlank { "Valued Foodie" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = ChakhLeTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (firebaseUid != null) Color(0xFFFFF7ED) else ChakhLeAmberLight,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (firebaseUid != null) "🔥 Firebase User" else "VIP Foodie",
                                color = if (firebaseUid != null) Color(0xFFC2410C) else ChakhLeAmberDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    if (userEmail.isNotBlank()) {
                        Text(
                            text = userEmail,
                            fontSize = 12.sp,
                            color = ChakhLeTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Text(
                        text = phoneNumber.ifBlank { if (userEmail.isNotBlank()) "Email Verified Account" else "+91 98765 12345" },
                        fontSize = 13.sp,
                        color = ChakhLeTextSecondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${allOrders.size} Orders Placed • ${savedAddresses.size} Addresses",
                        fontSize = 11.sp,
                        color = ChakhLeTextMuted
                    )
                }
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = ChakhLeSurface,
            contentColor = ChakhLeRedPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ChakhLeRedPrimary
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Past Order History
                if (allOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = ChakhLeTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No orders yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ChakhLeTextPrimary
                            )
                            Text(
                                text = "Your past delicious orders will show up here.",
                                fontSize = 12.sp,
                                color = ChakhLeTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allOrders, key = { it.orderId }) { order ->
                            PastOrderCard(
                                order = order,
                                onTrackOrder = {
                                    viewModel.selectOrderForTracking(order.orderId)
                                    onTrackOrder(order.orderId)
                                },
                                onReorder = {
                                    viewModel.reorder(order) {
                                        onNavigateToCart()
                                    }
                                }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Saved Addresses Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddressSheet = true },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add New Delivery Address", fontWeight = FontWeight.Bold)
                        }
                    }

                    items(savedAddresses, key = { it.id }) { address ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectAddress(address) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                            border = if (address.isDefault) androidx.compose.foundation.BorderStroke(1.5.dp, ChakhLeRedPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (address.isDefault) ChakhLeRedContainer else ChakhLeSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (address.tag.uppercase()) {
                                            "WORK" -> Icons.Default.Work
                                            else -> Icons.Default.Home
                                        },
                                        contentDescription = null,
                                        tint = if (address.isDefault) ChakhLeRedPrimary else ChakhLeTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = address.tag,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ChakhLeTextPrimary
                                        )
                                        if (address.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = ChakhLeRedLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "DEFAULT",
                                                    color = ChakhLeRedPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${address.houseNo}, ${address.area}",
                                        fontSize = 12.sp,
                                        color = ChakhLeTextSecondary
                                    )
                                    Text(
                                        text = "${address.city} - ${address.pincode}",
                                        fontSize = 11.sp,
                                        color = ChakhLeTextMuted
                                    )
                                }

                                if (address.isDefault) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Default",
                                        tint = ChakhLeRedPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Support & FAQs Tab + Live Bot Chat
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Interactive Support Chat Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ChakhLeRedPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HeadsetMic,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = "Khaibu Live Foodie Support",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ChakhLeTextPrimary
                                        )
                                        Text(
                                            text = "Instant 24/7 AI Delivery Assistant",
                                            fontSize = 11.sp,
                                            color = ChakhLeSuccess
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Chat messages log
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(ChakhLeSurfaceVariant, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    chatMessages.takeLast(4).forEach { msg ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                                        ) {
                                            Surface(
                                                color = if (msg.isUser) ChakhLeRedPrimary else Color.White,
                                                shape = RoundedCornerShape(10.dp),
                                                shadowElevation = 1.dp
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(
                                                        text = msg.message,
                                                        color = if (msg.isUser) Color.White else ChakhLeTextPrimary,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = msg.time,
                                                        color = if (msg.isUser) Color.White.copy(alpha = 0.7f) else ChakhLeTextMuted,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.align(Alignment.End)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Chat Input
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = chatInputText,
                                        onValueChange = { chatInputText = it },
                                        placeholder = { Text("Ask about refund, order status...", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = ChakhLeTextPrimary,
                                            unfocusedTextColor = ChakhLeTextPrimary,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = ChakhLeRedPrimary,
                                            unfocusedBorderColor = ChakhLeBorder
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            viewModel.sendChatMessage(chatInputText)
                                            chatInputText = ""
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(ChakhLeRedPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Send",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // FAQs Accordion
                    item {
                        Text(
                            text = "FREQUENTLY ASKED QUESTIONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChakhLeTextMuted,
                            letterSpacing = 1.sp
                        )
                    }

                    items(faqs) { faq ->
                        FaqAccordionItem(faq = faq)
                    }

                    // Staff & Administration Portals
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "STAFF & PARTNER ACCESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChakhLeTextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (userRole == UserRole.CUSTOMER) {
                            // Public Foodie view - Staff can authenticate
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showStaffLoginDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEFF6FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Staff Login",
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Staff & Partner Login",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = ChakhLeTextPrimary
                                            )
                                            Text(
                                                text = "Sign in to identify as Kitchen Staff or Admin",
                                                fontSize = 11.sp,
                                                color = ChakhLeTextSecondary
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { showStaffLoginDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else if (userRole == UserRole.KITCHEN) {
                            // Active Kitchen Session
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ChakhLeAmberDark)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "👨‍🍳", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Identified as Kitchen Staff",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = ChakhLeAmberDark
                                                )
                                                Text(
                                                    text = currentKitchen?.name ?: "Kitchen Outlet",
                                                    fontSize = 11.sp,
                                                    color = ChakhLeTextSecondary
                                                )
                                            }
                                        }
                                        Surface(
                                            color = ChakhLeAmberDark,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "KITCHEN ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = onNavigateToKitchen,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ChakhLeAmberDark),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("Open Kitchen Orders", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.loginAsCustomer() },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("Return to Foodie", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        } else if (userRole == UserRole.ADMIN) {
                            // Active Admin Session
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2563EB))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "🛡️", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Identified as Super Admin",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF2563EB)
                                                )
                                                Text(
                                                    text = "Master Controls Active",
                                                    fontSize = 11.sp,
                                                    color = ChakhLeTextSecondary
                                                )
                                            }
                                        }
                                        Surface(
                                            color = Color(0xFF2563EB),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ADMIN ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = onNavigateToAdmin,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("Open Admin Console", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.loginAsCustomer() },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("Return to Foodie", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddressSheet) {
            AddressSelectionBottomSheet(
                addresses = savedAddresses,
                selectedAddressId = 0L,
                onSelectAddress = { viewModel.selectAddress(it) },
                onAddNewAddress = { viewModel.addNewAddress(it) },
                onAutoDetectLocation = { viewModel.autoDetectGpsLocation() },
                onDismiss = { showAddressSheet = false }
            )
        }

        if (showStaffLoginDialog) {
            StaffLoginDialog(
                onDismiss = { showStaffLoginDialog = false },
                onLoginSuccess = { role ->
                    if (role == UserRole.KITCHEN) onNavigateToKitchen()
                    else if (role == UserRole.ADMIN) onNavigateToAdmin()
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun PastOrderCard(
    order: OrderEntity,
    onTrackOrder: () -> Unit,
    onReorder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(order.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.timestamp))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
                        text = order.restaurantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ChakhLeTextPrimary
                    )
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = ChakhLeTextSecondary
                    )
                }

                Surface(
                    color = when (order.status) {
                        OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                        else -> ChakhLeAmberLight
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = order.status.displayName,
                        color = when (order.status) {
                            OrderStatus.DELIVERED -> ChakhLeSuccess
                            else -> ChakhLeAmberDark
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 10.dp))

            Text(
                text = order.itemsSummary,
                fontSize = 13.sp,
                color = ChakhLeTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Paid: ₹${order.totalAmount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ChakhLeTextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onTrackOrder,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Track", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onReorder,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reorder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FaqAccordionItem(
    faq: FaqItem,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = ChakhLeTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ChakhLeTextSecondary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 12.sp,
                        color = ChakhLeTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Staff & Partner Login Dialog for Profile Screen
 */
@Composable
fun StaffLoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit,
    viewModel: ChakhLeViewModel
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Staff & Partner Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cloud kitchen owners and platform administrators: sign in using your assigned credentials.",
                    fontSize = 12.sp,
                    color = ChakhLeTextSecondary
                )

                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Kitchen accounts are generated only by Super Admin. Hotel owners receive login details from Admin.",
                        fontSize = 10.sp,
                        color = Color(0xFF78350F),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFDE8E8),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 11.sp,
                            color = Color(0xFF9B1C1C),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = userId,
                    onValueChange = {
                        userId = it
                        errorMessage = null
                    },
                    label = { Text("Staff User ID *") },
                    placeholder = { Text("e.g. behrouz_kitchen or admin") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ChakhLeTextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Staff Password *") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ChakhLeTextMuted) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = ChakhLeTextMuted
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Testing credential quick fills
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                userId = "admin"
                                password = "admin"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("🛡️ Admin Fill", fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                userId = "behrouz_kitchen"
                                password = "Royal@123"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("👨‍🍳 Kitchen Fill", fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val success = viewModel.loginWithStaffCredentials(userId, password)
                    if (success) {
                        onDismiss()
                        onLoginSuccess(viewModel.userRole.value)
                    } else {
                        errorMessage = "Invalid credentials. Hotel owners must obtain login credentials from Super Admin."
                    }
                },
                enabled = userId.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Identify & Sign In", fontWeight = FontWeight.Bold)
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
