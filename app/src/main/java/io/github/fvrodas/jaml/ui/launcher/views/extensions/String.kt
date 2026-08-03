package io.github.fvrodas.jaml.ui.launcher.views.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

fun String.hightlightCoincidence(value: String?, color: Color): AnnotatedString =
    buildAnnotatedString {
        append(this@hightlightCoincidence)
        val start = if (!value.isNullOrBlank()) this@hightlightCoincidence.indexOf(value, ignoreCase = true) else -1
        if (start >= 0) {
            this.addStyle(
                SpanStyle(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                ),
                start,
                start + value!!.length
            )
        }
    }
