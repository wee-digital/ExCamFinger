package wee.vietinbank.kiosk.io

import android.graphics.Bitmap
import wee.digital.library.usb.Usb
import wee.digital.library.usb.UsbDriver
import wee.digital.library.util.TimeUtil
import java.io.IOException
import java.io.UnsupportedEncodingException

/**
 * Cashino
 */
object Cashino {

    private val ERROR_STATUS = byteArrayOf(0x10, 0x04, 0x03)
    private val ONLINE_PRINTER_STATUS = byteArrayOf(0x10, 0x04, 0x01)
    private val OFFLINE_PRINTER_STATUS = byteArrayOf(0x10, 0x04, 0x02)
    private val FULL_CUT_PAPER = byteArrayOf(27, 105)
    private val HALF_CUT_PAPER = byteArrayOf(27, 109)
    private val CLEAN = byteArrayOf(27, 64)
    private val BIT_IMAGE_MODE = byteArrayOf(0x1B, 0x2A, 33, -128, 0)

    const val VENDOR_ID: Int = 4070

    val hasDevice: Boolean get() = Usb.getDevice(VENDOR_ID) != null

    private var driver = UsbDriver()

    /**
     * Methods
     */
    fun cutPaper() {
        clean()
        print("")
        print("")
        print("")
        clean()
        write(HALF_CUT_PAPER)
    }

    fun clean() = write(CLEAN)

    fun print(bitmap: Bitmap) {
        Thread {
            clean()
            write(PrinterCommand.getPrintData(bitmap))
            cutPaper()
        }.start()
    }

    fun print(str: String, ime: Int = 0) {
        try {
            val var5: ByteArray = str.toByteArray(charset("GB2312"))
            var textSize = var5.size
            if (ime == 0) ++textSize
            val byteArray = ByteArray(textSize)
            System.arraycopy(var5, 0, byteArray, 0, var5.size)
            if (ime == 0) byteArray[textSize - 1] = 10
            write(byteArray)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    val status: Int get() = read(ONLINE_PRINTER_STATUS)

    val offlineStatus: Int get() = read(OFFLINE_PRINTER_STATUS)

    val error: Int get() = read(ERROR_STATUS)

    fun lineSpace(space: Int) = write(byteArrayOf(27, 51, if (space > 127) 127 else space.toByte()))

    fun write(bytes: ByteArray) {
        var length = 0
        var size: Int
        val byteArray = ByteArray(4096)
        val startedTime = System.currentTimeMillis()
        while (length < bytes.size) {
            if (System.currentTimeMillis() - startedTime > 8 * TimeUtil.SECOND) break
            size = 4096
            if (length + 4096 > bytes.size) size = bytes.size - length
            System.arraycopy(bytes, length, byteArray, 0, size)
            val transferSize: Int? = driver.connection?.bulkTransfer(driver.endpointIn, byteArray, size, 5000)
            size = transferSize ?: throw IOException("bulk transfer error")
            if (size < 0) throw IOException("bulk transfer error")
            length += size
        }
    }

    fun read(bytes: ByteArray): Int {
        return -1
    }

    fun open() {
        driver = UsbDriver()
        val device = Usb.getDevice(VENDOR_ID) ?: return
        driver.open(device)
    }

    fun close() {
        driver.close()
    }


}

