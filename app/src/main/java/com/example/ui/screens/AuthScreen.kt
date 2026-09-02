package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val otpCode by viewModel.otpCode.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val otpTimer by viewModel.otpResendSeconds.collectAsState()
    val currentAddress by viewModel.currentAddressTitle.collectAsState()
    val isGpsDetecting by viewModel.isGpsDetecting.collectAsState()

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
            Spacer(modifier = Modifier.height(24.dp))

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
                    contentDescription = "ChakhLe Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ChakhLe",
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

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Card
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
                        text = if (!isOtpSent) "Enter Mobile Number" else "Verify 4-Digit OTP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChakhLeTextPrimary
                    )

                    Text(
                        text = if (!isOtpSent)
                            "We will send a 4-digit verification code"
                        else
                            "OTP sent to +91 ${phoneNumber.ifBlank { "98765 12345" }}",
                        fontSize = 12.sp,
                        color = ChakhLeTextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    if (!isOtpSent) {
                        // Phone input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) viewModel.setPhoneNumber(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input_field"),
                            placeholder = { Text("Enter 10-digit mobile number") },
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                ) {
                                    Text(
                                        text = "+91",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ChakhLeRedPrimary,
                                unfocusedBorderColor = ChakhLeBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.sendOtp() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("send_otp_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                            enabled = phoneNumber.length == 10
                        ) {
                            Text(
                                text = "Get Verification OTP",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // OTP Input
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) viewModel.setOtpCode(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input_field"),
                            placeholder = { Text("Enter 4-digit code") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "OTP",
                                    tint = ChakhLeRedPrimary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ChakhLeRedPrimary,
                                unfocusedBorderColor = ChakhLeBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ChakhLeAmberLight,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { viewModel.setOtpCode("7294") }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ChakhLeAmberDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Auto-fill SMS: 7294",
                                        color = ChakhLeAmberDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (otpTimer > 0) {
                                Text(
                                    text = "Resend SMS in ${otpTimer}s",
                                    fontSize = 11.sp,
                                    color = ChakhLeTextMuted
                                )
                            } else {
                                TextButton(onClick = { viewModel.sendOtp() }) {
                                    Text("Resend OTP", color = ChakhLeRedPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.verifyOtpAndLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("verify_otp_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChakhLeRedPrimary),
                            enabled = otpCode.length >= 4
                        ) {
                            Text(
                                text = "Verify & Proceed to Food",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
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

        // Demo Bypass & Terms
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.continueAsGuest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChakhLeTextPrimary)
            ) {
                Text("Continue as Guest User", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = ChakhLeTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "100% Safe & Secure Food Ordering",
                    fontSize = 11.sp,
                    color = ChakhLeTextMuted
                )
            }
        }
    }
}
