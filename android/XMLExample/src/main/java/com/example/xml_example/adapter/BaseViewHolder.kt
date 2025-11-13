package com.example.xml_example.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.shared.model.Message

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(message: Message)
}
