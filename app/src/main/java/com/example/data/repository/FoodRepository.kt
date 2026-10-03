package com.example.data.repository

import android.content.Context
import com.example.data.local.AddressDao
import com.example.data.local.CartDao
import com.example.data.local.OrderDao
import com.example.data.model.AddressEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.Dish
import com.example.data.model.FaqItem
import com.example.data.model.FoodCategory
import com.example.data.model.FoodCategoryType
import com.example.data.model.NotificationItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PromoCoupon
import com.example.data.model.Restaurant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class FoodRepository(
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val addressDao: AddressDao,
    private val context: Context? = null
) {
    val allCartItems: Flow<List<CartItemEntity>> = cartDao.getAllCartItems()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allAddresses: Flow<List<AddressEntity>> = addressDao.getAllAddresses()
    val latestActiveOrder: Flow<OrderEntity?> = orderDao.getLatestActiveOrder()

    fun getOrderById(orderId: String): Flow<OrderEntity?> = orderDao.getOrderById(orderId)

    suspend fun addToCart(dish: Dish) {
        val current = cartDao.getAllCartItems().firstOrNull() ?: emptyList()
        val existing = current.find { it.dishId == dish.id }
        if (existing != null) {
            cartDao.updateQuantity(dish.id, existing.quantity + 1)
        } else {
            cartDao.insertOrUpdate(
                CartItemEntity(
                    dishId = dish.id,
                    restaurantId = dish.restaurantId,
                    restaurantName = dish.restaurantName,
                    dishName = dish.name,
                    price = dish.price,
                    quantity = 1,
                    isVeg = dish.isVeg,
                    category = dish.category.name
                )
            )
        }
    }

    suspend fun decreaseCartItem(dishId: String) {
        val current = cartDao.getAllCartItems().firstOrNull() ?: emptyList()
        val existing = current.find { it.dishId == dishId } ?: return
        if (existing.quantity > 1) {
            cartDao.updateQuantity(dishId, existing.quantity - 1)
        } else {
            cartDao.deleteItem(dishId)
        }
    }

    suspend fun removeCartItem(dishId: String) {
        cartDao.deleteItem(dishId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    suspend fun placeOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
        cartDao.clearCart()
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status)
    }

    suspend fun updateOrderRider(orderId: String, riderName: String, riderPhone: String) {
        orderDao.updateOrderRider(orderId, riderName, riderPhone)
    }

    suspend fun deleteOrder(orderId: String) {
        orderDao.deleteOrder(orderId)
    }

    suspend fun addAddress(address: AddressEntity) {
        if (address.isDefault) {
            addressDao.resetDefaults()
        }
        val id = addressDao.insertAddress(address)
        if (address.isDefault) {
            addressDao.setDefaultAddress(id)
        }
    }

    suspend fun setDefaultAddress(id: Long) {
        addressDao.resetDefaults()
        addressDao.setDefaultAddress(id)
    }

    suspend fun deleteAddress(address: AddressEntity) {
        addressDao.deleteAddress(address)
    }

    suspend fun initializeSeedDataIfEmpty() {
        val addresses = addressDao.getAllAddresses().firstOrNull()
        if (addresses.isNullOrEmpty()) {
            addressDao.insertAddress(
                AddressEntity(
                    tag = "Home",
                    recipientName = "Aman Sharma",
                    phone = "+91 98765 12345",
                    houseNo = "Flat 402, Royal Palms Heights",
                    area = "Koramangala 4th Block",
                    landmark = "Opp. Sony World Junction",
                    city = "Bengaluru",
                    isDefault = true
                )
            )
            addressDao.insertAddress(
                AddressEntity(
                    tag = "Work",
                    recipientName = "Aman Sharma",
                    phone = "+91 98765 12345",
                    houseNo = "Tower B, 6th Floor, Cyber Park",
                    area = "Outer Ring Road, Bellandur",
                    landmark = "Near Ecospace Tech Park",
                    city = "Bengaluru",
                    isDefault = false
                )
            )
        }

        val orders = orderDao.getAllOrders().firstOrNull()
        if (orders.isNullOrEmpty()) {
            orderDao.insertOrder(
                OrderEntity(
                    orderId = "CK-8491",
                    timestamp = System.currentTimeMillis() - 86400000L * 2, // 2 days ago
                    restaurantId = "rest_1",
                    restaurantName = "Behrouz Royal Biryani & Kebabs",
                    customerName = "Aman Sharma",
                    customerPhone = "+91 98765 12345",
                    deliveryAddress = "Flat 402, Royal Palms, Koramangala, Bengaluru",
                    itemsSummary = "1x Dum Gosht Biryani, 1x Gulab Jamun",
                    itemsCount = 2,
                    subtotal = 499.0,
                    deliveryFee = 0.0,
                    platformFee = 15.0,
                    discount = 100.0,
                    totalAmount = 414.0,
                    paymentMethod = "UPI (Google Pay)",
                    status = OrderStatus.DELIVERED,
                    riderName = "Vikram Singh",
                    riderPhone = "+91 98123 45678",
                    riderVehicle = "TVS Jupiter • KA 01 EQ 3412",
                    riderRating = 4.9,
                    estimatedArrivalMinutes = 0
                )
            )
        }
    }

    // Custom Dynamic Catalogs with 100% Free Zero-Cost Local Persistence
    private val prefs = context?.getSharedPreferences("chakhle_free_store", Context.MODE_PRIVATE)
    private val _customRestaurants = kotlinx.coroutines.flow.MutableStateFlow<List<Restaurant>>(emptyList())
    private val _customDishes = kotlinx.coroutines.flow.MutableStateFlow<List<Dish>>(emptyList())

    init {
        loadPersistedData()
    }

    private fun loadPersistedData() {
        if (prefs == null) return
        try {
            val restJson = prefs.getString("custom_restaurants", null)
            if (!restJson.isNullOrBlank()) {
                val array = JSONArray(restJson)
                val list = mutableListOf<Restaurant>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Restaurant(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            cuisine = obj.getString("cuisine"),
                            rating = obj.optDouble("rating", 4.5),
                            reviewCount = obj.optString("reviewCount", "1.2k+"),
                            deliveryTimeMinutes = obj.optInt("deliveryTimeMinutes", 25),
                            priceForTwo = obj.optInt("priceForTwo", 350),
                            offerText = obj.optString("offerText", "20% OFF"),
                            distanceKm = obj.optDouble("distanceKm", 2.0),
                            isPureVeg = obj.optBoolean("isPureVeg", false),
                            address = obj.optString("address", ""),
                            featuredTag = if (obj.has("featuredTag") && !obj.isNull("featuredTag")) obj.getString("featuredTag") else null,
                            isOpen = obj.optBoolean("isOpen", true),
                            ownerPhone = obj.optString("ownerPhone", ""),
                            ownerName = obj.optString("ownerName", ""),
                            userId = obj.optString("userId", ""),
                            password = obj.optString("password", "")
                        )
                    )
                }
                _customRestaurants.value = list
            }

            val dishJson = prefs.getString("custom_dishes", null)
            if (!dishJson.isNullOrBlank()) {
                val array = JSONArray(dishJson)
                val list = mutableListOf<Dish>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val catName = obj.optString("category", "ALL")
                    val cat = try { FoodCategoryType.valueOf(catName) } catch (e: Exception) { FoodCategoryType.ALL }
                    list.add(
                        Dish(
                            id = obj.getString("id"),
                            restaurantId = obj.getString("restaurantId"),
                            restaurantName = obj.getString("restaurantName"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            price = obj.getDouble("price"),
                            originalPrice = if (obj.has("originalPrice") && !obj.isNull("originalPrice")) obj.getDouble("originalPrice") else null,
                            isVeg = obj.optBoolean("isVeg", false),
                            rating = obj.optDouble("rating", 4.5),
                            ratingCount = obj.optInt("ratingCount", 50),
                            category = cat,
                            prepTimeMinutes = obj.optInt("prepTimeMinutes", 20),
                            isBestseller = obj.optBoolean("isBestseller", false),
                            spicyLevel = obj.optInt("spicyLevel", 1),
                            portionSize = obj.optString("portionSize", "Serves 1-2")
                        )
                    )
                }
                _customDishes.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun persistCustomRestaurants(restaurants: List<Restaurant>) {
        if (prefs == null) return
        try {
            val array = JSONArray()
            for (r in restaurants) {
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("name", r.name)
                    put("cuisine", r.cuisine)
                    put("rating", r.rating)
                    put("reviewCount", r.reviewCount)
                    put("deliveryTimeMinutes", r.deliveryTimeMinutes)
                    put("priceForTwo", r.priceForTwo)
                    put("offerText", r.offerText)
                    put("distanceKm", r.distanceKm)
                    put("isPureVeg", r.isPureVeg)
                    put("address", r.address)
                    put("featuredTag", r.featuredTag ?: "")
                    put("isOpen", r.isOpen)
                    put("ownerPhone", r.ownerPhone)
                    put("ownerName", r.ownerName)
                    put("userId", r.userId)
                    put("password", r.password)
                }
                array.put(obj)
            }
            prefs.edit().putString("custom_restaurants", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun persistCustomDishes(dishes: List<Dish>) {
        if (prefs == null) return
        try {
            val array = JSONArray()
            for (d in dishes) {
                val obj = JSONObject().apply {
                    put("id", d.id)
                    put("restaurantId", d.restaurantId)
                    put("restaurantName", d.restaurantName)
                    put("name", d.name)
                    put("description", d.description)
                    put("price", d.price)
                    if (d.originalPrice != null) put("originalPrice", d.originalPrice)
                    put("isVeg", d.isVeg)
                    put("rating", d.rating)
                    put("ratingCount", d.ratingCount)
                    put("category", d.category.name)
                    put("prepTimeMinutes", d.prepTimeMinutes)
                    put("isBestseller", d.isBestseller)
                    put("spicyLevel", d.spicyLevel)
                    put("portionSize", d.portionSize)
                }
                array.put(obj)
            }
            prefs.edit().putString("custom_dishes", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addRestaurant(restaurant: Restaurant) {
        val updated = listOf(restaurant) + _customRestaurants.value
        _customRestaurants.value = updated
        persistCustomRestaurants(updated)
    }

    suspend fun addDish(dish: Dish) {
        val updated = listOf(dish) + _customDishes.value
        _customDishes.value = updated
        persistCustomDishes(updated)
    }

    suspend fun deleteDish(dishId: String) {
        val updated = _customDishes.value.filter { it.id != dishId }
        _customDishes.value = updated
        persistCustomDishes(updated)
    }

    suspend fun deleteRestaurant(restaurantId: String) {
        val updatedRest = _customRestaurants.value.filter { it.id != restaurantId }
        val updatedDishes = _customDishes.value.filter { it.restaurantId != restaurantId }
        _customRestaurants.value = updatedRest
        _customDishes.value = updatedDishes
        persistCustomRestaurants(updatedRest)
        persistCustomDishes(updatedDishes)
    }

    // Categories Catalog
    fun getCategories(): List<FoodCategory> = listOf(
        FoodCategory("cat_1", FoodCategoryType.BIRYANI, "Biryani", "Dum & Handi Pots", "🔥 Popular"),
        FoodCategory("cat_2", FoodCategoryType.PIZZA_FAST_FOOD, "Pizza & Fast Food", "Cheesy & Crispy", "⚡ 20 Mins"),
        FoodCategory("cat_3", FoodCategoryType.INDIAN_THALI, "Indian Thali", "Grand Meals", "👑 Royal"),
        FoodCategory("cat_4", FoodCategoryType.DESSERTS, "Desserts/Sweets", "Mithai & Cakes", "✨ Sweet"),
        FoodCategory("cat_5", FoodCategoryType.STREET_FOOD, "Street Food", "Chaat & Pav Bhaji", "🌶️ Spicy"),
        FoodCategory("cat_6", FoodCategoryType.BEVERAGES, "Beverages", "Lassi & Shakes", "❄️ Chilled")
    )

    // Multi-Vendor Restaurants Catalog
    fun getRestaurants(): List<Restaurant> = _customRestaurants.value + listOf(
        Restaurant(
            id = "rest_1",
            name = "Behrouz Royal Biryani & Kebabs",
            cuisine = "Biryani, North Indian, Mughlai",
            rating = 4.8,
            reviewCount = "4.2k+",
            deliveryTimeMinutes = 25,
            priceForTwo = 450,
            offerText = "50% OFF up to ₹100 • Use KHAIBU50",
            distanceKm = 1.8,
            isPureVeg = false,
            address = "80 Feet Rd, Koramangala 4th Block",
            featuredTag = "Must Try",
            ownerName = "Naveen Khan",
            ownerPhone = "+91 98765 11001",
            userId = "behrouz_kitchen",
            password = "Royal@123"
        ),
        Restaurant(
            id = "rest_2",
            name = "Punjab Grill & Desi Rasoi",
            cuisine = "North Indian, Thali, Paneer Specials",
            rating = 4.6,
            reviewCount = "2.9k+",
            deliveryTimeMinutes = 30,
            priceForTwo = 380,
            offerText = "FREE Delivery on orders > ₹299",
            distanceKm = 2.4,
            isPureVeg = false,
            address = "Near Sony Signal, 100 Ft Road",
            featuredTag = "Top Rated",
            ownerName = "Harpreet Singh",
            ownerPhone = "+91 98765 11002",
            userId = "punjab_grill",
            password = "Grill@123"
        ),
        Restaurant(
            id = "rest_3",
            name = "Crust & Co. Artisan Pizzeria",
            cuisine = "Gourmet Pizzas, Burgers, Fast Food",
            rating = 4.7,
            reviewCount = "3.5k+",
            deliveryTimeMinutes = 20,
            priceForTwo = 320,
            offerText = "Flat ₹75 OFF on First Pizza",
            distanceKm = 1.2,
            isPureVeg = false,
            address = "5th Block, KHB Colony, Koramangala",
            featuredTag = "Super Fast",
            ownerName = "Marco Rossi",
            ownerPhone = "+91 98765 11003",
            userId = "crust_co",
            password = "Pizza@123"
        ),
        Restaurant(
            id = "rest_4",
            name = "Haldiram Sweets & Chaat Bhavan",
            cuisine = "Pure Veg, Desserts, Street Chaat, Mithai",
            rating = 4.5,
            reviewCount = "5.1k+",
            deliveryTimeMinutes = 18,
            priceForTwo = 200,
            offerText = "Buy 2 Get 1 FREE Sweets",
            distanceKm = 1.5,
            isPureVeg = true,
            address = "Commercial Complex, 1st Block",
            featuredTag = "Pure Veg",
            ownerName = "Ramesh Agarwal",
            ownerPhone = "+91 98765 11004",
            userId = "haldiram",
            password = "Sweet@123"
        ),
        Restaurant(
            id = "rest_5",
            name = "Mumbai Chowpatty Tadka",
            cuisine = "Pav Bhaji, Pani Puri, Street Snacks",
            rating = 4.4,
            reviewCount = "1.8k+",
            deliveryTimeMinutes = 22,
            priceForTwo = 180,
            offerText = "Flat 20% OFF on all Chaats",
            distanceKm = 2.1,
            isPureVeg = true,
            address = "Food Street, 6th Block",
            featuredTag = "Street Special",
            ownerName = "Santosh Patil",
            ownerPhone = "+91 98765 11005",
            userId = "mumbai_tadka",
            password = "Chaat@123"
        ),
        Restaurant(
            id = "rest_6",
            name = "Chai Shai & Beverage Hub",
            cuisine = "Kulhad Chai, Shakes, Refreshing Coolers",
            rating = 4.9,
            reviewCount = "6.4k+",
            deliveryTimeMinutes = 15,
            priceForTwo = 140,
            offerText = "FLAT ₹50 Cashback with UPI",
            distanceKm = 0.9,
            isPureVeg = true,
            address = "Near Oasis Mall, Koramangala",
            featuredTag = "Bestseller",
            ownerName = "Vikas Sharma",
            ownerPhone = "+91 98765 11006",
            userId = "chai_shai",
            password = "Chai@123"
        )
    )

    // Complete Dishes Catalog
    fun getAllDishes(): List<Dish> = _customDishes.value + listOf(
        // Biryani
        Dish(
            id = "dish_1",
            restaurantId = "rest_1",
            restaurantName = "Behrouz Royal Biryani & Kebabs",
            name = "Royal Dum Gosht Biryani",
            description = "Tender marinated meat slow-cooked in sealed clay handi with aged basmati rice, saffron, fried onions, and royal spices. Served with creamy mint raita.",
            price = 389.0,
            originalPrice = 450.0,
            isVeg = false,
            rating = 4.9,
            ratingCount = 1840,
            category = FoodCategoryType.BIRYANI,
            prepTimeMinutes = 25,
            isBestseller = true,
            spicyLevel = 2,
            portionSize = "Serves 1-2 (500g)"
        ),
        Dish(
            id = "dish_2",
            restaurantId = "rest_1",
            restaurantName = "Behrouz Royal Biryani & Kebabs",
            name = "Hyderabadi Chicken Dum Biryani",
            description = "Authentic Hyderabadi delicacy with succulent juicy chicken pieces, aromatic kewra essence, boiled egg, and spicy salan.",
            price = 329.0,
            originalPrice = 399.0,
            isVeg = false,
            rating = 4.8,
            ratingCount = 2410,
            category = FoodCategoryType.BIRYANI,
            prepTimeMinutes = 20,
            isBestseller = true,
            spicyLevel = 3,
            portionSize = "Serves 1-2 (550g)"
        ),
        Dish(
            id = "dish_3",
            restaurantId = "rest_1",
            restaurantName = "Behrouz Royal Biryani & Kebabs",
            name = "Shahi Paneer Tikka Biryani",
            description = "Charcoal roasted cottage cheese cubes layered with long grain basmati rice, caramelized shallots, mint, and saffron milk.",
            price = 289.0,
            originalPrice = 330.0,
            isVeg = true,
            rating = 4.7,
            ratingCount = 940,
            category = FoodCategoryType.BIRYANI,
            prepTimeMinutes = 20,
            isBestseller = false,
            spicyLevel = 2,
            portionSize = "Serves 1-2 (500g)"
        ),
        // Pizza & Fast Food
        Dish(
            id = "dish_4",
            restaurantId = "rest_3",
            restaurantName = "Crust & Co. Artisan Pizzeria",
            name = "Gourmet Cheese Burst Supreme",
            description = "Double molten mozzarella core, sun-dried tomatoes, black olives, jalapenos, and Italian herbs on freshly hand-stretched sourdough crust.",
            price = 349.0,
            originalPrice = 420.0,
            isVeg = true,
            rating = 4.8,
            ratingCount = 1520,
            category = FoodCategoryType.PIZZA_FAST_FOOD,
            prepTimeMinutes = 20,
            isBestseller = true,
            spicyLevel = 1,
            portionSize = "Medium 10 inch"
        ),
        Dish(
            id = "dish_5",
            restaurantId = "rest_3",
            restaurantName = "Crust & Co. Artisan Pizzeria",
            name = "Smokey BBQ Chicken Overload Pizza",
            description = "Tender pulled chicken tossed in hickory BBQ sauce, sweet corn, bell peppers, mozzarella, and chili flakes.",
            price = 399.0,
            originalPrice = 480.0,
            isVeg = false,
            rating = 4.7,
            ratingCount = 1190,
            category = FoodCategoryType.PIZZA_FAST_FOOD,
            prepTimeMinutes = 22,
            isBestseller = true,
            spicyLevel = 2,
            portionSize = "Medium 10 inch"
        ),
        Dish(
            id = "dish_6",
            restaurantId = "rest_3",
            restaurantName = "Crust & Co. Artisan Pizzeria",
            name = "Crispy Loaded Crunch Burger",
            description = "Double crispy spiced patty, melted cheddar cheese slice, fresh lettuce, secret tangy aioli, in toasted brioche bun.",
            price = 189.0,
            originalPrice = 220.0,
            isVeg = false,
            rating = 4.6,
            ratingCount = 820,
            category = FoodCategoryType.PIZZA_FAST_FOOD,
            prepTimeMinutes = 15,
            isBestseller = false,
            spicyLevel = 2,
            portionSize = "Single Heavy Burger"
        ),
        // Indian Thali
        Dish(
            id = "dish_7",
            restaurantId = "rest_2",
            restaurantName = "Punjab Grill & Desi Rasoi",
            name = "Royal Maharaja Grand Thali",
            description = "Dal Makhani, Paneer Butter Masala, Butter Chicken, Jeera Rice, 2 Butter Naan, Gulab Jamun, Papad, Raita & Pickle. Complete feast!",
            price = 399.0,
            originalPrice = 499.0,
            isVeg = false,
            rating = 4.9,
            ratingCount = 2890,
            category = FoodCategoryType.INDIAN_THALI,
            prepTimeMinutes = 25,
            isBestseller = true,
            spicyLevel = 2,
            portionSize = "Grand Meal for 1-2"
        ),
        Dish(
            id = "dish_8",
            restaurantId = "rest_2",
            restaurantName = "Punjab Grill & Desi Rasoi",
            name = "Deluxe Amritsari Veg Thali",
            description = "Rich Dal Makhani, Shahi Paneer, Pindi Chole, Steamed Basmati Rice, 2 Lachha Parathas, Sweet Kheer, and fresh salad.",
            price = 299.0,
            originalPrice = 360.0,
            isVeg = true,
            rating = 4.7,
            ratingCount = 1750,
            category = FoodCategoryType.INDIAN_THALI,
            prepTimeMinutes = 20,
            isBestseller = true,
            spicyLevel = 2,
            portionSize = "Complete Meal for 1"
        ),
        // Desserts / Sweets
        Dish(
            id = "dish_9",
            restaurantId = "rest_4",
            restaurantName = "Haldiram Sweets & Chaat Bhavan",
            name = "Warm Gulab Jamun with Creamy Rabri",
            description = "Soft melt-in-mouth khoya gulab jamuns dunked in cardamom sugar syrup, topped with thick kesar pistachio rabri (2 pcs).",
            price = 149.0,
            originalPrice = 180.0,
            isVeg = true,
            rating = 4.9,
            ratingCount = 3100,
            category = FoodCategoryType.DESSERTS,
            prepTimeMinutes = 10,
            isBestseller = true,
            spicyLevel = 0,
            portionSize = "2 Large Pcs + Rabri"
        ),
        Dish(
            id = "dish_10",
            restaurantId = "rest_4",
            restaurantName = "Haldiram Sweets & Chaat Bhavan",
            name = "Kesar Royal Rasmalai",
            description = "Spongy cottage cheese patties immersed in chilled clotted saffron milk, garnished with toasted almonds and pistachios (2 pcs).",
            price = 169.0,
            originalPrice = 200.0,
            isVeg = true,
            rating = 4.8,
            ratingCount = 2140,
            category = FoodCategoryType.DESSERTS,
            prepTimeMinutes = 10,
            isBestseller = true,
            spicyLevel = 0,
            portionSize = "2 Pcs"
        ),
        // Street Food
        Dish(
            id = "dish_11",
            restaurantId = "rest_5",
            restaurantName = "Mumbai Chowpatty Tadka",
            name = "Special Butter Pav Bhaji Platter",
            description = "Spiced mashed vegetable gravy cooked in dollops of Amul butter on iron tawa, served with 2 toasted pavs, sliced onions, and lemon wedges.",
            price = 169.0,
            originalPrice = 199.0,
            isVeg = true,
            rating = 4.7,
            ratingCount = 2490,
            category = FoodCategoryType.STREET_FOOD,
            prepTimeMinutes = 15,
            isBestseller = true,
            spicyLevel = 2,
            portionSize = "Serves 1 (Bhaji + 2 Pav)"
        ),
        Dish(
            id = "dish_12",
            restaurantId = "rest_5",
            restaurantName = "Mumbai Chowpatty Tadka",
            name = "Dahi Papdi Chaat Royale",
            description = "Crisp flour crisps loaded with boiled potatoes, spiced chickpeas, chilled sweetened curd, tangy tamarind chutney, and crunchy sev.",
            price = 129.0,
            originalPrice = 150.0,
            isVeg = true,
            rating = 4.6,
            ratingCount = 1630,
            category = FoodCategoryType.STREET_FOOD,
            prepTimeMinutes = 12,
            isBestseller = false,
            spicyLevel = 2,
            portionSize = "Full Plate"
        ),
        // Beverages
        Dish(
            id = "dish_13",
            restaurantId = "rest_6",
            restaurantName = "Chai Shai & Beverage Hub",
            name = "Royal Kulhad Alphonso Mango Lassi",
            description = "Rich and thick churned yogurt blended with authentic Ratnagiri Alphonso mango pulp, saffron strands, and crushed dry fruits in terracotta cup.",
            price = 119.0,
            originalPrice = 140.0,
            isVeg = true,
            rating = 4.9,
            ratingCount = 3890,
            category = FoodCategoryType.BEVERAGES,
            prepTimeMinutes = 8,
            isBestseller = true,
            spicyLevel = 0,
            portionSize = "350 ml"
        ),
        Dish(
            id = "dish_14",
            restaurantId = "rest_6",
            restaurantName = "Chai Shai & Beverage Hub",
            name = "Adrak Elaichi Masala Chai Flask",
            description = "Slow-brewed Assam tea leaves with crushed fresh ginger, green cardamom, and fresh milk. Delivered piping hot in thermal flask.",
            price = 99.0,
            originalPrice = 120.0,
            isVeg = true,
            rating = 4.8,
            ratingCount = 2780,
            category = FoodCategoryType.BEVERAGES,
            prepTimeMinutes = 10,
            isBestseller = true,
            spicyLevel = 1,
            portionSize = "250 ml (Serves 2)"
        )
    )

    // Promo Coupons
    fun getPromoCoupons(): List<PromoCoupon> = listOf(
        PromoCoupon(
            code = "FIRST20",
            discountPercent = 40,
            maxDiscount = 120.0,
            minOrder = 199.0,
            title = "First 20 Users Special: 40% OFF",
            description = "Exclusive limited offer! Only first 20 foodies can claim.",
            maxUses = 20,
            usedCount = 7, // 7 already claimed, 13 left
            expiryTimestamp = System.currentTimeMillis() + (24 * 3600 * 1000L) // 24 hours validity
        ),
        PromoCoupon(
            code = "FLASH2HR",
            discountPercent = 60,
            maxDiscount = 100.0,
            minOrder = 149.0,
            title = "Flash Deal: 60% OFF",
            description = "Super fast meal offer! Valid for next 2 hours only.",
            maxUses = 30,
            usedCount = 14,
            expiryTimestamp = System.currentTimeMillis() + (2 * 3600 * 1000L) // 2 hours validity
        ),
        PromoCoupon(
            code = "KHAIBU50",
            discountPercent = 50,
            maxDiscount = 150.0,
            minOrder = 249.0,
            title = "50% OFF on your order",
            description = "Get 50% discount up to ₹150 on orders above ₹249"
        ),
        PromoCoupon(
            code = "FREEDEL",
            discountPercent = 100,
            maxDiscount = 35.0,
            minOrder = 199.0,
            title = "Zero Delivery Fee",
            description = "Free delivery on all orders above ₹199"
        ),
        PromoCoupon(
            code = "FEAST100",
            discountPercent = 30,
            maxDiscount = 100.0,
            minOrder = 399.0,
            title = "Flat ₹100 Discount",
            description = "Celebrate with family & save ₹100 on orders > ₹399"
        )
    )

    // FAQ items
    fun getFaqs(): List<FaqItem> = listOf(
        FaqItem(
            "faq_1",
            "How does real-time order tracking work?",
            "Once the restaurant accepts your order, you can follow your meal's preparation in the kitchen and watch the live delivery partner progress right to your doorstep on the live tracking screen."
        ),
        FaqItem(
            "faq_2",
            "What payment options are supported?",
            "Khaibu supports all major UPI apps (Google Pay, PhonePe, Paytm, BHIM), Cash on Delivery (COD), and Credit/Debit Cards with 100% bank-grade encryption."
        ),
        FaqItem(
            "faq_3",
            "Can I cancel or modify my food order?",
            "You can cancel your order within 60 seconds of placing it before the restaurant begins preparation. Tap the Help Desk button or call our 24/7 hotline."
        ),
        FaqItem(
            "faq_4",
            "How do I switch to the Restaurant Kitchen Portal?",
            "Tap the chef hat icon or the 'Kitchen Portal' tab in bottom navigation to view incoming live orders and update preparation states in real-time."
        )
    )

    // Default Notifications
    fun getInitialNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "notif_1",
            title = "🎉 Welcome to Khaibu!",
            message = "Enjoy 50% OFF on your first 3 orders using coupon code KHAIBU50.",
            time = "Just now"
        ),
        NotificationItem(
            id = "notif_2",
            title = "🍛 Biryani Feast Alert",
            message = "Get complimentary Gulab Jamun with Behrouz Royal Biryani orders today!",
            time = "1 hour ago"
        )
    )
}
