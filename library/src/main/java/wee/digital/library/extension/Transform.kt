package wee.digital.library.extension

fun <T> nonNull(block: (T) -> Unit): (T?) -> Unit {
    return {
        if (it != null) block(it)
    }
}

