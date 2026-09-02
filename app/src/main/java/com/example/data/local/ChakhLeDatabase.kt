package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.AddressEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus

class Converters {
    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = try {
        OrderStatus.valueOf(value)
    } catch (e: Exception) {
        OrderStatus.PLACED
    }
}

@Database(
    entities = [CartItemEntity::class, OrderEntity::class, AddressEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChakhLeDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun addressDao(): AddressDao

    companion object {
        @Volatile
        private var INSTANCE: ChakhLeDatabase? = null

        fun getInstance(context: Context): ChakhLeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChakhLeDatabase::class.java,
                    "chakhle_food_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
