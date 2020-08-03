package wee.vietinbank.kiosk.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.db.*
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment
import wee.vietinbank.kiosk.data.MyDB

class DBFragment : BaseFragment() {

    private val adapter = DBAdapter()

    override val layoutResourceId: Int = R.layout.db

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.bind(recyclerViewDB)
        adapter.onItemClick = { model, _ ->
            MyDB.instance.enrollDao.delete(model)
        }
        MyDB.instance.enrollDao.liveData().observe(viewLifecycleOwner, Observer {
            adapter.set(it)
        })
    }


}