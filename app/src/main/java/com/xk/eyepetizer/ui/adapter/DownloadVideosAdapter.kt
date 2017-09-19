package com.tt.lvruheng.eyepetizer.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import com.tt.lvruheng.eyepetizer.utils.SPUtils
import com.xk.eyepetizer.R
import com.xk.eyepetizer.mvp.model.bean.Item
import com.xk.eyepetizer.toActivityWithSerializable
import com.xk.eyepetizer.ui.activity.DetailActivity
import com.xk.eyepetizer.ui.view.detail.ListEndView
import com.xk.eyepetizer.ui.view.mine.VideosListItem
import com.xk.eyepetizer.util.GlideUtil
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.item_videolist.view.*
import kotlinx.android.synthetic.main.layout_list_end.view.*
import zlc.season.rxdownload2.entity.DownloadFlag
import zlc.season.rxdownload2.RxDownload


/**
 * Created by lvruheng on 2017/7/7.
 */
class DownloadVideosAdapter(context: Context, list: ArrayList<Item>) : RecyclerView.Adapter<DownloadVideosAdapter.DownloadViewHolder>() {
    lateinit var mOnItemLisenter: OnItemClickListener
    lateinit var mOnItemSelectedLisenter: OnItemSelectedListener
    var context: Context? = null
    var list: ArrayList<Item>? = null
    var inflater: LayoutInflater? = null
    var isDownload = false
    var hasLoaded = false
    lateinit var disposable: Disposable
    var mEditMode: Int = MODE_NORMAL

    private val TYPE_STANDARD = 1
    private val TYPE_END = 2

    companion object {
        val MODE_NORMAL = 0
        val MODE_CHECK = 1
    }

    init {
        this.context = context
        this.list = list
        this.inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DownloadViewHolder {
        var itemView: View? = null
        when (viewType) {
            TYPE_STANDARD -> {
                itemView = VideosListItem(parent?.context)
            }
            TYPE_END -> {
                itemView = ListEndView(parent?.context)
                itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
                itemView.tv_text_end.setTextColor(Color.BLACK)
            }
        }
        return DownloadViewHolder(itemView, context!!)
    }

    override fun getItemCount(): Int = if (list?.size == 0) 0 else list?.size!! + 1

    override fun getItemViewType(position: Int): Int {
        if (list?.size == position)
            return TYPE_END
        return TYPE_STANDARD
    }

    override fun onBindViewHolder(holder: DownloadViewHolder?, position: Int) {
//        holder?.setIsRecyclable(false)//item不复用
        val itemViewType = getItemViewType(position)
        when (itemViewType) {
            TYPE_STANDARD -> {
                val itemView : VideosListItem = holder?.itemView as VideosListItem
                if (mEditMode == MODE_CHECK) {
                    itemView.cb_select?.visibility = View.VISIBLE
                } else {
                    itemView.cb_select?.visibility = View.GONE
                }

                itemView.tv_title?.typeface = Typeface.createFromAsset(context?.assets, "fonts/FZLanTingHeiS-L-GB-Regular.TTF")
                val photoUrl: String? = list?.get(position)?.data?.cover?.feed
                photoUrl?.let { GlideUtil.display(context!!, holder?.itemView?.iv_photo, it) }
                val title: String? = list?.get(position)?.data?.title
                holder?.itemView?.tv_title?.text = title
                isDownload = SPUtils.getInstance(context!!, "download_state").getBoolean(list?.get(position)?.data?.playUrl!!)
                getDownloadState(list?.get(position)?.data?.playUrl, holder?.itemView)
                if (isDownload) {
                    itemView.iv_download_state?.setImageResource(R.mipmap.action_stop)
                } else {
                    itemView.iv_download_state?.setImageResource(R.mipmap.action_start)
                }
                itemView.iv_download_state?.setOnClickListener {
                    if (isDownload) {
                        isDownload = false
                        SPUtils.getInstance(context!!, "download_state").put(list?.get(position)?.data?.playUrl!!, false)
                        itemView.iv_download_state?.setImageResource(R.mipmap.action_start)
                        RxDownload.getInstance(context).pauseServiceDownload(list?.get(position)?.data?.playUrl).subscribe()
                    } else {
                        isDownload = true
                        SPUtils.getInstance(context!!, "download_state").put(list?.get(position)?.data?.playUrl!!, true)
                        itemView.iv_download_state?.setImageResource(R.mipmap.action_stop)
                        addMission(list?.get(position)?.data?.playUrl, position + 1)
                    }
                }

                itemView.cb_select?.isChecked = list?.get(position)?.isSelected()!!

                itemView.setOnClickListener {
                    mOnItemLisenter.onItemClick(position, list!!)
                    true
                }
                itemView.cb_select?.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
//                    if (b) {
//                        list?.get(position)!!.setSelected(true)
//                    } else {
//                        list?.get(position)!!.setSelected(false)
//                    }
//                    mOnItemSelectedLisenter.onItemSelected(position)
                }
            }

            TYPE_END -> {
                (holder?.itemView as ListEndView).setShow(true)
            }
        }

    }

    fun setEditMode(editMode: Int) {
        mEditMode = editMode
        notifyDataSetChanged()
    }

    private fun getDownloadState(playUrl: String?, itemView: View?) {
        disposable = RxDownload.getInstance(context).receiveDownloadStatus(playUrl)
                .subscribe { event ->
                    //当事件为Failed时, 才会有异常信息, 其余时候为null.
                    if (event.flag == DownloadFlag.FAILED) {
                        val throwable = event.error
                        Log.w("Error", throwable)
                    }
                    var downloadStatus = event.downloadStatus
                    var percent = downloadStatus.percentNumber
                    if (percent == 100L) {
                        if (!disposable.isDisposed && disposable != null) {
                            disposable.dispose()
                        }
                        hasLoaded = true
                        itemView?.iv_download_state?.visibility = View.GONE
                        itemView?.tv_detail?.text = "已缓存"
                        isDownload = false
                        SPUtils.getInstance(context!!, "download_state").put(playUrl.toString(), false)
                    } else {
                        if (itemView?.iv_download_state?.visibility != View.VISIBLE) {
                            itemView?.iv_download_state?.visibility = View.VISIBLE
                        }
                        if (isDownload) {
                            itemView?.tv_detail?.text = "缓存中 / $percent%"
                        } else {
                            itemView?.tv_detail?.text = "已暂停 / $percent%"
                        }
                    }
                }

    }

    private fun addMission(playUrl: String?, count: Int) {
        RxDownload.getInstance(context).serviceDownload(playUrl, "download$count").subscribe({
            Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show()
        }, {
            Toast.makeText(context, "添加任务失败", Toast.LENGTH_SHORT).show()
        })
    }

    class DownloadViewHolder(itemView: View?, context: Context) : RecyclerView.ViewHolder(itemView)

    interface OnItemClickListener {
        fun onItemClick(position: Int, items: List<Item>)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        mOnItemLisenter = onItemClickListener
    }

    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
        mOnItemSelectedLisenter = onItemSelectedListener
    }
}