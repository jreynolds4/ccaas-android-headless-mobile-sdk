package com.example.ccaiexample.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ccai.example.compose_example.R
import com.example.ccaiexample.widget.LoadingButton

@Composable
fun HomeViewContent(
    padding: PaddingValues,
    inputText: String,
    onMenuIdChange: (String) -> Unit,
    isLoading: Boolean,
    onContactClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onMenuIdChange,
            label = { Text(stringResource(R.string.input_menu_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Text(
            text = stringResource(R.string.menu_id_from_the_queue_menu_settings),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LoadingButton(
            onClick = onContactClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            containerColor = Color(0xFF007AFF),
            contentColor = Color.White,
            isLoading = isLoading,
            text = stringResource(R.string.contact_customer_support)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewContentPreview() {
    HomeViewContent(
        padding = PaddingValues(0.dp),
        inputText = "Menu id",
        onMenuIdChange = {},
        isLoading = false,
        onContactClick = {}
    )
}
