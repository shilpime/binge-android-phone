package com.tatasky.binge.ui.features.home.bottomsheet.categories


import android.content.Context
import android.view.MotionEvent
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.LeftMenuItem
import com.tatasky.binge.databinding.FragmentCategoriesBottomSheetDialogBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseBottomSheetDialogFragment
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import dagger.android.support.AndroidSupportInjection

class CategoriesBottomSheetDialog(private val mCategoriesList: List<LeftMenuItem>) :
    BaseBottomSheetDialogFragment<FragmentCategoriesBottomSheetDialogBinding, CommonSampleViewModel>(
        true) {
    private var mDisableCategoriesRecyclerListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            if (mBottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                return true
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }

    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }


    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_categories_bottom_sheet_dialog

    override fun getViewModelOwner(): ViewModelStoreOwner = requireActivity()

    override fun setObserver() {

    }

    override fun toBeCalledOnce() {
        binding.vm = viewModel
        binding.tvClose.setOnClickListener {
            dismiss()
        }
        viewModel.setCategoryItemList(mCategoriesList)
        binding.rvCategoryListing.addOnItemTouchListener(mDisableCategoriesRecyclerListener)
    }


}
