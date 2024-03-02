package com.codewithkael.walkietalkie.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.codewithkael.walkietalkie.R

@Composable
fun MicToggleButton(

    onToggle: (Boolean) -> Unit
) {
    var isToggledOn by remember { mutableStateOf(false) }

    val iconRes = if (isToggledOn) R.drawable.ic_mic_off else R.drawable.ic_mic
    val contentDesc = if (isToggledOn) "Stop Streaming" else "Start Streaming"

    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = contentDesc,
        tint = Color.White,
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(4.dp)
            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 10.dp)
            .size(40.dp) // Adjust size as needed
            .clickable {

                onToggle(!isToggledOn)
                isToggledOn = !isToggledOn
            }
    )
}
