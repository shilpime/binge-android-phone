package com.tatasky.binge.ui.features.transaction_history.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.TransactionHistoryList
import com.tatasky.binge.databinding.LayoutTransactionHistoryItemBinding
import com.tatasky.binge.utils.openInAppBrowserActivityWithoutCustomTab

class TransactionHistoryAdapter(var dataList: List<TransactionHistoryList> = listOf(), private val isNonDTHUser: Boolean) :
    RecyclerView.Adapter<TransactionHistoryAdapter.TransactionHistoryViewHolder>() {

    var invoiceClickListener:OnClickInvoiceIcon?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHistoryViewHolder {
        return TransactionHistoryViewHolder(
            LayoutTransactionHistoryItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    fun setInvoiceDownloadClickListener(invoiceListener:OnClickInvoiceIcon){
        this.invoiceClickListener=invoiceListener
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: TransactionHistoryViewHolder, position: Int) {
        holder.bind(dataList[position], isNonDTHUser)
        holder.mBinding.tvDownloadInvoice.setOnClickListener {
         openInAppBrowserActivityWithoutCustomTab(holder.mBinding.root.context, Uri.parse(dataList[position].weburl))
        }
        holder.mBinding.ivDownload.setOnClickListener {
            invoiceClickListener?.onInvoiceClick(dataList[position].invoiceNo)
        }
    }

    fun clearListener(){
        invoiceClickListener?.apply {
            null
        }
    }

    class TransactionHistoryViewHolder(val mBinding: LayoutTransactionHistoryItemBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(list: TransactionHistoryList, isNonDTHUser: Boolean) {
            mBinding.model = list
            mBinding.isNonDthUser = isNonDTHUser
        }
    }

    interface OnClickInvoiceIcon{
        fun onInvoiceClick(invoiceNo:String?)
    }
}