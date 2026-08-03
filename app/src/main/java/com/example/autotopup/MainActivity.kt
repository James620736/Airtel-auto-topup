package com.example.autotopup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.CALL_PHONE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        val amountInput = findViewById<EditText>(R.id.amountInput)
        val ussdInput = findViewById<EditText>(R.id.ussdInput)
        val senderInput = findViewById<EditText>(R.id.senderInput)
        val statusText = findViewById<TextView>(R.id.statusText)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val permissionButton = findViewById<Button>(R.id.permissionButton)

        amountInput.setText(prefs.getString(Prefs.AMOUNT, "20"))
        ussdInput.setText(
            prefs.getString(Prefs.USSD_TEMPLATE, "*141*1*1*{phone}*{amount}#")
        )
        senderInput.setText(prefs.getString(Prefs.SENDER_MATCH, "AirtelMoney"))

        updatePermissionStatus(statusText)

        permissionButton.setOnClickListener {
            ActivityCompat.requestPermissions(this, requiredPermissions, 100)
        }

        saveButton.setOnClickListener {
            prefs.edit()
                .putString(Prefs.AMOUNT, amountInput.text.toString().trim())
                .putString(Prefs.USSD_TEMPLATE, ussdInput.text.toString().trim())
                .putString(Prefs.SENDER_MATCH, senderInput.text.toString().trim())
                .apply()
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        updatePermissionStatus(findViewById(R.id.statusText))
    }

    private fun updatePermissionStatus(statusText: TextView) {
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        statusText.text = if (allGranted) {
            "Permissions granted. Auto top-up is active."
        } else {
            "Permissions needed — tap 'Grant permissions' below."
        }
    }
}

object Prefs {
    const val NAME = "auto_topup_prefs"
    const val AMOUNT = "amount"
    const val USSD_TEMPLATE = "ussd_template"
    const val SENDER_MATCH = "sender_match"
}
