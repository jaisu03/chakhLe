package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.SmsGatewayType
import com.example.data.model.UserRole
import com.example.ui.theme.ChakhLeAmber
import com.example.ui.theme.ChakhLeAmberDark
import com.example.ui.theme.ChakhLeAmberLight
import com.example.ui.theme.ChakhLeBackground
import com.example.ui.theme.ChakhLeBorder
import com.example.ui.theme.ChakhLeRedContainer
import com.example.ui.theme.ChakhLeRedPrimary
import com.example.ui.theme.ChakhLeSuccess
import com.example.ui.theme.ChakhLeSurface
import com.example.ui.theme.ChakhLeSurfaceVariant
import com.example.ui.theme.ChakhLeTextMuted
import com.example.ui.theme.ChakhLeTextPrimary
import com.example.ui.theme.ChakhLeTextSecondary
import com.example.ui.viewmodel.ChakhLeViewModel

@Composable
fun AuthScreen(
    viewModel: ChakhLeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }
    val clipboardManager = LocalClipboardManager.current

    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val cleanDigits = remember(phoneNumber) {
        val d = phoneNumber.filter { it.isDigit() }
        if (d.startsWith("91") && d.length > 10) d.drop(2).take(10)
        else if (d.startsWith("0") && d.length > 10) d.drop(1).take(10)
        else d.take(10)
    }

    LaunchedEffect(phoneNumber) {
        if (cleanDigits != phoneNumber) {
            viewModel.setPhoneNumber(cleanDigits)
        }
    }

    val otpCode by viewModel.otpCode.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val otpTimer by viewModel.otpResendSeconds.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val authIdentifiedMessage by viewModel.authIdentifiedMessage.collectAsState()
    val currentAddress by viewModel.currentAddressTitle.collectAsState()
    val isGpsDetecting by viewModel.isGpsDetecting.collectAsState()

    val attemptsRemaining by viewModel.attemptsRemaining.collectAsState()
    val otpValiditySeconds by viewModel.otpValiditySeconds.collectAsState()
    val lastDispatchedGateway by viewModel.lastDispatchedGateway.collectAsState()
    val selectedGateway by viewModel.selectedGateway.collectAsState()
    val otpSuccessMessage by viewModel.otpSuccessMessage.collectAsState()
    val carrierSmsNotice by viewModel.carrierSmsNotice.collectAsState()

    var showGatewaySettingsDialog by remember { mutableStateOf(false) }
    var showFirebaseGuideDialog by remember { mutableStateOf(false) }

    var selectedAuthTab by remember { mutableIntStateOf(0) }
    var staffUserId by remember { mutableStateOf("") }
    var staffPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChakhLeBackground)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(ChakhLeRedPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Khaibu Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Khaibu",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ChakhLeTextPrimary
            )

            Text(
                text = "Fresh & Fast Food Delivery",
                fontSize = 14.sp,
                color = ChakhLeAmberDark,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error banner if any
            AnimatedVisibility(
                visible = authErrorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFE02424),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authErrorMessage ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFF9B1C1C),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                            if (authErrorMessage?.contains("BILLING", ignoreCase = true) == true ||
                                authErrorMessage?.contains("quota", ignoreCase = true) == true ||
                                authErrorMessage?.contains("reCAPTCHA", ignoreCase = true) == true) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDC2626))
                                        .clickable { showGatewaySettingsDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Switch SMS Gateway / Settings",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Success Identified Banner
            AnimatedVisibility(
                visible = authIdentifiedMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authIdentifiedMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF15803D),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Segmented Tab Switcher (Customer Public Order View vs Staff & Partner Login)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedAuthTab = 0
                                viewModel.clearAuthError()
                            },
                        color = if (selectedAuthTab == 0) Color.White else Color.Transparent,
                        shadowElevation = if (selectedAuthTab == 0) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = if (selectedAuthTab == 0) ChakhLeRedPrimary else ChakhLeTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Customer Order",
                                fontWeight = if (selectedAuthTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedAuthTab == 0) ChakhLeRedPrimary else ChakhLeTextSecondary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedAuthTab = 1
                                viewModel.clearAuthError()
                            },
                        color = if (selectedAuthTab == 1) Color.White else Color.Transparent,
                        shadowElevation = if (selectedAuthTab == 1) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (selectedAuthTab == 1) Color(0xFF2563EB) else ChakhLeTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Staff & Partner",
                                fontWeight = if (selectedAuthTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedAuthTab == 1) Color(0xFF2563EB) else ChakhLeTextSecondary
                            )
                        }
                    }
                }
            }

            if (selectedAuthTab == 1) {
                // STAFF & PARTNER LOGIN CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Staff & Partner Portal",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChakhLeTextPrimary
                                )
                                Text(
                                    text = "Identify as Kitchen Staff or Super Admin",
                                    fontSize = 12.sp,
                                    color = ChakhLeTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🔒 EXCLUSIVE ACCESS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Only Super Admin creates kitchen accounts. Hotel owners must sign in using the User ID and Password shared by Admin.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = staffUserId,
                            onValueChange = {
                                staffUserId = it
                                viewModel.clearAuthError()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("staff_user_id_input"),
                            label = { Text("Staff User ID *") },
                            placeholder = { Text("e.g. behrouz_kitchen or admin") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ChakhLeTextMuted)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = staffPassword,
                            onValueChange = {
                                staffPassword = it
                                viewModel.clearAuthError()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("staff_password_input"),
                            label = { Text("Staff Password *") },
                            placeholder = { Text("Enter account password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ChakhLeTextMuted)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = ChakhLeTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                viewModel.loginWithStaffCredentials(staffUserId, staffPassword)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("staff_login_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            enabled = staffUserId.isNotBlank() && staffPassword.isNotBlank()
                        ) {
                            Text("Identify & Login", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "STAFF LOGIN PRESETS:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            staffUserId = "admin"
                                            staffPassword = "admin"
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("🛡️ Admin Fill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            staffUserId = "behrouz_kitchen"
                                            staffPassword = "Royal@123"
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("👨‍🍳 Kitchen Fill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Main Customer Auth Card with Real OTP System
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = if (!isOtpSent) "Enter Mobile Number" else "Verify 6-Digit OTP",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChakhLeTextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!isOtpSent) {
                            Text(
                                text = "Enter your 10-digit mobile number to receive a 6-digit carrier SMS code",
                                fontSize = 12.sp,
                                color = ChakhLeTextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Phone input
                            OutlinedTextField(
                                value = cleanDigits,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    val normalized = if (digits.startsWith("91") && digits.length > 10) {
                                        digits.drop(2).take(10)
                                    } else if (digits.startsWith("0") && digits.length > 10) {
                                        digits.drop(1).take(10)
                                    } else {
                                        digits.take(10)
                                    }
                                    viewModel.setPhoneNumber(normalized)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input_field"),
                                placeholder = {
                                    Text(
                                        text = "Enter 10-digit mobile number",
                                        color = ChakhLeTextMuted,
                                        fontSize = 15.sp
                                    )
                                },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                    ) {
                                        Text(
                                            text = "+91",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = ChakhLeTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(20.dp)
                                                .background(ChakhLeBorder)
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (cleanDigits.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.setPhoneNumber("") }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear phone number",
                                                tint = ChakhLeTextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                textStyle = TextStyle(
                                    color = ChakhLeTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ChakhLeTextPrimary,
                                    unfocusedTextColor = ChakhLeTextPrimary,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = ChakhLeRedPrimary,
                                    focusedBorderColor = ChakhLeRedPrimary,
                                    unfocusedBorderColor = ChakhLeBorder,
                                    focusedPlaceholderColor = ChakhLeTextMuted,
                                    unfocusedPlaceholderColor = ChakhLeTextMuted
                                ),
                                singleLine = true
                            )

                            if (cleanDigits.isNotEmpty() && cleanDigits.length < 10) {
                                Text(
                                    text = "${cleanDigits.length}/10 digits entered (${10 - cleanDigits.length} more needed)",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.sendOtp(activity)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("send_otp_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                                enabled = cleanDigits.length == 10 && !authLoading
                            ) {
                                if (authLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sending Verification SMS...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Send Verification SMS",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                        } else {
                            // Real OTP sent view
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val recipientText = "Dispatched via carrier SMS to +91 ${cleanDigits.ifBlank { "98765 12345" }}"
                                Text(
                                    text = recipientText,
                                    fontSize = 12.sp,
                                    color = ChakhLeTextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEE2E2),
                                    modifier = Modifier.clickable { viewModel.resetAuthState() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Change phone number",
                                            tint = ChakhLeRedPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Change",
                                            fontSize = 11.sp,
                                            color = ChakhLeRedPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Delivery status callout banner
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (viewModel.realOtpManager.isEmailSession) Color(0xFFFFF7ED) else Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (viewModel.realOtpManager.isEmailSession) Color(0xFFFFEDD5) else Color(0xFFBFDBFE)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (viewModel.realOtpManager.isEmailSession) Icons.Default.Email else Icons.Default.Sms,
                                        contentDescription = "Delivered",
                                        tint = if (viewModel.realOtpManager.isEmailSession) Color(0xFFEA580C) else Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = otpSuccessMessage ?: "Verification code dispatched. Please check your inbox.",
                                        fontSize = 11.sp,
                                        color = if (viewModel.realOtpManager.isEmailSession) Color(0xFF9A3412) else Color(0xFF1E40AF),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Expiry timer & Attempts left indicator
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val minutes = otpValiditySeconds / 60
                                val seconds = otpValiditySeconds % 60
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Timer",
                                        tint = if (otpValiditySeconds < 60) Color(0xFFDC2626) else ChakhLeTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Expires in: %02d:%02d".format(minutes, seconds),
                                        fontSize = 11.sp,
                                        color = if (otpValiditySeconds < 60) Color(0xFFDC2626) else ChakhLeTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Attempts",
                                        tint = if (attemptsRemaining <= 1) Color(0xFFDC2626) else ChakhLeTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Attempts left: $attemptsRemaining",
                                        fontSize = 11.sp,
                                        color = if (attemptsRemaining <= 1) Color(0xFFDC2626) else ChakhLeTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Segmented 6-digit Visual Boxes
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 6) {
                                    val digit = if (i < otpCode.length) otpCode[i].toString() else ""
                                    val isCurrentSlot = i == otpCode.length
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (digit.isNotEmpty()) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                                            .border(
                                                width = if (isCurrentSlot) 2.dp else 1.5.dp,
                                                color = if (isCurrentSlot) ChakhLeRedPrimary else if (digit.isNotEmpty()) ChakhLeRedPrimary else Color(0xFF94A3B8),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (digit.isNotEmpty()) {
                                            Text(
                                                text = digit,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = ChakhLeTextPrimary
                                            )
                                        } else {
                                            Text(
                                                text = "•",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentSlot) ChakhLeRedPrimary else Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }

                            // Direct OTP Input Field
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { input ->
                                    if (input.length <= 6 && input.all { char -> char.isDigit() }) {
                                        viewModel.setOtpCode(input)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input_field")
                                    .padding(top = 8.dp),
                                placeholder = {
                                    Text(
                                        text = "Type 6-digit OTP here",
                                        color = ChakhLeTextMuted,
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "OTP",
                                        tint = ChakhLeRedPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                textStyle = TextStyle(
                                    color = ChakhLeTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ChakhLeTextPrimary,
                                    unfocusedTextColor = ChakhLeTextPrimary,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = ChakhLeRedPrimary,
                                    focusedBorderColor = ChakhLeRedPrimary,
                                    unfocusedBorderColor = ChakhLeBorder,
                                    focusedPlaceholderColor = ChakhLeTextMuted,
                                    unfocusedPlaceholderColor = ChakhLeTextMuted
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Resend OTP row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (otpTimer > 0) {
                                    Text(
                                        text = "Resend OTP in ${otpTimer}s",
                                        fontSize = 11.sp,
                                        color = ChakhLeTextMuted
                                    )
                                } else {
                                    TextButton(
                                        onClick = { viewModel.sendOtp(activity, isResend = true) },
                                        enabled = !authLoading
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Resend",
                                            tint = ChakhLeRedPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Resend OTP", color = ChakhLeRedPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.verifyOtpAndLogin() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("verify_otp_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                                enabled = otpCode.length == 6 && !authLoading && attemptsRemaining > 0
                            ) {
                                if (authLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verifying Real OTP...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(
                                        text = "Verify OTP & Continue",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Carrier SMS Notice if any
                            AnimatedVisibility(visible = carrierSmsNotice != null) {
                                Surface(
                                    color = Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "SMS Dispatch Notice",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF991B1B)
                                            )
                                            Text(
                                                text = carrierSmsNotice ?: "",
                                                fontSize = 11.sp,
                                                color = Color(0xFFB91C1C),
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            if (carrierSmsNotice != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Carrier SMS Status",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF991B1B)
                                            )
                                            Text(
                                                text = carrierSmsNotice ?: "",
                                                fontSize = 11.sp,
                                                color = Color(0xFFB91C1C),
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }


                        }
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-detect GPS Location Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ChakhLeSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ChakhLeRedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGpsDetecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = ChakhLeRedPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "GPS",
                                    tint = ChakhLeRedPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Delivery Address",
                                fontSize = 11.sp,
                                color = ChakhLeTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentAddress,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeTextPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.autoDetectGpsLocation() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeRedPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Detect GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        }

        // Actions & Public Customer Ordering
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            if (selectedAuthTab == 0) {
                Button(
                    onClick = { viewModel.continueAsGuest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("public_order_browse_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse Food & Order (Public View)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { selectedAuthTab = 0 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeTextPrimary)
                ) {
                    Text("Return to Public Customer View", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = ChakhLeTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Public Order View by default • Staff authenticated by Role",
                    fontSize = 11.sp,
                    color = ChakhLeTextMuted
                )
            }
        }

        // SMS Gateway Configuration Dialog
        if (showGatewaySettingsDialog) {
            var tempGateway by remember { mutableStateOf(selectedGateway) }
            var tempApiKey by remember { mutableStateOf(viewModel.getGatewayApiKey()) }
            val twilioConfig = remember { viewModel.getTwilioDetails() }
            var tempTwilioSid by remember { mutableStateOf(twilioConfig.first) }
            var tempTwilioToken by remember { mutableStateOf(twilioConfig.second) }
            var tempTwilioFrom by remember { mutableStateOf(twilioConfig.third) }

            AlertDialog(
                onDismissRequest = { showGatewaySettingsDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = ChakhLeRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SMS Delivery Gateway", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Choose the carrier SMS gateway for customer phone verification:",
                            fontSize = 12.sp,
                            color = ChakhLeTextSecondary
                        )

                        for (gateway in SmsGatewayType.values()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { tempGateway = gateway },
                                color = if (tempGateway == gateway) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (tempGateway == gateway) 1.5.dp else 1.dp,
                                    color = if (tempGateway == gateway) ChakhLeRedPrimary else ChakhLeBorder
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = tempGateway == gateway,
                                        onClick = { tempGateway = gateway },
                                        colors = RadioButtonDefaults.colors(selectedColor = ChakhLeRedPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = gateway.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ChakhLeTextPrimary
                                        )
                                        Text(
                                            text = when (gateway) {
                                                SmsGatewayType.FIREBASE_PHONE -> "Official Google carrier SMS to mobile numbers"
                                                SmsGatewayType.FAST2SMS -> "Indian Carrier SMS via Fast2SMS Quick API"
                                                SmsGatewayType.TWO_FACTOR -> "Carrier SMS via 2Factor.in Telecom API"
                                                SmsGatewayType.TWILIO -> "Global Real SMS via Twilio API"
                                            },
                                            fontSize = 10.sp,
                                            color = ChakhLeTextMuted
                                        )
                                    }
                                }
                            }
                        }

                        if (tempGateway == SmsGatewayType.FAST2SMS) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Fast2SMS Authorization Key",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeTextPrimary
                            )
                            Text(
                                text = "• Fast2SMS transmits live carrier SMS directly to Indian mobile phone numbers.\n• Find or regenerate your key anytime at fast2sms.com → Dev API",
                                fontSize = 10.sp,
                                color = Color(0xFF6B7280),
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = tempApiKey,
                                onValueChange = { tempApiKey = it },
                                placeholder = { Text("Paste Authorization Key here", fontSize = 12.sp) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            val clip = clipboardManager.getText()?.text?.trim() ?: ""
                                            if (clip.isNotEmpty()) {
                                                tempApiKey = clip
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            tint = ChakhLeRedPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        if (tempGateway == SmsGatewayType.TWO_FACTOR) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "2Factor.in API Key",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChakhLeTextPrimary
                            )
                            OutlinedTextField(
                                value = tempApiKey,
                                onValueChange = { tempApiKey = it },
                                placeholder = { Text("Paste your 2Factor API Key here", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        if (tempGateway == SmsGatewayType.TWILIO) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Twilio Account SID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = tempTwilioSid,
                                onValueChange = { tempTwilioSid = it },
                                placeholder = { Text("ACxxxxxxxxxxxxxxxxxxxx", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Text("Twilio Auth Token", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = tempTwilioToken,
                                onValueChange = { tempTwilioToken = it },
                                placeholder = { Text("Auth Token", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Text("Twilio From Number", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = tempTwilioFrom,
                                onValueChange = { tempTwilioFrom = it },
                                placeholder = { Text("+1234567890", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.setSmsGateway(tempGateway)
                            if (tempGateway == SmsGatewayType.FAST2SMS || tempGateway == SmsGatewayType.TWO_FACTOR) {
                                viewModel.saveGatewayApiKey(tempApiKey)
                            } else if (tempGateway == SmsGatewayType.TWILIO) {
                                viewModel.saveTwilioConfig(tempTwilioSid, tempTwilioToken, tempTwilioFrom)
                            }
                            showGatewaySettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save & Apply", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGatewaySettingsDialog = false }) {
                        Text("Cancel", color = ChakhLeTextSecondary)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }

        // Firebase Setup Guide Dialog
        if (showFirebaseGuideDialog) {
            AlertDialog(
                onDismissRequest = { showFirebaseGuideDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Firebase SMS Setup Guide", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "WHY CARRIER SMS IS NOT ARRIVING:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Google Firebase Phone Auth requires enabling the 'Phone' sign-in provider and registering the app's SHA-256 fingerprint in Firebase Console. Without these, carrier SMS cannot be dispatched.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Text("App Configuration (Tap to Copy):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        // Project & Package
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Firebase Project: chakhle-6df6b", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChakhLeTextPrimary)
                                Text("Package Name: com.aistudio.chakhle.xvdzpq", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChakhLeTextPrimary)
                            }
                        }

                        // SHA-256 Fingerprint
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SHA-256 (Required for SMS):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString("44:36:3D:9A:F6:AC:AE:E3:AE:F3:27:4A:BC:4F:D6:86:CD:E1:F9:5A:47:AC:B5:61:C2:46:6D:33:ED:51:4E:B5"))
                                            Toast.makeText(context, "SHA-256 copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Copy SHA-256", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                    }
                                }
                                Text(
                                    text = "44:36:3D:9A:F6:AC:AE:E3:AE:F3:27:4A:BC:4F:D6:86:CD:E1:F9:5A:47:AC:B5:61:C2:46:6D:33:ED:51:4E:B5",
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }

                        // SHA-1 Fingerprint
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SHA-1 Fingerprint:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChakhLeTextPrimary)
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString("A2:1C:9D:E7:25:A0:27:22:CC:19:BA:10:82:FD:1F:77:5B:14:33:B3"))
                                            Toast.makeText(context, "SHA-1 copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Copy SHA-1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "A2:1C:9D:E7:25:A0:27:22:CC:19:BA:10:82:FD:1F:77:5B:14:33:B3",
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = ChakhLeTextSecondary
                                )
                            }
                        }

                        Text("Steps to Enable Carrier SMS in Firebase:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("1. Open console.firebase.google.com and open project 'chakhle-6df6b'.", fontSize = 11.sp, color = ChakhLeTextSecondary)
                            Text("2. Click 'Authentication' -> 'Sign-in method' -> Click 'Phone' -> Enable.", fontSize = 11.sp, color = ChakhLeTextSecondary)
                            Text("3. Go to Project Settings (gear icon) -> General -> scroll down to 'Your apps' -> click 'Add fingerprint' -> Paste SHA-256.", fontSize = 11.sp, color = ChakhLeTextSecondary)
                            Text("4. (Live Public Deployment): In Firebase Console, ensure the Phone provider is active with the registered SHA-256 fingerprint for live cellular SMS delivery to all public mobile numbers.", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showFirebaseGuideDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Got it", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}
