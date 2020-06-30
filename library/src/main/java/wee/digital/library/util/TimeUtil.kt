package wee.digital.library.util

import wee.digital.library.extension.now
import java.lang.reflect.InvocationTargetException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {

    const val SECOND: Long = 1000

    const val MIN: Long = 60 * SECOND

    const val HOUR: Long = 60 * MIN

    fun currentDateTime(format: String): String {
        return convert(now, format)
    }

    fun currentDateTime(sdf: SimpleDateFormat): String {
        return convert(now, sdf)
    }

    // if give up time in second convert to time in millis
    private fun Long.correctTime(): Long {
        return if (this < 1000000000000L) this * 1000 else this
    }

    fun convert(long: Long, formatter: SimpleDateFormat): String {
        return try {
            formatter.format(Date(long.correctTime()))
        } catch (e: ParseException) {
            "..."
        } catch (e: InvocationTargetException) {
            "..."
        }
    }

    fun convert(long: Long, formatter: String): String {
        return try {
            SimpleDateFormat(formatter).format(Date(long.correctTime()))
        } catch (e: ParseException) {
            "..."
        } catch (e: InvocationTargetException) {
            "..."
        }
    }

}