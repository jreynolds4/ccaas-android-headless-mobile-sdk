package com.example.xml_example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.ccai.example.xml_example.R
import com.ccaiplatform.ccaichat.model.ChatResponse
import com.ccaiplatform.ccaiui.CCAIUI
import com.example.shared.InitController
import com.example.xml_example.util.StatusBarController
import com.example.xml_example.view.ChatFragment
import com.example.xml_example.view.HomeFragment

class MainActivity : AppCompatActivity(), StatusBarController {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        InitController.instance.initializeSDK(this)
        // Initialize CCAIUI for push notifications
        CCAIUI.initialize(this)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    override fun updateStatusBarColor(color: Int) {
        window.statusBarColor = color
    }

    fun navigateToChat(menuId: Int, chat: ChatResponse?) {
        val chatFragment = ChatFragment().apply {
            arguments = Bundle().apply {
                putInt("menuId", menuId)
                putParcelable("chat", chat)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, chatFragment)
            .addToBackStack(null)
            .commit()
    }
}
