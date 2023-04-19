package com.tatasky.binge.utils

fun String.splitAnyString(splitChar: String): List<String> {
    return this.split(splitChar)
}

fun String?.takeIfNotEmptyOrNull() : String? {
    return this?.takeIf { it.isNotEmpty() }
}
