package com.xk.eyepetizer.ui.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.xk.eyepetizer.R
import com.xk.eyepetizer.ui.activity.CacheActivity
import com.xk.eyepetizer.ui.base.BaseFragment
import com.xk.eyepetizer.ui.base.tabsId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_mine.*

/**
 * Created by zongkaili on 2017/9/13.
 */
class MineFragment : BaseFragment(tabId = tabsId[3]), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mine, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        tv_message.setOnClickListener(this)
        tv_attention.setOnClickListener(this)
        tv_cache.setOnClickListener(this)
        tv_record.setOnClickListener(this)
        tv_message.typeface = Typeface.createFromAsset(context?.assets,"fonts/FZLanTingHeiS-DB1-GB-Regular.TTF")
        tv_attention.typeface = Typeface.createFromAsset(context?.assets,"fonts/FZLanTingHeiS-DB1-GB-Regular.TTF")
        tv_cache.typeface = Typeface.createFromAsset(context?.assets,"fonts/FZLanTingHeiS-DB1-GB-Regular.TTF")
        tv_record.typeface = Typeface.createFromAsset(context?.assets,"fonts/FZLanTingHeiS-DB1-GB-Regular.TTF")
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
        activity.toolbar.setBackgroundColor(resources.getColor(R.color.bg_white))
        activity.iv_search.setImageResource(R.mipmap.ic_menu_more)
        activity.tv_bar_title.setText("")
        return true
    }

    override fun onClick(v: View?) {
       when (v?.id) {
           R.id.tv_message -> Toast.makeText(context,"message",Toast.LENGTH_SHORT).show()
           R.id.tv_attention -> Toast.makeText(context,"attention",Toast.LENGTH_SHORT).show()
           R.id.tv_cache -> {
               startActivity(Intent(activity, CacheActivity::class.java))
           }
           R.id.tv_record -> Toast.makeText(context,"record",Toast.LENGTH_SHORT).show()
       }
    }
}