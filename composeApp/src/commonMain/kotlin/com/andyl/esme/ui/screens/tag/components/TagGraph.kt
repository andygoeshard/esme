package com.andyl.esme.ui.screens.tag.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.esme.ui.screens.tag.RelatedTag
import kotlin.math.PI

@Composable
fun TagGraph(
    centerTag: String,
    related: List<RelatedTag>,
    onClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {

        // 🧠 centro
        Text(
            text = centerTag,
            color = Color(0xFF50C878),
            fontWeight = FontWeight.Black
        )

        val radius = 80f

        related.take(8).forEachIndexed { index, tag ->

            val angle = (index / related.size.toFloat()) * (2 * PI)

            val x = (radius * kotlin.math.cos(angle)).toFloat()
            val y = (radius * kotlin.math.sin(angle)).toFloat()

            Text(
                text = tag.tag,
                color = Color.White,
                modifier = Modifier
                    .offset(x.dp, y.dp)
                    .clickable { onClick(tag.tag) }
            )
        }
    }
}