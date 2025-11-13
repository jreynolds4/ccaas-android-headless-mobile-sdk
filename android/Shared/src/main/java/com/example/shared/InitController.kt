package com.example.shared

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import com.ccaiplatform.ccaichat.initializeChat
import com.ccaiplatform.ccaikit.CCAI
import com.ccaiplatform.ccaikit.InitOptions
import com.ccaiplatform.ccaikit.models.screenShare.ScreenShareOptions
import com.ccaiplatform.ccaiscreenshare.initializeScreenShare
import org.json.JSONObject
import kotlin.system.exitProcess

class InitController private constructor() {
    private var key: String = ""
    private var urlHost: String = ""
    private var screenShareKey: String = ""
    private var screenShareDomain: String = ""

    companion object {
        val instance: InitController by lazy { InitController() }
    }

    private fun loadEnvironment(context: Context) {
        try {
            val inputStream = context.assets.open("environment.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val obj = JSONObject(json)
            key = obj.optString("key", "")
            urlHost = obj.optString("hostname", "")
            screenShareKey = obj.optString("screenShareKey", "")
            screenShareDomain = obj.optString("screenShareDomain", "")
        } catch (e: Exception) {
            Log.e("InitController", "Failed to load environment: ${e.localizedMessage}")
            showEnvironmentErrorAlert(context)
        }
    }

    private fun showEnvironmentErrorAlert(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Configuration Error")
            .setMessage("Environment configuration file 'environment.json' not found or invalid in android/Shared/src/main/assets. Please check your configuration.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                exitProcess(1)
            }
            .setCancelable(false)
            .show()
    }

    fun initializeSDK(context: Context) {
        loadEnvironment(context)
        val authController = AuthController()
        val options = InitOptions(key, urlHost, authController)
        options.cacheAuthToken = false

        // Initialize screen share options
        val screenShareOptions = ScreenShareOptions(screenShareKey, screenShareDomain)
        try {
            CCAI.initialize(context, options)
            CCAI.initializeChat(context, null, "en")
            //If you want to initialize screen share at the start, uncomment the line below
            CCAI.initializeScreenShare(context, screenShareOptions)
            Log.v("InitController", "CCAI initialized successfully")
        } catch (e: Exception) {
            Log.e("InitController", "Failed to initialize CCAI: ${e.localizedMessage}")
        }
    }
}
