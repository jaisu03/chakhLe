package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FoodCategoryType(val displayName: String) {
    BIRYANI("Biryani"),
    PIZZA_FAST_FOOD("Pizza & Fast Food"),
    INDIAN_THALI("Indian Thali"),
    DESSERTS("Desserts/Sweets"),
    STREET_FOOD("Street Food"),
    BEVERAGES("Beverages"),
    ALL("All Items")
}

data class FoodCategory(
    val id: String,
    val type: FoodCategoryType,
    val name: String,
    val subtitle: String,
    val badge: String? = null
)

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val reviewCount: String,
    val deliveryTimeMinutes: Int,
    val priceForTwo: Int,
    val offerText: String,
    val distanceKm: Double,
    val isPureVeg: Boolean = false,
    val address: String,
    val featuredTag: String? = null
)

data class Dish(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double? = null,
    val isVeg: Boolean,
    val rating: Double,
    val ratingCount: Int,
    val category: FoodCategoryType,
    val prepTimeMinutes: Int = 20,
    val isBestseller: Boolean = false,
    val spicyLevel: Int = 1, // 1 to 3
    val portionSize: String = "Serves 1-2"
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val dishId: String,
    val restaurantId: String,
    val restaurantName: String,
    val dishName: String,
    val price: Double,
    val quantity: Int,
    val isVeg: Boolean,
    val category: String,
    val notes: String = ""
)

enum class OrderStatus(val title: String, val description: String, val stepIndex: Int) {
    PLACED("Order Placed", "Waiting for restaurant confirmation", 0),
    ACCEPTED("Order Confirmed by Restaurant", "Restaurant is reviewing your order details", 1),
    PREPARING("Food Being Prepared in Kitchen", "Master chef is cooking your delicious meal", 2),
    OUT_FOR_DELIVERY("Rider Picked Up & Out for Delivery", "Rider is speeding towards your doorstep", 3),
    DELIVERED("Delivered at Doorstep", "Enjoy your hot and fresh meal!", 4),
    CANCELLED("Order Cancelled", "Refund initiated to original payment source", -1);

    val displayName: String get() = title
}

enum class PaymentMethod(val title: String, val subtitle: String) {
    UPI_GPAY("Google Pay", "Instant UPI payment via Tez / GPay"),
    UPI_PHONEPE("PhonePe", "Instant UPI payment via PhonePe"),
    UPI_PAYTM("Paytm UPI", "Paytm wallet & UPI"),
    COD("Cash on Delivery", "Pay via Cash or UPI at your doorstep"),
    CARD("Debit / Credit Card", "Visa, Mastercard, RuPay & Amex")
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val restaurantId: String,
    val restaurantName: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val itemsSummary: String, // E.g. "2x Hyderabadi Biryani, 1x Gulab Jamun"
    val itemsCount: Int,
    val subtotal: Double,
    val deliveryFee: Double,
    val platformFee: Double,
    val discount: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String = "PAID",
    val status: OrderStatus = OrderStatus.ACCEPTED,
    val riderName: String = "Rajesh Kumar",
    val riderPhone: String = "+91 98765 43210",
    val riderVehicle: String = "Honda Activa • MH 12 AB 4590",
    val riderRating: Double = 4.9,
    val estimatedArrivalMinutes: Int = 18,
    val deliveryNotes: String = "",
    val couponApplied: String = ""
)

@Entity(tableName = "saved_addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String, // "Home", "Work", "Other"
    val recipientName: String,
    val phone: String,
    val houseNo: String,
    val area: String,
    val landmark: String,
    val city: String,
    val pincode: String = "560034",
    val isDefault: Boolean = false
)

data class PromoCoupon(
    val code: String,
    val discountPercent: Int,
    val maxDiscount: Double,
    val minOrder: Double,
    val title: String,
    val description: String
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean = false,
    val orderId: String? = null
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val message: String,
    val time: String
)

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String
)
