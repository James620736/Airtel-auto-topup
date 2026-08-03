package com.example.autotopup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE)
        val senderMatch = prefs.getString(Prefs.SENDER_MATCH, "AirtelMoney") ?: "AirtelMoney"
        val amount = prefs.getString(Prefs.AMOUNT, "20") ?: "20"
        val ussdTemplate = prefs.getString(Prefs.USSD_TEMPLATE, "*141*1*1*{phone}*{amount}#")
            ?: "*141*1*1*{phone}*{amount}#"

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (msg in messages) {
            val from = msg.originatingAddress ?: ""
            val body = msg.messageBody ?: ""

            if (!from.contains(senderMatch, ignoreCase = true) &&
                !body.contains(senderMatch, ignoreCase = true)
            ) continue

            if (!body.contains("received", ignoreCase = true)) continue

            val senderPhone = extractPhoneNumber(body) ?: continue

            dialUssd(context, ussdTemplate, senderPhone, amount)
            Log.i("SmsReceiver", "Triggered top-up of $amount to $senderPhone")
        }
    }

    private fun extractPhoneNumber(body: String): String? {
        val regex = Regex("""(2547\d{8}|07\d{8}|01\d{8})""")
        return regex.find(body)?.value
    }

    private fun dialUssd(context: Context, template: String, phone: String, amount: String) {
        val ussd = template
            .replace("{phone}", phone)
            .replace("{amount}", amount)

        val encoded = Uri.encode(ussd)
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encoded")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            ContextCompat.startActivity(context, callIntent, null)
        } catch (e: SecurityException) {
            Log.e("SmsReceiver", "CALL_PHONE permission not granted", e)
        }
    }
}
