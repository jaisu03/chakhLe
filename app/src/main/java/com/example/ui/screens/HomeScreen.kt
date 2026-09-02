package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dish
import com.example.data.model.FoodCategory
import com.example.data.model.FoodCategoryType
import com.example.data.model.OrderStatus
import com.example.data.model.Restaurant
import com.example.ui.components.ActiveOrderFloatingBanner
import com.example.ui.components.AddressSelectionBottomSheet
import com.example.ui.components.DishFeedCard
import com.example.ui.components.FilterSearchBar
import com.example.ui.components.FoodArtVisual
import com.example.ui.components.HomeTopBar
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.PromoHeroBanner
import com.example.ui.components.RatingBadge
import com.example.ui.theme.CategoryBeveragesBg
import com.example.ui.theme.CategoryBiryaniBg
import com.example.ui.theme.CategoryDessertBg
import com.example.ui.theme.CategoryPizzaBg
import com.example.ui.theme.CategoryStreetFoodBg
import com.example.ui.theme.CategoryThaliBg
import com.example.ui.theme.ChakhLeAmber
import com.example.ui.theme.ChakhLeAmberDark
import com.example.ui.theme.ChakhLeAmberLight
import com.example.ui.theme.ChakhLeBackground
import com.example.ui.theme.ChakhLeBorder
import com.example.ui.theme.ChakhLeRedContainer
import com.example.ui.theme.ChakhLeRedPrimary
import com.example.ui.theme.ChakhLeSlate100
import com.example.ui.theme.ChakhLeSlate200
import com.example.ui.theme.ChakhLeSlate500
import com.example.ui.theme.ChakhLeSlate900
import com.example.ui.theme.ChakhLeSurface
import com.example.ui.theme.ChakhLeSurfaceVariant
import com.example.ui.theme.ChakhLeTextMuted
import com.example.ui.theme.ChakhLeTextPrimary
import com.example.ui.theme.ChakhLeTextSecondary
import com.example.ui.theme.ChakhLeVegGreen
import com.example.ui.viewmodel.ChakhLeViewModel

@Composable
fun HomeScreen(
    viewModel: ChakhLeViewModel,
    onNavigateToCart: () -> Unit,
    onNavigateToKitchen: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAddress by viewModel.currentAddressTitle.collectAsState()
    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isPureVegOnly by viewModel.isPureVegOnly.collectAsState()
    val isTopRatedOnly by viewModel.isTopRatedOnly.collectAsState()
    val dishes by viewModel.filteredDishes.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()

    var showAddressSheet by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val totalCartItems = cartItems.sumOf { it.quantity }
    val cartTotal = viewModel.calculateFinalTotal()
    val latestActiveOrder by viewModel.latestActiveOrder.collectAsState()
    val hasActiveOrder = latestActiveOrder != null && latestActiveOrder?.status != OrderStatus.DELIVERED && latestActiveOrder?.status != OrderStatus.CANCELLED

    Box(modifier = modifier.fillMaxSize().background(ChakhLeBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sticky Top Bar
            HomeTopBar(
                currentAddress = currentAddress,
                unreadNotificationsCount = notifications.count { !it.isRead },
                cartItemCount = totalCartItems,
                cartTotal = cartTotal,
                onAddressClick = { showAddressSheet = true },
                onNotificationClick = { showNotificationsDialog = true },
                onCartClick = onNavigateToCart,
                onKitchenToggleClick = onNavigateToKitchen
            )

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = if (totalCartItems > 0 || hasActiveOrder) 130.dp else 24.dp)
            ) {
                // 1. Search Bar & Quick Filters
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        FilterSearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            isPureVegOnly = isPureVegOnly,
                            onPureVegToggle = { viewModel.togglePureVeg(it) },
                            isTopRatedOnly = isTopRatedOnly,
                            onTopRatedToggle = { viewModel.toggleTopRated() }
                        )
                    }
                }

                // 2. Hero Promotional Carousel Banners
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            PromoHeroBanner(
                                title = "50% OFF on your first 3 orders",
                                subtitle = "Delicious meals delivered under 25 mins!",
                                code = "CHAKHLE50",
                                bannerType = 0,
                                onApplyCode = {
                                    viewModel.applyCoupon(it)
                                    onNavigateToCart()
                                },
                                modifier = Modifier.width(300.dp)
                            )
                        }
                        item {
                            PromoHeroBanner(
                                title = "Free Delivery on Biryani Feasts",
                                subtitle = "Authentic Dum handis with complimentary raita",
                                code = "FREEDEL",
                                bannerType = 1,
                                onApplyCode = {
                                    viewModel.applyCoupon(it)
                                    onNavigateToCart()
                                },
                                modifier = Modifier.width(300.dp)
                            )
                        }
                        item {
                            PromoHeroBanner(
                                title = "Save Flat ₹100 on Family Orders",
                                subtitle = "Grand Thalis & Gourmet Pizzas",
                                code = "FEAST100",
                                bannerType = 2,
                                onApplyCode = {
                                    viewModel.applyCoupon(it)
                                    onNavigateToCart()
                                },
                                modifier = Modifier.width(300.dp)
                            )
                        }
                    }
                }

                // 3. Category Carousel
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOP CATEGORIES",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.75.sp,
                                color = ChakhLeSlate900
                            )
                            if (selectedCategory != FoodCategoryType.ALL) {
                                TextButton(onClick = { viewModel.selectCategory(FoodCategoryType.ALL) }) {
                                    Text(
                                        text = "VIEW ALL",
                                        fontSize = 11.sp,
                                        color = ChakhLeRedPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewModel.categories) { cat ->
                                val isSelected = selectedCategory == cat.type
                                CategoryCarouselItem(
                                    category = cat,
                                    isSelected = isSelected,
                                    onClick = { viewModel.selectCategory(cat.type) }
                                )
                            }
                        }
                    }
                }

                // 4. Featured Multi-Vendor Restaurants Horizontal Spotlight
                if (searchQuery.isBlank() && selectedCategory == FoodCategoryType.ALL) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOP KITCHENS NEAR YOU",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.75.sp,
                                    color = ChakhLeSlate900
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(restaurants) { rest ->
                                    RestaurantSpotlightCard(restaurant = rest)
                                }
                            }
                        }
                    }
                }

                // 5. Popular Dishes & Live Food Feed
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory != FoodCategoryType.ALL) "${selectedCategory.displayName.uppercase()} (${dishes.size})" else "POPULAR DISHES NEAR YOU (${dishes.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.75.sp,
                            color = ChakhLeSlate900
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (dishes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Fastfood,
                                    contentDescription = null,
                                    tint = ChakhLeTextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No dishes found matching your filter",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ChakhLeTextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.setSearchQuery("")
                                        viewModel.togglePureVeg(false)
                                        viewModel.selectCategory(FoodCategoryType.ALL)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary)
                                ) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    }
                } else {
                    items(dishes, key = { it.id }) { dish ->
                        val cartItem = cartItems.find { it.dishId == dish.id }
                        val quantity = cartItem?.quantity ?: 0

                        DishFeedCard(
                            dish = dish,
                            cartQuantity = quantity,
                            onAddToCart = { viewModel.addToCart(dish) },
                            onDecrease = { viewModel.decreaseCartItem(dish.id) },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Floating Bars Stack at the bottom: Active Order status + Cart Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Order Tracker Pill when active
            if (hasActiveOrder && latestActiveOrder != null) {
                val active = latestActiveOrder!!
                ActiveOrderFloatingBanner(
                    orderNumber = active.orderId,
                    statusText = "${active.status.displayName} • Delivery to ${active.deliveryAddress}",
                    etaMinutes = active.estimatedArrivalMinutes,
                    onClick = { onNavigateToTracking(active.orderId) }
                )
            }

            // Floating Real-Time Bottom Cart Bar when items are present
            if (totalCartItems > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToCart() }
                        .testTag("floating_cart_bar"),
                    color = ChakhLeRedPrimary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = totalCartItems.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "$totalCartItems ITEM${if (totalCartItems > 1) "S" else ""} ADDED",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "₹${cartTotal.toInt()} plus taxes",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VIEW CART",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Cart",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Address Selector Sheet
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

        // Notifications Modal
        if (showNotificationsDialog) {
            NotificationsDialog(
                notifications = notifications,
                onDismiss = { showNotificationsDialog = false }
            )
        }
    }
}

/**
 * Category Carousel Item Button - Geometric Balance Style
 */
@Composable
fun CategoryCarouselItem(
    category: FoodCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when (category.type) {
        FoodCategoryType.BIRYANI -> CategoryBiryaniBg
        FoodCategoryType.PIZZA_FAST_FOOD -> CategoryPizzaBg
        FoodCategoryType.INDIAN_THALI -> CategoryThaliBg
        FoodCategoryType.DESSERTS -> CategoryDessertBg
        FoodCategoryType.STREET_FOOD -> CategoryStreetFoodBg
        FoodCategoryType.BEVERAGES -> CategoryBeveragesBg
        else -> ChakhLeSlate100
    }

    Column(
        modifier = modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) ChakhLeRedPrimary else ChakhLeSlate200,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(if (isSelected) CategoryPizzaBg else bgColor),
            contentAlignment = Alignment.Center
        ) {
            FoodArtVisual(
                category = category.type,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name.uppercase(),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) ChakhLeRedPrimary else ChakhLeSlate500,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            letterSpacing = 0.25.sp,
            maxLines = 2,
            lineHeight = 13.sp
        )
    }
}

/**
 * Restaurant Spotlight Card
 */
@Composable
fun RestaurantSpotlightCard(
    restaurant: Restaurant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(ChakhLeRedPrimary, ChakhLeAmber)
                        )
                    )
            ) {
                // Badge
                if (restaurant.featuredTag != null) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = restaurant.featuredTag,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Delivery Time Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${restaurant.deliveryTimeMinutes} mins",
                        color = ChakhLeTextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = restaurant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ChakhLeTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = restaurant.cuisine,
                    fontSize = 11.sp,
                    color = ChakhLeTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBadge(rating = restaurant.rating)
                    Text(
                        text = "₹${restaurant.priceForTwo} for two",
                        fontSize = 11.sp,
                        color = ChakhLeTextMuted
                    )
                }
            }
        }
    }
}
