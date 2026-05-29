package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SkillLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SkillLinkRepository(db.dao())

    // UI States
    val localUser: StateFlow<LocalUser?> = repository.localUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val gigs: StateFlow<List<Gig>> = repository.allGigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobPosts: StateFlow<List<JobPost>> = repository.allJobPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selections & Active Streams
    private val _selectedOrderId = MutableStateFlow<Int?>(null)
    val selectedOrderId = _selectedOrderId.asStateFlow()

    val selectedOrder: StateFlow<Order?> = _selectedOrderId
        .flatMapLatest { id ->
            if (id != null) repository.getOrderById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatMessages: StateFlow<List<ChatMessage>> = _selectedOrderId
        .flatMapLatest { id ->
            if (id != null) repository.getChatMessagesForOrder(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini API states
    private val _loadingSuggestions = MutableStateFlow(false)
    val loadingSuggestions = _loadingSuggestions.asStateFlow()

    private val _aiSuggestions = MutableStateFlow("")
    val aiSuggestions = _aiSuggestions.asStateFlow()

    private val _loadingJobDesc = MutableStateFlow(false)
    val loadingJobDesc = _loadingJobDesc.asStateFlow()

    private val _aiGeneratedJobDesc = MutableStateFlow("")
    val aiGeneratedJobDesc = _aiGeneratedJobDesc.asStateFlow()

    // Transaction / General Actions progress
    private val _actionProgress = MutableStateFlow<String?>(null)
    val actionProgress = _actionProgress.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.initializeSeedData()
            }
        }
    }

    fun selectOrder(orderId: Int?) {
        _selectedOrderId.value = orderId
    }

    fun toggleRole() {
        viewModelScope.launch {
            val currentUser = localUser.value ?: return@launch
            val nextRole = if (currentUser.selectedRole == "CLIENT") "FREELANCER" else "CLIENT"
            val updatedUser = currentUser.copy(selectedRole = nextRole)
            withContext(Dispatchers.IO) {
                repository.saveLocalUser(updatedUser)
            }
        }
    }

    fun updateProfile(fullName: String, bio: String, skills: String, rate: Double, portfolio: String, cnic: String) {
        viewModelScope.launch {
            val currentUser = localUser.value ?: return@launch
            val updatedUser = currentUser.copy(
                fullName = fullName,
                bio = bio,
                skills = skills,
                hourlyRate = rate,
                portfolioUrls = portfolio,
                cnicNumber = cnic,
                cnicUploaded = cnic.isNotEmpty()
            )
            withContext(Dispatchers.IO) {
                repository.saveLocalUser(updatedUser)
            }
        }
    }

    fun simulateCnicUpload(cnic: String) {
        viewModelScope.launch {
            val currentUser = localUser.value ?: return@launch
            val updatedUser = currentUser.copy(
                cnicNumber = cnic,
                cnicUploaded = true,
                isVerified = false // pending admin check in real life, but we will auto verify in 3 seconds to delight!
            )
            withContext(Dispatchers.IO) {
                repository.saveLocalUser(updatedUser)
            }
            
            // Auto Verify mock in 3 seconds
            _actionProgress.value = "CNIC Document is being reviewed via Smart local OCR..."
            kotlinx.coroutines.delay(2500)
            val freshUser = localUser.value ?: return@launch
            withContext(Dispatchers.IO) {
                repository.saveLocalUser(freshUser.copy(isVerified = true))
            }
            _actionProgress.value = "Identity Successfully Verified! CNIC Badge Added."
            kotlinx.coroutines.delay(1500)
            _actionProgress.value = null
        }
    }

    fun postJob(title: String, description: String, budget: Double, deadlineDays: Int) {
        viewModelScope.launch {
            _actionProgress.value = "Posting job to Pakistani network..."
            val user = localUser.value ?: return@launch
            val job = JobPost(
                clientId = user.id,
                clientName = user.fullName,
                title = title,
                description = description,
                budget = budget,
                deadlineDays = deadlineDays
            )
            withContext(Dispatchers.IO) {
                repository.createJobPost(job)
            }
            kotlinx.coroutines.delay(1000)
            _actionProgress.value = "Job Posted successfully!"
            kotlinx.coroutines.delay(1200)
            _actionProgress.value = null
        }
    }

    fun addServicelisting(title: String, description: String, price: Double, deliveryDays: Int, category: String) {
        viewModelScope.launch {
            _actionProgress.value = "Publishing Gig listing..."
            val user = localUser.value ?: return@launch
            val gig = Gig(
                freelancerId = user.id,
                freelancerName = user.fullName,
                freelancerRating = 5.0f,
                title = title,
                description = description,
                price = price,
                deliveryTimeDays = deliveryDays,
                category = category
            )
            withContext(Dispatchers.IO) {
                repository.createGig(gig)
            }
            kotlinx.coroutines.delay(1000)
            _actionProgress.value = "Service listing published!"
            kotlinx.coroutines.delay(1200)
            _actionProgress.value = null
        }
    }

    fun hireFreelancer(gig: Gig, selectedPayment: String) {
        viewModelScope.launch {
            val user = localUser.value ?: return@launch
            _actionProgress.value = "Processing $selectedPayment Escrow Locker deposit of PKR ${String.format("%,.0f", gig.price)}/-"
            kotlinx.coroutines.delay(1800)
            
            val order = Order(
                clientId = user.id,
                clientName = user.fullName,
                freelancerId = gig.freelancerId,
                freelancerName = gig.freelancerName,
                title = gig.title,
                price = gig.price,
                status = "ESCROW_LOCKED",
                escrowStatus = "Locked",
                paymentMethod = selectedPayment
            )
            
            val newOrderId = withContext(Dispatchers.IO) {
                repository.createOrder(order)
            }
            
            // Add automated initialization chat
            withContext(Dispatchers.IO) {
                repository.sendChatMessage(ChatMessage(
                    orderId = newOrderId,
                    senderName = "SkillLink Escrow System",
                    messageText = "🔔 DEPOSIT CONFIRMED: PKR ${String.format("%,.0f", gig.price)}/- has been locked securely in Escrow. Work can now safely begin. Freelancer has been notified."
                ))
            }
            
            _actionProgress.value = "Escrow locked! Chat and order initialized."
            kotlinx.coroutines.delay(1500)
            _actionProgress.value = null
            
            // Select the newly created order
            selectOrder(newOrderId)
        }
    }

    fun uploadDeliverables(order: Order, deliverablesText: String) {
        viewModelScope.launch {
            _actionProgress.value = "Submitting deliverables..."
            val updated = order.copy(
                status = "SUBMITTED",
                deliverablesText = deliverablesText
            )
            withContext(Dispatchers.IO) {
                repository.updateOrder(updated)
                repository.sendChatMessage(ChatMessage(
                    orderId = order.id,
                    senderName = order.freelancerName,
                    messageText = "📤 WORK SUBMITTED: \"$deliverablesText\""
                ))
                repository.sendChatMessage(ChatMessage(
                    orderId = order.id,
                    senderName = "SkillLink Escrow System",
                    messageText = "⏳ Alert: Deliverable uploaded! Client has 3 days to approve or request revision. If unsatisfied, Client can raise a dispute."
                ))
            }
            kotlinx.coroutines.delay(1000)
            _actionProgress.value = "Deliverables submitted!"
            kotlinx.coroutines.delay(1200)
            _actionProgress.value = null
        }
    }

    fun approveWork(order: Order, customRating: Float, customReview: String) {
        viewModelScope.launch {
            _actionProgress.value = "Releasing Escrowed funds..."
            kotlinx.coroutines.delay(1500)
            
            val updated = order.copy(
                status = "COMPLETED",
                escrowStatus = "Released",
                rating = customRating,
                review = customReview
            )
            
            withContext(Dispatchers.IO) {
                repository.updateOrder(updated)
                repository.sendChatMessage(ChatMessage(
                    orderId = order.id,
                    senderName = "SkillLink Escrow System",
                    messageText = "🟢 ORDER COMPLETED: Escrow funds released to ${order.freelancerName} (minus 8% framework fee). Feedback left: $customRating Star${if (customRating > 1) "s" else ""}."
                ))
            }

            // Simulate updating freelancer balance
            val currentUser = localUser.value
            if (currentUser != null) {
                if (currentUser.id == order.freelancerId) {
                    val feeFactor = 0.92 // 8% fee
                    val finalEarning = order.price * feeFactor
                    val updatedUser = currentUser.copy(
                        accountBalance = currentUser.accountBalance + finalEarning,
                        pendingEscrow = maxOf(0.0, currentUser.pendingEscrow - order.price)
                    )
                    withContext(Dispatchers.IO) {
                        repository.saveLocalUser(updatedUser)
                    }
                }
            }

            _actionProgress.value = "Funds successfully released: Order complete!"
            kotlinx.coroutines.delay(1500)
            _actionProgress.value = null
        }
    }

    fun disputeWork(order: Order, reason: String) {
        viewModelScope.launch {
            _actionProgress.value = "Filing dispute with mediation team..."
            kotlinx.coroutines.delay(1500)
            
            val updated = order.copy(
                status = "DISPUTED",
                escrowStatus = "Disputed",
                disputeReason = reason
            )
            
            withContext(Dispatchers.IO) {
                repository.updateOrder(updated)
                repository.sendChatMessage(ChatMessage(
                    orderId = order.id,
                    senderName = "SkillLink Escrow System",
                    messageText = "🚨 DISPUTE RAISED: User requested mediation. Reason: \"$reason\". Funds are frozen. Support SLA ensures resolution within 72 hours."
                ))
            }
            
            _actionProgress.value = "Meditation dispute initialized!"
            kotlinx.coroutines.delay(1200)
            _actionProgress.value = null
        }
    }

    fun requestWithdrawal(amount: Double, method: String, accountDetails: String) {
        viewModelScope.launch {
            val currentUser = localUser.value ?: return@launch
            if (amount > currentUser.accountBalance) {
                _actionProgress.value = "Ghalti: Account balance se zyada rayeem dakhil ki gai hai."
                kotlinx.coroutines.delay(1500)
                _actionProgress.value = null
                return@launch
            }

            _actionProgress.value = "Connecting to $method withdraw API..."
            kotlinx.coroutines.delay(2000)

            val updatedUser = currentUser.copy(
                accountBalance = currentUser.accountBalance - amount
            )

            withContext(Dispatchers.IO) {
                repository.saveLocalUser(updatedUser)
            }

            _actionProgress.value = "Transfer Done! Rs. ${String.format("%,.0f", amount)}/- successfully sent to $accountDetails."
            kotlinx.coroutines.delay(2500)
            _actionProgress.value = null
        }
    }

    fun sendChat(text: String, attachmentName: String? = null, attachmentSize: String? = null) {
        val user = localUser.value ?: return
        val orderId = selectedOrderId.value ?: return
        viewModelScope.launch {
            val msg = ChatMessage(
                orderId = orderId,
                senderName = user.fullName,
                messageText = text,
                attachmentName = attachmentName,
                attachmentSize = attachmentSize
            )
            withContext(Dispatchers.IO) {
                repository.sendChatMessage(msg)
            }
        }
    }

    fun fetchAiSuggestions() {
        val user = localUser.value ?: return
        viewModelScope.launch {
            _loadingSuggestions.value = true
            _aiSuggestions.value = ""
            try {
                val response = GeminiService.getOptimizeProfileSuggestions(user.skills, user.bio)
                _aiSuggestions.value = response
            } catch (e: Exception) {
                _aiSuggestions.value = "Error compiling suggestions. Code error."
            } finally {
                _loadingSuggestions.value = false
            }
        }
    }

    fun clearSuggestions() {
        _aiSuggestions.value = ""
    }

    fun fetchAiJobDescription(title: String, budget: Double, skills: String) {
        viewModelScope.launch {
            _loadingJobDesc.value = true
            _aiGeneratedJobDesc.value = ""
            try {
                val result = GeminiService.generateJobDescription(title, budget, skills)
                _aiGeneratedJobDesc.value = result
            } catch (e: Exception) {
                _aiGeneratedJobDesc.value = "AI generation temporarily paused. Using localized template."
            } finally {
                _loadingJobDesc.value = false
            }
        }
    }

    fun clearGeneratedJobDesc() {
        _aiGeneratedJobDesc.value = ""
    }
}
