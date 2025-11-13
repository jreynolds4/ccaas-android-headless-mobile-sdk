package com.example.ccaiexample.widget

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ImageLoader(
    model: String,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes errorResource: Int? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(model).crossfade(true).build(),
        contentDescription = "Image",
        modifier = modifier,
        placeholder = if (placeholder != null) painterResource(placeholder) else null,
        error = if (errorResource != null) painterResource(errorResource) else null,
        contentScale = ContentScale.Crop
    )
}
