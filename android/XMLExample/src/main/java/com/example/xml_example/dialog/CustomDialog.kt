package com.example.xml_example.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import co.ccai.example.xml_example.R

class CustomDialog(
    context: Context,
    private val title: String? = null,
    private val content: String,
    private val dismissTitle: String? = null,
    private val confirmTitle: String? = null,
    private val showDismissButton: Boolean = true,
    private val onDismiss: () -> Unit = {},
    private val onConfirm: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom)

        val textTitle = findViewById<TextView>(R.id.textTitle)
        if (!title.isNullOrBlank()) {
            textTitle.text = title
            textTitle.visibility = android.view.View.VISIBLE
        } else {
            textTitle.visibility = android.view.View.GONE
        }
        findViewById<TextView>(R.id.textContent).text = content

        val buttonDismiss = findViewById<Button>(R.id.buttonDismiss)
        buttonDismiss.text = dismissTitle ?: context.getString(R.string.no)
        buttonDismiss.visibility = if (showDismissButton) android.view.View.VISIBLE else android.view.View.GONE
        buttonDismiss.setOnClickListener {
            dismiss()
            onDismiss()
        }

        val buttonConfirm = findViewById<Button>(R.id.buttonConfirm)
        buttonConfirm.text = confirmTitle ?: context.getString(R.string.yes)
        buttonConfirm.setOnClickListener {
            dismiss()
            onConfirm()
        }
    }
}
