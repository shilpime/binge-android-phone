package com.tatasky.binge.ui.features.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.databinding.ItemSelectPlayerOptionBinding
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.player.model.Bitrate
import java.util.*

class PlayerOptionAdapter internal constructor(
    var itemArrayList: List<Bitrate?>,
    val onClickListener: View.OnClickListener,
    var selectedString: String
) : RecyclerView.Adapter<PlayerOptionAdapter.PlayerOptionViewHolder>() {

    private var prevSelectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerOptionViewHolder {
        return PlayerOptionViewHolder(
            ItemSelectPlayerOptionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlayerOptionViewHolder, position: Int) {
        var title = ""
        val bitrate = itemArrayList?.get(position)
        if (bitrate != null) {
            title = bitrate.getName()!!
        }
        holder.bind(title)
        holder.binding.text1.tag=bitrate

        if(selectedString.equals(title, ignoreCase = true)){
            prevSelectedPosition = position
        }
        holder.binding.text1.isChecked = selectedString.equals(title, ignoreCase = true)

        holder.binding.text1.setOnClickListener(View.OnClickListener {
            selectedString = holder.binding.text1.text.toString()
            notifyItemChanged(position)
            notifyItemChanged(prevSelectedPosition)
            prevSelectedPosition = position
//            notifyDataSetChanged()
            onClickListener.onClick(it)
        })
    }

    class PlayerOptionViewHolder(val binding: ItemSelectPlayerOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appPack: String) {
            binding.title = appPack
        }
    }

    override fun getItemCount(): Int {
        return itemArrayList.size
    }
}