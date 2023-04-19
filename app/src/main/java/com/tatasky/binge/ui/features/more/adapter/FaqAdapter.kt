package com.tatasky.binge.ui.features.more.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.FaqResponse
import com.tatasky.binge.databinding.LayoutFaqListItemBinding

class FaqAdapter : RecyclerView.Adapter<FaqAdapter.ViewHolder>() {
    private var previousExpandedPosition = -1
    private var mExpandedPosition = -1

    private lateinit var faqList: List<FaqResponse.Question>
    fun setList(faqList: List<FaqResponse.Question>) {
        this.faqList = faqList
    }

    class ViewHolder(val binding: LayoutFaqListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(faq:FaqResponse.Question) {
            binding.model = faq
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutFaqListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return faqList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val isExpanded = position == mExpandedPosition
        holder.binding.faqDetailTv.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.binding.expandFaqArrow.setImageResource(if (isExpanded) R.drawable.ic_filter_expanded else R.drawable.ic_filter_collapsed)

        holder.itemView.isActivated = isExpanded

        if (isExpanded) previousExpandedPosition = position

        holder.itemView.setOnClickListener {
            mExpandedPosition = if (isExpanded) -1 else position
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(position)
            if(!isExpanded)
                expand(holder.binding.faqDetailTv)
        }

        val faq = faqList[position]
        holder.bind(faq)
    }

    private fun expand(v: View) {
        val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            (v.parent as View).width,
            View.MeasureSpec.EXACTLY
        )
        val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            0,
            View.MeasureSpec.UNSPECIFIED
        )
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = v.measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.layoutParams.height = 1
        v.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
            override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation
            ) {
                v.layoutParams.height =
                    if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        a.duration = 100L
        v.startAnimation(a)
    }
}