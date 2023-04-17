package com.tatasky.binge.ui.features.myaccount.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.databinding.SubscribersProfileLayoutBinding
import com.tatasky.binge.helper.circularImageLoad
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.getCloudinaryUrl

class SubscriberProfileAdapter(
    val cloudinaryUrl : String?
) :
    RecyclerView.Adapter<SubscriberProfileAdapter.SubscriberProfileViewHolder>() {
    var dataList: List<LoginResponse.BingeSubscription> = listOf()
    var onItemClick: ((LoginResponse.BingeSubscription) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriberProfileViewHolder {
        return SubscriberProfileViewHolder(
            SubscribersProfileLayoutBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: SubscriberProfileViewHolder, position: Int) {
        holder.bind(dataList[position])
        setImage(position,holder,dataList[position])
        holder.mBinding.root.setOnClickListener {
            onItemClick?.invoke(dataList[position])
        }
    }

    fun setItem(list: List<LoginResponse.BingeSubscription>) {
        this.dataList = list
        notifyDataSetChanged()
    }

    class SubscriberProfileViewHolder(val mBinding: SubscribersProfileLayoutBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(list: LoginResponse.BingeSubscription) {
            mBinding.model = list
        }
    }

    private fun setImage(
        position: Int,
        holder: SubscriberProfileViewHolder,
        selectedProfile: LoginResponse.BingeSubscription
    ) {
        //if(dataList[position].imageUrl.isNullOrEmpty()) {
            holder.mBinding.layoutAccount.flProfileImage.hide()
            holder.mBinding.layoutAccount.flProfileText.show()
            holder.mBinding.layoutAccount.tvLetter.text= (selectedProfile.aliasName?.substring(0,1)?: "").toUpperCase()
//        }else{
//            holder.mBinding.layoutAccount.flProfileImage.show()
//            holder.mBinding.layoutAccount.flProfileText.hide()
//            val w = dpToPx(holder.mBinding.root.context!!, 65)
//            val imgUrl = getCloudinaryUrl(
//                cloudinaryUrl, w, w,
//                dataList[position].imageUrl!!
//            )
//            circularImageLoad(holder.mBinding.layoutAccount.profileImage, imgUrl)
//        }
    }
}