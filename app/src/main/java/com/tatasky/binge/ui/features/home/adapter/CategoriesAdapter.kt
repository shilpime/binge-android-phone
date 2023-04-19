package com.tatasky.binge.ui.features.home.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.LeftMenuItem
import com.tatasky.binge.databinding.ItemCategoriesBottomsheetBinding
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.getCloudinaryUrl
import com.tatasky.binge.utils.getLanguageWidgetWidth

class CategoriesAdapter(
    private val mSelectedCategory: MutableLiveData<SingleEvent<Pair<String, String>>>,
    val cloudenieryUrl: String?
) :
    RecyclerView.Adapter<CategoriesAdapter.CategoriesItemViewHolder>() {
    private var mCategoryLeftMenuItemList = mutableListOf<LeftMenuItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesItemViewHolder {
        return CategoriesItemViewHolder(
            ItemCategoriesBottomsheetBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(categoriesItemViewHolder: CategoriesItemViewHolder, position: Int) {
        categoriesItemViewHolder.bind(
            position,cloudenieryUrl
        )
    }

    override fun getItemCount(): Int {
        return mCategoryLeftMenuItemList.size
    }

    fun setCategoryItemList(categoryItemList: List<LeftMenuItem>) {
        this.mCategoryLeftMenuItemList.clear()
        this.mCategoryLeftMenuItemList.addAll(categoryItemList)
        notifyDataSetChanged()
    }

    fun getCategoryItemList() = mCategoryLeftMenuItemList

    inner class CategoriesItemViewHolder(val binding: ItemCategoriesBottomsheetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            position: Int, cloudenieryUrl: String?
        ) {
            val point = getLanguageWidgetWidth(binding.root.context!!)
            val width = point.x
            val height = dpToPx(binding.root.context, 90)
            val layoutParams =
                ConstraintLayout.LayoutParams(width, height)
            layoutParams.setMargins(
                dpToPx(binding.root.context, 4),
                dpToPx(binding.root.context, 4),
                dpToPx(binding.root.context, 4),
                dpToPx(binding.root.context, 8)
            )
            binding.clRootCategory.layoutParams = layoutParams
            binding.tvTitle.text = mCategoryLeftMenuItemList[position].pageName



            mCategoryLeftMenuItemList[position].subPageImage?.let { bgImage ->4
                val url = getCloudinaryUrl(
                    cloudenieryUrl,
                    width, height,
                    bgImage
                )
                transparentImageLoad(binding.ivBgCategory,url)
            }
            binding.clRootCategory.setOnClickListener {
                mCategoryLeftMenuItemList[position].let { leftMenuItem ->
                    leftMenuItem.pageName?.let { categoryPageName ->
                        leftMenuItem.pageType?.let { categoryPageType ->
                            mSelectedCategory.postValue(SingleEvent(Pair(categoryPageName,
                                categoryPageType)))
                        }
                    }

                }
            }
        }

    }
}