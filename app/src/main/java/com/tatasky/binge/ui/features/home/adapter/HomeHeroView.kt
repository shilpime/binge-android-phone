package com.tatasky.binge.ui.features.home.adapter

import android.content.Context
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.R
import com.tatasky.binge.analytics.EVENT_VALUE_RAIL_HB
import com.tatasky.binge.analytics.SOURCE_GAMES
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.customviews.MyGallery
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.data.networking.models.response.ProviderLogo
import com.tatasky.binge.databinding.LayoutHomeHeroBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.interfaces.CommonContentViewListener
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.isFullyVisibleOnScreen
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.utils.EventConstants
import com.tatasky.binge.utils.NON_DTH_USER
import com.tatasky.binge.utils.OrientationManager
import com.tatasky.binge.utils.PROVIDER_PRIME
import com.tatasky.binge.utils.SubscriptionPackStatusEnum
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.e
import com.tatasky.binge.utils.getCloudinaryUrl
import com.tatasky.binge.utils.getDisplayMatics
import com.tatasky.binge.utils.getLifecycleOwner
import com.tatasky.binge.utils.isTablet
import com.tatasky.binge.utils.isValidContent
import com.tatasky.binge.utils.updateProviderImage
import java.util.*

class HomeHeroView : FrameLayout {
    private var layout_point: LinearLayout? = null
    private var gallery: MyGallery? = null
    var len: Int = 0
    private lateinit var orientationManager: OrientationManager
    private var dots: ArrayList<ImageView> = ArrayList()
    internal var currentItem = 0// The currently selected viewPager item
    /**
     * Time sliding
     */
    private val slideHandler = Handler()

    /**
     * Slide
     */
    private val slideRun = object : Runnable {

        override fun run() {
            currentItem++
            currentItem = checkPosition(currentItem)
            gallery!!.slide(MyGallery.RIGHT)

            slideHandler.postDelayed(this, 3000)
        }
    }
    private var auto: Boolean = false

    constructor(context: Context) : super(context) {

        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.slide_gallery, this)
        this.layout_point = this.findViewById<View>(R.id.layout_dots) as LinearLayout
        this.gallery = findViewById<View>(R.id.mygallery) as MyGallery
        addSpaceValidation(false)
        this.gallery!!.isSoundEffectsEnabled = false

        this.gallery!!.setListener { startSlide(false) }
    }
    fun reset() {
        layout_point!!.removeAllViews()
        dots.clear()
    }

    private var mNonSubscribedPartnerList = HashSet<String>()
    private var mIsUserLogin  = false
    private var isPackAvailed = false
    private var mSharedPrefs: PrefsRepo? = null
    fun updateSbscriberList(sharedPrefs: PrefsRepo) {
        mSharedPrefs = sharedPrefs
        mIsUserLogin  = sharedPrefs.getLoginStatus()
        isPackAvailed = sharedPrefs.getSubscribedPack() != null &&
                SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPrefs.getSubscribedPack()?.subscriptionStatus, true)
        sharedPrefs.getSubscribedPack()?.nonSubscribedPartnerList?.let { partnerList ->
            e("RailAdapter","partnerList:$partnerList")
            for (partner in partnerList){
                mNonSubscribedPartnerList.add((partner.partnerName ?: "").toLowerCase())
            }
        }
    }

    fun initData(
        item: HomeResponse.Items,
        listener: CommonDTOClickListener?,
        position: Int,
        cloudinaryUrl: String?,
        providerLogos: ProviderLogo,
        hbViewListener: CommonContentViewListener,
        dthStatus: String?,
        initialBannerPosition : Int?,
        sharedPrefs: PrefsRepo,
        contentAnalyticsModel: ContentAnalyticsModel
    ) {
        updateSbscriberList(sharedPrefs)
        var filteredContentItems: List<ContentItem> = ArrayList()
        //CODE REVIEW REQUIRED provider is blank for HB_SEE_ALL TSF-5645
        filteredContentItems = item.contentItem.filter {
            it.provider.isBlank() || !(PROVIDER_PRIME.equals(it.provider, true) && (dthStatus.isNullOrEmpty() || NON_DTH_USER.equals(dthStatus, true))) && isValidContent(it)
        }
        auto = item.isAutoScroll
        len = filteredContentItems.size
        initialBannerPosition?.let {
            e("Hero Position : ", ""+it)
            if(!filteredContentItems.isNullOrEmpty()) {
                val l = filteredContentItems.size
                val x = l - (it % l) + 1
                Collections.rotate(filteredContentItems, x)
            }
        }
        for (j in 0 until len) {
            // Add index
            val imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(dpToPx(context, 11), dpToPx(context, 3))
            dots.add(imageView)
            val tv = TextView(context)
            tv.text = "  "
            if (j == 0) {
                // The default into the program after the first photo is selected;
                dots[j].setBackgroundResource(R.drawable.shp_off_white_rect)
            } else {
                dots[j].setBackgroundResource(R.drawable.shp_white_rect)
            }

            layout_point!!.addView(tv)
            layout_point!!.addView(imageView)
        }
        gallery!!.adapter = ImageAdapter(context, filteredContentItems, cloudinaryUrl,providerLogos, "${item.id}")
//        if (len > 1) gallery!!.setSelection(len * 100)
        gallery!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {
                var position = position
                if (position >= len) {
                    position %= len
                    currentItem = position
                } else {
                    currentItem = position
                }
                if (this@HomeHeroView.isFullyVisibleOnScreen())
                    hbViewListener.onContentViewed(
                        (currentItem + 1).toString(),
                        filteredContentItems[currentItem]
                    )
                selectPage()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        gallery!!.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                var position = position
                if (position >= len) {
                    position %= len
                }

                listener?.onSubItemClick(
                    filteredContentItems[position],
                    position,
                    0,
                    EventConstants.TYPE_HERO,
                    null,
//                    listOf(
//                        Pair(
//                            view.findViewById(R.id.aiv_layout_home_hero_banner),
//                            ViewCompat.getTransitionName(view.findViewById(R.id.aiv_layout_home_hero_banner))?:""
//                        )
//                    ),
                    item.title,
                    gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                        pageName = SOURCE_GAMES,
                        railTitle = item.title,
                        railType = com.tatasky.binge.analytics.EDITORIAL,
                        railCategory = EVENT_VALUE_RAIL_HB,
                        railPosition = "0",
                        gameGenre = filteredContentItems[position].getSubTitle(),
                        gamePartner = filteredContentItems[position].provider,
                        gamePosition = "${position+1}",
                        gameRating = filteredContentItems[position].gameRating,
                        releaseYear = filteredContentItems[position].releaseYear?:"",
                        source = SOURCE_GAMES
                    ),
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        setSelectedPosition()
    }

    inner class ImageAdapter internal constructor(
        private val context: Context?,
        private val iList: List<ContentItem>,
        private val cloudinaryUrl: String?,
        val providerLogos: ProviderLogo,
        val railId : String
    ) :
        BaseAdapter() {
        private val mInflater: LayoutInflater = LayoutInflater.from(context)
        private val len: Int = iList.size

        init {
            //            this.listener = listener;
        }

        override fun getCount(): Int {
            return if (len > 0) {
                Integer.MAX_VALUE
            } else {
                0
            }
        }

        override fun getItem(position: Int): Any {
            var position = position
            if (position >= len) {
                position %= len
            }

            return iList[position]
        }

        override fun getItemId(position: Int): Long {
            var position = position
            if (position >= len) {
                position %= len
            }
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var position = position
            var convertView = convertView
            //            View view = convertView;
            if (context != null) {
                var heroBinding = LayoutHomeHeroBinding.inflate(mInflater, null, false)
                if (position >= len) {
                    position %= len
                }
                val item = getItem(position) as ContentItem

                heroBinding.tempid.text = railId

                var width = getDisplayMatics().widthPixels
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    width = getDisplayMatics().heightPixels
                }
                if (convertView == null) {
                    convertView = heroBinding.root
                    heroBinding.aivLayoutHomeHeroBanner.layoutParams =
                        RelativeLayout.LayoutParams(
                            (width * 0.9999).toInt() + 1,
                            (width.toDouble() * 0.9999 * 0.56).toInt()
                        )
                    heroBinding.imgOverlay.layoutParams = RelativeLayout.LayoutParams(
                        (width * 0.9999).toInt() + 1,
                        (width.toDouble() * 0.9999 * 0.56).toInt()
                    )
                    heroBinding.aivLayoutHomeHeroBanner.setOnTouchListener { v, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            //                          iv.setAlpha(70);
                            slideHandler.removeCallbacks(slideRun)
                        }
                        false
                    }
                    convertView.tag = heroBinding
                } else {
                    heroBinding = convertView.tag as LayoutHomeHeroBinding
                }


                val url: String

                val imageUrl = if(item.appImageBM.isNullOrEmpty())  item.image else item.appImageBM
//                if (istablet(context)) {
//                    val width = getdisplaymatics().widthpixels
//
//                    url = getcloudinaryurl(
//                        cloudinaryurl,
//                        width, (width * 0.56).toint(),
//                        imageurl
//                    )
//                } else {
                url = getCloudinaryUrl(
                    cloudinaryUrl,
                    width, (width * 0.56).toInt(),
                    imageUrl
                )
//                }
                heroBinding.aivLayoutHomeHeroBanner.transitionName = item.id + "image"
                heroBinding.imgUrl = url
                heroBinding.provider = item.provider
                updateProviderImage(
                    heroBinding.ivLayoutHomeHeroChannelIcon,
                    item.provider,
                    providerLogos,
                    R.drawable.ic_banner_placeholder
                )
//                item.isPartnerSubscribed = isPackAvailed &&
//                        !mNonSubscribedPartnerList.contains(item.provider.toLowerCase())
                item.isPartnerSubscribed = mSharedPrefs?.getSubscribedPack() != null &&
                        SubscriptionPackStatusEnum.ACTIVE.status.equals(mSharedPrefs?.getSubscribedPack()?.subscriptionStatus, true) &&
                        (mNonSubscribedPartnerList?.contains(item.provider.toLowerCase()) == false)
                e("RailAdapter","mNonSubscribedPartnerList:$mNonSubscribedPartnerList," +
                        "getItemViewType(position): ${getItemViewType(position)}, " +
                        "contentItem.isPartnerSubscribed: ${item.isPartnerSubscribed}," +
                        " contentItem.partnerId: ${item.provider}")
                item.isGuestUser = !mIsUserLogin
                heroBinding.contentItem = item
//                if(item.containsCrown())
//                    heroBinding.ivCrownSmall.visibility = View.VISIBLE
//                else
//                    heroBinding.ivCrownSmall.visibility = View.GONE
            }
            return convertView!!
        }
    }

    /**
     * Start sliding
     */
    fun startSlide(frmZero: Boolean) {
        if (auto && len > 1) {
            if (frmZero) gallery!!.setSelection(len * 100)
            slideHandler.removeCallbacks(slideRun)
            slideHandler.postDelayed(slideRun, 3000)
        }
    }

    fun stopSlide() {
        slideHandler.removeCallbacksAndMessages(null)
    }

    fun checkPosition(position: Int): Int {
        var position = position
        if (len in 1..position) {
            position = position % len
        }

        return position
    }

    fun restartSlider(){
        slideHandler.removeCallbacks(slideRun)
        if (auto && len > 1) {
            slideHandler.postDelayed(slideRun, 3000)
        }
    }

    fun setSelectedPosition(){
        currentItem = checkPosition(0)
        if (len > 1) gallery!!.setSelection(len * 100)
        restartSlider()
        startSlide(false)
    }

    /**
     * Sets the currently selected pages
     */
    private fun selectPage() {
        /** Set the currently displayed page  */
        for (i in 0 until len) {
            dots[currentItem].setBackgroundResource(R.drawable.shp_off_white_rect)
            if (currentItem != i) {
                dots[i].setBackgroundResource(R.drawable.shp_white_rect)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startSlide(false)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        slideHandler.removeCallbacks(slideRun)
        context?.let {
            if (isTablet(it)) {
                if (::orientationManager.isInitialized)
                    orientationManager.disable()
            }
        }
    }

    fun addSpaceValidation(isNotifyImageAdapter: Boolean){
        if (isTablet(context) && (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            this.gallery?.setSpacing(20)
            findViewById<View>(R.id.space1).show()
            findViewById<View>(R.id.space2).show()
            findViewById<View>(R.id.space3).show()
        } else {
            this.gallery?.setSpacing(-1)
            findViewById<View>(R.id.space1).hide()
            findViewById<View>(R.id.space2).hide()
            findViewById<View>(R.id.space3).hide()
        }

        if(isNotifyImageAdapter){
            gallery?.let { mygallery ->
               (mygallery.adapter as ImageAdapter).notifyDataSetChanged()
            }
        }

    }
}
