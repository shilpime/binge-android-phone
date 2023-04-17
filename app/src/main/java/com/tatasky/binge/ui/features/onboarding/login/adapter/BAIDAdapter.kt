package com.tatasky.binge.ui.features.onboarding.login.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.databinding.LayoutBingeMobileIdBinding
import com.tatasky.binge.ui.features.onboarding.login.select_baid.BAIDSelector
import com.tatasky.binge.utils.subscriptionTypeAtv
import com.tatasky.binge.utils.subscriptionTypeFtv

class BAIDAdapter(
    private var dataList: List<LoginResponse.BingeSubscription> = listOf(),
    private val sidSelector: BAIDSelector
) :
    RecyclerView.Adapter<BAIDAdapter.ViewHolder>() {
    private var selected_position = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutBingeMobileIdBinding.inflate(
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
        holder.mBinding.root.setOnClickListener {
            setSelected(holder.adapterPosition)
        }
    }

    fun setSelected(position: Int) {
        selected_position = position
        sidSelector.onSidSelect(dataList[position])
        notifyDataSetChanged()
    }

    fun updateList(dataList: List<LoginResponse.BingeSubscription>, currentBaId: String? = null) {
        this.dataList = dataList
        selected_position = currentBaId?.let { dataList.indexOfFirst { it.baId == currentBaId } } ?: -1
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mBinding: LayoutBingeMobileIdBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(list: LoginResponse.BingeSubscription) {
            mBinding.cbSubscribed.isChecked = selected_position == adapterPosition
            if (true == list.subscriptionType?.equals(subscriptionTypeAtv, true) || true == list.subscriptionType?.equals(subscriptionTypeFtv, true) || !list.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()) {
                mBinding.ivDeviceType.setImageDrawable(AppCompatResources.getDrawable(mBinding.root.context, R.drawable.ic_tv))
            } else {
                mBinding.ivDeviceType.setImageDrawable(AppCompatResources.getDrawable(mBinding.root.context, R.drawable.ic_device))
            }
            mBinding.tvSubId.text = list.aliasName ?: list.baId ?: ""
        }
    }

}