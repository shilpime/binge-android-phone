package com.tatasky.binge.ui.features.home.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.databinding.ItemSelectLanguageBinding
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.ContentLanguageFragment
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.getCloudinaryUrl
import com.tatasky.binge.utils.getLanguageWidgetWidth

class SelectLanguageAdapter(
    private val cloudinaryUrl: String?,
    private val selectLanguageListener: ((Boolean) -> Unit)? = null
) :
    RecyclerView.Adapter<SelectLanguageAdapter.LanguageItemViewHolder>() {
    private var isSaveLanguageButtonAvailable = true
    private var mLanguageItemList = mutableListOf<ContentItem>()
    private var mSelectedLanguageItemList = mutableListOf<String>()
    private var mSelectedLanguageNameList = mutableListOf<String>()
    private val mEnableSelectContentLanguageButton =
        MutableLiveData(SingleEvent(false))
    private var mMaxAllowedSelectedLanguage:Int? = null
    private var fromClassName: String? = null
    private var _originalSelectedLanguageId = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageItemViewHolder {
        return LanguageItemViewHolder(
            ItemSelectLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(languageItemViewHolder: LanguageItemViewHolder, position: Int) {
        languageItemViewHolder.bind(
            position
        )
    }

    override fun getItemCount(): Int {
        return mLanguageItemList.size
    }

    fun setLanguageItemList(
        languageItemList: List<ContentItem>,
        preferredLanguages: HashSet<Int> = hashSetOf(),
        fromClassName: String?,
    ) {
        this.fromClassName = fromClassName
        this.mLanguageItemList.clear()
        this.mLanguageItemList.addAll(languageItemList)
        if (preferredLanguages.isNotEmpty()) {
            languageItemList.forEachIndexed { index, language ->
                if (preferredLanguages.contains(language.id.toInt())) {
                    if (fromClassName == ContentLanguageFragment::class.java.name)
                        _originalSelectedLanguageId.add(language.id)
                    handleLanguageItemClick(index)
                }
            }
        }
        notifyDataSetChanged()
    }
    //mSelectedLanguageNameList
    fun getSelectedLanguageNameList(): MutableList<String> {
        return mSelectedLanguageNameList
    }
    fun getSelectedLanguageItemList(): MutableList<String> {
        return mSelectedLanguageItemList
    }
    fun setIsSaveLanguageButtonAvailable(status:Boolean){
        isSaveLanguageButtonAvailable = status
    }
    fun setMaxAllowedSelectedLanguage(count:Int?){
        mMaxAllowedSelectedLanguage = count
    }
    fun isSaveLanguageButtonAvailable():Boolean = isSaveLanguageButtonAvailable


    fun getSelectContentLanguageButtonStatus(): LiveData<SingleEvent<Boolean>> =
        mEnableSelectContentLanguageButton

    inner class LanguageItemViewHolder(val binding: ItemSelectLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(
            position: Int
        ) {
            val point = getLanguageWidgetWidth(binding.root.context!!)
            val width = point.x//dpToPx(binding.root.context, 164)
            val height = dpToPx(binding.root.context, 54)
            //commented for tablet ui (two pane)
            val layoutParams =
                LinearLayout.LayoutParams(width, height)
            layoutParams.setMargins(
                dpToPx(binding.root.context, 4),
                0,
                dpToPx(binding.root.context, 4),
                dpToPx(binding.root.context, 8)
            )
           // binding.clLanguage.layoutParams = layoutParams
            binding.tvTitle.text = mLanguageItemList[position].title
            binding.clLanguage.isSelected = mLanguageItemList[position].isSelected
            val url = getCloudinaryUrl(
                cloudinaryUrl,
                mLanguageItemList[position].image
            )
            transparentImageLoad(binding.ivNativeName, url)
            val defaultResource = binding.root.context.resources.getDrawable(R.drawable.ic_language_background)
            binding.cvLanguage.background = defaultResource
            val bgUrl = mLanguageItemList[position].backgroundImage?.let {
                getCloudinaryUrl(
                    cloudinaryUrl,
                    width,height,
                    it
                )
            }?:""
            Glide.with(binding.root.context)
                .load(bgUrl)
                .apply(
                    RequestOptions().transform(
                        RoundedCorners(10)
                    )
                ).into(object :
                    CustomViewTarget<CardView, Drawable>(binding.cvLanguage) {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        binding.cvLanguage.background = defaultResource
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        binding.cvLanguage.background = resource
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                        binding.cvLanguage.background = defaultResource
                    }

                })
            binding.clLanguage.setOnClickListener {
                mMaxAllowedSelectedLanguage?.let { maxSelectedLanguage ->
                    if (mSelectedLanguageItemList.size >= maxSelectedLanguage
                        && !mLanguageItemList[position].isSelected
                    ) {
                        selectLanguageListener?.invoke(true)
                        return@setOnClickListener
                    }
                }
                handleLanguageItemClick(position, it)
                binding.clLanguage.isSelected = mLanguageItemList[position].isSelected
            }
        }

    }

    private fun handleLanguageItemClick(position: Int, view: View? = null) {
        mLanguageItemList[position].isSelected = !mLanguageItemList[position].isSelected
        if (mLanguageItemList[position].isSelected) {
            mSelectedLanguageItemList.add(mLanguageItemList[position].id)
            mSelectedLanguageNameList.add(mLanguageItemList[position].title)
            view?.let {
                val zoomInAnim = AnimationUtils.loadAnimation(it.context, R.anim.zoom_in)
                it.startAnimation(zoomInAnim)
            }

        } else {
            mSelectedLanguageItemList.remove(mLanguageItemList[position].id)
            mSelectedLanguageNameList.remove(mLanguageItemList[position].title)
            view?.let {
                val zoomOutAnim = AnimationUtils.loadAnimation(it.context, R.anim.zoom_out)
                it.startAnimation(zoomOutAnim)
            }
        }
        if (fromClassName == ContentLanguageFragment::class.java.name) {
            val hasUserChangedSelection =
                mSelectedLanguageItemList != _originalSelectedLanguageId &&
                        (!mSelectedLanguageItemList.containsAll(_originalSelectedLanguageId)) ||
                        (mSelectedLanguageItemList.size != _originalSelectedLanguageId.size)
            mEnableSelectContentLanguageButton.value = SingleEvent(hasUserChangedSelection)
        }
        else {
            if (mSelectedLanguageItemList.size > 0
                && mEnableSelectContentLanguageButton.value?.peekContent() == false
            ) {
                mEnableSelectContentLanguageButton.value = SingleEvent(true)
            } else if (mSelectedLanguageItemList.size == 0
                && mEnableSelectContentLanguageButton.value?.peekContent() == true
            ) {
                mEnableSelectContentLanguageButton.value = SingleEvent(false)
            }
            if (!isSaveLanguageButtonAvailable) {
                selectLanguageListener?.invoke(false)
            }
        }
    }

    fun resetAdapter(){
        fromClassName = null
        _originalSelectedLanguageId.clear()
        mLanguageItemList = mutableListOf()
        mSelectedLanguageItemList = mutableListOf()
        mSelectedLanguageNameList = mutableListOf()
        mEnableSelectContentLanguageButton.value = SingleEvent(false)
    }
}