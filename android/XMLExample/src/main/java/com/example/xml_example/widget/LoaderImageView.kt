package com.example.xml_example.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import coil.request.ImageRequest
import androidx.core.net.toUri
import coil.imageLoader

class LoaderImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    fun loadImage(
        url: String,
        @DrawableRes placeholder: Int? = null,
        @DrawableRes errorResource: Int? = null
    ) {
        val uri = url.toUri()

        val request = ImageRequest.Builder(context)
            .data(uri)
            .crossfade(true)
            .apply {
                placeholder?.let { placeholder(it) }
                errorResource?.let { error(it) }
            }
            .target(this)
            .build()

        context.imageLoader.enqueue(request)
    }
} 
