package com.tatasky.binge.ui.features.subscription.adapter

import android.graphics.Point
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.ComparePlanTickBinding
import com.tatasky.binge.helper.circularImageApps
import com.tatasky.binge.ui.base.frameworks.extensions.invisible
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.base.frameworks.extensions.hide


class CompareItemAdapter(
    val headList: List<String?>,
    val list: List<PartnerPacks.PartnerList>,
    var isHeader: Boolean = false,
    var hideFooterFlag: Boolean = false,
    val mappingList: List<PartnerPacks.PartnerList>,
) : RecyclerView.Adapter<CompareItemAdapter.ViewHolder>() {

    var spanCount = 0
    var point : Point? = null
    var recyclerView : RecyclerView ?= null
    var map:Map<String,Boolean>? = null


    inner class ViewHolder(val binding: ComparePlanTickBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item:String,position: Int){
            if(hideFooterFlag && position == list.size-1){
                binding.comparePlanDivider.hide()
            }
            if(isHeader){
                binding.compareTickTv.invisible()
                circularImageApps(binding.comparePlanIcon, list[position].iconUrl ?: "")
            }

            val partnerIdList = list.map {
                it.partnerId
            }



            val includedPartnerList  = mutableListOf<String>()

            mappingList.forEach {
                if (it.included == true){
                    it.partnerId?.let { it1 -> includedPartnerList.add(it1) }
                }
            }

            val partneridListWithFooter = mutableListOf<String>()

            list.forEach {
                if(!it.footerMessage.isNullOrBlank()){
                    it.partnerId?.let { it1 -> partneridListWithFooter.add(it1) }
                }
            }

            //Removed Seprate partner footer message
            if((item in partnerIdList) && (item in includedPartnerList)){
                binding.ivCompareTick.show()
                binding.compareTickTv.invisible()
            } else {
                binding.compareTickTv.invisible()
                binding.ivCompareTick.invisible()
            }


        //Code for adding Seprate partner footer message
//            if((item in partnerIdList) && (item in includedPartnerList)){
//                binding.ivCompareTick.show()
//                if (!isHeader) {
//                    if(item in partneridListWithFooter) {
//                        binding.compareTickTv.show()
//                        binding.ivCompareTick.invisible()
//                        binding.compareTickTv.text = list.filter {
//                            it.partnerId == item
//                        }.getOrNull(0)?.footerMessage
//                    } else {
//                        binding.compareTickTv.invisible()
//                    }
//                }
//            } else {
//                binding.compareTickTv.invisible()
//                binding.ivCompareTick.invisible()
//            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ComparePlanTickBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }



    override fun getItemCount(): Int {
        return headList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        headList[position]?.let { holder.bind(it,position) }
    }

}