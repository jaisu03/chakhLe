package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dish
import com.example.data.model.FoodCategoryType
import com.example.data.model.Restaurant
import com.example.ui.components.DishFeedCard
import com.example.ui.components.RatingBadge
import com.example.ui.theme.ChakhLeAmber
import com.example.ui.theme.ChakhLeAmberDark
import com.example.ui.theme.ChakhLeBackground
import com.example.ui.theme.ChakhLeBorder
import com.example.ui.theme.ChakhLeRedContainer
import com.example.ui.theme.ChakhLeRedPrimary
import com.example.ui.theme.ChakhLeSlate100
import com.example.ui.theme.ChakhLeSlate200
import com.example.ui.theme.ChakhLeSlate400
import com.example.ui.theme.ChakhLeSlate500
import com.example.ui.theme.ChakhLeSlate700
import com.example.ui.theme.ChakhLeSlate800
import com.example.ui.theme.ChakhLeSlate900
import com.example.ui.theme.ChakhLeSuccess
import com.example.ui.theme.ChakhLeSurface
import com.example.ui.theme.ChakhLeTextMuted
import com.example.ui.theme.ChakhLeTextPrimary
import com.example.ui.theme.ChakhLeTextSecondary
import com.example.ui.theme.ChakhLeVegGreen
import com.example.ui.viewmodel.ChakhLeViewModel

@Composable
fun KitchenDetailScreen(
    kitchenId: String,
    viewModel: ChakhLeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val restaurants by viewModel.restaurants.collectAsState()
    val allDishes by viewModel.allDishes.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val restaurant = remember(restaurants, kitchenId) {
        restaurants.find { it.id == kitchenId } ?: restaurants.firstOrNull()
    }

    val kitchenDishes = remember(allDishes, restaurant) {
        if (restaurant == null) emptyList()
        else allDishes.filter { it.restaurantId == restaurant.id || it.restaurantName.equals(restaurant.name, ignoreCase = true) }
    }

    val totalCartItems = cartItems.sumOf { it.quantity }
    val cartTotal = viewModel.calculateFinalTotal()

    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filterTabs = listOf("All Dishes (${kitchenDishes.size})", "Pure Veg", "Bestsellers")

    val displayedDishes = remember(kitchenDishes, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> kitchenDishes.filter { it.isVeg }
            2 -> kitchenDishes.filter { it.isBestseller || it.rating >= 4.7 }
            else -> kitchenDishes
        }
    }

    if (restaurant == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ChakhLeBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Kitchen details not found",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ChakhLeTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary)
                ) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChakhLeBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar with back button, kitchen name and cart shortcut
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ChakhLeSlate900
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = restaurant.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = ChakhLeSlate900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = restaurant.cuisine,
                                fontSize = 11.sp,
                                color = ChakhLeTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Cart Shortcut
                    IconButton(
                        onClick = onNavigateToCart,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (totalCartItems > 0) ChakhLeRedPrimary else ChakhLeSlate100)
                    ) {
                        BadgedBox(
                            badge = {
                                if (totalCartItems > 0) {
                                    Badge(containerColor = Color.White, contentColor = ChakhLeRedPrimary) {
                                        Text(
                                            text = totalCartItems.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Cart",
                                tint = if (totalCartItems > 0) Color.White else ChakhLeSlate800,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Scrollable Body: Kitchen Details Header + Location + Menu Items
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = if (totalCartItems > 0) 100.dp else 24.dp)
            ) {
                // Kitchen Hero Header Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, ChakhLeSlate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Hero Banner with Gradient & Badges
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ChakhLeRedPrimary, ChakhLeAmber)
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                // Restaurant Culinary Watermark Icon
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.22f),
                                    modifier = Modifier
                                        .size(64.dp)
                                        .align(Alignment.Center)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    if (restaurant.featuredTag != null) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.65f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = restaurant.featuredTag,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Surface(
                                        color = if (restaurant.isOpen) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (restaurant.isOpen) "● OPEN NOW" else "● CLOSED",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                // Delivery Time Badge on bottom
                                Surface(
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccessTime,
                                            contentDescription = null,
                                            tint = ChakhLeRedPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${restaurant.deliveryTimeMinutes} mins delivery",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ChakhLeSlate900
                                        )
                                    }
                                }
                            }

                            // Kitchen Info Block
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = restaurant.name,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 19.sp,
                                        color = ChakhLeSlate900
                                    )
                                    RatingBadge(rating = restaurant.rating, reviewCount = restaurant.reviewCount)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = restaurant.cuisine,
                                    fontSize = 13.sp,
                                    color = ChakhLeTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = ChakhLeSlate100)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Kitchen Location section
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(ChakhLeRedContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Kitchen Location",
                                            tint = ChakhLeRedPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "KITCHEN LOCATION",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ChakhLeRedPrimary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = restaurant.address,
                                            fontSize = 13.sp,
                                            color = ChakhLeSlate800,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.DirectionsWalk,
                                                contentDescription = null,
                                                tint = ChakhLeSlate400,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${restaurant.distanceKm} km away from your location",
                                                fontSize = 11.sp,
                                                color = ChakhLeSlate500
                                            )
                                        }
                                    }
                                }

                                if (restaurant.offerText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = ChakhLeAmber.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, ChakhLeAmber.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = restaurant.offerText,
                                                color = ChakhLeAmberDark,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (restaurant.ownerPhone.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = null,
                                            tint = ChakhLeSlate400,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Kitchen Contact: ${restaurant.ownerPhone}",
                                            fontSize = 11.sp,
                                            color = ChakhLeSlate500
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Food Items Header & Filter Tabs
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FOOD ITEMS FROM THIS KITCHEN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.75.sp,
                                color = ChakhLeSlate900
                            )
                            Text(
                                text = "${displayedDishes.size} available",
                                fontSize = 12.sp,
                                color = ChakhLeTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TabRow(
                            selectedTabIndex = selectedFilterIndex,
                            containerColor = Color.Transparent,
                            contentColor = ChakhLeRedPrimary,
                            indicator = { tabPositions ->
                                if (selectedFilterIndex < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[selectedFilterIndex]),
                                        color = ChakhLeRedPrimary,
                                        height = 3.dp
                                    )
                                }
                            },
                            divider = { HorizontalDivider(color = ChakhLeSlate200) }
                        ) {
                            filterTabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedFilterIndex == index,
                                    onClick = { selectedFilterIndex = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedFilterIndex == index) ChakhLeRedPrimary else ChakhLeSlate500
                                        )
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // All Food items from this kitchen
                if (displayedDishes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Fastfood,
                                    contentDescription = null,
                                    tint = ChakhLeTextMuted,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No dishes found in this category",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ChakhLeTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(displayedDishes, key = { it.id }) { dish ->
                        val cartItem = cartItems.find { it.dishId == dish.id }
                        val quantity = cartItem?.quantity ?: 0

                        DishFeedCard(
                            dish = dish,
                            cartQuantity = quantity,
                            onAddToCart = { viewModel.addToCart(dish) },
                            onDecrease = { viewModel.decreaseCartItem(dish.id) },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("kitchen_dish_${dish.id}")
                        )
                    }
                }
            }
        }

        // Floating Cart Bar at bottom when items exist
        if (totalCartItems > 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                color = ChakhLeRedPrimary,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCart() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$totalCartItems ITEM${if (totalCartItems > 1) "S" else ""} ADDED",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "₹${cartTotal.toInt()}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VIEW CART",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
