package com.example.xml_example.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import co.ccai.example.xml_example.R

class LoadingDialog(
    context: Context,
    private val message: String? = null
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)

        val textMessage = findViewById<TextView>(R.id.textMessage)
        if (!message.isNullOrEmpty()) {
            textMessage.text = message
            textMessage.visibility = android.view.View.VISIBLE
        } else {
            textMessage.visibility = android.view.View.GONE
        }

        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }
}
