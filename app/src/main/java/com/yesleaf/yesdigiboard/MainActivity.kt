package com.yesleaf.yesdigiboard

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Loads [MainFragment].
 */
class MainActivity : Activity() {
    private val workManager = WorkManager.getInstance(application)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setContentView(R.layout.activity_main)
        val btnGo: Button = findViewById(R.id.btnGo)
        val passcode: EditText = findViewById(R.id.tetPasscode)
        if (checkConfigFile()) {
            if (!hasInternet()){
                startWork(passcode.text.toString())
            }
        }
        btnGo.isClickable=true
        passcode.isEnabled=true

        btnGo.setOnClickListener {
            Log.d("MainActivity>> enter   ...", passcode.text.toString() )
            hideKeyboard(this.findViewById(R.id.main_browse_fragment))
            passcode.isEnabled=false
            btnGo.setText("Loading")
            startWork(passcode.text.toString())
     }
 }

    override fun onResume() {
        super.onResume()
        val btnGo: Button = findViewById(R.id.btnGo)
        btnGo.setText("Go")
    }

    fun startWork(code: String){
    val cfgBuilder = OneTimeWorkRequestBuilder<DownloadWorker>()
    val builder = Data.Builder()
    builder.putString("CODE", code)
    cfgBuilder.setInputData(builder.build())

    workManager.beginUniqueWork(
            "LOAD-CONFIG",
            ExistingWorkPolicy.REPLACE,
            cfgBuilder.build()
        ).enqueue()
}
    fun hasInternet() : Boolean{
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

 fun hideKeyboard(view: View) {
     val inputMethodManager: InputMethodManager =            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
     inputMethodManager.hideSoftInputFromWindow(            view.windowToken,            0      )
 }

  fun checkConfigFile():Boolean {
     lateinit var device : DeviceConfig

     val settingFilepath: String = filesDir.absolutePath.toString() + "/config.json"
     Log.d("MainActivity>>", settingFilepath)
     val settingFile = File(settingFilepath)

     val passcode: EditText = findViewById(R.id.tetPasscode)
     if (settingFile.exists()) {
         device = Json.decodeFromString<DeviceConfig>(settingFile.readText())
         passcode.setText(device.code)
     }

     return settingFile.exists()
 }
}