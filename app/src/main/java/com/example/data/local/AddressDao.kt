package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM saved_addresses ORDER BY isDefault DESC, id DESC")
    fun getAllAddresses(): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity): Long

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("UPDATE saved_addresses SET isDefault = 0")
    suspend fun resetDefaults()

    @Query("UPDATE saved_addresses SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultAddress(id: Long)
}
