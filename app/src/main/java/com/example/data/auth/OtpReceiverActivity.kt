package com.example.data.auth

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast

class OtpReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val code = intent.getStringExtra("otp_code")
        if (!code.isNullOrBlank()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Khaibu OTP", code)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(this, "OTP $code copied to clipboard! Paste in Khaibu app.", Toast.LENGTH_LONG).show()
        }
        finish()
    }
}
