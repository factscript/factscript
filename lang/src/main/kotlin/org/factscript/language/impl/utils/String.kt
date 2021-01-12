package org.factscript.language.impl.utils

fun String.toSentenceCase() = "(.)([A-Z\\d])".toRegex().replace(this) {
    "${it.groupValues[1]} ${it.groupValues[2].toLowerCase()}"
}

fun String.asLines(length: Int = 15) = "(.{1,$length})(?:\\s|$)".toRegex().replace(this) {
    "${it.groupValues[1]}\n"
}.trim()

fun String.toLines(length: Int = 15) = asLines(length).lines()
