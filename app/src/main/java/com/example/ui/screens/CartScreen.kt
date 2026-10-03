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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItemEntity
import com.example.data.model.PaymentMethod
import com.example.ui.components.FoodTypeIndicator
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CartScreen(
    viewModel: ChakhLeViewModel,
    onNavigateBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val deliveryInstructions by viewModel.deliveryInstructions.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val currentAddress by viewModel.currentAddressTitle.collectAsState()

    val subtotal = viewModel.calculateSubtotal()
    val deliveryFee = viewModel.calculateDeliveryFee(subtotal)
    val platformFee = 15.0
    val discount = viewModel.calculateDiscount(subtotal)
    val finalTotal = viewModel.calculateFinalTotal()

    var customCouponInput by remember { mutableStateOf("") }
    var couponError by remember { mutableStateOf<String?>(null) }
    var isPlacingOrder by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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

                Column {
                    Text(
                        text = "Your Food Cart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ChakhLeTextPrimary
                    )
                    if (cartItems.isNotEmpty()) {
                        Text(
                            text = "${cartItems.first().restaurantName} • ${cartItems.sumOf { it.quantity }} items",
                            fontSize = 12.sp,
                            color = ChakhLeTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            // Empty Cart State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(ChakhLeRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = ChakhLeRedPrimary,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Your Cart is Empty",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Explore delicious dishes from top local restaurants and add them to your cart!",
                        fontSize = 13.sp,
                        color = ChakhLeTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Browse Hot Dishes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Cart Items and Smart Billing
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Delivery Address Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ChakhLeRedContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = ChakhLeRedPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Delivering to",
                                    fontSize = 11.sp,
                                    color = ChakhLeTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentAddress,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChakhLeTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 2. Selected Items List
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ORDER ITEMS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeTextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            cartItems.forEachIndexed { index, item ->
                                CartItemRow(
                                    item = item,
                                    onIncrease = {
                                        val dish = viewModel.allDishes.value.find { it.id == item.dishId }
                                        if (dish != null) viewModel.addToCart(dish)
                                    },
                                    onDecrease = { viewModel.decreaseCartItem(item.dishId) }
                                )

                                if (index < cartItems.size - 1) {
                                    HorizontalDivider(
                                        color = ChakhLeBorder,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Delivery Instructions Note Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notes,
                                    contentDescription = null,
                                    tint = ChakhLeRedPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delivery Instructions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ChakhLeTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Leave with guard", "Don't ring bell", "Avoid calling").forEach { chip ->
                                    val isSelected = deliveryInstructions.contains(chip)
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                1.dp,
                                                if (isSelected) ChakhLeRedPrimary else ChakhLeBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                val newNotes = if (isSelected) "" else chip
                                                viewModel.setDeliveryInstructions(newNotes)
                                            },
                                        color = if (isSelected) ChakhLeRedLight else ChakhLeSurfaceVariant
                                    ) {
                                        Text(
                                            text = chip,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) ChakhLeRedPrimary else ChakhLeTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = deliveryInstructions,
                                onValueChange = { viewModel.setDeliveryInstructions(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Add specific notes for kitchen or rider...", fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ChakhLeTextPrimary,
                                    unfocusedTextColor = ChakhLeTextPrimary,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = ChakhLeRedPrimary,
                                    unfocusedBorderColor = ChakhLeBorder
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }

                // 4. Promo Coupons Box
                item {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = ChakhLeAmberDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Apply Promo Coupon",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ChakhLeTextPrimary
                                    )
                                }

                                if (appliedCoupon != null) {
                                    TextButton(onClick = { viewModel.removeCoupon() }) {
                                        Text("Remove", color = ChakhLeRedPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (appliedCoupon != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ChakhLeSuccess,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "'${appliedCoupon?.code}' Applied! Saved ₹${discount.toInt()}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = ChakhLeSuccess
                                            )
                                            Text(
                                                text = appliedCoupon?.description ?: "",
                                                fontSize = 11.sp,
                                                color = ChakhLeTextSecondary
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customCouponInput,
                                        onValueChange = {
                                            customCouponInput = it.uppercase()
                                            couponError = null
                                        },
                                        placeholder = { Text("e.g. FIRST20, KHAIBU50", fontSize = 12.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("coupon_input_field"),
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

                                    Button(
                                        onClick = {
                                            if (customCouponInput.isNotBlank()) {
                                                val (success, msg) = viewModel.applyCouponDetailed(customCouponInput, subtotal)
                                                if (success) {
                                                    customCouponInput = ""
                                                    couponError = null
                                                } else {
                                                    couponError = msg
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Apply", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (couponError != null) {
                                    Text(
                                        text = couponError!!,
                                        color = ChakhLeRedPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "AVAILABLE OFFERS & PROMOS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChakhLeTextMuted,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick coupons chips with scarcity badges
                                androidx.compose.foundation.lazy.LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(viewModel.availableCoupons.size) { idx ->
                                        val coupon = viewModel.availableCoupons[idx]
                                        val isExpired = coupon.isExpired

                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    if (isExpired) Color.LightGray else ChakhLeAmberDark,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    val (success, msg) = viewModel.applyCouponDetailed(coupon.code, subtotal)
                                                    if (!success) {
                                                        couponError = msg
                                                    } else {
                                                        couponError = null
                                                    }
                                                },
                                            color = if (isExpired) Color(0xFFF3F4F6) else ChakhLeAmberLight
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = coupon.code,
                                                            color = if (isExpired) Color.Gray else ChakhLeAmberDark,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "(${coupon.discountPercent}% OFF)",
                                                            color = if (isExpired) Color.Gray else ChakhLeSuccess,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    val statusText = when {
                                                        isExpired -> "EXPIRED"
                                                        coupon.maxUses != null -> "Only ${coupon.remainingUses} left"
                                                        coupon.getTimeRemainingString() != null -> coupon.getTimeRemainingString()!!
                                                        else -> "Min ₹${coupon.minOrder.toInt()}"
                                                    }
                                                    Text(
                                                        text = statusText,
                                                        fontSize = 9.sp,
                                                        fontWeight = if (coupon.maxUses != null && !isExpired) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (coupon.maxUses != null && !isExpired) ChakhLeRedPrimary else ChakhLeTextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Payment Method Selection
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PAYMENT METHOD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeTextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            PaymentOptionRow(
                                title = "Google Pay / PhonePe / Paytm",
                                subtitle = "Instant 1-tap UPI payment",
                                icon = Icons.Default.PhoneAndroid,
                                isSelected = selectedPaymentMethod == PaymentMethod.UPI_GPAY || selectedPaymentMethod == PaymentMethod.UPI_PHONEPE || selectedPaymentMethod == PaymentMethod.UPI_PAYTM,
                                onClick = { viewModel.setPaymentMethod(PaymentMethod.UPI_GPAY) }
                            )

                            HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 8.dp))

                            PaymentOptionRow(
                                title = "Cash on Delivery (COD)",
                                subtitle = "Pay with cash or UPI at your doorstep",
                                icon = Icons.Default.LocalAtm,
                                isSelected = selectedPaymentMethod == PaymentMethod.COD,
                                onClick = { viewModel.setPaymentMethod(PaymentMethod.COD) }
                            )

                            HorizontalDivider(color = ChakhLeBorder, modifier = Modifier.padding(vertical = 8.dp))

                            PaymentOptionRow(
                                title = "Credit / Debit Cards",
                                subtitle = "Visa, Mastercard, RuPay & Amex",
                                icon = Icons.Default.CreditCard,
                                isSelected = selectedPaymentMethod == PaymentMethod.CARD,
                                onClick = { viewModel.setPaymentMethod(PaymentMethod.CARD) }
                            )
                        }
                    }
                }

                // 6. Smart Billing Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ChakhLeSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "BILL DETAILS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeTextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            BillRow(label = "Item Subtotal", amount = "₹${subtotal.toInt()}")
                            BillRow(
                                label = "Delivery Partner Fee",
                                amount = if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}",
                                isFree = deliveryFee == 0.0
                            )
                            BillRow(label = "Restaurant Packaging & Platform", amount = "₹${platformFee.toInt()}")

                            if (discount > 0) {
                                BillRow(
                                    label = "Promo Discount (${appliedCoupon?.code})",
                                    amount = "-₹${discount.toInt()}",
                                    isDiscount = true
                                )
                            }

                            HorizontalDivider(
                                color = ChakhLeBorder,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "To Pay",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = ChakhLeTextPrimary
                                    )
                                    Text(
                                        text = "Inclusive of all taxes",
                                        fontSize = 11.sp,
                                        color = ChakhLeTextMuted
                                    )
                                }

                                Text(
                                    text = "₹${finalTotal.toInt()}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = ChakhLeRedPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sticky Checkout Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ChakhLeSurface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChakhLeTextMuted
                        )
                        Text(
                            text = "₹${finalTotal.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ChakhLeTextPrimary
                        )
                        Text(
                            text = "via ${selectedPaymentMethod.title}",
                            fontSize = 11.sp,
                            color = ChakhLeTextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            isPlacingOrder = true
                            scope.launch {
                                delay(1200) // Realistic checkout payment simulation
                                viewModel.placeOrder { orderId ->
                                    isPlacingOrder = false
                                    onOrderPlaced(orderId)
                                }
                            }
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .width(200.dp)
                            .testTag("place_order_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                        enabled = !isPlacingOrder && cartItems.isNotEmpty()
                    ) {
                        if (isPlacingOrder) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirming...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemEntity,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodTypeIndicator(isVeg = item.isVeg, size = 14.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = item.dishName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ChakhLeTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${item.price.toInt()} each",
                    fontSize = 12.sp,
                    color = ChakhLeTextSecondary
                )
            }
        }

        // Stepper Quantity Controller
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ChakhLeRedPrimary, RoundedCornerShape(8.dp))
                .background(ChakhLeSurface)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = ChakhLeRedPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = item.quantity.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ChakhLeRedPrimary,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = ChakhLeRedPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "₹${(item.price * item.quantity).toInt()}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ChakhLeTextPrimary
        )
    }
}

@Composable
fun BillRow(
    label: String,
    amount: String,
    isFree: Boolean = false,
    isDiscount: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isDiscount) ChakhLeSuccess else ChakhLeTextSecondary
        )
        Text(
            text = amount,
            fontSize = 13.sp,
            fontWeight = if (isFree || isDiscount) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isFree || isDiscount -> ChakhLeSuccess
                else -> ChakhLeTextPrimary
            }
        )
    }
}

@Composable
fun PaymentOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) ChakhLeRedContainer else ChakhLeSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) ChakhLeRedPrimary else ChakhLeTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ChakhLeTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = ChakhLeTextMuted
                )
            }
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = ChakhLeRedPrimary)
        )
    }
}
