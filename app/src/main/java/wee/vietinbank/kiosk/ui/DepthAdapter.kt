package wee.vietinbank.kiosk.ui

import android.graphics.Bitmap
import android.view.View
import kotlinx.android.synthetic.main.face2_item.view.*
import wee.digital.library.adapter.BaseRecyclerAdapter
import wee.digital.library.extension.color
import wee.digital.library.util.TimeUtil
import wee.vietinbank.kiosk.R

class DepthAdapter : BaseRecyclerAdapter<DepthAdapter.Item>() {

    data class Item(
            val label: String,
            val colorBitmap: Bitmap,
            val depthBitmap: Bitmap
    )

    override fun layoutResource(model: Item, position: Int): Int {
        return R.layout.face2_item
    }

    override fun View.onBindModel(model: Item, position: Int, layout: Int) {
        itemImageViewPortrait.setImageBitmap(model.depthBitmap)
        itemTextViewDepth.text = model.label
        itemTextViewDepth.color(if (model.label == "real")
            android.R.color.holo_green_dark
        else
            android.R.color.holo_red_light
        )
        itemTextViewTime.text = TimeUtil.convert(System.currentTimeMillis(), "HH:mm:ss")
    }

}