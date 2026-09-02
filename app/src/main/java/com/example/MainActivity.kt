package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.OrderStatus
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KitchenPortalScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.CategoryBiryaniBg
import com.example.ui.theme.ChakhLeAmber
import com.example.ui.theme.ChakhLeAmberDark
import com.example.ui.theme.ChakhLeBackground
import com.example.ui.theme.ChakhLeRedContainer
import com.example.ui.theme.ChakhLeRedLight
import com.example.ui.theme.ChakhLeRedPrimary
import com.example.ui.theme.ChakhLeSlate100
import com.example.ui.theme.ChakhLeSlate400
import com.example.ui.theme.ChakhLeSlate700
import com.example.ui.theme.ChakhLeSlate900
import com.example.ui.theme.ChakhLeSurface
import com.example.ui.theme.ChakhLeTextMuted
import com.example.ui.theme.ChakhLeTextPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChakhLeViewModel

enum class AppDestination(val route: String, val title: String) {
    SPLASH("splash", "Splash"),
    AUTH("auth", "Login"),
    HOME("home", "Delivery"),
    CART("cart", "Cart"),
    TRACKING("tracking", "Tracking"),
    KITCHEN("kitchen", "Kitchen"),
    PROFILE("profile", "Account")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChakhLeApp()
            }
        }
    }
}

@Composable
fun ChakhLeApp(viewModel: ChakhLeViewModel = viewModel()) {
    val isSplashFinished by viewModel.isSplashFinished.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val latestActiveOrder by viewModel.latestActiveOrder.collectAsState()

    var currentScreen by remember { mutableStateOf(AppDestination.HOME) }
    var trackingOrderId by remember { mutableStateOf<String?>(null) }

    val totalCartItems = cartItems.sumOf { it.quantity }
    val hasActiveOrder = latestActiveOrder != null && latestActiveOrder?.status != OrderStatus.DELIVERED && latestActiveOrder?.status != OrderStatus.CANCELLED

    if (!isSplashFinished) {
        SplashScreen(
            onContinue = {
                viewModel.finishSplash()
            }
        )
    } else if (!isLoggedIn) {
        AuthScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = currentScreen != AppDestination.SPLASH && currentScreen != AppDestination.AUTH,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.border(width = 1.dp, color = ChakhLeSlate100)
                    ) {
                        NavigationBar(
                            containerColor = Color.White,
                            contentColor = ChakhLeRedPrimary,
                            tonalElevation = 0.dp
                        ) {
                            // 1. Home / Food Delivery
                            NavigationBarItem(
                                selected = currentScreen == AppDestination.HOME,
                                onClick = { currentScreen = AppDestination.HOME },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = "Delivery"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "DELIVERY",
                                        fontWeight = if (currentScreen == AppDestination.HOME) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ChakhLeRedPrimary,
                                    selectedTextColor = ChakhLeRedPrimary,
                                    indicatorColor = CategoryBiryaniBg,
                                    unselectedIconColor = ChakhLeSlate400,
                                    unselectedTextColor = ChakhLeSlate400
                                ),
                                modifier = Modifier.testTag("nav_home")
                            )

                            // 2. Cart
                            NavigationBarItem(
                                selected = currentScreen == AppDestination.CART,
                                onClick = { currentScreen = AppDestination.CART },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (totalCartItems > 0) {
                                                Badge(
                                                    containerColor = ChakhLeRedPrimary,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        text = totalCartItems.toString(),
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (currentScreen == AppDestination.CART) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                                            contentDescription = "Cart"
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = "CART",
                                        fontWeight = if (currentScreen == AppDestination.CART) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ChakhLeRedPrimary,
                                    selectedTextColor = ChakhLeRedPrimary,
                                    indicatorColor = CategoryBiryaniBg,
                                    unselectedIconColor = ChakhLeSlate400,
                                    unselectedTextColor = ChakhLeSlate400
                                ),
                                modifier = Modifier.testTag("nav_cart")
                            )

                            // 3. Live Tracking
                            NavigationBarItem(
                                selected = currentScreen == AppDestination.TRACKING,
                                onClick = {
                                    currentScreen = AppDestination.TRACKING
                                },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (hasActiveOrder) {
                                                Badge(
                                                    containerColor = Color(0xFF16A34A),
                                                    modifier = Modifier.size(8.dp)
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (currentScreen == AppDestination.TRACKING) Icons.Filled.DeliveryDining else Icons.Outlined.DeliveryDining,
                                            contentDescription = "Tracking"
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = "TRACK",
                                        fontWeight = if (currentScreen == AppDestination.TRACKING) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ChakhLeRedPrimary,
                                    selectedTextColor = ChakhLeRedPrimary,
                                    indicatorColor = CategoryBiryaniBg,
                                    unselectedIconColor = ChakhLeSlate400,
                                    unselectedTextColor = ChakhLeSlate400
                                ),
                                modifier = Modifier.testTag("nav_tracking")
                            )

                            // 4. Kitchen Portal
                            NavigationBarItem(
                                selected = currentScreen == AppDestination.KITCHEN,
                                onClick = { currentScreen = AppDestination.KITCHEN },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppDestination.KITCHEN) Icons.Filled.Restaurant else Icons.Outlined.Restaurant,
                                        contentDescription = "Kitchen"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "KITCHEN",
                                        fontWeight = if (currentScreen == AppDestination.KITCHEN) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ChakhLeAmberDark,
                                    selectedTextColor = ChakhLeAmberDark,
                                    indicatorColor = Color(0xFFFEF3C7),
                                    unselectedIconColor = ChakhLeSlate400,
                                    unselectedTextColor = ChakhLeSlate400
                                ),
                                modifier = Modifier.testTag("nav_kitchen")
                            )

                            // 5. Profile & Order History
                            NavigationBarItem(
                                selected = currentScreen == AppDestination.PROFILE,
                                onClick = { currentScreen = AppDestination.PROFILE },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                        contentDescription = "Account"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "ACCOUNT",
                                        fontWeight = if (currentScreen == AppDestination.PROFILE) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ChakhLeRedPrimary,
                                    selectedTextColor = ChakhLeRedPrimary,
                                    indicatorColor = CategoryBiryaniBg,
                                    unselectedIconColor = ChakhLeSlate400,
                                    unselectedTextColor = ChakhLeSlate400
                                ),
                                modifier = Modifier.testTag("nav_profile")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ChakhLeBackground)
            ) {
                Crossfade(
                    targetState = currentScreen,
                    label = "screen_crossfade"
                ) { screen ->
                    when (screen) {
                        AppDestination.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToCart = { currentScreen = AppDestination.CART },
                                onNavigateToKitchen = { currentScreen = AppDestination.KITCHEN },
                                onNavigateToTracking = { orderId ->
                                    trackingOrderId = orderId
                                    currentScreen = AppDestination.TRACKING
                                }
                            )
                        }
                        AppDestination.CART -> {
                            CartScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = AppDestination.HOME },
                                onOrderPlaced = { orderId ->
                                    trackingOrderId = orderId
                                    currentScreen = AppDestination.TRACKING
                                }
                            )
                        }
                        AppDestination.TRACKING -> {
                            OrderTrackingScreen(
                                viewModel = viewModel,
                                orderIdParam = trackingOrderId,
                                onNavigateHome = { currentScreen = AppDestination.HOME },
                                onNavigateToKitchen = { currentScreen = AppDestination.KITCHEN }
                            )
                        }
                        AppDestination.KITCHEN -> {
                            KitchenPortalScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = AppDestination.HOME },
                                onTrackOrder = { orderId ->
                                    trackingOrderId = orderId
                                    currentScreen = AppDestination.TRACKING
                                }
                            )
                        }
                        AppDestination.PROFILE -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = AppDestination.HOME },
                                onTrackOrder = { orderId ->
                                    trackingOrderId = orderId
                                    currentScreen = AppDestination.TRACKING
                                },
                                onNavigateToCart = { currentScreen = AppDestination.CART }
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
