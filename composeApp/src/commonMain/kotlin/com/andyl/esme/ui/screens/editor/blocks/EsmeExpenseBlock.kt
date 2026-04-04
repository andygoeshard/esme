package com.andyl.esme.ui.screens.editor.blocks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EsmeExpenseBlock(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    onLabelChange: (String) -> Unit,
    onAmountChange: (Double) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Icon(
            Icons.Default.Payments,
            contentDescription = null,
            tint = Color(0xFF50C878),
            modifier = Modifier.size(24.dp).padding(end = 8.dp)
        )

        TextField(
            value = label,
            onValueChange = onLabelChange,
            placeholder = { Text("Concepto...", color = Color.Gray) },
            modifier = modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        TextField(
            value = if (amount == 0.0) "" else amount.toString(),
            onValueChange = { input ->
                val cleanInput = input.replace(",", ".")
                val newAmount = cleanInput.toDoubleOrNull() ?: 0.0
                onAmountChange(newAmount)
            },
            placeholder = { Text("0.00", color = Color.Gray) },
            modifier = Modifier.width(80.dp),
            prefix = { Text("$", color = Color(0xFF50C878)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color(0xFF50C878),
                unfocusedTextColor = Color.White
            )
        )

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Borrar", tint = Color.White.copy(0.3f))
        }
    }
}