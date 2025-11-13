package com.example.ccaiexample.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ccai.example.compose_example.R

@Composable
fun ChatMenuBottomSheet(
    onDismiss: () -> Unit,
    onExitChat: () -> Unit,
    isScreenShareActive: Boolean,
    isScreenShareEnabled: Boolean,
    toggleScreenShare: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.dialog_overlay))
                .clickable { onDismiss() }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
                .background(
                    color = colorResource(id = R.color.dialog_background),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.menu),
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.close),
                        modifier = Modifier
                            .clickable { onDismiss() },
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                // Screen share
                if (isScreenShareEnabled) {
                    val buttonText = if (isScreenShareActive) {
                        stringResource(com.example.shared.R.string.cobrowse_session_end_button_title)
                    } else {
                        stringResource(com.example.shared.R.string.cobrowse_session_initiate_button_title)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                toggleScreenShare()
                            }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_external_deflection_links),
                            contentDescription = context.getString(com.example.shared.R.string.screen_share),
                            modifier = Modifier.size(20.dp),
                            tint = if (isScreenShareActive) {
                                colorResource(id = R.color.screen_share_button_active)
                            } else {
                                colorResource(id = R.color.screen_share_button_inactive)
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = buttonText,
                            fontSize = 16.sp,
                            color = if (isScreenShareActive) {
                                colorResource(id = R.color.screen_share_button_active)
                            } else {
                                colorResource(id = R.color.screen_share_button_inactive)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Exit chat button at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(id = R.color.dialog_button_text), RoundedCornerShape(12.dp))
                        .clickable { onExitChat() }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.exit_chat),
                        color = colorResource(id = R.color.dialog_background),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
