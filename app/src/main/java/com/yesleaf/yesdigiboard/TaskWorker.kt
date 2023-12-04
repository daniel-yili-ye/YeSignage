package com.yesleaf.yesdigiboard

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.view.Window
import androidx.core.content.getSystemService
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TaskWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val workManager = WorkManager.getInstance(context)
    lateinit var tsks: List<DeviceTask>

    override fun doWork(): Result {
        val folderPath: String = applicationContext.filesDir.toString()
        checkConfigFile(folderPath)
        return try {
            val appContext = applicationContext
            var  curTask=  getTask (false)
            Log.d("TaskWorker - run", curTask.action +  " > " + curTask.time.toString() +  " > " + curTask.video)

            if (curTask.action =="play" && curTask.video!="") {
                val intent = Intent(applicationContext, PlayVideoActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("VIDEO", curTask.video)
                appContext?.startActivity(intent)
            }else if (curTask.action =="sleep"){
                Log.d("sleepTask", "run--")
/*            val powerManager = appContext.getSystemService<PowerManager>()

           val wakeLock = powerManager?.newWakeLock(
                   PowerManager.PARTIAL_WAKE_LOCK,
                   "MyApp::MyWakelockTag"
               )
           wakeLock?.acquire()
           wakeLock?.release()

           val layoutParams = Wow.attributes
           layoutParams.screenBrightness = 0f
           window.attributes = layoutParams

            */
       }

       setNextTask()
       Result.success()
   } catch (exception: Exception) {
       exception.printStackTrace()
       Result.failure()
   }
}

fun setNextTask() {
  var nextTask=  getTask()
   Log.d("setNextTask - run", nextTask.action +  " > " + nextTask.time.toString() +  " > " + nextTask.video)
   val calNow = Calendar.getInstance()
   val cal = Calendar.getInstance()
   cal.set(Calendar.HOUR_OF_DAY, nextTask.time.hour)
   cal.set(Calendar.MINUTE, nextTask.time.minute)
   cal.set(Calendar.SECOND, nextTask.time.second)
   if (cal.timeInMillis < calNow.timeInMillis) {
       cal.add(Calendar.DATE, 1)
   }

   val cfgBuilder = OneTimeWorkRequestBuilder<TaskWorker>()
   cfgBuilder.setInitialDelay(cal.timeInMillis - calNow.timeInMillis+70000, TimeUnit.MILLISECONDS)
   workManager.beginUniqueWork( "TASK-CONFIG",    ExistingWorkPolicy.REPLACE, cfgBuilder.build()).enqueue()
}

fun checkConfigFile(folderPath: String): Boolean {
   val settingFilepath: String = folderPath + "/config.json"
   val settingFile = File(settingFilepath)

   if (settingFile.exists()) {
       var device: DeviceConfig =
           Json.decodeFromString(settingFile.readText())
       tsks = device.tasks.sortedWith(compareBy({ it.time.hour }, { it.time.minute }))
   }
   return settingFile.exists()
}

fun getTask(next: Boolean = true): DeviceTask {
   val cal = Calendar.getInstance()
   var hour: Int = cal.get(Calendar.HOUR_OF_DAY)
   var minute: Int = cal.get(Calendar.MINUTE)
   Log.d("getTask - current time", cal.toString() )

   if (next) {
       var ls =
           tsks.filter { it.time.hour > hour || (it.time.hour == hour && it.time.minute > minute) }

       if (ls.count() > 0)
           return ls.first()
       else
           return tsks.first()
   } else {
       var ls =
           tsks.filter { it.time.hour < hour || (it.time.hour == hour && it.time.minute < minute) }

       if (ls.count() > 0)
           return ls.last()
       else
           return tsks.last()
   }
}
}