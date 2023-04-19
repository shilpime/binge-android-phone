package com.tatasky.binge.ui.features.onboarding.marketing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.MarketingResponseList
import com.tatasky.binge.databinding.LayoutMarketingBinding
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.utils.getCloudinaryUrl

class MarketingAdapter(var dataList: List<MarketingResponseList> = listOf(),val cloudinaryUrl: String?) : RecyclerView.Adapter<MarketingAdapter.MarketingViewHolder>() {


    class MarketingViewHolder(val mBinding: LayoutMarketingBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(list: MarketingResponseList, cloudinaryUrl: String?) {
            mBinding.heading.text = list.title
            mBinding.subHeading.text = list.description
            val url = list.image?.let {
                getCloudinaryUrl(
                    cloudinaryUrl,
                    it
                )
            }
            if (url != null) {
                transparentImageLoad(mBinding.mainIv, url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingViewHolder {
        return MarketingViewHolder(
            LayoutMarketingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        if(dataList.size == 1){
            return dataList.size
        }
        else
            return Integer.MAX_VALUE

    }

    override fun onBindViewHolder(holder: MarketingAdapter.MarketingViewHolder, position: Int) {
        if (dataList.size > 0) {
            holder.bind(dataList[position % dataList.size], cloudinaryUrl)
        }
    }



}