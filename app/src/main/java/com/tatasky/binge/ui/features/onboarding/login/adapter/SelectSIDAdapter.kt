package com.tatasky.binge.ui.features.onboarding.login.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.SubscriberIdListResponse
import com.tatasky.binge.databinding.LayoutSubscriberIdBinding
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.onboarding.login.select_sid.SIDSelector
import com.tatasky.binge.utils.SubscriptionPackStatusEnum

class SelectSIDAdapter(
    var dataList: List<SubscriberIdListResponse.SubscriberDetail> = listOf(),
    val sidSelector: SIDSelector
) :
    RecyclerView.Adapter<SelectSIDAdapter.ViewHolder>() {
    private var selected_position = -1
    private var previous_selected_position = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutSubscriberIdBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subscriberProfileListModel = dataList[position]
        holder.bind(subscriberProfileListModel)
        //Quick fix may need changes
        holder.setIsRecyclable(false)
        holder.mBinding.root.setOnClickListener {
            if (holder.adapterPosition >= 0) {
                sidSelector.onSidSelect(dataList[holder.adapterPosition])
                setSelected(holder.adapterPosition)
            }
        }
    }

    private fun setSelected(position: Int) {
        selected_position = position
        notifyDataSetChanged()
    }

    fun updateList(dataList: List<SubscriberIdListResponse.SubscriberDetail>) {
        this.dataList = dataList
        selected_position = -1
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mBinding: LayoutSubscriberIdBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(list: SubscriberIdListResponse.SubscriberDetail) {
            mBinding.cbSubscribed.isChecked = selected_position == adapterPosition
            if (list.listOfBaIds.isNullOrEmpty())
                mBinding.tvBingeActive.hide()
            else if (true == list.statusType?.equals("inactive" , true)) {
                mBinding.tvBingeActive.text = mBinding.root.context.getString(R.string.binge_deactive)
                mBinding.tvBingeActive.setTextColor(ContextCompat.getColor(mBinding.root.context, R.color.darkError))
                mBinding.tvBingeActive.show()
            } else if(true == list.statusType?.equals("active" , true)){
                mBinding.tvBingeActive.text = mBinding.root.context.getString(R.string.binge_active)
                mBinding.tvBingeActive.setTextColor(ContextCompat.getColor(mBinding.root.context, R.color.darkOnSecondary))
                mBinding.tvBingeActive.show()
            }else{
                mBinding.tvBingeActive.show()
            }
            mBinding.tvSubId.text = list.sid
        }
    }

}