package com.tatasky.binge.ui.features.myaccount.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.DeviceList
import com.tatasky.binge.databinding.LayoutItemDeviceBinding
import com.tatasky.binge.interfaces.DeviceDeleteClickListener
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show

class DeviceAdapter(
    private var list: MutableList<DeviceList>,
    private var currentDeviceId: String,
    val listener: DeviceDeleteClickListener
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private var shouldHideSmallDevicesHeader: Boolean = false
    private var maxAllowedSmallDevicesCount: Int = 0
    private var isFTVUSer: Boolean = false
    private var smallDeviceFooterMessage :String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            LayoutItemDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val contentItem = list[position]
        holder.bind(contentItem)
        if(contentItem.primary)
            holder.binding.ivDevice.setImageResource(R.drawable.ic_tv)
        else if("WEB".equals(contentItem.deviceType, ignoreCase = true))
            holder.binding.ivDevice.setImageResource(R.drawable.ic_browser)
        else
            holder.binding.ivDevice.setImageResource(R.drawable.ic_device)

        if(contentItem.deviceNumber == currentDeviceId){
            holder.binding.tvCurrentDevice.show()
            holder.binding.ivDelete.hide()
        }
        else{
            holder.binding.tvCurrentDevice.hide()
            holder.binding.ivDelete.show()
        }
        holder.binding.ivDelete.setOnClickListener {
            listener.onDeviceDelete(contentItem)
        }
        if(position == 0){
            if (shouldHideSmallDevicesHeader)
                holder.binding.tvTitle.hide()
            else
                holder.binding.tvTitle.show()
        }
        else{
            holder.binding.tvTitle.hide()
        }
        val text =
            if(isFTVUSer)
                holder.binding.tvCount.context!!.resources.getString(R.string.m_out_3)
            else
                holder.binding.tvCount.context!!.resources.getString(R.string.m_out_n)
        val countText = smallDeviceFooterMessage ?: String.format(text,
            list.size, maxAllowedSmallDevicesCount)
        holder.binding.tvCount.text = countText
        if(position == list.size-1){
            holder.binding.tvCount.show()
        }
        else{
            holder.binding.tvCount.hide()
        }
    }

    fun updateList(
        deviceList: List<DeviceList>,
        currentDeviceId: String,
        isPrimaryDevice: Boolean,
        smallDeviceFooterMessage: String?,
        smallDevicesCount: Int,
        shouldHideSmallDevicesHeader: Boolean
    ) {
        this.currentDeviceId = currentDeviceId
        this.isFTVUSer = isPrimaryDevice
        this.smallDeviceFooterMessage = smallDeviceFooterMessage
        maxAllowedSmallDevicesCount = smallDevicesCount
        this.shouldHideSmallDevicesHeader = shouldHideSmallDevicesHeader
        list.clear()
        list.addAll(deviceList)
        notifyDataSetChanged()
    }

    class DeviceViewHolder(val binding: LayoutItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: DeviceList) {
            binding.item = model
        }
    }
}