package com.xk.eyepetizer.ui.view.mine

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.FrameLayout
import com.xk.eyepetizer.R
import com.xk.eyepetizer.mvp.model.bean.Item
import kotlinx.android.synthetic.main.item_videolist.view.*

/**
 * Created by zongkaili on 2017/9/18.
 */
class VideosListItem : FrameLayout{
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.item_videolist, this)
    }

    fun setData(item: Item) {
        tv_title?.typeface = Typeface.createFromAsset(context?.assets, "fonts/FZLanTingHeiS-L-GB-Regular.TTF")
    }
}