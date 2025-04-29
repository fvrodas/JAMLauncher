package io.github.fvrodas.jaml.ui.common.extensions

import java.text.Normalizer

fun String.simplify() =
    REGEX_UNACCENTED.replace(Normalizer.normalize(this, Normalizer.Form.NFD), "")

internal val REGEX_UNACCENTED = "\\p{InCombiningDiacriticalMarks}+".toRegex()
