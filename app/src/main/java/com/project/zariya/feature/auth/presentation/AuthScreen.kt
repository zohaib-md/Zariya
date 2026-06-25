package com.project.zariya.feature.auth.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.feature.auth.presentation.components.*
import com.airbnb.lottie.compose.*
import com.project.zariya.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onGoogleSignIn: () -> Unit,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 0 = Login, 1 = Sign Up
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VanillaCream)
    ) {
        // Soft animated background
        SoftAnimatedBackground()
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP VISUAL SECTION (45% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                // LOTTIE ANIMATION
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(
                        if (selectedTab == 0) R.raw.lottie_login else R.raw.lottie_signup
                    )
                )
                
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(tween(600)) togetherWith fadeOut(tween(600))
                    },
                    label = "lottieCrossfade"
                ) { _ ->
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // BOTTOM AUTH PANEL
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = WarmIvory,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp, bottom = 16.dp)
                ) {
                    AuthSegmentedControl(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        label = "formSwitch"
                    ) { targetTab ->
                        if (targetTab == 0) {
                            LoginForm(viewModel = viewModel, uiState = uiState, onGoogleSignIn = onGoogleSignIn)
                        } else {
                            SignUpForm(viewModel = viewModel, uiState = uiState, onGoogleSignIn = onGoogleSignIn)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginForm(
    viewModel: AuthViewModel,
    uiState: AuthUiState,
    onGoogleSignIn: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "welcome back",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-1.5).sp,
            color = TextPrimary
        )
        Text(
            text = "Your wellness journey continues here.",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )
        
        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = Color(0xFFD88B76),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        PremiumTextField(
            value = uiState.loginEmail,
            onValueChange = { viewModel.onLoginEmailChange(it) },
            label = "Email Address",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PremiumTextField(
            value = uiState.loginPassword,
            onValueChange = { viewModel.onLoginPasswordChange(it) },
            label = "Password",
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 
                focusManager.clearFocus()
                viewModel.signInWithEmail() 
            })
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot Password?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Show forgot password dialog */ }
            )
        }
        
        PremiumSolidButton(
            text = "Begin Today's Journey",
            isLoading = uiState.isLoading,
            onClick = { viewModel.signInWithEmail() }
        )
        
        OrDivider()
        
        PremiumGoogleButton(onClick = onGoogleSignIn)
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SignUpForm(
    viewModel: AuthViewModel,
    uiState: AuthUiState,
    onGoogleSignIn: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "create your space",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-1.5).sp,
            color = TextPrimary
        )
        Text(
            text = "Let's start building healthier habits together.",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )
        
        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = Color(0xFFD88B76),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        PremiumTextField(
            value = uiState.signUpName,
            onValueChange = { viewModel.onSignUpNameChange(it) },
            label = "Full Name",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PremiumTextField(
            value = uiState.signUpEmail,
            onValueChange = { viewModel.onSignUpEmailChange(it) },
            label = "Email Address",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PremiumTextField(
            value = uiState.signUpPassword,
            onValueChange = { viewModel.onSignUpPasswordChange(it) },
            label = "Password",
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PremiumTextField(
            value = uiState.signUpConfirmPassword,
            onValueChange = { viewModel.onSignUpConfirmPasswordChange(it) },
            label = "Confirm Password",
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 
                focusManager.clearFocus()
                viewModel.signUpWithEmail() 
            })
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PremiumSolidButton(
            text = "Create Account",
            isLoading = uiState.isLoading,
            onClick = { viewModel.signUpWithEmail() }
        )
        
        OrDivider()
        
        PremiumGoogleButton(onClick = onGoogleSignIn)
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SoftAnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb1"
    )
    
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb2"
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur(60.dp)
    ) {
        val cx = size.width / 2
        val cy = size.height / 3
        
        // Dusty Rose Orb
        drawCircle(
            color = DustyRose.copy(alpha = 0.5f),
            radius = 280f,
            center = Offset(
                cx + 120f * cos(Math.toRadians(offset1.toDouble())).toFloat(),
                cy + 80f * sin(Math.toRadians(offset1.toDouble())).toFloat()
            )
        )
        
        // Soft Peach Orb
        drawCircle(
            color = SoftPeach.copy(alpha = 0.6f),
            radius = 240f,
            center = Offset(
                cx - 150f * cos(Math.toRadians(offset2.toDouble())).toFloat(),
                cy + 160f + 60f * sin(Math.toRadians(offset2.toDouble())).toFloat()
            )
        )
    }
}
