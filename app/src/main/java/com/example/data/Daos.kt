package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillLinkDao {
    // Local User operations
    @Query("SELECT * FROM local_users WHERE id = 1 LIMIT 1")
    fun getLocalUser(): Flow<LocalUser?>

    @Query("SELECT * FROM local_users WHERE id = 1 LIMIT 1")
    suspend fun getLocalUserSync(): LocalUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalUser(user: LocalUser)

    // Gig operations
    @Query("SELECT * FROM gigs ORDER BY id DESC")
    fun getAllGigs(): Flow<List<Gig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGig(gig: Gig)

    @Query("DELETE FROM gigs WHERE id = :id")
    suspend fun deleteGigById(id: Int)

    // Job Post operations
    @Query("SELECT * FROM job_posts ORDER BY timestamp DESC")
    fun getAllJobPosts(): Flow<List<JobPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobPost(post: JobPost)

    // Order operations
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Int): Flow<Order?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    // Chat operations
    @Query("SELECT * FROM chat_messages WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getChatMessagesForOrder(orderId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)
}
