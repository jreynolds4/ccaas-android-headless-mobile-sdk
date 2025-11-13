package com.example.xml_example.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import co.ccai.example.xml_example.R

enum class ButtonType {
    PHOTO, IMAGE
}

class MediaPickerButton : FrameLayout {
    private var buttonAdd: ImageButton? = null
    private var progressBar: ProgressBar? = null
    private var onButtonClickListener: OnButtonClickListener? = null

    interface OnButtonClickListener {
        fun onButtonClick(buttonType: ButtonType)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.widget_media_picker_button, this, true)
        buttonAdd = findViewById<ImageButton>(R.id.buttonAdd)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)

        buttonAdd?.setOnClickListener(OnClickListener { v: View? -> showMenu() })
    }

    fun setOnButtonClickListener(listener: OnButtonClickListener?) {
        this.onButtonClickListener = listener
    }

    private fun showMenu() {
        val popup = PopupMenu(context, buttonAdd)
        popup.menuInflater.inflate(R.menu.menu_media_picker, popup.menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener setOnMenuItemClickListener@{ item: MenuItem? ->
            val itemId = item?.itemId
            if (itemId == R.id.action_take_photo) {
                onButtonClickListener?.onButtonClick(ButtonType.PHOTO)
                return@setOnMenuItemClickListener true
            } else if (itemId == R.id.action_select_photo) {
                onButtonClickListener?.onButtonClick(ButtonType.IMAGE)
                return@setOnMenuItemClickListener true
            }
            false
        })

        popup.show()
    }
}
