package wee.digital.library.extension

import android.util.Base64
import wee.digital.library.util.TimeUtil
import java.math.BigDecimal

val now: Long get() = System.currentTimeMillis()

val currentTimeInSecond: Long get() = System.currentTimeMillis() / TimeUtil.SECOND

fun ByteArray?.toBase64String(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

fun BigDecimal?.isNullOrZero(): Boolean {
    return this == null || this == BigDecimal.ZERO
}