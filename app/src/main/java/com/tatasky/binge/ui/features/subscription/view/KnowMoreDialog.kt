package com.tatasky.binge.ui.features.subscription.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.tatasky.binge.customviews.CustomDialog
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.LayoutKnowMoreDialogBinding

class KnowMoreDialog() : CustomDialog() {

    private lateinit var binding: LayoutKnowMoreDialogBinding
    private var knowMoreDetails: PartnerPacks.KnowMore? = null

    private constructor(knowMoreDetails: PartnerPacks.KnowMore) : this() {
        this.knowMoreDetails = knowMoreDetails
    }

    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = LayoutKnowMoreDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.knowMore = this.knowMoreDetails
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(savedInstanceState !=null){
            dismiss()
        }else {
            Glide.with(binding.ivDialog.context).load(knowMoreDetails?.imageUrl).override(Target.SIZE_ORIGINAL).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(binding.ivDialog)
            binding.tvDialogMsg.text = knowMoreDetails?.value?.split(":").takeIf { (it?.size?:0) > 1 }?.joinToString(separator = "\n \n• ", prefix = "• ")?:knowMoreDetails?.value
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(knowMoreDetails: PartnerPacks.KnowMore): KnowMoreDialog =
            KnowMoreDialog(knowMoreDetails)
    }
}