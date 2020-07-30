package wee.vietinbank.kiosk.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.face2.*
import wee.digital.camera.RealSense
import wee.digital.camera.job.FaceDetectJob
import wee.vietinbank.kiosk.App
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class Face2Fragment : BaseFragment(), FaceDetectJob.Listener {

    private val adapter = Face2Adapter()

    override val layoutResourceId: Int = R.layout.face2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop)

        //requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        cameraView.observe(this)

        FaceDetectJob(this).observe(viewLifecycleOwner)

        adapter.bind(recyclerViewPortrait) {
            orientation = LinearLayoutManager.HORIZONTAL
            stackFromEnd = true
            reverseLayout = true
        }
    }

    override fun onViewClick(view: View) {
        when (view) {
            deviceViewStart -> {
                RealSense.start()
            }
            deviceViewStop -> {
                RealSense.stop()
            }
        }
    }

    override fun onFaceDetected(bitmap: Bitmap) {
        /*RealSense.imagesLiveData.value?.also {
            val now = System.currentTimeMillis()
            saveBitmap("$now color", it.first)
            saveBitmap("$now depth", it.second)
        }*/
        adapter.add(bitmap)
        if (adapter.size > 20) {
            adapter.currentList.removeAt(0)
        }
        recyclerViewPortrait?.smoothScrollToPosition(adapter.lastPosition)
    }

    override fun onFaceLeaved() {
    }

    override fun onFaceInvalid(message: String?) {
    }

    private fun saveBitmap(name: String, bitmap: Bitmap) {
        val path: String = Environment.getExternalStorageDirectory().toString()
        var fOut: OutputStream?
        val file = File(path, "$name.png")
        fOut = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.flush()
        fOut.close()
        MediaStore.Images.Media.insertImage(App.instance.contentResolver, file.absolutePath, file.name, file.name)
    }
}