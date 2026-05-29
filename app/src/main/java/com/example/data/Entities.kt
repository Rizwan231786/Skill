package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_users")
data class LocalUser(
    @PrimaryKey val id: Int = 1,
    val selectedRole: String = "CLIENT", // "CLIENT" or "FREELANCER"
    val fullName: String = "Muhammad Ali",
    val email: String = "ali.freelancer@gmail.com",
    val phone: String = "+923001234567",
    val cnicNumber: String = "35201-1234567-3",
    val cnicUploaded: Boolean = true,
    val isVerified: Boolean = true,
    val bio: String = "Expert Mobile Developer tailoring custom Jetpack Compose Android applications in lahore.",
    val skills: String = "Android, Kotlin, Jetpack Compose, Retrofit, RoomDB",
    val hourlyRate: Double = 15.0,
    val portfolioUrls: String = "github.com/mali-dev, dribbble.com/mali-design",
    val accountBalance: Double = 25000.0, // PKR
    val pendingEscrow: Double = 12000.0, // PKR
    val avatarUrl: String = ""
)

@Entity(tableName = "gigs")
data class Gig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val freelancerId: Int,
    val freelancerName: String,
    val freelancerRating: Float = 4.9f,
    val title: String,
    val description: String,
    val price: Double, // PKR
    val deliveryTimeDays: Int,
    val category: String,
    val coverImage: String = ""
)

@Entity(tableName = "job_posts")
data class JobPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val clientName: String,
    val title: String,
    val description: String,
    val budget: Double, // PKR
    val deadlineDays: Int,
    val bidsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val clientName: String,
    val freelancerId: Int,
    val freelancerName: String,
    val title: String,
    val price: Double, // PKR
    val status: String, // "ESCROW_LOCKED", "SUBMITTED", "COMPLETED", "DISPUTED", "REFUNDED"
    val deliverablesText: String = "",
    val escrowStatus: String = "Locked", // "Locked", "Released", "Disputed", "Refunded"
    val rating: Float = 0f,
    val review: String = "",
    val disputeReason: String = "",
    val paymentMethod: String = "JazzCash", // "JazzCash", "Easypaisa", "Bank Transfer"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val senderName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentName: String? = null,
    val attachmentSize: String? = null
)
