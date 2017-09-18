package com.tt.lvruheng.eyepetizer.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    lateinit var mOnLongLisenter: OnLongClickListener
    var context: Context? = null
    var list: ArrayList<Item>? = null
    var inflater: LayoutInflater? = null
    var isDownload = false
    var hasLoaded = false
    lateinit var disposable: Disposable

    private val TYPE_STANDARD = 1
    private val TYPE_END = 2

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

    override fun getItemCount(): Int = if(list?.size == 0) 0 else list?.size!! + 1

    override fun getItemViewType(position: Int): Int {
        if (list?.size == position)
            return TYPE_END
        return TYPE_STANDARD
    }

    override fun onBindViewHolder(holder: DownloadViewHolder?, position: Int) {
        val itemViewType = getItemViewType(position)
        if(itemViewType == TYPE_END) return
        when (itemViewType) {
            TYPE_STANDARD -> {
                holder?.itemView?.tv_title?.typeface = Typeface.createFromAsset(context?.assets, "fonts/FZLanTingHeiS-L-GB-Regular.TTF")
                var photoUrl: String? = list?.get(position)?.data?.cover?.feed
                photoUrl?.let { GlideUtil.display(context!!, holder?.itemView?.iv_photo, it) }
                var title: String? = list?.get(position)?.data?.title
                holder?.itemView?.tv_title?.text = title
                isDownload = SPUtils.getInstance(context!!, "download_state").getBoolean(list?.get(position)?.data?.playUrl!!)
                getDownloadState(list?.get(position)?.data?.playUrl, holder?.itemView)
                if (isDownload) {
                    holder?.itemView?.iv_download_state?.setImageResource(R.mipmap.action_stop)
                } else {
                    holder?.itemView?.iv_download_state?.setImageResource(R.mipmap.action_start)
                }
                holder?.itemView?.iv_download_state?.setOnClickListener {
                    if (isDownload) {
                        isDownload = false
                        SPUtils.getInstance(context!!, "download_state").put(list?.get(position)?.data?.playUrl!!, false)
                        holder?.itemView?.iv_download_state?.setImageResource(R.mipmap.action_start)
                        RxDownload.getInstance(context).pauseServiceDownload(list?.get(position)?.data?.playUrl).subscribe()
                    } else {
                        isDownload = true
                        SPUtils.getInstance(context!!, "download_state").put(list?.get(position)?.data?.playUrl!!, true)
                        holder?.itemView?.iv_download_state?.setImageResource(R.mipmap.action_stop)
                        addMission(list?.get(position)?.data?.playUrl, position + 1)
                    }
                }
                holder?.itemView?.setOnClickListener {
                    //跳转视频详情页
                    v->v.context.toActivityWithSerializable<DetailActivity>(list?.get(position)!!)
                }
                holder?.itemView?.setOnLongClickListener {
                    mOnLongLisenter.onLongClick(position)
                    true
                }
            }

            TYPE_END -> {
                (holder?.itemView as ListEndView).setShow(true)
            }
        }

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
                        if(!disposable.isDisposed&&disposable!= null){
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

    interface OnLongClickListener {
        fun onLongClick(position: Int)
    }

    fun setOnLongClickListener(onLongClickListener: OnLongClickListener) {
        mOnLongLisenter = onLongClickListener
    }
}