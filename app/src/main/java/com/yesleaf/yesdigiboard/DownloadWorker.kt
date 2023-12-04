package com.yesleaf.yesdigiboard

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask


class DownloadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
	private val workManager = WorkManager.getInstance(context)
	lateinit var tsks: List<DeviceTask>

	override fun doWork(): Result {
		val code = inputData.getString("CODE")
		val folderPath: String = applicationContext.filesDir.toString()
		return try {
			if (hasInternet())
				loadFile(folderPath, code + "tv.json", "config.json")
			if (checkConfigFile(folderPath)) {
				tsks.forEach {
					if (it.video != "") loadFile(folderPath, it.video)
				}

                val cfgBuilder = OneTimeWorkRequestBuilder<TaskWorker>()
                workManager.beginUniqueWork( "TASK-CONFIG",    ExistingWorkPolicy.REPLACE,                    cfgBuilder.build()).enqueue()
			}
			Result.success()
		} catch (exception: Exception) {
			exception.printStackTrace()
			Result.failure()
		}
	}

/*
		var playVideoTask: TimerTask = object : TimerTask() {
			override fun run() {
				Log.d("playVideoTask", "run--")
				val appContext = applicationContext
				val video = inputData.getString("VIDEO")
				Log.d("playVideoTask", curTask.video)
				val intent = Intent(appContext, PlayVideoActivity::class.java)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
				intent.putExtra("VIDEO", curTask.video)
				appContext?.startActivity(intent)
			}
		}

		var sleepTask: TimerTask = object : TimerTask() {
			override fun run() {
				Log.d("sleepTask", "run--")
				val appContext = applicationContext
				val powerManager = appContext.getSystemService<PowerManager>()
				val wakeLock =
					powerManager?.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK,
						"MyApp::MyWakelockTag"
					)
				wakeLock?.acquire()
				/*
				val layoutParams = window.attributes
				layoutParams.screenBrightness = 0f
				window.attributes = layoutParams

				 */
			}
		}
 */

		fun checkConfigFile(folderPath: String): Boolean {
			val settingFilepath: String = folderPath + "/config.json"
			val settingFile = File(settingFilepath)

			if (settingFile.exists()) {
				var device: DeviceConfig =
					Json.decodeFromString<DeviceConfig>(settingFile.readText())
				tsks = device.tasks.sortedWith(compareBy({ it.time.hour }, { it.time.minute }))
			}
			return settingFile.exists()
		}

		fun loadFile(folderPath: String, filename: String, savedFileName: String = "") {
			var strfil: String = savedFileName
			if (savedFileName == "") strfil = filename
			val savedFile = File( folderPath + "/" + strfil)
			if ( savedFile.exists() && savedFileName!= "config.json") return

			try {
				Log.d("loadFile", filename)
				val url: URL = URL("https://ironchef.ca/c/" + filename)
				var urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
				try {
					urlConnection.requestMethod = "GET"
					val innn: InputStream = urlConnection.inputStream
					savedFile.writeBytes(innn.readBytes())
				} catch (e: Exception) {
					e.printStackTrace()
				} finally {
					urlConnection.disconnect()
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		fun hasInternet(): Boolean {
			val appContext = applicationContext
			val connectivityManager =
				appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
			if (connectivityManager != null) {
				val capabilities =
					connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
				if (capabilities != null) {
					if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
						Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
						return true
					} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
						Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
						return true
					} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
						Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
						return true
					}
				}
			}
			return false
		}

	}