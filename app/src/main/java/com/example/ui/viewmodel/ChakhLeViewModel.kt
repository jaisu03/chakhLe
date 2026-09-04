package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseManager
import com.example.data.local.ChakhLeDatabase
import com.example.data.model.AddressEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.ChatMessage
import com.example.data.model.Dish
import com.example.data.model.FaqItem
import com.example.data.model.FoodCategory
import com.example.data.model.FoodCategoryType
import com.example.data.model.NotificationItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PromoCoupon
import com.example.data.model.Restaurant
import com.example.data.repository.FoodRepository
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChakhLeViewModel(application: Application) : AndroidViewModel(application) {

    val firebaseManager = FirebaseManager.getInstance(application)
    private val database = ChakhLeDatabase.getInstance(application)
    private val repository = FoodRepository(
        database.cartDao(),
        database.orderDao(),
        database.addressDao(),
        firebaseManager
    )

    // Splash / Auth State
    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished: StateFlow<Boolean> = _isSplashFinished.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _otpResendSeconds = MutableStateFlow(30)
    val otpResendSeconds: StateFlow<Int> = _otpResendSeconds.asStateFlow()
    private var timerJob: Job? = null

    // User Profile State
    private val _userName = MutableStateFlow("Suraj Jaiswar")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("surajjaiswar00000@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    // Location
    private val _currentAddressTitle = MutableStateFlow("Koramangala 4th Block, Bengaluru")
    val currentAddressTitle: StateFlow<String> = _currentAddressTitle.asStateFlow()

    private val _isGpsDetecting = MutableStateFlow(false)
    val isGpsDetecting: StateFlow<Boolean> = _isGpsDetecting.asStateFlow()

    // Catalogs & Dynamic Lists
    val categories: List<FoodCategory> = repository.getCategories()
    private val _restaurants = MutableStateFlow(repository.getRestaurants())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _allDishes = MutableStateFlow(repository.getAllDishes())
    val allDishes: StateFlow<List<Dish>> = _allDishes.asStateFlow()

    val availableCoupons: List<PromoCoupon> = repository.getPromoCoupons()
    val supportFaqs: List<FaqItem> = repository.getFaqs()

    // Filters & Search
    private val _selectedCategory = MutableStateFlow(FoodCategoryType.ALL)
    val selectedCategory: StateFlow<FoodCategoryType> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isPureVegOnly = MutableStateFlow(false)
    val isPureVegOnly: StateFlow<Boolean> = _isPureVegOnly.asStateFlow()

    private val _isTopRatedOnly = MutableStateFlow(false)
    val isTopRatedOnly: StateFlow<Boolean> = _isTopRatedOnly.asStateFlow()

    // Filtered Dishes Feed
    val filteredDishes: StateFlow<List<Dish>> = combine(
        _allDishes,
        _selectedCategory,
        _searchQuery,
        _isPureVegOnly,
        _isTopRatedOnly
    ) { dishes, category, query, pureVeg, topRated ->
        dishes.filter { dish ->
            val matchesCategory = (category == FoodCategoryType.ALL) || (dish.category == category)
            val matchesSearch = query.isBlank() ||
                dish.name.contains(query, ignoreCase = true) ||
                dish.restaurantName.contains(query, ignoreCase = true) ||
                dish.description.contains(query, ignoreCase = true) ||
                dish.category.displayName.contains(query, ignoreCase = true)
            val matchesVeg = !pureVeg || dish.isVeg
            val matchesRating = !topRated || dish.rating >= 4.0

            matchesCategory && matchesSearch && matchesVeg && matchesRating
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getAllDishes())

    // Room Persistent States
    val cartItems: StateFlow<List<CartItemEntity>> = repository.allCartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedAddresses: StateFlow<List<AddressEntity>> = repository.allAddresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestActiveOrder: StateFlow<OrderEntity?> = repository.latestActiveOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Tracked Order ID
    private val _selectedTrackingOrderId = MutableStateFlow<String?>(null)
    val selectedTrackingOrderId: StateFlow<String?> = _selectedTrackingOrderId.asStateFlow()

    // Cart Details & Calculations
    private val _appliedCoupon = MutableStateFlow<PromoCoupon?>(null)
    val appliedCoupon: StateFlow<PromoCoupon?> = _appliedCoupon.asStateFlow()

    private val _deliveryInstructions = MutableStateFlow("")
    val deliveryInstructions: StateFlow<String> = _deliveryInstructions.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.UPI_GPAY)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow(repository.getInitialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Support Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("1", false, "Namaste! Welcome to ChakhLe Support. How can we help you today?", "Just now")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfEmpty()
            if (firebaseManager.isAvailable()) {
                // Auto seed and configure Firebase collections if empty
                firebaseManager.autoSetupBackend(
                    defaultRestaurants = repository.getRestaurants(),
                    defaultDishes = repository.getAllDishes()
                )

                // Listen to live Cloud Firestore orders and sync to local database
                launch {
                    firebaseManager.observeOrdersRealtime().collect { firestoreOrders ->
                        if (firestoreOrders.isNotEmpty()) {
                            for (order in firestoreOrders) {
                                database.orderDao().insertOrder(order)
                            }
                        }
                    }
                }

                // Listen to live Cloud Firestore restaurants
                launch {
                    firebaseManager.observeRestaurantsRealtime().collect { firestoreRestaurants ->
                        if (firestoreRestaurants.isNotEmpty()) {
                            val current = repository.getRestaurants()
                            val combined = (firestoreRestaurants + current).distinctBy { it.id }
                            _restaurants.value = combined
                        }
                    }
                }

                // Listen to live Cloud Firestore dishes
                launch {
                    firebaseManager.observeDishesRealtime().collect { firestoreDishes ->
                        if (firestoreDishes.isNotEmpty()) {
                            val current = repository.getAllDishes()
                            val combined = (firestoreDishes + current).distinctBy { it.id }
                            _allDishes.value = combined
                        }
                    }
                }
            }
        }
    }

    fun finishSplash() {
        _isSplashFinished.value = true
    }

    fun setPhoneNumber(number: String) {
        _phoneNumber.value = number
        _authErrorMessage.value = null
    }

    fun setOtpCode(code: String) {
        _otpCode.value = code
        _authErrorMessage.value = null
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun resetAuthState() {
        _isOtpSent.value = false
        _otpCode.value = ""
        _authErrorMessage.value = null
        _authLoading.value = false
        _verificationId.value = null
        timerJob?.cancel()
    }

    fun sendOtp(activity: Activity? = null, isResend: Boolean = false) {
        val rawDigits = _phoneNumber.value.trim().filter { it.isDigit() }
        if (rawDigits.length < 10) {
            _authErrorMessage.value = "Please enter a valid 10-digit mobile number"
            return
        }

        val formattedPhone = if (rawDigits.startsWith("91") && rawDigits.length == 12) {
            "+$rawDigits"
        } else {
            "+91${rawDigits.takeLast(10)}"
        }

        _authLoading.value = true
        _authErrorMessage.value = null

        if (activity != null && firebaseManager.isAvailable()) {
            firebaseManager.sendPhoneVerificationCode(
                activity = activity,
                formattedPhoneNumber = formattedPhone,
                forceResendingToken = if (isResend) resendToken else null,
                onVerificationCompleted = { credential ->
                    _authLoading.value = false
                    _isOtpSent.value = true
                    if (credential.smsCode != null) {
                        _otpCode.value = credential.smsCode!!
                    }
                    viewModelScope.launch {
                        val result = firebaseManager.signInWithPhoneCredential(credential)
                        if (result.isSuccess) {
                            _isLoggedIn.value = true
                        }
                    }
                },
                onVerificationFailed = { error ->
                    _authLoading.value = false
                    Log.w("ChakhLeViewModel", "Firebase Phone Verification Failed: ${error.message}")
                    val rawMsg = error.message ?: ""
                    val msg = if (rawMsg.contains("This operation is not allowed") || rawMsg.contains("sign-in provider is disabled") || rawMsg.contains("ERROR_OPERATION_NOT_ALLOWED")) {
                        "Phone Auth or SMS Region is disabled in Firebase Console. Go to Firebase Console -> Authentication -> Sign-in method -> Enable 'Phone'."
                    } else if (rawMsg.contains("SMS unable to be sent until this region enabled")) {
                        "SMS Region (+91) not enabled. In Firebase Console -> Authentication -> Settings -> Enable India (+91)."
                    } else if (rawMsg.contains("BILLING_NOT_ENABLED") || rawMsg.contains("CONFIGURATION_NOT_FOUND") || rawMsg.contains("quota", ignoreCase = true) || rawMsg.contains("SMS quota", ignoreCase = true)) {
                        "Firebase Free SMS Quota reached for today or Carrier SMS is throttled. You can proceed with test verification code or Guest Mode."
                    } else if (rawMsg.contains("TOO_LONG") || rawMsg.contains("TOO_SHORT") || rawMsg.contains("invalid phone number", ignoreCase = true)) {
                        "Please enter a valid 10-digit mobile number."
                    } else {
                        error.localizedMessage ?: "SMS verification could not be completed."
                    }
                    _authErrorMessage.value = msg
                    // Allow moving to OTP screen with seamless verification so the user is never stuck
                    _isOtpSent.value = true
                    _otpCode.value = ""
                    startOtpTimer()
                },
                onCodeSent = { verificationId, token ->
                    _authLoading.value = false
                    _verificationId.value = verificationId
                    resendToken = token
                    _isOtpSent.value = true
                    _otpCode.value = ""
                    _authErrorMessage.value = null
                    startOtpTimer()
                }
            )
        } else {
            // Fallback when activity is not bound
            _authLoading.value = false
            _isOtpSent.value = true
            _otpCode.value = ""
            startOtpTimer()
        }
    }

    private fun startOtpTimer() {
        timerJob?.cancel()
        _otpResendSeconds.value = 30
        timerJob = viewModelScope.launch {
            while (_otpResendSeconds.value > 0) {
                delay(1000)
                _otpResendSeconds.value -= 1
            }
        }
    }

    fun verifyOtpAndLogin() {
        val code = _otpCode.value.trim()
        if (code.length < 4) {
            _authErrorMessage.value = "Please enter the 6-digit OTP code received via SMS"
            return
        }

        _authLoading.value = true
        _authErrorMessage.value = null

        val verId = _verificationId.value
        if (verId != null && firebaseManager.isAvailable()) {
            viewModelScope.launch {
                val result = firebaseManager.verifyAndSignInWithPhoneCode(verId, code)
                _authLoading.value = false
                if (result.isSuccess) {
                    _isLoggedIn.value = true
                } else {
                    val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Invalid verification code. Please check and try again."
                    _authErrorMessage.value = errorMsg
                }
            }
        } else {
            _authLoading.value = false
            _isLoggedIn.value = true
            viewModelScope.launch {
                if (firebaseManager.isAvailable()) {
                    firebaseManager.signInAnonymously()
                }
            }
        }
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        _userName.value = name.ifBlank { _userName.value }
        _userEmail.value = email.ifBlank { _userEmail.value }
        _phoneNumber.value = phone.ifBlank { _phoneNumber.value }
    }

    fun continueAsGuest() {
        if (_phoneNumber.value.isBlank()) {
            _phoneNumber.value = "+91 98765 12345"
        }
        _isLoggedIn.value = true
        viewModelScope.launch {
            if (firebaseManager.isAvailable()) {
                firebaseManager.signInAnonymously()
            }
        }
    }

    fun skipLoginForDemo() {
        continueAsGuest()
    }

    // Dynamic Kitchen & Menu Management (Admin / Partner Role)
    fun registerKitchen(
        name: String,
        cuisine: String,
        deliveryTime: Int,
        isPureVeg: Boolean,
        address: String,
        priceForTwo: Int,
        signatureDishName: String,
        dishPrice: Double,
        dishCategory: FoodCategoryType,
        dishDescription: String
    ) {
        val newRestId = "rest_${System.currentTimeMillis()}"
        val newRestaurant = Restaurant(
            id = newRestId,
            name = name,
            cuisine = cuisine,
            rating = 4.8,
            reviewCount = "New",
            deliveryTimeMinutes = deliveryTime,
            priceForTwo = priceForTwo,
            offerText = "🎉 Welcome Offer: Flat 20% OFF",
            distanceKm = 1.2,
            isPureVeg = isPureVeg,
            address = address,
            featuredTag = "Newly Added"
        )

        viewModelScope.launch {
            repository.addRestaurant(newRestaurant)
            _restaurants.value = repository.getRestaurants()

            if (signatureDishName.isNotBlank()) {
                val newDish = Dish(
                    id = "dish_${System.currentTimeMillis()}",
                    restaurantId = newRestId,
                    restaurantName = name,
                    name = signatureDishName,
                    description = dishDescription.ifBlank { "Signature special freshly prepared with handpicked ingredients." },
                    price = dishPrice,
                    originalPrice = dishPrice + 50.0,
                    isVeg = isPureVeg,
                    rating = 4.9,
                    ratingCount = 12,
                    category = dishCategory,
                    prepTimeMinutes = deliveryTime,
                    isBestseller = true,
                    spicyLevel = 2,
                    portionSize = "Serves 1-2"
                )
                repository.addDish(newDish)
                _allDishes.value = repository.getAllDishes()
            }

            // Notify
            val notif = NotificationItem(
                id = "notif_${System.currentTimeMillis()}",
                title = "🏪 New Kitchen Registered!",
                message = "$name is now LIVE on ChakhLe. Fresh dishes are available for delivery.",
                time = "Just now"
            )
            _notifications.value = listOf(notif) + _notifications.value
        }
    }

    fun addDishToKitchen(
        restaurantId: String,
        restaurantName: String,
        dishName: String,
        price: Double,
        category: FoodCategoryType,
        isVeg: Boolean,
        description: String
    ) {
        val newDish = Dish(
            id = "dish_${System.currentTimeMillis()}",
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            name = dishName,
            description = description.ifBlank { "Chef's gourmet creation with authentic spices." },
            price = price,
            originalPrice = price + 40.0,
            isVeg = isVeg,
            rating = 4.8,
            ratingCount = 8,
            category = category,
            prepTimeMinutes = 20,
            isBestseller = false,
            spicyLevel = 2,
            portionSize = "Regular Portion"
        )
        viewModelScope.launch {
            repository.addDish(newDish)
            _allDishes.value = repository.getAllDishes()
        }
    }

    fun autoDetectGpsLocation() {
        viewModelScope.launch {
            _isGpsDetecting.value = true
            delay(1200) // Realistic GPS fix delay
            val detectedAddresses = listOf(
                "Koramangala 4th Block, Bengaluru",
                "Indiranagar 100ft Road, Bengaluru",
                "HSR Layout Sector 3, Bengaluru",
                "Bandra West, Mumbai",
                "Connaught Place, New Delhi"
            )
            val randomAddress = detectedAddresses.random()
            _currentAddressTitle.value = randomAddress
            _isGpsDetecting.value = false
        }
    }

    fun selectAddress(address: AddressEntity) {
        _currentAddressTitle.value = "${address.houseNo}, ${address.area}, ${address.city}"
        viewModelScope.launch {
            repository.setDefaultAddress(address.id)
        }
    }

    fun addNewAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.addAddress(address)
            _currentAddressTitle.value = "${address.houseNo}, ${address.area}, ${address.city}"
        }
    }

    // Category & Filters
    fun selectCategory(type: FoodCategoryType) {
        _selectedCategory.value = if (_selectedCategory.value == type) FoodCategoryType.ALL else type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun togglePureVeg(enabled: Boolean) {
        _isPureVegOnly.value = enabled
    }

    fun toggleTopRated() {
        _isTopRatedOnly.value = !_isTopRatedOnly.value
    }

    // Cart Operations
    fun addToCart(dish: Dish) {
        viewModelScope.launch {
            repository.addToCart(dish)
        }
    }

    fun decreaseCartItem(dishId: String) {
        viewModelScope.launch {
            repository.decreaseCartItem(dishId)
        }
    }

    fun removeCartItem(dishId: String) {
        viewModelScope.launch {
            repository.removeCartItem(dishId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
            _appliedCoupon.value = null
        }
    }

    fun applyCoupon(code: String): Boolean {
        val coupon = availableCoupons.find { it.code.equals(code, ignoreCase = true) }
        return if (coupon != null) {
            _appliedCoupon.value = coupon
            true
        } else {
            false
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun setDeliveryInstructions(notes: String) {
        _deliveryInstructions.value = notes
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    // Calculate Cart Totals
    fun calculateSubtotal(): Double {
        return cartItems.value.sumOf { it.price * it.quantity }
    }

    fun calculateDeliveryFee(subtotal: Double): Double {
        if (_appliedCoupon.value?.code == "FREEDEL" || subtotal >= 499.0 || subtotal == 0.0) {
            return 0.0
        }
        return 35.0
    }

    fun calculateDiscount(subtotal: Double): Double {
        val coupon = _appliedCoupon.value ?: return 0.0
        if (subtotal < coupon.minOrder) return 0.0
        val discount = (subtotal * coupon.discountPercent) / 100.0
        return discount.coerceAtMost(coupon.maxDiscount)
    }

    fun calculateFinalTotal(): Double {
        val subtotal = calculateSubtotal()
        if (subtotal == 0.0) return 0.0
        val delivery = calculateDeliveryFee(subtotal)
        val platformFee = 15.0
        val discount = calculateDiscount(subtotal)
        return (subtotal + delivery + platformFee - discount).coerceAtLeast(0.0)
    }

    // Checkout & Place Order
    fun placeOrder(onOrderPlaced: (String) -> Unit) {
        val items = cartItems.value
        if (items.isEmpty()) return

        val subtotal = calculateSubtotal()
        val deliveryFee = calculateDeliveryFee(subtotal)
        val platformFee = 15.0
        val discount = calculateDiscount(subtotal)
        val finalTotal = (subtotal + deliveryFee + platformFee - discount).coerceAtLeast(0.0)

        val firstItem = items.first()
        val summary = items.joinToString(", ") { "${it.quantity}x ${it.dishName}" }
        val generatedOrderId = "CK-${(1000..9999).random()}"

        val newOrder = OrderEntity(
            orderId = generatedOrderId,
            timestamp = System.currentTimeMillis(),
            restaurantId = firstItem.restaurantId,
            restaurantName = firstItem.restaurantName,
            customerName = _userName.value,
            customerPhone = _phoneNumber.value.ifBlank { "+91 98765 12345" },
            deliveryAddress = _currentAddressTitle.value,
            itemsSummary = summary,
            itemsCount = items.sumOf { it.quantity },
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            platformFee = platformFee,
            discount = discount,
            totalAmount = finalTotal,
            paymentMethod = _selectedPaymentMethod.value.title,
            paymentStatus = "SUCCESS",
            status = OrderStatus.ACCEPTED,
            riderName = "Rajesh Kumar",
            riderPhone = "+91 98765 43210",
            riderVehicle = "Honda Activa • MH 12 AB 4590",
            riderRating = 4.9,
            estimatedArrivalMinutes = 18,
            deliveryNotes = _deliveryInstructions.value,
            couponApplied = _appliedCoupon.value?.code ?: ""
        )

        viewModelScope.launch {
            repository.placeOrder(newOrder)
            _selectedTrackingOrderId.value = generatedOrderId

            // Add notification
            val notif = NotificationItem(
                id = "notif_${System.currentTimeMillis()}",
                title = "🛵 Order Placed Successfully!",
                message = "Your order #$generatedOrderId at ${newOrder.restaurantName} is confirmed. Arriving in ~18 mins.",
                time = "Just now",
                orderId = generatedOrderId
            )
            _notifications.value = listOf(notif) + _notifications.value

            onOrderPlaced(generatedOrderId)
        }
    }

    fun selectOrderForTracking(orderId: String) {
        _selectedTrackingOrderId.value = orderId
    }

    // Step Order to Next Status (For Interactive Live Demo & Kitchen Portal)
    fun advanceOrderStatus(orderId: String, currentStatus: OrderStatus) {
        val nextStatus = when (currentStatus) {
            OrderStatus.PLACED -> OrderStatus.ACCEPTED
            OrderStatus.ACCEPTED -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.OUT_FOR_DELIVERY
            OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> OrderStatus.DELIVERED
            OrderStatus.CANCELLED -> OrderStatus.CANCELLED
        }
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, nextStatus)
        }
    }

    fun updateOrderStatusDirectly(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    // Reorder Past Order
    fun reorder(order: OrderEntity, onReordered: () -> Unit) {
        viewModelScope.launch {
            // Add dishes matching the restaurant to cart
            val matchedDishes = allDishes.value.filter { it.restaurantId == order.restaurantId }
            val dishToAdd = matchedDishes.firstOrNull() ?: allDishes.value.firstOrNull()
            if (dishToAdd != null) {
                repository.addToCart(dishToAdd)
            }
            onReordered()
        }
    }

    // Support Chat Simulation
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val userMsg = ChatMessage(id = "usr_${System.currentTimeMillis()}", isUser = true, message = userText, time = timeStr)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            delay(800)
            val botReply = when {
                userText.contains("order", ignoreCase = true) || userText.contains("status", ignoreCase = true) ->
                    "Your latest order is being prepared with utmost care and fresh ingredients. You can track your rider live in the Tracking tab!"
                userText.contains("refund", ignoreCase = true) || userText.contains("cancel", ignoreCase = true) ->
                    "If you faced an issue with your meal or delivery, our instant refund system credits the amount back to your UPI/Card account within 10 minutes."
                userText.contains("coupon", ignoreCase = true) || userText.contains("discount", ignoreCase = true) ->
                    "Use coupon code CHAKHLE50 for 50% discount on your order!"
                else ->
                    "Thank you for contacting ChakhLe support! A food delivery specialist is reviewing your request. We are here 24/7."
            }
            val replyMsg = ChatMessage(
                id = "bot_${System.currentTimeMillis()}",
                isUser = false,
                message = botReply,
                time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            )
            _chatMessages.value = _chatMessages.value + replyMsg
        }
    }
}
