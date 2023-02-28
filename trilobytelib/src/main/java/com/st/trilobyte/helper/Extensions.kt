package com.st.trilobyte.helper

fun MutableList<*>.removeAfterIndex(index: Int) {
    if (size < index)
        return

    for (i in size - 1 downTo index) {
        this.removeAt(index)
    }
}

fun String.removeTerminatorCharacters(): String {
    return this.replace("\n", "").replace("\r", "")
}