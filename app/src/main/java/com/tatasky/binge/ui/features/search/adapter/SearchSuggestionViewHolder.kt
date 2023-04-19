package com.tatasky.binge.ui.features.search.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.databinding.LayoutSearchSuggestionGenreItemBinding
import com.tatasky.binge.databinding.LayoutSearchSuggestionItemBinding
import com.tatasky.binge.databinding.LayoutSearchSuggestionKeywordItemBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.SuggestionSuggestors
import com.tatasky.binge.ui.features.search.model.SearchViewModel
import com.tatasky.binge.utils.ProvidersCache
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.getAppForProvider
import com.tatasky.binge.utils.getCloudinaryUrl
import com.tatasky.binge.utils.getCloudinaryUrlByWidthOrHeight

sealed class SearchSuggestionViewHolder(binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    class SuggestionDictionaryViewHolder(private val binding: LayoutSearchSuggestionKeywordItemBinding) :
        SearchSuggestionViewHolder(binding) {

        private fun checkCommonText(queryText: String, suggestedText: String): SpannableString {
            val spannable = SpannableString(suggestedText)
            val indexStart = suggestedText.indexOf(queryText, ignoreCase = true)
            val indexEnd = indexStart + queryText.length
            if (indexStart != -1) {

                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return spannable
            } else {
                return SpannableString(suggestedText)
            }
        }

        fun bind(suggestedItem: ContentItem, searchViewModel: SearchViewModel) {
            binding.apply {
                suggestion = suggestedItem
                vm = searchViewModel
                tvDicSearch.text = checkCommonText(searchViewModel.searchQuery, suggestedItem.title)
            }
        }
    }

    class SuggestionContentViewHolder(private val binding: LayoutSearchSuggestionItemBinding) :
        SearchSuggestionViewHolder(binding) {

        private fun checkCommonText(queryText: String, suggestedText: String): SpannableString {
            val spannable = SpannableString(suggestedText)
            val indexStart = suggestedText.indexOf(queryText, ignoreCase = true)
            val indexEnd = indexStart + queryText.length
            if (indexStart != -1) {

                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return spannable
            } else {
                return SpannableString(suggestedText)
            }
        }

        fun bind(suggestedItem: ContentItem, searchViewModel: SearchViewModel) {

            binding.apply {
                suggestion = suggestedItem
                vm = searchViewModel
                ivContentImage.context?.let{ ctx->
                    val width = dpToPx(
                        ctx,
                        ctx.resources.getDimension(
                            R.dimen._92sdp
                        ).toInt()
                    )
                    val height = dpToPx(
                        ctx,
                        ctx.resources.getDimension(
                            R.dimen._52sdp
                        ).toInt()
                    )
                    when (suggestedItem.suggestor.lowercase()) {
                        SuggestionSuggestors.ProviderSuggestor.name.lowercase() -> {
                            val appName = getAppForProvider(
                                suggestedItem.provider,
                                searchViewModel.sharedPrefs.getProviderLogo()
                            )
                            var url = appName?.logoCircular
                            if (appName == null) {
                                url =
                                    searchViewModel.sharedPrefs.getCloudenieryUrl() + ProvidersCache.availableProviders[suggestedItem.provider.lowercase()]?.logoCircular
                            }
                            url?.let {
                                transparentImageLoad(ivProviderImage, url)
                            }
                        }
                        SuggestionSuggestors.LanguageSuggestor.name.lowercase() -> {
                            if (!suggestedItem.backgroundImage.isNullOrEmpty()) {
                                val url = getCloudinaryUrl(
                                    searchViewModel.sharedPrefs.getCloudenieryUrl(),
                                    width, height,
                                    suggestedItem.backgroundImage
                                )
                                imageLoad(ivContentImage, url)
                                transparentImageLoad(ivLangIcon, suggestedItem.image)
                            } else {
                                val url = getCloudinaryUrl(
                                    searchViewModel.sharedPrefs.getCloudenieryUrl(),
                                    width, height,
                                    suggestedItem.image
                                )
                                imageLoad(ivContentImage, url)
                            }
                        }
                        else -> {
                            val url = getCloudinaryUrl(
                                searchViewModel.sharedPrefs.getCloudenieryUrl(),
                                width, height,
                                suggestedItem.image
                            )
                            imageLoad(ivContentImage, url)
                        }
                    }

                }
                tvTitle.text = checkCommonText(searchViewModel.searchQuery,suggestedItem.title)
            }

        }
    }



    class SuggestionGenreViewHolder(private val binding: LayoutSearchSuggestionGenreItemBinding) :
        SearchSuggestionViewHolder(binding) {
        private fun checkCommonText(queryText: String, suggestedText: String): SpannableString {
            val spannable = SpannableString(suggestedText)
            val indexStart = suggestedText.indexOf(queryText, ignoreCase = true)
            val indexEnd = indexStart + queryText.length
            if (indexStart != -1) {

                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return spannable
            } else {
                return SpannableString(suggestedText)
            }
        }

        fun bind(suggestedItem: ContentItem, searchViewModel: SearchViewModel) {
            binding.apply {
                suggestion = suggestedItem
                vm = searchViewModel
                val url = getCloudinaryUrlByWidthOrHeight(
                    searchViewModel.sharedPrefs.getCloudenieryUrl(),
                    suggestedItem.getLangGenreIcon(ItemViewType.GENRE.name),
                    height = 46
                )
                transparentImageLoad(img, url)
                tvTitle.text = checkCommonText(searchViewModel.searchQuery, suggestedItem.title)
            }
        }
    }

}
