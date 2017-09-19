package com.xk.eyepetizer.ui.activity

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.gyf.barlibrary.ImmersionBar
import com.tt.lvruheng.eyepetizer.adapter.DownloadVideosAdapter
import com.tt.lvruheng.eyepetizer.utils.ObjectSaveUtils
import com.tt.lvruheng.eyepetizer.utils.SPUtils
import com.xk.eyepetizer.R
import com.xk.eyepetizer.mvp.model.bean.Item
import com.xk.eyepetizer.toActivityWithSerializable
import kotlinx.android.synthetic.main.activity_cache.*
import kotlinx.android.synthetic.main.layout_bottom_controller.*

class CacheActivity : AppCompatActivity(), View.OnClickListener {
    var mList = ArrayList<Item>()
    lateinit var mAdapter: DownloadVideosAdapter
    var index: Int = 0
    var isSelectAll: Boolean = false

    var mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            var list = msg?.data?.getParcelableArrayList<Item>("beans")
            if (list?.size?.compareTo(0) == 0) {
                tv_hint.visibility = View.VISIBLE
            } else {
                tv_hint.visibility = View.GONE
                if (mList.size > 0) {
                    mList.clear()
                }
                list?.let { mList.addAll(it) }
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache)
        ImmersionBar.with(this).transparentBar().barAlpha(0.3f).fitsSystemWindows(true).init()
        setToolbar()
        DataAsyncTask(mHandler, this).execute()
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = DownloadVideosAdapter(this, mList)
        mAdapter.setOnItemClickListener(object : DownloadVideosAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, items: List<Item>) {
                if (mAdapter.mEditMode == DownloadVideosAdapter.MODE_NORMAL)
                    toActivityWithSerializable<DetailActivity>(items.get(position))
                else {
                    onItemSelected(position, items)
                }
            }
        })

//        mAdapter.setOnItemSelectedListener(object : DownloadVideosAdapter.OnItemSelectedListener {
//            override fun onItemSelected(position: Int) {
//                val item = mList.get(position)
//                if (item.isSelected()) {
//                    index++
//                    item.setSelected(true)
//                    if (index == mList.size) {
//                        isSelectAll = true
//                        tv_select_all.text = resources.getString(R.string.string_select_all_cancel)
//                    }
//                } else {
//                    item.setSelected(false)
//                    index--
//                    isSelectAll = false
//                    tv_select_all.text = resources.getString(R.string.string_select_all)
//                }
//                if (index == 0) {
//                    tv_delete.setTextColor(resources.getColor(R.color.black))
//                    tv_delete.isEnabled = false
//                } else {
//                    tv_delete.setTextColor(resources.getColor(R.color.colorPrimary))
//                    tv_delete.isEnabled = true
//                }
//                tv_delete.text = resources.getString(R.string.string_delete, index)
//                mAdapter.notifyDataSetChanged()
//            }
//        })
        recyclerView.adapter = mAdapter
        tv_select_all.setOnClickListener(this)
        tv_delete.setOnClickListener(this)
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        var bar = supportActionBar
        bar?.title = "我的缓存"
        bar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_toolbar_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.edit -> {
                if (mAdapter.mEditMode == DownloadVideosAdapter.MODE_NORMAL) {
                    mAdapter.setEditMode(DownloadVideosAdapter.MODE_CHECK)
                    item?.title = "取消"
                    bottomController.visibility = View.VISIBLE
                } else {
                    mAdapter.setEditMode(DownloadVideosAdapter.MODE_NORMAL)
                    item?.title = "编辑"
                    bottomController.visibility = View.GONE
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onItemSelected(position: Int, items: List<Item>) {
        val item = items.get(position)
        if (item.isSelected()) {
            index++
            item.setSelected(true)
            if (index == items.size) {
                isSelectAll = true
                tv_select_all.text = resources.getString(R.string.string_select_all_cancel)
            }
        } else {
            item.setSelected(false)
            index--
            isSelectAll = false
            tv_select_all.text = resources.getString(R.string.string_select_all)
        }
        if (index <= 0) {
            tv_delete.setTextColor(resources.getColor(R.color.black))
            tv_delete.isEnabled = false
            tv_delete.text = "删除"
        } else {
            tv_delete.setTextColor(resources.getColor(R.color.colorPrimary))
            tv_delete.isEnabled = true
            tv_delete.text = resources.getString(R.string.string_delete, index)
        }
        mAdapter.notifyItemChanged(position)
    }

    private class DataAsyncTask(handler: Handler, activity: CacheActivity) : AsyncTask<Void, Void, ArrayList<Item>>() {
        var activity: CacheActivity = activity
        var handler = handler
        override fun doInBackground(vararg params: Void?): ArrayList<Item>? {
            var list = ArrayList<Item>()
            var count: Int = SPUtils.getInstance(activity, "downloads").getInt("count")
            var i = 1
            while (i.compareTo(count) <= 0) {
                var bean: Item
                if (ObjectSaveUtils.getValue(activity, "download$i") == null) {
                    continue
                } else {
                    bean = ObjectSaveUtils.getValue(activity, "download$i") as Item
                }
                list.add(bean)
                i++
            }
            return list
        }

        override fun onPostExecute(result: ArrayList<Item>?) {
            super.onPostExecute(result)
            var message = handler.obtainMessage()
            var bundle = Bundle()
            bundle.putParcelableArrayList("beans", result)
            message.data = bundle
            handler.sendMessage(message)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_select_all -> {

            }
            R.id.tv_delete -> {

            }
        }
    }
}
