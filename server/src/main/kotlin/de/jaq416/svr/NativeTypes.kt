package de.jaq416.svr

import com.google.common.primitives.UnsignedInts

fun Int.encUChar(): Byte {
    require(this >= 0 && this < 256) { "encUChar: $this" }
    return toByte()
}

fun Byte.decUChar() = toInt() and 0xFF

fun Int.decSInt(): Long {
    return UnsignedInts.toLong(this)
}

fun Long.encSInt(): Int {
    return UnsignedInts.checkedCast(this)
}