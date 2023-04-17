package com.tatasky.binge.customviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R

abstract class ListAdapter<D, VH : RecyclerView.ViewHolder>(protected var mDataList: MutableList<D> = mutableListOf()) :
    RecyclerView.Adapter<VH>() {

    open fun getNoContentVisibility(): Int{
        return View.VISIBLE
    }

    fun setmDataList(mDataList: MutableList<D>) {
        this.mDataList = mDataList
        notifyDataSetChanged()
    }

    fun addTomDataList(mDataList: List<D>) {
        val initialCount = this.mDataList.size
        this.mDataList.addAll(mDataList)
        notifyItemRangeInserted(initialCount, this.mDataList.size)
    }


    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun updateDataWithDiffCallback(newList:List<D>, diffUtil: DiffUtil.DiffResult){
        this.mDataList.clear()
        this.mDataList.addAll(newList)
        diffUtil.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataList.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            getNormalItemViewType(position)
        }
    }

    open fun clear() {
        val size = mDataList.size
        mDataList.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun addAll(data: List<D>) {
        mDataList.addAll(data)
        notifyItemInserted(mDataList.size)
    }

    protected fun getItemAt(position: Int): D {
        return mDataList[position]
    }

    protected fun replaceItemAt(position: Int, newItem: D): D {
        val item = mDataList.set(position, newItem)
        notifyItemChanged(position)
        return item
    }

    fun addItem(index: Int, newItem: D) {
        mDataList.add(index, newItem)
        notifyItemRangeChanged(index, mDataList.size)
    }

    protected fun addItem(newItem: D) {
        mDataList.add(newItem)
        notifyItemInserted(mDataList.size - 1)
    }

    protected fun removeItem(index: Int): D? {
        if (mDataList.size > 0) {
            val item = mDataList.removeAt(index)
            notifyItemRemoved(index)
            return item
        }
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vh: RecyclerView.ViewHolder
        if (viewType == VIEW_TYPE_EMPTY) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_empty_view, parent, false)
            val noContent = v.findViewById<View>(R.id.txv_frag_livetv_error) as TextView
            noContent.visibility = getNoContentVisibility()
            vh = EmptyViewHolder(v)
        } else {
            vh = createNormalViewHolder(parent, viewType)
        }

        return vh as VH
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (holder is EmptyViewHolder) {
            // do nothing
        } else {
            // Prevent binding of child view holder when List is NULL/Empty
            if (mDataList.isNotEmpty()) {
                bindNormalViewHolder(holder, position)
            }
        }
    }

    protected abstract fun createNormalViewHolder(parent: ViewGroup, viewType: Int): VH

    protected abstract fun bindNormalViewHolder(holder: VH, position: Int)

    protected open fun getNormalItemViewType(position: Int): Int {
        return VIEW_TYPE_NORMAL
    }

    class EmptyViewHolder(v: View) : RecyclerView.ViewHolder(v)

    companion object {

        protected val VIEW_TYPE_NORMAL = 0
        val VIEW_TYPE_EMPTY = 111
    }
}

