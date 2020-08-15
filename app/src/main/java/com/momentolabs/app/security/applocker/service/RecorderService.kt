package com.momentolabs.app.security.applocker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.MODE_IN_CALL
import android.media.AudioManager.MODE_IN_COMMUNICATION
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.momentolabs.app.security.applocker.R
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

open class RecorderService : IntentService(RecorderService::class.simpleName) {
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var newFileName: String
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "Recorder started", Toast.LENGTH_SHORT).show()
        newFileName = generateFileName()
        val audioManager: AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.mode == MODE_IN_CALL || audioManager.mode == MODE_IN_COMMUNICATION)
            Log.e("Voice Call", "Voice call active")
        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
            0
        )
        mediaRecorder = MediaRecorder()
        val outputFile = "${externalCacheDir?.absolutePath}/$newFileName.amr"
        Log.e("Service", "output -> $outputFile")
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile)
        }

        try {
            mediaRecorder.prepare()
            Thread.sleep(2000)
            mediaRecorder.start()
        } catch (ise: IllegalStateException) {
            Log.e("Service", "Exception -> $ise")
        } catch (ioe: IOException) {
            Log.e("Service", "Exception -> $ioe")
        } catch (exe: Exception) {
            Log.e("Service", "Exception -> $exe")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            val notification = Notification.Builder(this, "default_channel")
                .setContentTitle("Securing call")
                .setContentText("Call volume boost activated")
                .setSmallIcon(R.drawable.ic_ring_volume_24px)
                .build()
            startForeground(10, notification)
        }
        return Service.START_STICKY
    }

    private fun generateFileName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dateTime = LocalDateTime.now()
            dateTime.format(
                DateTimeFormatter.ofLocalizedDateTime(
                    FormatStyle.LONG,
                    FormatStyle.SHORT
                )
            )
        } else {
            "audiotest"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channelId = "default_channel"
            val channel = NotificationChannel(channelId, name, importance)
            channel.enableLights(false)
            channel.setSound(null, null)
            channel.setShowBadge(false)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder.stop()
        mediaRecorder.release()
        Log.e("Service", "Destroyed")
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.e("Service", "OnHandleIntent")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}