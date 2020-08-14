package com.momentolabs.app.security.applocker.service

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.widget.Toast
import com.momentolabs.app.security.applocker.notification.ServiceNotificationManager
import com.momentolabs.app.security.applocker.receivers.CallReceiver
import com.momentolabs.app.security.applocker.util.ServiceStarter

private val tag = CallReceiverService::class.java.simpleName
const val restartBroadcastAction = "com.japps.restartCallReceiverService"

class CallReceiverService : IntentService(CallReceiverService::class.java.simpleName) {
    private lateinit var callReceiverBroadcast: CallReceiver

    override fun onCreate() {
        Toast.makeText(baseContext, "CallReceiverService Started", Toast.LENGTH_SHORT).show()
        registerCallReceiver()
        initializeNotification()
    }

    private fun initializeNotification() {
        val notification = ServiceNotificationManager(applicationContext).createNotification()
        startForeground(NOTIFICATION_ID_CALLRECEIVER_SERVICE, notification)
    }

    private fun registerCallReceiver() {
        isReceiverRegistered = true
        callReceiverBroadcast = CallReceiver()
        val intentFilter = IntentFilter("android.intent.action.PHONE_STATE")
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL")
        registerReceiver(callReceiverBroadcast, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isReceiverRegistered)
            registerCallReceiver()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onHandleIntent(intent: Intent?) {
        TODO("Not yet implemented")
    }

    private fun unregisterCallReceiverAndRestartReceiver() {
        unregisterCallReceiver()
        Intent(baseContext, CallReceiverServiceRestarter::class.java).let {
            it.action = restartBroadcastAction
            sendBroadcast(it)
            Toast.makeText(baseContext, "Destroyed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        unregisterCallReceiverAndRestartReceiver()
    }

    override fun onDestroy() {
        unregisterCallReceiverAndRestartReceiver()
        super.onDestroy()
    }

//    override fun onDestroy() {
//        com.momentolabs.app.security.applocker.util.ServiceStarter.startCallReceiverService(applicationContext)
//        unregisterCallReceiver()
////        Log.e(com.momentolabs.app.security.applocker.service.getTag, "com.momentolabs.app.security.applocker.service.CallReceiverService destroyed at ${Calendar.getInstance().time}}")
////        Toast.makeText(this, "com.momentolabs.app.security.applocker.service.CallReceiverService Stopped", Toast.LENGTH_SHORT).show()
////        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
////        val interval = System.currentTimeMillis() + 10000L
////        val intent = Intent(this, com.momentolabs.app.security.applocker.service.CallReceiverServiceRestarter::class.java)
////        val pendingIntent = PendingIntent.getBroadcast(baseContext, 0, intent, 0)
////        alarmManager.set(AlarmManager.RTC_WAKEUP, interval, pendingIntent)
//        // TODO: 8/11/2020 if service gets destroyed then set the alarmManager and start this service again after 5 seconds
//        super.onDestroy()
//    }

    private fun unregisterCallReceiver() {
        if (isReceiverRegistered) {
            isReceiverRegistered = false
            unregisterReceiver(callReceiverBroadcast)
        }
    }

    companion object {
        private var isReceiverRegistered = false
        private const val NOTIFICATION_ID_CALLRECEIVER_SERVICE = 109
    }
}

class CallReceiverServiceRestarter : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (!intent?.action.equals(restartBroadcastAction))
            return
        val cntx = context ?: context?.applicationContext
        cntx?.also {
            ServiceStarter.startCallReceiverService(it)
            Toast.makeText(cntx, "Service Restarted", Toast.LENGTH_SHORT).show()
        }
    }
}