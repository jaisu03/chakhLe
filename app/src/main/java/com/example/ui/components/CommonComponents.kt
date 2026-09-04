package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AddressEntity
import com.example.data.model.Dish
import com.example.data.model.FoodCategoryType
import com.example.data.model.NotificationItem
import com.example.data.model.PromoCoupon
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
import com.example.ui.theme.ChakhLeGold
import com.example.ui.theme.ChakhLeNonVegRed
import com.example.ui.theme.ChakhLeRedContainer
import com.example.ui.theme.ChakhLeRedDark
import com.example.ui.theme.ChakhLeRedLight
import com.example.ui.theme.ChakhLeRedPrimary
import com.example.ui.theme.ChakhLeSlate100
import com.example.ui.theme.ChakhLeSlate200
import com.example.ui.theme.ChakhLeSlate400
import com.example.ui.theme.ChakhLeSlate50
import com.example.ui.theme.ChakhLeSlate500
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

/**
 * Standard Indian Food Safety Veg / Non-Veg Indicator Symbol
 */
@Composable
fun FoodTypeIndicator(
    isVeg: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    val borderColor = if (isVeg) ChakhLeVegGreen else ChakhLeNonVegRed
    val dotColor = if (isVeg) ChakhLeVegGreen else ChakhLeNonVegRed

    Box(
        modifier = modifier
            .size(size)
            .border(1.5.dp, borderColor, RoundedCornerShape(3.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isVeg) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(dotColor)
            )
        } else {
            // Triangle for non-veg indicator
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(this@Canvas.size.width / 2f, 0f)
                    lineTo(this@Canvas.size.width, this@Canvas.size.height)
                    lineTo(0f, this@Canvas.size.height)
                    close()
                }
                drawPath(path, color = dotColor)
            }
        }
    }
}

/**
 * Rating Star Badge (e.g. 4.8 ★)
 */
@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
    reviewCount: String? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ChakhLeVegGreen)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = String.format("%.1f", rating),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating",
            tint = Color.White,
            modifier = Modifier.size(10.dp)
        )
        if (reviewCount != null) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "($reviewCount)",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Price Display in INR with optional Strikethrough
 */
@Composable
fun PriceDisplay(
    price: Double,
    modifier: Modifier = Modifier,
    originalPrice: Double? = null,
    fontSize: Int = 16
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "₹${price.toInt()}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize.sp,
            color = ChakhLeTextPrimary
        )
        if (originalPrice != null && originalPrice > price) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "₹${originalPrice.toInt()}",
                fontSize = (fontSize - 3).sp,
                color = ChakhLeTextMuted,
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}

/**
 * Rich Custom Visual Canvas Art for Food Categories and Dishes
 */
@Composable
fun FoodArtVisual(
    category: FoodCategoryType,
    modifier: Modifier = Modifier,
    isVeg: Boolean = true
) {
    val bgGradient = when (category) {
        FoodCategoryType.BIRYANI -> Brush.linearGradient(
            listOf(Color(0xFFE65100), Color(0xFFFFB74D), Color(0xFF8D6E63))
        )
        FoodCategoryType.PIZZA_FAST_FOOD -> Brush.linearGradient(
            listOf(Color(0xFFD84315), Color(0xFFFF8A65), Color(0xFFFFD54F))
        )
        FoodCategoryType.INDIAN_THALI -> Brush.linearGradient(
            listOf(Color(0xFF1B5E20), Color(0xFF66BB6A), Color(0xFFFFCA28))
        )
        FoodCategoryType.DESSERTS -> Brush.linearGradient(
            listOf(Color(0xFF880E4F), Color(0xFFEC407A), Color(0xFFFFCC80))
        )
        FoodCategoryType.STREET_FOOD -> Brush.linearGradient(
            listOf(Color(0xFFE64A19), Color(0xFFFF7043), Color(0xFFFFEE58))
        )
        FoodCategoryType.BEVERAGES -> Brush.linearGradient(
            listOf(Color(0xFF00695C), Color(0xFF26A69A), Color(0xFFFFE082))
        )
        else -> Brush.linearGradient(
            listOf(ChakhLeRedPrimary, ChakhLeAmber)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Abstract warm glow circle
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = w * 0.38f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            when (category) {
                FoodCategoryType.BIRYANI -> drawBiryaniClayPot(w, h)
                FoodCategoryType.PIZZA_FAST_FOOD -> drawPizzaSlice(w, h)
                FoodCategoryType.INDIAN_THALI -> drawThaliPlate(w, h)
                FoodCategoryType.DESSERTS -> drawDessertBowl(w, h)
                FoodCategoryType.STREET_FOOD -> drawChaatPlate(w, h)
                FoodCategoryType.BEVERAGES -> drawDrinkGlass(w, h)
                else -> drawGenericDish(w, h)
            }
        }
    }
}

private fun DrawScope.drawBiryaniClayPot(w: Float, h: Float) {
    // Clay Handi Pot
    val potPath = Path().apply {
        moveTo(w * 0.3f, h * 0.42f)
        cubicTo(w * 0.22f, h * 0.65f, w * 0.28f, h * 0.78f, w * 0.5f, h * 0.8f)
        cubicTo(w * 0.72f, h * 0.78f, w * 0.78f, h * 0.65f, w * 0.7f, h * 0.42f)
        close()
    }
    drawPath(potPath, color = Color(0xFF4E342E))

    // Golden Rice Mount
    drawOval(
        color = Color(0xFFFFD54F),
        topLeft = Offset(w * 0.32f, h * 0.35f),
        size = Size(w * 0.36f, h * 0.15f)
    )
    // Saffron and mint grains
    drawCircle(Color(0xFFE65100), radius = w * 0.025f, center = Offset(w * 0.45f, h * 0.40f))
    drawCircle(Color(0xFF2E7D32), radius = w * 0.02f, center = Offset(w * 0.52f, h * 0.38f))
    drawCircle(Color(0xFFFFB300), radius = w * 0.025f, center = Offset(w * 0.55f, h * 0.42f))

    // Steam
    drawLine(
        color = Color.White.copy(alpha = 0.6f),
        start = Offset(w * 0.45f, h * 0.28f),
        end = Offset(w * 0.45f, h * 0.18f),
        strokeWidth = 3f
    )
    drawLine(
        color = Color.White.copy(alpha = 0.6f),
        start = Offset(w * 0.55f, h * 0.25f),
        end = Offset(w * 0.55f, h * 0.15f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawPizzaSlice(w: Float, h: Float) {
    // Triangular slice
    val pizzaPath = Path().apply {
        moveTo(w * 0.5f, h * 0.78f)
        lineTo(w * 0.22f, h * 0.30f)
        cubicTo(w * 0.35f, h * 0.22f, w * 0.65f, h * 0.22f, w * 0.78f, h * 0.30f)
        close()
    }
    drawPath(pizzaPath, color = Color(0xFFFFC107))

    // Crust
    val crustPath = Path().apply {
        moveTo(w * 0.22f, h * 0.30f)
        cubicTo(w * 0.35f, h * 0.22f, w * 0.65f, h * 0.22f, w * 0.78f, h * 0.30f)
    }
    drawPath(crustPath, color = Color(0xFFD84315), style = Stroke(width = w * 0.06f))

    // Toppings
    drawCircle(Color(0xFFC62828), radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.42f))
    drawCircle(Color(0xFFC62828), radius = w * 0.035f, center = Offset(w * 0.58f, h * 0.48f))
    drawCircle(Color(0xFF2E7D32), radius = w * 0.03f, center = Offset(w * 0.48f, h * 0.60f))
    drawCircle(Color(0xFF212121), radius = w * 0.025f, center = Offset(w * 0.50f, h * 0.38f))
}

private fun DrawScope.drawThaliPlate(w: Float, h: Float) {
    // Silver Thali Plate
    drawCircle(
        color = Color(0xFFECEFF1),
        radius = w * 0.35f,
        center = Offset(w * 0.5f, h * 0.52f)
    )
    drawCircle(
        color = Color(0xFFCFD8DC),
        radius = w * 0.33f,
        center = Offset(w * 0.5f, h * 0.52f),
        style = Stroke(width = 3f)
    )

    // Center Rice / Bread
    drawCircle(
        color = Color(0xFFFFF9C4),
        radius = w * 0.12f,
        center = Offset(w * 0.5f, h * 0.52f)
    )

    // Katoris (Curry bowls)
    drawCircle(Color(0xFFD84315), radius = w * 0.07f, center = Offset(w * 0.32f, h * 0.42f)) // Paneer
    drawCircle(Color(0xFF3E2723), radius = w * 0.07f, center = Offset(w * 0.68f, h * 0.42f)) // Dal Makhani
    drawCircle(Color(0xFF2E7D32), radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.28f)) // Saag
    drawCircle(Color(0xFFFFB300), radius = w * 0.06f, center = Offset(w * 0.35f, h * 0.68f)) // Sweet
}

private fun DrawScope.drawDessertBowl(w: Float, h: Float) {
    // Sweet Bowl
    drawOval(
        color = Color(0xFFFFF8E1),
        topLeft = Offset(w * 0.25f, h * 0.45f),
        size = Size(w * 0.5f, h * 0.35f)
    )
    // Gulab Jamun balls
    drawCircle(Color(0xFF3E2723), radius = w * 0.09f, center = Offset(w * 0.42f, h * 0.52f))
    drawCircle(Color(0xFF4E342E), radius = w * 0.085f, center = Offset(w * 0.58f, h * 0.50f))
    // Pistachio garnish
    drawCircle(Color(0xFF689F38), radius = w * 0.02f, center = Offset(w * 0.42f, h * 0.48f))
    drawCircle(Color(0xFFFFD54F), radius = w * 0.015f, center = Offset(w * 0.56f, h * 0.46f))
}

private fun DrawScope.drawChaatPlate(w: Float, h: Float) {
    // Pav & Bhaji or Pani Puri
    drawOval(
        color = Color(0xFFFFECB3),
        topLeft = Offset(w * 0.22f, h * 0.42f),
        size = Size(w * 0.56f, h * 0.38f)
    )
    // Spicy Bhaji
    drawCircle(Color(0xFFC62828), radius = w * 0.14f, center = Offset(w * 0.42f, h * 0.58f))
    // Butter dollop
    drawCircle(Color(0xFFFFEE58), radius = w * 0.04f, center = Offset(w * 0.42f, h * 0.58f))
    // Golden Toasted Pav
    drawRoundRect(
        color = Color(0xFFFFB74D),
        topLeft = Offset(w * 0.58f, h * 0.48f),
        size = Size(w * 0.20f, h * 0.22f),
        cornerRadius = CornerRadius(8f, 8f)
    )
}

private fun DrawScope.drawDrinkGlass(w: Float, h: Float) {
    // Glass
    val glassPath = Path().apply {
        moveTo(w * 0.38f, h * 0.28f)
        lineTo(w * 0.62f, h * 0.28f)
        lineTo(w * 0.58f, h * 0.78f)
        lineTo(w * 0.42f, h * 0.78f)
        close()
    }
    drawPath(glassPath, color = Color(0xFFFFCA28))

    // Straw
    drawLine(
        color = Color(0xFFE91E63),
        start = Offset(w * 0.50f, h * 0.70f),
        end = Offset(w * 0.68f, h * 0.18f),
        strokeWidth = 6f
    )
    // Mint leaf
    drawCircle(Color(0xFF2E7D32), radius = w * 0.04f, center = Offset(w * 0.42f, h * 0.32f))
}

private fun DrawScope.drawGenericDish(w: Float, h: Float) {
    drawCircle(Color.White, radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.5f))
    drawCircle(ChakhLeAmber, radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.5f))
}

/**
 * Top App Bar with Delivery Address, Notification Bell & Cart Badge - Geometric Balance Style
 */
@Composable
fun HomeTopBar(
    currentAddress: String,
    unreadNotificationsCount: Int,
    cartItemCount: Int,
    cartTotal: Double,
    onAddressClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCartClick: () -> Unit,
    onKitchenToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Brand Logo + Delivery Location Selector
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAddressClick() }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Geometric Brand Badge "C"
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ChakhLeRedPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "C",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DELIVER TO",
                                fontSize = 10.sp,
                                color = ChakhLeRedPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ChakhLeAmber)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentAddress,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeSlate900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "▼",
                                fontSize = 9.sp,
                                color = ChakhLeSlate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Buttons with Geometric Balance styling
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Kitchen portal shortcut
                    IconButton(
                        onClick = onKitchenToggleClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CategoryBiryaniBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Kitchen Portal",
                            tint = ChakhLeAmberDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notifications
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ChakhLeSlate100)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = ChakhLeRedPrimary) {
                                        Text(unreadNotificationsCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = ChakhLeSlate800,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Cart Icon Button in Slate-100 or Red when filled
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (cartItemCount > 0) ChakhLeRedPrimary else ChakhLeSlate100)
                            .testTag("top_bar_cart_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(
                                        containerColor = ChakhLeAmber,
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            text = cartItemCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Cart",
                                tint = if (cartItemCount > 0) Color.White else ChakhLeSlate800,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Promotional Hero Banner Carousel Card - Geometric Balance Style
 */
@Composable
fun PromoHeroBanner(
    title: String,
    subtitle: String,
    code: String,
    bannerType: Int = 0,
    onApplyCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgBrush = when (bannerType % 3) {
        0 -> Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFFF59E0B)))
        1 -> Brush.linearGradient(listOf(Color(0xFFB91C1C), Color(0xFFEA580C)))
        else -> Brush.linearGradient(listOf(Color(0xFFC2410C), Color(0xFFD97706)))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(16.dp)
        ) {
            // Large circular platter artwork container in top-right
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (bannerType % 3) {
                        0 -> "🥘"
                        1 -> "🍛"
                        else -> "🍕"
                    },
                    fontSize = 38.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 80.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onApplyCode(code) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = ChakhLeRedPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "ORDER NOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = code,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dish Item Card with Geometric Balance Styling: Rounded corners, Slate accents, Crisp Badges & Dynamic Stepper
 */
@Composable
fun DishFeedCard(
    dish: Dish,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChakhLeSlate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FoodTypeIndicator(isVeg = dish.isVeg)
                        if (dish.isBestseller) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ChakhLeRedPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "BESTSELLER",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dish.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ChakhLeSlate900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = dish.restaurantName,
                        fontSize = 12.sp,
                        color = ChakhLeSlate500,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RatingBadge(rating = dish.rating, reviewCount = dish.ratingCount.toString())
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = "Prep time",
                                tint = ChakhLeSlate400,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${dish.prepTimeMinutes} mins",
                                fontSize = 11.sp,
                                color = ChakhLeSlate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dish.description,
                        fontSize = 11.sp,
                        color = ChakhLeSlate500,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                }

                // Right Column: Image Art + Dynamic Add/Quantity Controller
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(108.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        FoodArtVisual(
                            category = dish.category,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp)),
                            isVeg = dish.isVeg
                        )

                        // Floating Add / Stepper Controller
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                        ) {
                            if (cartQuantity == 0) {
                                Button(
                                    onClick = onAddToCart,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = ChakhLeRedPrimary
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, ChakhLeRedPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("add_dish_${dish.id}")
                                ) {
                                    Text(
                                        text = "+ ADD",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = ChakhLeRedPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .background(ChakhLeRedPrimary)
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = onDecrease,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Text(
                                        text = cartQuantity.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = onAddToCart,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = dish.portionSize,
                        fontSize = 10.sp,
                        color = ChakhLeSlate400,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Price & Free Delivery Guarantee Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriceDisplay(
                    price = dish.price,
                    originalPrice = dish.originalPrice,
                    fontSize = 16
                )

                // Geometric Free Delivery Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ChakhLeSlate50)
                        .border(1.dp, ChakhLeSlate200, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ChakhLeVegGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FREE DELIVERY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChakhLeSlate600,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Filter and Search Controls Bar - Geometric Balance Style
 */
@Composable
fun FilterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isPureVegOnly: Boolean,
    onPureVegToggle: (Boolean) -> Unit,
    isTopRatedOnly: Boolean,
    onTopRatedToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Geometric Clean Search Box with Slate-100 container
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_food_input"),
            placeholder = {
                Text(
                    text = "Search for 'Biryani', 'Pizza', 'Haldiram'...",
                    color = ChakhLeSlate400,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ChakhLeSlate500
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = ChakhLeSlate500
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ChakhLeTextPrimary,
                unfocusedTextColor = ChakhLeTextPrimary,
                focusedContainerColor = ChakhLeSlate100,
                unfocusedContainerColor = ChakhLeSlate100,
                focusedBorderColor = ChakhLeRedPrimary,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Filters Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pure Veg Switch Chip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.2.dp,
                        if (isPureVegOnly) ChakhLeVegGreen else ChakhLeSlate200,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onPureVegToggle(!isPureVegOnly) },
                color = if (isPureVegOnly) Color(0xFFECFDF5) else Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FoodTypeIndicator(isVeg = true, size = 12.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PURE VEG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPureVegOnly) ChakhLeVegGreen else ChakhLeSlate700,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Rating 4.0+ Chip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.2.dp,
                        if (isTopRatedOnly) ChakhLeAmberDark else ChakhLeSlate200,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onTopRatedToggle() },
                color = if (isTopRatedOnly) CategoryBiryaniBg else Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating filter",
                        tint = if (isTopRatedOnly) ChakhLeAmberDark else ChakhLeAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RATING 4.0+",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTopRatedOnly) ChakhLeAmberDark else ChakhLeSlate700,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Geometric Balance Live Order Tracker Floating Banner
 */
@Composable
fun ActiveOrderFloatingBanner(
    orderNumber: String,
    statusText: String,
    etaMinutes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(18.dp)),
        color = ChakhLeSlate900
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Amber Scooter Round Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ChakhLeAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = "Delivery in progress",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "ARRIVING IN $etaMinutes MINS",
                        color = ChakhLeAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Track Order",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Address Selector Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSelectionBottomSheet(
    addresses: List<AddressEntity>,
    selectedAddressId: Long,
    onSelectAddress: (AddressEntity) -> Unit,
    onAddNewAddress: (AddressEntity) -> Unit,
    onAutoDetectLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isAddingNew by remember { mutableStateOf(false) }

    var tag by remember { mutableStateOf("Home") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var houseNo by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bengaluru") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ChakhLeSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAddingNew) "Add New Delivery Address" else "Select Delivery Location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeTextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ChakhLeBorder)

            if (!isAddingNew) {
                // Auto Detect GPS Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onAutoDetectLocation()
                            onDismiss()
                        },
                    color = ChakhLeRedContainer
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS",
                            tint = ChakhLeRedPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Use Current GPS Location",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ChakhLeRedPrimary
                            )
                            Text(
                                text = "Auto-detect high accuracy doorstep coordinates",
                                fontSize = 11.sp,
                                color = ChakhLeTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "SAVED ADDRESSES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChakhLeTextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                addresses.forEach { addr ->
                    val isSelected = addr.id == selectedAddressId || (selectedAddressId == 0L && addr.isDefault)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectAddress(addr)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ChakhLeRedLight else ChakhLeSurfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ChakhLeRedPrimary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (addr.tag.lowercase()) {
                                "home" -> Icons.Default.Home
                                "work" -> Icons.Default.Work
                                else -> Icons.Default.LocationOn
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = addr.tag,
                                tint = if (isSelected) ChakhLeRedPrimary else ChakhLeTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = addr.tag,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ChakhLeTextPrimary
                                    )
                                    if (addr.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ChakhLeAmberLight)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("DEFAULT", fontSize = 9.sp, color = ChakhLeAmberDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = "${addr.houseNo}, ${addr.area}, ${addr.city}",
                                    fontSize = 12.sp,
                                    color = ChakhLeTextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ChakhLeRedPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { isAddingNew = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeRedPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Address", fontWeight = FontWeight.Bold)
                }
            } else {
                // Add New Address Form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Home", "Work", "Other").forEach { t ->
                        val selected = tag == t
                        Button(
                            onClick = { tag = t },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) ChakhLeRedPrimary else ChakhLeSurfaceVariant,
                                contentColor = if (selected) Color.White else ChakhLeTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(t, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Recipient Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = houseNo,
                    onValueChange = { houseNo = it },
                    label = { Text("Flat / House No / Building") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Street / Area / Locality") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (houseNo.isNotBlank() && area.isNotBlank()) {
                            val newAddr = AddressEntity(
                                tag = tag,
                                recipientName = name.ifBlank { "Aman Sharma" },
                                phone = phone.ifBlank { "+91 98765 12345" },
                                houseNo = houseNo,
                                area = area,
                                landmark = landmark,
                                city = city,
                                isDefault = false
                            )
                            onAddNewAddress(newAddr)
                            isAddingNew = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary)
                ) {
                    Text("Save & Deliver Here", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * Notifications Dialog / Sheet
 */
@Composable
fun NotificationsDialog(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = ChakhLeRedPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifications & Offers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ChakhLeTextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 8.dp))

            notifications.forEach { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = notif.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ChakhLeTextPrimary
                            )
                            Text(
                                text = notif.time,
                                fontSize = 10.sp,
                                color = ChakhLeTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notif.message,
                            fontSize = 12.sp,
                            color = ChakhLeTextSecondary
                        )
                    }
                }
            }
        }
    }
}
