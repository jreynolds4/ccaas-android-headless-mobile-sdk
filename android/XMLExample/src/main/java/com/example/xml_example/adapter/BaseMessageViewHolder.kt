package com.example.xml_example.adapter

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.shared.model.Message
import co.ccai.example.xml_example.R
import com.example.xml_example.widget.LoaderImageView
import java.text.SimpleDateFormat
import java.util.Locale

abstract class BaseMessageViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
    private val textTime: TextView = itemView.findViewById(R.id.textTime)
    private val image: LoaderImageView = itemView.findViewById(R.id.image)

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun bind(message: Message) {
        textMessage.text = message.text
        textTime.text = dateFormat.format(message.createdAt)
        if (message.attachments.isNotEmpty()) {
            val attachment = message.attachments.first()
            image.loadImage(attachment.url ?: "")
            image.visibility = View.VISIBLE
        } else {
            image.visibility = View.GONE
        }
        textMessage.isVisible = message.text?.isNotEmpty() == true
    }
}
