package com.tt.lvruheng.eyepetizer.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.tt.lvruheng.eyepetizer.utils.SPUtils
import com.xk.eyepetizer.R
import com.xk.eyepetizer.mvp.model.bean.Item
import com.xk.eyepetizer.toActivityWithSerializable
import com.xk.eyepetizer.ui.activity.DetailActivity
import com.xk.eyepetizer.util.GlideUtil
import io.reactivex.disposables.Disposable
import zlc.season.rxdownload2.entity.DownloadFlag
import zlc.season.rxdownload2.RxDownload


/**
 * Created by lvruheng on 2017/7/7.
 */
class VideoListAdapter(context: Context, list: ArrayList<Item>) : RecyclerView.Adapter<VideoListAdapter.DownloadViewHolder>() {
    lateinit var mOnLongLisenter: OnLongClickListener
    var context: Context? = null;
    var list: ArrayList<Item>? = null
    var inflater: LayoutInflater? = null
    var isDownload = false
    var hasLoaded = false
    lateinit var disposable: Disposable

    init {
        this.context = context
        this.list = list
        this.inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DownloadViewHolder {
        return DownloadViewHolder(inflater?.inflate(R.layout.item_videolist, parent, false), context!!)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: DownloadViewHolder?, position: Int) {
        var photoUrl: String? = list?.get(position)?.data?.thumbPlayUrl
        photoUrl?.let { GlideUtil.display(context!!, holder?.iv_photo, it) }
        var title: String? = list?.get(position)?.data?.title
        holder?.tv_title?.text = title
        var category = list?.get(position)?.data?.category
        var duration = list?.get(position)?.data?.duration
        isDownload = SPUtils.getInstance(context!!, "download_state").getBoolean(list?.get(position)?.data?.playUrl!!)
        getDownloadState(list?.get(position)?.data?.playUrl, holder)
        if (isDownload) {
            holder?.iv_download_state?.setImageResource(R.mipmap.action_stop)
        } else {
            holder?.iv_download_state?.setImageResource(R.mipmap.action_start)
        }
        holder?.iv_download_state?.setOnClickListener {
            if (isDownload) {
                isDownload = false
                SPUtils.getInstance(context!!, "download_state").put(list?.get(position)?.data?.playUrl!!, false)
                holder?.iv_download_state?.setImageResource(R.mipmap.action_start)
                RxDownload.getInstance(context).pauseServiceDownload(list?.get(position)?.data?.playUrl).subscribe()
            } else {
                isDownload = true
                SPUtils.getInstance(context!!, "download_state").put(list?.get(position)?.data?.playUrl!!, true)
                holder?.iv_download_state?.setImageResource(R.mipmap.action_stop)
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

    private fun getDownloadState(playUrl: String?, holder: DownloadViewHolder?) {
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
                        holder?.iv_download_state?.visibility = View.GONE
                        holder?.tv_detail?.text = "已缓存"
                        isDownload = false
                        SPUtils.getInstance(context!!, "download_state").put(playUrl.toString(), false)
                    } else {
                        if (holder?.iv_download_state?.visibility != View.VISIBLE) {
                            holder?.iv_download_state?.visibility = View.VISIBLE
                        }
                        if (isDownload) {
                            holder?.tv_detail?.text = "缓存中 / $percent%"
                        } else {
                            holder?.tv_detail?.text = "已暂停 / $percent%"
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

    class DownloadViewHolder(itemView: View?, context: Context) : RecyclerView.ViewHolder(itemView) {
        var iv_photo: ImageView = itemView?.findViewById(R.id.iv_photo) as ImageView
        var tv_title: TextView = itemView?.findViewById(R.id.tv_title) as TextView
        var tv_detail: TextView = itemView?.findViewById(R.id.tv_detail) as TextView
        var iv_download_state: ImageView = itemView?.findViewById(R.id.iv_download_state) as ImageView

        init {
            tv_title?.typeface = Typeface.createFromAsset(context?.assets, "fonts/FZLanTingHeiS-L-GB-Regular.TTF")
        }
    }

    interface OnLongClickListener {
        fun onLongClick(position: Int)
    }

    fun setOnLongClickListener(onLongClickListener: OnLongClickListener) {
        mOnLongLisenter = onLongClickListener
    }
}