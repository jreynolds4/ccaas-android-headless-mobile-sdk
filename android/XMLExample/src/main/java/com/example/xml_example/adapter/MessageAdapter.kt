package com.example.xml_example.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.shared.model.Message
import com.example.shared.model.UserRole
import co.ccai.example.xml_example.R
import com.example.shared.extensions.displayText
import com.example.shared.model.MessageType
import com.example.xml_example.widget.LoaderImageView

class MessageAdapter : ListAdapter<Message, BaseViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> MessageSentViewHolder(
                layoutInflater.inflate(
                    R.layout.item_message_sent,
                    parent,
                    false
                )
            )

            VIEW_TYPE_RECEIVED -> MessageReceivedViewHolder(
                layoutInflater.inflate(
                    R.layout.item_message_received,
                    parent,
                    false
                )
            )

            VIEW_TYPE_SYSTEM_MESSAGE -> SystemMessageViewHolder(
                layoutInflater.inflate(
                    R.layout.item_system_message,
                    parent,
                    false
                )
            )

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).type == MessageType.Notification) {
            return VIEW_TYPE_SYSTEM_MESSAGE
        }
        return when (getItem(position).user.role) {
            UserRole.CURRENT_USER -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    class MessageReceivedViewHolder(itemView: View) : BaseMessageViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val agentImage: LoaderImageView = itemView.findViewById(R.id.agentImage)
        private val agentLayout: LinearLayout = itemView.findViewById(R.id.agentLayout)


        override fun bind(message: Message) {
            super.bind(message)
            if (message.user.role != UserRole.CURRENT_USER) {
                textName.text = message.user.name
                agentLayout.visibility = View.VISIBLE
                agentImage.loadImage(
                    message.user.avatarUrl ?: "",
                    placeholder = R.drawable.ic_agent_sample,
                    errorResource = R.drawable.ic_agent_sample
                )
            } else {
                agentLayout.visibility = View.GONE
            }
        }
    }

    class MessageSentViewHolder(itemView: View) : BaseMessageViewHolder(itemView) {

        override fun bind(message: Message) {
            super.bind(message)
        }
    }

    class SystemMessageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val tvSystemMessage: TextView = itemView.findViewById(R.id.tv_system_message)

        override fun bind(message: Message) {
            val content = message.displayText(itemView.context)
            if (content.isNotEmpty()) {
                tvSystemMessage.text = content
                tvSystemMessage.visibility = View.VISIBLE
            } else {
                tvSystemMessage.visibility = View.GONE
            }
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.createdAt == newItem.createdAt && oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_SYSTEM_MESSAGE = 3
    }
}
