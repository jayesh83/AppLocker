package com.momentolabs.app.security.applocker.util

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class CloudPusher(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
//        Toast.makeText(applicationContext, "Worker running", Toast.LENGTH_SHORT).show()
        Log.e("Cloud checking", "work")

        // Do the work here--in this case, upload the images.
        uploadRecordings()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun uploadRecordings() {
//        Toast.makeText(applicationContext, "Faking uploading", Toast.LENGTH_SHORT).show()
        Log.e("Cloud checking", "Yes")
    }
}
