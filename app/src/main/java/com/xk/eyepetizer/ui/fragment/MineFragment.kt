package com.xk.eyepetizer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xk.eyepetizer.R
import com.xk.eyepetizer.ui.base.BaseFragment
import com.xk.eyepetizer.ui.base.tabsId
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by zongkaili on 2017/9/13.
 */
class MineFragment : BaseFragment(tabId = tabsId[3]), View.OnClickListener {
    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mine, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    var isFirst = true
    override fun onResume() {
        super.onResume()
        if (isFirst) {
            setupToolbar()
            isFirst = false
        }
    }

    override fun setupToolbar(): Boolean {
        if (super.setupToolbar()) {
            return true
        }
        activity.toolbar.visibility = View.VISIBLE
        activity.toolbar.setBackgroundColor(0xddffffff.toInt())
        activity.iv_search.setImageResource(R.mipmap.ic_menu_more)
        activity.tv_bar_title.setText("")
        return true
    }
}