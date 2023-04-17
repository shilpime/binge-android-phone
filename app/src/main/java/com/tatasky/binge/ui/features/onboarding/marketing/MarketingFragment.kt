package com.tatasky.binge.ui.features.onboarding.marketing

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_APP_LAUNCH
import com.tatasky.binge.analytics.SOURCE_MARKETING
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.MarketingResponseList
import com.tatasky.binge.databinding.FragmentMarketingBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.utils.isNetworkConnected
import java.util.*

class MarketingFragment : BaseFragment<FragmentMarketingBinding, MarketingViewModel>() {
    private var totalScreens:Int? = 0
    private var autoScrollTime:Int = 3000
    private var mHandler = Handler(Looper.getMainLooper())
    private var currentScreen = 0

    override fun getViewModelClass(): Class<MarketingViewModel> = MarketingViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_marketing

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun onError(errorModel: ErrorModel) {
        binding.marketingFragmentParent.hide()
        moveToLogin()
    }

    override fun onNetworkError(errorMessage: String, isRetry: Boolean) {
        binding.noNetwork.show()
        binding.btnRetry.setOnClickListener {
            if (isNetworkConnected(requireContext())) {
                binding.noNetwork.hide()
//                viewModel.retrySubject.onNext(Any())
                viewModel.getMarketingResponse()
            }
        }
    }

    override fun setObserver() {
        viewModel.getMarketingList().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                binding.marketingFragmentParent.show()
                totalScreens = response.data?.list?.size
                response.data?.list?.let { it -> setAdapter(it) }
                totalScreens?.let { it -> if(it!=1) binding.pageIndicator.setTotalPages(it) else binding.pageIndicator.hide()}
                autoScrollTime = response.data?.autoScrollTime?:3000
                setTimer(autoScrollTime.toLong())
            }
        })
    }
    private fun setTimer(autoScrollTime:Long){
        val update = Runnable {
            binding.marketingScreenVp.setCurrentItem(currentScreen++, true)
        }
        Timer().schedule(object : TimerTask() {
            // task to be scheduled
            override fun run() {
                mHandler.post(update)
            }
        }, 100, autoScrollTime)
    }

    private fun setAdapter(marketingResponseList: List<MarketingResponseList>) {
        binding.marketingScreenVp.adapter = MarketingAdapter(marketingResponseList,sharedPrefs.getCloudenieryUrl())
    }
    private fun moveToLogin(){
        activity?.let {
            val intent = Intent(it, LandingActivity::class.java)
            intent.putExtra("source", SOURCE_MARKETING)
            intent.data = it.intent.data
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            it.startActivity(intent)
            it.finish()
        }
    }

    override fun toBeCalledOnce() {

        viewModel.getMarketingResponse()

        binding.marketingScreenVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentScreen = position
                binding.pageIndicator.setSelected((position % totalScreens!!)+1)
            }

            var oldPosition = 0
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if ((position < oldPosition) && (position % totalScreens!! == totalScreens!!-1)) {
                    binding.marketingScreenVp.setCurrentItem(oldPosition, false)
                } else {
                    oldPosition = position
                }
            }

        })


        binding.marketingScreenVp.apply {
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        binding.btnNext.setOnClickListener {
            moveToLogin()
        }
    }

}