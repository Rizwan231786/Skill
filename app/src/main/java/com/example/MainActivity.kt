package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChatMessage
import com.example.data.Gig
import com.example.data.JobPost
import com.example.data.Order
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SkillLinkViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SkillLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SkillLinkApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillLinkApp(viewModel: SkillLinkViewModel) {
    val localUser by viewModel.localUser.collectAsStateWithLifecycle()
    val actionProgress by viewModel.actionProgress.collectAsStateWithLifecycle()
    
    var currentTab by remember { mutableStateOf("explore") } // "explore", "create", "orders", "profile"
    
    var selectedGigDetail by remember { mutableStateOf<Gig?>(null) }
    var selectedJobDetail by remember { mutableStateOf<JobPost?>(null) }
    var showHireDialog by remember { mutableStateOf<Gig?>(null) }
    
    val userRole = localUser?.selectedRole ?: "CLIENT"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SkillLink",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { viewModel.toggleRole() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (userRole == "CLIENT") Icons.Default.SwitchAccount else Icons.Default.Engineering,
                            contentDescription = "Switch Role",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userRole == "CLIENT") "Client Mode" else "Freelancer Mode",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "explore",
                    onClick = { currentTab = "explore" },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = currentTab == "create",
                    onClick = { currentTab = "create" },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Create") },
                    label = { Text("Publish") }
                )
                NavigationBarItem(
                    selected = currentTab == "orders",
                    onClick = { currentTab = "orders" },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Orders") },
                    label = { Text("Escrows") }
                )
                NavigationBarItem(
                    selected = currentTab == "profile",
                    onClick = { currentTab = "profile" },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Cabinet") }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "MainTabs"
            ) { tab ->
                when (tab) {
                    "explore" -> ExploreScreen(
                        viewModel = viewModel,
                        userRole = userRole,
                        onGigClick = { selectedGigDetail = it },
                        onJobClick = { selectedJobDetail = it }
                    )
                    "create" -> CreateScreen(
                        viewModel = viewModel,
                        userRole = userRole
                    )
                    "orders" -> OrdersScreen(
                        viewModel = viewModel,
                        userRole = userRole
                    )
                    "profile" -> ProfileScreen(
                        viewModel = viewModel
                    )
                }
            }

            actionProgress?.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(20.dp),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            selectedGigDetail?.let { gig ->
                GigDetailSheet(
                    gig = gig,
                    onDismiss = { selectedGigDetail = null },
                    onHire = {
                        selectedGigDetail = null
                        showHireDialog = gig
                    }
                )
            }

            selectedJobDetail?.let { job ->
                JobDetailSheet(
                    job = job,
                    onDismiss = { selectedJobDetail = null },
                    onBid = { selectedJobDetail = null }
                )
            }

            showHireDialog?.let { gig ->
                HireEscrowDialog(
                    gig = gig,
                    onDismiss = { showHireDialog = null },
                    onConfirmHire = { paymentMethod ->
                        showHireDialog = null
                        viewModel.hireFreelancer(gig, paymentMethod)
                        currentTab = "orders"
                    }
                )
            }
        }
    }
}

// ---------------- EXPLORE SCREEN ----------------
@Composable
fun ExploreScreen(
    viewModel: SkillLinkViewModel,
    userRole: String,
    onGigClick: (Gig) -> Unit,
    onJobClick: (JobPost) -> Unit
) {
    val gigs by viewModel.gigs.collectAsStateWithLifecycle()
    val jobPosts by viewModel.jobPosts.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All", "Design", "Mobile Dev", "Writing", "Backend")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (userRole == "CLIENT") "Hire Pakistani Talent" else "Explore Pakistani Gigs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Escrow Secured • JazzCash & Easypaisa Integration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search services, skills, keywords...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (userRole == "CLIENT") {
            val filteredGigs = gigs.filter {
                (selectedCategory == "All" || it.category == selectedCategory) &&
                (it.title.contains(searchQuery, ignoreCase = true) || 
                 it.description.contains(searchQuery, ignoreCase = true) ||
                 it.freelancerName.contains(searchQuery, ignoreCase = true))
            }

            if (filteredGigs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No services found in this category.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredGigs) { gig ->
                        GigCard(gig = gig, onClick = { onGigClick(gig) })
                    }
                }
            }
        } else {
            val filteredJobs = jobPosts.filter {
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.description.contains(searchQuery, ignoreCase = true)
            }

            if (filteredJobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No posted projects found matching search query.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredJobs) { job ->
                        JobPostCard(job = job, onClick = { onJobClick(job) })
                    }
                }
            }
        }
    }
}

@Composable
fun GigCard(gig: Gig, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = gig.freelancerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${gig.freelancerRating}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = gig.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(gig.category) }
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text("Budget", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "Rs. ${String.format("%,.0f", gig.price)}",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun JobPostCard(job: JobPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Posted by ${job.clientName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${job.deadlineDays} Days left",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = job.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = job.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Bids",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${job.bidsCount} Proposals",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "Rs. ${String.format("%,.0f", job.budget)}",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// ---------------- CREATE SCREEN ----------------
@Composable
fun CreateScreen(viewModel: SkillLinkViewModel, userRole: String) {
    val aiGeneratedDesc by viewModel.aiGeneratedJobDesc.collectAsStateWithLifecycle()
    val loadingJobDesc by viewModel.loadingJobDesc.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (userRole == "CLIENT") "Post a New Project" else "List a Service (Gig)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Reaches instantly across Pakistan's verified users",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (userRole == "CLIENT") {
            var title by remember { mutableStateOf("") }
            var budgetText by remember { mutableStateOf("") }
            var skillsNeeded by remember { mutableStateOf("") }
            var durationText by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }

            LaunchedEffect(aiGeneratedDesc) {
                if (aiGeneratedDesc.isNotEmpty()) {
                    description = aiGeneratedDesc
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What needs to be done? (Project Title)") },
                placeholder = { Text("e.g. Build an Urdu Language Tutor App") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                label = { Text("Estimated Budget (PKR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("e.g. 25000") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = skillsNeeded,
                onValueChange = { skillsNeeded = it },
                label = { Text("Required Skills (Comma separated)") },
                placeholder = { Text("e.g. Kotlin, Compose, Firebase") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = durationText,
                onValueChange = { durationText = it },
                label = { Text("Completion Deadline (Days)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("e.g. 7") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generate Professional Pitch with AI",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        IconButton(
                            onClick = {
                                if (title.isNotEmpty()) {
                                    viewModel.fetchAiJobDescription(
                                        title = title,
                                        budget = budgetText.toDoubleOrNull() ?: 10000.0,
                                        skills = skillsNeeded
                                    )
                                }
                            },
                            enabled = !loadingJobDesc
                        ) {
                            if (loadingJobDesc) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Generate",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Text(
                        text = "Provides outline, milestone schedules, and specialized deliverables instantly using Gemini AI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Requirements") },
                placeholder = { Text("Provide complete details about deliverables, API integrations, and code delivery standards.") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val budget = budgetText.toDoubleOrNull() ?: 0.0
                    val days = durationText.toIntOrNull() ?: 3
                    if (title.isNotEmpty() && budget > 0 && description.isNotEmpty()) {
                        viewModel.postJob(title, description, budget, days)
                        viewModel.clearGeneratedJobDesc()
                        title = ""
                        budgetText = ""
                        skillsNeeded = ""
                        durationText = ""
                        description = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = title.isNotEmpty() && budgetText.isNotEmpty() && description.isNotEmpty()
            ) {
                Text("Confirm and Post Project", fontWeight = FontWeight.Black)
            }

        } else {
            var title by remember { mutableStateOf("") }
            var priceText by remember { mutableStateOf("") }
            var durationText by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("Mobile Dev") }
            var description by remember { mutableStateOf("") }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("I will... (Service Title)") },
                placeholder = { Text("e.g. build optimized Android components") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Minimum Service Price (PKR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("e.g. 15000") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Mobile Dev", "Design", "Writing", "Backend").forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = durationText,
                onValueChange = { durationText = it },
                label = { Text("Delivery Duration (Days)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("e.g. 3") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Gig Description") },
                minLines = 4,
                placeholder = { Text("Let buyers know about your technology expertise, portfolio results, and source code transfer guarantee.") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val days = durationText.toIntOrNull() ?: 3
                    if (title.isNotEmpty() && price > 0 && description.isNotEmpty()) {
                        viewModel.addServicelisting("I will $title", description, price, days, category)
                        title = ""
                        priceText = ""
                        durationText = ""
                        description = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = title.isNotEmpty() && priceText.isNotEmpty() && description.isNotEmpty()
            ) {
                Text("Publish to Marketplace", fontWeight = FontWeight.Black)
            }
        }
    }
}

// ---------------- ORDERS & CHAT SCREEN ----------------
@Composable
fun OrdersScreen(viewModel: SkillLinkViewModel, userRole: String) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val selectedOrder by viewModel.selectedOrder.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    
    if (selectedOrder == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Escrow Orders Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Funds are locked automatically. Complete safely & release on client approval.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ongoing transactions. Post jobs or hire talent to begin!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(orders) { order ->
                        OrderSummaryCard(order = order, onClick = { viewModel.selectOrder(order.id) })
                    }
                }
            }
        }
    } else {
        OrderWorkSpace(
            order = selectedOrder!!,
            messages = chatMessages,
            viewModel = viewModel,
            userRole = userRole,
            onBack = { viewModel.selectOrder(null) }
        )
    }
}

@Composable
fun OrderSummaryCard(order: Order, onClick: () -> Unit) {
    val isCompleted = order.status == "COMPLETED"
    val isSubmitted = order.status == "SUBMITTED"
    val isDisputed = order.status == "DISPUTED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = if (order.price > 20000.0) "Gig Contract" else "Custom Proposal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                val badgeColor = when {
                    isCompleted -> Color(0xFFC8E6C9)
                    isSubmitted -> Color(0xFFB3E5FC)
                    isDisputed -> Color(0xFFFFCDD2)
                    else -> Color(0xFFFFF9C4)
                }
                val badgeTextColor = when {
                    isCompleted -> Color(0xFF1B5E20)
                    isSubmitted -> Color(0xFF01579B)
                    isDisputed -> Color(0xFFB71C1C)
                    else -> Color(0xFFF57F17)
                }
                val badgeText = when {
                    isCompleted -> "COMPLETED"
                    isSubmitted -> "SUBMITTED"
                    isDisputed -> "DISPUTED"
                    else -> "ESCROW LOCKED"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(badgeColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = order.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Freelancer: ${order.freelancerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Rs. ${String.format("%,.0f", order.price)}",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OrderWorkSpace(
    order: Order,
    messages: List<ChatMessage>,
    viewModel: SkillLinkViewModel,
    userRole: String,
    onBack: () -> Unit
) {
    var chatInput by remember { mutableStateOf("") }
    var showDeliverDialog by remember { mutableStateOf(false) }
    var deliverText by remember { mutableStateOf("") }
    
    var showApprovalDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(5f) }
    var userReviewText by remember { mutableStateOf("") }
    
    var showDisputeDialog by remember { mutableStateOf(false) }
    var disputeReasonInput by remember { mutableStateOf("") }

    val isPendingReview = order.status == "SUBMITTED"
    val isCompleted = order.status == "COMPLETED"
    val isDisputed = order.status == "DISPUTED"

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Workspace #${order.id}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Client: ${order.clientName} • Freelancer: ${order.freelancerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isCompleted) Color(0xFFE8F5E9)
                    else if (isDisputed) Color(0xFFFFEBEE)
                    else Color(0xFFE8F5E9)
                )
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle 
                                  else if (isDisputed) Icons.Default.Warning 
                                  else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isCompleted) Color(0xFF2E7D32)
                           else if (isDisputed) Color(0xFFC62828)
                           else Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isCompleted) "Escrow Released" 
                               else if (isDisputed) "Escrow Under Dispute" 
                               else "Escrow Vault PKR ${String.format("%,.0f", order.price)}/- Protected",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) Color(0xFF1B5E20)
                                else if (isDisputed) Color(0xFFB71C1C)
                                else Color(0xFF1B5E20)
                    )
                    Text(
                        text = if (isCompleted) "Framework payment transfer completed."
                               else if (isDisputed) "Mediation analyst reviewing deliverable chat logs."
                               else "Deposited via ${order.paymentMethod}. Approve deliverables to transfer.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = false,
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isSystem = msg.senderName.contains("System")
                val isSelf = msg.senderName == (viewModel.localUser.value?.fullName ?: "")

                if (isSystem) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray.copy(alpha = 0.4f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = msg.messageText,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = msg.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelf) MaterialTheme.colorScheme.primary 
                                                     else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.messageText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelf) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    msg.attachmentName?.let { file ->
                                        Row(
                                            modifier = Modifier
                                                .padding(top = 6.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Attachment,
                                                contentDescription = "Attachment",
                                                tint = if (isSelf) Color.White else Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$file (${msg.attachmentSize})",
                                                fontSize = 11.sp,
                                                color = if (isSelf) Color.White else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (userRole == "FREELANCER" && order.status == "ESCROW_LOCKED") {
                Button(
                    onClick = { showDeliverDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Deliverables", fontWeight = FontWeight.Bold)
                }
            } else if (userRole == "CLIENT" && isPendingReview) {
                Button(
                    onClick = { showApprovalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve Work", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = { showDisputeDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dispute Work", fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val files = listOf("wireframe_v2.png", "source_code.zip", "assets_package.rar", "database_migration.sql")
                    val sizes = listOf("4.2 MB", "12.8 MB", "18.5 MB", "1.1 MB")
                    val idx = (0..3).random()
                    viewModel.sendChat("I attached a required document for review:", files[idx], sizes[idx])
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Attachment,
                    contentDescription = "Attach File",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Write message here...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        viewModel.sendChat(chatInput)
                        chatInput = ""
                    }
                },
                enabled = chatInput.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (chatInput.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showDeliverDialog) {
        Dialog(onDismissRequest = { showDeliverDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Upload Work deliverables", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text("Ensure source codes or assets are hosted on OneDrive/GitHub correctly before sending.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = deliverText,
                        onValueChange = { deliverText = it },
                        label = { Text("GitHub Link / Cloud Drive Link or Summary Text") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDeliverDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (deliverText.isNotEmpty()) {
                                    showDeliverDialog = false
                                    viewModel.uploadDeliverables(order, deliverText)
                                }
                            }
                        ) {
                            Text("Submit Work")
                        }
                    }
                }
            }
        }
    }

    if (showApprovalDialog) {
        Dialog(onDismissRequest = { showApprovalDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Approve Work & Release Escrow", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text("This process transfers funds instantly. Rates and reviews will be visible in public profile.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Rate Freelancer Service (1-5):", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { userRating = star.toFloat() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (star <= userRating) Color(0xFFFFC107) else Color.LightGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = userReviewText,
                        onValueChange = { userReviewText = it },
                        label = { Text("Write brief public feedback") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showApprovalDialog = false }) {
                            Text("Close")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                showApprovalDialog = false
                                viewModel.approveWork(order, userRating, userReviewText)
                            }
                        ) {
                            Text("Approve & Pay")
                        }
                    }
                }
            }
        }
    }

    if (showDisputeDialog) {
        Dialog(onDismissRequest = { showDisputeDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Raise Escrow Dispute Form", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = Color.Red)
                    Text("Our mediation agent will audit chat history and files within 72 hours. Escrow funds are immediately frozen.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = disputeReasonInput,
                        onValueChange = { disputeReasonInput = it },
                        label = { Text("Mediation audit reason description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDisputeDialog = false }) {
                            Text("Go Back")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (disputeReasonInput.isNotEmpty()) {
                                    showDisputeDialog = false
                                    viewModel.disputeWork(order, disputeReasonInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("File Dispute", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- USER HUB & PROFILE SCREEN ----------------
@Composable
fun ProfileScreen(viewModel: SkillLinkViewModel) {
    val localUser by viewModel.localUser.collectAsStateWithLifecycle()
    val aiSuggestions by viewModel.aiSuggestions.collectAsStateWithLifecycle()
    val loadingSuggestions by viewModel.loadingSuggestions.collectAsStateWithLifecycle()

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var cnicInputText by remember { mutableStateOf(localUser?.cnicNumber ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        localUser?.let { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (user.isVerified) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified CNIC",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PKR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Expertise: ${user.skills}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("SkillLink Secure Escrow Wallet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Available", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "Rs. ${String.format("%,.0f", user.accountBalance)}",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Ready to withdraw", fontSize = 9.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Locked Escrow", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "Rs. ${String.format("%,.0f", user.pendingEscrow)}",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFF57F17)
                        )
                        Text("Active Projects Guard", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showWithdrawDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Withdraw to JazzCash / Easypaisa", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (user.isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (user.isVerified) Icons.Default.VerifiedUser else Icons.Default.Help,
                            contentDescription = null,
                            tint = if (user.isVerified) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (user.isVerified) "CNIC Verified Account" else "Identity Verification Pending",
                            fontWeight = FontWeight.Bold,
                            color = if (user.isVerified) Color(0xFF1B5E20) else Color(0xFFE65100)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (user.isVerified) "Your Pakistani ID check has completed successfully! Verified Badge added to direct-hire searches."
                               else "Postings and withdrawals require valid CNIC verification. Complete verification to secure client escrow payments.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )

                    if (!user.isVerified) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = cnicInputText,
                                onValueChange = { cnicInputText = it },
                                placeholder = { Text("e.g. 35201-1234567-3") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (cnicInputText.isNotEmpty()) {
                                        viewModel.simulateCnicUpload(cnicInputText)
                                    }
                                }
                            ) {
                                Text("Verify", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI Profile Optimizer",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        IconButton(
                            onClick = { viewModel.fetchAiSuggestions() },
                            enabled = !loadingSuggestions
                        ) {
                            if (loadingSuggestions) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Get advice")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Let Gemini optimize your core skills listing, bio description, and local payout rate standards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (aiSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = aiSuggestions,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        var withdrawAmount by remember { mutableStateOf("") }
        var withdrawAccount by remember { mutableStateOf("") }
        var selectedMethod by remember { mutableStateOf("JazzCash") }

        Dialog(onDismissRequest = { showWithdrawDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Secure Local Earnings Payout", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text("Withdraw funds directly using secure local instant transfer paths.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Select Local Account Channel:", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("JazzCash", "Easypaisa", "Bank").forEach { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Transfer Cash Amount (PKR)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = withdrawAccount,
                        onValueChange = { withdrawAccount = it },
                        label = { Text("Account Identifier (Phone or IPAN)") },
                        placeholder = { Text("e.g. 03001234567") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showWithdrawDialog = false }) {
                            Text("Reject")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val amount = withdrawAmount.toDoubleOrNull() ?: 0.0
                                if (amount > 0 && withdrawAccount.isNotEmpty()) {
                                    showWithdrawDialog = false
                                    viewModel.requestWithdrawal(amount, selectedMethod, "$selectedMethod ($withdrawAccount)")
                                }
                            }
                        ) {
                            Text("Approve Withdrawal")
                        }
                    }
                }
            }
        }
    }
}

// ---------------- DIALOGS & SHEET HELPERS ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GigDetailSheet(
    gig: Gig,
    onDismiss: () -> Unit,
    onHire: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = gig.freelancerName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${gig.freelancerRating}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = gig.title,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Service Overview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = gig.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Delivery Period", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${gig.deliveryTimeDays} Days", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Secure Escrow Price", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Rs. ${String.format("%,.0f", gig.price)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onHire,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hire with Escrow Safety", fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailSheet(
    job: JobPost,
    onDismiss: () -> Unit,
    onBid: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Project Proposal Opportunity",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = job.title,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Budget: Rs. ${String.format("%,.0f", job.budget)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Duration: ${job.deadlineDays} Days", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Requirements", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = job.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            var bidProposalInput by remember { mutableStateOf("") }
            var bidCostInput by remember { mutableStateOf("${job.budget.toInt()}") }

            OutlinedTextField(
                value = bidCostInput,
                onValueChange = { bidCostInput = it },
                label = { Text("Your Bid Proposal Cost (Rs.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bidProposalInput,
                onValueChange = { bidProposalInput = it },
                label = { Text("Write brief proposal details") },
                placeholder = { Text("Let Client know how fast you can deliver this project.") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (bidProposalInput.isNotEmpty() && bidCostInput.isNotEmpty()) {
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = bidProposalInput.isNotEmpty()
            ) {
                Text("Post Custom Proposal Bid", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun HireEscrowDialog(
    gig: Gig,
    onDismiss: () -> Unit,
    onConfirmHire: (String) -> Unit
) {
    var selectedChannel by remember { mutableStateOf("JazzCash") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Secure Escrow Payment Vault", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Confirm locking Rs. ${String.format("%,.0f", gig.price)} in local escrow safeguards. Funds are released ONLY when you approve delivery.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Text("Pay securely via:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("JazzCash", "Easypaisa", "Bank Transfer").forEach { payChannel ->
                        FilterChip(
                            selected = selectedChannel == payChannel,
                            onClick = { selectedChannel = payChannel },
                            label = { Text(payChannel) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onConfirmHire(selectedChannel) }
                    ) {
                        Text("Pay & Initialize Escrow")
                    }
                }
            }
        }
    }
}
