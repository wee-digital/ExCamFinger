package wee.digital.finger

import android.graphics.Bitmap

/**
 * @param rawImage: raw fingerprint got by [FingerCommand.captureImage]
 * @param patternRate: raw fingerprint pattern patternRate of @param fingerprint got by [FingerCommand.patternRate]
 */
class FingerData(val rawImage: ByteArray, val patternRate: Int) {

    val bitmap: Bitmap? get() = HeroFun.getBitmap(rawImage)

    val template: ByteArray?
        get() {
            return FingerCommand.createTemplate(rawImage)
        }

    companion object {

        fun bestIn(list: List<FingerData>): FingerData {
            if (list.isEmpty()) throw KotlinNullPointerException("FingerData collection is empty")
            if (list.size == 1) return list.first()
            var best = list.first()
            for (i in 1 until list.size) {
                if (list[i].patternRate > best.patternRate) {
                    best = list[i]
                }
            }
            return best
        }

    }

}
