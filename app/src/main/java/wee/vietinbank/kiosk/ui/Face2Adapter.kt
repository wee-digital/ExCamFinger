package wee.vietinbank.kiosk.ui

import android.graphics.Bitmap
import android.view.View
import kotlinx.android.synthetic.main.face2_item.view.*
import wee.digital.library.adapter.BaseRecyclerAdapter
import wee.vietinbank.kiosk.R

class Face2Adapter : BaseRecyclerAdapter<Bitmap>() {
    override fun layoutResource(model: Bitmap, position: Int): Int {
        return R.layout.face2_item
    }

    override fun View.onBindModel(model: Bitmap, position: Int, layout: Int) {
        imageViewPortrait.setImageBitmap(model)
    }

}