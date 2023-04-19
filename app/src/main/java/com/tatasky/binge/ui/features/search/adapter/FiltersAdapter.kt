//package com.tatasky.binge.ui.features.search.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.lifecycle.MutableLiveData
//import androidx.recyclerview.widget.RecyclerView
//import com.tatasky.binge.databinding.LayoutSearchFiltersBinding
//
//class FiltersAdapter(
//    val list: List<String>
//) : RecyclerView.Adapter<FiltersAdapter.FilterViewHolder>() {
//    private val filtersCheckedSet = HashSet<String>()
//    private val liveFiltersHashSet = MutableLiveData<HashSet<String>>()
//
//    fun getFilters():MutableLiveData<HashSet<String>> = liveFiltersHashSet
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
//        return FilterViewHolder(
//            LayoutSearchFiltersBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun getItemCount(): Int {
//        return list.size
//    }
//
//    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
//        val contentItem = list[position]
//        holder.bind(contentItem, filtersCheckedSet.contains(contentItem))
//        holder.binding.filterView.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked)
//                filtersCheckedSet.add(contentItem)
//            else
//                filtersCheckedSet.remove(contentItem)
//            liveFiltersHashSet.postValue(filtersCheckedSet)
//        }
//    }
//
//    class FilterViewHolder(val binding: LayoutSearchFiltersBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(model: String, isChecked: Boolean) {
//            binding.item = model
//            binding.checked = isChecked
//        }
//    }
//}