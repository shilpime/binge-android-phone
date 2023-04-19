//package com.tatasky.binge.ui.features.home.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.recyclerview.widget.RecyclerView
//import com.tatasky.binge.data.networking.models.response.ContentItem
//import com.tatasky.binge.data.networking.models.response.RailPoint
//import com.tatasky.binge.databinding.*
//import com.tatasky.binge.helper.transparentImageLoad
//import com.tatasky.binge.interfaces.CommonDTOClickListener
//import com.tatasky.binge.ui.base.frameworks.extensions.hide
//import com.tatasky.binge.ui.base.frameworks.extensions.show
//import com.tatasky.binge.ui.features.home.ItemLayoutType
//import com.tatasky.binge.ui.features.home.ItemViewType
//import com.tatasky.binge.ui.features.home.home_trailer.TrailerView
//import com.tatasky.binge.utils.*
//import kotlin.collections.HashSet
//
//class MidscrollAdapter(
//    var list: List<ContentItem>,
//) : RecyclerView.Adapter<MidscrollAdapter.ViewHolder>() {
//
//
//    inner class ViewHolder(val binding:LayoutMidscrollItemBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(contentItem: ContentItem) {
//            binding.root.setOnClickListener {
//
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MidscrollAdapter.ViewHolder {
//        return ViewHolder(
//            LayoutMidscrollItemBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val railPoint = RailPoint()
//        if(railPoint.landscapePoint==null || railPoint.mLandscapeHeight==null || railPoint.mLandscapeWidth==null) {
//            railPoint.landscapePoint = getMidscrollCardDimension(holder.binding.root.context!!)
//            railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
//            railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
//        }
//        val width = railPoint.mLandscapeWidth ?: 0
//        val height = railPoint.mLandscapeHeight ?: 0
//        val layoutParams = ConstraintLayout.LayoutParams(width, height)
//
//
//        layoutParams.setMargins(
//            dpToPx(holder.binding.root.context, 4),//left
//            dpToPx(holder.binding.root.context, 0),//top
//            dpToPx(holder.binding.root.context, 4),//right
//            dpToPx(holder.binding.root.context, 0)//bottom
//        )
//
//        holder.binding.root.layoutParams = layoutParams
//
//
//        holder.bind(list[position])
//    }
//
//    override fun getItemCount(): Int {
//            return list.size
//    }
//
//
//}