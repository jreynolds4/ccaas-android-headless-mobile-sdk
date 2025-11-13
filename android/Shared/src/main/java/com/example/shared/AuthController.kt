package com.example.shared

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import com.ccaiplatform.ccaikit.CCAI
import com.ccaiplatform.ccaikit.CCAIListener

class AuthController: CCAIListener {
    /// Replace with your server URL
    ///
    /// ### Example
    /// 1. Copy `server/.env.example` to `server/.env` and fill out secret
    /// 2. Run `node ./server/app.js`
    /// If you want to run in the physical device, tunnel the local server using `ssh -R 80:localhost:3000 ssh.localhost.run`
    /// and replace with the tunnel URL. For example, `https://yourname.lhr.life`
    private val signingBaseUrl = "http://10.0.2.2:3000"

    override suspend fun ccaiShouldAuthenticate(): String? {
        Log.v("AuthController", "ccaiShouldAuthenticate called")
        val jwt = requestJWTForEndUser() ?: return null
        return try {
            CCAI.authService?.authenticate(jwt)
        } catch (e: Exception) {
            Log.v("AuthController", "Authentication failed: ${e.localizedMessage}")
            null
        }
    }

    suspend fun requestJWTForEndUser(): String? = withContext(Dispatchers.IO) {
        val url = URL("$signingBaseUrl/ccai/auth")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val payload = JSONObject(
                mapOf(
                    "name" to "Android User",
                    "identifier" to "android_user",
                    "email" to "android@example.com",
                    "phone" to "+1234567890"
                )
            )

            OutputStreamWriter(connection.outputStream).use { it.write(payload.toString()) }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val response = BufferedReader(connection.inputStream.reader()).use { it.readText() }
                val json = JSONObject(response)
                json.optString("token")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
