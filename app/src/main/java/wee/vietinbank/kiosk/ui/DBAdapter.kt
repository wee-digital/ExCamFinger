package wee.vietinbank.kiosk.ui

import android.view.View
import kotlinx.android.synthetic.main.db_item.view.*
import wee.digital.camera.toBitmap
import wee.digital.library.adapter.BaseRecyclerAdapter
import wee.digital.library.extension.color
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.data.EnrollDBO

class DBAdapter : BaseRecyclerAdapter<EnrollDBO>() {

    override fun layoutResource(model: EnrollDBO, position: Int): Int {
        return R.layout.db_item
    }

    override fun View.onBindModel(model: EnrollDBO, position: Int, layout: Int) {
        itemImageViewColor.setImageBitmap(model.cropColorImage?.toBitmap())
        itemImageViewDepth.setImageBitmap(model.cropDepthImage?.toBitmap())
        itemTextViewDepth.text = model.label
        itemTextViewDepth.color(if (model.label == "real")
            android.R.color.holo_green_dark
        else
            android.R.color.holo_red_light
        )
    }

}