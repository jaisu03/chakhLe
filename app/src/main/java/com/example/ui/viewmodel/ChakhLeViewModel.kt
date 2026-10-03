package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.FirebaseAuthResult
import com.example.data.auth.FirebaseAuthService
import com.example.data.auth.OtpResult
import com.example.data.auth.RealOtpManager
import com.example.data.auth.SmsGatewayType
import com.example.data.auth.VerifyResult
import com.example.data.firestore.FirestoreOrderService
import com.example.data.firestore.FirestoreOrderUpdate
import com.example.data.firestore.FirestoreSyncState
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
import com.example.data.model.UserRole
import com.example.data.repository.FoodRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChakhLeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ChakhLeDatabase.getInstance(application)
    private val repository = FoodRepository(
        database.cartDao(),
        database.orderDao(),
        database.addressDao(),
        application
    )

    // Splash / Auth State
    val realOtpManager = RealOtpManager.getInstance(application)
    val firebaseAuthService = FirebaseAuthService.getInstance()
    val firestoreOrderService = FirestoreOrderService.getInstance()

    // Real-Time Firestore Order State
    private val _firestoreOrderUpdate = MutableStateFlow<FirestoreOrderUpdate?>(null)
    val firestoreOrderUpdate: StateFlow<FirestoreOrderUpdate?> = _firestoreOrderUpdate.asStateFlow()

    private var activeFirestoreListenerJob: Job? = null
    private var liveSimulationJob: Job? = null

    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished: StateFlow<Boolean> = _isSplashFinished.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _emailInput = MutableStateFlow("")
    val emailInput: StateFlow<String> = _emailInput.asStateFlow()

    private val _emailPassword = MutableStateFlow("")
    val emailPassword: StateFlow<String> = _emailPassword.asStateFlow()

    private val _isEmailAuthMode = MutableStateFlow(false)
    val isEmailAuthMode: StateFlow<Boolean> = _isEmailAuthMode.asStateFlow()

    private val _firebaseUid = MutableStateFlow<String?>(firebaseAuthService.getUserId())
    val firebaseUid: StateFlow<String?> = _firebaseUid.asStateFlow()

    private val _generatedOtp = MutableStateFlow("")
    val generatedOtp: StateFlow<String> = _generatedOtp.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _otpSuccessMessage = MutableStateFlow<String?>(null)
    val otpSuccessMessage: StateFlow<String?> = _otpSuccessMessage.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    val otpResendSeconds: StateFlow<Int> = realOtpManager.resendSeconds
    val otpValiditySeconds: StateFlow<Int> = realOtpManager.validitySeconds
    val attemptsRemaining: StateFlow<Int> = realOtpManager.attemptsRemaining
    val lastDispatchedGateway: StateFlow<String> = realOtpManager.lastDispatchedGateway
    val carrierSmsNotice: StateFlow<String?> = realOtpManager.carrierSmsNotice

    private val _selectedGateway = MutableStateFlow(realOtpManager.getSelectedGateway())
    val selectedGateway: StateFlow<SmsGatewayType> = _selectedGateway.asStateFlow()

    // User Profile State & Role-Based Access Control (RBAC)
    private val _userRole = MutableStateFlow(UserRole.CUSTOMER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _authIdentifiedMessage = MutableStateFlow<String?>(null)
    val authIdentifiedMessage: StateFlow<String?> = _authIdentifiedMessage.asStateFlow()

    private val _currentKitchenId = MutableStateFlow<String?>("rest_1")
    val currentKitchenId: StateFlow<String?> = _currentKitchenId.asStateFlow()

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

    val currentKitchen: StateFlow<Restaurant?> = combine(_restaurants, _currentKitchenId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _allDishes = MutableStateFlow(repository.getAllDishes())
    val allDishes: StateFlow<List<Dish>> = _allDishes.asStateFlow()

    private val _promoCoupons = MutableStateFlow(repository.getPromoCoupons())
    val promoCouponsFlow: StateFlow<List<PromoCoupon>> = _promoCoupons.asStateFlow()
    val availableCoupons: List<PromoCoupon> get() = _promoCoupons.value
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
        }
    }

    fun finishSplash() {
        _isSplashFinished.value = true
    }

    fun setPhoneNumber(number: String) {
        val digits = number.filter { it.isDigit() }
        val cleaned = if (digits.startsWith("91") && digits.length > 10) {
            digits.drop(2).take(10)
        } else if (digits.startsWith("0") && digits.length > 10) {
            digits.drop(1).take(10)
        } else {
            digits.take(10)
        }
        _phoneNumber.value = cleaned
        _authErrorMessage.value = null
    }

    fun setOtpCode(code: String) {
        _otpCode.value = code
        _authErrorMessage.value = null
    }

    fun setOtpSuccessMessage(message: String?) {
        _otpSuccessMessage.value = message
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun resetAuthState() {
        _isOtpSent.value = false
        _otpCode.value = ""
        _authErrorMessage.value = null
        _otpSuccessMessage.value = null
        _authLoading.value = false
        _verificationId.value = null
    }

    fun setSmsGateway(gateway: SmsGatewayType) {
        realOtpManager.setSelectedGateway(gateway)
        _selectedGateway.value = gateway
    }

    fun saveGatewayApiKey(apiKey: String) {
        realOtpManager.setGatewayApiKey(apiKey)
    }

    fun getGatewayApiKey(): String = realOtpManager.getGatewayApiKey()

    fun saveTwilioConfig(sid: String, token: String, fromNumber: String) {
        realOtpManager.setTwilioDetails(sid, token, fromNumber)
    }

    fun getTwilioDetails(): Triple<String, String, String> = realOtpManager.getTwilioDetails()

    fun setEmailInput(email: String) {
        _emailInput.value = email
    }

    fun setEmailPassword(password: String) {
        _emailPassword.value = password
    }

    fun setEmailAuthMode(isEmail: Boolean) {
        _isEmailAuthMode.value = isEmail
        _authErrorMessage.value = null
    }

    fun sendOtp(activity: Activity? = null, isResend: Boolean = false, onOtpGenerated: ((String) -> Unit)? = null) {
        if (realOtpManager.isEmailSession && isResend) {
            sendEmailOtp(isResend = true)
            return
        }

        val rawDigits = _phoneNumber.value.trim().filter { it.isDigit() }
        val tenDigits = if (rawDigits.startsWith("91") && rawDigits.length == 12) {
            rawDigits.drop(2)
        } else {
            rawDigits.takeLast(10)
        }
        if (tenDigits.length < 10) {
            _authErrorMessage.value = "Please enter a valid 10-digit mobile number"
            return
        }

        // Keep clean 10 digits in _phoneNumber so it remains editable, deletable, and valid
        _phoneNumber.value = tenDigits
        val formattedPhone = "+91$tenDigits"

        _authLoading.value = true
        _authErrorMessage.value = null
        _otpSuccessMessage.value = null
        _otpCode.value = "" // Clear input so user has to enter the real received OTP

        viewModelScope.launch {
            val result = realOtpManager.generateAndSendOtp(
                phoneNumber = formattedPhone,
                activity = activity,
                onFirebaseCodeSent = { vId ->
                    _verificationId.value = vId
                },
                onFirebaseAutoVerified = { fbUser ->
                    _isLoggedIn.value = true
                    _userRole.value = UserRole.CUSTOMER
                    _phoneNumber.value = formattedPhone
                    _firebaseUid.value = fbUser.uid
                    _userName.value = fbUser.displayName ?: "Valued Foodie"
                    _isOtpSent.value = false
                    _authLoading.value = false
                }
            )
            _authLoading.value = false
            when (result) {
                is OtpResult.Sent -> {
                    _isOtpSent.value = true
                    _otpSuccessMessage.value = result.message
                    val activeOtp = realOtpManager.getCurrentOtp()
                    _generatedOtp.value = activeOtp
                    _authErrorMessage.value = null
                    onOtpGenerated?.invoke(activeOtp)
                }
                is OtpResult.Error -> {
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun sendEmailOtp(isResend: Boolean = false) {
        val cleanEmail = _emailInput.value.trim().lowercase()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _authErrorMessage.value = "Please enter a valid email address (e.g. name@example.com)"
            return
        }

        _authLoading.value = true
        _authErrorMessage.value = null
        _otpSuccessMessage.value = null
        _otpCode.value = ""

        viewModelScope.launch {
            val result = realOtpManager.generateAndSendEmailOtp(cleanEmail)
            _authLoading.value = false
            when (result) {
                is OtpResult.Sent -> {
                    _isOtpSent.value = true
                    _otpSuccessMessage.value = result.message
                    val activeOtp = realOtpManager.getCurrentOtp()
                    _generatedOtp.value = activeOtp
                    _authErrorMessage.value = null
                }
                is OtpResult.Error -> {
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun registerWithFirebaseEmailPassword(name: String = "") {
        val email = _emailInput.value.trim().lowercase()
        val pass = _emailPassword.value
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authErrorMessage.value = "Please enter a valid email address"
            return
        }
        if (pass.length < 6) {
            _authErrorMessage.value = "Password must be at least 6 characters"
            return
        }

        _authLoading.value = true
        _authErrorMessage.value = null
        viewModelScope.launch {
            when (val result = firebaseAuthService.signUpWithEmail(email, pass)) {
                is FirebaseAuthResult.Success -> {
                    _authLoading.value = false
                    _userEmail.value = email
                    _firebaseUid.value = result.data.uid
                    if (name.isNotBlank()) _userName.value = name
                    _isLoggedIn.value = true
                    _userRole.value = UserRole.CUSTOMER
                    _authErrorMessage.value = null
                    _authIdentifiedMessage.value = "Welcome! Account created via Firebase (${result.data.email})"
                }
                is FirebaseAuthResult.Error -> {
                    _authLoading.value = false
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun signInWithFirebaseEmailPassword() {
        val email = _emailInput.value.trim().lowercase()
        val pass = _emailPassword.value
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authErrorMessage.value = "Please enter a valid email address"
            return
        }
        if (pass.isBlank()) {
            _authErrorMessage.value = "Please enter your password"
            return
        }

        _authLoading.value = true
        _authErrorMessage.value = null
        viewModelScope.launch {
            when (val result = firebaseAuthService.signInWithEmail(email, pass)) {
                is FirebaseAuthResult.Success -> {
                    _authLoading.value = false
                    _userEmail.value = email
                    _firebaseUid.value = result.data.uid
                    _isLoggedIn.value = true
                    _userRole.value = UserRole.CUSTOMER
                    _authErrorMessage.value = null
                    _authIdentifiedMessage.value = "Welcome back! Signed in via Firebase (${result.data.email})"
                }
                is FirebaseAuthResult.Error -> {
                    _authLoading.value = false
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun sendFirebasePasswordReset() {
        val email = _emailInput.value.trim().lowercase()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authErrorMessage.value = "Please enter your email to receive password reset link"
            return
        }
        _authLoading.value = true
        _authErrorMessage.value = null
        viewModelScope.launch {
            when (val result = firebaseAuthService.sendPasswordReset(email)) {
                is FirebaseAuthResult.Success -> {
                    _authLoading.value = false
                    _otpSuccessMessage.value = "Password reset email sent to $email. Please check your inbox."
                }
                is FirebaseAuthResult.Error -> {
                    _authLoading.value = false
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun verifyOtpAndLogin() {
        val code = _otpCode.value.trim()
        if (code.length < 6) {
            _authErrorMessage.value = "Please enter the complete 6-digit OTP code"
            return
        }

        _authLoading.value = true
        _authErrorMessage.value = null

        val vId = _verificationId.value
        if (!vId.isNullOrBlank() && !realOtpManager.isEmailSession) {
            // Verify via Firebase Phone Auth with fallback to local OTP
            viewModelScope.launch {
                when (val fbResult = firebaseAuthService.verifyPhoneOtpCode(vId, code)) {
                    is FirebaseAuthResult.Success -> {
                        _authLoading.value = false
                        _isLoggedIn.value = true
                        _userRole.value = UserRole.CUSTOMER
                        _firebaseUid.value = fbResult.data.uid
                        _authErrorMessage.value = null
                        _otpSuccessMessage.value = null
                        _verificationId.value = null
                        return@launch
                    }
                    is FirebaseAuthResult.Error -> {
                        // Check if local OTP matches as safety fallback
                        when (val localResult = realOtpManager.verifyOtp(code)) {
                            is VerifyResult.Success -> {
                                _authLoading.value = false
                                _isLoggedIn.value = true
                                _userRole.value = UserRole.CUSTOMER
                                _authErrorMessage.value = null
                                _otpSuccessMessage.value = null
                                _verificationId.value = null
                            }
                            else -> {
                                _authLoading.value = false
                                _authErrorMessage.value = fbResult.message
                            }
                        }
                    }
                }
            }
            return
        }

        when (val result = realOtpManager.verifyOtp(code)) {
            is VerifyResult.Success -> {
                _authLoading.value = false
                _isLoggedIn.value = true
                _userRole.value = UserRole.CUSTOMER
                _authErrorMessage.value = null
                _otpSuccessMessage.value = null

                if (realOtpManager.isEmailSession) {
                    val verifiedEmail = realOtpManager.getTargetRecipient() ?: _emailInput.value.trim()
                    if (verifiedEmail.isNotBlank()) {
                        _userEmail.value = verifiedEmail
                    }
                }

                // Sync session with Firebase Auth in background
                viewModelScope.launch {
                    val email = _userEmail.value.ifBlank { realOtpManager.getTargetRecipient() ?: "" }
                    if (email.contains("@")) {
                        val fbUser = firebaseAuthService.ensureSessionForVerifiedEmail(email)
                        _firebaseUid.value = fbUser?.uid
                    }
                }
            }
            is VerifyResult.InvalidCode -> {
                _authLoading.value = false
                _authErrorMessage.value = result.message
            }
            is VerifyResult.Expired -> {
                _authLoading.value = false
                _authErrorMessage.value = result.message
            }
            is VerifyResult.MaxAttemptsReached -> {
                _authLoading.value = false
                _authErrorMessage.value = result.message
            }
            is VerifyResult.NoSession -> {
                _authLoading.value = false
                _authErrorMessage.value = result.message
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
        _userRole.value = UserRole.CUSTOMER
        _isLoggedIn.value = true
    }

    fun skipLoginForDemo() {
        continueAsGuest()
    }

    // Role Identification & Access Control
    fun loginWithStaffCredentials(userIdInput: String, passwordInput: String): Boolean {
        val cleanUser = userIdInput.trim()
        val cleanPass = passwordInput.trim()

        if (cleanUser.isBlank() || cleanPass.isBlank()) {
            _authErrorMessage.value = "Please enter both User ID and Password."
            return false
        }

        // 1. Super Admin Check
        val isAdminUser = (cleanUser.equals("admin", ignoreCase = true) ||
                cleanUser.equals("superadmin", ignoreCase = true) ||
                cleanUser.equals("admin@khaibu.com", ignoreCase = true) ||
                cleanUser.equals("admin@chakhle.com", ignoreCase = true))
        val isAdminPass = (cleanPass == "admin" || cleanPass == "admin123" || cleanPass == "Admin@123" || cleanPass == "Khaibu@2026" || cleanPass == "ChakhLe@2026")

        if (isAdminUser && isAdminPass) {
            _userRole.value = UserRole.ADMIN
            _isLoggedIn.value = true
            _authErrorMessage.value = null
            _authIdentifiedMessage.value = "Identified as Super Admin. Master Controls enabled."
            return true
        }

        // 2. Kitchen Outlet Partner Check (User ID or Registered Owner Phone)
        val matchedKitchen = _restaurants.value.find { rest ->
            val matchUser = rest.userId.isNotBlank() && rest.userId.equals(cleanUser, ignoreCase = true)
            val matchPhone = rest.ownerPhone.isNotBlank() &&
                    rest.ownerPhone.filter { it.isDigit() }.takeLast(10) == cleanUser.filter { it.isDigit() }.takeLast(10)
            val matchPass = rest.password == cleanPass
            (matchUser || matchPhone) && matchPass
        }

        if (matchedKitchen != null) {
            _currentKitchenId.value = matchedKitchen.id
            _userRole.value = UserRole.KITCHEN
            _isLoggedIn.value = true
            _authErrorMessage.value = null
            _authIdentifiedMessage.value = "Identified as Kitchen: ${matchedKitchen.name}. Welcome Chef!"
            return true
        }

        _authErrorMessage.value = "Authentication failed. Invalid User ID or Password.\n(Kitchen accounts are created exclusively by Admin. Contact Admin for credentials.)"
        return false
    }

    fun clearAuthIdentifiedMessage() {
        _authIdentifiedMessage.value = null
    }

    fun setUserRole(role: UserRole, kitchenId: String? = null) {
        _userRole.value = role
        if (kitchenId != null) {
            _currentKitchenId.value = kitchenId
        } else if (role == UserRole.KITCHEN && _currentKitchenId.value == null) {
            _currentKitchenId.value = _restaurants.value.firstOrNull()?.id ?: "rest_1"
        }
    }

    fun selectKitchen(kitchenId: String) {
        _currentKitchenId.value = kitchenId
    }

    fun loginAsKitchen(kitchenId: String) {
        _currentKitchenId.value = kitchenId
        _userRole.value = UserRole.KITCHEN
        _isLoggedIn.value = true
    }

    fun loginAsAdmin() {
        _userRole.value = UserRole.ADMIN
        _isLoggedIn.value = true
    }

    fun loginAsCustomer() {
        _userRole.value = UserRole.CUSTOMER
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
        _userRole.value = UserRole.CUSTOMER
        _authIdentifiedMessage.value = null
        _firebaseUid.value = null
        firebaseAuthService.signOut()
    }

    // Dynamic Kitchen & Menu Management (Admin Exclusive: Only Admin can create kitchen logins)
    fun adminRegisterKitchen(
        name: String,
        cuisine: String,
        deliveryTime: Int,
        isPureVeg: Boolean,
        address: String,
        priceForTwo: Int,
        ownerName: String,
        ownerPhone: String,
        userId: String = "",
        password: String = "",
        signatureDishName: String = "",
        dishPrice: Double = 199.0,
        dishCategory: FoodCategoryType = FoodCategoryType.BIRYANI,
        dishDescription: String = ""
    ): Restaurant {
        val newRestId = "rest_${System.currentTimeMillis()}"
        val sanitizedBase = name.lowercase().replace("[^a-zA-Z0-9]".toRegex(), "").take(8)
        val finalUserId = if (userId.isNotBlank()) userId.trim().lowercase() else "kitchen_${sanitizedBase}"
        val finalPassword = if (password.isNotBlank()) password.trim() else "CK@${(1000..9999).random()}"

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
            featuredTag = "Newly Added",
            isOpen = true,
            ownerPhone = ownerPhone.ifBlank { "+91 98765 00000" },
            ownerName = ownerName.ifBlank { "Hotel Partner" },
            userId = finalUserId,
            password = finalPassword
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

            // Platform notification for Admin with credentials to share
            val notif = NotificationItem(
                id = "notif_${System.currentTimeMillis()}",
                title = "🏪 Kitchen Created: $name",
                message = "Login User ID: $finalUserId • Password: $finalPassword. Ready to share with $ownerName.",
                time = "Just now"
            )
            _notifications.value = listOf(notif) + _notifications.value
        }

        return newRestaurant
    }

    fun adminUpdateKitchenCredentials(
        restaurantId: String,
        newUserId: String,
        newPassword: String
    ) {
        val current = _restaurants.value
        _restaurants.value = current.map {
            if (it.id == restaurantId) {
                it.copy(
                    userId = if (newUserId.isNotBlank()) newUserId.trim().lowercase() else it.userId,
                    password = if (newPassword.isNotBlank()) newPassword.trim() else it.password
                )
            } else it
        }
    }

    fun registerAndLoginKitchen(
        name: String,
        cuisine: String,
        deliveryTime: Int,
        isPureVeg: Boolean,
        address: String,
        priceForTwo: Int,
        ownerName: String,
        ownerPhone: String,
        signatureDishName: String = "",
        dishPrice: Double = 199.0,
        dishCategory: FoodCategoryType = FoodCategoryType.BIRYANI,
        dishDescription: String = ""
    ) {
        val rest = adminRegisterKitchen(
            name = name,
            cuisine = cuisine,
            deliveryTime = deliveryTime,
            isPureVeg = isPureVeg,
            address = address,
            priceForTwo = priceForTwo,
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            userId = "",
            password = "",
            signatureDishName = signatureDishName,
            dishPrice = dishPrice,
            dishCategory = dishCategory,
            dishDescription = dishDescription
        )
        _currentKitchenId.value = rest.id
        _userRole.value = UserRole.KITCHEN
        _isLoggedIn.value = true
    }

    fun kitchenToggleStoreStatus(restaurantId: String) {
        val currentList = _restaurants.value
        _restaurants.value = currentList.map {
            if (it.id == restaurantId) it.copy(isOpen = !it.isOpen) else it
        }
    }

    // Admin Master Controls
    fun adminAssignRider(orderId: String, riderName: String, riderPhone: String) {
        viewModelScope.launch {
            repository.updateOrderRider(orderId, riderName, riderPhone)
        }
    }

    fun adminCancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
        }
    }

    fun adminDeleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
        }
    }

    fun adminDeleteDish(dishId: String) {
        viewModelScope.launch {
            repository.deleteDish(dishId)
            _allDishes.value = _allDishes.value.filter { it.id != dishId }
        }
    }

    fun adminDeleteRestaurant(restaurantId: String) {
        viewModelScope.launch {
            repository.deleteRestaurant(restaurantId)
            _restaurants.value = _restaurants.value.filter { it.id != restaurantId }
            _allDishes.value = _allDishes.value.filter { it.restaurantId != restaurantId }
        }
    }

    fun adminAddCoupon(
        code: String,
        discountPercent: Int,
        maxDiscount: Double,
        minOrder: Double,
        title: String,
        description: String,
        maxUses: Int? = null,
        validityHours: Int? = null
    ) {
        val expiryTimestamp = validityHours?.takeIf { it > 0 }?.let {
            System.currentTimeMillis() + (it * 3600L * 1000L)
        }
        val newCoupon = PromoCoupon(
            code = code.uppercase().trim(),
            discountPercent = discountPercent,
            maxDiscount = maxDiscount,
            minOrder = minOrder,
            title = title,
            description = description,
            maxUses = maxUses?.takeIf { it > 0 },
            usedCount = 0,
            expiryTimestamp = expiryTimestamp
        )
        _promoCoupons.value = listOf(newCoupon) + _promoCoupons.value.filter { it.code != newCoupon.code }
    }

    fun adminDeleteCoupon(code: String) {
        _promoCoupons.value = _promoCoupons.value.filter { it.code != code }
        if (_appliedCoupon.value?.code == code) {
            _appliedCoupon.value = null
        }
    }

    // Dynamic Kitchen & Menu Management
    fun registerKitchen(
        name: String,
        cuisine: String,
        deliveryTime: Int,
        isPureVeg: Boolean,
        address: String,
        priceForTwo: Int,
        signatureDishName: String = "",
        dishPrice: Double = 199.0,
        dishCategory: FoodCategoryType = FoodCategoryType.BIRYANI,
        dishDescription: String = "",
        ownerName: String = "Hotel Owner",
        ownerPhone: String = "+91 98765 00000",
        userId: String = "",
        password: String = ""
    ): Restaurant {
        return adminRegisterKitchen(
            name = name,
            cuisine = cuisine,
            deliveryTime = deliveryTime,
            isPureVeg = isPureVeg,
            address = address,
            priceForTwo = priceForTwo,
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            userId = userId,
            password = password,
            signatureDishName = signatureDishName,
            dishPrice = dishPrice,
            dishCategory = dishCategory,
            dishDescription = dishDescription
        )
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

    fun applyCouponDetailed(code: String, subtotal: Double = calculateSubtotal()): Pair<Boolean, String> {
        val coupon = availableCoupons.find { it.code.equals(code.trim(), ignoreCase = true) }
            ?: return Pair(false, "Coupon '$code' not found. Check code & try again.")

        if (coupon.isTimeExpired) {
            return Pair(false, "Sorry! Offer '${coupon.code}' has expired (Validity period ended).")
        }

        if (coupon.isQuotaExhausted) {
            return Pair(false, "Sorry! Offer '${coupon.code}' was limited to the first ${coupon.maxUses} people and all claims are taken.")
        }

        if (subtotal > 0 && subtotal < coupon.minOrder) {
            val shortage = (coupon.minOrder - subtotal).toInt()
            return Pair(false, "Add items worth ₹$shortage more to apply '${coupon.code}' (Min order ₹${coupon.minOrder.toInt()}).")
        }

        _appliedCoupon.value = coupon
        return Pair(true, "'${coupon.code}' applied successfully! Saved ${coupon.discountPercent}%.")
    }

    fun applyCoupon(code: String): Boolean {
        return applyCouponDetailed(code).first
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

            // Sync to Firestore and attach real-time listener
            firestoreOrderService.syncOrderToFirestore(newOrder)
            startObservingOrderInFirestore(generatedOrderId)

            // Add notification
            val notif = NotificationItem(
                id = "notif_${System.currentTimeMillis()}",
                title = "🛵 Order Placed Successfully!",
                message = "Your order #$generatedOrderId at ${newOrder.restaurantName} is confirmed. Arriving in ~18 mins.",
                time = "Just now",
                orderId = generatedOrderId
            )
            _notifications.value = listOf(notif) + _notifications.value

            // Increment coupon redemption usage if a coupon was used
            val appliedCode = _appliedCoupon.value?.code
            if (!appliedCode.isNullOrBlank()) {
                _promoCoupons.value = _promoCoupons.value.map { coupon ->
                    if (coupon.code.equals(appliedCode, ignoreCase = true)) {
                        coupon.copy(usedCount = coupon.usedCount + 1)
                    } else coupon
                }
            }

            _appliedCoupon.value = null
            onOrderPlaced(generatedOrderId)
        }
    }

    fun selectOrderForTracking(orderId: String) {
        _selectedTrackingOrderId.value = orderId
        startObservingOrderInFirestore(orderId)
    }

    /**
     * Connects real-time Firestore Snapshot Listener to orders/{orderId}.
     * Automatically syncs changes from Firestore into Room and UI state flow.
     */
    fun startObservingOrderInFirestore(orderId: String) {
        activeFirestoreListenerJob?.cancel()
        activeFirestoreListenerJob = viewModelScope.launch {
            val localOrder = repository.getOrderById(orderId).firstOrNull()
            if (localOrder != null) {
                // Ensure document is seeded in Firestore
                firestoreOrderService.syncOrderToFirestore(localOrder)
            }
            firestoreOrderService.observeOrder(orderId).collect { update ->
                _firestoreOrderUpdate.value = update
                if (update.order != null) {
                    repository.updateOrderStatus(update.order.orderId, update.order.status)
                }
            }
        }
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
        updateOrderStatusDirectly(orderId, nextStatus)
    }

    fun updateOrderStatusDirectly(orderId: String, status: OrderStatus, customStageNote: String? = null) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            firestoreOrderService.updateOrderStatusInFirestore(orderId, status, customStageNote)
        }
    }

    /**
     * Direct test trigger to progress order in Firestore (triggers real-time snapshot listener)
     */
    fun progressOrderInFirestore(orderId: String, status: OrderStatus, customStageNote: String? = null) {
        updateOrderStatusDirectly(orderId, status, customStageNote)
    }

    /**
     * Automated real-time simulation: walks through Preparing -> Out for Delivery -> Delivered
     * via Firestore updates, firing the Firestore snapshot listener in real time.
     */
    fun startLiveAutoProgressionSimulation(orderId: String) {
        liveSimulationJob?.cancel()
        liveSimulationJob = viewModelScope.launch {
            // Stage 1: Move to PREPARING
            delay(1000)
            updateOrderStatusDirectly(
                orderId,
                OrderStatus.PREPARING,
                "👨‍🍳 Master Chef is cooking your order with fresh spices & herbs"
            )

            // Stage 2: Move to OUT_FOR_DELIVERY
            delay(5000)
            updateOrderStatusDirectly(
                orderId,
                OrderStatus.OUT_FOR_DELIVERY,
                "🛵 Rider Rajesh Kumar picked up parcel and is speeding to your doorstep"
            )

            // Stage 3: Move to DELIVERED
            delay(6000)
            updateOrderStatusDirectly(
                orderId,
                OrderStatus.DELIVERED,
                "🎉 Delivered hot and fresh! Enjoy your authentic meal"
            )
        }
    }

    fun stopLiveAutoProgressionSimulation() {
        liveSimulationJob?.cancel()
        liveSimulationJob = null
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
                    "Use coupon code KHAIBU50 for 50% discount on your order!"
                else ->
                    "Thank you for contacting Khaibu support! A food delivery specialist is reviewing your request. We are here 24/7."
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
