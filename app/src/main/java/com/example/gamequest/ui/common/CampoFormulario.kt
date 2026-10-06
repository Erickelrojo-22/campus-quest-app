package com.example.gamequest.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gamequest.ui.theme.ErrorOnCream
import com.example.gamequest.ui.theme.ErrorOnDark
import com.example.gamequest.ui.theme.PixelCream
import com.example.gamequest.ui.theme.PixelInkOnCream

/**
 * Campo de formulario estilo "consola retro" con una etiqueta REAL (`label`),
 * de modo que TalkBack la asocia al campo (una etiqueta suelta encima del campo
 * no se anuncia al enfocarlo). Si es contraseña incluye el botón mostrar/ocultar.
 */
@Composable
fun CampoFormulario(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    icono: ImageVector? = null,
    esPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    ayuda: String? = null,
    hayError: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = if (esPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        } else null,
        supportingText = ayuda?.let { { Text(it) } },
        isError = hayError,
        singleLine = true,
        shape = RoundedCornerShape(2.dp),
        visualTransformation = if (esPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PixelCream,
            unfocusedContainerColor = PixelCream,
            errorContainerColor = PixelCream,
            focusedTextColor = PixelInkOnCream,
            unfocusedTextColor = PixelInkOnCream,
            errorTextColor = PixelInkOnCream,
            focusedBorderColor = PixelInkOnCream,
            unfocusedBorderColor = PixelInkOnCream.copy(alpha = 0.7f),
            errorBorderColor = ErrorOnCream,
            cursorColor = PixelInkOnCream,
            errorCursorColor = ErrorOnCream,
            focusedLabelColor = PixelInkOnCream,
            unfocusedLabelColor = PixelInkOnCream.copy(alpha = 0.85f),
            errorLabelColor = ErrorOnCream,
            focusedPlaceholderColor = PixelInkOnCream.copy(alpha = 0.65f),
            unfocusedPlaceholderColor = PixelInkOnCream.copy(alpha = 0.65f),
            focusedLeadingIconColor = PixelInkOnCream,
            unfocusedLeadingIconColor = PixelInkOnCream,
            errorLeadingIconColor = ErrorOnCream,
            focusedTrailingIconColor = PixelInkOnCream,
            unfocusedTrailingIconColor = PixelInkOnCream,
            errorTrailingIconColor = ErrorOnCream,
            focusedSupportingTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurface,
            errorSupportingTextColor = ErrorOnDark
        )
    )
}

/**
 * Mensaje de error accesible: ícono + texto (no depende solo del color) y región
 * "en vivo" asertiva para que TalkBack lo lea en cuanto aparece.
 */
@Composable
fun MensajeError(texto: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive },
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            tint = ErrorOnDark,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(texto, color = ErrorOnDark, style = MaterialTheme.typography.bodyMedium)
    }
}
