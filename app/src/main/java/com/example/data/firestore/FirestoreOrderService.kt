package com.example.data.firestore

import android.util.Log
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class FirestoreSyncState(val label: String, val isConnected: Boolean) {
    CONNECTING("Connecting to Firestore...", false),
    REALTIME_CONNECTED("Live Firestore Real-Time Listener Active", true),
    CACHE_SYNC("Syncing Cached Snapshot", true),
    OFFLINE_SYNC("Local Cache Sync (Offline)", false),
    ERROR("Firestore Retrying...", false),
    NOT_FOUND("Order Initializing in Firestore", false)
}

data class FirestoreOrderUpdate(
    val order: OrderEntity?,
    val syncState: FirestoreSyncState,
    val stageNote: String = "",
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val updateCount: Int = 0,
    val isFromCache: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Service managing real-time Cloud Firestore order listeners and state propagation.
 */
class FirestoreOrderService private constructor() {

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val ordersCollection by lazy {
        db.collection(COLLECTION_ORDERS)
    }

    companion object {
        private const val TAG = "FirestoreOrderService"
        private const val COLLECTION_ORDERS = "orders"

        @Volatile
        private var instance: FirestoreOrderService? = null

        fun getInstance(): FirestoreOrderService {
            return instance ?: synchronized(this) {
                instance ?: FirestoreOrderService().also { instance = it }
            }
        }
    }

    /**
     * Upload / Sync order to Firestore collection "orders/{orderId}"
     */
    suspend fun syncOrderToFirestore(order: OrderEntity): Boolean = suspendCoroutine { cont ->
        try {
            val orderMap = orderEntityToMap(order)
            ordersCollection.document(order.orderId)
                .set(orderMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully synced order #${order.orderId} to Firestore")
                    cont.resume(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to sync order #${order.orderId} to Firestore", e)
                    cont.resume(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing order to Firestore", e)
            cont.resume(false)
        }
    }

    /**
     * Update order status in Firestore "orders/{orderId}"
     * Triggers Firestore snapshot listeners in real time!
     */
    suspend fun updateOrderStatusInFirestore(
        orderId: String,
        status: OrderStatus,
        stageNote: String? = null,
        etaMinutes: Int? = null
    ): Boolean = suspendCoroutine { cont ->
        try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name,
                "statusTitle" to status.title,
                "stepIndex" to status.stepIndex,
                "updatedAt" to System.currentTimeMillis()
            )

            val note = stageNote ?: when (status) {
                OrderStatus.PLACED -> "Order placed, waiting for kitchen confirmation"
                OrderStatus.ACCEPTED -> "Kitchen confirmed! Order sent to cooking queue"
                OrderStatus.PREPARING -> "👨‍🍳 Master Chef is cooking your fresh meal with authentic ingredients"
                OrderStatus.OUT_FOR_DELIVERY -> "🛵 Rider Rajesh Kumar picked up parcel and is speeding to your doorstep"
                OrderStatus.DELIVERED -> "🎉 Order delivered! Enjoy your hot meal"
                OrderStatus.CANCELLED -> "Order was cancelled"
            }
            updates["currentStageText"] = note

            val calculatedEta = etaMinutes ?: when (status) {
                OrderStatus.PLACED -> 22
                OrderStatus.ACCEPTED -> 18
                OrderStatus.PREPARING -> 12
                OrderStatus.OUT_FOR_DELIVERY -> 6
                OrderStatus.DELIVERED -> 0
                OrderStatus.CANCELLED -> 0
            }
            updates["estimatedArrivalMinutes"] = calculatedEta

            ordersCollection.document(orderId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully updated status of #$orderId to ${status.name} in Firestore")
                    cont.resume(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to update status in Firestore for #$orderId", e)
                    cont.resume(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating order status in Firestore", e)
            cont.resume(false)
        }
    }

    /**
     * Real-time Snapshot Listener Flow observing orders/{orderId}
     * Emits immediately whenever status changes in Firestore (e.g. Preparing -> Out for Delivery)
     */
    fun observeOrder(orderId: String): Flow<FirestoreOrderUpdate> = callbackFlow {
        var updateCounter = 0
        Log.d(TAG, "Attaching real-time Firestore snapshot listener for order #$orderId")

        trySend(FirestoreOrderUpdate(null, FirestoreSyncState.CONNECTING))

        val docRef = ordersCollection.document(orderId)
        val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Firestore snapshot listener error for #$orderId: ${error.message}")
                trySend(
                    FirestoreOrderUpdate(
                        order = null,
                        syncState = FirestoreSyncState.ERROR,
                        errorMessage = error.message
                    )
                )
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                updateCounter++
                val order = snapshotToOrderEntity(snapshot)
                val stageNote = snapshot.getString("currentStageText") ?: ""
                val isFromCache = snapshot.metadata.isFromCache
                val syncState = if (isFromCache) FirestoreSyncState.CACHE_SYNC else FirestoreSyncState.REALTIME_CONNECTED

                Log.d(TAG, "Real-time snapshot update #$updateCounter received for #${order.orderId}: status=${order.status}")

                trySend(
                    FirestoreOrderUpdate(
                        order = order,
                        syncState = syncState,
                        stageNote = stageNote,
                        lastUpdatedMillis = snapshot.getLong("updatedAt") ?: System.currentTimeMillis(),
                        updateCount = updateCounter,
                        isFromCache = isFromCache
                    )
                )
            } else {
                Log.d(TAG, "Document for #$orderId does not exist in Firestore yet")
                trySend(
                    FirestoreOrderUpdate(
                        order = null,
                        syncState = FirestoreSyncState.NOT_FOUND
                    )
                )
            }
        }

        awaitClose {
            Log.d(TAG, "Removing Firestore snapshot listener for order #$orderId")
            registration.remove()
        }
    }

    private fun orderEntityToMap(order: OrderEntity): Map<String, Any> {
        val stageNote = when (order.status) {
            OrderStatus.PLACED -> "Order placed, waiting for kitchen confirmation"
            OrderStatus.ACCEPTED -> "Kitchen confirmed! Order sent to cooking queue"
            OrderStatus.PREPARING -> "👨‍🍳 Master Chef is cooking your fresh meal with authentic ingredients"
            OrderStatus.OUT_FOR_DELIVERY -> "🛵 Rider Rajesh Kumar picked up parcel and is speeding to your doorstep"
            OrderStatus.DELIVERED -> "🎉 Order delivered! Enjoy your hot meal"
            OrderStatus.CANCELLED -> "Order was cancelled"
        }

        return mapOf(
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
            "statusTitle" to order.status.title,
            "stepIndex" to order.status.stepIndex,
            "riderName" to order.riderName,
            "riderPhone" to order.riderPhone,
            "riderVehicle" to order.riderVehicle,
            "riderRating" to order.riderRating,
            "estimatedArrivalMinutes" to order.estimatedArrivalMinutes,
            "deliveryNotes" to order.deliveryNotes,
            "couponApplied" to order.couponApplied,
            "currentStageText" to stageNote,
            "updatedAt" to System.currentTimeMillis()
        )
    }

    private fun snapshotToOrderEntity(snapshot: DocumentSnapshot): OrderEntity {
        val statusString = snapshot.getString("status") ?: OrderStatus.ACCEPTED.name
        val status = try {
            OrderStatus.valueOf(statusString)
        } catch (e: Exception) {
            OrderStatus.ACCEPTED
        }

        return OrderEntity(
            orderId = snapshot.getString("orderId") ?: snapshot.id,
            timestamp = snapshot.getLong("timestamp") ?: System.currentTimeMillis(),
            restaurantId = snapshot.getString("restaurantId") ?: "",
            restaurantName = snapshot.getString("restaurantName") ?: "Restaurant",
            customerName = snapshot.getString("customerName") ?: "Customer",
            customerPhone = snapshot.getString("customerPhone") ?: "",
            deliveryAddress = snapshot.getString("deliveryAddress") ?: "Delivery Address",
            itemsSummary = snapshot.getString("itemsSummary") ?: "Food Items",
            itemsCount = (snapshot.getLong("itemsCount") ?: 1L).toInt(),
            subtotal = snapshot.getDouble("subtotal") ?: 0.0,
            deliveryFee = snapshot.getDouble("deliveryFee") ?: 0.0,
            platformFee = snapshot.getDouble("platformFee") ?: 15.0,
            discount = snapshot.getDouble("discount") ?: 0.0,
            totalAmount = snapshot.getDouble("totalAmount") ?: 0.0,
            paymentMethod = snapshot.getString("paymentMethod") ?: "UPI",
            paymentStatus = snapshot.getString("paymentStatus") ?: "PAID",
            status = status,
            riderName = snapshot.getString("riderName") ?: "Rajesh Kumar",
            riderPhone = snapshot.getString("riderPhone") ?: "+91 98765 43210",
            riderVehicle = snapshot.getString("riderVehicle") ?: "Honda Activa • MH 12 AB 4590",
            riderRating = snapshot.getDouble("riderRating") ?: 4.9,
            estimatedArrivalMinutes = (snapshot.getLong("estimatedArrivalMinutes") ?: 18L).toInt(),
            deliveryNotes = snapshot.getString("deliveryNotes") ?: "",
            couponApplied = snapshot.getString("couponApplied") ?: ""
        )
    }
}
