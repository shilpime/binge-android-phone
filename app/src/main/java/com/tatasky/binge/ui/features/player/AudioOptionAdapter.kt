package com.tatasky.binge.ui.features.player

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.databinding.ItemSelectPlayerOptionBinding
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.utils.e
import java.util.ArrayList

class AudioOptionAdapter internal constructor(
    var itemArrayList : ArrayList<String>,
    val onClickListener : View.OnClickListener,
    var selectedString : String
) : RecyclerView.Adapter<AudioOptionAdapter.PlayerOptionViewHolder>() {

    private val stringStringHashMap: HashMap<String, String> = HashMap()
    init {
        stringStringHashMap.put("eng", "English")
        stringStringHashMap.put("tam", "Tamil")
        stringStringHashMap.put("hin", "Hindi")
        stringStringHashMap.put("ben", "Bengali")
        stringStringHashMap.put("tel", "Telugu")
        stringStringHashMap.put("pan", "Punjabi")
        stringStringHashMap.put("mar", "Marathi")
        stringStringHashMap.put("kan", "Kannada")
        stringStringHashMap.put("ori", "Oriya")
        stringStringHashMap.put("guj", "Gujarati")
        stringStringHashMap.put("mal", "Malayalam")
        stringStringHashMap.put("asm", "Assamese")
        stringStringHashMap.put("bho", "Bhojpuri")
        stringStringHashMap.put("urd", "Urdu")
        stringStringHashMap.put("doi", "Dogri")
        stringStringHashMap.put("kas", "Kashmiri")
        stringStringHashMap.put("kok", "Konkani")
        stringStringHashMap.put("mai", "Maithili")
        stringStringHashMap.put("mni", "Manipuri")
        stringStringHashMap.put("nep", "Nepali")
        stringStringHashMap.put("san", "Sanskrit")
        stringStringHashMap.put("sat", "Santhali")
        stringStringHashMap.put("snd", "Sindhi")
    }

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
        title = itemArrayList[position]

        holder.binding.text1.isChecked = selectedString.equals(title, ignoreCase = true)

        if (!TextUtils.isEmpty(stringStringHashMap[title]))
            title = stringStringHashMap[title]!!

        holder.binding.text1.tag=title
        holder.binding.text1.setOnClickListener(View.OnClickListener {
            val currentSelectedLanguage = it.tag.toString()

            selectedString = holder.binding.text1.text.toString()
            notifyDataSetChanged()
            onClickListener.onClick(it)
        })
        holder.bind(title)
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