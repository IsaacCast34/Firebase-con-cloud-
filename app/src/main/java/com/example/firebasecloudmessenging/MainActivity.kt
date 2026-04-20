package com.example.firebasecloudmessenging

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var tvToken: TextView
    private lateinit var btnCopyToken: Button
    private lateinit var tvMessage: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getFCMToken()
        } else {
            Toast.makeText(this, "No se recibirán notificaciones sin permiso", Toast.LENGTH_LONG).show()
        }
    }

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val title = intent?.getStringExtra("title") ?: "Mensaje"
            val body = intent?.getStringExtra("body") ?: ""
            tvMessage.text = "$title: $body"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvToken = findViewById(R.id.tvToken)
        btnCopyToken = findViewById(R.id.btnCopyToken)
        tvMessage = findViewById(R.id.tvMessage)

        btnCopyToken.setOnClickListener {
            val token = tvToken.text.toString()
            if (token.isNotEmpty() && token != "Cargando token...") {
                copyToClipboard(token)
            }
        }

        checkAndRequestNotificationPermission()

        // Registrar el receiver para mensajes
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, IntentFilter("com.example.firebasecloudmessenging.NEW_MESSAGE"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    getFCMToken()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            getFCMToken()
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                tvToken.text = token
                println("FCM Token: $token")
            } else {
                tvToken.text = "Error obteniendo token"
                println("Error FCM: ${task.exception?.message}")
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FCM Token", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Token copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }
}