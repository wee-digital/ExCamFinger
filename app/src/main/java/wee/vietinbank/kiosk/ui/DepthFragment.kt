package wee.vietinbank.kiosk.ui

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.depth.*
import wee.digital.camera.RealSense
import wee.digital.camera.job.FaceDetectJob
import wee.digital.camera.toBytes
import wee.vietinbank.kiosk.App
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment
import wee.vietinbank.kiosk.data.EnrollDBO
import wee.vietinbank.kiosk.data.MyDB
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class DepthFragment : BaseFragment(), FaceDetectJob.Listener {

    private val detector = FaceDetectJob(this)

    private val adapter = DepthAdapter()

    override val layoutResourceId: Int = R.layout.depth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop, viewDB)

        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        cameraView.observe(this)

        detector.observe(viewLifecycleOwner)
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
            viewDB -> {
                add(DBFragment(), true)
            }
        }
    }

    override fun onFaceDetected(label: String, cropColor: Bitmap, cropDepth: Bitmap) {
        /*detector.hasDetect = false
        RealSense.imagesLiveData.value?.also {
            val now = System.currentTimeMillis()
            saveBitmap("$now color", it.first)
            saveBitmap("$now depth", it.second)
            detector.hasDetect = true
        }*/
        adapter.add(DepthAdapter.Item(label, cropColor, cropDepth))
        adapter.onItemClick = { model, _ ->
            MyDB.instance.enrollDao.insert(EnrollDBO().also {
                it.cropColorImage = model.colorBitmap.toBytes()
                it.cropDepthImage = model.depthBitmap.toBytes()
                it.label = model.label
                Toast.makeText(App.instance, "inserted", Toast.LENGTH_SHORT).show()
            })
        }
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