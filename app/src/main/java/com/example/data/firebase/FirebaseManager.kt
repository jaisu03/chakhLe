package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.AddressEntity
import com.example.data.model.Dish
import com.example.data.model.FoodCategoryType
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.Restaurant
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseManager"
        private const val COLLECTION_RESTAURANTS = "restaurants"
        private const val COLLECTION_DISHES = "dishes"
        private const val COLLECTION_ORDERS = "orders"
        private const val COLLECTION_USERS = "users"

        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(context: Context): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val _isFirebaseConfigured = MutableStateFlow(false)
    val isFirebaseConfigured: StateFlow<Boolean> = _isFirebaseConfigured.asStateFlow()

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        checkAndInitialize()
    }

    private fun checkAndInitialize() {
        try {
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }

            if (app != null) {
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                _isFirebaseConfigured.value = true
                _currentUser.value = auth?.currentUser

                auth?.addAuthStateListener { firebaseAuth ->
                    _currentUser.value = firebaseAuth.currentUser
                }
                Log.d(TAG, "Firebase successfully initialized for project: ${app.options.projectId}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not configured or missing google-services.json: ${e.message}")
            _isFirebaseConfigured.value = false
        }
    }

    fun isAvailable(): Boolean = _isFirebaseConfigured.value && firestore != null

    // -------------------------------------------------------------
    // Firebase Authentication
    // -------------------------------------------------------------

    suspend fun signInAnonymously(): Result<FirebaseUser?> {
        val authInstance = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))
        return try {
            val authResult = authInstance.signInAnonymously().await()
            _currentUser.value = authResult.user
            Log.d(TAG, "Anonymous sign-in successful: ${authResult.user?.uid}")
            Result.success(authResult.user)
        } catch (e: Exception) {
            if (e.message?.contains("CONFIGURATION_NOT_FOUND") == true) {
                Log.w(
                    TAG,
                    "Firebase Anonymous Sign-in is not enabled in Firebase Console. " +
                    "To enable: Firebase Console -> Build -> Authentication -> Sign-in method -> Enable 'Anonymous'. " +
                    "Continuing in guest mode without cloud authentication."
                )
            } else {
                Log.w(TAG, "Anonymous sign in not available: ${e.message}")
            }
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
        _currentUser.value = null
    }

    // -------------------------------------------------------------
    // Realtime Firestore Orders Sync
    // -------------------------------------------------------------

    fun observeOrdersRealtime(): Flow<List<OrderEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ORDERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to orders", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val orders = snapshot.documents.mapNotNull { doc ->
                            try {
                                val statusStr = doc.getString("status") ?: OrderStatus.ACCEPTED.name
                                val orderStatus = try {
                                    OrderStatus.valueOf(statusStr)
                                } catch (_: Exception) {
                                    OrderStatus.ACCEPTED
                                }

                                OrderEntity(
                                    orderId = doc.getString("orderId") ?: doc.id,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    restaurantId = doc.getString("restaurantId") ?: "",
                                    restaurantName = doc.getString("restaurantName") ?: "Kitchen",
                                    customerName = doc.getString("customerName") ?: "Customer",
                                    customerPhone = doc.getString("customerPhone") ?: "",
                                    deliveryAddress = doc.getString("deliveryAddress") ?: "",
                                    itemsSummary = doc.getString("itemsSummary") ?: "",
                                    itemsCount = (doc.getLong("itemsCount") ?: 1L).toInt(),
                                    subtotal = doc.getDouble("subtotal") ?: 0.0,
                                    deliveryFee = doc.getDouble("deliveryFee") ?: 0.0,
                                    platformFee = doc.getDouble("platformFee") ?: 10.0,
                                    discount = doc.getDouble("discount") ?: 0.0,
                                    totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                    paymentMethod = doc.getString("paymentMethod") ?: "UPI",
                                    paymentStatus = doc.getString("paymentStatus") ?: "PAID",
                                    status = orderStatus,
                                    riderName = doc.getString("riderName") ?: "Rider Partner",
                                    riderPhone = doc.getString("riderPhone") ?: "+91 98765 43210",
                                    riderVehicle = doc.getString("riderVehicle") ?: "Delivery Bike",
                                    riderRating = doc.getDouble("riderRating") ?: 4.8,
                                    estimatedArrivalMinutes = (doc.getLong("estimatedArrivalMinutes") ?: 20L).toInt(),
                                    deliveryNotes = doc.getString("deliveryNotes") ?: "",
                                    couponApplied = doc.getString("couponApplied") ?: ""
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing order document ${doc.id}", e)
                                null
                            }
                        }
                        trySend(orders)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting orders listener", e)
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun saveOrderToFirestore(order: OrderEntity): Boolean {
        val db = firestore ?: return false
        return try {
            val orderData = hashMapOf(
                "orderId" to order.orderId,
                "timestamp" to order.timestamp,
                "restaurantId" to order.restaurantId,
                "restaurantName" to order.restaurantName,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "deliveryAddress" to order.deliveryAddress,
                "itemsSummary" to order.itemsSummary,
                "itemsCount" to order.itemsCount,
                "subtotal" to order.subtotal,
                "deliveryFee" to order.deliveryFee,
                "platformFee" to order.platformFee,
                "discount" to order.discount,
                "totalAmount" to order.totalAmount,
                "paymentMethod" to order.paymentMethod,
                "paymentStatus" to order.paymentStatus,
                "status" to order.status.name,
                "riderName" to order.riderName,
                "riderPhone" to order.riderPhone,
                "riderVehicle" to order.riderVehicle,
                "riderRating" to order.riderRating,
                "estimatedArrivalMinutes" to order.estimatedArrivalMinutes,
                "deliveryNotes" to order.deliveryNotes,
                "couponApplied" to order.couponApplied
            )
            db.collection(COLLECTION_ORDERS).document(order.orderId)
                .set(orderData, SetOptions.merge())
                .await()
            Log.d(TAG, "Order ${order.orderId} saved to Firestore successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save order to Firestore", e)
            false
        }
    }

    suspend fun updateOrderStatusInFirestore(orderId: String, status: OrderStatus): Boolean {
        val db = firestore ?: return false
        return try {
            db.collection(COLLECTION_ORDERS).document(orderId)
                .update("status", status.name)
                .await()
            Log.d(TAG, "Order $orderId status updated to ${status.name} in Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update order status in Firestore", e)
            false
        }
    }

    // -------------------------------------------------------------
    // Realtime Firestore Restaurants Sync
    // -------------------------------------------------------------

    fun observeRestaurantsRealtime(): Flow<List<Restaurant>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_RESTAURANTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to restaurants", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val restaurants = snapshot.documents.mapNotNull { doc ->
                            try {
                                Restaurant(
                                    id = doc.getString("id") ?: doc.id,
                                    name = doc.getString("name") ?: "",
                                    cuisine = doc.getString("cuisine") ?: "",
                                    rating = doc.getDouble("rating") ?: 4.5,
                                    reviewCount = doc.getString("reviewCount") ?: "100+",
                                    deliveryTimeMinutes = (doc.getLong("deliveryTimeMinutes") ?: 25L).toInt(),
                                    priceForTwo = (doc.getLong("priceForTwo") ?: 300L).toInt(),
                                    offerText = doc.getString("offerText") ?: "Flat 20% OFF",
                                    distanceKm = doc.getDouble("distanceKm") ?: 2.0,
                                    isPureVeg = doc.getBoolean("isPureVeg") ?: false,
                                    address = doc.getString("address") ?: "",
                                    featuredTag = doc.getString("featuredTag")
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing restaurant ${doc.id}", e)
                                null
                            }
                        }
                        trySend(restaurants)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting restaurants listener", e)
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun saveRestaurantToFirestore(restaurant: Restaurant): Boolean {
        val db = firestore ?: return false
        return try {
            val map = hashMapOf(
                "id" to restaurant.id,
                "name" to restaurant.name,
                "cuisine" to restaurant.cuisine,
                "rating" to restaurant.rating,
                "reviewCount" to restaurant.reviewCount,
                "deliveryTimeMinutes" to restaurant.deliveryTimeMinutes,
                "priceForTwo" to restaurant.priceForTwo,
                "offerText" to restaurant.offerText,
                "distanceKm" to restaurant.distanceKm,
                "isPureVeg" to restaurant.isPureVeg,
                "address" to restaurant.address,
                "featuredTag" to (restaurant.featuredTag ?: "")
            )
            db.collection(COLLECTION_RESTAURANTS).document(restaurant.id)
                .set(map, SetOptions.merge())
                .await()
            Log.d(TAG, "Restaurant ${restaurant.id} saved to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save restaurant to Firestore", e)
            false
        }
    }

    // -------------------------------------------------------------
    // Realtime Firestore Dishes Sync
    // -------------------------------------------------------------

    fun observeDishesRealtime(): Flow<List<Dish>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_DISHES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to dishes", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val dishes = snapshot.documents.mapNotNull { doc ->
                            try {
                                val catName = doc.getString("category") ?: FoodCategoryType.BIRYANI.name
                                val category = try {
                                    FoodCategoryType.valueOf(catName)
                                } catch (_: Exception) {
                                    FoodCategoryType.BIRYANI
                                }

                                Dish(
                                    id = doc.getString("id") ?: doc.id,
                                    restaurantId = doc.getString("restaurantId") ?: "",
                                    restaurantName = doc.getString("restaurantName") ?: "",
                                    name = doc.getString("name") ?: "",
                                    description = doc.getString("description") ?: "",
                                    price = doc.getDouble("price") ?: 150.0,
                                    originalPrice = doc.getDouble("originalPrice"),
                                    isVeg = doc.getBoolean("isVeg") ?: true,
                                    rating = doc.getDouble("rating") ?: 4.6,
                                    ratingCount = (doc.getLong("ratingCount") ?: 100L).toInt(),
                                    category = category,
                                    prepTimeMinutes = (doc.getLong("prepTimeMinutes") ?: 20L).toInt(),
                                    isBestseller = doc.getBoolean("isBestseller") ?: false,
                                    spicyLevel = (doc.getLong("spicyLevel") ?: 1L).toInt(),
                                    portionSize = doc.getString("portionSize") ?: "Serves 1"
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing dish ${doc.id}", e)
                                null
                            }
                        }
                        trySend(dishes)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception listening to dishes", e)
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun saveDishToFirestore(dish: Dish): Boolean {
        val db = firestore ?: return false
        return try {
            val map = hashMapOf(
                "id" to dish.id,
                "restaurantId" to dish.restaurantId,
                "restaurantName" to dish.restaurantName,
                "name" to dish.name,
                "description" to dish.description,
                "price" to dish.price,
                "originalPrice" to (dish.originalPrice ?: (dish.price * 1.2)),
                "isVeg" to dish.isVeg,
                "rating" to dish.rating,
                "ratingCount" to dish.ratingCount,
                "category" to dish.category.name,
                "prepTimeMinutes" to dish.prepTimeMinutes,
                "isBestseller" to dish.isBestseller,
                "spicyLevel" to dish.spicyLevel,
                "portionSize" to dish.portionSize
            )
            db.collection(COLLECTION_DISHES).document(dish.id)
                .set(map, SetOptions.merge())
                .await()
            Log.d(TAG, "Dish ${dish.id} saved to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save dish to Firestore", e)
            false
        }
    }

    // -------------------------------------------------------------
    // Auto Setup & Seeding
    // -------------------------------------------------------------

    suspend fun autoSetupBackend(
        defaultRestaurants: List<Restaurant>,
        defaultDishes: List<Dish>
    ) {
        val db = firestore ?: return
        try {
            // 1. Ensure user is authenticated
            if (auth?.currentUser == null) {
                signInAnonymously()
            }

            // 2. Auto-seed restaurants if collection is empty
            val restSnapshot = db.collection(COLLECTION_RESTAURANTS).limit(1).get().await()
            if (restSnapshot.isEmpty) {
                Log.d(TAG, "Seeding initial ${defaultRestaurants.size} restaurants to Firestore...")
                for (rest in defaultRestaurants) {
                    saveRestaurantToFirestore(rest)
                }
            }

            // 3. Auto-seed dishes if collection is empty
            val dishSnapshot = db.collection(COLLECTION_DISHES).limit(1).get().await()
            if (dishSnapshot.isEmpty) {
                Log.d(TAG, "Seeding initial ${defaultDishes.size} dishes to Firestore...")
                for (dish in defaultDishes) {
                    saveDishToFirestore(dish)
                }
            }
            Log.d(TAG, "Firebase backend auto-setup completed successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Auto setup backend encountered an issue", e)
        }
    }
}
