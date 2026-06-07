package com.paisachat.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.paisachat.data.LocalMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextRange
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import android.speech.RecognizerIntent
import android.content.Intent
import java.util.Locale
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ── 3D CYBERPUNK SPACE SLATE COLOR DESIGN SYSTEM (ELEGANT DARK) ──
val Deep3DBackground = Color(0xFF0A0C14)
val GlassSurfaceCore = Color(0xD91A1D2E) // 85% opacity backdrop
val GlassBorderHighlight = Color(0xFF3B4261) // Reflective border
val NeonActiveGreen = Color(0xFF00FFA3) // Sharp green
val NeonCyanGlow = Color(0xFF00E5FF)
val Sender3DGradient = Brush.verticalGradient(listOf(Color(0xFF2E3B5E), Color(0xFF1A233D)))
val Receiver3DGradient = Brush.verticalGradient(listOf(Color(0xFF424B6E), Color(0xFF282F4B)))
val TextIceWhite = Color(0xFFF1F5F9)
val TextMutedBlue = Color(0xFF64748B)

// Navigation & Screen definitions
sealed interface ChatScreen {
    object Splash : ChatScreen
    object Login : ChatScreen
    object Dashboard : ChatScreen
    data class ChatRoom(val contact: ContactData) : ChatScreen
}

data class ContactData(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isOnline: Boolean,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val isOfficial: Boolean,
    val isGroup: Boolean = false,
    val members: List<String> = emptyList()
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Chat3DActivityScreen(
    currentUserId: String = "user_me",
    onBackPressed: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf<ChatScreen>(ChatScreen.Splash) }
    var userEmail by remember { mutableStateOf("") }
    var activeUserId by remember { mutableStateOf(currentUserId) }
    
    // Bottom pencil contacts sheet and profile tray states
    var isContactPickerOpen by remember { mutableStateOf(false) }
    var isProfileTrayOpen by remember { mutableStateOf(false) }
    
    // The Dashboard thread list starts completely blank as requested!
    var contactsList by remember { mutableStateOf(emptyList<ContactData>()) }

    // Dynamic database of historical & active chat logs
    var chatHistories by remember {
        mutableStateOf(
            mapOf(
                "sc_1" to listOf(
                    LocalMessage("sc_1", "other", "user_me", "Assalam-o-Alaikum bhai! Check this 3D design."),
                    LocalMessage("sc_1", "user_me", "other", "Walaikum Assalam! The spring physics and glassmorphism look incredible!"),
                    LocalMessage("sc_1", "other", "user_me", "Awesome, let's lock this terminal standard.")
                ),
                "sc_2" to listOf(
                    LocalMessage("sc_2", "other", "user_me", "Hello! Did you review the holographic layouts?"),
                    LocalMessage("sc_2", "user_me", "other", "Yes, rendering on dynamic classes."),
                    LocalMessage("sc_2", "other", "user_me", "Perfect, ready for presentation.")
                ),
                "sc_3" to listOf(
                    LocalMessage("sc_3", "other", "user_me", "I made visual adjustments to the system checks.")
                ),
                "sc_4" to listOf(
                    LocalMessage("sc_4", "user_me", "other", "Meeting tonight at secure feed?"),
                    LocalMessage("sc_4", "other", "user_me", "Affirmative, sending logs.")
                ),
                "sc_5" to listOf(
                    LocalMessage("sc_5", "other", "user_me", "The interactive spring transitions feel so smooth and organic!")
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Deep3DBackground)
    ) {
        // Shared 3D Ambient Glowing Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1F00FFA3), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.35f),
                    radius = size.width * 0.75f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1F00E5FF), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.75f),
                    radius = size.width * 0.75f
                )
            )
        }

        // Screen routing with smooth crossfades and slides
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (initialState is ChatScreen.Splash || targetState is ChatScreen.Splash) {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(400))
                } else {
                    val slideDirection = if (initialState is ChatScreen.ChatRoom || targetState is ChatScreen.Dashboard) -1 else 1
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> slideDirection * (fullWidth / 2) },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + scaleIn(
                        initialScale = 0.94f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -slideDirection * (fullWidth / 2) },
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + scaleOut(
                        targetScale = 0.94f,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            },
            label = "MasterNavigation"
        ) { screen ->
            when (screen) {
                is ChatScreen.Splash -> {
                    SplashScreen(onTimeout = { email ->
                        if (email != null) {
                            userEmail = email
                            activeUserId = try { FirebaseAuth.getInstance().currentUser?.uid ?: email.replace(".", "_") } catch (e: Exception) { email.replace(".", "_") }
                            currentScreen = ChatScreen.Dashboard
                        } else {
                            currentScreen = ChatScreen.Login
                        }
                    })
                }
                is ChatScreen.Login -> {
                    LoginScreen(
                        onLoginSuccess = { email ->
                            userEmail = email
                            activeUserId = try { FirebaseAuth.getInstance().currentUser?.uid ?: email.replace(".", "_") } catch (e: Exception) { email.replace(".", "_") }
                            currentScreen = ChatScreen.Dashboard
                        }
                    )
                }
                is ChatScreen.Dashboard -> {
                    DashboardScreen(
                        currentUserId = activeUserId,
                        contacts = contactsList,
                        onContactClicked = { contact ->
                            contactsList = contactsList.map {
                                if (it.id == contact.id) it.copy(unreadCount = 0) else it
                            }
                            currentScreen = ChatScreen.ChatRoom(contact)
                        },
                        isContactPickerOpen = isContactPickerOpen,
                        onTogglePicker = { isContactPickerOpen = !isContactPickerOpen },
                        isProfileTrayOpen = isProfileTrayOpen,
                        onToggleProfileTray = { isProfileTrayOpen = !isProfileTrayOpen },
                        onStartNewChat = { selectedContact ->
                            if (!contactsList.any { it.id == selectedContact.id }) {
                                contactsList = contactsList + selectedContact
                            }
                            currentScreen = ChatScreen.ChatRoom(selectedContact)
                        },
                        onStartNewGroup = { groupContact ->
                            if (!contactsList.any { it.id == groupContact.id }) {
                                contactsList = contactsList + groupContact
                            }
                            currentScreen = ChatScreen.ChatRoom(groupContact)
                        }
                    )
                }
                is ChatScreen.ChatRoom -> {
                    val activeContact = screen.contact
                    val activeMessages = chatHistories[activeContact.id] ?: emptyList()

                    ChatRoomScreen(
                        contact = activeContact,
                        messagesList = activeMessages,
                        currentUserId = activeUserId,
                        onBackPressed = { currentScreen = ChatScreen.Dashboard },
                        onSendMessage = { text, imageUrl ->
                            val newMessage = LocalMessage(
                                id = java.util.UUID.randomUUID().toString(),
                                chatId = activeContact.id,
                                senderId = activeUserId,
                                receiverId = "other",
                                messageText = text,
                                timestamp = System.currentTimeMillis(),
                                imageUrl = imageUrl
                            )
                            
                            // 1. Try to send via Firebase Realtime Database
                            try {
                                val database = FirebaseDatabase.getInstance("https://my-chat-5a268-default-rtdb.firebaseio.com/")
                                val messagesRef = database.getReference("chats").child(activeContact.id).child("messages")
                                val newMsgRef = messagesRef.push()
                                val msgId = newMsgRef.key ?: newMessage.id
                                val finalMessage = newMessage.copy(id = msgId)
                                newMsgRef.setValue(finalMessage)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            
                            // 2. Local reactive history append to support flawless sandbox preview
                            chatHistories = chatHistories.toMutableMap().apply {
                                val currentList = this[activeContact.id] ?: emptyList()
                                this[activeContact.id] = (currentList + newMessage).distinctBy { it.id }
                            }
                            
                            val displayMsg = if (imageUrl.isNotEmpty() && text.isEmpty()) "[Image]" else text
                            contactsList = contactsList.map {
                                if (it.id == activeContact.id) {
                                    it.copy(lastMessage = displayMsg, lastMessageTime = "Just now")
                                } else it
                            }
                        }
                    )
                }
            }
        }

        // Left slide-out Operator settings tray
        AnimatedVisibility(
            visible = isProfileTrayOpen && currentScreen is ChatScreen.Dashboard,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + scaleIn(
                initialScale = 0.95f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut() + scaleOut(
                targetScale = 0.95f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
        ) {
            ProfileSettingsTray(
                userEmail = userEmail,
                activeUserId = activeUserId,
                onDismiss = { isProfileTrayOpen = false },
                onLogout = {
                    try {
                        FirebaseAuth.getInstance().signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    isProfileTrayOpen = false
                    userEmail = ""
                    contactsList = emptyList() // Clear states
                    currentScreen = ChatScreen.Login
                }
            )
        }
    }
}

// ────────────────────────────────────────────────────────
// 1. WELCOME LOGO SPLASH SCREEN
// ────────────────────────────────────────────────────────
@Composable
fun RefractedThreeDLogo(modifier: Modifier = Modifier) {
    var rawOffsetX by remember { mutableStateOf(0f) }
    var rawOffsetY by remember { mutableStateOf(0f) }

    val tiltX by animateFloatAsState(
        targetValue = rawOffsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoTiltX"
    )
    val tiltY by animateFloatAsState(
        targetValue = rawOffsetY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoTiltY"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "LogoFloating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )

    // Breathing glow highlight parameter
    val breathingGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowBreathe"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.firstOrNull()?.position
                        val isPressed = event.changes.any { it.pressed }
                        if (position != null && isPressed) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            rawOffsetX = ((position.x - cx) / cx).coerceIn(-1f, 1f) * 18f
                            rawOffsetY = ((position.y - cy) / cy).coerceIn(-1f, 1f) * 18f
                        } else {
                            rawOffsetX = 0f
                            rawOffsetY = 0f
                        }
                    }
                }
            }
            .graphicsLayer {
                rotationX = -tiltY
                rotationY = tiltX
                translationY = floatOffset.dp.toPx()
                cameraDistance = 15f * density
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // LAYER 1: Ambient Drop Shadow
            val shadowOffsetX = tiltX * 0.4f
            val shadowOffsetY = 15f + (-tiltY * 0.4f)
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = w * 0.35f,
                center = androidx.compose.ui.geometry.Offset(cx + shadowOffsetX, cy + shadowOffsetY)
            )

            // LAYER 3: Glass Crystalline Envelope (Rounded Hexagonal Shield)
            val hexPath = Path().apply {
                val r = w * 0.42f
                for (i in 0 until 6) {
                    val angleRad = Math.toRadians((i * 60 - 30).toDouble())
                    val x = cx + r * Math.cos(angleRad).toFloat()
                    val y = cy + r * Math.sin(angleRad).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            // Glass background fill
            val glassBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x3B475569), // Slate gray-blue semi-translucent
                    Color(0x0A0F172A)  // Deep dark translucent
                )
            )

            // Draw glass outer fill
            drawPath(
                path = hexPath,
                brush = glassBrush
            )

            // 1.5dp glowing rim highlight Around Edges
            val glowColor = NeonCyanGlow.copy(alpha = breathingGlow)
            val rimBrush = Brush.linearGradient(
                colors = listOf(
                    glowColor,
                    Color.Transparent,
                    NeonActiveGreen.copy(alpha = 0.5f * breathingGlow),
                    Color.Transparent,
                    glowColor
                )
            )

            drawPath(
                path = hexPath,
                brush = rimBrush,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // LAYER 2: Core Metallic 3D Isometric Speech Bubble
            val rCore = w * 0.19f
            val coreCy = cy - 4f

            // Prepare paths for Isometric faces
            val vTop = androidx.compose.ui.geometry.Offset(cx, coreCy - rCore)
            val vTopRight = androidx.compose.ui.geometry.Offset(cx + rCore * 0.866f, coreCy - rCore * 0.5f)
            val vBottomRight = androidx.compose.ui.geometry.Offset(cx + rCore * 0.866f, coreCy + rCore * 0.5f)
            val vBottom = androidx.compose.ui.geometry.Offset(cx, coreCy + rCore)
            val vBottomLeft = androidx.compose.ui.geometry.Offset(cx - rCore * 0.866f, coreCy + rCore * 0.5f)
            val vTopLeft = androidx.compose.ui.geometry.Offset(cx - rCore * 0.866f, coreCy - rCore * 0.5f)
            val vCenter = androidx.compose.ui.geometry.Offset(cx, coreCy)

            // 1. TOP FACE (lit face)
            val topFacePath = Path().apply {
                moveTo(vTop.x, vTop.y)
                lineTo(vTopRight.x, vTopRight.y)
                lineTo(vCenter.x, vCenter.y)
                lineTo(vTopLeft.x, vTopLeft.y)
                close()
            }
            val satinTopBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF38BDF8), // Highly lit Metallic sky/cyan accent
                    Color(0xFF1E293B)  // Satin Dark Slate
                )
            )
            drawPath(path = topFacePath, brush = satinTopBrush)

            // 2. RIGHT FACE (medium shadow)
            val rightFacePath = Path().apply {
                moveTo(vCenter.x, vCenter.y)
                lineTo(vTopRight.x, vTopRight.y)
                lineTo(vBottomRight.x, vBottomRight.y)
                lineTo(vBottom.x, vBottom.y)
                close()
            }
            val satinRightBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E293B),
                    Color(0xFF0F172A)
                )
            )
            drawPath(path = rightFacePath, brush = satinRightBrush)

            // 3. LEFT FACE + Speech Bubble Tip (shaded face)
            val leftFacePath = Path().apply {
                moveTo(vTopLeft.x, vTopLeft.y)
                lineTo(vCenter.x, vCenter.y)
                lineTo(vBottom.x, vBottom.y)
                lineTo(vBottomLeft.x, vBottomLeft.y)
                
                // Add perfect geometric speech bubble tail pointing downwards-left
                val tailTipX = cx - rCore * 1.3f
                val tailTipY = coreCy + rCore * 1.0f
                lineTo(tailTipX, tailTipY)
                lineTo(cx - rCore * 0.4f, coreCy + rCore * 0.7f)
                close()
            }
            val satinLeftBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF121824),
                    Color(0xFF090D14)
                )
            )
            drawPath(path = leftFacePath, brush = satinLeftBrush)

            // Highlight core contours with extremely subtle inner edge highlights for depth
            val innerStrokeBrush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
            )
            drawPath(path = topFacePath, brush = innerStrokeBrush, style = Stroke(width = 2f))

            // Draw Parallel Communication Grooves/Arcs
            val lineStrokeColor1 = NeonCyanGlow.copy(alpha = 0.85f * breathingGlow)
            val lineStrokeColor2 = NeonActiveGreen.copy(alpha = 0.85f * breathingGlow)

            drawLine(
                color = lineStrokeColor1,
                start = androidx.compose.ui.geometry.Offset(cx - rCore * 0.5f, coreCy - rCore * 0.35f),
                end = androidx.compose.ui.geometry.Offset(cx + rCore * 0.2f, coreCy - rCore * 0.35f + rCore * 0.35f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = lineStrokeColor2,
                start = androidx.compose.ui.geometry.Offset(cx - rCore * 0.3f, coreCy - rCore * 0.5f),
                end = androidx.compose.ui.geometry.Offset(cx + rCore * 0.4f, coreCy - rCore * 0.5f + rCore * 0.35f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SplashScreen(onTimeout: (String?) -> Unit) {
    var startAnimations by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimations) 1.0f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )

    val loadingProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        startAnimations = true
        loadingProgress.animateTo(1f, animationSpec = tween(2300, easing = EaseInOutCubic))
        val currentUserEmail = try {
            FirebaseAuth.getInstance().currentUser?.email
        } catch (e: Exception) {
            null
        }
        onTimeout(currentUserEmail)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_logo_1780652305998),
                contentDescription = "AR CHAT logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "AR CHAT",
                color = TextIceWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "INITIALIZING SECURE TERMINAL...",
                color = NeonActiveGreen.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(3.dp)
                    .background(Color(0xFF16192A), RoundedCornerShape(1.5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(loadingProgress.value)
                        .background(Brush.horizontalGradient(listOf(NeonCyanGlow, NeonActiveGreen)))
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// 2. SECURE USER AUTHENTICATION (EMAIL + PASSWORD)
// ────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var mobileNumber by rememberSaveable { mutableStateOf("") }
    var emailAddress by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    
    var isMobileFocused by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    
    val isProceeding by viewModel.isProceeding.collectAsState()
    val firebaseStatusText by viewModel.firebaseStatusText.collectAsState()
    val isErrorState by viewModel.isErrorState.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isSignUpMode) {
        viewModel.resetStatus()
    }

    // Validation
    val isValidMobile = remember(mobileNumber) {
        val trimmed = mobileNumber.trim()
        trimmed.isNotEmpty() && trimmed.length >= 8
    }
    val isValidEmail = remember(emailAddress) {
        val trimmed = emailAddress.trim()
        trimmed.contains("@") && trimmed.endsWith(".com") && trimmed.length >= 6
    }
    val isValidPassword = remember(password) {
        password.length >= 6
    }
    
    val isFormValid = if (isSignUpMode) {
        isValidMobile && isValidEmail && isValidPassword
    } else {
        isValidMobile && isValidPassword
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Photo Logo with Glassmorphic Border
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(16.dp, CircleShape)
                    .background(GlassSurfaceCore, CircleShape)
                    .border(
                        1.5.dp, 
                        if (isFormValid) NeonActiveGreen else GlassBorderHighlight, 
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_logo_1780652305998),
                    contentDescription = "AR CHAT Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "AR CHAT",
                color = TextIceWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isSignUpMode)
                    "Provision a new 3D holographic node inside the encrypted network"
                else
                    "Establish cryptographic link to sign in to your node terminal",
                color = TextMutedBlue,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // MAIN LOGIN & SIGNUP FIELDS
            Column(modifier = Modifier.fillMaxWidth()) {
                
                // 1. MOBILE NUMBER INPUT (Both modes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUpMode) "SECURE MOBILE NUMBER" else "REGISTERED MOBILE NUMBER",
                        color = if (isValidMobile) NeonActiveGreen else NeonCyanGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (mobileNumber.isNotEmpty() && !isValidMobile) {
                        Text(
                            text = "Must be >= 8 digits",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val mobileFocusScale by animateFloatAsState(
                    if (isMobileFocused) 1.03f else 1f, 
                    spring(dampingRatio = Spring.DampingRatioHighBouncy)
                )
                val mobileShadowDp by animateDpAsState(if (isMobileFocused) 12.dp else 4.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(mobileFocusScale)
                        .shadow(mobileShadowDp, RoundedCornerShape(16.dp))
                        .background(GlassSurfaceCore, RoundedCornerShape(16.dp))
                        .border(
                            width = if (isMobileFocused) 1.5.dp else 1.dp,
                            color = if (isMobileFocused) NeonCyanGlow else GlassBorderHighlight,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Mobile configuration node",
                            tint = TextMutedBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .background(GlassBorderHighlight)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextField(
                            value = mobileNumber,
                            onValueChange = { input ->
                                mobileNumber = input.filter { it.isDigit() || it == '+' }
                            },
                            placeholder = { Text("e.g. +923001234567 or 03001234567", color = TextMutedBlue, fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isMobileFocused = it.isFocused },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextIceWhite,
                                unfocusedTextColor = TextIceWhite
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = if (isSignUpMode) ImeAction.Next else ImeAction.Done
                            ),
                            singleLine = true
                        )
                    }
                }

                if (isSignUpMode) {
                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. EMAIL INPUT (SignUp only)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GMAIL OPERATOR NODE",
                            color = if (isValidEmail) NeonActiveGreen else NeonCyanGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        if (emailAddress.isNotEmpty() && !isValidEmail) {
                            Text(
                                text = "Format: ...@domain.com",
                                color = Color.Red.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val emailFocusScale by animateFloatAsState(
                        if (isEmailFocused) 1.03f else 1f, 
                        spring(dampingRatio = Spring.DampingRatioHighBouncy)
                    )
                    val emailShadowDp by animateDpAsState(if (isEmailFocused) 12.dp else 4.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(emailFocusScale)
                            .shadow(emailShadowDp, RoundedCornerShape(16.dp))
                            .background(GlassSurfaceCore, RoundedCornerShape(16.dp))
                            .border(
                                width = if (isEmailFocused) 1.5.dp else 1.dp,
                                color = if (isEmailFocused) NeonCyanGlow else GlassBorderHighlight,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email node configuration",
                                tint = TextMutedBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(18.dp)
                                    .background(GlassBorderHighlight)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            TextField(
                                value = emailAddress,
                                onValueChange = { input ->
                                    emailAddress = input.filter { !it.isWhitespace() }
                                },
                                placeholder = { Text("operator@gmail.com", color = TextMutedBlue, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isEmailFocused = it.isFocused },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextIceWhite,
                                    unfocusedTextColor = TextIceWhite
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. PASSWORD INPUT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CRYPTOGRAPHIC PASSKEY",
                        color = if (isValidPassword) NeonActiveGreen else NeonCyanGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (password.isNotEmpty() && !isValidPassword) {
                        Text(
                            text = "Must be >= 6 chars",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val passwordFocusScale by animateFloatAsState(
                    if (isPasswordFocused) 1.03f else 1f, 
                    spring(dampingRatio = Spring.DampingRatioHighBouncy)
                )
                val passwordShadowDp by animateDpAsState(if (isPasswordFocused) 12.dp else 4.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(passwordFocusScale)
                        .shadow(passwordShadowDp, RoundedCornerShape(16.dp))
                        .background(GlassSurfaceCore, RoundedCornerShape(16.dp))
                        .border(
                            width = if (isPasswordFocused) 1.5.dp else 1.dp,
                            color = if (isPasswordFocused) NeonCyanGlow else GlassBorderHighlight,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure passkey input",
                            tint = TextMutedBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .background(GlassBorderHighlight)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextField(
                            value = password,
                            onValueChange = { input ->
                                password = input
                            },
                            placeholder = { Text("••••••••", color = TextMutedBlue, fontSize = 14.sp) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isPasswordFocused = it.isFocused },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextIceWhite,
                                unfocusedTextColor = TextIceWhite
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )
                        Box(
                            modifier = Modifier
                                .clickable { passwordVisible = !passwordVisible }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                color = NeonActiveGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ACTION BUTTON
            var isNextPressed by remember { mutableStateOf(false) }
            val nextScale by animateFloatAsState(
                targetValue = if (isNextPressed) 0.92f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
                label = "NextButtonScale"
            )

            val buttonBackground = if (isFormValid) {
                Brush.horizontalGradient(listOf(NeonCyanGlow, Color(0xFF007A9B)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF1E2235), Color(0xFF161925)))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .scale(nextScale)
                    .shadow(
                        if (isFormValid) 14.dp else 2.dp, 
                        RoundedCornerShape(18.dp), 
                        ambientColor = NeonCyanGlow, 
                        spotColor = NeonCyanGlow
                    )
                    .background(buttonBackground, RoundedCornerShape(18.dp))
                    .border(
                        1.dp, 
                        if (isFormValid) NeonCyanGlow.copy(alpha = 0.5f) else Color.Transparent, 
                        RoundedCornerShape(18.dp)
                    )
                    .clickable(enabled = isFormValid && !isProceeding) {
                        isNextPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            delay(120)
                            isNextPressed = false
                            
                            if (isSignUpMode) {
                                viewModel.signUp(
                                    mobileNumber = mobileNumber.trim(),
                                    email = emailAddress.trim(),
                                    pass = password,
                                    onSuccess = { resolvedEmail ->
                                        Toast.makeText(context, "Welcome to Air Chat!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(resolvedEmail)
                                    },
                                    onFailure = { errorMsg ->
                                        Toast.makeText(context, "Provision FAILED: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                viewModel.loginWithMobile(
                                    mobileNumber = mobileNumber.trim(),
                                    pass = password,
                                    onSuccess = { resolvedEmail ->
                                        Toast.makeText(context, "Uplink verified!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(resolvedEmail)
                                    },
                                    onFailure = { errorMsg ->
                                        Toast.makeText(context, "Authorization ERROR: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isProceeding) {
                    CircularProgressIndicator(color = NeonActiveGreen, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isSignUpMode) "PROVISION PRIVATE NODE" else "ESTABLISH SECURE UPLINK",
                        color = if (isFormValid) Color.White else TextMutedBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Status feedback message unit
            if (firebaseStatusText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = firebaseStatusText,
                    color = if (isErrorState) Color.Red.copy(0.9f) else if (firebaseStatusText.contains("successfully") || firebaseStatusText.contains("authorized") || firebaseStatusText.contains("complete!")) NeonActiveGreen else NeonCyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SIGN UP MODE SWITCH TOGGLER
            var isTogglePressed by remember { mutableStateOf(false) }
            val toggleScale by animateFloatAsState(if (isTogglePressed) 0.95f else 1f)

            Box(
                modifier = Modifier
                    .scale(toggleScale)
                    .clickable {
                        isTogglePressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        coroutineScope.launch {
                            delay(100)
                            isTogglePressed = false
                            isSignUpMode = !isSignUpMode
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSignUpMode) 
                        "Existing node? AUTHORIZE CONNECTION" 
                    else 
                        "Need terminal key? PROVISION PRIVATE NODE",
                    color = NeonActiveGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// 3. MAIN DASHBOARD WITH TOGGLABLE CHANNEL SPACES
// ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUserId: String = "user_me",
    contacts: List<ContactData>,
    onContactClicked: (ContactData) -> Unit,
    isContactPickerOpen: Boolean,
    onTogglePicker: () -> Unit,
    isProfileTrayOpen: Boolean,
    onToggleProfileTray: () -> Unit,
    onStartNewChat: (ContactData) -> Unit,
    onStartNewGroup: (ContactData) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = CHATS, 1 = GROUPS
    
    // Filter contacts dynamic registry
    val filteredHistory = remember(searchQuery, contacts, selectedTab) {
        contacts.filter { contact ->
            val matchesSearch = contact.name.contains(searchQuery, ignoreCase = true)
            val matchesTab = if (selectedTab == 0) !contact.isGroup else contact.isGroup
            matchesSearch && matchesTab
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header panel (Clicking avatar triggers left Tray Settings profile toggling)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp), clip = false)
                    .border(1.dp, GlassBorderHighlight, RoundedCornerShape(24.dp)),
                color = GlassSurfaceCore
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.clickable { onToggleProfileTray() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .border(1.5.dp, NeonActiveGreen, CircleShape)
                                .clip(CircleShape)
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&h=100&q=80",
                                contentDescription = "User settings biometric toggle",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = "AR CHAT Node",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextIceWhite
                            )
                            Text(
                                text = "Channel: SECURE",
                                fontSize = 10.sp,
                                color = NeonActiveGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { onToggleProfileTray() },
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, GlassBorderHighlight, CircleShape)
                            .background(Deep3DBackground.copy(0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Config System",
                            tint = TextIceWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // STYLIZED DEDICATED TAB ROW BAR SYSTEM
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .border(1.dp, GlassBorderHighlight.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    color = GlassSurfaceCore.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        listOf("DIRECT PIPES", "COLLABORATIVE GROUPS").forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            val springBg by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFF20263F) else Color.Transparent,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(springBg)
                                    .clickable { selectedTab = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = if (isSelected) NeonCyanGlow else TextMutedBlue
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar Input focus effect
            var isSearchFocused by remember { mutableStateOf(false) }
            val searchScale by animateFloatAsState(if (isSearchFocused) 1.02f else 1f)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .scale(searchScale)
                        .border(
                            1.dp,
                            if (isSearchFocused) NeonCyanGlow else GlassBorderHighlight,
                            RoundedCornerShape(24.dp)
                        ),
                    color = GlassSurfaceCore.copy(0.7f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Discovery",
                            tint = TextMutedBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Trace channels or targets...", color = TextMutedBlue, fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSearchFocused = it.isFocused },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextIceWhite,
                                unfocusedTextColor = TextIceWhite
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Dynamic Blank Chat dashboard handling
            if (filteredHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Person else Icons.Default.Person,
                            contentDescription = "Empty state icon",
                            tint = TextMutedBlue.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (selectedTab == 0) 
                                "No secure direct wires connected.\nTap the glowing pencil to list systems." 
                            else 
                                "No active collaborative group streams initialized.\nCompose a new channel space now.",
                            color = TextMutedBlue,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    itemsIndexed(filteredHistory) { _, contact ->
                        ConversationRow(contact = contact, onClicked = { onContactClicked(contact) })
                    }
                }
            }
        }

        // Functional Elastic FAB (Starts Picker overlay with 3D Spring Morph)
        var isFabSecPressed by remember { mutableStateOf(false) }
        val fabScale by animateFloatAsState(
            targetValue = if (isFabSecPressed) 0.82f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .size(56.dp)
                .scale(fabScale)
                .shadow(16.dp, CircleShape, ambientColor = NeonActiveGreen, spotColor = NeonActiveGreen)
                .background(Brush.linearGradient(listOf(Color(0xFF00FFA3), Color(0xFF00B09B))), CircleShape)
                .clickable {
                    isFabSecPressed = true
                    onTogglePicker()
                    isFabSecPressed = false
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Add node",
                tint = Color(0xFF0A0C14),
                modifier = Modifier.size(24.dp)
            )
        }

        // Elasticsearch Contact & Group Launcher Modal Dialog Wrapper
        AnimatedVisibility(
            visible = isContactPickerOpen,
            enter = fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + scaleIn(
                initialScale = 0.82f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + slideInVertically(
                initialOffsetY = { 60 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + scaleOut(
                targetScale = 0.82f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + slideOutVertically(
                targetOffsetY = { 60 },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        ) {
            ContactPickerModal(
                currentUserId = currentUserId,
                onDismiss = onTogglePicker,
                onSelectContact = { contact ->
                    onStartNewChat(contact)
                    onTogglePicker()
                },
                onGroupCreated = { name, selectedMembers ->
                    val gId = "sc_group_${System.currentTimeMillis()}"
                    val groupVal = ContactData(
                        id = gId,
                        name = name,
                        avatarUrl = "https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?auto=format&fit=crop&w=100&h=100&q=80",
                        isOnline = true,
                        lastMessage = "Holographic space constructed.",
                        lastMessageTime = "Just now",
                        unreadCount = 0,
                        isOfficial = false,
                        isGroup = true,
                        members = selectedMembers
                    )
                    onStartNewGroup(groupVal)
                    onTogglePicker()
                }
            )
        }
    }
}

// ────────────────────────────────────────────────────────
// 3.1. MOBILE CONTACT PICKER MODAL & GROUP COMPOSE
// ────────────────────────────────────────────────────────
@Composable
fun HolographicQRCodeIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .border(1.dp, NeonCyanGlow.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = size.width
            val qSize = s / 3f
            drawRect(color = NeonCyanGlow, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(qSize, qSize))
            drawRect(color = NeonCyanGlow, topLeft = androidx.compose.ui.geometry.Offset(s - qSize, 0f), size = androidx.compose.ui.geometry.Size(qSize, qSize))
            drawRect(color = NeonCyanGlow, topLeft = androidx.compose.ui.geometry.Offset(0f, s - qSize), size = androidx.compose.ui.geometry.Size(qSize, qSize))
            drawRect(color = NeonCyanGlow, topLeft = androidx.compose.ui.geometry.Offset(s - qSize, s - qSize), size = androidx.compose.ui.geometry.Size(qSize/2, qSize/2))
            drawRect(color = NeonCyanGlow, topLeft = androidx.compose.ui.geometry.Offset(s - qSize/2, s - qSize/2), size = androidx.compose.ui.geometry.Size(qSize/2, qSize/2))
            drawRect(color = NeonCyanGlow, topLeft = androidx.compose.ui.geometry.Offset(s/2, s/2), size = androidx.compose.ui.geometry.Size(qSize/2, qSize/2))
        }
    }
}

fun getDeterministicChatId(userId1: String, userId2: String): String {
    val sorted = listOf(userId1, userId2).sorted()
    return "${sorted[0]}_${sorted[1]}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerModal(
    currentUserId: String = "user_me",
    onDismiss: () -> Unit,
    onSelectContact: (ContactData) -> Unit,
    onGroupCreated: (String, List<String>) -> Unit
) {
    var pickerTab by remember { mutableStateOf(0) } // 0 = Chat, 1 = Compose Group
    var pickerScreenMode by remember { mutableStateOf(0) } // 0 = Directory List, 1 = Add Contact Pane
    
    var groupSubjectName by remember { mutableStateOf("") }
    var selectedGroupMembers by remember { mutableStateOf(setOf<String>()) }

    // Manual contact creation states
    var fullNameInput by remember { mutableStateOf("") }
    var phoneInputVal by remember { mutableStateOf("") }
    
    var isNameFocused by remember { mutableStateOf(false) }
    var isPhoneFocused by remember { mutableStateOf(false) }

    val systemDirectory = remember {
        listOf(
            ContactData("sc_1", "Ali Raza", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=100&h=100&q=80", true, "Assalam-o-Alaikum bhai! Ready to sync.", "1:45 pm", 0, true),
            ContactData("sc_2", "Ayesha Khan", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=100&h=100&q=80", true, "Encrypted secure pipeline.", "Yesterday", 0, true),
            ContactData("sc_3", "Zainab Ahmed", "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=100&h=100&q=80", false, "Ready checklist coordinates.", "9:15 am", 0, false),
            ContactData("sc_4", "Saad Bin Junaid", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=100&h=100&q=80", false, "Will check server deployment.", "2 days ago", 0, false),
            ContactData("sc_5", "Amna Farooq", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=100&h=100&q=80", true, "Interactive elements fit beautifully.", "Active now", 0, false)
        )
    }

    // Interactive alpha breathing glow for focused inputs text-fields
    val infiniteTransition = rememberInfiniteTransition(label = "InputBreatheGlow")
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "InputAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.7f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f)
                .border(1.5.dp, GlassBorderHighlight, RoundedCornerShape(26.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            color = GlassSurfaceCore,
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (pickerScreenMode == 0) "TRANSCIEVER DIRECTORY" else "ADD TRANSCEIVER",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMutedBlue)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedContent(
                    targetState = pickerScreenMode,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (targetState == 1) fullWidth else -fullWidth },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn() togetherWith slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (targetState == 1) -fullWidth else fullWidth },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        ) + fadeOut()
                    },
                    label = "PickerSlideTransition"
                ) { screenMode ->
                    if (screenMode == 0) {
                        // DIRECTORY VIEW WITH UTILITY BUTTONS & TAB CHOOSERS
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Selector Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .background(Color(0xFF0F111E), RoundedCornerShape(10.dp))
                            ) {
                                listOf("NEW SECURE LINE", "COLLABORATIVE GROUP").forEachIndexed { index, title ->
                                    val active = pickerTab == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(if (active) Color(0xFF232845) else Color.Transparent, RoundedCornerShape(10.dp))
                                            .clickable { pickerTab = index },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) NeonCyanGlow else TextMutedBlue
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (pickerTab == 0) {
                                // Start Direct Chat
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // WhatsApp-Style Utility Row items as custom header items inside LazyColumn
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, GlassBorderHighlight.copy(0.4f), RoundedCornerShape(14.dp))
                                                .background(Color(0xFF161928), RoundedCornerShape(14.dp))
                                                .clickable { pickerTab = 1 }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(NeonActiveGreen.copy(0.12f), CircleShape)
                                                    .border(1.0.dp, NeonActiveGreen.copy(0.5f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "New group button",
                                                    tint = NeonActiveGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Text(
                                                text = "New group",
                                                color = TextIceWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, GlassBorderHighlight.copy(0.4f), RoundedCornerShape(14.dp))
                                                .background(Color(0xFF161928), RoundedCornerShape(14.dp))
                                                .clickable { pickerScreenMode = 1 }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(NeonActiveGreen.copy(0.12f), CircleShape)
                                                        .border(1.0.dp, NeonActiveGreen.copy(0.5f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "New contact button",
                                                        tint = NeonActiveGreen,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Text(
                                                    text = "New contact",
                                                    color = TextIceWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            HolographicQRCodeIcon(modifier = Modifier.padding(end = 4.dp))
                                        }
                                    }
                                    
                                    item {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                itemsIndexed(systemDirectory) { _, contact ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF161928), RoundedCornerShape(14.dp))
                                            .border(1.dp, GlassBorderHighlight.copy(0.4f), RoundedCornerShape(14.dp))
                                            .clickable { onSelectContact(contact) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .border(1.dp, if (contact.isOnline) NeonActiveGreen else GlassBorderHighlight, CircleShape)
                                                .clip(CircleShape)
                                        ) {
                                            AsyncImage(model = contact.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = contact.name, color = TextIceWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(text = contact.lastMessage, color = TextMutedBlue, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Create Group Node Space
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(text = "GROUP SUBJECT / NAME", color = NeonCyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .border(1.dp, GlassBorderHighlight, RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F111E), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = groupSubjectName,
                                        onValueChange = { groupSubjectName = it },
                                        textStyle = androidx.compose.ui.text.TextStyle(color = TextIceWhite, fontSize = 13.sp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (groupSubjectName.isEmpty()) {
                                        Text(text = "E.g., Pakistan Developers Suite", color = TextMutedBlue, fontSize = 13.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(text = "SELECT NODE PARTICIPANTS", color = NeonCyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(systemDirectory) { _, contact ->
                                        val isSelected = selectedGroupMembers.contains(contact.name)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF161928), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    selectedGroupMembers = if (isSelected) {
                                                        selectedGroupMembers - contact.name
                                                    } else {
                                                        selectedGroupMembers + contact.name
                                                    }
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = {
                                                    selectedGroupMembers = if (isSelected) {
                                                        selectedGroupMembers - contact.name
                                                    } else {
                                                        selectedGroupMembers + contact.name
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = NeonActiveGreen,
                                                    checkmarkColor = Color(0xFF0A0C14)
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                            ) {
                                                AsyncImage(model = contact.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(text = contact.name, color = TextIceWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val isCreateEnabled = groupSubjectName.isNotBlank() && selectedGroupMembers.isNotEmpty()

                                Button(
                                    onClick = { onGroupCreated(groupSubjectName, selectedGroupMembers.toList()) },
                                    enabled = isCreateEnabled,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCreateEnabled) NeonActiveGreen else Color(0xFF1F2231),
                                        contentColor = Color(0xFF0A0C14)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = "ESTABLISH INTEGRATION INSTANCE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCreateEnabled) Color(0xFF0A0C14) else TextMutedBlue
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // SLEEK INTERACTIVE "ADD NEW CONTACT" DOCK VIEW
                    val isNameValid = fullNameInput.trim().length >= 3 && fullNameInput.all { it.isLetter() || it.isWhitespace() }
                    val isPhoneValid = phoneInputVal.length == 11 && phoneInputVal.startsWith("03") && phoneInputVal.all { it.isDigit() }
                    val isStartChatEnabled = isNameValid && isPhoneValid

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { pickerScreenMode = 0 },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Go back",
                                    tint = NeonCyanGlow
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RETURN TO DIRECTORY",
                                color = NeonCyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Full Name Custom Input
                        Text(text = "FULL NAME", color = TextMutedBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))

                        val nameBorderColor = if (isNameFocused) NeonCyanGlow.copy(alpha = breatheAlpha) else GlassBorderHighlight
                        val nameBorderWidth = if (isNameFocused) 1.8.dp else 1.dp

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(nameBorderWidth, nameBorderColor, RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F111E).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = fullNameInput,
                                onValueChange = { input ->
                                    // Filter characters for alphabet & whitespace entries only
                                    fullNameInput = input.filter { it.isLetter() || it.isWhitespace() }
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(color = TextIceWhite, fontSize = 13.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isNameFocused = it.isFocused },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                cursorBrush = SolidColor(NeonCyanGlow)
                            )
                            if (fullNameInput.isEmpty()) {
                                Text(text = "Enter contact name (letters only)", color = TextMutedBlue, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone Design Box (Locked to 03XXXXXXXXX operators)
                        Text(
                            text = "PHONE ADDRESS",
                            color = TextMutedBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Standard Pakistani operator format (11 digits: 03XXXXXXXXX)",
                            color = TextMutedBlue.copy(0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val phoneBorderColor = if (isPhoneFocused) NeonCyanGlow.copy(alpha = breatheAlpha) else GlassBorderHighlight
                        val phoneBorderWidth = if (isPhoneFocused) 1.8.dp else 1.dp

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(phoneBorderWidth, phoneBorderColor, RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F111E).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = phoneInputVal,
                                onValueChange = { input ->
                                    // Restricted to digits and max 11 digits
                                    val digits = input.filter { it.isDigit() }
                                    if (digits.length <= 11) {
                                        phoneInputVal = digits
                                    }
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = TextIceWhite,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isPhoneFocused = it.isFocused },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                cursorBrush = SolidColor(NeonCyanGlow)
                            )
                            if (phoneInputVal.isEmpty()) {
                                Text(text = "E.g., 03001234567", color = TextMutedBlue, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        val hapticFeedback = LocalHapticFeedback.current
                        var isStartChatPressed by remember { mutableStateOf(false) }
                        val buttonScale by animateFloatAsState(
                            targetValue = if (isStartChatPressed) 0.90f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
                            label = "StartChatBtnScale"
                        )

                        // Start Chat Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .scale(buttonScale)
                                .shadow(if (isStartChatEnabled) 14.dp else 2.dp, RoundedCornerShape(16.dp), ambientColor = NeonCyanGlow, spotColor = NeonCyanGlow)
                                .background(
                                    if (isStartChatEnabled) {
                                        Brush.horizontalGradient(listOf(NeonActiveGreen, Color(0xFF007A5E)))
                                    } else {
                                        SolidColor(Color(0xFF1E2235))
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(1.dp, if (isStartChatEnabled) NeonCyanGlow.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(16.dp))
                                .clickable(enabled = isStartChatEnabled) {
                                    isStartChatPressed = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    
                                    val deterministicChatId = getDeterministicChatId(currentUserId, phoneInputVal)
                                    val newlyCreatedContact = ContactData(
                                        id = deterministicChatId,
                                        name = fullNameInput.trim(),
                                        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=100&h=100&q=80",
                                        isOnline = true,
                                        lastMessage = "Tunnel link initiated successfully.",
                                        lastMessageTime = "Just now",
                                        unreadCount = 0,
                                        isOfficial = false,
                                        isGroup = false,
                                        members = listOf(currentUserId, phoneInputVal)
                                    )
                                    
                                    onSelectContact(newlyCreatedContact)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "START SECURE CHAT",
                                color = if (isStartChatEnabled) Color(0xFF0A0C14) else TextMutedBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
}

// ────────────────────────────────────────────────────────
// 3.2. RECTANGULAR ROW RECORD FOR CONTACT CHATS
// ────────────────────────────────────────────────────────
@Composable
fun ConversationRow(
    contact: ContactData,
    onClicked: () -> Unit
) {
    var isTapped by remember { mutableStateOf(false) }
    val rowScale by animateFloatAsState(if (isTapped) 0.96f else 1.0f)

    LaunchedEffect(isTapped) {
        if (isTapped) {
            delay(100)
            isTapped = false
            onClicked()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(rowScale)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .border(1.0.dp, GlassBorderHighlight.copy(0.4f), RoundedCornerShape(20.dp))
            .clickable { isTapped = true },
        color = GlassSurfaceCore.copy(0.7f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .border(1.2.dp, if (contact.isOnline) NeonActiveGreen else GlassBorderHighlight, CircleShape)
                    .clip(CircleShape)
            ) {
                AsyncImage(model = contact.avatarUrl, contentDescription = contact.name, modifier = Modifier.fillMaxSize())
                if (contact.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(10.dp)
                            .border(1.5.dp, Deep3DBackground, CircleShape)
                            .background(NeonActiveGreen, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = contact.name, color = TextIceWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    if (contact.isGroup) {
                        Box(
                            modifier = Modifier
                                .background(NeonCyanGlow.copy(0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = "GROUP", color = NeonCyanGlow, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    } else if (contact.isOfficial) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified ID badge", tint = NeonCyanGlow, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = contact.lastMessage,
                    color = if (contact.unreadCount > 0) TextIceWhite else TextMutedBlue,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = contact.lastMessageTime, color = TextMutedBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                if (contact.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(NeonActiveGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = contact.unreadCount.toString(), color = Color(0xFF0A0C14), fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// 4. ACTIVE INNER CHAT ROOM (INCLUDES MESSAGE ENGINE)
// ────────────────────────────────────────────────────────
@Composable
fun TypingDot(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(NeonCyanGlow.copy(alpha = alpha))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    contact: ContactData,
    messagesList: List<LocalMessage>,
    currentUserId: String,
    onBackPressed: () -> Unit,
    onSendMessage: (String, String) -> Unit
) {
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var isMessageFocused by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val spokenText = results[0]
                val currentText = inputText.text
                val spacePrefix = if (currentText.isEmpty() || currentText.endsWith(" ")) "" else " "
                val newText = currentText + spacePrefix + spokenText
                inputText = TextFieldValue(
                    text = newText,
                    selection = TextRange(newText.length)
                )
            }
        }
    }
    
    var contactOnlineState by remember(contact.id) { mutableStateOf(contact.isOnline) }
    var typingUsers by remember(contact.id) { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val isOtherUserTyping = typingUsers.values.any { it }

    LaunchedEffect(inputText.text) {
        if (currentUserId.isNotEmpty() && currentUserId != "user_me") {
            try {
                val typingRef = FirebaseDatabase.getInstance("https://my-chat-5a268-default-rtdb.firebaseio.com/")
                    .getReference("chats")
                    .child(contact.id)
                    .child("typing")
                    .child(currentUserId)
                if (inputText.text.isNotEmpty()) {
                    typingRef.setValue(true)
                    delay(3000)
                    typingRef.setValue(false)
                } else {
                    typingRef.setValue(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(contact.id, currentUserId) {
        var statusRef: com.google.firebase.database.DatabaseReference? = null
        var typingRef: com.google.firebase.database.DatabaseReference? = null
        var statusListener: com.google.firebase.database.ValueEventListener? = null
        var typingListener: com.google.firebase.database.ValueEventListener? = null
        
        try {
            val database = FirebaseDatabase.getInstance("https://my-chat-5a268-default-rtdb.firebaseio.com/")
            
            // 1. Status/presence listener
            statusRef = database.getReference("status").child(contact.id).child("isOnline")
            statusListener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    contactOnlineState = snapshot.getValue(Boolean::class.java) ?: false
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            statusRef.addValueEventListener(statusListener)

            // 2. Typing listener
            typingRef = database.getReference("chats").child(contact.id).child("typing")
            typingListener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = mutableMapOf<String, Boolean>()
                    for (childSnapshot in snapshot.children) {
                        val uid = childSnapshot.key
                        val typing = childSnapshot.getValue(Boolean::class.java) ?: false
                        if (uid != null && uid != currentUserId) {
                            map[uid] = typing
                        }
                    }
                    typingUsers = map
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            typingRef.addValueEventListener(typingListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            try {
                if (statusRef != null && statusListener != null) statusRef.removeEventListener(statusListener)
                if (typingRef != null && typingListener != null) typingRef.removeEventListener(typingListener)
                if (currentUserId.isNotEmpty() && currentUserId != "user_me" && typingRef != null) {
                    typingRef.child(currentUserId).setValue(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingImage = true
            uploadProgress = "Processing image..."
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (originalBitmap != null) {
                            val maxDimension = 480
                            val width = originalBitmap.width
                            val height = originalBitmap.height
                            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                                val ratio = width.toFloat() / height.toFloat()
                                val (newWidth, newHeight) = if (ratio > 1) {
                                    Pair(maxDimension, (maxDimension / ratio).toInt())
                                } else {
                                    Pair((maxDimension * ratio).toInt(), maxDimension)
                                }
                                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                            } else {
                                originalBitmap
                            }
                            
                            val outputStream = java.io.ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                            val compressedBytes = outputStream.toByteArray()
                            val base64String = "data:image/jpeg;base64," + android.util.Base64.encodeToString(compressedBytes, android.util.Base64.NO_WRAP)
                            
                            withContext(Dispatchers.Main) {
                                isUploadingImage = false
                                uploadProgress = ""
                                onSendMessage(inputText.text, base64String)
                                inputText = TextFieldValue("")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                isUploadingImage = false
                                uploadProgress = ""
                                Toast.makeText(context, "Failed to parse image bytes", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isUploadingImage = false
                            uploadProgress = ""
                            Toast.makeText(context, "Failed to read image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        isUploadingImage = false
                        uploadProgress = ""
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    var activeMessages by remember(contact.id) { mutableStateOf(messagesList) }

    LaunchedEffect(messagesList) {
        activeMessages = (activeMessages + messagesList).distinctBy { it.id }.sortedBy { it.timestamp }
    }

    DisposableEffect(contact.id) {
        var messagesRef: com.google.firebase.database.DatabaseReference? = null
        var listener: com.google.firebase.database.ValueEventListener? = null
        try {
            messagesRef = FirebaseDatabase.getInstance("https://my-chat-5a268-default-rtdb.firebaseio.com/").getReference("chats")
                .child(contact.id).child("messages")
            listener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<LocalMessage>()
                    for (childSnapshot in snapshot.children) {
                        val msg = childSnapshot.getValue(LocalMessage::class.java)
                        if (msg != null) {
                            list.add(msg)
                        }
                    }
                    activeMessages = list
                }

                override fun onCancelled(error: DatabaseError) {
                    // Ignore or handle
                }
            }
            messagesRef.addValueEventListener(listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                if (messagesRef != null && listener != null) {
                    messagesRef.removeEventListener(listener)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(activeMessages.size, activeMessages.lastOrNull()?.id) {
        if (activeMessages.isNotEmpty()) {
            listState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), clip = false)
                    .border(1.dp, GlassBorderHighlight, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                color = GlassSurfaceCore
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { onBackPressed() },
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, GlassBorderHighlight, CircleShape)
                                .background(Color(0xFF1E2235), CircleShape)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return", tint = TextIceWhite, modifier = Modifier.size(18.dp))
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = contact.name, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextIceWhite, letterSpacing = (-0.2).sp)
                                if (contact.isOfficial) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verif badge", tint = NeonCyanGlow, modifier = Modifier.size(13.dp))
                                }
                                if (contactOnlineState) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NeonActiveGreen)
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    )
                                }
                            }
                            Text(
                                text = if (contactOnlineState) "ONLINE ENCRYPTED" else "OFFLINE TIMEOUT",
                                fontSize = 9.sp,
                                color = if (contactOnlineState) NeonActiveGreen else TextMutedBlue,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.2.dp, GlassBorderHighlight, CircleShape)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(model = contact.avatarUrl, contentDescription = contact.name, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Group membership indicator notice inside ChatRoom
            if (contact.isGroup) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(GlassSurfaceCore.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .border(1.dp, GlassBorderHighlight.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Members: ${contact.members.joinToString(", ")}",
                        color = NeonCyanGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp)
            ) {
                itemsIndexed(activeMessages) { _, message ->
                    val isMe = message.senderId == currentUserId
                    Animated3DBubbleWrapper(message = message, isMe = isMe)
                }
            }

            // Msg Input container focus expansion scale physics
            val dynamicInputScale by animateFloatAsState(if (isMessageFocused) 1.01f else 1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                if (isOtherUserTyping) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurfaceCore.copy(alpha = 0.5f))
                            .border(1.dp, GlassBorderHighlight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TypingDot(delayMillis = 0)
                            TypingDot(delayMillis = 200)
                            TypingDot(delayMillis = 400)
                        }
                        Text(
                            text = "${contact.name} is typing...",
                            color = TextIceWhite.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.2.sp
                        )
                    }
                }

                if (isUploadingImage && uploadProgress.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .background(GlassSurfaceCore, RoundedCornerShape(8.dp))
                            .border(1.dp, NeonCyanGlow.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = NeonCyanGlow,
                                strokeWidth = 1.5.dp
                            )
                            Text(
                                text = uploadProgress,
                                color = TextIceWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(dynamicInputScale)
                        .shadow(16.dp, RoundedCornerShape(28.dp))
                        .border(
                            1.dp,
                            if (isMessageFocused) NeonCyanGlow else GlassBorderHighlight,
                            RoundedCornerShape(28.dp)
                        ),
                    color = GlassSurfaceCore
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (!isUploadingImage) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Attach image",
                                tint = NeonCyanGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Write secured message...", color = TextMutedBlue, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isMessageFocused = it.isFocused },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextIceWhite,
                                unfocusedTextColor = TextIceWhite
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.text.isNotBlank()) {
                                        onSendMessage(inputText.text, "")
                                        inputText = TextFieldValue("")
                                    }
                                }
                            )
                        )

                        var isMicPressed by remember { mutableStateOf(false) }
                        val micScale by animateFloatAsState(if (isMicPressed) 0.82f else 1f, label = "micScale")

                        IconButton(
                            onClick = {
                                isMicPressed = true
                                scope.launch {
                                    delay(100)
                                    isMicPressed = false
                                }
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Transmit vocal telemetry...")
                                }
                                try {
                                    speechRecognizerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Voice synthesis engine unavailable on this node", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .scale(micScale)
                                .padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice input transcription",
                                tint = NeonCyanGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        var isSendTapped by remember { mutableStateOf(false) }
                        val sendScale by animateFloatAsState(if (isSendTapped) 0.85f else 1f)

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(sendScale)
                                .background(Brush.verticalGradient(listOf(NeonCyanGlow, Color(0xFF007FA9))), CircleShape)
                                .clickable {
                                    if (inputText.text.isNotBlank()) {
                                        isSendTapped = true
                                        onSendMessage(inputText.text, "")
                                        inputText = TextFieldValue("")
                                        scope.launch {
                                            delay(100)
                                            isSendTapped = false
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Transmit Message", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Animated3DBubbleWrapper(message: LocalMessage, isMe: Boolean) {
    var isAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isAppeared = true
    }
    AnimatedVisibility(
        visible = isAppeared,
        enter = fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + slideInHorizontally(
            initialOffsetX = { if (isMe) it / 3 else -it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + slideInVertically(
            initialOffsetY = { 25 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + scaleIn(
            initialScale = 0.88f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium
            )
        ) + scaleOut(
            targetScale = 0.88f,
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        Animated3DBubble(message = message, isMe = isMe)
    }
}

@Composable
fun Animated3DBubble(message: LocalMessage, isMe: Boolean) {
    var isTapped by remember { mutableStateOf(false) }
    val bubbleScale by animateFloatAsState(if (isTapped) 0.94f else 1.0f)

    LaunchedEffect(isTapped) {
        if (isTapped) {
            delay(100)
            isTapped = false
        }
    }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .scale(bubbleScale)
                .shadow(10.dp, bubbleShape)
                .background(if (isMe) Sender3DGradient else Receiver3DGradient, bubbleShape)
                .border(1.dp, GlassBorderHighlight, bubbleShape)
                .clickable { isTapped = true }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!message.imageUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        val base64ImageModel = remember(message.imageUrl) {
                            val uri = message.imageUrl
                            if (uri != null && (uri.startsWith("data:image/") || uri.contains(";base64,"))) {
                                try {
                                    val base64Data = if (uri.contains(",")) uri.substringAfter(",") else uri
                                    val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                } catch (e: Exception) {
                                    uri
                                }
                            } else {
                                uri
                            }
                        }
                        AsyncImage(
                            model = base64ImageModel,
                            contentDescription = "Image attachment",
                            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
                if (message.messageText.isNotBlank()) {
                    Text(text = message.messageText, color = TextIceWhite, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// 5. SLIDE OUT PROFILE SETTINGS SIDE TRAY
// ────────────────────────────────────────────────────────
@Composable
fun ProfileSettingsTray(
    userEmail: String,
    activeUserId: String,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    var displayName by remember { mutableStateOf("Authorized Operator") }
    var tempDisplayName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    val dbAddress = "https://my-chat-5a268-default-rtdb.firebaseio.com/"

    // Fetch existing display name
    LaunchedEffect(activeUserId) {
        if (activeUserId.isNotEmpty()) {
            FirebaseDatabase.getInstance(dbAddress)
                .getReference("users")
                .child("profiles")
                .child(activeUserId)
                .child("displayName")
                .get()
                .addOnSuccessListener { snapshot ->
                    val value = snapshot.getValue(String::class.java)
                    if (!value.isNullOrBlank()) {
                        displayName = value
                        tempDisplayName = value
                    } else {
                        tempDisplayName = "Authorized Operator"
                    }
                }
        }
    }

    val onSaveProfile = {
        if (tempDisplayName.isNotBlank()) {
            isSaving = true
            saveStatus = "Uploading profile node data..."
            FirebaseDatabase.getInstance(dbAddress)
                .getReference("users")
                .child("profiles")
                .child(activeUserId)
                .child("displayName")
                .setValue(tempDisplayName.trim())
                .addOnCompleteListener { task ->
                    isSaving = false
                    if (task.isSuccessful) {
                        displayName = tempDisplayName.trim()
                        saveStatus = "Identity Node updated successfully!"
                    } else {
                        saveStatus = "Error: " + (task.exception?.localizedMessage ?: "Sync fail")
                    }
                }
        }
    }

    // Backdrop touch catcher
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onDismiss() }
    ) {
        // Core Slide-out panel Surface
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .align(Alignment.CenterStart)
                .border(1.5.dp, GlassBorderHighlight, RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            color = GlassSurfaceCore,
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header Area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AIR CHAT TERMINAL",
                        color = NeonCyanGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close settings tray", tint = TextMutedBlue)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Avatar and name card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F111D), RoundedCornerShape(18.dp))
                        .border(1.dp, GlassBorderHighlight, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .border(2.dp, NeonActiveGreen, CircleShape)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&h=150&q=80",
                            contentDescription = "User Identity",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = displayName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = userEmail,
                        color = TextMutedBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // PROFILE CONFIGURATION SECTION
                Text(
                    text = "PROFILE CONFIGURATION",
                    color = TextMutedBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                var isNameFocused by remember { mutableStateOf(false) }
                val nameFocusScale by animateFloatAsState(if (isNameFocused) 1.02f else 1f, label = "nameFocusScale")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .scale(nameFocusScale)
                        .background(Color(0xFF131522), RoundedCornerShape(12.dp))
                        .border(1.dp, if (isNameFocused) NeonCyanGlow else GlassBorderHighlight, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Display Name",
                            tint = TextMutedBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(GlassBorderHighlight)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = tempDisplayName,
                            onValueChange = { tempDisplayName = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TextIceWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(NeonCyanGlow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isNameFocused = it.isFocused },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                var isSyncPressed by remember { mutableStateOf(false) }
                val syncScale by animateFloatAsState(if (isSyncPressed) 0.95f else 1f, label = "syncScale")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .scale(syncScale)
                        .background(if (tempDisplayName.isNotBlank() && !isSaving) NeonActiveGreen else Color(0xFF1D262F), RoundedCornerShape(12.dp))
                        .border(1.dp, if (tempDisplayName.isNotBlank() && !isSaving) NeonActiveGreen else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable(enabled = tempDisplayName.isNotBlank() && !isSaving) {
                            isSyncPressed = true
                            coroutineScope.launch {
                                delay(100)
                                isSyncPressed = false
                                onSaveProfile()
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF0F111D), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UPLINKING...",
                            color = Color(0xFF0F111D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (tempDisplayName.isNotBlank()) Color(0xFF0F111D) else TextMutedBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYNCHRONIZE PROFILE NODE",
                            color = if (tempDisplayName.isNotBlank()) Color(0xFF0F111D) else TextMutedBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (saveStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = saveStatus,
                        color = if (saveStatus.contains("successfully")) NeonActiveGreen else NeonCyanGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Diagnostic variables & calibration metrics
                Text(
                    text = "HARDWARE TELEMETRY Check",
                    color = TextMutedBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161928), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonActiveGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Biometric Lock", color = TextIceWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        var biometricChecked by remember { mutableStateOf(true) }
                        Switch(
                            checked = biometricChecked,
                            onCheckedChange = { biometricChecked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0A0C14),
                                checkedTrackColor = NeonActiveGreen
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }

                    // Option 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161928), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NeonCyanGlow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Hologram Sync Rate", color = TextIceWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "99.8 MHz", color = NeonCyanGlow, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Logout terminal standard action
                var isLogoutPressed by remember { mutableStateOf(false) }
                val scaleFactor by animateFloatAsState(if (isLogoutPressed) 0.94f else 1f, label = "scaleFactor")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .scale(scaleFactor)
                        .background(Color(0xFF4A151C), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF9E2A2B), RoundedCornerShape(12.dp))
                        .clickable {
                            isLogoutPressed = true
                            isLogoutPressed = false
                            onLogout()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log Out link", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "DE-AUTHORIZE TERMINAL", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}
