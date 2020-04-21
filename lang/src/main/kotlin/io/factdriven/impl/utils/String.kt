package io.factdriven.impl.utils

fun String.toSentenceCase() = "(.)([A-Z\\d])".toRegex().replace(this) {
    "${it.groupValues[1]} ${it.groupValues[2].toLowerCase()}"
}

fun String.asLines(length: Int = 12) = "(.{1,$length})(?:\\s|$)".toRegex().replace(this) {
    "${it.groupValues[1]}\n"
}.trim()

fun String.toLines(length: Int = 12) = asLines(length).lines()
