package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SkillLinkRepository(private val dao: SkillLinkDao) {

    val localUser: Flow<LocalUser?> = dao.getLocalUser()
    val allGigs: Flow<List<Gig>> = dao.getAllGigs()
    val allJobPosts: Flow<List<JobPost>> = dao.getAllJobPosts()
    val allOrders: Flow<List<Order>> = dao.getAllOrders()

    fun getOrderById(orderId: Int): Flow<Order?> = dao.getOrderById(orderId)
    fun getChatMessagesForOrder(orderId: Int): Flow<List<ChatMessage>> = dao.getChatMessagesForOrder(orderId)

    suspend fun saveLocalUser(user: LocalUser) {
        dao.insertLocalUser(user)
    }

    suspend fun createGig(gig: Gig) {
        dao.insertGig(gig)
    }

    suspend fun createJobPost(post: JobPost) {
        dao.insertJobPost(post)
    }

    suspend fun createOrder(order: Order): Int {
        dao.insertOrder(order)
        // Find the inserted order to get its auto-generated ID
        val latest = dao.getAllOrders().firstOrNull()?.firstOrNull()
        return latest?.id ?: 1
    }

    suspend fun updateOrder(order: Order) {
        dao.insertOrder(order)
    }

    suspend fun sendChatMessage(message: ChatMessage) {
        dao.insertChatMessage(message)
    }

    suspend fun initializeSeedData() {
        // Only seed if empty
        val existingUser = dao.getLocalUserSync()
        if (existingUser == null) {
            // Seed current user
            dao.insertLocalUser(LocalUser())

            // Seed Gigs (Freelancers)
            dao.insertGig(Gig(
                freelancerId = 101,
                freelancerName = "Zara Ahmed",
                freelancerRating = 4.9f,
                title = "I will design high-converting UI/UX screens in Figma",
                description = "Salam! I am Zara, a professional UI/UX designer based in Karachi. I specialize in clean, accessible mobile and web designs prioritizing intuitive user journeys and modern branding rules.",
                price = 12000.0,
                deliveryTimeDays = 3,
                category = "Design"
            ))

            dao.insertGig(Gig(
                freelancerId = 102,
                freelancerName = "Kamran Khan",
                freelancerRating = 4.8f,
                title = "I will build responsive Android Jetpack Compose Apps",
                description = "Expert Android Developer from Lahore. I build super fast, fully offline first mobile applications with modern Room database integrations, Retrofit REST APIs, and Material 3 design systems.",
                price = 35000.0,
                deliveryTimeDays = 10,
                category = "Mobile Dev"
            ))

            dao.insertGig(Gig(
                freelancerId = 103,
                freelancerName = "Ayesha Noor",
                freelancerRating = 4.7f,
                title = "I will write SEO content and blogs in Urdu and English",
                description = "Professional writer ready to craft viral ranking blogs, detailed product reviews, or app store optimization text. High keyword research density and zero AI plagiarism.",
                price = 4500.0,
                deliveryTimeDays = 2,
                category = "Writing"
            ))

            dao.insertGig(Gig(
                freelancerId = 104,
                freelancerName = "Bilal Shah",
                freelancerRating = 5.0f,
                title = "I will secure your Node.js and PostgreSQL backend APIs",
                description = "Backend security auditor from Islamabad. I configure Firebase database security protocols, JWT token refresh services, Rate Limiting, and S3 asset uploads with complete security logic.",
                price = 28000.0,
                deliveryTimeDays = 5,
                category = "Backend"
            ))

            // Seed Job Posts (Clients)
            dao.insertJobPost(JobPost(
                clientId = 201,
                clientName = "Asim Qureshi",
                title = "Need an E-commerce App clone in Jetpack Compose",
                description = "Looking for a seasoned Pakistani developer to assemble a pixel-perfect clone of local shopping apps. Must use clean MVVM architecture, type-safe state-routing, local offline caching, and JazzCash/Easypaisa sandbox simulations.",
                budget = 65000.0,
                deadlineDays = 14,
                bidsCount = 4
            ))

            dao.insertJobPost(JobPost(
                clientId = 202,
                clientName = "Fatima Malik",
                title = "Custom Brand Identity and Logo Design for Lahore Cafe",
                description = "We are launching a specialized artisanal chai café in Gulberg. Looking for a modern, minimal brand logo, custom menu layouts, packaging styles, and high Resolution social media assets.",
                budget = 15000.0,
                deadlineDays = 5,
                bidsCount = 7
            ))

            dao.insertJobPost(JobPost(
                clientId = 203,
                clientName = "Zainab Shah",
                title = "Urgent connection timeout bug fix in Express backend",
                description = "Our mobile clients are randomly experiencing 504 gateway timeouts on heavy list endpoints. Need a backend guru to inspect database index configurations, query structures, and implement local caching in Redis.",
                budget = 10000.0,
                deadlineDays = 2,
                bidsCount = 3
            ))

            // Seed order with chat history
            val orderId = createOrder(Order(
                clientId = 1, // Current user as client in sample
                clientName = "Muhammad Ali",
                freelancerId = 102,
                freelancerName = "Kamran Khan",
                title = "Build responsive Android Jetpack Compose Apps",
                price = 35000.0,
                status = "ESCROW_LOCKED", 
                escrowStatus = "Locked",
                paymentMethod = "Easypaisa"
            ))

            dao.insertChatMessage(ChatMessage(
                orderId = orderId,
                senderName = "Muhammad Ali",
                messageText = "Assalam-o-Alaikum Kamran, main ne detail guide and UI wireframes leak leak share kar diye hain. Please confirm karain k kiya funds escrow mei lock ho gaye hain?"
            ))

            dao.insertChatMessage(ChatMessage(
                orderId = orderId,
                senderName = "Kamran Khan",
                messageText = "Walaikum-Assalam Ali bhai! Haan jee, mujhe SkillLink confirmation notification mil gaya hai ke Rs. 35,000/- successfully escrow mein safe hain! Main kaam shuru kar raha hoon aur kal tak basic UI review share karunga."
            ))
        }
    }
}
