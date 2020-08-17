package wee.digital.camera.detector

import android.graphics.Bitmap
import android.graphics.PointF
import com.intel.realsense.librealsense.DepthFrame
import wee.digital.camera.*

class DepthDetector {

    companion object {
        const val MIN_DISTANCE = 120
        const val MIN_SIZE = RealSenseControl.COLOR_WIDTH / 5
    }

    private val maskFilter = ModelFilter("face/mask/manifest.json")

    private val depthFilter = ModelFilter("face/depth/manifest.json")

    private val mtcnn: MTCNN = MTCNN(RealSense.app.assets)

    private var currentFace: Box? = null

    private var isDetecting: Boolean = false

    var dataListener: DataListener? = null

    var statusListener: StatusListener? = null

    var optionListener: OptionListener = object : OptionListener {}

    fun release() {
        currentFace = null
    }

    fun detectFace(colorBitmap: Bitmap?, depthFrame: DepthFrame?) {
        colorBitmap ?: return
        depthFrame ?: return
        if (isDetecting) return
        isDetecting = true
        mtcnn.detectFacesAsync(colorBitmap, MIN_SIZE)
                .addOnCompleteListener { isDetecting = false }
                .addOnCanceledListener { isDetecting = false }
                .addOnFailureListener { statusListener?.onFaceLeaved() }
                .addOnCompleteListener { task ->
                    val box: Box? = task.result.largestBox()
                    if (box == null) {
                        statusListener?.onFaceLeaved()
                    } else {
                        statusListener?.onFacePerformed()
                        if (!faceChangeProcess(box)) statusListener?.onFaceChanged()
                        currentFace = box
                        onFaceDetect(box, colorBitmap, depthFrame)
                    }
                }
    }

    fun destroy() {
        depthFilter.destroy()
        maskFilter.destroy()
    }


    /**
     * Detect method 1st: use [OptionListener] filter face properties to continue [onDepthDetect]
     */
    private fun onFaceDetect(box: Box, colorBitmap: Bitmap, depthFrame: DepthFrame) {

        if (!optionListener.onFaceScore(box.score)) return

        if (!optionListener.onFaceRect(box.left(), box.top(), box.faceWidth(), box.faceHeight())) {
            return
        }

        var degreesValid = false
        box.getDegrees { x, y -> degreesValid = optionListener.onFaceDegrees(x, y) }
        if (!degreesValid) return

        val boxRect = box.transformToRect()

        val faceBitmap = boxRect.cropColorFace(colorBitmap) ?: return
        dataListener?.onFaceColorImage(faceBitmap)

        onDepthDetect(box, faceBitmap, depthFrame)
    }

    private var isDepthDetecting: Boolean = false

    private fun onDepthDetect(box: Box, colorBitmap: Bitmap, depthFrame: DepthFrame) {

        if (isDepthDetecting) return
        isDepthDetecting = true

        if (depthFrame.width != 0 && depthFrame.height != 0) {
            val unit = depthFrame.units
            val list = mutableListOf<Float>()
            try {
                list.add((depthFrame.getDistance(160, 120) * unit).trim())
                list.add((depthFrame.getDistance(320, 120) * unit).trim())
                list.add((depthFrame.getDistance(480, 120) * unit).trim())
                list.add((depthFrame.getDistance(160, 240) * unit).trim())
                list.add((depthFrame.getDistance(320, 240) * unit).trim())
                list.add((depthFrame.getDistance(480, 240) * unit).trim())
                list.add((depthFrame.getDistance(160, 360) * unit).trim())
                list.add((depthFrame.getDistance(320, 360) * unit).trim())
                list.add((depthFrame.getDistance(480, 360) * unit).trim())
                RealSense.coordLiveData.postValue(list)
            } catch (e: Exception) {
            }
        }

        isDepthDetecting = false
    }

    private fun faceChangeProcess(face: Box): Boolean {
        currentFace ?: return false
        val nowRect = face.transformToRect()
        val nowCenterX = nowRect.exactCenterX()
        val nowCenterY = nowRect.exactCenterY()
        val nowCenterPoint = PointF(nowCenterX, nowCenterY)
        val curRect = currentFace!!.transformToRect()
        val curCenterX = curRect.exactCenterX()
        val curCenterY = curRect.exactCenterY()
        val curCenterPoint = PointF(curCenterX, curCenterY)
        val dist = distancePoint(nowCenterPoint, curCenterPoint)
        return dist < MIN_DISTANCE
    }


    /**
     * Callback methods return full size image, face crop image of color image & depth image
     */
    interface DataListener {

        fun onFaceColorImage(bitmap: Bitmap?) {}

        fun onFaceDepthImage(bitmap: Bitmap?) {}

        fun onPortraitImage(bitmap: Bitmap)
    }

    /**
     * Callback methods when detect on a pair of color image & depth image
     */
    interface StatusListener {

        fun onFacePerformed()

        fun onFaceLeaved()

        fun onFaceChanged() {}

    }

    /**
     * Face detector option filter to get a portrait image if all method return true
     */
    interface OptionListener {

        fun onFaceScore(score: Float): Boolean {
            return true
        }

        fun onFaceRect(left: Int, top: Int, width: Int, height: Int): Boolean {
            return true
        }

        fun onFaceDegrees(x: Double, y: Double): Boolean {
            return true
        }

    }

}