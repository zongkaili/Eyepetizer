package com.xk.eyepetizer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xk.eyepetizer.R
import com.xk.eyepetizer.ui.base.BaseFragment
import com.xk.eyepetizer.ui.base.tabsId

/**
 * Created by zongkaili on 2017/9/13.
 */
class MineFragment : BaseFragment(tabId = tabsId[3]), View.OnClickListener{
    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mine,null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}