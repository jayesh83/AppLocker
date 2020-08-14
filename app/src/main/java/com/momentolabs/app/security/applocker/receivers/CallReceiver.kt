package com.momentolabs.app.security.applocker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager.*
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.momentolabs.app.security.applocker.service.RecorderService

class CallReceiver : BroadcastReceiver() {
    companion object{
        private var idle: Boolean = false
        private var rang: Boolean = false
        private var offhook: Boolean = false
        private var ongoingCall = false
        private var anotherCall = false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "There's Call", Toast.LENGTH_LONG).show()
        if (!intent?.action.equals("android.intent.action.PHONE_STATE")) {
            context?.also {
                Toast.makeText(context, "Receiver", Toast.LENGTH_LONG).show()
            }
            return
        }
        Log.e("State", "-> $rang, $offhook, $idle")

        when (intent?.getStringExtra(EXTRA_STATE)) {
            EXTRA_STATE_RINGING -> {
                rang = true
                if (ongoingCall)
                    anotherCall = true
                Log.e("Ringing", "Yes")
            }

            EXTRA_STATE_OFFHOOK -> {
                offhook = true

                if (anotherCall) {
                    anotherCall = false
                    return
                }

                if (rang && offhook) {
                    Log.e("Incoming", "Talking")
                    ongoingCall = true
                    startRecorder(context)
                }

                if (!rang && offhook) {
                    ongoingCall = true
                    startRecorder(context)
                    Log.e("Outgoing", "outgoing yes")
                }

            }

            EXTRA_STATE_IDLE -> {
                idle = true

                Log.e("Idle", "Yes")
                if (rang && offhook) {
                    Log.e("Cut", "talked and cutted at last")
                    ongoingCall = false
                    stopRecorder(context)
                }

                if (rang && !offhook)
                    Log.e("Cut", "cutted call or didn't pickup or caller cutted the call")

                if (!rang && offhook && idle) {
                    Log.e(
                        "Cut",
                        "outgoing and talked at last cutted or recepient cutted the call no talks"
                    )
                    ongoingCall = false
                    stopRecorder(context)
                }

                if (!rang && !offhook && idle)
                    Log.e("Cut", "outgoing and didn't talked at last cutted")

                rang = false
                offhook = false
                idle = false
            }
        }
    }

    private fun startRecorder(context: Context?) {
        context?.let {
            val recorderService = Intent(context, RecorderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ContextCompat.startForegroundService(it, recorderService)
            else
                context.startService(recorderService)
        }
    }

    private fun stopRecorder(context: Context?) {
        context?.let {
            val intent = Intent(context, RecorderService::class.java)
            context.stopService(intent)
        }
    }
}