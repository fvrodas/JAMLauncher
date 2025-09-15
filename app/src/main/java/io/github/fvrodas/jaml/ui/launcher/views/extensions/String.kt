package io.github.fvrodas.jaml.ui.launcher.views.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun String.hightlightCoincidence(value: String?, color: Color): AnnotatedString =
    buildAnnotatedString {
        append(this@hightlightCoincidence)
        if (!value.isNullOrBlank() && this@hightlightCoincidence.contains(
                value,
                ignoreCase = true
            )
        ) {
            val start = this@hightlightCoincidence.indexOf(value, ignoreCase = true)
            this.addStyle(
                SpanStyle(color = color, fontWeight = FontWeight.Bold),
                start,
                start + (value.length)
            )
        }
    }