package com.tatasky.binge.ui.features.prime.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.analytics.NO
import com.tatasky.binge.analytics.PLATFORM_ANDROID
import com.tatasky.binge.analytics.YES
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.CWRequest
import com.tatasky.binge.data.networking.models.requests.PrimeActivationRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.player.PlayerAnalytics
import com.tatasky.binge.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by Srikant on 27/05/21.
 */
class PrimeViewModel @Inject constructor(private val sharedPrefs: PrefsRepo, private val useCase: CommonUseCase, private val playerAnalytics: PlayerAnalytics) :
	BaseViewModel() {

	var source: String? = null
	private val _primeActivationResponse = MutableLiveData<SingleEvent<PrimeActivationResponse>>()
	private val _rechargeResponse = MutableLiveData<SingleEvent<RechargeResponse>>()
	private val _primeInterstitialResponse = MutableLiveData<SingleEvent<PrimeInterstitialResponse>>()
	private val _primeResumeSuccess = MutableLiveData<SingleEvent<BaseResponse>>()
	var firstError = false

	fun getPrimeInterstitialResponse(): LiveData<SingleEvent<PrimeInterstitialResponse>> = _primeInterstitialResponse
	fun rechargeResponse(): LiveData<SingleEvent<RechargeResponse>> = _rechargeResponse
	fun getPrimeActivationResponse(): LiveData<SingleEvent<PrimeActivationResponse>> = _primeActivationResponse
	fun getPrimeResumeResponse(): LiveData<SingleEvent<BaseResponse>> = _primeResumeSuccess

	fun activatePrime(primePack: String) {
		setProgressing(true)
		val disposable =
			useCase.activatePrime(PrimeActivationRequest(primePack, sharedPrefs.getOriginalSubscriberId()))
				.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
				.subscribeWith(object : CallbackWrapper<PrimeActivationResponse>() {
					override fun onSuccessResponse(t: PrimeActivationResponse) {
						setProgressing(false)
						_primeActivationResponse.postValue(SingleEvent(t))
					}

					override fun onError(error: ErrorModel?) {
						setProgressing(false)
						firstError = false
						setError(error)
					}

					override fun onSubscribe(d: Disposable) {
						super.onSubscribe(d)
						addDisposable(d)
					}
				})
	}

	@SuppressLint("CheckResult")
	fun fetchInterstitialScreenData() {
		setProgressing(true)
		useCase.fetchPrimePackList()
			.subscribeOn(Schedulers.io())
			.zipWith(useCase.fetchInterstitialPageResponse()
				.subscribeOn(Schedulers.io()), BiFunction<PrimePackListResponse, PrimeInterstitialResponse, PrimeInterstitialResponse> { t1, t2 -> combineResult(t1, t2) })
			.observeOn(AndroidSchedulers.mainThread())
			.subscribeWith(object : CallbackWrapper<PrimeInterstitialResponse>() {
				override fun onSuccessResponse(t: PrimeInterstitialResponse) {
					setProgressing(false)
					if(t.code == CODE_SUCCESS && !t.packList.isNullOrEmpty()){
						_primeInterstitialResponse.postValue(SingleEvent(t))
					} else {
						firstError = true
						setError(ErrorModel(t.code, COMMON_ERROR_MSG))
					}
				}
				override fun onError(error: ErrorModel?) {
					setProgressing(false)
					firstError = true
					setError(error)
				}

				override fun onSubscribe(d: Disposable) {
					super.onSubscribe(d)
					addDisposable(d)
				}
			})
	}

	fun combineResult(primePackListResponse: PrimePackListResponse, primeInterstitialResponse: PrimeInterstitialResponse): PrimeInterstitialResponse {
		return primeInterstitialResponse.apply {
			this.code = primePackListResponse.code + primeInterstitialResponse.code
			this.packList = primePackListResponse.data
		}
	}
	fun getMaximumRechargeAmount() = sharedPrefs.getMaxRechargeAmount()

	@SuppressLint("CheckResult")
	fun startRecharge(source: String? = null /*Source from where the user initiated recharge*/, amount: String? = null) {
		setProgressing(true)
		useCase.initiateRecharge(sharedPrefs.getOriginalSubscriberId())
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribeWith(object : CallbackWrapper<RechargeResponse>() {
				override fun onSuccessResponse(t: RechargeResponse) {
					setProgressing(false)
					when (t.code) {
						CODE_SUCCESS -> {
							_rechargeResponse.postValue(SingleEvent(t))
						}
						else -> {
							setError(ErrorModel(message = t.message))
						}
					}
				}

				override fun onError(errorModel: ErrorModel?) {
					setProgressing(false)
					setError(errorModel)
				}

				override fun onSubscribe(d: Disposable) {
					super.onSubscribe(d)
					addDisposable(d)
				}
			})
	}

	fun subscribedPack() = sharedPrefs.getSubscribedPack()

	fun actionCW(contentItem: ContentItem, watchDuration: Int, durationInSeconds: Int) {
		if(contentItem.contentType.contains(TYPE_BRAND, true) || contentItem.contentType.contains(TYPE_SERIES, true))
			return
		var watchDuration = watchDuration
		if(watchDuration == 0) watchDuration = 1
		val request = CWRequest(
			sharedPrefs.getOriginalSubscriberId(),
			sharedPrefs.getProfileId()?:"",
			contentType = contentItem.contentType,
			contentId = contentItem.id,
			watchDuration = watchDuration,
			totalDuration = try {
				val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
				val reference: Date? = dateFormat.parse("00:00:00")
				val date: Date? = dateFormat.parse(contentItem.duration)
				var seconds: Long = ((date?.time?:0L) - (reference?.time?:0L))/1000L //durationInSeconds
				if(seconds==0L)
					throw Exception()
				seconds.toInt()
			} catch (e:Exception){
				durationInSeconds
			},
			uniqueId = sharedPrefs.getAnonymousId()?:""
		)
		val contentAuth=isFreeContent(contentItem.contractName,sharedPrefs.getPartnerIdsList(),contentItem.partnerId?:"",sharedPrefs.getSubscribedPack()?.subscriptionStatus) || !PREMIUM.equals(contentItem.partnerSubscriptionType,true)

        //TODO Commenting this as pr TP requirement for PV TSF-17739
/*
        playerAnalytics.trackPlayContent(
            title = contentItem.title,
            genre = contentItem.genres,
            type = contentItem.contentType,
            partnerName = contentItem.provider,
            rail = contentItem.railName,
            origin = contentItem.origin.toUpperCase(),
            source = contentItem.source.takeIf { it.isNotEmpty() } ?: "Deeplink",
            language = contentItem.language,
            pack = sharedPrefs.getSubscribedPack(),
            railPosition = contentItem.railPosition,
            contractName = contentItem.contractName,
            parentTitle = "",
            isFreeContent = contentItem.partnerSubscriptionType?.contains(PREMIUM, true) == false,
            pageName = contentItem.source,
            railType = contentItem.origin,
            railCategory = contentItem.railCategory,
            contentLanguagePrimary = contentItem?.language?.getOrNull(0),
            contentGenrePrimary = contentItem?.genres?.getOrNull(0),
            contentAuth = if (contentAuth) YES else NO,
            contentCategory = contentItem.contentType,
            contentPosition = contentItem.contentPosition.toString(),
            contentRating = contentItem.masterRating,
            deviceType = PLATFORM_ANDROID,
            actors = emptyList(),
            autoPlayed = NO,
            liveContent = NO,
            contentConfigType = contentItem.contentConfigType
        )
*/

		if(contentItem.partnerSubscriptionType?.contains(PREMIUM, true) == false){
			if(!sharedPrefs.getFirstFreeContentPlay()){
				playerAnalytics.trackFirstFreeContentPlay(
					contentItem.title ,
					contentItem.contentType,
					contentItem.provider
				)
				sharedPrefs.saveFirstFreeContentPlay()
			}
		} else {
			if(!sharedPrefs.getFirstPremiumContentPlay()){
				playerAnalytics.trackFirstPremiumContentPlay(
					contentItem.title ,
					contentItem.contentType,
					contentItem.provider
				)
				sharedPrefs.saveFirstPremiumContentPlay()
			}
		}

		val disposable = useCase.getCWAction(request)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(
				{
					//                    relatedSeries.value = SingleEvent(it)
					//                    fetchingSeries.postValue(false)
				},
				{

				}
			)
		addDisposable(disposable)
	}

	fun resumePrime() {
		setProgressing(true)
		val disposable =
			useCase.requestPrimeResume()
				.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
				.subscribeWith(object : CallbackWrapper<BaseResponse>() {
					override fun onSuccessResponse(t: BaseResponse) {
						setProgressing(false)
						_primeResumeSuccess.postValue(SingleEvent(t))
					}

					override fun onError(error: ErrorModel?) {
						setProgressing(false)
						_primeResumeSuccess.postValue(SingleEvent(BaseResponse().apply { this.code = -1 }))
						setError(error)
					}

					override fun onSubscribe(d: Disposable) {
						super.onSubscribe(d)
						addDisposable(d)
					}
				})
	}
}
