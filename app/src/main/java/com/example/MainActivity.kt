package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.AuthScreen
import com.example.ui.components.BadgesAndAchievementsView
import com.example.ui.components.BookmarkedMessagesView
import com.example.ui.components.CelebrationDialog
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.ContactDeveloperDialog
import com.example.ui.components.ShareChatDialog
import com.example.ui.components.ModeSelectorBar
import com.example.ui.components.SubjectCategoryChips
import com.example.ui.components.TutorHeaderBar
import com.example.ui.components.VoiceInputButton
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MahimTutorApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MahimTutorApp(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isGuest by viewModel.isGuest.collectAsStateWithLifecycle()

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val bookmarkedMessages by viewModel.bookmarkedMessages.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val learningMode by viewModel.learningMode.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val celebratingBadge by viewModel.showCelebrationDialog.collectAsStateWithLifecycle()
    val currentlySpeakingId by viewModel.ttsHelper.currentlySpeakingId.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showContactDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // If no active user session and not in guest mode, show AuthScreen
    if (currentUser == null && !isGuest) {
        AuthScreen(
            onLogin = { identifier, password, callback ->
                viewModel.login(identifier, password, callback)
            },
            onSignUp = { username, email, password, callback ->
                viewModel.signUp(username, email, password, callback)
            },
            onGuestLogin = {
                viewModel.loginAsGuest()
            }
        )
        return
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (celebratingBadge != null) {
        CelebrationDialog(
            badgeTitle = celebratingBadge!!,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }

    if (showContactDialog) {
        ContactDeveloperDialog(
            onDismiss = { showContactDialog = false }
        )
    }

    if (showShareDialog) {
        ShareChatDialog(
            messages = messages,
            onDismiss = { showShareDialog = false }
        )
    }

    val isKeyboardVisible = WindowInsets.isImeVisible

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TutorHeaderBar(
                starsCount = userStats?.starsCount ?: 5,
                currentUser = currentUser,
                onClearChat = { viewModel.clearChatHistory() },
                onLogout = { viewModel.logout() },
                onContactDeveloper = { showContactDialog = true },
                onShareChat = { showShareDialog = true }
            )
        },
        bottomBar = {
            if (!isKeyboardVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat") },
                        label = { Text("টিউটর আড্ডা", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Saved") },
                        label = { Text("প্রিয় উত্তর", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Badges") },
                        label = { Text("আমার ব্যাজ", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> {
                    // Chat Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        // Learning Mode Segment Bar
                        ModeSelectorBar(
                            currentMode = learningMode,
                            onModeSelected = { viewModel.setLearningMode(it) }
                        )

                        // Quick Subject Chips
                        SubjectCategoryChips(
                            onPromptSelected = { prompt ->
                                inputText = prompt
                            }
                        )

                        // Messages LazyColumn
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
                            ) {
                                items(messages, key = { it.id }) { message ->
                                    ChatMessageBubble(
                                        message = message,
                                        isSpeakingThisMsg = currentlySpeakingId == message.id,
                                        onToggleBookmark = { id, currentBookmarked ->
                                            viewModel.toggleBookmark(id, currentBookmarked)
                                        },
                                        onSpeakMessage = { text, id ->
                                            viewModel.speakMessage(text, id)
                                        }
                                    )
                                }

                                if (isLoading) {
                                    item {
                                        LoadingTutorBubble()
                                    }
                                }
                            }
                        }

                        // Input Box Bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VoiceInputButton(
                                    onTextRecognized = { text ->
                                        inputText = text
                                    }
                                )

                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = {
                                        Text(
                                            text = when (learningMode) {
                                                "QUIZ" -> "ধাঁধার উত্তর দাও বা প্রশ্ন করো..."
                                                "STORY" -> "কিসের গল্প শুনতে চাও?"
                                                else -> "মাহিমকে যেকোনো প্রশ্ন করো..."
                                            },
                                            fontSize = 14.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    maxLines = 3
                                )

                                IconButton(
                                    onClick = {
                                        if (inputText.isNotBlank()) {
                                            val textToSend = inputText
                                            inputText = ""
                                            viewModel.sendMessage(textToSend)
                                        }
                                    },
                                    enabled = inputText.isNotBlank() && !isLoading,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (inputText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send Message",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Bookmarked / Saved Screen
                    BookmarkedMessagesView(
                        bookmarkedList = bookmarkedMessages,
                        onToggleBookmark = { id, currentBookmarked ->
                            viewModel.toggleBookmark(id, currentBookmarked)
                        },
                        onSpeakMessage = { text, id ->
                            viewModel.speakMessage(text, id)
                        }
                    )
                }

                2 -> {
                    // Badges & Achievements Screen
                    BadgesAndAchievementsView(
                        stats = userStats
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingTutorBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Thinking",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "মাহিম গভীর চিন্তা করছে... 🦉✨",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
