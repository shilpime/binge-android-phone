package com.tatasky.binge.ui.features.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.tatasky.binge.databinding.ItemSelectPlayerOptionBinding

class TracksAdapter(
    val isDisableEnabled: Boolean,
    selectedFormat: Format?,
    val listOfFormats: List<Format>,
    val clickListener: View.OnClickListener
) : RecyclerView.Adapter<TracksAdapter.PlayerOptionViewHolder>() {

    var mSelectedFormat = selectedFormat
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
        if (isDisableEnabled && position == 0) {
            holder.bind(
                "None", mSelectedFormat == null
            )
            holder.binding.root.setOnClickListener(View.OnClickListener {
                it.tag = null
                mSelectedFormat = null
                notifyDataSetChanged()
                clickListener.onClick(it)
            })
        } else {
            val format =
                if (isDisableEnabled) listOfFormats[position - 1] else listOfFormats[position]
            holder.bind(
                DefaultTrackNameProvider(holder.itemView.resources).getTrackName(format)
                    .split(",")[0].capitalize(), mSelectedFormat == format
            )
            holder.binding.root.setOnClickListener(View.OnClickListener {
                it.tag = format
                mSelectedFormat = format
                if (isDisableEnabled)
                    notifyDataSetChanged()
                else
                    notifyItemRangeChanged(0,listOfFormats.size)
                clickListener.onClick(it)
            })
        }
    }

    override fun getItemCount(): Int {
        return if (isDisableEnabled)
            listOfFormats.size + 1
        else listOfFormats.size
    }

    class PlayerOptionViewHolder(val binding: ItemSelectPlayerOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appPack: String, selected: Boolean) {
            binding.title = appPack
            binding.text1.isChecked = selected
        }
    }
}
