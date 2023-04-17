package com.tatasky.binge.customviews


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.databinding.LayoutProgressBarBinding
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.utils.e
import java.util.logging.Logger

abstract class EndlessListAdapter<D : Any, VH : RecyclerView.ViewHolder>(dataList: MutableList<D>) :
    ListAdapter<D, VH>(dataList) {
    var autoUpdating = true
    var isAppending = false
        set(isAppending) {
            if (this.isAppending != isAppending) {
                field = isAppending
                try {
                    if (!this.isAppending) notifyItemRemoved(itemCount)
                } catch (exe: Exception) {
                    e("EndlessListAdapter", exe.message)
                }

            }
        }

    override fun getNoContentVisibility(): Int {
        return View.VISIBLE
    }

    override fun getItemCount(): Int {
        return if (isAppending)
            super.getItemCount() + 1
        else
            super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAppending && position >= super.getItemCount())
            if (autoUpdating)
                VIEW_TYPE_PROGRESS
            else
                VIEW_TYPE_MORE
        else
            super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vh: RecyclerView.ViewHolder
        if (viewType == VIEW_TYPE_PROGRESS) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_progress_bar, parent, false)
            vh = ProgressViewHolder(v)
        } else if (viewType == VIEW_TYPE_MORE) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_load_more, parent, false)
            vh = LoadMoreViewHolder(v)
        } else {
            vh = super.onCreateViewHolder(parent, viewType)
        }

        return vh as VH
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (holder is ProgressViewHolder && getItemViewType(position) != VIEW_TYPE_MORE) {
            holder.bind()
        } else {
            super.onBindViewHolder(holder, position)
        }
    }

    class LoadMoreViewHolder(v: View) : RecyclerView.ViewHolder(v){
        /*
        * Nothing to hold
        * */
    }
    class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v){
        val binding: LayoutProgressBarBinding? = DataBindingUtil.bind(v)
        fun bind(){
            (binding?.recyclerProgress?.startProgressAvd(true))
        }
    }

    companion object {
        val VIEW_TYPE_PROGRESS = 333
        val VIEW_TYPE_MORE = 334
    }

    fun getListSize(): Int {
        return mDataList.size
    }


}

